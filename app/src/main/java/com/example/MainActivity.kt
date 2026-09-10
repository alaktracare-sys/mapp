package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Playlist
import com.example.ui.components.*
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.theme.*
import com.example.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val viewModel: MusicViewModel = viewModel()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val shareTrack by viewModel.shareTrack.collectAsState()

    // Screen navigation state
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Explore, 1: Library, 2: Jam, 3: Analytics
    var isFullPlayerOpen by remember { mutableStateOf(false) }
    var isLyricsOpen by remember { mutableStateOf(false) }
    var isEqualizerOpen by remember { mutableStateOf(false) }
    var isJamRoomOpen by remember { mutableStateOf(false) }
    var selectedBlend by remember { mutableStateOf<Playlist?>(null) }

    // Dialog state
    var isHiFiDialogOpen by remember { mutableStateOf(false) }
    var isVoiceModalOpen by remember { mutableStateOf(false) }
    var isDevicesDialogOpen by remember { mutableStateOf(false) }
    var isCreateBlendDialogOpen by remember { mutableStateOf(false) }

    // Android Speech Recognizer Intent Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenData = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenData?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.processVoiceCommand(spokenText)
                isVoiceModalOpen = true
            }
        }
    }

    val launchVoiceRecognizer = {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a music command, e.g. 'Play Midnight' or 'Smart Shuffle'")
            }
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            // Fallback gracefully to in-app voice command modal if Google Speech isn't available on device/emulator
            isVoiceModalOpen = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            Column {
                // Mini Player docked above Bottom Navigation
                if (currentTrack != null && !isFullPlayerOpen && !isLyricsOpen && !isEqualizerOpen && !isJamRoomOpen && selectedBlend == null) {
                    MiniPlayer(
                        viewModel = viewModel,
                        onExpand = { isFullPlayerOpen = true },
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Standard M3 Navigation Bar with window insets support
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0 && selectedBlend == null,
                        onClick = {
                            selectedBlend = null
                            selectedTab = 0
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonGreen,
                            selectedTextColor = NeonGreen,
                            indicatorColor = NeonGreen.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_explore")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1 && selectedBlend == null,
                        onClick = {
                            selectedBlend = null
                            selectedTab = 1
                        },
                        icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonGreen,
                            selectedTextColor = NeonGreen,
                            indicatorColor = NeonGreen.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_library")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2 || isJamRoomOpen,
                        onClick = {
                            selectedBlend = null
                            selectedTab = 2
                            isJamRoomOpen = true
                        },
                        icon = { Icon(Icons.Filled.Group, contentDescription = "Live Jam") },
                        label = { Text("Live Jam", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_jam")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3 && selectedBlend == null,
                        onClick = {
                            selectedBlend = null
                            selectedTab = 3
                        },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = "Analytics") },
                        label = { Text("Pulse Stats", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricPurple,
                            selectedTextColor = ElectricPurple,
                            indicatorColor = ElectricPurple.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_analytics")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main tab contents
            when {
                selectedBlend != null -> {
                    BlendView(
                        viewModel = viewModel,
                        playlist = selectedBlend!!,
                        onBack = { selectedBlend = null }
                    )
                }
                selectedTab == 0 -> {
                    ExploreScreen(
                        viewModel = viewModel,
                        onOpenVoice = launchVoiceRecognizer,
                        onOpenHiFi = { isHiFiDialogOpen = true },
                        onOpenEqualizer = { isEqualizerOpen = true },
                        onOpenJam = { isJamRoomOpen = true },
                        onOpenBlend = { blend -> selectedBlend = blend }
                    )
                }
                selectedTab == 1 -> {
                    LibraryScreen(
                        viewModel = viewModel,
                        onOpenBlend = { blend -> selectedBlend = blend },
                        onOpenCreateBlend = { isCreateBlendDialogOpen = true }
                    )
                }
                selectedTab == 2 -> {
                    JamRoomScreen(
                        viewModel = viewModel,
                        onClose = {
                            isJamRoomOpen = false
                            selectedTab = 0
                        }
                    )
                }
                selectedTab == 3 -> {
                    AnalyticsScreen(viewModel = viewModel)
                }
            }

            // Full Player Screen (Overlays on tap or swipe up)
            AnimatedVisibility(
                visible = isFullPlayerOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FullPlayerSheet(
                    viewModel = viewModel,
                    onDismiss = { isFullPlayerOpen = false },
                    onOpenLyrics = { isLyricsOpen = true },
                    onOpenEqualizer = { isEqualizerOpen = true },
                    onOpenJam = { isJamRoomOpen = true },
                    onOpenHiFi = { isHiFiDialogOpen = true },
                    onOpenShare = {
                        if (currentTrack != null) viewModel.openShareDialog(currentTrack!!)
                    },
                    onOpenDevices = { isDevicesDialogOpen = true }
                )
            }

            // Realtime Karaoke Synced Lyrics View
            AnimatedVisibility(
                visible = isLyricsOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                LyricsView(
                    viewModel = viewModel,
                    onClose = { isLyricsOpen = false }
                )
            }

            // Studio 5-Band Equalizer Screen
            AnimatedVisibility(
                visible = isEqualizerOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                EqualizerScreen(
                    viewModel = viewModel,
                    onClose = { isEqualizerOpen = false }
                )
            }

            // Live Jam Screen (when opened from player or button)
            if (isJamRoomOpen && selectedTab != 2) {
                JamRoomScreen(
                    viewModel = viewModel,
                    onClose = { isJamRoomOpen = false }
                )
            }

            // High-Fidelity Audio Settings Dialog
            if (isHiFiDialogOpen) {
                HiFiSettingsDialog(
                    viewModel = viewModel,
                    onDismiss = { isHiFiDialogOpen = false }
                )
            }

            // Voice Control Feedback & Quick Commands Dialog
            if (isVoiceModalOpen) {
                VoiceControlModal(
                    viewModel = viewModel,
                    onDismiss = { isVoiceModalOpen = false }
                )
            }

            // Cross-Platform Device Sync Dialog
            if (isDevicesDialogOpen) {
                DeviceSyncDialog(
                    viewModel = viewModel,
                    onDismiss = { isDevicesDialogOpen = false }
                )
            }

            // Create Blend with Friends Dialog
            if (isCreateBlendDialogOpen) {
                CreateBlendDialog(
                    viewModel = viewModel,
                    onDismiss = { isCreateBlendDialogOpen = false }
                )
            }

            // Social Share Modal Dialog
            if (shareTrack != null) {
                ShareCardDialog(
                    track = shareTrack!!,
                    onDismiss = { viewModel.closeShareDialog() }
                )
            }
        }
    }
}
