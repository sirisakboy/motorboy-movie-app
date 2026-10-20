package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import com.example.model.MovieCategory
import com.example.ui.ZeedViewModel
import com.example.ui.components.MoviePosterCard
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    viewModel: ZeedViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(MovieCategory.ALL) }
    var selectedAudioFilter by remember { mutableStateOf("ทั้งหมด") } // "ทั้งหมด", "พากย์ไทย", "ซับไทย"
    var selectedResolutionFilter by remember { mutableStateOf("ทั้งหมด") } // "ทั้งหมด", "4K UHD", "1080p"

    val filteredMovies = remember(selectedCategory, selectedAudioFilter, selectedResolutionFilter) {
        val base = viewModel.getMoviesByCategory(selectedCategory)
        base.filter { movie ->
            val matchAudio = when (selectedAudioFilter) {
                "พากย์ไทย" -> movie.audio.contains("พากย์ไทย") || movie.audio.contains("เสียงไทย")
                "ซับไทย" -> movie.audio.contains("ซับไทย")
                else -> true
            }
            val matchRes = when (selectedResolutionFilter) {
                "4K UHD" -> movie.resolution.contains("4K")
                "1080p" -> movie.resolution.contains("1080p")
                else -> true
            }
            matchAudio && matchRes
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .statusBarsPadding()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 105.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "หมวดหมู่ภาพยนตร์ & ซีรีส์",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    // Categories Flow / Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MovieCategory.values().take(4).forEach { cat ->
                            val isSel = cat == selectedCategory
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = { Text("${cat.iconEmoji} ${cat.displayName}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ZeedCrimson,
                                    selectedLabelColor = Color.White,
                                    containerColor = CinemaSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MovieCategory.values().drop(4).forEach { cat ->
                            val isSel = cat == selectedCategory
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = { Text("${cat.iconEmoji} ${cat.displayName}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ZeedCrimson,
                                    selectedLabelColor = Color.White,
                                    containerColor = CinemaSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    // Audio & Resolution Sub-filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CinemaSurface)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ระบบเสียง:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            listOf("ทั้งหมด", "พากย์ไทย", "ซับไทย").forEach { aud ->
                                val active = aud == selectedAudioFilter
                                Text(
                                    text = aud,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (active) ZeedGold4K else TextSecondary,
                                    modifier = Modifier
                                        .clickable { selectedAudioFilter = aud }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${filteredMovies.size} เรื่อง",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ZeedCyanNeon
                        )
                    }
                }
            }

            // Movie Grid Items
            items(filteredMovies) { movie ->
                MoviePosterCard(
                    movie = movie,
                    onClick = {
                        viewModel.openMovieDetail(movie)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
