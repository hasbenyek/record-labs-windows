package com.recordlabs.desktop

import com.sun.jna.Library
import com.sun.jna.Native
import java.util.concurrent.Executors

/**
 * Keeps the screen on (no screen saver, no display sleep, no system sleep)
 * while music or a video is playing, and gives that back as soon as playback
 * stops, so Windows' own timers apply again.
 *
 * Windows ties the request to the thread that made it, so every call runs on
 * one long-lived thread; when the app exits that thread ends and Windows drops
 * the request by itself.
 *
 * Linux: a `systemd-inhibit` child process holds an idle + sleep inhibitor
 * (honoured by logind, GNOME and KDE) while playing; it is ended on pause and
 * dies with the app.
 */
internal object PlaybackKeepAwake {
    private val isWindows = com.recordlabs.desktop.data.Platform.isWindows
    private val isLinux = com.recordlabs.desktop.data.Platform.isLinux
    @Volatile
    private var linuxInhibitor: Process? = null
    private val worker by lazy {
        Executors.newSingleThreadExecutor { task -> Thread(task, "record-labs-keep-awake").apply { isDaemon = true } }
    }
    private val kernel32: PowerRequests? by lazy {
        runCatching { Native.load("kernel32", PowerRequests::class.java) }.getOrNull()
    }
    @Volatile
    private var keepingAwake = false

    fun setPlaying(playing: Boolean) {
        if ((!isWindows && !isLinux) || playing == keepingAwake) return
        keepingAwake = playing
        runCatching {
            worker.execute {
                // Only the latest wish counts if calls pile up.
                val awake = keepingAwake
                if (isLinux) {
                    linuxInhibit(awake)
                    return@execute
                }
                runCatching {
                    kernel32?.SetThreadExecutionState(
                        if (awake) ES_CONTINUOUS or ES_SYSTEM_REQUIRED or ES_DISPLAY_REQUIRED else ES_CONTINUOUS,
                    )
                }
            }
        }
    }

    private fun linuxInhibit(awake: Boolean) {
        if (awake) {
            if (linuxInhibitor?.isAlive == true) return
            linuxInhibitor = runCatching {
                ProcessBuilder(
                    "systemd-inhibit", "--what=idle:sleep", "--who=Record Labs", "--why=Playing music", "--mode=block",
                    "sleep", "infinity",
                ).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start()
            }.getOrNull()
            if (linuxInhibitor != null && !shutdownHookAdded) {
                shutdownHookAdded = runCatching { Runtime.getRuntime().addShutdownHook(shutdownHook) }.isSuccess
            }
        } else {
            linuxInhibitor?.let { process -> runCatching { process.destroy() } }
            linuxInhibitor = null
        }
    }

    private var shutdownHookAdded = false

    private val shutdownHook: Thread by lazy {
        Thread({ linuxInhibitor?.let { runCatching { it.destroy() } } }, "record-labs-keep-awake-exit")
    }

    @Suppress("FunctionName")
    private interface PowerRequests : Library {
        fun SetThreadExecutionState(esFlags: Int): Int
    }

    private const val ES_CONTINUOUS = 0x80000000.toInt()
    private const val ES_SYSTEM_REQUIRED = 0x00000001
    private const val ES_DISPLAY_REQUIRED = 0x00000002
}
