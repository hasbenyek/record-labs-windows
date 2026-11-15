package com.recordlabs.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.recordlabs.desktop.data.AppDataEvents
import com.recordlabs.desktop.data.AppDataRequest
import com.recordlabs.desktop.data.Platform
import com.recordlabs.desktop.data.WindowsPreferences
import kotlinx.coroutines.delay

/**
 * macOS only: what the Mac version does differently from Windows and Linux.
 *
 *  - The window follows the system's light / dark appearance (title bar included)
 *    and never shows a white background before the first frame.
 *  - The menu bar at the top of the screen holds the profile page: account,
 *    music recognition, integrations and every settings page.
 *  - Record Labs > About / Settings… / Quit open the matching pages, and Quit
 *    closes the app the same way as the window's close button (the playback
 *    position is saved and a downloaded update installs).
 *  - Full screen is the system's own (a Space of its own, like the green button).
 *
 * Every function returns at once on Windows and Linux.
 */
internal object MacDesktop {
    val isActive: Boolean get() = Platform.isMac

    /** Called first thing in main(), before AWT starts. */
    fun configureBeforeAwt() {
        if (!isActive) return
        // The title bar follows System Settings > Appearance (dark or light).
        if (System.getProperty("apple.awt.application.appearance").isNullOrBlank()) {
            System.setProperty("apple.awt.application.appearance", "system")
        }
        // The main window's menu bar goes to the top of the screen.
        System.setProperty("apple.laf.useScreenMenuBar", "true")
        System.setProperty("apple.awt.application.name", "Record Labs")
        // The Mac package carries a stand-in version (Apple requires a first number
        // above 0, so 0.1.9 is packaged as 1.1.9); AppInfo then uses the real one.
        System.clearProperty("jpackage.app-version")
        // An Intel Mac with 4 processor threads or fewer (MacBook Air 2018–2020, say) gets
        // lighter visuals by default (LightVisuals; the user can turn them on or off).
        if (System.getProperty("recordlabs.slowerMac").isNullOrBlank()) {
            val slower = !Platform.isAppleSilicon && Runtime.getRuntime().availableProcessors() <= 4
            System.setProperty("recordlabs.slowerMac", slower.toString())
        }
    }

    /** The window's own background (seen for a moment before Compose draws): dark like the app. */
    fun prepareWindow(window: java.awt.Window) {
        if (!isActive) return
        runCatching { window.background = java.awt.Color(0x0E, 0x0E, 0x10) }
        runCatching {
            (window as? javax.swing.RootPaneContainer)?.contentPane?.background = java.awt.Color(0x0E, 0x0E, 0x10)
        }
    }

    /** Record Labs > About, Settings… and Quit (the app menu macOS adds itself). */
    fun installAppMenuHandlers(onQuit: () -> Unit) {
        if (!isActive) return
        runCatching {
            val desktop = java.awt.Desktop.getDesktop()
            if (desktop.isSupported(java.awt.Desktop.Action.APP_ABOUT)) {
                desktop.setAboutHandler { ProfileMenuBridge.open("Settings", "About") }
            }
            if (desktop.isSupported(java.awt.Desktop.Action.APP_PREFERENCES)) {
                desktop.setPreferencesHandler { ProfileMenuBridge.open("Settings") }
            }
            if (desktop.isSupported(java.awt.Desktop.Action.APP_QUIT_HANDLER)) {
                desktop.setQuitHandler { _, response ->
                    // Our own closing (save, install a ready update, exit); macOS is told
                    // to cancel its default quit, which would skip all that.
                    javax.swing.SwingUtilities.invokeLater { onQuit() }
                    response.cancelQuit()
                }
            }
        }
    }

    /** The system's own full screen for [frame] (the Compose window's drawing surface does it). */
    fun setFullscreen(frame: java.awt.Frame, enabled: Boolean) {
        val layer = findLayer(frame)
        if (layer != null) {
            runCatching { layer.fullscreen = enabled }
        } else {
            val device = frame.graphicsConfiguration?.device
            if (device?.isFullScreenSupported == true) device.fullScreenWindow = if (enabled) frame else null
        }
    }

    private fun findLayer(component: java.awt.Component): org.jetbrains.skiko.SkiaLayer? {
        if (component is org.jetbrains.skiko.SkiaLayer) return component
        if (component is java.awt.Container) {
            for (child in component.components) findLayer(child)?.let { return it }
        }
        return null
    }
}

/**
 * The macOS menu bar: the profile page's sections and buttons, doing the same as there.
 * Only composed on macOS (see runDesktopApp).
 */
@Composable
internal fun FrameWindowScope.MacProfileMenuBar() {
    // Account settings can change in the app too; the menu reads them again every moment.
    val accountVersion = ProfileMenuBridge.accountVersion
    var signedIn by remember { mutableStateOf(WindowsPreferences.googleSession() != null) }
    var moreContent by remember { mutableStateOf(WindowsPreferences.moreContent()) }
    var autoSync by remember { mutableStateOf(WindowsPreferences.autoSync()) }
    LaunchedEffect(accountVersion) {
        while (true) {
            signedIn = WindowsPreferences.googleSession() != null
            moreContent = WindowsPreferences.moreContent()
            autoSync = WindowsPreferences.autoSync()
            delay(1_500)
        }
    }
    fun openSettings(subpage: String? = null) = ProfileMenuBridge.open("Settings", subpage)

    MenuBar {
        Menu("Account", mnemonic = 'A') {
            Item("Profile", onClick = { ProfileMenuBridge.open("Home") })
            Separator()
            if (signedIn) {
                Item("Log Out", onClick = { ProfileMenuBridge.logout() })
            } else {
                Item("Log In with Google…", onClick = { ProfileMenuBridge.open("Login") })
            }
            Separator()
            CheckboxItem(
                "More content",
                checked = moreContent,
                onCheckedChange = {
                    moreContent = it
                    WindowsPreferences.setMoreContent(it)
                    AppDataEvents.request(AppDataRequest.ReloadHome)
                    ProfileMenuBridge.accountVersion++
                },
            )
            CheckboxItem(
                "Auto-sync with account",
                checked = autoSync,
                onCheckedChange = {
                    autoSync = it
                    WindowsPreferences.setAutoSync(it)
                    ProfileMenuBridge.accountVersion++
                },
            )
        }
        Menu("Music", mnemonic = 'M') {
            Item("Music Recognition History", onClick = { ProfileMenuBridge.open("Recognitions") })
        }
        Menu("Integrations", mnemonic = 'I') {
            Item("All Integrations", onClick = { ProfileMenuBridge.open("Integrations") })
            Separator()
            Item("Discord Integration", onClick = { ProfileMenuBridge.open("Discord") })
            Item("Last.fm Integration", onClick = { ProfileMenuBridge.open("LastFm") })
            Item("Listen Together", onClick = { ProfileMenuBridge.open("ListenTogether") })
        }
        Menu("Settings", mnemonic = 'S') {
            // ⌘, is Record Labs > Settings… (the app menu macOS adds), which opens the same page.
            Item("All Settings", onClick = { openSettings() })
            Separator()
            Item("Appearance", onClick = { openSettings("Appearance") })
            Item("Player and Audio", onClick = { openSettings("Player and audio") })
            Item("Stream Sources", onClick = { openSettings("Stream sources") })
            Item("Content", onClick = { openSettings("Content") })
            Item("Lyrics Translation", onClick = { openSettings("Lyrics translation") })
            Separator()
            Item("Privacy", onClick = { openSettings("Privacy") })
            Item("Storage", onClick = { openSettings("Storage") })
            Item("Backup & Restore", onClick = { openSettings("Backup & restore") })
            Separator()
            Item("About Record Labs", onClick = { openSettings("About") })
            Item("Check for Updates…", onClick = {
                com.recordlabs.desktop.data.AppUpdater.checkNow()
                openSettings("About")
            })
        }
    }
}
