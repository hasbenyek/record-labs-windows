package com.recordlabs.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine

/**
 * One way to record: a mixer (null: the system's default recording device) and a format.
 * 16 kHz mono first (what the recognition reads); sound cards that only take their own
 * rate or stereo (often on Linux) get 48/44.1 kHz and stereo, mixed down to mono.
 */
private class MicSource(val mixer: Mixer.Info?, val format: AudioFormat)

private val micFormats = listOf(
    AudioFormat(16_000f, 16, 1, true, false),
    AudioFormat(48_000f, 16, 1, true, false),
    AudioFormat(44_100f, 16, 1, true, false),
    AudioFormat(48_000f, 16, 2, true, false),
    AudioFormat(44_100f, 16, 2, true, false),
)

/** Every way to record, the default recording device first, then each other one. */
private fun micSources(): List<MicSource> {
    val sources = ArrayList<MicSource>()
    for (format in micFormats) {
        if (AudioSystem.isLineSupported(DataLine.Info(TargetDataLine::class.java, format))) sources += MicSource(null, format)
    }
    val mixers = runCatching { AudioSystem.getMixerInfo().toList() }.getOrDefault(emptyList())
        // Ports are volume controls, not recordings.
        .filterNot { it.name.startsWith("Port ", ignoreCase = true) }
    for (mixer in mixers) {
        val device = runCatching { AudioSystem.getMixer(mixer) }.getOrNull() ?: continue
        for (format in micFormats) {
            if (runCatching { device.isLineSupported(DataLine.Info(TargetDataLine::class.java, format)) }.getOrDefault(false)) {
                sources += MicSource(mixer, format)
                break
            }
        }
    }
    return sources
}

private fun open(source: MicSource): TargetDataLine? = runCatching {
    val info = DataLine.Info(TargetDataLine::class.java, source.format)
    val line = (if (source.mixer == null) AudioSystem.getLine(info) else AudioSystem.getMixer(source.mixer).getLine(info)) as TargetDataLine
    line.open(source.format)
    line.start()
    line
}.getOrNull()

actual fun openMicrophone(): MicrophoneCapture? {
    val sources = micSources()
    for ((index, source) in sources.withIndex()) {
        val line = open(source) ?: continue
        return DesktopMicrophone(sources, index, line)
    }
    return null
}

/**
 * Records from the first source that opens; one that gives only digital silence for its
 * first 1.2 s (a muted or virtual default device, or a driver that sends zeros at a rate
 * it does not really take, as some do on Windows) is left for the next one.
 */
private class DesktopMicrophone(
    private val sources: List<MicSource>,
    firstIndex: Int,
    firstLine: TargetDataLine,
) : MicrophoneCapture {
    @Volatile private var open = true
    @Volatile private var collector = PcmCollector(sources[firstIndex].format.sampleRate.toInt())
    @Volatile private var line: TargetDataLine? = firstLine

    private val reader = Thread({
        var index = firstIndex
        var current: TargetDataLine? = firstLine
        while (open && current != null) {
            val format = sources[index].format
            val channels = format.channels
            val rate = format.sampleRate.toInt()
            val frameBytes = 2 * channels
            val bytes = ByteArray(rate / 10 * frameBytes)
            val mono = ShortArray(rate / 10)
            var heardSamples = 0
            var loudest = 0
            var silent = false
            while (open) {
                val read = runCatching { current!!.read(bytes, 0, bytes.size) }.getOrDefault(-1)
                if (read <= 0) break
                val frames = read / frameBytes
                for (f in 0 until frames) {
                    var sum = 0
                    for (c in 0 until channels) {
                        val at = f * frameBytes + c * 2
                        sum += ((bytes[at + 1].toInt() shl 8) or (bytes[at].toInt() and 0xFF)).toShort().toInt()
                    }
                    val sample = sum / channels
                    mono[f] = sample.toShort()
                    if (kotlin.math.abs(sample) > loudest) loudest = kotlin.math.abs(sample)
                }
                collector.add(mono, frames)
                heardSamples += frames
                if (heardSamples >= rate * 12 / 10 && loudest <= 1 && index < sources.lastIndex) { silent = true; break }
                if (heardSamples >= rate * 12 / 10) loudest = Int.MAX_VALUE // past the check
            }
            runCatching { current?.stop() }
            runCatching { current?.close() }
            current = null
            if (!open || !silent) break
            // Silence only: the next recording device or format.
            while (++index < sources.size) {
                current = open(sources[index]) ?: continue
                collector = PcmCollector(sources[index].format.sampleRate.toInt())
                line = current
                break
            }
        }
        // Closed while the next device was being opened: that one is let go too (it would
        // otherwise keep the system showing the app using the microphone).
        runCatching { current?.stop() }
        runCatching { current?.close() }
    }, "record-labs-microphone").apply { isDaemon = true; start() }

    override fun samples(): ShortArray = collector.samples()
    override val level: Float get() = collector.level

    override fun close() {
        open = false
        runCatching { line?.stop() }
        runCatching { line?.close() }
    }
}

/** Desktop: the system asks by itself (macOS), or its privacy settings decide (Windows). */
@Composable
actual fun rememberMicrophonePermission(onResult: (Boolean) -> Unit): () -> Unit {
    val latest = rememberUpdatedState(onResult)
    return remember { { latest.value(true) } }
}
