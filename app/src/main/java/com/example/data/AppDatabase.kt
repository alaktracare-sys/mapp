package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TrackEntity::class, PlaylistEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulse_music_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch(Dispatchers.IO) {
                                populateInitialData(getDatabase(context, scope))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val trackEntities = DefaultMusicData.sampleTracks.map { track ->
                TrackEntity(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    durationMs = track.durationMs,
                    coverRes = track.coverRes,
                    genre = track.genre,
                    bpm = track.bpm,
                    lyricsJson = DefaultMusicData.serializeLyrics(track.lyrics),
                    isLiked = track.isLiked,
                    isDownloaded = track.isDownloaded,
                    qualityTag = track.quality.name,
                    playCount = 12
                )
            }
            database.trackDao().insertAll(trackEntities)

            // Initial Playlists: Normal, Collaborative, and Blend
            val playlists = listOf(
                PlaylistEntity(
                    id = "playlist_favorites",
                    title = "Liked Songs",
                    description = "Your favorite tracks, always in sync and ready offline",
                    coverRes = com.example.R.drawable.ic_launcher_art,
                    isCollaborative = false,
                    isBlend = false,
                    collaboratorsCsv = "You",
                    trackIdsCsv = "track_1,track_2,track_4"
                ),
                PlaylistEntity(
                    id = "playlist_blend_alex",
                    title = "Alex + You Blend",
                    description = "94% Taste Match • Blending Synthwave & Chillhop vibes",
                    coverRes = com.example.R.drawable.album_neon_nights,
                    isCollaborative = true,
                    isBlend = true,
                    blendMatchPercent = 94,
                    friendName = "Alex",
                    collaboratorsCsv = "You,Alex",
                    trackIdsCsv = "track_1,track_3,track_5"
                ),
                PlaylistEntity(
                    id = "playlist_collab_jam",
                    title = "Late Night Studio Jam",
                    description = "Live collaborative playlist with real-time contributions",
                    coverRes = com.example.R.drawable.album_celestial_echo,
                    isCollaborative = true,
                    isBlend = false,
                    collaboratorsCsv = "You,Elena,Marcus",
                    trackIdsCsv = "track_2,track_4,track_1"
                ),
                PlaylistEntity(
                    id = "playlist_deep_focus",
                    title = "Deep Focus & Hi-Res Ambient",
                    description = "24-bit 192kHz master recordings for uninterrupted flow",
                    coverRes = com.example.R.drawable.album_celestial_echo,
                    isCollaborative = false,
                    isBlend = false,
                    collaboratorsCsv = "You",
                    trackIdsCsv = "track_2,track_3,track_5"
                )
            )
            database.playlistDao().insertAll(playlists)

            // Initial History records
            val historyDao = database.historyDao()
            historyDao.recordHistory(HistoryEntity(trackId = "track_1", trackTitle = "Midnight Drive (Neon Horizons)", artist = "Aura & The Synth", genre = "Synthwave"))
            historyDao.recordHistory(HistoryEntity(trackId = "track_2", trackTitle = "Celestial Echoes", artist = "Nova Stellar", genre = "Ambient Electronic"))
            historyDao.recordHistory(HistoryEntity(trackId = "track_3", trackTitle = "Velvet Pulse", artist = "Kroma Beats", genre = "Chillhop / R&B"))
        }
    }
}
