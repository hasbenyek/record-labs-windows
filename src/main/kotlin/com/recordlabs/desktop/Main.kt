package com.recordlabs.desktop

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerIcon

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import com.recordlabs.desktop.ui.Heroicons
import com.recordlabs.desktop.ui.CustomPlayerIcons
import androidx.compose.animation.animateColorAsState


import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material.AlertDialog as MaterialAlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.darkColors
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text


import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.text.drawText
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.asComposeRenderEffect

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.recordlabs.desktop.data.WindowsMusicRepository
import com.recordlabs.desktop.data.Track
import com.recordlabs.desktop.data.catalogKindFor
import com.recordlabs.desktop.data.playsAsIs
import com.recordlabs.desktop.data.HomeFeed
import com.recordlabs.desktop.data.HomeChip
import com.recordlabs.desktop.data.HomeItem
import com.recordlabs.desktop.data.HomeShelf
import com.recordlabs.desktop.data.HomeShelfLayout
import com.recordlabs.desktop.data.SearchHistoryStore
import com.recordlabs.desktop.data.SearchResults
import com.recordlabs.desktop.data.SearchSummaryShelf
import com.recordlabs.desktop.data.SearchTopResult
import com.recordlabs.desktop.data.rankSongsForQuery
import com.recordlabs.desktop.data.searchNameMatches
import com.recordlabs.desktop.data.CatalogItem
import com.recordlabs.desktop.data.CatalogPageData
import com.recordlabs.desktop.data.WindowsMediaPlayer
import com.recordlabs.desktop.data.WindowsLibraryStore
import com.recordlabs.desktop.data.WindowsLibrarySnapshot
import com.recordlabs.desktop.data.mostPlayed
import com.recordlabs.desktop.data.WindowsListeningTracker
import com.recordlabs.desktop.data.WindowsArtistLogoRepository
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.recordlabs.desktop.data.WindowsPlaylist
import com.recordlabs.desktop.data.WindowsAudioCache
import com.recordlabs.desktop.data.WindowsAnimatedArtworkRepository
import com.recordlabs.desktop.data.WindowsOfflineArtworkCache
import com.recordlabs.desktop.data.WindowsLocalMusicLibrary
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.math.sqrt
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URI

import androidx.compose.ui.res.painterResource
import com.recordlabs.desktop.data.PlayerColorExtractor
import com.recordlabs.desktop.data.WindowsLyricsProviderRegistry
import com.recordlabs.desktop.data.LyricsTranslationSettings
import com.recordlabs.desktop.data.WindowsLyricsTranslator
import com.recordlabs.desktop.data.WindowsPreferences
import com.recordlabs.desktop.data.WindowsListenTogetherClient
import com.recordlabs.desktop.data.ListenTogetherConnection
import com.recordlabs.desktop.data.ListenTogetherUser
import com.recordlabs.desktop.data.RichLyricLine
import com.recordlabs.desktop.data.LyricsCandidate
import com.recordlabs.desktop.data.LyricsResult
import com.recordlabs.desktop.data.WindowsRichLyrics
import com.recordlabs.desktop.data.WindowsRomanizer
import com.recordlabs.desktop.data.WindowsRecordLabsConnect
import com.recordlabs.desktop.data.ConnectPlaybackState
import com.recordlabs.desktop.data.ConnectDevice
import com.recordlabs.desktop.data.WindowsConnectDiscoveryStatus
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Shapes

private val Background = Color(0xFF000000)
val LocalOpenCatalog = androidx.compose.runtime.staticCompositionLocalOf<(String, String, String?, String?, String?) -> Unit> { { _, _, _, _, _ -> } }
/** Opens the album a track belongs to (resolved from the track itself); null outside the app shell. */
val LocalOpenTrackAlbum = androidx.compose.runtime.staticCompositionLocalOf<((Track) -> Unit)?> { null }
/** Opens a song's artist (track, artist name) through the song's own artist link. */
val LocalOpenTrackArtist = androidx.compose.runtime.staticCompositionLocalOf<((Track, String) -> Unit)?> { null }
internal val SurfaceColor = Color(0xFF1A1C20)
// Header buttons and search box: see-through, like the player's Lyrics/PiP pill.
// Over dark pages this looks exactly like that pill (12 % white); over a bright
// photo it darkens what is behind a little, so the white icons stay readable.
internal val HeaderGlass = Color(0.28f, 0.28f, 0.28f, 0.43f)
internal val HeaderGlassBorder = Color.White.copy(alpha = 0.14f)
// The search suggestions panel: the same grey over a blurred copy of the page.
private val SearchPanelGlass = Color(0.20f, 0.20f, 0.20f, 0.52f)

/** The page recorded under the open search suggestions, for their frosted-glass blur. */
private object SearchBackdrop {
    var layer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null
    var active by mutableStateOf(false)
    var originInWindow = androidx.compose.ui.geometry.Offset.Zero
}
internal val Elevated = Color(0xFF252832)
// Record Labs Android default theme seed color.
private val AccentColor = Color(0xFFFF2D55)
/** Settings > Appearance > Flat: the neutral colour used instead of every accent. */
internal val FlatAccent = Color(0xFFE6E6E6)
// Bottom room on Profile/Settings pages so the floating mini player
// (68dp tall, 10dp above the bottom) never covers their last item.
internal val MiniPlayerClearance = 84.dp

/** The little red count on the profile icon (and the rows leading to About) while an update waits. */
@Composable
internal fun UpdateBadge(count: Int, modifier: Modifier = Modifier) {
    androidx.compose.animation.AnimatedVisibility(
        visible = count > 0,
        modifier = modifier,
        enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut(),
    ) {
        Box(
            Modifier.size(18.dp).background(Color(0xFFFF3B30), CircleShape).border(1.5.dp, Color(0xFF101114), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(count.coerceAtMost(9).toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 10.sp)
        }
    }
}
/** The accent colour, or the neutral one when the Flat style is chosen. */
internal val Accent: Color get() = ThemeTransition.accent

/** Colours that change smoothly when Settings > Appearance > Style changes (set by the app window). */
internal object ThemeTransition {
    var accent by mutableStateOf(if (com.recordlabs.desktop.data.AppSettings.flatTheme) FlatAccent else AccentColor)
}

/**
 * The artwork's accent, lighter where needed (same hue), so icons in that
 * colour stand out against the player's moving artwork background, which is
 * kept in the middle of the brightness range.
 */
private fun contrastingAccent(color: Color): Color {
    fun linear(c: Float) = if (c <= 0.04045f) c / 12.92f else Math.pow(((c + 0.055f) / 1.055f).toDouble(), 2.4).toFloat()
    fun gamma(c: Float) = if (c <= 0.0031308f) c * 12.92f else (1.055f * Math.pow(c.toDouble(), 1.0 / 2.4).toFloat() - 0.055f)
    val r = linear(color.red); val g = linear(color.green); val b = linear(color.blue)
    val l = Math.cbrt((0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b).toDouble()).toFloat()
    val m = Math.cbrt((0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b).toDouble()).toFloat()
    val s = Math.cbrt((0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b).toDouble()).toFloat()
    val lightness = 0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s
    val a = 1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s
    val bb = 0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s
    val target = maxOf(lightness, PLAYER_ACCENT_MIN_LIGHTNESS)
    if (target == lightness) return color
    // Same hue, a little more colourful; reduced only as far as needed to stay displayable.
    var chroma = 1.15f
    while (chroma > 0f) {
        val l2 = target + 0.3963377774f * a * chroma + 0.2158037573f * bb * chroma
        val m2 = target - 0.1055613458f * a * chroma - 0.0638541728f * bb * chroma
        val s2 = target - 0.0894841775f * a * chroma - 1.2914855480f * bb * chroma
        val lc = l2 * l2 * l2; val mc = m2 * m2 * m2; val sc = s2 * s2 * s2
        val rr = 4.0767416621f * lc - 3.3077115913f * mc + 0.2309699292f * sc
        val gg = -1.2684380046f * lc + 2.6097574011f * mc - 0.3413193965f * sc
        val bl = -0.0041960863f * lc - 0.7034186147f * mc + 1.7076147010f * sc
        if (rr in -0.001f..1.001f && gg in -0.001f..1.001f && bl in -0.001f..1.001f) {
            return Color(gamma(rr.coerceIn(0f, 1f)), gamma(gg.coerceIn(0f, 1f)), gamma(bl.coerceIn(0f, 1f)), color.alpha)
        }
        chroma -= 0.05f
    }
    return Color(gamma(target * target * target), gamma(target * target * target), gamma(target * target * target), color.alpha)
}

/** Brightness (OKLab) the accent has at least on the player and lyrics pages. */
private const val PLAYER_ACCENT_MIN_LIGHTNESS = 0.80f

@Composable
private fun PlayerPageAccentTheme(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colors
    val accent = remember(colors.primary) { contrastingAccent(colors.primary) }
    MaterialTheme(colors = colors.copy(primary = accent, secondary = contrastingAccent(colors.secondary)), content = content)
}
internal val SecondaryText = Color.White.copy(alpha = .65f)

internal object DesktopTextInputFocus {
    private val focusedFields = java.util.Collections.newSetFromMap(
        java.util.concurrent.ConcurrentHashMap<Any, Boolean>(),
    )

    val active: Boolean get() = focusedFields.isNotEmpty()

    fun update(token: Any, focused: Boolean) {
        if (focused) focusedFields += token else focusedFields -= token
    }
}

@Composable
internal fun Modifier.trackTextInputFocus(): Modifier {
    val token = remember { Any() }
    DisposableEffect(token) {
        onDispose { DesktopTextInputFocus.update(token, false) }
    }
    return onFocusChanged { DesktopTextInputFocus.update(token, it.isFocused) }
}

/**
 * Shared dialog surface for every popup in the desktop app. Compose Desktop
 * cannot sample the pixels of a separate native dialog window for a true
 * backdrop blur, so a dense translucent glass surface, rounded corners, and a
 * soft entrance transform provide the same visual hierarchy without blurring
 * the dialog's own content.
 */
@Composable
internal fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val entrance by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "popup-entrance",
    )
    MaterialAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        title = title,
        text = text,
        modifier = modifier
            .graphicsLayer {
                alpha = entrance
                scaleX = .94f + entrance * .06f
                scaleY = .94f + entrance * .06f
                translationY = (1f - entrance) * 18.dp.toPx()
            }
            .shadow(30.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        backgroundColor = Color(0xF21A1C20),
        contentColor = Color.White,
    )
}

private fun Modifier.dragHorizontally(state: androidx.compose.foundation.ScrollState): Modifier =
    pointerInput(state) {
        detectHorizontalDragGestures { change, dragAmount ->
            change.consume()
            // using dispatchRawDelta for ScrollState (Compose internal actually doesn't have dispatchRawDelta public on ScrollState on all platforms, let's check)
            // Wait, ScrollState dispatchRawDelta is valid on Desktop. 
            state.dispatchRawDelta(-dragAmount)
        }
    }

private fun Modifier.dragHorizontally(state: LazyListState): Modifier =
    pointerInput(state) {
        detectHorizontalDragGestures { change, dragAmount ->
            change.consume()
            state.dispatchRawDelta(-dragAmount)
        }
    }

private fun Modifier.dragVertically(state: LazyListState): Modifier =
    pointerInput(state) {
        detectVerticalDragGestures { change, dragAmount ->
            change.consume()
            state.dispatchRawDelta(-dragAmount)
        }
    }

private fun Modifier.swipeDownToDismiss(onDismiss: () -> Unit): Modifier =
    pointerInput(onDismiss) {
        var distance = 0f
        detectVerticalDragGestures(
            onDragEnd = {
                if (distance >= 120f) onDismiss()
                distance = 0f
            },
            onDragCancel = { distance = 0f },
            onVerticalDrag = { change, dragAmount ->
                if (dragAmount > 0f || distance > 0f) {
                    change.consume()
                    distance = (distance + dragAmount).coerceAtLeast(0f)
                }
            },
        )
    }

private fun Modifier.observeSwipeDownToDismiss(onDismiss: () -> Unit): Modifier =
    pointerInput(onDismiss) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val start = down.position
            var dismissed = false
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                val distance = pointer.position - start
                if (!dismissed && distance.y >= 120f && distance.y > kotlin.math.abs(distance.x) * 1.25f) {
                    dismissed = true
                    onDismiss()
                }
            } while (event.changes.any { it.pressed })
        }
    }

private fun Modifier.observePullToRefresh(
    state: LazyListState,
    enabled: Boolean,
    onRefresh: () -> Unit,
): Modifier = pointerInput(state, enabled) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val start = down.position
        // Presses on the page's scroll bar (right edge) are not pulls.
        if (start.x >= size.width - 12.dp.toPx()) return@awaitEachGesture
        var triggered = false
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
            val distance = pointer.position - start
            val atTop = state.firstVisibleItemIndex == 0 && state.firstVisibleItemScrollOffset == 0
            if (!triggered && enabled && atTop && distance.y >= 80f && distance.y > kotlin.math.abs(distance.x) * 1.25f) {
                triggered = true
                onRefresh()
            }
        } while (event.changes.any { it.pressed })
    }
}


private enum class AppSection(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Search("Search", Icons.Default.Search),
    Library("Library", Icons.Default.LibraryMusic),
}

private enum class ProfilePage {
    Home,
    Integrations,
    ListenTogether,
    Discord,
    LastFm,
    Settings,
    Login,
}

private enum class PlayerDestination { Player, Lyrics }
private data class CatalogDestination(
    val title: String,
    val kind: String,
    val browseId: String? = null,
    val searchQuery: String? = null,
    val params: String? = null,
    /** For an album opened from a song title: the song, whose own album link is used. */
    val sourceTrack: Track? = null,
)
private data class NavigationSnapshot(
    val section: AppSection,
    val profilePage: ProfilePage?,
    val playerDestination: PlayerDestination?,
    val catalogDestination: CatalogDestination?,
    val settingsSubpage: String? = null,
    /** The Library sub-page (Liked, a playlist...) so Back returns to it. */
    val libraryTarget: String? = null,
)

/**
 * A windowed-borderless fullscreen controller for Windows multi-monitor setups.
 *
 * Compose's [WindowPlacement.Fullscreen] maps to a native fullscreen mode that
 * Windows is allowed to minimize when another monitor receives focus. Keeping
 * the same AWT frame alive as a borderless window avoids that focus-loss
 * behaviour and also gives us one explicit source of truth for entering and
 * leaving fullscreen.
 */
private class DesktopFullscreenController(
    private val frame: java.awt.Frame,
) {
    private var active = false
    private var previousStyle: Int? = null
    private var previousPlacement: com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT? = null

    private val user32 get() = com.sun.jna.platform.win32.User32.INSTANCE

    fun setFullscreen(enabled: Boolean) = onAwtThread {
        if (enabled == active) return@onAwtThread
        if (!com.recordlabs.desktop.data.Platform.isWindows) {
            // Linux (X11/Wayland): the window manager owns fullscreen; ask it
            // through AWT instead of Win32 window styles.
            linuxFullscreen(enabled)
            return@onAwtThread
        }
        if (enabled) enter() else leave()
    }

    private var linuxPreviousBounds: java.awt.Rectangle? = null

    private fun linuxFullscreen(enabled: Boolean) {
        val device = frame.graphicsConfiguration.device
        runCatching {
            if (enabled) {
                linuxPreviousBounds = java.awt.Rectangle(frame.bounds)
                if (device.isFullScreenSupported) device.fullScreenWindow = frame
                else {
                    frame.extendedState = java.awt.Frame.NORMAL
                    frame.bounds = java.awt.Rectangle(frame.graphicsConfiguration.bounds)
                }
            } else {
                if (device.fullScreenWindow === frame) device.fullScreenWindow = null
                linuxPreviousBounds?.let { frame.bounds = it }
            }
        }.onFailure { it.printStackTrace() }
        active = enabled
    }

    private fun enter() {
        val hwnd = hwnd()
        previousStyle = user32.GetWindowLong(hwnd, GWL_STYLE)
        previousPlacement = com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT().also { placement ->
            placement.length = placement.size()
            check(user32.GetWindowPlacement(hwnd, placement).booleanValue())
        }
        // GraphicsConfiguration bounds are expressed in Java's DPI-aware
        // coordinate space. Feeding Win32's physical pixels back into AWT made
        // high-DPI secondary monitors shrink or shift the window.
        val monitorBounds = java.awt.Rectangle(frame.graphicsConfiguration.bounds)
        frame.extendedState = java.awt.Frame.NORMAL
        frame.bounds = monitorBounds
        val borderlessStyle = previousStyle!! and FULLSCREEN_DECORATIONS.inv()
        user32.SetWindowLong(hwnd, GWL_STYLE, borderlessStyle)
        check(
            user32.SetWindowPos(
                hwnd,
                null,
                0,
                0,
                0,
                0,
                SWP_NOMOVE or SWP_NOSIZE or SWP_NOZORDER or SWP_NOOWNERZORDER or SWP_FRAMECHANGED,
            ),
        )
        active = true
    }

    private fun leave() {
        val hwnd = hwnd()
        previousStyle?.let { user32.SetWindowLong(hwnd, GWL_STYLE, it) }
        previousPlacement?.let { user32.SetWindowPlacement(hwnd, it) }
        user32.SetWindowPos(
            hwnd,
            null,
            0,
            0,
            0,
            0,
            SWP_NOMOVE or SWP_NOSIZE or SWP_NOZORDER or SWP_NOOWNERZORDER or SWP_FRAMECHANGED,
        )
        active = false
    }

    private fun hwnd(): com.sun.jna.platform.win32.WinDef.HWND =
        com.sun.jna.platform.win32.WinDef.HWND(com.sun.jna.Native.getWindowPointer(frame))

    private inline fun onAwtThread(crossinline action: () -> Unit) {
        if (java.awt.EventQueue.isDispatchThread()) action()
        else java.awt.EventQueue.invokeAndWait { action() }
    }

    private companion object {
        const val GWL_STYLE = -16
        const val WS_CAPTION = 0x00C00000
        const val WS_THICKFRAME = 0x00040000
        const val WS_MINIMIZEBOX = 0x00020000
        const val WS_MAXIMIZEBOX = 0x00010000
        const val WS_SYSMENU = 0x00080000
        const val FULLSCREEN_DECORATIONS =
            WS_CAPTION or WS_THICKFRAME or WS_MINIMIZEBOX or WS_MAXIMIZEBOX or WS_SYSMENU
        const val SWP_NOSIZE = 0x0001
        const val SWP_NOMOVE = 0x0002
        const val SWP_NOZORDER = 0x0004
        const val SWP_NOOWNERZORDER = 0x0200
        const val SWP_FRAMECHANGED = 0x0020
    }
}

private val globalCatalogCache = mutableMapOf<CatalogDestination, CatalogPageData>()
// Artist pages whose full Top songs list is already in [globalCatalogCache].
private val expandedArtistPages = mutableSetOf<CatalogDestination>()

fun main(args: Array<String>) {
    // Packaging check: play a tone through the real audio path, no window.
    if ("--self-test-audio" in args) kotlin.system.exitProcess(runAudioSelfTest())
    // Load JavaFX media natives before the off-screen video decoder starts.
    ensureJavaFxMediaNatives()
    val shutdownAction = {
        runCatching { com.recordlabs.desktop.data.WindowsAudioProxy.stop() }
        runCatching { WindowsLyricsProviderRegistry.close() }
    }
    Runtime.getRuntime().addShutdownHook(Thread {
        // Windows shutdown / sign-out ends the app without a close request:
        // still write the latest playback position.
        runCatching { BackgroundPreferenceWriter.flushOnClose() }
        // The play in progress still counts in the listening stats.
        runCatching { com.recordlabs.desktop.data.WindowsListeningTracker.flushOnExit() }
        shutdownAction()
    })
    application {
        val savedWindow = remember { WindowsPreferences.windowSession() }
        val initialPlacement = when (savedWindow?.placement) {
            "Floating" -> WindowPlacement.Floating
            "Maximized" -> WindowPlacement.Maximized
            else -> WindowPlacement.Maximized
        }
        val windowState = rememberWindowState(
            placement = initialPlacement,
            width = (savedWindow?.width ?: 1180f).dp,
            height = (savedWindow?.height ?: 760f).dp,
        )
        var isFullscreen by remember { mutableStateOf(false) }
        val toggleFullscreen = { isFullscreen = !isFullscreen }
        LaunchedEffect(windowState, isFullscreen) {
            // Written on a background thread, and only when it changed.
            var savedSession: Triple<String, Float, Float>? = null
            while (true) {
                if (!isFullscreen) {
                    val session = Triple(
                        windowState.placement.name,
                        windowState.size.width.value,
                        windowState.size.height.value,
                    )
                    if (session != savedSession) {
                        savedSession = session
                        BackgroundPreferenceWriter.later("windowSession") {
                            WindowsPreferences.saveWindowSession(session.first, session.second, session.third)
                        }
                    }
                }
                delay(600L)
            }
        }
        val closeRecordLabs: () -> Unit = {
            if (!isFullscreen) {
                val placement = windowState.placement.name
                val width = windowState.size.width.value
                val height = windowState.size.height.value
                BackgroundPreferenceWriter.now("windowSession") {
                    WindowsPreferences.saveWindowSession(placement, width, height)
                }
            }
            BackgroundPreferenceWriter.flushOnClose()
            shutdownAction()
            // A downloaded update installs once Record Labs has closed.
            runCatching { com.recordlabs.desktop.data.AppUpdater.installOnExit() }
            exitApplication()
            kotlin.system.exitProcess(0)
        }
        androidx.compose.runtime.SideEffect { com.recordlabs.desktop.data.AppUpdater.exitApp = closeRecordLabs }
        LaunchedEffect(Unit) {
            // Looks for a new release shortly after start, then every 6 hours.
            withContext(Dispatchers.IO) { com.recordlabs.desktop.data.AppUpdater.cleanUp() }
            delay(8_000L)
            while (true) {
                com.recordlabs.desktop.data.AppUpdater.check(manual = false)
                delay(6L * 60L * 60L * 1000L)
            }
        }
        Window(
            onCloseRequest = closeRecordLabs,
            title = "Record Labs",
            icon = painterResource("recordlabs.ico"),
            state = windowState,
            // Keep native window decoration stable. Toggling this property at
            // runtime recreates a Compose Desktop window on Windows and was
            // the reason F11 could appear to close the application.
            undecorated = false,
            // Fullscreen should not become a permanent topmost window; that
            // can interfere with resize/focus transitions on multi-monitor
            // Windows setups.
            alwaysOnTop = false,
        ) {
            val fullscreenController = remember(window) { DesktopFullscreenController(window) }
            val currentToggleFullscreen by rememberUpdatedState(toggleFullscreen)
            val currentlyFullscreen by rememberUpdatedState(isFullscreen)

            LaunchedEffect(window) {
                // Dark title bar through DWM exists only on Windows.
                if (com.recordlabs.desktop.data.Platform.isWindows) try {
                    val hwnd = com.sun.jna.platform.win32.WinDef.HWND(com.sun.jna.Native.getWindowPointer(window))
                    val useDarkMode = com.sun.jna.ptr.IntByReference(1)
                    val result = RecordLabsDwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, 20, useDarkMode, 4)
                    if (result != 0) {
                        RecordLabsDwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, 19, useDarkMode, 4)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            LaunchedEffect(fullscreenController, isFullscreen) {
                fullscreenController.setFullscreen(isFullscreen)
            }
            DisposableEffect(window, fullscreenController) {
                val dispatcher = java.awt.KeyEventDispatcher { event ->
                    if (event.id != java.awt.event.KeyEvent.KEY_PRESSED) return@KeyEventDispatcher false
                    when {
                        event.keyCode == java.awt.event.KeyEvent.VK_F11 -> {
                            currentToggleFullscreen()
                            true
                        }
                        event.keyCode == java.awt.event.KeyEvent.VK_ESCAPE && currentlyFullscreen -> {
                            currentToggleFullscreen()
                            true
                        }
                        else -> false
                    }
                }
                val desktopStateListener = java.awt.event.WindowStateListener { event ->
                    val isAppVisible = event.newState and java.awt.Frame.ICONIFIED == 0
                    setAnimatedArtworkAppVisible(isAppVisible)
                    if (isAppVisible) {
                        FloatingLyricsState.isPipMinimized = true
                    } else if (FloatingLyricsState.isEnabled) {
                        FloatingLyricsState.isPipMinimized = false
                    }
                }
                val desktopFocusListener = object : java.awt.event.WindowFocusListener {
                    override fun windowGainedFocus(e: java.awt.event.WindowEvent?) {
                        // User switched to / focused on Record Labs -> hide floating lyrics
                        FloatingLyricsState.isPipMinimized = true
                    }
                    override fun windowLostFocus(e: java.awt.event.WindowEvent?) {
                        // User switched to another app (Chrome, game, Discord, etc.)
                        val opposite = e?.oppositeWindow
                        if (opposite != null && (opposite === window || window.ownedWindows.contains(opposite))) {
                            return
                        }
                        if (FloatingLyricsState.isEnabled) {
                            // PiP is still enabled -> show floating lyrics
                            FloatingLyricsState.isPipMinimized = false
                        }
                    }
                }
                FloatingLyricsState.requestMinimizeDesktop = {
                    windowState.isMinimized = true
                    runCatching {
                        window.extendedState = window.extendedState or java.awt.Frame.ICONIFIED
                    }
                }
                FloatingLyricsState.requestRestoreDesktop = {
                    windowState.isMinimized = false
                    runCatching {
                        window.extendedState = window.extendedState and java.awt.Frame.ICONIFIED.inv()
                        window.toFront()
                        window.requestFocus()
                    }
                }
                java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
                window.addWindowStateListener(desktopStateListener)
                window.addWindowFocusListener(desktopFocusListener)
                val initialAppVisible = window.extendedState and java.awt.Frame.ICONIFIED == 0
                setAnimatedArtworkAppVisible(initialAppVisible)
                if (initialAppVisible) {
                    FloatingLyricsState.isPipMinimized = true
                } else if (FloatingLyricsState.isEnabled) {
                    FloatingLyricsState.isPipMinimized = false
                }
                onDispose {
                    java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
                    window.removeWindowStateListener(desktopStateListener)
                    window.removeWindowFocusListener(desktopFocusListener)
                    setAnimatedArtworkAppVisible(false)
                    fullscreenController.setFullscreen(false)
                    FloatingLyricsState.requestMinimizeDesktop = null
                    FloatingLyricsState.requestRestoreDesktop = null
                }
            }
            Box(Modifier.fillMaxSize()) {
                RecordLabsWindow(
                    isFullscreen = isFullscreen,
                    onToggleFullscreen = toggleFullscreen,
                )
                // A music video opened full screen from the player/lyrics artwork.
                MusicVideoFullscreenOverlay(
                    currentTrackId = FloatingLyricsState.currentTrack?.id,
                    isWindowFullscreen = isFullscreen,
                    onWindowFullscreen = { isFullscreen = it },
                )
            }
        }
        if (FloatingLyricsState.isEnabled && FloatingLyricsState.currentTrack != null) {
            FloatingLyricsWindow()
        }
    }
}


@Composable
private fun RecordLabsWindow(
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
) {

    var section by remember { mutableStateOf(AppSection.Home) }
    var originSection by remember { mutableStateOf(AppSection.Home) }
    var isPlaying by remember { mutableStateOf(false) }
    val restoredPlayback = remember { WindowsPreferences.lastPlayback() }
    var currentTrack by remember { mutableStateOf<Track?>(restoredPlayback?.track) }
    var profilePage by remember { mutableStateOf<ProfilePage?>(null) }
    var playerDestination by remember { mutableStateOf<PlayerDestination?>(null) }
    var catalogDestination by remember { mutableStateOf<CatalogDestination?>(null) }
    var settingsSubpage by remember { mutableStateOf<String?>(null) }
    val navigationHistory = remember { androidx.compose.runtime.mutableStateListOf<NavigationSnapshot>() }
    val forwardNavigationHistory = remember { androidx.compose.runtime.mutableStateListOf<NavigationSnapshot>() }
    var lastPlayerDestination by remember { mutableStateOf(PlayerDestination.Player) }
    var dynamicAccent by remember { mutableStateOf(Accent) }
    val animatedAccent by animateColorAsState(dynamicAccent, tween(600))
    val flatStyle = com.recordlabs.desktop.data.AppSettings.flatTheme
    LaunchedEffect(dynamicAccent, flatStyle) { MusicVideoFullscreen.accent = if (flatStyle) FlatAccent else dynamicAccent }
    // Switching Style fades every accent (fixed and artwork-based) instead of jumping.
    val styleAccent by animateColorAsState(if (flatStyle) FlatAccent else AccentColor, tween(450))
    androidx.compose.runtime.SideEffect { ThemeTransition.accent = styleAccent }

    LaunchedEffect(currentTrack?.id, currentTrack?.artworkUrl) {
        val artUrl = currentTrack?.artworkUrl ?: currentTrack?.localPath?.let { path ->
            runCatching { WindowsLocalMusicLibrary.cachedEmbeddedArtwork(File(path)) }.getOrNull()
        }
        dynamicAccent = PlayerColorExtractor.extractAccentColor(artUrl)
    }

    // Settings > Appearance > Style: Flat uses one neutral colour instead of the artwork's accent.
    val themeAccent by animateColorAsState(if (flatStyle) FlatAccent else animatedAccent, tween(450))
    MaterialTheme(
        colors = darkColors(
            primary = themeAccent,
            secondary = themeAccent,
            background = Background,
            surface = SurfaceColor,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
        ),
        typography = androidx.compose.material.Typography(defaultFontFamily = FontFamily.Default),
        shapes = Shapes(
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(22.dp),
            large = RoundedCornerShape(28.dp),
        ),
    ) {
        val repository = remember { WindowsMusicRepository() }
        val mediaPlayer = remember { WindowsMediaPlayer() }
        DisposableEffect(repository, mediaPlayer) {
            onDispose {
                mediaPlayer.release()
                repository.close()
            }
        }
        val libraryStore = remember { WindowsLibraryStore() }
        var library by remember { mutableStateOf(libraryStore.snapshot()) }
        var accountSyncGeneration by remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()
        var homeFeed by remember { mutableStateOf<HomeFeed?>(null) }
        var homeLoading by remember { mutableStateOf(true) }
        var homeError by remember { mutableStateOf<String?>(null) }
        val homeLoadGeneration = remember { java.util.concurrent.atomic.AtomicInteger() }
        var playbackNotice by remember { mutableStateOf<String?>(null) }
        var playbackLoading by remember { mutableStateOf(false) }
        // Read through [PlaybackPositionScope] (or inside effects/callbacks) so a
        // new position (several times a second) does not recompose the whole window.
        val playbackPositionState = remember { mutableStateOf(restoredPlayback?.positionMs ?: 0L) }
        var playbackPositionMs by playbackPositionState
        var playbackDurationMs by remember { mutableStateOf(restoredPlayback?.track?.durationMs ?: 0L) }
        var playbackQueue by remember { mutableStateOf(restoredPlayback?.track?.let(::listOf).orEmpty()) }
        var playbackQueueIndex by remember { mutableStateOf(if (restoredPlayback == null) -1 else 0) }
        var manualQueueTailIndex by remember { mutableStateOf(if (restoredPlayback == null) -1 else 0) }
        var queuePrefetching by remember { mutableStateOf(false) }
        var fullPlayerQueueVisible by remember { mutableStateOf(false) }
        // Open by default; afterwards it opens as the user last left it.
        var globalQueueVisible by remember { mutableStateOf(WindowsPreferences.globalQueueOpen()) }
        LaunchedEffect(globalQueueVisible) {
            val open = globalQueueVisible
            // Saved off the UI thread; still written if the app closes right after.
            BackgroundPreferenceWriter.later("globalQueueOpen") { WindowsPreferences.setGlobalQueueOpen(open) }
        }

        // Volume (and, per Settings > Player, speed/pitch) come back as they were last time.
        val playerSettings = com.recordlabs.desktop.data.AppSettings
        var playbackVolume by remember { mutableStateOf(if (playerSettings.rememberVolume.value) playerSettings.savedVolume.value else 1f) }
        var volumeBeforeMute by remember { mutableStateOf(playbackVolume.takeIf { it > 0f } ?: 1f) }
        var playbackTempo by remember { mutableStateOf(if (playerSettings.rememberSpeedPitch.value) playerSettings.savedTempo.value else 1f) }
        var playbackPitch by remember { mutableStateOf(if (playerSettings.rememberSpeedPitch.value) playerSettings.savedPitch.value else 0) }
        remember { mediaPlayer.presetState(playbackVolume, playbackTempo, playbackPitch); 0 }
        // A muted player (volume 0) is not remembered as muted.
        LaunchedEffect(playbackVolume) { if (playbackVolume > 0f) playerSettings.savedVolume.value = playbackVolume }
        LaunchedEffect(playbackTempo, playbackPitch) {
            playerSettings.savedTempo.value = playbackTempo
            playerSettings.savedPitch.value = playbackPitch
        }
        var applyingConnectSync by remember { mutableStateOf(false) }
        var connectApplyJob by remember { mutableStateOf<Job?>(null) }
        val connectRemoteOwnerId by WindowsRecordLabsConnect.remoteOwnerId.collectAsState()
        val connectDevices by WindowsRecordLabsConnect.devices.collectAsState()
        val connectDiscovery by WindowsRecordLabsConnect.discoveryStatus.collectAsState()
        var showConnectDevices by remember { mutableStateOf(false) }
        var trackMenuTarget by remember { mutableStateOf<Track?>(null) }
        var trackDetailsTarget by remember { mutableStateOf<Track?>(null) }
        var playbackRequestId by remember { mutableStateOf(0L) }
        var shuffleEnabled by remember { mutableStateOf(playerSettings.rememberShuffleRepeat.value && playerSettings.savedShuffle.value) }
        var repeatMode by remember { mutableStateOf(if (playerSettings.rememberShuffleRepeat.value) playerSettings.savedRepeat.value else 0) }
        LaunchedEffect(shuffleEnabled, repeatMode) {
            playerSettings.savedShuffle.value = shuffleEnabled
            playerSettings.savedRepeat.value = repeatMode
        }
        var sleepTimerDeadlineMs by remember { mutableStateOf<Long?>(null) }
        val searchHistory = remember {
            androidx.compose.runtime.mutableStateListOf<String>().apply { addAll(SearchHistoryStore.load()) }
        }
        val searchQuery = remember { mutableStateOf("") }
        val searchFilter = remember { mutableStateOf("All") }
        val searchResults = remember { mutableStateOf<SearchResults?>(null) }
        // Keep the exact submitted text together with the generation. Reading
        // the live TextField state later could replay the previous result when
        // Enter was pressed quickly after editing the query.
        val searchSubmitGeneration = remember { mutableStateOf(0L to "") }
        val libraryTarget = remember { mutableStateOf<String?>(null) }
        var landscapeSearchExpanded by remember { mutableStateOf(false) }
        var searchBoxFocused by remember { mutableStateOf(false) }
        val playerHeaderVisible = remember { mutableStateOf(true) }
        val searchFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
        val searchSelectAllTrigger = remember { mutableStateOf(0) }
        var headerHovered by remember { mutableStateOf(false) }
        val hasSeenHeaderHint = remember { mutableStateOf(java.util.prefs.Preferences.userRoot().node("com.recordlabs.desktop").getBoolean("hasSeenHeaderHint", false)) }
        val homeListState = androidx.compose.foundation.lazy.rememberLazyListState()

        LaunchedEffect(playerDestination) {
            if (playerDestination != null) playerHeaderVisible.value = true
        }
        LaunchedEffect(playerDestination, playerHeaderVisible.value, landscapeSearchExpanded, headerHovered) {
            if (playerDestination != null && playerHeaderVisible.value && !landscapeSearchExpanded && !headerHovered) {
                kotlinx.coroutines.delay(3000)
                playerHeaderVisible.value = false
            }
        }
        var landscapeSearchWidthPx by remember { mutableStateOf(500) }
        var landscapeSearchLeftPx by remember { mutableStateOf(180) }
        var animatedArtworkEnabled by com.recordlabs.desktop.data.AppSettings.animatedArtwork
        val remotePlaybackSync by WindowsListenTogetherClient.playbackSync.collectAsState()
        val listenTogetherConnection by WindowsListenTogetherClient.connection.collectAsState()
        val listenTogetherIsHost by WindowsListenTogetherClient.isHost.collectAsState()
        val listenTogetherGuestCommand by WindowsListenTogetherClient.guestCommand.collectAsState()
        // Collected here rather than read in composition: each playback sample
        // (several a second) then only updates the state that changed.
        LaunchedEffect(mediaPlayer, currentTrack?.id) {
            mediaPlayer.state.collect { nativePlayback ->
                if (nativePlayback.trackId == currentTrack?.id) {
                    // Throttle position updates: only trigger recomposition when the
                    // position shifts by more than 100 ms. The progress bar and
                    // smooth-clock already interpolate sub-250ms movement themselves,
                    // so writing every poll value causes 4 unnecessary recompositions/sec.
                    // isPlaying, duration, and loading always update immediately.
                    val newPos = nativePlayback.positionMs
                    if (kotlin.math.abs(newPos - playbackPositionMs) > 100L || newPos == 0L) {
                        playbackPositionMs = newPos
                    }
                    val realDuration = nativePlayback.durationMs
                    if (realDuration > 0L && currentTrack?.durationMs != realDuration) {
                        currentTrack = currentTrack?.copy(durationMs = realDuration)
                    }
                    playbackDurationMs = realDuration.takeIf { it > 0L && (!playbackLoading || it != 180_000L) }
                        ?: currentTrack?.durationMs?.takeIf { !playbackLoading || it != 180_000L }
                        ?: 0L
                    isPlaying = nativePlayback.isPlaying
                    if (nativePlayback.positionMs > 0L) playbackLoading = false
                }
                nativePlayback.error?.let {
                    playbackNotice = it
                    playbackLoading = false
                }
            }
        }

        // Last playback: written on a background thread about once a second,
        // only when it changed, and once more when the app closes.
        LaunchedEffect(currentTrack?.id) {
            val trackId = currentTrack?.id ?: return@LaunchedEffect
            var savedTrack: Track? = null
            var savedPositionMs = -1L
            while (true) {
                // The latest copy of this track (its length may be corrected while it plays).
                val track = currentTrack?.takeIf { it.id == trackId } ?: savedTrack ?: return@LaunchedEffect
                val positionMs = playbackPositionMs
                if (positionMs != savedPositionMs || track != savedTrack) {
                    savedPositionMs = positionMs
                    savedTrack = track
                    BackgroundPreferenceWriter.later("lastPlayback") {
                        WindowsPreferences.saveLastPlayback(track, positionMs)
                    }
                }
                delay(1_000L)
            }
        }

        DisposableEffect(Unit) {
            fun latestPlaybackWrite(): (() -> Unit)? {
                val track = currentTrack ?: return null
                val positionMs = playbackPositionMs
                return { WindowsPreferences.saveLastPlayback(track, positionMs) }
            }
            BackgroundPreferenceWriter.writeOnClose("lastPlayback", ::latestPlaybackWrite)
            onDispose {
                BackgroundPreferenceWriter.writeOnClose("lastPlayback", null)
                latestPlaybackWrite()?.let { BackgroundPreferenceWriter.later("lastPlayback", it) }
            }
        }

        suspend fun performHomeLoad(params: String?, isRefresh: Boolean) {
            // A newer load (refresh, mood chip, "Try again") replaces this one.
            val generation = homeLoadGeneration.incrementAndGet()
            homeLoading = true
            // Like the artist page: a failed first answer is tried again (1 s,
            // 2 s, 4 s... up to 15 s apart). With nothing on screen it keeps
            // trying (the error and "Try again" show after 3 failures); a
            // refresh over a home that is already shown gives up after 3.
            var attempt = 0
            var firstResult: Result<HomeFeed>
            while (true) {
                firstResult = runCatching {
                    if (params != null) repository.home(params) else repository.home()
                }
                firstResult.exceptionOrNull()?.let { if (it is kotlinx.coroutines.CancellationException) throw it }
                if (firstResult.getOrNull()?.shelves?.isNotEmpty() == true) break
                if (firstResult.isSuccess && attempt >= 2) break
                attempt++
                val hasContent = homeFeed?.shelves?.isNotEmpty() == true
                if (hasContent && attempt >= 3) break
                if (!hasContent && attempt >= 3) {
                    homeError = firstResult.exceptionOrNull()?.message ?: "No home content"
                }
                delay(loadingRetryDelayMs(attempt))
                if (generation != homeLoadGeneration.get()) return
            }
            var currentFeed = firstResult.getOrNull()?.takeIf { it.shelves.isNotEmpty() || homeFeed == null }
            
            if (currentFeed != null) {
                if (!isRefresh) homeFeed = currentFeed
                
                val intlJob = kotlinx.coroutines.coroutineScope { async {
                    runCatching {
                        val searchResult = repository.search("Today's international hits", "Playlists")
                        val playlistId = searchResult.playlists.firstOrNull()?.browseId
                        if (playlistId != null) {
                            val catalog = repository.catalogPage(playlistId, "Playlist")
                            val tracks = catalog.tracks.take(50)
                            if (tracks.isNotEmpty()) {
                                val items = tracks.map { t ->
                                    com.recordlabs.desktop.data.HomeItem(
                                        title = t.title,
                                        subtitle = t.artist,
                                        artworkUrl = t.artworkUrl,
                                        track = t
                                    )
                                }
                                com.recordlabs.desktop.data.HomeShelf("Today's international hits", items, com.recordlabs.desktop.data.HomeShelfLayout.Cards)
                            } else null
                        } else null
                    }.getOrNull()
                } }
                
                var currentContinuation = currentFeed.continuation
                var loops = 0
                while (currentContinuation != null && loops < 10) {
                    loops++
                    // A page that fails is tried again a few times before the
                    // rest of the home is given up on.
                    var more: HomeFeed? = null
                    for (pageAttempt in 0 until 4) {
                        more = runCatching { repository.homeContinuation(currentContinuation!!) }
                            .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }
                            .getOrNull()
                        if (more != null) break
                        delay(loadingRetryDelayMs(pageAttempt + 1))
                    }
                    if (more != null && more.shelves.isNotEmpty()) {
                        currentFeed = currentFeed!!.copy(
                            shelves = currentFeed!!.shelves + more.shelves,
                            continuation = more.continuation
                        )
                        if (!isRefresh) homeFeed = currentFeed
                        currentContinuation = more.continuation
                    } else {
                        break
                    }
                }
                
                val supplemental = runCatching { repository.homeSupplement() }.getOrDefault(emptyList())
                val personalized = runCatching { repository.personalizedHome(library.history, library.liked) }.getOrDefault(emptyList())
                val intlShelf = intlJob.await()
                
                val extraShelves = mutableListOf<com.recordlabs.desktop.data.HomeShelf>()
                if (intlShelf != null) extraShelves.add(intlShelf)
                extraShelves.addAll(personalized)
                extraShelves.addAll(supplemental)
                
                if (extraShelves.isNotEmpty()) {
                    currentFeed = currentFeed!!.copy(
                        shelves = (extraShelves + currentFeed!!.shelves).distinctBy { it.title }
                    )
                    if (!isRefresh) homeFeed = currentFeed
                }

                if (isRefresh) {
                    if (params != null) {
                        currentFeed = currentFeed!!.copy(chips = homeFeed?.chips.orEmpty())
                    } else {
                        currentFeed = currentFeed!!.copy(chips = homeFeed?.chips.orEmpty().ifEmpty { currentFeed!!.chips })
                    }
                    homeFeed = currentFeed
                }
            }
            homeError = firstResult.exceptionOrNull()?.message
            homeLoading = false
            // Afterwards, shelves with a "More" page on YouTube Music get more
            // items. Only shelves still showing exactly what was expanded
            // (another load may have replaced them meanwhile) are updated.
            val loadedShelves = currentFeed?.shelves.orEmpty()
            if (loadedShelves.isNotEmpty()) {
                val expanded = runCatching { repository.expandHomeShelves(loadedShelves) }.getOrNull().orEmpty()
                    .zip(loadedShelves)
                    .filter { (grown, original) -> grown.items.size > original.items.size }
                    .associate { (grown, original) -> original.title to (original to grown) }
                if (expanded.isNotEmpty()) {
                    homeFeed = homeFeed?.let { feed ->
                        feed.copy(shelves = feed.shelves.map { shelf ->
                            val (original, grown) = expanded[shelf.title] ?: return@map shelf
                            if (shelf.items == original.items) shelf.copy(items = grown.items) else shelf
                        })
                    }
                }
            }
        }

        LaunchedEffect(repository) {
            val session = WindowsPreferences.googleSession()
            if (session != null) {
                val isValid = runCatching { repository.validateGoogleSession() }.getOrDefault(false)
                if (!isValid) {
                    val adopted = WindowsPreferences.adoptLatestSession()
                    val adoptedValid = adopted != null && runCatching { repository.validateGoogleSession() }.getOrDefault(false)
                    if (!adoptedValid) {
                        WindowsPreferences.clearGoogleSession()
                    }
                }
            }
            performHomeLoad(null, false)
        }
        LaunchedEffect(repository, accountSyncGeneration) {
            if (WindowsPreferences.googleSession() != null && WindowsPreferences.autoSync()) {
                runCatching {
                    if (!repository.validateGoogleSession()) {
                        val adopted = WindowsPreferences.adoptLatestSession()
                        if (adopted == null || !repository.validateGoogleSession()) {
                            WindowsPreferences.clearGoogleSession()
                            error("Google session expired")
                        }
                    }
                    repository.googleAccountProfile()
                    repository.googleLibrary()
                }.onSuccess { remote ->
                    libraryStore.mergeRemote(remote.liked, remote.playlists)
                    library = libraryStore.snapshot()
                    performHomeLoad(null, false)
                }
            }
        }
        var skipOnErrorRef: (() -> Unit)? = null
        fun beginPlayback(
            rawTrack: Track,
            notifyRoom: Boolean = true,
            startPlaying: Boolean = true,
            onPrepared: (() -> Unit)? = null,
        ) {
            val track = if (rawTrack.artworkUrl.isNullOrBlank() && !rawTrack.localPath.isNullOrBlank()) {
                val artUrl = runCatching {
                    WindowsLocalMusicLibrary.cachedEmbeddedArtwork(File(rawTrack.localPath))
                }.getOrNull()
                if (!artUrl.isNullOrBlank()) rawTrack.copy(artworkUrl = artUrl) else rawTrack
            } else {
                rawTrack
            }
            if (!applyingConnectSync) {
                WindowsRecordLabsConnect.takePlaybackOwnership()
                mediaPlayer.setVolume(playbackVolume)
            }
            val requestId = ++playbackRequestId
            mediaPlayer.stop()
            currentTrack = track
            isPlaying = startPlaying
            playbackLoading = true
            playbackPositionMs = 0L
            playbackDurationMs = track.durationMs.takeIf { !playbackLoading || it != 180_000L } ?: 0L
            playbackNotice = null
            if (notifyRoom) {
                WindowsListenTogetherClient.broadcastPlayback(
                    "change_track",
                    track,
                    0L,
                    queue = playbackQueue,
                    queueTitle = "Record Labs queue",
                )
            }
            scope.launch {
                var rawUrl: String? = null
                for (attempt in 1..4) {
                    rawUrl = runCatching { repository.playbackStreamUrl(track) }.getOrNull()
                    if (rawUrl != null) break
                    if (requestId != playbackRequestId) return@launch
                    delay(800L)
                }
                if (requestId != playbackRequestId) return@launch
                if (rawUrl != null) {
                    mediaPlayer.play(track, rawUrl)
                    suspend fun awaitNativePrepared(): Boolean {
                        val deadline = System.currentTimeMillis() + 10_000L
                        while (requestId == playbackRequestId &&
                            currentTrack?.id == track.id &&
                            mediaPlayer.lastError == null &&
                            !mediaPlayer.isReady &&
                            System.currentTimeMillis() < deadline
                        ) {
                            delay(50L)
                        }
                        val ready = requestId == playbackRequestId &&
                            currentTrack?.id == track.id &&
                            mediaPlayer.lastError == null &&
                            mediaPlayer.isReady
                        // A takeover requested while this song was loading can run now.
                        if (ready) WindowsRecordLabsConnect.notifyPlayerReady()
                        return ready
                    }
                    if (!startPlaying) {
                        // MetroServer waits until every guest has prepared the new
                        // source. Do not let a guest race ahead while the server is
                        // still collecting buffer_ready messages.
                        if (!awaitNativePrepared()) return@launch
                        mediaPlayer.pause()
                        isPlaying = false
                        playbackLoading = false
                        WindowsListenTogetherClient.sendBufferReady(track.id)
                        onPrepared?.invoke()
                    } else {
                        isPlaying = true
                        onPrepared?.let { callback ->
                            scope.launch {
                                if (awaitNativePrepared()) callback()
                            }
                        }
                    }
                    playbackNotice = null
                    if (notifyRoom && startPlaying) WindowsListenTogetherClient.broadcastPlayback("play", track, 0L)
                } else {
                    isPlaying = false
                    playbackLoading = false
                    playbackNotice = "Audio belum bisa disiapkan. Coba lagu lain."
                    // Settings > Player > Skip to the next song on errors.
                    if (com.recordlabs.desktop.data.AppSettings.autoSkipOnError.value && startPlaying) {
                        delay(1_500L)
                        if (requestId == playbackRequestId && currentTrack?.id == track.id) skipOnErrorRef?.invoke()
                    }
                }
            }
        }

        fun playTrack(track: Track, queue: List<Track> = listOf(track)) {
            // Only tracks without any YouTube Music type are matched to an official
            // audio version; a chosen song or music video is played exactly as picked.
            if (!track.playsAsIs) {
                playbackNotice = "Mencari audio resmi YouTube Music…"
                scope.launch {
                    val official = repository.resolveOfficialTrack(track)
                    if (official == null) {
                        playbackNotice = "Audio resmi YouTube Music tidak ditemukan."
                    } else {
                        val officialQueue = queue.map { if (it.id == track.id) official else it }
                        playTrack(official, officialQueue)
                    }
                }
                return
            }
            if (WindowsListenTogetherClient.connection.value == ListenTogetherConnection.InRoom &&
                !WindowsListenTogetherClient.isHost.value
            ) {
                WindowsListenTogetherClient.suggestTrack(track)
                playbackNotice = "Suggestion sent to the host"
                return
            }
            if (WindowsRecordLabsConnect.hasRemotePlaybackDevice() &&
                WindowsRecordLabsConnect.sendPlayTrack(track)
            ) {
                libraryStore.addToHistory(track)
                library = libraryStore.snapshot()
                playbackNotice = "Playing on the connected device"
                return
            }
            libraryStore.addToHistory(track)
            library = libraryStore.snapshot()
            playbackQueue = queue
                .distinctBy { it.id }
                .filterNot { it.id.startsWith("pending:") }
                .ifEmpty { listOf(track) }
            playbackQueueIndex = playbackQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            manualQueueTailIndex = playbackQueueIndex
            beginPlayback(track)
        }

        fun enqueueTrack(track: Track) {
            if (!track.playsAsIs) {
                scope.launch {
                    repository.resolveOfficialTrack(track)?.let(::enqueueTrack)
                        ?: run { playbackNotice = "Audio resmi YouTube Music tidak ditemukan." }
                }
                return
            }
            if (listenTogetherConnection == ListenTogetherConnection.InRoom && !listenTogetherIsHost) {
                WindowsListenTogetherClient.suggestTrack(track)
                playbackNotice = "Suggestion sent to the host"
                return
            }
            if (WindowsRecordLabsConnect.hasRemotePlaybackDevice() &&
                WindowsRecordLabsConnect.sendQueueNext(track)
            ) {
                playbackNotice = "Added to the connected device queue"
                return
            }
            if (currentTrack == null || playbackQueueIndex !in playbackQueue.indices) {
                playTrack(track)
                return
            }
            val activeId = currentTrack?.id
            val withoutDuplicate = playbackQueue.filterNot { it.id == track.id }
            val currentIndex = withoutDuplicate.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
            val pendingCount = (manualQueueTailIndex - playbackQueueIndex).coerceAtLeast(0)
            val insertion = (currentIndex + 1 + pendingCount).coerceIn(currentIndex + 1, withoutDuplicate.size)
            playbackQueue = withoutDuplicate.toMutableList().apply { add(insertion, track) }
            playbackQueueIndex = currentIndex
            manualQueueTailIndex = insertion
            currentTrack?.let {
                WindowsListenTogetherClient.broadcastPlayback("sync_queue", it, playbackPositionMs, queue = playbackQueue, queueTitle = "Record Labs queue")
            }
        }

        fun moveQueueItem(trackId: String, to: Int) {
            val from = playbackQueue.indexOfFirst { it.id == trackId }
            if (from !in playbackQueue.indices || to !in playbackQueue.indices || from == to) return
            val activeIndex = playbackQueue.indexOfFirst { it.id == currentTrack?.id }.coerceAtLeast(0)
            if (from == activeIndex) return
            val safeTarget = to.coerceIn((activeIndex + 1).coerceAtMost(playbackQueue.lastIndex), playbackQueue.lastIndex)
            if (from == safeTarget) return
            val activeId = currentTrack?.id
            if (WindowsRecordLabsConnect.sendQueueMove(trackId, safeTarget)) {
                // Keep the controller UI optimistic while the host applies the
                // command; its next state heartbeat confirms this exact order.
                playbackQueue = playbackQueue.toMutableList().apply { add(safeTarget, removeAt(from)) }
                playbackQueueIndex = playbackQueue.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
                return
            }
            playbackQueue = playbackQueue.toMutableList().apply { add(safeTarget, removeAt(from)) }
            playbackQueueIndex = playbackQueue.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
            manualQueueTailIndex = manualQueueTailIndex.coerceIn(playbackQueueIndex, playbackQueue.lastIndex)
            currentTrack?.let {
                WindowsListenTogetherClient.broadcastPlayback("sync_queue", it, playbackPositionMs, queue = playbackQueue, queueTitle = "Record Labs queue")
            }
        }

        fun removeQueueItem(trackId: String) {
            val index = playbackQueue.indexOfFirst { it.id == trackId }
            if (index !in playbackQueue.indices || playbackQueue[index].id == currentTrack?.id) return
            val activeId = currentTrack?.id
            playbackQueue = playbackQueue.toMutableList().apply { removeAt(index) }
            playbackQueueIndex = playbackQueue.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
            manualQueueTailIndex = manualQueueTailIndex.coerceIn(playbackQueueIndex, playbackQueue.lastIndex.coerceAtLeast(0))
            currentTrack?.let {
                WindowsListenTogetherClient.broadcastPlayback(
                    "sync_queue", it, playbackPositionMs,
                    queue = playbackQueue,
                    queueTitle = "Record Labs queue",
                )
    }
}

        fun setVolume(value: Float) {
            playbackVolume = value.coerceIn(0f, 1f)
            if (playbackVolume > 0f) volumeBeforeMute = playbackVolume
            // While another device plays the sound this PC's mirror stays silent;
            // the new level applies once the sound comes back here.
            if (!WindowsRecordLabsConnect.hasRemotePlaybackDevice()) mediaPlayer.setVolume(playbackVolume)
        }

        fun toggleMute() {
            if (playbackVolume > 0f) {
                volumeBeforeMute = playbackVolume
                setVolume(0f)
            } else setVolume(volumeBeforeMute.coerceAtLeast(.25f))
        }
        var openCatalogPageRef: ((String, String, String?, String?, String?) -> Unit)? = null
        fun playSearchTrack(track: Track) {
            // The global queue prefetcher fills InnerTube autoplay for every
            // one-off selection. Keeping that work in one place avoids two
            // simultaneous radio requests and the long empty-queue pause they
            // could cause at the end of the first track.
            playTrack(track)
        }
        fun playHomeItem(item: HomeItem, sourceItems: List<HomeItem>) {
            if (item.track == null && item.browseId != null) {
                openCatalogPageRef?.invoke(item.title, item.kind ?: "Album", item.browseId, null, null)
                return
            }
            item.track?.let { track ->
                playTrack(track, listOf(track))
            } ?: scope.launch {
                currentTrack = Track(
                    id = "pending:${item.title}",
                    title = item.title,
                    artist = item.subtitle.ifBlank { "YouTube Music" },
                    artworkUrl = item.artworkUrl,
                )
                isPlaying = false
                playbackNotice = "Mencari lagu dari ${item.title}…"
                val track = runCatching { repository.search(item.title).tracks.firstOrNull() }.getOrNull()
                if (track != null) playTrack(track) else playbackNotice = "Tidak ada lagu yang bisa diputar dari item ini."
            }
        }

        // Match Android's paged YouTubeQueue behavior: fill the radio queue as
        // soon as a track becomes visible in the full player/lyrics surface,
        // and refill well before the last item is reached.
        LaunchedEffect(currentTrack?.id, playbackQueueIndex, playbackQueue.size) {
            val seed = currentTrack ?: return@LaunchedEffect
            // Settings > Player > Load more automatically.
            if (!com.recordlabs.desktop.data.AppSettings.autoLoadMore.value) return@LaunchedEffect
            val remaining = playbackQueue.size - playbackQueueIndex - 1
            if (queuePrefetching || remaining >= 24) return@LaunchedEffect
            queuePrefetching = true
            try {
                val radio = runCatching { repository.nextRadioQueue(seed.id) }.getOrDefault(emptyList())
                if (currentTrack?.id == seed.id && radio.isNotEmpty()) {
                    val activeId = currentTrack?.id
                    playbackQueue = (playbackQueue + radio)
                        .filterNot { it.id.startsWith("pending:") }
                        .distinctBy(Track::id)
                    playbackQueueIndex = playbackQueue.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
                }
            } finally {
                queuePrefetching = false
            }
        }
        // Warm every expensive source for the next queue item only after the
        // current track is ready. Next then starts from disk/memory instead of
        // opening with a fresh audio, lyrics, artwork and motion-art request.
        // Small thumbnails no longer download the full-size cover, so fetch the
        // current song's full-size cover once (to the image cache) for the
        // player and lyrics pages, as the mini player's thumbnail used to.
        val coverPlatformContext = coil3.compose.LocalPlatformContext.current
        LaunchedEffect(currentTrack?.id, currentTrack?.artworkUrl) {
            val track = currentTrack ?: return@LaunchedEffect
            val cover = withContext(Dispatchers.IO) { WindowsLocalMusicLibrary.resolveArtworkModel(track) } as? String
                ?: return@LaunchedEffect
            runCatching {
                coil3.SingletonImageLoader.get(coverPlatformContext).enqueue(
                    coil3.request.ImageRequest.Builder(coverPlatformContext)
                        .data(cover)
                        .size(64)
                        .memoryCachePolicy(coil3.request.CachePolicy.DISABLED)
                        .build(),
                )
            }
        }
        LaunchedEffect(currentTrack?.id, playbackLoading, playbackQueueIndex, playbackQueue.size) {
            if (playbackLoading) return@LaunchedEffect
            val next = playbackQueue.getOrNull(playbackQueueIndex + 1) ?: return@LaunchedEffect
            coroutineScope {
                launch { runCatching { repository.streamUrl(next) } }
                launch { runCatching { WindowsLyricsProviderRegistry.getLyricsResult(next) } }
                launch { runCatching { WindowsAnimatedArtworkRepository.find(next) } }
                launch(Dispatchers.IO) { runCatching { WindowsOfflineArtworkCache.download(next) } }
            }
        }
        fun applyLocalPlaybackState(shouldPlay: Boolean) {
            val track = currentTrack ?: return
            if (!shouldPlay) {
                // A pending source lookup must not be allowed to start playback again after
                // the user has already pressed pause. This also clears stale informational
                // notices left by a Connect handoff; notices must never lock transport controls.
                playbackRequestId += 1L
                mediaPlayer.pause()
                isPlaying = false
                playbackLoading = false
                playbackNotice = null
                WindowsListenTogetherClient.broadcastPlayback("pause", track, playbackPositionMs)
                return
            }
            if (mediaPlayer.currentTrack?.id == track.id) {
                mediaPlayer.resume()
                isPlaying = true
                playbackLoading = false
                playbackNotice = null
                WindowsListenTogetherClient.broadcastPlayback("play", track, playbackPositionMs)
                return
            }
            if (playbackLoading) return
            val requestId = ++playbackRequestId
            val resumePosition = playbackPositionMs
            playbackLoading = true
            playbackNotice = null
            scope.launch {
                var rawUrl: String? = null
                for (attempt in 1..4) {
                    rawUrl = runCatching { repository.playbackStreamUrl(track) }.getOrNull()
                    if (rawUrl != null) break
                    if (requestId != playbackRequestId || currentTrack?.id != track.id) return@launch
                    delay(800L)
                }
                if (requestId != playbackRequestId || currentTrack?.id != track.id) return@launch
                if (rawUrl == null) {
                    playbackLoading = false
                    playbackNotice = "Audio belum bisa disiapkan. Coba lagu lain."
                } else {
                    mediaPlayer.play(track, rawUrl, resumePosition)
                    playbackLoading = false
                    isPlaying = true
                    WindowsListenTogetherClient.broadcastPlayback("play", track, resumePosition)
                    if (resumePosition > 0L) {
                        scope.launch {
                            delay(1_200L)
                            if (requestId == playbackRequestId && currentTrack?.id == track.id) {
                                mediaPlayer.seekTo(resumePosition)
                            }
                        }
                    }
                }
            }
        }
        fun togglePlaybackInternal(fromConnectCommand: Boolean) {
            // A command received from the remote controller is already at its output device.
            // Never feed it back through Connect even if a delayed owner heartbeat is still
            // visible for a moment during handoff.
            // An explicit play or pause (not "toggle"): the same request arriving
            // twice can then never cancel itself out on the other device.
            if (!fromConnectCommand &&
                WindowsRecordLabsConnect.sendCommand(if (isPlaying) "pause" else "play")
            ) return
            if (WindowsListenTogetherClient.connection.value == ListenTogetherConnection.InRoom &&
                !WindowsListenTogetherClient.isHost.value
            ) {
                WindowsListenTogetherClient.sendGuestCommand("TOGGLE_PLAY")
                playbackNotice = "Playback request sent to the host"
                return
            }
            applyLocalPlaybackState(!(mediaPlayer.isPlaying || isPlaying))
        }
        fun togglePlayback() = togglePlaybackInternal(fromConnectCommand = false)
        fun skipTrack(offset: Int, fromConnectCommand: Boolean = false) {
            if (!fromConnectCommand &&
                WindowsRecordLabsConnect.sendCommand(if (offset > 0) "next" else "previous")
            ) return
            if (WindowsListenTogetherClient.connection.value == ListenTogetherConnection.InRoom &&
                !WindowsListenTogetherClient.isHost.value
            ) {
                WindowsListenTogetherClient.sendGuestCommand(if (offset > 0) "SKIP_NEXT" else "SKIP_PREV")
                playbackNotice = "Skip request sent to the host"
                return
            }
            if (playbackQueue.isEmpty() || playbackQueueIndex !in playbackQueue.indices) return
            if (offset < 0 && playbackPositionMs > 3_000L) {
                mediaPlayer.seekTo(0L)
                return
            }
            var targetIndex = playbackQueueIndex + offset
            if (repeatMode == 1 && playbackQueue.isNotEmpty()) {
                targetIndex = when {
                    targetIndex > playbackQueue.lastIndex -> 0
                    targetIndex < 0 -> playbackQueue.lastIndex
                    else -> targetIndex
                }
            }
            if (targetIndex in playbackQueue.indices) {
                playbackQueueIndex = targetIndex
                beginPlayback(playbackQueue[targetIndex])
            } else if (offset > 0 && com.recordlabs.desktop.data.AppSettings.autoLoadMore.value) {
                currentTrack?.id?.let { currentId ->
                    scope.launch {
                        val nextRadio = runCatching { repository.nextRadioQueue(currentId) }.getOrDefault(emptyList())
                        if (nextRadio.isNotEmpty()) {
                            val newQueue = playbackQueue + nextRadio
                            playbackQueue = newQueue
                            playbackQueueIndex = (playbackQueueIndex + 1).coerceAtMost(newQueue.lastIndex)
                            beginPlayback(newQueue[playbackQueueIndex])
                        }
                    }
                }
            }
        }

        // Not for a silent Connect mirror: its loading problem must not skip the other device's song.
        skipOnErrorRef = { if (!WindowsRecordLabsConnect.hasRemotePlaybackDevice()) skipTrack(1) }
        // Record Labs Connect runs independently from Listen Together: it is
        // account-scoped, auto-discovers the user's other device on the LAN,
        // and mirrors playback silently on the controller device.
        LaunchedEffect(accountSyncGeneration, repository) {
            // Refresh the account header before starting discovery so Windows
            // and Android can match the same unique YouTube channel handle even
            // when one side does not expose an email or DATASYNC_ID.
            val profile = runCatching { repository.googleAccountProfile() }.getOrNull()
            val accountKeys = buildList {
                (profile?.email ?: WindowsPreferences.googleAccountEmail()).trim().takeIf(String::isNotBlank)
                    ?.let { add("email:$it") }
                (profile?.channelHandle ?: WindowsPreferences.googleAccountChannelHandle())
                    .trim().takeIf(String::isNotBlank)
                    ?.let { add("channel:$it") }
                WindowsPreferences.googleSession()?.dataSyncId?.trim()?.takeIf(String::isNotBlank)
                    ?.let { add("sync:$it") }
            }
            if (accountKeys.isEmpty()) {
                WindowsRecordLabsConnect.stop()
                return@LaunchedEffect
            }
            WindowsRecordLabsConnect.start(
                accountKeys = accountKeys,
                deviceName = com.recordlabs.desktop.data.Platform.deviceName,
                playbackProvider = {
                    ConnectPlaybackState(
                        ownerId = WindowsPreferences.connectDeviceId(),
                        track = currentTrack,
                        positionMs = mediaPlayer.positionMs,
                        durationMs = mediaPlayer.durationMs,
                        isPlaying = isPlaying,
                        sentAtMs = System.currentTimeMillis(),
                        queue = playbackQueue,
                        queueIndex = playbackQueueIndex,
                        // A requested takeover waits for this: the song is loaded here.
                        isReady = mediaPlayer.isReady && currentTrack != null &&
                            mediaPlayer.currentTrack?.id == currentTrack?.id,
                    )
                },
                onRemoteState = { remote ->
                    connectApplyJob?.cancel()
                    connectApplyJob = scope.launch {
                        applyingConnectSync = true
                        try {
                            if (remote.track == null && !WindowsRecordLabsConnect.hasRemotePlaybackDevice()) {
                                mediaPlayer.setVolume(playbackVolume)
                                // The device playing the sound vanished. This PC was only its
                                // silent mirror: pause rather than start playing on its own
                                // (when the other device comes back both would be audible).
                                if (isPlaying || mediaPlayer.isPlaying) {
                                    applyLocalPlaybackState(false)
                                    playbackNotice = "Lost the connection to the playing device"
                                }
                                return@launch
                            }
                            // Ownership claims arrive before the next full
                            // playback heartbeat. Mute immediately so two
                            // devices can never produce sound during handoff.
                            mediaPlayer.setVolume(0f)
                            val remoteTrack = remote.track ?: return@launch
                            // A controller mirrors the host silently, but stays
                            // deliberately behind it. Do not add wall-clock
                            // transport latency here: clocks can differ between
                            // Android and Windows, and after repeated handoffs
                            // that estimate could put the controller ahead.
                            val target = (remote.positionMs - if (remote.isPlaying) 750L else 0L).coerceAtLeast(0L)
                            val remoteQueue = remote.queue.distinctBy(Track::id)
                            if (remoteQueue.isNotEmpty()) {
                                playbackQueue = remoteQueue
                                playbackQueueIndex = remote.queueIndex.coerceIn(0, remoteQueue.lastIndex)
                                manualQueueTailIndex = playbackQueueIndex
                            }
                            if (currentTrack?.id != remoteTrack.id) {
                                beginPlayback(
                                    remoteTrack,
                                    notifyRoom = false,
                                    startPlaying = remote.isPlaying,
                                    onPrepared = {
                                        mediaPlayer.seekTo(target)
                                        WindowsRecordLabsConnect.sendTrackReady(remote.ownerId, remoteTrack.id)
                                    },
                                )
                                playbackPositionMs = target
                                if (remote.durationMs > 0L && currentTrack?.durationMs != remote.durationMs) {
                                    currentTrack = currentTrack?.copy(durationMs = remote.durationMs)
                                }
                                playbackDurationMs = remote.durationMs
                                return@launch
                            }
                            val drift = mediaPlayer.positionMs - target
                            // Correct any meaningful lead immediately. Falling
                            // behind is harmless until it exceeds the requested
                            // 500-1000 ms controller window. While the mirror is
                            // still loading, seeking or resuming would restart it.
                            val mirrorReady = mediaPlayer.isReady
                            if (mirrorReady && (drift > 50L || drift < -250L)) mediaPlayer.seekTo(target)
                            if (mirrorReady && remote.isPlaying && !mediaPlayer.isPlaying) mediaPlayer.resume()
                            if (mirrorReady && !remote.isPlaying && mediaPlayer.isPlaying) mediaPlayer.pause()
                            playbackPositionMs = target
                            if (remote.durationMs > 0L && currentTrack?.durationMs != remote.durationMs) {
                                currentTrack = currentTrack?.copy(durationMs = remote.durationMs)
                            }
                            playbackDurationMs = remote.durationMs
                            isPlaying = remote.isPlaying
                            if (mirrorReady) WindowsRecordLabsConnect.notifyPlayerReady()
                            WindowsRecordLabsConnect.sendTrackReady(remote.ownerId, remoteTrack.id)
                        } finally {
                            applyingConnectSync = false
                        }
                    }
                },
                onCommand = { command, position, commandTrack ->
                    scope.launch {
                        when (command) {
                            "take_ownership" -> {
                                applyingConnectSync = false
                                mediaPlayer.setVolume(playbackVolume)
                                playbackNotice = null
                            }
                            // These commands have already arrived at the selected output.
                            // Apply the requested state directly, independent of stale UI flags
                            // or ownership heartbeats, and never route them back through Connect.
                            "toggle" -> applyLocalPlaybackState(!(mediaPlayer.isPlaying || isPlaying))
                            "play" -> applyLocalPlaybackState(true)
                            "pause" -> applyLocalPlaybackState(false)
                            "next" -> skipTrack(1, fromConnectCommand = true)
                            "previous" -> skipTrack(-1, fromConnectCommand = true)
                            "seek" -> position?.let { mediaPlayer.seekTo(it) }
                            "play_track" -> commandTrack?.let { requestedTrack ->
                                applyingConnectSync = true
                                try {
                                    mediaPlayer.setVolume(playbackVolume)
                                    playbackQueue = listOf(requestedTrack)
                                    playbackQueueIndex = 0
                                    manualQueueTailIndex = 0
                                    beginPlayback(requestedTrack, notifyRoom = false)
                                } finally {
                                    applyingConnectSync = false
                                }
                            }
                            "queue_next" -> commandTrack?.let(::enqueueTrack)
                            "queue_move" -> {
                                val toIndex = position?.toInt()
                                if (commandTrack != null && toIndex != null) moveQueueItem(commandTrack.id, toIndex)
                            }
                        }
                    }
                },
                onLocalOwnership = {
                    // Another device handed the sound to this PC (it chose this PC, or
                    // this PC was picked as the output). Unmute the mirrored song and
                    // make sure it really plays; reload it if the mirror never got ready.
                    scope.launch {
                        connectApplyJob?.cancel()
                        applyingConnectSync = false
                        mediaPlayer.setVolume(playbackVolume)
                        playbackNotice = null
                        val track = currentTrack ?: return@launch
                        if (!mediaPlayer.isReady || mediaPlayer.currentTrack?.id != track.id) {
                            val resumeAt = playbackPositionMs
                            applyingConnectSync = true
                            try {
                                beginPlayback(track, startPlaying = isPlaying, onPrepared = {
                                    if (resumeAt > 0L) mediaPlayer.seekTo(resumeAt)
                                })
                            } finally {
                                applyingConnectSync = false
                            }
                        } else if (isPlaying && !mediaPlayer.isPlaying) {
                            mediaPlayer.resume()
                        }
                    }
                },
                onTransferStalled = { deviceName ->
                    scope.launch { playbackNotice = "Couldn't move playback to $deviceName" }
                },
            )
        }

        // Dedicated media keys are handled by NativePlayer's Win32 hook. Do
        // not also process VK_MEDIA_* through AWT: some laptop firmware emits
        // both routes, which would toggle play twice and appear to do nothing.
        val latestNativeMediaCommand by rememberUpdatedState(newValue = { command: String ->
            when (command) {
                "play_pause", "play", "pause" -> PlaybackKeyPulse.emit(PlaybackKeyAction.PlayPause)
                "next" -> PlaybackKeyPulse.emit(PlaybackKeyAction.Next)
                "previous" -> PlaybackKeyPulse.emit(PlaybackKeyAction.Previous)
            }
            when (command) {
                "play_pause" -> togglePlayback()
                "play" -> if (!isPlaying) togglePlayback()
                "pause" -> if (isPlaying) togglePlayback()
                "next" -> skipTrack(1)
                "previous" -> skipTrack(-1)
                "stop" -> {
                    mediaPlayer.pause()
                    mediaPlayer.seekTo(0L)
                    playbackPositionMs = 0L
                    isPlaying = false
                }
            }
        })
        DisposableEffect(mediaPlayer) {
            mediaPlayer.onMediaCommand = { command ->
                scope.launch { latestNativeMediaCommand(command) }
            }
            mediaPlayer.onBluetoothConnectPacket = WindowsRecordLabsConnect::receiveBluetooth
            WindowsRecordLabsConnect.configureBluetooth(mediaPlayer::sendBluetoothConnectPacket)
            onDispose {
                mediaPlayer.onMediaCommand = null
                mediaPlayer.onBluetoothConnectPacket = null
                WindowsRecordLabsConnect.configureBluetooth(null)
            }
        }

        // Library listening stats ("My Top 50", "Monthly Most", "Weekly Most"),
        // measured like the Android app: the real playing time of each play of
        // a track is saved as one listen when that play ends (next track,
        // repeat, app closed) and lasted at least 30 s.
        LaunchedEffect(libraryStore) { WindowsListeningTracker.attach(libraryStore) }
        LaunchedEffect(currentTrack?.id) {
            val track = currentTrack?.takeUnless { it.id.startsWith("pending:") }
            if (withContext(Dispatchers.IO) { WindowsListeningTracker.begin(track) }) library = libraryStore.snapshot()
            if (track == null) return@LaunchedEffect
            var lastTick = System.nanoTime()
            var lastPosition = playbackPositionMs
            while (true) {
                delay(1_000L)
                val now = System.nanoTime()
                // Real elapsed time; a long gap (sleep, debugger) is not counted.
                val elapsedMs = ((now - lastTick) / 1_000_000L).coerceIn(0L, 3_000L)
                lastTick = now
                if (currentTrack?.id != track.id) break
                if (isPlaying && !playbackLoading) WindowsListeningTracker.addPlayed(track.id, elapsedMs)
                val position = playbackPositionMs
                val duration = playbackDurationMs
                // Played to the end and started over (repeat one): a new play.
                if (duration > 10_000L && lastPosition >= duration - 5_000L && position < 5_000L) {
                    if (withContext(Dispatchers.IO) { WindowsListeningTracker.begin(currentTrack ?: track) }) library = libraryStore.snapshot()
                }
                lastPosition = position
            }
        }

        fun closeFullPlayer() {
            navigationHistory.removeLastOrNull()?.takeIf { it.playerDestination == null }?.let {
                section = it.section
                profilePage = it.profilePage
                playerDestination = it.playerDestination
                catalogDestination = it.catalogDestination
                settingsSubpage = it.settingsSubpage
                return
            }
            playerDestination = null
            section = originSection
            profilePage = null
        }
        fun currentNavigation() = NavigationSnapshot(section, profilePage, playerDestination, catalogDestination, settingsSubpage, libraryTarget.value)
        fun pushNavigation() {
            val snapshot = currentNavigation()
            if (navigationHistory.lastOrNull() != snapshot) navigationHistory += snapshot
            forwardNavigationHistory.clear()
        }
        fun restoreNavigation(snapshot: NavigationSnapshot) {
            section = snapshot.section
            profilePage = snapshot.profilePage
            playerDestination = snapshot.playerDestination
            catalogDestination = snapshot.catalogDestination
            settingsSubpage = snapshot.settingsSubpage
            libraryTarget.value = snapshot.libraryTarget
        }
        fun navigateBack(): Boolean {
            if (profilePage == ProfilePage.ListenTogether && ListenTogetherUi.settingsOpen && playerDestination == null) {
                ListenTogetherUi.settingsOpen = false
                return true
            }
            if (profilePage == ProfilePage.Settings && settingsSubpage != null && navigationHistory.isEmpty()) {
                settingsSubpage = null
                return true
            }
            navigationHistory.removeLastOrNull()?.let {
                forwardNavigationHistory += currentNavigation()
                restoreNavigation(it)
                return true
            }
            return when {
                playerDestination == PlayerDestination.Lyrics -> {
                    playerDestination = PlayerDestination.Player
                    lastPlayerDestination = PlayerDestination.Player
                    true
                }
                playerDestination != null -> { closeFullPlayer(); true }
                catalogDestination != null -> { catalogDestination = null; true }
                profilePage != null -> { profilePage = null; true }
                else -> false
            }
        }
        fun runAfterQueueClose(action: () -> Unit): Boolean {
            action()
            return true
        }
        fun navigateForward(): Boolean {
            forwardNavigationHistory.removeLastOrNull()?.let {
                val current = currentNavigation()
                if (navigationHistory.lastOrNull() != current) navigationHistory += current
                restoreNavigation(it)
                return true
            }
            return false
        }
        val performHomeAction: () -> Unit = {
            if (playerDestination != null) {
                // Home in the header over the music player / lyrics page closes
                // that page (back to where it was opened), never refreshes Home.
                closeFullPlayer()
            } else if (catalogDestination == null && profilePage == null && section == AppSection.Home) {
                if (homeListState.firstVisibleItemIndex == 0 && homeListState.firstVisibleItemScrollOffset == 0) {
                    scope.launch { performHomeLoad(null, true) }
                } else {
                    scope.launch { homeListState.animateScrollToItem(0) }
                }
            } else {
                pushNavigation(); catalogDestination = null; profilePage = null; playerDestination = null; section = AppSection.Home
            }
        }
        // Back to the page the user was on before entering the Library (Home
        // when there is none).
        fun leaveLibrary() {
            val index = navigationHistory.indexOfLast {
                it.playerDestination == null && (it.section != AppSection.Library || it.catalogDestination != null || it.profilePage != null)
            }
            if (index >= 0) {
                val snapshot = navigationHistory[index]
                forwardNavigationHistory += currentNavigation()
                while (navigationHistory.size > index) navigationHistory.removeAt(navigationHistory.lastIndex)
                restoreNavigation(snapshot)
            } else {
                pushNavigation(); catalogDestination = null; profilePage = null; playerDestination = null
                section = AppSection.Home
            }
        }
        // Opens a Library page (null = the Library hub) as a step Back can undo.
        // The button of the page already shown (Library, Liked, Downloaded,
        // Playlists) leaves the Library instead.
        fun openLibrary(target: String?) {
            if (section == AppSection.Library && catalogDestination == null && profilePage == null &&
                playerDestination == null && libraryTarget.value == target
            ) {
                leaveLibrary()
                return
            }
            pushNavigation(); catalogDestination = null; profilePage = null; playerDestination = null
            section = AppSection.Library
            libraryTarget.value = target
        }
        val performNavSelect: (AppSection) -> Unit = { it ->
            if (it == AppSection.Home) {
                performHomeAction()
            } else if (it == AppSection.Library && section == AppSection.Library && catalogDestination == null && profilePage == null) {
                // Library again while inside it: to the Library hub, or (already
                // there) back to the page before the Library.
                openLibrary(null)
            } else {
                pushNavigation(); catalogDestination = null; profilePage = null; playerDestination = null; section = it
            }
        }
        fun toggleShuffle() {
            shuffleEnabled = !shuffleEnabled
            if (shuffleEnabled && playbackQueueIndex in playbackQueue.indices) {
                val active = playbackQueue[playbackQueueIndex]
                playbackQueue = listOf(active) + playbackQueue.filterNot { it.id == active.id }.shuffled()
                playbackQueueIndex = 0
            }
        }
        fun startRadio(track: Track) {
            if (currentTrack?.id != track.id) playTrack(track)
            scope.launch {
                val radio = runCatching { repository.nextRadioQueue(track.id) }.getOrDefault(emptyList())
                if (currentTrack?.id == track.id && radio.isNotEmpty()) {
                    playbackQueue = (listOf(track) + radio).distinctBy { it.id }
                    playbackQueueIndex = 0
                }
            }
        }
        LaunchedEffect(remotePlaybackSync?.sequence) {
            val sync = remotePlaybackSync ?: return@LaunchedEffect
            val queueAction = sync.action in setOf("queue_add", "queue_remove", "queue_clear", "sync_queue")
            if (WindowsListenTogetherClient.isHost.value && !queueAction) return@LaunchedEffect
            val targetPosition = if (sync.action == "play" && sync.serverTimeMs > 0L) {
                sync.positionMs + (System.currentTimeMillis() - sync.serverTimeMs).coerceIn(0L, 5_000L)
            } else sync.positionMs
            if (sync.queue.isNotEmpty()) {
                playbackQueue = sync.queue.distinctBy(Track::id)
                playbackQueueIndex = playbackQueue.indexOfFirst { it.id == (sync.trackId ?: sync.track?.id) }.coerceAtLeast(0)
                manualQueueTailIndex = playbackQueue.lastIndex
            }
            when (sync.action) {
                "change_track" -> sync.track?.let { beginPlayback(it, notifyRoom = false, startPlaying = false) }
                "play" -> {
                    val expectedTrackId = sync.trackId ?: sync.track?.id ?: currentTrack?.id
                    sync.track?.takeIf { it.id != currentTrack?.id }?.let {
                        beginPlayback(it, notifyRoom = false, startPlaying = false)
                    }
                    // Stream resolution and Media Foundation source creation are
                    // asynchronous. Wait for the prepared track instead of seeking
                    // a previous session and causing the visible timeline jumps.
                    var prepareAttempts = 0
                    while ((mediaPlayer.currentTrack?.id != expectedTrackId || playbackLoading) && prepareAttempts < 40) {
                        delay(200L)
                        prepareAttempts++
                    }
                    if (mediaPlayer.currentTrack?.id != expectedTrackId) return@LaunchedEffect
                    val drift = kotlin.math.abs(mediaPlayer.positionMs - targetPosition)
                    if (targetPosition > 0L && drift > 3_000L) mediaPlayer.seekTo(targetPosition)
                    mediaPlayer.resume()
                    isPlaying = true
                }
                "pause" -> {
                    val drift = kotlin.math.abs(mediaPlayer.positionMs - targetPosition)
                    if (targetPosition > 0L && drift > 1_500L) mediaPlayer.seekTo(targetPosition)
                    mediaPlayer.pause()
                    isPlaying = false
                }
                "seek" -> if (kotlin.math.abs(mediaPlayer.positionMs - targetPosition) > 750L) mediaPlayer.seekTo(targetPosition)
                "queue_add" -> sync.track?.let { suggested ->
                    val withoutDuplicate = playbackQueue.filterNot { it.id == suggested.id }
                    val activeId = currentTrack?.id
                    val currentIndex = withoutDuplicate.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
                    val pendingCount = (manualQueueTailIndex - playbackQueueIndex).coerceAtLeast(0)
                    val insertion = (currentIndex + 1 + pendingCount).coerceIn(currentIndex + 1, withoutDuplicate.size)
                    playbackQueue = withoutDuplicate.toMutableList().apply { add(insertion, suggested) }
                    playbackQueueIndex = currentIndex
                    manualQueueTailIndex = insertion
                }
                "queue_remove" -> sync.trackId?.let { removedId ->
                    playbackQueue = playbackQueue.filterNot { it.id == removedId }
                    playbackQueueIndex = playbackQueue.indexOfFirst { it.id == currentTrack?.id }.coerceAtLeast(0)
                }
                "queue_clear" -> {
                    playbackQueue = currentTrack?.let(::listOf).orEmpty()
                    playbackQueueIndex = if (currentTrack == null) -1 else 0
                }
                "sync_queue" -> if (sync.queue.isNotEmpty()) {
                    playbackQueue = sync.queue.distinctBy(Track::id)
                    playbackQueueIndex = playbackQueue.indexOfFirst { it.id == currentTrack?.id }.coerceAtLeast(0)
                    manualQueueTailIndex = playbackQueue.lastIndex
                }
            }
        }
        LaunchedEffect(listenTogetherGuestCommand?.sequence) {
            val command = listenTogetherGuestCommand ?: return@LaunchedEffect
            if (!listenTogetherIsHost || !com.recordlabs.desktop.data.AppSettings.listenGuestControls.value) return@LaunchedEffect
            val normalizedCommand = command.action.uppercase()
            when (normalizedCommand) {
                "PLAY" -> if (!isPlaying) togglePlayback()
                "PAUSE" -> if (isPlaying) togglePlayback()
                "TOGGLE_PLAY" -> togglePlayback()
                "SKIP_NEXT" -> skipTrack(1)
                "SKIP_PREV" -> skipTrack(-1)
                else -> if (normalizedCommand.startsWith("SEEK:")) {
                    normalizedCommand.substringAfter(':').toLongOrNull()?.let { target ->
                        mediaPlayer.seekTo(target)
                        currentTrack?.let { WindowsListenTogetherClient.broadcastPlayback("seek", it, target) }
                    }
                }
            }
        }
        LaunchedEffect(listenTogetherConnection, listenTogetherIsHost) {
            if (listenTogetherConnection == ListenTogetherConnection.InRoom && listenTogetherIsHost) {
                currentTrack?.let { track ->
                    WindowsListenTogetherClient.broadcastPlayback(
                        "change_track",
                        track,
                        playbackPositionMs,
                        queue = playbackQueue,
                        queueTitle = "Record Labs queue",
                    )
                    if (isPlaying) {
                        delay(150L)
                        WindowsListenTogetherClient.broadcastPlayback("play", track, playbackPositionMs)
                    }
                }
            }
        }
        fun copyTrackLink(track: Track) {
            runCatching {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    StringSelection("https://music.youtube.com/watch?v=${track.id}"),
                    null,
                )
    }
}
        fun seekPlayback(position: Long) {
            if (WindowsRecordLabsConnect.sendCommand("seek", position)) return
            if (WindowsListenTogetherClient.connection.value == ListenTogetherConnection.InRoom &&
                !WindowsListenTogetherClient.isHost.value
            ) {
                WindowsListenTogetherClient.sendGuestCommand("SEEK:$position")
                playbackNotice = "Seek request sent to the host"
                return
            }
            mediaPlayer.seekTo(position)
            currentTrack?.let { WindowsListenTogetherClient.broadcastPlayback("seek", it, position) }
        }
        fun submitSearch(rawQuery: String) {
            val query = rawQuery.trim()
            if (query.isBlank()) return
            // Invalidate the previous result synchronously. SearchPage can be
            // recreated in the same frame when navigating from Home/catalog,
            // so keeping the old object here could make query A look loaded
            // for a newly submitted query B.
            searchResults.value = null
            searchFilter.value = "All"
            searchQuery.value = query
            landscapeSearchExpanded = false
            playerDestination = null
            profilePage = null
            catalogDestination = null
            section = AppSection.Search
            searchSubmitGeneration.value = (searchSubmitGeneration.value.first + 1L) to query
        }
        fun openCatalogSearch(query: String) {
            submitSearch(query)
        }
        fun openCatalogPage(rawTitle: String, kind: String, browseId: String? = null, searchQuery: String? = null, params: String? = null) {
            // An artist page is never named after play/view counts shown beside the name.
            val title = if (kind == "Artist") {
                rawTitle.split(" • ").filterNot { it.isListingStatsText() }.joinToString(" • ").trim().ifBlank { rawTitle }
            } else rawTitle
            if (title.isBlank()) return
            pushNavigation()
            playerDestination = null
            profilePage = null
            catalogDestination = CatalogDestination(title, kind, browseId, searchQuery, params)
        }
        openCatalogPageRef = ::openCatalogPage
        // A song's artist: opened through the song's own artist link, not a
        // name search (which can land on "Prince Tinashe" for "Tinashe").
        fun openTrackArtist(track: Track, name: String) {
            val artistName = name.split(" • ").filterNot { it.isListingStatsText() }.joinToString(" • ").trim().ifBlank { name }
            if (artistName.isBlank()) return
            pushNavigation()
            playerDestination = null
            profilePage = null
            catalogDestination = CatalogDestination(
                title = artistName,
                kind = "Artist",
                sourceTrack = track.takeIf { it.localPath == null && !it.id.startsWith("pending:") },
            )
        }
        fun openTrackAlbum(track: Track) {
            val albumName = track.album.takeIf { it.isNotBlank() } ?: track.title
            if (albumName.isBlank()) return
            pushNavigation()
            playerDestination = null
            profilePage = null
            catalogDestination = CatalogDestination(
                title = albumName,
                kind = "Album",
                searchQuery = albumName + " " + track.artist,
                sourceTrack = track.takeIf { it.localPath == null && !it.id.startsWith("pending:") },
            )
        }
        
        fun openListenTogether() {
            pushNavigation()
            playerDestination = null
            catalogDestination = null
            profilePage = ProfilePage.ListenTogether
            ListenTogetherUi.settingsOpen = false
        }
        DisposableEffect(Unit) {
            val mouseListener = java.awt.event.AWTEventListener { awtEvent ->
                val mouse = awtEvent as? java.awt.event.MouseEvent ?: return@AWTEventListener
                if (mouse.id == java.awt.event.MouseEvent.MOUSE_PRESSED && mouse.button == 4) {
                    if (MusicVideoFullscreen.trackId != null) {
                        // Back leaves the full-screen video, not the page under it.
                        MusicVideoFullscreen.close()
                    } else if (playerDestination == PlayerDestination.Lyrics) {
                        runAfterQueueClose {
                            playerDestination = null
                            section = originSection
                        }
                    } else runAfterQueueClose { navigateBack() }
                    mouse.consume()
                }
            }
            Toolkit.getDefaultToolkit().addAWTEventListener(mouseListener, java.awt.AWTEvent.MOUSE_EVENT_MASK)
            onDispose { Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener) }
        }
        val latestTogglePlayback by rememberUpdatedState(newValue = { togglePlayback() })
        val latestSkipTrack by rememberUpdatedState(newValue = { offset: Int -> skipTrack(offset) })
        val latestSeekPlayback by rememberUpdatedState(newValue = { pos: Long -> seekPlayback(pos) })
        LaunchedEffect(currentTrack, isPlaying, playbackDurationMs) {
            FloatingLyricsState.currentTrack = currentTrack
            FloatingLyricsState.isPlaying = isPlaying
            FloatingLyricsState.positionMs = playbackPositionMs
            FloatingLyricsState.durationMs = playbackDurationMs
            // The position follows on its own, without recomposing the window.
            androidx.compose.runtime.snapshotFlow { playbackPositionMs }.collect { FloatingLyricsState.positionMs = it }
        }
        // No screen saver or sleep while music or a video plays; Windows' own
        // timers apply again once playback stops.
        LaunchedEffect(isPlaying) { PlaybackKeepAwake.setPlaying(isPlaying) }
        LaunchedEffect(latestTogglePlayback, latestSkipTrack, latestSeekPlayback) {
            FloatingLyricsState.onPlayPause = { latestTogglePlayback() }
            FloatingLyricsState.onPrevious = { latestSkipTrack(-1) }
            FloatingLyricsState.onNext = { latestSkipTrack(1) }
            FloatingLyricsState.onSeek = { pos -> latestSeekPlayback(pos) }
        }
        val latestHasTrack by rememberUpdatedState(newValue = currentTrack != null)
        val latestPlayerDestination by rememberUpdatedState(playerDestination)
        DisposableEffect(Unit) {
            var spaceHeld = false
            val dispatcher = java.awt.KeyEventDispatcher { event ->
                when (event.keyCode) {
                    java.awt.event.KeyEvent.VK_SPACE -> {
                        if (event.id == java.awt.event.KeyEvent.KEY_RELEASED) {
                            spaceHeld = false
                            false
                        } else if (event.id == java.awt.event.KeyEvent.KEY_PRESSED && !spaceHeld &&
                            latestHasTrack && !DesktopTextInputFocus.active && !event.isConsumed
                        ) {
                            spaceHeld = true
                            PlaybackKeyPulse.emit(PlaybackKeyAction.PlayPause)
                            latestTogglePlayback()
                            event.consume()
                            true
                        } else false
                    }

                    java.awt.event.KeyEvent.VK_BACK_SLASH -> {
                        if (event.id == java.awt.event.KeyEvent.KEY_PRESSED && !DesktopTextInputFocus.active && !event.isConsumed) {
                            if (!hasSeenHeaderHint.value) {
                                hasSeenHeaderHint.value = true
                                java.util.prefs.Preferences.userRoot().node("com.recordlabs.desktop").putBoolean("hasSeenHeaderHint", true)
                            }
                            scope.launch {
                                playerHeaderVisible.value = true
                                landscapeSearchExpanded = true
                                searchSelectAllTrigger.value++
                                kotlinx.coroutines.delay(100)
                                runCatching { searchFocusRequester.requestFocus() }
                                kotlinx.coroutines.delay(50)
                                searchSelectAllTrigger.value++
                            }
                            event.consume()
                            true
                        } else false
                    }
                    else -> false
                }
            }
            val keyboardManager = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager()
            keyboardManager.addKeyEventDispatcher(dispatcher)
            onDispose { keyboardManager.removeKeyEventDispatcher(dispatcher) }
        }
        LaunchedEffect(mediaPlayer, repeatMode) {
            mediaPlayer.onPlaybackEnded = {
                scope.launch {
                    // A silent Connect mirror runs a little behind the device playing
                    // the sound. That device moves to the next song itself; acting on
                    // the mirror's own end sent another "next" (a skipped song) or,
                    // with repeat-one, took the sound back (both devices audible).
                    if (WindowsRecordLabsConnect.hasRemotePlaybackDevice()) return@launch
                    when {
                        repeatMode == 2 -> currentTrack?.let(::beginPlayback)
                        // Settings > Player > Autoplay off: stop when the song ends.
                        !com.recordlabs.desktop.data.AppSettings.autoplay.value -> isPlaying = false
                        else -> skipTrack(1)
                    }
                    Unit
                }
            }
        }
        LaunchedEffect(sleepTimerDeadlineMs) {
            val deadline = sleepTimerDeadlineMs ?: return@LaunchedEffect
            delay((deadline - System.currentTimeMillis()).coerceAtLeast(0L))
            mediaPlayer.pause()
            isPlaying = false
            sleepTimerDeadlineMs = null
        }
        // Discord Rich Presence (Settings > Integrations > Discord): the playback is
        // reported here; DiscordRichPresence decides when Discord needs an update.
        fun reportDiscordPlayback() = com.recordlabs.desktop.data.DiscordRichPresence.report(
            com.recordlabs.desktop.data.DiscordPlayback(
                currentTrack,
                isPlaying,
                playbackPositionMs,
                playbackDurationMs.takeIf { it > 0L } ?: currentTrack?.durationMs ?: 0L,
                playbackTempo,
            ),
        )
        LaunchedEffect(currentTrack?.id, isPlaying, playbackTempo) { reportDiscordPlayback() }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1_000L)
                reportDiscordPlayback()
                // Settings > Integrations > Last.fm (does nothing until signed in).
                com.recordlabs.desktop.data.LastFmScrobbler.report(
                    currentTrack,
                    isPlaying && !playbackLoading,
                    playbackPositionMs,
                    playbackDurationMs.takeIf { it > 0L } ?: currentTrack?.durationMs ?: 0L,
                )
            }
        }
        // Settings > Privacy / Storage actions that need the library, the search
        // list or the song playing (never removed from disk while it plays).
        LaunchedEffect(Unit) {
            com.recordlabs.desktop.data.AppDataEvents.requests.collect { request ->
                val playingIds = setOfNotNull(currentTrack?.id)
                when (request) {
                    com.recordlabs.desktop.data.AppDataRequest.ClearListenHistory -> {
                        withContext(Dispatchers.IO) { libraryStore.clearListenHistory() }
                        library = libraryStore.snapshot()
                    }
                    com.recordlabs.desktop.data.AppDataRequest.ClearSearchHistory -> {
                        searchHistory.clear()
                        withContext(Dispatchers.IO) { SearchHistoryStore.save(emptyList()) }
                    }
                    com.recordlabs.desktop.data.AppDataRequest.DeleteDownloads ->
                        withContext(Dispatchers.IO) { WindowsAudioCache.deleteAllDownloads(keep = playingIds) }
                    com.recordlabs.desktop.data.AppDataRequest.ClearSongCache ->
                        withContext(Dispatchers.IO) { WindowsAudioCache.trimCache(0L, keep = playingIds) }
                    com.recordlabs.desktop.data.AppDataRequest.ApplySongCacheLimit -> {
                        val limitMb = com.recordlabs.desktop.data.AppSettings.songCacheLimitMb.value
                        if (limitMb >= 0) withContext(Dispatchers.IO) { WindowsAudioCache.trimCache(limitMb * 1024L * 1024L, keep = playingIds) }
                    }
                    com.recordlabs.desktop.data.AppDataRequest.ReloadHome -> performHomeLoad(null, true)
                    is com.recordlabs.desktop.data.AppDataRequest.RestoreLibrary -> {
                        request.library?.let { restored ->
                            withContext(Dispatchers.IO) { libraryStore.replaceWith(restored) }
                            library = libraryStore.snapshot()
                        }
                        request.searchHistory?.let { restored ->
                            searchHistory.clear()
                            searchHistory.addAll(restored.takeLast(20))
                            withContext(Dispatchers.IO) { SearchHistoryStore.save(searchHistory.toList()) }
                        }
                    }
                }
                com.recordlabs.desktop.data.AppDataEvents.storageGeneration++
            }
        }
        // Keep the playback cache within its size limit (Settings > Storage) as songs change.
        LaunchedEffect(currentTrack?.id) {
            if (com.recordlabs.desktop.data.AppSettings.songCacheLimitMb.value >= 0) {
                com.recordlabs.desktop.data.AppDataEvents.request(com.recordlabs.desktop.data.AppDataRequest.ApplySongCacheLimit)
            }
        }
        val rootFocusManager = androidx.compose.ui.platform.LocalFocusManager.current
        Surface(
            Modifier
                .fillMaxSize()
                .background(Background)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Backslash && !DesktopTextInputFocus.active) {
                        if (!hasSeenHeaderHint.value) {
                            hasSeenHeaderHint.value = true
                            java.util.prefs.Preferences.userRoot().node("com.recordlabs.desktop").putBoolean("hasSeenHeaderHint", true)
                        }
                        scope.launch {
                            playerHeaderVisible.value = true
                            landscapeSearchExpanded = true
                            searchSelectAllTrigger.value++
                            kotlinx.coroutines.delay(100)
                            runCatching { searchFocusRequester.requestFocus() }
                            kotlinx.coroutines.delay(50)
                            searchSelectAllTrigger.value++
                        }
                        return@onPreviewKeyEvent true
                    }
                    val isUniversalBack = event.type == KeyEventType.KeyDown &&
                        (event.key == Key.Escape || (event.isAltPressed && event.key == Key.DirectionLeft))
                    if (!isUniversalBack) return@onPreviewKeyEvent false
                    if (event.key == Key.Escape && profilePage != null && playerDestination == null &&
                        MusicVideoFullscreen.trackId == null && DesktopTextInputFocus.active
                    ) {
                        // Esc while typing in a Profile / Settings field only leaves the field.
                        rootFocusManager.clearFocus()
                        return@onPreviewKeyEvent true
                    }
                    if (MusicVideoFullscreen.trackId != null) {
                        // Back leaves the full-screen video, not the page under it.
                        MusicVideoFullscreen.close()
                        return@onPreviewKeyEvent true
                    }
                    runAfterQueueClose { navigateBack() }
                },
            color = Background,
        ) {
            val latestLandscapeSearchExpanded by rememberUpdatedState(landscapeSearchExpanded)
            val latestSearchBoxFocused by rememberUpdatedState(searchBoxFocused)
            val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
            androidx.compose.runtime.CompositionLocalProvider(LocalOpenCatalog provides ::openCatalogPage, LocalOpenTrackAlbum provides ::openTrackAlbum, LocalOpenTrackArtist provides ::openTrackArtist) {
            BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type == PointerEventType.Press) {
                            val pos = event.changes.firstOrNull()?.position
                            if (pos != null) {
                                val x = pos.x
                                val y = pos.y
                                val inSearchBox = x >= landscapeSearchLeftPx && x <= landscapeSearchLeftPx + landscapeSearchWidthPx && y <= 72.dp.toPx()
                                
                                if (latestSearchBoxFocused && !inSearchBox) {
                                    focusManager.clearFocus()
                                }
                                
                                if (playerDestination != null && playerHeaderVisible.value && !latestLandscapeSearchExpanded) {
                                    if (y > 72.dp.toPx()) {
                                        playerHeaderVisible.value = false
                                    }
                                }
                            }
                        }
                    }
                }
            }) {
                val compact = maxWidth < 700.dp
                // While the search suggestions are open, the page under them is
                // recorded so the panel can show it blurred (frosted glass).
                val searchBackdropLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
                SearchBackdrop.layer = searchBackdropLayer
                SearchBackdrop.active = !compact && landscapeSearchExpanded
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize()
                        .onGloballyPositioned { SearchBackdrop.originInWindow = it.positionInWindow() }
                        .drawWithContent {
                            if (SearchBackdrop.active) {
                                searchBackdropLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(searchBackdropLayer)
                            } else drawContent()
                        },
                ) {

                    // BASE LAYER

                    if (catalogDestination != null) {
                    Box(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxSize()) {
                        if (!compact) { Spacer(Modifier.width(76.dp)) }
                        Box(Modifier.weight(1f).fillMaxHeight().progressiveHeaderBlur(compact = compact)) {
                            CatalogPage(
                                compact = compact,
                                destination = catalogDestination!!,
                                repository = repository,
                                onBack = { navigateBack() },
                                onTrack = ::playTrack,
                                onTrackMore = { trackMenuTarget = it },
                                onPlayNext = ::enqueueTrack,
                                onOpenCatalog = { title, kind, browseId, query, params ->
                                    pushNavigation()
                                    catalogDestination = CatalogDestination(title, kind, browseId, query, params)
                                },
                            )
                            Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = if (compact) 10.dp else 18.dp, vertical = 10.dp)) {
                                PlaybackPositionScope(playbackPositionState) { positionMs ->
                                    MiniPlayer(currentTrack, isPlaying, playbackLoading, positionMs, playbackDurationMs, playbackNotice, ::togglePlayback, { skipTrack(-1) }, { skipTrack(1) }, { showConnectDevices = true }, globalQueueVisible, { globalQueueVisible = !globalQueueVisible }) {
                                        pushNavigation()
                                        originSection = section
                                        playerDestination = lastPlayerDestination
                                    }
                                }
                            }
                        }
                        val queueWidth by androidx.compose.animation.core.animateDpAsState(
                            targetValue = if (globalQueueVisible) 368.dp else 0.dp,
                            animationSpec = androidx.compose.animation.core.tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                        )
                        if (queueWidth > 0.dp || globalQueueVisible) {
                            Box(Modifier.width(queueWidth).fillMaxHeight()) {
                                Box(Modifier.width(368.dp).fillMaxHeight()) {
                                    PlaybackQueueDialog(
                                        visible = globalQueueVisible,
                                        queue = playbackQueue,
                                        activeIndex = playbackQueueIndex,
                                        onSelect = { skipTrack(it - playbackQueueIndex) },
                                        onMove = ::moveQueueItem,
                                        onRemove = ::removeQueueItem,
                                        onClose = { globalQueueVisible = false },
                                        asPopup = false,
                                        fromRight = true, closeButtonOnLeft = true)
                                }
                            }
                        }
                    }
                                            // The page's header is the shared one drawn over every page (below),
                                            // so moving between pages and the player animates one header.
                        if (!compact) {
                            if (playerDestination == null) {
                                WideNavigation(selected = section, isProfileSelected = false, onProfile = {}, libraryTarget = libraryTarget.value, onLibraryTarget = ::openLibrary, onListenTogether = {}, onSelect = { if (it != AppSection.Library) performNavSelect(it) })
                            }
                        }
}
                } else if (profilePage != null) {
                    val profileContent: @Composable () -> Unit = {
                        ProfilePageContent(
                            profilePage!!,
                            compact,
                            repository,
                            onBack = { navigateBack() },
                            settingsSubpage = settingsSubpage,
                            onSettingsSubpage = {
                                // A settings sub page is a step back/forward can return to.
                                if (it != null && it != settingsSubpage) pushNavigation()
                                settingsSubpage = it
                            },
                            onGoogleSessionChanged = { accountSyncGeneration++ },
                        ) {
                            pushNavigation()
                            profilePage = it
                            // Settings always opens on its main list, not the sub page left last time.
                            if (it == ProfilePage.Settings) settingsSubpage = null
                            if (it == ProfilePage.ListenTogether) ListenTogetherUi.settingsOpen = false
                        }
                    }
                    if (compact) {
                        Scaffold(
                            backgroundColor = Background,
                            bottomBar = {
                            CompactNavigation(selected = null, isProfileSelected = true, homeLoading = homeLoading, onProfile = {}, onSelect = performNavSelect)
                            },
                        ) { padding ->
                            Box(Modifier.fillMaxSize().padding(padding)) {
                                profileContent()
                                Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 10.dp, vertical = 10.dp)) {
                                    PlaybackPositionScope(playbackPositionState) { positionMs ->
                                        MiniPlayer(currentTrack, isPlaying, playbackLoading, positionMs, playbackDurationMs, playbackNotice, ::togglePlayback, { skipTrack(-1) }, { skipTrack(1) }, { showConnectDevices = true }, globalQueueVisible, { globalQueueVisible = !globalQueueVisible }) {
                                            pushNavigation()
                                            originSection = section
                                            playerDestination = lastPlayerDestination
                                        }
                                    }
                                }
                            }
                                                if (!compact && (playerDestination == null || playerHeaderVisible.value)) LandscapeHeader(
                            searchFocusRequester = searchFocusRequester,
                                query = searchQuery.value,
                                selectAllTrigger = searchSelectAllTrigger.value,
                                canGoBack = settingsSubpage != null || navigationHistory.isNotEmpty(),
                                canGoForward = forwardNavigationHistory.isNotEmpty(),
                                onBack = { navigateBack() },
                                onForward = { navigateForward() },
                                onHome = performHomeAction,
homeLoading = homeLoading,
                                onQueryChange = { value -> searchQuery.value = value; landscapeSearchExpanded = true },
                                onSubmit = { submitSearch(searchQuery.value) },
                                onSearchFocusChange = { focused -> searchBoxFocused = focused; if (focused) landscapeSearchExpanded = true },
                                onSearchBounds = { left, width -> landscapeSearchLeftPx = left; landscapeSearchWidthPx = width },
                                onListenTogether = ::openListenTogether,
                                onFullscreen = onToggleFullscreen,
                                onProfile = { if (profilePage != ProfilePage.Home) { pushNavigation(); profilePage = ProfilePage.Home } },
                                startPadding = if (playerDestination == null) 76.dp else 0.dp, queueWidth = if (playerDestination != null && fullPlayerQueueVisible) 368.dp else 0.dp, endInset = if (playerDestination == null && globalQueueVisible) 368.dp else 0.dp,
                            )
    }
} else {
                        // The queue panel runs the full height (as on every other page);
                        // only the page itself leaves room for the header.
                        Row(Modifier.fillMaxSize()) {
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            // Room for the shared header drawn over every page.
                            Spacer(Modifier.height(72.dp))
                        Row(Modifier.weight(1f).fillMaxWidth()) {

                            WideNavigation(
                                selected = null,
                                isProfileSelected = profilePage != ProfilePage.ListenTogether,
                                onProfile = {},
                                listenTogetherSelected = profilePage == ProfilePage.ListenTogether,
                                onListenTogether = ::openListenTogether,
                                libraryTarget = libraryTarget.value,
                                onLibraryTarget = ::openLibrary, onSelect = { if (it != AppSection.Library) performNavSelect(it) })
                            Box(Modifier.weight(1f).fillMaxSize()) {
                                profileContent()
                                Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 18.dp, vertical = 10.dp)) {
                                    PlaybackPositionScope(playbackPositionState) { positionMs ->
                                        MiniPlayer(currentTrack, isPlaying, playbackLoading, positionMs, playbackDurationMs, playbackNotice, ::togglePlayback, { skipTrack(-1) }, { skipTrack(1) }, { showConnectDevices = true }, globalQueueVisible, { globalQueueVisible = !globalQueueVisible }) {
                                            pushNavigation()
                                            originSection = section
                                            playerDestination = lastPlayerDestination
                                        }
                                    }
                                }
                            }
                        }
                        }
                            val queueWidth by androidx.compose.animation.core.animateDpAsState(
                                targetValue = if (globalQueueVisible) 368.dp else 0.dp,
                                animationSpec = androidx.compose.animation.core.tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                            )
                            if (queueWidth > 0.dp || globalQueueVisible) {
                                Box(Modifier.width(queueWidth).fillMaxHeight()) {
                                    // Wrap in a fixed width box inside so the inner content doesn't get squeezed
                                    // while the outer container width animates.
                                    Box(Modifier.width(368.dp).fillMaxHeight()) {
                                        PlaybackQueueDialog(
                                            visible = globalQueueVisible,
                                            queue = playbackQueue,
                                            activeIndex = playbackQueueIndex,
                                            onSelect = { skipTrack(it - playbackQueueIndex) },
                                            onMove = ::moveQueueItem,
                                            onRemove = ::removeQueueItem,
                                            onClose = { globalQueueVisible = false },
                                            asPopup = false,
                                            fromRight = true, closeButtonOnLeft = true)
                                    }
                                }
                            }
                        }
                    }
                } else if (compact) {
                    Scaffold(
                        backgroundColor = Background,
                        bottomBar = {
                            CompactNavigation(selected = section, isProfileSelected = false, homeLoading = homeLoading, onProfile = { pushNavigation(); profilePage = ProfilePage.Home }, onSelect = performNavSelect)
                        },
                        contentWindowInsets = WindowInsets.navigationBars,
                    ) { padding ->
                        Row(Modifier.fillMaxSize().padding(padding)) {
                        Box(Modifier.weight(1f).fillMaxHeight().progressiveHeaderBlur(compact = false)) {
                            MainContent(section, compact = true, homeListState = homeListState, repository = repository, homeFeed = homeFeed, homeLoading = homeLoading, homeError = homeError, searchHistory = searchHistory, searchQuery = searchQuery, searchFilter = searchFilter, searchResults = searchResults, searchSubmitGeneration = searchSubmitGeneration, libraryTarget = libraryTarget, onLibraryNavigate = ::openLibrary, onLibraryBack = ::navigateBack, library = library, onCreatePlaylist = { name -> libraryStore.createPlaylist(name).also { if (it) library = libraryStore.snapshot() } }, onTrack = ::playTrack, onSearchTrack = ::playSearchTrack, onTrackMore = { trackMenuTarget = it }, onPlayNext = ::enqueueTrack, onOpenCatalog = ::openCatalogPage, onHomeItem = ::playHomeItem, onListenTogether = ::openListenTogether, onHomeRefresh = {
                                scope.launch {
                                    performHomeLoad(null, true)
                                }
                            }, onHomeChip = { chip ->
                                scope.launch {
                                    performHomeLoad(chip?.params, true)
                                }
                            })
                            Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 10.dp, vertical = 10.dp)) {
                                PlaybackPositionScope(playbackPositionState) { positionMs ->
                                    MiniPlayer(currentTrack, isPlaying, playbackLoading, positionMs, playbackDurationMs, playbackNotice, ::togglePlayback, { skipTrack(-1) }, { skipTrack(1) }, { showConnectDevices = true }, globalQueueVisible, { globalQueueVisible = !globalQueueVisible }) {
                                        pushNavigation()
                                        originSection = section
                                        playerDestination = lastPlayerDestination
                                    }
                                }
                            }
                        }
                            val queueWidth by androidx.compose.animation.core.animateDpAsState(
                                targetValue = if (globalQueueVisible) 368.dp else 0.dp,
                                animationSpec = androidx.compose.animation.core.tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                            )
                            if (queueWidth > 0.dp || globalQueueVisible) {
                                Box(Modifier.width(queueWidth).fillMaxHeight()) {
                                    // Wrap in a fixed width box inside so the inner content doesn't get squeezed
                                    // while the outer container width animates.
                                    Box(Modifier.width(368.dp).fillMaxHeight()) {
                                        PlaybackQueueDialog(
                                            visible = globalQueueVisible,
                                            queue = playbackQueue,
                                            activeIndex = playbackQueueIndex,
                                            onSelect = { skipTrack(it - playbackQueueIndex) },
                                            onMove = ::moveQueueItem,
                                            onRemove = ::removeQueueItem,
                                            onClose = { globalQueueVisible = false },
                                            asPopup = false,
                                            fromRight = true, closeButtonOnLeft = true)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize()) {
                        Row(Modifier.fillMaxSize()) {
                        Spacer(Modifier.width(76.dp))
                        Box(Modifier.weight(1f).fillMaxHeight().progressiveHeaderBlur(compact = false)) {
                            MainContent(section, compact = false, homeListState = homeListState, repository = repository, homeFeed = homeFeed, homeLoading = homeLoading, homeError = homeError, searchHistory = searchHistory, searchQuery = searchQuery, searchFilter = searchFilter, searchResults = searchResults, searchSubmitGeneration = searchSubmitGeneration, libraryTarget = libraryTarget, onLibraryNavigate = ::openLibrary, onLibraryBack = ::navigateBack, library = library, onCreatePlaylist = { name -> libraryStore.createPlaylist(name).also { if (it) library = libraryStore.snapshot() } }, onTrack = ::playTrack, onSearchTrack = ::playSearchTrack, onTrackMore = { trackMenuTarget = it }, onPlayNext = ::enqueueTrack, onOpenCatalog = ::openCatalogPage, onHomeItem = ::playHomeItem, onListenTogether = ::openListenTogether, onHomeRefresh = {
                                scope.launch {
                                    performHomeLoad(null, true)
                                }
                            }, onHomeChip = { chip ->
                                scope.launch {
                                    performHomeLoad(chip?.params, true)
                                }
                            })
                            Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 18.dp, vertical = 10.dp)) {
                                PlaybackPositionScope(playbackPositionState) { positionMs ->
                                    MiniPlayer(currentTrack, isPlaying, playbackLoading, positionMs, playbackDurationMs, playbackNotice, ::togglePlayback, { skipTrack(-1) }, { skipTrack(1) }, { showConnectDevices = true }, globalQueueVisible, { globalQueueVisible = !globalQueueVisible }) {
                                        pushNavigation()
                                        originSection = section
                                        playerDestination = lastPlayerDestination
                                    }
                                }
                            }
                        }
                            val queueWidth by androidx.compose.animation.core.animateDpAsState(
                                targetValue = if (globalQueueVisible) 368.dp else 0.dp,
                                animationSpec = androidx.compose.animation.core.tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                            )
                            if (queueWidth > 0.dp || globalQueueVisible) {
                                Box(Modifier.width(queueWidth).fillMaxHeight()) {
                                    // Wrap in a fixed width box inside so the inner content doesn't get squeezed
                                    // while the outer container width animates.
                                    Box(Modifier.width(368.dp).fillMaxHeight()) {
                                        PlaybackQueueDialog(
                                            visible = globalQueueVisible,
                                            queue = playbackQueue,
                                            activeIndex = playbackQueueIndex,
                                            onSelect = { skipTrack(it - playbackQueueIndex) },
                                            onMove = ::moveQueueItem,
                                            onRemove = ::removeQueueItem,
                                            onClose = { globalQueueVisible = false },
                                            asPopup = false,
                                            fromRight = true, closeButtonOnLeft = true)
                                    }
                                }
                            }
                        }
                    }
                }

                

                    

                    // PLAYER LAYER

                    androidx.compose.animation.AnimatedVisibility(

                        visible = playerDestination != null && currentTrack != null,

                        // Opens and closes as one sheet sliding up/down (like Apple
                        // Music): the page keeps its full size the whole way and only
                        // fades near the end of closing.
                        enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220)) +
                            androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(380, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it },

                        exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200, delayMillis = 160)) +
                            androidx.compose.animation.slideOutVertically(androidx.compose.animation.core.tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it },

                        modifier = Modifier.fillMaxSize()

                    ) {

                        // While the sheet slides away, it keeps showing the page that
                        // was open (the destination is already null by then); before,
                        // it cross-faded to nothing and shrank to a small card.
                        val closingPlayerDestination = remember { arrayOfNulls<PlayerDestination>(1) }
                        if (playerDestination != null) closingPlayerDestination[0] = playerDestination
                        androidx.compose.animation.AnimatedContent(

                            targetState = playerDestination ?: closingPlayerDestination[0],

                            transitionSpec = {

                                (androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(260)) togetherWith
                                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)))
                                    .using(androidx.compose.animation.SizeTransform(clip = false) { _, _ -> androidx.compose.animation.core.snap() })

                            },

                            label = "player-page-transition",

                        ) { destination ->
                            // Accent icons stay readable over the moving background.
                            PlayerPageAccentTheme {

                            
                if (destination != null && currentTrack != null) {
                    when (destination) {
                        PlayerDestination.Player -> PlayerPage(
                            track = currentTrack!!,
                            isPlaying = isPlaying,
                            isLoading = playbackLoading,
                            positionMs = playbackPositionMs,
                            durationMs = playbackDurationMs.takeIf { it > 0 } ?: currentTrack!!.durationMs,
                            animatedArtworkEnabled = animatedArtworkEnabled,
                            onBack = ::closeFullPlayer,
                            onPlayPause = ::togglePlayback,
                            onLyrics = {
                                playerDestination = PlayerDestination.Lyrics
                                lastPlayerDestination = PlayerDestination.Lyrics
                            },
                            isLiked = library.liked.any { it.id == currentTrack!!.id },
                            onToggleLiked = {
                                libraryStore.toggleLiked(currentTrack!!)
                                library = libraryStore.snapshot()
                            },
                            onSeek = ::seekPlayback,
                            onPrevious = { skipTrack(-1) },
                            onDevices = { showConnectDevices = true },
                            onNext = { skipTrack(1) },
                            queue = playbackQueue,
                            queueIndex = playbackQueueIndex,
                            onQueueTrack = { index ->
                                if (index in playbackQueue.indices && index != playbackQueueIndex) {
                                    playbackQueueIndex = index
                                    beginPlayback(playbackQueue[index])
                                }
                            },
                            onQueueMove = ::moveQueueItem,
                            onQueueRemove = ::removeQueueItem,
                            volume = playbackVolume,
                            onVolume = ::setVolume,
                            isListenTogetherGuest = listenTogetherConnection == ListenTogetherConnection.InRoom && !listenTogetherIsHost,
                            onToggleMute = ::toggleMute,
                            shuffleEnabled = shuffleEnabled,
                            onToggleShuffle = ::toggleShuffle,
                            repeatMode = repeatMode,
                            onCycleRepeat = { repeatMode = (repeatMode + 1) % 3 },
                            sleepTimerActive = sleepTimerDeadlineMs != null,
                            onToggleSleepTimer = {
                                sleepTimerDeadlineMs = if (sleepTimerDeadlineMs == null) System.currentTimeMillis() + 30 * 60_000L else null
                            },
                            onShare = { copyTrackLink(currentTrack!!) },
                            onStartRadio = { startRadio(currentTrack!!) },
                            playbackTempo = playbackTempo,
                            playbackPitch = playbackPitch,
                            onPlaybackTuning = { tempo, pitch ->
                                playbackTempo = tempo
                                playbackPitch = pitch
                                mediaPlayer.setAudioTuning(tempo, pitch)
                            },
                            playlists = library.playlists,
                            onAddToPlaylist = { playlist ->
                                libraryStore.addToPlaylist(playlist, currentTrack!!)
                                library = libraryStore.snapshot()
                            },
                            onViewArtist = { openTrackArtist(currentTrack!!, currentTrack!!.artist) },
                            onViewAlbum = { openCatalogPage(currentTrack!!.album, "Album") },
                            onDownload = {
                                scope.launch {
                                    runCatching { repository.downloadOffline(currentTrack!!) }
                                        .onFailure { playbackNotice = "Download gagal: ${it.message}" }
                                }
                            },
                            onListenTogether = {
                                pushNavigation(); playerDestination = null; profilePage = ProfilePage.ListenTogether; ListenTogetherUi.settingsOpen = false
                            },
                            isQueueVisible = fullPlayerQueueVisible,
                            onQueueVisibilityChanged = { fullPlayerQueueVisible = it },
                            isFullscreen = isFullscreen,
                            onToggleFullscreen = onToggleFullscreen,
                            onToggleAnimatedArtwork = {
                                val newState = !animatedArtworkEnabled
                                animatedArtworkEnabled = newState
                                WindowsPreferences.setAnimatedArtworkEnabled(newState)
                                if (newState) {
                                    WindowsAnimatedArtworkRepository.clearAllMisses()
                                    currentTrack?.let { WindowsAnimatedArtworkRepository.invalidate(it) }
                                }
                            },
                        )
                        PlayerDestination.Lyrics -> LyricsPage(
                            track = currentTrack!!,
                            repository = repository,
                            isPlaying = isPlaying,
                            isLoading = playbackLoading,
                            positionMs = playbackPositionMs,
                            durationMs = playbackDurationMs.takeIf { it > 0 } ?: currentTrack!!.durationMs,
                            animatedArtworkEnabled = animatedArtworkEnabled,
                            onToggleAnimatedArtwork = {
                                val newState = !animatedArtworkEnabled
                                animatedArtworkEnabled = newState
                                WindowsPreferences.setAnimatedArtworkEnabled(newState)
                                if (newState) {
                                    WindowsAnimatedArtworkRepository.clearAllMisses()
                                    currentTrack?.let { WindowsAnimatedArtworkRepository.invalidate(it) }
                                }
                            },
                            onBack = {
                                playerDestination = PlayerDestination.Player
                                lastPlayerDestination = PlayerDestination.Player
                            },
                            onClose = ::closeFullPlayer,
                            onPlayPause = ::togglePlayback,
                            onSeek = ::seekPlayback,
                            onPrevious = { skipTrack(-1) },
                            onDevices = { showConnectDevices = true },
                            onNext = { skipTrack(1) },
                            isLiked = library.liked.any { it.id == currentTrack!!.id },
                            onToggleLiked = {
                                libraryStore.toggleLiked(currentTrack!!)
                                library = libraryStore.snapshot()
                            },
                            onShare = { copyTrackLink(currentTrack!!) },
                            queue = playbackQueue,
                            queueIndex = playbackQueueIndex,
                            onQueueTrack = { index ->
                                if (index in playbackQueue.indices && index != playbackQueueIndex) {
                                    playbackQueueIndex = index
                                    beginPlayback(playbackQueue[index])
                                }
                            },
                            onQueueMove = ::moveQueueItem,
                            onQueueRemove = ::removeQueueItem,
                            volume = playbackVolume,
                            onVolume = ::setVolume,
                            isListenTogetherGuest = listenTogetherConnection == ListenTogetherConnection.InRoom && !listenTogetherIsHost,
                            onToggleMute = ::toggleMute,
                            shuffleEnabled = shuffleEnabled,
                            onToggleShuffle = ::toggleShuffle,
                            repeatMode = repeatMode,
                            onCycleRepeat = { repeatMode = (repeatMode + 1) % 3 },
                            sleepTimerActive = sleepTimerDeadlineMs != null,
                            onToggleSleepTimer = {
                                sleepTimerDeadlineMs = if (sleepTimerDeadlineMs == null) System.currentTimeMillis() + 30 * 60_000L else null
                            },
                            onStartRadio = { startRadio(currentTrack!!) },
                            playbackTempo = playbackTempo,
                            playbackPitch = playbackPitch,
                            onPlaybackTuning = { tempo, pitch ->
                                playbackTempo = tempo
                                playbackPitch = pitch
                                mediaPlayer.setAudioTuning(tempo, pitch)
                            },
                            isFullscreen = isFullscreen,
                            onToggleFullscreen = onToggleFullscreen,
                            playlists = library.playlists,
                            onAddToPlaylist = { playlist ->
                                libraryStore.addToPlaylist(playlist, currentTrack!!)
                                library = libraryStore.snapshot()
                            },
                            onViewArtist = { openTrackArtist(currentTrack!!, currentTrack!!.artist) },
                            onViewAlbum = { openCatalogPage(currentTrack!!.album, "Album") },
                            onDownload = {
                                scope.launch {
                                    runCatching { repository.downloadOffline(currentTrack!!) }
                                        .onFailure { playbackNotice = "Download gagal: ${it.message}" }
                                }
                            },
                            onListenTogether = {
                                pushNavigation(); playerDestination = null; profilePage = ProfilePage.ListenTogether; ListenTogetherUi.settingsOpen = false
                            },
                            isQueueVisible = fullPlayerQueueVisible,
                            onQueueVisibilityChanged = { fullPlayerQueueVisible = it },
                        )
    }
}

                                                    }
}

                    }

                }
                        // The one header for every page (home, library, artist, album,
                        // profile) and the player, so its buttons, search box and
                        // paddings animate smoothly between all of them.
                        val playerHeaderShown = !compact && playerDestination != null && playerHeaderVisible.value
                        androidx.compose.runtime.SideEffect { PlayerChrome.headerShown = playerHeaderShown }
                        if (!compact) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = playerDestination == null || playerHeaderVisible.value,
                                enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(300)) { -it },
                                exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) + androidx.compose.animation.slideOutVertically(androidx.compose.animation.core.tween(300)) { -it },
                                modifier = Modifier.pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            if (event.type == PointerEventType.Enter) headerHovered = true
                                            if (event.type == PointerEventType.Exit) headerHovered = false
                                        }
                                    }
                                }
                            ) {
                                LandscapeHeader(
                                    searchFocusRequester = searchFocusRequester,
                                    query = searchQuery.value,
                                    selectAllTrigger = searchSelectAllTrigger.value,
                                    canGoBack = settingsSubpage != null || navigationHistory.isNotEmpty(),
                                    canGoForward = forwardNavigationHistory.isNotEmpty(),
                                    onBack = { navigateBack() },
                                    onForward = { navigateForward() },
                                    onHome = performHomeAction,
homeLoading = homeLoading,
                                    onQueryChange = { value -> searchQuery.value = value; landscapeSearchExpanded = true },
                                    onSubmit = { submitSearch(searchQuery.value) },
                                    onSearchFocusChange = { focused -> searchBoxFocused = focused; if (focused) landscapeSearchExpanded = true },
                                    onSearchBounds = { left, width -> landscapeSearchLeftPx = left; landscapeSearchWidthPx = width },
                                    onListenTogether = ::openListenTogether,
                                    onFullscreen = onToggleFullscreen,
                                    onProfile = {
                                        if (profilePage != ProfilePage.Home || catalogDestination != null || playerDestination != null) {
                                            pushNavigation(); catalogDestination = null; playerDestination = null; profilePage = ProfilePage.Home
                                        }
                                    },
                                    startPadding = if (playerDestination == null) 76.dp else 0.dp, queueWidth = if (playerDestination != null && fullPlayerQueueVisible) 368.dp else 0.dp, endInset = if (playerDestination == null && globalQueueVisible) 368.dp else 0.dp,
                                    showLyricsTranslation = playerDestination != null,
                                )
                            }
                            if (playerDestination == null) {
                                WideNavigation(selected = section, isProfileSelected = false, onProfile = {}, libraryTarget = libraryTarget.value, onLibraryTarget = ::openLibrary, onListenTogether = {}, onSelect = { if (it != AppSection.Library) performNavSelect(it) })
                            }
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = playerDestination != null && !hasSeenHeaderHint.value && !playerHeaderVisible.value,
                            enter = androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.fadeOut(),
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 36.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Black.copy(alpha = 0.6f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Hint: Tekan '\\' untuk menampilkan tombol header", color = Color.White, fontSize = 13.sp)
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { 
                                            hasSeenHeaderHint.value = true
                                            java.util.prefs.Preferences.userRoot().node("com.recordlabs.desktop").putBoolean("hasSeenHeaderHint", true)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Tutup hint",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
    }
}
                            }
                        }

                if (!compact && landscapeSearchExpanded) {
                    LandscapeSearchOverlay(
                        repository = repository,
                        query = searchQuery.value,
                        history = searchHistory,
                        widthPx = landscapeSearchWidthPx,
                        leftPx = landscapeSearchLeftPx,
                        onSearch = { suggestion ->
                            submitSearch(suggestion)
                        },
                        onTrack = { track -> landscapeSearchExpanded = false; playSearchTrack(track) },
                        onTrackMore = { trackMenuTarget = it },
                        onPlayNext = ::enqueueTrack,
                        onCatalog = { item, kind -> landscapeSearchExpanded = false; openCatalogPage(item.title, kind, item.browseId) },
                        onRemoveHistory = { searchHistory.remove(it); com.recordlabs.desktop.data.SearchHistoryStore.save(searchHistory) },
                        onDismiss = { landscapeSearchExpanded = false },
                    )
                }
                if (showConnectDevices) {
                    ConnectDevicesPage(
                        devices = connectDevices,
                        discovery = connectDiscovery,
                        localDeviceId = WindowsRecordLabsConnect.localDeviceId,
                        remoteOwnerId = connectRemoteOwnerId,
                        onSelect = { deviceId ->
                            if (WindowsRecordLabsConnect.selectPlaybackDevice(deviceId)) {
                                // Sound is switched only when the handoff really happens
                                // (this PC unmutes when it becomes the output, and mutes when
                                // the other device takes over), so the music neither stops
                                // early nor plays twice.
                                if (deviceId == WindowsRecordLabsConnect.localDeviceId) {
                                    playbackNotice = if (WindowsRecordLabsConnect.hasRemotePlaybackDevice()) "Moving playback to this PC…" else "Playing on this PC"
                                } else {
                                    playbackNotice = "Moving playback to selected device…"
                                }
                                showConnectDevices = false
                            }
                        },
                        onDismiss = { showConnectDevices = false },
                    )
                }
                trackMenuTarget?.let { menuTrack ->
                    SearchTrackMenu(
                        track = menuTrack,
                        isGuest = listenTogetherConnection == ListenTogetherConnection.InRoom && !listenTogetherIsHost,
                        onDismiss = { trackMenuTarget = null },
                        onPlayNext = { enqueueTrack(menuTrack); trackMenuTarget = null },
                        playlists = library.playlists,
                        onAddToPlaylist = { playlistName ->
                            libraryStore.addToPlaylist(playlistName, menuTrack); library = libraryStore.snapshot()
                            trackMenuTarget = null
                        },
                        onStartRadio = { startRadio(menuTrack); trackMenuTarget = null },
                        onShare = { copyTrackLink(menuTrack); trackMenuTarget = null },
                        onAddToLibrary = {
                            libraryStore.toggleLiked(menuTrack); library = libraryStore.snapshot(); trackMenuTarget = null
                        },
                        onPin = { libraryStore.togglePinned(menuTrack); library = libraryStore.snapshot(); trackMenuTarget = null },
                        onDownload = {
                            scope.launch {
                                runCatching { repository.downloadOffline(menuTrack) }
                                    .onFailure { playbackNotice = "Download gagal: ${it.message}" }
                            }
                            trackMenuTarget = null
                        },
                        onViewArtist = { trackMenuTarget = null; openTrackArtist(menuTrack, menuTrack.artist) },
                        onDetails = { trackMenuTarget = null; trackDetailsTarget = menuTrack },
                    )
                }
                                trackDetailsTarget?.let { detailsTrack -> TrackDetailsDialog(detailsTrack) { trackDetailsTarget = null } }
        }
            } // closes CompositionLocalProvider
        } // closes BoxWithConstraints
    } // closes Surface
} // closes App

@Composable
private fun ConnectDevicesPage(
    devices: List<ConnectDevice>,
    discovery: WindowsConnectDiscoveryStatus,
    localDeviceId: String,
    remoteOwnerId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .zIndex(80f)
            .background(Color.Black.copy(alpha = .88f))
            .clickable(onClick = onDismiss)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxWidth()
                .clickable { },
            shape = RoundedCornerShape(30.dp),
            color = SurfaceColor.copy(alpha = .97f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = .14f)),
            elevation = 24.dp,
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Devices Connect", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Choose where Record Labs plays audio", color = SecondaryText, fontSize = 13.sp)
                    }
                    Icon(
                        androidx.compose.ui.res.painterResource("record_labs_connect.svg"),
                        null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(30.dp),
                    )
                }

                Spacer(Modifier.height(6.dp))
                ConnectDeviceRow(
                    name = "This Windows PC",
                    platform = "Windows • This device",
                    selected = remoteOwnerId == null,
                    onClick = { onSelect(localDeviceId) },
                )
                devices.forEach { device ->
                    ConnectDeviceRow(
                        name = device.name,
                        platform = device.platform.replaceFirstChar { it.uppercaseChar() } + " • Nearby",
                        selected = remoteOwnerId == device.id,
                        onClick = { onSelect(device.id) },
                    )
                }
                if (devices.isEmpty()) {
                    Text(
                        "No other device found yet. Open Record Labs on a device using the same Google account and the same Wi-Fi, or enable Bluetooth on both devices.",
                        color = SecondaryText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                    )
                    Text(
                        buildString {
                            append(if (discovery.listenerReady) "Discovery listening" else "Discovery not listening")
                            append(" • IDs ${discovery.accountIdentityCount}")
                            append(" • IPv4 ${discovery.ipv4InterfaceCount}")
                            append(" • RX ${discovery.receivedPackets}")
                            append(" • TX ${discovery.sentPackets}")
                        },
                        color = SecondaryText,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    discovery.lastError?.let { error ->
                        Text(error, color = Color(0xFFFF6B6B), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp))
                    }
                    if (discovery.rejectedAccountPackets > 0) {
                        Text(
                            "Network packets arrived, but their Google account identity did not match.",
                            color = Color(0xFFFF6B6B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
    }
}
            }
        }
    }
}

@Composable
private fun ConnectDeviceRow(
    name: String,
    platform: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colors.primary.copy(alpha = .18f) else Elevated,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colors.primary.copy(alpha = .72f) else Color.White.copy(alpha = .08f)),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(Color.Black.copy(alpha = .32f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    androidx.compose.ui.res.painterResource("record_labs_connect.svg"),
                    null,
                    tint = if (selected) MaterialTheme.colors.primary else Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(platform, color = SecondaryText, fontSize = 12.sp)
            }
            if (selected) Text("✓", color = MaterialTheme.colors.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MainContent(
    section: AppSection,
    compact: Boolean,
    homeListState: androidx.compose.foundation.lazy.LazyListState,
    repository: WindowsMusicRepository,
    homeFeed: HomeFeed?,
    homeLoading: Boolean,
    homeError: String?,
    searchHistory: MutableList<String>,
    searchQuery: MutableState<String>,
    searchFilter: MutableState<String>,
    searchResults: MutableState<SearchResults?>,
    searchSubmitGeneration: MutableState<Pair<Long, String>>,
    libraryTarget: MutableState<String?>,
    onLibraryNavigate: (String?) -> Unit = { libraryTarget.value = it },
    onLibraryBack: () -> Boolean = { false },
    library: WindowsLibrarySnapshot,
    onCreatePlaylist: (String) -> Boolean,
    onTrack: (Track, List<Track>) -> Unit,
    onSearchTrack: (Track) -> Unit,
    onTrackMore: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onOpenCatalog: (String, String, String?, String?, String?) -> Unit,
    onHomeItem: (HomeItem, List<HomeItem>) -> Unit,
    onListenTogether: () -> Unit = {},
    onHomeRefresh: () -> Unit = {},
    onHomeChip: (HomeChip?) -> Unit = {},
) {
    when (section) {
        AppSection.Home -> HomePage(compact, homeFeed, homeLoading, homeError, onHomeRefresh, onHomeChip, onListenTogether, onTrackMore, onPlayNext, onHomeItem, homeListState)
        AppSection.Search -> SearchPage(compact, repository, searchHistory, searchQuery, searchFilter, searchResults, searchSubmitGeneration, onOpenCatalog, onTrackMore, onPlayNext, { searchHistory.remove(it); com.recordlabs.desktop.data.SearchHistoryStore.save(searchHistory) }) { track, _ -> onSearchTrack(track) }
        AppSection.Library -> LibraryPage(compact, libraryTarget, library, onCreatePlaylist, onTrack, onTrackMore, onPlayNext, onLibraryNavigate, onLibraryBack) { playlist ->
            playlist.browseId?.let { onOpenCatalog(playlist.name, "Playlist", it, null, null) }
        }

    }
}

@Composable
private fun WideNavigation(
    selected: AppSection?,
    isProfileSelected: Boolean,
    onProfile: () -> Unit,
    libraryTarget: String? = null,
    onLibraryTarget: (String?) -> Unit = {},
    listenTogetherSelected: Boolean = false,
    onListenTogether: () -> Unit = {},
    onSelect: (AppSection) -> Unit,
) {
    Column(
        Modifier.zIndex(10f).width(76.dp).fillMaxHeight().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = { onLibraryTarget(null); onSelect(AppSection.Library) }, modifier = Modifier.size(52.dp)) {
            Icon(Icons.Default.LibraryMusic, "Library", tint = if (selected == AppSection.Library) MaterialTheme.colors.primary else Color.White)
        }
        Divider(color = Color.White.copy(alpha = .12f), modifier = Modifier.padding(horizontal = 12.dp))
        Column(
            Modifier.weight(1f).padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = { onLibraryTarget("Liked"); onSelect(AppSection.Library) }) {
                Icon(Icons.Default.Favorite, "Liked songs", tint = if (selected == AppSection.Library && libraryTarget == "Liked") Color(0xFFFF3B5C) else SecondaryText)
            }
            IconButton(onClick = { onLibraryTarget("Downloaded"); onSelect(AppSection.Library) }) { Icon(Icons.Default.Download, "Offline music", tint = if (selected == AppSection.Library && libraryTarget == "Downloaded") MaterialTheme.colors.primary else SecondaryText) }
            IconButton(onClick = { onLibraryTarget("Playlists"); onSelect(AppSection.Library) }) { Icon(Icons.Default.QueueMusic, "Playlists", tint = if (selected == AppSection.Library && libraryTarget == "Playlists") MaterialTheme.colors.primary else SecondaryText) }
        }
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun LandscapeHeader(
    searchFocusRequester: androidx.compose.ui.focus.FocusRequester? = null,
    query: String,
    selectAllTrigger: Int = 0,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    homeLoading: Boolean = false,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    onSearchBounds: (Int, Int) -> Unit,
    onListenTogether: () -> Unit,
    onFullscreen: () -> Unit,
    onProfile: () -> Unit,
    startPadding: androidx.compose.ui.unit.Dp = 0.dp,
    /**
     * Width of the open queue panel at the left of the player pages. The whole
     * header (search too) starts after it, so it never covers the panel's
     * close button.
     */
    queueWidth: androidx.compose.ui.unit.Dp = 0.dp,
    /** Music player / lyrics page: the lyrics translate button, left of Listen Together. */
    showLyricsTranslation: Boolean = false,
    /** Width of the side queue panel open at the right: the header ends before it. */
    endInset: androidx.compose.ui.unit.Dp = 0.dp,
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = query,
                selection = if (selectAllTrigger > 0 && query.isNotEmpty()) TextRange(0, query.length) else TextRange(query.length)
            )
        )
    }

    LaunchedEffect(query) {
        if (query != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = query,
                selection = TextRange(query.length)
            )
        }
    }

    var lastHandledTrigger by remember { mutableStateOf(0) }
    var pendingSelectAll by remember { mutableStateOf(selectAllTrigger > 0) }

    LaunchedEffect(selectAllTrigger) {
        if (selectAllTrigger > 0 && selectAllTrigger != lastHandledTrigger) {
            lastHandledTrigger = selectAllTrigger
            if (textFieldValue.text.isNotEmpty()) {
                pendingSelectAll = true
                textFieldValue = textFieldValue.copy(
                    selection = TextRange(0, textFieldValue.text.length)
                )
                kotlinx.coroutines.delay(80)
                if (textFieldValue.text.isNotEmpty()) {
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                }
            }
        }
    }

    val animatedQueueWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = queueWidth,
        animationSpec = androidx.compose.animation.core.tween(320),
        label = "header-queue-width",
    )
    // Same timing as the side queue panel opening/closing.
    val animatedEndInset by androidx.compose.animation.core.animateDpAsState(
        targetValue = endInset,
        animationSpec = androidx.compose.animation.core.tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "header-end-inset",
    )
    Box(Modifier.fillMaxWidth().height(72.dp)) {
    // The shade behind the header. With the queue open at the left it fades in
    // from the panel's edge, so there is no hard line or bright strip next to it.
    Box(
        Modifier.fillMaxSize()
            .padding(start = (animatedQueueWidth - 40.dp).coerceAtLeast(0.dp), end = animatedEndInset)
            .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
            .drawBehind {
                drawRect(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.65f),
                        0.6f to Color.Transparent,
                        1f to Color.Transparent
                    )
                )
                val queueOpen = (animatedQueueWidth / 368.dp).coerceIn(0f, 1f)
                if (queueOpen > 0f) {
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color.Black.copy(alpha = 1f - queueOpen), Color.Black),
                            startX = 0f,
                            endX = 96.dp.toPx(),
                        ),
                        blendMode = androidx.compose.ui.graphics.BlendMode.DstIn,
                    )
                }
            },
    )
    BoxWithConstraints(
        Modifier.fillMaxWidth().padding(start = animatedQueueWidth, end = animatedEndInset).height(72.dp).padding(horizontal = 14.dp),
    ) {
        val animatedStartPadding by androidx.compose.animation.core.animateDpAsState(
            targetValue = startPadding,
            animationSpec = androidx.compose.animation.core.tween(320),
            label = "header-start-padding"
        )
        Row(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = animatedStartPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(shape = CircleShape, color = HeaderGlass, border = BorderStroke(1.dp, HeaderGlassBorder), modifier = Modifier.size(44.dp).clickable(enabled = canGoBack, onClick = onBack)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.ArrowBack, "Back", tint = if (canGoBack) Color.White else SecondaryText.copy(alpha = .35f)) }
            }
            Surface(shape = CircleShape, color = HeaderGlass, border = BorderStroke(1.dp, HeaderGlassBorder), modifier = Modifier.size(44.dp).clickable(enabled = canGoForward, onClick = onForward)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.ArrowBack, "Forward", tint = if (canGoForward) Color.White else SecondaryText.copy(alpha = .35f), modifier = Modifier.graphicsLayer { rotationZ = 180f }) }
            }
        }
        // 725 px on a 2560 px reference window. The ratio keeps the field
        // centered and proportional on smaller or larger displays.
        val searchWidth = (maxWidth * (725f / 2560f)).coerceIn(360.dp, 900.dp)
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(shape = CircleShape, color = HeaderGlass, border = BorderStroke(1.dp, HeaderGlassBorder), modifier = Modifier.size(44.dp).clickable(onClick = onHome)) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedContent(targetState = homeLoading) { loading ->
                        if (loading) {
                            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                    animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.LinearEasing)
                                )
                            )
                            Icon(Icons.Default.Refresh, "Reload", tint = Color.White, modifier = Modifier.graphicsLayer { rotationZ = rotation })
                        } else {
                            Icon(Icons.Default.Home, "Home", tint = Color.White)
                        }
                    }
                }
            }
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    pendingSelectAll = false
                    textFieldValue = newValue
                    if (newValue.text != query) {
                        onQueryChange(newValue.text)
                    }
                },
                modifier = Modifier.width(searchWidth).clip(RoundedCornerShape(50)).border(1.dp, HeaderGlassBorder, RoundedCornerShape(50)).trackTextInputFocus().let { if (searchFocusRequester != null) it.focusRequester(searchFocusRequester) else it }
                .onGloballyPositioned { coordinates ->
                    onSearchBounds(coordinates.positionInWindow().x.roundToInt(), coordinates.size.width)
                }
                .onFocusChanged { focusState ->
                    onSearchFocusChange(focusState.isFocused)
                    if (focusState.isFocused && pendingSelectAll && textFieldValue.text.isNotEmpty()) {
                        textFieldValue = textFieldValue.copy(
                            selection = TextRange(0, textFieldValue.text.length)
                        )
                        pendingSelectAll = false
                    }
                }
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                        onSubmit(); true
                    } else false
                },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = SecondaryText) },
                trailingIcon = if (textFieldValue.text.isNotBlank()) {
                    {
                        IconButton(onClick = {
                            textFieldValue = TextFieldValue("")
                            onQueryChange("")
                        }) {
                            Icon(Icons.Default.Close, "Clear search", tint = SecondaryText)
                        }
                    }
                } else null,
                placeholder = { Text("What do you want to play?") },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = HeaderGlass,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
            Surface(shape = CircleShape, color = HeaderGlass, border = BorderStroke(1.dp, HeaderGlassBorder), modifier = Modifier.size(44.dp).clickable { onFullscreen() }) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Fullscreen, "Fullscreen", tint = Color.White) }
            }
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (showLyricsTranslation) LyricsTranslationButton(buttonSize = 44.dp, iconSize = 22.dp, backing = HeaderGlass)
            Surface(shape = CircleShape, color = HeaderGlass, border = BorderStroke(1.dp, HeaderGlassBorder), modifier = Modifier.size(44.dp).clickable(onClick = onListenTogether)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Group, "Listen Together", tint = Color.White) }
            }
            val profilePhoto = WindowsPreferences.googleAccountPhoto()
            Box {
            if (profilePhoto.isNotBlank()) {
                Surface(shape = CircleShape, color = SurfaceColor, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)), modifier = Modifier.size(44.dp).clickable(onClick = onProfile)) {
                    Box(contentAlignment = Alignment.Center) { Artwork(profilePhoto, Modifier.fillMaxSize(), CircleShape) }
                }
            } else {
                Surface(shape = CircleShape, color = HeaderGlass, border = BorderStroke(1.dp, HeaderGlassBorder), modifier = Modifier.size(44.dp).clickable(onClick = onProfile)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, "Profile", tint = Color.White) }
                }
            }
                // An update is waiting (Settings > About).
                UpdateBadge(com.recordlabs.desktop.data.AppUpdater.badgeCount, Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = (-3).dp))
            }
        }
    }
    }
}

@Composable
private fun LandscapeSearchOverlay(
    repository: WindowsMusicRepository,
    query: String,
    history: List<String>,
    widthPx: Int,
    leftPx: Int,
    onSearch: (String) -> Unit,
    onTrack: (Track) -> Unit,
    onTrackMore: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onCatalog: (CatalogItem, String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val overlayWidth = with(LocalDensity.current) { widthPx.toDp() }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var suggestionItems by remember { mutableStateOf(SearchResults()) }
    // The query whose suggestions have finished loading. Until then the panel
    // is loading, from the very first frame (no "No suggestions" flash).
    var finishedQuery by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(query) {
        if (query.isBlank()) {
            suggestions = emptyList()
            suggestionItems = SearchResults()
            finishedQuery = query
            return@LaunchedEffect
        }
        delay(70L)
        // Terms and the first matches arrive together (one request), then the
        // list grows as the other searches answer. A failed first request is
        // tried again a few times (1 s, 2 s, 4 s apart).
        for (attempt in 0 until 4) {
            val result = runCatching {
                repository.searchSuggestionsLive(query).collect { (terms, items) ->
                    suggestions = terms
                    suggestionItems = items
                }
            }
            result.exceptionOrNull()?.let { if (it is kotlinx.coroutines.CancellationException) throw it }
            if (result.isSuccess) break
            if (attempt < 3) delay(loadingRetryDelayMs(attempt + 1))
        }
        finishedQuery = query
    }
    val loading = query.isNotBlank() && finishedQuery != query
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(leftPx, 66),
        onDismissRequest = onDismiss,
        // A focusable Popup steals the TextField focus as soon as the first
        // suggestion arrives, which previously stopped further typing.
        properties = PopupProperties(focusable = false),
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { -it / 12 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(180)) { -it / 12 },
        ) {
            var panelOrigin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            // Redrawn every frame while open, so the blurred page follows what moves under it.
            val frameTick by produceState(0L) { while (true) withFrameNanos { value = it } }
            Surface(
                modifier = Modifier.width(overlayWidth).heightIn(max = 560.dp).shadow(26.dp, RoundedCornerShape(22.dp))
                    .onGloballyPositioned { panelOrigin = it.positionInWindow() },
                color = Color.Transparent,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, HeaderGlassBorder),
            ) {
                Box {
                // Frosted glass like iOS: the page under the panel, blurred, under
                // the search box's see-through grey, so the list stays readable.
                Box(
                    Modifier.matchParentSize()
                        .blur(28.dp, androidx.compose.ui.draw.BlurredEdgeTreatment.Rectangle)
                        .drawBehind {
                            frameTick
                            val layer = SearchBackdrop.layer ?: return@drawBehind
                            if (!SearchBackdrop.active) return@drawBehind
                            translate(SearchBackdrop.originInWindow.x - panelOrigin.x, SearchBackdrop.originInWindow.y - panelOrigin.y) {
                                drawLayer(layer)
                            }
                        },
                )
                Box(Modifier.matchParentSize().background(SearchPanelGlass))
                androidx.compose.runtime.CompositionLocalProvider(LocalGlassListRows provides true) {
                LazyColumn(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (query.isBlank() && history.isNotEmpty()) {
                        item { Text("Recent searches", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(start = 10.dp, bottom = 6.dp)) }
                        items(history.asReversed().take(10)) { term ->
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onSearch(term) }.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.Search, null, tint = SecondaryText, modifier = Modifier.size(19.dp))
                                Text(term, color = Color.White, modifier = Modifier.padding(start = 12.dp).weight(1f), maxLines = 1)
                                IconButton(onClick = { onRemoveHistory(term) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, "Remove", tint = SecondaryText, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    // Nothing to show yet for this search: a spinner (or a note when
                    // the search really found nothing).
                    val nothingShown = query.isNotBlank() && suggestions.isEmpty() &&
                        suggestionItems.tracks.isEmpty() && suggestionItems.artists.isEmpty() &&
                        suggestionItems.albums.isEmpty() && suggestionItems.topResult == null
                    if (nothingShown && loading) item {
                        Box(Modifier.fillMaxWidth().height(72.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(26.dp), color = MaterialTheme.colors.primary, strokeWidth = 2.5.dp)
                        }
                    }
                    if (nothingShown && !loading) item {
                        Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                            Text("No suggestions for \"$query\"", color = SecondaryText, fontSize = 13.sp)
                        }
                    }
                    items(suggestions.take(7)) { suggestion ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onSearch(suggestion) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Search, null, tint = SecondaryText, modifier = Modifier.size(19.dp))
                            Text(suggestion, color = Color.White, modifier = Modifier.padding(start = 12.dp), maxLines = 1)
                        }
                    }
                    // YouTube Music's best match first (the artist for an artist's
                    // name, the song for a song title), then songs, artists, albums.
                    val top = suggestionTopResult(query, suggestionItems)
                    val tracks = suggestionItems.tracks.filterNot { it.id == top?.track?.id }.take(5)
                    val albums = suggestionItems.albums.filterNot { it.browseId == top?.item?.browseId }.take(3)
                    val artists = suggestionItems.artists.filterNot { it.browseId == top?.item?.browseId }.take(3)
                    top?.let { best ->
                        item { Text("Top result", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(start = 10.dp, top = 8.dp)) }
                        // Keyed, so suggestions arriving while it is being dragged do not reset the row.
                        item(key = "top:" + (best.track?.id ?: best.item?.browseId ?: best.kind)) {
                            SuggestionTopResultRow(
                                best,
                                onTrack = onTrack,
                                onTrackMore = onTrackMore,
                                onPlayNext = onPlayNext,
                                onCatalog = onCatalog,
                            )
                        }
                    }
                    if (tracks.isNotEmpty()) item { Text("Songs", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(start = 10.dp, top = 8.dp)) }
                    items(tracks, key = Track::id) { track -> SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }) { onTrack(track) } }
                    if (artists.isNotEmpty()) item { Text("Artists", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(start = 10.dp, top = 8.dp)) }
                    items(artists) { artist -> SearchEntityRow("Artist", artist.title, artist.artworkUrl, Icons.Default.Person) { onCatalog(artist, "Artist") } }
                    if (albums.isNotEmpty()) item { Text("Albums", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(start = 10.dp, top = 8.dp)) }
                    items(albums) { album -> SearchEntityRow("Album", album.title, album.artworkUrl, Icons.Default.Album) { onCatalog(album, "Album") } }
                    // More results are still coming in.
                    if (loading && !nothingShown) item {
                        Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colors.primary, strokeWidth = 2.dp)
                        }
                    }
                }
                }
            }
            }
        }
    }
}

@Composable
private fun CompactNavigation(selected: AppSection?, isProfileSelected: Boolean, homeLoading: Boolean = false, onProfile: () -> Unit, onSelect: (AppSection) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(80.dp).background(SurfaceColor).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppSection.values().forEach { item ->
            NavigationIcon(item, item == selected, item == AppSection.Home && homeLoading, onSelect)
        }
        ProfileNavigationIcon(isProfileSelected, onProfile)
    }
}

@Composable
private fun ProfileNavigationIcon(selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-2).dp),
    ) {
        Box {
            IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Person, "Profile", tint = if (selected) MaterialTheme.colors.primary else SecondaryText, modifier = Modifier.size(25.dp))
            }
            UpdateBadge(com.recordlabs.desktop.data.AppUpdater.badgeCount, Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp))
        }
        if (selected) Text("Profile", color = MaterialTheme.colors.primary, fontSize = 10.sp)
    }
}


@Composable
private fun NavigationIcon(item: AppSection, selected: Boolean, isLoading: Boolean = false, onSelect: (AppSection) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-2).dp),
    ) {
        IconButton(onClick = { onSelect(item) }, modifier = Modifier.size(44.dp)) {
                        androidx.compose.animation.AnimatedContent(targetState = isLoading) { loading ->
                            if (loading && item == AppSection.Home) {
                                val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
                                val rotation by infiniteTransition.animateFloat(0f, 360f, androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.LinearEasing)))
                                Icon(Icons.Default.Refresh, item.label, tint = if (selected) MaterialTheme.colors.primary else SecondaryText, modifier = Modifier.size(25.dp).graphicsLayer { rotationZ = rotation })
                            } else {
                                Icon(item.icon, item.label, tint = if (selected) MaterialTheme.colors.primary else SecondaryText, modifier = Modifier.size(25.dp))
                            }
                        }
        }
        if (selected) Text(item.label, color = MaterialTheme.colors.primary, fontSize = 10.sp)
    }
}

@Composable
private fun ProfileButtonMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOpenPage: (ProfilePage) -> Unit,
) {
    var moreContent by remember { mutableStateOf(false) }
    var autoSync by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceColor),
    ) {
        Column(Modifier.width(310.dp).padding(vertical = 6.dp)) {
            Text("Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(16.dp))
            Text("Not signed in", color = SecondaryText, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))
            DropdownMenuItem(onClick = onDismiss) {
                Icon(Icons.Default.Person, null, tint = Accent)
                Spacer(Modifier.width(12.dp))
                Text("Login")
            }
            Divider(color = Color.White.copy(alpha = .12f))
            ProfileToggleRow("More content", moreContent) { moreContent = it }
            ProfileToggleRow("Auto-sync with account", autoSync) { autoSync = it }
            Divider(color = Color.White.copy(alpha = .12f))
            DropdownMenuItem(onClick = { onOpenPage(ProfilePage.Integrations) }) {
                Icon(Icons.Default.Extension, null, tint = Accent)
                Spacer(Modifier.width(12.dp))
                Text("Integrations")
            }
        }
    }
}

@Composable
private fun ProfileToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = Color.White, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ProfilePageContent(
    page: ProfilePage,
    compact: Boolean,
    repository: WindowsMusicRepository,
    onBack: () -> Unit,
    settingsSubpage: String?,
    onSettingsSubpage: (String?) -> Unit,
    onGoogleSessionChanged: () -> Unit,
    onOpenPage: (ProfilePage) -> Unit,
) {
    val route = ProfileRoute(
        page,
        settingsSubpage.takeIf { page == ProfilePage.Settings },
        page == ProfilePage.ListenTogether && ListenTogetherUi.settingsOpen,
    )
    AnimatedContent(
        targetState = route,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            // Going deeper slides in from the right, going back from the left.
            val forward = targetState.depth >= initialState.depth
            val enter = fadeIn(tween(240, delayMillis = 40)) +
                slideInHorizontally(tween(300, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { width -> (if (forward) width else -width) / 14 }
            val exit = fadeOut(tween(140)) +
                slideOutHorizontally(tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { width -> (if (forward) -width else width) / 14 }
            (enter togetherWith exit).using(null)
        },
    ) { shown ->
        ProfileRouteContent(shown, compact, repository, onBack, onSettingsSubpage, onGoogleSessionChanged, onOpenPage)
    }
}

/** What a profile page shows; each change animates as a page transition. */
private data class ProfileRoute(val page: ProfilePage, val settingsSubpage: String?, val listenTogetherSettings: Boolean) {
    val depth: Int
        get() = when (page) {
            ProfilePage.Home -> 0
            ProfilePage.Integrations, ProfilePage.Login -> 1
            ProfilePage.Settings -> if (settingsSubpage == null) 1 else 2
            ProfilePage.Discord, ProfilePage.LastFm -> 2
            ProfilePage.ListenTogether -> if (listenTogetherSettings) 3 else 2
        }
}

@Composable
private fun ProfileRouteContent(
    route: ProfileRoute,
    compact: Boolean,
    repository: WindowsMusicRepository,
    onBack: () -> Unit,
    onSettingsSubpage: (String?) -> Unit,
    onGoogleSessionChanged: () -> Unit,
    onOpenPage: (ProfilePage) -> Unit,
) {
    val page = route.page
    Column(Modifier.fillMaxSize().padding(if (compact) 16.dp else 28.dp)) {
        if (compact && page != ProfilePage.ListenTogether) Row(verticalAlignment = Alignment.CenterVertically) {
            // Compact windows have no header bar: every sub page gets its own way back.
            if (page != ProfilePage.Home) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                Spacer(Modifier.width(4.dp))
            }
            Text(
                when (page) {
                    ProfilePage.Home -> "Profile"
                    ProfilePage.Integrations -> "Integrations"
                    ProfilePage.ListenTogether -> "Listen Together"
                    ProfilePage.Discord -> "Discord Integration"
                    ProfilePage.LastFm -> "Last.fm Integration"
                    ProfilePage.Settings -> "Settings"
                    ProfilePage.Login -> "Google login"
                },
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (compact && page != ProfilePage.ListenTogether) Spacer(Modifier.height(18.dp))
        when (page) {
            ProfilePage.Home -> ProfileHomePage(repository, onOpenPage, onGoogleSessionChanged)
            ProfilePage.Integrations -> IntegrationsPage(compact, onOpenPage)
            ProfilePage.ListenTogether -> ListenTogetherPage(route.listenTogetherSettings, compact)
            ProfilePage.Discord -> DiscordIntegrationPage()
            ProfilePage.LastFm -> LastFmIntegrationPage()
            ProfilePage.Settings -> SettingsPage(compact, route.settingsSubpage, onSettingsSubpage, onOpenIntegrations = { onOpenPage(ProfilePage.Integrations) })
            ProfilePage.Login -> GoogleLoginView {
                onGoogleSessionChanged()
                onOpenPage(ProfilePage.Home)
            }
        }
    }
}

@Composable
private fun ProfileHomePage(
    repository: WindowsMusicRepository,
    onOpenPage: (ProfilePage) -> Unit,
    onGoogleSessionChanged: () -> Unit,
) {
    var moreContent by remember { mutableStateOf(WindowsPreferences.moreContent()) }
    var autoSync by remember { mutableStateOf(WindowsPreferences.autoSync()) }
    var signedIn by remember { mutableStateOf(WindowsPreferences.googleSession() != null) }
    var accountName by remember { mutableStateOf(WindowsPreferences.googleAccountName()) }
    var accountPhoto by remember { mutableStateOf(WindowsPreferences.googleAccountPhoto()) }
    LaunchedEffect(signedIn) {
        if (signedIn) repository.googleAccountProfile()?.let { profile ->
            accountName = profile.name
            accountPhoto = profile.photoUrl.orEmpty()
        }
    }
    val pageScroll = rememberScrollState()
    Column(Modifier.pageScrollbar(pageScroll).verticalScroll(pageScroll)) {
        Row(
            Modifier.fillMaxWidth()
                .background(SurfaceColor, RoundedCornerShape(18.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (signedIn && accountPhoto.isNotBlank()) {
                Artwork(accountPhoto, Modifier.size(42.dp), CircleShape)
            } else Icon(Icons.Default.Person, null, tint = Accent, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (signedIn) accountName.ifBlank { "Google account" } else "Not signed in",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!signedIn) Text("Login to sync your Record Labs account", color = SecondaryText, fontSize = 12.sp)
            }
            GlassButton(prominent = true, onClick = {
                if (signedIn) {
                    WindowsPreferences.signOutGoogle()
                    signedIn = false
                    onGoogleSessionChanged()
                } else onOpenPage(ProfilePage.Login)
            }) { Text(if (signedIn) "Logout" else "Login") }
        }
        Spacer(Modifier.height(18.dp))
        SettingsGroup("Account") {
            SettingToggle("More content", "Use your account for personalized Home recommendations", moreContent) {
                moreContent = it; WindowsPreferences.setMoreContent(it)
                com.recordlabs.desktop.data.AppDataEvents.request(com.recordlabs.desktop.data.AppDataRequest.ReloadHome)
            }
            SettingToggle("Auto-sync with account", "Keep your library synchronized", autoSync) {
                autoSync = it; WindowsPreferences.setAutoSync(it)
            }
        }
        SettingsGroup("Services") {
            SettingRow("Integrations", "Discord, Last.fm and Listen Together", leading = Icons.Default.Extension) { onOpenPage(ProfilePage.Integrations) }
            SettingRow("Settings", "Appearance, playback and app preferences", leading = Icons.Default.Settings, badge = com.recordlabs.desktop.data.AppUpdater.badgeCount) { onOpenPage(ProfilePage.Settings) }
        }
        // Room at the end so the floating mini player never covers the last item.
        Spacer(Modifier.height(MiniPlayerClearance))
    }
}

@Composable
private fun IntegrationsPage(compact: Boolean, onOpenPage: (ProfilePage) -> Unit) {
    // Scrolls on short windows so the mini player never covers the last row.
    val pageScroll = rememberScrollState()
    Column(Modifier.pageScrollbar(pageScroll).verticalScroll(pageScroll)) {
        // Compact windows already show "Integrations" with their back button above.
        if (compact) Text("Connect Record Labs with external services", color = SecondaryText, fontSize = 13.sp, modifier = Modifier.padding(bottom = 14.dp))
        else SettingsPageHeader("Integrations", "Connect Record Labs with external services", Icons.Default.Extension, compact = false)
        SettingsGroup("Services") {
            SettingRow("Discord Integration", "Show the song currently playing in Discord", leading = Icons.Default.Headset) { onOpenPage(ProfilePage.Discord) }
            SettingRow("Last.fm Integration", "Scrobble songs and sync listening history", leading = Icons.Default.History) { onOpenPage(ProfilePage.LastFm) }
        }
        SettingsGroup("Social") {
            SettingRow("Listen Together", "Listen in sync with friends", leading = Icons.Default.Group) { onOpenPage(ProfilePage.ListenTogether) }
        }
        Spacer(Modifier.height(MiniPlayerClearance))
    }
}

@Composable
private fun ListenTogetherPage(showSettings: Boolean, compact: Boolean) {
    val connection by WindowsListenTogetherClient.connection.collectAsState()
    val roomCode by WindowsListenTogetherClient.roomCode.collectAsState()
    val statusMessage by WindowsListenTogetherClient.message.collectAsState()
    val isHost by WindowsListenTogetherClient.isHost.collectAsState()
    val pendingRequests by WindowsListenTogetherClient.pendingJoinRequests.collectAsState()
    val pendingSuggestions by WindowsListenTogetherClient.pendingSuggestions.collectAsState()
    val connectedUsers by WindowsListenTogetherClient.users.collectAsState()
    val bufferingUsers by WindowsListenTogetherClient.bufferingUsers.collectAsState()
    var username by com.recordlabs.desktop.data.AppSettings.listenUsername
    var joinCode by remember { mutableStateOf("") }

    if (showSettings) {
        ListenTogetherSettingsPage(compact, onBack = { ListenTogetherUi.settingsOpen = false })
        return
    }

    val inRoom = connection == ListenTogetherConnection.InRoom
    val busy = connection == ListenTogetherConnection.Connecting
    val pageScroll = rememberScrollState()
    Column(Modifier.fillMaxWidth().pageScrollbar(pageScroll).verticalScroll(pageScroll)) {
        // Title, connection state and the Listen Together settings.
        Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Listen Together", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text("Listen in sync with friends, wherever they are", color = SecondaryText, fontSize = 13.sp)
            }
            ListenTogetherStatusPill(connection)
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = { ListenTogetherUi.settingsOpen = true }) { Icon(Icons.Default.Settings, "Listen Together settings", tint = Color.White) }
        }

        // Creating / joining a room (or leaving it) cross-fades to the other layout,
        // the page height follows smoothly, and its cards rise in one after another.
        androidx.compose.animation.AnimatedContent(
            targetState = inRoom,
            transitionSpec = {
                (fadeIn(tween(260, delayMillis = 60)) + androidx.compose.animation.scaleIn(tween(380, easing = androidx.compose.animation.core.FastOutSlowInEasing), initialScale = 0.985f))
                    .togetherWith(fadeOut(tween(160)) + androidx.compose.animation.scaleOut(tween(200), targetScale = 0.985f))
                    .using(androidx.compose.animation.SizeTransform(clip = false) { _, _ -> tween(380, easing = androidx.compose.animation.core.FastOutSlowInEasing) })
            },
            contentAlignment = Alignment.TopStart,
            label = "listen-together-layout",
        ) { showRoom ->
        Column(Modifier.fillMaxWidth()) {
        if (showRoom) {
            // While the room layout fades out after leaving, it keeps showing the room
            // as it was instead of the cleared state.
            var roomIsHost by remember { mutableStateOf(isHost) }
            var roomUsers by remember { mutableStateOf(connectedUsers) }
            if (inRoom) { roomIsHost = isHost; roomUsers = connectedUsers }
            val isHost = roomIsHost
            val connectedUsers = roomUsers
            val hostName = connectedUsers.firstOrNull(ListenTogetherUser::isHost)?.username
            // The code shown while the room layout fades out after leaving.
            var shownCode by remember { mutableStateOf(roomCode.orEmpty()) }
            if (!roomCode.isNullOrBlank()) shownCode = roomCode.orEmpty()
            ListenTogetherEntrance(0) { ListenTogetherRoomCard(
                roomCode = shownCode,
                isHost = isHost,
                hostName = hostName,
                status = when {
                    bufferingUsers.isNotEmpty() -> "Getting the song ready on ${bufferingUsers.size} device${if (bufferingUsers.size == 1) "" else "s"}…"
                    else -> statusMessage
                },
            ) }
            Spacer(Modifier.height(22.dp))
            ListenTogetherEntrance(1) { SettingsGroup("Listeners · ${connectedUsers.size}") {
                if (connectedUsers.isEmpty()) SettingNote("Waiting for the room's listeners…")
                connectedUsers.forEach { user ->
                    ListenTogetherListenerRow(
                        user = user,
                        buffering = user.userId in bufferingUsers,
                        canManage = isHost && !user.isHost,
                        onMakeHost = { WindowsListenTogetherClient.transferHost(user.userId) },
                        onRemove = { WindowsListenTogetherClient.kickUser(user.userId) },
                        onBlock = {
                            com.recordlabs.desktop.data.AppSettings.blockListener(user.username)
                            WindowsListenTogetherClient.kickUser(user.userId)
                        },
                    )
                }
            } }
            if (isHost) {
                ListenTogetherEntrance(2) { SettingsGroup(if (pendingRequests.isEmpty()) "Join requests" else "Join requests · ${pendingRequests.size}") {
                    if (pendingRequests.isEmpty()) {
                        SettingNote(
                            if (com.recordlabs.desktop.data.AppSettings.listenAutoApproveJoins.value) "Anyone with the room code joins straight away (auto-approve is on in Listen Together settings)."
                            else "No one is waiting to join. Share the room code to invite friends.",
                        )
                    }
                    pendingRequests.forEach { request ->
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            ListenTogetherAvatar(request.username, highlighted = false)
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(request.username, color = Color.White, fontSize = 15.sp, maxLines = 1)
                                Text("Wants to join", color = SecondaryText, fontSize = 12.sp)
                            }
                            TextButton(onClick = {
                                com.recordlabs.desktop.data.AppSettings.blockListener(request.username)
                                WindowsListenTogetherClient.rejectJoin(request.userId)
                            }) { Text("Block", color = SecondaryText) }
                            TextButton(onClick = { WindowsListenTogetherClient.rejectJoin(request.userId) }) { Text("Decline", color = Color.White) }
                            Spacer(Modifier.width(4.dp))
                            GlassButton(prominent = true, onClick = { WindowsListenTogetherClient.approveJoin(request.userId) }) { Text("Allow") }
                        }
                    }
                } }
                if (pendingSuggestions.isNotEmpty()) {
                    SettingsGroup("Song suggestions · ${pendingSuggestions.size}") {
                        pendingSuggestions.forEach { suggestion ->
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Artwork(suggestion.track.artworkUrl, Modifier.size(44.dp), RoundedCornerShape(8.dp))
                                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                    Text(suggestion.track.title, color = Color.White, fontSize = 15.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    Text(
                                        "${suggestion.track.artist} · suggested by ${suggestion.username}",
                                        color = SecondaryText, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    )
                                }
                                TextButton(onClick = { WindowsListenTogetherClient.rejectSuggestion(suggestion.suggestionId) }) { Text("Decline", color = Color.White) }
                                Spacer(Modifier.width(4.dp))
                                GlassButton(prominent = true, onClick = { WindowsListenTogetherClient.approveSuggestion(suggestion.suggestionId) }) { Text("Add") }
                            }
                        }
                    }
                }
            } else {
                ListenTogetherEntrance(2) { SettingsGroup("Playback") {
                    SettingRow("Re-sync playback", "Catch up with the host's player", Icons.Default.Refresh) { WindowsListenTogetherClient.requestSync() }
                    SettingNote("To suggest a song to the host, open the song's ⋮ menu and choose Suggest to host.")
                } }
            }
        } else {
            // Not in a room: a short notice when something happened, then the name and the two ways in.
            val notice = statusMessage.takeIf {
                connection == ListenTogetherConnection.Error || busy ||
                    (connection == ListenTogetherConnection.Disconnected && it.isNotBlank() && it != "Not connected" && it != "Disconnected")
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = notice != null,
                enter = fadeIn(tween(220)) + androidx.compose.animation.expandVertically(tween(260)),
                exit = fadeOut(tween(160)) + androidx.compose.animation.shrinkVertically(tween(220)),
            ) {
                var shownNotice by remember { mutableStateOf(notice.orEmpty()) }
                if (notice != null) shownNotice = notice
                Column {
                    ListenTogetherNotice(shownNotice, error = connection == ListenTogetherConnection.Error)
                    Spacer(Modifier.height(18.dp))
                }
            }
            ListenTogetherEntrance(0) { SettingsGroup("Your name") {
                ListenTogetherTextField(
                    value = username,
                    onValueChange = { username = it.take(32) },
                    placeholder = "How friends see you in the room",
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                )
                SettingNote(if (username.isBlank()) "Enter a name to create or join a room." else "Shown to everyone in the room.")
            } }
            val canStart = username.isNotBlank() && !busy
            ListenTogetherEntrance(1) { BoxWithConstraints(Modifier.fillMaxWidth()) {
                val startCard: @Composable (Modifier) -> Unit = { modifier ->
                    ListenTogetherActionCard(
                        icon = Icons.Default.Group,
                        title = "Start a room",
                        text = "You choose the music. Friends join with your room code and listen along.",
                        modifier = modifier,
                    ) {
                        GlassButton(prominent = true, enabled = canStart, onClick = { WindowsListenTogetherClient.createRoom(username) }) {
                            if (busy) { CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                            Text("Create room")
                        }
                    }
                }
                val joinCard: @Composable (Modifier) -> Unit = { modifier ->
                    val canJoin = canStart && joinCode.isNotBlank()
                    ListenTogetherActionCard(
                        icon = androidx.compose.material.icons.Icons.Default.QueueMusic,
                        title = "Join a room",
                        text = "Enter the code a friend shared with you.",
                        modifier = modifier,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ListenTogetherTextField(
                                value = joinCode,
                                onValueChange = { joinCode = it.uppercase().filter(Char::isLetterOrDigit).take(8) },
                                placeholder = "ROOM CODE",
                                monospace = true,
                                onDone = { if (canJoin) WindowsListenTogetherClient.joinRoom(joinCode, username) },
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(10.dp))
                            GlassButton(prominent = true, enabled = canJoin, onClick = { WindowsListenTogetherClient.joinRoom(joinCode, username) }) { Text("Join") }
                        }
                    }
                }
                if (maxWidth >= 640.dp) {
                    Row(Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        startCard(Modifier.weight(1f).fillMaxHeight())
                        joinCard(Modifier.weight(1f).fillMaxHeight())
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        startCard(Modifier.fillMaxWidth())
                        joinCard(Modifier.fillMaxWidth())
                    }
                }
            } }
            Spacer(Modifier.height(18.dp))
            ListenTogetherEntrance(2) { Text(
                "Playback, join approval, song suggestions and host changes stay in sync with Record Labs on Android too.",
                color = SecondaryText,
                fontSize = 12.sp,
            ) }
        }
        }
        }
        // Room at the end so the floating mini player never covers the last item.
        Spacer(Modifier.height(MiniPlayerClearance))
    }
}

/** Fades a section in while it rises a little; [index] staggers the sections of one layout. */
@Composable
private fun ListenTogetherEntrance(index: Int, content: @Composable () -> Unit) {
    val progress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(80L + index * 70L)
        progress.animateTo(1f, tween(420, easing = androidx.compose.animation.core.FastOutSlowInEasing))
    }
    Box(Modifier.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 16.dp.toPx()
    }) { content() }
}

/** A small pill with a coloured dot: where the connection stands. */
@Composable
private fun ListenTogetherStatusPill(connection: ListenTogetherConnection) {
    val (label, dot) = when (connection) {
        ListenTogetherConnection.Disconnected -> "Not connected" to SecondaryText
        ListenTogetherConnection.Connecting -> "Connecting…" to Color(0xFFFFB020)
        ListenTogetherConnection.Connected -> "Connected" to Color(0xFF34C759)
        ListenTogetherConnection.InRoom -> "In a room" to Color(0xFF34C759)
        ListenTogetherConnection.Error -> "Couldn't connect" to Color(0xFFFF6B6B)
    }
    val dotColor by animateColorAsState(dot, tween(300), label = "lt-status-dot")
    Row(
        Modifier.background(Elevated, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(dotColor, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ListenTogetherNotice(text: String, error: Boolean) {
    Row(
        Modifier.fillMaxWidth()
            .background(if (error) Color(0xFFFF6B6B).copy(alpha = 0.12f) else Elevated, RoundedCornerShape(14.dp))
            .border(1.dp, if (error) Color(0xFFFF6B6B).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Info, null, tint = if (error) Color(0xFFFF6B6B) else SecondaryText, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun ListenTogetherActionCard(
    icon: ImageVector,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit,
) {
    Surface(color = SurfaceColor, shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(Modifier.padding(18.dp)) {
            Box(Modifier.size(40.dp).background(Accent.copy(alpha = 0.16f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(text, color = SecondaryText, fontSize = 12.sp)
            Spacer(Modifier.weight(1f, fill = true).heightIn(min = 16.dp))
            action()
        }
    }
}

/** The room code (big, easy to read out), who leads, and the share / leave buttons. */
@Composable
private fun ListenTogetherRoomCard(roomCode: String, isHost: Boolean, hostName: String?, status: String) {
    var copied by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(copied) {
        if (copied != null) { kotlinx.coroutines.delay(1600); copied = null }
    }
    Surface(color = SurfaceColor, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.background(Brush.linearGradient(listOf(Accent.copy(alpha = 0.22f), Color.Transparent), start = androidx.compose.ui.geometry.Offset.Zero, end = androidx.compose.ui.geometry.Offset(900f, 500f)))) {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(22.dp)) {
                val wide = maxWidth >= 620.dp
                val info: @Composable () -> Unit = {
                    Column {
                        Text("ROOM CODE", color = SecondaryText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.6.sp)
                        Text(
                            roomCode,
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            letterSpacing = 4.sp,
                        )
                        Text(
                            if (isHost) "You're the host · share the code so friends can join"
                            else "Listening with ${hostName ?: "the host"}",
                            color = Color.White.copy(alpha = 0.86f),
                            fontSize = 13.sp,
                        )
                        if (status.isNotBlank()) Text(status, color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                val shareButtons: @Composable () -> Unit = {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        GlassButton(onClick = {
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(roomCode), null)
                            copied = "code"
                        }) { Icon(if (copied == "code") Icons.Default.Check else Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text(if (copied == "code") "Copied" else "Copy code") }
                        GlassButton(onClick = {
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection("https://metrolist.cc/listen?code=$roomCode"), null)
                            copied = "link"
                        }) { Icon(if (copied == "link") Icons.Default.Check else Icons.Default.Share, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text(if (copied == "link") "Copied" else "Copy link") }
                    }
                }
                val leaveButton: @Composable () -> Unit = {
                    GlassButton(tint = Color(0xFFFF453A), onClick = WindowsListenTogetherClient::leave) { Text("Leave room", maxLines = 1, softWrap = false) }
                }
                if (wide) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { info() }
                        Spacer(Modifier.width(16.dp))
                        shareButtons()
                        Spacer(Modifier.width(10.dp))
                        leaveButton()
                    }
                } else {
                    // Narrow windows: sharing on one line, leaving on its own.
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        info()
                        Spacer(Modifier.height(2.dp))
                        shareButtons()
                        leaveButton()
                    }
                }
            }
        }
    }
}

@Composable
private fun ListenTogetherAvatar(name: String, highlighted: Boolean, dimmed: Boolean = false) {
    Box(
        Modifier.size(40.dp).alpha(if (dimmed) 0.5f else 1f)
            .background(if (highlighted) Accent else Elevated, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(name.trim().take(1).uppercase().ifBlank { "?" }, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ListenTogetherListenerRow(
    user: ListenTogetherUser,
    buffering: Boolean,
    canManage: Boolean,
    onMakeHost: () -> Unit,
    onRemove: () -> Unit,
    onBlock: () -> Unit,
) {
    var menu by remember(user.userId) { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        ListenTogetherAvatar(user.username, highlighted = user.isHost, dimmed = !user.isConnected)
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(user.username.ifBlank { "Listener" }, color = if (user.isConnected) Color.White else SecondaryText, fontSize = 15.sp, maxLines = 1)
            Text(
                when {
                    user.isHost -> "Host"
                    !user.isConnected -> "Offline"
                    buffering -> "Getting the song ready…"
                    else -> "Listening"
                },
                color = if (user.isHost) Accent else SecondaryText,
                fontSize = 12.sp,
            )
        }
        if (canManage) Box {
            IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Manage ${user.username}", tint = SecondaryText) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(onClick = { menu = false; onMakeHost() }) { Text("Make host") }
                DropdownMenuItem(onClick = { menu = false; onRemove() }) { Text("Remove from room") }
                DropdownMenuItem(onClick = { menu = false; onBlock() }) { Text("Block", color = Color(0xFFFF6B6B)) }
            }
        }
    }
}

/** A text box in the app's style (dark field, accent edge while typing). */
@Composable
private fun ListenTogetherTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    monospace: Boolean = false,
    onDone: (() -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val edge by animateColorAsState(if (focused) Accent.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.10f), tween(180), label = "lt-field-edge")
    val style = androidx.compose.ui.text.TextStyle(
        color = if (enabled) Color.White else SecondaryText,
        fontSize = if (monospace) 17.sp else 15.sp,
        fontFamily = if (monospace) androidx.compose.ui.text.font.FontFamily.Monospace else null,
        letterSpacing = if (monospace) 3.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
        fontWeight = if (monospace) FontWeight.SemiBold else null,
    )
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = style,
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Accent),
        modifier = modifier
            .trackTextInputFocus()
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { event ->
                if (onDone != null && event.type == KeyEventType.KeyDown && (event.key == Key.Enter || event.key == Key.NumPadEnter)) { onDone(); true } else false
            },
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth()
                    .background(Elevated, RoundedCornerShape(12.dp))
                    .border(1.dp, edge, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) Text(placeholder, style = style.copy(color = SecondaryText.copy(alpha = 0.7f)), maxLines = 1)
                inner()
            }
        },
    )
}

/** Whether Listen Together's own settings are showing (so Back returns to the room page first). */
private object ListenTogetherUi {
    var settingsOpen by mutableStateOf(false)
}

@Composable
private fun ListenTogetherSettingsPage(compact: Boolean, onBack: () -> Unit) {
    val settings = com.recordlabs.desktop.data.AppSettings
    var username by settings.listenUsername
    var autoJoins by settings.listenAutoApproveJoins
    var autoSuggestions by settings.listenAutoApproveSuggestions
    var guestControls by settings.listenGuestControls
    val connection by WindowsListenTogetherClient.connection.collectAsState()
    val inRoom = connection == ListenTogetherConnection.InRoom
    val blocked = settings.blockedListeners()
    var showBlocked by remember { mutableStateOf(false) }
    val pageScroll = rememberScrollState()
    Column(Modifier.fillMaxWidth().pageScrollbar(pageScroll).verticalScroll(pageScroll)) {
        // Wide windows go back with the shared header's back button; compact ones have no header bar.
        if (compact) Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back to Listen Together", tint = Color.White) }
            Spacer(Modifier.width(4.dp))
            Text("Listen Together settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        } else SettingsPageHeader("Listen Together settings", "How you appear and what guests can do in your room", Icons.Default.Group, compact = false)
        SettingsGroup("Profile") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text("Username", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                ListenTogetherTextField(
                    value = username,
                    onValueChange = { username = it.take(32) },
                    placeholder = "How friends see you in the room",
                    enabled = !inRoom,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (inRoom) Text("Leave the room to change it.", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
            SettingRow(
                "Blocked users",
                if (blocked.isEmpty()) "No blocked users" else "${blocked.size} blocked",
                enabled = blocked.isNotEmpty(),
                leading = Icons.Default.Person,
            ) { showBlocked = true }
        }
        SettingsGroup("Room") {
            SettingToggle("Auto-approve join requests", "Approve join requests without manual review", autoJoins) { autoJoins = it }
            SettingToggle("Auto-approve song suggestions", "Automatically add guest suggestions", autoSuggestions) { autoSuggestions = it }
            SettingToggle("Guest playback controls", "Allow guests to play, pause, seek, and skip", guestControls) { guestControls = it }
        }
        // Room at the end so the floating mini player never covers the last item.
        Spacer(Modifier.height(MiniPlayerClearance))
    }
    if (showBlocked) AlertDialog(
        onDismissRequest = { showBlocked = false },
        title = { Text("Blocked users") },
        text = {
            Column {
                if (settings.blockedListeners().isEmpty()) Text("No blocked users", color = SecondaryText)
                settings.blockedListeners().forEach { name ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(name, color = Color.White, modifier = Modifier.weight(1f))
                        TextButton(onClick = { settings.unblockListener(name) }) { Text("Unblock") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { showBlocked = false }) { Text("Close") } },
    )
}

@Composable
private fun IntegrationRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).background(SurfaceColor, RoundedCornerShape(16.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).background(Elevated, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Accent)
        }
        Column(Modifier.padding(horizontal = 14.dp).weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp)
            Text(subtitle, color = SecondaryText, fontSize = 12.sp)
        }
        Icon(Icons.Default.MoreVert, null, tint = SecondaryText)
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun HomePage(
    compact: Boolean,
    feed: HomeFeed?,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit = {},
    onChipSelect: (HomeChip?) -> Unit = {},
    onListenTogether: () -> Unit = {},
    onTrackMore: (Track) -> Unit = {},
    onPlayNext: (Track) -> Unit = {},
    onItemClick: (HomeItem, List<HomeItem>) -> Unit,
    homeListState: androidx.compose.foundation.lazy.LazyListState,
) {
    var selectedChip by remember { mutableStateOf<HomeChip?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (!isLoading) isRefreshing = false
    }

    val hasContent = feed != null && feed.shelves.isNotEmpty()
    // Nothing to show yet: the page's shape with a moving light while it loads
    // (and is tried again); the message only after several failed tries.
    val showSkeleton = !hasContent && isLoading && error == null
    androidx.compose.runtime.CompositionLocalProvider(LocalArtworkLoading provides ArtworkLoadingStyle.Persistent) {
    Column(
        Modifier
            .fillMaxSize()
            .observePullToRefresh(homeListState, !isRefreshing && !isLoading) {
                isRefreshing = true
                onRefresh()
            },
    ) {
        if (compact) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onListenTogether) {
                    Icon(Icons.Default.QueueMusic, "Listen Together", tint = MaterialTheme.colors.primary)
                }
            }
        }

        if (!hasContent && !showSkeleton) {
            // Centered on the page.
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Looks like your connection is having trouble.",
                        color = SecondaryText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    GlassButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Try again")
                    }
                }
            }
        } else LazyColumn(
            state = homeListState,
            modifier = Modifier.weight(1f).pageScrollbar(homeListState).dragVertically(homeListState).padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
                if (!compact) item { Spacer(Modifier.height(56.dp)) }
            when {
                showSkeleton || feed == null -> item { HomeLoadingSkeleton(compact) }
                else -> {
                    if (feed.chips.isNotEmpty()) item {
                        HomeMoodFilters(feed.chips, selectedChip) { chip ->
                            selectedChip = if (selectedChip?.params == chip.params) null else chip
                            onChipSelect(selectedChip)
                        }
                    }
                    feed.shelves.forEachIndexed { index, shelf ->
                        item(key = "$index-${shelf.title}") { HomeShelfSection(shelf, compact, onTrackMore, onPlayNext, onItemClick) }
                    }
                    // More shelves are still on the way.
                    if (isLoading) item(key = "home-more-loading") { HomeShelfSkeleton(compact, list = false) }
                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }
    }
    }
}


@Composable
private fun HomeLoadingSkeleton(compact: Boolean) {
    // The same layout as the loaded home (mood chips, a "Quick picks" style
    // list shelf, card shelves with the same card sizes), shimmering.
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(74, 92, 68, 104, 86, 78, 96, 70).take(if (compact) 4 else 8).forEach { width ->
                Box(Modifier.width(width.dp).height(36.dp).clip(RoundedCornerShape(22.dp)).loadingShimmer())
            }
        }
        HomeShelfSkeleton(compact, list = true)
        HomeShelfSkeleton(compact, list = false)
        HomeShelfSkeleton(compact, list = false)
    }
}

/** One home shelf's shape (title, then cards or list panels as [HomeShelfSection] lays them out). */
@Composable
private fun HomeShelfSkeleton(compact: Boolean, list: Boolean) {
    Column {
        Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(if (list) 140.dp else 210.dp).height(20.dp).clip(RoundedCornerShape(6.dp)).loadingShimmer())
        }
        Spacer(Modifier.height(10.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().clip(androidx.compose.ui.graphics.RectangleShape)) {
            if (list) {
                val panelWidth = if (compact) (maxWidth * 0.75f).coerceIn(240.dp, 320.dp)
                else adaptivePanelWidth(maxWidth, (maxWidth * 0.42f).coerceIn(240.dp, 360.dp), 16.dp, 5)
                val panels = ((maxWidth + 16.dp) / (panelWidth + 16.dp)).toInt().coerceAtLeast(1) + 1
                Row(Modifier.wrapContentWidth(Alignment.Start, unbounded = true), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(panels) {
                        Column(Modifier.width(panelWidth), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(4) {
                                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(8.dp)).loadingShimmer())
                                    Column(Modifier.padding(horizontal = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        Box(Modifier.fillMaxWidth(0.7f).height(13.dp).clip(RoundedCornerShape(5.dp)).loadingShimmer())
                                        Box(Modifier.fillMaxWidth(0.45f).height(10.dp).clip(RoundedCornerShape(5.dp)).loadingShimmer())
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val cardWidth = if (compact) 128.dp else adaptiveCardWidth(maxWidth, 160.dp)
                val cards = ((maxWidth + 12.dp) / (cardWidth + 12.dp)).toInt().coerceAtLeast(1) + 1
                Row(Modifier.wrapContentWidth(Alignment.Start, unbounded = true), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(cards) {
                        Column(Modifier.width(cardWidth)) {
                            Box(Modifier.size(cardWidth).clip(RoundedCornerShape(16.dp)).loadingShimmer())
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.fillMaxWidth(0.8f).height(13.dp).clip(RoundedCornerShape(5.dp)).loadingShimmer())
                            Spacer(Modifier.height(6.dp))
                            Box(Modifier.fillMaxWidth(0.55f).height(10.dp).clip(RoundedCornerShape(5.dp)).loadingShimmer())
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
private fun Modifier.smoothHorizontalScroll(state: LazyListState): Modifier = this
    .pointerInput(state) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            state.dispatchRawDelta(-dragAmount.x * 1.25f)
        }
    }
    .onPointerEvent(PointerEventType.Scroll) { event ->
        val change = event.changes.firstOrNull() ?: return@onPointerEvent
        val deltaY = change.scrollDelta.y
        val deltaX = change.scrollDelta.x
        val scrollAmount = if (kotlin.math.abs(deltaX) > kotlin.math.abs(deltaY)) deltaX else deltaY
        if (scrollAmount != 0f) {
            state.dispatchRawDelta(scrollAmount * 35f)
        }
    }

@Composable
private fun HomeShelfSection(shelf: HomeShelf, compact: Boolean, onTrackMore: (Track) -> Unit, onPlayNext: (Track) -> Unit, onItemClick: (HomeItem, List<HomeItem>) -> Unit) {
    if (shelf.items.isEmpty()) return
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(shelf.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            shelf.items.firstOrNull { it.track != null }?.let { playable ->
                TextButton(onClick = { onItemClick(playable, shelf.items) }) {
                    Text("Play all", color = Accent, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (shelf.layout == HomeShelfLayout.List) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val panelWidth = if (compact) (maxWidth * 0.75f).coerceIn(240.dp, 320.dp) else adaptivePanelWidth(maxWidth, (maxWidth * 0.42f).coerceIn(240.dp, 360.dp), 16.dp, (shelf.items.size + 3) / 4)
                val rowState = androidx.compose.foundation.lazy.rememberLazyListState()
                LazyRow(
                    state = rowState,
                    modifier = Modifier.smoothHorizontalScroll(rowState),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(shelf.items.chunked(4)) { panel ->
                        Column(Modifier.width(panelWidth), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            panel.forEach { item -> HomeListRow(item, shelf.items, onTrackMore, onPlayNext, onItemClick) }
                        }
                    }
                }
            }
        } else {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val cardWidth = if (compact) 128.dp else adaptiveCardWidth(maxWidth, 160.dp)
                val rowState = androidx.compose.foundation.lazy.rememberLazyListState()
                LazyRow(
                    state = rowState,
                    modifier = Modifier.smoothHorizontalScroll(rowState),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(shelf.items) { item -> HomeCard(item, cardWidth, shelf.items, onTrackMore, onItemClick) }
                }
            }
        }
    }
}



@Composable
private fun HomeMoodFilters(chips: List<HomeChip>, selected: HomeChip?, onSelect: (HomeChip) -> Unit) {
    val scrollState = rememberScrollState()
    Row(
        Modifier.fillMaxWidth().horizontalScroll(scrollState).dragHorizontally(scrollState).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { chip ->
            Surface(
                modifier = Modifier.clickable { onSelect(chip) },
                color = if (selected?.params == chip.params) Accent else Elevated,
                shape = RoundedCornerShape(22.dp),
            ) {
                Text(chip.title, color = if (selected?.params == chip.params) Color.Black else SecondaryText, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HomeCard(item: HomeItem, width: Dp, sourceItems: List<HomeItem>, onTrackMore: (Track) -> Unit, onItemClick: (HomeItem, List<HomeItem>) -> Unit) {
    Column(
        Modifier.width(width)
            .onPointerEvent(PointerEventType.Press, PointerEventPass.Initial) { event ->
                if (event.buttons.isSecondaryPressed) item.track?.let(onTrackMore)
            }
            .clickable { onItemClick(item, sourceItems) },
    ) {
        Artwork(item.artworkUrl, Modifier.size(width), RoundedCornerShape(16.dp))
        Spacer(Modifier.height(8.dp))
        if (item.track != null) TrackTitleText(item.track!!, color = Color.White, fontSize = 14.sp, maxLines = 1) else Text(item.title, color = Color.White, fontSize = 14.sp, maxLines = 1)
        if (item.track != null) ExplicitArtistText(item.track!!, item.subtitle, color = SecondaryText, fontSize = 12.sp) else Text(item.subtitle, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun HomeListRow(item: HomeItem, sourceItems: List<HomeItem>, onTrackMore: (Track) -> Unit, onPlayNext: (Track) -> Unit, onItemClick: (HomeItem, List<HomeItem>) -> Unit) {
    val track = item.track
    Row(
            Modifier.fillMaxWidth().background(Background)
                .clickable { onItemClick(item, sourceItems) }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(item.artworkUrl, Modifier.size(54.dp), RoundedCornerShape(8.dp))
            Column(Modifier.padding(horizontal = 12.dp).weight(1f)) {
                if (item.track != null) TrackTitleText(item.track!!, color = Color.White, fontSize = 14.sp, maxLines = 1) else Text(item.title, color = Color.White, fontSize = 14.sp, maxLines = 1)
                if (item.track != null) ExplicitArtistText(item.track!!, item.subtitle, color = SecondaryText, fontSize = 12.sp) else Text(item.subtitle, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
            }
            track?.let {
                IconButton(onClick = { onTrackMore(it) }) { Icon(Icons.Default.MoreVert, "More", tint = SecondaryText) }
            }
        }
    }

@Composable
private fun SearchPageLegacy(compact: Boolean, repository: WindowsMusicRepository, onTrack: (Track) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            isLoading = false
        } else {
            delay(350)
            isLoading = true
            results = runCatching { repository.search(query).tracks }.getOrDefault(emptyList())
            isLoading = false
        }
    }
    Column(Modifier.fillMaxSize()) {
        if (!compact) Spacer(Modifier.height(56.dp))
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().trackTextInputFocus(),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text("What songs for today?") },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = SurfaceColor,
                focusedIndicatorColor = Accent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
        Column(Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
        Spacer(Modifier.height(18.dp))
        if (query.isBlank()) {
            Text("Browse all", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            val browseState = rememberScrollState()
            Row(Modifier.horizontalScroll(browseState).dragHorizontally(browseState), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Songs", "Albums", "Artists", "Playlists").forEach { ContentCard(it, if (compact) 128.dp else 160.dp) }
            }
        } else if (isLoading) {
            Text("Searching…", color = SecondaryText, fontSize = 16.sp)
        } else if (results.isEmpty()) {
            Text("No results", color = SecondaryText, fontSize = 16.sp)
        } else {
            Text("Results for $query", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (results.isNotEmpty()) {
                    items(results) { track ->
                        SearchResultTrackRow(track, onClick = { onTrack(track) })
                    }
                        /*
                        SearchResultRow("${track.title} — ${track.artist}", onClick = { onTrack(track) })
                    }
                        */
                } else {
                items(listOf("$query — song", "$query — album", "$query — artist")) { result ->
                    SearchResultRow(result)
                }
                }
            }
        }
        }
    }
}

@Composable
private fun SearchPage(
    compact: Boolean,
    repository: WindowsMusicRepository,
    history: MutableList<String>,
    searchQueryState: MutableState<String>,
    searchFilterState: MutableState<String>,
    searchResultsState: MutableState<SearchResults?>,
    searchSubmitGeneration: MutableState<Pair<Long, String>>,
    onOpenCatalog: (String, String, String?, String?, String?) -> Unit,
    onTrackMore: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onTrack: (Track, List<Track>) -> Unit,
) {
    var query by searchQueryState
    var filter by searchFilterState
    var results by searchResultsState
    var suggestions by remember { mutableStateOf(emptyList<String>()) }
    var songSuggestions by remember { mutableStateOf(emptyList<Track>()) }
    var suggestionCatalog by remember { mutableStateOf(SearchResults()) }
    var submittedQuery by remember {
        mutableStateOf<String?>(searchQueryState.value.takeIf { searchResultsState.value != null && it.isNotBlank() })
    }
    var loadedQuery by remember { mutableStateOf(searchQueryState.value.takeIf { searchResultsState.value != null }) }
    var loadedFilter by remember { mutableStateOf(searchFilterState.value.takeIf { searchResultsState.value != null }) }

    LaunchedEffect(searchSubmitGeneration.value.first) {
        val submitted = searchSubmitGeneration.value.second.trim()
        if (searchSubmitGeneration.value.first > 0L && submitted.isNotBlank()) {
            if (submittedQuery != submitted || loadedFilter != filter) {
                // Results can still hold query A after returning Home. Do not
                // relabel that object as query B before the new request runs.
                results = null
                loadedQuery = null
                loadedFilter = null
            }
            submittedQuery = submitted
        }
    }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            suggestions = emptyList()
        } else {
            delay(150)
            try {
                val res = repository.searchSuggestions(query)
                suggestions = res
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }
        }
    }
    LaunchedEffect(query) {
        if (query.isBlank()) {
            songSuggestions = emptyList()
        } else {
            delay(180)
            try {
                val res = repository.search(query, "Songs").tracks.take(5)
                songSuggestions = res
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }
        }
    }
    LaunchedEffect(query) {
        if (query.isBlank()) {
            suggestionCatalog = SearchResults()
        } else {
            delay(120)
            try {
                // Songs come from this page's own Songs search (songSuggestions).
                val res = repository.searchSuggestionItems(query, includeSongs = false)
                suggestionCatalog = res
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }
        }
    }
    LaunchedEffect(submittedQuery, filter) {
        val submitted = submittedQuery ?: return@LaunchedEffect
        // Returning from the full player must restore the existing result page,
        // including its scroll/content, without firing the same network search.
        if (results != null && loadedQuery == submitted && loadedFilter == filter) return@LaunchedEffect
        results = null
        val found = runCatching { repository.search(submitted, filter) }.getOrNull()
        if (submittedQuery == submitted) {
            results = found
            loadedQuery = submitted
            loadedFilter = filter
            // Settings > Privacy > Pause search history keeps new searches out of the list.
            if (found != null && (found.tracks.isNotEmpty() || found.summaries.isNotEmpty()) && !com.recordlabs.desktop.data.AppSettings.pauseSearchHistory.value) {
                history.remove(submitted)
                history.add(submitted)
                while (history.size > 20) history.removeAt(0)
                SearchHistoryStore.save(history)
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (!compact) Spacer(Modifier.height(56.dp))
        if (compact) {
        Row(Modifier.fillMaxWidth().background(SurfaceColor), verticalAlignment = Alignment.CenterVertically) {
            if (query.isNotBlank()) {
                IconButton(onClick = { query = ""; results = null; submittedQuery = null; filter = "All" }) {
                    Icon(Icons.Default.ArrowBack, "Back to search history", tint = Color.White)
                }
            }
            TextField(
                value = query,
                onValueChange = {
                    query = it
                    filter = "All"
                    submittedQuery = null
                    results = null
                },
                modifier = Modifier.weight(1f).trackTextInputFocus().onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter && query.isNotBlank()) {
                        submittedQuery = query
                        true
                    } else false
                },
                leadingIcon = {
                    IconButton(
                        onClick = { if (query.isNotBlank()) submittedQuery = query },
                        enabled = query.isNotBlank(),
                    ) { Icon(Icons.Default.Search, "Search") }
                },
                placeholder = { Text("What songs for today?") },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = SurfaceColor,
                    focusedIndicatorColor = Accent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
    }
}
        
        val currentResults = results
        if (submittedQuery != null && currentResults?.filters?.isNotEmpty() == true) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentResults.filters.forEach { chip ->
                    LibraryFilterChip(chip, filter == chip) { filter = chip }
                }
            }
        }
        
        if (query.isBlank()) {
            val historyListState = rememberLazyListState()
            LazyColumn(
                Modifier.fillMaxSize().pageScrollbar(historyListState).padding(horizontal = 10.dp, vertical = 12.dp),
                state = historyListState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (history.isNotEmpty()) {
                    item { Text("Recent searches", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(history.asReversed()) { term ->
                        Row(
                            Modifier.fillMaxWidth().clickable { query = term; filter = "All"; submittedQuery = term }.padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Search, null, tint = SecondaryText)
                            Text(term, color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(start = 14.dp).weight(1f))
                            IconButton(
                                onClick = {
                                    history.remove(term)
                                    SearchHistoryStore.save(history)
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Default.Close, "Remove $term from search history", tint = SecondaryText, modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                }
            }
        } else if (submittedQuery != null && currentResults == null) {
            SearchResultsSkeleton()
        } else if (submittedQuery != null && currentResults != null) {
            val resultsListState = rememberLazyListState()
            LazyColumn(
                Modifier.fillMaxSize().pageScrollbar(resultsListState).padding(horizontal = 10.dp, vertical = 12.dp),
                state = resultsListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (filter == "All") {
                    val songsSummary = currentResults.summaries.firstOrNull { it.title.equals("Songs", true) }
                    val topSummary = currentResults.summaries.firstOrNull { it.title.contains("Top", true) }
                    val primarySongs = songsSummary?.items.orEmpty().ifEmpty { currentResults.tracks }
                    if (primarySongs.isNotEmpty()) {
                        item { Text("Songs", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp)) }
                        items(primarySongs.take(12), key = Track::id) { track ->
                            SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }, onClick = { onTrack(track, primarySongs) })
                        }
                    }
                    topSummary?.takeIf { it.items.isNotEmpty() }?.let { summary ->
                        item { Text("Top result", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                        items(summary.items) { track ->
                            SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }, onClick = { onTrack(track, summary.items) })
                        }
                    }
                    val artistMatch = currentResults.artists.firstOrNull { it.title.equals(query, ignoreCase = true) }
                        ?: currentResults.artists.firstOrNull()
                    val albumMatch = currentResults.albums.firstOrNull { it.title.equals(query, ignoreCase = true) }
                        ?: currentResults.albums.firstOrNull()
                    if (artistMatch != null) {
                        item {
                            SearchEntityRow("Artist", artistMatch.title, artistMatch.artworkUrl, Icons.Default.Person) {
                                onOpenCatalog(artistMatch.title, "Artist", artistMatch.browseId, null, null)
                            }
                        }
                    }
                    if (albumMatch != null) {
                        item {
                            SearchEntityRow("Album", albumMatch.title, albumMatch.artworkUrl, Icons.Default.Album) {
                                onOpenCatalog(albumMatch.title, "Album", albumMatch.browseId, null, null)
                            }
                        }
                    }
                    val additionalArtists = currentResults.artists.filterNot { it.browseId == artistMatch?.browseId }.take(8)
                    if (additionalArtists.isNotEmpty()) {
                        item { Text("Artists", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                        items(additionalArtists) { artist ->
                            SearchEntityRow("Artist", artist.title, artist.artworkUrl, Icons.Default.Person) {
                                onOpenCatalog(artist.title, "Artist", artist.browseId, null, null)
                            }
                        }
                    }
                    val additionalAlbums = currentResults.albums.filterNot { it.browseId == albumMatch?.browseId }.take(12)
                    if (additionalAlbums.isNotEmpty()) {
                        item { Text("Albums", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                        items(additionalAlbums) { album ->
                            SearchEntityRow("Album", album.title, album.artworkUrl, Icons.Default.Album) {
                                onOpenCatalog(album.title, "Album", album.browseId, null, null)
                            }
                        }
                    }
                    currentResults.summaries.filterNot { it === songsSummary || it === topSummary }.forEach { summary ->
                        item {
                            Text(summary.title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                        }
                        items(summary.items) { track ->
                            SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }, onClick = { onTrack(track, summary.items) })
                        }
                    }
                } else if (filter.equals("Artists", true)) {
                    items(currentResults.artists) { artist -> SearchEntityRow("Artist", artist.title, artist.artworkUrl, Icons.Default.Person) { onOpenCatalog(artist.title, "Artist", artist.browseId, null, null) } }
                } else if (filter.equals("Albums", true)) {
                    items(currentResults.albums) { album -> SearchEntityRow("Album", album.title, album.artworkUrl, Icons.Default.Album) { onOpenCatalog(album.title, "Album", album.browseId, null, null) } }
                } else {
                    items(currentResults.tracks) { track -> SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }, onClick = { onTrack(track, currentResults.tracks) }) }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        } else {
            if (query.isBlank() && history.isNotEmpty()) {
                val historyListState = rememberLazyListState()
                LazyColumn(Modifier.fillMaxSize().pageScrollbar(historyListState).padding(horizontal = 10.dp, vertical = 8.dp), state = historyListState, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    item { Text("Recent searches", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(start = 10.dp, bottom = 6.dp)) }
                    items(history.asReversed().take(10)) { term ->
                        Row(
                            Modifier.fillMaxWidth().clickable { submittedQuery = term; query = term; filter = "All"; results = null }.padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Search, null, tint = SecondaryText)
                            Text(term, color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(start = 14.dp).weight(1f))
                            IconButton(onClick = { onRemoveHistory(term) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, "Remove", tint = SecondaryText, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            } else if (query.isNotBlank() && suggestions.isEmpty() && songSuggestions.isEmpty() && suggestionCatalog.albums.isEmpty() && suggestionCatalog.artists.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            } else {
                val suggestionListState = rememberLazyListState()
                LazyColumn(
                    Modifier.fillMaxSize().pageScrollbar(suggestionListState).padding(horizontal = 10.dp, vertical = 8.dp),
                    state = suggestionListState,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(suggestions) { suggestion ->
                        Row(
                            Modifier.fillMaxWidth().clickable { query = suggestion; filter = "All"; submittedQuery = suggestion; results = null }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Search, null, tint = SecondaryText)
                            Text(suggestion, color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(start = 14.dp))
                        }
                    }
                    // Best match first (YouTube Music's top result), then songs
                    // (the ones matching the query on top), an artist and an album.
                    val rankedSongs = rankSongsForQuery(query, songSuggestions)
                    val top = suggestionTopResult(query, suggestionCatalog, rankedSongs)
                    val songs = rankedSongs.filterNot { it.id == top?.track?.id }
                    top?.let { best ->
                        item {
                            SuggestionTopResultRow(
                                best,
                                onTrack = { track -> onTrack(track, listOf(track)) },
                                onTrackMore = onTrackMore,
                                onPlayNext = onPlayNext,
                                onCatalog = { item, kind -> onOpenCatalog(item.title, kind, item.browseId, null, null) },
                            )
                        }
                    }
                    if (songs.isNotEmpty()) {
                        item { Text("Songs", color = SecondaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                        items(songs) { track ->
                            SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }) { onTrack(track, songs) }
                        }
                    }
                    suggestionCatalog.artists.firstOrNull { it.browseId != top?.item?.browseId }?.let { artist ->
                        item {
                            SearchEntityRow("Artist", artist.title, artist.artworkUrl, Icons.Default.Person) {
                                onOpenCatalog(artist.title, "Artist", artist.browseId, null, null)
                            }
                        }
                    }
                    suggestionCatalog.albums.firstOrNull { it.browseId != top?.item?.browseId }?.let { album ->
                        item {
                            SearchEntityRow("Album", album.title, album.artworkUrl, Icons.Default.Album) {
                                onOpenCatalog(album.title, "Album", album.browseId, null, null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsSkeleton() {
    LazyColumn(
        Modifier.fillMaxSize().background(Color.Black).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Box(Modifier.width(150.dp).height(22.dp).background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))) }
        items(8) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).background(Color.White.copy(alpha = 0.09f), RoundedCornerShape(9.dp)))
                Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.fillMaxWidth(0.55f).height(14.dp).background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(6.dp)))
                    Box(Modifier.fillMaxWidth(0.32f).height(11.dp).background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(6.dp)))
                }
            }
        }
    }
}

/**
 * Best match of the search suggestions: YouTube Music's own top result, else
 * an artist, song or album named exactly like the query.
 */
private fun suggestionTopResult(query: String, results: SearchResults, songs: List<Track> = results.tracks): SearchTopResult? =
    results.topResult?.let { top ->
        // The same song's row from the song list carries its full details.
        val id = top.track?.id ?: return@let top
        songs.firstOrNull { it.id == id }?.let { top.copy(track = it) } ?: top
    }
        ?: results.artists.firstOrNull { searchNameMatches(query, it.title) }?.let { SearchTopResult("Artist", item = it) }
        ?: songs.firstOrNull { searchNameMatches(query, it.title) }?.let { SearchTopResult("Song", track = it) }
        ?: results.albums.firstOrNull { searchNameMatches(query, it.title) }?.let { SearchTopResult("Album", item = it) }

@Composable
private fun SuggestionTopResultRow(
    top: SearchTopResult,
    onTrack: (Track) -> Unit,
    onTrackMore: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onCatalog: (CatalogItem, String) -> Unit,
) {
    val track = top.track
    val item = top.item
    if (track != null) {
        SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }) { onTrack(track) }
    } else if (item != null) {
        val icon = when (top.kind) {
            "Artist" -> Icons.Default.Person
            "Playlist" -> Icons.Default.QueueMusic
            else -> Icons.Default.Album
        }
        SearchEntityRow(top.kind, item.title, item.artworkUrl, icon) { onCatalog(item, top.kind) }
    }
}

@Composable
private fun SearchEntityRow(kind: String, name: String, artworkUrl: String?, icon: ImageVector, onClick: () -> Unit) {
    val glass = LocalGlassListRows.current
    Row(
        Modifier.fillMaxWidth().let { if (glass) it.listRowContainer() else it }.clickable(onClick = onClick).padding(horizontal = if (glass) 10.dp else 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (artworkUrl.isNullOrBlank()) {
            Box(Modifier.size(54.dp).background(Elevated, if (kind == "Artist") CircleShape else RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Accent)
            }
        } else Artwork(artworkUrl, Modifier.size(54.dp), if (kind == "Artist") CircleShape else RoundedCornerShape(8.dp))
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(kind, color = SecondaryText, fontSize = 13.sp)
        }
        Icon(Icons.Default.ArrowBack, null, tint = SecondaryText, modifier = Modifier.graphicsLayer { rotationZ = 180f })
    }
}

@Composable
private fun LibraryPage(
    compact: Boolean,
    requestedTarget: MutableState<String?>,
    library: WindowsLibrarySnapshot,
    onCreatePlaylist: (String) -> Boolean,
    onTrack: (Track, List<Track>) -> Unit,
    onTrackMore: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onNavigate: (String?) -> Unit,
    onBack: () -> Boolean,
    onOpenRemotePlaylist: (WindowsPlaylist) -> Unit,
) {
    // Every Library page change goes through [onNavigate], so Back/Forward
    // move between Library pages (Library > Liked > Back = Library).
    val selected = requestedTarget.value ?: LIBRARY_HUB
    var query by remember(selected) { mutableStateOf("") }
    var localTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var localLoading by remember { mutableStateOf(false) }
    var localRefresh by remember { mutableStateOf(0) }
    LaunchedEffect(selected, localRefresh) {
        if (selected != "Local Music") return@LaunchedEffect
        localLoading = true
        localTracks = withContext(Dispatchers.IO) { WindowsLocalMusicLibrary.scan() }
        localLoading = false
    }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    val playlist = selected.removePrefixOrNull(LIBRARY_PLAYLIST_PREFIX)?.let { name -> library.playlists.firstOrNull { it.name == name } }
    // Worked out again only when the library itself changes (not per keystroke).
    val savedTracks = remember(library) { (library.liked + library.playlists.flatMap { it.tracks } + library.history).distinctBy(Track::id) }
    val allKnownTracks = remember(savedTracks, library, localTracks) { (savedTracks + library.listenedTracks.map { it.track } + localTracks).distinctBy(Track::id) }
    val downloadedTracks = remember(allKnownTracks, com.recordlabs.desktop.data.AppDataEvents.storageGeneration) { allKnownTracks.filter { WindowsAudioCache.isDownloaded(it.id) } }
    // Cache files are checked off the UI thread.
    val cachedTracks by produceState(emptyList<Track>(), allKnownTracks, com.recordlabs.desktop.data.AppDataEvents.storageGeneration) {
        value = withContext(Dispatchers.IO) { allKnownTracks.filter { WindowsAudioCache.isCached(it.id) } }
    }
    // Same periods as the Android app: all time (top 50), the last month and
    // the last week, ranked by real listening time.
    val now = remember(selected, library) { java.time.ZonedDateTime.now() }
    val topTracks = remember(library) { library.mostPlayed(50) }
    val monthlyTracks = remember(library, now) { library.mostPlayed(sinceMs = now.minusMonths(1).toInstant().toEpochMilli()) }
    val weeklyTracks = remember(library, now) { library.mostPlayed(sinceMs = now.minusWeeks(1).toInstant().toEpochMilli()) }
    val albumGroups = remember(allKnownTracks) { allKnownTracks.filter { it.album.isNotBlank() }.groupBy { it.album } }
    val artistGroups = remember(allKnownTracks) { allKnownTracks.filter { it.artist.isNotBlank() && it.artist != "YouTube Music" }.groupBy { it.artist } }
    val albumName = selected.removePrefixOrNull(LIBRARY_ALBUM_PREFIX)
    val artistName = selected.removePrefixOrNull(LIBRARY_ARTIST_PREFIX)

    fun matches(track: Track) = query.isBlank() || track.title.contains(query, true) ||
        track.artist.contains(query, true) || track.album.contains(query, true)

    val collectionTracks: List<Track>? = when {
        selected == "Liked" -> library.liked
        selected == "Downloaded" -> downloadedTracks
        selected == "Cached" -> cachedTracks
        selected == "My Top 50" -> topTracks
        selected == "Monthly Most" -> monthlyTracks
        selected == "Weekly Most" -> weeklyTracks
        selected == "Local Music" -> localTracks
        selected == "Songs" -> savedTracks
        albumName != null -> albumGroups[albumName].orEmpty()
        artistName != null -> artistGroups[artistName].orEmpty()
        playlist != null -> playlist.tracks
        else -> null
    }
    val shownTracks = collectionTracks?.filter(::matches).orEmpty()
    val chipViews = listOf("Playlists", "Songs", "Albums", "Artists", "Podcasts")
    val showChips = selected == LIBRARY_HUB || selected in chipViews
    val ranked = selected == "My Top 50" || selected == "Monthly Most" || selected == "Weekly Most"

    fun openPlaylist(item: WindowsPlaylist) {
        if (item.browseId != null && item.tracks.isEmpty()) onOpenRemotePlaylist(item)
        else onNavigate(LIBRARY_PLAYLIST_PREFIX + item.name)
    }
    fun play(list: List<Track>, shuffle: Boolean = false) {
        val ordered = if (shuffle) list.shuffled() else list
        ordered.firstOrNull()?.let { onTrack(it, ordered) }
    }
    fun addLocalFolder() {
        val chooser = javax.swing.JFileChooser().apply {
            fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Add local music folder"
        }
        if (chooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
            WindowsLocalMusicLibrary.addFolder(chooser.selectedFile)
            localRefresh++
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = if (compact) 12.dp else 20.dp)) {
            // Clear of the floating header bar (back/home/search) on wide windows.
            Spacer(Modifier.height(if (compact) 8.dp else 76.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when {
                        selected == LIBRARY_HUB -> "Library"
                        selected in chipViews -> selected
                        else -> ""
                    },
                    color = Color.White,
                    fontSize = if (selected == LIBRARY_HUB) 28.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (!compact) LibrarySearchField(query, { query = it }, Modifier.width(320.dp))
            }
            if (compact) LibrarySearchField(query, { query = it }, Modifier.fillMaxWidth().padding(bottom = 12.dp))
            if (showChips) {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chipViews.forEach { name ->
                        // The chosen chip again returns to the Library hub.
                        LibraryFilterChip(name, selected == name) { onNavigate(if (selected == name) null else name) }
                    }
                }
            }

            // Each Library page keeps its own scroll position.
            val listState = remember(selected) { androidx.compose.foundation.lazy.LazyListState() }
            when {
                selected == LIBRARY_HUB -> LazyColumn(Modifier.fillMaxSize().pageScrollbar(listState), state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        val rowState = rememberLazyListState()
                        LazyRow(state = rowState, modifier = Modifier.smoothHorizontalScroll(rowState), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            item { LibraryHubTile("Liked", songCount(library.liked.size), Icons.Default.Favorite, Color(0xFFE36B7C)) { onNavigate("Liked") } }
                            item { LibraryHubTile("Downloaded", songCount(downloadedTracks.size), Icons.Default.Download, Color(0xFF7B93A8)) { onNavigate("Downloaded") } }
                            item { LibraryHubTile("Cached", songCount(cachedTracks.size), Icons.Default.Refresh, Color(0xFF66737F)) { onNavigate("Cached") } }
                            item { LibraryHubTile("My Top 50", if (topTracks.isEmpty()) "Most played" else songCount(topTracks.size), Icons.Default.StarBorder, Color(0xFF536779)) { onNavigate("My Top 50") } }
                            item { LibraryHubTile("Monthly Most", if (monthlyTracks.isEmpty()) "Last 30 days" else songCount(monthlyTracks.size), Icons.Default.QueueMusic, Color(0xFF3B4C5A)) { onNavigate("Monthly Most") } }
                            item { LibraryHubTile("Weekly Most", if (weeklyTracks.isEmpty()) "Last 7 days" else songCount(weeklyTracks.size), Icons.Default.QueueMusic, Color(0xFF263238)) { onNavigate("Weekly Most") } }
                            item { LibraryHubTile("Local Music", "MP3, FLAC & more", Icons.Default.MusicNote, Color(0xFF455A64)) { onNavigate("Local Music") } }
                        }
                    }
                    val playlists = library.playlists.filter { query.isBlank() || it.name.contains(query, true) }
                    item { LibrarySectionTitle("Playlists") }
                    if (playlists.isEmpty()) {
                        item { Text(if (query.isBlank()) "No playlists yet. Use + to create one." else "No playlists match \"$query\".", color = SecondaryText, fontSize = 13.sp) }
                    }
                    items(playlists) { item -> LibraryPlaylistRow(item) { openPlaylist(item) } }
                    if (query.isNotBlank()) {
                        val songs = savedTracks.filter(::matches)
                        item { LibrarySectionTitle("Songs") }
                        if (songs.isEmpty()) item { Text("No songs match \"$query\".", color = SecondaryText, fontSize = 13.sp) }
                        items(songs) { track -> SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }) { onTrack(track, songs) } }
                    }
                    item { Spacer(Modifier.height(120.dp)) }
                }
                selected == "Playlists" -> {
                    val playlists = library.playlists.filter { query.isBlank() || it.name.contains(query, true) }
                    if (playlists.isEmpty()) {
                        LibraryEmptyState(
                            Icons.Default.QueueMusic,
                            if (query.isBlank()) "No playlists yet" else "No playlists match \"$query\"",
                            if (query.isBlank()) "Create one with the + button." else "",
                        )
                    } else LazyColumn(Modifier.fillMaxSize().pageScrollbar(listState), state = listState, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(playlists) { item -> LibraryPlaylistRow(item) { openPlaylist(item) } }
                        item { Spacer(Modifier.height(120.dp)) }
                    }
                }
                selected == "Albums" || selected == "Artists" -> {
                    val albums = selected == "Albums"
                    val groups = (if (albums) albumGroups else artistGroups).entries
                        .filter { query.isBlank() || it.key.contains(query, true) || (albums && it.value.any { track -> track.artist.contains(query, true) }) }
                        .sortedBy { it.key.lowercase() }
                    if (groups.isEmpty()) {
                        LibraryEmptyState(
                            if (albums) Icons.Default.Album else Icons.Default.Person,
                            if (query.isNotBlank()) "Nothing matches \"$query\"" else if (albums) "No albums yet" else "No artists yet",
                            if (query.isNotBlank()) "" else "Songs you like, play or add to playlists are grouped here.",
                        )
                    } else {
                        val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(if (compact) 140.dp else 160.dp),
                            state = gridState,
                            modifier = Modifier.fillMaxSize().pageScrollbar(gridState),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp),
                        ) {
                            gridItems(groups, key = { it.key }) { entry ->
                                val art = entry.value.firstNotNullOfOrNull { it.artworkUrl?.takeIf(String::isNotBlank) }
                                Column(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                        .clickable { onNavigate((if (albums) LIBRARY_ALBUM_PREFIX else LIBRARY_ARTIST_PREFIX) + entry.key) }
                                        .padding(4.dp),
                                    horizontalAlignment = if (albums) Alignment.Start else Alignment.CenterHorizontally,
                                ) {
                                    if (albums) Artwork(art, Modifier.fillMaxWidth().aspectRatio(1f), RoundedCornerShape(10.dp))
                                    else Artwork(art, Modifier.fillMaxWidth().aspectRatio(1f), CircleShape)
                                    Text(
                                        entry.key, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2,
                                        lineHeight = 18.sp, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = if (albums) androidx.compose.ui.text.style.TextAlign.Start else androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                    val albumArtist = if (albums) entry.value.groupingBy { it.artist }.eachCount().maxByOrNull { it.value }?.key.orEmpty() else ""
                                    Text(
                                        listOf(albumArtist, songCount(entry.value.size)).filter(String::isNotBlank).joinToString(" • "),
                                        color = SecondaryText, fontSize = 12.sp, maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
                selected == "Podcasts" -> LibraryEmptyState(Icons.Default.Radio, "No podcasts saved yet", "Saved podcast episodes will show up here.")
                selected == "Local Music" && collectionTracks.isNullOrEmpty() -> {
                    if (localLoading) {
                        Box(Modifier.fillMaxWidth().padding(top = 56.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Accent) }
                    } else {
                        Column(Modifier.fillMaxWidth().padding(top = 44.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.MusicNote, null, tint = Accent, modifier = Modifier.size(50.dp))
                            Text("No local music found", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Text("Record Labs scans your Music folder automatically.", color = SecondaryText)
                            GlassButton(onClick = ::addLocalFolder) { Text("Add folder") }
                        }
                    }
                }
                collectionTracks != null -> LazyColumn(Modifier.fillMaxSize().pageScrollbar(listState), state = listState, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (selected != "Songs") item {
                        val headerArt = collectionTracks.firstNotNullOfOrNull { it.artworkUrl?.takeIf(String::isNotBlank) }
                        val (icon, color) = when (selected) {
                            "Liked" -> Icons.Default.Favorite to Color(0xFFE36B7C)
                            "Downloaded" -> Icons.Default.Download to Color(0xFF7B93A8)
                            "Cached" -> Icons.Default.Refresh to Color(0xFF66737F)
                            "My Top 50" -> Icons.Default.StarBorder to Color(0xFF536779)
                            "Monthly Most" -> Icons.Default.QueueMusic to Color(0xFF3B4C5A)
                            "Weekly Most" -> Icons.Default.QueueMusic to Color(0xFF263238)
                            "Local Music" -> Icons.Default.MusicNote to Color(0xFF455A64)
                            else -> Icons.Default.QueueMusic to Elevated
                        }
                        val title = albumName ?: artistName ?: playlist?.name ?: selected
                        val subtitle = when (selected) {
                            "My Top 50" -> "Your most played songs"
                            "Monthly Most" -> "Most played in the last month"
                            "Weekly Most" -> "Most played in the last 7 days"
                            "Cached" -> "Played recently and ready to start instantly"
                            "Local Music" -> "${WindowsLocalMusicLibrary.folders().size} folders"
                            else -> if (albumName != null) collectionTracks.groupingBy { it.artist }.eachCount().maxByOrNull { it.value }?.key.orEmpty()
                            else if (playlist != null) "Playlist" else ""
                        }
                        LibraryCollectionHeader(
                            compact = compact,
                            title = title,
                            subtitle = listOf(subtitle, songCount(collectionTracks.size)).filter(String::isNotBlank).joinToString(" • "),
                            artworkUrl = headerArt.takeIf { albumName != null || artistName != null || playlist != null },
                            circleArtwork = artistName != null,
                            icon = icon,
                            color = color,
                            canPlay = collectionTracks.isNotEmpty(),
                            onPlay = { play(shownTracks.ifEmpty { collectionTracks }) },
                            onShuffle = { play(shownTracks.ifEmpty { collectionTracks }, shuffle = true) },
                        ) {
                            if (selected == "Local Music") {
                                GlassButton(onClick = ::addLocalFolder) { Text("Add folder") }
                                IconButton(onClick = { localRefresh++ }) { Icon(Icons.Default.Refresh, "Rescan", tint = Color.White) }
                            }
                        }
                    } else if (collectionTracks.isNotEmpty()) item {
                        Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            GlassButton(onClick = { play(shownTracks) }, tint = Accent) {
                                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Play")
                            }
                            GlassButton(onClick = { play(shownTracks, shuffle = true) }) {
                                Icon(Icons.Default.Shuffle, null, tint = Color.White); Spacer(Modifier.width(6.dp)); Text("Shuffle", color = Color.White)
                            }
                            Text(songCount(shownTracks.size), color = SecondaryText, fontSize = 13.sp)
                        }
                    }
                    if (shownTracks.isEmpty()) item {
                        val (heading, hint) = when {
                            query.isNotBlank() -> "No songs match \"$query\"" to ""
                            selected == "Liked" -> "No liked songs yet" to "Tap the heart on a song to save it here."
                            selected == "Downloaded" -> "No downloads yet" to "Use Download in a song's menu to keep it offline."
                            selected == "Cached" -> "Nothing cached yet" to "Songs you play are kept here for a few days."
                            ranked -> "No listening stats yet" to "Songs you listen to will be ranked here."
                            selected == "Songs" -> "No songs yet" to "Songs you like, play or add to playlists show up here."
                            playlist != null -> "This playlist is empty" to "Add songs with \"Add to playlist\" in a song's menu."
                            else -> "No songs here yet" to ""
                        }
                        LibraryEmptyState(Icons.Default.LibraryMusic, heading, hint)
                    }
                    itemsIndexed(shownTracks) { index, track ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (ranked && query.isBlank()) {
                                Text(
                                    (index + 1).toString(), color = SecondaryText, fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.width(26.dp),
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }) { onTrack(track, shownTracks) }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(120.dp)) }
                }
                else -> LibraryEmptyState(Icons.Default.LibraryMusic, "This page is no longer available", "It may have been renamed or removed.")
            }
        }
        if (selected == LIBRARY_HUB || selected == "Playlists") {
            FloatingActionButton(
                onClick = { showCreatePlaylist = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 104.dp),
                backgroundColor = Accent,
                contentColor = Color.Black,
            ) { Icon(Icons.Default.Add, "Create playlist") }
        }
        if (showCreatePlaylist) {
            AlertDialog(
                onDismissRequest = { showCreatePlaylist = false },
                title = { Text("Create playlist") },
                text = { TextField(value = playlistName, onValueChange = { playlistName = it }, placeholder = { Text("Playlist name") }, singleLine = true, modifier = Modifier.trackTextInputFocus()) },
                confirmButton = {
                    TextButton(onClick = {
                        if (onCreatePlaylist(playlistName)) {
                            val created = playlistName.trim()
                            playlistName = ""
                            showCreatePlaylist = false
                            onNavigate(LIBRARY_PLAYLIST_PREFIX + created)
                        }
                    }) { Text("Create") }
                },
                dismissButton = { TextButton(onClick = { showCreatePlaylist = false }) { Text("Cancel") } },
            )
        }
    }
}

private const val LIBRARY_HUB = "Library"
private const val LIBRARY_PLAYLIST_PREFIX = "playlist:"
private const val LIBRARY_ALBUM_PREFIX = "album:"
private const val LIBRARY_ARTIST_PREFIX = "artist:"

private fun String.removePrefixOrNull(prefix: String): String? = if (startsWith(prefix)) removePrefix(prefix) else null

private fun songCount(count: Int) = if (count == 1) "1 song" else "$count songs"

@Composable
private fun LibrarySearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.height(42.dp).background(SurfaceColor, RoundedCornerShape(21.dp)).padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, null, tint = SecondaryText, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) Text("Search library", color = SecondaryText, fontSize = 14.sp, maxLines = 1)
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Accent),
                modifier = Modifier.fillMaxWidth().trackTextInputFocus(),
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.Close, "Clear search", tint = SecondaryText, modifier = Modifier.size(18.dp))
            }
        } else Spacer(Modifier.width(10.dp))
    }
}

@Composable
private fun LibrarySectionTitle(text: String) {
    Text(text, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
}

@Composable
private fun LibraryPlaylistRow(item: WindowsPlaylist, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val art = item.tracks.firstNotNullOfOrNull { it.artworkUrl?.takeIf(String::isNotBlank) }
        if (art != null) {
            Artwork(art, Modifier.size(54.dp), RoundedCornerShape(8.dp))
        } else {
            Box(Modifier.size(54.dp).background(Elevated, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.QueueMusic, null, tint = SecondaryText)
            }
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(item.name, color = Color.White, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(
                if (item.browseId != null && item.tracks.isEmpty()) "YouTube Music playlist" else songCount(item.tracks.size),
                color = SecondaryText, fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun LibraryEmptyState(icon: ImageVector, title: String, hint: String) {
    Column(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (hint.isNotBlank()) Text(hint, color = SecondaryText, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun LibraryCollectionHeader(
    compact: Boolean,
    title: String,
    subtitle: String,
    artworkUrl: String?,
    circleArtwork: Boolean,
    icon: ImageVector,
    color: Color,
    canPlay: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    extraActions: @Composable RowScope.() -> Unit = {},
) {
    val coverSize = if (compact) 96.dp else 128.dp
    Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (artworkUrl != null) {
            Artwork(artworkUrl, Modifier.size(coverSize), if (circleArtwork) CircleShape else RoundedCornerShape(14.dp))
        } else {
            Box(Modifier.size(coverSize).background(color, if (circleArtwork) CircleShape else RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(coverSize * 0.45f))
            }
        }
        Column(Modifier.padding(start = 20.dp).weight(1f)) {
            Text(
                title, color = Color.White, fontSize = if (compact) 22.sp else 28.sp, lineHeight = if (compact) 27.sp else 34.sp,
                fontWeight = FontWeight.Bold, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) Text(subtitle, color = SecondaryText, fontSize = 13.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (canPlay) {
                    IconButton(onClick = onPlay, modifier = Modifier.size(48.dp).background(Accent, CircleShape)) {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black)
                    }
                    IconButton(onClick = onShuffle, modifier = Modifier.size(42.dp).background(SurfaceColor, CircleShape)) {
                        Icon(Icons.Default.Shuffle, "Shuffle", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                extraActions()
            }
        }
    }
}

@Composable
private fun LibraryHubTile(label: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(Modifier.width(126.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick)) {
        Box(Modifier.size(126.dp).background(color, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White.copy(alpha = .85f), modifier = Modifier.size(58.dp))
        }
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 7.dp), maxLines = 1)
        Text(subtitle, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun LegacyLibraryPage(compact: Boolean) {
    var selectedFilter by remember { mutableStateOf("Library") }
    var gridView by remember { mutableStateOf(true) }
    var searchActive by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Recently added") }
    var openedContainer by remember { mutableStateOf<String?>(null) }
    val filters = listOf("Library", "Playlists", "Songs", "Albums", "Artists", "Podcasts")
    val content = when (selectedFilter) {
        "Playlists" -> listOf("Liked", "Offline", "My Top 50", "Uploaded playlists", "Community playlists", "Focus mix")
        "Songs" -> listOf("Recently liked", "Downloaded songs", "Recently played", "Most played", "New additions")
        "Albums" -> listOf("Recent albums", "Favorite albums", "Downloaded albums", "New releases")
        "Artists" -> listOf("Favorite artists", "Recently played artists", "Recommended artists")
        "Podcasts" -> listOf("New episodes", "Episodes for later", "Subscribed shows")
        else -> listOf("Liked", "Offline", "My Top 50", "Favorite albums", "Favorite artists", "Community playlists", "Recently added songs")
    }.filter { query.isBlank() || it.contains(query, ignoreCase = true) }

    openedContainer?.let { container ->
        LibraryContainerPage(container) { openedContainer = null }
        return
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
        PageHeader("Library", "")
        Column(Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
        val filterScrollState = rememberScrollState()
        Row(
            Modifier.horizontalScroll(filterScrollState).dragHorizontally(filterScrollState).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filters.forEach { filter ->
                LibraryFilterChip(filter, selectedFilter == filter) { selectedFilter = filter }
            }
        }

        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                GlassButton(onClick = { sortMenuOpen = true }) { Text("Sort: $sortOption") }
                DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                    listOf("Recently added", "A–Z", "Z–A").forEach { option ->
                        DropdownMenuItem(onClick = { sortOption = option; sortMenuOpen = false }) { Text(option) }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            if (searchActive) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.width(if (compact) 140.dp else 220.dp).trackTextInputFocus(),
                    placeholder = { Text("Search library") },
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = SurfaceColor,
                        focusedIndicatorColor = Accent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
            } else {
                IconButton(onClick = { searchActive = true }) { Icon(Icons.Default.Search, "Search library", tint = Color.White) }
            }
            IconButton(onClick = { gridView = !gridView }) {
                Icon(if (gridView) Icons.Default.List else Icons.Default.GridView, "Toggle library view", tint = Color.White)
            }
        }

        val sortedContent = when (sortOption) {
            "A–Z" -> content.sortedBy { it.lowercase() }
            "Z–A" -> content.sortedByDescending { it.lowercase() }
            else -> content
        }
        if (gridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (compact) 3 else 5),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                gridItems(sortedContent) { item -> LibraryCard(item) { openedContainer = item } }
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sortedContent) { item ->
                    LibraryListRow(item, selectedFilter) { openedContainer = item }
                }
            }
        }
        }

    }

        if (selectedFilter == "Playlists" || selectedFilter == "Library") {
            FloatingActionButton(
                onClick = { showCreatePlaylist = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                backgroundColor = Accent,
                contentColor = Color.Black,
            ) { Icon(Icons.Default.Add, "Create playlist") }
        }

        if (showCreatePlaylist) {
            AlertDialog(
                onDismissRequest = { showCreatePlaylist = false },
                title = { Text("Create playlist") },
                text = {
                    TextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        placeholder = { Text("Playlist name") },
                        singleLine = true,
                        modifier = Modifier.trackTextInputFocus(),
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showCreatePlaylist = false }) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylist = false }) { Text("Cancel") }
                },
            )
    }
}
}

@Composable
private fun LibraryFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Accent else SurfaceColor,
        contentColor = if (selected) Color.Black else Color.White,
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp), fontSize = 13.sp)
    }
}

@Composable
private fun LibraryCard(label: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(1f).background(Elevated, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                when {
                    label.contains("artist", ignoreCase = true) -> Icons.Default.Person
                    label.contains("playlist", ignoreCase = true) || label.contains("mix", ignoreCase = true) -> Icons.Default.QueueMusic
                    else -> Icons.Default.Album
                },
                null,
                tint = Accent,
                modifier = Modifier.size(42.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 14.sp, maxLines = 1)
        Text("Library", color = SecondaryText, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun LibraryListRow(label: String, filter: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).background(SurfaceColor, RoundedCornerShape(14.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(54.dp).background(Elevated, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(if (filter == "Artists") Icons.Default.Person else Icons.Default.Album, null, tint = Accent)
        }
        Column(Modifier.padding(horizontal = 12.dp).weight(1f)) {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(filter, color = SecondaryText, fontSize = 12.sp)
        }
        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "More", tint = SecondaryText) }
    }
}

@Composable
private fun LibraryContainerPage(title: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(65.dp).background(SurfaceColor).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            
            Text(title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Your $title music will appear here.", color = SecondaryText, fontSize = 15.sp)
        }
    }
}

@Composable
private fun PageHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        // Android uses a taller header in a full-width landscape surface and a shorter one in
        // phone-like portrait. These are maxima; Compose naturally scales the rest of the UI.
        val headerHeight = if (maxWidth < 700.dp) 65.dp else 75.dp
        Surface(
            modifier = Modifier.fillMaxWidth().height(headerHeight).padding(bottom = 8.dp),
            color = SurfaceColor,
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ContentCard(label: String, width: Dp) {
    Column(Modifier.width(width)) {
        Box(Modifier.size(width).background(Elevated, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Album, null, tint = Accent, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 14.sp, maxLines = 1)
        Text("Record Labs", color = SecondaryText, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun SearchResultRow(label: String, onClick: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(52.dp).background(Elevated, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Album, null, tint = Accent)
        }
        Column(Modifier.padding(horizontal = 12.dp).weight(1f)) {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text("Artist or album", color = SecondaryText, fontSize = 12.sp)
        }
        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, "More", tint = SecondaryText) }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SwipePlayNextContainer(
    modifier: Modifier = Modifier,
    onMore: () -> Unit,
    onPlayNext: (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    var dragPx by remember { mutableStateOf(0f) }
    // The gesture keeps running when the row is recomposed with a new callback.
    val latestOnPlayNext by rememberUpdatedState(onPlayNext)
    val density = LocalDensity.current
    val thresholdPx = with(density) { 76.dp.toPx() }
    val maxDragPx = with(density) { 150.dp.toPx() }
    val renderedDrag by animateFloatAsState(
        targetValue = dragPx,
        animationSpec = if (dragPx == 0f) spring(stiffness = 520f, dampingRatio = .82f) else snap(),
        label = "song-play-next-swipe",
    )
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .onPointerEvent(PointerEventType.Press, PointerEventPass.Initial) { event ->
                if (event.buttons.isSecondaryPressed) onMore()
            }
            .then(
                if (onPlayNext == null) Modifier else Modifier.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, amount ->
                            if (amount > 0f || dragPx > 0f) {
                                change.consume()
                                dragPx = (dragPx + amount).coerceIn(0f, maxDragPx)
                            }
                        },
                        onDragEnd = {
                            if (dragPx >= thresholdPx) latestOnPlayNext?.invoke()
                            dragPx = 0f
                        },
                        onDragCancel = { dragPx = 0f },
                    )
                },
            ),
    ) {
        // Only drawn while the row is pulled aside: a pill in the gap the row
        // leaves, filling up until the release point and then lit fully.
        if (onPlayNext != null && renderedDrag > 0.5f) {
            val progress = (renderedDrag / thresholdPx).coerceIn(0f, 1f)
            val armed = renderedDrag >= thresholdPx
            val gap = with(density) { renderedDrag.toDp() }
            Box(Modifier.matchParentSize()) {
            Row(
                Modifier
                    .width(gap)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colors.primary.copy(alpha = if (armed) .95f else .25f + .45f * progress)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.QueueMusic,
                    "Play next",
                    tint = if (armed) Color.Black else Color.White,
                    modifier = Modifier.size(22.dp).graphicsLayer {
                        val scale = .75f + .25f * progress
                        scaleX = scale
                        scaleY = scale
                    },
                )
                if (gap >= 122.dp) Text(
                    "Play next",
                    color = if (armed) Color.Black else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            }
        }
        // The pulled row gets a solid backing, so the pill never shows through see-through rows.
        Box(
            Modifier
                .graphicsLayer { translationX = renderedDrag }
                .then(if (renderedDrag > 0.5f) Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFF17191D)) else Modifier),
        ) { content() }
    }
}

@Composable
private fun SearchResultTrackRow(
    track: Track,
    swipeable: Boolean = true,
    onMore: () -> Unit = {},
    onPlayNext: (() -> Unit)? = null,
    /** Album pages: shown as "Artist • Album • Year" (the artist stays clickable). */
    albumDetails: Pair<String, String>? = null,
    onClick: () -> Unit,
) {
    val wrapper: @Composable (@Composable () -> Unit) -> Unit = if (swipeable) { { SwipePlayNextContainer(Modifier.fillMaxWidth(), onMore, onPlayNext, it) } } else { { it() } }
    wrapper {
        Row(
            Modifier.fillMaxWidth().listRowContainer().clickable(onClick = onClick).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(track.artworkUrl, Modifier.size(52.dp), RoundedCornerShape(8.dp))
            Column(Modifier.padding(horizontal = 12.dp).weight(1f)) {
                TrackTitleText(track, color = Color.White, fontSize = 14.sp, maxLines = 1)
                if (albumDetails != null) {
                    // "Artist • Album • Year": a long album name is shortened, the
                    // artist and the year always stay readable.
                    val album = albumDetails.first.takeIf { it.isNotBlank() }
                    val year = albumDetails.second.takeIf { it.isNotBlank() }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExplicitArtistText(track, color = SecondaryText, fontSize = 12.sp, modifier = Modifier.widthIn(max = 220.dp))
                        if (album != null) {
                            Text(
                                " • $album",
                                color = SecondaryText,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        }
                        if (year != null) Text(" • $year", color = SecondaryText, fontSize = 12.sp, maxLines = 1)
                    }
                } else {
                    ExplicitArtistText(track, listOf(track.artist, track.album).filter { it.isNotBlank() }.joinToString("   "), color = SecondaryText, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onMore) { Icon(Icons.Default.MoreVert, "More", tint = SecondaryText) }
        }
    }
}

@Composable
private fun SearchTrackMenu(
    track: Track,
    isGuest: Boolean,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    playlists: List<WindowsPlaylist>,
    onAddToPlaylist: (String) -> Unit,
    onStartRadio: () -> Unit,
    onShare: () -> Unit,
    onAddToLibrary: () -> Unit,
    onPin: () -> Unit,
    onDownload: () -> Unit,
    onViewArtist: () -> Unit,
    onDetails: () -> Unit,
) {
    var showPlaylistPicker by remember(track.id) { mutableStateOf(false) }
    if (showPlaylistPicker) {
        PlaylistPickerDialog(playlists, { showPlaylistPicker = false }) { playlist ->
            showPlaylistPicker = false
            onAddToPlaylist(playlist)
        }
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Artwork(track.artworkUrl, Modifier.size(48.dp), RoundedCornerShape(8.dp))
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    TrackTitleText(track, fontWeight = FontWeight.Bold, maxLines = 1)
                    ExplicitArtistText(track, color = SecondaryText, fontSize = 12.sp)
                }
                Icon(Icons.Default.FavoriteBorder, null, tint = Color.White)
            }
        },
        text = {
            Column(Modifier.width(520.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassButton(prominent = true, onClick = onPlayNext, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.QueueMusic, null); Spacer(Modifier.width(6.dp)); Text(if (isGuest) "Suggest to host" else "Play next")
                    }
                    GlassButton(onClick = { showPlaylistPicker = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Add to playlist")
                    }
                    GlassButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Share, null); Spacer(Modifier.width(6.dp)); Text("Share")
                    }
                }
                DropdownMenuItem(onClick = onStartRadio) { Icon(Icons.Default.Radio, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Start radio") }
                if (!isGuest) DropdownMenuItem(onClick = onPlayNext) { Icon(Icons.Default.QueueMusic, null, tint = Accent); Spacer(Modifier.width(12.dp)); Column { Text("Add to queue"); Text("Add to the end of requested songs", color = SecondaryText, fontSize = 11.sp) } }
                DropdownMenuItem(onClick = onPin) { Icon(Icons.Default.Add, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Pin to speed dial") }
                DropdownMenuItem(onClick = onAddToLibrary) { Icon(Icons.Default.LibraryMusic, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Add to library") }
                DropdownMenuItem(onClick = onDownload) { Icon(Icons.Default.Download, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Download") }
                DropdownMenuItem(onClick = onViewArtist) { Icon(Icons.Default.Person, null, tint = Accent); Spacer(Modifier.width(12.dp)); Column { Text("View artist"); ExplicitArtistText(track, color = SecondaryText, fontSize = 11.sp) } }
                DropdownMenuItem(onClick = onDetails) { Icon(Icons.Default.Info, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Details") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun CatalogPage(
    compact: Boolean,
    destination: CatalogDestination,
    repository: WindowsMusicRepository,
    onBack: () -> Unit,
    onTrack: (Track, List<Track>) -> Unit,
    onTrackMore: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onOpenCatalog: (String, String, String?, String?, String?) -> Unit,
) {
    var page by remember(destination) { mutableStateOf<CatalogPageData?>(globalCatalogCache[destination]) }
    var loading by remember(destination) { mutableStateOf(page == null) }
    LaunchedEffect(destination) {
        if (page != null) return@LaunchedEffect
        loading = true
        val isArtistPage = destination.kind == "Artist"
        // Artist pages are shown before their full Top songs list is in (it
        // follows below), and their page request is tried again on errors.
        suspend fun fetchPage(): CatalogPageData {
            val suppliedBrowseId = destination.browseId?.takeIf { id ->
                !destination.kind.equals("Album", true) ||
                    id.startsWith("MPRE", true) || id.startsWith("OLAK5uy_", true)
            }
            // A song's own album link beats a search by album name, which can
            // land on another album of the same artist.
            val trackAlbumId = if (suppliedBrowseId == null && destination.kind.equals("Album", true)) {
                destination.sourceTrack?.let { repository.albumBrowseIdForTrack(it.id, it.title, it.artist) }
            } else if (suppliedBrowseId == null && isArtistPage) {
                // The song's own artist link beats a search by name.
                destination.sourceTrack?.let { repository.artistBrowseIdForTrack(it.id, destination.title) }
            } else null
            val browseId = suppliedBrowseId ?: trackAlbumId ?: repository.search(destination.title, "${destination.kind}s").let { result ->
                val entries = if (destination.kind == "Artist") result.artists else result.albums
                entries.firstOrNull { it.title.equals(destination.title, true) }?.browseId
                    ?: entries.firstOrNull { it.title.contains(destination.title, true) }?.browseId
                    ?: entries.firstOrNull()?.browseId
            }
            if (browseId == null && isArtistPage) error("Artist not found (yet)")
            return if (browseId != null) repository.catalogPage(browseId, destination.kind, destination.params, expandArtistTopSongs = !isArtistPage)
            else {
                val fallbackTracks = repository.search(destination.title, "Songs").tracks
                CatalogPageData(destination.title, destination.kind, tracks = fallbackTracks)
            }
        }
        val loaded = if (isArtistPage) {
            // Keeps trying (1 s, 2 s, 4 s... up to 15 s apart) while the page is
            // open; an artist that is still not found after a few tries gets
            // the empty page as before.
            var attempt = 0
            var result: CatalogPageData? = null
            while (result == null) {
                result = runCatching { fetchPage() }
                    .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }
                    .getOrNull()
                    ?.takeIf { it.tracks.isNotEmpty() || it.shelves.isNotEmpty() || attempt >= 2 }
                if (result == null) {
                    attempt++
                    if (attempt >= 6 && destination.browseId == null) result = CatalogPageData(destination.title, destination.kind)
                    else delay((1_000L shl (attempt - 1).coerceAtMost(4)).coerceAtMost(15_000L))
                }
            }
            result
        } else runCatching { fetchPage() }.getOrElse {
            it.printStackTrace()
            CatalogPageData(destination.title, destination.kind)
        }
        // An empty "Show All" page is not kept: it can be a transient failure.
        if (!(destination.kind == "Shelf" && loaded.tracks.isEmpty() && loaded.albums.isEmpty())) {
            globalCatalogCache[destination] = loaded
        }
        page = loaded
        loading = false
    }
    // Artist pages: the full Top songs list loads after the page is shown,
    // trying again (with growing pauses) until it is in.
    var topSongsLoading by remember(destination) { mutableStateOf(false) }
    LaunchedEffect(destination, page != null) {
        val current = page ?: return@LaunchedEffect
        if (destination.kind != "Artist" || destination in expandedArtistPages) return@LaunchedEffect
        val shelf = current.shelves.firstOrNull { it.title.equals("Top songs", true) || it.title.equals("Lagu teratas", true) }
        val more = shelf?.moreEndpoint
        if (shelf == null || more == null) { expandedArtistPages += destination; return@LaunchedEffect }
        fun showTopSongs(full: List<Track>) {
            val latest = page ?: current
            val updated = latest.copy(
                tracks = full,
                shelves = latest.shelves.map { if (it.title == shelf.title) it.copy(items = full) else it },
            )
            val cached = globalCatalogCache[destination]
            if (cached == null || cached === latest || cached === current) globalCatalogCache[destination] = updated
            page = updated
        }
        // The full list saved on the last visit shows at once, even on a weak
        // connection; it is then refreshed below.
        val saved = repository.savedArtistTopSongs(more)
        if (saved != null && saved.size > current.tracks.size) showTopSongs(saved)
        topSongsLoading = saved == null
        try {
            var attempt = 0
            var emptyAnswers = 0
            while (true) {
                // Each try may take longer: 15 s, 30 s, then up to a minute.
                val timeoutMs = when (attempt) { 0 -> 15_000L; 1 -> 30_000L; else -> 60_000L }
                val full = runCatching { repository.artistTopSongs(more, shelf.moreParams, timeoutMs) }
                    .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it }
                    .getOrNull()
                if (!full.isNullOrEmpty()) {
                    showTopSongs(full)
                    expandedArtistPages += destination
                    break
                }
                // Several answers without songs: keep the list already shown.
                if (full != null && ++emptyAnswers >= 5) { expandedArtistPages += destination; break }
                attempt++
                delay(loadingRetryDelayMs(attempt))
            }
        } finally {
            topSongsLoading = false
        }
    }
    // Albums served as music videos are matched to their songs in the
    // background; a row clicked meanwhile waits for that match.
    val albumScope = rememberCoroutineScope()
    var albumAudio by remember(destination) { mutableStateOf<Deferred<List<Track>>?>(null) }
    var albumAudioChecked by remember(destination) { mutableStateOf(false) }
    LaunchedEffect(destination, page != null) {
        val current = page ?: return@LaunchedEffect
        if (albumAudioChecked) return@LaunchedEffect
        albumAudioChecked = true
        if (!destination.kind.equals("Album", true) || current.tracks.none(repository::needsAlbumAudio)) return@LaunchedEffect
        val job = albumScope.async {
            runCatching { repository.albumAudioTracks(current.tracks, current.title, current.artist) }
                .getOrDefault(current.tracks)
        }
        albumAudio = job
        val resolved = job.await()
        if (resolved != current.tracks && resolved.size == current.tracks.size) {
            val updated = current.copy(tracks = resolved)
            if (globalCatalogCache[destination] === current) globalCatalogCache[destination] = updated
            if (page === current) page = updated
        }
    }
    var albumClickJob by remember(destination) { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val playFromPage: (Track, List<Track>) -> Unit = { track, list ->
        val pending = albumAudio
        albumClickJob?.cancel()
        if (pending != null && pending.isActive) {
            // The whole queue is played from the matched songs, so any click
            // waits; a newer click replaces one that is still waiting.
            albumClickJob = albumScope.launch {
                // Never fails (the match falls back to the rows); leaving the page
                // cancels this click together with the match.
                val resolved: List<Track>? = pending.await()
                val index = list.indexOfFirst { it.id == track.id }
                if (resolved != null && resolved.size == list.size && index >= 0) onTrack(resolved[index], resolved)
                else onTrack(track, list)
            }
        } else {
            onTrack(track, list)
        }
    }
    if (loading) {
        if (destination.kind == "Artist") ArtistPageSkeleton(compact) else SearchResultsSkeleton()
        return
    }
    val rawLoaded = page ?: return
    val loaded = if (rawLoaded.title.equals("Music", true) || rawLoaded.title.startsWith("Unknown", true)) {
        rawLoaded.copy(title = destination.title)
    } else rawLoaded
    val isAlbumPage = destination.kind.equals("Album", true)
    // Album rows: "Artist • Album • Year" (the artist part is the clickable name).
    val albumRowDetails: (Track) -> Pair<String, String>? = { track ->
        if (!isAlbumPage) null else track.album.ifBlank { loaded.title } to loaded.year
    }

    if (destination.kind == "Shelf") {
        ArtistShelfPage(compact, destination, loaded, onBack, onTrack, onTrackMore, onPlayNext, onOpenCatalog)
        return
    }

    if (destination.kind != "Artist" && !compact) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            loaded.artworkUrl?.let { url ->
                val catalogBgModel = WindowsLocalMusicLibrary.parseLocalArtworkFile(url) ?: url.lowResBlurThumbnailUrl()
                AsyncImage(
                    model = catalogBgModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(84.dp).graphicsLayer { alpha = 0.42f; scaleX = 1.35f; scaleY = 1.35f }
                )
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Black.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.95f)))))
            }
            
            Row(Modifier.fillMaxSize()) {
                Column(
                    Modifier.weight(0.45f).fillMaxHeight().padding(top = 64.dp, bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Artwork(loaded.artworkUrl, Modifier.size(280.dp), RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(24.dp))
                    // Long titles get a smaller size and a third line instead of
                    // being cut; the line height keeps wrapped lines apart.
                    val longTitle = loaded.title.length > 28
                    Text(
                        loaded.title,
                        color = Color.White,
                        fontSize = if (longTitle) 26.sp else 32.sp,
                        lineHeight = if (longTitle) 32.sp else 38.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    if (loaded.subtitle.isNotBlank()) {
                        Text(
                            loaded.subtitle,
                            color = SecondaryText,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                    val totalDuration = loaded.tracks.sumOf(Track::durationMs)
                    Text(
                        if (isAlbumPage && loaded.artist.isNotBlank()) loaded.artist
                        else "${loaded.tracks.size} songs" + if (totalDuration > 0L) " • ${totalDuration.toClock()}" else "",
                        color = SecondaryText,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Elevated.copy(alpha = 0.5f), shape = CircleShape, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.FavoriteBorder, "Like", tint = Color.White) }
                        }
                        Surface(
                            color = Color(0xFFCC0000),
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp).clickable { loaded.tracks.firstOrNull()?.let { playFromPage(it, loaded.tracks) } }
                        ) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(36.dp)) }
                        }
                        Surface(color = Elevated.copy(alpha = 0.5f), shape = CircleShape, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.MoreVert, "More", tint = Color.White) }
                        }
                    }
                }

                val trackListState = rememberLazyListState()
                LazyColumn(
                    Modifier.weight(0.55f).fillMaxHeight().pageScrollbar(trackListState),
                    state = trackListState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 112.dp, bottom = 168.dp, end = 32.dp)
                ) {
                    items(loaded.tracks.indices.toList()) { trackIndex ->
                        val track = loaded.tracks[trackIndex]
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                (trackIndex + 1).toString(),
                                color = SecondaryText,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                modifier = Modifier.width(28.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.weight(1f)) {
                                SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }, albumDetails = albumRowDetails(track)) { playFromPage(track, loaded.tracks) }
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // Artist pages: pictures still loading shimmer, and failed ones are tried again.
    androidx.compose.runtime.CompositionLocalProvider(
        LocalArtworkLoading provides if (destination.kind == "Artist") ArtworkLoadingStyle.Persistent else ArtworkLoadingStyle.Default,
    ) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val maxH = maxHeight
        // Width the horizontal rows share (they have 24 dp of padding each side).
        val rowWidth = maxWidth - 48.dp
        val catalogListState = rememberLazyListState()
        LazyColumn(
            Modifier.fillMaxSize().pageScrollbar(catalogListState)
                // Artist pages scroll by click-and-drag too, like the home page.
                .let { if (destination.kind == "Artist") it.dragVertically(catalogListState) else it },
            state = catalogListState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp),
        ) {
            if (!compact && destination.kind != "Artist") item { Spacer(Modifier.height(56.dp)) }
            item {
                if (destination.kind == "Artist") {
                    Box(Modifier.fillMaxWidth().height(maxH * 0.7f)) {
                        ArtistHeroImage(loaded.artworkUrl, Modifier.fillMaxSize())
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .25f), Color.Black))))
                        Column(Modifier.align(Alignment.BottomStart).padding(28.dp)) {
                            ArtistNameHeading(loaded.title, compact)
                            if (loaded.subtitle.isNotBlank()) Text(loaded.subtitle, color = Color.White.copy(alpha = .75f), fontSize = 14.sp)
                            Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GlassButton(onClick = {}) { Text("Subscribe") }
                                GlassButton(prominent = true, onClick = { loaded.tracks.firstOrNull()?.let { onTrack(it, loaded.tracks) } }) { Icon(Icons.Default.Radio, null); Spacer(Modifier.width(8.dp)); Text("Radio") }
                                IconButton(onClick = { if (loaded.tracks.isNotEmpty()) onTrack(loaded.tracks.shuffled().first(), loaded.tracks.shuffled()) }) { Icon(Icons.Default.Shuffle, "Shuffle", tint = Color.White) }
                            }
                        }
                    }
                } else {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Artwork(loaded.artworkUrl, Modifier.size(260.dp), RoundedCornerShape(4.dp))
                        Text(
                            loaded.title,
                            color = Color.White,
                            fontSize = 28.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 20.dp),
                        )
                        if (loaded.subtitle.isNotBlank()) {
                            Text(
                                loaded.subtitle,
                                color = SecondaryText,
                                fontSize = 15.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            )
                        }
                        val totalDuration = loaded.tracks.sumOf(Track::durationMs)
                        Text(
                            if (isAlbumPage && loaded.artist.isNotBlank()) loaded.artist
                            else "${loaded.tracks.size} songs" + if (totalDuration > 0L) " • ${totalDuration.toClock()}" else "",
                            color = SecondaryText,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Elevated, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.FavoriteBorder, "Like album", tint = Color.White) }
                            }
                            Surface(
                                color = MaterialTheme.colors.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(72.dp).clickable { loaded.tracks.firstOrNull()?.let { playFromPage(it, loaded.tracks) } },
                            ) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.PlayArrow, "Play album", tint = Color.Black, modifier = Modifier.size(38.dp)) }
                            }
                            Surface(color = Elevated, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.MoreVert, "Album menu", tint = Color.White) }
                            }
                        }
                    }
                }
            }
            item { Text(if (destination.kind == "Artist") "Top songs" else "Tracks", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) }
            if (destination.kind == "Artist") {
                item {
                    val rowState = androidx.compose.foundation.lazy.rememberLazyListState()
                    val panelWidth = if (compact) 320.dp else adaptivePanelWidth(rowWidth, 400.dp, 16.dp, (loaded.tracks.size + 3) / 4)
                    LazyRow(
                        state = rowState,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
                        modifier = Modifier.smoothHorizontalScroll(rowState),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(loaded.tracks.chunked(4)) { columnTracks ->
                            Column(Modifier.width(panelWidth), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                columnTracks.forEach { track ->
                                    SearchResultTrackRow(track, swipeable = false, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }) { onTrack(track, loaded.tracks) }
                                }
                            }
                        }
                        // The rest of the Top songs is still loading.
                        if (topSongsLoading) item {
                            Column(Modifier.width(panelWidth), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(4) { ShimmerTrackRow() }
                            }
                        }
                    }
                }
            } else {
                items(loaded.tracks.indices.toList()) { trackIndex ->
                    val track = loaded.tracks[trackIndex]
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            (trackIndex + 1).toString(),
                            color = SecondaryText,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            modifier = Modifier.width(28.dp),
                        )
                        Box(Modifier.weight(1f)) {
                            SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }, albumDetails = albumRowDetails(track)) { playFromPage(track, loaded.tracks) }
                        }
                    }
                }
            }
            if (loaded.albums.isNotEmpty() && destination.kind == "Shelf") {
                val cols = if (compact) 2 else 4
                items(loaded.albums.chunked(cols)) { rowItems ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        rowItems.forEach { rawItem ->
                            val itemKind = catalogKindFor(rawItem.browseId) { if (rawItem.subtitle.contains("Playlist", true)) "Playlist" else if (rawItem.subtitle.contains("Artist", true) || rawItem.browseId.startsWith("UC")) "Artist" else "Album" }
                            Column(Modifier.weight(1f).clickable { onOpenCatalog(rawItem.title, itemKind, rawItem.browseId, null, null) }) {
                                Artwork(rawItem.artworkUrl, Modifier.fillMaxWidth().aspectRatio(1f), RoundedCornerShape(12.dp))
                                Text(rawItem.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2, modifier = Modifier.padding(top = 8.dp))
                                Text(rawItem.subtitle, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                        repeat(cols - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
            if (destination.kind == "Artist") {
                loaded.shelves.forEach { shelf ->
                    if (shelf.title.equals("Top songs", true) || shelf.title.equals("Lagu teratas", true)) return@forEach
                    if (shelf.items.isEmpty()) return@forEach
                    item {
                        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text(shelf.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            if (shelf.moreEndpoint != null) {
                                TextButton(onClick = { onOpenCatalog(shelf.title, "Shelf", shelf.moreEndpoint, null, shelf.moreParams) }) {
                                    Text("Show All")
                                }
                            }
                        }
                    }
                    item {
                        val shelfScrollState = rememberScrollState()
                        val cardWidth = if (compact) 180.dp else adaptiveCardWidth(rowWidth, 180.dp)
                        Row(Modifier.horizontalScroll(shelfScrollState).dragHorizontally(shelfScrollState).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            shelf.items.forEach { rawItem ->
                                if (rawItem is com.recordlabs.desktop.data.CatalogItem) {
                                    val itemKind = catalogKindFor(rawItem.browseId) { if (rawItem.subtitle.contains("Playlist", true)) "Playlist" else if (rawItem.subtitle.contains("Artist", true) || rawItem.browseId.startsWith("UC")) "Artist" else "Album" }
                                    Column(Modifier.width(cardWidth).clickable { onOpenCatalog(rawItem.title, itemKind, rawItem.browseId, null, null) }) {
                                        Artwork(rawItem.artworkUrl, Modifier.size(cardWidth), RoundedCornerShape(12.dp))
                                        Text(rawItem.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2, modifier = Modifier.padding(top = 8.dp))
                                        Text(rawItem.subtitle, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
                                    }
                                } else if (rawItem is Track) {
                                    Column(Modifier.width(cardWidth).clickable { onTrack(rawItem, listOf(rawItem)) }) {
                                        Artwork(rawItem.artworkUrl, Modifier.size(cardWidth), RoundedCornerShape(12.dp))
                                        Text(rawItem.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2, modifier = Modifier.padding(top = 8.dp))
                                        ExplicitArtistText(rawItem, color = SecondaryText, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (compact) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(alpha = .45f), CircleShape)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
        }
    }
    }
}

/**
 * Everything behind an artist shelf's "Show All" (all albums, singles & EPs,
 * videos, playlists...): the songs/videos as a list, albums and playlists as a grid.
 */
@Composable
private fun ArtistShelfPage(
    compact: Boolean,
    destination: CatalogDestination,
    loaded: CatalogPageData,
    onBack: () -> Unit,
    onTrack: (Track, List<Track>) -> Unit,
    onTrackMore: (Track) -> Unit,
    onPlayNext: (Track) -> Unit,
    onOpenCatalog: (String, String, String?, String?, String?) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val cols = if (compact) 2 else (maxWidth / 200.dp).toInt().coerceIn(3, 7)
        // The page header names the artist; the shelf title says what is listed.
        val artistName = loaded.title.trim().takeUnless {
            it.isBlank() || it.startsWith("Unknown", true) || it.equals(destination.title, true)
        }
        val shelfListState = rememberLazyListState()
        LazyColumn(
            Modifier.fillMaxSize().pageScrollbar(shelfListState).dragVertically(shelfListState),
            state = shelfListState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = if (compact) 72.dp else 96.dp, bottom = 168.dp),
        ) {
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Text(destination.title, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    if (artistName != null) Text(artistName, color = SecondaryText, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if (loaded.tracks.isEmpty() && loaded.albums.isEmpty()) {
                item { Text("Nothing to show here.", color = SecondaryText, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) }
            }
            items(loaded.tracks.indices.toList()) { trackIndex ->
                val track = loaded.tracks[trackIndex]
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        (trackIndex + 1).toString(),
                        color = SecondaryText,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        modifier = Modifier.width(28.dp),
                    )
                    Box(Modifier.weight(1f)) {
                        SearchResultTrackRow(track, onMore = { onTrackMore(track) }, onPlayNext = { onPlayNext(track) }) { onTrack(track, loaded.tracks) }
                    }
                }
            }
            if (loaded.tracks.isNotEmpty() && loaded.albums.isNotEmpty()) item { Spacer(Modifier.height(24.dp)) }
            items(loaded.albums.chunked(cols)) { rowItems ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowItems.forEach { rawItem ->
                        val itemKind = catalogKindFor(rawItem.browseId) { if (rawItem.subtitle.contains("Playlist", true)) "Playlist" else if (rawItem.subtitle.contains("Artist", true) || rawItem.browseId.startsWith("UC")) "Artist" else "Album" }
                        Column(Modifier.weight(1f).clickable { onOpenCatalog(rawItem.title, itemKind, rawItem.browseId, null, null) }) {
                            Artwork(rawItem.artworkUrl, Modifier.fillMaxWidth().aspectRatio(1f), RoundedCornerShape(12.dp))
                            Text(rawItem.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2, modifier = Modifier.padding(top = 8.dp))
                            Text(rawItem.subtitle, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                    repeat(cols - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        if (compact) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(alpha = .45f), CircleShape)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
        }
    }
}

@Composable
private fun TrackCard(track: Track, width: Dp, onClick: (Track) -> Unit) {
    Column(Modifier.width(width).clickable { onClick(track) }) {
        Artwork(track.artworkUrl, Modifier.size(width), RoundedCornerShape(16.dp))
        Spacer(Modifier.height(8.dp))
        TrackTitleText(track, color = Color.White, fontSize = 14.sp, maxLines = 1)
        ExplicitArtistText(track, color = SecondaryText, fontSize = 12.sp)
    }
}

private fun resolveArtworkModel(url: String?, highQuality: Boolean = false): Any? {
    if (url.isNullOrBlank()) return null
    val localFile = WindowsLocalMusicLibrary.parseLocalArtworkFile(url)
    if (localFile != null) return localFile
    return if (highQuality) url.highQualityArtworkUrl() else url
}

/**
 * How pictures behave while they load. [Persistent] (artist pages): a moving
 * shimmer until the picture is in, and an online picture that fails is tried
 * again (1 s, 2 s, 4 s... up to 15 s apart) for as long as it is on screen.
 */
private class ArtworkLoadingStyle(val shimmer: Boolean, val retry: Boolean) {
    companion object {
        val Default = ArtworkLoadingStyle(shimmer = false, retry = false)
        val Persistent = ArtworkLoadingStyle(shimmer = true, retry = true)
    }
}

private val LocalArtworkLoading = androidx.compose.runtime.staticCompositionLocalOf { ArtworkLoadingStyle.Default }

/** Pause before loading attempt [attempt] + 1 of something that failed. */
private fun loadingRetryDelayMs(attempt: Int): Long = (1_000L shl (attempt - 1).coerceIn(0, 4)).coerceAtMost(15_000L)

/** A moving light across a placeholder: this part of the page is still loading. */
@Composable
private fun Modifier.loadingShimmer(): Modifier {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "loading-shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            tween(1_300, easing = androidx.compose.animation.core.LinearEasing),
        ),
        label = "loading-shimmer-x",
    )
    return this
        .background(Color.White.copy(alpha = 0.06f))
        .drawWithContent {
            drawContent()
            val band = size.width * 0.5f + 160f
            val x = -band + (size.width + band * 2f) * progress
            drawRect(
                Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.11f), Color.Transparent),
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x + band, size.height * 0.35f),
                ),
            )
        }
}

@Composable
internal fun Artwork(url: String?, modifier: Modifier, shape: Shape = RoundedCornerShape(10.dp), alignment: Alignment = Alignment.Center) {
    val loadingStyle = LocalArtworkLoading.current
    var fullResolutionReady by remember(url) { mutableStateOf(false) }
    var loadFailed by remember(url) { mutableStateOf(false) }
    // Persistent loading: failed attempts so far, and whether a retry is pending.
    var failedAttempts by remember(url) { mutableStateOf(0) }
    var retryPending by remember(url) { mutableStateOf(false) }
    LaunchedEffect(url, retryPending) {
        if (!retryPending) return@LaunchedEffect
        delay(loadingRetryDelayMs(failedAttempts))
        retryPending = false
    }
    // A local picture file, or the online address as given.
    val sourceModel = remember(url) { resolveArtworkModel(url) }
    // Online pictures are asked for at about the size they are shown (with room
    // for the window growing) instead of always 1200 px. The size only goes up;
    // while a sharper one loads, the current picture stays on screen.
    var requestedEdge by remember(url) { mutableStateOf(0) }
    var shownEdge by remember(url) { mutableStateOf(0) }
    var shownKey by remember(url) { mutableStateOf<coil3.memory.MemoryCache.Key?>(null) }
    var sharperFailed by remember(url) { mutableStateOf(false) }
    val edge = if (sharperFailed) shownEdge else requestedEdge
    val platformContext = coil3.compose.LocalPlatformContext.current
    val artworkModel = remember(url, sourceModel, edge) {
        when {
            sourceModel !is String -> sourceModel
            edge <= 0 -> null
            else -> {
                val sized = sourceModel.sizedArtworkUrl(edge)
                val placeholder = shownKey
                if (placeholder == null) sized
                else coil3.request.ImageRequest.Builder(platformContext).data(sized).placeholderMemoryCacheKey(placeholder).build()
            }
        }
    }
    Box(
        modifier.clip(shape).background(Elevated).onSizeChanged { size ->
            val wanted = artworkRequestEdge(maxOf(size.width, size.height))
            if (wanted > requestedEdge) requestedEdge = wanted
        },
        contentAlignment = Alignment.Center,
    ) {
        val canRetry = loadingStyle.retry && sourceModel is String
        if (loadingStyle.shimmer && sourceModel != null && !fullResolutionReady && !loadFailed) {
            Box(Modifier.fillMaxSize().loadingShimmer())
        }
        if (artworkModel != null && !loadFailed && !retryPending) {
            // A new attempt is a new request (Coil does not retry on its own).
            androidx.compose.runtime.key(failedAttempts) {
                AsyncImage(
                    model = artworkModel,
                    contentDescription = "Album artwork",
                    onSuccess = { state ->
                        fullResolutionReady = true
                        shownKey = state.result.memoryCacheKey
                        shownEdge = edge
                    },
                    onError = {
                        // A sharper version that fails keeps the picture already shown.
                        if (shownEdge > 0 && edge > shownEdge) sharperFailed = true
                        else if (canRetry) { failedAttempts++; retryPending = true }
                        else loadFailed = true
                    },
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (fullResolutionReady) 1f else 0f },
                    contentScale = ContentScale.Crop,
                    alignment = alignment,
                )
            }
        }
        if (sourceModel == null || loadFailed) {
            Icon(Icons.Default.Album, null, tint = Accent, modifier = Modifier.size(42.dp))
        }
    }
}

/** Faces in artist photos sit in the upper third: crop around there, not the middle or the top. */
private val ArtistHeroFocus = androidx.compose.ui.BiasAlignment(0f, -0.4f)

/**
 * The artist page's header photo. YouTube Music artist photos are wide 16:9
 * banners; [Artwork] asks for square pictures (right for covers), which made
 * YouTube cut the banner to a square that was then cut again. Here the banner
 * is asked for at its own shape, as wide as it is shown (up to 2560 px), and
 * only cropped as much as the header needs, around the upper third.
 */
@Composable
private fun ArtistHeroImage(url: String?, modifier: Modifier) {
    val bannerBase = remember(url) {
        url?.takeIf { it.contains("googleusercontent.com") || it.contains("ggpht.com") }
            ?.let { address -> Regex("=(?:w\\d+-h\\d+|s\\d+|w\\d+)[^/=]*$").find(address)?.let { address.substring(0, it.range.first) } }
    }
    var heroFailed by remember(url) { mutableStateOf(false) }
    // A failed banner is tried again a few times before the square picture is used.
    var heroAttempts by remember(url) { mutableStateOf(0) }
    var heroRetryPending by remember(url) { mutableStateOf(false) }
    LaunchedEffect(url, heroRetryPending) {
        if (!heroRetryPending) return@LaunchedEffect
        delay(loadingRetryDelayMs(heroAttempts))
        heroRetryPending = false
    }
    if (bannerBase == null || heroFailed) {
        Artwork(url, modifier, RoundedCornerShape(0.dp), ArtistHeroFocus)
        return
    }
    // Asked width only grows, in a few steps, so resizing the window does not refetch.
    var requestedWidth by remember(url) { mutableStateOf(0) }
    var shownKey by remember(url) { mutableStateOf<coil3.memory.MemoryCache.Key?>(null) }
    var ready by remember(url) { mutableStateOf(false) }
    val platformContext = coil3.compose.LocalPlatformContext.current
    val model = remember(bannerBase, requestedWidth) {
        if (requestedWidth <= 0) null
        else {
            val address = "$bannerBase=w$requestedWidth"
            val placeholder = shownKey
            if (placeholder == null) address
            else coil3.request.ImageRequest.Builder(platformContext).data(address).placeholderMemoryCacheKey(placeholder).build()
        }
    }
    Box(
        modifier.background(Elevated).onSizeChanged { size ->
            val wanted = when {
                size.width <= 0 -> 0
                size.width <= 1280 -> 1280
                size.width <= 1920 -> 1920
                else -> 2560
            }
            if (wanted > requestedWidth) requestedWidth = wanted
        },
    ) {
        if (!ready) Box(Modifier.fillMaxSize().loadingShimmer())
        if (model != null && !heroRetryPending) {
            androidx.compose.runtime.key(heroAttempts) {
                AsyncImage(
                    model = model,
                    contentDescription = "Artist photo",
                    onSuccess = { state ->
                        ready = true
                        shownKey = state.result.memoryCacheKey
                    },
                    onError = {
                        if (!ready) {
                            heroAttempts++
                            if (heroAttempts >= 4) heroFailed = true else heroRetryPending = true
                        }
                    },
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (ready) 1f else 0f },
                    contentScale = ContentScale.Crop,
                    alignment = ArtistHeroFocus,
                )
            }
        }
    }
}

/**
 * Picture size (px) to ask for a box of [shownEdgePx]: half as large again (a
 * window that grows makes the cards larger), in a few steps so pictures are
 * shared between places. 1200 px when the size is not known.
 */
private fun artworkRequestEdge(shownEdgePx: Int): Int {
    if (shownEdgePx <= 0) return 1200
    val needed = shownEdgePx * 3 / 2
    return when {
        needed <= 226 -> 226
        needed <= 544 -> 544
        else -> 1200
    }
}

/** [highQualityArtworkUrl] with [edge] px instead of 1200. */
private fun String.sizedArtworkUrl(edge: Int): String =
    if (edge >= 1200) highQualityArtworkUrl()
    else replace(Regex("=w\\d+-h\\d+"), "=w$edge-h$edge")
        .replace(Regex("=s\\d+"), "=s$edge")
        .replace(Regex("=w\\d+"), "=w$edge")
        .replace(Regex("/(?:default|mqdefault|hqdefault|sddefault)\\.jpg"), "/maxresdefault.jpg")

private fun String.highQualityArtworkUrl(): String =
    replace(Regex("=w\\d+-h\\d+"), "=w1200-h1200")
        .replace(Regex("=s\\d+"), "=s1200")
        .replace(Regex("=w\\d+"), "=w1200")
        .replace(Regex("/(?:default|mqdefault|hqdefault|sddefault)\\.jpg"), "/maxresdefault.jpg")

private fun String.lowResBlurThumbnailUrl(): String {
    if (isBlank()) return this
    if (contains("googleusercontent.com") || contains("ggpht.com") || contains("ytimg.com")) {
        return replace(Regex("=w\\d+-h\\d+[^=]*$"), "=w48-h48-p-k-no-nu")
            .replace(Regex("=s\\d+[^=]*$"), "=s48")
    }
    return this
}

/**
 * Player and lyrics background: the artwork's colours drifting slowly into
 * each other (see AmbientArtworkBackground), sampled from a small thumbnail.
 */
@Composable
private fun PlayerAmbientBackground(artworkUrl: String?) {
    val model = remember(artworkUrl) {
        artworkUrl?.let { url -> WindowsLocalMusicLibrary.parseLocalArtworkFile(url) ?: url.lowResBlurThumbnailUrl() }
    }
    AmbientArtworkBackground(model, Background)
}


/** Reads the playback position in its own small scope (see playbackPositionState). */
@Composable
private fun PlaybackPositionScope(position: androidx.compose.runtime.State<Long>, content: @Composable (Long) -> Unit) {
    content(position.value)
}

@Composable
private fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    isLoading: Boolean,
    positionMs: Long,
    durationMs: Long,
    notice: String?,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDevices: () -> Unit,
    globalQueueVisible: Boolean = false,
    onToggleQueue: () -> Unit = {},
    onOpenCatalog: ((String, String, String?, String?) -> Unit)? = null,
    onOpenPlayer: () -> Unit,
) {
    if (track == null) return
    // Settings > Appearance: the shape (pill or rounded box) and the artwork's
    // colour behind it change with a short animation.
    val roundedShape = com.recordlabs.desktop.data.AppSettings.miniPlayerShape.value == "rounded"
    val cornerPercent by androidx.compose.animation.core.animateIntAsState(if (roundedShape) 22 else 50, tween(380), label = "mini-player-corners")
    val artworkCorner by androidx.compose.animation.core.animateIntAsState(if (roundedShape) 22 else 50, tween(380), label = "mini-player-art-corners")
    val miniShape = RoundedCornerShape(cornerPercent)
    val colourAlpha by animateFloatAsState(if (com.recordlabs.desktop.data.AppSettings.flatMiniPlayer) 0f else 0.35f, tween(450), label = "mini-player-colour")
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, miniShape)
            .clip(miniShape)
            .background(SurfaceColor.copy(alpha = 0.88f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), miniShape)
            .clickable(onClick = onOpenPlayer),
    ) {
        // The Flat styles leave out the artwork's colour behind the mini player.
        if (colourAlpha > 0.005f) track.artworkUrl?.let { url ->
            AsyncImage(
                model = resolveArtworkModel(url) ?: url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(miniShape)
                    .blur(32.dp)
                    .graphicsLayer { alpha = colourAlpha },
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(track.artworkUrl, Modifier.size(52.dp), RoundedCornerShape(artworkCorner))
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                TrackTitleText(track, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                if (notice != null) Text(notice, color = Color.White.copy(alpha = 0.80f), fontSize = 13.sp, maxLines = 1)
                else ExplicitArtistText(track, color = Color.White.copy(alpha = 0.80f), fontSize = 13.sp)
            }
            BouncyIconButton(onClick = onDevices) {
                Icon(androidx.compose.ui.res.painterResource("record_labs_connect.svg"), "Connected devices", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            BouncyIconButton(onClick = onToggleQueue) {
                Icon(Heroicons.QueueList, "Queue", tint = if (globalQueueVisible) MaterialTheme.colors.primary else Color.White, modifier = Modifier.size(24.dp))
            }
            BouncyIconButton(onClick = onPrevious, keyPulse = PlaybackKeyAction.Previous) {
                Icon(Icons.Default.SkipPrevious, "Previous track", tint = Color.White)
            }
            BouncyIconButton(onClick = onPlayPause, keyPulse = PlaybackKeyAction.PlayPause, enabled = !isLoading || isPlaying) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(if (isPlaying) Heroicons.PauseSolid else Heroicons.PlaySolid, "Play or pause", tint = Color.White)
                }
            }
            BouncyIconButton(onClick = onNext, keyPulse = PlaybackKeyAction.Next) {
                Icon(Icons.Default.SkipNext, "Next track", tint = Color.White)
            }
        }
        val progress = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(progress)
                .height(3.dp)
                .background(MaterialTheme.colors.primary),
        )
    }
}


@Composable
private fun PlayerPage(
    track: Track,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    positionMs: Long,
    durationMs: Long,
    animatedArtworkEnabled: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onLyrics: () -> Unit,
    isLiked: Boolean,
    onToggleLiked: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: () -> Unit,
    onDevices: () -> Unit,
    onNext: () -> Unit,
    queue: List<Track>,
    queueIndex: Int,
    onQueueTrack: (Int) -> Unit,
    onQueueMove: (String, Int) -> Unit,
    onQueueRemove: (String) -> Unit,
    volume: Float,
    onVolume: (Float) -> Unit,
    isListenTogetherGuest: Boolean,
    onToggleMute: () -> Unit,
    shuffleEnabled: Boolean,
    onToggleShuffle: () -> Unit,
    repeatMode: Int,
    onCycleRepeat: () -> Unit,
    sleepTimerActive: Boolean,
    onToggleSleepTimer: () -> Unit,
    onShare: () -> Unit,
    onStartRadio: () -> Unit,
    playbackTempo: Float,
    playbackPitch: Int,
    onPlaybackTuning: (Float, Int) -> Unit,
    playlists: List<WindowsPlaylist>,
    onAddToPlaylist: (String) -> Unit,
    onViewArtist: () -> Unit,
    onViewAlbum: () -> Unit,
    onDownload: () -> Unit,
    onListenTogether: () -> Unit,
    isQueueVisible: Boolean,
    onQueueVisibilityChanged: (Boolean) -> Unit,
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    onToggleAnimatedArtwork: () -> Unit = {},
) {

    var showMenu by remember(track.id) { mutableStateOf(false) }
    var showAdvanced by remember(track.id) { mutableStateOf(false) }
    var showPlaylistPicker by remember(track.id) { mutableStateOf(false) }
    var showDetails by remember(track.id) { mutableStateOf(false) }
    var lyricsFetchNotice by remember(track.id) { mutableStateOf<String?>(null) }
    val playerPageScope = rememberCoroutineScope()



    fun closeQueueThen(action: () -> Unit) {

        action()
    }


    var dragOffsetY by remember { mutableStateOf(0f) }
    var isPageDragging by remember { mutableStateOf(false) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = if (isPageDragging) snap() else spring(stiffness = 500f),
        label = "player-drag-offset",
    )
    val queueShift by animateDpAsState(if (isQueueVisible) 368.dp else 0.dp, tween(320), label = "player-queue-shift")

    Box(Modifier.fillMaxSize().offset { IntOffset(0, animatedOffsetY.roundToInt()) }) {
        PlayerAmbientBackground(track.artworkUrl)
        
        // Slides down below the header while it is showing, so it is never covered.
        val pillTop by animateDpAsState(if (PlayerChrome.headerShown) 80.dp else 16.dp, tween(300), label = "drag-pill-top")
        // Pull down, or click it, to close the page.
        DismissPill(
            Modifier.align(Alignment.TopCenter).zIndex(3f).padding(top = pillTop - 12.dp),
            onDragStart = { isPageDragging = true },
            onDrag = { amount -> dragOffsetY = (dragOffsetY + amount).coerceAtLeast(0f) },
            onDragEnd = {
                isPageDragging = false
                if (dragOffsetY > 140f) closeQueueThen(onBack) else dragOffsetY = 0f
            },
        ) { closeQueueThen(onBack) }

        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(start = queueShift)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { isPageDragging = true },
                        onDragEnd = {
                            isPageDragging = false
                            if (dragOffsetY > 140f) {
                                closeQueueThen(onBack)
                            } else {
                                dragOffsetY = 0f
                            }
                        },
                        onDragCancel = { isPageDragging = false; dragOffsetY = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            if (dragAmount > 0f || dragOffsetY > 0f) {
                                change.consume()
                                dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                            }
                        },
                    )
                },
        ) {
            val compact = maxWidth < 700.dp
            if (compact) {
                // PORTRAIT LAYOUT
                Column(Modifier.fillMaxSize()) {
                    // TOP 50% - ARTWORK attached to TOP/LEFT/RIGHT (0 padding, square corners + bottom alpha opacity fade into blurred bg)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(0.50f)
                            .graphicsLayer { alpha = 0.99f }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black,
                                            Color.Black.copy(alpha = 0.8f),
                                            Color.Transparent,
                                        ),
                                        startY = size.height * 0.4f,
                                        endY = size.height,
                                    ),
                                    blendMode = BlendMode.DstIn,
                                )
                            },
                    ) {
                        AnimatedTrackArtwork(track, Modifier.fillMaxSize(), RoundedCornerShape(0.dp), animatedArtworkEnabled, musicVideoScale = ContentScale.Crop)
                        LyricsTranslationButton(Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 12.dp))
                    }


                    // LOWER 50% - DETAILS, PROGRESS, CONTROLS & BOTTOM NAV
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(0.50f)
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Spacer(Modifier.weight(0.2f))
                        // Title/Artist & Actions (Moved DOWN closer to slider)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                TrackTitleText(track, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(
                                    listOf(track.artist, track.album).filter { it.isNotBlank() }.joinToString(" • "),
                                    color = SecondaryText,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                )
                            }
                            BouncyIconButton(onClick = onToggleFullscreen) { Icon(if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, "Toggle fullscreen", tint = Color.White) }
                            if (isListenTogetherGuest) BouncyIconButton(onClick = onToggleMute) {
                                Icon(if (volume <= 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp, "Mute guest playback", tint = Color.White)
                            }
                            BouncyIconButton(onClick = onToggleLiked) {
                                Icon(if (isLiked) Heroicons.HeartSolid else Heroicons.Heart, "Like", tint = if (isLiked) MaterialTheme.colors.primary else Color.White)
                            }
                            BouncyIconButton(onClick = onToggleFullscreen) {
                                Icon(if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, "Toggle fullscreen", tint = Color.White)
                            }
                            BouncyIconButton(onClick = { showMenu = true }) { Icon(Heroicons.EllipsisVertical, "Menu", tint = Color.White) }
                        }

                        // Progress Slider
                        Column(Modifier.fillMaxWidth()) {
                            val safeDuration = durationMs.coerceAtLeast(1L)
                            var isSeeking by remember(track.id) { mutableStateOf(false) }
                            var pendingProgress by remember(track.id) { mutableStateOf(0f) }
                            LaunchedEffect(track.id, positionMs, safeDuration, isSeeking) {
                                if (!isSeeking) pendingProgress = (positionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
                            }
                            Slider(
                                value = pendingProgress,
                                onValueChange = {
                                    isSeeking = true
                                    pendingProgress = it
                                },
                                onValueChangeFinished = {
                                    onSeek((pendingProgress * safeDuration).toLong())
                                    isSeeking = false
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colors.primary,
                                    activeTrackColor = MaterialTheme.colors.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            PlaybackTimeRow(track, positionMs, durationMs, 12f)
                        }

                        // Primary Controls (Moved UP closer to progress slider)
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            BouncyIconButton(onClick = onDevices, modifier = Modifier.align(Alignment.CenterStart)) { Icon(androidx.compose.ui.res.painterResource("record_labs_connect.svg"), "Connected devices", tint = Color.White, modifier = Modifier.size(30.dp)) }
                            Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
                                BouncyIconButton(onClick = onPrevious, keyPulse = PlaybackKeyAction.Previous) { Icon(Heroicons.Backward, "Previous", tint = Color.White, modifier = Modifier.size(36.dp)) }
                                BouncyIconButton(onClick = onPlayPause, keyPulse = PlaybackKeyAction.PlayPause, modifier = Modifier.size(68.dp), enabled = !isLoading || isPlaying) {
                                    if (isLoading) {
                                        androidx.compose.material.CircularProgressIndicator(Modifier.size(40.dp), color = Color.White, strokeWidth = 3.dp)
                                    } else {
                                        Icon(androidx.compose.ui.res.painterResource(if (isPlaying) "pause.svg" else "play.svg"), "Play or pause", tint = Color.White, modifier = Modifier.size(60.dp))
                                    }
                                }
                                BouncyIconButton(onClick = onNext, keyPulse = PlaybackKeyAction.Next) { Icon(Heroicons.Forward, "Next", tint = Color.White, modifier = Modifier.size(36.dp)) }
                            }
                        }

                        Spacer(Modifier.weight(0.3f))

                        // Shared Translucent Glass Bottom Action Bar
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // 1. Queue
                                BouncyIconButton(onClick = { onQueueVisibilityChanged(!isQueueVisible) }) {
                                    Icon(Heroicons.QueueList, "Queue", tint = if (isQueueVisible) MaterialTheme.colors.primary else Color.White)
                                }

                                // Shuffle
                                BouncyIconButton(onClick = onToggleShuffle) { Icon(androidx.compose.ui.res.painterResource("shuffle_icon.png"), "Shuffle", tint = if (shuffleEnabled) MaterialTheme.colors.primary else Color.White, modifier = Modifier.size(24.dp)) }

                                // 4. Lyrics
                                BouncyIconButton(onClick = { onLyrics() }) { Icon(androidx.compose.ui.res.painterResource("lyrics_icon.png"), "Lyrics", tint = Color.White, modifier = Modifier.size(24.dp)) }

                                // 5. Repeat
                                val isRepeatActive = repeatMode > 0
                                val repeatIcon = if (repeatMode == 2) "repeat_one_icon.png" else "repeat_icon.png"
                                BouncyIconButton(
                                    onClick = onCycleRepeat,
                                    modifier = if (isRepeatActive) Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape) else Modifier,
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(repeatIcon),
                                        contentDescription = "Repeat",
                                        tint = if (isRepeatActive) Color.White else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // 6. Three dots menu
                                BouncyIconButton(onClick = { showMenu = true }) { Icon(Heroicons.EllipsisVertical, "More", tint = Color.White) }
                            }
                        }
                        VolumeControl(volume, onVolume)
                    }

                }
            } else {
                // LANDSCAPE LAYOUT
                // The open queue moves the page aside; the artwork keeps the size it
                // has without the queue as long as it fits its half of the page.
                val artworkSizeHere = minOf(maxWidth * 0.30f, maxHeight * 0.72f).coerceAtLeast(360.dp)
                val artworkSize = minOf((maxWidth + queueShift) * 0.30f, maxHeight * 0.72f).coerceAtLeast(360.dp)
                    .coerceAtMost(maxOf(artworkSizeHere, (maxWidth - 84.dp) / 2))
                val pageWidth = maxWidth
                val pageHeight = maxHeight
                Row(Modifier.fillMaxSize().padding(horizontal = 42.dp, vertical = 28.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        // A playing music video keeps its own shape instead of the square,
                        // and grows into its half of the page (as wide as it fits, and
                        // a little shorter than the page) instead of staying cover-sized.
                        var musicVideoAspect by remember(track.id) { mutableStateOf<Float?>(null) }
                        val boxEdge = musicVideoEdge(
                            artworkSize,
                            musicVideoAspect,
                            maxWidth = (pageWidth - 84.dp) / 2 - 8.dp,
                            maxHeight = (pageHeight - 56.dp) * 0.86f,
                        )
                        val artworkBox = artworkBoxSize(boxEdge, musicVideoAspect)
                        AnimatedTrackArtwork(track, Modifier.size(artworkBox).shadow(44.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = .70f), spotColor = Color.Black.copy(alpha = .85f)), RoundedCornerShape(14.dp), animatedArtworkEnabled, onMusicVideoAspectRatio = { musicVideoAspect = it }, showFullscreenButton = true)
                    }
                    Column(Modifier.weight(1f).padding(start = 44.dp), verticalArrangement = Arrangement.Center) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                TrackTitleText(track, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                                ExplicitArtistText(track, listOf(track.artist, track.album).filter { it.isNotBlank() }.joinToString(" - "), color = SecondaryText, fontSize = 16.sp)
                            }
                            if (isListenTogetherGuest) BouncyIconButton(onClick = onToggleMute) {
                                Icon(if (volume <= 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp, "Mute guest playback", tint = Color.White)
                            }
                            AnimatedArtworkToggleButton(animatedArtworkEnabled, onToggleAnimatedArtwork)
                            BouncyIconButton(onClick = onToggleLiked) {
                                Icon(if (isLiked) Heroicons.HeartSolid else Heroicons.Heart, "Like", tint = if (isLiked) MaterialTheme.colors.primary else Color.White)
                            }
                            BouncyIconButton(onClick = { showMenu = true }) { Icon(Heroicons.EllipsisVertical, "More", tint = Color.White) }
                        }
                        Spacer(Modifier.height(24.dp))
                        val safeDuration = durationMs.coerceAtLeast(1L)
                        var isSeeking by remember(track.id) { mutableStateOf(false) }
                        var pendingProgress by remember(track.id) { mutableStateOf(0f) }
                        LaunchedEffect(track.id, positionMs, safeDuration, isSeeking) {
                            if (!isSeeking) pendingProgress = (positionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
                        }
                        Slider(
                            value = pendingProgress,
                            onValueChange = {
                                isSeeking = true
                                pendingProgress = it
                            },
                            onValueChangeFinished = {
                                onSeek((pendingProgress * safeDuration).toLong())
                                isSeeking = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colors.primary,
                                activeTrackColor = MaterialTheme.colors.primary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        PlaybackTimeRow(track, positionMs, durationMs, 12f)
                        Spacer(Modifier.height(18.dp))
                        LandscapeTransportControls(
                            sizes = PLAYER_PAGE_TRANSPORT,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            shuffleEnabled = shuffleEnabled,
                            repeatMode = repeatMode,
                            queueActive = isQueueVisible,
                            onDevices = onDevices,
                            onShuffle = onToggleShuffle,
                            onPrevious = onPrevious,
                            onPlayPause = onPlayPause,
                            onNext = onNext,
                            onRepeat = onCycleRepeat,
                            onQueue = { onQueueVisibilityChanged(!isQueueVisible) },
                        )
                        Spacer(Modifier.height(20.dp))
                        VolumeControl(volume, onVolume)
                        lyricsFetchNotice?.let { Text(it, color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                    }
                }
                LandscapePageCorners(
                    isFullscreen = isFullscreen,
                    onToggleFullscreen = onToggleFullscreen,
                    lyricsActive = false,
                    onLyrics = { onLyrics() },
                )
            }
        }
    PlaybackQueueDialog(
            visible = isQueueVisible,
            queue = queue,
            activeIndex = queueIndex,
            onSelect = onQueueTrack,
            onMove = onQueueMove,
            onRemove = onQueueRemove,
            onClose = { onQueueVisibilityChanged(false) },
            asPopup = false,

        )
    }

    if (showMenu) {
        PlayerMenuBottomSheet(
            track = track,
            onDismiss = { showMenu = false },
            onStartRadio = { onStartRadio(); showMenu = false },
            onAddToPlaylist = { showMenu = false; showPlaylistPicker = true },
            onCopyLink = { onShare(); showMenu = false },
            onRefetchLyrics = {
                playerPageScope.launch {
                    lyricsFetchNotice = "Fetching lyrics…"
                    val fetched = runCatching { WindowsLyricsProviderRegistry.refetchLyrics(track) }.getOrDefault("")
                    lyricsFetchNotice = if (fetched.isBlank()) "Lyrics not found" else "Lyrics refreshed"
                }
                showMenu = false
            },
            onAdvanced = { showMenu = false; showAdvanced = true },
            onViewArtist = { showMenu = false; onViewArtist() },
            onViewAlbum = { showMenu = false; onViewAlbum() },
            onToggleLibrary = { onToggleLiked(); showMenu = false },
            onDownload = { onDownload(); showMenu = false },
            onListenTogether = { onListenTogether(); showMenu = false },
            onDetails = { showMenu = false; showDetails = true },
        )
    }
    if (showAdvanced) AdvancedTempoPitchDialog(
        initialTempo = playbackTempo,
        initialPitch = playbackPitch,
        onDismiss = { showAdvanced = false },
        onChange = onPlaybackTuning,
    )
    if (showPlaylistPicker) PlaylistPickerDialog(playlists, { showPlaylistPicker = false }) {
        onAddToPlaylist(it); showPlaylistPicker = false
    }
    if (showDetails) TrackDetailsDialog(track) { showDetails = false }
}

@Composable
private fun PlayerMenuBottomSheet(
    track: Track,
    onDismiss: () -> Unit,
    onStartRadio: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onCopyLink: () -> Unit,
    onRefetchLyrics: () -> Unit,
    onAdvanced: () -> Unit,
    onViewArtist: () -> Unit,
    onViewAlbum: () -> Unit,
    onToggleLibrary: () -> Unit,
    onDownload: () -> Unit,
    onListenTogether: () -> Unit,
    onDetails: () -> Unit,
) {
    val roomConnection by WindowsListenTogetherClient.connection.collectAsState()
    val isRoomHost by WindowsListenTogetherClient.isHost.collectAsState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Artwork(track.artworkUrl, Modifier.size(42.dp), RoundedCornerShape(8.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    TrackTitleText(track, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    ExplicitArtistText(track, color = SecondaryText, fontSize = 13.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (roomConnection == ListenTogetherConnection.InRoom && !isRoomHost) {
                    DropdownMenuItem(onClick = { WindowsListenTogetherClient.suggestTrack(track); onDismiss() }) {
                        Icon(Icons.Default.QueueMusic, null, tint = Accent)
                        Spacer(Modifier.width(12.dp))
                        Text("Suggest to host")
                    }
                }
                DropdownMenuItem(onClick = onStartRadio) {
                    Icon(Icons.Default.Radio, null, tint = Accent)
                    Spacer(Modifier.width(12.dp))
                    Text("Start radio")
                }
                DropdownMenuItem(onClick = onAddToPlaylist) {
                    Icon(Icons.Default.Add, null, tint = Accent)
                    Spacer(Modifier.width(12.dp))
                    Text("Add to playlist")
                }
                DropdownMenuItem(onClick = onCopyLink) {
                    Icon(Icons.Default.ContentCopy, null, tint = Accent)
                    Spacer(Modifier.width(12.dp))
                    Text("Copy link")
                }
                DropdownMenuItem(onClick = onViewArtist) {
                    Icon(Icons.Default.Person, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("View artist")
                }
                if (track.album.isNotBlank()) DropdownMenuItem(onClick = onViewAlbum) {
                    Icon(Icons.Default.Album, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("View album")
                }
                DropdownMenuItem(onClick = onToggleLibrary) {
                    Icon(Icons.Default.LibraryMusic, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Add to library")
                }
                DropdownMenuItem(onClick = onDownload) {
                    Icon(Icons.Default.Download, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Download")
                }
                DropdownMenuItem(onClick = onListenTogether) {
                    Icon(Icons.Default.QueueMusic, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Listen Together")
                }
                DropdownMenuItem(onClick = onDetails) {
                    Icon(Icons.Default.Info, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Details")
                }
                DropdownMenuItem(onClick = onAdvanced) {
                    Icon(Icons.Default.Tune, null, tint = Accent)
                    Spacer(Modifier.width(12.dp))
                    Text("Advanced — tempo and pitch")
                }
                LyricsTranslationLanguageItem()
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun TrackDetailsDialog(track: Track, onDismiss: () -> Unit) {
    // A plain dialog: AlertDialog's title/text slots made the artwork overlap the title.
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.widthIn(min = 360.dp, max = 520.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colors.surface,
            border = BorderStroke(1.dp, HeaderGlassBorder),
        ) {
            Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 12.dp)) {
                Text("Track details", color = SecondaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Artwork(track.artworkUrl, Modifier.size(112.dp), RoundedCornerShape(12.dp))
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TrackTitleText(track, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 2)
                        TrackDetailLine("Artist", track.artist)
                        if (track.album.isNotBlank()) TrackDetailLine("Album", track.album)
                        TrackDetailLine("Duration", track.durationMs.toClock())
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("YouTube Music ID: ${track.id}", color = SecondaryText, fontSize = 11.sp)
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun TrackDetailLine(label: String, value: String) {
    Column {
        Text(label, color = SecondaryText, fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 14.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

@Composable
private fun PlayerQuickActionsBar(
    shuffleEnabled: Boolean,
    repeatMode: Int,
    lyricsActive: Boolean,
    queueActive: Boolean,
    sleepTimerActive: Boolean,
    onQueue: () -> Unit,
    onSleepTimer: () -> Unit,
    onShuffle: () -> Unit,
    onLyrics: () -> Unit,
    onRepeat: () -> Unit,
    onMore: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BouncyIconButton(onClick = onQueue) {
                Icon(Heroicons.QueueList, "Queue", tint = if (queueActive) MaterialTheme.colors.primary else Color.White)
            }
            BouncyIconButton(onClick = onShuffle) {
                Icon(painterResource("shuffle_icon.png"), "Shuffle", tint = if (shuffleEnabled) MaterialTheme.colors.primary else Color.White, modifier = Modifier.size(22.dp))
            }
            BouncyIconButton(onClick = onLyrics) {
                Icon(painterResource("lyrics_icon.png"), "Lyrics", tint = if (lyricsActive) MaterialTheme.colors.primary else Color.White, modifier = Modifier.size(22.dp))
            }
            BouncyIconButton(onClick = onRepeat) {
                Icon(
                    painterResource(if (repeatMode == 2) "repeat_one_icon.png" else "repeat_icon.png"),
                    "Repeat",
                    tint = if (repeatMode > 0) MaterialTheme.colors.primary else Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colors.primary).clickable(onClick = onMore), contentAlignment = Alignment.Center) {
                Icon(Heroicons.EllipsisVertical, "More", tint = Color.Black, modifier = Modifier.size(22.dp))
            }
        }
    }
}

private val PLAYER_PAGE_TRANSPORT = TransportSizes(
    edgeButton = 48.dp, edgeIcon = 28.dp, sideButton = 48.dp, sideIcon = 32.dp, modeIcon = 24.dp,
    playButton = 76.dp, playIcon = 64.dp, loadingSize = 44.dp, maxGap = 30.dp,
)
private val LYRICS_PAGE_TRANSPORT = TransportSizes(
    edgeButton = 46.dp, edgeIcon = 28.dp, sideButton = 46.dp, sideIcon = 32.dp, modeIcon = 22.dp,
    playButton = 66.dp, playIcon = 56.dp, loadingSize = 44.dp, maxGap = 30.dp,
)
private val LYRICS_PAGE_TRANSPORT_COMPACT = TransportSizes(
    edgeButton = 38.dp, edgeIcon = 24.dp, sideButton = 38.dp, sideIcon = 27.dp, modeIcon = 20.dp,
    playButton = 52.dp, playIcon = 44.dp, loadingSize = 32.dp, maxGap = 20.dp,
)

/** Sizes of the landscape transport row (player page and lyrics page differ a little). */
private class TransportSizes(
    val edgeButton: Dp,
    val edgeIcon: Dp,
    val sideButton: Dp,
    val sideIcon: Dp,
    val modeIcon: Dp,
    val playButton: Dp,
    val playIcon: Dp,
    val loadingSize: Dp,
    val maxGap: Dp,
)

/**
 * Landscape transport row: devices at the left edge, queue at the right edge,
 * and shuffle / previous / play / next / repeat in the middle.
 */
@Composable
private fun LandscapeTransportControls(
    sizes: TransportSizes,
    isPlaying: Boolean,
    isLoading: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    queueActive: Boolean,
    onDevices: () -> Unit,
    onShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    onQueue: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // Narrow column: everything in one evenly spaced row, scaled down if needed.
        val minGap = 4.dp
        val needed = sizes.edgeButton * 2 + sizes.sideButton * 4 + sizes.playButton + minGap * 6 + 16.dp
        val narrow = maxWidth < needed
        val scale = if (narrow) (maxWidth / needed).coerceIn(0.6f, 1f) else 1f
        val edgeButton = sizes.edgeButton * scale
        val sideButton = sizes.sideButton * scale
        val playButton = sizes.playButton * scale
        val devices: @Composable (Modifier) -> Unit = { modifier ->
            BouncyIconButton(onClick = onDevices, modifier = modifier.size(edgeButton)) {
                Icon(androidx.compose.ui.res.painterResource("record_labs_connect.svg"), "Connected devices", tint = Color.White, modifier = Modifier.size(sizes.edgeIcon * scale))
            }
        }
        val queue: @Composable (Modifier) -> Unit = { modifier ->
            BouncyIconButton(onClick = onQueue, modifier = modifier.size(edgeButton)) {
                Icon(Heroicons.QueueList, "Queue", tint = if (queueActive) MaterialTheme.colors.primary else Color.White, modifier = Modifier.size(sizes.edgeIcon * scale))
            }
        }
        val middle: @Composable () -> Unit = {
            BouncyIconButton(onClick = onShuffle, modifier = Modifier.size(sideButton)) {
                Icon(painterResource("shuffle_icon.png"), "Shuffle", tint = if (shuffleEnabled) MaterialTheme.colors.primary else Color.White, modifier = Modifier.size(sizes.modeIcon * scale))
            }
            BouncyIconButton(onClick = onPrevious, keyPulse = PlaybackKeyAction.Previous, modifier = Modifier.size(sideButton)) { Icon(Heroicons.Backward, "Previous", tint = Color.White, modifier = Modifier.size(sizes.sideIcon * scale)) }
            BouncyIconButton(onClick = onPlayPause, keyPulse = PlaybackKeyAction.PlayPause, modifier = Modifier.size(playButton), enabled = !isLoading || isPlaying) {
                if (isLoading) {
                    androidx.compose.material.CircularProgressIndicator(Modifier.size(sizes.loadingSize * scale), color = Color.White, strokeWidth = 3.dp)
                } else {
                    Icon(if (isPlaying) Heroicons.PauseSolid else Heroicons.PlaySolid, "Play or pause", tint = Color.White, modifier = Modifier.size(sizes.playIcon * scale))
                }
            }
            BouncyIconButton(onClick = onNext, keyPulse = PlaybackKeyAction.Next, modifier = Modifier.size(sideButton)) { Icon(Heroicons.Forward, "Next", tint = Color.White, modifier = Modifier.size(sizes.sideIcon * scale)) }
            BouncyIconButton(onClick = onRepeat, modifier = Modifier.size(sideButton)) {
                Icon(
                    painterResource(if (repeatMode == 2) "repeat_one_icon.png" else "repeat_icon.png"),
                    "Repeat",
                    tint = if (repeatMode > 0) MaterialTheme.colors.primary else Color.White,
                    modifier = Modifier.size(sizes.modeIcon * scale),
                )
            }
        }
        if (narrow) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                devices(Modifier)
                middle()
                queue(Modifier)
            }
        } else {
            // The middle row shrinks its gaps before it would reach the edge buttons.
            val free = maxWidth - sizes.edgeButton * 2 - 16.dp - sizes.sideButton * 4 - sizes.playButton
            val gap = (free / 4).coerceIn(minGap, sizes.maxGap)
            devices(Modifier.align(Alignment.CenterStart))
            Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) { middle() }
            queue(Modifier.align(Alignment.CenterEnd))
        }
    }
}

/** Animated artwork on/off, shown left of the like button in landscape. */
@Composable
private fun AnimatedArtworkToggleButton(enabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier, iconSize: Dp = 24.dp) {
    BouncyIconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            if (enabled) Icons.Default.MotionPhotosOn else Icons.Default.MotionPhotosOff,
            if (enabled) "Disable animated artwork" else "Enable animated artwork",
            tint = if (enabled) Color.White else SecondaryText,
            modifier = Modifier.size(iconSize),
        )
    }
}

/**
 * Landscape page corners: full screen at the top left, and a pill with the
 * lyrics button and the floating lyrics (PiP) button at the bottom right.
 */
@Composable
private fun LandscapePageCorners(
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    lyricsActive: Boolean,
    onLyrics: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        // While the header is showing it has its own full-screen button and
        // the translate button (next to Listen Together); these two corner
        // buttons come back when the header hides.
        val cornersVisible = !PlayerChrome.headerShown
        androidx.compose.animation.AnimatedVisibility(
            visible = cornersVisible,
            enter = androidx.compose.animation.fadeIn(tween(300)),
            exit = androidx.compose.animation.fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 12.dp),
        ) {
            BouncyIconButton(onClick = onToggleFullscreen) {
                Icon(if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, "Toggle fullscreen", tint = Color.White)
            }
        }
        // Mirrors the full-screen button in the other top corner.
        androidx.compose.animation.AnimatedVisibility(
            visible = cornersVisible,
            enter = androidx.compose.animation.fadeIn(tween(300)),
            exit = androidx.compose.animation.fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 19.dp, top = 17.dp),
        ) {
            LyricsTranslationButton()
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 22.dp),
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        ) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                BouncyIconButton(onClick = onLyrics) {
                    Icon(painterResource("lyrics_icon.png"), "Lyrics", tint = if (lyricsActive) MaterialTheme.colors.primary else Color.White, modifier = Modifier.size(22.dp))
                }
                // Divider "|" between the two buttons.
                Box(Modifier.padding(horizontal = 2.dp).width(1.dp).height(20.dp).background(Color.White.copy(alpha = 0.28f)))
                BouncyIconButton(onClick = { FloatingLyricsState.togglePip() }) {
                    Icon(
                        if (FloatingLyricsState.isEnabled) Icons.Default.PictureInPictureAlt else Icons.Default.PictureInPicture,
                        if (FloatingLyricsState.isEnabled) "Disable floating lyrics" else "Enable floating lyrics",
                        tint = if (FloatingLyricsState.isEnabled) MaterialTheme.colors.primary else Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun VolumeControl(volume: Float, onVolume: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth().height(30.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (volume <= 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp, null, tint = Color.White.copy(alpha = .72f), modifier = Modifier.size(17.dp))
        Slider(
            value = volume,
            onValueChange = onVolume,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = .25f),
            ),
        )
    }
}

@Composable
private fun PlaybackQueueDialog(
    visible: Boolean,
    queue: List<Track>,
    activeIndex: Int,
    onSelect: (Int) -> Unit,
    onMove: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onClose: () -> Unit,
    verticalOffsetPx: Int = 0,
    horizontalDismissProgress: Float = 0f,
    asPopup: Boolean = true,
    fromRight: Boolean = false,
    closeButtonOnLeft: Boolean = false,
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val dragScrollScope = rememberCoroutineScope()
    val density = LocalDensity.current
    // Keep one stable visual list for the lifetime of the panel. Recreating
    // this state after every committed move discarded LazyColumn's placement
    // history, which is why ripple animation only worked on the first drag.
    var visualQueue by remember { mutableStateOf(queue) }
    var draggedId by remember { mutableStateOf<String?>(null) }
    var dragX by remember { mutableStateOf(0f) }
    var dragY by remember { mutableStateOf(0f) }
    var dragAxis by remember { mutableStateOf(0) } // 0 undecided, 1 vertical, 2 horizontal
    var dragOrigin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var draggedSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var panelCoordinates by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }
    var panelSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val rowCenters = remember { androidx.compose.runtime.mutableStateMapOf<String, Float>() }
    val activeId = queue.getOrNull(activeIndex)?.id
    LaunchedEffect(queue, draggedId) {
        if (draggedId == null) visualQueue = queue
    }
    LaunchedEffect(visible, activeIndex) {
        if (visible && activeIndex in queue.indices) listState.scrollToItem(activeIndex)
    }
    val content: @Composable () -> Unit = {
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { if (fromRight) it else -it } + fadeIn(tween(240)),
            exit = slideOutHorizontally(tween(340, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { if (fromRight) it else -it } + fadeOut(tween(220)),
        ) {
        Box(
            Modifier.width(360.dp).fillMaxHeight()
                .offset {
                    IntOffset(
                        (-360.dp.toPx() * horizontalDismissProgress.coerceIn(0f, 1f)).roundToInt(),
                        verticalOffsetPx,
                    )
                }
                .onGloballyPositioned {
                    panelCoordinates = it
                    panelSize = it.size
                },
        ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(8.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            color = Color.Black.copy(alpha = .90f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = .14f)),
        ) {
            Box(Modifier.fillMaxSize()) {
                // The Flat style leaves out the artwork's colour behind the queue (fading out).
                val queueColourAlpha by animateFloatAsState(if (com.recordlabs.desktop.data.AppSettings.flatQueue) 0f else .42f, tween(450), label = "queue-colour")
                if (queueColourAlpha > 0.005f) queue.getOrNull(activeIndex)?.artworkUrl?.let { url ->
                    val queueBgModel = WindowsLocalMusicLibrary.parseLocalArtworkFile(url) ?: url.lowResBlurThumbnailUrl()
                    AsyncImage(
                        model = queueBgModel,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().blur(84.dp).graphicsLayer { alpha = queueColourAlpha; scaleX = 1.35f; scaleY = 1.35f },
                    )
                }
            Column(Modifier.fillMaxSize().padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (closeButtonOnLeft) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, "Close queue", tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Queue", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (!closeButtonOnLeft) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, "Close queue", tint = Color.White)
                        }
                    }
                }
                LazyColumn(
                    Modifier.weight(1f)
                        .pageScrollbar(listState)
                        .queueScrollLock(listState) { visualQueue.indexOfFirst { it.id == activeId } },
                    state = listState,
                    // Room for the scroll bar, clear of the rows' drag handles.
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                items(visualQueue, key = Track::id) { item ->
                    val index = visualQueue.indexOfFirst { it.id == item.id }
                    val isDragged = draggedId == item.id
                    var rowPosition by remember(item.id) { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                    var rowSize by remember(item.id) { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
                    var gestureAccepted by remember(item.id) { mutableStateOf(false) }
                    var pendingX by remember(item.id) { mutableStateOf(0f) }
                    var pendingY by remember(item.id) { mutableStateOf(0f) }
                    Row(
                        Modifier.fillMaxWidth().animateItem(
                            placementSpec = tween(
                                durationMillis = 280,
                                easing = androidx.compose.animation.core.CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
                            ),
                        ).clip(RoundedCornerShape(10.dp))
                            .graphicsLayer { alpha = if (isDragged) 0f else 1f }
                            .onGloballyPositioned { coordinates ->
                                rowSize = coordinates.size
                                panelCoordinates?.takeIf { it.isAttached }?.let { panel ->
                                    rowPosition = panel.localPositionOf(coordinates, androidx.compose.ui.geometry.Offset.Zero)
                                    rowCenters[item.id] = rowPosition.y + coordinates.size.height / 2f
                                }
                            }
                            .pointerInput(item.id) {
                                fun resetDrag(resetOrder: Boolean) {
                                    if (resetOrder) visualQueue = queue
                                    gestureAccepted = false
                                    pendingX = 0f
                                    pendingY = 0f
                                    draggedId = null
                                    dragX = 0f
                                    dragY = 0f
                                    dragAxis = 0
                                }
                                detectDragGestures(
                                    onDragStart = { start ->
                                        val activeVisualIndex = visualQueue.indexOfFirst { it.id == activeId }
                                        val itemVisualIndex = visualQueue.indexOfFirst { it.id == item.id }
                                        gestureAccepted = itemVisualIndex > activeVisualIndex &&
                                            start.x >= size.width - 72.dp.toPx()
                                        if (!gestureAccepted) return@detectDragGestures
                                        // A fresh gesture must always start from
                                        // neutral motion state, including when
                                        // this same track has already moved.
                                        dragX = 0f
                                        dragY = 0f
                                        dragAxis = 0
                                        pendingX = 0f
                                        pendingY = 0f
                                        dragOrigin = rowPosition
                                        draggedSize = rowSize
                                        draggedId = item.id
                                    },
                                    onDragEnd = {
                                        if (!gestureAccepted) {
                                            resetDrag(resetOrder = true)
                                            return@detectDragGestures
                                        }
                                        val finalIndex = visualQueue.indexOfFirst { it.id == item.id }
                                        if (dragAxis == 2 && kotlin.math.abs(dragX) >= 96.dp.toPx()) {
                                            onRemove(item.id)
                                        } else if (finalIndex >= 0) {
                                            onMove(item.id, finalIndex)
                                        }
                                        resetDrag(resetOrder = false)
                                    },
                                    onDragCancel = { resetDrag(resetOrder = true) },
                                ) { change, drag ->
                                    if (!gestureAccepted) return@detectDragGestures
                                    change.consume()
                                    if (dragAxis == 0) {
                                        pendingX += drag.x
                                        pendingY += drag.y
                                        if (kotlin.math.abs(pendingX) + kotlin.math.abs(pendingY) >= 12.dp.toPx()) {
                                            dragAxis = if (kotlin.math.abs(pendingX) > kotlin.math.abs(pendingY)) 2 else 1
                                            dragX = if (dragAxis == 2) pendingX else 0f
                                            dragY = if (dragAxis == 1) pendingY else 0f
                                            pendingX = 0f
                                            pendingY = 0f
                                        }
                                    } else if (dragAxis == 2) {
                                        dragX += drag.x
                                    } else {
                                        dragY += drag.y
                                    }
                                    if (dragAxis == 1) {
                                        val currentIndex = visualQueue.indexOfFirst { it.id == item.id }
                                        val pointerCenter = dragOrigin.y + dragY + draggedSize.height / 2f
                                        val edge = 84.dp.toPx()
                                        when {
                                            pointerCenter < edge -> dragScrollScope.launch { listState.scrollBy(-38f) }
                                            pointerCenter > panelSize.height - edge -> dragScrollScope.launch { listState.scrollBy(38f) }
                                        }
                                        val activeVisualIndex = visualQueue.indexOfFirst { it.id == activeId }
                                        val firstMovable = (activeVisualIndex + 1).coerceAtLeast(0)
                                        val target = visualQueue.indices
                                            .filter { it >= firstMovable }
                                            .minByOrNull { candidate ->
                                                kotlin.math.abs((rowCenters[visualQueue[candidate].id] ?: Float.MAX_VALUE) - pointerCenter)
                                            }
                                        if (currentIndex >= firstMovable && target != null && target != currentIndex) {
                                            visualQueue = visualQueue.toMutableList().apply { add(target, removeAt(currentIndex)) }
                                        }
                                    }
                                }
                            }
                            .clickable(enabled = draggedId == null) {
                                queue.indexOfFirst { it.id == item.id }.takeIf { it >= 0 }?.let(onSelect)
                            }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) { QueueTrackRowContent(item, activeId == item.id) }
                }
            }
            }
            }
        }
        val draggedTrack = draggedId?.let { id -> visualQueue.firstOrNull { it.id == id } }
        if (draggedTrack != null && draggedSize.width > 0 && draggedSize.height > 0) {
            Surface(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (dragOrigin.x + dragX).roundToInt(),
                            (dragOrigin.y + dragY).roundToInt(),
                        )
                    }
                    .width(with(density) { draggedSize.width.toDp() })
                    .height(with(density) { draggedSize.height.toDp() })
                    .shadow(26.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xF22B2D33),
                border = BorderStroke(1.dp, Color.White.copy(alpha = .24f)),
            ) {
                Row(
                    Modifier.fillMaxSize().padding(10.dp).graphicsLayer {
                        alpha = if (dragAxis == 2) (1f - kotlin.math.abs(dragX) / 340f).coerceIn(.35f, 1f) else 1f
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) { QueueTrackRowContent(draggedTrack, false) }
            }
        }
        }
        }
    } // end of content
    
    if (asPopup) {
        Popup(
            alignment = if (fromRight) Alignment.CenterEnd else Alignment.CenterStart,
            onDismissRequest = {},
            properties = PopupProperties(focusable = false),
        ) {
            content()
        }
    } else {
        content()
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.QueueTrackRowContent(item: Track, isActive: Boolean) {
    Artwork(item.artworkUrl, Modifier.size(48.dp), RoundedCornerShape(8.dp))
    Column(Modifier.padding(start = 12.dp).weight(1f)) {
        Text(item.title, color = Color.White, fontSize = 15.sp, maxLines = 1)
        ExplicitArtistText(item, color = SecondaryText, fontSize = 12.sp)
    }
    if (isActive) Icon(Icons.Default.PlayArrow, "Playing", tint = Accent)
    else Icon(
        Icons.Default.List,
        "Drag to reorder or swipe to remove",
        tint = Color.White.copy(alpha = .72f),
        modifier = Modifier.size(38.dp).padding(9.dp),
    )
}

@Composable
private fun LyricsPage(
    track: Track,
    repository: WindowsMusicRepository,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    positionMs: Long = 0L,
    durationMs: Long = 0L,
    animatedArtworkEnabled: Boolean,
    onToggleAnimatedArtwork: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit = onBack,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit = {},
    onPrevious: () -> Unit = {},
    onDevices: () -> Unit = {},
    onNext: () -> Unit = {},
    isLiked: Boolean,
    onToggleLiked: () -> Unit,
    onShare: () -> Unit,
    queue: List<Track>,
    queueIndex: Int,
    onQueueTrack: (Int) -> Unit,
    onQueueMove: (String, Int) -> Unit,
    onQueueRemove: (String) -> Unit,
    volume: Float,
    onVolume: (Float) -> Unit,
    isListenTogetherGuest: Boolean,
    onToggleMute: () -> Unit,
    shuffleEnabled: Boolean,
    onToggleShuffle: () -> Unit,
    repeatMode: Int,
    onCycleRepeat: () -> Unit,
    sleepTimerActive: Boolean,
    onToggleSleepTimer: () -> Unit,
    onStartRadio: () -> Unit,
    playbackTempo: Float,
    playbackPitch: Int,
    onPlaybackTuning: (Float, Int) -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    playlists: List<WindowsPlaylist>,
    onAddToPlaylist: (String) -> Unit,
    onViewArtist: () -> Unit,
    onViewAlbum: () -> Unit,
    onDownload: () -> Unit,
    onListenTogether: () -> Unit,
    isQueueVisible: Boolean,
    onQueueVisibilityChanged: (Boolean) -> Unit,
) {
    val cachedLyrics = remember(track.id) { WindowsLyricsProviderRegistry.peekCached(track) }
    var rawLyrics by remember(track.id) { mutableStateOf(cachedLyrics?.lyrics) }
    var lyricsProvider by remember(track.id) { mutableStateOf(cachedLyrics?.provider.orEmpty()) }
    var lyricsError by remember(track.id) { mutableStateOf(false) }
    var refreshGeneration by remember(track.id) { mutableStateOf(0) }
    var manualSelectionGeneration by remember(track.id) { mutableStateOf(0) }




    var showOptions by remember(track.id) { mutableStateOf(false) }
    var showOffsetEditor by remember(track.id) { mutableStateOf(false) }
    var showSongMenu by remember(track.id) { mutableStateOf(false) }
    var showPageMenu by remember(track.id) { mutableStateOf(false) }
    var showLyricsSearch by remember(track.id) { mutableStateOf(false) }
    var showLyricsEditor by remember(track.id) { mutableStateOf(false) }
    var showAdvanced by remember(track.id) { mutableStateOf(false) }
    var showDetails by remember(track.id) { mutableStateOf(false) }
    var showPlaylistPicker by remember(track.id) { mutableStateOf(false) }
    // Shared with Settings > Appearance / Content, so both always show the same values.
    var portraitTextSize by com.recordlabs.desktop.data.AppSettings.lyricsTextSizePortrait
    var landscapeTextSize by com.recordlabs.desktop.data.AppSettings.lyricsTextSizeLandscape
    var lyricsOffsetMs by com.recordlabs.desktop.data.AppSettings.lyricsOffsetMs
    var pronunciationEnabled by com.recordlabs.desktop.data.AppSettings.lyricsPronunciation
    var isLandscapeLayout by remember { mutableStateOf(false) }
    var pageDragOffsetY by remember { mutableStateOf(0f) }
    var pageDragging by remember { mutableStateOf(false) }
    val lyricsPageScope = rememberCoroutineScope()
    fun closeQueueThen(action: () -> Unit) {

        action()
    }


    val animatedPageOffsetY by animateFloatAsState(
        targetValue = pageDragOffsetY,
        animationSpec = if (pageDragging) snap() else spring(stiffness = 500f),
        label = "lyrics-page-drag",
    )
    val queueShift by animateDpAsState(if (isQueueVisible) 368.dp else 0.dp, tween(320), label = "lyrics-queue-shift")
    LaunchedEffect(track.id, refreshGeneration) {
        // Cached lyrics are rendered immediately. Preferred-provider checking
        // runs independently below so reopening this page never blocks or
        // flashes a loading state.
        if (refreshGeneration == 0 && rawLyrics?.isNotBlank() == true) return@LaunchedEffect
        val selectionAtStart = manualSelectionGeneration
        val result = runCatching { WindowsLyricsProviderRegistry.getLyricsResult(track, forceRefresh = refreshGeneration > 0) }
        if (manualSelectionGeneration != selectionAtStart) return@LaunchedEffect
        rawLyrics = result.getOrNull()?.lyrics.orEmpty()
        lyricsProvider = result.getOrNull()?.provider.orEmpty()
        lyricsError = result.isFailure
    }

    // Lyrics stored later in the background (Paxsenix answering after another
    // provider was shown, or after none had this song) replace what is shown.
    DisposableEffect(track.id) {
        val unsubscribe = WindowsLyricsProviderRegistry.addListener { updatedTrackId, result ->
            if (updatedTrackId == track.id && result.lyrics.isNotBlank()) {
                lyricsPageScope.launch {
                    if (rawLyrics != result.lyrics || lyricsProvider != result.provider) {
                        rawLyrics = result.lyrics
                        lyricsProvider = result.provider
                        lyricsError = false
                    }
                }
            }
        }
        onDispose { unsubscribe() }
    }

    LaunchedEffect(track.id, rawLyrics, lyricsProvider, manualSelectionGeneration) {
        val visibleLyrics = rawLyrics?.takeIf(String::isNotBlank) ?: return@LaunchedEffect
        // BetterLyrics can still be replaced by Paxsenix when it arrives later.
        if (lyricsProvider == "Paxsenix") return@LaunchedEffect
        val selectionAtStart = manualSelectionGeneration
        val provisional = LyricsResult(visibleLyrics, lyricsProvider)
        val upgraded = runCatching {
            WindowsLyricsProviderRegistry.getPreferredLyricsUpgrade(track, provisional)
        }.getOrNull() ?: return@LaunchedEffect
        if (manualSelectionGeneration != selectionAtStart) return@LaunchedEffect
        rawLyrics = upgraded.lyrics
        lyricsProvider = upgraded.provider
        lyricsError = false
    }

    val lines = remember(rawLyrics) { parseLyrics(rawLyrics.orEmpty()) }

    val lyricGroups = remember(lines) { groupRichLyrics(lines) }
    // Lyrics translation (top-right button; language in the ⋮ menu), worked
    // out in the background and cached per song and language.
    val translationEnabled = LyricsTranslationSettings.enabled
    val translationTarget = LyricsTranslationSettings.targetLanguage
    val lyricTranslations by produceState(emptyMap<String, String>(), lyricGroups, translationEnabled, translationTarget) {
        value = emptyMap()
        if (!translationEnabled || lyricGroups.isEmpty()) return@produceState
        val texts = lyricGroups.flatMap { it.lines }.map(::lyricPlainText)
        value = WindowsLyricsTranslator.translate(texts, translationTarget).orEmpty()
    }
    // Positive offset delays the lyric; negative offset advances it.
    val lyricsPositionMs = (positionMs - lyricsOffsetMs).coerceAtLeast(0L)
    val currentGroupIndex = remember(lyricGroups, lyricsPositionMs) {
        if (lyricGroups.isEmpty()) -1
        else {
            // Match Android's scroll target: background entries can become
            // active, but never replace the associated main vocal as the row
            // kept in the viewport focus position.
            val idx = lyricGroups.indexOfLast { group ->
                group.startMs <= lyricsPositionMs && group.lines.any { !it.isBackground }
            }
            // Before the first real timestamp there is no active lyric. The
            // intro interval indicator owns the focus until that timestamp.
            idx
        }
    }

    Box(Modifier.fillMaxSize().offset { IntOffset(0, animatedPageOffsetY.roundToInt()) }) {
        PlayerAmbientBackground(track.artworkUrl)
        
        // Slides down below the header while it is showing, so it is never covered.
        val pillTop by animateDpAsState(if (PlayerChrome.headerShown) 80.dp else 16.dp, tween(300), label = "drag-pill-top")
        // Pull down, or click it, to close the page.
        DismissPill(
            Modifier.align(Alignment.TopCenter).zIndex(3f).padding(top = pillTop - 12.dp),
            onDragStart = { pageDragging = true },
            onDrag = { amount -> pageDragOffsetY = (pageDragOffsetY + amount).coerceAtLeast(0f) },
            onDragEnd = {
                pageDragging = false
                if (pageDragOffsetY > 140f) closeQueueThen(onClose) else pageDragOffsetY = 0f
            },
        ) { closeQueueThen(onClose) }

        BoxWithConstraints(
            Modifier.fillMaxSize().padding(start = queueShift)
                .pointerInput(Unit) {
                    var gestureEnabled = false
                    detectVerticalDragGestures(
                        onDragStart = { start ->
                            val landscape = size.width >= 700.dp.toPx()
                            gestureEnabled = if (landscape) start.x <= size.width * .54f
                            else start.y <= size.height * .16f || start.y >= size.height * .72f
                            pageDragging = gestureEnabled
                        },
                        onDragEnd = {
                            if (gestureEnabled && pageDragOffsetY > 140f) closeQueueThen(onClose)
                            else pageDragOffsetY = 0f
                            pageDragging = false
                            gestureEnabled = false
                        },
                        onDragCancel = {
                            pageDragging = false
                            pageDragOffsetY = 0f
                            gestureEnabled = false
                        },
                        onVerticalDrag = { change, amount ->
                            if (gestureEnabled && (amount > 0f || pageDragOffsetY > 0f)) {
                                change.consume()
                                pageDragOffsetY = (pageDragOffsetY + amount).coerceAtLeast(0f)
                            }
                        },
                    )
                },
        ) {
            val containerWidth = maxWidth
            val containerHeight = maxHeight
            val isLandscape = containerWidth >= 700.dp
            LaunchedEffect(isLandscape) { isLandscapeLayout = isLandscape }
            if (!isLandscape) {
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        BouncyIconButton(onClick = {
                            FloatingLyricsState.togglePip()
                        }) {
                            Icon(
                                if (FloatingLyricsState.isEnabled) Icons.Default.PictureInPictureAlt else Icons.Default.PictureInPicture,
                                if (FloatingLyricsState.isEnabled) "Disable floating lyrics" else "Enable floating lyrics",
                                tint = if (FloatingLyricsState.isEnabled) MaterialTheme.colors.primary else Color.White,
                            )
                        }
                        BouncyIconButton(onClick = onToggleAnimatedArtwork) {
                            Icon(
                                if (animatedArtworkEnabled) Icons.Default.MotionPhotosOn else Icons.Default.MotionPhotosOff,
                                if (animatedArtworkEnabled) "Disable animated artwork" else "Enable animated artwork",
                                tint = if (animatedArtworkEnabled) Color.White else SecondaryText,
                            )
                        }
                        BouncyIconButton(onClick = onToggleFullscreen) {
                            Icon(if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, "Toggle fullscreen", tint = Color.White)
                        }
                        LyricsTranslationButton()
                    }
                    androidx.compose.runtime.CompositionLocalProvider(LocalLyricsTranslations provides lyricTranslations) {
                        LyricsLines(lyricGroups, currentGroupIndex, lyricsPositionMs, isPlaying && !isLoading, Modifier.weight(1f), rawLyrics == null, lyricsError, lyricsProvider, onSeek, portraitTextSize, pronunciationEnabled)
                    }
                    LyricsControlPanel(
                        track = track,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        isLiked = isLiked,
                        shuffleEnabled = shuffleEnabled,
                        repeatMode = repeatMode,
                        sleepTimerActive = sleepTimerActive,
                        queueActive = isQueueVisible,
                        compact = true,
                        volume = volume,
                        onVolume = onVolume,
                        isListenTogetherGuest = isListenTogetherGuest,
                        onToggleMute = onToggleMute,
                        onPlayPause = onPlayPause,
                        onSeek = onSeek,
                        onPrevious = onPrevious,
                        onDevices = onDevices,
                        onNext = onNext,
                        onLike = onToggleLiked,
                        onQueue = { onQueueVisibilityChanged(!isQueueVisible) },
                        onSleepTimer = onToggleSleepTimer,
                        onShuffle = onToggleShuffle,
                        onLyrics = { onBack() },
                        onRepeat = onCycleRepeat,
                        onLyricsTools = { showOptions = true },
                        onSongMenu = { showSongMenu = true },
                    )
    }
} else {
                // Shape of the music video playing (null for a cover), shared by
                // the artwork and the lyrics column (which moves a little right
                // to give the larger video room).
                var musicVideoAspect by remember(track.id) { mutableStateOf<Float?>(null) }
                val lyricsVideoGap by animateDpAsState(if (musicVideoAspect != null) 64.dp else 8.dp, tween(450, easing = androidx.compose.animation.core.FastOutSlowInEasing), label = "lyrics-video-gap")
                Row(Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 24.dp)) {
                    Column(
                        Modifier.weight(0.50f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val ultraShortViewport = containerHeight < 700.dp
                        val shortViewport = containerHeight < 900.dp
                        // Short windows (a 14" HD laptop): the artwork takes the height that is
                        // really free. The column (artwork, then the controls, drawn at 0.88)
                        // is centred, so it keeps clear of the header at the top and the same
                        // room below. Taller windows keep 58 % of the height as before.
                        val controlsBelowArtwork = 210.dp // 10 dp gap + compact controls, at 0.88
                        val headerClearance = 84.dp
                        val artworkHeightLimit = if (shortViewport) {
                            minOf(containerHeight * 0.58f, (containerHeight - headerClearance * 2) / 0.88f - controlsBelowArtwork)
                        } else containerHeight * 0.58f
                        val minimumArtwork = when {
                            ultraShortViewport -> 180.dp
                            shortViewport -> 240.dp
                            else -> 360.dp
                        }
                        // The open queue moves the page aside; the artwork keeps the size it
                        // has without the queue as long as it fits its half of the page
                        // (drawn at 0.88 of the page's scale below).
                        val artworkSizeHere = minOf(containerWidth * 0.30f, artworkHeightLimit)
                            .coerceAtLeast(minimumArtwork)
                        val artworkSize = minOf((containerWidth + queueShift) * 0.30f, artworkHeightLimit)
                            .coerceAtLeast(minimumArtwork)
                            .coerceAtMost(maxOf(artworkSizeHere, (containerWidth - 100.dp) * 0.5f / 0.88f))
                        val baseDensity = androidx.compose.ui.platform.LocalDensity.current
                        androidx.compose.runtime.CompositionLocalProvider(
                            androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(baseDensity.density * 0.88f, baseDensity.fontScale),
                        ) {
                        // A playing music video keeps its own shape instead of the square,
                        // and grows into the free room of its half of the page (sizes
                        // here are at 0.88 of the page's scale), leaving room for the
                        // controls below it. The controls take the video's width.
                        val boxEdge = musicVideoEdge(
                            artworkSize,
                            musicVideoAspect,
                            maxWidth = (containerWidth - 68.dp) * 0.5f / 0.88f - 24.dp,
                            maxHeight = (containerHeight - 48.dp) / 0.88f - 260.dp,
                        )
                        Column(Modifier.width(boxEdge)) {
                            // Floating lyrics, animated artwork and full screen moved to
                            // the page corners and the title row (LandscapePageCorners).
                            val artworkBox = artworkBoxSize(boxEdge, musicVideoAspect)
                            AnimatedTrackArtwork(track, Modifier.size(artworkBox).align(Alignment.CenterHorizontally).shadow(44.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = .70f), spotColor = Color.Black.copy(alpha = .85f)), RoundedCornerShape(14.dp), animatedArtworkEnabled, onMusicVideoAspectRatio = { musicVideoAspect = it }, showFullscreenButton = true)
                            Spacer(Modifier.height(10.dp))
                            LyricsControlPanel(
                                track = track,
                                isPlaying = isPlaying,
                                isLoading = isLoading,
                                positionMs = positionMs,
                                durationMs = durationMs,
                                isLiked = isLiked,
                                shuffleEnabled = shuffleEnabled,
                                repeatMode = repeatMode,
                                sleepTimerActive = sleepTimerActive,
                                queueActive = isQueueVisible,
                                compact = shortViewport,
                                volume = volume,
                                onVolume = onVolume,
                                isListenTogetherGuest = isListenTogetherGuest,
                                onToggleMute = onToggleMute,
                                modifier = Modifier.fillMaxWidth(),
                                onPlayPause = onPlayPause,
                                onSeek = onSeek,
                                onPrevious = onPrevious,
                                onDevices = onDevices,
                                onNext = onNext,
                                onLike = onToggleLiked,
                                onQueue = { onQueueVisibilityChanged(!isQueueVisible) },
                                onSleepTimer = onToggleSleepTimer,
                                onShuffle = onToggleShuffle,
                                onLyrics = { onBack() },
                                onRepeat = onCycleRepeat,
                                // One menu (song actions + lyrics tools) in landscape.
                                onLyricsTools = { showPageMenu = true },
                                onSongMenu = { showPageMenu = true },
                                landscape = true,
                                animatedArtworkEnabled = animatedArtworkEnabled,
                                onToggleAnimatedArtwork = onToggleAnimatedArtwork,
                            )
    }
}
                    }

                    Spacer(Modifier.width(32.dp))

                    Column(Modifier.weight(0.40f).fillMaxHeight().padding(start = lyricsVideoGap, top = 24.dp, bottom = 24.dp)) {
                        androidx.compose.runtime.CompositionLocalProvider(LocalLyricsTranslations provides lyricTranslations) {
                            LyricsLines(lyricGroups, currentGroupIndex, lyricsPositionMs, isPlaying && !isLoading, Modifier.fillMaxSize(), rawLyrics == null, lyricsError, lyricsProvider, onSeek, landscapeTextSize, pronunciationEnabled)
                        }
                    }
                    Spacer(Modifier.weight(0.10f))
                }
                LandscapePageCorners(
                    isFullscreen = isFullscreen,
                    onToggleFullscreen = onToggleFullscreen,
                    lyricsActive = true,
                    onLyrics = { onBack() },
                )
            }
        }
    PlaybackQueueDialog(
        visible = isQueueVisible,
        queue = queue,
        activeIndex = queueIndex,
        onSelect = onQueueTrack,
        onMove = onQueueMove,
        onRemove = onQueueRemove,
        onClose = { onQueueVisibilityChanged(false) },
            asPopup = false,

    )
    }

    if (showSongMenu) {
        val currentSize = if (isLandscapeLayout) landscapeTextSize else portraitTextSize
        LyricsOptionsDialog(
            track = track,
            textSize = currentSize,
            onTextSize = { size ->
                // Saved by AppSettings on the background preference thread.
                if (isLandscapeLayout) landscapeTextSize = size else portraitTextSize = size
            },
            onRefetch = {
                rawLyrics = null
                refreshGeneration++
                showSongMenu = false
            },
            onStartRadio = { onStartRadio(); showSongMenu = false },
            onShare = { onShare(); showSongMenu = false },
            onViewArtist = { showSongMenu = false; onViewArtist() },
            onViewAlbum = { showSongMenu = false; onViewAlbum() },
            onAddToPlaylist = { showSongMenu = false; showPlaylistPicker = true },
            isLiked = isLiked,
            onToggleLiked = onToggleLiked,
            onDismiss = { showSongMenu = false },
            onAdvanced = { showSongMenu = false; showAdvanced = true },
            onDownload = { onDownload(); showSongMenu = false },
            onListenTogether = { onListenTogether(); showSongMenu = false },
            onDetails = { showSongMenu = false; showDetails = true },
        )
    }
    if (showPageMenu) {
        val currentSize = if (isLandscapeLayout) landscapeTextSize else portraitTextSize
        LyricsPageMenuDialog(
            track = track,
            onStartRadio = { onStartRadio(); showPageMenu = false },
            onShare = { onShare(); showPageMenu = false },
            onViewArtist = { showPageMenu = false; onViewArtist() },
            onViewAlbum = { showPageMenu = false; onViewAlbum() },
            onAddToPlaylist = { showPageMenu = false; showPlaylistPicker = true },
            isLiked = isLiked,
            onToggleLiked = onToggleLiked,
            onAdvanced = { showPageMenu = false; showAdvanced = true },
            onDownload = { onDownload(); showPageMenu = false },
            onListenTogether = { onListenTogether(); showPageMenu = false },
            onDetails = { showPageMenu = false; showDetails = true },
            textSize = currentSize,
            onTextSize = { size ->
                // Saved by AppSettings on the background preference thread.
                if (isLandscapeLayout) landscapeTextSize = size else portraitTextSize = size
            },
            onEdit = { showPageMenu = false; showLyricsEditor = true },
            onRefetch = { rawLyrics = null; manualSelectionGeneration++; refreshGeneration++; showPageMenu = false },
            onSearch = { showPageMenu = false; showLyricsSearch = true },
            onCopy = {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(rawLyrics.orEmpty()), null)
                showPageMenu = false
            },
            lyricsOffsetMs = lyricsOffsetMs,
            onEditOffset = { showPageMenu = false; showOffsetEditor = true },
            pronunciationEnabled = pronunciationEnabled,
            onPronunciationEnabled = { enabled ->
                pronunciationEnabled = enabled
            },
            onDismiss = { showPageMenu = false },
        )
    }
    if (showAdvanced) AdvancedTempoPitchDialog(
        initialTempo = playbackTempo,
        initialPitch = playbackPitch,
        onDismiss = { showAdvanced = false },
        onChange = onPlaybackTuning,
    )
    if (showDetails) TrackDetailsDialog(track) { showDetails = false }
    if (showOptions) {
        val currentSize = if (isLandscapeLayout) landscapeTextSize else portraitTextSize
        LyricsToolsDialog(
            track = track,
            textSize = currentSize,
            onTextSize = { size ->
                // Saved by AppSettings on the background preference thread.
                if (isLandscapeLayout) landscapeTextSize = size else portraitTextSize = size
            },
            onEdit = { showOptions = false; showLyricsEditor = true },
            onRefetch = { rawLyrics = null; manualSelectionGeneration++; refreshGeneration++; showOptions = false },
            onSearch = { showOptions = false; showLyricsSearch = true },
            onCopy = {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(rawLyrics.orEmpty()), null)
                showOptions = false
            },
            lyricsOffsetMs = lyricsOffsetMs,
            onEditOffset = { showOptions = false; showOffsetEditor = true },
            pronunciationEnabled = pronunciationEnabled,
            onPronunciationEnabled = { enabled ->
                pronunciationEnabled = enabled
            },
            onDismiss = { showOptions = false },
        )
    }
    if (showOffsetEditor) {
        LyricsOffsetDialog(
            offsetMs = lyricsOffsetMs,
            onOffset = { value ->
                lyricsOffsetMs = value
            },
            onDismiss = { showOffsetEditor = false },
        )
    }
    if (showLyricsEditor) {
        LyricsEditDialog(rawLyrics.orEmpty(), onDismiss = { showLyricsEditor = false }) {
            val selected = WindowsLyricsProviderRegistry.selectLyrics(track, LyricsCandidate("Edited", it))
            manualSelectionGeneration++
            rawLyrics = selected.lyrics
            lyricsProvider = selected.provider
            showLyricsEditor = false
        }
    }
    if (showLyricsSearch) {
        LyricsSearchDialog(track, onDismiss = { showLyricsSearch = false }) { candidate ->
            val selected = WindowsLyricsProviderRegistry.selectLyrics(track, candidate)
            manualSelectionGeneration++
            rawLyrics = selected.lyrics
            lyricsProvider = selected.provider
            showLyricsSearch = false
        }
    }
    if (showPlaylistPicker) {
        PlaylistPickerDialog(
            playlists = playlists,
            onDismiss = { showPlaylistPicker = false },
            onSelect = { name ->
                onAddToPlaylist(name)
                showPlaylistPicker = false
            },
        )
    }
}

@Composable
private fun LyricsControlPanel(
    track: Track,
    onViewArtist: (String) -> Unit = {},
    onViewAlbum: (Track) -> Unit = {},
    isPlaying: Boolean,
    isLoading: Boolean = false,
    positionMs: Long,
    durationMs: Long,
    isLiked: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    sleepTimerActive: Boolean,
    queueActive: Boolean,
    compact: Boolean,
    volume: Float,
    onVolume: (Float) -> Unit,
    isListenTogetherGuest: Boolean,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: () -> Unit,
    onDevices: () -> Unit,
    onNext: () -> Unit,
    onLike: () -> Unit,
    onQueue: () -> Unit,
    onSleepTimer: () -> Unit,
    onShuffle: () -> Unit,
    onLyrics: () -> Unit,
    onRepeat: () -> Unit,
    onLyricsTools: () -> Unit,
    onSongMenu: () -> Unit,
    /** Landscape layout: shuffle/repeat beside previous/next, queue at the right, no pill. */
    landscape: Boolean = false,
    animatedArtworkEnabled: Boolean = false,
    onToggleAnimatedArtwork: () -> Unit = {},
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 9.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val hover = interactionSource.collectIsHoveredAsState().value
                TrackTitleText(track, color = Color.White, fontSize = if (compact) 18.sp else 23.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                
                val artistInteraction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val artistHover = artistInteraction.collectIsHoveredAsState().value
                ExplicitArtistText(track, listOf(track.artist, track.album).filter(String::isNotBlank).joinToString(" - "), color = Color.White.copy(alpha = 0.72f), fontSize = if (compact) 12.sp else 15.sp)
            }
            if (isListenTogetherGuest) BouncyIconButton(onClick = onToggleMute, modifier = Modifier.size(if (compact) 34.dp else 40.dp)) {
                Icon(if (volume <= 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp, "Mute guest playback", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            if (landscape) AnimatedArtworkToggleButton(animatedArtworkEnabled, onToggleAnimatedArtwork, Modifier.size(if (compact) 34.dp else 40.dp), iconSize = 20.dp)
            BouncyIconButton(onClick = onLike, modifier = Modifier.size(if (compact) 34.dp else 40.dp)) {
                Icon(if (isLiked) Heroicons.HeartSolid else Heroicons.Heart, "Like", tint = if (isLiked) MaterialTheme.colors.primary else Color.White, modifier = Modifier.size(20.dp))
            }
            BouncyIconButton(onClick = onLyricsTools, modifier = Modifier.size(if (compact) 34.dp else 40.dp)) { Icon(Heroicons.EllipsisVertical, "Lyrics tools", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }

        val safeDuration = durationMs.coerceAtLeast(1L)
        var isSeeking by remember(track.id) { mutableStateOf(false) }
        var pendingProgress by remember(track.id) { mutableStateOf(0f) }
        LaunchedEffect(track.id, positionMs, safeDuration, isSeeking) {
            if (!isSeeking) pendingProgress = (positionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
        }
        Slider(
            value = pendingProgress,
            onValueChange = { isSeeking = true; pendingProgress = it },
            onValueChangeFinished = { onSeek((pendingProgress * safeDuration).toLong()); isSeeking = false },
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().height(if (compact) 28.dp else 36.dp),
        )
        PlaybackTimeRow(track, positionMs, durationMs, 11f)
        if (landscape) {
            LandscapeTransportControls(
                sizes = if (compact) LYRICS_PAGE_TRANSPORT_COMPACT else LYRICS_PAGE_TRANSPORT,
                isPlaying = isPlaying,
                isLoading = isLoading,
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode,
                queueActive = queueActive,
                onDevices = onDevices,
                onShuffle = onShuffle,
                onPrevious = onPrevious,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onRepeat = onRepeat,
                onQueue = onQueue,
            )
        } else Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BouncyIconButton(onClick = onDevices, modifier = Modifier.align(Alignment.CenterStart).size(if (compact) 38.dp else 46.dp)) {
                Icon(androidx.compose.ui.res.painterResource("record_labs_connect.svg"), "Connected devices", tint = Color.White, modifier = Modifier.size(if (compact) 24.dp else 28.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 20.dp else 34.dp), verticalAlignment = Alignment.CenterVertically) {
                BouncyIconButton(onClick = onPrevious, keyPulse = PlaybackKeyAction.Previous, modifier = Modifier.size(if (compact) 38.dp else 46.dp)) { Icon(Heroicons.Backward, "Previous", tint = Color.White, modifier = Modifier.size(if (compact) 27.dp else 32.dp)) }
                BouncyIconButton(onClick = onPlayPause, keyPulse = PlaybackKeyAction.PlayPause, modifier = Modifier.size(if (compact) 52.dp else 66.dp), enabled = !isLoading || isPlaying) {
                    if (isLoading) {
                        androidx.compose.material.CircularProgressIndicator(Modifier.size(if (compact) 32.dp else 44.dp), color = Color.White, strokeWidth = 3.dp)
                    } else {
                        Icon(if (isPlaying) Heroicons.PauseSolid else Heroicons.PlaySolid, "Play or pause", tint = Color.White, modifier = Modifier.size(if (compact) 44.dp else 56.dp))
                    }
                }
                BouncyIconButton(onClick = onNext, keyPulse = PlaybackKeyAction.Next, modifier = Modifier.size(if (compact) 38.dp else 46.dp)) { Icon(Heroicons.Forward, "Next", tint = Color.White, modifier = Modifier.size(if (compact) 27.dp else 32.dp)) }
            }
        }
        if (!landscape) PlayerQuickActionsBar(
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode,
            lyricsActive = true,
            queueActive = queueActive,
            sleepTimerActive = sleepTimerActive,
            onQueue = onQueue,
            onSleepTimer = onSleepTimer,
            onShuffle = onShuffle,
            onLyrics = onLyrics,
            onRepeat = onRepeat,
            onMore = onSongMenu,
        )
        VolumeControl(volume, onVolume)
    }
}

@Composable
private fun LyricsToolsDialog(
    track: Track,
    textSize: Float,
    onTextSize: (Float) -> Unit,
    onEdit: () -> Unit,
    onRefetch: () -> Unit,
    onSearch: () -> Unit,
    onCopy: () -> Unit,
    lyricsOffsetMs: Long,
    onEditOffset: () -> Unit,
    pronunciationEnabled: Boolean,
    onPronunciationEnabled: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lyrics", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LyricsToolsItems(
                    textSize = textSize,
                    onTextSize = onTextSize,
                    onEdit = onEdit,
                    onRefetch = onRefetch,
                    onSearch = onSearch,
                    onCopy = onCopy,
                    lyricsOffsetMs = lyricsOffsetMs,
                    onEditOffset = onEditOffset,
                    pronunciationEnabled = pronunciationEnabled,
                    onPronunciationEnabled = onPronunciationEnabled,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

/** Lyrics tools: edit/refetch/search/copy, text size, offset, floating lyrics, pronunciation. */
@Composable
private fun LyricsToolsItems(
    textSize: Float,
    onTextSize: (Float) -> Unit,
    onEdit: () -> Unit,
    onRefetch: () -> Unit,
    onSearch: () -> Unit,
    onCopy: () -> Unit,
    lyricsOffsetMs: Long,
    onEditOffset: () -> Unit,
    pronunciationEnabled: Boolean,
    onPronunciationEnabled: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(
            Triple("Edit", Icons.Default.Edit, onEdit),
            Triple("Refetch", Icons.Default.Refresh, onRefetch),
            Triple("Search", Icons.Default.Search, onSearch),
            Triple("Copy", Icons.Default.ContentCopy, onCopy),
        ).forEach { (label, icon, action) ->
            Column(Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = action).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, label, tint = Accent)
                Text(label, fontSize = 12.sp)
            }
        }
    }
    Divider(color = Color.White.copy(alpha = .12f))
    Text("Lyrics text size — ${textSize.toInt()} sp", fontWeight = FontWeight.SemiBold)
    Slider(value = textSize, onValueChange = onTextSize, valueRange = 20f..68f)
    DropdownMenuItem(onClick = { }) {
        Icon(Icons.Default.HourglassEmpty, null, tint = Accent)
        Spacer(Modifier.width(12.dp)); Text("Show interval indicator")
        Spacer(Modifier.weight(1f)); Switch(true, {})
    }
    DropdownMenuItem(onClick = onEditOffset) {
        Icon(Icons.Default.Tune, null, tint = Accent)
        Spacer(Modifier.width(12.dp)); Text("Lyrics offset")
        Spacer(Modifier.weight(1f)); Text(
            when {
                lyricsOffsetMs > 0L -> "Delay +${lyricsOffsetMs} ms"
                lyricsOffsetMs < 0L -> "Advance ${-lyricsOffsetMs} ms"
                else -> "0 ms"
            },
            color = SecondaryText,
        )
    }
    DropdownMenuItem(onClick = {
        FloatingLyricsState.togglePip()
    }) {
        Icon(Icons.Default.PictureInPictureAlt, null, tint = Accent)
        Spacer(Modifier.width(12.dp)); Text("Floating lyrics (PiP)")
        Spacer(Modifier.weight(1f))
        Switch(
            checked = FloatingLyricsState.isEnabled,
            onCheckedChange = {
                FloatingLyricsState.setPipEnabled(it)
            }
        )
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Translate, null, tint = Accent)
        Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Pronunciation"); Text("Shown automatically for non-Latin lyrics", color = SecondaryText, fontSize = 11.sp) }
        Switch(checked = pronunciationEnabled, onCheckedChange = onPronunciationEnabled)
    }
    LyricsTranslationLanguageItem()
}

/**
 * Landscape lyrics page: one menu with the song actions and the lyrics tools
 * (the two menus of the earlier layout, merged).
 */
@Composable
private fun LyricsPageMenuDialog(
    track: Track,
    onStartRadio: () -> Unit,
    onShare: () -> Unit,
    onViewArtist: () -> Unit,
    onViewAlbum: () -> Unit,
    onAddToPlaylist: () -> Unit,
    isLiked: Boolean,
    onToggleLiked: () -> Unit,
    onAdvanced: () -> Unit,
    onDownload: () -> Unit,
    onListenTogether: () -> Unit,
    onDetails: () -> Unit,
    textSize: Float,
    onTextSize: (Float) -> Unit,
    onEdit: () -> Unit,
    onRefetch: () -> Unit,
    onSearch: () -> Unit,
    onCopy: () -> Unit,
    lyricsOffsetMs: Long,
    onEditOffset: () -> Unit,
    pronunciationEnabled: Boolean,
    onPronunciationEnabled: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    // Fits short windows too: the list scrolls, the Close button stays visible.
    val windowHeight = with(LocalDensity.current) { androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.height.toDp() }
    val maxListHeight = (windowHeight * 0.55f).coerceIn(220.dp, 560.dp)
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            // Lyrics tools first, then the song (header + actions).
            Column(
                Modifier.dialogScrollFix().heightIn(max = maxListHeight).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Lyrics", fontWeight = FontWeight.Bold)
                LyricsToolsItems(
                    textSize = textSize,
                    onTextSize = onTextSize,
                    onEdit = onEdit,
                    onRefetch = onRefetch,
                    onSearch = onSearch,
                    onCopy = onCopy,
                    lyricsOffsetMs = lyricsOffsetMs,
                    onEditOffset = onEditOffset,
                    pronunciationEnabled = pronunciationEnabled,
                    onPronunciationEnabled = onPronunciationEnabled,
                )
                Divider(color = Color.White.copy(alpha = .12f))
                // Same look as the song header that used to be the dialog title.
                androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material.LocalContentAlpha provides androidx.compose.material.ContentAlpha.high) {
                    androidx.compose.material.ProvideTextStyle(MaterialTheme.typography.subtitle1) {
                        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Artwork(track.artworkUrl, Modifier.size(44.dp), RoundedCornerShape(8.dp))
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(track.title, maxLines = 1, fontWeight = FontWeight.Bold)
                                ExplicitArtistText(track, color = SecondaryText, fontSize = 13.sp)
                            }
                        }
                    }
                }
                LyricsSongMenuItems(
                    track = track,
                    onStartRadio = onStartRadio,
                    onShare = onShare,
                    onViewArtist = onViewArtist,
                    onViewAlbum = onViewAlbum,
                    onAddToPlaylist = onAddToPlaylist,
                    isLiked = isLiked,
                    onToggleLiked = onToggleLiked,
                    onAdvanced = onAdvanced,
                    onDownload = onDownload,
                    onListenTogether = onListenTogether,
                    onDetails = onDetails,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun LyricsOffsetDialog(
    offsetMs: Long,
    onOffset: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var pending by remember(offsetMs) { mutableStateOf(offsetMs.toFloat()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lyrics offset", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    when {
                        pending > 0f -> "Lyrics delayed by ${pending.roundToInt()} ms"
                        pending < 0f -> "Lyrics advanced by ${-pending.roundToInt()} ms"
                        else -> "Lyrics follows the original timestamp"
                    },
                    color = SecondaryText,
                )
                Slider(
                    value = pending,
                    onValueChange = { pending = (it / 50f).roundToInt() * 50f },
                    valueRange = -5_000f..5_000f,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Advance", color = SecondaryText, fontSize = 12.sp)
                    Text("Delay", color = SecondaryText, fontSize = 12.sp)
                }
            }
        },
        confirmButton = { TextButton(onClick = { onOffset(pending.roundToInt().toLong()); onDismiss() }) { Text("Apply") } },
        dismissButton = {
            Row {
                TextButton(onClick = { pending = 0f }) { Text("Reset") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
private fun LyricsEditDialog(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit lyrics") },
        text = { TextField(text, { text = it }, modifier = Modifier.fillMaxWidth().height(360.dp).trackTextInputFocus()) },
        confirmButton = { TextButton(onClick = { onSave(text) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * One "Available lyrics" search: what each provider found, kept for the rest
 * of the session, so closing the menu by accident and opening it again shows
 * the same results instead of starting over. The search runs on its own (it
 * is not stopped by closing the menu).
 */
private class LyricsSearchSession(val trackId: String, val title: String, val artist: String) {
    /** Candidates per provider that has answered. */
    val results = androidx.compose.runtime.mutableStateMapOf<String, List<LyricsCandidate>>()
    /** Providers still being asked. */
    val running = androidx.compose.runtime.mutableStateListOf<String>()

    val loading: Boolean get() = running.isNotEmpty()

    fun candidates(): List<LyricsCandidate> = WindowsLyricsProviderRegistry.searchProviderOrder
        .flatMap { results[it].orEmpty() }
        .distinctBy { it.provider to it.lyrics }

    /**
     * Asks [providers] (all by default) that are not being asked already.
     * Results already shown stay until a provider answers again.
     */
    fun load(track: Track, providers: Collection<String> = WindowsLyricsProviderRegistry.searchProviderOrder) {
        val wanted = synchronized(this) {
            providers.filter { it !in running }.also { running += it }
        }
        if (wanted.isEmpty()) return
        LyricsSearchSessions.scope.launch {
            // Providers this search has already released: a later search may
            // have claimed them again, so they must not be released twice.
            val answered = mutableSetOf<String>()
            try {
                WindowsLyricsProviderRegistry.searchProviders(track.copy(title = title, artist = artist), wanted) { provider, found ->
                    synchronized(this@LyricsSearchSession) {
                        // An answer with nothing does not hide what an earlier search found.
                        if (found.isNotEmpty() || provider !in results) results[provider] = found
                        running -= provider
                        answered += provider
                    }
                }
            } finally {
                synchronized(this@LyricsSearchSession) { running.removeAll(wanted.filterNot { it in answered }) }
            }
        }
    }

    /** Providers that have shown nothing yet (not answered, or answered with nothing). */
    fun missing(): List<String> = WindowsLyricsProviderRegistry.searchProviderOrder.filter { results[it].isNullOrEmpty() }
}

private object LyricsSearchSessions {
    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.Default)
    private val sessions = object : LinkedHashMap<String, LyricsSearchSession>(8, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, LyricsSearchSession>?) = size > 12
    }

    fun latest(trackId: String): LyricsSearchSession? = sessions[trackId]

    fun start(track: Track, title: String, artist: String): LyricsSearchSession {
        val existing = sessions[track.id]
        if (existing != null && existing.title == title && existing.artist == artist) return existing
        return LyricsSearchSession(track.id, title, artist).also { sessions[track.id] = it }
    }
}

@Composable
private fun LyricsSearchDialog(track: Track, onDismiss: () -> Unit, onSelect: (LyricsCandidate) -> Unit) {
    // Opening the menu again shows the earlier search of this song as it was.
    var session by remember(track.id) { mutableStateOf(LyricsSearchSessions.latest(track.id)) }
    var editing by remember(track.id) { mutableStateOf(session == null) }
    var title by remember(track.id) { mutableStateOf(session?.title ?: track.title) }
    var artist by remember(track.id) { mutableStateOf(session?.artist ?: track.artist) }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    // Back in the menu: only the providers that showed nothing are asked again.
    LaunchedEffect(track.id) {
        session?.let { earlier -> earlier.missing().takeIf { it.isNotEmpty() }?.let { earlier.load(track, it) } }
    }
    fun search() {
        val current = LyricsSearchSessions.start(track, title.trim(), artist.trim())
        val fresh = current !== session || current.results.isEmpty()
        session = current
        editing = false
        expandedIndex = null
        // The same search again: keep what it found and ask the rest.
        if (fresh) current.load(track) else current.missing().takeIf { it.isNotEmpty() }?.let { current.load(track, it) }
    }
    val activeSession = session
    val loading = activeSession?.loading == true
    val results = if (editing) null else activeSession?.candidates()
    // The list never makes the dialog taller than the window (it scrolls instead).
    val windowHeightPx = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.height
    val windowHeight = with(LocalDensity.current) { windowHeightPx.toDp() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            if (results == null) Text("Search lyrics")
            else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Available lyrics", modifier = Modifier.weight(1f))
                if (loading) {
                    Text("Searching…", color = SecondaryText, fontSize = 12.sp)
                    Spacer(Modifier.width(10.dp))
                }
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    if (loading) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colors.primary, strokeWidth = 2.5.dp)
                    } else {
                        // Asks every provider again; results stay until each one answers.
                        IconButton(onClick = { activeSession?.load(track) }) {
                            Icon(Icons.Default.Refresh, "Load lyrics again", tint = Accent)
                        }
                    }
                }
            }
        },
        text = {
            if (results == null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(title, { title = it }, label = { Text("Song title") }, singleLine = true, modifier = Modifier.trackTextInputFocus())
                    TextField(artist, { artist = it }, label = { Text("Song artists") }, singleLine = true, modifier = Modifier.trackTextInputFocus())
                }
            } else {
                val resultCount = results.size
                val dialogWidth = (520 + (resultCount.coerceAtMost(7) * 38)).dp.coerceAtMost(800.dp)
                val dialogHeight = (180 + (resultCount.coerceAtMost(6) * 104)).dp.coerceIn(300.dp, 720.dp)
                    .coerceAtMost((windowHeight - 260.dp).coerceAtLeast(200.dp))
                LazyColumn(
                    Modifier.dialogScrollFix().width(dialogWidth).height(dialogHeight),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(results.indices.toList()) { index ->
                        val candidate = results[index]
                        val expanded = expandedIndex == index
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Elevated).padding(14.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(candidate.provider, color = Accent, fontWeight = FontWeight.Bold)
                                    Text("Result ${results.take(index + 1).count { it.provider == candidate.provider }}", color = SecondaryText, fontSize = 11.sp)
                                }
                                TextButton(onClick = { expandedIndex = if (expanded) null else index }) { Text(if (expanded) "Collapse" else "Expand") }
                            }
                            val preview = candidate.lyrics.lineSequence().filter { it.isNotBlank() }
                                .take(if (expanded) 160 else 4).joinToString("\n")
                            Text(
                                preview,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White.copy(alpha = .84f),
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                maxLines = if (expanded) 160 else 4,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                            GlassButton(prominent = true, onClick = { onSelect(candidate) }, modifier = Modifier.align(Alignment.End)) { Text("Use this lyrics") }
                        }
                    }
                    if (results.isEmpty() && !loading) item { Text("No lyrics candidates found.", color = SecondaryText) }
                    if (loading) item {
                        Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(Modifier.size(28.dp), color = MaterialTheme.colors.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (results == null) TextButton(
                enabled = title.isNotBlank() && artist.isNotBlank(),
                onClick = { search() },
            ) { Text("Search online") }
            else TextButton(onClick = { editing = true }) { Text("Change search") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun LyricsOptionsDialog(
    track: Track,
    textSize: Float,
    onTextSize: (Float) -> Unit,
    onRefetch: () -> Unit,
    onStartRadio: () -> Unit,
    onShare: () -> Unit,
    onViewArtist: () -> Unit,
    onViewAlbum: () -> Unit,
    onAddToPlaylist: () -> Unit,
    isLiked: Boolean,
    onToggleLiked: () -> Unit,
    onDismiss: () -> Unit,
    onAdvanced: () -> Unit,
    onDownload: () -> Unit,
    onListenTogether: () -> Unit,
    onDetails: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Artwork(track.artworkUrl, Modifier.size(44.dp), RoundedCornerShape(8.dp))
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(track.title, maxLines = 1, fontWeight = FontWeight.Bold)
                    ExplicitArtistText(track, color = SecondaryText, fontSize = 13.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LyricsSongMenuItems(
                    track = track,
                    onStartRadio = onStartRadio,
                    onShare = onShare,
                    onViewArtist = onViewArtist,
                    onViewAlbum = onViewAlbum,
                    onAddToPlaylist = onAddToPlaylist,
                    isLiked = isLiked,
                    onToggleLiked = onToggleLiked,
                    onAdvanced = onAdvanced,
                    onDownload = onDownload,
                    onListenTogether = onListenTogether,
                    onDetails = onDetails,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

/** Song actions of the lyrics page menu. */
@Composable
private fun LyricsSongMenuItems(
    track: Track,
    onStartRadio: () -> Unit,
    onShare: () -> Unit,
    onViewArtist: () -> Unit,
    onViewAlbum: () -> Unit,
    onAddToPlaylist: () -> Unit,
    isLiked: Boolean,
    onToggleLiked: () -> Unit,
    onAdvanced: () -> Unit,
    onDownload: () -> Unit,
    onListenTogether: () -> Unit,
    onDetails: () -> Unit,
) {
    DropdownMenuItem(onClick = onViewArtist) {
        Icon(Icons.Default.Person, null, tint = Accent)
        Spacer(Modifier.width(12.dp))
        Column {
            Text("View artist")
            ExplicitArtistText(track, color = SecondaryText, fontSize = 12.sp)
        }
    }
    if (track.album.isNotBlank()) {
        DropdownMenuItem(onClick = onViewAlbum) {
            Icon(Icons.Default.Album, null, tint = Accent)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("View album")
                Text(track.album, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
    DropdownMenuItem(onClick = onAddToPlaylist) {
        Icon(Icons.Default.Add, null, tint = Accent)
        Spacer(Modifier.width(12.dp))
        Text("Add to playlist")
    }
    DropdownMenuItem(onClick = onToggleLiked) {
        Icon(if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = Accent)
        Spacer(Modifier.width(12.dp))
        Text(if (isLiked) "Remove from library" else "Add to library")
    }
    DropdownMenuItem(onClick = onStartRadio) {
        Icon(Icons.Default.Radio, null, tint = Accent)
        Spacer(Modifier.width(12.dp))
        Text("Start radio")
    }
    DropdownMenuItem(onClick = onShare) {
        Icon(Icons.Default.Share, null, tint = Accent)
        Spacer(Modifier.width(12.dp))
        Text("Share / copy link")
    }
    DropdownMenuItem(onClick = onAdvanced) {
        Icon(Icons.Default.Tune, null, tint = Accent)
        Spacer(Modifier.width(12.dp))
        Text("Advanced — tempo and pitch")
    }
    DropdownMenuItem(onClick = onDownload) {
        Icon(Icons.Default.Download, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Download")
    }
    DropdownMenuItem(onClick = onListenTogether) {
        Icon(Icons.Default.QueueMusic, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Listen Together")
    }
    DropdownMenuItem(onClick = onDetails) {
        Icon(Icons.Default.Info, null, tint = Accent); Spacer(Modifier.width(12.dp)); Text("Details")
    }
}

@Composable
private fun AdvancedTempoPitchDialog(
    initialTempo: Float,
    initialPitch: Int,
    onDismiss: () -> Unit,
    onChange: (Float, Int) -> Unit,
) {
    var rate by remember { mutableStateOf(initialTempo) }
    var pitch by remember { mutableStateOf(initialPitch) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tempo and Pitch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, null, tint = Accent)
                    IconButton(onClick = {
                        rate = (rate - .05f).coerceAtLeast(.5f)
                        onChange(rate, pitch)
                    }) { Text("−", fontSize = 24.sp) }
                    Text("x${"%.2f".format(rate)}", modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    IconButton(onClick = {
                        rate = (rate + .05f).coerceAtMost(2f)
                        onChange(rate, pitch)
                    }) { Text("+", fontSize = 24.sp) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, null, tint = Accent)
                    IconButton(onClick = {
                        pitch = (pitch - 1).coerceAtLeast(-12)
                        onChange(rate, pitch)
                    }) { Text("−", fontSize = 24.sp) }
                    Text("$pitch semitone", modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    IconButton(onClick = {
                        pitch = (pitch + 1).coerceAtMost(12)
                        onChange(rate, pitch)
                    }) { Text("+", fontSize = 24.sp) }
                }
                Text("Changes are applied immediately.", color = SecondaryText, fontSize = 11.sp)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = {
            TextButton(onClick = {
                rate = 1f
                pitch = 0
                onChange(rate, pitch)
            }) {
                Text("Reset")
            }
        },
    )
}

/**
 * For scrolling content inside an AlertDialog. The dialog places its content
 * by the content's first line of text; while scrolling that line moves, and the
 * dialog pushed the whole list down by the scrolled amount (an empty band at
 * the top, the upper items gone). This keeps reporting the first line as it was
 * before any scrolling, so the list stays where it is.
 */
@Composable
internal fun Modifier.dialogScrollFix(): Modifier {
    val firstBaseline = remember { intArrayOf(Int.MIN_VALUE) }
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val baseline = placeable[androidx.compose.ui.layout.FirstBaseline]
        if (firstBaseline[0] == Int.MIN_VALUE && baseline != androidx.compose.ui.layout.AlignmentLine.Unspecified) {
            firstBaseline[0] = baseline
        }
        val lines: Map<androidx.compose.ui.layout.AlignmentLine, Int> =
            if (firstBaseline[0] == Int.MIN_VALUE) emptyMap()
            else mapOf(androidx.compose.ui.layout.FirstBaseline to firstBaseline[0])
        layout(placeable.width, placeable.height, lines) { placeable.place(0, 0) }
    }
}

@Composable
private fun PlaylistPickerDialog(
    playlists: List<WindowsPlaylist>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to playlist") },
        text = {
            if (playlists.isEmpty()) {
                Text("Create a playlist from Library first.", color = SecondaryText)
            } else {
                LazyColumn(Modifier.dialogScrollFix().height(280.dp)) {
                    items(playlists) { playlist ->
                        DropdownMenuItem(onClick = { onSelect(playlist.name) }) {
                            Icon(Icons.Default.LibraryMusic, null, tint = Accent)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(playlist.name)
                                Text("${playlist.tracks.size} tracks", color = SecondaryText, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

private fun parseLyrics(raw: String): List<RichLyricLine> = WindowsRichLyrics.parse(raw)

private data class RichLyricGroup(val startMs: Long, val lines: List<RichLyricLine>)

private fun groupRichLyrics(lines: List<RichLyricLine>): List<RichLyricGroup> {
    // Android keeps every LyricsEntry as an independent list item. Timestamp
    // proximity never merges a background lyric into a main-vocal container.
    return lines.sortedBy(RichLyricLine::startMs)
        .map { line -> RichLyricGroup(line.startMs, listOf(line)) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LyricsLines(
    groups: List<RichLyricGroup>,
    activeGroupIndex: Int,
    positionMs: Long,
    isPlaying: Boolean,
    modifier: Modifier,
    isLoading: Boolean,
    hasError: Boolean,
    provider: String,
    onSeek: (Long) -> Unit,
    textSize: Float,
    pronunciationEnabled: Boolean,
) {
    var manualBrowseOverride by remember { mutableStateOf(false) }
    // While the main window is minimized the lyrics stand still at the last
    // position shown (the music plays on) and catch up when it is back.
    val windowShown = MainWindowActivity.isShown
    val heldPosition = remember(groups) { longArrayOf(positionMs, activeGroupIndex.toLong()) }
    if (windowShown) {
        heldPosition[0] = positionMs
        heldPosition[1] = activeGroupIndex.toLong()
    }
    val isPlaying = isPlaying && windowShown
    val activeGroupIndex = if (windowShown) activeGroupIndex else heldPosition[1].toInt()
    // Structural lyric layout follows the native playback snapshots. Smooth
    // per-word progress is extrapolated inside the active row only, avoiding a
    // 60-fps recomposition of every off-focus lyric and its blur/layout state.
    val displayPositionMs = if (windowShown) positionMs else heldPosition[0]
    val mainIndices = remember(groups) {
        groups.indices.filter { index -> groups[index].lines.any { !it.isBackground } }
    }
    val hasWordTimings = remember(groups) {
        groups.any { group -> group.lines.any { line -> !line.isBackground && line.words.isNotEmpty() } }
    }
    // Keep the rendered focus tied to the latest main-vocal timestamp. This is
    // the invariant used by Android's lyrics screen: an active row cannot be
    // displaced above the focus point by a stale/overlapping scroll target.
    val scrollTargetIndex = remember(groups, mainIndices, displayPositionMs, hasWordTimings) {
        val anticipationMs = if (hasWordTimings) 0L else 250L
        mainIndices.lastOrNull { index -> groups[index].startMs <= displayPositionMs + anticipationMs }
            ?: mainIndices.firstOrNull()
            ?: 0
    }

    val itemHeights = remember(groups, textSize) { androidx.compose.runtime.mutableStateMapOf<Int, Int>() }
    var userManualOffset by remember(groups) { mutableStateOf(0f) }
    LaunchedEffect(scrollTargetIndex) {
        if (manualBrowseOverride) {
            val start = userManualOffset
            val animation = androidx.compose.animation.core.Animatable(start)
            animation.animateTo(
                0f,
                tween((kotlin.math.abs(start) / 4f).toInt().coerceIn(200, 600), easing = androidx.compose.animation.core.FastOutSlowInEasing),
            ) { userManualOffset = value }
            userManualOffset = 0f
            manualBrowseOverride = false
        }
    }

    BoxWithConstraints(
        modifier
            .clipToBounds()
            .graphicsLayer { alpha = 0.99f }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        0.0f to androidx.compose.ui.graphics.Color.Transparent,
                        0.15f to androidx.compose.ui.graphics.Color.Black,
                        0.85f to androidx.compose.ui.graphics.Color.Black,
                        1.0f to androidx.compose.ui.graphics.Color.Transparent
                    ),
                    blendMode = androidx.compose.ui.graphics.BlendMode.DstIn
                )
            }
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val change = event.changes.firstOrNull() ?: return@onPointerEvent
                val delta = change.scrollDelta.y
                if (delta != 0f) {
                    manualBrowseOverride = true
                    userManualOffset -= delta * 42f
                    change.consume()
                }
            }
            .pointerInput(groups) {
                detectVerticalDragGestures(
                    onDragStart = { manualBrowseOverride = true },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        userManualOffset += dragAmount
                    },
                )
            },
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LyricsLoadingIndicator() }
        } else if (groups.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (hasError) "Lyrics could not be loaded." else "Lyrics are not available for this track.", color = SecondaryText, fontSize = 18.sp)
            }
        } else {
            val density = LocalDensity.current
            val anchorY = constraints.maxHeight * 0.35f
            val fallbackHeight = with(density) { (textSize * 1.85f).dp.toPx() }
            val gapPx = with(density) { 22.dp.toPx() }
            val indicatorHeightPx = with(density) { 72.dp.toPx() }
            val firstVocalStart = groups.firstOrNull { group -> group.lines.any { !it.isBackground } }?.startMs
            val introGapActive = firstVocalStart != null && firstVocalStart > 4_000L &&
                displayPositionMs in 0L until firstVocalStart
            // Only word-timed lyrics provide a trustworthy vocal end for an
            // interval indicator. Treat the indicator as its own layout item,
            // just like Android, so it can never overlap a lyric row.
            val activeTimedGapAfter = remember(groups, displayPositionMs) {
                (0 until groups.lastIndex).firstOrNull { index ->
                    val vocalEnd = groups[index].lines.mapNotNull { it.endMs ?: it.words.maxOfOrNull { w -> w.endMs } }.maxOfOrNull { it }
                        ?: (groups[index].startMs + (groups[index + 1].startMs - groups[index].startMs).coerceAtMost(8_000L))
                    val nextStart = groups[index + 1].startMs
                    nextStart - vocalEnd > 4_000L && displayPositionMs in vocalEnd until nextStart
                }
            }
            val activeGapAfter = when {
                introGapActive -> -1
                activeTimedGapAfter != null -> activeTimedGapAfter
                else -> null
            }
            val activeGapStartMs = when (activeGapAfter) {
                -1 -> 0L
                null -> null
        else -> groups[activeGapAfter].lines.mapNotNull { it.endMs ?: it.words.maxOfOrNull { w -> w.endMs } }.maxOfOrNull { it } ?: (groups[activeGapAfter].startMs + (groups.getOrNull(activeGapAfter + 1)?.startMs?.minus(groups[activeGapAfter].startMs) ?: 8_000L).coerceAtMost(8_000L))
            }
            val activeGapEndMs = when (activeGapAfter) {
                -1 -> firstVocalStart
                null -> null
                else -> groups.getOrNull(activeGapAfter + 1)?.startMs
            }
            val positions = remember(itemHeights.toMap(), scrollTargetIndex, groups, fallbackHeight, gapPx, indicatorHeightPx, activeGapAfter) {
                val result = mutableMapOf<Int, Float>()
                when (activeGapAfter) {
                    -1 -> {
                        result[0] = indicatorHeightPx + gapPx
                        var y = result.getValue(0)
                        for (index in 0 until groups.lastIndex) {
                            y += (itemHeights[index]?.toFloat() ?: fallbackHeight) + gapPx
                            result[index + 1] = y
                        }
                    }
                    null -> {
                        val target = scrollTargetIndex.coerceIn(0, groups.lastIndex)
                        result[target] = 0f
                        var y = 0f
                        for (index in target - 1 downTo 0) {
                            y -= (itemHeights[index]?.toFloat() ?: fallbackHeight) + gapPx
                            result[index] = y
                        }
                        y = 0f
                        for (index in target until groups.lastIndex) {
                            y += (itemHeights[index]?.toFloat() ?: fallbackHeight) + gapPx
                            result[index + 1] = y
                        }
                    }
                    else -> {
                        val before = activeGapAfter.coerceIn(0, groups.lastIndex - 1)
                        var y = -(itemHeights[before]?.toFloat() ?: fallbackHeight) - gapPx
                        result[before] = y
                        for (index in before - 1 downTo 0) {
                            y -= (itemHeights[index]?.toFloat() ?: fallbackHeight) + gapPx
                            result[index] = y
                        }
                        y = indicatorHeightPx + gapPx
                        result[before + 1] = y
                        for (index in before + 1 until groups.lastIndex) {
                            y += (itemHeights[index]?.toFloat() ?: fallbackHeight) + gapPx
                            result[index + 1] = y
                        }
                    }
                }
                result
            }
            val contentExtent = kotlin.math.abs((positions[groups.lastIndex] ?: 0f) - (positions[0] ?: 0f))
            val clampedManualOffset = userManualOffset.coerceIn(-contentExtent, contentExtent)

            val providerTarget = if (activeGapAfter == -1) {
                anchorY - with(density) { 38.dp.toPx() }
            } else {
                anchorY + (positions[0] ?: 0f) - with(density) { 38.dp.toPx() }
            }
            val providerOffset by animateFloatAsState(
                targetValue = providerTarget,
                animationSpec = tween(550, easing = androidx.compose.animation.core.CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                label = "lyrics-provider-position",
            )
            Text(
                text = if (provider.isBlank()) "Lyrics" else "Lyrics from $provider",
                color = Color.White.copy(alpha = 0.52f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth().offset { IntOffset(0, (providerOffset + clampedManualOffset).roundToInt()) },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            AnimatedVisibility(
                visible = activeGapAfter != null,
                modifier = Modifier.fillMaxWidth()
                    .height(72.dp)
                    .offset { IntOffset(0, (anchorY + clampedManualOffset).roundToInt()) }
                    .zIndex(2f),
                enter = fadeIn(tween(360)),
                exit = fadeOut(tween(300)),
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val start = activeGapStartMs ?: 0L
                    val end = activeGapEndMs ?: (start + 1L)
                    LyricsIntervalIndicator(
                        startMs = start,
                        endMs = end,
                        positionMs = displayPositionMs,
                        isPlaying = isPlaying,
                    )
    }
}

            groups.forEachIndexed { index, group ->
                androidx.compose.runtime.key(index, group.startMs) {
                    val distance = kotlin.math.abs(index - scrollTargetIndex)
                    val targetOffset = anchorY + positions.getOrDefault(index, (index - scrollTargetIndex) * fallbackHeight)
                    val animatedOffset by animateFloatAsState(
                        targetValue = targetOffset,
                        animationSpec = tween(
                            durationMillis = 550,
                            delayMillis = (distance * 18).coerceAtMost(110),
                            easing = androidx.compose.animation.core.CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
                        ),
                        label = "lyric-position-$index",
                    )
                // This must occupy the whole lyric pane. Without fillMaxWidth,
                // End/Center alignment is calculated inside only the widest
                // line, making v2 and background lines look left aligned.
                Column(
                    Modifier.fillMaxWidth()
                        .zIndex(if (index == scrollTargetIndex) 1f else 0f)
                        .offset { IntOffset(0, (animatedOffset + clampedManualOffset).roundToInt()) }
                        .onSizeChanged { itemHeights[index] = it.height },
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    group.lines.sortedBy { if (it.isBackground) 1 else 0 }.forEach { line ->
                        val alignment = when {
                            line.isBackground -> androidx.compose.ui.text.style.TextAlign.Center
                            line.agent.equals("v2", ignoreCase = true) -> androidx.compose.ui.text.style.TextAlign.End
                            else -> androidx.compose.ui.text.style.TextAlign.Start
                        }
                        val nextMainStart = groups.drop(index + 1)
                            .firstOrNull { next -> next.lines.any { !it.isBackground } }
                            ?.startMs
                        // Plain timestamp lyrics have no word/end timing. Keep
                        // the current row active until the following timestamp
                        // actually becomes active instead of expiring after an
                        // arbitrary 2.5 seconds.
                        val lineEnd = line.words.maxOfOrNull { it.endMs }
                            ?: nextMainStart?.minus(1L)
                            ?: Long.MAX_VALUE
                        val precedingMainIndex = (index - 1 downTo 0).firstOrNull { previousIndex ->
                            groups[previousIndex].lines.any { !it.isBackground }
                        }
                        val precedingMain = precedingMainIndex?.let { groups[it].lines.firstOrNull { line -> !line.isBackground } }
                        val precedingNextStart = precedingMainIndex?.let { previousIndex ->
                            groups.drop(previousIndex + 1).firstOrNull { next -> next.lines.any { !it.isBackground } }?.startMs
                        }
                        val precedingEnd = precedingMain?.words?.maxOfOrNull { it.endMs }
                            ?: precedingNextStart?.minus(1L)
                            ?: precedingMain?.let { Long.MAX_VALUE }
                        val previousMainIsActive = precedingMain != null &&
                            displayPositionMs in precedingMain.startMs..(precedingEnd ?: precedingMain.startMs)
                        val backgroundIsActive = line.isBackground && displayPositionMs in line.startMs..lineEnd
                        val isInGapWithMain = line.isBackground && precedingMain != null &&
                            displayPositionMs in precedingMain.startMs..line.startMs
                        // This mirrors Android's bgVisible: the independent
                        // background row fades in with its paired main line,
                        // remains visible through the gap, then uses its own
                        // timestamps for word-level highlighting.
                        val backgroundVisible = line.isBackground
            val lineIsActive = if (line.isBackground) backgroundIsActive
            else if (line.words.isEmpty()) {
                if (line.endMs != null) displayPositionMs in line.startMs..line.endMs else displayPositionMs in line.startMs..(line.startMs + (groups.getOrNull(index + 1)?.startMs?.minus(line.startMs) ?: 8_000L).coerceAtMost(8_000L))
            }
            else displayPositionMs in line.startMs..lineEnd
                        val belowFocus = (index - activeGroupIndex).coerceAtLeast(0)
                        val behindFocus = (activeGroupIndex - index).coerceAtLeast(0)
                        val aheadAlpha = (1f - belowFocus * 0.13f).coerceIn(0.12f, 1f)
                        // A finished lyric remains readable for the following
                        // two timestamps, then fades away behind the focus.
                        val behindAlpha = when (behindFocus) {
                            0 -> 1f
                            1 -> 0.68f
                            2 -> 0.34f
                            else -> 0f
                        }
                        val synchronizedAlpha = minOf(aheadAlpha, behindAlpha)
                        val synchronizedBlur = maxOf(
                            ((belowFocus - 1).coerceAtLeast(0) * 1.35f).coerceAtMost(9f),
                            when (behindFocus) { 0 -> 0f; 1 -> 1.5f; 2 -> 4f; else -> 10f },
                        )
                        // A user who scrolls back is explicitly browsing the
                        // transcript, so hidden historic rows become readable
                        // again until the following auto-sync update.
                        // Multiple vocals can overlap. Every line whose own
                        // timestamp is active must stay sharp regardless of its
                        // list position; depth effects begin only after it ends.
                        val depthAlpha = if (manualBrowseOverride || lineIsActive) 1f else synchronizedAlpha
                        val depthBlur = if (manualBrowseOverride || lineIsActive) 0f else synchronizedBlur
                        val lineTransitionDelay = (
                            distance * 18 + if (lineIsActive) 110 else 0
                        ).coerceAtMost(180)
                        if (line.isBackground) {
                            // Keep the row measured while it fades. Animating
                            // its layout height restarted every surrounding
                            // offset animation on each frame and destroyed the
                            // agent/background ripple cadence.
                            val backgroundPresence by animateFloatAsState(
                                targetValue = if (backgroundVisible) 1f else 0f,
                                animationSpec = tween(
                                    durationMillis = 550,
                                    delayMillis = if (backgroundVisible) lineTransitionDelay else 0,
                                    easing = androidx.compose.animation.core.CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
                                ),
                                label = "background-lyric-presence-$index",
                            )
                            Box(
                                Modifier.fillMaxWidth().graphicsLayer { alpha = backgroundPresence },
                            ) {
                                TimedLyricText(
                                    line, lineIsActive, displayPositionMs, isPlaying, textSize, alignment,
                                    onSeek, depthAlpha, depthBlur, lineTransitionDelay, pronunciationEnabled,
                                )
    }
} else {
                            TimedLyricText(
                                line, lineIsActive, displayPositionMs, isPlaying, textSize, alignment,
                                onSeek, depthAlpha, depthBlur, lineTransitionDelay, pronunciationEnabled,
                            )
    }
}
                }
            }
        }
    }
}
}

@Composable
private fun smoothPlaybackClock(
    key: Any,
    reportedPositionMs: Long,
    isPlaying: Boolean,
    animate: Boolean,
): Long {
    var anchorPositionMs by remember(key) { mutableStateOf(reportedPositionMs) }
    var anchorNanos by remember(key) { mutableStateOf(System.nanoTime()) }
    // frameTick increments every frame solely to trigger recomposition.
    // The elapsed-time calculation reads System.nanoTime() directly at
    // composition time, keeping it on the same clock as anchorNanos and
    // avoiding any race between the two LaunchedEffects.
    var frameTick by remember(key) { mutableStateOf(0L) }
    var previousReportedPositionMs by remember(key) { mutableStateOf(reportedPositionMs) }

    LaunchedEffect(reportedPositionMs, isPlaying, key) {
        val now = System.nanoTime()
        val estimated = if (isPlaying) {
            anchorPositionMs + ((now - anchorNanos) / 1_000_000L).coerceAtLeast(0L)
        } else anchorPositionMs
        // Native playback is sampled every 250 ms. Re-anchoring on every
        // sample made the visual clock jump backwards at roughly 4 fps. A
        // normal late sample may correct the visual clock forward, but never
        // backward; only an actual seek is allowed to reset it to an earlier
        // timestamp.
        // A Connect correction intentionally seeks the controller about
        // 750 ms backward, so treat a 350 ms drop as a real seek while still
        // ignoring tiny native timestamp jitter.
            val backwardSeek = reportedPositionMs + 350L < previousReportedPositionMs
            val visualTooFarAhead = estimated - reportedPositionMs > 400L
            if (!isPlaying || backwardSeek || reportedPositionMs - estimated > 320L || visualTooFarAhead) {
                anchorPositionMs = if (isPlaying && !backwardSeek && !visualTooFarAhead) {
                maxOf(reportedPositionMs, estimated)
            } else reportedPositionMs
            anchorNanos = now
        }
        previousReportedPositionMs = reportedPositionMs
    }
    LaunchedEffect(isPlaying, animate, key) {
        if (!isPlaying || !animate) return@LaunchedEffect
        while (true) withFrameNanos { _ ->
            frameTick++
        }
    }
    @Suppress("UNUSED_EXPRESSION")
    frameTick // read state so this scope is subscribed to recomposition
    return if (isPlaying && animate) {
        anchorPositionMs + ((System.nanoTime() - anchorNanos) / 1_000_000L).coerceAtLeast(0L)
    } else reportedPositionMs
}

@Composable
private fun TimedLyricText(
    line: RichLyricLine,
    isActive: Boolean,
    positionMs: Long,
    isPlaying: Boolean,
    textSize: Float,
    textAlign: androidx.compose.ui.text.style.TextAlign,
    onSeek: (Long) -> Unit,
    depthAlpha: Float,
    depthBlur: Float,
    transitionDelayMillis: Int,
    pronunciationEnabled: Boolean,
) {
    val plain = lyricPlainText(line)
    val transitionEasing = androidx.compose.animation.core.CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val activeEmphasis by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isActive) 350 else 550,
            delayMillis = if (isActive) transitionDelayMillis else 0,
            easing = transitionEasing,
        ),
        label = "lyric-active-fade",
    )
    val smoothPositionMs = smoothPlaybackClock(
        key = "lyric-",
        reportedPositionMs = positionMs,
        isPlaying = isPlaying,
        // Also while the line fades out, so a held word eases back smoothly.
        animate = (isActive || activeEmphasis > 0.001f) && line.words.isNotEmpty(),
    )
    val targetVisualSize = if (isActive) textSize * 1.1f else textSize
    val visualSize by animateFloatAsState(
        targetValue = targetVisualSize,
        animationSpec = tween(
            durationMillis = 450,
            delayMillis = if (isActive) transitionDelayMillis else 0,
            easing = transitionEasing,
        ),
        label = "lyric-scale",
    )
    val renderedAlpha by animateFloatAsState(
        targetValue = depthAlpha,
        animationSpec = tween(
            durationMillis = 550,
            delayMillis = if (isActive) transitionDelayMillis else 0,
            easing = transitionEasing,
        ),
        label = "lyric-depth-fade",
    )
    // Reserve the maximum metrics for every state, then animate only the
    // rendered scale. Active/inactive wrapping therefore stays identical.
    val layoutSize = (textSize * 1.1f) + 4f
    val visualScale = (visualSize / layoutSize).coerceAtMost(1f)
    
    val romanized = remember(plain) { WindowsRomanizer.romanize(plain) }
    
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    
    val globalSweep: Float = if ((isActive || activeEmphasis > 0.001f) && line.words.isNotEmpty()) run {
        var cursor = 0
        var previousStartMs = Long.MIN_VALUE
        line.words.forEachIndexed { index, word ->
            val wordStart = cursor
            val trailing = if (word.trailingSpace && index < line.words.lastIndex) 1 else 0
            val wordEnd = (wordStart + word.text.length).coerceAtMost(plain.length)
            val normalizedStart = maxOf(word.startMs, previousStartMs)
            val nextStart = line.words.getOrNull(index + 1)?.startMs?.takeIf { it > normalizedStart }
            val normalizedEnd = maxOf(word.endMs, normalizedStart + 40L).let { end -> nextStart?.let { minOf(end, it) } ?: end }
            val wordLen = word.text.length + trailing
            when {
                smoothPositionMs >= normalizedEnd -> { /* completed */ }
                smoothPositionMs >= normalizedStart -> {
                    val progress = ((smoothPositionMs - normalizedStart).toFloat() / (normalizedEnd - normalizedStart).coerceAtLeast(1L)).coerceIn(0f, 1f)
                    return@run wordStart.toFloat() + progress * wordLen
                }
                else -> {
                    return@run wordStart.toFloat()
                }
            }
            cursor = wordEnd + trailing
            previousStartMs = normalizedStart
        }
        plain.length.toFloat()
    } else 0f

    // Word-timed lyrics while the line is lit: every letter is drawn on its own,
    // so it can rise a little once lit and, where the highlight lingers (slowly
    // sung), grow and shrink back like under a magnifying glass, with a soft
    // wipe and no seams (like Apple Music). Every other line draws as before.
    // Worked out only once the line is lit (not for every line of the song).
    val floatingGroups = remember(line, plain) { lazy(LazyThreadSafetyMode.NONE) { floatingWordGroups(line, plain) } }
    val fontPx = with(LocalDensity.current) { layoutSize.sp.toPx() }
    val floatingGeometry = remember(textLayoutResult, floatingGroups, line, plain) {
        lazy(LazyThreadSafetyMode.NONE) {
            val layout = textLayoutResult
            val groups = floatingGroups.value
            if (layout == null || groups == null) null else floatingLineGeometry(groups, layout, line, plain)
        }
    }
    val floatingWords: List<FloatingWordFrame>? = if (activeEmphasis <= 0.001f || line.words.isEmpty()) null else
        floatingGeometry.value?.let { geometry -> floatingWordFrames(geometry, smoothPositionMs, globalSweep, activeEmphasis, fontPx) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // The usual blur clips to the line, which would cut off the glow of floating letters.
            .then(
                if (floatingWords != null) Modifier.blur(depthBlur.dp, androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                else Modifier.blur(depthBlur.dp),
            )
            .graphicsLayer {
                alpha = renderedAlpha
                // Fading as a whole would draw into a layer cut to the line, cutting off the
                // glow of floating letters; they fade one by one instead.
                if (floatingWords != null) compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.ModulateAlpha
                scaleX = visualScale
                scaleY = visualScale
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(
                    pivotFractionX = when (textAlign) {
                        androidx.compose.ui.text.style.TextAlign.Start -> 0f
                        androidx.compose.ui.text.style.TextAlign.End -> 1f
                        else -> .5f
                    },
                    pivotFractionY = .5f,
                )
            }
            .clickable(enabled = line.startMs > 0L) { onSeek(line.startMs) },
    ) {
        Box(contentAlignment = when (textAlign) {
            androidx.compose.ui.text.style.TextAlign.Start -> Alignment.TopStart
            androidx.compose.ui.text.style.TextAlign.End -> Alignment.TopEnd
            else -> Alignment.TopCenter
        }, modifier = Modifier.fillMaxWidth()) {
            
            // Inactive Background Text (Handles the shadow without any clipping!)
            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    append(plain)
                    if (activeEmphasis > 0.001f && floatingWords == null) {
                        if (line.words.isNotEmpty()) {
                            val sweepIndex = globalSweep.toInt().coerceIn(0, maxOf(0, plain.length - 1))
                            val sweepRem = globalSweep - sweepIndex
                            
                            // Fully swept characters get full shadow opacity
                            if (sweepIndex > 0) {
                                addStyle(
                                    androidx.compose.ui.text.SpanStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(Color.White.copy(alpha = 0.45f * activeEmphasis), blurRadius = 12f)
                                    ),
                                    0,
                                    sweepIndex
                                )
                            }
                            
                            // The character currently being wiped gets progressive shadow opacity
                            // so it fades in smoothly instead of popping instantly to 100%
                            if (sweepIndex < plain.length) {
                                addStyle(
                                    androidx.compose.ui.text.SpanStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(Color.White.copy(alpha = 0.45f * activeEmphasis * sweepRem.coerceIn(0f, 1f)), blurRadius = 12f)
                                    ),
                                    sweepIndex,
                                    sweepIndex + 1
                                )
                            }
                        } else {
                            addStyle(
                                androidx.compose.ui.text.SpanStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(Color.White.copy(alpha = 0.45f * activeEmphasis), blurRadius = 12f)
                                ),
                                0,
                                plain.length
                            )
                        }
                    }
                },
                color = Color.White.copy(alpha = 0.40f - 0.22f * activeEmphasis),
                fontSize = layoutSize.sp,
                lineHeight = (layoutSize * 1.42f).sp,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                textAlign = textAlign,
                onTextLayout = { textLayoutResult = it },
                // Still laid out (word positions), drawn word by word while floating.
                modifier = Modifier.fillMaxWidth().drawWithContent { if (floatingWords == null) drawContent() },
            )
            
            // Active Swept Text (Pure white text, perfectly masked, NO shadow so nothing gets clipped)
            if (activeEmphasis > 0.001f && floatingWords == null) {
                if (line.words.isEmpty()) {
                    Text(
                        text = plain,
                        color = Color.White.copy(alpha = activeEmphasis),
                        fontSize = layoutSize.sp,
                        lineHeight = (layoutSize * 1.42f).sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = plain,
                        color = Color.White.copy(alpha = activeEmphasis),
                        fontSize = layoutSize.sp,
                        lineHeight = (layoutSize * 1.42f).sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                        textAlign = textAlign,
                        modifier = Modifier.fillMaxWidth().drawWithContent {
                            val layout = textLayoutResult ?: return@drawWithContent
                            
                            val sweepIndex = globalSweep.toInt().coerceIn(0, maxOf(0, plain.length - 1))
                            val sweepRem = globalSweep - sweepIndex
                            val rect = layout.getBoundingBox(sweepIndex)
                            val lineIndex = layout.getLineForOffset(sweepIndex)

                            // Exact pixel X coordinate for the current character fraction
                            val currentX = rect.left + (rect.right - rect.left) * sweepRem

                            // Erase path with DstOut!
                            // Create a comet-like soft gradient wipe:
                            // The leading edge is tight (only bleeds 2px ahead) to prevent leaking into the next character.
                            // The trailing edge is smooth (12px behind) for a buttery fade-in effect.
                            val startFade = currentX - 12f
                            val endFade = currentX + 2f

                            val wipeBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                0.0f to Color.Transparent,
                                1.0f to Color.Black,
                                startX = startFade,
                                endX = endFade
                            )

                            val bounds = androidx.compose.ui.geometry.Rect(-200f, -200f, size.width + 200f, size.height + 200f)
                            drawContext.canvas.saveLayer(bounds, androidx.compose.ui.graphics.Paint())
                            drawContent()
                            
                            // 1. Soft-erase the current active line
                            drawRect(
                                brush = wipeBrush,
                                topLeft = androidx.compose.ui.geometry.Offset(-200f, layout.getLineTop(lineIndex)),
                                size = androidx.compose.ui.geometry.Size(size.width + 400f, layout.getLineBottom(lineIndex) - layout.getLineTop(lineIndex)),
                                blendMode = androidx.compose.ui.graphics.BlendMode.DstOut
                            )

                            // 2. Hard-erase all lines below the active line
                            if (lineIndex < layout.lineCount - 1) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = androidx.compose.ui.geometry.Offset(-200f, layout.getLineBottom(lineIndex)),
                                    size = androidx.compose.ui.geometry.Size(size.width + 400f, size.height + 200f),
                                    blendMode = androidx.compose.ui.graphics.BlendMode.DstOut
                                )
                            }

                            drawContext.canvas.restore()
                        }
                    )
                }
            }
            floatingWords?.let { words ->
                // The line is read out once, from the text underneath.
                // Each letter is drawn once into an image that is then only moved and
                // scaled, so it glides smoothly instead of snapping from pixel to pixel.
                val letterMeasurer = androidx.compose.ui.text.rememberTextMeasurer(cacheSize = 0)
                val letterStyle = androidx.compose.material.LocalTextStyle.current.merge(
                    androidx.compose.ui.text.TextStyle(
                        fontSize = layoutSize.sp,
                        lineHeight = (layoutSize * 1.42f).sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    ),
                )
                val sprites = remember(floatingGeometry, letterStyle, letterMeasurer) { FloatingLetterSprites() }
                val dimAlpha = 0.40f - 0.22f * activeEmphasis
                Spacer(
                    Modifier
                        .matchParentSize()
                        .clearAndSetSemantics {}
                        .drawBehind {
                            val emPx = layoutSize.sp.toPx()
                            // Images are made a few letters per frame, so lighting a line never stalls;
                            // until then a letter is drawn as plain text (it is not moving yet).
                            var budget = FLOATING_LETTER_IMAGES_PER_FRAME
                            words.forEach { word ->
                                val sprite = sprites.find(word.start) ?: if (budget > 0) {
                                    budget--
                                    sprites.put(word.start, floatingLetterSprite(word.text, letterMeasurer, letterStyle))
                                } else {
                                    null
                                }
                                if (sprite != null) {
                                    drawFloatingLetter(word, sprite, sprites.paint, dimAlpha, activeEmphasis, emPx)
                                } else {
                                    val layout = sprites.layout(word.start) {
                                        letterMeasurer.measure(word.text, letterStyle, overflow = androidx.compose.ui.text.style.TextOverflow.Visible, softWrap = false, maxLines = 1)
                                    }
                                    val topLeft = androidx.compose.ui.geometry.Offset(word.x, word.baseline - layout.firstBaseline - word.lift)
                                    drawText(layout, color = Color.White.copy(alpha = dimAlpha), topLeft = topLeft)
                                    if (word.fullyLit || word.wipeX > 0f) {
                                        drawText(layout, color = Color.White.copy(alpha = activeEmphasis * word.litRamp), topLeft = topLeft)
                                    }
                                }
                            }
                        },
                )
            }
        }
        
        if (pronunciationEnabled) romanized?.let { pronunciation ->
            Text(
                text = pronunciation,
                color = Color.White.copy(alpha = .30f + activeEmphasis * .42f),
                fontSize = (layoutSize * .46f).coerceAtLeast(12f).sp,
                lineHeight = (layoutSize * .62f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
        }
        // Translated line (lyrics translation switched on, line in another language).
        // It fades and unfolds in when translation is switched on, and back out
        // (still showing its text) when switched off.
        val translation = LocalLyricsTranslations.current[plain.trim()]
        var shownTranslation by remember { mutableStateOf(translation) }
        if (translation != null) shownTranslation = translation
        androidx.compose.animation.AnimatedVisibility(
            visible = translation != null,
            enter = androidx.compose.animation.fadeIn(tween(420, delayMillis = 80)) +
                androidx.compose.animation.expandVertically(tween(380), expandFrom = Alignment.Top),
            exit = androidx.compose.animation.fadeOut(tween(260)) +
                androidx.compose.animation.shrinkVertically(tween(380, delayMillis = 60), shrinkTowards = Alignment.Top),
        ) {
            Text(
                text = shownTranslation.orEmpty(),
                color = Color.White.copy(alpha = .32f + activeEmphasis * .40f),
                fontSize = (layoutSize * .50f).coerceAtLeast(12f).sp,
                lineHeight = (layoutSize * .66f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
            )
        }
    }
}

/** A lyric line's text as it is shown (word-timed lines are joined from their words). */
private fun lyricPlainText(line: RichLyricLine): String = if (line.words.isEmpty()) line.text else buildString {
    line.words.forEachIndexed { index, word ->
        append(word.text)
        if (word.trailingSpace && index < line.words.lastIndex) append(' ')
    }
}

/** Translations of the shown lyrics, by line text (empty while translation is off). */
private val LocalLyricsTranslations = androidx.compose.runtime.compositionLocalOf { emptyMap<String, String>() }

/**
 * An icon button that gives a little bounce when pressed: it sinks while
 * held and springs back, overshooting slightly, when let go (player and
 * lyrics page controls).
 */
/** Playback actions that buttons on screen also show when triggered by a key. */
private enum class PlaybackKeyAction { PlayPause, Next, Previous }

private object PlaybackKeyPulse {
    val events = kotlinx.coroutines.flow.MutableSharedFlow<PlaybackKeyAction>(extraBufferCapacity = 8)
    fun emit(action: PlaybackKeyAction) { events.tryEmit(action) }
}

@Composable
private fun BouncyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    /** Also bounce when this action comes from the keyboard (Space, media keys). */
    keyPulse: PlaybackKeyAction? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    if (keyPulse != null) {
        LaunchedEffect(keyPulse) {
            PlaybackKeyPulse.events.collect { action ->
                if (action != keyPulse) return@collect
                launch {
                    // The same sink-and-bounce as a click.
                    scale.animateTo(BUTTON_PRESSED_SCALE, tween(90, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                    scale.animateTo(1f, spring(dampingRatio = 0.38f, stiffness = 520f))
                }
            }
        }
    }
    LaunchedEffect(interactionSource) {
        var bounce: kotlinx.coroutines.Job? = null
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is androidx.compose.foundation.interaction.PressInteraction.Press -> {
                    bounce?.cancel()
                    bounce = launch { scale.animateTo(BUTTON_PRESSED_SCALE, spring(dampingRatio = 1f, stiffness = 1_400f)) }
                }
                is androidx.compose.foundation.interaction.PressInteraction.Release,
                is androidx.compose.foundation.interaction.PressInteraction.Cancel -> {
                    bounce?.cancel()
                    bounce = launch {
                        // A quick tap still sinks a little before it bounces back.
                        if (scale.value > BUTTON_PRESSED_SCALE + 0.06f) {
                            scale.animateTo(BUTTON_PRESSED_SCALE + 0.03f, tween(70, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        }
                        scale.animateTo(1f, spring(dampingRatio = 0.38f, stiffness = 520f))
                    }
                }
            }
        }
    }
    IconButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        enabled = enabled,
        interactionSource = interactionSource,
        content = content,
    )
}

/**
 * Text buttons in the see-through "glass" style of the header buttons (Apple's
 * Liquid Glass look): a translucent pill with a thin light edge and a soft
 * highlight at the top, sinking and bouncing back when pressed.
 * [prominent] main actions are a little brighter; [tint] colours the glass
 * (e.g. the accent colour for Play).
 */
@Composable
internal fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    prominent: Boolean = false,
    tint: Color? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is androidx.compose.foundation.interaction.PressInteraction.Press ->
                    launch { scale.animateTo(0.94f, spring(dampingRatio = 1f, stiffness = 1_400f)) }
                is androidx.compose.foundation.interaction.PressInteraction.Release,
                is androidx.compose.foundation.interaction.PressInteraction.Cancel ->
                    launch { scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 520f)) }
            }
        }
    }
    val fill = when {
        tint != null -> tint.copy(alpha = 0.38f)
        prominent -> Color.White.copy(alpha = 0.20f)
        else -> HeaderGlass
    }
    val shape = RoundedCornerShape(50)
    Row(
        modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                alpha = if (enabled) 1f else 0.45f
            }
            .heightIn(min = 40.dp)
            .clip(shape)
            .background(fill)
            // The glass's light edge: brighter along the top, like light on a lens.
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent)))
            .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.26f), Color.White.copy(alpha = 0.08f))), shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = androidx.compose.material.ripple(color = Color.White),
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material.LocalContentColor provides Color.White, androidx.compose.material.LocalContentAlpha provides 1f) {
            androidx.compose.material.ProvideTextStyle(MaterialTheme.typography.button.copy(color = Color.White)) { content() }
        }
    }
}

/** Rows shown in a see-through panel (search suggestions) use glass containers. */
private val LocalGlassListRows = androidx.compose.runtime.staticCompositionLocalOf { false }

/** A list row's container: black normally, a glass pill (like Lyrics | PiP) inside see-through panels. */
@Composable
private fun Modifier.listRowContainer(): Modifier =
    if (LocalGlassListRows.current) {
        this.clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
    } else this.background(Background)

/** How far a pressed player button sinks. */
private const val BUTTON_PRESSED_SCALE = 0.80f

/** Top-right switch for lyrics translation (the language is chosen in the ⋮ menu). */
@Composable
private fun LyricsTranslationButton(
    modifier: Modifier = Modifier,
    // About the size of the full-screen button across the top (the header uses its own size).
    buttonSize: androidx.compose.ui.unit.Dp = 38.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    backing: Color = Color.White.copy(alpha = 0.12f),
) {
    val enabled = LyricsTranslationSettings.enabled
    // Round backing like the Lyrics | PiP pill.
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = backing,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
    ) {
        BouncyIconButton(onClick = { LyricsTranslationSettings.toggle() }, modifier = Modifier.size(buttonSize)) {
            Icon(
                Icons.Default.Translate,
                if (enabled) "Turn off lyrics translation" else "Translate lyrics",
                tint = if (enabled) MaterialTheme.colors.primary else Color.White,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/** The small bar at the top of the player pages: drag the page down or click it to close. */
@kotlin.OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun DismissPill(
    modifier: Modifier,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onClick: () -> Unit,
) {
    var hovered by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (hovered || dragging) 0.75f else 0.4f, tween(160), label = "dismiss-pill-alpha")
    val latestDragStart by rememberUpdatedState(onDragStart)
    val latestDrag by rememberUpdatedState(onDrag)
    val latestDragEnd by rememberUpdatedState(onDragEnd)
    Box(
        modifier
            .size(width = 96.dp, height = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            // Dragging the pill pulls the page down, like dragging the page itself.
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragging = true; latestDragStart() },
                    onDragEnd = { dragging = false; latestDragEnd() },
                    onDragCancel = { dragging = false; latestDragEnd() },
                    onVerticalDrag = { change, amount -> change.consume(); latestDrag(amount) },
                )
            }
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(width = 48.dp, height = 4.dp).background(Color.White.copy(alpha = alpha), RoundedCornerShape(2.dp)))
    }
}

/** Whether the header is showing over the music player / lyrics page. */
private object PlayerChrome {
    var headerShown by mutableStateOf(false)
}

/** ⋮ menu entry: the language lyrics are translated into. */
@Composable
private fun LyricsTranslationLanguageItem() {
    var choosing by remember { mutableStateOf(false) }
    DropdownMenuItem(onClick = { choosing = true }) {
        Icon(Icons.Default.Language, null, tint = Accent)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Translate lyrics to")
            Text(
                if (LyricsTranslationSettings.enabled) "On · use the translate button at the top right" else "Off · use the translate button at the top right",
                color = SecondaryText,
                fontSize = 11.sp,
            )
        }
        Text(LyricsTranslationSettings.nameOf(LyricsTranslationSettings.targetLanguage), color = SecondaryText, maxLines = 1)
    }
    if (choosing) LyricsTranslationLanguageDialog(onDismiss = { choosing = false })
}

@Composable
internal fun LyricsTranslationLanguageDialog(onDismiss: () -> Unit) {
    val windowHeight = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.height
    val density = LocalDensity.current
    val maxListHeight = with(density) { (windowHeight * 0.55f).toDp() }.coerceIn(220.dp, 520.dp)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Translate lyrics to") },
        text = {
            Column {
                Text(
                    "The language of each lyric line is detected automatically; lines already in this language are not translated.",
                    color = SecondaryText,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = maxListHeight).dialogScrollFix()) {
                    items(LyricsTranslationSettings.languages, key = { it.code }) { language ->
                        val selected = language.code == LyricsTranslationSettings.targetLanguage
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    LyricsTranslationSettings.selectLanguage(language.code)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(language.name, modifier = Modifier.weight(1f), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            if (selected) Icon(Icons.Default.Check, null, tint = Accent)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

/** A word (or the syllables of one word) of a word-timed lyric line, as characters of the line. */
private class FloatingWordGroup(
    val start: Int,
    val end: Int,
    val text: String,
    val firstWord: Int,
    val lastWord: Int,
    /** Where its letters start and end: [start], …, [end] (one piece for scripts drawn whole). */
    val letterBounds: IntArray,
)

/** Where every letter of a lit line sits and when the highlight reaches it; the same every frame. */
private class FloatingLineGeometry(
    val layout: androidx.compose.ui.text.TextLayoutResult,
    val groups: List<FloatingWordGroup>,
    val groupLeft: FloatArray,
    val groupRight: FloatArray,
    val groupBaseline: FloatArray,
    val groupStartMs: LongArray,
    val groupEndMs: LongArray,
    val letterStart: IntArray,
    val letterText: Array<String>,
    val letterGroup: IntArray,
    val letterLeft: FloatArray,
    val letterRight: FloatArray,
    /** When the highlight reaches the letter and when it has passed it. */
    val letterLitMs: FloatArray,
    val letterPassedMs: FloatArray,
    /** Top of the paragraph's first line to its baseline (until a letter's own is known). */
    val firstBaseline: Float,
)

/** One letter (or a whole word of a script drawn whole) as drawn in the current frame. */
private data class FloatingWordFrame(
    val start: Int,
    val text: String,
    val x: Float,
    /** Baseline of the letter's line in the paragraph. */
    val baseline: Float,
    val firstBaseline: Float,
    val lift: Float,
    val scale: Float,
    /** Where it grows from, px from its left. */
    val pivotX: Float,
    /** Lit up to here (px from the letter's left); the soft edge is [fade] wide. */
    val wipeX: Float,
    val fade: Float,
    val fullyLit: Boolean,
    val glow: Float,
    /** 0..1 as the letter starts to light, so its glow does not show before it is lit. */
    val litRamp: Float,
)

/** How high a letter rises once the highlight reaches it, in em (a little, like Apple Music). */
private const val LETTER_LIFT_EM = 0.06f
/** How long that rise takes. */
private const val LETTER_LIFT_MS = 600f
/** How much a letter grows at most when the highlight lingers on it. */
private const val LETTER_MAGNIFY = 0.12f
/** How much of that growth pushes the other letters of the word aside (the rest overlaps a little). */
private const val LETTER_SPREAD = 0.6f
/** How far a magnified word may spread past its own ends, in em. */
private const val LETTER_SPREAD_OUT_EM = 0.08f

/** Characters of scripts written without spaces between words (or timed per syllable block). */
private fun isUnspacedScript(c: Char): Boolean = when (Character.UnicodeScript.of(c.code)) {
    Character.UnicodeScript.HAN, Character.UnicodeScript.HIRAGANA, Character.UnicodeScript.KATAKANA,
    Character.UnicodeScript.HANGUL, Character.UnicodeScript.THAI, Character.UnicodeScript.LAO,
    Character.UnicodeScript.KHMER, Character.UnicodeScript.MYANMAR -> true
    else -> false
}

/** Scripts whose letters look the same drawn one by one (others, e.g. Devanagari, join their letters). */
private fun isDrawnPerLetter(codePoint: Int): Boolean = when (Character.UnicodeScript.of(codePoint)) {
    Character.UnicodeScript.LATIN, Character.UnicodeScript.GREEK, Character.UnicodeScript.CYRILLIC,
    Character.UnicodeScript.ARMENIAN, Character.UnicodeScript.GEORGIAN, Character.UnicodeScript.HAN,
    Character.UnicodeScript.HIRAGANA, Character.UnicodeScript.KATAKANA, Character.UnicodeScript.HANGUL,
    Character.UnicodeScript.COMMON, Character.UnicodeScript.INHERITED -> true
    else -> false
}

private fun floatingLetterBounds(plain: String, start: Int, end: Int): IntArray {
    val text = plain.substring(start, end)
    var index = 0
    while (index < text.length) {
        val codePoint = text.codePointAt(index)
        // Emoji sequences (joined, flags, keycaps) are drawn whole as well.
        if (!isDrawnPerLetter(codePoint) || codePoint > 0xFFFF || codePoint == 0x200D || codePoint == 0x20E3 || codePoint == 0xFE0F) {
            return intArrayOf(start, end)
        }
        index += Character.charCount(codePoint)
    }
    val bounds = ArrayList<Int>(text.length + 1)
    bounds += start
    // Whole characters (an accent stays with its letter, an emoji stays whole).
    val breaks = java.text.BreakIterator.getCharacterInstance()
    breaks.setText(text)
    var next = breaks.next()
    while (next != java.text.BreakIterator.DONE && next < text.length) {
        // Letters a font may join into one (fi, fl, ff, ft, fj) stay together.
        if (text[next - 1] != 'f' || text[next] !in "fijlt") bounds += start + next
        next = breaks.next()
    }
    bounds += end
    return bounds.toIntArray()
}

private fun floatingWordGroups(line: RichLyricLine, plain: String): List<FloatingWordGroup>? {
    if (line.words.isEmpty()) return null
    val groups = mutableListOf<FloatingWordGroup>()
    var cursor = 0
    var groupStart = -1
    var groupFirst = 0
    line.words.forEachIndexed { index, word ->
        val wordStart = cursor
        val wordEnd = (wordStart + word.text.length).coerceAtMost(plain.length)
        val trailing = if (word.trailingSpace && index < line.words.lastIndex) 1 else 0
        if (groupStart < 0) { groupStart = wordStart; groupFirst = index }
        val next = line.words.getOrNull(index + 1)
        // Pieces of one word ("don"+"'t", "beau-"+"ti"): no space between them,
        // except in scripts written without spaces (each character floats).
        val lastChar = word.text.lastOrNull()
        val nextChar = next?.text?.firstOrNull()
        val joinsNext = next != null && trailing == 0 && lastChar != null && nextChar != null &&
            !lastChar.isWhitespace() && !nextChar.isWhitespace() &&
            !isUnspacedScript(lastChar) && !isUnspacedScript(nextChar)
        if (!joinsNext) {
            // Letters only: a space before or after the word stays out of it.
            var s = groupStart
            var e = wordEnd
            while (s < e && plain[s].isWhitespace()) s++
            while (e > s && plain[e - 1].isWhitespace()) e--
            if (e > s) groups += FloatingWordGroup(s, e, plain.substring(s, e), groupFirst, index, floatingLetterBounds(plain, s, e))
            groupStart = -1
        }
        cursor = wordEnd + trailing
    }
    return groups.takeIf { it.isNotEmpty() }
}

/**
 * Positions and timing of every letter; null when the line cannot be drawn
 * letter by letter (a word broken over two lines, right-to-left text, a layout
 * of other text), which keeps the earlier drawing.
 */
private fun floatingLineGeometry(
    groups: List<FloatingWordGroup>,
    layout: androidx.compose.ui.text.TextLayoutResult,
    line: RichLyricLine,
    plain: String,
): FloatingLineGeometry? {
    if (layout.layoutInput.text.text != plain || layout.lineCount == 0) return null
    val count = line.words.size
    // Word times as the sweep uses them, and the characters each word's sweep covers.
    val starts = LongArray(count)
    val ends = LongArray(count)
    val pieceStart = IntArray(count)
    val pieceLength = IntArray(count)
    var previousStartMs = Long.MIN_VALUE
    var cursor = 0
    line.words.forEachIndexed { index, word ->
        val normalizedStart = maxOf(word.startMs, previousStartMs)
        val nextStart = line.words.getOrNull(index + 1)?.startMs?.takeIf { it > normalizedStart }
        starts[index] = normalizedStart
        ends[index] = maxOf(word.endMs, normalizedStart + 40L).let { end -> nextStart?.let { minOf(end, it) } ?: end }
        previousStartMs = normalizedStart
        val trailing = if (word.trailingSpace && index < line.words.lastIndex) 1 else 0
        pieceStart[index] = cursor
        pieceLength[index] = word.text.length + trailing
        cursor = (cursor + word.text.length).coerceAtMost(plain.length) + trailing
    }
    fun sweptAt(offset: Int, firstWord: Int, lastWord: Int): Float {
        for (index in firstWord..lastWord) {
            val length = pieceLength[index]
            if (length > 0 && offset < pieceStart[index] + length) {
                val fraction = ((offset - pieceStart[index]).toFloat() / length).coerceIn(0f, 1f)
                return starts[index] + fraction * (ends[index] - starts[index])
            }
        }
        return ends[lastWord].toFloat()
    }
    val groupCount = groups.size
    val groupLeft = FloatArray(groupCount)
    val groupRight = FloatArray(groupCount)
    val groupBaseline = FloatArray(groupCount)
    val groupStartMs = LongArray(groupCount)
    val groupEndMs = LongArray(groupCount)
    val letterCount = groups.sumOf { it.letterBounds.size - 1 }
    val letterStart = IntArray(letterCount)
    val letterText = Array(letterCount) { "" }
    val letterGroup = IntArray(letterCount)
    val letterLeft = FloatArray(letterCount)
    val letterRight = FloatArray(letterCount)
    val letterLitMs = FloatArray(letterCount)
    val letterPassedMs = FloatArray(letterCount)
    var letter = 0
    groups.forEachIndexed { groupIndex, group ->
        val lineIndex = layout.getLineForOffset(group.start)
        if (layout.getLineForOffset(group.end - 1) != lineIndex) return null
        if (layout.getBidiRunDirection(group.start) == androidx.compose.ui.text.style.ResolvedTextDirection.Rtl) return null
        val left = layout.getBoundingBox(group.start).left
        val right = layout.getBoundingBox(group.end - 1).right
        if (right < left) return null
        groupLeft[groupIndex] = left
        groupRight[groupIndex] = right
        groupBaseline[groupIndex] = layout.getLineBaseline(lineIndex)
        val startMs = starts[group.firstWord]
        groupStartMs[groupIndex] = startMs
        groupEndMs[groupIndex] = ends[group.lastWord].coerceAtLeast(startMs + 1L)
        val bounds = group.letterBounds
        for (piece in 0 until bounds.size - 1) {
            val from = bounds[piece]
            val to = bounds[piece + 1]
            var letterLeftPx = Float.MAX_VALUE
            var letterRightPx = -Float.MAX_VALUE
            for (offset in from until to) {
                val box = layout.getBoundingBox(offset)
                letterLeftPx = minOf(letterLeftPx, box.left)
                letterRightPx = maxOf(letterRightPx, box.right)
            }
            letterStart[letter] = from
            letterText[letter] = plain.substring(from, to)
            letterGroup[letter] = groupIndex
            letterLeft[letter] = letterLeftPx
            letterRight[letter] = maxOf(letterRightPx, letterLeftPx)
            letterLitMs[letter] = sweptAt(from, group.firstWord, group.lastWord)
            letterPassedMs[letter] = maxOf(sweptAt(to, group.firstWord, group.lastWord), letterLitMs[letter] + 1f)
            letter++
        }
    }
    return FloatingLineGeometry(
        layout = layout,
        groups = groups,
        groupLeft = groupLeft,
        groupRight = groupRight,
        groupBaseline = groupBaseline,
        groupStartMs = groupStartMs,
        groupEndMs = groupEndMs,
        letterStart = letterStart,
        letterText = letterText,
        letterGroup = letterGroup,
        letterLeft = letterLeft,
        letterRight = letterRight,
        letterLitMs = letterLitMs,
        letterPassedMs = letterPassedMs,
        firstBaseline = layout.getLineBaseline(0) - layout.getLineTop(0),
    )
}

private val LetterGrowEasing = androidx.compose.animation.core.CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
private val LetterShrinkEasing = androidx.compose.animation.core.CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

/**
 * How much a letter is magnified [sinceLitMs] after the highlight reached it:
 * it grows, stays a moment and shrinks back (like Apple Music), each step taking
 * longer the longer the highlight lingered on it ([lingerMs]).
 */
private fun letterMagnifyEnvelope(sinceLitMs: Float, lingerMs: Float): Float {
    if (sinceLitMs <= 0f) return 0f
    val growMs = (lingerMs * 1.4f).coerceIn(200f, 750f)
    val holdMs = lingerMs.coerceIn(0f, 450f)
    val shrinkMs = (lingerMs * 2.0f).coerceIn(300f, 850f)
    return when {
        sinceLitMs < growMs -> LetterGrowEasing.transform(sinceLitMs / growMs)
        sinceLitMs < growMs + holdMs -> 1f
        sinceLitMs < growMs + holdMs + shrinkMs -> 1f - LetterShrinkEasing.transform((sinceLitMs - growMs - holdMs) / shrinkMs)
        else -> 0f
    }
}

/**
 * Every letter for this frame. Once the highlight reaches a letter it rises a
 * little; when the highlight lingers on it (slowly sung) it also grows like
 * under a magnifying glass and shrinks back, the longer it lingers the longer
 * it takes to grow and to shrink back.
 */
private fun floatingWordFrames(
    geometry: FloatingLineGeometry,
    positionMs: Long,
    sweep: Float,
    emphasis: Float,
    fontPx: Float,
): List<FloatingWordFrame> {
    val layout = geometry.layout
    val groups = geometry.groups
    val now = positionMs.toFloat()
    // The soft lit edge of every word, as before (px from the word's left).
    val groupWipe = FloatArray(groups.size)
    val groupFade = FloatArray(groups.size)
    val groupFullyLit = BooleanArray(groups.size)
    groups.forEachIndexed { index, group ->
        val startMs = geometry.groupStartMs[index]
        val endMs = geometry.groupEndMs[index]
        val durationMs = (endMs - startMs).toFloat()
        val timeProgress = (positionMs - startMs).coerceIn(0L, endMs - startMs).toFloat() / durationMs
        val left = geometry.groupLeft[index]
        val right = geometry.groupRight[index]
        // The lit edge follows the sung characters; it is softer on slow words.
        val slowness = ((durationMs - 300f) / 1_200f).coerceIn(0f, 1f)
        val fade = 12f + (fontPx * 0.55f - 12f).coerceAtLeast(0f) * slowness
        val litChars = sweep.coerceIn(group.start.toFloat(), group.end.toFloat())
        val charX = if (litChars >= group.end) right - left else {
            val charIndex = litChars.toInt().coerceIn(group.start, group.end - 1)
            val box = layout.getBoundingBox(charIndex)
            box.left + (box.right - box.left) * (litChars - charIndex) - left
        }
        val fullyLit = positionMs >= endMs
        groupFullyLit[index] = fullyLit
        groupFade[index] = fade
        // Not reached by the sweep yet (e.g. two words starting together): nothing lit.
        groupWipe[index] = if (positionMs <= startMs || (!fullyLit && sweep <= group.start)) 0f else charX + fade * timeProgress
    }
    val lineWidth = layout.size.width.toFloat()
    val letterCount = geometry.letterStart.size
    val scales = FloatArray(letterCount)
    val magnifies = FloatArray(letterCount)
    val rises = FloatArray(letterCount)
    for (letter in 0 until letterCount) {
        val group = geometry.letterGroup[letter]
        val width = geometry.letterRight[letter] - geometry.letterLeft[letter]
        val litMs = geometry.letterLitMs[letter]
        val lingerMs = geometry.letterPassedMs[letter] - litMs
        // How slowly the highlight crosses the word, or this letter of it (ms per
        // em of width; a narrow letter counts as an average one of its word):
        // fast words only rise, slow ones are magnified.
        val groupWidth = geometry.groupRight[group] - geometry.groupLeft[group]
        val groupWidthEm = (groupWidth / fontPx).coerceAtLeast(0.2f)
        val groupMsPerEm = (geometry.groupEndMs[group] - geometry.groupStartMs[group]) / groupWidthEm
        val averageLetter = groupWidth / (groups[group].end - groups[group].start).coerceAtLeast(1)
        val msPerEm = maxOf(groupMsPerEm, lingerMs / (maxOf(width, averageLetter) / fontPx).coerceAtLeast(0.2f))
        val slow = ((msPerEm - 300f) / 250f).coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }
        val magnify = if (slow <= 0f) 0f else slow * letterMagnifyEnvelope(now - litMs, lingerMs)
        magnifies[letter] = magnify
        scales[letter] = 1f + LETTER_MAGNIFY * magnify * emphasis
        rises[letter] = ((now - litMs + 60f) / LETTER_LIFT_MS).coerceIn(0f, 1f).let { 1f - (1f - it) * (1f - it) * (1f - it) }
    }
    // Grown letters make room for each other (partly, like Apple Music), the
    // word spreading from its middle; a word at the edge of the line stays inside it.
    val shifts = FloatArray(letterCount)
    var first = 0
    while (first < letterCount) {
        val group = geometry.letterGroup[first]
        var last = first
        while (last + 1 < letterCount && geometry.letterGroup[last + 1] == group) last++
        var total = 0f
        for (letter in first..last) total += (geometry.letterRight[letter] - geometry.letterLeft[letter]) * (scales[letter] - 1f)
        if (total > 0f) {
            // The word grows outward by at most [LETTER_SPREAD_OUT_EM] on each side,
            // so two slow words next to each other do not run into each other.
            val maxOut = fontPx * LETTER_SPREAD_OUT_EM
            val firstExtra = (geometry.letterRight[first] - geometry.letterLeft[first]) * (scales[first] - 1f)
            val lastExtra = (geometry.letterRight[last] - geometry.letterLeft[last]) * (scales[last] - 1f)
            fun spreadFor(edgeExtra: Float): Float {
                val rest = total / 2f - edgeExtra / 2f
                return if (rest <= 0.0001f) LETTER_SPREAD else ((maxOut - edgeExtra / 2f) / rest).coerceIn(0f, LETTER_SPREAD)
            }
            val spread = minOf(spreadFor(firstExtra), spreadFor(lastExtra))
            var before = 0f
            for (letter in first..last) {
                val extra = (geometry.letterRight[letter] - geometry.letterLeft[letter]) * (scales[letter] - 1f)
                shifts[letter] = spread * (before + extra / 2f - total / 2f)
                before += extra
            }
            fun grownLeft(letter: Int): Float {
                val width = geometry.letterRight[letter] - geometry.letterLeft[letter]
                return geometry.letterLeft[letter] + shifts[letter] + width / 2f * (1f - scales[letter])
            }
            fun grownRight(letter: Int): Float {
                val width = geometry.letterRight[letter] - geometry.letterLeft[letter]
                return geometry.letterLeft[letter] + shifts[letter] + width / 2f * (1f + scales[letter])
            }
            val push = when {
                grownLeft(first) < 0f -> -grownLeft(first)
                grownRight(last) > lineWidth -> lineWidth - grownRight(last)
                else -> 0f
            }
            if (push != 0f) for (letter in first..last) shifts[letter] += push
        }
        first = last + 1
    }
    val frames = ArrayList<FloatingWordFrame>(letterCount)
    for (letter in 0 until letterCount) {
        val group = geometry.letterGroup[letter]
        val left = geometry.letterLeft[letter]
        val width = geometry.letterRight[letter] - left
        val fade = groupFade[group]
        // The lit edge stays where it is on screen while the letters move aside.
        val wipeX = groupWipe[group] - (left - geometry.groupLeft[group]) - shifts[letter]
        val fullyLit = groupFullyLit[group] || wipeX - fade >= width + 40f
        frames += FloatingWordFrame(
            start = geometry.letterStart[letter],
            text = geometry.letterText[letter],
            x = left + shifts[letter],
            baseline = geometry.groupBaseline[group],
            firstBaseline = geometry.firstBaseline,
            lift = fontPx * LETTER_LIFT_EM * rises[letter] * emphasis,
            scale = scales[letter],
            pivotX = width / 2f,
            wipeX = wipeX.coerceAtLeast(0f),
            fade = fade,
            fullyLit = fullyLit,
            glow = magnifies[letter],
            litRamp = if (fullyLit) 1f else (wipeX / fade).coerceIn(0f, 1f),
        )
    }
    return frames
}

/** How much larger than on screen a letter is drawn into its image (kept sharp while magnified). */
private const val LETTER_IMAGE_SCALE = 2f
/** Its glow is soft, so a smaller image is enough. */
private const val LETTER_GLOW_IMAGE_SCALE = 0.5f
/** Room around a glow for its blur, px. */
private const val LETTER_GLOW_PAD = 66f
/** How many letter images are made per frame at most. */
private const val FLOATING_LETTER_IMAGES_PER_FRAME = 8
private val LetterSampling = org.jetbrains.skia.CubicResampler(1f / 3f, 1f / 3f)
private val LetterGlowSampling = org.jetbrains.skia.FilterMipmap(org.jetbrains.skia.FilterMode.LINEAR, org.jetbrains.skia.MipmapMode.NONE)

/** One letter drawn once: white, with a soft glow and a wider glow (for when it is magnified). */
private class FloatingLetterSprite(
    val glyph: org.jetbrains.skia.Image,
    val glyphPad: Float,
    val glow: org.jetbrains.skia.Image,
    val wideGlow: org.jetbrains.skia.Image,
    val width: Float,
    val height: Float,
    /** Top of the letter's box to its baseline. */
    val baseline: Float,
) {
    fun close() {
        glyph.close()
        glow.close()
        wideGlow.close()
    }
}

/** The letter images of one lit line; freed as soon as the line no longer needs them. */
private class FloatingLetterSprites : androidx.compose.runtime.RememberObserver {
    private val sprites = HashMap<Int, FloatingLetterSprite>()
    private val layouts = HashMap<Int, androidx.compose.ui.text.TextLayoutResult>()
    val paint = org.jetbrains.skia.Paint()

    fun find(start: Int): FloatingLetterSprite? = sprites[start]

    fun put(start: Int, sprite: FloatingLetterSprite): FloatingLetterSprite {
        sprites[start] = sprite
        layouts.remove(start)
        return sprite
    }

    fun layout(start: Int, measure: () -> androidx.compose.ui.text.TextLayoutResult) = layouts.getOrPut(start, measure)

    private fun release() {
        sprites.values.forEach { it.close() }
        sprites.clear()
        layouts.clear()
        paint.close()
    }

    override fun onRemembered() = Unit
    override fun onForgotten() = release()
    override fun onAbandoned() = release()
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.floatingLetterSprite(
    text: String,
    measurer: androidx.compose.ui.text.TextMeasurer,
    style: androidx.compose.ui.text.TextStyle,
): FloatingLetterSprite {
    val layout = measurer.measure(text, style, overflow = androidx.compose.ui.text.style.TextOverflow.Visible, softWrap = false, maxLines = 1)
    val glyphPad = kotlin.math.ceil(style.fontSize.toPx() * 0.25f)
    val made = ArrayList<org.jetbrains.skia.Image>(3)
    try {
        made += renderLetterImage(layout, LETTER_IMAGE_SCALE, glyphPad, shadowBlur = null)
        made += renderLetterImage(layout, LETTER_GLOW_IMAGE_SCALE, LETTER_GLOW_PAD, shadowBlur = 12f)
        made += renderLetterImage(layout, LETTER_GLOW_IMAGE_SCALE, LETTER_GLOW_PAD, shadowBlur = 22f)
    } catch (error: Throwable) {
        made.forEach { it.close() }
        throw error
    }
    return FloatingLetterSprite(
        glyph = made[0],
        glyphPad = glyphPad,
        glow = made[1],
        wideGlow = made[2],
        width = layout.size.width.toFloat(),
        height = layout.size.height.toFloat(),
        baseline = layout.firstBaseline,
    )
}

/** The letter (or only its glow, when [shadowBlur] is set) in white, [scale] times larger, [pad] px around it. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderLetterImage(
    layout: androidx.compose.ui.text.TextLayoutResult,
    scale: Float,
    pad: Float,
    shadowBlur: Float?,
): org.jetbrains.skia.Image {
    val width = kotlin.math.ceil((layout.size.width + pad * 2f) * scale).toInt().coerceAtLeast(1)
    val height = kotlin.math.ceil((layout.size.height + pad * 2f) * scale).toInt().coerceAtLeast(1)
    val surface = org.jetbrains.skia.Surface.makeRasterN32Premul(width, height)
    try {
        androidx.compose.ui.graphics.drawscope.CanvasDrawScope().draw(
            this,
            layoutDirection,
            surface.canvas.asComposeCanvas(),
            androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat()),
        ) {
            // The blur of a shadow does not follow the scale, so it is scaled here.
            drawContext.transform.scale(scale, scale, androidx.compose.ui.geometry.Offset.Zero)
            if (shadowBlur == null) {
                drawText(layout, color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(pad, pad))
            } else {
                drawText(
                    layout,
                    color = Color.Transparent,
                    topLeft = androidx.compose.ui.geometry.Offset(pad, pad),
                    shadow = androidx.compose.ui.graphics.Shadow(Color.White, blurRadius = shadowBlur * scale),
                )
            }
        }
        return surface.makeImageSnapshot()
    } finally {
        surface.close()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLetterImage(
    image: org.jetbrains.skia.Image,
    scale: Float,
    pad: Float,
    alpha: Float,
    paint: org.jetbrains.skia.Paint,
    sampling: org.jetbrains.skia.SamplingMode,
) {
    if (alpha <= 0.002f) return
    paint.setAlphaf(alpha.coerceAtMost(1f))
    drawContext.canvas.nativeCanvas.drawImageRect(
        image,
        org.jetbrains.skia.Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
        org.jetbrains.skia.Rect.makeXYWH(-pad, -pad, image.width / scale, image.height / scale),
        sampling,
        paint,
        false,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFloatingLetter(
    word: FloatingWordFrame,
    sprite: FloatingLetterSprite,
    paint: org.jetbrains.skia.Paint,
    dimAlpha: Float,
    emphasis: Float,
    emPx: Float,
) {
    val canvas = drawContext.canvas
    canvas.save()
    drawContext.transform.translate(word.x, word.baseline - sprite.baseline - word.lift)
    // Grows from just above its baseline, as under a magnifying glass.
    drawContext.transform.scale(
        word.scale,
        word.scale,
        androidx.compose.ui.geometry.Offset(word.pivotX, sprite.baseline - emPx * 0.05f),
    )
    drawLetterImage(sprite.glyph, LETTER_IMAGE_SCALE, sprite.glyphPad, dimAlpha, paint, LetterSampling)
    if (word.fullyLit || word.wipeX > 0f) {
        val margin = 160f
        if (!word.fullyLit) {
            canvas.saveLayer(
                androidx.compose.ui.geometry.Rect(-margin, -margin, sprite.width + margin, sprite.height + margin),
                androidx.compose.ui.graphics.Paint(),
            )
        }
        // The glow brightens and widens as the letter is magnified (as before).
        val glowAlpha = emphasis * word.litRamp
        val wide = word.glow.coerceIn(0f, 1f)
        drawLetterImage(sprite.glow, LETTER_GLOW_IMAGE_SCALE, LETTER_GLOW_PAD, 0.45f * (1f - wide) * glowAlpha, paint, LetterGlowSampling)
        drawLetterImage(sprite.wideGlow, LETTER_GLOW_IMAGE_SCALE, LETTER_GLOW_PAD, 0.75f * wide * glowAlpha, paint, LetterGlowSampling)
        drawLetterImage(sprite.glyph, LETTER_IMAGE_SCALE, sprite.glyphPad, emphasis, paint, LetterSampling)
        if (!word.fullyLit) {
            // Soft wipe: lit on the left, fading out over [fade] px at the edge.
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    0f to Color.Transparent,
                    1f to Color.Black,
                    startX = word.wipeX - word.fade,
                    endX = word.wipeX + 2f,
                ),
                topLeft = androidx.compose.ui.geometry.Offset(-margin, -margin),
                size = androidx.compose.ui.geometry.Size(sprite.width + margin * 2, sprite.height + margin * 2),
                blendMode = androidx.compose.ui.graphics.BlendMode.DstOut,
            )
            canvas.restore()
        }
    }
    canvas.restore()
}

@Composable
private fun LyricsLoadingIndicator() {
    val transition = rememberInfiniteTransition(label = "lyrics-loading")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1_600, easing = LinearEasing)),
        label = "lyrics-loading-rotation",
    )
    val pulse by transition.animateFloat(
        initialValue = .82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(820, easing = androidx.compose.animation.core.FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lyrics-loading-pulse",
    )
    val artworkAccent = MaterialTheme.colors.primary
    Canvas(Modifier.size(48.dp).graphicsLayer { rotationZ = rotation; scaleX = pulse; scaleY = pulse }) {
        drawCircle(lerp(Color.Black, artworkAccent, .58f), radius = size.minDimension * .46f)
        drawCircle(lerp(artworkAccent, Color.White, .68f), radius = size.minDimension * .31f, center = center.copy(x = center.x + size.minDimension * .06f))
    }
}

@Composable
private fun LyricsIntervalIndicator(
    startMs: Long,
    endMs: Long,
    positionMs: Long,
    isPlaying: Boolean,
) {
    val displayPositionMs = smoothPlaybackClock(
        key = "progress-$startMs-$endMs",
        reportedPositionMs = positionMs,
        isPlaying = isPlaying,
        animate = true,
    )
    val progress = ((displayPositionMs - startMs).toFloat() / (endMs - startMs).coerceAtLeast(1L))
        .coerceIn(0f, 1f)
    Canvas(Modifier.size(36.dp)) {
        val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        drawArc(Color.White.copy(alpha = .20f), -90f, 360f, false, style = stroke)
        // Position is already extrapolated at display refresh cadence. Drawing
        // it directly avoids continuously restarting a tween and gives the
        // progressive indicator an even, frame-perfect movement.
        val sweepAngle = progress * 360f
        drawArc(Color.White.copy(alpha = .18f), -90f, sweepAngle, false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
        drawArc(Color.White.copy(alpha = .90f), -90f, sweepAngle, false, style = stroke)
        if (progress in 0.001f..0.999f) {
            drawArc(Color.White.copy(alpha = .12f), -90f + sweepAngle - 18f, 18f, false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
            drawArc(Color.White.copy(alpha = .24f), -90f + sweepAngle - 11f, 11f, false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
            drawArc(Color.White.copy(alpha = .42f), -90f + sweepAngle - 5f, 5f, false, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

private fun Track.isLocalLossless(): Boolean = localPath
    ?.substringAfterLast('.', "")
    ?.lowercase() in setOf("flac", "wav", "aiff", "aif", "alac", "ape", "wv")

@Composable
private fun PlaybackTimeRow(track: Track, positionMs: Long, durationMs: Long, fontSize: Float) {
    Box(Modifier.fillMaxWidth()) {
        Text(positionMs.toClock(), color = SecondaryText, fontSize = fontSize.sp, modifier = Modifier.align(Alignment.CenterStart))
        if (track.isLocalLossless()) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White.copy(alpha = .12f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = .10f)),
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("〽", color = Color.White.copy(alpha = .90f), fontSize = (fontSize + 1f).sp)
                    Text("Lossless", color = Color.White.copy(alpha = .90f), fontSize = fontSize.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Text(durationMs.toClock(), color = SecondaryText, fontSize = fontSize.sp, modifier = Modifier.align(Alignment.CenterEnd))
    }
}

private fun Long.toClock(): String = if (this <= 0) "0:00" else "%d:%02d".format(this / 60_000, (this / 1_000) % 60)





private val blurShader = org.jetbrains.skia.RuntimeEffect.makeForShader("""
    uniform shader normal;
    uniform shader blurred;
    uniform float headerHeight;

    vec4 main(vec2 coord) {
        vec4 cNormal = normal.eval(coord);
        vec4 cBlurred = blurred.eval(coord);
        
        float blurEnd = headerHeight * 0.6;
        
        float blurAmount = 0.0;
        if (coord.y < blurEnd) {
            blurAmount = 1.0 - (coord.y / blurEnd);
        }
        
        return mix(cNormal, cBlurred, blurAmount);
    }
""")

@Composable
fun Modifier.progressiveHeaderBlur(compact: Boolean): Modifier {
    if (compact) return this
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val effect = remember(density) {
        val builder = org.jetbrains.skia.RuntimeShaderBuilder(blurShader)
        builder.uniform("headerHeight", 72f * density)
        val normalFilter = org.jetbrains.skia.ImageFilter.makeOffset(0f, 0f, null, null)
        val blurFilter = org.jetbrains.skia.ImageFilter.makeBlur(30f, 30f, org.jetbrains.skia.FilterTileMode.CLAMP, null, null)
        val combinedFilter = org.jetbrains.skia.ImageFilter.makeRuntimeShader(
            builder,
            arrayOf("normal", "blurred"),
            arrayOf(normalFilter, blurFilter)
        )
        combinedFilter.asComposeRenderEffect()
    }
    // The blur spreads past the page's edge; it is cut off on the right so it
    // never shows behind the queue panel (left, top and bottom stay as before).
    val rightEdgeClip = remember {
        androidx.compose.foundation.shape.GenericShape { size, _ ->
            addRect(androidx.compose.ui.geometry.Rect(-100_000f, -100_000f, size.width, size.height + 100_000f))
        }
    }
    return this.graphicsLayer {
        renderEffect = effect
        shape = rightEdgeClip
        clip = true
    }
}
interface RecordLabsDwmapi : com.sun.jna.win32.StdCallLibrary {
    companion object {
        val INSTANCE: RecordLabsDwmapi = com.sun.jna.Native.load("dwmapi", RecordLabsDwmapi::class.java, com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS)
    }
    fun DwmSetWindowAttribute(hwnd: com.sun.jna.platform.win32.WinDef.HWND, attr: Int, attrValue: com.sun.jna.ptr.IntByReference, attrSize: Int): Int
}






@kotlin.OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun ExplicitArtistText(
    track: Track,
    text: String = track.artist,
    color: Color = SecondaryText,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    modifier: Modifier = Modifier,
    textDecoration: androidx.compose.ui.text.style.TextDecoration = androidx.compose.ui.text.style.TextDecoration.None,
) {
    val onOpenCatalog = LocalOpenCatalog.current
    val onOpenTrackArtist = LocalOpenTrackArtist.current
    var hovered by remember { mutableStateOf(false) }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (track.explicit) {
            Box(Modifier.padding(end = 6.dp).size(width = 15.dp, height = 15.dp).background(Color.Transparent, RoundedCornerShape(2.dp)).border(2.dp, color.copy(alpha = 0.6f), RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
                Text("E", color = color.copy(alpha = 0.8f), fontSize = 7.8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = (-4).dp))
            }
        }
        val effDeco = if (hovered && onOpenCatalog != null) androidx.compose.ui.text.style.TextDecoration.Underline else textDecoration
        Text(text, color = color, fontSize = fontSize, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, 
            modifier = Modifier.weight(1f, fill = false)
                .onPointerEvent(PointerEventType.Enter) { hovered = true }
                .onPointerEvent(PointerEventType.Exit) { hovered = false }
                .let { m -> 
                    // The artist's name only, never the rest of the line
                    // ("Artist • 266M plays", "Artist   Album").
                    if (onOpenCatalog != null) m.clickable {
                        val artistName = artistNameForCatalog(track, text)
                        onOpenTrackArtist?.invoke(track, artistName) ?: onOpenCatalog(artistName, "Artist", null, null, null)
                    } 
                    else m 
                }, 
            textDecoration = effDeco)
    }
}

// Decoded artist logos, so returning to an artist page shows the logo at once.
private val artistLogoBitmaps = java.util.concurrent.ConcurrentHashMap<String, androidx.compose.ui.graphics.ImageBitmap>()

/**
 * The artist page's name: the artist's own name logo when Apple Music has one
 * (Apple Music artist pages, iOS 27), otherwise the usual bold text.
 */
@Composable
private fun ArtistNameHeading(name: String, compact: Boolean) {
    // The state belongs to this artist: produceState's own state is not reset
    // when the name changes, so going back to an earlier artist kept the logo
    // of the artist shown last.
    val logoState = remember(name) { mutableStateOf(artistLogoBitmaps[name]) }
    val logo by logoState
    LaunchedEffect(name) {
        var value by logoState
        if (value != null) return@LaunchedEffect
        // A network error is tried again a few times; "no logo" is final.
        var attempt = 0
        while (true) {
            val lookup = withContext(Dispatchers.IO) {
                runCatching {
                    WindowsArtistLogoRepository.logoFile(name)?.readBytes()?.let { bytes ->
                        org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
                    }
                }
            }
            lookup.exceptionOrNull()?.let { if (it is kotlinx.coroutines.CancellationException) throw it }
            if (lookup.isSuccess) {
                value = lookup.getOrNull()?.also { artistLogoBitmaps[name] = it }
                return@LaunchedEffect
            }
            attempt++
            if (attempt >= 6) return@LaunchedEffect
            delay(loadingRetryDelayMs(attempt))
        }
    }
    androidx.compose.animation.Crossfade(logo, animationSpec = tween(320)) { bitmap ->
        if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) {
            Text(name, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        } else {
            Box(
                Modifier
                    .padding(bottom = 6.dp)
                    .widthIn(max = if (compact) 320.dp else 560.dp)
                    .heightIn(max = if (compact) 84.dp else 128.dp)
                    .aspectRatio(bitmap.width.toFloat() / bitmap.height),
            ) {
                // A soft dark shadow of the logo keeps light lettering readable
                // over bright artist pictures.
                androidx.compose.foundation.Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black.copy(alpha = 0.55f)),
                    modifier = Modifier.matchParentSize().offset(y = 2.dp).blur(10.dp, androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded),
                )
                androidx.compose.foundation.Image(
                    bitmap = bitmap,
                    contentDescription = name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}

/** A song row placeholder with the loading shimmer. */
@Composable
private fun ShimmerTrackRow() {
    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).loadingShimmer())
        Column(Modifier.padding(horizontal = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.62f).height(13.dp).clip(RoundedCornerShape(6.dp)).loadingShimmer())
            Box(Modifier.fillMaxWidth(0.38f).height(10.dp).clip(RoundedCornerShape(6.dp)).loadingShimmer())
        }
    }
}

/** The artist page's shape while its first answer is still on the way. */
@Composable
private fun ArtistPageSkeleton(compact: Boolean) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val pageHeight = maxHeight
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().height(pageHeight * 0.7f).loadingShimmer()) {
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .25f), Color.Black))))
                Column(Modifier.align(Alignment.BottomStart).padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.width(if (compact) 200.dp else 320.dp).height(if (compact) 34.dp else 44.dp).clip(RoundedCornerShape(10.dp)).loadingShimmer())
                    Box(Modifier.width(140.dp).height(12.dp).clip(RoundedCornerShape(6.dp)).loadingShimmer())
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.width(104.dp).height(38.dp).clip(RoundedCornerShape(10.dp)).loadingShimmer())
                        Box(Modifier.width(104.dp).height(38.dp).clip(RoundedCornerShape(10.dp)).loadingShimmer())
                    }
                }
            }
            Box(Modifier.padding(start = 24.dp, top = 12.dp, bottom = 12.dp).width(150.dp).height(22.dp).clip(RoundedCornerShape(8.dp)).loadingShimmer())
            Row(Modifier.padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(if (compact) 1 else 3) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { repeat(4) { ShimmerTrackRow() } }
                }
            }
        }
    }
}

/** "266M plays", "1.2B views", "3.4M subscribers", "3:21": stats, not a name. */
private fun String.isListingStatsText(): Boolean {
    val text = trim()
    return text.matches(Regex("\\d{1,2}:\\d{2}(:\\d{2})?")) ||
        text.matches(Regex("(?i)[\\d.,]+\\s*[KMB]?\\s*(plays?|views?|subscribers?|listeners?|monthly audience)"))
}

/** The artist to open when an artist line is clicked: the track's artist, else the line's first name. */
private fun artistNameForCatalog(track: Track, shown: String): String {
    track.artist.trim().takeIf { it.isNotBlank() && it != "YouTube Music" && !it.isListingStatsText() }?.let { return it }
    return shown.split(" • ", "   ", " · ").map(String::trim)
        .firstOrNull { it.isNotBlank() && !it.isListingStatsText() } ?: shown
}

@kotlin.OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun TrackTitleText(
    track: Track,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
) {
    val onOpenCatalog = LocalOpenCatalog.current
    val onOpenTrackAlbum = LocalOpenTrackAlbum.current
    var hovered by remember { mutableStateOf(false) }
    Text(
        text = track.title,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        textDecoration = if (hovered && onOpenCatalog != null) androidx.compose.ui.text.style.TextDecoration.Underline else androidx.compose.ui.text.style.TextDecoration.None,
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .let { m ->
                if (onOpenCatalog != null) {
                    m.clickable {
                        if (onOpenTrackAlbum != null) {
                            onOpenTrackAlbum(track)
                        } else {
                            val albumName = track.album.takeIf { it.isNotBlank() } ?: track.title
                            val query = albumName + " " + track.artist
                            onOpenCatalog(albumName, "Album", null, query, null)
                        }
                    }
                } else m
            }
    )
}
















































