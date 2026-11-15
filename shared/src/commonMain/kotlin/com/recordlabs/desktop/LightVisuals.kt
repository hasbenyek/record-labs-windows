package com.recordlabs.desktop

import com.recordlabs.desktop.data.AppSettings
import com.recordlabs.desktop.platform.AppPlatform
import com.recordlabs.desktop.platform.isMac
import com.recordlabs.desktop.platform.systemProperty

/**
 * macOS: lighter visuals for slower Macs (Settings > Appearance > Lighter visuals).
 *
 * "auto" (the default) turns them on for an Intel Mac with 4 processor threads or fewer
 * (a MacBook Air 2018–2020, say; see MacDesktop); the user can turn them on or off.
 * Then the background colours stay still, the other lyric lines have no soft blur and
 * the sung line moves 30 times a second instead of 60. The music, lyrics timing and
 * everything else stay as they are. Windows, Linux, Android and iOS never use it.
 */
object LightVisuals {
    /** This Mac counts as a slower one (set at launch by MacDesktop). */
    val isSlowerMac: Boolean by lazy { AppPlatform.isMac && systemProperty("recordlabs.slowerMac") == "true" }

    /** Read in composition: follows the setting. */
    val active: Boolean
        get() {
            if (!AppPlatform.isMac) return false
            return when (AppSettings.macLighterVisuals.value) {
                "on" -> true
                "off" -> false
                else -> isSlowerMac
            }
        }
}
