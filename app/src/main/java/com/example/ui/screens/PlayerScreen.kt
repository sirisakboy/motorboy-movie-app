package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Movie
import com.example.ui.PlayerState
import com.example.ui.ZeedViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: ZeedViewModel,
    playerState: PlayerState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val movie = playerState.movie ?: return

    BackHandler {
        if (playerState.isFullscreen) {
            viewModel.toggleFullscreen()
        } else {
            onClose()
        }
    }

    var showQualitySheet by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showServerSheet by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                viewModel.toggleControlsVisibility()
            }
            .testTag("player_container")
    ) {
        // Video Backdrop Image
        val resId = movie.backdropResId ?: movie.drawableResId ?: R.drawable.img_hero_banner
        Image(
            painter = painterResource(id = resId),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark dimming overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (playerState.showControls) Color.Black.copy(alpha = 0.55f) else Color.Transparent
                )
        )

        // Controls
        AnimatedVisibility(
            visible = playerState.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(ZeedGold4K)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = playerState.quality.take(6),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = CinemaBlack
                                    )
                                }
                                Text(
                                    text = playerState.audioTrack,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Top Action Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.togglePlaybackMode() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("player_mode_button")
                        ) {
                            val effectiveMode = viewModel.getEffectivePlaybackMode(playerState.playbackMode)
                            Icon(
                                imageVector = when (effectiveMode) {
                                    PlaybackMode.DAY -> Icons.Default.WbSunny
                                    PlaybackMode.NIGHT -> Icons.Default.NightsStay
                                    PlaybackMode.AUTO -> Icons.Default.Brightness7
                                },
                                contentDescription = "Playback Mode",
                                tint = when (effectiveMode) {
                                    PlaybackMode.DAY -> Color(0xFFFFD700)
                                    PlaybackMode.NIGHT -> Color(0xFFB0C4DE)
                                    PlaybackMode.AUTO -> Color.White
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showServerSheet = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("player_server_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "Server",
                                tint = ZeedCyanNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { showQualitySheet = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("player_quality_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = "Quality",
                                tint = ZeedGold4K,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Center Play / Pause / Rewind / FastForward
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = { viewModel.seekBy(-10) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("player_rewind_10")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Replay 10s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play / Pause
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ZeedCrimson)
                            .clickable { viewModel.togglePlayPause() }
                            .testTag("player_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = { viewModel.seekBy(10) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("player_forward_10")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Time and Bitrate info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatSeconds(playerState.currentPositionSeconds)} / ${formatSeconds(playerState.totalDurationSeconds)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ZeedCyanNeon)
                            )
                            Text(
                                text = "4K HDR • ${movie.bitrate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZeedCyanNeon
                            )
                        }
                    }

                    // Scrubber Slider
                    Slider(
                        value = playerState.currentPositionSeconds.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toInt()) },
                        valueRange = 0f..playerState.totalDurationSeconds.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = ZeedCrimson,
                            activeTrackColor = ZeedCrimson,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("player_slider")
                    )

                    // Control buttons toolbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Audio & Subtitle
                            TextButton(
                                onClick = { showAudioSheet = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("player_audio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = "Audio/Sub",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "เสียง/ซับ",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            // Speed
                            TextButton(
                                onClick = { showSpeedSheet = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("player_speed_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Speed",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${playerState.speed}x",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        // Fullscreen button
                        IconButton(
                            onClick = { viewModel.toggleFullscreen() },
                            modifier = Modifier.testTag("player_fullscreen_button")
                        ) {
                            Icon(
                                imageVector = if (playerState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for Quality
    if (showQualitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showQualitySheet = false },
            containerColor = CinemaSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "เลือกความละเอียดภาพยนตร์",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                movie.videoQualityOptions.forEach { opt ->
                    val isSelected = opt == playerState.quality
                    Surface(
                        onClick = {
                            viewModel.setQuality(opt)
                            showQualitySheet = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) ZeedCrimson.copy(alpha = 0.2f) else CinemaSurfaceVariant,
                        border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(ZeedCrimson, ZeedGold4K))
                        ) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = opt,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) ZeedGold4K else Color.White
                                )
                                Text(
                                    text = if (opt.contains("4K")) "Ultra HD • คมชัดสูงสุด 60fps HDR" else "ความละเอียดมาตรฐาน",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = ZeedGold4K
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Sheet for Audio & Subtitle
    if (showAudioSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAudioSheet = false },
            containerColor = CinemaSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ระบบเสียงและคำบรรยาย",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "ระบบเสียง (Audio)",
                    style = MaterialTheme.typography.labelMedium.copy(color = ZeedGold4K)
                )

                movie.audioTracks.forEach { track ->
                    val isSelected = track == playerState.audioTrack
                    Surface(
                        onClick = { viewModel.setAudioTrack(track) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) ZeedCrimson.copy(alpha = 0.2f) else CinemaSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = track, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ZeedGold4K)
                            }
                        }
                    }
                }

                Text(
                    text = "คำบรรยาย (Subtitles)",
                    style = MaterialTheme.typography.labelMedium.copy(color = ZeedCyanNeon)
                )

                movie.subtitles.forEach { sub ->
                    val isSelected = sub == playerState.subtitle
                    Surface(
                        onClick = { viewModel.setSubtitle(sub) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) ZeedCrimson.copy(alpha = 0.2f) else CinemaSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = sub, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ZeedGold4K)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Sheet for Speed
    if (showSpeedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedSheet = false },
            containerColor = CinemaSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ความเร็วในการเล่น",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                    val isSelected = speed == playerState.speed
                    Surface(
                        onClick = {
                            viewModel.setPlaybackSpeed(speed)
                            showSpeedSheet = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) ZeedCrimson.copy(alpha = 0.2f) else CinemaSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (speed == 1.0f) "1.0x (ความเร็วปกติ)" else "${speed}x",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ZeedGold4K)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Sheet for Server
    if (showServerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showServerSheet = false },
            containerColor = CinemaSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "สลับเซิร์ฟเวอร์สตรีมมิ่ง",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                movie.servers.forEach { server ->
                    val isSelected = server == playerState.server
                    Surface(
                        onClick = {
                            viewModel.setServer(server)
                            showServerSheet = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) ZeedCrimson.copy(alpha = 0.2f) else CinemaSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = server, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                Text(text = "ความเร็วเฉลี่ย 85 MB/s • Low Latency", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ZeedGold4K)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun formatSeconds(seconds: Int): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
