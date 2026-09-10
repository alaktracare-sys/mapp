package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val coverRes: Int,
    val genre: String,
    val bpm: Int,
    val lyricsJson: String,
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val qualityTag: String = "HI_RES_LOSSLESS",
    val playCount: Int = 0
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coverRes: Int,
    val isCollaborative: Boolean = false,
    val isBlend: Boolean = false,
    val blendMatchPercent: Int = 0,
    val friendName: String? = null,
    val collaboratorsCsv: String = "You",
    val trackIdsCsv: String = ""
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val trackTitle: String,
    val artist: String,
    val genre: String,
    val playedAt: Long = System.currentTimeMillis()
)
