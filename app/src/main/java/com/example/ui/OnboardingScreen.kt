package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    onInitialize: (Int, String) -> Unit
) {
    var yearsText by remember { mutableStateOf("5") }
    var goalText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(DesignTokens.PaddingExtraLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Matrix Logo
        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(64.dp)
                .testTag("app_logo_image")
        )

        Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

        // Stark Brutalist Minimal Title
        Text(
            text = DesignTokens.APP_TITLE,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = DesignTokens.LetterSpacingHeadline,
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
        
        Text(
            text = DesignTokens.ONBOARDING_SUBTITLE,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = DesignTokens.LetterSpacingWide,
                fontWeight = FontWeight.Bold
            ),
            color = Zinc500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(DesignTokens.ButtonHeight)) // 48.dp

        // Geometric boundary box (0.dp rounded corners)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary),
                    RoundedCornerShape(DesignTokens.PaddingZero)
                )
                .background(MaterialTheme.colorScheme.background)
                .padding(DesignTokens.PaddingExtraLarge)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = DesignTokens.ONBOARDING_PROMPT_TITLE,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = DesignTokens.LetterSpacingExtraWide
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

                Text(
                    text = DesignTokens.ONBOARDING_PROMPT_DESC,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Zinc400,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(DesignTokens.PaddingDoubleExtraLarge))

                // Selector row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(DesignTokens.StepButtonSize)
                            .border(
                                BorderStroke(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary),
                                RoundedCornerShape(DesignTokens.PaddingZero)
                            )
                            .clickable {
                                val current = yearsText.toIntOrNull() ?: 5
                                if (current > 1) {
                                    yearsText = (current - 1).toString()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TextField(
                        value = yearsText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 2) {
                                yearsText = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .width(80.dp)
                            .testTag("years_input_field")
                    )

                    Box(
                        modifier = Modifier
                            .size(DesignTokens.StepButtonSize)
                            .border(
                                BorderStroke(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary),
                                RoundedCornerShape(DesignTokens.PaddingZero)
                            )
                            .clickable {
                                val current = yearsText.toIntOrNull() ?: 5
                                if (current < 50) {
                                    yearsText = (current + 1).toString()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                Text(
                    text = DesignTokens.ONBOARDING_CONFIG_LABEL,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = DesignTokens.LetterSpacingWide,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Zinc500
                )

                Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

                Text(
                    text = "MAIN GOAL (OPTIONAL)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = DesignTokens.LetterSpacingWide,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Zinc500
                )

                Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    placeholder = {
                        Text(
                            text = DesignTokens.GOAL_FIELD_PLACEHOLDER,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Zinc500,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_goal_input"),
                    shape = RoundedCornerShape(DesignTokens.PaddingZero),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GridLevel4,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))
                    Text(
                        text = "ERROR: $msg",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(DesignTokens.PaddingDoubleExtraLarge))

                Button(
                    onClick = {
                        val years = yearsText.toIntOrNull()
                        if (years == null || years <= 0) {
                            errorMessage = DesignTokens.ONBOARDING_ERROR_POSITIVE
                        } else {
                            onInitialize(years, goalText.trim())
                        }
                    },
                    shape = RoundedCornerShape(DesignTokens.PaddingZero),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GridLevel4, // Neon Green contrast button!
                        contentColor = MonochromeBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DesignTokens.ButtonHeight)
                        .testTag("initialize_button")
                ) {
                    Text(
                        text = DesignTokens.ONBOARDING_INIT_BUTTON,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = DesignTokens.LetterSpacingWide,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}
