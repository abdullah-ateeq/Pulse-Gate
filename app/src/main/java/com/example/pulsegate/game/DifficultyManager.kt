package com.example.pulsegate.game

import kotlin.random.Random

class DifficultyManager {

    val milestones = listOf(25, 50, 75, 100)

    fun getApproachSpeed(score: Int): Float {
        return when {
            score < 10 -> 0.38f
            score < 25 -> 0.45f
            score < 50 -> 0.52f
            score < 75 -> 0.60f
            score < 100 -> 0.68f
            else -> 0.76f + (score - 100) * 0.001f
        }
    }

    fun getGateRotationSpeed(score: Int): Float {
        return when {
            score < 10 -> 65f + Random.nextFloat() * 15f
            score < 25 -> 85f + Random.nextFloat() * 20f
            score < 50 -> 110f + Random.nextFloat() * 25f
            score < 75 -> 135f + Random.nextFloat() * 30f
            score < 100 -> 160f + Random.nextFloat() * 35f
            else -> (180f + (score - 100) * 0.4f).coerceAtMost(240f)
        }
    }

    fun getGateGapSpan(score: Int): Float {
        return when {
            score < 10 -> 95f
            score < 25 -> 80f
            score < 50 -> 68f
            score < 75 -> 58f
            score < 100 -> 50f
            else -> (45f - (score - 100) * 0.05f).coerceAtLeast(38f)
        }
    }

    fun chooseGateType(score: Int): GateType {
        if (score < 10) {
            return GateType.BASIC
        }

        val roll = Random.nextFloat()
        return when {
            score < 25 -> {
                if (roll < 0.70f) GateType.BASIC else GateType.REVERSE
            }
            score < 50 -> {
                when {
                    roll < 0.45f -> GateType.BASIC
                    roll < 0.70f -> GateType.REVERSE
                    roll < 0.90f -> GateType.DOUBLE_GAP
                    else -> GateType.FAST
                }
            }
            score < 100 -> {
                when {
                    roll < 0.30f -> GateType.BASIC
                    roll < 0.50f -> GateType.DOUBLE_GAP
                    roll < 0.70f -> GateType.REVERSE
                    roll < 0.85f -> GateType.PULSE
                    else -> GateType.SPLIT
                }
            }
            else -> {
                // High-speed arcade mode
                when {
                    roll < 0.25f -> GateType.FAST
                    roll < 0.50f -> GateType.DOUBLE_GAP
                    roll < 0.70f -> GateType.PULSE
                    roll < 0.85f -> GateType.SPLIT
                    else -> GateType.REVERSE
                }
            }
        }
    }

    fun generateGate(id: Long, score: Int, initialZ: Float = 1.0f): Gate {
        val gateType = chooseGateType(score)
        val rotationSpeed = getGateRotationSpeed(score)
        val gapSpan = getGateGapSpan(score)
        val direction = if (gateType == GateType.REVERSE) -1.0f else if (Random.nextBoolean()) 1.0f else -1.0f

        val hasDoubleGap = gateType == GateType.DOUBLE_GAP
        val secondarySpan = (gapSpan * 0.85f).coerceAtLeast(35f)

        // Spawns with random starting rotation
        val startRotation = Random.nextFloat() * 360f

        // Occasional collectible bonus orb (approx 15% chance after score 5)
        val hasBonusOrb = score >= 5 && Random.nextFloat() < 0.18f

        return Gate(
            id = id,
            gateType = gateType,
            zDistance = initialZ,
            currentRotation = startRotation,
            rotationSpeed = rotationSpeed,
            rotationDirection = direction,
            gapStartAngle = 0f,
            gapSpanAngle = gapSpan,
            hasSecondaryGap = hasDoubleGap,
            secondaryGapStartAngle = 180f,
            secondaryGapSpanAngle = secondarySpan,
            hasBonusOrb = hasBonusOrb
        )
    }

    fun isMilestone(score: Int): Boolean {
        return milestones.contains(score)
    }
}
