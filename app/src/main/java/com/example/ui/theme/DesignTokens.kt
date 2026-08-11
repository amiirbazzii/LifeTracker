package com.example.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DesignTokens {
    // Spacing and Paddings
    val PaddingZero = 0.dp
    val PaddingMicro = 2.dp
    val PaddingTiny = 4.dp
    val PaddingSmall = 8.dp
    val PaddingMedium = 12.dp
    val PaddingLarge = 16.dp
    val PaddingExtraLarge = 24.dp
    val PaddingDoubleExtraLarge = 32.dp

    // Element Dimensions
    val ButtonHeight = 48.dp
    val InputHeight = 52.dp
    val ControlBoxSize = 36.dp
    val StepButtonSize = 44.dp
    val YearsInputWidth = 80.dp
    val DividerThickness = 1.dp

    // Borders & Strokes
    val StrokeThin = 0.5.dp
    val StrokeMedium = 1.dp
    val StrokeThick = 1.5.dp
    val StrokeExtraThick = 2.dp

    // Text Letter Spacings
    val LetterSpacingCondensed = (-0.5).sp
    val LetterSpacingNormal = 0.sp
    val LetterSpacingWide = 1.sp
    val LetterSpacingExtraWide = 1.5.sp
    val LetterSpacingUltraWide = 2.sp
    val LetterSpacingHeadline = 4.sp

    // Font Sizes
    val FontSizeMicro = 7.5.sp
    val FontSizeTiny = 9.sp
    val FontSizeSmall = 10.sp
    val FontSizeMedium = 11.sp
    val FontSizeLarge = 16.sp

    // String Resources / Static System Labels
    const val APP_TITLE = "LIFETRACKER"
    const val APP_VERSION = "v1.0"
    const val LOCAL_FIRST = "LOCAL-FIRST"
    const val HEADER_GOAL_PROTOCOL = "EDIT GOAL"
    const val GOAL_FIELD_PLACEHOLDER = "e.g., Learn Kotlin and Jetpack Compose"
    const val GOAL_COMMIT = "SAVE GOAL"
    const val GOAL_ABORT = "CANCEL"
    const val GOAL_INPUT_DESCRIPTION = "Enter your main long-term goal. This goal will be displayed at the top of your dashboard."
    
    const val ONBOARDING_SUBTITLE = "LOCAL-FIRST TIMELINE TRACKER"
    const val ONBOARDING_PROMPT_TITLE = "SET UP YOUR TIMELINE"
    const val ONBOARDING_PROMPT_DESC = "Select how many years to include in your timeline."
    const val ONBOARDING_CONFIG_LABEL = "TIMELINE YEARS"
    const val ONBOARDING_INIT_BUTTON = "START TIMELINE"
    const val ONBOARDING_ERROR_POSITIVE = "Please enter a valid number of years."

    const val RESET_DIALOG_TITLE = "RESET ALL DATA?"
    const val RESET_DIALOG_TEXT = "This will permanently reset your timeline and delete all logged tasks."
    const val RESET_CONFIRM = "RESET"
    const val RESET_CANCEL = "CANCEL"

    const val READ_ONLY_BANNER = "PAST WEEKS ARE READ-ONLY"
    const val MOMENTUM_EMPTY = "NO TASKS"
    const val MOMENTUM_EMPTY_DESC = "Add tasks below to start tracking your progress."
    const val NO_HISTORY_TITLE = "NO PAST TASKS"
    const val NO_HISTORY_DESC = "No tasks were logged during this week."
}
