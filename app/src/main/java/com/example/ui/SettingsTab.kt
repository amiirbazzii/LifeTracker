package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun SettingsTab(
    userPoints: Int,
    categoriesCount: Int,
    routinesCount: Int,
    rewardsCount: Int,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(DesignTokens.PaddingLarge),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMedium)
    ) {
        Text(
            text = "APP SUMMARY & METRICS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = Zinc500,
                fontWeight = FontWeight.Bold,
                letterSpacing = DesignTokens.LetterSpacingWide
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingSmall)
        ) {
            MetricRow(label = "TOTAL POINTS", value = "$userPoints PTS")
            MetricRow(label = "ACTIVE CATEGORIES", value = categoriesCount.toString())
            MetricRow(label = "ROUTINES TRACKED", value = routinesCount.toString())
            MetricRow(label = "REWARDS CREATED", value = rewardsCount.toString())
            MetricRow(label = "DATA STORAGE", value = "LOCAL DB")
        }

        Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

        Text(
            text = "DATA MANAGEMENT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = Zinc500,
                fontWeight = FontWeight.Bold,
                letterSpacing = DesignTokens.LetterSpacingWide
            )
        )

        Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

        Button(
            onClick = onResetClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.ButtonHeight)
                .testTag("hub_reset_system_button"),
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Red
            ),
            border = BorderStroke(DesignTokens.StrokeThick, Color.Red.copy(alpha = 0.5f))
        ) {
            Text(
                text = "RESET ALL APP DATA",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = DesignTokens.LetterSpacingWide
                )
            )
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                DesignTokens.StrokeThin,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .padding(DesignTokens.PaddingMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = Zinc400
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}
