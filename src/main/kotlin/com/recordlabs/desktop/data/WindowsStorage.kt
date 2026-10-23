package com.recordlabs.desktop.data

import java.io.File

/**
 * Stable app storage shared by updates of the same installation.
 * Windows: %LOCALAPPDATA%\RecordLabs\<installId>. Linux: ~/.local/share/record-labs/default.
 */
object WindowsStorage {
    private val installRoot = System.getProperty("compose.application.resources.dir")
        ?.let(::File)?.parentFile?.parentFile
    val installId: String = if (Platform.usesStableInstallId) {
        if (installRoot == null) "development" else "default"
    } else installRoot?.let { root ->
        // Keep the same prefix used by the former "prefix-timestamp" IDs so
        // their preferences and media directory can be migrated in place.
        root.absolutePath.hashCode().toUInt().toString(16)
    } ?: "development"

    val directory: File by lazy {
        val parent = Platform.dataRoot.apply { mkdirs() }
        val target = if (File(parent, installId).exists()) {
            File(parent, installId)
        } else {
            parent.listFiles()
                ?.filter { it.isDirectory && it.name.startsWith("$installId-") }
                ?.maxByOrNull(File::lastModified)
                ?: File(parent, installId).apply { mkdirs() }
        }

        // Ensure the newest library_snapshot.json is synced so history and liked songs aren't lost
        val targetLibrary = File(target, "library_snapshot.json")
        val candidateSnapshots = parent.listFiles()
            ?.filter { it.isDirectory && it != target }
            ?.map { File(it, "library_snapshot.json") }
            ?.filter { it.isFile && it.length() > 200 }
            .orEmpty()
        val bestSnapshot = candidateSnapshots.maxByOrNull { it.lastModified() }
        if (bestSnapshot != null) {
            if (!targetLibrary.exists() || (bestSnapshot.lastModified() > targetLibrary.lastModified() && bestSnapshot.length() > targetLibrary.length())) {
                runCatching { bestSnapshot.copyTo(targetLibrary, overwrite = true) }
            }
        }
        target
    }
}
