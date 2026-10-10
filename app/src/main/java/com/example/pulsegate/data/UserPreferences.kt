package com.example.pulsegate.data

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val particlesEnabled: Boolean = true,
    val reducedEffects: Boolean = false,
    val tutorialCompleted: Boolean = false,
    val bestScore: Int = 0
)
