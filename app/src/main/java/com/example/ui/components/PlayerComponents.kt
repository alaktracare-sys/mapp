package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RepeatMode
import com.example.model.ShuffleMode
import com.example.model.Track
import com.example.ui.theme.*
import com.example.viewmodel.MusicViewModel

@Composable
fun MiniPlayer(
    viewModel: MusicViewModel,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()

    if (track == null) return

    val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    // Horizontal drag to skip / previous gesture
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        dragAccumulator += delta
        if (dragAccumulator > 120f) {
            viewModel.previousTrack()
            dragAccumulator = 0f
        } else if (dragAccumulator < -120f) {
            viewModel.nextTrack()
            dragAccumulator = 0f
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onExpand() }
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = { dragAccumulator = 0f }
            )
            .testTag("mini_player_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xF5161A22)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Linear thin playback progress bar on top edge of mini player
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = NeonGreen,
                trackColor = Color(0x22FFFFFF)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art
                Image(
                    painter = painterResource(id = track!!.coverRes),
                    contentDescription = "Track Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title, Artist, and Hi-Fi Tag
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track!!.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            color = HiResGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = track!!.quality.badge,
                                color = HiResGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = track!!.artist,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Like button
                IconButton(
                    onClick = { viewModel.toggleLikeCurrentTrack() },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("mini_player_like_button")
                ) {
                    Icon(
                        imageVector = if (track!!.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (track!!.isLiked) AppleRed else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play/Pause button
                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                        .testTag("mini_player_play_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Next button
                IconButton(
                    onClick = { viewModel.nextTrack() },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("mini_player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenJam: () -> Unit,
    onOpenHiFi: () -> Unit,
    onOpenShare: () -> Unit,
    onOpenDevices: () -> Unit
) {
    val track by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val isSpatialAudio by viewModel.isSpatialAudio.collectAsState()
    val isJamActive by viewModel.isJamActive.collectAsState()

    if (track == null) return

    // Dynamic background glow gradient matching track mood
    val dynamicGradient = when (track!!.genre.lowercase()) {
        "synthwave" -> listOf(Color(0xFF280B45), Color(0xFF130926), DarkBackground)
        "ambient electronic" -> listOf(Color(0xFF09253B), Color(0xFF0D162B), DarkBackground)
        "chillhop / r&b" -> listOf(Color(0xFF381E11), Color(0xFF1F120D), DarkBackground)
        "cyber edm" -> listOf(Color(0xFF330921), Color(0xFF1F092E), DarkBackground)
        else -> listOf(Color(0xFF1A1C29), Color(0xFF10121D), DarkBackground)
    }

    // Vinyl rotation animation for active playback
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val vinylAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "vinyl_rotation"
    )

    // Gesture swipe on artwork
    var artDragAccumulator by remember { mutableFloatStateOf(0f) }
    val artDraggableState = rememberDraggableState { delta ->
        artDragAccumulator += delta
        if (artDragAccumulator > 140f) {
            viewModel.previousTrack()
            artDragAccumulator = 0f
        } else if (artDragAccumulator < -140f) {
            viewModel.nextTrack()
            artDragAccumulator = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(dynamicGradient))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_player_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("player_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM LIBRARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = track!!.album,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onOpenShare,
                    modifier = Modifier.testTag("player_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Center Artwork with Vinyl Effect & Gestures
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .aspectRatio(1f)
                    .draggable(
                        state = artDraggableState,
                        orientation = Orientation.Horizontal,
                        onDragStopped = { artDragAccumulator = 0f }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle vinyl glow ring
                Box(
                    modifier = Modifier
                        .size(290.dp)
                        .rotate(if (isPlaying) vinylAngle else 0f)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                )

                // Main Cover
                Image(
                    painter = painterResource(id = track!!.coverRes),
                    contentDescription = "Full Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(270.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .shadow(28.dp, RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Track Title, Artist, Hi-Fi Pill, Like & Download Icons
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track!!.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track!!.artist,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.toggleTrackDownload(track!!) },
                            modifier = Modifier.testTag("player_download_button")
                        ) {
                            Icon(
                                imageVector = if (track!!.isDownloaded) Icons.Filled.CheckCircle else Icons.Outlined.Download,
                                contentDescription = "Download Offline",
                                tint = if (track!!.isDownloaded) NeonGreen else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleLikeCurrentTrack() },
                            modifier = Modifier.testTag("player_like_button")
                        ) {
                            Icon(
                                imageVector = if (track!!.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (track!!.isLiked) AppleRed else TextSecondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Hi-Res Lossless & Audio Tech Badge
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenHiFi() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = HiResGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = HiResGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = audioQuality.title,
                                color = HiResGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isSpatialAudio) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = CyberCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SPATIAL AUDIO",
                                color = CyberCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seek Scrubbing Slider
            var sliderScrubValue by remember { mutableFloatStateOf(-1f) }
            val currentProgress = if (durationMs > 0) {
                if (sliderScrubValue >= 0) sliderScrubValue else (positionMs.toFloat() / durationMs.toFloat())
            } else 0f

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = currentProgress.coerceIn(0f, 1f),
                    onValueChange = { sliderScrubValue = it },
                    onValueChangeFinished = {
                        val seekTargetMs = (sliderScrubValue * durationMs).toLong()
                        viewModel.seekTo(seekTargetMs)
                        sliderScrubValue = -1f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_seek_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonGreen,
                        activeTrackColor = NeonGreen,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayedPos = if (sliderScrubValue >= 0) (sliderScrubValue * durationMs).toLong() else positionMs
                    Text(
                        text = formatTime(displayedPos),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = formatTime(durationMs),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Playback Controls: Shuffle, Previous, Play/Pause, Next, Loop
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Shuffle Button (Normal / Smart / Off)
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.testTag("player_shuffle_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (shuffleMode == ShuffleMode.SMART) {
                                Badge(
                                    containerColor = NeonGreen,
                                    contentColor = Color.Black
                                ) {
                                    Text("AI", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = when (shuffleMode) {
                                ShuffleMode.OFF -> TextMuted
                                ShuffleMode.NORMAL -> NeonGreen
                                ShuffleMode.SMART -> CyberCyan
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Quick -10s seek
                IconButton(
                    onClick = { viewModel.seekRelative(-10) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Replay10,
                        contentDescription = "Rewind 10 seconds",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Previous Track
                IconButton(
                    onClick = { viewModel.previousTrack() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_previous_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Main Play/Pause Button
                Surface(
                    shape = CircleShape,
                    color = NeonGreen,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.togglePlayPause() }
                        .testTag("player_main_play_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Next Track
                IconButton(
                    onClick = { viewModel.nextTrack() },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Track",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Quick +10s seek
                IconButton(
                    onClick = { viewModel.seekRelative(10) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Forward10,
                        contentDescription = "Fast forward 10 seconds",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Repeat Button (Off / All / One)
                IconButton(
                    onClick = { viewModel.toggleRepeat() },
                    modifier = Modifier.testTag("player_repeat_button")
                ) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) NeonGreen else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Toolbar: Lyrics, Equalizer, Jam, Device Sync
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Realtime Lyrics button
                IconButton(
                    onClick = onOpenLyrics,
                    modifier = Modifier.testTag("player_open_lyrics")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Lyrics",
                            tint = NeonGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Text("Lyrics", fontSize = 10.sp, color = NeonGreen)
                    }
                }

                // Equalizer button
                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier.testTag("player_open_eq")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Equalizer",
                            tint = ElectricPurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Text("Equalizer", fontSize = 10.sp, color = ElectricPurple)
                    }
                }

                // Live Jam button
                IconButton(
                    onClick = onOpenJam,
                    modifier = Modifier.testTag("player_open_jam")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = "Live Jam",
                            tint = if (isJamActive) NeonGreen else CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(if (isJamActive) "Jam Live" else "Jam", fontSize = 10.sp, color = CyberCyan)
                    }
                }

                // Cross-Platform Device Sync button
                IconButton(
                    onClick = onOpenDevices,
                    modifier = Modifier.testTag("player_open_devices")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Devices,
                            contentDescription = "Devices",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text("Devices", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
