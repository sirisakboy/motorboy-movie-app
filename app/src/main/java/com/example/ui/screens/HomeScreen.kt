package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Movie
import com.example.model.MovieCategory
import com.example.ui.AppTab
import com.example.ui.ZeedViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: ZeedViewModel,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val watchlistIds by viewModel.watchlistIds.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    val allMovies = remember { viewModel.getAllMovies() }
    val featuredMovie = remember(allMovies) { allMovies.firstOrNull() ?: return@remember null }
    val trendingMovies = remember(allMovies) { viewModel.getTrendingMovies() }
    val cinemaNewMovies = remember(allMovies) { viewModel.getCinemaNewMovies() }
    val thaiMovies = remember(allMovies) { viewModel.getMoviesByCategory(MovieCategory.THAI_MOVIES) }
    val horrorMovies = remember(allMovies) { viewModel.getMoviesByCategory(MovieCategory.HORROR) }
    val actionMovies = remember(allMovies) { viewModel.getMoviesByCategory(MovieCategory.ACTION_SCIFI) }
    val animeMovies = remember(allMovies) { viewModel.getMoviesByCategory(MovieCategory.ANIME) }

    // Map history to movie items
    val continueWatchingItems = remember(watchHistory, allMovies) {
        watchHistory.mapNotNull { hist ->
            val movie = allMovies.find { it.id == hist.movieId }
            if (movie != null) movie to hist else null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZeedLogo()

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // VIP badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(ZeedGold4K, Color(0xFFFF9100))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VIP 4K",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                ),
                                color = CinemaBlack
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSearch,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CinemaSurfaceVariant)
                                .testTag("home_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CinemaSurfaceVariant)
                                .testTag("home_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Category Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MovieCategory.values().forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Surface(
                            onClick = {
                                viewModel.setCategory(cat)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) ZeedCrimson else CinemaSurfaceVariant,
                            border = if (!isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(CinemaCardBorder, CinemaCardBorder))
                            ) else null,
                            modifier = Modifier.testTag("category_chip_${cat.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = cat.iconEmoji, fontSize = 12.sp)
                                Text(
                                    text = cat.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Featured Hero Banner
            if (featuredMovie != null) {
                item {
                    HeroFeaturedSection(
                        movie = featuredMovie,
                        onPlayClick = {
                            viewModel.startPlaying(featuredMovie)
                        },
                        onInfoClick = {
                            viewModel.openMovieDetail(featuredMovie)
                        },
                        isInWatchlist = watchlistIds.contains(featuredMovie.id),
                        onToggleWatchlist = {
                            viewModel.toggleWatchlist(featuredMovie.id)
                        }
                    )
                }
            }

            // Continue Watching Section
            if (continueWatchingItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    SectionHeader(
                        title = "รับชมต่อ",
                        accentEmoji = "▶️"
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(continueWatchingItems) { (movie, hist) ->
                            val remainingMins = ((hist.totalDurationSeconds - hist.currentPositionSeconds) / 60).coerceAtLeast(1)
                            ContinueWatchingCard(
                                movie = movie,
                                progress = hist.progress,
                                remainingMinutes = remainingMins,
                                onResumeClick = {
                                    viewModel.startPlaying(movie, hist.currentPositionSeconds)
                                }
                            )
                        }
                    }
                }
            }

            // Trending Section (Top 10 4K)
            item {
                Spacer(modifier = Modifier.height(14.dp))
                SectionHeader(
                    title = "10 อันดับภาพยนตร์ยอดนิยมวันนี้",
                    accentEmoji = "🔥"
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(trendingMovies) { index, movie ->
                        MoviePosterCard(
                            movie = movie,
                            rankingNumber = index + 1,
                            onClick = {
                                viewModel.openMovieDetail(movie)
                            }
                        )
                    }
                }
            }

            // Cinema Releases (หนังชนโรง)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "หนังชนโรง คมชัดระดับ 4K",
                    accentEmoji = "🎬"
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cinemaNewMovies) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = {
                                viewModel.openMovieDetail(movie)
                            }
                        )
                    }
                }
            }

            // Thai Cinema & Horror (หนังไทย / ผีสยองขวัญ)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "หนังไทย & ตำนานความสยองขวัญ",
                    accentEmoji = "👻"
                )
                val combinedThaiHorror = (thaiMovies + horrorMovies).distinctBy { it.id }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(combinedThaiHorror) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = {
                                viewModel.openMovieDetail(movie)
                            }
                        )
                    }
                }
            }

            // Action & Sci-Fi Blockbusters
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "แอคชั่น & ไซไฟ ระห่ำเต็มพิกัด",
                    accentEmoji = "⚡"
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(actionMovies) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = {
                                viewModel.openMovieDetail(movie)
                            }
                        )
                    }
                }
            }

            // Anime & Series
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "อนิเมะ & ซีรีส์ 4K 60fps",
                    accentEmoji = "🎌"
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(animeMovies) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = {
                                viewModel.openMovieDetail(movie)
                            }
                        )
                    }
                }
            }
        }
    }
}
