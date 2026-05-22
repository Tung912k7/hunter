package com.hackathon.hunter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.hunter.ui.theme.*

@Composable
fun ReportConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF1A1D24),
        modifier = modifier.border(
            BorderStroke(1.dp, WarningRed.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(20.dp)
        ),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = WarningRed,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Xác nhận báo cáo",
                style = AppTypography.titleMedium,
                color = Color.White
            )
        },
        text = {
            Text(
                text = "Bạn có chắc chắn muốn báo cáo cuộc thi này cấm nhà phát triển Việt Nam? Điều này sẽ ẩn cuộc thi khỏi danh sách hiển thị của bạn.",
                style = AppTypography.bodyLarge,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Báo cáo", style = AppTypography.labelSmall)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Hủy",
                    style = AppTypography.labelSmall,
                    color = TextMuted
                )
            }
        }
    )
}
