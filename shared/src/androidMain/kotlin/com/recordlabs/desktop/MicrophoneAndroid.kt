package com.recordlabs.desktop

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@SuppressLint("MissingPermission")
actual fun openMicrophone(): MicrophoneCapture? {
    val context = AndroidAppContext.context
    if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return null
    val rate = 16_000
    val minimum = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    if (minimum <= 0) return null
    val record = runCatching {
        AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minimum, rate / 5 * 2))
    }.getOrNull() ?: return null
    if (record.state != AudioRecord.STATE_INITIALIZED) { record.release(); return null }
    runCatching { record.startRecording() }.onFailure { record.release(); return null }
    return AndroidMicrophone(record, rate)
}

private class AndroidMicrophone(private val record: AudioRecord, rate: Int) : MicrophoneCapture {
    private val collector = PcmCollector(rate)
    @Volatile private var open = true
    private val reader = Thread({
        val buffer = ShortArray(rate / 10)
        while (open) {
            val read = record.read(buffer, 0, buffer.size)
            if (read < 0) break
            if (read > 0) collector.add(buffer, read)
        }
    }, "record-labs-microphone").apply { isDaemon = true; start() }

    override fun samples(): ShortArray = collector.samples()
    override val level: Float get() = collector.level

    override fun close() {
        open = false
        runCatching { record.stop() }
        runCatching { record.release() }
    }
}

@Composable
actual fun rememberMicrophonePermission(onResult: (Boolean) -> Unit): () -> Unit {
    val latest = rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> latest.value(granted) }
    return remember(launcher) {
        {
            val context = AndroidAppContext.context
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) latest.value(true)
            else runCatching { launcher.launch(Manifest.permission.RECORD_AUDIO) }.onFailure { latest.value(false) }
        }
    }
}
