package com.recordlabs.desktop

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FloatingLyricsMultiMonitorTest {

    @Test
    fun testScreenTopologySignatureNotEmpty() {
        val sig = getScreenTopologySignature()
        assertNotNull(sig)
    }

    @Test
    fun testGetAllUsableScreenBoundsNotEmpty() {
        val bounds = getAllUsableScreenBounds()
        assertFalse(bounds.isEmpty(), "Should find at least 1 screen bound")
        val primary = bounds.first()
        assertTrue(primary.width > 0)
        assertTrue(primary.height > 0)
    }

    @Test
    fun testClampPositionWhenWindowIsFarOffScreenRecoversToSafeScreen() {
        // Assume coordinates far off into the void (e.g. from an unplugged secondary monitor)
        val farX = -99999
        val farY = -99999
        val (safeX, safeY) = clampPositionToScreens(farX, farY, 720, 175)

        val usableScreens = getAllUsableScreenBounds()
        val windowRect = java.awt.Rectangle(safeX, safeY, 720, 175)
        val intersectsAny = usableScreens.any { !it.intersection(windowRect).isEmpty }
        assertTrue(intersectsAny, "Recovered position must be inside an active screen")
    }

    @Test
    fun testClampPositionWhenWithinPrimaryScreenBoundsPreservesPosition() {
        val primary = getPrimaryUsableScreenBounds()
        val targetX = primary.x + 50
        val targetY = primary.y + 50

        val (clampedX, clampedY) = clampPositionToScreens(targetX, targetY, 720, 175)
        assertEquals(targetX, clampedX)
        assertEquals(targetY, clampedY)
    }
}
