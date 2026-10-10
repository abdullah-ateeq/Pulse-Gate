package com.example.pulsegate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.pulsegate.ui.theme.DarkBgIndigo
import com.example.pulsegate.ui.theme.DarkBgNavy
import com.example.pulsegate.ui.theme.DarkBgVoid
import com.example.pulsegate.ui.theme.NeonCyan
import com.example.pulsegate.ui.theme.NeonViolet
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedTunnelBackground(
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    speedMultiplier: Float = 1.0f
) {
    val transition = rememberInfiniteTransition(label = "tunnelBgAnim")

    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (4000 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseProgress"
    )

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (24000 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationDeg"
    )

    val bgGradient = if (isLightMode) {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFE2E8F0),
                Color(0xFFF1F5F9),
                Color(0xFFE2E8F0)
            )
        )
    } else {
        Brush.radialGradient(
            colors = listOf(
                DarkBgIndigo,
                DarkBgNavy,
                DarkBgVoid
            )
        )
    }

    val ringColor = if (isLightMode) {
        Color(0x220284C7)
    } else {
        NeonCyan.copy(alpha = 0.12f)
    }

    val spokeColor = if (isLightMode) {
        Color(0x187C3AED)
    } else {
        NeonViolet.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxRadius = (size.width.coerceAtLeast(size.height)) * 0.75f

            // Draw Perspective Spokes (Tunnel Wireframe)
            val numSpokes = 12
            val spokeAngleStep = (2 * Math.PI / numSpokes).toFloat()
            val rotRad = Math.toRadians(rotation.toDouble()).toFloat()

            for (i in 0 until numSpokes) {
                val angle = i * spokeAngleStep + rotRad
                val endX = cx + cos(angle) * maxRadius
                val endY = cy + sin(angle) * maxRadius
                drawLine(
                    color = spokeColor,
                    start = Offset(cx, cy),
                    end = Offset(endX, endY),
                    strokeWidth = 1.5f
                )
            }

            // Draw Concentric Tunnel Rings expanding outward
            val numRings = 6
            for (i in 0 until numRings) {
                val ringPhase = (pulse + i.toFloat() / numRings) % 1.0f
                // Perspective exponential scaling
                val scale = ringPhase * ringPhase
                val ringRadius = maxRadius * scale
                val alpha = (sin(ringPhase * Math.PI)).toFloat() * 0.35f

                if (ringRadius > 10f) {
                    drawCircle(
                        color = ringColor.copy(alpha = alpha),
                        center = Offset(cx, cy),
                        radius = ringRadius,
                        style = Stroke(width = 1.5f + scale * 2.5f)
                    )
                }
            }
        }
    }
}
