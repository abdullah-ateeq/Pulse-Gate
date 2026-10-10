package com.example.pulsegate.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsegate.ui.theme.NeonCyan
import com.example.pulsegate.ui.theme.NeonGold

@Composable
fun ScoreCounter(
    score: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    var previousScore by remember { mutableStateOf(score) }
    var triggerPop by remember { mutableStateOf(false) }

    LaunchedEffect(score) {
        if (score > previousScore) {
            triggerPop = true
            previousScore = score
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (triggerPop) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = {
            triggerPop = false
        },
        label = "scorePop"
    )

    AnimatedContent(
        targetState = score,
        transitionSpec = {
            slideInVertically { height -> -height } togetherWith
                    slideOutVertically { height -> height }
        },
        modifier = modifier.scale(scale),
        label = "scoreAnim"
    ) { targetScore ->
        Text(
            text = "$targetScore",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            color = color
        )
    }
}

@Composable
fun GameTopBar(
    currentScore: Int,
    bestScore: Int,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Top-Left: Best Score
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "BEST",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                ),
                color = NeonGold.copy(alpha = 0.85f)
            )
            Text(
                text = "$bestScore",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        // Top-Center: Current Score
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScoreCounter(score = currentScore)
        }

        // Top-Right: Pause Button
        NeonIconButton(
            icon = Icons.Default.Pause,
            contentDescription = "Pause Game",
            onClick = onPauseClick,
            tint = NeonCyan
        )
    }
}
