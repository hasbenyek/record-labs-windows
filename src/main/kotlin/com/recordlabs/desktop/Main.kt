// Its own class name: the :shared module already has a com.recordlabs.desktop.MainKt
// (the app UI), and two classes with one name made the app fail to start.
@file:JvmName("RecordLabsLauncher")

package com.recordlabs.desktop

/**
 * Desktop entry point (Windows and Linux). The app itself lives in the
 * :shared module, together with the code the Android app uses.
 */
fun main(args: Array<String>) {
    limitNativeMemoryPools()
    runDesktopApp(args)
}

/**
 * Linux: glibc gives busy threads (Java, VLC, the graphics) their own memory pools, up
 * to eight per processor core, and most of them stay large and nearly empty (hundreds
 * of MB counted as the app's own). Two pools are plenty for a music player. Set here
 * at the very start, so it holds however the app is started (the launcher script also
 * sets MALLOC_ARENA_MAX, which this then leaves as it is).
 */
private fun limitNativeMemoryPools() {
    if (!System.getProperty("os.name").orEmpty().startsWith("Linux", ignoreCase = true)) return
    if (!System.getenv("MALLOC_ARENA_MAX").isNullOrBlank()) return
    runCatching {
        // mallopt(M_ARENA_MAX, 2); M_ARENA_MAX is -8 in glibc.
        com.sun.jna.NativeLibrary.getInstance("c").getFunction("mallopt").invokeInt(arrayOf<Any>(-8, 2))
    }
}
