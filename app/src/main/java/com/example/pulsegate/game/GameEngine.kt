package com.example.pulsegate.game

import androidx.compose.ui.graphics.Color
import com.example.pulsegate.audio.SoundEffect
import com.example.pulsegate.audio.SoundManager
import com.example.pulsegate.haptics.HapticType
import com.example.pulsegate.haptics.HapticsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    private val soundManager: SoundManager,
    private val hapticsManager: HapticsManager,
    private val onGameOverCallback: (score: Int, isNewBest: Boolean, perfectCount: Int, gatesCount: Int) -> Unit,
    private val onTutorialPassedCallback: () -> Unit
) {

    val player = Player()
    val difficultyManager = DifficultyManager()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Gates in the tunnel
    val gates = mutableListOf<Gate>()
    private var nextGateId = 1L

    // Particle pool (fixed size to avoid garbage collection)
    val particles = mutableListOf<Particle>()
    val shockwaves = mutableListOf<Shockwave>()

    // Tunnel rings z-offsets (0.0 to 1.0)
    val tunnelRings = floatArrayOf(0.12f, 0.28f, 0.44f, 0.60f, 0.76f, 0.92f)

    // Game settings flags
    var particlesEnabled: Boolean = true
    var reducedEffects: Boolean = false

    // Camera and animation states
    private var timeDilation: Float = 1.0f
    private var timeDilationTimer: Float = 0f
    private var freezeTimer: Float = 0f
    private var gameOverTransitionTimer: Float = 0f

    // Performance tracking
    private var gatesPassedInRun = 0
    private var perfectPassesInRun = 0

    fun startNewGame(bestScore: Int, tutorialCompleted: Boolean) {
        player.reset()
        gates.clear()
        particles.clear()
        shockwaves.clear()
        nextGateId = 1L
        gatesPassedInRun = 0
        perfectPassesInRun = 0
        timeDilation = 1.0f
        timeDilationTimer = 0f
        freezeTimer = 0f
        gameOverTransitionTimer = 0f

        // Initial gates: 1st approaching, 2nd and 3rd visible down the tunnel
        gates.add(difficultyManager.generateGate(nextGateId++, 0, initialZ = 0.95f))
        gates.add(difficultyManager.generateGate(nextGateId++, 0, initialZ = 1.95f))
        gates.add(difficultyManager.generateGate(nextGateId++, 0, initialZ = 2.95f))

        _gameState.value = GameState(
            phase = GamePhase.COUNTDOWN,
            score = 0,
            bestScore = bestScore,
            isNewBest = false,
            perfectStreak = 0,
            totalGatesPassed = 0,
            totalPerfectPasses = 0,
            countdownValue = 3,
            countdownText = "3",
            showTutorial = !tutorialCompleted,
            tutorialStep = 0
        )
    }

    fun updateCountdown(remainingStep: Int) {
        if (_gameState.value.phase != GamePhase.COUNTDOWN) return

        when (remainingStep) {
            3 -> _gameState.update { it.copy(countdownValue = 3, countdownText = "3") }
            2 -> {
                soundManager.play(SoundEffect.BUTTON_CLICK)
                _gameState.update { it.copy(countdownValue = 2, countdownText = "2") }
            }
            1 -> {
                soundManager.play(SoundEffect.BUTTON_CLICK)
                _gameState.update { it.copy(countdownValue = 1, countdownText = "1") }
            }
            0 -> {
                soundManager.play(SoundEffect.GATE_PASS)
                _gameState.update { it.copy(countdownValue = 0, countdownText = "GO!") }
            }
            -1 -> {
                // Countdown complete -> PLAYING
                _gameState.update { it.copy(phase = GamePhase.PLAYING) }
            }
        }
    }

    fun pauseGame() {
        if (_gameState.value.phase == GamePhase.PLAYING) {
            _gameState.update { it.copy(phase = GamePhase.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_gameState.value.phase == GamePhase.PAUSED) {
            _gameState.update { it.copy(phase = GamePhase.PLAYING) }
        }
    }

    /**
     * Called on each frame with delta time in seconds.
     */
    fun update(rawDt: Float) {
        val currentState = _gameState.value

        // Handle game over sequence
        if (currentState.phase == GamePhase.GAME_OVER) {
            updateGameOverSequence(rawDt)
            return
        }

        if (currentState.phase != GamePhase.PLAYING) return

        // Freeze frame brief check (e.g. right before shatter)
        if (freezeTimer > 0f) {
            freezeTimer -= rawDt
            return
        }

        // Slow motion (time dilation) decay
        if (timeDilationTimer > 0f) {
            timeDilationTimer -= rawDt
            if (timeDilationTimer <= 0f) {
                timeDilation = 1.0f
            }
        }

        val dt = (rawDt * timeDilation).coerceIn(0.001f, 0.05f)
        val score = currentState.score
        val approachSpeed = difficultyManager.getApproachSpeed(score)

        // 1. Update Player
        player.update(dt)

        // Spawn ball energy trail particles
        if (particlesEnabled && !reducedEffects) {
            spawnBallTrailParticle()
        }

        // 2. Update Tunnel Rings
        val tunnelSpeed = approachSpeed * (if (player.isDashing) 2.2f else 1.0f)
        for (i in tunnelRings.indices) {
            tunnelRings[i] -= dt * tunnelSpeed
            if (tunnelRings[i] < 0f) {
                tunnelRings[i] += 1.0f
            }
        }

        // 3. Update Gates
        var activeGateIndex = -1
        for (i in gates.indices) {
            val gate = gates[i]
            gate.update(dt, speedMultiplier = if (score > 100) 1.2f else 1.0f)
            gate.zDistance -= dt * tunnelSpeed

            if (!gate.isPassed && activeGateIndex == -1) {
                activeGateIndex = i
            }
        }

        // Check if active gate reached player plane without dashing
        if (activeGateIndex != -1) {
            val activeGate = gates[activeGateIndex]
            if (activeGate.zDistance <= 0.05f && !activeGate.isPassed) {
                // Ball crossed gate plane!
                val (isInsideGap, minDiff) = activeGate.checkPassAlignment(player.angleDegrees)
                if (isInsideGap) {
                    // Safe glide through!
                    handleGatePassSuccess(activeGate, minDiff)
                } else {
                    // Crashed into solid gate!
                    handleCollision(activeGate)
                    return
                }
            }
        }

        // Remove gates that moved far behind camera and spawn new ones
        gates.removeAll { it.zDistance < -0.4f }
        while (gates.size < 3) {
            val lastZ = gates.lastOrNull()?.zDistance ?: 1.0f
            gates.add(difficultyManager.generateGate(nextGateId++, score, initialZ = lastZ + 1.0f))
        }

        // 4. Update Particles
        if (particlesEnabled) {
            val pIterator = particles.iterator()
            while (pIterator.hasNext()) {
                val p = pIterator.next()
                p.update(dt)
                if (!p.isAlive) {
                    pIterator.remove()
                }
            }
        }

        // 5. Update Shockwaves
        val sIterator = shockwaves.iterator()
        while (sIterator.hasNext()) {
            val sw = sIterator.next()
            sw.update(dt)
            if (!sw.isAlive) {
                sIterator.remove()
            }
        }

        // 6. Decay visual overlays (flash, perfect notification, speed up notification, shake)
        updateVisualOverlays(dt)
    }

    /**
     * User tapped on the screen!
     */
    fun onTapToDash() {
        if (_gameState.value.phase != GamePhase.PLAYING) return

        player.triggerDash()
        soundManager.play(SoundEffect.DASH)
        hapticsManager.vibrate(HapticType.TAP_DASH)

        // Subtle camera zoom pulse
        _gameState.update {
            it.copy(
                cameraZoom = 1.03f,
                cameraShakeX = (Random.nextFloat() - 0.5f) * 4f,
                cameraShakeY = (Random.nextFloat() - 0.5f) * 4f
            )
        }

        // Spawn burst trail
        if (particlesEnabled && !reducedEffects) {
            spawnDashBurstParticles()
        }

        // Check if there is an active gate approaching in the dash reach zone
        val activeGate = gates.firstOrNull { !it.isPassed }
        if (activeGate != null) {
            if (activeGate.zDistance <= 0.65f) {
                // Within strike distance! Break through!
                val (isInsideGap, minDiff) = activeGate.checkPassAlignment(player.angleDegrees)
                if (isInsideGap) {
                    handleGatePassSuccess(activeGate, minDiff)
                } else {
                    handleCollision(activeGate)
                }
            } else {
                // Gate is still far; advance approach distance slightly for responsiveness
                activeGate.zDistance = (activeGate.zDistance - 0.18f).coerceAtLeast(0.66f)
            }
        }
    }

    private fun handleGatePassSuccess(gate: Gate, minDiff: Float) {
        gate.isPassed = true
        gatesPassedInRun++

        val perfectThreshold = (gate.gapSpanAngle * 0.22f).coerceAtLeast(8f)
        val isPerfect = minDiff <= perfectThreshold

        var addedScore = 1
        var newStreak = 0

        if (isPerfect) {
            perfectPassesInRun++
            addedScore += 1 // Bonus +1 for perfect
            newStreak = _gameState.value.perfectStreak + 1

            // Trigger brief slow-mo micro-pause (0.12s)
            timeDilation = 0.35f
            timeDilationTimer = 0.12f

            soundManager.play(SoundEffect.PERFECT_PASS)
            hapticsManager.vibrate(HapticType.PERFECT_PASS)
            player.onGatePass(isPerfect = true)

            // Spawn cyan/gold perfect shockwave
            shockwaves.add(
                Shockwave(
                    centerX = 0f,
                    centerY = 0f,
                    currentRadius = 10f,
                    maxRadius = 380f,
                    color = Color(0xFF00F0FF),
                    strokeWidth = 6f
                )
            )

            // High-density sparkle burst
            if (particlesEnabled) {
                spawnPassBurstParticles(isPerfect = true)
            }

            val streakText = if (newStreak > 1) "PERFECT x$newStreak" else "PERFECT"
            _gameState.update {
                it.copy(
                    perfectNotification = streakText,
                    perfectNotificationAlpha = 1.0f,
                    screenFlashAlpha = 0.35f,
                    screenFlashColor = Color(0xFF00F0FF),
                    tunnelGlowAlpha = 0.45f
                )
            }
        } else {
            newStreak = 0
            soundManager.play(SoundEffect.GATE_PASS)
            hapticsManager.vibrate(HapticType.GATE_PASS)
            player.onGatePass(isPerfect = false)

            shockwaves.add(
                Shockwave(
                    centerX = 0f,
                    centerY = 0f,
                    currentRadius = 10f,
                    maxRadius = 300f,
                    color = Color(0xFF00E5BC),
                    strokeWidth = 4f
                )
            )

            if (particlesEnabled) {
                spawnPassBurstParticles(isPerfect = false)
            }
        }

        // Bonus orb check
        if (gate.hasBonusOrb && !gate.bonusOrbCollected) {
            gate.bonusOrbCollected = true
            addedScore += 2
        }

        val currentScore = _gameState.value.score
        val newScore = currentScore + addedScore
        val currentBest = _gameState.value.bestScore
        val isNewBest = newScore > currentBest
        val updatedBest = if (isNewBest) newScore else currentBest

        // Speed milestone check
        val isMilestone = difficultyManager.isMilestone(newScore)
        if (isMilestone) {
            soundManager.play(SoundEffect.MILESTONE)
            _gameState.update {
                it.copy(
                    speedUpNotification = true,
                    speedUpAlpha = 1.0f,
                    tunnelGlowAlpha = 0.6f
                )
            }
        }

        // Tutorial handling (dismiss after 2 passes)
        var showTutorial = _gameState.value.showTutorial
        var tutorialStep = _gameState.value.tutorialStep
        if (showTutorial) {
            if (gatesPassedInRun >= 2) {
                showTutorial = false
                tutorialStep = 2
                onTutorialPassedCallback()
            } else {
                tutorialStep = 1
            }
        }

        // Camera forward impulse
        _gameState.update {
            it.copy(
                score = newScore,
                bestScore = updatedBest,
                isNewBest = isNewBest,
                perfectStreak = newStreak,
                totalGatesPassed = gatesPassedInRun,
                totalPerfectPasses = perfectPassesInRun,
                showTutorial = showTutorial,
                tutorialStep = tutorialStep,
                cameraZoom = 1.05f
            )
        }
    }

    private fun handleCollision(gate: Gate) {
        gate.isHit = true
        freezeTimer = 0.08f // Brief dramatic freeze frame

        player.onHit()
        soundManager.play(SoundEffect.COLLISION)
        hapticsManager.vibrate(HapticType.COLLISION)

        // Shatter player into sparks
        if (particlesEnabled) {
            spawnCollisionShatterParticles()
        }

        // Red hazard shockwave from gate
        shockwaves.add(
            Shockwave(
                centerX = 0f,
                centerY = 0f,
                currentRadius = 20f,
                maxRadius = 450f,
                color = Color(0xFFFF2A55),
                strokeWidth = 8f,
                duration = 0.6f
            )
        )

        val finalScore = _gameState.value.score
        val isNewBest = _gameState.value.isNewBest

        _gameState.update {
            it.copy(
                phase = GamePhase.GAME_OVER,
                perfectStreak = 0,
                screenFlashAlpha = 0.7f,
                screenFlashColor = Color(0xFFFF2A55),
                cameraShakeX = (Random.nextFloat() - 0.5f) * 24f,
                cameraShakeY = (Random.nextFloat() - 0.5f) * 24f
            )
        }

        gameOverTransitionTimer = 0.85f // Delay before opening result screen
    }

    private fun updateGameOverSequence(dt: Float) {
        if (gameOverTransitionTimer > 0f) {
            gameOverTransitionTimer -= dt
            updateVisualOverlays(dt)

            // Continue updating shatter particles during game over sequence
            if (particlesEnabled) {
                val pIterator = particles.iterator()
                while (pIterator.hasNext()) {
                    val p = pIterator.next()
                    p.update(dt)
                    if (!p.isAlive) pIterator.remove()
                }
            }

            val sIterator = shockwaves.iterator()
            while (sIterator.hasNext()) {
                val sw = sIterator.next()
                sw.update(dt)
                if (!sw.isAlive) sIterator.remove()
            }

            if (gameOverTransitionTimer <= 0f) {
                val s = _gameState.value
                onGameOverCallback(s.score, s.isNewBest, perfectPassesInRun, gatesPassedInRun)
            }
        }
    }

    private fun updateVisualOverlays(dt: Float) {
        _gameState.update { state ->
            val newFlash = (state.screenFlashAlpha - dt * 2.5f).coerceAtLeast(0f)
            val newPerfectAlpha = (state.perfectNotificationAlpha - dt * 1.5f).coerceAtLeast(0f)
            val newSpeedUpAlpha = (state.speedUpAlpha - dt * 1.2f).coerceAtLeast(0f)
            val newShakeX = state.cameraShakeX * (1f - dt * 10f).coerceIn(0f, 1f)
            val newShakeY = state.cameraShakeY * (1f - dt * 10f).coerceIn(0f, 1f)
            val newZoom = 1.0f + (state.cameraZoom - 1.0f) * (1f - dt * 8f).coerceIn(0f, 1f)
            val newTunnelGlow = 0.2f + (state.tunnelGlowAlpha - 0.2f) * (1f - dt * 3f).coerceIn(0f, 1f)

            state.copy(
                screenFlashAlpha = newFlash,
                perfectNotificationAlpha = newPerfectAlpha,
                perfectNotification = if (newPerfectAlpha <= 0f) null else state.perfectNotification,
                speedUpAlpha = newSpeedUpAlpha,
                speedUpNotification = newSpeedUpAlpha > 0f,
                cameraShakeX = newShakeX,
                cameraShakeY = newShakeY,
                cameraZoom = newZoom,
                tunnelGlowAlpha = newTunnelGlow
            )
        }
    }

    // Particle Spawners
    private fun spawnBallTrailParticle() {
        if (particles.size >= 80) return
        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        val dist = Random.nextFloat() * 12f
        particles.add(
            Particle(
                x = cos(angle) * dist,
                y = 120f + sin(angle) * dist, // Relative to ball
                vx = (Random.nextFloat() - 0.5f) * 20f,
                vy = 40f + Random.nextFloat() * 60f,
                radius = 2.5f + Random.nextFloat() * 2.5f,
                alpha = 0.8f,
                color = if (Random.nextBoolean()) Color(0xFF00F0FF) else Color(0xFFFFFFFF),
                maxLife = 0.35f
            )
        )
    }

    private fun spawnDashBurstParticles() {
        val count = if (reducedEffects) 8 else 18
        for (i in 0 until count) {
            val speed = 80f + Random.nextFloat() * 180f
            val angle = (Math.PI / 2f + (Random.nextFloat() - 0.5f) * 1.2f).toFloat() // Stream backwards
            particles.add(
                Particle(
                    x = (Random.nextFloat() - 0.5f) * 16f,
                    y = 120f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    radius = 3.5f + Random.nextFloat() * 3f,
                    alpha = 1.0f,
                    color = Color(0xFF00F0FF),
                    maxLife = 0.4f
                )
            )
        }
    }

    private fun spawnPassBurstParticles(isPerfect: Boolean) {
        val count = if (isPerfect) 28 else 16
        val color = if (isPerfect) Color(0xFF00F0FF) else Color(0xFF00E5BC)
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 120f + Random.nextFloat() * (if (isPerfect) 260f else 180f)
            particles.add(
                Particle(
                    x = 0f,
                    y = 120f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    radius = 3f + Random.nextFloat() * 4f,
                    alpha = 1.0f,
                    color = color,
                    maxLife = if (isPerfect) 0.55f else 0.4f
                )
            )
        }
    }

    private fun spawnCollisionShatterParticles() {
        val count = if (reducedEffects) 18 else 45
        val colors = listOf(Color(0xFFFF2A55), Color(0xFFFF7700), Color(0xFFFFFFFF), Color(0xFFFFD700))
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 80f + Random.nextFloat() * 320f
            particles.add(
                Particle(
                    x = 0f,
                    y = 120f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    radius = 4f + Random.nextFloat() * 5f,
                    alpha = 1.0f,
                    color = colors[Random.nextInt(colors.size)],
                    maxLife = 0.75f
                )
            )
        }
    }
}
