package com.recordlabs.desktop.data

/**
 * How a platform plays YouTube songs itself (Android: its own engine; iOS: its
 * player): the URI the player opens for a song, a resolver for a stream URL
 * (downloads), the signed-in account, a session check and a prefetch of the next
 * song. Unset on the desktop. (Platform.androidX on Android points here.)
 */
object StreamHooks {
    @kotlin.concurrent.Volatile var streamUri: ((String) -> String)? = null
    @kotlin.concurrent.Volatile var streamResolver: (suspend (String) -> String?)? = null
    @kotlin.concurrent.Volatile var accountProfile: (suspend () -> GoogleAccountProfile?)? = null
    @kotlin.concurrent.Volatile var validateSession: (suspend () -> Boolean?)? = null
    @kotlin.concurrent.Volatile var prefetch: (suspend (String) -> Unit)? = null
    /**
     * Android: a signed-in request for the account's playlists through the original Android
     * app's own account client (endpoint under youtubei/v1, body without "context"); the
     * answer's text. The shared requests were not taken as the account there.
     */
    @kotlin.concurrent.Volatile var accountPost: (suspend (endpoint: String, fieldsJson: String) -> String)? = null
    /** Android: a playlist's own cover uploaded through that client; null when done, else why not. */
    @kotlin.concurrent.Volatile var uploadPlaylistCover: (suspend (playlistId: String, image: ByteArray) -> String?)? = null
}
