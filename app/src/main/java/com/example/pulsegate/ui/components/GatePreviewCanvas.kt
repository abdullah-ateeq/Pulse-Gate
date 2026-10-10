package com.example.pulsegate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pulsegate.ui.theme.DangerRed
import com.example.pulsegate.ui.theme.HazardOrange
import com.example.pulsegate.ui.theme.NeonAqua
import com.example.pulsegate.ui.theme.NeonCyan
import com.example.pulsegate.ui.theme.NeonViolet
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GatePreviewCanvas(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 260.dp,
    autoDashBall: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "gatePreviewAnim")

    // Rotation of gate ring
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gateRot"
    )

    // Breathing pulse
    val breathing by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gatePulse"
    )

    // Ball loop dash animation (from bottom towards center and past)
    val dashPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ballDash"
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseRadius = (size.width / 2f) * 0.72f * breathing

        // 1. Draw outer neon ambient ring
        drawCircle(
            color = NeonCyan.copy(alpha = 0.15f),
            center = Offset(cx, cy),
            radius = baseRadius + 14f,
            style = Stroke(width = 1.5f)
        )

        // 2. Rotating gate hazard arcs (gap is e.g. 90 degrees wide from 0 to 90)
        val gapSpan = 85f
        val startSolidAngle = rotation + gapSpan
        val solidSweep = 360f - gapSpan

        val rectTopLeft = Offset(cx - baseRadius, cy - baseRadius)
        val rectSize = Size(baseRadius * 2f, baseRadius * 2f)

        // Draw solid hazard segment
        drawArc(
            color = DangerRed.copy(alpha = 0.25f),
            startAngle = startSolidAngle,
            sweepAngle = solidSweep,
            useCenter = false,
            topLeft = rectTopLeft,
            size = rectSize,
            style = Stroke(width = 24f)
        )

        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(HazardOrange, DangerRed, NeonViolet, HazardOrange)
            ),
            startAngle = startSolidAngle,
            sweepAngle = solidSweep,
            useCenter = false,
            topLeft = rectTopLeft,
            size = rectSize,
            style = Stroke(width = 14f)
        )

        // Draw edge markers on gap
        val gapEdge1 = Math.toRadians((rotation + gapSpan).toDouble())
        val gapEdge2 = Math.toRadians(rotation.toDouble())

        drawCircle(
            color = NeonCyan,
            center = Offset(
                cx + (cos(gapEdge1) * baseRadius).toFloat(),
                cy + (sin(gapEdge1) * baseRadius).toFloat()
            ),
            radius = 6f
        )
        drawCircle(
            color = NeonCyan,
            center = Offset(
                cx + (cos(gapEdge2) * baseRadius).toFloat(),
                cy + (sin(gapEdge2) * baseRadius).toFloat()
            ),
            radius = 6f
        )

        // 3. Draw safe opening soft indicator
        drawArc(
            color = NeonCyan.copy(alpha = 0.35f),
            startAngle = rotation,
            sweepAngle = gapSpan,
            useCenter = false,
            topLeft = rectTopLeft,
            size = rectSize,
            style = Stroke(width = 4f)
        )

        // 4. Player Energy Sphere
        // In preview mode: ball hovers at bottom or dashes forward
        val ballY = if (autoDashBall) {
            // dash starts at 0.70 of cycle
            when {
                dashPhase < 0.65f -> {
                    // Hover at bottom
                    cy + baseRadius * 0.95f + sin(dashPhase * 16.0).toFloat() * 4f
                }
                dashPhase < 0.90f -> {
                    // Dash forward through gate!
                    val progress = (dashPhase - 0.65f) / 0.25f
                    cy + baseRadius * 0.95f - progress * (baseRadius * 1.3f)
                }
                else -> {
                    // Reset smoothly
                    val progress = (dashPhase - 0.90f) / 0.10f
                    cy - baseRadius * 0.35f + progress * (baseRadius * 1.3f)
                }
            }
        } else {
            cy + baseRadius * 0.95f
        }

        val ballCenter = Offset(cx, ballY)

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.65f),
                    NeonAqua.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = ballCenter,
                radius = 32f
            ),
            center = ballCenter,
            radius = 32f
        )

        // Core energy sphere
        drawCircle(
            color = NeonCyan,
            center = ballCenter,
            radius = 16f
        )

        // Bright white center
        drawCircle(
            color = Color.White,
            center = ballCenter,
            radius = 9f
        )
    }
}
