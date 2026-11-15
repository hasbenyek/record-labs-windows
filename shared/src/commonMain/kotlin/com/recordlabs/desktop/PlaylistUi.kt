package com.recordlabs.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.recordlabs.desktop.data.MigrationMatch
import com.recordlabs.desktop.ui.LineIcons
import com.recordlabs.desktop.data.MigrationSource
import com.recordlabs.desktop.data.PlaylistMigration
import com.recordlabs.desktop.data.Track
import com.recordlabs.desktop.data.WindowsMusicRepository
import com.recordlabs.desktop.data.WindowsPlaylist
import kotlinx.coroutines.launch

/**
 * What the Library's playlist screens ask the app to do (set by the app): the library on
 * this device and the YouTube Music account change together.
 */
internal object PlaylistActions {
    /** title, description, privacy: true when made (false: the name is taken). */
    var create: (String, String, String) -> Boolean = { _, _, _ -> false }
    /** name, new title, description, privacy. */
    var edit: (String, String, String, String) -> Boolean = { _, _, _, _ -> false }
    var delete: (WindowsPlaylist) -> Unit = {}
    var togglePin: (WindowsPlaylist) -> Unit = {}
    var share: (WindowsPlaylist) -> Unit = {}
    /** A playlist brought over by Playlists Migration: name, songs. The name it got, or null. */
    var createImported: (String, List<Track>) -> String? = { _, _ -> null }
    /** A playlist's songs, from the account when they are not kept here. */
    var loadTracks: suspend (WindowsPlaylist) -> List<Track> = { it.tracks }
    var notice: (String) -> Unit = {}
    /** A new cover (JPEG or PNG) for the playlist on the account; null removes its own cover. */
    var setCover: (WindowsPlaylist, ByteArray?) -> Unit = { _, _ -> }
    /** Collaboration on (a new invite link, copied) or off. */
    var collaborate: (WindowsPlaylist, Boolean) -> Unit = { _, _ -> }
}

/** YouTube Music's three privacy settings, as in its "New playlist" form. */
internal enum class PlaylistPrivacy(val value: String, val label: String, val hint: String, val icon: ImageVector) {
    Public("PUBLIC", "Public", "Anyone can search for and view", Icons.Default.Public),
    Unlisted("UNLISTED", "Unlisted", "Anyone with the link can view", Icons.Default.Link),
    Private("PRIVATE", "Private", "Only you can view", Icons.Default.Lock);

    companion object {
        fun of(value: String) = entries.firstOrNull { it.value == value } ?: Private
    }
}

/**
 * New playlist / Edit playlist, as on YouTube Music: title, description and privacy
 * (private unless chosen otherwise). [onSave] returns false when the title is taken.
 */
@Composable
internal fun PlaylistEditorDialog(
    initial: WindowsPlaylist?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, privacy: String) -> Boolean,
) {
    var title by remember { mutableStateOf(initial?.name.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var privacy by remember { mutableStateOf(PlaylistPrivacy.of(initial?.privacy ?: "PRIVATE")) }
    var privacyOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    SmoothDialog(onDismissRequest = onDismiss) { close ->
        GlassDialogCard(24.dp, Color(0xFF212121), Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                Text(if (initial == null) "New playlist" else "Edit playlist", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                GlassTextField(value = title, onValueChange = { title = it.take(150); error = null }, placeholder = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth().trackTextInputFocus())
                Spacer(Modifier.height(12.dp))
                GlassTextField(value = description, onValueChange = { description = it.take(5000) }, placeholder = { Text("Description") }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).trackTextInputFocus())
                Spacer(Modifier.height(16.dp))
                Text("Privacy", color = SecondaryText, fontSize = 12.sp)
                Box {
                    Row(
                        Modifier.padding(top = 4.dp).clip(RoundedCornerShape(12.dp)).clickable { privacyOpen = !privacyOpen }.padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(privacy.icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Text(privacy.label, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp, end = 8.dp))
                        Text(if (privacyOpen) "▴" else "▾", color = SecondaryText)
                    }
                }
                if (privacyOpen) {
                    // On the glass card: a see-through list, otherwise the grey one as before.
                    val onGlass = LocalInPageGlass.current != null
                    Surface(shape = RoundedCornerShape(12.dp), color = if (onGlass) Color.White.copy(alpha = .08f) else Color(0xFF2E2E2E), border = if (onGlass) androidx.compose.foundation.BorderStroke(0.6.dp, Color.White.copy(alpha = .14f)) else null, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Column {
                            PlaylistPrivacy.entries.forEach { option ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(if (option == privacy) Color.White.copy(alpha = .08f) else Color.Transparent)
                                        .clickable { privacy = option; privacyOpen = false }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(option.icon, null, tint = Color.White.copy(alpha = .85f), modifier = Modifier.size(22.dp))
                                    Column(Modifier.padding(start = 16.dp)) {
                                        Text(option.label, color = Color.White, fontWeight = FontWeight.SemiBold)
                                        Text(option.hint, color = SecondaryText, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                error?.let { Text(it, color = Color(0xFFFF6B60), fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp)) }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    DialogTextButton("Cancel", primary = false, onClick = close)
                    Spacer(Modifier.width(8.dp))
                    val canSave = title.isNotBlank()
                    DialogTextButton(if (initial == null) "Create" else "Save", primary = true, enabled = canSave) {
                        if (onSave(title.trim(), description.trim(), privacy.value)) close()
                        else error = "There is already a playlist named \"${title.trim()}\"."
                    }
                }
            }
        }
    }
}

/** A playlist's menu in the Library: edit, pin, share, delete (with a confirmation). */
@Composable
internal fun PlaylistMenu(
    playlist: WindowsPlaylist,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onChangeCover: () -> Unit = {},
) {
    IosMenuDialog(onDismiss = onDismiss) {
        IosMenuGroup {
            IosMenuRow("Edit playlist", LineIcons.Pencil, { onDismiss(); onEdit() }, subtitle = "Name, description, privacy")
            IosMenuRow("Change cover", LineIcons.Photo, { onDismiss(); onChangeCover() }, subtitle = "Upload your own picture")
            if (playlist.coverUrl != null && playlist.browseId != null) {
                IosMenuRow("Remove own cover", LineIcons.Xmark, { onDismiss(); PlaylistActions.setCover(playlist, null) })
            }
            IosMenuRow(if (playlist.pinned) "Unpin" else "Pin to top", LineIcons.Pin, { onDismiss(); PlaylistActions.togglePin(playlist) })
        }
        IosMenuGroup {
            IosMenuRow("Share link", LineIcons.Share, { onDismiss(); PlaylistActions.share(playlist) })
            if (playlist.collaborative) {
                IosMenuRow("Copy a new invite link", LineIcons.People, { onDismiss(); PlaylistActions.collaborate(playlist, true) }, subtitle = "Friends with it can add songs")
                IosMenuRow("Stop collaboration", LineIcons.Xmark, { onDismiss(); PlaylistActions.collaborate(playlist, false) })
            } else {
                IosMenuRow(
                    "Collaborate", LineIcons.People, { onDismiss(); PlaylistActions.collaborate(playlist, true) },
                    subtitle = if (playlist.privacy == "PRIVATE") "Invite friends to add songs (makes it unlisted)" else "Invite friends to add songs",
                )
            }
        }
        IosMenuGroup {
            IosMenuRow("Delete playlist", LineIcons.Trash, { onDismiss(); onDeleteRequest() }, destructive = true)
        }
    }
}

@Composable
internal fun DeletePlaylistDialog(playlist: WindowsPlaylist, onDismiss: () -> Unit) {
    SmoothDialog(onDismissRequest = onDismiss) { close ->
        GlassDialogCard(24.dp, Color(0xFF212121), Modifier.widthIn(max = 440.dp).fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                Text("Delete playlist?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (playlist.browseId != null) "\"${playlist.name}\" will be deleted here and from your account. This can't be undone."
                    else "\"${playlist.name}\" will be deleted. This can't be undone.",
                    color = SecondaryText, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp),
                )
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    DialogTextButton("Cancel", primary = false, onClick = close)
                    Spacer(Modifier.width(8.dp))
                    DialogTextButton("Delete", primary = true, primaryFill = Color(0xFFD93A30), primaryText = Color.White) {
                        PlaylistActions.delete(playlist); close()
                    }
                }
            }
        }
    }
}

/**
 * Playlists Migration. Import: a playlist link from Spotify, Apple Music, Deezer or
 * YouTube (or a pasted list of songs) becomes a private playlist here and on YouTube
 * Music, its songs matched on YouTube Music. Export: a playlist's songs as a list (and
 * its YouTube Music link) for other apps and transfer services.
 */
@Composable
internal fun PlaylistMigrationDialog(
    repository: WindowsMusicRepository,
    playlists: List<WindowsPlaylist>,
    onDismiss: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
) {
    var exportTab by remember { mutableStateOf(false) }
    SmoothDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) { close ->
        GlassDialogCard(24.dp, Color(0xFF1C1C1E), Modifier.padding(16.dp).widthIn(max = 620.dp).fillMaxWidth().heightIn(max = 720.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Playlists Migration", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Bring playlists from other apps, or take yours to them", color = SecondaryText, fontSize = 13.sp)
                    }
                    Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = close).padding(8.dp))
                }
                Row(Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MigrationTab("Import to Record Labs", !exportTab) { exportTab = false }
                    MigrationTab("Export from Record Labs", exportTab) { exportTab = true }
                }
                if (exportTab) MigrationExport(playlists) else MigrationImport(repository, close, onOpenPlaylist)
            }
        }
    }
}

@Composable
private fun MigrationTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) Color.Black else Color.White,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (selected) Color.White else Color.White.copy(alpha = .10f))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun MigrationImport(repository: WindowsMusicRepository, onDismiss: () -> Unit, onOpenPlaylist: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var source by remember { mutableStateOf<MigrationSource?>(null) }
    var matches by remember { mutableStateOf<List<MigrationMatch>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Text("Paste a playlist link from Spotify, Apple Music, Deezer or YouTube, or a list of songs (one \"Artist - Title\" per line).", color = SecondaryText, fontSize = 13.sp)
    Spacer(Modifier.height(10.dp))
    GlassTextField(
        value = input,
        onValueChange = { input = it; error = null },
        placeholder = { Text("https://open.spotify.com/playlist/…") },
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp, max = 160.dp).trackTextInputFocus(),
    )
    Spacer(Modifier.height(12.dp))
    if (source == null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlassButton(prominent = true, enabled = input.isNotBlank() && !busy, onClick = {
                busy = true; error = null; matches = emptyList(); progress = 0f
                scope.launch {
                    runCatching {
                        status = "Reading the playlist…"
                        val read = PlaylistMigration.read(repository, input)
                        name = read.name
                        status = "Finding ${read.tracks.size} songs…"
                        val found = PlaylistMigration.match(repository, read) { done, total -> progress = done / total.toFloat() }
                        source = read
                        matches = found
                    }.onFailure { error = it.message ?: "The playlist could not be read." }
                    status = null
                    busy = false
                }
            }) {
                if (busy) { CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text("Find songs")
            }
        }
        status?.let {
            Text(it, color = SecondaryText, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
            if (progress > 0f) LinearProgressIndicator(progress = progress, color = Color.White, backgroundColor = Color.White.copy(alpha = .15f), modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
    } else {
        val found = matches.mapNotNull { it.track }.distinctBy { it.id }
        val missing = matches.filter { it.track == null }
        GlassTextField(value = name, onValueChange = { name = it.take(150) }, placeholder = { Text("Playlist name") }, singleLine = true, modifier = Modifier.fillMaxWidth().trackTextInputFocus())
        Text(
            "${source!!.service}: ${found.size} of ${matches.size} songs found" + if (missing.isNotEmpty()) " (${missing.size} not found, listed below)" else "",
            color = SecondaryText, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp),
        )
        LazyColumn(Modifier.heightIn(max = 300.dp).padding(top = 8.dp)) {
            items(matches) { match ->
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (match.track != null) Icons.Default.Check else Icons.Default.Close, null, tint = if (match.track != null) Color(0xFF5BD47A) else Color(0xFFFF6B60), modifier = Modifier.size(16.dp))
                    Column(Modifier.padding(start = 10.dp)) {
                        Text(match.source.title, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            match.track?.let { "${it.title} • ${it.artist}" } ?: match.source.artist.ifBlank { "Not found" },
                            color = SecondaryText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        error?.let { Text(it, color = Color(0xFFFF6B60), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassButton(onClick = { source = null; matches = emptyList() }) { Text("Back") }
            GlassButton(prominent = true, enabled = found.isNotEmpty() && name.isNotBlank(), onClick = {
                val created = PlaylistActions.createImported(name.trim(), found)
                if (created == null) error = "There is already a playlist named \"${name.trim()}\"."
                else {
                    PlaylistActions.notice("\"$created\" added with ${found.size} songs")
                    onDismiss()
                    onOpenPlaylist(created)
                }
            }) { Text("Create playlist") }
        }
        Text("It is made as a private playlist, saved to your account.", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
    }
    error?.takeIf { source == null }?.let { Text(it, color = Color(0xFFFF6B60), fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp)) }
}

@Composable
private fun MigrationExport(playlists: List<WindowsPlaylist>) {
    val scope = rememberCoroutineScope()
    var busyName by remember { mutableStateOf<String?>(null) }
    Text("Other apps don't let Record Labs add playlists to them directly. Copy a playlist's songs as a list, or save it as a CSV file, then import it in the other app or a transfer service (such as SongShift, TuneMyMusic or Soundiiz).", color = SecondaryText, fontSize = 13.sp)
    Spacer(Modifier.height(10.dp))
    if (playlists.isEmpty()) Text("No playlists yet.", color = SecondaryText, fontSize = 13.sp)
    LazyColumn(Modifier.heightIn(max = 420.dp)) {
        items(playlists) { playlist ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(playlist.name, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(PlaylistPrivacy.of(playlist.privacy).label, color = SecondaryText, fontSize = 12.sp)
                }
                GlassButton(enabled = busyName == null, onClick = {
                    busyName = playlist.name
                    scope.launch {
                        val tracks = runCatching { PlaylistActions.loadTracks(playlist) }.getOrDefault(playlist.tracks)
                        val link = playlist.browseId?.let { "https://music.youtube.com/playlist?list=${it.removePrefix("VL")}" }
                        copyToClipboard(PlaylistMigration.exportText(playlist.name, tracks, link))
                        PlaylistActions.notice("Copied ${tracks.size} songs of \"${playlist.name}\"")
                        busyName = null
                    }
                }) {
                    if (busyName == playlist.name) { CircularProgressIndicator(Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(6.dp)) }
                    Text("Copy list")
                }
                Spacer(Modifier.width(8.dp))
                GlassButton(enabled = busyName == null, onClick = {
                    busyName = playlist.name + "\u0000csv"
                    scope.launch {
                        val tracks = runCatching { PlaylistActions.loadTracks(playlist) }.getOrDefault(playlist.tracks)
                        val csv = PlaylistMigration.exportCsv(playlist.name, tracks)
                        saveExportFile(PlaylistMigration.exportFileName(playlist.name, "csv"), "text/csv", ("\uFEFF" + csv).encodeToByteArray()) { where ->
                            PlaylistActions.notice(if (where != null) "Saved ${tracks.size} songs to $where" else "The CSV file was not saved")
                        }
                        busyName = null
                    }
                }) {
                    if (busyName == playlist.name + "\u0000csv") { CircularProgressIndicator(Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(6.dp)) }
                    Text("CSV")
                }
            }
        }
    }
}
