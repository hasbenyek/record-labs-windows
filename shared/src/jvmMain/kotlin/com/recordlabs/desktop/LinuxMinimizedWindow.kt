package com.recordlabs.desktop

import com.recordlabs.desktop.platform.AppPlatform

/**
 * Linux only: the main window stops drawing to the screen while it is minimized.
 *
 * Skiko draws every Compose window of the app in one loop on the UI thread and waits for
 * the screen's refresh (vsync) on one of them. A minimized window has no screen to wait
 * for: X / XWayland then answer its "wait for the refresh" once a second. When the main
 * window was the one picked to wait (it keeps asking for frames), each frame held the UI
 * thread for about a second (perf log: edtMax=981ms, 1 frame a second in both windows),
 * so the floating lyrics glided in jumps and changed line up to a second late.
 *
 * While minimized, the main window's drawing surface (its SkiaLayer) is hidden, so Skiko
 * leaves it out of that loop and only the floating lyrics wait for the refresh. The main
 * window's Compose content still runs: a few times a second its frame is recorded into a
 * picture that is thrown away (no graphics card, no waiting), so its recompositions and
 * effects (song changes, play/pause, what the floating lyrics are given) go on as before.
 * Everything is put back as soon as the window is restored.
 *
 * Windows, Android and iOS: never installed. RECORDLABS_KEEP_MAIN_LAYER=1 turns it off.
 */
internal object LinuxMinimizedWindow {
    private val enabled: Boolean =
        AppPlatform.isLinux && System.getenv("RECORDLABS_KEEP_MAIN_LAYER") != "1"

    /** How often the hidden main window's content is moved on (ms). */
    private const val TICK_MS = 250

    /** Wait after minimizing before hiding, so the desktop's minimize animation still has the picture. */
    private const val HIDE_DELAY_MS = 400

    private var installed = false
    private var hiddenLayer: org.jetbrains.skiko.SkiaLayer? = null
    private var hiddenWindow: java.awt.Window? = null
    private var tickTimer: javax.swing.Timer? = null
    private var hideTimer: javax.swing.Timer? = null

    /** The main window's surface is hidden right now (for the perf log). */
    @Volatile var isHidden: Boolean = false
        private set

    /** Starts following the main window. Any thread; does nothing on Windows / Android. */
    fun install() {
        if (!enabled) return
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater { install() }
            return
        }
        if (installed) return
        installed = true
        runCatching {
            java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(
                { event -> runCatching { onEvent(event) } },
                java.awt.AWTEvent.WINDOW_EVENT_MASK or
                    java.awt.AWTEvent.WINDOW_STATE_EVENT_MASK or
                    java.awt.AWTEvent.WINDOW_FOCUS_EVENT_MASK,
            )
        }
        // Already minimized when this starts (the floating lyrics come up after minimizing).
        java.awt.Window.getWindows().filter { isMainWindow(it) }.forEach { window ->
            if (isIconified(window)) scheduleHide(window)
        }
    }

    private fun isMainWindow(window: java.awt.Window?): Boolean =
        window is java.awt.Frame && window.title == "Record Labs" && window.isDisplayable

    private fun isIconified(window: java.awt.Window): Boolean =
        ((window as? java.awt.Frame)?.extendedState ?: 0) and java.awt.Frame.ICONIFIED != 0

    private fun onEvent(event: java.awt.AWTEvent) {
        val window = (event as? java.awt.event.WindowEvent)?.window ?: return
        if (!isMainWindow(window) && window !== hiddenWindow) return
        when (event.id) {
            java.awt.event.WindowEvent.WINDOW_ICONIFIED -> scheduleHide(window)
            java.awt.event.WindowEvent.WINDOW_STATE_CHANGED ->
                if (isIconified(window)) scheduleHide(window) else restore()
            java.awt.event.WindowEvent.WINDOW_DEICONIFIED,
            java.awt.event.WindowEvent.WINDOW_ACTIVATED,
            java.awt.event.WindowEvent.WINDOW_GAINED_FOCUS -> if (!isIconified(window)) restore()
            java.awt.event.WindowEvent.WINDOW_CLOSING,
            java.awt.event.WindowEvent.WINDOW_CLOSED -> restore()
        }
    }

    private fun scheduleHide(window: java.awt.Window) {
        if (hiddenLayer != null || hideTimer != null) return
        hideTimer = javax.swing.Timer(HIDE_DELAY_MS) {
            hideTimer = null
            if (window.isDisplayable && isIconified(window)) hide(window)
        }.apply { isRepeats = false; start() }
    }

    private fun hide(window: java.awt.Window) {
        if (hiddenLayer != null) return
        val layer = findLayer(window) ?: return
        if (!layer.isVisible) return
        layer.isVisible = false
        hiddenLayer = layer
        hiddenWindow = window
        isHidden = true
        tickTimer = javax.swing.Timer(TICK_MS) { tick() }.apply { isRepeats = true; start() }
    }

    private fun restore() {
        hideTimer?.stop()
        hideTimer = null
        tickTimer?.stop()
        tickTimer = null
        val layer = hiddenLayer ?: return
        hiddenLayer = null
        hiddenWindow = null
        isHidden = false
        runCatching {
            layer.isVisible = true
            layer.revalidate()
            if (layer.isDisplayable) {
                layer.needRender()
                layer.requestFocusInWindow()
            }
        }
    }

    /** Moves the hidden window's Compose content one frame on, drawing into a picture nobody shows. */
    private fun tick() {
        val layer = hiddenLayer ?: return
        val window = hiddenWindow
        if (window == null || !window.isDisplayable || !layer.isDisplayable) {
            restore()
            return
        }
        if (!isIconified(window)) {
            restore()
            return
        }
        val delegate = layer.renderDelegate ?: return
        val scale = runCatching { layer.contentScale }.getOrDefault(1f)
        val width = (layer.width * scale).toInt()
        val height = (layer.height * scale).toInt()
        if (width <= 0 || height <= 0) return
        val recorder = org.jetbrains.skia.PictureRecorder()
        try {
            val canvas = recorder.beginRecording(org.jetbrains.skia.Rect.makeWH(width.toFloat(), height.toFloat()))
            delegate.onRender(canvas, width, height, System.nanoTime())
            recorder.finishRecordingAsPicture().close()
        } catch (_: Throwable) {
        } finally {
            recorder.close()
        }
    }

    private fun findLayer(component: java.awt.Component): org.jetbrains.skiko.SkiaLayer? {
        if (component is org.jetbrains.skiko.SkiaLayer) return component
        if (component is java.awt.Container) {
            for (child in component.components) findLayer(child)?.let { return it }
        }
        return null
    }
}
