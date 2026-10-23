package com.recordlabs.desktop.data

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * A music video's picture stream (YouTube DASH, fragmented MP4) prepared so the
 * VLC decoder can start it at any point.
 *
 * VLC 3's MP4 demuxer cannot seek in fragmented MP4 that only carries a `sidx`
 * index (YouTube's format): `:start-time` and `setTime()` silently play from
 * the beginning. Instead of seeking, [urlFrom] serves a virtual file made of
 * the init segment (ftyp + moov) followed by the stream from fragment N on,
 * which VLC plays linearly with the fragment's real timestamps. The fragment
 * table comes from the `sidx` box, so the start time of every fragment and the
 * exact video length are known without trusting VLC's own clock.
 */
class MusicVideoStream internal constructor(
    val videoId: String,
    internal val remoteUrl: String,
    internal val userAgent: String,
    internal val contentLength: Long,
    internal val init: ByteArray,
    /** Fragment start times (ms) and byte offsets, in order. */
    val fragments: List<Fragment>,
    /** Length of the picture stream itself (may differ slightly from the audio). */
    val durationMs: Long,
) {
    data class Fragment(val startMs: Long, val offset: Long)

    /**
     * Real picture size from the format list (0 = unknown). VLC can report a
     * padded size for some codecs (AV1 frames are aligned to 128 pixels), so
     * the decoder takes the shape from here when it is known.
     */
    @Volatile var pictureWidth: Int = 0
    @Volatile var pictureHeight: Int = 0
    /** YouTube's quality step of the picture ("2160" for 4K), 0 = unknown. */
    @Volatile var pictureQuality: Int = 0

    /** Last fragment starting at or before [positionMs]. */
    fun fragmentIndexFor(positionMs: Long): Int {
        var low = 0
        var high = fragments.lastIndex
        var found = 0
        while (low <= high) {
            val mid = (low + high) ushr 1
            if (fragments[mid].startMs <= positionMs) {
                found = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return found
    }

    /** Remote byte offset where fragment [index] ends (exclusive). */
    internal fun fragmentEnd(index: Int): Long = if (index < fragments.lastIndex) fragments[index + 1].offset else contentLength

    /** Fragment holding remote byte [offset]. */
    internal fun fragmentIndexAtOffset(offset: Long): Int {
        var low = 0
        var high = fragments.lastIndex
        var found = 0
        while (low <= high) {
            val mid = (low + high) ushr 1
            if (fragments[mid].offset <= offset) {
                found = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return found
    }

    /** Loopback URL of the virtual file starting with fragment [index]. */
    fun urlFrom(index: Int): String = MusicVideoStreamServer.urlFor(this, index.coerceIn(0, fragments.lastIndex))

    companion object {
        /**
         * Builds the stream from the file's first bytes (init segment + sidx,
         * i.e. byte 0 up to the end of YouTube's `indexRange`). Returns null for
         * anything that is not a single-track fragmented MP4 with a sidx index.
         */
        internal fun parse(
            videoId: String,
            remoteUrl: String,
            userAgent: String,
            contentLength: Long,
            head: ByteArray,
        ): MusicVideoStream? = runCatching {
            val buffer = ByteBuffer.wrap(head)
            var position = 0
            var moovEnd = -1
            var sidxStart = -1
            var sidxSize = 0
            while (position + 8 <= head.size) {
                var size = buffer.getInt(position).toLong() and 0xffffffffL
                val type = String(head, position + 4, 4, Charsets.US_ASCII)
                var headerSize = 8
                if (size == 1L && position + 16 <= head.size) {
                    size = buffer.getLong(position + 8)
                    headerSize = 16
                }
                if (size < headerSize) break
                when (type) {
                    "moov" -> moovEnd = (position + size).toInt()
                    "sidx" -> {
                        sidxStart = position
                        sidxSize = size.toInt()
                    }
                }
                if (type == "sidx") break
                position += size.toInt()
            }
            if (moovEnd <= 0 || sidxStart < moovEnd || sidxStart + sidxSize > head.size) return@runCatching null

            val sidx = ByteBuffer.wrap(head, sidxStart, sidxSize).slice()
            val version = sidx.get(8).toInt() and 0xff
            val timescale = sidx.getInt(16).toLong() and 0xffffffffL
            if (timescale <= 0L) return@runCatching null
            var cursor: Int
            val earliest: Long
            val firstOffset: Long
            if (version == 0) {
                earliest = sidx.getInt(20).toLong() and 0xffffffffL
                firstOffset = sidx.getInt(24).toLong() and 0xffffffffL
                cursor = 28
            } else {
                earliest = sidx.getLong(20)
                firstOffset = sidx.getLong(28)
                cursor = 36
            }
            val referenceCount = sidx.getShort(cursor + 2).toInt() and 0xffff
            cursor += 4
            if (referenceCount == 0) return@runCatching null
            var offset = sidxStart.toLong() + sidxSize + firstOffset
            var time = earliest
            val fragments = ArrayList<Fragment>(referenceCount)
            repeat(referenceCount) {
                val sizeWord = sidx.getInt(cursor).toLong() and 0xffffffffL
                val duration = sidx.getInt(cursor + 4).toLong() and 0xffffffffL
                cursor += 12
                // A set reference-type bit points at another sidx (not used by YouTube).
                if (sizeWord and 0x80000000L != 0L) return@runCatching null
                fragments += Fragment(startMs = time * 1000L / timescale, offset = offset)
                offset += sizeWord and 0x7fffffffL
                time += duration
            }
            if (fragments.last().offset >= contentLength) return@runCatching null
            MusicVideoStream(
                videoId = videoId,
                remoteUrl = remoteUrl,
                userAgent = userAgent,
                contentLength = contentLength,
                init = head.copyOfRange(0, moovEnd),
                fragments = fragments,
                durationMs = time * 1000L / timescale,
            )
        }.getOrNull()
    }
}

/** Loopback server for [MusicVideoStream] virtual files (separate from the audio proxy). */
/**
 * Picture fragments already downloaded once (whole fragments only), so a seek
 * back, or a restart of the picture, starts from memory at once instead of
 * waiting for the network again. Least recently used fragments go first.
 */
internal object MusicVideoFragmentCache {
    private const val BUDGET_BYTES = 160L * 1024 * 1024
    private val entries = LinkedHashMap<Pair<String, Int>, ByteArray>(64, .75f, true)
    private var totalBytes = 0L

    @Synchronized
    fun get(stream: MusicVideoStream, index: Int): ByteArray? = entries[stream.remoteUrl to index]

    @Synchronized
    fun put(stream: MusicVideoStream, index: Int, data: ByteArray) {
        val key = stream.remoteUrl to index
        entries.put(key, data)?.let { totalBytes -= it.size }
        totalBytes += data.size
        val iterator = entries.entries.iterator()
        while (totalBytes > BUDGET_BYTES && iterator.hasNext()) {
            val eldest = iterator.next()
            if (eldest.key == key) continue
            totalBytes -= eldest.value.size
            iterator.remove()
        }
    }
}

internal object MusicVideoStreamServer {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "record-labs-music-video-stream").apply { isDaemon = true }
    }
    private val streams = object : LinkedHashMap<String, MusicVideoStream>(16, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, MusicVideoStream>?) = size > 16
    }
    private val tokens = HashMap<MusicVideoStream, String>()

    @Volatile private var server: HttpServer? = null

    @Synchronized
    private fun ensureServer(): HttpServer = server ?: HttpServer.create(
        InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0,
    ).apply {
        createContext("/mv") { exchange -> serve(exchange) }
        executor = this@MusicVideoStreamServer.executor
        start()
        server = this
    }

    fun urlFor(stream: MusicVideoStream, index: Int): String {
        val port = ensureServer().address.port
        val token = synchronized(streams) {
            tokens[stream]?.takeIf { streams.containsKey(it) } ?: UUID.randomUUID().toString().also { token ->
                tokens.entries.removeIf { !streams.containsKey(it.value) }
                tokens[stream] = token
                streams[token] = stream
            }
        }
        return "http://127.0.0.1:$port/mv/$token/$index.mp4"
    }

    /** The stream behind a URL returned by [urlFor], or null once it was dropped. */
    fun streamFor(url: String): MusicVideoStream? {
        val token = url.substringAfter("/mv/", "").substringBefore('/')
        return synchronized(streams) { streams[token] }
    }

    private fun serve(exchange: HttpExchange) {
        try {
            if (exchange.requestMethod != "GET" && exchange.requestMethod != "HEAD") {
                exchange.sendResponseHeaders(405, -1)
                return
            }
            val parts = exchange.requestURI.path.trim('/').split('/')
            val stream = parts.getOrNull(1)?.let { token -> synchronized(streams) { streams[token] } }
            val index = parts.getOrNull(2)?.substringBefore('.')?.toIntOrNull()
            if (stream == null || index == null || index !in stream.fragments.indices) {
                exchange.sendResponseHeaders(404, -1)
                return
            }
            val initSize = stream.init.size.toLong()
            val fragmentOffset = stream.fragments[index].offset
            val totalLength = initSize + (stream.contentLength - fragmentOffset)

            val range = exchange.requestHeaders.getFirst("Range")
                ?.takeIf { it.startsWith("bytes=") }?.removePrefix("bytes=")?.substringBefore(',')?.trim()
            // Suffix ranges ("bytes=-N") are not needed by VLC: refuse them.
            val start = if (range == null) 0L else range.substringBefore('-').toLongOrNull() ?: -1L
            val end = (range?.substringAfter('-', "")?.toLongOrNull() ?: (totalLength - 1L)).coerceAtMost(totalLength - 1L)
            if (start < 0L || start > end || start >= totalLength) {
                exchange.responseHeaders.set("Content-Range", "bytes */$totalLength")
                exchange.sendResponseHeaders(416, -1)
                return
            }
            val length = end - start + 1L
            exchange.responseHeaders.set("Content-Type", "video/mp4")
            exchange.responseHeaders.set("Accept-Ranges", "bytes")
            if (range != null) exchange.responseHeaders.set("Content-Range", "bytes $start-$end/$totalLength")
            val status = if (range != null) 206 else 200
            if (exchange.requestMethod == "HEAD") {
                exchange.responseHeaders.set("Content-Length", length.toString())
                exchange.sendResponseHeaders(status, -1)
                return
            }
            val initPart = if (start < initSize) minOf(initSize - 1L, end) - start + 1L else 0L
            val remoteStart = fragmentOffset + (start + initPart - initSize)
            val remoteEnd = fragmentOffset + (end - initSize)
            // Whole fragments downloaded before are served from memory first.
            val cachedParts = ArrayList<Triple<ByteArray, Int, Int>>()
            var remoteResume: Long? = null
            if (initPart < length) {
                var position = remoteStart
                while (position <= remoteEnd) {
                    val index = stream.fragmentIndexAtOffset(position)
                    val data = MusicVideoFragmentCache.get(stream, index) ?: break
                    val fragmentStart = stream.fragments[index].offset
                    val take = minOf(stream.fragmentEnd(index) - 1L, remoteEnd) - position + 1L
                    if (take <= 0L || data.size.toLong() < (position - fragmentStart) + take) break
                    cachedParts += Triple(data, (position - fragmentStart).toInt(), take.toInt())
                    position += take
                }
                if (position <= remoteEnd) remoteResume = position
            }
            fun openUpstream(from: Long): okhttp3.Response? {
                val request = Request.Builder()
                    .url(stream.remoteUrl)
                    .header("User-Agent", stream.userAgent)
                    .header("Accept-Encoding", "identity")
                    .header("Range", "bytes=$from-$remoteEnd")
                    .build()
                val response = client.newCall(request).execute()
                if (response.code != 206) {
                    response.close()
                    return null
                }
                return response
            }
            // When the answer starts with network data, the CDN range is opened
            // before answering, so an expired or refused URL reaches VLC as an
            // error (and the decoder re-fetches the stream) instead of an empty success.
            var upstream: okhttp3.Response? = null
            if (remoteResume != null && cachedParts.isEmpty()) {
                upstream = openUpstream(remoteResume) ?: run {
                    exchange.sendResponseHeaders(502, -1)
                    return
                }
            }
            try {
                exchange.sendResponseHeaders(status, length)
                exchange.responseBody.use { output ->
                    if (initPart > 0L) output.write(stream.init, start.toInt(), initPart.toInt())
                    for ((data, from, count) in cachedParts) output.write(data, from, count)
                    val resume = remoteResume
                    if (resume != null) {
                        val response = upstream ?: openUpstream(resume)?.also { upstream = it } ?: throw IOException("CDN refused the range")
                        val input = response.body?.byteStream() ?: throw IOException("empty body")
                        val buffer = ByteArray(64 * 1024)
                        // Whole fragments passing through are kept for later.
                        var position = resume
                        var index = stream.fragmentIndexAtOffset(position)
                        var collecting: java.io.ByteArrayOutputStream? =
                            if (position == stream.fragments[index].offset) java.io.ByteArrayOutputStream((stream.fragmentEnd(index) - position).toInt().coerceIn(0, 32 * 1024 * 1024)) else null
                        while (position <= remoteEnd) {
                            val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remoteEnd - position + 1L).toInt())
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            var consumed = 0
                            while (consumed < read) {
                                val fragmentEnd = stream.fragmentEnd(index)
                                val piece = minOf((read - consumed).toLong(), fragmentEnd - position).toInt()
                                collecting?.write(buffer, consumed, piece)
                                consumed += piece
                                position += piece
                                if (position >= fragmentEnd) {
                                    collecting?.let { MusicVideoFragmentCache.put(stream, index, it.toByteArray()) }
                                    if (index >= stream.fragments.lastIndex) break
                                    index++
                                    collecting = java.io.ByteArrayOutputStream((stream.fragmentEnd(index) - position).toInt().coerceIn(0, 32 * 1024 * 1024))
                                }
                            }
                        }
                    }
                    output.flush()
                }
            } finally {
                upstream?.close()
            }
        } catch (_: IOException) {
            // VLC dropped the connection (restart/track change) or the CDN failed;
            // the decoder notices missing pictures and restarts or gives up.
        } catch (error: Exception) {
            runCatching { exchange.sendResponseHeaders(502, -1) }
        } finally {
            exchange.close()
        }
    }
}
