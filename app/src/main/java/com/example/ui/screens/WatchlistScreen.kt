package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Movie
import com.example.ui.ZeedViewModel
import com.example.ui.theme.*

@Composable
fun WatchlistScreen(
    viewModel: ZeedViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: รายการที่บันทึก, 1: รับชมต่อ, 2: ดาวน์โหลดแล้ว

    val watchlistIds by viewModel.watchlistIds.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val allMovies = remember { viewModel.getAllMovies() }

    val savedMovies = remember(watchlistIds, allMovies) {
        allMovies.filter { watchlistIds.contains(it.id) }
    }

    val historyMovies = remember(watchHistory, allMovies) {
        watchHistory.mapNotNull { hist ->
            val movie = allMovies.find { it.id == hist.movieId }
            if (movie != null) movie to hist else null
        }
    }

    val downloadedMovies = remember(downloads, allMovies) {
        downloads.mapNotNull { dl ->
            val movie = allMovies.find { it.id == dl.movieId }
            if (movie != null) movie to dl else null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Text(
                    text = "รายการของฉัน & ออฟไลน์",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Sub Tab selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CinemaSurface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("รายการที่บันทึก (${savedMovies.size})", "รับชมต่อ", "ดาวน์โหลด (${downloadedMovies.size})")
                    tabs.forEachIndexed { index, label ->
                        val isSelected = index == selectedSubTab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ZeedCrimson else Color.Transparent)
                                .clickable { selectedSubTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            // Tab 0: Watchlist
            if (selectedSubTab == 0) {
                if (savedMovies.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            icon = Icons.Default.BookmarkBorder,
                            title = "ไม่มีรายการที่บันทึกไว้",
                            subtitle = "กดปุ่ม '+ รายการของฉัน' ในภาพยนตร์เพื่อเพิ่มที่นี่"
                        )
                    }
                } else {
                    items(savedMovies) { movie ->
                        WatchlistItemCard(
                            movie = movie,
                            onPlayClick = { viewModel.startPlaying(movie) },
                            onDetailClick = { viewModel.openMovieDetail(movie) },
                            onRemoveClick = { viewModel.toggleWatchlist(movie.id) }
                        )
                    }
                }
            }

            // Tab 1: Continue Watching History
            if (selectedSubTab == 1) {
                if (historyMovies.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            icon = Icons.Default.History,
                            title = "ยังไม่มีประวัติการรับชม",
                            subtitle = "เมื่อคุณเริ่มดูภาพยนตร์ ประวัติจะแสดงที่นี่"
                        )
                    }
                } else {
                    items(historyMovies) { (movie, hist) ->
                        HistoryItemCard(
                            movie = movie,
                            progress = hist.progress,
                            onPlayClick = { viewModel.startPlaying(movie, hist.currentPositionSeconds) },
                            onDetailClick = { viewModel.openMovieDetail(movie) }
                        )
                    }
                }
            }

            // Tab 2: Downloads
            if (selectedSubTab == 2) {
                item {
                    // Storage Indicator Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CinemaSurfaceVariant,
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CinemaCardBorder)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = "Storage",
                                        tint = ZeedGold4K,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "พื้นที่จัดเก็บไฟล์ออฟไลน์",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "8.7 GB / 64 GB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZeedCyanNeon
                                )
                            }
                            LinearProgressIndicator(
                                progress = { 0.14f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ZeedGold4K,
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                }

                if (downloadedMovies.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            icon = Icons.Default.DownloadDone,
                            title = "ยังไม่มีภาพยนตร์ที่ดาวน์โหลด",
                            subtitle = "กดไอคอนดาวน์โหลดในหน้ารายละเอียดภาพยนตร์เพื่อดูแบบออฟไลน์"
                        )
                    }
                } else {
                    items(downloadedMovies) { (movie, dl) ->
                        DownloadItemCard(
                            movie = movie,
                            fileSizeMb = dl.fileSizeMb,
                            quality = dl.quality,
                            onPlayClick = { viewModel.startPlaying(movie) },
                            onDeleteClick = { viewModel.downloadMovie(movie.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistItemCard(
    movie: Movie,
    onPlayClick: () -> Unit,
    onDetailClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Surface(
        onClick = onDetailClick,
        shape = RoundedCornerShape(10.dp),
        color = CinemaSurfaceVariant,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(CinemaCardBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val resId = movie.drawableResId ?: R.drawable.ic_app_icon
            Image(
                painter = painterResource(id = resId),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 70.dp, height = 95.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${movie.year} • ${movie.duration} • ${movie.resolution}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = movie.audio,
                    style = MaterialTheme.typography.labelSmall,
                    color = TagThaiDubText
                )
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ZeedCrimson)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    movie: Movie,
    progress: Float,
    onPlayClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    Surface(
        onClick = onDetailClick,
        shape = RoundedCornerShape(10.dp),
        color = CinemaSurfaceVariant,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(CinemaCardBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val resId = movie.backdropResId ?: movie.drawableResId ?: R.drawable.img_hero_banner
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 90.dp, height = 60.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "ดูไปแล้ว ${(progress * 100).toInt()}% • ${movie.resolution}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ZeedCrimson)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = ZeedCrimson,
                trackColor = Color.DarkGray
            )
        }
    }
}

@Composable
private fun DownloadItemCard(
    movie: Movie,
    fileSizeMb: Int,
    quality: String,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CinemaSurfaceVariant,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(CinemaCardBorder)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val resId = movie.drawableResId ?: R.drawable.ic_app_icon
            Image(
                painter = painterResource(id = resId),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 65.dp, height = 85.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$quality • ${(fileSizeMb / 1024f * 10).toInt() / 10f} GB",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZeedCyanNeon
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Offline Ready",
                        tint = ZeedGold4K,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "พร้อมรับชมแบบออฟไลน์",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeedGold4K
                    )
                }
            }

            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ZeedCrimson)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyPlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
