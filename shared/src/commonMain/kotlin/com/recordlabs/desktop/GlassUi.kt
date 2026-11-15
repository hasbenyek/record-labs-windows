package com.recordlabs.desktop

import com.recordlabs.desktop.platform.*

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.recordlabs.desktop.data.WindowsPreferences

/*
 * Minimal iOS-style "liquid glass" surfaces shared by every platform: the
 * dialogs (alerts and forms such as Search lyrics / Available lyrics), their
 * text fields, and the resizable queue panel.
 */

private val GlassDialogShape = RoundedCornerShape(30.dp)
private val GlassPillShape = RoundedCornerShape(50)
private val GlassFieldShape = RoundedCornerShape(14.dp)

/** The glass card: dark tinted glass, a light sheen at the top and a bright rim. */
internal fun Modifier.liquidGlass(shape: androidx.compose.ui.graphics.Shape, elevation: Dp = 40.dp): Modifier = this
    .shadow(elevation, shape, ambientColor = Color.Black.copy(alpha = .45f), spotColor = Color.Black.copy(alpha = .6f))
    .clip(shape)
    .background(Brush.verticalGradient(listOf(Color(0xEB303137), Color(0xF21C1D22))))
    .drawBehind {
        // The sheen of glass catching the light at its top edge.
        drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = .085f), .38f to Color.Transparent))
    }
    .border(
        1.dp,
        Brush.verticalGradient(listOf(Color.White.copy(alpha = .30f), Color.White.copy(alpha = .06f), Color.White.copy(alpha = .14f))),
        shape,
    )

/**
 * iPhone: the glass card as real liquid glass over the app ([LocalAppBackdrop]),
 * frosted so its text reads over anything; elsewhere [liquidGlass] as before.
 */
@Composable
internal fun Modifier.liquidGlassPanel(radius: Dp, elevation: Dp = 40.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    val backdrop = LocalAppBackdrop.current ?: return liquidGlass(shape, elevation)
    return this
        // No shadow round the glass (it read as a black rim around the panel).
        // The glass before the clip (see DockGlass in Main.kt).
        .liquidGlassBackground(backdrop, cornerRadius = radius, frosted = true)
        .clip(shape)
        .drawBehind {
            drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = .06f), .35f to Color.Transparent))
        }
        .border(
            0.8.dp,
            Brush.verticalGradient(listOf(Color.White.copy(alpha = .28f), Color.White.copy(alpha = .05f), Color.White.copy(alpha = .12f))),
            shape,
        )
}

/**
 * The dialog of the whole app (desktop and Android, every orientation), as a
 * minimal iOS alert: a liquid-glass card, the title centred, the content, and
 * the buttons as two equal pills at the bottom (Cancel in glass, the main
 * action in white). It zooms in softly like an iOS alert.
 *
 * Same parameters as Material's AlertDialog, so every dialog of the app uses it.
 */
@Composable
internal fun GlassDialog(
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
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 520f),
        label = "glass-dialog-entrance",
    )
    val windowSize = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val windowWidth = with(density) { windowSize.width.toDp() }
    val maxCardWidth = (windowWidth - 32.dp).coerceIn(260.dp, 600.dp)
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Layout(
                modifier = modifier
                    .graphicsLayer {
                        alpha = entrance.coerceIn(0f, 1f)
                        val scale = 1.08f - 0.08f * entrance
                        scaleX = scale
                        scaleY = scale
                    }
                    .liquidGlassPanel(30.dp),
                content = {
                    if (title != null) Box(Modifier.layoutId("title").fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ProvideTextStyle(TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)) { title() }
                    }
                    if (text != null) Box(Modifier.layoutId("text")) {
                        ProvideTextStyle(TextStyle(color = Color.White.copy(alpha = .78f), fontSize = 14.sp, lineHeight = 19.sp)) { text() }
                    }
                    Row(Modifier.layoutId("buttons").fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (dismissButton != null) {
                            DialogPill(prominent = false, modifier = Modifier.weight(1f)) { dismissButton() }
                            DialogPill(prominent = true, modifier = Modifier.weight(1f)) { confirmButton() }
                        } else {
                            DialogPill(prominent = true, modifier = Modifier.weight(1f)) { confirmButton() }
                        }
                    }
                },
            ) { measurables, constraints ->
                val pad = 22.dp.roundToPx()
                val top = 22.dp.roundToPx()
                val bottom = 18.dp.roundToPx()
                val titleGap = 12.dp.roundToPx()
                val buttonsGap = 20.dp.roundToPx()
                val maxWidth = minOf(constraints.maxWidth, maxCardWidth.roundToPx())
                val minWidth = minOf(300.dp.roundToPx(), maxWidth)
                val innerMax = (maxWidth - pad * 2).coerceAtLeast(0)
                val maxHeight = constraints.maxHeight
                // The content decides the width (forms are narrow, the lyrics list is wide).
                val textPlaceable = measurables.firstOrNull { it.layoutId == "text" }?.measure(
                    Constraints(maxWidth = innerMax, maxHeight = if (maxHeight == Constraints.Infinity) Constraints.Infinity else (maxHeight - 190.dp.roundToPx()).coerceAtLeast(80.dp.roundToPx())),
                )
                val width = maxOf(minWidth, (textPlaceable?.width ?: 0) + pad * 2).coerceAtMost(maxWidth)
                val inner = (width - pad * 2).coerceAtLeast(0)
                val titlePlaceable = measurables.firstOrNull { it.layoutId == "title" }?.measure(Constraints(minWidth = inner, maxWidth = inner))
                val buttonsPlaceable = measurables.first { it.layoutId == "buttons" }.measure(Constraints(minWidth = inner, maxWidth = inner))
                var height = top
                if (titlePlaceable != null) height += titlePlaceable.height
                if (titlePlaceable != null && textPlaceable != null) height += titleGap
                if (textPlaceable != null) height += textPlaceable.height
                height += buttonsGap + buttonsPlaceable.height + bottom
                layout(width, height) {
                    var y = top
                    titlePlaceable?.let { it.place(pad, y); y += it.height }
                    if (titlePlaceable != null && textPlaceable != null) y += titleGap
                    textPlaceable?.let { it.place(pad, y); y += it.height }
                    y += buttonsGap
                    buttonsPlaceable.place(pad, y)
                }
            }
        }
    }
}

/**
 * One button of a [GlassDialog] as a pill. The dialog's buttons are plain
 * TextButtons: the pill gives them its colours and stretches them over its
 * whole area, so the entire pill is the button.
 */
@Composable
private fun DialogPill(prominent: Boolean, modifier: Modifier, content: @Composable () -> Unit) {
    val foreground = if (prominent) Color(0xFF111114) else Color.White
    Box(
        modifier
            .height(46.dp)
            .clip(GlassPillShape)
            .background(if (prominent) Color.White.copy(alpha = .94f) else Color.White.copy(alpha = .11f))
            .border(0.6.dp, Color.White.copy(alpha = if (prominent) 0f else .12f), GlassPillShape),
    ) {
        MaterialTheme(
            colors = MaterialTheme.colors.copy(primary = foreground, onSurface = foreground),
            typography = MaterialTheme.typography.copy(button = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)),
        ) {
            CompositionLocalProvider(LocalContentColor provides foreground) {
                Layout(content = content) { measurables, constraints ->
                    val w = constraints.maxWidth
                    val h = constraints.maxHeight
                    val placeables = measurables.map { it.measure(Constraints.fixed(w, h)) }
                    layout(w, h) { placeables.forEach { it.place(0, 0) } }
                }
            }
        }
    }
}

/** A text field in the glass style: a soft rounded well, the label small above the text. */
@Composable
internal fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    // Enter, or the keyboard's search key, runs this (null: the key does nothing special).
    onSearch: (() -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        keyboardOptions = if (onSearch != null) androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search) else androidx.compose.foundation.text.KeyboardOptions.Default,
        keyboardActions = if (onSearch != null) androidx.compose.foundation.text.KeyboardActions(onSearch = { onSearch() }, onDone = { onSearch() }) else androidx.compose.foundation.text.KeyboardActions.Default,
        visualTransformation = visualTransformation,
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        cursorBrush = SolidColor(Color.White),
        modifier = modifier
            // At least as wide as Material's fields (280 dp) unless the caller says otherwise.
            .widthIn(min = 280.dp)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(GlassFieldShape)
                    .background(Color.White.copy(alpha = if (focused) .13f else .08f))
                    .border(1.dp, Color.White.copy(alpha = if (focused) .30f else .10f), GlassFieldShape)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                if (label != null) {
                    ProvideTextStyle(TextStyle(color = Color.White.copy(alpha = .55f), fontSize = 11.5.sp, fontWeight = FontWeight.Medium)) { label() }
                }
                Box {
                    if (value.isEmpty() && placeholder != null) {
                        ProvideTextStyle(TextStyle(color = Color.White.copy(alpha = .38f), fontSize = 16.sp)) { placeholder() }
                    }
                    inner()
                }
            }
        },
    )
}

/**
 * The side queue panel's width, shared by every queue (home, player, lyrics)
 * on every platform. It can be dragged narrower; the original size is the
 * widest. Kept between launches.
 */
internal object QueuePanel {
    val MaxWidth = 368.dp
    val MinWidth = 260.dp
    var width by mutableStateOf(MaxWidth)
        private set
    /** True while the edge is being dragged (animations follow the finger at once). */
    var resizing by mutableStateOf(false)
        private set

    init {
        WindowsPreferences.queuePanelWidth()?.let { width = it.dp.coerceIn(MinWidth, MaxWidth) }
    }

    fun resizeBy(delta: Dp) {
        width = (width + delta).coerceIn(MinWidth, MaxWidth)
    }

    fun startResize() { resizing = true }

    fun endResize() {
        resizing = false
        WindowsPreferences.setQueuePanelWidth(width.value)
    }
}

/**
 * The grip on the queue panel's inner edge: drag it to make the queue narrower
 * (or wide again). [panelOnRight] is true for a panel at the right of the window,
 * whose grip is on its left edge.
 */
@Composable
internal fun QueueResizeHandle(modifier: Modifier, panelOnRight: Boolean) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val active = hovered || QueuePanel.resizing
    val gripAlpha by animateFloatAsState(if (active) .6f else .2f, label = "queue-grip")
    Box(
        modifier
            .fillMaxHeight()
            .width(16.dp)
            .hoverable(interaction)
            .pointerHoverIcon(PointerIcon.Hand)
            .pointerInput(panelOnRight) {
                detectHorizontalDragGestures(
                    onDragStart = { QueuePanel.startResize() },
                    onDragEnd = { QueuePanel.endResize() },
                    onDragCancel = { QueuePanel.endResize() },
                ) { change, dragAmount ->
                    change.consume()
                    val delta = dragAmount.toDp()
                    QueuePanel.resizeBy(if (panelOnRight) -delta else delta)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.width(4.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = gripAlpha)))
    }
}

/**
 * The page under a page's own floating buttons (back, more): the scrolling content
 * only, recorded beside the buttons (never around them), so they can be drawn in liquid
 * glass over it. Null where a page does not record one.
 */
internal val LocalHeaderBackdrop = androidx.compose.runtime.staticCompositionLocalOf<GlassBackdrop?> { null }

/**
 * A round floating button (back arrow, the album page's ⋯): liquid glass over the page
 * ([LocalHeaderBackdrop]) on the iPhone, stretching under the finger as the dock's
 * buttons do; where there is no recording of the page, a glass-like circle (dark tint,
 * light rim and sheen) instead.
 */
@Composable
internal fun LiquidGlassCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    tint: Color = Color.White,
    enabled: Boolean = true,
    backdrop: GlassBackdrop? = LocalHeaderBackdrop.current,
    /**
     * Clear glass (what is behind shows through): always for buttons inside a page
     * ([LocalInPageGlass]), whose recording is see-through; the tinted glass bent that
     * emptiness into black round the button when it was pulled.
     */
    clear: Boolean = backdrop != null && backdrop === LocalInPageGlass.current,
) {
    val press = remember { GlassPressState() }
    val circle = androidx.compose.foundation.shape.CircleShape
    val surface = if (backdrop != null) {
        // No shadow round the glass (a black rim around the button).
        Modifier.liquidGlassBackground(backdrop, press, clear = clear)
            .then(if (clear) Modifier.border(0.8.dp, Color.White.copy(alpha = .16f), circle) else Modifier)
    } else {
        Modifier
            .glassShadow(circle, spread = 9.dp, strength = 0.16f)
            .clip(circle)
            .background(Brush.verticalGradient(listOf(Color(0x8C3A3B40), Color(0xA6202125))))
            .drawBehind {
                drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = .14f), .45f to Color.Transparent))
            }
            .border(
                0.8.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = .38f), Color.White.copy(alpha = .06f), Color.White.copy(alpha = .16f))),
                circle,
            )
    }
    Box(
        modifier
            .size(size)
            .glassPress(press)
            .then(surface)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material.Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}


/**
 * Glass for buttons inside a page (Listen Together's, Library's filter chips), with
 * Settings > Appearance > Liquid Glass Material: transparent liquid glass that stretches
 * when pulled, like the navigation bar and the mini player. Their glass reads a
 * see-through recording laid under the page (never the buttons themselves, which would
 * read their own glass), so what is behind shows through clearly, with the glass's lit rim
 * and no dark fill. Null without Liquid Glass Material.
 */
internal val LocalInPageGlass = androidx.compose.runtime.staticCompositionLocalOf<GlassBackdrop?> { null }

@Composable
internal fun InPageGlass(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val glass = rememberGlassBackdrop(onDemand = true)
    Box(modifier) {
        if (glass != null) Box(Modifier.matchParentSize().glassBackdropSource(glass))
        androidx.compose.runtime.CompositionLocalProvider(LocalInPageGlass provides glass) { content() }
    }
}


/**
 * A short message as a small bubble low in the middle of the app, gone after a moment
 * ([onDone] with the message shown), as most Android apps show them.
 */
@Composable
internal fun AppToast(message: String?, bottomPadding: androidx.compose.ui.unit.Dp, onDone: (String) -> Unit) {
    var shown by remember { mutableStateOf(message) }
    if (message != null) shown = message
    LaunchedEffect(message) {
        if (message == null) return@LaunchedEffect
        kotlinx.coroutines.delay(if (message.length > 60) 4_200L else 2_800L)
        onDone(message)
    }
    Box(Modifier.fillMaxSize().padding(bottom = bottomPadding).zIndex(95f), contentAlignment = Alignment.BottomCenter) {
        androidx.compose.animation.AnimatedVisibility(
            visible = message != null,
            enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(180)) +
                androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(220)) { it / 2 },
            exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(220)),
        ) {
            androidx.compose.material.Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xF0303033),
                elevation = 6.dp,
                modifier = Modifier.padding(horizontal = 32.dp).widthIn(max = 520.dp),
            ) {
                androidx.compose.material.Text(
                    shown.orEmpty(),
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 3,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                )
            }
        }
    }
}


/**
 * The card of the Library's playlist dialogs (New playlist, Delete, Playlists Migration):
 * with Settings > Appearance > Liquid Glass Material, the app's liquid glass as the
 * navigation bar (its buttons in clear glass, see [InPageGlass]); otherwise the solid
 * [fallback] card as before.
 */
@Composable
internal fun GlassDialogCard(
    radius: Dp,
    fallback: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalAppBackdrop.current
    val shape = RoundedCornerShape(radius)
    if (backdrop == null) {
        androidx.compose.material.Surface(shape = shape, color = fallback, modifier = modifier) { content() }
        return
    }
    Box(
        modifier
            // The glass before the clip (see DockGlass in Main.kt), no shadow (a black rim).
            .liquidGlassBackground(backdrop, cornerRadius = radius)
            .clip(shape)
            .background(Color.Black.copy(alpha = .14f))
            .border(
                0.8.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = .30f), Color.White.copy(alpha = .06f), Color.White.copy(alpha = .14f))),
                shape,
            ),
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White, LocalInPageGlass provides backdrop) { content() }
    }
}

/**
 * A text button of those dialogs: clear liquid glass inside a [GlassDialogCard] with Liquid
 * Glass Material ([primary] a little lighter), otherwise the pill as before ([primary]:
 * [primaryFill] with dark text; else plain white text).
 */
@Composable
internal fun DialogTextButton(
    label: String,
    primary: Boolean,
    enabled: Boolean = true,
    primaryFill: Color = Color.White,
    primaryText: Color = Color.Black,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val glass = LocalInPageGlass.current
    if (glass != null) {
        Box(
            Modifier
                .graphicsLayer {
                    alpha = if (enabled) 1f else 0.45f
                    // Faded without an offscreen layer: one would cut the glass off at the
                    // button's edges while it is stretched.
                    compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.ModulateAlpha
                }
                .liquidGlassInteractive(glass, clear = true)
                .clip(shape)
                .background(if (primary) (if (primaryFill == Color.White) Color.White.copy(alpha = .16f) else primaryFill.copy(alpha = .55f)) else Color.Transparent)
                .border(0.8.dp, Color.White.copy(alpha = .18f), shape)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            androidx.compose.material.Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    } else if (primary) {
        androidx.compose.material.Text(
            label,
            color = if (enabled) primaryText else primaryText.copy(alpha = .4f),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(shape).background(if (enabled) primaryFill else primaryFill.copy(alpha = .4f))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        )
    } else {
        androidx.compose.material.Text(
            label, color = Color.White, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(shape).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}


/**
 * A dialog that opens and closes smoothly: it rises in slightly from below while growing
 * from 94 % and fading in (a soft spring, no bounce), and closing runs the same way back,
 * quicker, before [onDismissRequest] (a tap outside, Back, or [content]'s close).
 */
@Composable
internal fun SmoothDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable (close: () -> Unit) -> Unit,
) {
    var closing by remember { mutableStateOf(false) }
    val progress = remember { androidx.compose.animation.core.Animatable(0f) }
    val latestDismiss = androidx.compose.runtime.rememberUpdatedState(onDismissRequest)
    LaunchedEffect(closing) {
        if (!closing) {
            progress.animateTo(1f, spring(dampingRatio = 1f, stiffness = 260f))
        } else {
            progress.animateTo(0f, androidx.compose.animation.core.tween(190, easing = androidx.compose.animation.core.FastOutLinearInEasing))
            latestDismiss.value()
        }
    }
    val close: () -> Unit = { closing = true }
    Dialog(onDismissRequest = close, properties = properties) {
        Box(
            Modifier.graphicsLayer {
                val p = progress.value
                alpha = (p / 0.7f).coerceIn(0f, 1f)
                val scale = 0.94f + 0.06f * p
                scaleX = scale
                scaleY = scale
                translationY = (1f - p) * 24.dp.toPx()
            },
        ) { content(close) }
    }
}

