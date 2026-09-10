package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MusicViewModel

@Composable
fun EqualizerScreen(
    viewModel: MusicViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled by viewModel.isEqualizerEnabled.collectAsState()
    val currentPreset by viewModel.eqPreset.collectAsState()
    val bands by viewModel.eqBands.collectAsState()
    val bassBoost by viewModel.bassBoost.collectAsState()
    val virtualizer by viewModel.virtualizer.collectAsState()

    val presets = listOf("Bass Boost", "Electronic", "Acoustic", "Rock", "Hip-Hop", "Vocal Booster", "Flat")
    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("equalizer_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
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
                    modifier = Modifier.testTag("eq_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Equalizer",
                        tint = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = ElectricPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STUDIO EQUALIZER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.setEqualizerEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonGreen,
                        checkedTrackColor = Color(0xFF0E3D1E)
                    ),
                    modifier = Modifier.testTag("eq_master_switch")
                )
            }

            // Live Dynamic EQ Visualizer Curve Canvas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val w = size.width
                        val h = size.height
                        val midY = h / 2f

                        // Center zero dB line
                        drawLine(
                            color = Color(0x33FFFFFF),
                            start = Offset(0f, midY),
                            end = Offset(w, midY),
                            strokeWidth = 2f
                        )

                        // Draw frequency curve
                        val path = Path()
                        val points = mutableListOf<Offset>()
                        for (i in bands.indices) {
                            val x = (i.toFloat() / (bands.size - 1)) * w
                            val gain = if (isEnabled) bands[i] else 0f
                            val y = midY - (gain / 12f) * (midY - 10f)
                            points.add(Offset(x, y))
                        }

                        if (points.isNotEmpty()) {
                            path.moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val cur = points[i]
                                val cX = (prev.x + cur.x) / 2f
                                path.cubicTo(cX, prev.y, cX, cur.y, cur.x, cur.y)
                            }

                            drawPath(
                                path = path,
                                color = if (isEnabled) NeonGreen else TextMuted,
                                style = Stroke(width = 4f)
                            )
                        }
                    }

                    Text(
                        text = if (isEnabled) "Active EQ • 5-Band Master Curve" else "Bypass (Disabled)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) NeonGreen else TextMuted,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preset selector chips
            Text(
                text = "SOUND PROFILES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    val isSelected = currentPreset == preset
                    Surface(
                        color = if (isSelected) ElectricPurple else DarkSurfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.setEqPreset(preset) }
                            .testTag("preset_$preset")
                    ) {
                        Text(
                            text = preset,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5 Band Gain Sliders
            Text(
                text = "FREQUENCY BANDS (-12dB to +12dB)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bands.forEachIndexed { index, gain ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${if (gain > 0) "+" else ""}${gain.toInt()}dB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gain > 0) NeonGreen else if (gain < 0) AppleRed else TextSecondary
                        )

                        // Vertical Slider simulation using Custom Slider
                        Slider(
                            value = gain,
                            onValueChange = { newGain ->
                                viewModel.setBandGain(index, newGain)
                            },
                            valueRange = -12f..12f,
                            enabled = isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricPurple,
                                activeTrackColor = ElectricPurple,
                                inactiveTrackColor = DarkSurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .testTag("eq_band_slider_$index")
                        )

                        Text(
                            text = bandLabels[index],
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bass Boost & Virtualizer knobs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bass Boost Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BASS BOOST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text("$bassBoost%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        }
                        Slider(
                            value = bassBoost.toFloat(),
                            onValueChange = { viewModel.setBassBoost(it.toInt()) },
                            valueRange = 0f..100f,
                            enabled = isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonGreen,
                                activeTrackColor = NeonGreen
                            ),
                            modifier = Modifier.testTag("bass_boost_slider")
                        )
                    }
                }

                // 3D Virtualizer Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3D VIRTUALIZER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text("$virtualizer%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        }
                        Slider(
                            value = virtualizer.toFloat(),
                            onValueChange = { viewModel.setVirtualizer(it.toInt()) },
                            valueRange = 0f..100f,
                            enabled = isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan
                            ),
                            modifier = Modifier.testTag("virtualizer_slider")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
