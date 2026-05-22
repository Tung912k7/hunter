package com.hackathon.hunter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.hunter.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismissRequest: () -> Unit,
    vietnamOnly: Boolean,
    onVietnamOnlyChange: (Boolean) -> Unit,
    prizeType: String, // "all", "fiat", "crypto"
    onPrizeTypeChange: (String) -> Unit,
    selectedPlatforms: Set<String>,
    onPlatformToggle: (String) -> Unit,
    isOnline: Boolean?, // null (All), true (Online), false (In-Person)
    onOnlineChange: (Boolean?) -> Unit,
    minPrizeValue: Double,
    onMinPrizeValueChange: (Double) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onResetAll: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ObsidianBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) },
        modifier = modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bộ lọc cuộc thi",
                    style = AppTypography.titleLarge,
                    color = Color.White
                )

                TextButton(
                    onClick = onResetAll,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp) // Touch target size
                ) {
                    Text(
                        text = "Thiết lập lại",
                        style = AppTypography.labelSmall,
                        color = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keywords custom tag entry
            Text(
                text = "TÌM KIẾM TỪ KHÓA",
                style = AppTypography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Nhập từ khóa...", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = GlassCardBg.copy(alpha = 0.5f),
                    unfocusedContainerColor = GlassCardBg.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Vietnam Eligibility
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.dp,
                            if (vietnamOnly) ElectricCyan.copy(alpha = 0.4f) else DarkBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (vietnamOnly) ElectricCyan.copy(alpha = 0.05f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🇻🇳 Chỉ hiển thị Việt Nam được tham gia",
                        style = AppTypography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Ẩn các cuộc thi bị báo cáo hạn chế quốc tịch Việt Nam.",
                        style = AppTypography.bodyMedium,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = vietnamOnly,
                    onCheckedChange = onVietnamOnlyChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElectricCyan,
                        checkedTrackColor = ElectricCyan.copy(alpha = 0.2f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Prize Type Segmented Control
            Text(
                text = "LOẠI GIẢI THƯỞNG",
                style = AppTypography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassCardBg, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val options = listOf("all" to "Tất cả", "fiat" to "Tiền mặt (Fiat)", "crypto" to "Crypto / Token")
                options.forEach { (type, label) ->
                    val isSelected = prizeType.lowercase() == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(listOf(NeonTeal, ElectricCyan))
                                } else {
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onPrizeTypeChange(type) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) ObsidianBg else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Platform Selection (3x2 grid of custom Checkbox cards)
            Text(
                text = "NỀN TẢNG TỔ CHỨC",
                style = AppTypography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            val platformsList = listOf(
                "devpost" to DevpostBg,
                "devfolio" to DevfolioBg,
                "hackerearth" to HackerEarthBg,
                "gitcoin" to GitcoinBg,
                "dorahacks" to DoraHacksBg,
                "bewater" to BeWaterBg
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            val (platformName, brandColor) = platformsList[index]
                            val isChecked = selectedPlatforms.contains(platformName)
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isChecked) brandColor else DarkBorder
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        if (isChecked) brandColor.copy(alpha = 0.12f) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onPlatformToggle(platformName) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = platformName.replaceFirstChar { it.uppercase() },
                                    color = if (isChecked) Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Event Format Selector
            Text(
                text = "HÌNH THỨC TỔ CHỨC",
                style = AppTypography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null to "Tất cả", true to "Online", false to "In-Person").forEach { (onlineVal, label) ->
                    val isSelected = isOnline == onlineVal
                    FilterChip(
                        selected = isSelected,
                        onClick = { onOnlineChange(onlineVal) },
                        label = { Text(text = label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.15f),
                            selectedLabelColor = ElectricCyan,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ElectricCyan else DarkBorder,
                            selectedBorderColor = ElectricCyan,
                            selectedBorderWidth = 1.dp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 5: Minimum Cash Value Slider (Discrete steps $0 to $50,000+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GIÁ TRỊ GIẢI THƯỞNG TỐI THIỂU",
                    style = AppTypography.labelSmall,
                    color = TextMuted
                )
                Box(
                    modifier = Modifier
                        .background(ElectricCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (minPrizeValue >= 50000.0) "$50,000+" else "$${String.format("%,.0f", minPrizeValue)}",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = minPrizeValue.toFloat(),
                onValueChange = { onMinPrizeValueChange(it.toDouble()) },
                valueRange = 0f..50000f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = ElectricCyan,
                    activeTrackColor = ElectricCyan,
                    inactiveTrackColor = DarkBorder
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
