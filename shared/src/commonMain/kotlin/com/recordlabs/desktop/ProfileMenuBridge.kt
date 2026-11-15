package com.recordlabs.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The macOS menu bar (see MacDesktop in jvmMain) opens the profile pages from outside the
 * main window's content. It leaves a request here; RecordLabsWindow carries it out. On
 * Windows, Linux, Android and iOS nothing ever sets a request, so nothing changes there.
 */
object ProfileMenuBridge {
    sealed interface Request {
        /** A profile page by name (Home, Integrations, ListenTogether, Discord, LastFm, Settings, Login, Recognitions). */
        data class Open(val page: String, val settingsSubpage: String? = null) : Request
        /** Sign out of the Google account, like the Logout button on the profile page. */
        data object Logout : Request
    }

    var pending by mutableStateOf<Request?>(null)

    /** Raised when the menu bar changed an account setting, so the profile page reads it again. */
    var accountVersion by mutableStateOf(0)

    fun open(page: String, settingsSubpage: String? = null) { pending = Request.Open(page, settingsSubpage) }
    fun logout() { pending = Request.Logout }
}
