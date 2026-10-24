package com.recordlabs.desktop

import com.recordlabs.desktop.data.Platform
import com.recordlabs.desktop.data.WindowsPreferences
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.sql.DriverManager
import java.util.concurrent.TimeUnit

/**
 * Google login in Firefox (the default browser of Pop!_OS, Ubuntu, Fedora...).
 *
 * Firefox is opened normally with a fresh temporary profile. It is NOT put in
 * remote-control mode: Firefox then reports navigator.webdriver = true and
 * Google refuses to sign in ("this browser may not be secure"). Instead the
 * app reads the profile's cookie database (cookies.sqlite) until YouTube's
 * SAPISID sign-in cookie appears, saves the YouTube cookies like the Chromium
 * flow does, closes that Firefox window and deletes the temporary profile.
 */
internal object LinuxFirefoxLogin {
    private val http = OkHttpClient.Builder().callTimeout(12, TimeUnit.SECONDS).build()

    suspend fun login(browser: LinuxLoginBrowser.Browser, startUrl: String, onProgress: (String) -> Unit): Result<Unit> = runCatching {
        val profile = browser.newProfileDir()
        writePrefs(profile)
        val command = browser.command + listOf("--new-instance", "--no-remote", "--profile", profile.absolutePath, startUrl)
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()
        try {
            onProgress("${browser.name} terbuka. Login dengan akun Google di sana, lalu tunggu YouTube Music tampil…")
            val started = System.currentTimeMillis()
            val deadline = started + 5 * 60_000L
            while (System.currentTimeMillis() < deadline) {
                val cookies = runCatching { youtubeCookies(profile) }.getOrDefault(emptyList())
                if (cookies.any { it.first == "SAPISID" }) {
                    onProgress("Login berhasil. Menyiapkan sesi YouTube Music…")
                    val header = cookies.distinctBy { it.first }.joinToString("; ") { "${it.first}=${it.second}" }
                    val (visitor, dataSync) = youtubeConfig(header)
                    WindowsPreferences.saveGoogleSession(header, visitor, dataSync)
                    return@runCatching
                }
                val browserOpen = process.isAlive || anyProcessUses(profile)
                if (!browserOpen && System.currentTimeMillis() - started > 4_000L) {
                    error("${browser.name} ditutup sebelum login selesai.")
                }
                delay(1_200L)
            }
            error("Waktu login habis. Coba lagi.")
        } finally {
            runCatching { process.descendants().toList().asReversed().forEach { if (it.isAlive) it.destroy() } }
            if (process.isAlive) process.destroy()
            LinuxLoginBrowser.killProfileProcesses(profile)
            Thread.sleep(500)
            runCatching { profile.deleteRecursively() }
        }
    }

    /** A quiet, private login window: no welcome pages, default-browser prompt or saved passwords. */
    private fun writePrefs(profile: File) {
        File(profile, "user.js").writeText(
            listOf(
                "browser.shell.checkDefaultBrowser" to "false",
                "browser.aboutwelcome.enabled" to "false",
                "browser.startup.homepage_override.mstone" to "\"ignore\"",
                "startup.homepage_welcome_url" to "\"\"",
                "startup.homepage_welcome_url.additional" to "\"\"",
                "datareporting.policy.dataSubmissionPolicyBypassNotification" to "true",
                "toolkit.telemetry.reportingpolicy.firstRun" to "false",
                "browser.tabs.warnOnClose" to "false",
                "browser.warnOnQuitShortcut" to "false",
                "browser.sessionstore.resume_from_crash" to "false",
                "signon.rememberSignons" to "false",
                "extensions.pocket.enabled" to "false",
            ).joinToString("\n", postfix = "\n") { (key, value) -> "user_pref(\"$key\", $value);" },
        )
    }

    /**
     * (name, value) of the youtube.com cookies in the profile. The database is
     * copied first (with its write-ahead log) so Firefox's lock never blocks it.
     */
    internal fun youtubeCookies(profile: File): List<Pair<String, String>> {
        val database = File(profile, "cookies.sqlite")
        if (!database.isFile) return emptyList()
        val copyDir = Files.createTempDirectory(Platform.cacheRoot.apply { mkdirs() }.toPath(), "cookies").toFile()
        try {
            Files.copy(database.toPath(), File(copyDir, "cookies.sqlite").toPath(), StandardCopyOption.REPLACE_EXISTING)
            File(profile, "cookies.sqlite-wal").takeIf(File::isFile)?.let {
                Files.copy(it.toPath(), File(copyDir, "cookies.sqlite-wal").toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            // The SQLite driver unpacks a native library; keep it out of a possibly noexec /tmp.
            System.setProperty("org.sqlite.tmpdir", Platform.cacheRoot.absolutePath)
            Class.forName("org.sqlite.JDBC")
            DriverManager.getConnection("jdbc:sqlite:" + File(copyDir, "cookies.sqlite").absolutePath).use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery(
                        "SELECT name, value FROM moz_cookies WHERE host = 'youtube.com' OR host LIKE '%.youtube.com' ORDER BY host",
                    ).use { rows ->
                        val result = mutableListOf<Pair<String, String>>()
                        while (rows.next()) {
                            val name = rows.getString(1).orEmpty()
                            if (name.isNotBlank()) result += name to rows.getString(2).orEmpty()
                        }
                        return result
                    }
                }
            }
        } finally {
            copyDir.deleteRecursively()
        }
    }

    /** VISITOR_DATA and DATASYNC_ID from YouTube Music's page config (the Chromium flow reads them from the page). */
    private fun youtubeConfig(cookieHeader: String): Pair<String, String> = runCatching {
        val request = Request.Builder()
            .url("https://music.youtube.com/")
            .header("Cookie", cookieHeader)
            .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:131.0) Gecko/20100101 Firefox/131.0")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
        val html = http.newCall(request).execute().use { it.body?.string().orEmpty() }
        val visitor = Regex("\"VISITOR_DATA\"\\s*:\\s*\"([^\"]+)\"").find(html)?.groupValues?.get(1).orEmpty()
        val dataSync = Regex("\"DATASYNC_ID\"\\s*:\\s*\"([^\"]+)\"").find(html)?.groupValues?.get(1).orEmpty().substringBefore("||")
        visitor to dataSync
    }.getOrDefault("" to "")

    private fun anyProcessUses(profile: File): Boolean = runCatching {
        val marker = profile.absolutePath
        ProcessHandle.allProcesses().anyMatch { it.isAlive && it.info().commandLine().orElse("").contains(marker) }
    }.getOrDefault(false)
}
