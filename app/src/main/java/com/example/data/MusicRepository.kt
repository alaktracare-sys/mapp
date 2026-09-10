package com.example.data

import com.example.model.AudioQuality
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(private val database: AppDatabase) {

    val allTracks: Flow<List<Track>> = database.trackDao().getAllTracks().map { entities ->
        entities.map { entityToTrack(it) }
    }

    val downloadedTracks: Flow<List<Track>> = database.trackDao().getDownloadedTracks().map { entities ->
        entities.map { entityToTrack(it) }
    }

    val likedTracks: Flow<List<Track>> = database.trackDao().getLikedTracks().map { entities ->
        entities.map { entityToTrack(it) }
    }

    val allPlaylists: Flow<List<Playlist>> = database.playlistDao().getAllPlaylists().map { entities ->
        entities.map { entity ->
            Playlist(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                coverRes = entity.coverRes,
                isCollaborative = entity.isCollaborative,
                isBlend = entity.isBlend,
                blendMatchPercent = entity.blendMatchPercent,
                friendName = entity.friendName,
                collaborators = entity.collaboratorsCsv.split(",").filter { it.isNotBlank() },
                tracks = emptyList() // Populated dynamically or when selected
            )
        }
    }

    suspend fun toggleLiked(trackId: String, currentLiked: Boolean) {
        database.trackDao().updateLiked(trackId, !currentLiked)
    }

    suspend fun toggleDownloaded(trackId: String, currentDownloaded: Boolean) {
        database.trackDao().updateDownloaded(trackId, !currentDownloaded)
    }

    suspend fun recordTrackPlayed(track: Track) {
        database.trackDao().incrementPlayCount(track.id)
        database.historyDao().recordHistory(
            HistoryEntity(
                trackId = track.id,
                trackTitle = track.title,
                artist = track.artist,
                genre = track.genre
            )
        )
    }

    suspend fun createPlaylist(title: String, description: String, isCollab: Boolean, coverRes: Int): String {
        val id = "pl_${System.currentTimeMillis()}"
        val playlist = PlaylistEntity(
            id = id,
            title = title,
            description = description,
            coverRes = coverRes,
            isCollaborative = isCollab,
            isBlend = false,
            collaboratorsCsv = if (isCollab) "You,Alex" else "You",
            trackIdsCsv = "track_1,track_2"
        )
        database.playlistDao().insertPlaylist(playlist)
        return id
    }

    suspend fun createBlendPlaylist(friendName: String, matchPercent: Int): String {
        val id = "blend_${System.currentTimeMillis()}"
        val playlist = PlaylistEntity(
            id = id,
            title = "$friendName + You Blend",
            description = "$matchPercent% Taste Match • Auto-curated collaborative mix",
            coverRes = com.example.R.drawable.album_neon_nights,
            isCollaborative = true,
            isBlend = true,
            blendMatchPercent = matchPercent,
            friendName = friendName,
            collaboratorsCsv = "You,$friendName",
            trackIdsCsv = "track_1,track_3,track_4,track_5"
        )
        database.playlistDao().insertPlaylist(playlist)
        return id
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        val playlist = database.playlistDao().getPlaylistById(playlistId) ?: return
        val currentIds = playlist.trackIdsCsv.split(",").filter { it.isNotBlank() }.toMutableList()
        if (!currentIds.contains(trackId)) {
            currentIds.add(trackId)
            database.playlistDao().updatePlaylist(
                playlist.copy(trackIdsCsv = currentIds.joinToString(","))
            )
        }
    }

    suspend fun getPlaylistTrackIds(playlistId: String): List<String> {
        val playlist = database.playlistDao().getPlaylistById(playlistId) ?: return emptyList()
        return playlist.trackIdsCsv.split(",").filter { it.isNotBlank() }
    }

    private fun entityToTrack(entity: TrackEntity): Track {
        val quality = try {
            AudioQuality.valueOf(entity.qualityTag)
        } catch (_: Exception) {
            AudioQuality.HI_RES_LOSSLESS
        }
        return Track(
            id = entity.id,
            title = entity.title,
            artist = entity.artist,
            album = entity.album,
            durationMs = entity.durationMs,
            coverRes = entity.coverRes,
            genre = entity.genre,
            bpm = entity.bpm,
            lyrics = DefaultMusicData.deserializeLyrics(entity.lyricsJson),
            isLiked = entity.isLiked,
            isDownloaded = entity.isDownloaded,
            quality = quality
        )
    }
}
