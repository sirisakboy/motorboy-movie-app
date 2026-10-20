package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Movie
import com.example.ui.ZeedViewModel
import com.example.ui.components.MoviePosterCard
import com.example.ui.theme.*

@Composable
fun MovieDetailSheet(
    movie: Movie,
    viewModel: ZeedViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val watchlistIds by viewModel.watchlistIds.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val isDownloaded = viewModel.isDownloaded(movie.id)
    val isInWatchlist = watchlistIds.contains(movie.id)
    val downloadProgress = isDownloading[movie.id]

    val similarMovies = remember(movie) {
        viewModel.getAllMovies().filter { it.id != movie.id }
    }

    BackHandler {
        onClose()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .testTag("movie_detail_sheet")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Backdrop
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val resId = movie.backdropResId ?: movie.drawableResId ?: R.drawable.img_hero_banner
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient fade
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        CinemaDarkBackground.copy(alpha = 0.5f),
                                        CinemaDarkBackground
                                    )
                                )
                            )
                    )

                    // Close Button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(16.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .align(Alignment.TopStart)
                            .testTag("detail_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    // Play FAB in center
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(ZeedCrimson)
                            .align(Alignment.Center)
                            .clickable {
                                viewModel.startPlaying(movie)
                            }
                            .testTag("detail_center_play"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            // Info Content
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Title and Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ZeedGold4K)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "4K ULTRA HD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = CinemaBlack
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = ZeedGold4K,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${movie.rating}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "• ${movie.viewsCount} ยอดเข้าชม",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    Text(
                        text = movie.originalTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )

                    // Metadata Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${movie.year}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ZeedCyanNeon
                        )
                        Text(text = "•", color = TextMuted)
                        Text(
                            text = movie.duration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(text = "•", color = TextMuted)
                        Box(
                            modifier = Modifier
                                .border(0.5.dp, TextMuted, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = movie.ageRating,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Text(text = "•", color = TextMuted)
                        Text(
                            text = movie.audio,
                            style = MaterialTheme.typography.labelSmall,
                            color = TagThaiDubText
                        )
                    }

                    // Main Action Play Button
                    Button(
                        onClick = { viewModel.startPlaying(movie) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("detail_play_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZeedCrimson)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "รับชมภาพยนตร์ (4K UHD)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    // Secondary Action Icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Watchlist
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.toggleWatchlist(movie.id) }
                                .padding(8.dp)
                                .testTag("detail_watchlist_toggle")
                        ) {
                            Icon(
                                imageVector = if (isInWatchlist) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = "Watchlist",
                                tint = if (isInWatchlist) ZeedGold4K else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isInWatchlist) "ในรายการ" else "รายการฉัน",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isInWatchlist) ZeedGold4K else TextSecondary
                            )
                        }

                        // Download
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.downloadMovie(movie.id) }
                                .padding(8.dp)
                                .testTag("detail_download_button")
                        ) {
                            if (downloadProgress != null) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = ZeedGold4K
                                )
                            } else {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = if (isDownloaded) ZeedCyanNeon else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    downloadProgress != null -> "$downloadProgress%"
                                    isDownloaded -> "ดาวน์โหลดแล้ว"
                                    else -> "ดาวน์โหลด 4K"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDownloaded) ZeedCyanNeon else TextSecondary
                            )
                        }

                        // Share
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "ดูภาพยนตร์ ${movie.title} ระดับ 4K Ultra HD ได้ที่ Zeed4K!")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "แชร์ภาพยนตร์"))
                                }
                                .padding(8.dp)
                                .testTag("detail_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "แชร์",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = CinemaCardBorder)

                    // Synopsis
                    Text(
                        text = "เรื่องย่อ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Text(
                        text = movie.synopsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp
                        ),
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Cast & Crew
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ผู้กำกับ:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                        Text(
                            text = movie.director,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "นักแสดง:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                        Text(
                            text = movie.cast.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Similar movies section
                    Text(
                        text = "ภาพยนตร์ที่เกี่ยวข้อง",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Similar Movies horizontal row
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(similarMovies) { simMovie ->
                        MoviePosterCard(
                            movie = simMovie,
                            onClick = {
                                viewModel.openMovieDetail(simMovie)
                            }
                        )
                    }
                }
            }
        }
    }
}
