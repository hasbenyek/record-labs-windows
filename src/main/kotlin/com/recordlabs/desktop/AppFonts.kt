package com.recordlabs.desktop

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.platform.SystemFont
import com.recordlabs.desktop.data.Platform

/**
 * The typeface of the whole app, lyrics included.
 *
 * On Windows Compose's default family is Segoe UI, and that stays untouched.
 * Linux's default is Noto Sans / DejaVu Sans, which made text (especially the
 * lyrics) look different. Linux therefore uses:
 *  1. Segoe UI when it is installed (for example copied from a Windows install), else
 *  2. Selawik, Microsoft's open-source Segoe UI replacement with the same metrics,
 *     bundled in resources/fonts/selawik (SIL Open Font License).
 * Characters these fonts lack (Japanese, Korean, emoji...) fall back to the
 * system fonts, as before.
 */
object AppFonts {
    val family: FontFamily by lazy {
        if (Platform.isWindows) FontFamily.Default else runCatching { linuxFamily() }.getOrDefault(FontFamily.Default)
    }

    /** Which typeface the Linux build ended up with (shown in the self-test). */
    val description: String
        get() = when {
            Platform.isWindows -> "Segoe UI (Windows default)"
            segoeUiInstalled -> "Segoe UI (installed)"
            else -> "Selawik (bundled Segoe UI replacement)"
        }

    private val segoeUiInstalled: Boolean by lazy {
        // Exact family names only: fontconfig answers a lookup for a missing
        // family with a substitute, so matchFamily() alone can't tell.
        runCatching {
            val fonts = org.jetbrains.skia.FontMgr.default
            (0 until fonts.familiesCount).any { fonts.getFamilyName(it).equals("Segoe UI", ignoreCase = true) }
        }.getOrDefault(false)
    }

    @OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
    private fun linuxFamily(): FontFamily = if (segoeUiInstalled) {
        FontFamily(
            SystemFont("Segoe UI", FontWeight.Light),
            SystemFont("Segoe UI", FontWeight.Normal),
            SystemFont("Segoe UI", FontWeight.SemiBold),
            SystemFont("Segoe UI", FontWeight.Bold),
            SystemFont("Segoe UI", FontWeight.Black),
        )
    } else {
        FontFamily(
            Font("fonts/selawik/Selawik-Light.ttf", FontWeight.Light),
            Font("fonts/selawik/Selawik-Semilight.ttf", FontWeight(350)),
            Font("fonts/selawik/Selawik-Regular.ttf", FontWeight.Normal),
            Font("fonts/selawik/Selawik-Semibold.ttf", FontWeight.SemiBold),
            Font("fonts/selawik/Selawik-Bold.ttf", FontWeight.Bold),
        )
    }
}
