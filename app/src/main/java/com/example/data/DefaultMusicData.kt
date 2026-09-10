package com.example.data

import com.example.R
import com.example.model.AudioQuality
import com.example.model.LyricLine
import com.example.model.Track

object DefaultMusicData {
    val sampleTracks: List<Track> = listOf(
        Track(
            id = "track_1",
            title = "Midnight Drive (Neon Horizons)",
            artist = "Aura & The Synth",
            album = "Neon Nights EP",
            durationMs = 210000L,
            coverRes = R.drawable.album_neon_nights,
            genre = "Synthwave",
            bpm = 124,
            lyrics = listOf(
                LyricLine(0L, "♪ Instrumental Synth Intro ♪"),
                LyricLine(8000L, "City lights reflect across the dash"),
                LyricLine(16000L, "Fading memories in an endless flash"),
                LyricLine(24000L, "Miles of asphalt, glowing in the rain"),
                LyricLine(32000L, "Chasing frequencies to wash the pain"),
                LyricLine(42000L, "Take me where the midnight river flows"),
                LyricLine(52000L, "In the electric glow that nobody knows"),
                LyricLine(64000L, "Neon horizons, take us away tonight"),
                LyricLine(76000L, "Underneath the cybernetic purple light"),
                LyricLine(92000L, "♪ Melodic Solo ♪"),
                LyricLine(110000L, "Accelerate beyond the digital sound"),
                LyricLine(124000L, "Feel our heartbeat off the ground"),
                LyricLine(140000L, "Neon horizons, we will never fade"),
                LyricLine(156000L, "In this timeless rhythm we have made"),
                LyricLine(180000L, "♪ Outro Fadeout ♪")
            ),
            isLiked = true,
            isDownloaded = true,
            quality = AudioQuality.HI_RES_LOSSLESS
        ),
        Track(
            id = "track_2",
            title = "Celestial Echoes",
            artist = "Nova Stellar",
            album = "Cosmic Reverie",
            durationMs = 195000L,
            coverRes = R.drawable.album_celestial_echo,
            genre = "Ambient Electronic",
            bpm = 110,
            lyrics = listOf(
                LyricLine(0L, "♪ Cosmic Atmosphere Waves ♪"),
                LyricLine(10000L, "Drifting stars whispering your name"),
                LyricLine(22000L, "In the deep void, nothing stays the same"),
                LyricLine(35000L, "Nebula dust swirling around our eyes"),
                LyricLine(48000L, "A million light-years in crystal skies"),
                LyricLine(62000L, "Listen close to the celestial sound"),
                LyricLine(74000L, "Gravity lost, no longer bound"),
                LyricLine(88000L, "Echoes floating in the quiet space"),
                LyricLine(102000L, "Timeless harmony, timeless grace"),
                LyricLine(120000L, "♪ Ethereal Synth Sweep ♪"),
                LyricLine(140000L, "We shine like supernovas in the night"),
                LyricLine(158000L, "Guiding every traveler with our light"),
                LyricLine(175000L, "♪ Stellar Ambient Dissolve ♪")
            ),
            isLiked = true,
            isDownloaded = true,
            quality = AudioQuality.HI_RES_LOSSLESS
        ),
        Track(
            id = "track_3",
            title = "Velvet Pulse",
            artist = "Kroma Beats",
            album = "Urban Odyssey",
            durationMs = 178000L,
            coverRes = R.drawable.ic_launcher_art,
            genre = "Chillhop / R&B",
            bpm = 92,
            lyrics = listOf(
                LyricLine(0L, "♪ Smooth Rhodes & Lo-Fi Beat ♪"),
                LyricLine(7000L, "Coffee steaming on the windowsill"),
                LyricLine(15000L, "Time slows down, the world stands still"),
                LyricLine(25000L, "Catch the groove, let the tension fall"),
                LyricLine(35000L, "Smooth basslines echoing down the hall"),
                LyricLine(46000L, "Velvet warmth running through the vein"),
                LyricLine(58000L, "Sunlight breaking through the gentle rain"),
                LyricLine(70000L, "Just breathe in, let the record spin"),
                LyricLine(82000L, "This is where the peaceful days begin"),
                LyricLine(100000L, "♪ Jazzy Saxophone Riff ♪"),
                LyricLine(120000L, "Every second wrapped in gold"),
                LyricLine(135000L, "The sweetest story ever told"),
                LyricLine(155000L, "Velvet pulse... keep playing on")
            ),
            isLiked = false,
            isDownloaded = false,
            quality = AudioQuality.LOSSLESS
        ),
        Track(
            id = "track_4",
            title = "Hyperdrive Odyssey",
            artist = "Vortex 99",
            album = "Cyberpunk Run",
            durationMs = 225000L,
            coverRes = R.drawable.album_neon_nights,
            genre = "Cyber EDM",
            bpm = 138,
            lyrics = listOf(
                LyricLine(0L, "♪ Bass Drop Charging ♪"),
                LyricLine(12000L, "System online, ignition armed"),
                LyricLine(20000L, "Through cyber barriers unharmed"),
                LyricLine(30000L, "Speed of sound, speed of light"),
                LyricLine(40000L, "Breaking boundaries through the night!"),
                LyricLine(50000L, "♪ Massive Drop & Synth Arpeggios ♪"),
                LyricLine(75000L, "Hold on tight to the sonic beam"),
                LyricLine(88000L, "Living inside a neon dream"),
                LyricLine(105000L, "Hyperdrive! We don't slow down"),
                LyricLine(120000L, "Electric pulse across the town"),
                LyricLine(140000L, "♪ High-Energy Climax ♪"),
                LyricLine(180000L, "Terminal reached. Mission complete.")
            ),
            isLiked = true,
            isDownloaded = false,
            quality = AudioQuality.HI_RES_LOSSLESS
        ),
        Track(
            id = "track_5",
            title = "Solaris Sunset",
            artist = "Mira Luna",
            album = "Acoustic Tides",
            durationMs = 185000L,
            coverRes = R.drawable.album_celestial_echo,
            genre = "Indie Dream Pop",
            bpm = 104,
            lyrics = listOf(
                LyricLine(0L, "♪ Acoustic Guitar Picking ♪"),
                LyricLine(9000L, "Golden rays melting in the sea"),
                LyricLine(18000L, "You whispered softly next to me"),
                LyricLine(28000L, "Tides roll in, washing prints away"),
                LyricLine(38000L, "Leaving behind the warmest yesterday"),
                LyricLine(50000L, "Solaris, take my hand and dance"),
                LyricLine(62000L, "Give twilight sky another chance"),
                LyricLine(75000L, "Orange, pink, and shades of violet hue"),
                LyricLine(88000L, "Everything feels brand new with you"),
                LyricLine(110000L, "♪ Melodic Whistle & Chimes ♪"),
                LyricLine(135000L, "When the stars awake to guide our path"),
                LyricLine(150000L, "We'll remember this golden aftermath"),
                LyricLine(170000L, "♪ Gentle Acoustic Outro ♪")
            ),
            isLiked = false,
            isDownloaded = true,
            quality = AudioQuality.LOSSLESS
        )
    )

    fun serializeLyrics(lyrics: List<LyricLine>): String {
        return lyrics.joinToString(separator = "|||") { "${it.timestampMs}:::${it.text}" }
    }

    fun deserializeLyrics(raw: String): List<LyricLine> {
        if (raw.isBlank()) return emptyList()
        return raw.split("|||").mapNotNull { entry ->
            val parts = entry.split(":::", limit = 2)
            if (parts.size == 2) {
                LyricLine(parts[0].toLongOrNull() ?: 0L, parts[1])
            } else null
        }
    }
}
