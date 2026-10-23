package com.recordlabs.desktop.data

import java.io.File

/**
 * Finds the helper programs that ship inside Record Labs (yt-dlp, Deno).
 *
 * Order: the installed app's resources folder, then the development checkout
 * (src/appResources/windows-x64 or linux-x64). On Linux a copy installed by
 * the system package manager (found on PATH) is the last fallback.
 */
object BundledTools {
    fun find(baseName: String): File? {
        val name = Platform.executableName(baseName)
        val resourceDir = System.getProperty("compose.application.resources.dir")
        val nativeDir = Platform.nativeResourceDirName
        val candidates = buildList {
            if (!resourceDir.isNullOrBlank()) add(File(resourceDir, name))
            add(File(System.getProperty("user.dir"), "src/appResources/$nativeDir/$name"))
            add(File("src/appResources/$nativeDir/$name"))
            add(File("windows-recordlabs/src/appResources/$nativeDir/$name"))
        }
        candidates.map(File::getAbsoluteFile).firstOrNull { it.isFile }?.let { file ->
            if (!Platform.isWindows && !file.canExecute()) runCatching { file.setExecutable(true) }
            return file
        }
        if (Platform.isWindows) return null
        return System.getenv("PATH").orEmpty().split(File.pathSeparator)
            .filter(String::isNotBlank)
            .map { File(it, name) }
            .firstOrNull { it.isFile && it.canExecute() }
    }
}
