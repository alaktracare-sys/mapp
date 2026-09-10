package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioQuality
import com.example.model.Track
import com.example.ui.theme.*
import com.example.viewmodel.MusicViewModel

@Composable
fun HiFiSettingsDialog(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val quality by viewModel.audioQuality.collectAsState()
    val isSpatial by viewModel.isSpatialAudio.collectAsState()
    val crossfadeSec by viewModel.crossfadeSec.collectAsState()
    val isGapless by viewModel.isGapless.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = HiResGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("High-Fidelity Audio Settings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Live DAC status
                Surface(
                    color = HiResGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("INTERNAL DAC STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = HiResGold)
                        Text(
                            text = "${quality.sampleRate} • ${quality.bitrate} • Bit-Perfect Output",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("AUDIO STREAMING QUALITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                AudioQuality.values().forEach { q ->
                    val isSelected = quality == q
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setAudioQuality(q) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(q.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) HiResGold else TextPrimary)
                            Text(q.bitrate, fontSize = 11.sp, color = TextSecondary)
                        }
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setAudioQuality(q) },
                            colors = RadioButtonDefaults.colors(selectedColor = HiResGold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Spatial Audio Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Spatial Audio with Dolby Atmos", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Dynamic 3D head-tracking simulation", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = isSpatial,
                        onCheckedChange = { viewModel.setSpatialAudio(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Seamless Transitions & Crossfade
                Text("CROSSFADE & SEAMLESS TRANSITION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Crossfade Duration", fontSize = 12.sp, color = TextPrimary)
                    Text("${crossfadeSec}s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                }
                Slider(
                    value = crossfadeSec.toFloat(),
                    onValueChange = { viewModel.setCrossfadeSec(it.toInt()) },
                    valueRange = 0f..12f,
                    colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gapless Playback", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Switch(
                        checked = isGapless,
                        onCheckedChange = { viewModel.setGapless(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = HiResGold)) {
                Text("Apply & Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun VoiceControlModal(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val voiceFeedback by viewModel.voiceFeedback.collectAsState()
    var simulatedQuery by remember { mutableStateOf("") }

    val quickCommands = listOf(
        "Play Midnight Drive",
        "Smart Shuffle",
        "Bass Boost",
        "Offline Mode",
        "Next Song",
        "Loop All"
    )

    AlertDialog(
        onDismissRequest = {
            viewModel.clearVoiceFeedback()
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Alaktra Voice Assistant", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Control playback hands-free using intuitive natural language voice commands.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Feedback Banner
                if (voiceFeedback != null) {
                    Surface(
                        color = NeonGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(voiceFeedback!!, color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Quick command chips
                Text("QUICK VOICE INTENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickCommands.forEach { cmd ->
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.processVoiceCommand(cmd)
                                }
                                .testTag("voice_cmd_$cmd")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("\"$cmd\"", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.clearVoiceFeedback()
                    onDismiss()
                }
            ) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun ShareCardDialog(
    track: Track,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isLinkCopied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Share Track", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Instagram Story / TikTok styled social preview card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF831843), Color(0xFF3B0764), Color(0xFF0F172A))
                                )
                            )
                            .padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = track.coverRes),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = track.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = track.artist,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "♪ Streaming on Alaktra",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLinkCopied) {
                    Text("✓ Shareable link copied!", color = NeonGreen, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { isLinkCopied = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Link", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Listen to ${track.title}")
                                putExtra(Intent.EXTRA_TEXT, "Listen to ${track.title} by ${track.artist} on Alaktra: https://alaktra.app/track/${track.id}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun DeviceSyncDialog(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Devices, contentDescription = null, tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connect to a Device", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = CyberCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = syncStatus,
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("AVAILABLE ALAKTRA DEVICES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                devices.forEach { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.switchDevice(device.id)
                            }
                            .testTag("device_${device.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (device.isCurrent) Color(0xFF132B1D) else DarkSurfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (device.type) {
                                        "Phone" -> Icons.Filled.Smartphone
                                        "Laptop" -> Icons.Filled.Laptop
                                        "Speaker" -> Icons.Filled.Speaker
                                        else -> Icons.Filled.Tablet
                                    },
                                    contentDescription = null,
                                    tint = if (device.isCurrent) NeonGreen else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        device.name,
                                        fontWeight = if (device.isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (device.isCurrent) NeonGreen else TextPrimary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        if (device.isCurrent) "Current Playback Device" else "Alaktra Connect Ready",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (device.isCurrent) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = NeonGreen)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = NeonGreen)
            }
        },
        containerColor = DarkSurface
    )
}
