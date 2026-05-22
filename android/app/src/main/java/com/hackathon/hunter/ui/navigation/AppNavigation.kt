package com.hackathon.hunter.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hackathon.hunter.ui.screens.detail.HackathonDetailScreen
import com.hackathon.hunter.ui.screens.detail.HackathonDetailViewModel
import com.hackathon.hunter.ui.screens.list.HackathonListScreen
import com.hackathon.hunter.ui.screens.list.HackathonListViewModel
import com.hackathon.hunter.ui.theme.AppTypography
import com.hackathon.hunter.ui.theme.TextMuted

@Composable
fun AppNavigation(
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    if (isExpanded) {
        // Dual Pane Layout (Tablet/Foldable)
        var selectedId by remember { mutableStateOf<Int?>(null) }
        val listViewModel: HackathonListViewModel = hiltViewModel()
        val hackathons by listViewModel.hackathons.collectAsState()

        // Auto-select first item if selection is null and list is populated
        LaunchedEffect(hackathons) {
            if (selectedId == null && hackathons.isNotEmpty()) {
                selectedId = hackathons.first().id
            }
        }

        Row(modifier = modifier.fillMaxSize()) {
            // Left Pane (40% width)
            Box(modifier = Modifier.fillMaxHeight().weight(0.4f)) {
                HackathonListScreen(
                    viewModel = listViewModel,
                    onNavigateToDetail = { id -> selectedId = id }
                )
            }

            // Vertical separator
            Spacer(modifier = Modifier.width(1.dp))

            // Right Pane (60% width)
            Box(
                modifier = Modifier.fillMaxHeight().weight(0.6f),
                contentAlignment = Alignment.Center
            ) {
                selectedId?.let { id ->
                    val detailViewModel: HackathonDetailViewModel = hiltViewModel(
                        key = "detail_$id"
                    )
                    LaunchedEffect(id) {
                        detailViewModel.setHackathonId(id)
                    }
                    HackathonDetailScreen(
                        viewModel = detailViewModel,
                        onBackClick = {},
                        showBackButton = false
                    )
                } ?: run {
                    Text(
                        text = "Chọn một cuộc thi để xem chi tiết",
                        style = AppTypography.bodyLarge,
                        color = TextMuted
                    )
                }
            }
        }
    } else {
        // Single Pane Layout (Mobile)
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "list",
            modifier = modifier.fillMaxSize()
        ) {
            composable("list") {
                val listViewModel: HackathonListViewModel = hiltViewModel()
                HackathonListScreen(
                    viewModel = listViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate("detail/$id")
                    }
                )
            }
            composable(
                route = "detail/{hackathonId}",
                arguments = listOf(navArgument("hackathonId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("hackathonId") ?: -1
                val detailViewModel: HackathonDetailViewModel = hiltViewModel()
                LaunchedEffect(id) {
                    detailViewModel.setHackathonId(id)
                }
                HackathonDetailScreen(
                    viewModel = detailViewModel,
                    onBackClick = { navController.popBackStack() },
                    showBackButton = true
                )
            }
        }
    }
}
