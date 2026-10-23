package com.recordlabs.desktop.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Collections

data class WindowsAnimatedArtwork(
    val streamUrl: String,
    val staticUrl: String?,
)

internal data class ArtworkQueryCandidate(
    val title: String,
    val artist: String,
    val album: String?,
    val durationSeconds: Int,
)

object WindowsAnimatedArtworkRepository {
    private const val MEMORY_CACHE_LIMIT = 64
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 8_000
            socketTimeoutMillis = 15_000
        }
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }
    private val positiveCache = LinkedHashMap<String, WindowsAnimatedArtwork>(24, 0.75f, true)
    private val definitiveMisses = Collections.synchronizedSet(LinkedHashSet<String>())

    fun cacheKey(track: Track): String =
        "${normalize(track.title)}|${normalize(track.artist)}|${normalize(track.album.ifBlank { track.streams })}"

    fun invalidate(track: Track) {
        val titleNorm = normalize(track.title)
        val artistNorm = normalize(track.artist)
        synchronized(positiveCache) {
            val toRemove = positiveCache.keys.filter { it.startsWith("$titleNorm|$artistNorm|") || it == "$titleNorm|$artistNorm" }
            toRemove.forEach { positiveCache.remove(it) }
        }
        definitiveMisses.removeIf { it.startsWith("$titleNorm|$artistNorm|") || it == "$titleNorm|$artistNorm" }
    }

    fun clearDefinitiveMiss(track: Track) {
        val titleNorm = normalize(track.title)
        val artistNorm = normalize(track.artist)
        definitiveMisses.removeIf { it.startsWith("$titleNorm|$artistNorm|") || it == "$titleNorm|$artistNorm" }
    }

    fun clearAllMisses() {
        definitiveMisses.clear()
    }

    fun isDefinitiveMiss(track: Track): Boolean {
        val key = cacheKey(track)
        return definitiveMisses.contains(key)
    }

    internal suspend fun resolveAlbumFromItunes(title: String, artist: String): String? = runCatching {
        val cleanArtist = artist.split(Regex("[,&]|\\s+(?:feat\\.?|ft\\.?|featuring)\\s+", RegexOption.IGNORE_CASE)).firstOrNull()?.trim() ?: artist
        val cleanTitle = cleanVideoNoise(title)
        val query = "$cleanArtist $cleanTitle"
        val responseText: String = client.get("https://itunes.apple.com/search") {
            parameter("term", query)
            parameter("entity", "song")
            parameter("limit", "10")
            header("User-Agent", "RecordLabs-Windows/0.1.1")
        }.body()

        val root = jsonParser.parseToJsonElement(responseText).jsonObject
        val results = root["results"] as? JsonArray ?: return@runCatching null

        data class AlbumCandidate(val name: String, val score: Int)
        val candidates = mutableListOf<AlbumCandidate>()
        val expectedTitleVersions = extractVersionTags(cleanTitle)

        for (item in results) {
            val obj = item as? JsonObject ?: continue
            val trackName = obj["trackName"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val collectionName = obj["collectionName"]?.jsonPrimitive?.contentOrNull.orEmpty()
            if (collectionName.isBlank() || (!metadataMatches(title, trackName) && !metadataMatches(cleanTitle, trackName))) {
                continue
            }

            val collectionVersions = extractVersionTags(collectionName)
            // Reject collections with version tags that the track doesn't have (e.g. track is not a cappella/live/instrumental)
            if ((collectionVersions - expectedTitleVersions).isNotEmpty()) {
                continue
            }

            var score = 0
            val cleanCollection = collectionName.replace(Regex("""\s*[\\[\\(].*?(?:message|bonus).*?[\\]\\)]""", RegexOption.IGNORE_CASE), "").trim()
            val baseCollection = cleanMetadataString(cleanCollection)

            if (collectionName.equals(cleanTitle, ignoreCase = true)) {
                score += 50
            } else if (baseCollection.equals(cleanTitle, ignoreCase = true)) {
                score += 40
            }

            // Prefer collections with fewer words (studio albums over lengthy repackages)
            score -= collectionName.split(" ").filter { it.isNotBlank() }.size

            if (collectionName.contains(":") || collectionName.contains(" - ")) {
                score -= 10
            }
            if (collectionName.contains("deluxe", ignoreCase = true)) {
                score -= 5
            }

            candidates.add(AlbumCandidate(cleanCollection.ifBlank { collectionName }, score))
        }

        candidates.maxByOrNull { it.score }?.name
    }.getOrNull()

    private val appleHlsResolutionRegex = Regex("""\bRESOLUTION=(\d+)x(\d+)""", RegexOption.IGNORE_CASE)

    private fun selectCompatibleAppleAvcPlaylist(masterPlaylist: String): String? {
        val lines = masterPlaylist.lineSequence().map(String::trim).filter(String::isNotEmpty).toList()
        return lines.mapIndexedNotNull { index, line ->
            if (!line.startsWith("#EXT-X-STREAM-INF:", ignoreCase = true) ||
                !line.contains("CODECS=\"avc1", ignoreCase = true)
            ) {
                return@mapIndexedNotNull null
            }
            val resolution = appleHlsResolutionRegex.find(line) ?: return@mapIndexedNotNull null
            val width = resolution.groupValues[1].toIntOrNull() ?: return@mapIndexedNotNull null
            val height = resolution.groupValues[2].toIntOrNull() ?: return@mapIndexedNotNull null
            val uri = lines.drop(index + 1).firstOrNull { !it.startsWith("#") }
                ?: return@mapIndexedNotNull null
            Triple(width * height, maxOf(width, height), uri)
        }
            .filter { (_, longestEdge, _) -> longestEdge <= 1440 }
            .maxByOrNull { (pixelCount, _, _) -> pixelCount }
            ?.third
    }

    private fun firstAppleMp4Path(mediaPlaylist: String): String? = mediaPlaylist
        .lineSequence()
        .map(String::trim)
        .firstOrNull { it.isNotEmpty() && !it.startsWith("#") && it.substringBefore('?').endsWith(".mp4") }

    private suspend fun resolveCompatibleAppleAvcMp4(masterUrl: String): String? = runCatching {
        val masterPlaylist: String = client.get(masterUrl) {
            header("User-Agent", "RecordLabs-Windows/0.1.1")
        }.body()
        val mediaPlaylistPath = selectCompatibleAppleAvcPlaylist(masterPlaylist) ?: return@runCatching null
        val mediaPlaylistUrl = java.net.URI(masterUrl).resolve(mediaPlaylistPath).toString()
        val mediaPlaylist: String = client.get(mediaPlaylistUrl) {
            header("User-Agent", "RecordLabs-Windows/0.1.1")
        }.body()
        val mp4Path = firstAppleMp4Path(mediaPlaylist) ?: return@runCatching null
        java.net.URI(mediaPlaylistUrl).resolve(mp4Path).toString()
    }.getOrNull()

    private suspend fun extractStreamUrl(response: JsonObject): String? {
        val directMp4 = sequenceOf("videoUrl", "videoUrlVertical")
            .mapNotNull { response[it]?.jsonPrimitive?.contentOrNull }
            .firstOrNull { isAllowedAppleVideoUrl(it) && it.substringBefore('?').endsWith(".mp4") }
        if (directMp4 != null) return directMp4

        val hlsStream = sequenceOf("animated", "animatedVertical", "videoUrl", "videoUrlVertical")
            .mapNotNull { response[it]?.jsonPrimitive?.contentOrNull }
            .firstOrNull(::isAllowedAppleVideoUrl) ?: return null

        if (hlsStream.substringBefore('?').endsWith(".mp4")) return hlsStream
        return resolveCompatibleAppleAvcMp4(hlsStream) ?: hlsStream
    }

    private val YOUTUBE_MEDIA_NOISE_REGEX = Regex(
        """\s*(?:\(|\[)\s*(?:official\s+(?:music\s+)?video|official\s+audio|official\s+lyric\s+video|official\s+visualizer|music\s+video|lyric\s+video|video\s+clip|clip\s+officiel|visualizer|lyrics?|audio|performance\s+video|\d+k|hd|hq)[^()\[\]]*(?:\)|\])""",
        RegexOption.IGNORE_CASE
    )
    private val YOUTUBE_TRAILING_NOISE_REGEX = Regex(
        """\s*(?:[|\-–—/]|//)\s*(?:official\s+(?:music\s+)?video|official\s+audio|official\s+video|lyrics?|audio|visualizer)\s*$""",
        RegexOption.IGNORE_CASE
    )

    internal fun cleanVideoNoise(value: String): String = value
        .replace(YOUTUBE_MEDIA_NOISE_REGEX, "")
        .replace(YOUTUBE_TRAILING_NOISE_REGEX, "")
        .trim()

    private val METADATA_CLEAN_REGEX = Regex(
        """\s*(?:\(|\[)\s*[^()\[\]]*(?:feat\.?|ft\.?|featuring|remaster(?:ed)?|deluxe|anniversary|expanded|edition|bonus\s+track)[^()\[\]]*(?:\)|\])""",
        RegexOption.IGNORE_CASE
    )

    internal fun cleanMetadataString(value: String): String {
        val protectedValue = value.replace(Regex("""(?i)\b(taylor['’]s\s*version)\b"""), "###TAYLORS_VERSION###")
        val cleaned = protectedValue
            .replace(METADATA_CLEAN_REGEX, "")
            .replace("###TAYLORS_VERSION###", "Taylor's Version")
            .trim()
        return cleaned.ifBlank { value.trim() }
    }

    private val VERSION_TAG_REGEX = Regex(
        """\b(taylor['’]s\s*version|10\s*minute\s*version|a\s*cap+el+a(?:\s*version)?|acappella|instrumental(?:\s*version)?|karaoke|live(?:\s*version)?|acoustic(?:\s*version)?|remix|orchestral|demo|sped\s*up|slowed)\b""",
        RegexOption.IGNORE_CASE
    )

    private fun canonicalVersionTag(tag: String): String {
        val v = tag.lowercase().replace("’", "'").replace(Regex("\\s+"), " ").trim()
        return when {
            v.contains("cap") -> "a cappella"
            v.contains("instrumental") -> "instrumental"
            v.contains("live") -> "live"
            v.contains("acoustic") -> "acoustic"
            else -> v
        }
    }

    internal fun extractVersionTags(value: String): Set<String> =
        VERSION_TAG_REGEX.findAll(value)
            .map { canonicalVersionTag(it.value) }
            .toSet()

    private fun normalizeVersionText(value: String): String =
        VERSION_TAG_REGEX.replace(value) { match ->
            " " + canonicalVersionTag(match.value) + " "
        }

    internal fun metadataMatches(expected: String, actual: String): Boolean {
        if (expected.isBlank() || actual.isBlank()) return false

        // 1. Version tag consistency: if expected or actual specifies a distinctive version
        // (such as "Taylor's Version", "Live", "Remix", "Acoustic", "A Cappella"), both must strictly agree!
        val expectedVersions = extractVersionTags(expected)
        val actualVersions = extractVersionTags(actual)
        if (expectedVersions != actualVersions) {
            return false
        }

        val a = normalize(expected)
        val b = normalize(actual)
        if (a == b) return true

        val wordsA = a.split(" ").filter { it.isNotBlank() }
        val wordsB = b.split(" ").filter { it.isNotBlank() }
        if (wordsA.isEmpty() || wordsB.isEmpty()) return false
        if (wordsA == wordsB) return true

        val shorter = if (wordsA.size <= wordsB.size) wordsA else wordsB
        val longer = if (wordsA.size <= wordsB.size) wordsB else wordsA

        // All words of the shorter title must be present in the longer title
        if (shorter.all { it in longer }) {
            val ratio = shorter.size.toDouble() / longer.size
            if (ratio >= 0.65 || (shorter.size >= 2 && longer.size - shorter.size <= 2)) {
                return true
            }
        }

        return false
    }

    internal suspend fun buildQueryCandidates(track: Track): List<ArtworkQueryCandidate> {
        val durationSec = if (track.durationMs > 0) (track.durationMs / 1000).toInt() else 0
        val candidates = mutableListOf<ArtworkQueryCandidate>()

        val primaryArtist = track.artist
            .split(Regex("[,&]|\\s+(?:feat\\.?|ft\\.?|featuring)\\s+", RegexOption.IGNORE_CASE))
            .firstOrNull()?.trim().orEmpty().ifBlank { track.artist.trim() }

        val cleanTitle = cleanVideoNoise(track.title).ifBlank { track.title.trim() }
        val rawAlbum = track.album.ifBlank { track.streams }.trim()
        val cleanAlbum = if (rawAlbum.isNotBlank()) cleanVideoNoise(rawAlbum) else null
        val expectedTitleVersions = extractVersionTags(cleanTitle)

        // 1. If the track already has an album provided (e.g. from album page or local tags):
        if (!cleanAlbum.isNullOrBlank()) {
            val albumVersions = extractVersionTags(cleanAlbum)
            // Reject albums whose version tags conflict with the track title
            // (e.g. don't query a cappella album if the track itself is standard vocal)
            val albumVersionMismatch = (albumVersions - expectedTitleVersions).isNotEmpty()

            if (!albumVersionMismatch) {
                candidates.add(
                    ArtworkQueryCandidate(
                        title = cleanTitle,
                        artist = track.artist.trim(),
                        album = rawAlbum,
                        durationSeconds = durationSec,
                    )
                )
                if (cleanAlbum != rawAlbum) {
                    candidates.add(
                        ArtworkQueryCandidate(
                            title = cleanTitle,
                            artist = track.artist.trim(),
                            album = cleanAlbum,
                            durationSeconds = durationSec,
                        )
                    )
                }
                if (track.artist.trim() != primaryArtist) {
                    candidates.add(
                        ArtworkQueryCandidate(
                            title = cleanTitle,
                            artist = primaryArtist,
                            album = cleanAlbum,
                            durationSeconds = durationSec,
                        )
                    )
                }

                // If cleanAlbum has an edition/deluxe tag (e.g. "eternal sunshine (slightly deluxe)"),
                // also try the base studio album (e.g. "eternal sunshine") as a direct candidate!
                val baseAlbum = cleanMetadataString(cleanAlbum)
                if (baseAlbum != cleanAlbum && baseAlbum.isNotBlank()) {
                    candidates.add(
                        ArtworkQueryCandidate(
                            title = cleanTitle,
                            artist = primaryArtist,
                            album = baseAlbum,
                            durationSeconds = durationSec,
                        )
                    )
                }
            }
        }

        // 2. Exact clean title and artist WITHOUT album.
        // Apple Music's index on artwork.boidu.dev resolves the official song directly from title and artist!
        // This is extremely accurate for "Red (Taylor's Version)", "Fearless (Taylor's Version)", etc.
        candidates.add(
            ArtworkQueryCandidate(
                title = cleanTitle,
                artist = primaryArtist,
                album = null,
                durationSeconds = durationSec,
            )
        )
        if (track.artist.trim() != primaryArtist) {
            candidates.add(
                ArtworkQueryCandidate(
                    title = cleanTitle,
                    artist = track.artist.trim(),
                    album = null,
                    durationSeconds = durationSec,
                )
            )
        }

        // 3. Studio album resolved from iTunes (with intelligent scoring and version filtering)
        val resolvedAlbum = if (cleanAlbum.isNullOrBlank() || (extractVersionTags(cleanAlbum) - expectedTitleVersions).isNotEmpty()) {
            resolveAlbumFromItunes(cleanTitle, primaryArtist)
        } else null

        if (!resolvedAlbum.isNullOrBlank()) {
            candidates.add(
                ArtworkQueryCandidate(
                    title = cleanTitle,
                    artist = primaryArtist,
                    album = resolvedAlbum,
                    durationSeconds = durationSec,
                )
            )
        }

        // 4. Fallback: try raw uncleaned title if cleanTitle was different
        if (track.title.trim() != cleanTitle) {
            candidates.add(
                ArtworkQueryCandidate(
                    title = track.title.trim(),
                    artist = primaryArtist,
                    album = cleanAlbum ?: resolvedAlbum,
                    durationSeconds = durationSec,
                )
            )
        }

        // 5. Relaxed metadata fallback (e.g. strip feat / remaster / deluxe)
        // ONLY if the title does not have a protected version tag (like Taylor's Version or Live)
        val relaxedTitle = cleanMetadataString(cleanTitle)
        if (relaxedTitle != cleanTitle && extractVersionTags(cleanTitle).isEmpty()) {
            candidates.add(
                ArtworkQueryCandidate(
                    title = relaxedTitle,
                    artist = primaryArtist,
                    album = cleanAlbum ?: resolvedAlbum,
                    durationSeconds = durationSec,
                )
            )
            candidates.add(
                ArtworkQueryCandidate(
                    title = relaxedTitle,
                    artist = primaryArtist,
                    album = null,
                    durationSeconds = durationSec,
                )
            )
        }

        return candidates.distinct()
    }

    /** Lookup off the UI thread: the HTTP replies are parsed where this runs. */
    suspend fun find(track: Track, forceRefresh: Boolean = false): WindowsAnimatedArtwork? =
        withContext(Dispatchers.IO) { findOnCaller(track, forceRefresh) }

    private suspend fun findOnCaller(track: Track, forceRefresh: Boolean): WindowsAnimatedArtwork? {
        if (track.title.isBlank() || track.artist.isBlank()) return null
        val key = cacheKey(track)

        if (forceRefresh) {
            invalidate(track)
        } else {
            synchronized(positiveCache) {
                positiveCache[key]?.let { return it }
            }
            if (definitiveMisses.contains(key)) return null
        }

        val primaryArtist = track.artist
            .split(Regex("[,&]|\\s+(?:feat\\.?|ft\\.?|featuring)\\s+", RegexOption.IGNORE_CASE))
            .firstOrNull()?.trim().orEmpty().ifBlank { track.artist.trim() }
        val cleanTitle = cleanVideoNoise(track.title).ifBlank { track.title.trim() }

        val candidates = buildQueryCandidates(track).toMutableList()

        for (attempt in 0..1) {
            var allCandidates404OrEmpty = true

            val result = runCatching {
                for (candidate in candidates) {
                    val responseHttp = try {
                        client.get("https://artwork.boidu.dev/") {
                            parameter("s", candidate.title)
                            parameter("a", candidate.artist)
                            if (!candidate.album.isNullOrBlank()) {
                                parameter("albumName", candidate.album)
                            }
                            if (candidate.durationSeconds > 0) {
                                parameter("duration", candidate.durationSeconds)
                            }
                            header("User-Agent", "RecordLabs-Windows/0.1.1")
                        }
                    } catch (e: ClientRequestException) {
                        if (e.response.status.value == 404) {
                            continue
                        }
                        allCandidates404OrEmpty = false
                        throw e
                    } catch (e: Exception) {
                        allCandidates404OrEmpty = false
                        throw e
                    }

                    val response: JsonObject = responseHttp.body()
                    val matchedTitle = response["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val matchedArtist = response["artist"]?.jsonPrimitive?.contentOrNull.orEmpty()

                    val titleMatches = metadataMatches(track.title, matchedTitle) || metadataMatches(cleanTitle, matchedTitle)
                    val artistMatches = metadataMatches(track.artist, matchedArtist) || metadataMatches(primaryArtist, matchedArtist)

                    if (!titleMatches || !artistMatches) {
                        continue
                    }

                    val stream = extractStreamUrl(response)

                    if (stream != null) {
                        return@runCatching WindowsAnimatedArtwork(
                            streamUrl = stream,
                            staticUrl = response["static"]?.jsonPrimitive?.contentOrNull,
                        )
                    }
                }

                // If all candidates failed, try resolving the official studio album from iTunes
                val existingAlbum = track.album.ifBlank { track.streams }
                val fallbackAlbum = resolveAlbumFromItunes(cleanTitle, primaryArtist)
                if (!fallbackAlbum.isNullOrBlank() && !fallbackAlbum.equals(existingAlbum, ignoreCase = true)) {
                    val fallbackResponse = try {
                        client.get("https://artwork.boidu.dev/") {
                            parameter("s", cleanTitle)
                            parameter("a", primaryArtist)
                            parameter("albumName", fallbackAlbum)
                            header("User-Agent", "RecordLabs-Windows/0.1.1")
                        }
                    } catch (e: Exception) { null }

                    if (fallbackResponse != null) {
                        val respJson: JsonObject = fallbackResponse.body()
                        val matchedTitle = respJson["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
                        val matchedArtist = respJson["artist"]?.jsonPrimitive?.contentOrNull.orEmpty()
                        val titleMatches = metadataMatches(track.title, matchedTitle) || metadataMatches(cleanTitle, matchedTitle)
                        val artistMatches = metadataMatches(track.artist, matchedArtist) || metadataMatches(primaryArtist, matchedArtist)

                        if (titleMatches && artistMatches) {
                            val stream = extractStreamUrl(respJson)
                            if (stream != null) {
                                return@runCatching WindowsAnimatedArtwork(
                                    streamUrl = stream,
                                    staticUrl = respJson["static"]?.jsonPrimitive?.contentOrNull,
                                )
                            }
                        }
                    }
                }

                null
            }

            val artwork = result.getOrNull()
            if (artwork != null) {
                synchronized(positiveCache) {
                    positiveCache[key] = artwork
                    while (positiveCache.size > MEMORY_CACHE_LIMIT) {
                        val firstKey = positiveCache.keys.firstOrNull() ?: break
                        positiveCache.remove(firstKey)
                    }
                }
                definitiveMisses.remove(key)
                return artwork
            }

            if (result.isSuccess && allCandidates404OrEmpty) {
                definitiveMisses.add(key)
                return null
            }

            // Temporary network failure: brief pause before second attempt
            if (attempt == 0) {
                delay(1200L)
            }
        }

        return null
    }

    private fun isAllowedAppleVideoUrl(value: String): Boolean = runCatching {
        val uri = java.net.URI(value)
        uri.scheme.equals("https", true) && (uri.host.endsWith("apple.com", true) || uri.host.endsWith("mzstatic.com", true))
    }.getOrDefault(false)

    private fun normalize(value: String): String = normalizeVersionText(
        value.lowercase()
            .replace(Regex("""(?i)\s*(?:\(|\[)[^()\[\]]*(?:official|video|audio|lyrics?|visualizer)[^()\[\]]*(?:\)|\])"""), "")
    )
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
}
