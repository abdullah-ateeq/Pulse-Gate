package com.example.pulsegate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsegate.ui.components.AnimatedTunnelBackground
import com.example.pulsegate.ui.components.GatePreviewCanvas
import com.example.pulsegate.ui.components.GlassPanel
import com.example.pulsegate.ui.components.NeonPrimaryButton
import com.example.pulsegate.ui.theme.NeonCyan
import com.example.pulsegate.ui.theme.NeonGold

@Composable
fun HomeScreen(
    bestScore: Int,
    onPlayClick: () -> Unit,
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedTunnelBackground(isLightMode = isLightMode)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Pulse Gate Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "PULSE GATE",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp,
                        letterSpacing = 4.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "TIME THE PULSE • BREAK THROUGH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = NeonCyan.copy(alpha = 0.9f)
                )
            }

            // Center: Large Animated Interactive Preview
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                GatePreviewCanvas(sizeDp = 270.dp, autoDashBall = true)
            }

            // Bottom Section: Best Score & PLAY Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Best Score Pill
                GlassPanel(
                    cornerRadius = 16.dp,
                    modifier = Modifier.padding(bottom = 20.dp),
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "BEST SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = NeonGold
                        )
                        Text(
                            text = "$bestScore",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Main PLAY Button
                NeonPrimaryButton(
                    text = "PLAY",
                    icon = Icons.Default.PlayArrow,
                    onClick = onPlayClick,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(64.dp)
                )
            }
        }
    }
}
