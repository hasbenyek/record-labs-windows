package com.recordlabs.desktop.data

import com.recordlabs.desktop.platform.*

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
@Serializable
data class WindowsPlaylist(
    val name: String,
    val tracks: List<Track> = emptyList(),
    val browseId: String? = null,
    val description: String = "",
    /** As on YouTube Music: "PUBLIC", "UNLISTED" or "PRIVATE" (the default, as for backups). */
    val privacy: String = "PRIVATE",
    /** Shown first among the playlists in the Library. */
    val pinned: Boolean = false,
    /** The account's picture of it (its own cover, or YouTube's collage of its songs). */
    val coverUrl: String? = null,
    /** Collaboration is on (YouTube Music's invite link, set from here). */
    val collaborative: Boolean = false,
    /** How many songs it has on the account (its songs are kept there, not here). */
    val remoteCount: Int? = null,
    /** When it was linked to the account from here (the account's list may lag behind). */
    val linkedAtMs: Long = 0L,
    /** The account's list has shown it (so its absence there later means it was deleted). */
    val seenOnAccount: Boolean = false,
    /**
     * Brought over from the earlier app linked to a YouTube Music playlist: kept even when
     * the account does not list it (backed up to the account then, see mergeRemote).
     */
    val keepLocal: Boolean = false,
    /** The cover of the song added last from this device (the sidebar's picture of it). */
    val lastAddedArtwork: String? = null,
    /** Its cover was changed to an own picture from this device. */
    val customCover: Boolean = false,
)

/**
 * The playlist's picture: its own cover when one was set, else the song added last,
 * else the account's picture of it (or its first song's).
 */
val WindowsPlaylist.sidebarArtwork: String?
    get() = (if (customCover) coverUrl else null)
        ?: lastAddedArtwork
        ?: tracks.lastOrNull { !it.artworkUrl.isNullOrBlank() }?.artworkUrl
        ?: coverUrl

/** How many songs it has: the account's count once its songs are kept there. */
val WindowsPlaylist.songTotal: Int get() = if (browseId != null && tracks.isEmpty()) remoteCount ?: 0 else tracks.size

/** YouTube Music's own "Liked Music" playlist: the Library's Liked page already shows it. */
fun WindowsPlaylist.isLikedMusic(): Boolean = browseId == "VLLM" || browseId == "LM"

/**
 * One listen, recorded like the Android app's history event: when a play of a
 * track ends (next track, repeat, app closed) and it was heard for at least
 * [WindowsLibraryStore.MIN_LISTEN_MS]. [playTimeMs] is the time it really played.
 */
@Serializable
data class WindowsListenEvent(
    val trackId: String,
    val atMs: Long,
    val playTimeMs: Long,
)

/** A listened track with its all-time listening time (Android: song.totalPlayTime). */
@Serializable
data class WindowsListenedTrack(
    val track: Track,
    val totalPlayTimeMs: Long = 0L,
    val lastListenedMs: Long = 0L,
)

/** An album, playlist or artist saved to the library from its page (the ⋯ menu). */
@Serializable
data class WindowsSavedCollection(
    val title: String,
    /** "Album", "Playlist" or "Artist". */
    val kind: String,
    val browseId: String,
    val subtitle: String = "",
    val artworkUrl: String? = null,
    val savedAtMs: Long = 0L,
)

@Serializable
data class WindowsLibrarySnapshot(
    val liked: List<Track> = emptyList(),
    val history: List<Track> = emptyList(),
    val playlists: List<WindowsPlaylist> = emptyList(),
    val pinned: List<Track> = emptyList(),
    /** Listening history for "My Top 50", "Monthly Most" and "Weekly Most". */
    val listenedTracks: List<WindowsListenedTrack> = emptyList(),
    val listenEvents: List<WindowsListenEvent> = emptyList(),
    /** Albums, playlists and artists saved from their pages (newest first). */
    val savedCollections: List<WindowsSavedCollection> = emptyList(),
    /** Songs recognized from the sound around (newest first). */
    val recognitions: List<RecognitionEntry> = emptyList(),
)

/** A song recognized from the sound around ("Music Recognition History"). */
@Serializable
data class RecognitionEntry(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val artworkUrl: String? = null,
    val recognizedAtMs: Long,
    /** Its YouTube Music match (null when none was found). */
    val track: Track? = null,
)

/**
 * Most listened tracks, ranked like the Android app: by the total time they
 * were really played. With [sinceMs], only listens after that moment count
 * (Weekly / Monthly Most); without it, all-time totals (My Top 50).
 */
fun WindowsLibrarySnapshot.mostPlayed(limit: Int = Int.MAX_VALUE, sinceMs: Long? = null): List<Track> {
    if (sinceMs == null) {
        return listenedTracks.asSequence()
            .filter { it.totalPlayTimeMs > 0L }
            .sortedWith(compareByDescending<WindowsListenedTrack> { it.totalPlayTimeMs }.thenByDescending { it.lastListenedMs })
            .take(limit)
            .map { it.track }
            .toList()
    }
    val tracks = listenedTracks.associateBy { it.track.id }
    data class Window(var playTimeMs: Long = 0L, var lastMs: Long = 0L)
    val windows = LinkedHashMap<String, Window>()
    for (event in listenEvents) {
        if (event.atMs <= sinceMs) continue
        val window = windows.getOrPut(event.trackId) { Window() }
        window.playTimeMs += event.playTimeMs
        window.lastMs = maxOf(window.lastMs, event.atMs)
    }
    return windows.entries.asSequence()
        .mapNotNull { (id, window) -> tracks[id]?.let { it.track to window } }
        .sortedWith(compareByDescending<Pair<Track, Window>> { it.second.playTimeMs }.thenByDescending { it.second.lastMs })
        .take(limit)
        .map { it.first }
        .toList()
}

/** Desktop library persistence storing data inside the app-owned %LOCALAPPDATA%\RecordLabs folder. */
class WindowsLibraryStore {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file: File by lazy {
        File(AppStorage.directory, "library_snapshot.json")
    }

    private var state = load()

    @JvmSync
    fun snapshot(): WindowsLibrarySnapshot = state

    @JvmSync
    fun addToHistory(track: Track) {
        // Settings > Privacy > Pause listening history.
        if (AppSettings.pauseListenHistory.value) return
        state = state.copy(history = listOf(track) + state.history.filterNot { it.id == track.id }.take(99))
        persist()
    }

    /**
     * Records one listen of [track] that played for [playTimeMs] (ignored when
     * shorter than [MIN_LISTEN_MS], like the Android app's 30 s history
     * threshold). Listens older than [EVENT_RETENTION_MS] are dropped from the
     * event list (they stay in the all-time totals).
     */
    fun recordListen(track: Track, playTimeMs: Long, atMs: Long = currentTimeMillis(), writeNow: Boolean = false): Boolean {
        if (AppSettings.pauseListenHistory.value) return false
        // Settings > Player > "Count a listen after" (default 30 s, like Android).
        if (playTimeMs < AppSettings.historyMinSeconds.value * 1_000L || track.id.isBlank()) return false
        locked(this) {
            val previous = state.listenedTracks.firstOrNull { it.track.id == track.id }
            val updated = WindowsListenedTrack(
                track = track,
                totalPlayTimeMs = (previous?.totalPlayTimeMs ?: 0L) + playTimeMs,
                lastListenedMs = atMs,
            )
            val events = (state.listenEvents + WindowsListenEvent(track.id, atMs, playTimeMs))
                .filter { it.atMs > atMs - EVENT_RETENTION_MS }
                .takeLast(MAX_EVENTS)
            state = state.copy(
                listenedTracks = listOf(updated) + state.listenedTracks.filterNot { it.track.id == track.id },
                listenEvents = events,
            )
        }
        // Written outside the state lock: the writer takes its own lock first.
        if (writeNow) writeLatest() else persist()
        return true
    }

    /** Settings > Backup > Restore: the whole library from a backup. */
    fun replaceWith(snapshot: WindowsLibrarySnapshot) {
        locked(this) { state = snapshot }
        writeLatest()
    }

    /** The player's history list (Clear): only the recently played songs; the listening stats stay. */
    fun clearRecentlyPlayed() {
        locked(this) { state = state.copy(history = emptyList()) }
        writeLatest()
    }

    /** Settings > Privacy > Clear listening history: recently played and the listening stats (likes and playlists stay). */
    fun clearListenHistory() {
        locked(this) {
            state = state.copy(history = emptyList(), listenedTracks = emptyList(), listenEvents = emptyList())
        }
        writeLatest()
    }

    @JvmSync
    fun toggleLiked(track: Track): Boolean {
        val liked = if (state.liked.any { it.id == track.id }) {
            state.liked.filterNot { it.id == track.id }
        } else {
            listOf(track) + state.liked
        }
        state = state.copy(liked = liked)
        persist()
        val nowLiked = liked.any { it.id == track.id }
        // Settings > Integrations > Last.fm > Send likes.
        runCatching { LastFmScrobbler.onLikeChanged(track, nowLiked) }
        return nowLiked
    }

    @JvmSync
    fun toggleSavedCollection(item: WindowsSavedCollection): Boolean {
        val saved = state.savedCollections.any { it.browseId == item.browseId }
        state = state.copy(
            savedCollections = if (saved) state.savedCollections.filterNot { it.browseId == item.browseId }
            else listOf(item.copy(savedAtMs = currentTimeMillis())) + state.savedCollections,
        )
        persist()
        return !saved
    }

    @JvmSync
    fun togglePinned(track: Track): Boolean {
        val pinned = if (state.pinned.any { it.id == track.id }) state.pinned.filterNot { it.id == track.id }
        else listOf(track) + state.pinned
        state = state.copy(pinned = pinned)
        persist()
        return pinned.any { it.id == track.id }
    }

    @JvmSync
    fun createPlaylist(name: String, description: String = "", privacy: String = "PRIVATE"): Boolean {
        val cleanName = name.trim()
        if (cleanName.isBlank() || state.playlists.any { it.name.equals(cleanName, ignoreCase = true) }) return false
        state = state.copy(playlists = state.playlists + WindowsPlaylist(cleanName, description = description.trim(), privacy = privacy))
        persist()
        return true
    }

    /** New name, description and privacy for the playlist [name]; false when the new name is taken. */
    @JvmSync
    fun updatePlaylist(name: String, newName: String, description: String, privacy: String): Boolean {
        val clean = newName.trim()
        if (clean.isBlank()) return false
        if (!clean.equals(name, ignoreCase = true) && state.playlists.any { it.name.equals(clean, ignoreCase = true) }) return false
        state = state.copy(playlists = state.playlists.map {
            if (it.name == name) it.copy(name = clean, description = description.trim(), privacy = privacy) else it
        })
        persist()
        return true
    }

    /** Collaboration on [name] turned on or off (YouTube Music's invite link). */
    @JvmSync
    fun setPlaylistCollaborative(name: String, collaborative: Boolean) {
        state = state.copy(playlists = state.playlists.map { if (it.name == name) it.copy(collaborative = collaborative) else it })
        persist()
    }

    /** [name]'s privacy only ("PUBLIC", "UNLISTED" or "PRIVATE"). */
    @JvmSync
    fun setPlaylistPrivacy(name: String, privacy: String) {
        state = state.copy(playlists = state.playlists.map { if (it.name == name) it.copy(privacy = privacy) else it })
        persist()
    }

    @JvmSync
    fun togglePlaylistPinned(name: String) {
        state = state.copy(playlists = state.playlists.map { if (it.name == name) it.copy(pinned = !it.pinned) else it })
        persist()
    }

    /**
     * The playlist [name] is kept on the YouTube Music account only: its songs are not kept
     * on this device any more (they load from the account when it is opened).
     */
    @JvmSync
    fun releasePlaylistTracks(name: String) {
        state = state.copy(playlists = state.playlists.map { if (it.name == name && it.browseId != null) it.copy(tracks = emptyList()) else it })
        persist()
    }

    @JvmSync
    fun addToPlaylist(name: String, track: Track): Boolean {
        val playlist = state.playlists.firstOrNull { it.name == name } ?: return false
        // On the account: the song goes there (see Main's playlist sync); nothing kept here.
        if (playlist.browseId != null && playlist.tracks.isEmpty()) {
            // One more there (the Library shows it before the account is read again).
            state = state.copy(playlists = state.playlists.map {
                if (it.name == name) it.copy(remoteCount = (it.remoteCount ?: 0) + 1, lastAddedArtwork = track.artworkUrl?.takeIf(String::isNotBlank) ?: it.lastAddedArtwork) else it
            })
            persist()
            return true
        }
        val updated = playlist.copy(tracks = playlist.tracks + track, lastAddedArtwork = track.artworkUrl?.takeIf(String::isNotBlank) ?: playlist.lastAddedArtwork)
        state = state.copy(playlists = state.playlists.map { if (it.name == name) updated else it })
        persist()
        return true
    }

    /** [name]'s cover is an own picture set from here ([custom]), or the account's again. */
    @JvmSync
    fun setPlaylistCustomCover(name: String, custom: Boolean) {
        state = state.copy(playlists = state.playlists.map { if (it.name == name) it.copy(customCover = custom) else it })
        persist()
    }

    @JvmSync
    fun removePlaylist(name: String) {
        state = state.copy(playlists = state.playlists.filterNot { it.name == name })
        persist()
    }

    /** A recognized song at the top of the history (at most 1000 kept). */
    @JvmSync
    fun addRecognition(entry: RecognitionEntry) {
        state = state.copy(recognitions = (listOf(entry) + state.recognitions.filterNot { it.id == entry.id }).take(1000))
        persist()
    }

    @JvmSync
    fun removeRecognition(id: String) {
        state = state.copy(recognitions = state.recognitions.filterNot { it.id == id })
        persist()
    }

    @JvmSync
    fun clearRecognitions() {
        state = state.copy(recognitions = emptyList())
        persist()
    }

    /** The playlist [name] is on the YouTube Music account now, as [browseId]. */
    @JvmSync
    fun linkPlaylist(name: String, browseId: String) {
        if (state.playlists.none { it.name == name }) return
        state = state.copy(playlists = state.playlists.map { if (it.name == name && it.browseId == null) it.copy(browseId = browseId, linkedAtMs = currentTimeMillis()) else it })
        persist()
    }

    /** Playlists kept only on this device (made while signed out, or by the earlier Android app). */
    @JvmSync
    fun localOnlyPlaylists(): List<WindowsPlaylist> = state.playlists.filter { it.browseId == null }

    /**
     * The earlier Android app's playlists and liked songs, added to this library: a
     * playlist of the same name gets the songs it does not have yet, the others are
     * added as they were. Nothing here is removed or replaced. The number of playlists
     * added or completed.
     */
    @JvmSync
    fun importLegacy(legacy: LegacyLibrary): Int {
        var changed = 0
        var playlists = state.playlists
        for (old in legacy.playlists) {
            if (old.name.isBlank()) continue
            val existing = playlists.firstOrNull {
                (old.browseId != null && it.browseId == old.browseId) || it.name.equals(old.name, ignoreCase = true)
            }
            if (existing == null) {
                // One linked to a YouTube Music playlist is kept even if the account no longer
                // lists it (then backed up to the account as the user's own, see mergeRemote);
                // one only on the phone is backed up as a new playlist.
                playlists = playlists + old.copy(tracks = old.tracks.distinctBy { it.id }, keepLocal = old.browseId != null)
                changed++
            } else {
                val missing = old.tracks.filter { track -> existing.tracks.none { it.id == track.id } }
                if (missing.isNotEmpty()) {
                    playlists = playlists.map { if (it === existing) it.copy(tracks = (it.tracks + missing).distinctBy { t -> t.id }) else it }
                    changed++
                }
            }
        }
        val liked = (state.liked + legacy.liked).distinctBy { it.id }
        if (changed == 0 && liked.size == state.liked.size) return 0
        state = state.copy(playlists = playlists, liked = liked)
        persist()
        return changed
    }

    /** Merge the signed-in YouTube Music collection without discarding local-only data. */
    @JvmSync
    fun mergeRemote(liked: List<Track>, playlists: List<WindowsPlaylist>, complete: Boolean = false) {
        val mergedLiked = (liked + state.liked).distinctBy { it.id }
        // YouTube Music's "Liked Music" is the Liked page, not a playlist of its own.
        val accountPlaylists = playlists.filterNot { it.isLikedMusic() }
        val remoteIds = accountPlaylists.mapNotNull { it.browseId }.toSet()
        val now = currentTimeMillis()
        val localOnly = state.playlists.mapNotNull { playlist ->
            when {
                playlist.isLikedMusic() -> null
                playlist.browseId == null -> playlist
                playlist.browseId in remoteIds -> null
                // Only part of the list could be read: kept (and asked about by itself,
                // see syncAccountPlaylists).
                !complete -> playlist
                // Brought over from the earlier app and not on the account: its songs are
                // backed up there as the user's own playlist (nothing of the old library
                // is lost); with no songs there is nothing to keep.
                playlist.keepLocal -> if (playlist.tracks.isNotEmpty()) playlist.copy(browseId = null, keepLocal = false) else null
                // The Library follows the account: linked to it but not in its whole list
                // means deleted there (on YouTube Music or another device), so gone here
                // too. One linked from here and not yet shown by the account's list stays
                // for 10 minutes (the list can lag behind).
                !playlist.seenOnAccount && now - playlist.linkedAtMs < 10 * 60_000L -> playlist
                else -> null
            }
        }
        val mergedRemote = accountPlaylists.map { remote ->
            val existing = state.playlists.firstOrNull { it.browseId == remote.browseId }
            // What was set here (description, privacy, pin) stays; the songs are the account's.
            remote.copy(
                tracks = (remote.tracks + existing?.tracks.orEmpty()).distinctBy { it.id },
                description = existing?.description ?: remote.description,
                privacy = existing?.privacy ?: remote.privacy,
                pinned = existing?.pinned ?: false,
                coverUrl = remote.coverUrl ?: existing?.coverUrl,
                remoteCount = remote.remoteCount ?: existing?.remoteCount,
                collaborative = existing?.collaborative ?: false,
                seenOnAccount = true,
                linkedAtMs = existing?.linkedAtMs ?: 0L,
                keepLocal = existing?.keepLocal ?: false,
                lastAddedArtwork = existing?.lastAddedArtwork,
                customCover = existing?.customCover ?: false,
            )
        }
        state = state.copy(liked = mergedLiked, playlists = localOnly + mergedRemote)
        persist()
    }

    private fun load(): WindowsLibrarySnapshot = runCatching {
        if (file.exists()) json.decodeFromString<WindowsLibrarySnapshot>(file.readText()).withArtistPlaceholderRenamed() else WindowsLibrarySnapshot()
    }.getOrDefault(WindowsLibrarySnapshot())

    /** Songs saved by earlier versions with "YouTube Music" for an unknown artist read "Unknown artist". */
    private fun WindowsLibrarySnapshot.withArtistPlaceholderRenamed(): WindowsLibrarySnapshot {
        fun Track.renamed() = if (artist.isPlaceholderArtist() && artist != UNKNOWN_ARTIST) copy(artist = UNKNOWN_ARTIST) else this
        fun List<Track>.renamed() = if (none { it.artist.isPlaceholderArtist() && it.artist != UNKNOWN_ARTIST }) this else map { it.renamed() }
        return copy(
            liked = liked.renamed(),
            history = history.renamed(),
            pinned = pinned.renamed(),
            playlists = playlists.map { it.copy(tracks = it.tracks.renamed()) },
            listenedTracks = listenedTracks.map { if (it.track.artist.isPlaceholderArtist() && it.track.artist != UNKNOWN_ARTIST) it.copy(track = it.track.renamed()) else it },
        )
    }

    private val writeLock = Any()

    private fun persist() {
        CoroutineScope(IoDispatcher).launch { writeLatest() }
    }

    // One write at a time, through a temporary file, so two saves close
    // together (a counted listen and a like) can never leave a garbled file.
    // Each write saves the newest state, so their order does not matter.
    private fun writeLatest() {
        locked(writeLock) {
            val snapshot = snapshot()
            runCatching {
                val temp = File(file.parentFile, file.name + ".tmp")
                temp.writeText(json.encodeToString(snapshot))
                atomicReplace(temp, file)
            }.onFailure { runCatching { file.writeText(json.encodeToString(snapshot)) } }
        }
    }

    companion object {
        /** The Android app's default "history duration": 30 s. */
        const val MIN_LISTEN_MS = 30_000L
        /** Events are needed for "Monthly Most" (one month back) at most. */
        const val EVENT_RETENTION_MS = 62L * 24 * 60 * 60 * 1000
        const val MAX_EVENTS = 20_000
    }
}
