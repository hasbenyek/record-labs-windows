package com.recordlabs.desktop

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberImagePicker(onPicked: (ByteArray?) -> Unit): () -> Unit {
    val latest = rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        latest.value(uri?.let { runCatching { coverBytes(it) }.getOrNull() })
    }
    return remember(launcher) { { runCatching { launcher.launch("image/*") }.onFailure { latest.value(null) } } }
}

/** The picture made at most 1600 px wide or high, as JPEG (phone photos are many MB). */
private fun coverBytes(uri: android.net.Uri): ByteArray? {
    val resolver = AndroidAppContext.context.contentResolver
    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 1600) sample *= 2
    val bitmap = resolver.openInputStream(uri)?.use {
        android.graphics.BitmapFactory.decodeStream(it, null, android.graphics.BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: return null
    val largest = maxOf(bitmap.width, bitmap.height)
    val sized = if (largest > 1600) {
        val scale = 1600f / largest
        android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt().coerceAtLeast(1), (bitmap.height * scale).toInt().coerceAtLeast(1), true)
    } else bitmap
    return java.io.ByteArrayOutputStream().use { out ->
        sized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
        out.toByteArray()
    }
}
