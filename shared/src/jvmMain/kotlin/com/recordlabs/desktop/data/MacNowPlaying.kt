package com.recordlabs.desktop.data

import com.sun.jna.Callback
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

/**
 * macOS: Record Labs as the Mac's "Now Playing" app.
 *
 * macOS sends the play/pause button of wired earphones (the remote on the cable), the
 * keyboard's media keys (F7 / F8 / F9) and the Now Playing controls in Control Center to
 * the app registered with MPRemoteCommandCenter (MediaPlayer framework). A JVM app is
 * not registered by itself, so this registers it through the Objective-C runtime (JNA):
 * a small Objective-C class is made at run time whose methods call back into Kotlin, and
 * the commands arrive as the same names as everywhere else ("play_pause", "next", …)
 * through [onCommand]. The song's title, artist, length and position go to
 * MPNowPlayingInfoCenter, which is also what makes macOS pick Record Labs for the buttons.
 *
 * Only started on macOS (see WindowsMediaPlayer); any failure leaves the app as before.
 */
internal object MacNowPlaying {
    /** "play_pause", "play", "pause", "next", "previous", "stop" (called on the AWT thread). */
    @Volatile var onCommand: ((String) -> Unit)? = null

    @Volatile private var started = false
    @Volatile private var failed = false

    private val objc by lazy { NativeLibrary.getInstance("objc") }
    private val system by lazy { NativeLibrary.getInstance("System") }
    private val mediaPlayer by lazy { NativeLibrary.getInstance("/System/Library/Frameworks/MediaPlayer.framework/MediaPlayer") }
    private val msgSend by lazy { objc.getFunction("objc_msgSend") }

    // Kept for the life of the app: Objective-C calls these, so they must never be collected.
    private val handlers = mutableListOf<CommandHandler>()
    private val mainQueueTask = MainQueueTask()
    private val pendingOnMain = java.util.concurrent.ConcurrentLinkedQueue<() -> Unit>()

    // What was last given to macOS, so the 4-times-a-second state updates send only changes.
    private var lastTrackId: String? = null
    private var lastPlaying = false
    private var lastDurationMs = 0L
    private var lastPositionMs = 0L
    private var lastSentAtNanos = 0L

    /** Objective-C method body: (id self, SEL _cmd, MPRemoteCommandEvent *event) -> MPRemoteCommandHandlerStatus. */
    private class CommandHandler(val command: String) : Callback {
        @Suppress("unused")
        fun callback(self: Pointer?, selector: Pointer?, event: Pointer?): Long {
            val target = onCommand ?: return 1L // MPRemoteCommandHandlerStatusNoSuchContent
            javax.swing.SwingUtilities.invokeLater { runCatching { target(command) } }
            return 0L // MPRemoteCommandHandlerStatusSuccess
        }
    }

    /** dispatch_function_t: runs what was queued, on the Mac's main thread. */
    private class MainQueueTask : Callback {
        @Suppress("unused")
        fun callback(context: Pointer?) {
            while (true) {
                val work = pendingOnMain.poll() ?: break
                runCatching { work() }
            }
        }
    }

    /** Registers the commands once. Any thread; does nothing off macOS. */
    fun start(onCommand: (String) -> Unit) {
        this.onCommand = onCommand
        if (!Platform.isMac || started || failed) return
        started = true
        onMainThread {
            runCatching { registerCommands() }.onFailure {
                failed = true
                System.err.println("Record Labs: Mac media controls unavailable: ${it.message}")
            }
        }
    }

    /** The song and whether it plays; called with every playback state change (cheap when nothing changed). */
    @Synchronized
    fun update(track: Track?, isPlaying: Boolean, positionMs: Long, durationMs: Long) {
        if (!started || failed) return
        val now = System.nanoTime()
        val trackId = track?.id
        // Where macOS thinks the song is now (it moves the position on by itself while playing).
        val expected = if (lastPlaying) lastPositionMs + (now - lastSentAtNanos) / 1_000_000 else lastPositionMs
        val changed = trackId != lastTrackId || isPlaying != lastPlaying ||
            (durationMs > 0 && durationMs != lastDurationMs) ||
            kotlin.math.abs(positionMs - expected) > 1_500L
        if (!changed) return
        lastTrackId = trackId
        lastPlaying = isPlaying
        lastDurationMs = durationMs
        lastPositionMs = positionMs
        lastSentAtNanos = now
        val title = track?.title.orEmpty()
        val artist = track?.artist.orEmpty()
        val album = track?.album.orEmpty()
        onMainThread {
            runCatching { publish(track != null, title, artist, album, isPlaying, positionMs, durationMs) }
        }
    }

    /**
     * `--self-test-media` (macOS): registers the commands on this thread, calls each
     * Objective-C handler the way macOS does, and reads the Now Playing info back.
     * 0 = every command reached Kotlin and the info was kept.
     */
    fun selfTest(): Int {
        if (!Platform.isMac) { println("MEDIA-TEST skipped (not macOS)"); return 0 }
        val received = java.util.Collections.synchronizedList(mutableListOf<String>())
        onCommand = { received += it }
        registerCommands()
        started = true
        val target = send(cls("RecordLabsRemoteCommandTarget"), "new")
        val names = listOf("play_pause", "play", "pause", "next", "previous", "stop")
        names.forEach { name ->
            val status = msgSend.invoke(Long::class.javaObjectType, arrayOf(target, sel("recordLabs_$name:"), null)) as Long
            println("MEDIA-TEST handler $name -> status $status")
        }
        // The handlers hand the command to the AWT thread; wait for it.
        javax.swing.SwingUtilities.invokeAndWait { }
        val center = send(cls("MPRemoteCommandCenter"), "sharedCommandCenter")
        val toggle = send(center, "togglePlayPauseCommand")
        val enabled = msgSend.invoke(Boolean::class.javaObjectType, arrayOf(toggle, sel("isEnabled"))) as Boolean
        publish(true, "Self test", "Record Labs", "", true, 12_000L, 180_000L)
        val info = send(send(cls("MPNowPlayingInfoCenter"), "defaultCenter"), "nowPlayingInfo")
        val count = info?.let { msgSend.invoke(Long::class.javaObjectType, arrayOf(it, sel("count"))) as Long } ?: 0L
        println("MEDIA-TEST received=${received.toList()} toggleEnabled=$enabled nowPlayingKeys=$count")
        val pass = received.toList() == names && enabled && count >= 4
        println(if (pass) "MEDIA-TEST PASS" else "MEDIA-TEST FAIL")
        return if (pass) 0 else 1
    }

    // ---- Objective-C ----

    private fun registerCommands() {
        mediaPlayer // loads MediaPlayer.framework, so its classes exist
        val nsObject = cls("NSObject")
        val className = "RecordLabsRemoteCommandTarget"
        val targetClass = objcCall("objc_allocateClassPair", nsObject, className, 0L)
            ?: cls(className) // already made (should not happen)
        val commands = listOf(
            "togglePlayPauseCommand" to "play_pause",
            "playCommand" to "play",
            "pauseCommand" to "pause",
            "nextTrackCommand" to "next",
            "previousTrackCommand" to "previous",
            "stopCommand" to "stop",
        )
        for ((_, name) in commands) {
            val handler = CommandHandler(name).also(handlers::add)
            objc.getFunction("class_addMethod").invokeInt(arrayOf(targetClass, sel("recordLabs_$name:"), handler, "q@:@"))
        }
        objc.getFunction("objc_registerClassPair").invokeVoid(arrayOf(targetClass))
        val target = send(send(targetClass, "alloc"), "init")
        val center = send(cls("MPRemoteCommandCenter"), "sharedCommandCenter")
        for ((property, name) in commands) {
            val command = send(center, property) ?: continue
            msgSend.invokeVoid(arrayOf(command, sel("setEnabled:"), true))
            msgSend.invoke(Pointer::class.java, arrayOf(command, sel("addTarget:action:"), target, sel("recordLabs_$name:")))
        }
    }

    private fun publish(hasSong: Boolean, title: String, artist: String, album: String, playing: Boolean, positionMs: Long, durationMs: Long) {
        val infoCenter = send(cls("MPNowPlayingInfoCenter"), "defaultCenter") ?: return
        if (!hasSong) {
            msgSend.invokeVoid(arrayOf(infoCenter, sel("setNowPlayingInfo:"), null))
            msgSend.invokeVoid(arrayOf(infoCenter, sel("setPlaybackState:"), 3L)) // stopped
            return
        }
        val info = send(cls("NSMutableDictionary"), "dictionary") ?: return
        fun put(key: String, value: Pointer?) {
            val keyString = mediaPlayer.getGlobalVariableAddress(key).getPointer(0) ?: return
            if (value != null) msgSend.invokeVoid(arrayOf(info, sel("setObject:forKey:"), value, keyString))
        }
        put("MPMediaItemPropertyTitle", nsString(title))
        put("MPMediaItemPropertyArtist", nsString(artist))
        if (album.isNotBlank()) put("MPMediaItemPropertyAlbumTitle", nsString(album))
        if (durationMs > 0) put("MPMediaItemPropertyPlaybackDuration", nsNumber(durationMs / 1000.0))
        put("MPNowPlayingInfoPropertyElapsedPlaybackTime", nsNumber(positionMs.coerceAtLeast(0L) / 1000.0))
        put("MPNowPlayingInfoPropertyPlaybackRate", nsNumber(if (playing) 1.0 else 0.0))
        msgSend.invokeVoid(arrayOf(infoCenter, sel("setNowPlayingInfo:"), info))
        msgSend.invokeVoid(arrayOf(infoCenter, sel("setPlaybackState:"), if (playing) 1L else 2L)) // playing / paused
    }

    private fun onMainThread(work: () -> Unit) {
        pendingOnMain.add(work)
        runCatching {
            val mainQueue = system.getGlobalVariableAddress("_dispatch_main_q")
            system.getFunction("dispatch_async_f").invokeVoid(arrayOf(mainQueue, null, mainQueueTask))
        }.onFailure { failed = true }
    }

    private fun cls(name: String): Pointer? = objc.getFunction("objc_getClass").invoke(Pointer::class.java, arrayOf(name)) as Pointer?
    private fun sel(name: String): Pointer? = objc.getFunction("sel_registerName").invoke(Pointer::class.java, arrayOf(name)) as Pointer?
    private fun send(receiver: Pointer?, selector: String): Pointer? =
        receiver?.let { msgSend.invoke(Pointer::class.java, arrayOf(it, sel(selector))) as Pointer? }
    private fun objcCall(function: String, vararg args: Any?): Pointer? =
        objc.getFunction(function).invoke(Pointer::class.java, args) as Pointer?
    private fun nsString(value: String): Pointer? =
        msgSend.invoke(Pointer::class.java, arrayOf(cls("NSString"), sel("stringWithUTF8String:"), value)) as Pointer?
    private fun nsNumber(value: Double): Pointer? =
        msgSend.invoke(Pointer::class.java, arrayOf(cls("NSNumber"), sel("numberWithDouble:"), value)) as Pointer?
}
