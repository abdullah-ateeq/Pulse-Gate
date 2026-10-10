package com.example.pulsegate.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class HapticType {
    TAP_DASH,
    GATE_PASS,
    PERFECT_PASS,
    COLLISION,
    BUTTON_CLICK
}

class HapticsManager(private val context: Context) {

    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrate(type: HapticType) {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (type) {
                    HapticType.TAP_DASH -> {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    }
                    HapticType.BUTTON_CLICK -> {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    }
                    HapticType.GATE_PASS -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(12, 100))
                    }
                    HapticType.PERFECT_PASS -> {
                        vibrator.vibrate(
                            VibrationEffect.createWaveform(
                                longArrayOf(0, 20, 30, 35),
                                intArrayOf(0, 160, 0, 240),
                                -1
                            )
                        )
                    }
                    HapticType.COLLISION -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(90, 255))
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (type) {
                    HapticType.TAP_DASH, HapticType.BUTTON_CLICK -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    HapticType.GATE_PASS -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(10, 80))
                    }
                    HapticType.PERFECT_PASS -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(45, 200))
                    }
                    HapticType.COLLISION -> {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, 255))
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.TAP_DASH, HapticType.BUTTON_CLICK, HapticType.GATE_PASS -> vibrator.vibrate(15)
                    HapticType.PERFECT_PASS -> vibrator.vibrate(40)
                    HapticType.COLLISION -> vibrator.vibrate(100)
                }
            }
        } catch (e: Exception) {
            // Ignore haptic failures gracefully
        }
    }
}
