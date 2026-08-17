package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    initialGoal: String = "",
    initialYears: Int = 5,
    initialStep: Int = 1,
    canDismiss: Boolean = false,
    onInitialize: (Int, String) -> Unit,
    onSkip: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(initialStep) }
    var goalText by remember { mutableStateOf(initialGoal) }
    var yearsText by remember { mutableStateOf(initialYears.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val isGoalValid = goalText.trim().isNotBlank()
    val parsedYears = yearsText.toIntOrNull()
    val isYearsValid = parsedYears != null && parsedYears in 1..50

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = DesignTokens.PaddingLarge)
                .padding(top = DesignTokens.PaddingSmall, bottom = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (canDismiss) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = DesignTokens.PaddingSmall),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("dismiss_onboarding_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Zinc400
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
            }

            // App Logo
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(48.dp)
                    .testTag("app_logo_image")
            )

            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

            // Stark Brutalist Header
            Text(
                text = DesignTokens.APP_TITLE,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = DesignTokens.LetterSpacingHeadline,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

            Text(
                text = if (currentStep == 1) "STEP 1 · YOUR GOAL" else "STEP 2 · TIMELINE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = DesignTokens.LetterSpacingWide,
                    fontWeight = FontWeight.Bold
                ),
                color = GridLevel4,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

            // Animated transition between Step 1 and Step 2
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "OnboardingStepTransition"
            ) { step ->
                if (step == 1) {
                    // ==================== STEP 1: ULTIMATE GOAL ====================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Educational Explanation Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(DesignTokens.StrokeMedium, Zinc700),
                                    RoundedCornerShape(DesignTokens.PaddingZero)
                                )
                                .background(Color(0xFF0D0D0D))
                                .padding(DesignTokens.PaddingMedium)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = GridLevel4,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "WHAT IS THIS?",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = DesignTokens.LetterSpacingWide
                                        ),
                                        color = MonochromeWhite
                                    )
                                }

                                Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

                                Text(
                                    text = "Your primary long-term target. Your routines and matrix will align with it.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    ),
                                    color = Zinc400
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                        // Goal Input Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(
                                        if (isGoalValid) DesignTokens.StrokeThick else DesignTokens.StrokeMedium,
                                        if (isGoalValid) GridLevel4 else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    ),
                                    RoundedCornerShape(DesignTokens.PaddingZero)
                                )
                                .background(Color.Black)
                                .padding(DesignTokens.PaddingMedium)
                        ) {
                            Column {
                                Text(
                                    text = "ENTER GOAL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = DesignTokens.LetterSpacingWide
                                    ),
                                    color = if (isGoalValid) GridLevel4 else MonochromeWhite
                                )

                                Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                                OutlinedTextField(
                                    value = goalText,
                                    onValueChange = {
                                        goalText = it
                                        errorMessage = null
                                    },
                                    placeholder = {
                                        Text(
                                            text = "e.g. Launch business, master design, get fit",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Zinc500,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = MonochromeWhite,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_goal_input"),
                                    shape = RoundedCornerShape(DesignTokens.PaddingZero),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GridLevel4,
                                        unfocusedBorderColor = Zinc700,
                                        focusedContainerColor = Color(0xFF0D0D0D),
                                        unfocusedContainerColor = Color(0xFF0D0D0D)
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        focusManager.clearFocus()
                                        if (isGoalValid) {
                                            currentStep = 2
                                        }
                                    }),
                                    singleLine = false,
                                    maxLines = 3
                                )
                            }
                        }

                        errorMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                            Text(
                                text = "⚠️ $msg",
                                color = Color(0xFFFF5252),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                    }
                } else {
                    // ==================== STEP 2: TIMELINE ====================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Summary of Goal entered in Step 1
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(DesignTokens.StrokeMedium, GridLevel4.copy(alpha = 0.6f)),
                                    RoundedCornerShape(DesignTokens.PaddingZero)
                                )
                                .background(Color(0xFF0D0D0D))
                                .padding(DesignTokens.PaddingMedium)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "GOAL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = DesignTokens.LetterSpacingWide
                                        ),
                                        color = GridLevel4
                                    )

                                    Text(
                                        text = "EDIT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = DesignTokens.LetterSpacingWide
                                        ),
                                        color = Zinc400,
                                        modifier = Modifier.clickable { currentStep = 1 }
                                    )
                                }

                                Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

                                Text(
                                    text = goalText.trim(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = MonochromeWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                        // Timeline Selector Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                    RoundedCornerShape(DesignTokens.PaddingZero)
                                )
                                .background(Color.Black)
                                .padding(DesignTokens.PaddingMedium)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "YEARS TO ACHIEVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = DesignTokens.LetterSpacingWide
                                    ),
                                    color = MonochromeWhite
                                )

                                Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

                                Text(
                                    text = "How many years to reach this goal?",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = Zinc400,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                                // Stepper and Numeric Entry
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Decrease button
                                    Box(
                                        modifier = Modifier
                                            .size(DesignTokens.StepButtonSize)
                                            .background(Color.Black)
                                            .border(
                                                BorderStroke(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary),
                                                RoundedCornerShape(DesignTokens.PaddingZero)
                                            )
                                            .clickable {
                                                val current = yearsText.toIntOrNull() ?: 5
                                                if (current > 1) {
                                                    yearsText = (current - 1).toString()
                                                    errorMessage = null
                                                }
                                            }
                                            .testTag("step_decrement_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "-",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    OutlinedTextField(
                                        value = yearsText,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() } && input.length <= 2) {
                                                yearsText = input
                                                errorMessage = null
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center,
                                            fontFamily = FontFamily.Monospace,
                                            color = MonochromeWhite
                                        ),
                                        singleLine = true,
                                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GridLevel4,
                                            unfocusedBorderColor = Zinc700,
                                            focusedContainerColor = Color(0xFF0D0D0D),
                                            unfocusedContainerColor = Color(0xFF0D0D0D)
                                        ),
                                        modifier = Modifier
                                            .width(DesignTokens.YearsInputWidth)
                                            .padding(horizontal = DesignTokens.PaddingSmall)
                                            .testTag("years_input_field")
                                    )

                                    // Increase button
                                    Box(
                                        modifier = Modifier
                                            .size(DesignTokens.StepButtonSize)
                                            .background(Color.Black)
                                            .border(
                                                BorderStroke(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary),
                                                RoundedCornerShape(DesignTokens.PaddingZero)
                                            )
                                            .clickable {
                                                val current = yearsText.toIntOrNull() ?: 5
                                                if (current < 50) {
                                                    yearsText = (current + 1).toString()
                                                    errorMessage = null
                                                }
                                            }
                                            .testTag("step_increment_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                                val totalWeeks = (yearsText.toIntOrNull() ?: 5) * 52
                                Text(
                                    text = "$yearsText YEARS · $totalWeeks WEEKS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = DesignTokens.LetterSpacingWide,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GridLevel4
                                )
                            }
                        }

                        errorMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                            Text(
                                text = "⚠️ $msg",
                                color = Color(0xFFFF5252),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                    }
                }
            }
        }

        // Pinned Bottom Actions Bar - Always fixed at bottom of page
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .fillMaxWidth()
                .border(
                    BorderStroke(DesignTokens.StrokeThin, Zinc800),
                    RoundedCornerShape(DesignTokens.PaddingZero)
                ),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.PaddingLarge)
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                if (currentStep == 1) {
                    Button(
                        onClick = {
                            if (isGoalValid) {
                                focusManager.clearFocus()
                                currentStep = 2
                                errorMessage = null
                            } else {
                                errorMessage = "Please enter a goal or tap Skip."
                            }
                        },
                        enabled = isGoalValid,
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GridLevel4,
                            contentColor = MonochromeBlack,
                            disabledContainerColor = Color(0xFF1E1E1E),
                            disabledContentColor = Zinc600
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DesignTokens.ButtonHeight)
                            .testTag("confirm_goal_button")
                    ) {
                        Text(
                            text = if (isGoalValid) "CONTINUE →" else "ENTER GOAL TO CONTINUE",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = DesignTokens.LetterSpacingWide,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            onSkip()
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        border = BorderStroke(DesignTokens.StrokeMedium, Zinc600),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Zinc400
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DesignTokens.ButtonHeight)
                            .testTag("skip_goal_button")
                    ) {
                        Text(
                            text = "SKIP",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = DesignTokens.LetterSpacingWide,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            val years = yearsText.toIntOrNull()
                            if (years == null || years !in 1..50) {
                                errorMessage = "Enter 1 to 50 years."
                            } else {
                                focusManager.clearFocus()
                                onInitialize(years, goalText.trim())
                            }
                        },
                        enabled = isYearsValid,
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GridLevel4,
                            contentColor = MonochromeBlack,
                            disabledContainerColor = Color(0xFF1E1E1E),
                            disabledContentColor = Zinc600
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DesignTokens.ButtonHeight)
                            .testTag("initialize_button")
                    ) {
                        Text(
                            text = "START →",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = DesignTokens.LetterSpacingWide,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            currentStep = 1
                            errorMessage = null
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        border = BorderStroke(DesignTokens.StrokeMedium, Zinc600),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Zinc400
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DesignTokens.ButtonHeight)
                            .testTag("back_to_step1_button")
                    ) {
                        Text(
                            text = "← BACK",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = DesignTokens.LetterSpacingWide,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }
    }
}
