package com.example.pulsegate

import com.example.pulsegate.game.DifficultyManager
import com.example.pulsegate.game.Gate
import com.example.pulsegate.game.GateType
import com.example.pulsegate.game.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PulseGateLogicTest {

    @Test
    fun gateCollision_whenBallAlignedWithCenterOfGap_returnsInsideGapAndMinimalDiff() {
        // Gap spans from 45 to 135 degrees (centered at 90)
        // Target angle is 90 degrees (bottom crossing lane)
        val gate = Gate(
            id = 1L,
            gateType = GateType.BASIC,
            zDistance = 0.5f,
            currentRotation = 45f,
            rotationSpeed = 60f,
            rotationDirection = 1f,
            gapStartAngle = 0f,
            gapSpanAngle = 90f // 45 + 0 to 45 + 90 = 45..135, center = 90
        )

        val (isInside, minDiff) = gate.checkPassAlignment(targetAngle = 90f, toleranceDeg = 2f)

        assertTrue("Expected ball to be inside gap", isInside)
        assertTrue("Expected minimal difference close to 0", minDiff < 0.1f)
    }

    @Test
    fun gateCollision_whenBallHitsSolidHazard_returnsOutsideGap() {
        // Gap is at 270 (top, 225..315). Target is at 90 (bottom)
        val gate = Gate(
            id = 2L,
            gateType = GateType.BASIC,
            zDistance = 0.5f,
            currentRotation = 225f,
            rotationSpeed = 60f,
            rotationDirection = 1f,
            gapStartAngle = 0f,
            gapSpanAngle = 90f
        )

        val (isInside, minDiff) = gate.checkPassAlignment(targetAngle = 90f, toleranceDeg = 2f)

        assertFalse("Expected ball to collide with solid section", isInside)
        assertTrue("Expected difference around 180 degrees", minDiff > 170f)
    }

    @Test
    fun gateCollision_doubleGapGate_detectsSecondaryOpening() {
        // Primary gap at 270 (top). Secondary gap at 270 + 180 = 90 (bottom)!
        val gate = Gate(
            id = 3L,
            gateType = GateType.DOUBLE_GAP,
            zDistance = 0.5f,
            currentRotation = 245f,
            rotationSpeed = 60f,
            rotationDirection = 1f,
            gapStartAngle = 0f,
            gapSpanAngle = 50f, // 245..295
            hasSecondaryGap = true,
            secondaryGapStartAngle = 180f,
            secondaryGapSpanAngle = 50f // 245 + 180 = 65..115 (centered at 90)
        )

        val (isInside, minDiff) = gate.checkPassAlignment(targetAngle = 90f, toleranceDeg = 2f)

        assertTrue("Expected ball to pass through secondary gap", isInside)
        assertTrue("Expected minimal diff to center of secondary gap", minDiff < 0.1f)
    }

    @Test
    fun difficultyProgression_increasesSpeedAndNarrowsGapFairly() {
        val diffManager = DifficultyManager()

        val earlySpeed = diffManager.getApproachSpeed(0)
        val lateSpeed = diffManager.getApproachSpeed(80)
        assertTrue("Later approach speed must be faster", lateSpeed > earlySpeed)

        val earlyGap = diffManager.getGateGapSpan(0)
        val lateGap = diffManager.getGateGapSpan(80)
        assertTrue("Later gap should be more challenging but fair", lateGap < earlyGap)
        assertTrue("Gap must remain fair and passable", lateGap >= 38f)

        // Milestones
        assertTrue(diffManager.isMilestone(25))
        assertTrue(diffManager.isMilestone(50))
        assertTrue(diffManager.isMilestone(75))
        assertTrue(diffManager.isMilestone(100))
        assertFalse(diffManager.isMilestone(15))
    }

    @Test
    fun player_dashTriggersSquashAndStretch() {
        val player = Player()
        assertEquals(1.0f, player.stretchX, 0.001f)
        assertEquals(1.0f, player.stretchY, 0.001f)
        assertFalse(player.isDashing)

        player.triggerDash()
        assertTrue(player.isDashing)

        // Advance by 0.05s (acceleration phase)
        player.update(0.05f)
        assertTrue("X should squash during forward dash", player.stretchX < 1.0f)
        assertTrue("Y should stretch along forward velocity", player.stretchY > 1.0f)

        // Advance past dash duration (0.18s)
        player.update(0.20f)
        assertFalse(player.isDashing)
        assertEquals(1.0f, player.stretchX, 0.001f)
        assertEquals(1.0f, player.stretchY, 0.001f)
    }
}
