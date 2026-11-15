package com.recordlabs.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberImagePicker(onPicked: (ByteArray?) -> Unit): () -> Unit {
    val latest = rememberUpdatedState(onPicked)
    return remember {
        {
            val chosen = nativeFileDialog(
                title = "Choose a cover picture",
                mode = java.awt.FileDialog.LOAD,
                folder = java.io.File(System.getProperty("user.home"), "Pictures").takeIf { it.isDirectory },
                extensions = listOf("jpg", "jpeg", "png"),
            )
            latest.value(chosen?.let { runCatching { coverBytes(it) }.getOrNull() })
        }
    }
}

/** The picture as it is when small enough, otherwise made at most 1600 px wide or high (JPEG). */
private fun coverBytes(file: java.io.File): ByteArray {
    val raw = file.readBytes()
    val image = javax.imageio.ImageIO.read(java.io.ByteArrayInputStream(raw)) ?: return raw
    val largest = maxOf(image.width, image.height)
    if (largest <= 1600 && raw.size <= 4_000_000) return raw
    val scale = minOf(1f, 1600f / largest)
    val width = (image.width * scale).toInt().coerceAtLeast(1)
    val height = (image.height * scale).toInt().coerceAtLeast(1)
    val scaled = java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB)
    val g = scaled.createGraphics()
    g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.drawImage(image, 0, 0, width, height, null)
    g.dispose()
    return java.io.ByteArrayOutputStream().use { out ->
        javax.imageio.ImageIO.write(scaled, "jpg", out)
        out.toByteArray()
    }
}

/**
 * The system's own file window (File Explorer's on Windows, the desktop's on Linux), not
 * Java's. [extensions] narrow what it lists; with SAVE, [fileName] is filled in.
 * Null when it was closed without a file.
 */
internal fun nativeFileDialog(
    title: String,
    mode: Int,
    folder: java.io.File? = null,
    extensions: List<String> = emptyList(),
    fileName: String? = null,
): java.io.File? {
    val dialog = java.awt.FileDialog(null as java.awt.Frame?, title, mode)
    folder?.let { dialog.directory = it.absolutePath }
    if (extensions.isNotEmpty()) {
        // Linux and macOS go by the filter; Windows only by a pattern in the name box.
        dialog.setFilenameFilter { _, name -> extensions.any { name.endsWith(".$it", ignoreCase = true) } }
    }
    dialog.file = when {
        fileName != null -> fileName
        extensions.isNotEmpty() && System.getProperty("os.name").orEmpty().startsWith("Windows") -> extensions.joinToString(";") { "*.$it" }
        else -> null
    }
    dialog.isAlwaysOnTop = true
    dialog.isVisible = true
    val name = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    return java.io.File(directory, name)
}
