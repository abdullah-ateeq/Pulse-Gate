package com.example.pulsegate.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.isActive
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsegate.game.GamePhase
import com.example.pulsegate.ui.components.GamePlayCanvas
import com.example.pulsegate.ui.components.GameTopBar
import com.example.pulsegate.ui.components.GlassPanel
import com.example.pulsegate.ui.components.NeonPrimaryButton
import com.example.pulsegate.ui.components.NeonSecondaryButton
import com.example.pulsegate.ui.theme.DarkBgVoid
import com.example.pulsegate.ui.theme.NeonAqua
import com.example.pulsegate.ui.theme.NeonCyan
import com.example.pulsegate.ui.theme.NeonGold
import com.example.pulsegate.ui.theme.WarningYellow
import com.example.pulsegate.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToResult: (score: Int, isNewBest: Boolean, perfectCount: Int, gatesCount: Int) -> Unit,
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()

    // Start a fresh game when entering the screen
    LaunchedEffect(Unit) {
        viewModel.startNewGame()
    }

    // Listen for Game Over event to navigate to Result Screen
    LaunchedEffect(Unit) {
        viewModel.gameOverEvent.collect { event ->
            onNavigateToResult(event.score, event.isNewBest, event.perfectCount, event.gatesCount)
        }
    }

    // Run frame-based game loop synced to display VSYNC
    LaunchedEffect(gameState.phase) {
        if (gameState.phase == GamePhase.PLAYING || gameState.phase == GamePhase.GAME_OVER) {
            var lastFrameTimeNanos = 0L
            while (isActive) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        val dt = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
                        viewModel.updateFrame(dt)
                    }
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isLightMode) MaterialTheme.colorScheme.background else DarkBgVoid)
            // Tap anywhere on the main screen to pulse / dash!
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (gameState.phase == GamePhase.PLAYING) {
                    viewModel.onTap()
                }
            }
    ) {
        // 1. Core Canvas (Tunnel, Gates, Particles, Shockwaves, Player Ball)
        GamePlayCanvas(
            gameEngine = viewModel.gameEngine,
            gameState = gameState,
            isLightMode = isLightMode
        )

        // 2. Minimal Sci-Fi Top Bar HUD (Best Score, Current Score, Pause Button)
        GameTopBar(
            currentScore = gameState.score,
            bestScore = gameState.bestScore,
            onPauseClick = { viewModel.pause() }
        )

        // 3. Countdown Overlay (3, 2, 1, GO!)
        AnimatedVisibility(
            visible = gameState.phase == GamePhase.COUNTDOWN,
            enter = fadeIn(tween(150)) + scaleIn(tween(150)),
            exit = fadeOut(tween(200)) + scaleOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CountdownDisplay(text = gameState.countdownText)
        }

        // 4. Perfect Notification Banner ("PERFECT", "PERFECT x2", etc.)
        if (gameState.perfectNotificationAlpha > 0.05f && gameState.perfectNotification != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 90.dp)
                    .alpha(gameState.perfectNotificationAlpha)
            ) {
                GlassPanel(
                    cornerRadius = 14.dp,
                    borderColor = NeonCyan,
                    backgroundColor = Color(0x9900F0FF)
                ) {
                    Text(
                        text = gameState.perfectNotification ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // 5. Speed Up Notification Banner
        if (gameState.speedUpAlpha > 0.05f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 135.dp)
                    .alpha(gameState.speedUpAlpha)
            ) {
                GlassPanel(
                    cornerRadius = 14.dp,
                    borderColor = WarningYellow,
                    backgroundColor = Color(0x99FFB703)
                ) {
                    Text(
                        text = "SPEED UP!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // 6. First-time Tutorial Banner
        if (gameState.showTutorial && gameState.phase == GamePhase.PLAYING) {
            TutorialHintBanner(
                step = gameState.tutorialStep,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
            )
        }

        // 7. Pause Menu Overlay
        AnimatedVisibility(
            visible = gameState.phase == GamePhase.PAUSED,
            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            PauseMenuDialog(
                onResume = { viewModel.resume() },
                onRestart = { viewModel.restart() },
                onHome = onNavigateToHome
            )
        }
    }
}

@Composable
private fun CountdownDisplay(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.Black,
            fontSize = 76.sp,
            letterSpacing = 4.sp
        ),
        color = if (text == "GO!") NeonAqua else NeonCyan
    )
}

@Composable
private fun TutorialHintBanner(step: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "tutHandAnim")
    val handBounce by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handBounce"
    )

    GlassPanel(
        cornerRadius = 18.dp,
        borderColor = NeonCyan.copy(alpha = 0.4f),
        backgroundColor = Color(0xCC070A14),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier
                    .size(32.dp)
                    .scale(handBounce)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (step == 0) "TAP WHEN THE GAP ALIGNS" else "TAP TO DASH",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun PauseMenuDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        GlassPanel(
            cornerRadius = 24.dp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            borderColor = NeonCyan.copy(alpha = 0.4f),
            backgroundColor = Color(0xEE0B1124)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                NeonPrimaryButton(
                    text = "RESUME",
                    icon = Icons.Default.PlayArrow,
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth()
                )

                NeonSecondaryButton(
                    text = "RESTART",
                    icon = Icons.Default.Refresh,
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonAqua
                )

                NeonSecondaryButton(
                    text = "HOME",
                    icon = Icons.Default.Home,
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color.Gray
                )
            }
        }
    }
}
