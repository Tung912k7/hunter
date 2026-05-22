package com.hackathon.hunter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.hunter.data.local.entity.HackathonEntity
import com.hackathon.hunter.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HackathonCard(
    hackathon: HackathonEntity,
    onCardClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Manage dynamic animation states when reported
    var isCollapsed by remember { mutableStateOf(false) }

    LaunchedEffect(hackathon.isReportedByUser) {
        if (hackathon.isReportedByUser) {
            // After 1.5s delay, trigger the height collapse
            delay(1500)
            isCollapsed = true
        } else {
            isCollapsed = false
        }
    }

    val alphaAnim by animateFloatAsState(
        targetValue = if (hackathon.isReportedByUser) 0.15f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "AlphaAnimation"
    )

    val heightAnim by animateDpAsState(
        targetValue = if (isCollapsed) 0.dp else Dp.Unspecified,
        animationSpec = tween(durationMillis = 500),
        label = "HeightAnimation"
    )

    if (heightAnim > 0.dp || !isCollapsed) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .then(
                    if (isCollapsed) Modifier.height(heightAnim) else Modifier
                )
                .alpha(alphaAnim)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    BorderStroke(1.dp, GlassCardBorder),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(GlassCardBg.copy(alpha = 0.7f))
                .clickable(enabled = !hackathon.isReportedByUser) { onCardClick() }
                .semantics(mergeDescendants = true) {
                    contentDescription = buildTalkBackDescription(hackathon)
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlatformBadge(platform = hackathon.platform)
                        PrizeTypeBadge(prizeType = hackathon.prizeType)
                    }

                    IconButton(
                        onClick = { if (!hackathon.isReportedByUser) onBookmarkClick() },
                        modifier = Modifier.size(48.dp) // Touch target minimum size
                    ) {
                        Icon(
                            imageVector = if (hackathon.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (hackathon.isBookmarked) "Bỏ lưu cuộc thi" else "Lưu cuộc thi",
                            tint = if (hackathon.isBookmarked) ElectricCyan else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title & Subtitle
                Text(
                    text = hackathon.title,
                    style = AppTypography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Host: ${hackathon.platform.replaceFirstChar { it.uppercase() }}",
                    style = AppTypography.bodyLarge,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        MetadataItem(
                            label = "Giải thưởng",
                            value = formatPrizeValue(hackathon.prizeValue, hackathon.prizeCurrency),
                            accentColor = getPrizeAccentColor(hackathon.prizeCurrency)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        MetadataItem(
                            label = "Hình thức",
                            value = if (hackathon.isOnline) "Online" else "In-Person"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        MetadataItem(
                            label = "Độ hợp lệ VN",
                            value = if (hackathon.isVietnamEligible) "Được tham gia" else "Bị cấm / Hạn chế",
                            valueColor = if (hackathon.isVietnamEligible) StatusGreenText else StatusRedText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        MetadataItem(
                            label = "Lượt báo cáo",
                            value = "${hackathon.reportCount} lượt"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tag Row & Report CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        maxItemsInEachRow = 3
                    ) {
                        TextBadge(text = hackathon.prizeCurrency)
                        if (hackathon.isOnline) {
                            TextBadge(text = "Online")
                        } else {
                            TextBadge(text = "In-Person")
                        }
                    }

                    Button(
                        onClick = { if (!hackathon.isReportedByUser) onReportClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusRedBg.copy(alpha = 0.4f),
                            contentColor = StatusRedText
                        ),
                        border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = StatusRedText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Báo cấm VN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Reported Overlay Banner
            if (hackathon.isReportedByUser) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(WarningRed, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Đã báo cáo cấm VN",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataItem(
    label: String,
    value: String,
    accentColor: Color? = null,
    valueColor: Color = TextPrimary
) {
    Column {
        Text(text = label, style = AppTypography.bodyMedium, color = TextMuted)
        Text(
            text = value,
            style = AppTypography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = accentColor ?: valueColor
        )
    }
}

@Composable
fun PlatformBadge(platform: String) {
    val (bgColor, textColor) = when (platform.lowercase()) {
        "devpost" -> DevpostBg to DevpostText
        "devfolio" -> DevfolioBg to DevfolioText
        "hackerearth" -> HackerEarthBg to HackerEarthText
        "gitcoin" -> GitcoinBg to GitcoinText
        "dorahacks" -> DoraHacksBg to DoraHacksText
        "bewater" -> BeWaterBg to BeWaterText
        else -> DarkBorder to TextSecondary
    }

    Box(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = platform.uppercase(),
            color = textColor,
            style = AppTypography.labelSmall
        )
    }
}

@Composable
fun PrizeTypeBadge(prizeType: String) {
    val (bgColor, textColor, strokeColor) = when (prizeType.lowercase()) {
        "fiat" -> Triple(AssetFiatBg, AssetFiatBorder, AssetFiatBorder)
        "crypto" -> Triple(AssetCryptoBg, TextPrimary, AssetCryptoBorder)
        else -> Triple(GlassCardBg, TextPrimary, DarkBorder)
    }

    Box(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, strokeColor), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = prizeType.uppercase(),
            color = textColor,
            style = AppTypography.labelSmall
        )
    }
}

@Composable
fun TextBadge(text: String) {
    Box(
        modifier = Modifier
            .background(DarkBorder, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatPrizeValue(value: Double, currency: String): String {
    val formattedVal = if (value % 1 == 0.0) {
        String.format("%,.0f", value)
    } else {
        String.format("%,.2f", value)
    }
    return when (currency.uppercase()) {
        "USD" -> "$$formattedVal"
        "ETH" -> "Ξ$formattedVal"
        "SOL" -> "⟁$formattedVal"
        "TON" -> "💎$formattedVal"
        else -> "$formattedVal $currency"
    }
}

fun getPrizeAccentColor(currency: String): Color {
    return when (currency.uppercase()) {
        "USD" -> AssetFiatBorder
        "USDC", "USDT" -> DevpostText // USDC Blue/Cyan
        "ETH" -> Color(0xFF627EEA)
        "SOL" -> Color(0xFF14F195)
        "TON" -> Color(0xFF0098EA)
        else -> AssetCryptoBorder
    }
}

private fun buildTalkBackDescription(hackathon: HackathonEntity): String {
    val prizeStr = formatPrizeValue(hackathon.prizeValue, hackathon.prizeCurrency)
    val eligibilityStr = if (hackathon.isVietnamEligible) "Hợp lệ cho Việt Nam" else "Không hợp lệ cho Việt Nam"
    val reportedStr = if (hackathon.isReportedByUser) "Đã báo cáo" else "Chưa báo cáo"
    return "Cuộc thi: ${hackathon.title}. Nền tảng: ${hackathon.platform}. Giải thưởng: $prizeStr. $eligibilityStr. Trạng thái: $reportedStr."
}
