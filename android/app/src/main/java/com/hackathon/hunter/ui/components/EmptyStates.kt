package com.hackathon.hunter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.hunter.ui.theme.*

@Composable
fun EmptyStateView(
    title: String,
    description: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with Gradient Tint
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBorder, GlassCardBg)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = title,
            style = AppTypography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = description,
            style = AppTypography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button
        OutlinedButton(
            onClick = onActionClick,
            border = BorderStroke(1.dp, NeonTeal),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ElectricCyan
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = actionLabel,
                fontSize = 14.sp,
                style = AppTypography.labelSmall
            )
        }
    }
}
