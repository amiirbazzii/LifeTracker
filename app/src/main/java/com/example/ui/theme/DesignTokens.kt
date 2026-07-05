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
    const val APP_TITLE = "LIFETRACKER.sys"
    const val APP_VERSION = "v1.0.0-PROD"
    const val LOCAL_FIRST = "100% LOCAL-FIRST"
    const val HEADER_GOAL_PROTOCOL = "EXECUTION GOAL PROTOCOL"
    const val GOAL_FIELD_PLACEHOLDER = "e.g., CONTINUOUS ITERATION // MINIMAL DISTRACTION"
    const val GOAL_COMMIT = "COMMIT TO MATRIX PROFILE"
    const val GOAL_ABORT = "ABORT PROTOCOL (DISCARD)"
    const val GOAL_INPUT_DESCRIPTION = "Enter a core objective, mantra, or rule to direct your life matrix grid. This goal will scroll in the top control panel of your dashboard."
    
    const val ONBOARDING_SUBTITLE = "PROTOCOL v1.0.0 // LOCAL-FIRST TIMELINE MATRIX"
    const val ONBOARDING_PROMPT_TITLE = "THE GRAND MATRIX PROMPT"
    const val ONBOARDING_PROMPT_DESC = "Specify timeline scope to map out your execution matrix."
    const val ONBOARDING_CONFIG_LABEL = "TARGET CONFIGURATION: YEARS"
    const val ONBOARDING_INIT_BUTTON = "INITIALIZE TIMELINE MATRIX"
    const val ONBOARDING_ERROR_POSITIVE = "Please enter a valid positive duration."

    const val RESET_DIALOG_TITLE = "RESET TIMELINE CONFIG?"
    const val RESET_DIALOG_TEXT = "This will permanently remove your timeline matrix configuration and delete all logged tasks across all weeks."
    const val RESET_CONFIRM = "RESET"
    const val RESET_CANCEL = "CANCEL"

    const val READ_ONLY_BANNER = "HISTORICAL RECORD IS READ-ONLY"
    const val MOMENTUM_EMPTY = "MOMENTUM EMPTY"
    const val MOMENTUM_EMPTY_DESC = "Enter objectives below to start logging execution details."
    const val NO_HISTORY_TITLE = "NO HISTORY RECORDED"
    const val NO_HISTORY_DESC = "No tasks were logged during this historical period."
}
