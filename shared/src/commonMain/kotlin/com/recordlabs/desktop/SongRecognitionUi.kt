package com.recordlabs.desktop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recordlabs.desktop.data.RecognizedSong
import com.recordlabs.desktop.data.SongSignatureGenerator
import com.recordlabs.desktop.data.Track
import com.recordlabs.desktop.data.WindowsMusicRepository
import com.recordlabs.desktop.ui.LineIcons
import kotlinx.coroutines.delay

/**
 * Recognize the song playing around (the button in the search box): asks for the
 * microphone when needed, then the app shows [SongRecognitionDialog]. Set by the app.
 */
internal object SongRecognition {
    /** The button was pressed: the app asks for the microphone, then opens the window. */
    var requested by mutableStateOf(false)
    /** The listening window is open. */
    var open by mutableStateOf(false)

    fun request() { requested = true }
}

/**
 * The search box's button for [SongRecognition]: with Liquid Glass Material, clear liquid
 * glass (no dark fill) that stretches when pulled; otherwise the search box's own material.
 */
@Composable
internal fun RecognizeSongButton(backdrop: GlassBackdrop?, modifier: Modifier = Modifier, size: Dp = 34.dp) {
    val shape = CircleShape
    val surface = if (backdrop != null) {
        Modifier.liquidGlassInteractive(backdrop, clear = true).clip(shape).border(0.8.dp, Color.White.copy(alpha = .2f), shape)
    } else {
        Modifier.clip(shape).background(HeaderGlass).border(1.dp, HeaderGlassBorder, shape)
    }
    Box(
        modifier.size(size).then(surface).clickable { SongRecognition.request() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(LineIcons.Listen, "Recognize song", tint = Color.White, modifier = Modifier.size(size * 0.56f))
    }
}

private sealed interface RecognitionStep {
    data object Listening : RecognitionStep
    data class Found(val song: RecognizedSong, val track: Track?) : RecognitionStep
    data object NoMatch : RecognitionStep
    data class Failed(val message: String) : RecognitionStep
}

/**
 * Listens for up to about 12 seconds and asks after 4, 8 and 12 seconds of sound (most
 * songs are found at the first), then shows the song with Play (its YouTube Music match)
 * and Search.
 */
@Composable
internal fun SongRecognitionDialog(
    repository: WindowsMusicRepository,
    onDismiss: () -> Unit,
    onPlay: (Track) -> Unit,
    onSearch: (String) -> Unit,
) {
    var step by remember { mutableStateOf<RecognitionStep>(RecognitionStep.Listening) }
    var attempt by remember { mutableStateOf(0) }
    var level by remember { mutableStateOf(0f) }
    var seconds by remember { mutableStateOf(0f) }
    LaunchedEffect(attempt) {
        step = RecognitionStep.Listening
        val microphone = openMicrophone()
        if (microphone == null) {
            step = RecognitionStep.Failed("The microphone could not be opened. Check that the app may use it and that no other app is using it.")
            return@LaunchedEffect
        }
        try {
            var loudest = 0f
            var failure: String? = null
            for (target in listOf(4, 8, 12)) {
                var samples = microphone.samples()
                while (samples.size < target * SongSignatureGenerator.SAMPLE_RATE) {
                    delay(120)
                    level = microphone.level
                    loudest = maxOf(loudest, level)
                    samples = microphone.samples()
                    seconds = samples.size / SongSignatureGenerator.SAMPLE_RATE.toFloat()
                }
                // Desktop microphones hear music across a room faintly: made louder there
                // (Android recognized well as it was).
                val signature = SongSignatureGenerator.generate(if (com.recordlabs.desktop.platform.AppPlatform.isAndroid) samples else louder(samples)) ?: continue
                if (signature.peakCount < 5) continue
                val song = runCatching { repository.recognizeSong(signature) }
                    .onFailure { failure = it.message }
                    // Answered (even with no song): an earlier busy moment is not the outcome.
                    .onSuccess { failure = null }
                    .getOrNull() ?: continue
                failure = null
                microphone.close()
                val track = runCatching { repository.search("${song.title} ${song.artist}", "songs").tracks.firstOrNull() }.getOrNull()
                RecognitionHistory.add(song, track)
                step = RecognitionStep.Found(song, track)
                return@LaunchedEffect
            }
            step = when {
                loudest < 0.002f -> RecognitionStep.Failed("No sound came from the microphone. Check that it is on, not muted, and chosen as the recording device, and that the app may use it.")
                failure?.contains("busy") == true -> RecognitionStep.Failed("The song recognition service is busy right now. Wait a few seconds and try again.")
                failure != null -> RecognitionStep.Failed("The song recognition service could not be reached: $failure")
                else -> RecognitionStep.NoMatch
            }
        } finally {
            microphone.close()
        }
    }

    SmoothDialog(onDismissRequest = onDismiss) { close ->
        GlassDialogCard(28.dp, Color(0xFF1C1C1E), Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
            Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(targetState = step, transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) }) { current ->
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        when (current) {
                            RecognitionStep.Listening -> {
                                ListeningRings(level)
                                Spacer(Modifier.height(18.dp))
                                Text(if (seconds < 4f) "Listening…" else "Still listening…", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text("Hold your device near the music", color = SecondaryText, fontSize = 14.sp, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(20.dp))
                                DialogTextButton("Cancel", primary = false, onClick = close)
                            }
                            is RecognitionStep.Found -> {
                                Artwork(current.song.artworkUrl ?: current.track?.artworkUrl, Modifier.size(196.dp), RoundedCornerShape(14.dp))
                                Spacer(Modifier.height(16.dp))
                                Text(current.song.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(current.song.artist, color = SecondaryText, fontSize = 15.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (current.song.album.isNotBlank()) Text(current.song.album, color = SecondaryText.copy(alpha = .8f), fontSize = 13.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(20.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    DialogTextButton("Search", primary = false) { onSearch("${current.song.title} ${current.song.artist}"); close() }
                                    current.track?.let { track -> DialogTextButton("Play", primary = true) { onPlay(track); close() } }
                                }
                            }
                            RecognitionStep.NoMatch -> {
                                RecognizeMark(Color.White.copy(alpha = .5f))
                                Spacer(Modifier.height(16.dp))
                                Text("No song found", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text("Try again closer to the music, or when it is a bit louder.", color = SecondaryText, fontSize = 14.sp, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(20.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    DialogTextButton("Close", primary = false, onClick = close)
                                    DialogTextButton("Try again", primary = true) { attempt++ }
                                }
                            }
                            is RecognitionStep.Failed -> {
                                RecognizeMark(Color.White.copy(alpha = .5f))
                                Spacer(Modifier.height(16.dp))
                                Text(current.message, color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(20.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    DialogTextButton("Close", primary = false, onClick = close)
                                    DialogTextButton("Try again", primary = true) { attempt++ }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** The mark in a soft circle. */
@Composable
private fun RecognizeMark(tint: Color, size: Dp = 96.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(Color.White.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
        Icon(LineIcons.Listen, null, tint = tint, modifier = Modifier.size(size * 0.48f))
    }
}

/** The mark with rings going out from it, wider as the sound gets louder. */
@Composable
private fun ListeningRings(level: Float) {
    val transition = rememberInfiniteTransition()
    val wave by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart))
    val loudness by animateFloatAsState(level.coerceIn(0f, 1f), tween(140))
    Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
        for (ring in 0 until 3) {
            val phase = (wave + ring / 3f) % 1f
            Box(
                Modifier.size(96.dp).graphicsLayer {
                    val grow = 1f + phase * (0.55f + loudness * 0.45f)
                    scaleX = grow
                    scaleY = grow
                    alpha = (1f - phase) * 0.5f
                }.clip(CircleShape).background(Accent.copy(alpha = .35f)),
            )
        }
        Box(
            Modifier.size(96.dp).graphicsLayer {
                val beat = 1f + loudness * 0.08f
                scaleX = beat
                scaleY = beat
            }.clip(CircleShape).background(Accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(LineIcons.Listen, null, tint = Color.Black, modifier = Modifier.size(46.dp))
        }
    }
}

/** The recognition history and what its rows do. Set by the app. */
internal object RecognitionHistory {
    var entries by mutableStateOf<List<com.recordlabs.desktop.data.RecognitionEntry>>(emptyList())
    var add: (RecognizedSong, Track?) -> Unit = { _, _ -> }
    var play: (Track) -> Unit = {}
    var search: (String) -> Unit = {}
    var remove: (String) -> Unit = {}
    var clear: () -> Unit = {}
}

private val MonthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
private val DayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

/** Days since 1 January 1970 in the device's local time. */
private fun localDay(atMs: Long): Long = (atMs + com.recordlabs.desktop.platform.localUtcOffsetMs(atMs)).floorDiv(86_400_000L)

/** Year, month (1-12) and day of [day] (days since 1970-01-01), the proleptic Gregorian calendar. */
private fun civilDate(day: Long): Triple<Int, Int, Int> {
    val z = day + 719_468
    val era = (if (z >= 0) z else z - 146_096) / 146_097
    val doe = z - era * 146_097
    val yoe = (doe - doe / 1460 + doe / 36_524 - doe / 146_096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
    val y = (yoe + era * 400 + if (m <= 2) 1 else 0).toInt()
    return Triple(y, m, d)
}

/** "Today", "Yesterday", "Wednesday, 11 November" or, another year, "Wednesday, 11 November 2025". */
private fun dayLabel(day: Long, today: Long): String = when (day) {
    today -> "Today"
    today - 1 -> "Yesterday"
    else -> {
        val (y, m, d) = civilDate(day)
        val (thisYear, _, _) = civilDate(today)
        val weekday = DayNames[(day + 4).mod(7L).toInt()]
        "$weekday, $d ${MonthNames[m - 1]}" + if (y != thisYear) " $y" else ""
    }
}

/** "16:48" in local time. */
private fun clockLabel(atMs: Long): String {
    val minutes = ((atMs + com.recordlabs.desktop.platform.localUtcOffsetMs(atMs)).floorDiv(60_000L)).mod(24L * 60)
    return "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}"
}

/**
 * Music Recognition History (in the profile menu): every song recognized, newest first,
 * under a heading per day ("Today", "Yesterday", "Wednesday, 11 November"), as the
 * iPhone's Shazam lists them. A song plays (its YouTube Music match) or is searched for;
 * its menu also removes it, and the history can be cleared.
 */
@Composable
internal fun RecognitionHistoryPage(compact: Boolean) {
    val entries = RecognitionHistory.entries
    var menuFor by remember { mutableStateOf<com.recordlabs.desktop.data.RecognitionEntry?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    val today = localDay(com.recordlabs.desktop.platform.currentTimeMillis())
    val groups = remember(entries, today) {
        entries.sortedByDescending { it.recognizedAtMs }.groupBy { localDay(it.recognizedAtMs) }.toList().sortedByDescending { it.first }
    }
    Column(Modifier.fillMaxSize()) {
        if (!compact) SettingsPageHeader("Music Recognition History", "The songs you recognized, newest first", LineIcons.Listen, compact = false)
        if (entries.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                RecognizeMark(Color.White.copy(alpha = .55f))
                Spacer(Modifier.height(16.dp))
                Text("No songs yet", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Songs you recognize with the button in the search box show up here.",
                    color = SecondaryText, fontSize = 14.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
            return@Column
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (entries.size == 1) "1 song" else "${entries.size} songs",
                color = SecondaryText, fontSize = 13.sp, modifier = Modifier.weight(1f),
            )
            Text(
                "Clear history", color = Color(0xFFFF6B5E), fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { confirmClear = true }.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxWidth().weight(1f).pageScrollbar(listState), state = listState) {
            groups.forEach { (day, songs) ->
                item(key = "day-$day") {
                    Text(
                        dayLabel(day, today),
                        color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
                    )
                }
                items(songs.size, key = { songs[it].id }) { index ->
                    val entry = songs[index]
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val track = entry.track
                                if (track != null) RecognitionHistory.play(track) else RecognitionHistory.search("${entry.title} ${entry.artist}")
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Artwork(entry.artworkUrl ?: entry.track?.artworkUrl, Modifier.size(56.dp), RoundedCornerShape(8.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(entry.artist, color = SecondaryText, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(clockLabel(entry.recognizedAtMs), color = SecondaryText.copy(alpha = .75f), fontSize = 12.sp)
                        }
                        androidx.compose.material.IconButton(onClick = { menuFor = entry }) {
                            Icon(androidx.compose.material.icons.Icons.Default.MoreVert, "More", tint = SecondaryText)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(MiniPlayerClearance + LocalBottomBarInset.current)) }
        }
    }
    menuFor?.let { entry ->
        IosMenuDialog(onDismiss = { menuFor = null }) {
            IosMenuGroup {
                entry.track?.let { track -> IosMenuRow("Play", LineIcons.Listen, { menuFor = null; RecognitionHistory.play(track) }) }
                IosMenuRow("Search", LineIcons.Search, { menuFor = null; RecognitionHistory.search("${entry.title} ${entry.artist}") })
            }
            IosMenuGroup {
                IosMenuRow("Remove from history", LineIcons.Trash, { menuFor = null; RecognitionHistory.remove(entry.id) }, destructive = true)
            }
        }
    }
    if (confirmClear) {
        SmoothDialog(onDismissRequest = { confirmClear = false }) { close ->
            GlassDialogCard(24.dp, Color(0xFF212121), Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
                Column(Modifier.padding(22.dp)) {
                    Text("Clear the history?", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("All ${entries.size} recognized songs are removed from this list.", color = SecondaryText, fontSize = 14.sp)
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        DialogTextButton("Cancel", primary = false, onClick = close)
                        Spacer(Modifier.width(8.dp))
                        DialogTextButton("Clear", primary = true, primaryFill = Color(0xFFD93A30), primaryText = Color.White) {
                            RecognitionHistory.clear(); close()
                        }
                    }
                }
            }
        }
    }
}
