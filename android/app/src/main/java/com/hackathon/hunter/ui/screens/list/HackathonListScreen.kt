package com.hackathon.hunter.ui.screens.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.hunter.data.local.entity.HackathonEntity
import com.hackathon.hunter.ui.components.*
import com.hackathon.hunter.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackathonListScreen(
    viewModel: HackathonListViewModel,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hackathons by viewModel.hackathons.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val vietnamOnly by viewModel.vietnamOnly.collectAsState()
    val prizeType by viewModel.prizeType.collectAsState()
    val selectedPlatforms by viewModel.selectedPlatforms.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val minPrizeValue by viewModel.minPrizeValue.collectAsState()
    val showBookmarksOnly by viewModel.showBookmarksOnly.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var reportingHackathonId by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle showing error messages in a Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearErrorMessage()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianBg)
                    .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
            ) {
                // Title and Refresh Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Hackathon Hunter",
                        style = AppTypography.titleLarge,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới danh sách",
                            tint = ElectricCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar and Bookmarks filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Tìm kiếm cuộc thi...", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = GlassCardBg.copy(alpha = 0.3f),
                            unfocusedContainerColor = GlassCardBg.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconToggleButton(
                        checked = showBookmarksOnly,
                        onCheckedChange = { viewModel.setShowBookmarksOnly(it) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (showBookmarksOnly) ElectricCyan.copy(alpha = 0.15f) else GlassCardBg.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = if (showBookmarksOnly) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Lọc chỉ hiển thị Đã lưu",
                            tint = if (showBookmarksOnly) ElectricCyan else TextSecondary
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isFilterSheetOpen = true },
                containerColor = ElectricCyan,
                contentColor = ObsidianBg
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Mở bộ lọc",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = ElectricCyan,
                    trackColor = DarkBorder
                )
            }

            if (hackathons.isEmpty()) {
                val emptyTitle = if (showBookmarksOnly) "Không có cuộc thi đã lưu" else "Không tìm thấy cuộc thi"
                val emptyDesc = if (showBookmarksOnly) {
                    "Lưu cuộc thi bạn thích để theo dõi trạng thái và thời gian."
                } else {
                    "Thử thay đổi từ khóa hoặc điều chỉnh các bộ lọc để tìm kiếm thêm."
                }
                val emptyAction = if (showBookmarksOnly) "Xem tất cả cuộc thi" else "Thiết lập lại bộ lọc"
                val emptyActionClick = {
                    if (showBookmarksOnly) {
                        viewModel.setShowBookmarksOnly(false)
                    } else {
                        viewModel.resetFilters()
                    }
                }

                EmptyStateView(
                    title = emptyTitle,
                    description = emptyDesc,
                    actionLabel = emptyAction,
                    onActionClick = emptyActionClick,
                    icon = if (showBookmarksOnly) Icons.Default.BookmarkBorder else Icons.Default.Search
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = hackathons,
                        key = { it.id }
                    ) { item ->
                        // Wrap in AnimatedVisibility to fade/shrink item when reported
                        AnimatedVisibility(
                            visible = !item.isReportedByUser,
                            exit = fadeOut(animationSpec = tween(1500)) + shrinkVertically(animationSpec = tween(1500, delayMillis = 1500))
                        ) {
                            HackathonCard(
                                hackathon = item,
                                onCardClick = { onNavigateToDetail(item.id) },
                                onBookmarkClick = { viewModel.toggleBookmark(item.id, !item.isBookmarked) },
                                onReportClick = { reportingHackathonId = item.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Filters Bottom Sheet Modal
    if (isFilterSheetOpen) {
        FilterBottomSheet(
            onDismissRequest = { isFilterSheetOpen = false },
            vietnamOnly = vietnamOnly,
            onVietnamOnlyChange = { viewModel.setVietnamOnly(it) },
            prizeType = prizeType,
            onPrizeTypeChange = { viewModel.setPrizeType(it) },
            selectedPlatforms = selectedPlatforms,
            onPlatformToggle = { viewModel.togglePlatform(it) },
            isOnline = isOnline,
            onOnlineChange = { viewModel.setOnline(it) },
            minPrizeValue = minPrizeValue,
            onMinPrizeValueChange = { viewModel.setMinPrizeValue(it) },
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            onResetAll = { viewModel.resetFilters() }
        )
    }

    // Report Confirmation Dialog
    reportingHackathonId?.let { id ->
        ReportConfirmationDialog(
            onConfirm = {
                viewModel.reportHackathon(id)
                reportingHackathonId = null
                scope.launch {
                    snackbarHostState.showSnackbar("Đã gửi báo cáo thành công.")
                }
            },
            onDismiss = { reportingHackathonId = null }
        )
    }
}
