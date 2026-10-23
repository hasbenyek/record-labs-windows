package com.recordlabs.desktop.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.Base64
import kotlin.concurrent.thread

data class WindowsPlaybackSnapshot(
    val trackId: String? = null,
    val isPlaying: Boolean = false,
    val isReady: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null,
)

/**
 * Controller for the bundled Windows Media Foundation helper.
 * Commands are serialized off the Compose UI thread and query values are URL encoded.
 *
 * On Linux there is no Windows helper: the same commands go to
 * [VlcPlayerBackend] inside this process, which answers with the same status
 * JSON, so everything below behaves the same on both systems.
 */
class WindowsMediaPlayer {
    @Volatile var isPlaying: Boolean = false
        private set
    @Volatile var isReady: Boolean = false
        private set
    @Volatile var currentTrack: Track? = null
        private set
    @Volatile var positionMs: Long = 0L
        private set
    @Volatile var durationMs: Long = 0L
        private set
    @Volatile var lastError: String? = null
        private set

    var onPlaybackEnded: (() -> Unit)? = null
    var onMediaCommand: ((String) -> Unit)? = null
    var onBluetoothConnectPacket: ((String) -> Unit)? = null

    private val _state = MutableStateFlow(WindowsPlaybackSnapshot())
    val state: StateFlow<WindowsPlaybackSnapshot> = _state.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(750, TimeUnit.MILLISECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .callTimeout(3, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val json = Json { ignoreUnknownKeys = true }
    private val lifecycleLock = Any()
    private val commandVersion = AtomicLong()
    private val commandExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "record-labs-native-player-commands").apply { isDaemon = true }
    }

    /** In-process engine for systems without the Windows helper (Linux). Null on Windows. */
    private val inProcessBackend: VlcPlayerBackend? = if (Platform.isWindows) null else VlcPlayerBackend()

    /** Media keys/desktop controls on Linux (MPRIS) report through the same channel as the Windows helper. */
    internal fun emitExternalCommand(command: String) {
        inProcessBackend?.emitExternalCommand(command)
    }

    @Volatile private var process: Process? = null
    @Volatile private var port = 0
    @Volatile private var pollingThread: Thread? = null
    @Volatile private var isPendingPlay = false
    @Volatile private var desiredPlaying = false
    @Volatile private var endedNotified = false
    @Volatile private var released = false
    @Volatile private var desiredVolume = 1f
    @Volatile var playbackTempo = 1f
        private set
    @Volatile var pitchSemitones = 0
        private set
    @Volatile private var lastExternalCommandSequence = 0L

    init {
        pollingThread = thread(isDaemon = true, name = "record-labs-native-player-status") {
            while (!Thread.currentThread().isInterrupted && !released) {
                if (process?.isAlive == true || inProcessBackend?.isStarted == true) pollStatus()
                try {
                    // While a new source is opening, notice "ready"/first position
                    // quickly so the loading state clears as soon as audio starts.
                    Thread.sleep(if (isPendingPlay && !isReady && lastError == null) 60L else 250L)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
        commandExecutor.execute { ensureStarted() }
    }

    private fun ensureStarted(): Boolean = synchronized(lifecycleLock) {
        if (released) return@synchronized false
        inProcessBackend?.let { backend ->
            if (backend.start()) {
                lastError = null
                return@synchronized true
            }
            fail("Pemutar audio (VLC) tidak bisa dijalankan. Instal ulang Record Labs.")
            return@synchronized false
        }
        if (port > 0 && process?.isAlive == true && request("status", targetPort = port, failSilently = true) != null) {
            return@synchronized true
        }

        runCatching { process?.destroyForcibly() }
        process = null
        port = 0

        val exeFile = nativePlayerExecutable()
        if (exeFile == null) {
            fail("Native player executable tidak ditemukan. Instal ulang Record Labs.")
            return@synchronized false
        }

        val selectedPort = runCatching { ServerSocket(0).use { it.localPort } }.getOrElse {
            fail("Tidak bisa menyiapkan port player: ${it.message}")
            return@synchronized false
        }

        try {
            val startedProcess = ProcessBuilder(
                exeFile.absolutePath,
                selectedPort.toString(),
                ProcessHandle.current().pid().toString(),
            )
                .directory(exeFile.parentFile)
                .redirectErrorStream(true)
                .start()
            process = startedProcess
            port = selectedPort

            thread(isDaemon = true, name = "record-labs-native-player-log") {
                runCatching {
                    startedProcess.inputStream.bufferedReader().useLines { lines ->
                        lines.forEach { line ->
                            if (line.isNotBlank() && line != "ready") println("NativePlayer: $line")
                        }
                    }
                }
            }

            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(6)
            while (startedProcess.isAlive && System.nanoTime() < deadline) {
                if (request("status", targetPort = selectedPort, failSilently = true) != null) {
                    lastError = null
                    return@synchronized true
                }
                Thread.sleep(50)
            }

            runCatching { startedProcess.destroyForcibly() }
            process = null
            port = 0
            fail("Player native Windows tidak merespons saat dijalankan.")
            false
        } catch (error: Exception) {
            process = null
            port = 0
            fail("Gagal menjalankan player native: ${error.message}")
            false
        }
    }

    private fun nativePlayerExecutable(): File? {
        val resourceDir = System.getProperty("compose.application.resources.dir")
        val candidates = buildList {
            if (!resourceDir.isNullOrBlank()) add(File(resourceDir, "RecordLabs.NativePlayer.exe"))
            add(File("native-player/bin/Release/net8.0-windows10.0.22000.0/win-x64/publish/RecordLabs.NativePlayer.exe"))
            add(File("native-player/bin/Release/net8.0-windows10.0.22000.0/win-x64/RecordLabs.NativePlayer.exe"))
            add(File("windows-recordlabs/native-player/bin/Release/net8.0-windows10.0.22000.0/win-x64/publish/RecordLabs.NativePlayer.exe"))
            add(File("windows-recordlabs/native-player/bin/Release/net8.0-windows10.0.22000.0/win-x64/RecordLabs.NativePlayer.exe"))
            add(File("src/appResources/windows-x64/RecordLabs.NativePlayer.exe"))
        }
        return candidates.map(File::getAbsoluteFile).firstOrNull(File::isFile)
    }

    private fun pollStatus() {
        val body = request("status", failSilently = true) ?: return
        runCatching {
            val state = json.decodeFromString<JsonObject>(body)
            val reportedPlaying = state["IsPlaying"]?.jsonPrimitive?.booleanOrNull ?: false
            isReady = state["IsReady"]?.jsonPrimitive?.booleanOrNull ?: false
            positionMs = state["PositionMs"]?.jsonPrimitive?.longOrNull ?: positionMs
            state["DurationMs"]?.jsonPrimitive?.longOrNull
                ?.takeIf { it > 0 }
                ?.let { durationMs = it }

            val nativeError = state["Error"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)
            val ended = state["Ended"]?.jsonPrimitive?.booleanOrNull ?: false
            val externalSequence = state["ExternalCommandSequence"]?.jsonPrimitive?.longOrNull ?: 0L
            if (externalSequence > lastExternalCommandSequence) {
                lastExternalCommandSequence = externalSequence
                state["ExternalCommand"]?.jsonPrimitive?.contentOrNull
                    ?.takeIf(String::isNotBlank)?.let { onMediaCommand?.invoke(it) }
            }
            state["BluetoothPackets"]?.jsonArray?.forEach { packet ->
                packet.jsonPrimitive.contentOrNull?.takeIf(String::isNotBlank)
                    ?.let { onBluetoothConnectPacket?.invoke(it) }
            }
            when {
                nativeError != null -> {
                    isReady = false
                    isPendingPlay = false
                    desiredPlaying = false
                    isPlaying = false
                    lastError = nativeError
                }
                ended && !isPendingPlay -> {
                    desiredPlaying = false
                    isPlaying = false
                    if (!endedNotified) {
                        endedNotified = true
                        onPlaybackEnded?.invoke()
                    }
                }
                else -> {
                    if (reportedPlaying) isPendingPlay = false
                    // Paused tracks retain their position, so position > 0 must not imply playback.
                    isPlaying = desiredPlaying && (reportedPlaying || isPendingPlay)
                }
            }
            publishState()
        }
    }

    fun play(track: Track, url: String, startPositionMs: Long = 0L) {
        val version = commandVersion.incrementAndGet()
        currentTrack = track
        positionMs = startPositionMs.coerceAtLeast(0L)
        durationMs = track.durationMs
        lastError = null
        isReady = false
        endedNotified = false
        desiredPlaying = true
        isPendingPlay = true
        isPlaying = true
        publishState()

        commandExecutor.execute {
            if (version != commandVersion.get() || !ensureStarted()) return@execute
            try {
                request("play", mapOf("url" to url, "positionMs" to positionMs.toString())) ?: throw IOException("player tidak merespons")
                // A newly created Media Foundation source resets a few session
                // properties on some Windows builds. Re-apply the user's volume
                // after every source change instead of only when the slider moves.
                request("volume", mapOf("value" to desiredVolume.toString()), failSilently = true)
                request(
                    "tuning",
                    mapOf("tempo" to playbackTempo.toString(), "pitch" to pitchSemitones.toString()),
                    failSilently = true,
                )
            } catch (error: Exception) {
                if (version == commandVersion.get()) fail("Gagal memutar lagu: ${error.message}")
            }
        }
    }

    fun pause() {
        desiredPlaying = false
        isPendingPlay = false
        isPlaying = false
        publishState()
        commandExecutor.execute {
            runCatching { request("pause") ?: throw IOException("player tidak merespons") }
                .onFailure {
                    lastError = "Gagal menjeda lagu: ${it.message}"
                    publishState()
                }
        }
    }

    fun resume() {
        if (currentTrack == null) return
        lastError = null
        desiredPlaying = true
        isPendingPlay = true
        isPlaying = true
        publishState()
        commandExecutor.execute {
            if (!ensureStarted()) return@execute
            runCatching { request("resume") ?: throw IOException("player tidak merespons") }
                .onFailure { fail("Gagal melanjutkan lagu: ${it.message}") }
            request("volume", mapOf("value" to desiredVolume.toString()), failSilently = true)
        }
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceAtLeast(0L)
        this.positionMs = target
        publishState()
        commandExecutor.execute {
            runCatching { request("seek", mapOf("positionMs" to target.toString())) }
                .onFailure {
                    lastError = "Gagal menggeser posisi lagu: ${it.message}"
                    publishState()
                }
        }
    }

    /** Start-up values remembered from last time; applied with the first song (no player command yet). */
    fun presetState(volume: Float, tempo: Float, pitch: Int) {
        desiredVolume = volume.coerceIn(0f, 1f)
        playbackTempo = tempo.coerceIn(0.5f, 2f)
        pitchSemitones = pitch.coerceIn(-12, 12)
    }

    fun setPlaybackRate(rate: Float) {
        setAudioTuning(rate, pitchSemitones)
    }

    fun setAudioTuning(tempo: Float, pitch: Int) {
        val safeTempo = tempo.coerceIn(0.5f, 2f)
        val safePitch = pitch.coerceIn(-12, 12)
        playbackTempo = safeTempo
        pitchSemitones = safePitch
        commandExecutor.execute {
            runCatching {
                request(
                    "tuning",
                    mapOf("tempo" to safeTempo.toString(), "pitch" to safePitch.toString()),
                ) ?: throw IOException("player tidak merespons")
            }
                .onFailure {
                    lastError = "Gagal mengubah tempo/pitch: ${it.message}"
                    publishState()
                }
        }
    }

    fun setVolume(volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)
        desiredVolume = safeVolume
        commandExecutor.execute {
            runCatching {
                if (!ensureStarted()) throw IOException("player native belum siap")
                request("volume", mapOf("value" to safeVolume.toString()))
                    ?: throw IOException("player tidak merespons")
            }
                .onFailure {
                    lastError = "Gagal mengubah volume: ${it.message}"
                    publishState()
                }
        }
    }

    fun sendBluetoothConnectPacket(payload: String) {
        val encoded = Base64.getEncoder().encodeToString(payload.toByteArray(Charsets.UTF_8))
        commandExecutor.execute {
            if (ensureStarted()) request(
                "connect-bluetooth-send",
                mapOf("payload" to encoded),
                failSilently = true,
            )
        }
    }

    fun stop() {
        commandVersion.incrementAndGet()
        desiredPlaying = false
        isPendingPlay = false
        isPlaying = false
        currentTrack = null
        isReady = false
        positionMs = 0L
        durationMs = 0L
        publishState()
        commandExecutor.execute { runCatching { request("stop", failSilently = true) } }
    }

    fun release() {
        if (released) return
        released = true
        commandVersion.incrementAndGet()
        desiredPlaying = false
        isPendingPlay = false
        isPlaying = false
        isReady = false
        pollingThread?.interrupt()
        pollingThread = null
        inProcessBackend?.let { runCatching { it.shutdown() } }
        runCatching { request("exit", failSilently = true) }
        runCatching { process?.waitFor(500, TimeUnit.MILLISECONDS) }
        runCatching { if (process?.isAlive == true) process?.destroyForcibly() }
        process = null
        port = 0
        commandExecutor.shutdownNow()
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }

    private fun request(
        path: String,
        parameters: Map<String, String> = emptyMap(),
        targetPort: Int = port,
        failSilently: Boolean = false,
    ): String? {
        inProcessBackend?.let { backend ->
            if (!backend.isStarted) return null
            return try {
                backend.handle(path, parameters)
            } catch (error: Exception) {
                if (!failSilently) throw IOException(error.message ?: "player tidak merespons", error)
                null
            }
        }
        if (targetPort <= 0) return null
        return try {
            val url = "http://127.0.0.1:$targetPort/$path".toHttpUrl().newBuilder().apply {
                parameters.forEach { (name, value) -> addQueryParameter(name, value) }
            }.build()
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                response.body?.string().orEmpty()
            }
        } catch (error: Exception) {
            if (!failSilently) throw error
            null
        }
    }

    private fun fail(message: String) {
        isPendingPlay = false
        desiredPlaying = false
        isPlaying = false
        lastError = message
        publishState()
        println(message)
    }

    private fun publishState() {
        // "Ready" matters: while a new source is still opening the position is
        // frozen at 0, and the video must not run ahead of silent audio.
        MusicVideoClock.publish(currentTrack?.id, isPlaying && isReady, positionMs, playbackTempo, durationMs)
        _state.value = WindowsPlaybackSnapshot(
            trackId = currentTrack?.id,
            isPlaying = isPlaying,
            isReady = isReady,
            positionMs = positionMs,
            durationMs = durationMs,
            error = lastError,
        )
    }
}
