package com.recordlabs.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLayoutResult
import com.recordlabs.desktop.data.RichLyricLine
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.rememberWindowState
import com.recordlabs.desktop.data.Track
import com.recordlabs.desktop.data.WindowsLyricsProviderRegistry
import com.recordlabs.desktop.data.WindowsPreferences
import com.recordlabs.desktop.data.WindowsRichLyrics
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

object FloatingLyricsState {
    var isEnabled by mutableStateOf(WindowsPreferences.floatingLyricsEnabled())
    var isPipMinimized by mutableStateOf(true)
    var currentTrack by mutableStateOf<Track?>(null)
    var isPlaying by mutableStateOf(false)
    var positionMs by mutableStateOf(0L)
    var durationMs by mutableStateOf(0L)
    var onPlayPause: (() -> Unit)? = null
    var onPrevious: (() -> Unit)? = null
    var onNext: (() -> Unit)? = null
    var onSeek: ((Long) -> Unit)? = null
    var requestMinimizeDesktop: (() -> Unit)? = null
    var requestRestoreDesktop: (() -> Unit)? = null

    fun activatePip() {
        isEnabled = true
        WindowsPreferences.setFloatingLyricsEnabled(true)
        isPipMinimized = false
        requestMinimizeDesktop?.invoke()
    }

    fun togglePip() {
        if (!isEnabled) {
            activatePip()
        } else {
            setPipEnabled(false)
        }
    }

    fun setPipEnabled(enabled: Boolean) {
        if (enabled) {
            activatePip()
        } else {
            isEnabled = false
            WindowsPreferences.setFloatingLyricsEnabled(false)
            isPipMinimized = true
        }
    }
}

internal fun getAllUsableScreenBounds(): List<Rectangle> {
    return runCatching {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        ge.screenDevices.mapNotNull { dev ->
            val gc = dev.defaultConfiguration ?: return@mapNotNull null
            val bounds = gc.bounds
            val insets = runCatching { Toolkit.getDefaultToolkit().getScreenInsets(gc) }.getOrNull()
            if (insets != null) {
                Rectangle(
                    bounds.x + insets.left,
                    bounds.y + insets.top,
                    (bounds.width - insets.left - insets.right).coerceAtLeast(100),
                    (bounds.height - insets.top - insets.bottom).coerceAtLeast(100),
                )
            } else {
                bounds
            }
        }
    }.getOrDefault(emptyList()).ifEmpty {
        val d = Toolkit.getDefaultToolkit().screenSize
        listOf(Rectangle(0, 0, d.width, (d.height - 48).coerceAtLeast(100)))
    }
}

internal fun getPrimaryUsableScreenBounds(): Rectangle {
    return runCatching {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val dev = ge.defaultScreenDevice
        val gc = dev.defaultConfiguration
        val bounds = gc.bounds
        val insets = runCatching { Toolkit.getDefaultToolkit().getScreenInsets(gc) }.getOrNull()
        if (insets != null) {
            Rectangle(
                bounds.x + insets.left,
                bounds.y + insets.top,
                (bounds.width - insets.left - insets.right).coerceAtLeast(100),
                (bounds.height - insets.top - insets.bottom).coerceAtLeast(100),
            )
        } else {
            bounds
        }
    }.getOrElse {
        val d = Toolkit.getDefaultToolkit().screenSize
        Rectangle(0, 0, d.width, (d.height - 48).coerceAtLeast(100))
    }
}

internal fun clampPositionToScreens(x: Int, y: Int, width: Int, height: Int): Pair<Int, Int> {
    val usableScreens = getAllUsableScreenBounds()
    val windowRect = Rectangle(x, y, width, height)

    var bestScreen: Rectangle? = null
    var maxIntersectionArea = 0

    for (screen in usableScreens) {
        val inter = windowRect.intersection(screen)
        if (!inter.isEmpty) {
            val area = inter.width * inter.height
            if (area > maxIntersectionArea) {
                maxIntersectionArea = area
                bestScreen = screen
            }
        }
    }

    return if (bestScreen != null && maxIntersectionArea >= 3200) {
        val minX = bestScreen.x
        val maxX = (bestScreen.x + bestScreen.width - width).coerceAtLeast(minX)
        val minY = bestScreen.y
        val maxY = (bestScreen.y + bestScreen.height - height).coerceAtLeast(minY)
        Pair(x.coerceIn(minX, maxX), y.coerceIn(minY, maxY))
    } else {
        val primary = getPrimaryUsableScreenBounds()
        val safeX = primary.x + ((primary.width - width) / 2).coerceAtLeast(0)
        val safeY = primary.y + (primary.height - height - 30).coerceAtLeast(0)
        Pair(safeX, safeY)
    }
}

internal fun getScreenTopologySignature(): String {
    return runCatching {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        ge.screenDevices.joinToString(";") { dev ->
            val b = dev.defaultConfiguration?.bounds ?: Rectangle(0, 0, 0, 0)
            "${dev.iDstring}:${b.x},${b.y},${b.width},${b.height}"
        }
    }.getOrDefault("")
}

internal fun recoverFloatingWindow(window: java.awt.Window, windowState: androidx.compose.ui.window.WindowState) {
    runCatching {
        val currentW = window.width.takeIf { it > 100 } ?: windowState.size.width.value.roundToInt()
        val currentH = window.height.takeIf { it > 50 } ?: windowState.size.height.value.roundToInt()
        val currentX = window.x
        val currentY = window.y

        val (clampedX, clampedY) = clampPositionToScreens(currentX, currentY, currentW, currentH)

        if (clampedX != currentX || clampedY != currentY) {
            window.setLocation(clampedX, clampedY)
            windowState.position = WindowPosition(clampedX.dp, clampedY.dp)
            WindowsPreferences.saveFloatingLyricsPosition(clampedX.toFloat(), clampedY.toFloat())
        }

        window.isAlwaysOnTop = false
        window.isAlwaysOnTop = true
        window.toFront()

        window.background = java.awt.Color(0, 0, 0, 0)

        // 1-pixel size nudge forces Skiko / SkiaLayer to re-create its Direct3D/DWM swapchain
        window.setSize(currentW + 1, currentH)
        window.setSize(currentW, currentH)

        window.revalidate()
        window.repaint()
    }
}

@Composable
fun FloatingLyricsWindow(
    onClose: () -> Unit = {
        FloatingLyricsState.setPipEnabled(false)
    }
) {
    val track = FloatingLyricsState.currentTrack ?: return
    val savedPos = remember { WindowsPreferences.floatingLyricsPosition() }
    val savedSize = remember { WindowsPreferences.floatingLyricsSize() }

    val initialWidth = savedSize?.first?.dp ?: 720.dp
    val initialHeight = savedSize?.second?.dp ?: 175.dp

    val initialPos = remember {
        val w = initialWidth.value.roundToInt()
        val h = initialHeight.value.roundToInt()
        if (savedPos != null) {
            val (safeX, safeY) = clampPositionToScreens(savedPos.first.roundToInt(), savedPos.second.roundToInt(), w, h)
            WindowPosition(safeX.dp, safeY.dp)
        } else {
            val primary = getPrimaryUsableScreenBounds()
            val defaultX = (primary.x + ((primary.width - w) / 2)).coerceAtLeast(primary.x)
            val defaultY = (primary.y + (primary.height - h - 30)).coerceAtLeast(primary.y)
            WindowPosition(defaultX.dp, defaultY.dp)
        }
    }

    val windowState = rememberWindowState(
        position = initialPos,
        width = initialWidth,
        height = initialHeight,
    )

    LaunchedEffect(windowState.position, windowState.size) {
        val pos = windowState.position
        val size = windowState.size
        WindowsPreferences.saveFloatingLyricsPosition(pos.x.value, pos.y.value)
        WindowsPreferences.saveFloatingLyricsSize(size.width.value, size.height.value)
    }

    Window(
        onCloseRequest = onClose,
        state = windowState,
        visible = !FloatingLyricsState.isPipMinimized,
        title = "Floating Lyrics - Record Labs",
        icon = painterResource("recordlabs.ico"),
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        resizable = true,
        // Linux window managers give keyboard focus to every window that appears,
        // so the lyrics window took focus away from the app the user had just
        // switched to (on Windows a topmost window shown this way does not).
        // Its buttons, dragging and scrolling still work without focus.
        focusable = com.recordlabs.desktop.data.Platform.isWindows,
    ) {
        // Multi-monitor dynamic watcher: catches monitor plug/unplug & resolution/topology changes
        LaunchedEffect(window) {
            var lastSignature = getScreenTopologySignature()
            while (isActive) {
                delay(1000L)
                val currentSignature = getScreenTopologySignature()
                if (currentSignature != lastSignature) {
                    lastSignature = currentSignature
                    if (!FloatingLyricsState.isPipMinimized) {
                        recoverFloatingWindow(window, windowState)
                        // Second pass after 800ms when Windows finishes display handshake and DWM settles
                        delay(800L)
                        recoverFloatingWindow(window, windowState)
                    }
                }
            }
        }

        // Recover window position, topmost Z-order, and Skiko rendering surface when shown / unminimized
        LaunchedEffect(window, FloatingLyricsState.isPipMinimized) {
            if (!FloatingLyricsState.isPipMinimized) {
                recoverFloatingWindow(window, windowState)
            }
        }

        // Keep transparency alive when dragged across displays with different scaling factors
        DisposableEffect(window) {
            var lastGc = window.graphicsConfiguration
            val listener = object : ComponentAdapter() {
                override fun componentMoved(e: ComponentEvent?) {
                    val currentGc = window.graphicsConfiguration
                    if (lastGc != null && currentGc != null && lastGc !== currentGc) {
                        lastGc = currentGc
                        runCatching {
                            window.background = java.awt.Color(0, 0, 0, 0)
                            window.revalidate()
                            window.repaint()
                        }
                    }
                }
            }
            window.addComponentListener(listener)
            onDispose {
                window.removeComponentListener(listener)
            }
        }

        FloatingLyricsContent(
            track = track,
            isPlaying = FloatingLyricsState.isPlaying,
            positionMs = FloatingLyricsState.positionMs,
            onPlayPause = { FloatingLyricsState.onPlayPause?.invoke() },
            onPrevious = { FloatingLyricsState.onPrevious?.invoke() },
            onNext = { FloatingLyricsState.onNext?.invoke() },
            onSeek = { pos -> FloatingLyricsState.onSeek?.invoke(pos) },
            onOpenDesktop = {
                FloatingLyricsState.isPipMinimized = true
                FloatingLyricsState.requestRestoreDesktop?.invoke()
            },
            onClose = onClose,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WindowScope.FloatingLyricsContent(
    track: Track,
    isPlaying: Boolean,
    positionMs: Long,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenDesktop: () -> Unit,
    onClose: () -> Unit,
) {
    val cachedLyrics = remember(track.id) { WindowsLyricsProviderRegistry.peekCached(track) }
    var rawLyrics by remember(track.id) { mutableStateOf(cachedLyrics?.lyrics) }
    var isLoadingLyrics by remember(track.id) { mutableStateOf(rawLyrics.isNullOrBlank()) }
    val lyricsScope = rememberCoroutineScope()

    DisposableEffect(track.id) {
        val unsubscribe = WindowsLyricsProviderRegistry.addListener { updatedTrackId, result ->
            if (updatedTrackId == track.id) {
                lyricsScope.launch {
                    rawLyrics = result.lyrics
                    isLoadingLyrics = false
                }
            }
        }
        val latest = WindowsLyricsProviderRegistry.peekCached(track)
        if (latest != null && latest.lyrics.isNotBlank() && latest.lyrics != rawLyrics) {
            rawLyrics = latest.lyrics
            isLoadingLyrics = false
        }
        onDispose {
            unsubscribe()
        }
    }

    LaunchedEffect(track.id) {
        if (rawLyrics.isNullOrBlank()) {
            isLoadingLyrics = true
            val result = runCatching { WindowsLyricsProviderRegistry.getLyricsResult(track) }.getOrNull()
            if (result != null && result.lyrics.isNotBlank()) {
                rawLyrics = result.lyrics
            }
            isLoadingLyrics = false
        }
        val current = WindowsLyricsProviderRegistry.peekCached(track)
        if (current != null && !current.manuallySelected && current.provider != "Paxsenix") {
            val upgraded = runCatching {
                WindowsLyricsProviderRegistry.getPreferredLyricsUpgrade(track, current)
            }.getOrNull()
            if (upgraded != null && upgraded.lyrics.isNotBlank()) {
                rawLyrics = upgraded.lyrics
            }
        }
    }

    val lines = remember(rawLyrics) {
        WindowsRichLyrics.parse(rawLyrics.orEmpty())
    }
    val vocalLines = remember(lines) {
        lines.filter { !it.isBackground && it.text.isNotBlank() }
    }

    val currentIndex = remember(vocalLines, positionMs) {
        if (vocalLines.isEmpty()) -1
        else vocalLines.indexOfLast { it.startMs <= positionMs }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val density = LocalDensity.current

    var manualBrowseOverride by remember { mutableStateOf(false) }
    var userManualOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var lastScrollTimestamp by remember { mutableStateOf(0L) }

    LaunchedEffect(track.id) {
        manualBrowseOverride = false
        userManualOffset = 0f
    }

    LaunchedEffect(manualBrowseOverride, lastScrollTimestamp) {
        if (manualBrowseOverride) {
            delay(4500L)
            manualBrowseOverride = false
            userManualOffset = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isHovered) Color.Black.copy(alpha = 0.45f) else Color.Transparent
            )
            .then(
                if (isHovered) {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                } else Modifier
            )
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val change = event.changes.firstOrNull() ?: return@onPointerEvent
                val delta = change.scrollDelta.y
                if (delta != 0f) {
                    manualBrowseOverride = true
                    lastScrollTimestamp = System.currentTimeMillis()
                    userManualOffset += delta * 45f
                    change.consume()
                }
            },
    ) {
        // Dedicated draggable strip across top of window so user can reposition the window freely
        WindowDraggableArea(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .align(Alignment.TopCenter),
        ) {
            Box(Modifier.fillMaxSize())
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 34.dp, bottom = 12.dp, start = 14.dp, end = 14.dp)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            isDragging = true
                            manualBrowseOverride = true
                            lastScrollTimestamp = System.currentTimeMillis()
                        },
                        onDragEnd = {
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            manualBrowseOverride = true
                            lastScrollTimestamp = System.currentTimeMillis()
                            userManualOffset -= dragAmount
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            val gapPx = with(density) {
                maxOf(8.dp, (maxHeight * 0.05f).coerceIn(8.dp, 14.dp)).toPx()
            }
            val defaultLineHeight = with(density) { 26.dp.toPx() }
            val itemHeights = remember(track.id, rawLyrics) { mutableStateMapOf<Int, Int>() }

            // Global cumulative centers for all vocal lines in document coordinates
            val globalPositions = remember(vocalLines, itemHeights.toMap(), gapPx, defaultLineHeight) {
                val map = mutableMapOf<Int, Float>()
                var currentY = 0f
                for (i in vocalLines.indices) {
                    val h = itemHeights[i]?.toFloat() ?: defaultLineHeight
                    map[i] = currentY + h / 2f
                    currentY += h + gapPx
                }
                map
            }

            val baseCameraY = if (currentIndex in vocalLines.indices) {
                globalPositions[currentIndex] ?: 0f
            } else if (vocalLines.isNotEmpty()) {
                (globalPositions[0] ?: 0f) - (defaultLineHeight + gapPx)
            } else 0f

            val minCamera = (globalPositions[0] ?: 0f) - 60f
            val maxCamera = (globalPositions[vocalLines.lastIndex] ?: 0f) + 60f
            val targetCameraY = if (vocalLines.isNotEmpty()) {
                (baseCameraY + userManualOffset).coerceIn(minCamera, maxCamera)
            } else 0f

            val animatedCameraY by animateFloatAsState(
                targetValue = targetCameraY,
                animationSpec = tween(
                    durationMillis = if (isDragging) 60 else 500,
                    easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
                ),
                label = "floating-camera-scroll",
            )

            val focusedIndex = if (!manualBrowseOverride && currentIndex in vocalLines.indices) {
                currentIndex
            } else if (vocalLines.isNotEmpty()) {
                vocalLines.indices.minByOrNull { i ->
                    kotlin.math.abs((globalPositions[i] ?: 0f) - animatedCameraY)
                } ?: currentIndex
            } else -1

            if (vocalLines.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    NetflixSubtitleText(
                        text = "♫  ${track.title}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        alpha = 0.75f,
                        modifier = Modifier.fillMaxWidth(0.92f),
                    )
                    Spacer(Modifier.height(8.dp))
                    NetflixSubtitleText(
                        text = if (isLoadingLyrics) "Loading lyrics..." else track.artist,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        alpha = 1.0f,
                        modifier = Modifier.fillMaxWidth(0.92f),
                    )
                }
            } else if (currentIndex == -1 && !manualBrowseOverride) {
                // Intro interval / before first lyric starts
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    NetflixSubtitleText(
                        text = "♫  ${track.title} • ${track.artist}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        alpha = 0.65f,
                        modifier = Modifier.fillMaxWidth(0.92f),
                    )
                    Spacer(Modifier.height(8.dp))
                    NetflixSubtitleText(
                        text = vocalLines.firstOrNull()?.text ?: "• • •",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        alpha = 0.90f,
                        modifier = Modifier.fillMaxWidth(0.92f),
                    )
                }
            } else {
                val maxVisibleDist = constraints.maxHeight * 1.35f
                val visibleIndices = vocalLines.indices.filter { index ->
                    val relY = (globalPositions[index] ?: 0f) - animatedCameraY
                    kotlin.math.abs(relY) <= maxVisibleDist
                }

                visibleIndices.forEach { index ->
                    key(index, vocalLines[index].startMs) {
                        val relY = (globalPositions[index] ?: 0f) - animatedCameraY
                        val distFromFocus = kotlin.math.abs(index - focusedIndex)
                        val isCenterLine = distFromFocus == 0

                        val targetAlpha = when (distFromFocus) {
                            0 -> 1.0f
                            1 -> if (index > focusedIndex) 0.70f else 0.50f
                            2 -> if (constraints.maxHeight >= with(density) { 180.dp.toPx() }) 0.35f else 0.0f
                            else -> 0.0f
                        }
                        val animatedAlpha by animateFloatAsState(
                            targetValue = targetAlpha,
                            animationSpec = tween(
                                durationMillis = 450,
                                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
                            ),
                            label = "floating-line-alpha-$index",
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(0, relY.roundToInt()) }
                                .graphicsLayer {
                                    alpha = animatedAlpha
                                }
                                .onSizeChanged { itemHeights[index] = it.height }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        onSeek(vocalLines[index].startMs)
                                        manualBrowseOverride = false
                                        userManualOffset = 0f
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            NetflixSubtitleKaraokeLine(
                                line = vocalLines[index],
                                isActive = isCenterLine,
                                distFromFocus = distFromFocus,
                                positionMs = positionMs,
                                isPlaying = isPlaying,
                                fontSize = 18.sp,
                                lineHeight = 25.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(0.92f),
                            )
                        }
                    }
                }
            }
        }

        // Top control bar visible on hover
        AnimatedVisibility(
            visible = isHovered,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                WindowDraggableArea(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Drag window",
                            tint = Color.White.copy(alpha = 0.60f),
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            fontFamily = AppFonts.lyrics,
                            text = "${track.title} • ${track.artist}",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    IconButton(
                        onClick = onOpenDesktop,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open Record Labs",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close floating lyrics",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        // Floating "Sync" pill when user scrolled away from live playback
        AnimatedVisibility(
            visible = manualBrowseOverride,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.68f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier.clickable {
                    manualBrowseOverride = false
                    userManualOffset = 0f
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = Color.White.copy(alpha = 0.90f),
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        fontFamily = AppFonts.lyrics,
                        text = "Sync",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun NetflixSubtitleText(
    text: String,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = Color.White,
    alpha: Float = 1.0f,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Layer 1: Extra wide ambient diffuse halo
        Text(
            fontFamily = AppFonts.lyrics,
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            color = Color.Black.copy(alpha = 0.95f),
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.95f),
                    offset = Offset(0f, 2.5f),
                    blurRadius = 22f,
                ),
            ),
        )
        // Layer 2: Medium ambient spreading shadow
        Text(
            fontFamily = AppFonts.lyrics,
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            color = Color.Black.copy(alpha = 0.92f),
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.92f),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 12f,
                ),
            ),
        )
        // Layer 3: Tight contour outline shadow (for crisp stroke and edge definition)
        Text(
            fontFamily = AppFonts.lyrics,
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            color = Color.Black.copy(alpha = 0.90f),
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.90f),
                    offset = Offset(0f, 0f),
                    blurRadius = 4f,
                ),
            ),
        )
        // Layer 4: Foreground crisp white text
        Text(
            fontFamily = AppFonts.lyrics,
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            color = textColor.copy(alpha = alpha),
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.85f),
                    offset = Offset(0f, 1f),
                    blurRadius = 2f,
                ),
            ),
        )
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
    var frameTick by remember(key) { mutableStateOf(0L) }
    var previousReportedPositionMs by remember(key) { mutableStateOf(reportedPositionMs) }

    LaunchedEffect(reportedPositionMs, isPlaying, key) {
        val now = System.nanoTime()
        val estimated = if (isPlaying) {
            anchorPositionMs + ((now - anchorNanos) / 1_000_000L).coerceAtLeast(0L)
        } else anchorPositionMs

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
    frameTick
    return if (isPlaying && animate) {
        anchorPositionMs + ((System.nanoTime() - anchorNanos) / 1_000_000L).coerceAtLeast(0L)
    } else reportedPositionMs
}

@Composable
fun NetflixSubtitleKaraokeLine(
    line: RichLyricLine,
    isActive: Boolean,
    distFromFocus: Int,
    positionMs: Long,
    isPlaying: Boolean,
    fontSize: TextUnit = 18.sp,
    lineHeight: TextUnit = 25.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    modifier: Modifier = Modifier,
) {
    val plain = remember(line) {
        if (line.words.isEmpty()) line.text
        else buildString {
            line.words.forEachIndexed { index, word ->
                append(word.text)
                if (word.trailingSpace && index < line.words.lastIndex) append(' ')
            }
        }
    }

    val smoothPositionMs = smoothPlaybackClock(
        key = "pip-line-${line.startMs}",
        reportedPositionMs = positionMs,
        isPlaying = isPlaying,
        animate = isActive && line.words.isNotEmpty(),
    )

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val globalSweep: Float = if (isActive && line.words.isNotEmpty()) run {
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

    val baseTextColor = if (isActive) Color(0xFFB8B8B8) else Color(0xFFA0A0A0)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Layer 1: Extra wide ambient diffuse halo
        Text(
            fontFamily = AppFonts.lyrics,
            text = plain,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            color = Color.Black.copy(alpha = 0.95f),
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.95f),
                    offset = Offset(0f, 2.5f),
                    blurRadius = 22f,
                ),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        // Layer 2: Medium ambient spreading shadow
        Text(
            fontFamily = AppFonts.lyrics,
            text = plain,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            color = Color.Black.copy(alpha = 0.92f),
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.92f),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 12f,
                ),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        // Layer 3: Tight contour outline shadow (for crisp stroke and edge definition)
        Text(
            fontFamily = AppFonts.lyrics,
            text = plain,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            color = Color.Black.copy(alpha = 0.90f),
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.90f),
                    offset = Offset(0f, 0f),
                    blurRadius = 4f,
                ),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        // Layer 4: Base foreground gray text
        Text(
            fontFamily = AppFonts.lyrics,
            text = plain,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            softWrap = true,
            color = baseTextColor,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.85f),
                    offset = Offset(0f, 1f),
                    blurRadius = 2f,
                ),
            ),
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier.fillMaxWidth(),
        )

        // Layer 5: Active white highlight text (pure white, no glow/cahaya, smooth soft-edge wipe)
        if (isActive) {
            if (line.words.isNotEmpty()) {
                if (globalSweep > 0.01f) {
                    Text(
                        fontFamily = AppFonts.lyrics,
                        text = plain,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        fontWeight = fontWeight,
                        textAlign = TextAlign.Center,
                        softWrap = true,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawWithContent {
                                if (globalSweep >= plain.length.toFloat()) {
                                    drawContent()
                                    return@drawWithContent
                                }
                                val layout = textLayoutResult ?: return@drawWithContent
                                val sweepIndex = globalSweep.toInt().coerceIn(0, maxOf(0, plain.length - 1))
                                val sweepRem = globalSweep - sweepIndex
                                val rect = layout.getBoundingBox(sweepIndex)
                                val lineIndex = layout.getLineForOffset(sweepIndex)

                                val currentX = rect.left + (rect.right - rect.left) * sweepRem
                                val startFade = currentX - 10f
                                val endFade = currentX + 2f

                                val wipeBrush = Brush.horizontalGradient(
                                    0.0f to Color.Transparent,
                                    1.0f to Color.Black,
                                    startX = startFade,
                                    endX = endFade,
                                )

                                val bounds = Rect(-200f, -200f, size.width + 200f, size.height + 200f)
                                drawContext.canvas.saveLayer(bounds, Paint())
                                drawContent()

                                // Soft-erase the un-sung part on current line
                                drawRect(
                                    brush = wipeBrush,
                                    topLeft = Offset(-200f, layout.getLineTop(lineIndex)),
                                    size = Size(size.width + 400f, layout.getLineBottom(lineIndex) - layout.getLineTop(lineIndex)),
                                    blendMode = BlendMode.DstOut,
                                )

                                // Hard-erase all lines below the current line
                                if (lineIndex < layout.lineCount - 1) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(-200f, layout.getLineBottom(lineIndex)),
                                        size = Size(size.width + 400f, size.height + 200f),
                                        blendMode = BlendMode.DstOut,
                                    )
                                }

                                drawContext.canvas.restore()
                            },
                    )
                }
            } else {
                val activeEmphasis by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0f,
                    animationSpec = tween(350, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
                    label = "pip-line-active-fade",
                )
                if (activeEmphasis > 0.01f) {
                    Text(
                        fontFamily = AppFonts.lyrics,
                        text = plain,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        fontWeight = fontWeight,
                        textAlign = TextAlign.Center,
                        softWrap = true,
                        color = Color.White.copy(alpha = activeEmphasis),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
