package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.ZeedViewModel
import com.example.ui.theme.*

@Composable
fun MainScreen(
    viewModel: ZeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val selectedDetailMovie by viewModel.selectedDetailMovie.collectAsState()

    // Handle back button: if not on Home, return to Home
    BackHandler(enabled = currentTab != AppTab.HOME && playerState.movie == null && selectedDetailMovie == null) {
        viewModel.setTab(AppTab.HOME)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CinemaDarkBackground,
            bottomBar = {
                // Hide bottom nav if player is active
                if (playerState.movie == null) {
                    NavigationBar(
                        containerColor = CinemaSurface,
                        contentColor = TextPrimary,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("bottom_nav_bar")
                    ) {
                        val items = listOf(
                            Triple(AppTab.HOME, "หน้าแรก", Icons.Default.Home),
                            Triple(AppTab.EXPLORE, "หมวดหมู่", Icons.Default.Category),
                            Triple(AppTab.SEARCH, "ค้นหา", Icons.Default.Search),
                            Triple(AppTab.WATCHLIST, "ของฉัน", Icons.Default.Bookmark),
                            Triple(AppTab.PROFILE, "โปรไฟล์", Icons.Default.Person)
                        )

                        items.forEach { (tab, label, icon) ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.setTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = ZeedGold4K,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = ZeedCrimson
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSearch = { viewModel.setTab(AppTab.SEARCH) }
                    )
                    AppTab.EXPLORE -> ExploreScreen(viewModel = viewModel)
                    AppTab.SEARCH -> SearchScreen(viewModel = viewModel)
                    AppTab.WATCHLIST -> WatchlistScreen(viewModel = viewModel)
                    AppTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                }
            }
        }

        // Movie Detail Sheet Overlay
        selectedDetailMovie?.let { movie ->
            MovieDetailSheet(
                movie = movie,
                viewModel = viewModel,
                onClose = { viewModel.closeMovieDetail() },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Immersive Player Overlay
        if (playerState.movie != null) {
            PlayerScreen(
                viewModel = viewModel,
                playerState = playerState,
                onClose = { viewModel.closePlayer() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
