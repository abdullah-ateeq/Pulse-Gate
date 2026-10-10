package com.example.pulsegate.game

import kotlin.math.PI
import kotlin.math.sin

class Player(
    val baseRadius: Float = 24f,
    val angleDegrees: Float = 90f // Bottom lane (6 o'clock)
) {
    var currentRadius: Float = baseRadius
    var hoverOffsetY: Float = 0f
    var stretchX: Float = 1.0f
    var stretchY: Float = 1.0f
    var isDashing: Boolean = false
    var dashProgress: Float = 0f // 0f..1f
    var glowIntensity: Float = 1.0f // 1.0f to 2.5f on pass
    var hitFlashAlpha: Float = 0f

    private var timeAccumulator: Float = 0f
    private val dashDuration: Float = 0.18f
    private var dashElapsed: Float = 0f

    fun update(dt: Float) {
        timeAccumulator += dt

        // Smooth floating hover when idle
        hoverOffsetY = sin(timeAccumulator * 3.5f) * 6f

        // Breathing energy pulse
        val breathingScale = 1.0f + 0.08f * sin(timeAccumulator * 5.0f)
        currentRadius = baseRadius * breathingScale

        // Dash animation
        if (isDashing) {
            dashElapsed += dt
            dashProgress = (dashElapsed / dashDuration).coerceIn(0f, 1f)

            // Dynamic squash and stretch during dash
            if (dashProgress < 0.4f) {
                // Acceleration forward stretch
                val factor = dashProgress / 0.4f
                stretchX = 1.0f - 0.25f * factor
                stretchY = 1.0f + 0.45f * factor
            } else {
                // Recovery spring back
                val factor = (dashProgress - 0.4f) / 0.6f
                stretchX = 0.75f + 0.25f * factor
                stretchY = 1.45f - 0.45f * factor
            }

            if (dashElapsed >= dashDuration) {
                isDashing = false
                dashProgress = 0f
                stretchX = 1.0f
                stretchY = 1.0f
            }
        } else {
            stretchX = 1.0f
            stretchY = 1.0f
        }

        // Decay extra glow
        if (glowIntensity > 1.0f) {
            glowIntensity = (glowIntensity - dt * 2.5f).coerceAtLeast(1.0f)
        }

        // Decay hit flash
        if (hitFlashAlpha > 0f) {
            hitFlashAlpha = (hitFlashAlpha - dt * 4f).coerceAtLeast(0f)
        }
    }

    fun triggerDash() {
        isDashing = true
        dashElapsed = 0f
        dashProgress = 0f
        glowIntensity = 2.0f
    }

    fun onGatePass(isPerfect: Boolean) {
        glowIntensity = if (isPerfect) 3.0f else 2.2f
    }

    fun onHit() {
        hitFlashAlpha = 1.0f
    }

    fun reset() {
        currentRadius = baseRadius
        hoverOffsetY = 0f
        stretchX = 1.0f
        stretchY = 1.0f
        isDashing = false
        dashProgress = 0f
        dashElapsed = 0f
        glowIntensity = 1.0f
        hitFlashAlpha = 0f
        timeAccumulator = 0f
    }
}
