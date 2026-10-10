package com.example.pulsegate.game

import androidx.compose.ui.graphics.Color

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float,
    var color: Color,
    val maxLife: Float,
    var currentLife: Float = 0f,
    var isAlive: Boolean = true
) {
    fun update(dt: Float) {
        if (!isAlive) return
        currentLife += dt
        if (currentLife >= maxLife) {
            isAlive = false
            return
        }
        x += vx * dt
        y += vy * dt
        // Fade out smoothly
        val progress = currentLife / maxLife
        alpha = (1.0f - progress).coerceIn(0f, 1f)
    }
}

data class Shockwave(
    val centerX: Float,
    val centerY: Float,
    var currentRadius: Float,
    val maxRadius: Float,
    val color: Color,
    val strokeWidth: Float = 4f,
    val duration: Float = 0.45f,
    var elapsed: Float = 0f,
    var isAlive: Boolean = true
) {
    fun update(dt: Float) {
        if (!isAlive) return
        elapsed += dt
        if (elapsed >= duration) {
            isAlive = false
            return
        }
        val progress = elapsed / duration
        currentRadius = progress * maxRadius
    }

    val alpha: Float
        get() = (1f - (elapsed / duration)).coerceIn(0f, 1f)
}
