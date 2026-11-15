package com.recordlabs.desktop

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.recordlabs.desktop.data.PlaybackEngine
import com.recordlabs.desktop.data.Track
import com.recordlabs.desktop.data.WindowsMediaPlayer

// Desktop side of PlatformUi.kt / PlaybackEngine.kt.

actual object PlatformDisplayScale {
    actual val supported: Boolean get() = DesktopUiScale.isLinux()
    actual val options: List<Pair<String, String>> get() = DesktopUiScale.options
    actual val mode: State<String> get() = DesktopUiScale.currentModeState
    actual fun saveMode(mode: String) = DesktopUiScale.saveMode(mode)
    actual fun restart() = DesktopUiScale.restartWithCurrentScale()
}

actual object PlatformKeepAwake {
    actual fun setPlaying(playing: Boolean) = PlaybackKeepAwake.setPlaying(playing)
    actual fun setPlayerPageOpen(open: Boolean) = Unit
}

actual fun prefetchAnimatedArtwork(remoteUrl: String) = AnimatedArtworkDiskCache.prefetch(remoteUrl)

@Composable
actual fun AnimatedTrackArtwork(
    track: Track,
    modifier: Modifier,
    shape: RoundedCornerShape,
    enabled: Boolean,
    musicVideoScale: ContentScale,
    onMusicVideoAspectRatio: (Float?) -> Unit,
    showFullscreenButton: Boolean,
) = DesktopAnimatedTrackArtwork(track, modifier, shape, enabled, musicVideoScale, onMusicVideoAspectRatio, showFullscreenButton)

@Composable
actual fun GoogleLoginView(onBack: () -> Unit, onAuthenticated: () -> Unit) = DesktopGoogleLoginView(onAuthenticated)

@Composable
actual fun appPainter(name: String): androidx.compose.ui.graphics.painter.Painter =
    androidx.compose.ui.res.painterResource(name)

actual fun decodeImageBitmap(bytes: ByteArray): androidx.compose.ui.graphics.ImageBitmap =
    org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()

// Fonts (AppFonts.kt).

internal actual fun systemFontInstalled(name: String): Boolean = runCatching {
    val fonts = org.jetbrains.skia.FontMgr.default
    (0 until fonts.familiesCount).any { fonts.getFamilyName(it).equals(name, ignoreCase = true) }
}.getOrDefault(false)

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
internal actual fun systemFontFamily(name: String, weights: List<androidx.compose.ui.text.font.FontWeight>): androidx.compose.ui.text.font.FontFamily =
    androidx.compose.ui.text.font.FontFamily(weights.map { androidx.compose.ui.text.platform.SystemFont(name, it) })

internal actual fun bundledFontFamily(fonts: List<Pair<String, androidx.compose.ui.text.font.FontWeight>>): androidx.compose.ui.text.font.FontFamily =
    androidx.compose.ui.text.font.FontFamily(fonts.map { (path, weight) -> androidx.compose.ui.text.platform.Font(path, weight) })

actual fun copyToClipboard(text: String) {
    runCatching {
        java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(java.awt.datatransfer.StringSelection(text), null)
    }
}

actual fun pickMusicFolder(title: String, onPicked: (com.recordlabs.desktop.platform.File) -> Unit) {
    val chooser = javax.swing.JFileChooser(System.getProperty("user.home")).apply {
        dialogTitle = title
        fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
    }
    if (chooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) onPicked(com.recordlabs.desktop.platform.File.wrap(chooser.selectedFile))
}

@Composable
actual fun PlatformBackHandler(onBack: () -> Boolean) = Unit

actual fun pickBackupSaveFile(defaultName: String, onPicked: (com.recordlabs.desktop.platform.File) -> Unit) {
    // The system's own file window (File Explorer's on Windows), not Java's.
    var target = nativeFileDialog(
        title = "Save Record Labs backup",
        mode = java.awt.FileDialog.SAVE,
        folder = java.io.File(System.getProperty("user.home")),
        extensions = listOf("zip"),
        fileName = defaultName,
    ) ?: return
    if (!target.name.endsWith(".zip", ignoreCase = true)) target = java.io.File(target.parentFile, "${target.name}.zip")
    onPicked(com.recordlabs.desktop.platform.File.wrap(target))
}

actual fun saveExportFile(defaultName: String, mimeType: String, bytes: ByteArray, onDone: (String?) -> Unit) {
    val extension = defaultName.substringAfterLast('.', "csv")
    val downloads = java.io.File(System.getProperty("user.home"), "Downloads").takeIf { it.isDirectory }
    var target = nativeFileDialog(
        title = "Export playlist",
        mode = java.awt.FileDialog.SAVE,
        folder = downloads ?: java.io.File(System.getProperty("user.home")),
        extensions = listOf(extension),
        fileName = defaultName,
    ) ?: return onDone(null)
    if (!target.name.endsWith(".$extension", ignoreCase = true)) target = java.io.File(target.parentFile, "${target.name}.$extension")
    onDone(runCatching { target.writeBytes(bytes); target.absolutePath }.getOrNull())
}

actual fun pickBackupOpenFile(onPicked: (com.recordlabs.desktop.platform.File) -> Unit) {
    nativeFileDialog(
        title = "Open Record Labs backup",
        mode = java.awt.FileDialog.LOAD,
        folder = java.io.File(System.getProperty("user.home")),
        extensions = listOf("zip"),
    )?.let { onPicked(com.recordlabs.desktop.platform.File.wrap(it)) }
}

actual fun lyricGlowShadow(color: androidx.compose.ui.graphics.Color, desktopBlurRadius: Float): androidx.compose.ui.graphics.Shadow =
    androidx.compose.ui.graphics.Shadow(color, blurRadius = desktopBlurRadius)

internal actual fun platformUiFontFamily(): androidx.compose.ui.text.font.FontFamily? = null
