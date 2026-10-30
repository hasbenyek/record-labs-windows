package com.recordlabs.desktop.data

import java.io.File
import java.net.InetAddress
import java.util.Locale

/**
 * The operating system Record Labs runs on, and the few places where Windows
 * and Linux differ (folders, bundled program names, device name).
 *
 * Everything that is the same on both systems keeps using the existing code;
 * only callers that touch the OS ask this object which path to take.
 */
object Platform {
    enum class Os { Windows, Linux, MacOs, Other }

    val os: Os = System.getProperty("os.name").orEmpty().lowercase(Locale.ROOT).let { name ->
        when {
            name.startsWith("windows") -> Os.Windows
            name.contains("linux") -> Os.Linux
            name.contains("mac") || name.contains("darwin") -> Os.MacOs
            else -> Os.Other
        }
    }

    val isWindows: Boolean get() = os == Os.Windows
    val isLinux: Boolean get() = os == Os.Linux
    val isMac: Boolean get() = os == Os.MacOs

    /** Apple Silicon (arm64) rather than Intel, on a Mac. */
    val isAppleSilicon: Boolean
        get() = isMac && System.getProperty("os.arch").orEmpty().lowercase(Locale.ROOT).let { it == "aarch64" || it == "arm64" }

    private val home: String get() = System.getProperty("user.home").orEmpty()

    /**
     * Root folder for the app's own data.
     * Windows: %LOCALAPPDATA%\RecordLabs (unchanged from earlier versions).
     * Linux: $XDG_DATA_HOME/record-labs, normally ~/.local/share/record-labs.
     */
    val dataRoot: File by lazy {
        if (isMac) {
            // macOS: ~/Library/Application Support/Record Labs (Apple's place for app data).
            File("$home/Library/Application Support", "Record Labs")
        } else if (isWindows) {
            val appData = System.getenv("LOCALAPPDATA")
                ?: System.getenv("APPDATA")
                ?: "$home/AppData/Local"
            File(appData, "RecordLabs")
        } else {
            val xdg = System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotBlank() && File(it).isAbsolute }
                ?: "$home/.local/share"
            File(xdg, "record-labs")
        }
    }

    /** Temporary/cache root: %LOCALAPPDATA%\RecordLabs on Windows, ~/.cache/record-labs on Linux. */
    val cacheRoot: File by lazy {
        if (isMac) File("$home/Library/Caches", "Record Labs")
        else if (isWindows) dataRoot else {
            val xdg = System.getenv("XDG_CACHE_HOME")?.takeIf { it.isNotBlank() && File(it).isAbsolute }
                ?: "$home/.cache"
            File(xdg, "record-labs")
        }
    }

    /** Folder name for bundled native files in a development checkout (src/appResources/<name>). */
    val nativeResourceDirName: String get() = when {
        isWindows -> "windows-x64"
        isMac -> if (isAppleSilicon) "macos-arm64" else "macos-x64"
        else -> "linux-x64"
    }

    /** Adds ".exe" on Windows: "deno" -> "deno.exe". */
    fun executableName(baseName: String): String = if (isWindows) "$baseName.exe" else baseName

    /** Name shown to other devices in Record Labs Connect. */
    val deviceName: String by lazy {
        val name = if (isWindows) System.getenv("COMPUTERNAME") else if (isMac) {
            // The name set in System Settings > General > Sharing ("Hasbi's MacBook Air").
            runCatching {
                val process = ProcessBuilder("scutil", "--get", "ComputerName").redirectErrorStream(true).start()
                process.inputStream.bufferedReader().readText().trim().takeIf { process.waitFor() == 0 }
            }.getOrNull() ?: runCatching { InetAddress.getLocalHost().hostName.removeSuffix(".local") }.getOrNull()
        } else
            System.getenv("HOSTNAME")
                ?: runCatching { File("/etc/hostname").readText().trim() }.getOrNull()
                ?: runCatching { InetAddress.getLocalHost().hostName }.getOrNull()
        name.orEmpty().ifBlank { if (isWindows) "Windows PC" else if (isMac) "Mac" else "Linux PC" }
    }

    /**
     * Linux AppImage builds run from a new temporary mount every launch, so the
     * install location cannot identify the installation. Linux therefore uses
     * one stable data folder per user.
     */
    val usesStableInstallId: Boolean get() = !isWindows

    /** Opens a web link in the default browser. Linux falls back to xdg-open when AWT can't. */
    fun openUrl(url: String) {
        val viaAwt = runCatching {
            val desktop = java.awt.Desktop.getDesktop()
            check(desktop.isSupported(java.awt.Desktop.Action.BROWSE))
            desktop.browse(java.net.URI(url))
        }.isSuccess
        if (!viaAwt && !isWindows) runCatching { ProcessBuilder("xdg-open", url).start() }
    }

    /** Opens a folder in the file manager. Linux falls back to xdg-open when AWT can't. */
    fun openFolder(folder: File) {
        val viaAwt = runCatching {
            val desktop = java.awt.Desktop.getDesktop()
            check(desktop.isSupported(java.awt.Desktop.Action.OPEN))
            desktop.open(folder)
        }.isSuccess
        if (!viaAwt && !isWindows) runCatching { ProcessBuilder("xdg-open", folder.absolutePath).start() }
    }

    /** Path of the running AppImage file, when Record Labs was started from one. */
    val appImagePath: File? get() = System.getenv("APPIMAGE")?.takeIf(String::isNotBlank)?.let(::File)?.takeIf(File::isFile)
}
