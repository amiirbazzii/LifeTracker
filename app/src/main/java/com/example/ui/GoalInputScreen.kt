package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GoalInputScreen(
    currentGoal: String,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignTokens.PaddingExtraLarge)
    ) {
        // Goal page header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DesignTokens.HEADER_GOAL_PROTOCOL,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = DesignTokens.LetterSpacingUltraWide
                ),
                color = MaterialTheme.colorScheme.primary
            )

            // Minimalist exit box matching the reset box style
            Box(
                modifier = Modifier
                    .size(DesignTokens.ControlBoxSize)
                    .border(
                        DesignTokens.StrokeMedium,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(DesignTokens.PaddingZero)
                    )
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Goal Input",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.PaddingExtraLarge))

        Text(
            text = DesignTokens.GOAL_INPUT_DESCRIPTION,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = Zinc400
        )

        Spacer(modifier = Modifier.height(DesignTokens.PaddingDoubleExtraLarge))

        // Custom Outlined TextField designed with brutalist stark lines
        OutlinedTextField(
            value = goalText,
            onValueChange = { goalText = it },
            placeholder = {
                Text(
                    text = DesignTokens.GOAL_FIELD_PLACEHOLDER,
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
                .testTag("goal_input_field"),
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

        Spacer(modifier = Modifier.height(DesignTokens.PaddingDoubleExtraLarge))

        // Geometric Brutalist Save Button
        Button(
            onClick = { onSave(goalText.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.ButtonHeight)
                .testTag("save_goal_button"),
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            colors = ButtonDefaults.buttonColors(
                containerColor = GridLevel4,
                contentColor = MonochromeBlack
            )
        ) {
            Text(
                text = DesignTokens.GOAL_COMMIT,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = DesignTokens.LetterSpacingWide
                )
            )
        }

        Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

        // Cancel/Back Text Button
        TextButton(
            onClick = { onBack() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = DesignTokens.GOAL_ABORT,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Zinc500
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        var showResetAlert by remember { mutableStateOf(false) }

        Button(
            onClick = { showResetAlert = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.ButtonHeight)
                .testTag("reset_button_goal_screen"),
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Red
            ),
            border = BorderStroke(DesignTokens.StrokeMedium, Color.Red.copy(alpha = 0.5f))
        ) {
            Text(
                text = "RESET ALL PROTOCOLS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = DesignTokens.LetterSpacingWide
                )
            )
        }

        if (showResetAlert) {
            AlertDialog(
                onDismissRequest = { showResetAlert = false },
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                title = {
                    Text(
                        text = DesignTokens.RESET_DIALOG_TITLE,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                },
                text = {
                    Text(
                        text = DesignTokens.RESET_DIALOG_TEXT,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showResetAlert = false
                            onReset()
                        }
                    ) {
                        Text(
                            DesignTokens.RESET_CONFIRM,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetAlert = false }) {
                        Text(
                            DesignTokens.RESET_CANCEL,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            )
        }
    }
}
