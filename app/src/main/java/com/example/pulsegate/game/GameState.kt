package com.example.pulsegate.game

import androidx.compose.ui.graphics.Color

enum class GamePhase {
    READY,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    GAME_OVER
}

data class GameState(
    val phase: GamePhase = GamePhase.READY,
    val score: Int = 0,
    val bestScore: Int = 0,
    val isNewBest: Boolean = false,
    val perfectStreak: Int = 0,
    val totalGatesPassed: Int = 0,
    val totalPerfectPasses: Int = 0,
    val countdownValue: Int = 3,
    val countdownText: String = "3",
    val showTutorial: Boolean = false,
    val tutorialStep: Int = 0, // 0 = "TAP WHEN THE GAP ALIGNS", 1 = "TAP TO DASH"
    val perfectNotification: String? = null,
    val perfectNotificationAlpha: Float = 0f,
    val speedUpNotification: Boolean = false,
    val speedUpAlpha: Float = 0f,
    val screenFlashAlpha: Float = 0f,
    val screenFlashColor: Color = Color.Cyan,
    val cameraShakeX: Float = 0f,
    val cameraShakeY: Float = 0f,
    val cameraZoom: Float = 1.0f,
    val tunnelGlowAlpha: Float = 0.2f
)
