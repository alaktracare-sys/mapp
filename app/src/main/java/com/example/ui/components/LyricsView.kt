package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MusicViewModel

@Composable
fun LyricsView(
    viewModel: MusicViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track by viewModel.currentTrack.collectAsState()
    val activeLyricIndex by viewModel.currentLyricIndex.collectAsState()
    val listState = rememberLazyListState()
    var isSingAlongMode by remember { mutableStateOf(false) }

    // Auto-scroll to active lyric smoothly
    LaunchedEffect(activeLyricIndex) {
        if (activeLyricIndex >= 0 && track != null && activeLyricIndex < track!!.lyrics.size) {
            val targetScroll = (activeLyricIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    val dynamicLyricsGradient = listOf(
        Color(0xFF1E0B36),
        Color(0xFF100720),
        DarkBackground
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(dynamicLyricsGradient))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("lyrics_view_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("lyrics_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Lyrics",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "REALTIME SYNCED LYRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = NeonGreen
                    )
                    Text(
                        text = track?.title ?: "No Track",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                // Sing-Along Karaoke Mode toggle
                FilledTonalButton(
                    onClick = { isSingAlongMode = !isSingAlongMode },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSingAlongMode) NeonGreen else Color(0x33FFFFFF),
                        contentColor = if (isSingAlongMode) Color.Black else TextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("karaoke_sing_along_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Karaoke",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSingAlongMode) "Sing" else "Vocal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (track == null || track!!.lyrics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No synced lyrics available for this track.",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    itemsIndexed(track!!.lyrics) { index, lyric ->
                        val isActive = index == activeLyricIndex
                        val isPassed = index < activeLyricIndex

                        val textColor by animateColorAsState(
                            targetValue = when {
                                isActive -> TextPrimary
                                isPassed -> TextSecondary.copy(alpha = 0.5f)
                                else -> TextMuted.copy(alpha = 0.4f)
                            },
                            animationSpec = tween(300),
                            label = "lyric_color"
                        )

                        val fontSize = if (isActive) 28.sp else 22.sp
                        val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    // Interactive seeking: Tap any line to jump right to that timestamp!
                                    viewModel.seekTo(lyric.timestampMs)
                                }
                                .padding(vertical = 8.dp, horizontal = 10.dp)
                                .testTag("lyric_line_$index")
                        ) {
                            Text(
                                text = lyric.text,
                                fontSize = fontSize,
                                fontWeight = fontWeight,
                                color = textColor,
                                lineHeight = 34.sp
                            )
                        }
                    }
                }
            }

            // Tip at bottom
            Surface(
                color = Color(0x22FFFFFF),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Tap any lyric to jump playback directly to that verse",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 14.dp)
                )
            }
        }
    }
}
