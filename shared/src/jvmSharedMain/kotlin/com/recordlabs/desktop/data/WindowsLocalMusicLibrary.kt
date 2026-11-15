package com.recordlabs.desktop.data

import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.security.MessageDigest

/** Lightweight desktop local-music index with embedded MP3/FLAC tag support. */
object WindowsLocalMusicLibrary {
    private val supported = setOf("mp3", "flac", "m4a", "m4b", "aac", "wav", "ogg", "oga", "opus", "wma", "aiff", "aif", "alac", "ape", "wv")
    private val foldersFile = File(WindowsStorage.directory, "local-music-folders.txt")
    /** Default folders (Music, OneDrive\\Music) the user removed in Settings > Storage. */
    private val excludedFile = File(WindowsStorage.directory, "local-music-excluded.txt")

    private fun excluded(): Set<String> =
        runCatching { excludedFile.readLines().map { it.trim().lowercase() }.filter(String::isNotBlank).toSet() }.getOrDefault(emptySet())

    fun folders(): List<File> {
        val saved = runCatching { foldersFile.readLines().map(::File) }.getOrDefault(emptyList())
        val home = File(System.getProperty("user.home"))
        val defaults = listOf(File(home, "Music"), File(home, "OneDrive/Music"))
        val hidden = excluded()
        return (saved + defaults).map(File::getAbsoluteFile).filter(File::isDirectory).distinctBy { it.path.lowercase() }
            .filterNot { it.path.lowercase() in hidden }
    }

    fun addFolder(folder: File) {
        if (!folder.isDirectory) return
        val path = folder.absoluteFile.path.lowercase()
        val hidden = excluded()
        if (path in hidden) runCatching { excludedFile.writeText((hidden - path).joinToString("\n")) }
        val updated = (folders() + folder.absoluteFile).distinctBy { it.path.lowercase() }
        foldersFile.parentFile?.mkdirs()
        foldersFile.writeText(updated.joinToString("\n", transform = File::getAbsolutePath))
    }

    /** Settings > Storage > Local music folders: stop scanning [folder] (its files are not touched). */
    fun removeFolder(folder: File) {
        val path = folder.absoluteFile.path.lowercase()
        val saved = runCatching { foldersFile.readLines().map(::File) }.getOrDefault(emptyList())
        runCatching {
            foldersFile.parentFile?.mkdirs()
            foldersFile.writeText(saved.filterNot { it.absoluteFile.path.lowercase() == path }.joinToString("\n", transform = File::getAbsolutePath))
            excludedFile.writeText((excluded() + path).joinToString("\n"))
        }
    }

    fun scan(): List<Track> = folders().asSequence()
        .flatMap { root -> root.walkTopDown().onEnter { it.name !in setOf(".git", "node_modules") }.asSequence() }
        .filter { it.isFile && it.extension.lowercase() in supported }
        .mapNotNull(::readTrack)
        .distinctBy(Track::id)
        .sortedWith(compareBy<Track> { it.artist.lowercase() }.thenBy { it.album.lowercase() }.thenBy { it.title.lowercase() })
        .toList()

    private fun readTrack(file: File): Track? = runCatching {
        val tags = when (file.extension.lowercase()) {
            "mp3" -> readId3(file)
            "flac" -> readFlacComments(file)
            else -> emptyMap()
        }
        val stem = file.nameWithoutExtension
        val split = stem.split(" - ", limit = 2)
        val fallbackArtist = split.getOrNull(0).takeIf { split.size == 2 }.orEmpty()
        val fallbackTitle = split.getOrNull(1) ?: stem
        val title = tags["TITLE"].orEmpty().ifBlank { fallbackTitle }
        val artist = tags["ARTIST"].orEmpty().ifBlank { fallbackArtist.ifBlank { "Local Music" } }
        val album = tags["ALBUM"].orEmpty().ifBlank { file.parentFile?.name.orEmpty() }
        val artworkUrl = cachedEmbeddedArtwork(file)
        Track(
            id = "local:${sha256(file.absolutePath.lowercase())}",
            title = title,
            artist = artist,
            album = album,
            artworkUrl = artworkUrl,
            isOfficial = true,
            localPath = file.absolutePath,
        )
    }.getOrNull()

    private fun readId3(file: File): Map<String, String> {
        RandomAccessFile(file, "r").use { input ->
            if (input.length() < 10) return emptyMap()
            val header = ByteArray(10).also(input::readFully)
            if (String(header, 0, 3, Charsets.ISO_8859_1) != "ID3") return readId3v1(input)
            val version = header[3].toInt() and 0xff
            val tagSize = synchsafe(header, 6)
            val end = minOf(input.length(), 10L + tagSize)
            val values = linkedMapOf<String, String>()
            while (input.filePointer + 10 <= end) {
                val frameHeader = ByteArray(10).also(input::readFully)
                val frameId = String(frameHeader, 0, 4, Charsets.ISO_8859_1)
                if (frameId.all { it == '\u0000' }) break
                val frameSize = if (version >= 4) synchsafe(frameHeader, 4) else
                    ((frameHeader[4].toInt() and 0xff) shl 24) or
                        ((frameHeader[5].toInt() and 0xff) shl 16) or
                        ((frameHeader[6].toInt() and 0xff) shl 8) or
                        (frameHeader[7].toInt() and 0xff)
                if (frameSize <= 0 || input.filePointer + frameSize > end) break
                val data = ByteArray(frameSize).also(input::readFully)
                val key = when (frameId) { "TIT2" -> "TITLE"; "TPE1" -> "ARTIST"; "TALB" -> "ALBUM"; else -> null }
                if (key != null) decodeId3Text(data)?.let { values[key] = it }
            }
            return values.ifEmpty { readId3v1(input) }
        }
    }

    private fun readId3v1(input: RandomAccessFile): Map<String, String> {
        if (input.length() < 128) return emptyMap()
        input.seek(input.length() - 128)
        val block = ByteArray(128).also(input::readFully)
        if (String(block, 0, 3, Charsets.ISO_8859_1) != "TAG") return emptyMap()
        fun field(offset: Int, length: Int) = String(block, offset, length, Charsets.ISO_8859_1).trim('\u0000', ' ')
        return mapOf("TITLE" to field(3, 30), "ARTIST" to field(33, 30), "ALBUM" to field(63, 30)).filterValues(String::isNotBlank)
    }

    private fun decodeId3Text(data: ByteArray): String? {
        if (data.size < 2) return null
        val charset = when (data[0].toInt() and 0xff) {
            1 -> Charsets.UTF_16
            2 -> Charset.forName("UTF-16BE")
            3 -> Charsets.UTF_8
            else -> Charsets.ISO_8859_1
        }
        return String(data, 1, data.size - 1, charset).trim('\u0000', ' ').takeIf(String::isNotBlank)
    }

    private fun readFlacComments(file: File): Map<String, String> {
        RandomAccessFile(file, "r").use { input ->
            if (input.length() < 8 || ByteArray(4).also(input::readFully).let { String(it, Charsets.US_ASCII) } != "fLaC") return emptyMap()
            var last = false
            while (!last && input.filePointer + 4 <= input.length()) {
                val typeByte = input.readUnsignedByte()
                last = typeByte and 0x80 != 0
                val type = typeByte and 0x7f
                val length = (input.readUnsignedByte() shl 16) or (input.readUnsignedByte() shl 8) or input.readUnsignedByte()
                if (length < 0 || input.filePointer + length > input.length()) break
                if (type != 4) { input.seek(input.filePointer + length); continue }
                val block = ByteArray(length).also(input::readFully)
                var offset = 0
                fun intLe(): Int {
                    if (offset + 4 > block.size) return 0
                    return (block[offset++].toInt() and 0xff) or ((block[offset++].toInt() and 0xff) shl 8) or
                        ((block[offset++].toInt() and 0xff) shl 16) or ((block[offset++].toInt() and 0xff) shl 24)
                }
                val vendor = intLe(); offset = (offset + vendor).coerceAtMost(block.size)
                val count = intLe().coerceIn(0, 10_000)
                val values = linkedMapOf<String, String>()
                repeat(count) {
                    val size = intLe()
                    if (size <= 0 || offset + size > block.size) return@repeat
                    val entry = String(block, offset, size, Charsets.UTF_8); offset += size
                    val key = entry.substringBefore('=').uppercase()
                    if (key in setOf("TITLE", "ARTIST", "ALBUM")) values.putIfAbsent(key, entry.substringAfter('=').trim())
                }
                return values
            }
        }
        return emptyMap()
    }

    fun parseLocalArtworkFile(url: String): File? {
        if (url.startsWith("file:", ignoreCase = true)) {
            val fromUri = runCatching { File(java.net.URI(url)) }.getOrNull()
            if (fromUri != null && fromUri.isFile) return fromUri

            val rawPath = url.substringAfter("file:").trimStart('/')
            val decoded = runCatching { java.net.URLDecoder.decode(rawPath, "UTF-8") }.getOrDefault(rawPath)
            val file = File(decoded)
            if (file.isFile) return file
            val rawFile = File(rawPath)
            if (rawFile.isFile) return rawFile
        }
        val direct = runCatching { File(url) }.getOrNull()
        if (direct != null && direct.isFile) return direct
        return null
    }

    fun resolveArtworkModel(track: Track): Any? {
        // 1. Offline cache for downloaded tracks
        val offlineFile = WindowsOfflineArtworkCache.file(track.id).takeIf { it.isFile && it.length() > 1_024L }
        if (offlineFile != null) return offlineFile

        // 2. Track's artworkUrl (could be file: URI or remote http URL)
        val url = track.artworkUrl
        if (!url.isNullOrBlank()) {
            val localFile = parseLocalArtworkFile(url)
            if (localFile != null) return localFile
            if (!url.startsWith("file:", ignoreCase = true)) {
                return url.replace("w120-h120", "w1200-h1200").replace("w60-h60", "w1200-h1200")
            }
        }

        // 3. Fallback for local files: if artworkUrl is missing or invalid, resolve on the fly from localPath
        track.localPath?.let { path ->
            val audioFile = File(path)
            if (audioFile.isFile) {
                val cachedUrl = cachedEmbeddedArtwork(audioFile)
                if (!cachedUrl.isNullOrBlank()) {
                    val file = parseLocalArtworkFile(cachedUrl)
                    if (file != null) return file
                }
            }
        }

        return null
    }

    private data class EmbeddedArtwork(val bytes: ByteArray, val extension: String)

    /** The smallest picture taken as a cover (a few hundred bytes is already a small thumbnail). */
    private const val MIN_ARTWORK_BYTES = 256L

    fun cachedEmbeddedArtwork(file: File): String? {
        val directory = File(WindowsStorage.directory, "local-artwork").apply { mkdirs() }
        val cacheName = sha256("${file.absolutePath.lowercase()}|${file.length()}|${file.lastModified()}")
        listOf("jpg", "png", "webp", "gif").asSequence()
            .map { extension -> File(directory, "$cacheName.$extension") }
            .firstOrNull { it.isFile && it.length() > MIN_ARTWORK_BYTES }
            ?.let { return it.toURI().toASCIIString() }

        val artwork = readEmbeddedArtwork(file)
        if (artwork != null && artwork.bytes.size > MIN_ARTWORK_BYTES) {
            val destination = File(directory, "$cacheName.${artwork.extension}")
            val partial = File(directory, "$cacheName.${artwork.extension}.part")
            val saved = runCatching {
                partial.writeBytes(artwork.bytes)
                if (destination.exists()) destination.delete()
                if (!partial.renameTo(destination)) {
                    partial.copyTo(destination, overwrite = true)
                    partial.delete()
                }
                destination.toURI().toASCIIString()
            }.onFailure { partial.delete() }.getOrNull()
            if (saved != null) return saved
        }
        return folderArtwork(file)?.toURI()?.toASCIIString()
    }

    /**
     * The picture inside [file], read by its container's own tag format (MP3/AAC/WAV/AIFF:
     * ID3v2; FLAC: PICTURE; M4A/MP4/ALAC: covr; Ogg Vorbis/Opus: METADATA_BLOCK_PICTURE;
     * WMA: WM/Picture; APE/WavPack: APEv2), and else any ID3v2 tag at its start or APEv2
     * tag at its end (taggers put those on other formats too).
     */
    private fun readEmbeddedArtwork(file: File): EmbeddedArtwork? {
        val own = runCatching {
            when (file.extension.lowercase()) {
                "mp3", "aac" -> readId3Artwork(file)
                "flac" -> readFlacArtwork(file)
                "m4a", "mp4", "m4b", "alac" -> readM4aArtwork(file)
                "ogg", "oga", "opus" -> readOggArtwork(file)
                "wav", "aiff", "aif" -> readChunkedId3Artwork(file)
                "wma" -> readAsfArtwork(file)
                "ape", "wv" -> readApeArtwork(file)
                else -> null
            }
        }.getOrNull()
        return own
            ?: runCatching { readId3Artwork(file) }.getOrNull()
            ?: runCatching { readApeArtwork(file) }.getOrNull()
            ?: runCatching { if (file.extension.lowercase() == "aac") readM4aArtwork(file) else null }.getOrNull()
    }

    /** A cover picture beside the song: the usual names (any case), else the folder's only picture. */
    private fun folderArtwork(file: File): File? {
        val parent = file.parentFile?.takeIf(File::isDirectory) ?: return null
        val pictures = parent.listFiles { candidate ->
            candidate.isFile && candidate.extension.lowercase() in setOf("jpg", "jpeg", "png", "webp") && candidate.length() > MIN_ARTWORK_BYTES
        }?.toList().orEmpty()
        if (pictures.isEmpty()) return null
        val stem = file.nameWithoutExtension.lowercase()
        fun named(vararg names: String) = pictures.firstOrNull { it.nameWithoutExtension.lowercase() in names }
        return named(stem)
            ?: named("cover", "folder", "front", "album", "albumart", "artwork")
            ?: pictures.filter { it.name.lowercase().startsWith("albumart") }.maxByOrNull(File::length)
            ?: pictures.firstOrNull { name -> listOf("cover", "front", "folder").any { it in name.nameWithoutExtension.lowercase() } }
            ?: pictures.singleOrNull()
    }

    // ---- ID3v2 (MP3, AAC; inside WAV and AIFF) ----

    /** The ID3v2 tag at the start of [file] (after any leading padding), whole, or null. */
    private fun readId3Artwork(file: File): EmbeddedArtwork? = RandomAccessFile(file, "r").use { input ->
        if (input.length() < 10) return@use null
        val header = ByteArray(10).also(input::readFully)
        if (String(header, 0, 3, Charsets.ISO_8859_1) != "ID3") return@use null
        val footer = if ((header[5].toInt() and 0x10) != 0) 10 else 0
        val size = (10L + synchsafe(header, 6) + footer).coerceAtMost(minOf(input.length(), 64L * 1024 * 1024))
        val tag = ByteArray(size.toInt())
        input.seek(0)
        input.readFully(tag)
        id3Artwork(tag)
    }

    /** The cover in an ID3v2 tag ([tag] starts with its "ID3" header): the front cover if there are several. */
    private fun id3Artwork(tag: ByteArray): EmbeddedArtwork? {
        if (tag.size < 10 || String(tag, 0, 3, Charsets.ISO_8859_1) != "ID3") return null
        val version = tag[3].toInt() and 0xff
        val flags = tag[5].toInt() and 0xff
        val tagUnsync = (flags and 0x80) != 0
        // ID3v2.2 and 2.3 unsynchronise the whole tag; 2.4 each frame (flagged per frame too).
        var body = tag.copyOfRange(10, minOf(tag.size, 10 + synchsafe(tag, 6)))
        if (tagUnsync && version < 4) body = deUnsync(body)
        var pos = 0
        if ((flags and 0x40) != 0 && version >= 3 && body.size >= 4) {
            pos = if (version >= 4) synchsafe(body, 0) else intBe(body, 0) + 4
        }
        val headerLength = if (version == 2) 6 else 10
        var fallback: EmbeddedArtwork? = null
        while (pos >= 0 && pos + headerLength <= body.size) {
            val id = String(body, pos, if (version == 2) 3 else 4, Charsets.ISO_8859_1)
            if (id.all { it == '\u0000' }) break
            var size = when (version) {
                2 -> ((body[pos + 3].toInt() and 0xff) shl 16) or ((body[pos + 4].toInt() and 0xff) shl 8) or (body[pos + 5].toInt() and 0xff)
                3 -> intBe(body, pos + 4)
                else -> synchsafe(body, pos + 4)
            }
            // Some 2.4 taggers write plain sizes: taken when the synchsafe one runs past the tag.
            if (version >= 4 && (size <= 0 || pos + headerLength + size > body.size)) size = intBe(body, pos + 4)
            if (size <= 0 || pos + headerLength + size > body.size) break
            if (id == "APIC" || id == "PIC") {
                var data = body.copyOfRange(pos + headerLength, pos + headerLength + size)
                if (version >= 4) {
                    val format = body[pos + 9].toInt() and 0xff
                    if ((format and 0x01) != 0 && data.size > 4) data = data.copyOfRange(4, data.size)
                    if ((format and 0x02) != 0 || tagUnsync) data = deUnsync(data)
                }
                val picture = imagePayload(data)
                if (picture != null) {
                    if (id3PictureType(data, version == 2) == 3) return picture
                    if (fallback == null) fallback = picture
                }
            }
            pos += headerLength + size
        }
        return fallback
    }

    /** An APIC / PIC frame's picture type (3: front cover), or -1. */
    private fun id3PictureType(data: ByteArray, v22: Boolean): Int {
        if (data.size < 5) return -1
        if (v22) return data[4].toInt() and 0xff
        var index = 1
        while (index < data.size && data[index] != 0.toByte()) index++
        return if (index + 1 < data.size) data[index + 1].toInt() and 0xff else -1
    }

    /** Undoes ID3 unsynchronisation (0xFF 0x00 back to 0xFF). */
    private fun deUnsync(data: ByteArray): ByteArray {
        val out = java.io.ByteArrayOutputStream(data.size)
        var index = 0
        while (index < data.size) {
            val byte = data[index]
            out.write(byte.toInt())
            index += if (byte == 0xff.toByte() && index + 1 < data.size && data[index + 1] == 0.toByte()) 2 else 1
        }
        return out.toByteArray()
    }

    /** WAV ("id3 " chunk of RIFF) and AIFF ("ID3 " chunk of FORM). */
    private fun readChunkedId3Artwork(file: File): EmbeddedArtwork? = RandomAccessFile(file, "r").use { input ->
        if (input.length() < 12) return@use null
        val head = ByteArray(12).also(input::readFully)
        val kind = String(head, 0, 4, Charsets.US_ASCII)
        val littleEndian = kind == "RIFF" || kind == "RF64"
        if (!littleEndian && kind != "FORM") return@use null
        var pos = 12L
        while (pos + 8 <= input.length()) {
            input.seek(pos)
            val chunk = ByteArray(8).also(input::readFully)
            val id = String(chunk, 0, 4, Charsets.US_ASCII)
            val size = (if (littleEndian) intLe(chunk, 4) else intBe(chunk, 4)).toLong() and 0xFFFFFFFFL
            if (id.equals("id3 ", ignoreCase = true) && size in 10..(64L * 1024 * 1024)) {
                val tag = ByteArray(size.toInt()).also(input::readFully)
                return@use id3Artwork(tag)
            }
            pos += 8 + size + (size and 1)
        }
        null
    }

    // ---- FLAC ----

    private fun readFlacArtwork(file: File): EmbeddedArtwork? = RandomAccessFile(file, "r").use { input ->
        if (input.length() < 8) return@use null
        // A FLAC file may carry an ID3v2 tag before its "fLaC".
        val head = ByteArray(10).also(input::readFully)
        val start = if (String(head, 0, 3, Charsets.ISO_8859_1) == "ID3") 10L + synchsafe(head, 6) + (if ((head[5].toInt() and 0x10) != 0) 10 else 0) else 0L
        if (start + 8 > input.length()) return@use null
        input.seek(start)
        if (ByteArray(4).also(input::readFully).let { String(it, Charsets.US_ASCII) } != "fLaC") return@use null
        var last = false
        var fallback: EmbeddedArtwork? = null
        while (!last && input.filePointer + 4 <= input.length()) {
            val typeByte = input.readUnsignedByte()
            last = typeByte and 0x80 != 0
            val type = typeByte and 0x7f
            val length = (input.readUnsignedByte() shl 16) or (input.readUnsignedByte() shl 8) or input.readUnsignedByte()
            if (length <= 0 || input.filePointer + length > input.length()) break
            if (type != 6) {
                input.seek(input.filePointer + length)
                continue
            }
            val block = ByteArray(length).also(input::readFully)
            val picture = flacPicture(block) ?: continue
            if (intBe(block, 0) == 3) return@use picture
            if (fallback == null) fallback = picture
        }
        fallback
    }

    /** A FLAC PICTURE block's picture (also how Ogg's METADATA_BLOCK_PICTURE holds it). */
    private fun flacPicture(block: ByteArray): EmbeddedArtwork? {
        var offset = 4 // picture type
        val mimeLength = intBe(block, offset); offset += 4
        if (mimeLength !in 0..block.size || offset + mimeLength > block.size) return null
        offset += mimeLength
        val descriptionLength = intBe(block, offset); offset += 4
        if (descriptionLength !in 0..block.size || offset + descriptionLength > block.size) return null
        offset += descriptionLength + 16 // width, height, depth, palette size
        val dataLength = intBe(block, offset); offset += 4
        if (dataLength <= 0 || offset + dataLength > block.size) return imagePayload(block)
        return imagePayload(block.copyOfRange(offset, offset + dataLength))
    }

    // ---- Ogg Vorbis / Opus ----

    /** The comment header (the stream's second packet) of an Ogg file, its covers base64 in it. */
    private fun readOggArtwork(file: File): EmbeddedArtwork? = RandomAccessFile(file, "r").use { input ->
        val packet = java.io.ByteArrayOutputStream()
        var packets = 0
        var serial = -1
        val limit = minOf(input.length(), 48L * 1024 * 1024)
        var comments: ByteArray? = null
        while (comments == null && input.filePointer + 27 <= limit) {
            val header = ByteArray(27).also(input::readFully)
            if (String(header, 0, 4, Charsets.US_ASCII) != "OggS") break
            val pageSerial = intLe(header, 14)
            val segments = header[26].toInt() and 0xff
            val lacing = ByteArray(segments).also(input::readFully)
            if (serial == -1) serial = pageSerial
            if (pageSerial != serial) {
                input.seek(input.filePointer + lacing.sumOf { it.toInt() and 0xff })
                continue
            }
            for (lace in lacing) {
                val length = lace.toInt() and 0xff
                val bytes = ByteArray(length).also(input::readFully)
                if (packets == 1) packet.write(bytes)
                if (length < 255) {
                    if (packets == 1) { comments = packet.toByteArray(); break }
                    packets++
                }
            }
        }
        val data = comments ?: return@use null
        var offset = when {
            data.size > 8 && String(data, 0, 8, Charsets.US_ASCII) == "OpusTags" -> 8
            data.size > 7 && data[0].toInt() == 3 && String(data, 1, 6, Charsets.US_ASCII) == "vorbis" -> 7
            else -> return@use null
        }
        val vendor = intLe(data, offset); offset += 4 + vendor.coerceAtLeast(0)
        if (offset + 4 > data.size) return@use null
        val count = intLe(data, offset).coerceIn(0, 100_000); offset += 4
        var fallback: EmbeddedArtwork? = null
        repeat(count) {
            if (offset + 4 > data.size) return@repeat
            val size = intLe(data, offset); offset += 4
            if (size <= 0 || offset + size > data.size) return@repeat
            val entry = String(data, offset, size, Charsets.UTF_8); offset += size
            val key = entry.substringBefore('=').uppercase()
            val value = entry.substringAfter('=', "")
            val decoded = if (key == "METADATA_BLOCK_PICTURE" || key == "COVERART") {
                runCatching { java.util.Base64.getMimeDecoder().decode(value) }.getOrNull()
            } else null
            if (decoded != null) {
                val picture = if (key == "METADATA_BLOCK_PICTURE") flacPicture(decoded) else imagePayload(decoded)
                if (picture != null) {
                    if (key == "METADATA_BLOCK_PICTURE" && intBe(decoded, 0) == 3) return@use picture
                    if (fallback == null) fallback = picture
                }
            }
        }
        fallback
    }

    // ---- WMA (ASF) ----

    /** "WM/Picture" in the ASF header (Extended Content Description or Metadata Library). */
    private fun readAsfArtwork(file: File): EmbeddedArtwork? = RandomAccessFile(file, "r").use { input ->
        if (input.length() < 30) return@use null
        val head = ByteArray(30).also(input::readFully)
        val headerSize = (head[16].toLong() and 0xff) or ((head[17].toLong() and 0xff) shl 8) or
            ((head[18].toLong() and 0xff) shl 16) or ((head[19].toLong() and 0xff) shl 24)
        val size = headerSize.coerceIn(30L, minOf(input.length(), 48L * 1024 * 1024))
        val header = ByteArray(size.toInt())
        input.seek(0)
        input.readFully(header)
        val name = "WM/Picture\u0000".toByteArray(Charsets.UTF_16LE)
        var from = 0
        while (true) {
            val at = indexOf(header, name, from)
            if (at < 0) return@use null
            from = at + 1
            val valueStart: Int
            val valueLength: Int
            if (at >= 2 && shortLe(header, at - 2) == name.size) {
                // Extended Content Description: name length, name, value type, value length, value.
                valueStart = at + name.size + 4
                valueLength = shortLe(header, at + name.size + 2)
            } else if (at >= 8 && shortLe(header, at - 8) == name.size) {
                // Metadata Library: name length, data type, data length (4), name, data.
                valueStart = at + name.size
                valueLength = intLe(header, at - 4)
            } else continue
            if (valueLength <= 0 || valueStart + valueLength > header.size) continue
            imagePayload(header.copyOfRange(valueStart, valueStart + valueLength))?.let { return@use it }
        }
        @Suppress("UNREACHABLE_CODE") null
    }

    // ---- APEv2 (APE, WavPack, and any file a tagger gave one) ----

    private fun readApeArtwork(file: File): EmbeddedArtwork? = RandomAccessFile(file, "r").use { input ->
        val length = input.length()
        // The footer is last, or before an ID3v1 tag.
        val hasId3v1 = length >= 128 && run {
            input.seek(length - 128)
            String(ByteArray(3).also(input::readFully), Charsets.ISO_8859_1) == "TAG"
        }
        val footerAt = length - 32 - (if (hasId3v1) 128 else 0)
        if (footerAt < 0) return@use null
        input.seek(footerAt)
        val footer = ByteArray(32).also(input::readFully)
        if (String(footer, 0, 8, Charsets.US_ASCII) != "APETAGEX") return@use null
        val tagSize = intLe(footer, 12).toLong() and 0xFFFFFFFFL
        val count = intLe(footer, 16)
        val itemsStart = footerAt + 32 - tagSize
        if (itemsStart < 0 || tagSize > 64L * 1024 * 1024 || tagSize < 32) return@use null
        input.seek(itemsStart)
        val items = ByteArray((tagSize - 32).toInt()).also(input::readFully)
        var offset = 0
        var fallback: EmbeddedArtwork? = null
        repeat(count.coerceIn(0, 10_000)) {
            if (offset + 8 > items.size) return@repeat
            val valueSize = intLe(items, offset)
            offset += 8
            var keyEnd = offset
            while (keyEnd < items.size && items[keyEnd] != 0.toByte()) keyEnd++
            val key = String(items, offset, keyEnd - offset, Charsets.US_ASCII)
            offset = keyEnd + 1
            if (valueSize < 0 || offset + valueSize > items.size) return@repeat
            if (key.startsWith("Cover Art", ignoreCase = true)) {
                val picture = imagePayload(items.copyOfRange(offset, offset + valueSize))
                if (picture != null) {
                    if (key.equals("Cover Art (Front)", ignoreCase = true)) return@use picture
                    if (fallback == null) fallback = picture
                }
            }
            offset += valueSize
        }
        fallback
    }

    // ---- MP4 / M4A ----

    private fun readM4aArtwork(file: File): EmbeddedArtwork? = runCatching {
        RandomAccessFile(file, "r").use { input ->
            val length = input.length()
            if (length < 8) return@use null
            val limit = minOf(length, 25L * 1024 * 1024)
            val buf = ByteArray(64 * 1024)
            var pos = 0L
            while (pos < limit) {
                input.seek(pos)
                val read = input.read(buf)
                if (read <= 8) break
                for (i in 0 until read - 8) {
                    if (buf[i] == 'c'.code.toByte() && buf[i + 1] == 'o'.code.toByte() &&
                        buf[i + 2] == 'v'.code.toByte() && buf[i + 3] == 'r'.code.toByte()
                    ) {
                        val covrOffset = pos + i - 4
                        if (covrOffset >= 0 && covrOffset + 16 < length) {
                            input.seek(covrOffset)
                            val covrSize = input.readInt().toLong() and 0xFFFFFFFFL
                            val covrTag = ByteArray(4).also(input::readFully)
                            if (String(covrTag, Charsets.US_ASCII) == "covr" && covrSize in 16..(12 * 1024 * 1024)) {
                                val dataSize = input.readInt().toLong() and 0xFFFFFFFFL
                                val dataTag = ByteArray(4).also(input::readFully)
                                if (String(dataTag, Charsets.US_ASCII) == "data" && dataSize in 16..covrSize) {
                                    val flags = input.readInt()
                                    input.readInt()
                                    val payloadSize = (dataSize - 16).toInt()
                                    if (payloadSize > 0) {
                                        val payload = ByteArray(payloadSize).also(input::readFully)
                                        imagePayload(payload)?.let { return@use it }
                                        val ext = if (flags == 14) "png" else "jpg"
                                        return@use EmbeddedArtwork(payload, ext)
                                    }
                                }
                            }
                        }
                    }
                }
                pos += read - 8
            }
            null
        }
    }.getOrNull()

    private fun imagePayload(data: ByteArray): EmbeddedArtwork? {
        val png = indexOf(data, byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a))
        val jpeg = indexOf(data, byteArrayOf(0xff.toByte(), 0xd8.toByte(), 0xff.toByte()))
        val gif = indexOf(data, "GIF8".toByteArray(Charsets.US_ASCII))
        var webp = indexOf(data, "RIFF".toByteArray(Charsets.US_ASCII))
        if (webp >= 0 && !(webp + 12 <= data.size && String(data, webp + 8, 4, Charsets.US_ASCII) == "WEBP")) webp = -1
        // The earliest signature is the picture (a later one may be inside it, as a JPEG's thumbnail).
        val found = listOf(png to "png", jpeg to "jpg", gif to "gif", webp to "webp").filter { it.first >= 0 }.minByOrNull { it.first }
            ?: return null
        return EmbeddedArtwork(data.copyOfRange(found.first, data.size), found.second)
    }

    private fun indexOf(data: ByteArray, signature: ByteArray, from: Int = 0): Int {
        if (signature.isEmpty()) return -1
        var start = from.coerceAtLeast(0)
        val first = signature[0]
        while (start + signature.size <= data.size) {
            if (data[start] == first) {
                var match = true
                for (index in 1 until signature.size) if (data[start + index] != signature[index]) { match = false; break }
                if (match) return start
            }
            start++
        }
        return -1
    }

    private fun intBe(bytes: ByteArray, offset: Int): Int {
        if (offset < 0 || offset + 4 > bytes.size) return -1
        return ((bytes[offset].toInt() and 0xff) shl 24) or ((bytes[offset + 1].toInt() and 0xff) shl 16) or
            ((bytes[offset + 2].toInt() and 0xff) shl 8) or (bytes[offset + 3].toInt() and 0xff)
    }

    private fun intLe(bytes: ByteArray, offset: Int): Int {
        if (offset < 0 || offset + 4 > bytes.size) return -1
        return (bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8) or
            ((bytes[offset + 2].toInt() and 0xff) shl 16) or ((bytes[offset + 3].toInt() and 0xff) shl 24)
    }

    private fun shortLe(bytes: ByteArray, offset: Int): Int =
        if (offset < 0 || offset + 2 > bytes.size) -1 else (bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8)

    private fun synchsafe(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0x7f) shl 21) or ((bytes[offset + 1].toInt() and 0x7f) shl 14) or
            ((bytes[offset + 2].toInt() and 0x7f) shl 7) or (bytes[offset + 3].toInt() and 0x7f)

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray()).take(12).joinToString("") { "%02x".format(it) }
}
