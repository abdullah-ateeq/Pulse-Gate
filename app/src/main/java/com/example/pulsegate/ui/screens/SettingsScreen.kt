package com.example.pulsegate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsegate.data.ThemeMode
import com.example.pulsegate.ui.components.AnimatedTunnelBackground
import com.example.pulsegate.ui.components.ConfirmDialog
import com.example.pulsegate.ui.components.GlassPanel
import com.example.pulsegate.ui.components.NeonSecondaryButton
import com.example.pulsegate.ui.components.SettingRow
import com.example.pulsegate.ui.theme.DangerRed
import com.example.pulsegate.ui.theme.NeonAqua
import com.example.pulsegate.ui.theme.NeonCyan
import com.example.pulsegate.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedTunnelBackground(isLightMode = isLightMode, speedMultiplier = 0.4f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Title
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            // 1. Theme Selection Group
            GlassPanel(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "THEME",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = NeonCyan,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionPill(
                            label = "DARK",
                            isSelected = settings.themeMode == ThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionPill(
                            label = "LIGHT",
                            isSelected = settings.themeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionPill(
                            label = "SYSTEM",
                            isSelected = settings.themeMode == ThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Audio & Haptics Group
            GlassPanel(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "FEEDBACK & AUDIO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = NeonCyan
                    )

                    SettingRow(
                        title = "Sound Effects",
                        subtitle = "Synthesized laser dash, gate chime, and crash audio",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        checked = settings.soundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) }
                    )

                    SettingRow(
                        title = "Haptic Feedback",
                        subtitle = "Tactile vibration pulses for dashes and gate breaks",
                        icon = Icons.Default.Vibration,
                        checked = settings.hapticsEnabled,
                        onCheckedChange = { viewModel.setHapticsEnabled(it) }
                    )
                }
            }

            // 3. Visuals & Performance Group
            GlassPanel(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "GRAPHICS & PARTICLES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = NeonCyan
                    )

                    SettingRow(
                        title = "Particle Effects",
                        subtitle = "Ball trail, spark bursts, and gate shockwaves",
                        icon = Icons.Default.AutoAwesome,
                        checked = settings.particlesEnabled,
                        onCheckedChange = { viewModel.setParticlesEnabled(it) }
                    )

                    SettingRow(
                        title = "Reduced Effects",
                        subtitle = "Optimized rendering for maximum battery life",
                        icon = Icons.Default.MotionPhotosOn,
                        checked = settings.reducedEffects,
                        onCheckedChange = { viewModel.setReducedEffects(it) }
                    )
                }
            }

            // 4. Data Reset Group
            GlassPanel(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                borderColor = DangerRed.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DATA MANAGEMENT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = DangerRed,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 12.dp)
                    )

                    NeonSecondaryButton(
                        text = "RESET BEST SCORE",
                        icon = Icons.Default.Delete,
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = DangerRed
                    )
                }
            }
        }

        // Confirmation Dialog
        if (showResetDialog) {
            ConfirmDialog(
                title = "RESET BEST SCORE?",
                message = "This will permanently clear your high score of ${settings.bestScore}. This action cannot be undone.",
                confirmText = "RESET",
                dismissText = "CANCEL",
                onConfirm = {
                    viewModel.resetBestScore()
                },
                onDismiss = {
                    showResetDialog = false
                }
            )
        }
    }
}

@Composable
private fun ThemeOptionPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
    val textColor = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
    val borderCol = if (isSelected) NeonCyan else Color.Transparent

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                letterSpacing = 1.sp
            ),
            color = textColor
        )
    }
}
