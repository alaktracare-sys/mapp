package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.theme.*
import com.example.viewmodel.LibraryFilter
import com.example.viewmodel.MusicViewModel

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onOpenBlend: (Playlist) -> Unit,
    onOpenCreateBlend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTracks by viewModel.allTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val likedTracks by viewModel.likedTracks.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val filter by viewModel.libraryFilter.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistTitle by remember { mutableStateOf("") }
    var newPlaylistDescription by remember { mutableStateOf("") }
    var isNewCollab by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        Pair(LibraryFilter.ALL, "All"),
        Pair(LibraryFilter.PLAYLISTS, "Playlists"),
        Pair(LibraryFilter.LIKED, "Liked Songs (${likedTracks.size})"),
        Pair(LibraryFilter.DOWNLOADED, "Downloaded (${downloadedTracks.size})"),
        Pair(LibraryFilter.ALBUMS, "Albums"),
        Pair(LibraryFilter.ARTISTS, "Artists")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .testTag("library_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header: "Your Library", Add button, Sync status
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "YOUR LIBRARY",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.8.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(Icons.Filled.Sync, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = syncStatus,
                                fontSize = 11.sp,
                                color = NeonGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = onOpenCreateBlend,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .testTag("library_create_blend_button")
                        ) {
                            Icon(Icons.Filled.GroupAdd, contentDescription = "Create Blend", tint = ElectricPurple, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { showCreatePlaylistDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .testTag("library_add_playlist_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Create Playlist", tint = NeonGreen, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // Offline Mode Toggle Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isOffline) NeonGreen.copy(alpha = 0.2f) else Color(0x22FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOffline) Icons.Filled.WifiOff else Icons.Filled.Wifi,
                                    contentDescription = null,
                                    tint = if (isOffline) NeonGreen else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Offline Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(
                                    if (isOffline) "Only downloaded lossless tracks are played" else "Stream online & offline tracks",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = isOffline,
                            onCheckedChange = { viewModel.toggleOfflineMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen),
                            modifier = Modifier.testTag("library_offline_switch")
                        )
                    }
                }
            }

            // Organization Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { (optionFilter, label) ->
                        val isSelected = filter == optionFilter
                        Surface(
                            color = if (isSelected) NeonGreen else DarkSurfaceVariant,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { viewModel.setLibraryFilter(optionFilter) }
                                .testTag("filter_${optionFilter.name}")
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Liked Songs Card Shortcut
            if (filter == LibraryFilter.ALL || filter == LibraryFilter.LIKED) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (likedTracks.isNotEmpty()) {
                                    viewModel.playTrack(likedTracks[0], likedTracks)
                                }
                            }
                            .testTag("liked_songs_card"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4C1D95), DarkSurfaceVariant)
                                    )
                                )
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(AppleRed, ElectricPurple)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Liked Songs", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text("${likedTracks.size} songs • Auto-synced offline", fontSize = 12.sp, color = TextSecondary)
                            }

                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play Liked", tint = NeonGreen, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // Playlists List (with Collab and Blend badges)
            if (filter == LibraryFilter.ALL || filter == LibraryFilter.PLAYLISTS) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "PLAYLISTS & COLLABS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(playlists) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (playlist.isBlend) {
                                    onOpenBlend(playlist)
                                } else if (allTracks.isNotEmpty()) {
                                    viewModel.playTrack(allTracks[0], allTracks)
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .testTag("playlist_item_${playlist.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = playlist.coverRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = playlist.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (playlist.isCollaborative) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = CyberCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "COLLAB",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberCyan,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                if (playlist.isBlend) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = ElectricPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "BLEND",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricPurple,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = playlist.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }

            // Tracks Section (Downloaded tracks or All)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (filter == LibraryFilter.DOWNLOADED) "OFFLINE DOWNLOADED TRACKS" else "ALL SONGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val tracksToShow = if (filter == LibraryFilter.DOWNLOADED || isOffline) downloadedTracks else allTracks

            items(tracksToShow) { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playTrack(track, tracksToShow) }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .testTag("lib_track_${track.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = track.coverRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (track.isDownloaded) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Downloaded",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "${track.artist} • ${track.quality.badge}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleTrackDownload(track) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (track.isDownloaded) Icons.Filled.CheckCircle else Icons.Outlined.Download,
                            contentDescription = "Download",
                            tint = if (track.isDownloaded) NeonGreen else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                title = { Text("New Playlist", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newPlaylistTitle,
                            onValueChange = { newPlaylistTitle = it },
                            label = { Text("Playlist Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPlaylistDescription,
                            onValueChange = { newPlaylistDescription = it },
                            label = { Text("Description") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Collaborative Playlist", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Invite friends to add & reorder tracks", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = isNewCollab,
                                onCheckedChange = { isNewCollab = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistTitle.isNotBlank()) {
                                viewModel.createCollaborativePlaylist(newPlaylistTitle, newPlaylistDescription)
                                newPlaylistTitle = ""
                                newPlaylistDescription = ""
                                showCreatePlaylistDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}
