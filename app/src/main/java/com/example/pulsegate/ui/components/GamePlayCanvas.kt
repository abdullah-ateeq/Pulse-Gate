package com.example.pulsegate.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.example.pulsegate.game.GameEngine
import com.example.pulsegate.game.GameState
import com.example.pulsegate.game.Gate
import com.example.pulsegate.game.GateType
import com.example.pulsegate.ui.theme.DangerRed
import com.example.pulsegate.ui.theme.HazardOrange
import com.example.pulsegate.ui.theme.NeonAqua
import com.example.pulsegate.ui.theme.NeonCyan
import com.example.pulsegate.ui.theme.NeonGold
import com.example.pulsegate.ui.theme.NeonViolet
import com.example.pulsegate.ui.theme.WarningYellow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GamePlayCanvas(
    gameEngine: GameEngine,
    gameState: GameState,
    isLightMode: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.46f // Vanishing point
        val maxTunnelRadius = (size.width.coerceAtLeast(size.height)) * 0.85f
        val baseGateRadius = size.width * 0.38f

        // Apply camera shake and zoom
        translate(left = gameState.cameraShakeX, top = gameState.cameraShakeY) {
            scale(scale = gameState.cameraZoom, pivot = Offset(cx, cy)) {

                // 1. Draw Deep Futuristic Tunnel Perspective Wireframe
                drawTunnelPerspective(
                    cx = cx,
                    cy = cy,
                    maxRadius = maxTunnelRadius,
                    tunnelRings = gameEngine.tunnelRings,
                    glowAlpha = gameState.tunnelGlowAlpha,
                    isLightMode = isLightMode
                )

                // 2. Draw Gates (back-to-front sorting for correct occlusion)
                val sortedGates = gameEngine.gates.sortedByDescending { it.zDistance }
                for (gate in sortedGates) {
                    if (gate.zDistance > -0.3f) {
                        drawFuturisticGate(
                            cx = cx,
                            cy = cy,
                            baseRadius = baseGateRadius,
                            gate = gate,
                            isLightMode = isLightMode
                        )
                    }
                }

                // 3. Draw Particles (speed dust, pass sparks, shatter debris)
                for (p in gameEngine.particles) {
                    if (p.isAlive) {
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            center = Offset(cx + p.x, cy + p.y),
                            radius = p.radius
                        )
                    }
                }

                // 4. Draw Expanding Shockwaves
                for (sw in gameEngine.shockwaves) {
                    if (sw.isAlive) {
                        drawCircle(
                            color = sw.color.copy(alpha = sw.alpha),
                            center = Offset(cx + sw.centerX, cy + sw.centerY),
                            radius = sw.currentRadius,
                            style = Stroke(width = sw.strokeWidth)
                        )
                    }
                }

                // 5. Draw Player Energy Sphere
                val player = gameEngine.player
                val crossingGateY = cy + baseGateRadius * 0.96f
                val playerY = crossingGateY + player.hoverOffsetY - (if (player.isDashing) 40f * (1f - player.dashProgress) else 0f)
                val ballCenter = Offset(cx, playerY)

                drawPlayerSphere(
                    center = ballCenter,
                    player = player,
                    isLightMode = isLightMode
                )
            }
        }

        // 6. Draw Fullscreen Flash Overlay (e.g. on perfect pass or hit)
        if (gameState.screenFlashAlpha > 0.01f) {
            drawRect(
                color = gameState.screenFlashColor.copy(alpha = gameState.screenFlashAlpha)
            )
        }
    }
}

private fun DrawScope.drawTunnelPerspective(
    cx: Float,
    cy: Float,
    maxRadius: Float,
    tunnelRings: FloatArray,
    glowAlpha: Float,
    isLightMode: Boolean
) {
    val spokeColor = if (isLightMode) Color(0x180284C7) else NeonCyan.copy(alpha = 0.08f)
    val ringBaseColor = if (isLightMode) Color(0x300284C7) else NeonCyan.copy(alpha = 0.15f * glowAlpha * 4f)

    // Perspective Lines from Vanishing Point
    val numSpokes = 8
    val angleStep = (2 * Math.PI / numSpokes).toFloat()
    for (i in 0 until numSpokes) {
        val angle = i * angleStep
        val endX = cx + cos(angle) * maxRadius
        val endY = cy + sin(angle) * maxRadius
        drawLine(
            color = spokeColor,
            start = Offset(cx, cy),
            end = Offset(endX, endY),
            strokeWidth = 1.2f
        )
    }

    // Concentric Tunnel Depth Rings
    for (z in tunnelRings) {
        val scale = perspectiveScale(z)
        val radius = maxRadius * 0.35f * scale
        if (radius > 8f && radius < maxRadius * 1.2f) {
            val alpha = ((1.0f - z) * 0.4f * (glowAlpha / 0.2f)).coerceIn(0.05f, 0.7f)
            drawCircle(
                color = ringBaseColor.copy(alpha = alpha),
                center = Offset(cx, cy),
                radius = radius,
                style = Stroke(width = 1.5f + scale * 0.8f)
            )
        }
    }
}

private fun DrawScope.drawFuturisticGate(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    gate: Gate,
    isLightMode: Boolean
) {
    val z = gate.zDistance
    val scale = perspectiveScale(z)
    val gateRadius = baseRadius * scale * 0.75f
    if (gateRadius < 4f) return

    val alpha = if (z < 0f) {
        (1.0f + z * 3f).coerceIn(0f, 1f) // Fade out when passing behind camera
    } else {
        ((1.5f - z * 0.5f) / 1.5f).coerceIn(0.2f, 1f)
    }

    val rectTopLeft = Offset(cx - gateRadius, cy - gateRadius)
    val rectSize = Size(gateRadius * 2f, gateRadius * 2f)
    val strokeWidth = (14f * scale * 0.65f).coerceIn(4f, 32f)

    // 1. Thin Outer Decorative Ring
    drawCircle(
        color = (if (isLightMode) Color(0x400284C7) else NeonCyan.copy(alpha = 0.22f * alpha)),
        center = Offset(cx, cy),
        radius = gateRadius + strokeWidth * 0.8f,
        style = Stroke(width = 1.2f)
    )

    // 2. Solid Hazard Segments vs Safe Gaps
    if (gate.hasSecondaryGap) {
        // Double Gap Gate
        drawDoubleGapSegments(
            rectTopLeft = rectTopLeft,
            rectSize = rectSize,
            gate = gate,
            strokeWidth = strokeWidth,
            alpha = alpha
        )
    } else {
        // Single Gap Gate (Basic, Fast, Pulse, Reverse, Split)
        drawSingleGapSegments(
            rectTopLeft = rectTopLeft,
            rectSize = rectSize,
            gate = gate,
            strokeWidth = strokeWidth,
            alpha = alpha
        )
    }

    // 3. Safe Gap Edge Indicators (Glowing Nodes)
    val radEdge1 = Math.toRadians((gate.currentRotation + gate.gapStartAngle).toDouble())
    val radEdge2 = Math.toRadians((gate.currentRotation + gate.gapStartAngle + gate.gapSpanAngle).toDouble())
    val nodeRadius = (5f * scale * 0.6f).coerceIn(2.5f, 9f)

    drawCircle(
        color = NeonCyan.copy(alpha = alpha),
        center = Offset(cx + (cos(radEdge1) * gateRadius).toFloat(), cy + (sin(radEdge1) * gateRadius).toFloat()),
        radius = nodeRadius
    )
    drawCircle(
        color = NeonCyan.copy(alpha = alpha),
        center = Offset(cx + (cos(radEdge2) * gateRadius).toFloat(), cy + (sin(radEdge2) * gateRadius).toFloat()),
        radius = nodeRadius
    )

    // 4. Bonus Energy Orb inside gap if available
    if (gate.hasBonusOrb && !gate.bonusOrbCollected) {
        val orbAngle = Math.toRadians((gate.currentRotation + gate.gapStartAngle + gate.gapSpanAngle / 2f).toDouble())
        val orbCenter = Offset(
            cx + (cos(orbAngle) * gateRadius).toFloat(),
            cy + (sin(orbAngle) * gateRadius).toFloat()
        )
        val orbRadius = (8f * scale * 0.6f).coerceIn(3.5f, 14f)

        // Orb Halo
        drawCircle(
            color = NeonGold.copy(alpha = 0.5f * alpha),
            center = orbCenter,
            radius = orbRadius * 1.8f
        )
        // Orb Core
        drawCircle(
            color = NeonGold.copy(alpha = alpha),
            center = orbCenter,
            radius = orbRadius
        )
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            center = orbCenter,
            radius = orbRadius * 0.45f
        )
    }
}

private fun DrawScope.drawSingleGapSegments(
    rectTopLeft: Offset,
    rectSize: Size,
    gate: Gate,
    strokeWidth: Float,
    alpha: Float
) {
    val startAngle = gate.currentRotation + gate.gapStartAngle + gate.gapSpanAngle
    val sweepAngle = 360f - gate.gapSpanAngle

    val hazardColor = when (gate.gateType) {
        GateType.FAST -> HazardOrange
        GateType.PULSE -> NeonViolet
        GateType.REVERSE -> WarningYellow
        GateType.SPLIT -> DangerRed
        else -> DangerRed
    }

    // Hazard dark metallic backing
    drawArc(
        color = Color(0xFF1E2235).copy(alpha = 0.85f * alpha),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = rectTopLeft,
        size = rectSize,
        style = Stroke(width = strokeWidth * 1.35f)
    )

    // Vibrant Hazard Neon Arc
    drawArc(
        color = hazardColor.copy(alpha = alpha),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = rectTopLeft,
        size = rectSize,
        style = Stroke(width = strokeWidth)
    )

    // Safe Gap Subtle Neutral Arc (Guide Line)
    drawArc(
        color = NeonCyan.copy(alpha = 0.28f * alpha),
        startAngle = gate.currentRotation + gate.gapStartAngle,
        sweepAngle = gate.gapSpanAngle,
        useCenter = false,
        topLeft = rectTopLeft,
        size = rectSize,
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawDoubleGapSegments(
    rectTopLeft: Offset,
    rectSize: Size,
    gate: Gate,
    strokeWidth: Float,
    alpha: Float
) {
    // Gap 1: from currentRotation to currentRotation + gapSpan
    // Gap 2: from currentRotation + 180 to currentRotation + 180 + secSpan
    // Solid 1: between end of Gap 1 and start of Gap 2
    val solid1Start = gate.currentRotation + gate.gapSpanAngle
    val solid1Sweep = (180f - gate.gapSpanAngle).coerceAtLeast(10f)

    // Solid 2: between end of Gap 2 and start of Gap 1
    val solid2Start = gate.currentRotation + 180f + gate.secondaryGapSpanAngle
    val solid2Sweep = (180f - gate.secondaryGapSpanAngle).coerceAtLeast(10f)

    for ((start, sweep) in listOf(Pair(solid1Start, solid1Sweep), Pair(solid2Start, solid2Sweep))) {
        drawArc(
            color = Color(0xFF1E2235).copy(alpha = 0.85f * alpha),
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = rectTopLeft,
            size = rectSize,
            style = Stroke(width = strokeWidth * 1.35f)
        )
        drawArc(
            color = HazardOrange.copy(alpha = alpha),
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = rectTopLeft,
            size = rectSize,
            style = Stroke(width = strokeWidth)
        )
    }

    // Safe gap subtle guides
    drawArc(
        color = NeonAqua.copy(alpha = 0.3f * alpha),
        startAngle = gate.currentRotation,
        sweepAngle = gate.gapSpanAngle,
        useCenter = false,
        topLeft = rectTopLeft,
        size = rectSize,
        style = Stroke(width = 2.5f)
    )
    drawArc(
        color = NeonAqua.copy(alpha = 0.3f * alpha),
        startAngle = gate.currentRotation + 180f,
        sweepAngle = gate.secondaryGapSpanAngle,
        useCenter = false,
        topLeft = rectTopLeft,
        size = rectSize,
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawPlayerSphere(
    center: Offset,
    player: com.example.pulsegate.game.Player,
    isLightMode: Boolean
) {
    scale(
        scaleX = player.stretchX,
        scaleY = player.stretchY,
        pivot = center
    ) {
        val radius = player.currentRadius
        val glowRadius = radius * (2.2f * player.glowIntensity)

        // 1. Soft Outer Bloom Halo
        val haloColor = if (player.hitFlashAlpha > 0.05f) {
            DangerRed.copy(alpha = 0.8f * player.hitFlashAlpha)
        } else {
            NeonCyan.copy(alpha = (0.35f * player.glowIntensity).coerceAtMost(0.85f))
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(haloColor, NeonAqua.copy(alpha = 0.15f), Color.Transparent),
                center = center,
                radius = glowRadius
            ),
            center = center,
            radius = glowRadius
        )

        // 2. Cyan Energy Core
        val coreColor = if (player.hitFlashAlpha > 0.05f) {
            Color.White
        } else {
            NeonCyan
        }
        drawCircle(
            color = coreColor,
            center = center,
            radius = radius
        )

        // 3. Bright White Hot Center
        drawCircle(
            color = Color.White,
            center = center,
            radius = radius * 0.55f
        )
    }
}

private fun perspectiveScale(z: Float): Float {
    return 1.0f / (0.32f + z * 1.55f).coerceAtLeast(0.08f)
}
