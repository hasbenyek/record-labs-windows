package com.recordlabs.desktop

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

/** The Android application, set by the app before the UI starts. */
object AndroidAppContext {
    @Volatile lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
        // The earlier app's playlists (Record Labs 1.9.x), read once after the update.
        com.recordlabs.desktop.data.LegacyLibrarySource.reader = {
            com.recordlabs.desktop.data.readLegacyLibrary(this.context)
        }
    }
}

actual fun copyToClipboard(text: String) {
    runCatching {
        val context = AndroidAppContext.context
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Record Labs", text))
    }
}

actual fun pickMusicFolder(title: String, onPicked: (com.recordlabs.desktop.platform.File) -> Unit) {
    runCatching {
        Toast.makeText(AndroidAppContext.context, "Local music folders are not available on Android yet", Toast.LENGTH_SHORT).show()
    }
}

/** Backups go to Android/data/com.recordlabs.android/files/backups (reachable from a computer over USB). */
private fun backupFolder(): java.io.File =
    (AndroidAppContext.context.getExternalFilesDir("backups") ?: java.io.File(AndroidAppContext.context.filesDir, "backups")).apply { mkdirs() }

actual fun pickBackupSaveFile(defaultName: String, onPicked: (com.recordlabs.desktop.platform.File) -> Unit) {
    runCatching { onPicked(com.recordlabs.desktop.platform.File.wrap(java.io.File(backupFolder(), defaultName))) }
}

actual fun saveExportFile(defaultName: String, mimeType: String, bytes: ByteArray, onDone: (String?) -> Unit) {
    val context = AndroidAppContext.context
    val saved = runCatching {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, defaultName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS + "/Record Labs")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Downloads is not available")
            resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("Downloads is not available")
            "Download/Record Labs/$defaultName"
        } else {
            val folder = (context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: java.io.File(context.filesDir, "exports")).apply { mkdirs() }
            java.io.File(folder, defaultName).also { it.writeBytes(bytes) }.absolutePath
        }
    }.getOrNull()
    onDone(saved)
}

actual fun pickBackupOpenFile(onPicked: (com.recordlabs.desktop.platform.File) -> Unit) {
    val newest = runCatching {
        backupFolder().listFiles { file -> file.isFile && file.extension.equals("zip", true) }?.maxByOrNull { it.lastModified() }
    }.getOrNull()
    if (newest != null) onPicked(com.recordlabs.desktop.platform.File.wrap(newest))
    else runCatching {
        Toast.makeText(AndroidAppContext.context, "No backup found in ${backupFolder().absolutePath}", Toast.LENGTH_LONG).show()
    }
}
