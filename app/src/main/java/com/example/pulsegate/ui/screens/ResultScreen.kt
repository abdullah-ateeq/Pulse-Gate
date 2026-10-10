package com.example.pulsegate.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsegate.ui.components.AnimatedTunnelBackground
import com.example.pulsegate.ui.components.GlassPanel
import com.example.pulsegate.ui.components.NeonPrimaryButton
import com.example.pulsegate.ui.components.NeonSecondaryButton
import com.example.pulsegate.ui.theme.NeonAqua
import com.example.pulsegate.ui.theme.NeonCyan
import com.example.pulsegate.ui.theme.NeonGold

@Composable
fun ResultScreen(
    score: Int,
    isNewBest: Boolean,
    perfectCount: Int,
    gatesCount: Int,
    onPlayAgainClick: () -> Unit,
    onHomeClick: () -> Unit,
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scaleAnim = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedTunnelBackground(isLightMode = isLightMode, speedMultiplier = 0.5f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .scale(scaleAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "RUN COMPLETE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (isNewBest) {
                    Spacer(modifier = Modifier.height(10.dp))
                    NewBestBadge()
                }
            }

            // Central Score Card
            GlassPanel(
                cornerRadius = 28.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = NeonCyan
                    )

                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 72.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Minimal Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "GATES PASSED",
                            value = "$gatesCount"
                        )
                        StatItem(
                            label = "PERFECT PASSES",
                            value = "$perfectCount"
                        )
                    }
                }
            }

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonPrimaryButton(
                    text = "PLAY AGAIN",
                    icon = Icons.Default.Replay,
                    onClick = onPlayAgainClick,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(60.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NeonSecondaryButton(
                        text = "HOME",
                        icon = Icons.Default.Home,
                        onClick = onHomeClick,
                        modifier = Modifier.weight(1f),
                        borderColor = NeonCyan
                    )

                    NeonSecondaryButton(
                        text = "SHARE",
                        icon = Icons.Default.Share,
                        onClick = { shareScore(context, score) },
                        modifier = Modifier.weight(1f),
                        borderColor = NeonAqua
                    )
                }
            }
        }
    }
}

@Composable
private fun NewBestBadge() {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonGold, Color(0xFFFF9E00))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.6f), shape)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "★ NEW BEST ★",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            ),
            color = Color.Black
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun shareScore(context: Context, score: Int) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "I broke through $score gates in Pulse Gate! Can you beat my timing? #PulseGate"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Pulse Gate Score"))
}
