package com.recordlabs.desktop.data

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin

/** A song recognized from the sound around ([WindowsMusicRepository.recognizeSong]). */
data class RecognizedSong(val title: String, val artist: String, val album: String, val artworkUrl: String?)

/** An audio fingerprint ready to be sent: its data URI and how much sound it covers. */
class SongSignature(val uri: String, val sampleMs: Long, val peakCount: Int)

/**
 * Audio fingerprints in the signature format song recognition services read (the one the
 * open-source SongRec and shazamio clients write): 16 kHz mono 16-bit samples go through
 * a 2048-point FFT every 128 samples, the loudest local peaks in time and frequency are
 * kept per frequency band, and they are packed into the binary signature with its CRC-32.
 * Pure Kotlin: the same on every platform.
 */
object SongSignatureGenerator {
    const val SAMPLE_RATE = 16_000
    private const val WINDOW = 2048
    private const val BINS = 1025
    private const val HISTORY = 256

    /** The window ("np.hanning(2050)[1:-1]": a Hann window without its zero ends). */
    private val hanning = DoubleArray(WINDOW) { i -> 0.5 - 0.5 * cos(2.0 * PI * (i + 1) / (WINDOW + 1)) }

    private class Peak(val fftPass: Int, val magnitude: Int, val bin: Int)

    private fun mod(value: Int, by: Int): Int = ((value % by) + by) % by

    /**
     * The signature of [samples] (16 kHz mono), from [startSample] on, covering up to
     * [maxSeconds] of sound (and at least enough for 255 peaks when there is more).
     * Null when there is too little sound.
     */
    fun generate(samples: ShortArray, startSample: Int = 0, maxSeconds: Double = 12.0): SongSignature? {
        if (samples.size - startSample < 128) return null
        val ring = IntArray(WINDOW)
        var ringPosition = 0
        val fftOutputs = Array(HISTORY) { DoubleArray(BINS) }
        var fftPosition = 0
        val spread = Array(HISTORY) { DoubleArray(BINS) }
        var spreadPosition = 0
        var spreadWritten = 0
        val bands = HashMap<Int, MutableList<Peak>>()
        var peakTotal = 0
        var numberSamples = 0
        var processed = startSample

        val real = DoubleArray(WINDOW)
        val imaginary = DoubleArray(WINDOW)
        val spreadLast = DoubleArray(BINS)
        val neighbours = intArrayOf(-10, -7, -4, -3, 1, 2, 5, 8)
        val others = intArrayOf(-53, -45, 165, 172, 179, 186, 193, 200, 214, 221, 228, 235, 242, 249)

        while (samples.size - processed >= 128 && (numberSamples.toDouble() / SAMPLE_RATE < maxSeconds || peakTotal < 255)) {
            numberSamples += 128
            // FFT of the last 2048 samples.
            for (i in 0 until 128) ring[ringPosition + i] = samples[processed + i].toInt()
            processed += 128
            ringPosition = (ringPosition + 128) % WINDOW
            for (i in 0 until WINDOW) {
                real[i] = hanning[i] * ring[(ringPosition + i) % WINDOW]
                imaginary[i] = 0.0
            }
            fft(real, imaginary)
            val output = fftOutputs[fftPosition]
            for (i in 0 until BINS) {
                output[i] = max((real[i] * real[i] + imaginary[i] * imaginary[i]) / (1 shl 17), 0.0000000001)
            }
            fftPosition = (fftPosition + 1) % HISTORY

            // Peaks spread over neighbouring bins and the previous FFTs.
            for (i in 0 until BINS - 3) spreadLast[i] = max(output[i], max(output[i + 1], output[i + 2]))
            for (i in BINS - 3 until BINS) spreadLast[i] = output[i]
            val i1 = mod(spreadPosition - 1, HISTORY)
            val i2 = mod(spreadPosition - 3, HISTORY)
            val i3 = mod(spreadPosition - 6, HISTORY)
            for (i in 0 until BINS) {
                val a = max(spreadLast[i], spread[i1][i])
                val b = max(a, spread[i2][i])
                val c = max(b, spread[i3][i])
                spread[i1][i] = a
                spread[i2][i] = b
                spread[i3][i] = c
            }
            spreadLast.copyInto(spread[spreadPosition])
            spreadPosition = (spreadPosition + 1) % HISTORY
            spreadWritten++
            if (spreadWritten < 46) continue

            // Peak recognition, 46 FFTs back (where the spreading is complete).
            val f46 = fftOutputs[mod(fftPosition - 46, HISTORY)]
            val s49 = spread[mod(spreadPosition - 49, HISTORY)]
            for (bin in 10 until 1015) {
                val value = f46[bin]
                if (value < 1.0 / 64 || value < s49[bin - 1]) continue
                var maxNeighbour = 0.0
                for (offset in neighbours) maxNeighbour = max(s49[bin + offset], maxNeighbour)
                if (value <= maxNeighbour) continue
                var maxOther = maxNeighbour
                for (offset in others) maxOther = max(spread[mod(spreadPosition + offset, HISTORY)][bin - 1], maxOther)
                if (value <= maxOther) continue

                val fftNumber = spreadWritten - 46
                val magnitude = ln(max(1.0 / 64, value)) * 1477.3 + 6144
                val before = ln(max(1.0 / 64, f46[bin - 1])) * 1477.3 + 6144
                val after = ln(max(1.0 / 64, f46[bin + 1])) * 1477.3 + 6144
                val variation1 = magnitude * 2 - before - after
                if (variation1 <= 0) continue
                val variation2 = (after - before) * 32 / variation1
                val correctedBin = bin * 64 + variation2
                val frequencyHz = (correctedBin * (SAMPLE_RATE / 2.0 / 1024 / 64)).toInt()
                val band = when (frequencyHz) {
                    in 250..519 -> 0
                    in 520..1449 -> 1
                    in 1450..3499 -> 2
                    in 3500..5500 -> 3
                    else -> continue
                }
                bands.getOrPut(band) { mutableListOf() } += Peak(fftNumber, magnitude.toInt(), correctedBin.toInt())
                peakTotal++
            }
        }
        if (numberSamples == 0) return null
        val binary = encode(bands, numberSamples)
        return SongSignature(
            uri = "data:audio/vnd.shazam.sig;base64," + base64(binary),
            sampleMs = numberSamples.toLong() * 1000 / SAMPLE_RATE,
            peakCount = peakTotal,
        )
    }

    private fun encode(bands: Map<Int, List<Peak>>, numberSamples: Int): ByteArray {
        val contents = Bytes()
        for (band in bands.keys.sorted()) {
            val peaks = bands.getValue(band)
            val buffer = Bytes()
            var pass = 0
            for (peak in peaks) {
                if (peak.fftPass - pass >= 255) {
                    buffer.byte(0xFF)
                    buffer.int(peak.fftPass)
                    pass = peak.fftPass
                }
                buffer.byte(peak.fftPass - pass)
                buffer.short(peak.magnitude)
                buffer.short(peak.bin)
                pass = peak.fftPass
            }
            contents.int(0x60030040 + band)
            contents.int(buffer.size)
            contents.bytes(buffer.toByteArray())
            repeat(mod(-buffer.size, 4)) { contents.byte(0) }
        }
        val body = Bytes()
        body.int(0x94119C00.toInt())
        repeat(3) { body.int(0) }
        body.int(3 shl 27) // 16 000 Hz
        repeat(2) { body.int(0) }
        body.int((numberSamples + SAMPLE_RATE * 0.24).toInt())
        body.int((15 shl 19) + 0x40000)
        body.int(0x40000000)
        body.int(contents.size + 8)
        body.bytes(contents.toByteArray())
        val afterCrc = Bytes()
        afterCrc.int(contents.size + 8) // size minus the 48-byte header
        afterCrc.bytes(body.toByteArray())
        val checked = afterCrc.toByteArray()
        val message = Bytes()
        message.int(0xCAFE2580.toInt())
        message.int(crc32(checked).toInt())
        message.bytes(checked)
        return message.toByteArray()
    }

    /** In-place radix-2 FFT of 2048 points. */
    private fun fft(re: DoubleArray, im: DoubleArray) {
        val n = re.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) {
                var t = re[i]; re[i] = re[j]; re[j] = t
                t = im[i]; im[i] = im[j]; im[j] = t
            }
        }
        var length = 2
        while (length <= n) {
            val angle = -2 * PI / length
            val wRe = cos(angle)
            val wIm = sin(angle)
            var start = 0
            while (start < n) {
                var curRe = 1.0
                var curIm = 0.0
                for (k in 0 until length / 2) {
                    val a = start + k
                    val b = a + length / 2
                    val tRe = re[b] * curRe - im[b] * curIm
                    val tIm = re[b] * curIm + im[b] * curRe
                    re[b] = re[a] - tRe
                    im[b] = im[a] - tIm
                    re[a] += tRe
                    im[a] += tIm
                    val nextRe = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe
                    curRe = nextRe
                }
                start += length
            }
            length = length shl 1
        }
    }

    private val crcTable = LongArray(256) { n ->
        var c = n.toLong()
        repeat(8) { c = if (c and 1L != 0L) 0xEDB88320L xor (c ushr 1) else c ushr 1 }
        c
    }

    private fun crc32(data: ByteArray): Long {
        var crc = 0xFFFFFFFFL
        for (b in data) crc = crcTable[((crc xor b.toLong()) and 0xFF).toInt()] xor (crc ushr 8)
        return crc xor 0xFFFFFFFFL
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun base64(bytes: ByteArray): String = Base64.encode(bytes)

    /** A little-endian byte writer. */
    private class Bytes {
        private var data = ByteArray(1024)
        var size = 0
            private set

        private fun room(extra: Int) {
            if (size + extra > data.size) data = data.copyOf(maxOf(data.size * 2, size + extra))
        }

        fun byte(value: Int) { room(1); data[size++] = value.toByte() }
        fun short(value: Int) { byte(value and 0xFF); byte((value shr 8) and 0xFF) }
        fun int(value: Int) { short(value and 0xFFFF); short((value ushr 16) and 0xFFFF) }
        fun bytes(value: ByteArray) { room(value.size); value.copyInto(data, size); size += value.size }
        fun toByteArray(): ByteArray = data.copyOf(size)
    }
}
