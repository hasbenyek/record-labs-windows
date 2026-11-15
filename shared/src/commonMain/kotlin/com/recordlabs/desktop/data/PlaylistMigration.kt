package com.recordlabs.desktop.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

/** A song of a playlist from another app: what it is called, by whom, how long. */
data class MigrationTrack(val title: String, val artist: String, val durationMs: Long = 0L)

/** A playlist read from a shared link (or a pasted list of songs). */
data class MigrationSource(
    val service: String,
    val name: String,
    val tracks: List<MigrationTrack>,
    /** Songs already known on YouTube Music (a YouTube link): nothing to look up. */
    val ready: List<Track> = emptyList(),
)

/** One song after the look-up: its YouTube Music match, or none. */
data class MigrationMatch(val source: MigrationTrack, val track: Track?)

/**
 * Playlists Migration: playlists from Spotify, Apple Music, Deezer and YouTube (a shared
 * link), or a pasted list of songs ("Artist - Title" per line), become playlists on the
 * YouTube Music account; and the other way, a playlist's songs as a list other apps (and
 * transfer services) can read. Only public pages are read, with no account of those apps.
 */
object PlaylistMigration {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** The service a link belongs to, or null when it is not a link (a pasted list). */
    fun serviceOf(input: String): String? {
        val text = input.trim().lowercase()
        return when {
            "spotify.com" in text || "spotify.link" in text -> "Spotify"
            "music.apple.com" in text || "itunes.apple.com" in text -> "Apple Music"
            "deezer.com" in text || "deezer.page.link" in text || "link.deezer.com" in text -> "Deezer"
            "youtube.com" in text || "youtu.be" in text -> "YouTube"
            text.startsWith("http") -> "Link"
            else -> null
        }
    }

    suspend fun read(repository: WindowsMusicRepository, input: String): MigrationSource {
        val text = input.trim()
        return when (serviceOf(text)) {
            null -> pastedList(text)
            "Spotify" -> spotify(repository, text)
            "Apple Music" -> appleMusic(repository, text)
            "Deezer" -> deezer(repository, text)
            "YouTube" -> youTube(repository, text)
            else -> error("This link is not supported yet. Use a Spotify, Apple Music, Deezer or YouTube playlist link.")
        }
    }

    /** "Artist - Title" (or "Title by Artist", or a title alone) per line. */
    private fun pastedList(text: String): MigrationSource {
        val tracks = text.lines().map(String::trim).filter(String::isNotBlank).mapNotNull { line ->
            val clean = line.removePrefix("•").trim().replace(Regex("^\\d+[.)]\\s*"), "")
            val dash = Regex("\\s[-–—]\\s").find(clean)
            when {
                dash != null -> MigrationTrack(title = clean.substring(dash.range.last + 1).trim(), artist = clean.substring(0, dash.range.first).trim())
                " by " in clean -> MigrationTrack(title = clean.substringBeforeLast(" by ").trim(), artist = clean.substringAfterLast(" by ").trim())
                else -> MigrationTrack(title = clean, artist = "")
            }.takeIf { it.title.isNotBlank() }
        }
        require(tracks.isNotEmpty()) { "Paste a playlist link, or a list of songs (one per line)." }
        return MigrationSource("List", "Imported playlist", tracks)
    }

    private suspend fun spotify(repository: WindowsMusicRepository, link: String): MigrationSource {
        // A short link (spotify.link) leads to the real one.
        val id = Regex("playlist[/:]([A-Za-z0-9]{10,})").find(link)?.groupValues?.get(1)
            ?: Regex("playlist[/:]([A-Za-z0-9]{10,})").find(repository.fetchPublicPage(link))?.groupValues?.get(1)
            ?: error("That Spotify link is not a playlist.")
        val page = repository.fetchPublicPage("https://open.spotify.com/embed/playlist/$id")
        // The page's data: a __NEXT_DATA__ block, or (newer pages) pieces of it inside script
        // strings; the songs are read from either.
        val entity = scriptJson(page, "__NEXT_DATA__")?.let { data -> findObject(data) { it["trackList"] is JsonArray } }
        val fromData = entity?.get("trackList")?.jsonArray?.mapNotNull { element ->
            val item = element as? JsonObject ?: return@mapNotNull null
            val title = item.string("title") ?: return@mapNotNull null
            MigrationTrack(title, item.string("subtitle").orEmpty().replace("\u00a0", " "), item.long("duration") ?: 0L)
        }.orEmpty()
        val tracks = fromData.ifEmpty { spotifyTracksInText(page) }
        val name = entity?.string("name") ?: entity?.string("title")
            ?: Regex("<meta property=\"og:title\" content=\"([^\"]+)\"").find(page)?.groupValues?.get(1)?.htmlDecoded()
            ?: Regex("<title>([^<]+)</title>").find(page)?.groupValues?.get(1)?.htmlDecoded()?.substringBefore(" | ")?.substringBefore(" - playlist")
            ?: "Spotify playlist"
        require(tracks.isNotEmpty()) { "Spotify did not show the songs of this playlist (it may be private, or made for your account only)." }
        return MigrationSource("Spotify", name, tracks)
    }

    private suspend fun appleMusic(repository: WindowsMusicRepository, link: String): MigrationSource {
        val page = repository.fetchPublicPage(link)
        val tracks = mutableListOf<MigrationTrack>()
        scriptJson(page, "serialized-server-data")?.let { data ->
            collectObjects(data) { it.string("title") != null && it.string("artistName") != null }.forEach { item ->
                val duration = item.long("duration") ?: 0L
                tracks += MigrationTrack(item.string("title")!!, item.string("artistName")!!, duration)
            }
        }
        val name = ldJson(page)?.string("name")
            ?: Regex("<meta property=\"og:title\" content=\"([^\"]+)\"").find(page)?.groupValues?.get(1)?.htmlDecoded()?.substringBefore(" by ")?.substringBefore(" - ")
            ?: "Apple Music playlist"
        if (tracks.isEmpty()) {
            // The page's summary for search engines: titles (and durations) only.
            ldJson(page)?.get("track")?.let { it as? JsonArray }?.forEach { element ->
                (element as? JsonObject)?.string("name")?.let { tracks += MigrationTrack(it, "") }
            }
        }
        require(tracks.isNotEmpty()) { "Apple Music did not show the songs of this playlist." }
        return MigrationSource("Apple Music", name, tracks.distinct())
    }

    private suspend fun deezer(repository: WindowsMusicRepository, link: String): MigrationSource {
        val id = Regex("playlist/(\\d+)").find(link)?.groupValues?.get(1)
            ?: Regex("playlist/(\\d+)").find(repository.fetchPublicPage(link))?.groupValues?.get(1)
            ?: error("That Deezer link is not a playlist.")
        val playlist = json.parseToJsonElement(repository.fetchPublicPage("https://api.deezer.com/playlist/$id")).jsonObject
        playlist["error"]?.let { error("Deezer did not show this playlist (it may be private).") }
        val name = playlist.string("title") ?: "Deezer playlist"
        val tracks = mutableListOf<MigrationTrack>()
        var page: JsonObject? = playlist["tracks"] as? JsonObject
        var pages = 0
        while (page != null && pages < 25) {
            (page["data"] as? JsonArray)?.forEach { element ->
                val item = element as? JsonObject ?: return@forEach
                val artist = (item["artist"] as? JsonObject)?.string("name").orEmpty()
                item.string("title")?.let { tracks += MigrationTrack(it, artist, (item.long("duration") ?: 0L) * 1000L) }
            }
            val next = page.string("next") ?: break
            page = json.parseToJsonElement(repository.fetchPublicPage(next)).jsonObject
            pages++
        }
        require(tracks.isNotEmpty()) { "This Deezer playlist has no songs to bring over." }
        return MigrationSource("Deezer", name, tracks)
    }

    private suspend fun youTube(repository: WindowsMusicRepository, link: String): MigrationSource {
        val id = Regex("[?&]list=([A-Za-z0-9_-]+)").find(link)?.groupValues?.get(1) ?: error("That YouTube link is not a playlist.")
        val page = repository.catalogPage("VL$id", "Playlist")
        require(page.tracks.isNotEmpty()) { "YouTube did not show the songs of this playlist." }
        return MigrationSource(
            "YouTube",
            page.title.ifBlank { "YouTube playlist" },
            page.tracks.map { MigrationTrack(it.title, it.artist, it.durationMs) },
            ready = page.tracks,
        )
    }

    /**
     * Each song looked up on YouTube Music (a few at a time); [onProgress] counts the songs
     * done. A song with no close match is left out (and listed as not found).
     */
    suspend fun match(
        repository: WindowsMusicRepository,
        source: MigrationSource,
        onProgress: (done: Int, total: Int) -> Unit,
    ): List<MigrationMatch> {
        if (source.ready.isNotEmpty()) {
            onProgress(source.ready.size, source.ready.size)
            return source.ready.map { MigrationMatch(MigrationTrack(it.title, it.artist, it.durationMs), it) }
        }
        val gate = Semaphore(4)
        val counter = kotlinx.coroutines.sync.Mutex()
        var done = 0
        return coroutineScope {
            source.tracks.map { wanted ->
                async {
                    gate.withPermit {
                        val found = runCatching { bestMatch(repository, wanted) }.getOrNull()
                        val count = counter.withLock { ++done }
                        onProgress(count, source.tracks.size)
                        MigrationMatch(wanted, found)
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun bestMatch(repository: WindowsMusicRepository, wanted: MigrationTrack): Track? {
        val query = listOf(wanted.title, wanted.artist.substringBefore(",")).filter(String::isNotBlank).joinToString(" ")
        val candidates = repository.search(query, "songs").tracks.take(8)
        val scored = candidates.map { it to score(wanted, it) }.maxByOrNull { it.second } ?: return null
        return scored.first.takeIf { scored.second >= 0.45 }
    }

    /** 0..1: how surely [candidate] is [wanted] (title words, artist, length). */
    private fun score(wanted: MigrationTrack, candidate: Track): Double {
        val titleWords = words(wanted.title)
        val candidateWords = words(candidate.title)
        val titleScore = if (titleWords.isEmpty()) 0.0 else titleWords.count { it in candidateWords }.toDouble() / titleWords.size
        val artistScore = if (wanted.artist.isBlank()) 0.5 else {
            val wantedArtists = words(wanted.artist)
            val candidateArtists = words(candidate.artist)
            if (wantedArtists.isEmpty()) 0.5 else wantedArtists.count { it in candidateArtists }.toDouble() / wantedArtists.size
        }
        val lengthScore = if (wanted.durationMs <= 0L || candidate.durationMs <= 0L) 0.5 else {
            val diff = kotlin.math.abs(wanted.durationMs - candidate.durationMs)
            when { diff < 4_000L -> 1.0; diff < 12_000L -> 0.6; diff < 30_000L -> 0.3; else -> 0.0 }
        }
        return titleScore * 0.55 + artistScore * 0.3 + lengthScore * 0.15
    }

    private fun words(text: String): Set<String> =
        text.lowercase()
            .replace(Regex("\\((feat|ft|with)[^)]*\\)|\\[(feat|ft|with)[^]]*]"), " ")
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .split(' ').filter { it.length > 1 || it.firstOrNull()?.isDigit() == true }
            .toSet()

    /** The playlist's songs as text, one "Artist - Title" per line (most transfer services read this). */
    fun exportText(name: String, tracks: List<Track>, link: String?): String = buildString {
        appendLine(name)
        link?.let { appendLine(it) }
        appendLine()
        tracks.forEach { appendLine(listOf(it.artist, it.title).filter(String::isNotBlank).joinToString(" - ")) }
    }.trimEnd()

    /**
     * The playlist as a .csv file in the column names most transfer services read
     * (TuneMyMusic, Soundiiz, SongShift's file import): one song per row.
     */
    fun exportCsv(name: String, tracks: List<Track>): String = buildString {
        fun cell(value: String) = if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + value.replace("\"", "\"\"") + "\"" else value
        append("Track name,Artist name,Album,Playlist name,Duration (seconds),YouTube video ID\r\n")
        tracks.forEach { track ->
            val artist = track.artist.takeUnless { it.isPlaceholderArtist() }.orEmpty()
            append(listOf(track.title, artist, track.album, name, if (track.durationMs > 0) (track.durationMs / 1000).toString() else "", track.id).joinToString(",") { cell(it) })
            append("\r\n")
        }
    }

    /** A file name for [name] without characters that Windows, Android or iOS refuse. */
    fun exportFileName(name: String, extension: String): String =
        (name.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), " ").trim().ifBlank { "Playlist" }.take(80)) + "." + extension

    /** Songs found in the page's text: after each "spotify:track:" its title, artists and length. */
    private fun spotifyTracksInText(page: String): List<MigrationTrack> {
        val text = unescapeJsonText(page)
        val found = LinkedHashMap<String, MigrationTrack>()
        Regex("\"uri\":\"spotify:track:([A-Za-z0-9]+)\"").findAll(text).forEach { match ->
            val id = match.groupValues[1]
            if (id in found) return@forEach
            val window = text.substring(match.range.last, minOf(text.length, match.range.last + 900))
            val end = window.indexOf("\"uri\":\"spotify:").takeIf { it > 0 } ?: window.length
            val item = window.substring(0, end)
            val title = Regex("\"title\":\"((?:[^\"\\\\]|\\\\.)*)\"").find(item)?.groupValues?.get(1) ?: return@forEach
            val artist = Regex("\"subtitle\":\"((?:[^\"\\\\]|\\\\.)*)\"").find(item)?.groupValues?.get(1).orEmpty()
            val duration = Regex("\"duration\":(\\d+)").find(item)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            found[id] = MigrationTrack(title, artist.replace("\u00a0", " "), duration)
        }
        return found.values.toList()
    }

    /** JSON kept as text inside a script string (\" and \uXXXX) back to plain JSON text. */
    private fun unescapeJsonText(text: String): String {
        var out = text
        repeat(2) {
            out = out.replace("\\\\\"", "\"").replace("\\\"", "\"")
        }
        return Regex("\\\\u([0-9a-fA-F]{4})").replace(out) { it.groupValues[1].toInt(16).toChar().toString() }
    }

    // --- page reading ---

    private fun scriptJson(page: String, id: String): JsonElement? {
        val start = Regex("<script[^>]*id=\"$id\"[^>]*>").find(page) ?: return null
        val end = page.indexOf("</script>", start.range.last)
        if (end < 0) return null
        return runCatching { json.parseToJsonElement(page.substring(start.range.last + 1, end)) }.getOrNull()
    }

    private fun ldJson(page: String): JsonObject? =
        Regex("<script[^>]*type=\"application/ld\\+json\"[^>]*>(.*?)</script>", RegexOption.DOT_MATCHES_ALL).findAll(page)
            .mapNotNull { runCatching { json.parseToJsonElement(it.groupValues[1]) }.getOrNull() as? JsonObject }
            .firstOrNull { it.string("@type")?.contains("Playlist") == true }

    private fun findObject(element: JsonElement, test: (JsonObject) -> Boolean): JsonObject? = when (element) {
        is JsonObject -> if (test(element)) element else element.values.firstNotNullOfOrNull { findObject(it, test) }
        is JsonArray -> element.firstNotNullOfOrNull { findObject(it, test) }
        else -> null
    }

    private fun collectObjects(element: JsonElement, test: (JsonObject) -> Boolean): List<JsonObject> = buildList {
        fun walk(node: JsonElement) {
            when (node) {
                is JsonObject -> if (test(node)) add(node) else node.values.forEach(::walk)
                is JsonArray -> node.forEach(::walk)
                else -> Unit
            }
        }
        walk(element)
    }

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf(String::isNotBlank)
    private fun JsonObject.long(key: String): Long? = (this[key] as? JsonPrimitive)?.let { it.longOrNull ?: it.contentOrNull?.toDoubleOrNull()?.toLong() }

    private fun String.htmlDecoded(): String =
        replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&#x27;", "'").replace("&lt;", "<").replace("&gt;", ">")
}
