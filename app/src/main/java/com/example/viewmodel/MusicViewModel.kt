package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.data.AppDatabase
import com.example.data.MusicRepository
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class LibraryFilter {
    ALL, PLAYLISTS, LIKED, ALBUMS, ARTISTS, DOWNLOADED
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = MusicRepository(database)
    val audioEngine = AudioEngine()

    // Library Data
    val allTracks: StateFlow<List<Track>> = repository.allTracks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val likedTracks: StateFlow<List<Track>> = repository.likedTracks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val downloadedTracks: StateFlow<List<Track>> = repository.downloadedTracks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Player State
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentPositionMs: StateFlow<Long> = audioEngine.currentPositionMs
    val durationMs: StateFlow<Long> = audioEngine.durationMs

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()
    private var queueIndex = 0

    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    // Library Filter & Search
    private val _libraryFilter = MutableStateFlow(LibraryFilter.ALL)
    val libraryFilter: StateFlow<LibraryFilter> = _libraryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Offline Mode
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    // Dark theme variant
    private val _darkThemeStyle = MutableStateFlow("OLED") // "OLED", "Midnight", "Charcoal"
    val darkThemeStyle: StateFlow<String> = _darkThemeStyle.asStateFlow()

    // Equalizer
    private val _isEqualizerEnabled = MutableStateFlow(true)
    val isEqualizerEnabled: StateFlow<Boolean> = _isEqualizerEnabled.asStateFlow()

    private val _eqPreset = MutableStateFlow("Bass Boost")
    val eqPreset: StateFlow<String> = _eqPreset.asStateFlow()

    private val _eqBands = MutableStateFlow(listOf(4.0f, 2.0f, 0.0f, 1.5f, 3.0f))
    val eqBands: StateFlow<List<Float>> = _eqBands.asStateFlow()

    private val _bassBoost = MutableStateFlow(60)
    val bassBoost: StateFlow<Int> = _bassBoost.asStateFlow()

    private val _virtualizer = MutableStateFlow(35)
    val virtualizer: StateFlow<Int> = _virtualizer.asStateFlow()

    // Hi-Fi & Latency Settings
    private val _audioQuality = MutableStateFlow(AudioQuality.HI_RES_LOSSLESS)
    val audioQuality: StateFlow<AudioQuality> = _audioQuality.asStateFlow()

    private val _isSpatialAudio = MutableStateFlow(true)
    val isSpatialAudio: StateFlow<Boolean> = _isSpatialAudio.asStateFlow()

    private val _crossfadeSec = MutableStateFlow(3)
    val crossfadeSec: StateFlow<Int> = _crossfadeSec.asStateFlow()

    private val _isGapless = MutableStateFlow(true)
    val isGapless: StateFlow<Boolean> = _isGapless.asStateFlow()

    // Live Jam Session
    private val _isJamActive = MutableStateFlow(false)
    val isJamActive: StateFlow<Boolean> = _isJamActive.asStateFlow()

    private val _jamRoomCode = MutableStateFlow("ALAKTRA-882")
    val jamRoomCode: StateFlow<String> = _jamRoomCode.asStateFlow()

    private val _jamParticipants = MutableStateFlow<List<JamParticipant>>(
        listOf(
            JamParticipant("p1", "You (Host)", 0xFF1DB954, isHost = true),
            JamParticipant("p2", "Elena S.", 0xFF8A2BE2),
            JamParticipant("p3", "Marcus K.", 0xFF00F0FF)
        )
    )
    val jamParticipants: StateFlow<List<JamParticipant>> = _jamParticipants.asStateFlow()

    private val _jamReactions = MutableStateFlow<List<JamReaction>>(emptyList())
    val jamReactions: StateFlow<List<JamReaction>> = _jamReactions.asStateFlow()

    // Cross-platform Devices
    private val _devices = MutableStateFlow(
        listOf(
            SyncDevice("d1", "Pixel 9 Pro (This device)", "Phone", isCurrent = true, isConnected = true),
            SyncDevice("d2", "MacBook Pro M3 Max", "Laptop", isCurrent = false, isConnected = true),
            SyncDevice("d3", "Living Room Hi-Fi Amp", "Speaker", isCurrent = false, isConnected = true),
            SyncDevice("d4", "iPad Pro Studio", "Tablet", isCurrent = false, isConnected = false)
        )
    )
    val devices: StateFlow<List<SyncDevice>> = _devices.asStateFlow()

    private val _syncStatus = MutableStateFlow("Synced with Alaktra Cloud")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    // Voice Control State
    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening.asStateFlow()

    private val _voiceFeedback = MutableStateFlow<String?>(null)
    val voiceFeedback: StateFlow<String?> = _voiceFeedback.asStateFlow()

    // Social Sharing
    private val _shareTrack = MutableStateFlow<Track?>(null)
    val shareTrack: StateFlow<Track?> = _shareTrack.asStateFlow()

    // Selected Playlist Detail
    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    // Realtime synced lyrics derived state
    val currentLyricIndex: StateFlow<Int> = combine(currentTrack, currentPositionMs) { track, pos ->
        if (track == null || track.lyrics.isEmpty()) -1
        else {
            var activeIdx = 0
            for (i in track.lyrics.indices) {
                if (pos >= track.lyrics[i].timestampMs) {
                    activeIdx = i
                } else break
            }
            activeIdx
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    // User Analytics / Wrapped
    val userStats = UserStats(
        totalMinutes = 3420,
        totalPlays = 894,
        topGenre = "Synthwave & Ambient",
        topArtist = "Aura & The Synth",
        audioAuraTitle = "Electric Euphoria",
        audioAuraDescription = "Driven by deep low-end pulses and luminous retro-futuristic chords.",
        genreBreakdown = mapOf(
            "Synthwave" to 0.42f,
            "Ambient Electronic" to 0.28f,
            "Chillhop / R&B" to 0.16f,
            "Cyber EDM" to 0.14f
        ),
        peakListeningHour = "11:30 PM (Late Night Focus)"
    )

    init {
        // Automatically start with first track in queue
        viewModelScope.launch {
            allTracks.collect { tracks ->
                if (tracks.isNotEmpty() && _currentTrack.value == null) {
                    _queue.value = tracks
                    _currentTrack.value = tracks[0]
                }
            }
        }

        audioEngine.setOnTrackCompletedListener {
            handleTrackCompletion()
        }
        applyAudioEngineParams()
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                audioEngine.seekTo(0)
                audioEngine.resume()
            }
            RepeatMode.ALL -> {
                nextTrack()
            }
            RepeatMode.OFF -> {
                if (queueIndex < _queue.value.size - 1) {
                    nextTrack()
                } else {
                    audioEngine.pause()
                }
            }
        }
    }

    private fun applyAudioEngineParams() {
        audioEngine.crossfadeDurationSec = _crossfadeSec.value
        audioEngine.isGaplessEnabled = _isGapless.value
        audioEngine.isSpatialAudioEnabled = _isSpatialAudio.value
        audioEngine.audioQuality = _audioQuality.value
        audioEngine.bassBoostPercent = _bassBoost.value
        audioEngine.virtualizerPercent = _virtualizer.value
        val bands = _eqBands.value
        audioEngine.eqBands = floatArrayOf(bands[0], bands[1], bands[2], bands[3], bands[4])
    }

    // Playback Controls
    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        if (newQueue != null) {
            _queue.value = newQueue
            queueIndex = newQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        } else {
            val idx = _queue.value.indexOfFirst { it.id == track.id }
            if (idx >= 0) queueIndex = idx
        }
        _currentTrack.value = track
        audioEngine.playTrack(track)
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordTrackPlayed(track)
        }
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            audioEngine.pause()
        } else {
            if (_currentTrack.value == null && _queue.value.isNotEmpty()) {
                playTrack(_queue.value[0])
            } else {
                audioEngine.resume()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun seekRelative(seconds: Int) {
        val newPos = currentPositionMs.value + (seconds * 1000L)
        audioEngine.seekTo(newPos)
    }

    fun nextTrack() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_shuffleMode.value != ShuffleMode.OFF) {
            if (_shuffleMode.value == ShuffleMode.SMART) {
                // Smart shuffle: Pick track matching genre/vibe or BPM
                val current = _currentTrack.value
                val smartPick = q.filter { it.id != current?.id }
                    .maxByOrNull {
                        var score = 0
                        if (it.genre == current?.genre) score += 50
                        if (kotlin.math.abs(it.bpm - (current?.bpm ?: 100)) <= 15) score += 30
                        score + Random.nextInt(20)
                    } ?: q.random()
                queueIndex = q.indexOf(smartPick).coerceAtLeast(0)
            } else {
                queueIndex = Random.nextInt(q.size)
            }
        } else {
            queueIndex = (queueIndex + 1) % q.size
        }
        val next = q[queueIndex]
        playTrack(next)
    }

    fun previousTrack() {
        if (currentPositionMs.value > 3000L) {
            seekTo(0)
            return
        }
        val q = _queue.value
        if (q.isEmpty()) return
        queueIndex = if (queueIndex - 1 < 0) q.size - 1 else queueIndex - 1
        playTrack(q[queueIndex])
    }

    fun toggleShuffle() {
        _shuffleMode.value = when (_shuffleMode.value) {
            ShuffleMode.OFF -> ShuffleMode.NORMAL
            ShuffleMode.NORMAL -> ShuffleMode.SMART
            ShuffleMode.SMART -> ShuffleMode.OFF
        }
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    // Likes & Downloads
    fun toggleLikeCurrentTrack() {
        val track = _currentTrack.value ?: return
        viewModelScope.launch {
            repository.toggleLiked(track.id, track.isLiked)
            _currentTrack.value = track.copy(isLiked = !track.isLiked)
        }
    }

    fun toggleTrackLike(track: Track) {
        viewModelScope.launch {
            repository.toggleLiked(track.id, track.isLiked)
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = track.copy(isLiked = !track.isLiked)
            }
        }
    }

    fun toggleTrackDownload(track: Track) {
        viewModelScope.launch {
            repository.toggleDownloaded(track.id, track.isDownloaded)
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = track.copy(isDownloaded = !track.isDownloaded)
            }
        }
    }

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }

    fun setLibraryFilter(filter: LibraryFilter) {
        _libraryFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Equalizer & Presets
    fun setEqualizerEnabled(enabled: Boolean) {
        _isEqualizerEnabled.value = enabled
        applyAudioEngineParams()
    }

    fun setEqPreset(presetName: String) {
        _eqPreset.value = presetName
        when (presetName) {
            "Bass Boost" -> {
                _eqBands.value = listOf(7.0f, 4.0f, 0.0f, 1.0f, 2.0f)
                _bassBoost.value = 80
            }
            "Electronic" -> {
                _eqBands.value = listOf(5.5f, 2.5f, -1.0f, 3.0f, 5.0f)
                _bassBoost.value = 65
            }
            "Acoustic" -> {
                _eqBands.value = listOf(3.0f, 1.0f, 3.5f, 2.0f, 4.0f)
                _bassBoost.value = 30
            }
            "Rock" -> {
                _eqBands.value = listOf(4.5f, 2.0f, -1.5f, 2.5f, 4.5f)
                _bassBoost.value = 50
            }
            "Hip-Hop" -> {
                _eqBands.value = listOf(8.0f, 5.0f, 0.5f, 2.0f, 3.5f)
                _bassBoost.value = 85
            }
            "Vocal Booster" -> {
                _eqBands.value = listOf(-2.0f, 1.0f, 5.5f, 4.0f, 1.0f)
                _bassBoost.value = 20
            }
            else -> { // Flat
                _eqBands.value = listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                _bassBoost.value = 40
            }
        }
        applyAudioEngineParams()
    }

    fun setBandGain(index: Int, gainDb: Float) {
        val current = _eqBands.value.toMutableList()
        if (index in current.indices) {
            current[index] = gainDb
            _eqBands.value = current
            _eqPreset.value = "Custom"
            applyAudioEngineParams()
        }
    }

    fun setBassBoost(level: Int) {
        _bassBoost.value = level
        applyAudioEngineParams()
    }

    fun setVirtualizer(level: Int) {
        _virtualizer.value = level
        applyAudioEngineParams()
    }

    // Hi-Fi and Transitions
    fun setAudioQuality(quality: AudioQuality) {
        _audioQuality.value = quality
        applyAudioEngineParams()
    }

    fun setSpatialAudio(enabled: Boolean) {
        _isSpatialAudio.value = enabled
        applyAudioEngineParams()
    }

    fun setCrossfadeSec(seconds: Int) {
        _crossfadeSec.value = seconds
        applyAudioEngineParams()
    }

    fun setGapless(enabled: Boolean) {
        _isGapless.value = enabled
        applyAudioEngineParams()
    }

    // Blend Feature
    fun createBlendWithFriend(friendName: String, matchPercent: Int) {
        viewModelScope.launch {
            val newId = repository.createBlendPlaylist(friendName, matchPercent)
            _syncStatus.value = "Blend created with $friendName ($matchPercent% match)"
        }
    }

    // Collaborative Playlist
    fun createCollaborativePlaylist(title: String, description: String) {
        viewModelScope.launch {
            repository.createPlaylist(
                title = title,
                description = description,
                isCollab = true,
                coverRes = com.example.R.drawable.album_neon_nights
            )
        }
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    // Jam Session
    fun toggleJam() {
        _isJamActive.value = !_isJamActive.value
        if (_isJamActive.value) {
            _syncStatus.value = "Live Jam active on ${_jamRoomCode.value}"
        }
    }

    fun sendJamReaction(emoji: String) {
        val reaction = JamReaction(
            id = "rx_${System.currentTimeMillis()}",
            emoji = emoji,
            senderName = "You"
        )
        _jamReactions.value = _jamReactions.value + reaction
    }

    fun addTrackToJamQueue(track: Track) {
        _queue.value = _queue.value + track
        sendJamReaction("🎵")
    }

    // Cross-Platform Device Sync
    fun switchDevice(deviceId: String) {
        val updated = _devices.value.map { dev ->
            dev.copy(isCurrent = dev.id == deviceId)
        }
        _devices.value = updated
        val current = updated.firstOrNull { it.isCurrent }
        _syncStatus.value = "Playback transferred to ${current?.name}"
    }

    // Social Sharing
    fun openShareDialog(track: Track) {
        _shareTrack.value = track
    }

    fun closeShareDialog() {
        _shareTrack.value = null
    }

    // Voice Control Integration
    fun setVoiceListening(isListening: Boolean) {
        _isVoiceListening.value = isListening
    }

    fun processVoiceCommand(rawQuery: String) {
        val query = rawQuery.lowercase().trim()
        when {
            query.contains("play") && (query.contains("neon") || query.contains("midnight")) -> {
                val match = allTracks.value.firstOrNull { it.title.contains("Midnight", ignoreCase = true) }
                if (match != null) playTrack(match)
                _voiceFeedback.value = "Playing '${match?.title}'"
            }
            query.contains("play") && query.contains("celestial") -> {
                val match = allTracks.value.firstOrNull { it.title.contains("Celestial", ignoreCase = true) }
                if (match != null) playTrack(match)
                _voiceFeedback.value = "Playing '${match?.title}'"
            }
            query.contains("pause") || query.contains("stop") -> {
                audioEngine.pause()
                _voiceFeedback.value = "Playback paused"
            }
            query.contains("resume") || query == "play" -> {
                audioEngine.resume()
                _voiceFeedback.value = "Playback resumed"
            }
            query.contains("next") || query.contains("skip") -> {
                nextTrack()
                _voiceFeedback.value = "Skipped to next track"
            }
            query.contains("previous") || query.contains("back") -> {
                previousTrack()
                _voiceFeedback.value = "Playing previous track"
            }
            query.contains("smart shuffle") -> {
                _shuffleMode.value = ShuffleMode.SMART
                _voiceFeedback.value = "Smart Shuffle enabled (AI vibe match)"
            }
            query.contains("shuffle") -> {
                toggleShuffle()
                _voiceFeedback.value = "Shuffle set to ${_shuffleMode.value.name}"
            }
            query.contains("bass boost") -> {
                setEqPreset("Bass Boost")
                _voiceFeedback.value = "Bass Boost equalizer preset applied"
            }
            query.contains("offline") -> {
                toggleOfflineMode()
                _voiceFeedback.value = "Offline mode ${if (_isOfflineMode.value) "enabled" else "disabled"}"
            }
            query.contains("loop") || query.contains("repeat") -> {
                toggleRepeat()
                _voiceFeedback.value = "Repeat mode: ${_repeatMode.value.name}"
            }
            else -> {
                // Fuzzy search title
                val match = allTracks.value.firstOrNull {
                    it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
                }
                if (match != null) {
                    playTrack(match)
                    _voiceFeedback.value = "Playing '${match.title}' by ${match.artist}"
                } else {
                    _voiceFeedback.value = "Command '$rawQuery' recognized"
                }
            }
        }
    }

    fun clearVoiceFeedback() {
        _voiceFeedback.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
