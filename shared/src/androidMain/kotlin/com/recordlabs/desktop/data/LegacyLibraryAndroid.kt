package com.recordlabs.desktop.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * The earlier Android app (Record Labs 1.9.x, same app id) kept its library in the
 * Room database "song.db" in this app's own data folder, and the update leaves it
 * there. Its playlists were only on the phone unless "Sync" was switched on when one
 * was made, so they did not come back with the account. They are read from that
 * database: a copy of it (with its write-ahead log), so the original stays untouched.
 */
internal fun readLegacyLibrary(context: Context): LegacyLibrary? {
    val source = context.getDatabasePath("song.db")
    if (!source.isFile) return null
    val work = File(context.cacheDir, "legacy-library").apply { deleteRecursively(); mkdirs() }
    return try {
        for (suffix in listOf("", "-wal", "-shm")) {
            val file = File(source.path + suffix)
            if (file.isFile) file.copyTo(File(work, "song.db$suffix"), overwrite = true)
        }
        SQLiteDatabase.openDatabase(File(work, "song.db").path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            fun columns(table: String): Set<String> = buildSet {
                db.rawQuery("PRAGMA table_info($table)", null).use { c -> while (c.moveToNext()) add(c.getString(1)) }
            }
            val playlistColumns = columns("playlist")
            val songColumns = columns("song")
            if (playlistColumns.isEmpty() || songColumns.isEmpty()) return null
            val artistsBySong = HashMap<String, String>()
            fun artistsOf(songId: String): String = artistsBySong.getOrPut(songId) {
                runCatching {
                    db.rawQuery(
                        "SELECT a.name FROM song_artist_map m JOIN artist a ON a.id = m.artistId WHERE m.songId = ? ORDER BY m.position",
                        arrayOf(songId),
                    ).use { c -> buildList { while (c.moveToNext()) c.getString(0)?.takeIf(String::isNotBlank)?.let(::add) } }
                        .joinToString(", ")
                }.getOrDefault("")
            }
            val songSelect = buildString {
                append("s.id, s.title")
                append(if ("duration" in songColumns) ", s.duration" else ", -1")
                append(if ("thumbnailUrl" in songColumns) ", s.thumbnailUrl" else ", NULL")
                append(if ("albumName" in songColumns) ", s.albumName" else ", NULL")
                append(if ("explicit" in songColumns) ", s.explicit" else ", 0")
            }
            val notLocal = if ("isLocal" in songColumns) " AND (s.isLocal IS NULL OR s.isLocal = 0)" else ""
            fun track(c: android.database.Cursor): Track? {
                val id = c.getString(0)?.takeIf(String::isNotBlank) ?: return null
                val seconds = if (c.isNull(2)) -1 else c.getInt(2)
                return Track(
                    id = id,
                    title = c.getString(1).orEmpty(),
                    artist = artistsOf(id),
                    album = if (c.isNull(4)) "" else c.getString(4).orEmpty(),
                    artworkUrl = if (c.isNull(3)) null else c.getString(3),
                    durationMs = if (seconds > 0) seconds * 1000L else 0L,
                    explicit = !c.isNull(5) && c.getInt(5) != 0,
                )
            }
            // The library's playlists: the ones made in the app (no browse id) and the ones
            // saved to it (bookmarked); other rows are only cached pages.
            val playlistFilter = when {
                "bookmarkedAt" in playlistColumns -> "WHERE p.browseId IS NULL OR p.bookmarkedAt IS NOT NULL"
                else -> ""
            }
            val order = if ("createdAt" in playlistColumns) "ORDER BY p.createdAt" else "ORDER BY p.rowid"
            val rows = db.rawQuery("SELECT p.id, p.name, p.browseId FROM playlist p $playlistFilter $order", null).use { c ->
                buildList { while (c.moveToNext()) add(Triple(c.getString(0), c.getString(1).orEmpty(), if (c.isNull(2)) null else c.getString(2))) }
            }
            val playlists = rows.mapNotNull { (id, name, browseId) ->
                if (name.isBlank()) return@mapNotNull null
                val tracks = db.rawQuery(
                    "SELECT $songSelect FROM playlist_song_map m JOIN song s ON s.id = m.songId WHERE m.playlistId = ?$notLocal ORDER BY m.position",
                    arrayOf(id),
                ).use { c -> buildList { while (c.moveToNext()) track(c)?.let(::add) } }
                // A saved YouTube Music playlist keeps its browse id ("VL..."), as the account's do.
                val accountId = browseId?.takeIf(String::isNotBlank)?.let { if (it.startsWith("VL")) it else "VL$it" }
                WindowsPlaylist(name = name, tracks = tracks.distinctBy { it.id }, browseId = accountId)
            }
            val liked = if ("liked" in songColumns) {
                val likedOrder = if ("likedDate" in songColumns) "ORDER BY s.likedDate DESC" else ""
                db.rawQuery("SELECT $songSelect FROM song s WHERE s.liked = 1$notLocal $likedOrder", null)
                    .use { c -> buildList { while (c.moveToNext()) track(c)?.let(::add) } }
            } else emptyList()
            LegacyLibrary(playlists, liked)
        }
    } catch (e: Throwable) {
        android.util.Log.w("RecordLabs", "Earlier app library could not be read", e)
        null
    } finally {
        work.deleteRecursively()
    }
}
