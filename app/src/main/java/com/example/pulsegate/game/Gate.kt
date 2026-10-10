package com.example.pulsegate.game

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

enum class GateType {
    BASIC,        // Single generous gap
    DOUBLE_GAP,   // Two opposite gaps
    REVERSE,      // Rotates in opposite direction
    FAST,         // Faster rotation
    PULSE,        // Rhythmically modulating speed
    SPLIT         // Multi-segment ring with safe opening
}

data class Gate(
    val id: Long,
    val gateType: GateType,
    var zDistance: Float, // 1.0f = distant spawn, 0.0f = crossing plane
    var currentRotation: Float, // degrees
    var rotationSpeed: Float, // degrees / second
    var rotationDirection: Float, // 1.0f or -1.0f
    val gapStartAngle: Float, // relative to currentRotation (degrees)
    val gapSpanAngle: Float, // angular width of safe opening (degrees)
    val hasSecondaryGap: Boolean = false,
    val secondaryGapStartAngle: Float = 180f,
    val secondaryGapSpanAngle: Float = 50f,
    var pulseTimer: Float = 0f,
    val hasBonusOrb: Boolean = false,
    var bonusOrbCollected: Boolean = false,
    var isPassed: Boolean = false,
    var isHit: Boolean = false
) {

    fun update(dt: Float, speedMultiplier: Float) {
        pulseTimer += dt
        val effectiveSpeed = when (gateType) {
            GateType.PULSE -> {
                rotationSpeed * (1.0f + 0.5f * sin(pulseTimer * 4.0).toFloat())
            }
            GateType.FAST -> rotationSpeed * 1.35f
            else -> rotationSpeed
        } * speedMultiplier

        currentRotation = (currentRotation + effectiveSpeed * rotationDirection * dt) % 360f
        if (currentRotation < 0f) currentRotation += 360f
    }

    /**
     * Checks if the given angle (in degrees, 0..360, where 90 = bottom) is within a safe gap.
     * Returns:
     * - isInsideGap: Boolean
     * - angularDistanceFromCenter: Float (minimum degrees away from center of the gap)
     */
    fun checkPassAlignment(targetAngle: Float, toleranceDeg: Float = 4f): Pair<Boolean, Float> {
        val normalizedTarget = (targetAngle % 360f + 360f) % 360f

        // Check primary gap
        val primaryCenter = normalizeAngle(currentRotation + gapStartAngle + gapSpanAngle / 2f)
        val primaryHalfSpan = (gapSpanAngle / 2f) + toleranceDeg
        val diffPrimary = angleDifference(normalizedTarget, primaryCenter)

        var insideGap = abs(diffPrimary) <= primaryHalfSpan
        var minDiff = abs(diffPrimary)

        // Check secondary gap if present
        if (!insideGap && hasSecondaryGap) {
            val secondaryCenter = normalizeAngle(currentRotation + secondaryGapStartAngle + secondaryGapSpanAngle / 2f)
            val secondaryHalfSpan = (secondaryGapSpanAngle / 2f) + toleranceDeg
            val diffSecondary = angleDifference(normalizedTarget, secondaryCenter)

            if (abs(diffSecondary) <= secondaryHalfSpan) {
                insideGap = true
                minDiff = abs(diffSecondary)
            }
        }

        return Pair(insideGap, minDiff)
    }

    private fun normalizeAngle(angle: Float): Float {
        var a = angle % 360f
        if (a < 0f) a += 360f
        return a
    }

    private fun angleDifference(angle1: Float, angle2: Float): Float {
        var diff = (angle1 - angle2 + 180f) % 360f - 180f
        if (diff < -180f) diff += 360f
        return diff
    }
}
