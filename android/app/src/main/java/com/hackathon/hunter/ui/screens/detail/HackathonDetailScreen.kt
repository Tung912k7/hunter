package com.hackathon.hunter.ui.screens.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.hunter.ui.components.*
import com.hackathon.hunter.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HackathonDetailScreen(
    viewModel: HackathonDetailViewModel,
    onBackClick: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hackathon by viewModel.hackathon.collectAsState()
    val reportSuccess by viewModel.reportSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(reportSuccess) {
        if (reportSuccess) {
            viewModel.clearReportSuccess()
            if (showBackButton) {
                onBackClick()
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            hackathon?.let { item ->
                // Sticky Bottom Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ObsidianBg,
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Bookmark Toggle (56dp square)
                        IconButton(
                            onClick = { viewModel.toggleBookmark(!item.isBookmarked) },
                            modifier = Modifier
                                .size(56.dp)
                                .border(BorderStroke(1.dp, DarkBorder), shape = RoundedCornerShape(12.dp))
                                .background(GlassCardBg.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (item.isBookmarked) "Bỏ lưu cuộc thi" else "Lưu cuộc thi",
                                tint = if (item.isBookmarked) ElectricCyan else TextSecondary
                            )
                        }

                        // Report Button (120dp width)
                        Button(
                            onClick = { showReportDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusRedBg.copy(alpha = 0.4f),
                                contentColor = StatusRedText
                            ),
                            border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(135.dp)
                                .height(56.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = StatusRedText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Báo cấm VN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Register/Apply Now CTA (fills remaining space)
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = ObsidianBg
                            ),
                            contentPadding = PaddingValues(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(NeonTeal, ElectricCyan)),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ĐĂNG KÝ NGAY",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = ObsidianBg
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        hackathon?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Banner Header (220dp height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0B192C),
                                    Color(0xFF1E5F74),
                                    Color(0xFF1D1A39)
                                )
                            )
                        )
                ) {
                    // Linear mask overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        ObsidianBg.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    // Hero Title lower 40%
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        PlatformBadge(platform = item.platform)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.title,
                            style = AppTypography.titleLarge,
                            fontSize = 24.sp,
                            lineHeight = 30.sp,
                            color = Color.White
                        )
                    }

                    // Floating Controls on top
                    if (showBackButton) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .size(48.dp)
                                .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(24.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${item.title} - Săn ngay tại: ${item.url}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ cuộc thi"))
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(24.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ",
                            tint = Color.White
                        )
                    }
                }

                // Detail Content Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Main Prize Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassCardBg.copy(alpha = 0.7f)),
                        border = BorderStroke(1.dp, GlassCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Tổng giải thưởng", style = AppTypography.bodyMedium, color = TextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = getPrizeAccentColor(item.prizeCurrency),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formatPrizeValue(item.prizeValue, item.prizeCurrency),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            PrizeTypeBadge(prizeType = item.prizeType)
                        }
                    }

                    // Vietnam Eligibility Status
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isVietnamEligible) StatusGreenBg.copy(alpha = 0.3f) else StatusRedBg.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (item.isVietnamEligible) StatusGreenText.copy(alpha = 0.4f) else StatusRedText.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.isVietnamEligible) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (item.isVietnamEligible) StatusGreenText else StatusRedText,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (item.isVietnamEligible) "Hợp lệ cho Việt Nam" else "Không hợp lệ / Bị cấm",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isVietnamEligible) StatusGreenText else StatusRedText
                                )
                                Text(
                                    text = if (item.isVietnamEligible) {
                                        "Chưa phát hiện điều khoản cấm Việt Nam tham gia trong quy chế."
                                    } else {
                                        "Có thông tin quy chế cấm nhà phát triển Việt Nam tham gia."
                                    },
                                    style = AppTypography.bodyLarge,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Warning / Crowdsourced Moderation Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassCardBg.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusOrangeText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Báo cáo cộng đồng (${item.reportCount} lượt)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nếu bạn phát hiện quy chế cuộc thi này cấm nhà phát triển có quốc tịch hoặc cư trú tại Việt Nam tham gia, hãy nhấn nút 'Báo cấm VN' ở góc dưới để cảnh báo cộng đồng.",
                                style = AppTypography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }

                    // Description Section
                    if (!item.description.isNullOrBlank()) {
                        Text(
                            text = "Mô tả cuộc thi",
                            style = AppTypography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = item.description,
                            style = AppTypography.bodyLarge,
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                    }

                    // Timeline details
                    Text(
                        text = "Thời gian",
                        style = AppTypography.titleMedium,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Bắt đầu", style = AppTypography.bodyMedium, color = TextMuted)
                            Text(text = item.startDate ?: "--/--/----", style = AppTypography.bodyLarge, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Kết thúc", style = AppTypography.bodyMedium, color = TextMuted)
                            Text(text = item.endDate ?: "--/--/----", style = AppTypography.bodyLarge, color = Color.White)
                        }
                    }

                    // Rules link
                    if (!item.rulesUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.rulesUrl))
                                context.startActivity(intent)
                            },
                            border = BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Xem quy chế chính thức", style = AppTypography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Confirmation Dialog
    if (showReportDialog) {
        ReportConfirmationDialog(
            onConfirm = {
                viewModel.reportHackathon()
                showReportDialog = false
            },
            onDismiss = { showReportDialog = false }
        )
    }
}
