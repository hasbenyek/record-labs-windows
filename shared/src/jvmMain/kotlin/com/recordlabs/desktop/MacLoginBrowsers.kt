package com.recordlabs.desktop

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * macOS: the browsers Google login can open (it drives the browser to read the session,
 * which Safari doesn't allow). The default browser comes first when it is one of them,
 * then the installed ones: Chrome, Brave, Edge, Chromium, Vivaldi, Opera, then Firefox.
 * Apps are looked for in /Applications and ~/Applications.
 */
internal object MacLoginBrowsers {
    private data class Known(
        val bundleId: String,
        val appName: String,
        val executable: String,
        val engine: LinuxLoginBrowser.Engine,
    )

    private val known = listOf(
        Known("com.google.chrome", "Google Chrome", "Google Chrome", LinuxLoginBrowser.Engine.Chromium),
        Known("com.brave.browser", "Brave Browser", "Brave Browser", LinuxLoginBrowser.Engine.Chromium),
        Known("com.microsoft.edgemac", "Microsoft Edge", "Microsoft Edge", LinuxLoginBrowser.Engine.Chromium),
        Known("org.chromium.chromium", "Chromium", "Chromium", LinuxLoginBrowser.Engine.Chromium),
        Known("com.vivaldi.vivaldi", "Vivaldi", "Vivaldi", LinuxLoginBrowser.Engine.Chromium),
        Known("com.operasoftware.opera", "Opera", "Opera", LinuxLoginBrowser.Engine.Chromium),
        Known("org.mozilla.firefox", "Firefox", "firefox", LinuxLoginBrowser.Engine.Firefox),
    )

    /** Every usable browser, the default one first. */
    fun all(): List<LinuxLoginBrowser.Browser> {
        val defaultId = defaultBrowserBundleId()
        val installed = known.mapNotNull { browser ->
            val program = appFolders()
                .map { File(it, "${browser.appName}.app/Contents/MacOS/${browser.executable}") }
                .firstOrNull { it.isFile && it.canExecute() }
                ?: return@mapNotNull null
            LinuxLoginBrowser.Browser(
                name = browser.appName,
                command = listOf(program.absolutePath),
                engine = browser.engine,
                sandbox = LinuxLoginBrowser.Sandbox.Native,
                isDefault = browser.bundleId.equals(defaultId, ignoreCase = true),
            )
        }
        return installed.sortedByDescending { it.isDefault }
    }

    private fun appFolders(): List<File> = listOf(
        File("/Applications"),
        File(System.getProperty("user.home").orEmpty(), "Applications"),
    )

    /** The bundle id of the app that opens https links (System Settings > Desktop & Dock > Default web browser). */
    private fun defaultBrowserBundleId(): String? = runCatching {
        val process = ProcessBuilder(
            "defaults", "read", "com.apple.LaunchServices/com.apple.launchservices.secure", "LSHandlers",
        ).redirectErrorStream(true).start()
        val text = process.inputStream.bufferedReader().readText()
        if (!process.waitFor(5, TimeUnit.SECONDS) || process.exitValue() != 0) return@runCatching null
        // Old-style plist: one { ... } block per handler; the https one names the browser.
        text.split("},").firstOrNull { block ->
            Regex("""LSHandlerURLScheme\s*=\s*"?https"?\s*;""").containsMatchIn(block)
        }?.let { block ->
            Regex("""LSHandlerRoleAll\s*=\s*"?([A-Za-z0-9.\-]+)"?\s*;""").find(block)?.groupValues?.get(1)
        }
    }.getOrNull()
}
