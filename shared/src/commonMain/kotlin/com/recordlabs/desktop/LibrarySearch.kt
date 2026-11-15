package com.recordlabs.desktop

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * One search box for Library pages: the header's. On a Library page (the Library itself,
 * Liked, Downloaded, a playlist...) it searches that page, which narrows as one types
 * (it had a second search box of its own under the header). Its suggestions panel is
 * then a thin strip: Library / All, and "Search ... on YouTube Music", one tap away.
 * Elsewhere the box searches YouTube Music as always.
 */
internal object LibrarySearch {
    /** The Library page on screen ("Library", "Liked", a playlist's name...), or null. */
    var page by mutableStateOf<String?>(null)

    /** Searching that page (true) or all of YouTube Music (false). Back to true on every page. */
    var inLibrary by mutableStateOf(true)

    /** The header's text (the app's search query). */
    var headerQuery: MutableState<String>? = null

    /** The header's box filters the Library page on screen. */
    val filtering: Boolean get() = page != null && inLibrary

    /** What the Library page on screen is narrowed to ("" when it is not). */
    val filterText: String get() = if (filtering) headerQuery?.value.orEmpty().trim() else ""

    /** The header box's placeholder on a Library page ("Search Liked"), null elsewhere. */
    val placeholder: String? get() = page?.takeIf { inLibrary }?.let { "Search $it" }

    /**
     * The text of whichever scope is not in the box: what was typed for All (the app's
     * search, kept from the other pages) while the box searches the Library, and the
     * other way round. A Library page starts with an empty box; "All" brings that text back.
     */
    private var otherText = ""

    /** Library page [newPage] (null: none) is now on screen. */
    fun onPage(newPage: String?) {
        val query = headerQuery
        val wasOnLibrary = page != null
        if (newPage != null && !wasOnLibrary) {
            // Into the Library: the app's search text waits for "All".
            otherText = query?.value.orEmpty()
            query?.value = ""
        } else if (newPage != null && filtering) {
            // Another Library page starts unfiltered.
            query?.value = ""
        } else if (newPage == null && wasOnLibrary) {
            // Out of the Library: the box shows the app's search text again.
            if (filtering) query?.value = otherText
            otherText = ""
        }
        page = newPage
        if (newPage != null && !inLibrary && wasOnLibrary) {
            // Back to searching the page on screen (its text was set aside for "All").
            val all = query?.value.orEmpty()
            query?.value = ""
            otherText = all
        }
        inLibrary = true
    }

    /** Library (true) or All (false) chosen in the box's strip: each keeps its own text. */
    fun choose(library: Boolean) {
        if (library == inLibrary || page == null) { inLibrary = library; return }
        val query = headerQuery
        val shown = query?.value.orEmpty()
        query?.value = otherText
        otherText = shown
        inLibrary = library
    }
}

/**
 * Portrait header on a Library page below the Library itself (Liked, a playlist, Albums...):
 * Back at the left of the search box and Playlists Migration at its right, the box a
 * little shorter on both sides. Set by the app.
 */
internal object LibraryHeader {
    var back: (() -> Unit)? = null
    var migration: (() -> Unit)? = null
    /** Pull to refresh on a Library page: the account's playlists (and covers) read again. */
    var refresh: (suspend () -> Unit)? = null

    /** A Library page below the Library itself is on screen. */
    val onSubPage: Boolean get() = LibrarySearch.page.let { it != null && it != "Library" }
}
