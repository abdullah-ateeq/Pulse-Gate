package com.example.pulsegate.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class SoundEffect {
    DASH,
    GATE_PASS,
    PERFECT_PASS,
    COLLISION,
    BUTTON_CLICK,
    MILESTONE
}

class SoundManager {

    private val sampleRate = 44100
    private val tracks = mutableMapOf<SoundEffect, AudioTrack>()
    private val scope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true

    init {
        scope.launch {
            pregenerateSounds()
        }
    }

    private fun pregenerateSounds() {
        SoundEffect.values().forEach { effect ->
            val pcmData = generatePcmForEffect(effect)
            val audioTrack = createAudioTrack(pcmData)
            synchronized(tracks) {
                tracks[effect] = audioTrack
            }
        }
    }

    private fun createAudioTrack(pcmData: ShortArray): AudioTrack {
        val bufferSize = pcmData.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcmData, 0, pcmData.size)
        return track
    }

    private fun generatePcmForEffect(effect: SoundEffect): ShortArray {
        return when (effect) {
            SoundEffect.DASH -> generateDashSound()
            SoundEffect.GATE_PASS -> generateGatePassSound()
            SoundEffect.PERFECT_PASS -> generatePerfectPassSound()
            SoundEffect.COLLISION -> generateCollisionSound()
            SoundEffect.BUTTON_CLICK -> generateButtonClickSound()
            SoundEffect.MILESTONE -> generateMilestoneSound()
        }
    }

    private fun generateDashSound(): ShortArray {
        val durationMs = 120
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val freq = 800.0 - 550.0 * (t * t)
            val envelope = (1.0 - t) * (1.0 - t)
            val sample = sin(2.0 * PI * freq * (i.toDouble() / sampleRate)) * envelope * 0.8
            samples[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun generateGatePassSound(): ShortArray {
        val durationMs = 160
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val envelope = Math.exp(-4.5 * t)
            val s1 = sin(2.0 * PI * 880.0 * (i.toDouble() / sampleRate))
            val s2 = sin(2.0 * PI * 1320.0 * (i.toDouble() / sampleRate)) * 0.4
            val sample = (s1 + s2) * envelope * 0.65
            samples[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun generatePerfectPassSound(): ShortArray {
        val durationMs = 280
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val envelope = Math.exp(-3.0 * t)
            val s1 = sin(2.0 * PI * 1046.5 * (i.toDouble() / sampleRate)) // C6
            val s2 = sin(2.0 * PI * 1318.5 * (i.toDouble() / sampleRate)) * 0.7 // E6
            val s3 = sin(2.0 * PI * 1567.98 * (i.toDouble() / sampleRate)) * 0.5 // G6
            val sample = ((s1 + s2 + s3) / 2.2) * envelope * 0.8
            samples[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun generateCollisionSound(): ShortArray {
        val durationMs = 380
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val freq = 180.0 * Math.exp(-4.0 * t) + 40.0
            val noise = (Math.random() * 2.0 - 1.0) * Math.exp(-6.0 * t) * 0.3
            val tone = sin(2.0 * PI * freq * (i.toDouble() / sampleRate))
            val envelope = Math.exp(-3.5 * t)
            val sample = (tone * 0.7 + noise) * envelope * 0.95
            samples[i] = (sample * Short.MAX_VALUE).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
        }
        return samples
    }

    private fun generateButtonClickSound(): ShortArray {
        val durationMs = 45
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val envelope = Math.exp(-8.0 * t)
            val sample = sin(2.0 * PI * 920.0 * (i.toDouble() / sampleRate)) * envelope * 0.5
            samples[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private fun generateMilestoneSound(): ShortArray {
        val durationMs = 320
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        val noteLength = numSamples / 3
        val freqs = listOf(587.33, 739.99, 880.0) // D5, F#5, A5
        for (i in 0 until numSamples) {
            val noteIndex = (i / noteLength).coerceAtMost(2)
            val noteSample = i % noteLength
            val noteT = noteSample.toDouble() / noteLength
            val freq = freqs[noteIndex]
            val envelope = Math.exp(-2.5 * noteT)
            val sample = sin(2.0 * PI * freq * (i.toDouble() / sampleRate)) * envelope * 0.7
            samples[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    fun play(effect: SoundEffect) {
        if (!isSoundEnabled) return
        scope.launch {
            synchronized(tracks) {
                tracks[effect]?.let { track ->
                    try {
                        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            track.stop()
                        }
                        track.reloadStaticData()
                        track.play()
                    } catch (e: Exception) {
                        // ignore audio playback errors gracefully
                    }
                }
            }
        }
    }

    fun release() {
        synchronized(tracks) {
            tracks.values.forEach { track ->
                try {
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    // ignore
                }
            }
            tracks.clear()
        }
    }
}
