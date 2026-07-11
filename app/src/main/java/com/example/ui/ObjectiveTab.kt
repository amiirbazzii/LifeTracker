package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ObjectiveTab(
    currentGoal: String,
    onSaveGoal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var goalText by remember(currentGoal) { mutableStateOf(currentGoal) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(DesignTokens.PaddingLarge)
    ) {
        Text(
            text = "CURRENT ACTIVE OBJECTIVE:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = Zinc500,
                fontWeight = FontWeight.Bold,
                letterSpacing = DesignTokens.LetterSpacingWide
            )
        )

        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    DesignTokens.StrokeThick,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(DesignTokens.PaddingZero)
                )
                .background(MaterialTheme.colorScheme.surface)
                .padding(DesignTokens.PaddingMedium)
        ) {
            Text(
                text = if (currentGoal.isNotBlank()) currentGoal.uppercase() else "NO CORE OBJECTIVE YET ASSIGNED",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (currentGoal.isNotBlank()) MaterialTheme.colorScheme.primary else GridLevel4,
                    lineHeight = 20.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

        Text(
            text = "RECONFIGURE GOAL PROTOCOL:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = Zinc500,
                fontWeight = FontWeight.Bold,
                letterSpacing = DesignTokens.LetterSpacingWide
            )
        )

        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

        OutlinedTextField(
            value = goalText,
            onValueChange = { goalText = it },
            placeholder = {
                Text(
                    text = "e.g., CONTINUOUS ITERATION // MINIMAL DISTRACTION",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Zinc500,
                        fontFamily = FontFamily.Monospace
                    )
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hub_goal_input_field"),
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GridLevel4,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            singleLine = false,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

        Button(
            onClick = { onSaveGoal(goalText.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.ButtonHeight)
                .testTag("hub_save_goal_button"),
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            colors = ButtonDefaults.buttonColors(
                containerColor = GridLevel4,
                contentColor = MonochromeBlack
            )
        ) {
            Text(
                text = "COMMIT TO PROFILE",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = DesignTokens.LetterSpacingWide
                )
            )
        }
    }
}
