package com.recordlabs.desktop

/**
 * Performance log for Linux.
 *
 * - With RECORDLABS_PERF_LOG=1 in the environment: printed to the terminal.
 * - Fedora / Red Hat family (diagnostic build, while the floating lyrics are being fixed
 *   there): also written to ~/Music/record-labs-perf.txt (at most about 2 MB), unless
 *   RECORDLABS_PERF_LOG=0. Everywhere else it is off: nothing is hooked or started.
 *
 * Every 2 seconds: per window the frames drawn, the longest gap between two frames and
 * the graphics API; how long the UI thread was held up at most; how late the floating
 * lyrics changed line; and the background state.
 */
internal object PerfLog {
    private val env = System.getenv("RECORDLABS_PERF_LOG")
    /** macOS test builds (-Drecordlabs.perfLog=true, see macos.yml): written to ~/Music too. */
    private val macTestBuild: Boolean =
        com.recordlabs.desktop.data.Platform.isMac && System.getProperty("recordlabs.perfLog") == "true"
    private val toFile: Boolean = env != "0" && env != "1" && (MainWindowActivity.isRedHatFamily || macTestBuild)
    val enabled: Boolean = env == "1" || toFile

    private class Counter {
        var frames = 0
        var lastNanos = 0L
        var maxGapMs = 0L
        var gaps = ArrayList<Long>()
        var info: String? = null
    }

    private val counters = java.util.WeakHashMap<org.jetbrains.skiko.SkiaLayer, Counter>()
    @Volatile private var started = false
    @Volatile private var edtMaxMs = 0L
    private val lineLateness = java.util.concurrent.ConcurrentLinkedQueue<Long>()
    private var file: java.io.File? = null

    /** The floating lyrics changed line [lateMs] after its timestamp (from their own clock). */
    fun lineChanged(lateMs: Long) {
        if (enabled) lineLateness.add(lateMs)
    }

    fun start() {
        if (!enabled || started) return
        started = true
        if (toFile) {
            file = runCatching {
                java.io.File(System.getProperty("user.home"), "Music").takeIf { it.isDirectory }
                    ?.resolve("record-labs-perf.txt")
            }.getOrNull()
        }
        Thread({
            val isMac = com.recordlabs.desktop.data.Platform.isMac
            fun run(vararg command: String) = runCatching {
                ProcessBuilder(*command).redirectErrorStream(true).start()
                    .inputStream.bufferedReader().readText().trim()
            }.getOrNull()
            // macOS: the processor and the Mac model instead of the Linux desktop shell.
            val shell = if (isMac) {
                "${run("sysctl", "-n", "machdep.cpu.brand_string")} model=${run("sysctl", "-n", "hw.model")} " +
                    "threads=${Runtime.getRuntime().availableProcessors()} lighterVisuals=${LightVisuals.active}"
            } else run("gnome-shell", "--version") ?: "?"
            val osName = if (isMac) "macOS ${run("sw_vers", "-productVersion")}" else runCatching {
                java.io.File("/etc/os-release").readLines().firstOrNull { it.startsWith("PRETTY_NAME=") }
                    ?.substringAfter('=')?.trim('"')
            }.getOrNull()
            val densityStart = System.nanoTime()
            val density = runCatching { DesktopUiScale.effectiveDensityScale() }.getOrNull()
            val densityMs = (System.nanoTime() - densityStart) / 1_000_000L
            write("[perf] ===== start ${java.time.LocalDateTime.now()} =====")
            write("[perf] os=$osName shell=$shell redHat=${MainWindowActivity.isRedHatFamily} " +
                "session=${System.getenv("XDG_SESSION_TYPE")} desktop=${System.getenv("XDG_CURRENT_DESKTOP")} " +
                "uiScale=${System.getProperty("sun.java2d.uiScale")} displayScale=${System.getProperty("recordlabs.displayScale")} " +
                "nativeScaling=${DesktopUiScale.xwaylandNativeScaling} density=$density (took ${densityMs}ms) " +
                "java=${System.getProperty("java.version")}")
        }, "record-labs-perf-info").apply { isDaemon = true; start() }
        // How long the UI thread takes to run a tiny task posted every 20 ms.
        Thread({
            while (true) {
                val posted = System.nanoTime()
                runCatching {
                    java.awt.EventQueue.invokeAndWait {
                        val waited = (System.nanoTime() - posted) / 1_000_000L
                        if (waited > edtMaxMs) edtMaxMs = waited
                    }
                }
                Thread.sleep(20L)
            }
        }, "record-labs-perf-edt").apply { isDaemon = true; start() }
        Thread({
            while (true) {
                Thread.sleep(2_000L)
                runCatching { java.awt.EventQueue.invokeAndWait { report() } }
            }
        }, "record-labs-perf-log").apply { isDaemon = true; start() }
    }

    private fun write(line: String) {
        println(line)
        val f = file ?: return
        runCatching {
            if (f.length() < 2_000_000L) f.appendText(line + "\n")
        }
    }

    private fun report() {
        val focused = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().focusedWindow
        val parts = java.awt.Window.getWindows().filter { it.isDisplayable }.mapNotNull { window ->
            val layer = findLayer(window) ?: return@mapNotNull null
            val counter = counters.getOrPut(layer) { hook(layer) }
            if (counter.info == null) counter.info = runCatching { layer.renderInfo.replace('\n', ' ').take(160) }.getOrNull()
            val frames = counter.frames
            val sorted = counter.gaps.sorted()
            val p90 = if (sorted.isEmpty()) 0 else sorted[(sorted.size * 9 / 10).coerceAtMost(sorted.size - 1)]
            val maxGap = counter.maxGapMs
            counter.frames = 0
            counter.maxGapMs = 0
            counter.gaps = ArrayList()
            val title = (window as? java.awt.Frame)?.title.orEmpty().ifBlank { window.javaClass.simpleName }
            val state = (window as? java.awt.Frame)?.extendedState ?: 0
            "'$title' ${window.width}x${window.height} api=${runCatching { layer.renderApi }.getOrNull()} " +
                "frames=$frames gapP90=${p90}ms gapMax=${maxGap}ms" +
                (if (state and java.awt.Frame.ICONIFIED != 0) " minimized" else "") +
                (if (window.isShowing) "" else " hidden") + (if (window === focused) " focused" else "")
        }
        val late = generateSequence { lineLateness.poll() }.toList()
        val edt = edtMaxMs
        edtMaxMs = 0
        write("[perf] lite=${MainWindowActivity.isLite} shown=${MainWindowActivity.isShown} " +
            "pip=${FloatingLyricsState.isEnabled && !FloatingLyricsState.isPipMinimized} " +
            "pipLines=${MainWindowActivity.LiteClock.floatingShown} mainHidden=${LinuxMinimizedWindow.isHidden} edtMax=${edt}ms " +
            "lineLate=${late.joinToString(",")} | ${parts.joinToString(" | ")}")
        counters.forEach { (layer, c) ->
            if (c.info != null && !loggedInfo.contains(c.info!!)) {
                loggedInfo += c.info!!
                write("[perf] renderInfo: ${c.info}")
            }
        }
    }

    private val loggedInfo = HashSet<String>()

    private fun findLayer(component: java.awt.Component): org.jetbrains.skiko.SkiaLayer? {
        if (component is org.jetbrains.skiko.SkiaLayer) return component
        if (component is java.awt.Container) {
            for (child in component.components) findLayer(child)?.let { return it }
        }
        return null
    }

    /** Counts the frames a window draws and the gaps between them (wraps its render delegate; logging only). */
    private fun hook(layer: org.jetbrains.skiko.SkiaLayer): Counter {
        val counter = Counter()
        val original = layer.renderDelegate ?: return counter
        layer.renderDelegate = object : org.jetbrains.skiko.SkikoRenderDelegate {
            override fun onRender(canvas: org.jetbrains.skia.Canvas, width: Int, height: Int, nanoTime: Long) {
                val now = System.nanoTime()
                if (counter.lastNanos != 0L) {
                    val gap = (now - counter.lastNanos) / 1_000_000L
                    // Gaps longer than half a second are pauses between glides, not stutter.
                    if (gap < 500L) {
                        counter.gaps.add(gap)
                        if (gap > counter.maxGapMs) counter.maxGapMs = gap
                    }
                }
                counter.lastNanos = now
                counter.frames++
                original.onRender(canvas, width, height, nanoTime)
            }
        }
        return counter
    }
}
