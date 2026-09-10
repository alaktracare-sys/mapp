package com.example.model

enum class ShuffleMode {
    OFF,
    NORMAL,
    SMART // AI-style acoustic & vibe matching recommendation shuffle
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class AudioQuality(val title: String, val badge: String, val bitrate: String, val sampleRate: String) {
    STANDARD("Standard (AAC)", "AAC", "160 kbps", "44.1 kHz"),
    HIGH("High Quality (AAC)", "320k", "320 kbps", "48.0 kHz"),
    LOSSLESS("Lossless (ALAC)", "LOSSLESS", "1411 kbps", "24-bit / 48 kHz"),
    HI_RES_LOSSLESS("Hi-Res Lossless (FLAC)", "HI-RES", "2304 kbps", "24-bit / 192 kHz")
}

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val coverRes: Int,
    val genre: String,
    val bpm: Int,
    val lyrics: List<LyricLine>,
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val quality: AudioQuality = AudioQuality.HI_RES_LOSSLESS,
    val addedBy: String = "You"
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val coverRes: Int,
    val isCollaborative: Boolean = false,
    val isBlend: Boolean = false,
    val blendMatchPercent: Int = 0,
    val friendName: String? = null,
    val collaborators: List<String> = listOf("You"),
    val tracks: List<Track> = emptyList()
)

data class JamParticipant(
    val id: String,
    val name: String,
    val avatarColorHex: Long,
    val isHost: Boolean = false
)

data class JamReaction(
    val id: String,
    val emoji: String,
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SyncDevice(
    val id: String,
    val name: String,
    val type: String, // Phone, Laptop, Speaker, Tablet
    val isCurrent: Boolean = false,
    val isConnected: Boolean = true
)

data class UserStats(
    val totalMinutes: Int,
    val totalPlays: Int,
    val topGenre: String,
    val topArtist: String,
    val audioAuraTitle: String,
    val audioAuraDescription: String,
    val genreBreakdown: Map<String, Float>,
    val peakListeningHour: String
)
