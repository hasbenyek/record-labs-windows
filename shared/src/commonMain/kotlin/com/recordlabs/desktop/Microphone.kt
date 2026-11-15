package com.recordlabs.desktop

import androidx.compose.runtime.Composable
import com.recordlabs.desktop.platform.locked
import kotlin.math.sqrt

/** Sound from the microphone while it is open (to recognize the song playing around). */
interface MicrophoneCapture {
    /** All the sound so far, 16 kHz mono 16-bit. */
    fun samples(): ShortArray

    /** How loud the latest sound is, 0 (silence) to 1. */
    val level: Float

    fun close()
}

/**
 * Opens the microphone (its permission already given, see [rememberMicrophonePermission]).
 * Null when it cannot be opened (none, or taken by another app).
 */
expect fun openMicrophone(): MicrophoneCapture?

/**
 * What asks for the microphone when it is not allowed yet; [onResult] is true when it may
 * be used (at once when it already was). Desktop: always true.
 */
@Composable
expect fun rememberMicrophonePermission(onResult: (Boolean) -> Unit): () -> Unit

/**
 * Collects a platform's microphone sound (any sample rate, mono) and gives it as 16 kHz
 * mono (averaging each 16 kHz sample's span of the source, which also keeps higher
 * pitches out). At most [maxSeconds] are kept.
 */
internal class PcmCollector(private val sourceRate: Int, private val maxSeconds: Int = 16) {
    private val lock = Any()
    private var source = FloatArray(sourceRate * 2)
    private var count = 0

    @kotlin.concurrent.Volatile
    var level: Float = 0f
        private set

    /** [length] samples of [data] (-1..1). */
    fun add(data: FloatArray, length: Int = data.size) {
        var sum = 0.0
        var peak = 0f
        for (i in 0 until length) {
            sum += data[i] * data[i]
            val a = kotlin.math.abs(data[i])
            if (a > peak) peak = a
        }
        // Music across a room reaches a laptop's microphone quietly: the louder of the
        // average and the peaks, raised so the rings move for it too.
        level = maxOf(sqrt(sum / length.coerceAtLeast(1)) * 8, peak * 1.5).toFloat().coerceIn(0f, 1f)
        locked(lock) {
            val room = sourceRate * maxSeconds - count
            val taken = minOf(room, length)
            if (taken <= 0) return
            if (count + taken > source.size) source = source.copyOf(maxOf(source.size * 2, count + taken))
            data.copyInto(source, count, 0, taken)
            count += taken
        }
    }

    /** [length] samples of [data] (16-bit). */
    fun add(data: ShortArray, length: Int = data.size) {
        add(FloatArray(length) { data[it] / 32768f }, length)
    }

    fun samples(): ShortArray {
        val (copy, size) = locked(lock) { source.copyOf(count) to count }
        if (size == 0) return ShortArray(0)
        val ratio = sourceRate / 16_000.0
        val out = ShortArray((size / ratio).toInt())
        for (n in out.indices) {
            val from = (n * ratio).toInt()
            val to = ((n + 1) * ratio).toInt().coerceIn(from + 1, size)
            var sum = 0.0
            for (i in from until to) sum += copy[i]
            out[n] = (sum / (to - from) * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }
}

/**
 * [samples] made louder when they are quiet (up to 24×, the loudest point to about 80 %):
 * the fingerprint keeps only peaks above a fixed loudness, so music heard faintly across
 * a room gave too few of them to be recognized.
 */
internal fun louder(samples: ShortArray): ShortArray {
    var peak = 0
    for (s in samples) { val a = kotlin.math.abs(s.toInt()); if (a > peak) peak = a }
    if (peak == 0) return samples
    val gain = minOf(24.0, 26_000.0 / peak)
    if (gain <= 1.2) return samples
    return ShortArray(samples.size) { (samples[it] * gain).toInt().coerceIn(-32768, 32767).toShort() }
}

