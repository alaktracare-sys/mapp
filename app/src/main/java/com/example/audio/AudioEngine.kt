package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.AudioQuality
import com.example.model.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

class AudioEngine {

    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(180000L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    // Audio settings
    var crossfadeDurationSec: Int = 2
    var isGaplessEnabled: Boolean = true
    var isSpatialAudioEnabled: Boolean = false
    var audioQuality: AudioQuality = AudioQuality.HI_RES_LOSSLESS

    // Equalizer gains (-12dB to +12dB, converted to linear scale)
    var eqBands = floatArrayOf(0f, 0f, 0f, 0f, 0f) // 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
    var bassBoostPercent = 40
    var virtualizerPercent = 20

    private var currentTrack: Track? = null
    private var onTrackCompletedListener: (() -> Unit)? = null

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufSize, sampleRate * 2)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (_: Exception) {
            // Fallback gracefully if hardware audio fails
        }
    }

    fun setOnTrackCompletedListener(listener: () -> Unit) {
        onTrackCompletedListener = listener
    }

    fun playTrack(track: Track, startPositionMs: Long = 0L) {
        currentTrack = track
        _durationMs.value = track.durationMs
        _currentPositionMs.value = startPositionMs
        startPlaybackLoop()
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun resume() {
        if (currentTrack != null) {
            startPlaybackLoop()
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _durationMs.value)
        _currentPositionMs.value = clamped
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        _isPlaying.value = true

        playbackJob = scope.launch {
            val track = currentTrack ?: return@launch
            val bpm = track.bpm.coerceIn(70, 160)
            val beatPeriodMs = (60000.0 / bpm).toLong()

            // Musical root pitch based on track genre
            val baseFreq = when (track.genre.lowercase()) {
                "synthwave" -> 130.81 // C3
                "ambient electronic" -> 110.0 // A2
                "chillhop / r&b" -> 98.0 // G2
                "cyber edm" -> 146.83 // D3
                else -> 123.47 // B2
            }

            val bufferSize = 2048
            val audioBuffer = ShortArray(bufferSize * 2) // Stereo

            var sampleIndex = (_currentPositionMs.value * sampleRate / 1000).toLong()
            var lastUpdateMs = System.currentTimeMillis()

            while (isActive && _isPlaying.value) {
                val currentPos = (sampleIndex * 1000 / sampleRate)
                _currentPositionMs.value = currentPos

                if (currentPos >= track.durationMs) {
                    _isPlaying.value = false
                    withContext(Dispatchers.Main) {
                        onTrackCompletedListener?.invoke()
                    }
                    break
                }

                // Crossfade volume calculation
                val fadeVol = calculateFadeVolume(currentPos, track.durationMs)

                // Equalizer multipliers
                val bassGain = (1.0 + (eqBands[0] / 12.0) + (bassBoostPercent / 100.0)).coerceIn(0.2, 2.5)
                val lowMidGain = (1.0 + (eqBands[1] / 12.0)).coerceIn(0.2, 2.0)
                val midGain = (1.0 + (eqBands[2] / 12.0)).coerceIn(0.2, 2.0)
                val highMidGain = (1.0 + (eqBands[3] / 12.0)).coerceIn(0.2, 2.0)
                val trebleGain = (1.0 + (eqBands[4] / 12.0)).coerceIn(0.2, 2.0)

                val spatialWidening = if (isSpatialAudioEnabled) 1.25 else 1.0

                // Generate harmonic ambient wave samples
                for (i in 0 until bufferSize) {
                    val t = (sampleIndex + i).toDouble() / sampleRate
                    val beatTime = ((sampleIndex + i) * 1000 / sampleRate) / beatPeriodMs

                    // Melodic progression
                    val chordOffset = when ((beatTime % 16).toInt()) {
                        0, 1, 2, 3 -> 1.0 // Root
                        4, 5, 6, 7 -> 1.25 // Major 3rd
                        8, 9, 10, 11 -> 1.5 // 5th
                        else -> 1.125 // 2nd
                    }

                    // Low-frequency bass layer
                    val bassWave = sin(2.0 * Math.PI * baseFreq * t) * 0.35 * bassGain

                    // Mid-harmonic melodic layer
                    val melodyWave = (sin(2.0 * Math.PI * baseFreq * chordOffset * 2.0 * t) +
                            0.5 * sin(2.0 * Math.PI * baseFreq * chordOffset * 3.0 * t)) * 0.25 * midGain

                    // High sparkle / shimmer
                    val sparkleWave = sin(2.0 * Math.PI * baseFreq * chordOffset * 6.0 * t) * 0.1 * trebleGain

                    // Beat rhythm pulse (subtle kick & hi-hat groove)
                    val beatFraction = (t * (bpm / 60.0)) % 1.0
                    val rhythmEnvelope = kotlin.math.max(0.0, 1.0 - beatFraction * 4.0) * 0.2 * lowMidGain

                    val combinedMono = (bassWave + melodyWave + sparkleWave + rhythmEnvelope) * fadeVol * 0.65

                    // Stereo separation & spatial audio
                    val panPhase = sin(2.0 * Math.PI * 0.2 * t) * (virtualizerPercent / 100.0)
                    val leftSample = (combinedMono * (1.0 - panPhase * 0.3) * spatialWidening)
                        .coerceIn(-1.0, 1.0)
                    val rightSample = (combinedMono * (1.0 + panPhase * 0.3) * spatialWidening)
                        .coerceIn(-1.0, 1.0)

                    audioBuffer[i * 2] = (leftSample * Short.MAX_VALUE).toInt().toShort()
                    audioBuffer[i * 2 + 1] = (rightSample * Short.MAX_VALUE).toInt().toShort()
                }

                sampleIndex += bufferSize
                audioTrack?.write(audioBuffer, 0, audioBuffer.size)

                // Throttle UI position update slightly
                val now = System.currentTimeMillis()
                if (now - lastUpdateMs > 100) {
                    lastUpdateMs = now
                    delay(15)
                }
            }
        }
    }

    private fun calculateFadeVolume(currentPosMs: Long, totalDurationMs: Long): Double {
        val crossfadeMs = crossfadeDurationSec * 1000L
        if (crossfadeMs <= 0L) return 1.0

        // Fade in at start
        if (currentPosMs < crossfadeMs) {
            return (currentPosMs.toDouble() / crossfadeMs).coerceIn(0.0, 1.0)
        }

        // Fade out at end
        val remainingMs = totalDurationMs - currentPosMs
        if (remainingMs < crossfadeMs) {
            return (remainingMs.toDouble() / crossfadeMs).coerceIn(0.0, 1.0)
        }

        return 1.0
    }

    fun release() {
        playbackJob?.cancel()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
