package com.recordlabs.desktop.data

/**
 * The library of the earlier Android app (Record Labs 1.9.x): its playlists and liked
 * songs, read once from its database after the update (see [LegacyLibrarySource]).
 */
data class LegacyLibrary(
    val playlists: List<WindowsPlaylist>,
    val liked: List<Track>,
)

/**
 * Where the earlier app's library is read from. Set by the Android app at start (its
 * database is in the app's own data folder); nothing elsewhere.
 */
object LegacyLibrarySource {
    @kotlin.concurrent.Volatile
    var reader: (() -> LegacyLibrary?)? = null
}
