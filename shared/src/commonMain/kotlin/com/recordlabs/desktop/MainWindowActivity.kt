package com.recordlabs.desktop

import com.recordlabs.desktop.platform.*

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
/**
 * Whether the main window's moving pictures run: the animated artwork / music video
 * picture and the moving colours of the player and lyrics pages' background.
 *
 * They stop while the main window is minimized, and on the desktop (Settings >
 * Appearance > Pause visuals in the background, on by default) also while another
 * window is in use, the app open on a second screen say: on a laptop's integrated
 * graphics the app alone took more than half of the graphics card. The music plays on
 * either way, and the lyrics keep moving unless the window is minimized ([isOnScreen]).
 * The floating lyrics window is not affected.
 */
internal object MainWindowActivity {
    /** The moving pictures run (composables, UI thread). */
    var isShown by mutableStateOf(true)
        private set

    /** The same, for decoder threads. */
    @kotlin.concurrent.Volatile
    var shownNow = true
        private set

    /** Not minimized (the lyrics follow the song while this is true). */
    var isOnScreen by mutableStateOf(true)
        private set

    /**
     * Linux, Pause visuals in the background on, another window in use: the window
     * draws as little as it can. The lyrics drop the light swept word by word, the
     * magnified letters and the blur of the lines out of focus, and simply follow the
     * song line by line; the seek bars and times move once a second. Everything comes
     * back as soon as the window is used again. (Windows is not affected.)
     */
    var isLite by mutableStateOf(false)
        private set

    /**
     * When the playback position has to reach the screen while [isLite]. Every new
     * position recomposes what reads it, and each recomposition draws the whole window
     * again (the GPU redoes every blur and glass even when nothing changed). In the
     * background the main window gets the position only on a seek, a play/pause, and
     * at the next lyric line of its lyrics page ([setLyricsNext]) unless the floating
     * lyrics are showing (they show the lyrics then); seek bars and times catch up as
     * soon as the window is used again. The floating lyrics time their lines with a
     * clock of their own and get the position on a seek, a play/pause, at their next
     * line ([floatingNextMs], a check) and every few seconds to stay in step.
     */
    object LiteClock {
        /** Raw playback position at which the floating lyrics change line (Long.MAX_VALUE: none). */
        @kotlin.concurrent.Volatile var floatingNextMs: Long = Long.MAX_VALUE
        /** The floating lyrics are showing (with lyrics). */
        @kotlin.concurrent.Volatile var floatingShown: Boolean = false
        /** The last position the main window was given (raw). */
        @kotlin.concurrent.Volatile var mainRawMs: Long = 0L
        private val lyricsNext = HashMap<Any, Long>()
        private var sampleMs = 0L
        private var sampleAtNanos = 0L
        private var samplePlaying = false

        /** A lyrics view of the main window: its next change, as a raw playback position. */
        fun setLyricsNext(owner: Any, rawMs: Long) { synchronized(lyricsNext) { lyricsNext[owner] = rawMs } }
        fun clearLyricsNext(owner: Any) { synchronized(lyricsNext) { lyricsNext.remove(owner) } }
        private fun lyricsNextMs(): Long = synchronized(lyricsNext) { lyricsNext.values.minOrNull() } ?: Long.MAX_VALUE

        /**
         * Every playback sample: true when it is a seek (not where the last one would have
         * moved to by now), a new song or a play/pause.
         */
        fun sample(newMs: Long, playing: Boolean, nowNanos: Long): Boolean {
            val expected = sampleMs + if (samplePlaying && sampleAtNanos != 0L) (nowNanos - sampleAtNanos) / 1_000_000L else 0L
            val changed = sampleAtNanos == 0L || newMs == 0L || playing != samplePlaying ||
                kotlin.math.abs(newMs - expected) > 1_500L
            sampleMs = newMs
            sampleAtNanos = nowNanos
            samplePlaying = playing
            return changed
        }

        /** The main window needs [newMs] (it shows [lastMs]); a seek is handled by the caller. */
        fun mainDue(lastMs: Long, newMs: Long): Boolean =
            !floatingShown && crossed(lastMs, newMs, lyricsNextMs())

        /** The floating lyrics need [newMs] (they show [lastMs]); a seek is handled by the caller. */
        fun floatingDue(lastMs: Long, newMs: Long): Boolean =
            crossed(lastMs, newMs, floatingNextMs) || newMs - lastMs >= 4_000L

        private fun crossed(lastMs: Long, newMs: Long, nextMs: Long): Boolean =
            nextMs != Long.MAX_VALUE && newMs >= nextMs && lastMs < nextMs
    }

    private var visible = true
    private var active = true

    private val listeners = CopyOnWriteList<(Boolean) -> Unit>()

    /** The main window is on screen (true) or minimized (false). UI thread. */
    fun update(shown: Boolean) {
        visible = shown
        isOnScreen = shown
        apply()
    }

    /**
     * The main window is the one in use, or the pause in the background is off (desktop).
     * UI thread.
     */
    fun updateActive(inUse: Boolean) {
        active = inUse
        apply()
    }

    /**
     * Fedora / Red Hat family only (set by the desktop app, with Pause visuals in the
     * background on): the window also counts as in the background while it is minimized
     * or the floating lyrics show, whatever the keyboard focus says. GNOME there runs the
     * app through XWayland at twice the size (native scaling), and a frame of the big
     * window held up the floating lyrics' glide. Elsewhere this stays false and nothing
     * changes.
     */
    @kotlin.concurrent.Volatile var hiddenIsBackground = false
        private set

    /**
     * Fedora, RHEL, CentOS Stream, Rocky, Alma... (they ship /etc/redhat-release, or say
     * so in os-release). False on Windows, Android, iOS, Debian, Ubuntu and the rest.
     */
    val isRedHatFamily: Boolean by lazy {
        if (!AppPlatform.isLinux) return@lazy false
        runCatching {
            if (File("/etc/redhat-release").isFile) return@runCatching true
            val osRelease = File("/etc/os-release").takeIf { it.isFile }?.readText().orEmpty()
            osRelease.lineSequence()
                .filter { it.startsWith("ID=") || it.startsWith("ID_LIKE=") }
                .any { line -> line.substringAfter('=').trim('"', ' ').split(' ').any { it == "fedora" || it == "rhel" || it == "centos" } }
        }.getOrDefault(false)
    }
    private var floatingShowing = false

    /** See [hiddenIsBackground]. UI thread. */
    fun updateHiddenIsBackground(enabled: Boolean, floatingLyricsShowing: Boolean) {
        if (hiddenIsBackground == enabled && floatingShowing == floatingLyricsShowing) return
        hiddenIsBackground = enabled
        floatingShowing = floatingLyricsShowing
        apply()
    }

    private fun apply() {
        // Linux and macOS: lighter drawing while the window is in the background.
        val lite = (AppPlatform.isLinux || AppPlatform.isMac) && (!active || (hiddenIsBackground && (!visible || floatingShowing)))
        if (isLite != lite) isLite = lite
        val run = visible && active
        if (shownNow == run && isShown == run) return
        shownNow = run
        isShown = run
        listeners.forEach { listener -> runCatching { listener(run) } }
    }

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }
}
