package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyTask
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: LifeTrackerUiState.Dashboard,
    userGoal: String,
    onEditGoalClick: () -> Unit,
    onSelectWeek: (Int) -> Unit,
    onAddTask: (String, Int, Int) -> Unit,
    onToggleTask: (DailyTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onReset: () -> Unit
) {
    val currentDayOfWeekIndex = remember(state.meta.inceptionTimestamp, state.currentWeekIndex) {
        Utils.getTodayDayIndex(state.meta.inceptionTimestamp, state.currentWeekIndex)
    }
    
    var newTaskTitle by remember { mutableStateOf("") }
    var taskDayOfWeek by remember { mutableStateOf(currentDayOfWeekIndex) }

    LaunchedEffect(state.selectedWeekIndex) {
        if (state.selectedWeekIndex != state.currentWeekIndex) {
            taskDayOfWeek = 1
        } else {
            taskDayOfWeek = currentDayOfWeekIndex
        }
    }

    val isHistorical = state.selectedWeekIndex < state.currentWeekIndex
    val focusManager = LocalFocusManager.current
    var isInputFocused by remember { mutableStateOf(false) }

    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }

    BackHandler(enabled = isInputFocused) {
        focusManager.clearFocus()
    }

    val topHalfHeight by animateDpAsState(
        targetValue = if (isInputFocused) DesignTokens.PaddingZero else 215.dp,
        animationSpec = tween(durationMillis = 400),
        label = "topHalfHeight"
    )
    val topHalfAlpha by animateFloatAsState(
        targetValue = if (isInputFocused) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "topHalfAlpha"
    )
    val spacerHeightTop by animateDpAsState(
        targetValue = if (isInputFocused) DesignTokens.PaddingZero else DesignTokens.PaddingMedium,
        animationSpec = tween(durationMillis = 400),
        label = "spacerHeightTop"
    )
    val spacerHeightBot by animateDpAsState(
        targetValue = if (isInputFocused) DesignTokens.PaddingZero else DesignTokens.PaddingSmall,
        animationSpec = tween(durationMillis = 400),
        label = "spacerHeightBot"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignTokens.PaddingLarge)
    ) {
        // App Header conforming to Clean Minimalism HTML mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    RoundedCornerShape(DesignTokens.PaddingZero)
                )
                .padding(DesignTokens.PaddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = DesignTokens.PaddingLarge)
                    .clickable { onEditGoalClick() }
            ) {
                Text(
                    text = "YOUR NEXT ${state.meta.targetYears} YEAR GOAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Zinc500,
                        fontSize = DesignTokens.FontSizeSmall,
                        letterSpacing = DesignTokens.LetterSpacingUltraWide,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                val goalTextToShow = if (userGoal.isBlank()) {
                    "> TAP TO SET AN EXECUTION GOAL"
                } else {
                    userGoal.uppercase()
                }
                Text(
                    text = goalTextToShow,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = DesignTokens.LetterSpacingCondensed,
                        color = if (userGoal.isBlank()) GridLevel4 else MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            initialDelayMillis = 1000,
                            velocity = 20.dp
                        )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMedium)
            ) {
                // Clean Reset Box
                var showResetAlert by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(DesignTokens.ControlBoxSize)
                        .border(
                            DesignTokens.StrokeMedium,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .clickable { showResetAlert = true }
                        .testTag("reset_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Timeline",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
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

        Spacer(modifier = Modifier.height(spacerHeightTop))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topHalfHeight)
                .alpha(topHalfAlpha)
                .clip(RoundedCornerShape(DesignTokens.PaddingZero))
        ) {
            if (topHalfHeight > 24.dp) {
                // --- TOP HALF: MACRO GRID WINDOW ---
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MACRO TIMELINE MATRIX (${state.meta.totalWeeks} WEEKS)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Zinc400,
                                letterSpacing = DesignTokens.LetterSpacingExtraWide,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(DesignTokens.PaddingZero))
                                .padding(horizontal = DesignTokens.PaddingTiny, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${state.meta.targetYears}Y GOAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = DesignTokens.FontSizeTiny
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scrollable Grid Matrix using exactly 13 columns mimicking HTML structure
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .border(
                                DesignTokens.StrokeMedium,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(DesignTokens.PaddingZero)
                            )
                            .background(if (isSystemInDarkTheme()) GridLevel0_Dark else Color(0xFFFCFCFC))
                            .padding(DesignTokens.PaddingTiny)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(13),
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMicro),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMicro),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                count = state.meta.totalWeeks,
                                span = { index ->
                                    if (index == state.currentWeekIndex) {
                                        GridItemSpan(7)
                                    } else {
                                        GridItemSpan(1)
                                    }
                                }
                            ) { weekIdx ->
                                val isDark = isSystemInDarkTheme()
                                val isSelected = weekIdx == state.selectedWeekIndex
                                val isCurrent = weekIdx == state.currentWeekIndex

                                if (isCurrent) {
                                    val itemStroke = if (isSelected) {
                                        BorderStroke(DesignTokens.StrokeExtraThick, MaterialTheme.colorScheme.primary)
                                    } else {
                                        BorderStroke(DesignTokens.StrokeThick, if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0))
                                    }

                                    Row(
                                        modifier = Modifier
                                            .aspectRatio(7f)
                                            .clip(RoundedCornerShape(DesignTokens.PaddingZero))
                                            .background(if (isDark) GridLevel0_Dark else Color(0xFFF0F0F0))
                                            .border(itemStroke, RoundedCornerShape(DesignTokens.PaddingZero))
                                            .testTag("week_box_current_active_$weekIdx"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (d in 1..7) {
                                            val dayTasks = state.currentWeekTasks.filter { it.dayOfWeek == d }
                                            val total = dayTasks.size
                                            val dayLevel = if (total == 0) {
                                                0
                                            } else {
                                                val completed = dayTasks.count { it.isCompleted == 1 }
                                                val sr = (completed.toFloat() / total.toFloat()) * 100f
                                                when {
                                                    sr == 0f -> 0
                                                    sr < 33f -> 1
                                                    sr < 66f -> 2
                                                    sr < 100f -> 3
                                                    else -> 4
                                                }
                                            }

                                            val dayColor = when (dayLevel) {
                                                0 -> if (isDark) GridLevel0_Dark else GridLevel0_Light
                                                1 -> GridLevel1
                                                2 -> GridLevel2
                                                3 -> GridLevel3
                                                4 -> GridLevel4
                                                else -> if (isDark) GridLevel0_Dark else GridLevel0_Light
                                            }

                                            val isToday = remember(state.meta.inceptionTimestamp, weekIdx, d) {
                                                Utils.isCellToday(state.meta.inceptionTimestamp, weekIdx, d)
                                            }
                                            val isDaySelected = isSelected && taskDayOfWeek == d
                                            val dayBorder = if (isToday) {
                                                if (isDaySelected) {
                                                    BorderStroke(DesignTokens.StrokeExtraThick, GridLevel4)
                                                } else {
                                                    BorderStroke(DesignTokens.StrokeThick, GridLevel4)
                                                }
                                            } else {
                                                if (isDaySelected) {
                                                    BorderStroke(DesignTokens.StrokeThick, MaterialTheme.colorScheme.primary)
                                                } else {
                                                    BorderStroke(DesignTokens.StrokeThin, if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0))
                                                }
                                            }

                                            val absoluteDayOfMonthText = remember(state.meta.inceptionTimestamp, weekIdx, d) {
                                                val cal = Calendar.getInstance()
                                                cal.timeInMillis = state.meta.inceptionTimestamp
                                                cal.add(Calendar.WEEK_OF_YEAR, weekIdx)
                                                cal.add(Calendar.DAY_OF_YEAR, d - 1)
                                                cal.get(Calendar.DAY_OF_MONTH).toString()
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .background(dayColor)
                                                    .border(dayBorder, RoundedCornerShape(DesignTokens.PaddingZero))
                                                    .clickable {
                                                        onSelectWeek(weekIdx)
                                                        taskDayOfWeek = d
                                                    }
                                                    .testTag("active_week_day_$d"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = absoluteDayOfMonthText,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = DesignTokens.FontSizeTiny,
                                                        fontWeight = if (isDaySelected) FontWeight.Black else FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = if (isToday) {
                                                            if (dayLevel == 4) MonochromeBlack else GridLevel4
                                                        } else if (dayLevel > 0) {
                                                            MonochromeWhite
                                                        } else {
                                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                                        }
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    val level = state.weekColors[weekIdx] ?: 0
                                    val cellColor = when (level) {
                                        0 -> if (isDark) GridLevel0_Dark else GridLevel0_Light
                                        1 -> GridLevel1
                                        2 -> GridLevel2
                                        3 -> GridLevel3
                                        4 -> GridLevel4
                                        else -> if (isDark) GridLevel0_Dark else GridLevel0_Light
                                    }

                                    val itemStroke = if (isSelected) {
                                        BorderStroke(DesignTokens.StrokeExtraThick, MaterialTheme.colorScheme.primary)
                                    } else {
                                        BorderStroke(DesignTokens.StrokeThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(DesignTokens.PaddingZero))
                                            .background(cellColor)
                                            .border(itemStroke, RoundedCornerShape(DesignTokens.PaddingZero))
                                            .clickable {
                                                onSelectWeek(weekIdx)
                                            }
                                            .testTag("week_box_$weekIdx"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (weekIdx + 1).toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = DesignTokens.FontSizeMicro,
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (level > 0) MonochromeWhite 
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

                    // Sub-grid footer zone: Inception dates + Legend Panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val yearFormatter = SimpleDateFormat("yyyy", Locale.US)
                        val inceptionYear = yearFormatter.format(Date(state.meta.inceptionTimestamp))
                        val quarter = 1 + (Calendar.getInstance().get(Calendar.MONTH) / 3)
                        Text(
                            text = "INCEPTION: ${inceptionYear}.Q${quarter}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = DesignTokens.FontSizeTiny,
                                color = Zinc500,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        LegendPanel()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(spacerHeightBot))
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f * topHalfAlpha), thickness = DesignTokens.DividerThickness)
        Spacer(modifier = Modifier.height(spacerHeightBot))

        // --- BOTTOM HALF: MICRO PERSISTENT TASK MANAGER ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val filteredTasks = remember(state.selectedWeekTasks, taskDayOfWeek) {
                state.selectedWeekTasks.filter { it.dayOfWeek == taskDayOfWeek }
            }
            val completedCount = filteredTasks.count { it.isCompleted == 1 }
            val totalCount = filteredTasks.size
            val completionRate = if (totalCount == 0) 0 else (completedCount * 100 / totalCount)

            // Dynamic header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DesignTokens.PaddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    val headerText = if (state.selectedWeekIndex == state.currentWeekIndex) {
                        "WEEK ${state.selectedWeekIndex + 1} : ${Utils.getDayAbsoluteDateString(state.meta.inceptionTimestamp, state.selectedWeekIndex, taskDayOfWeek)}"
                    } else {
                        "WEEK ${state.selectedWeekIndex + 1} : ${Utils.getWeekRangeString(state.meta.inceptionTimestamp, state.selectedWeekIndex)}"
                    }
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = DesignTokens.LetterSpacingExtraWide
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$completedCount OF $totalCount TASKS COMPLETED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = DesignTokens.FontSizeSmall,
                            color = Zinc500
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SR: $completionRate%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GridLevel4,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = -1.sp
                        )
                    )
                    val level = when {
                        totalCount == 0 -> 0
                        else -> {
                            val sr = (completedCount.toFloat() / totalCount.toFloat()) * 100f
                            when {
                                sr == 0f -> 0
                                sr < 33f -> 1
                                sr < 66f -> 2
                                sr < 100f -> 3
                                else -> 4
                            }
                        }
                    }
                    Text(
                        text = "LEVEL $level",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Zinc500,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Task list container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (filteredTasks.isEmpty()) {
                    EmptyStatePanel(isHistorical = isHistorical)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingTiny)
                    ) {
                        items(
                            count = filteredTasks.size,
                            key = { index -> filteredTasks[index].taskId }
                        ) { index ->
                            val task = filteredTasks[index]
                            TaskItemRow(
                                task = task,
                                inceptionTimestamp = state.meta.inceptionTimestamp,
                                isReadOnly = isHistorical,
                                onToggle = onToggleTask,
                                onDelete = onDeleteTask
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

            // Task creating panel
            if (isHistorical) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            DesignTokens.StrokeMedium,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .padding(DesignTokens.PaddingMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Read Only",
                            tint = Zinc500,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.PaddingSmall))
                        Text(
                            text = DesignTokens.READ_ONLY_BANNER,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = DesignTokens.LetterSpacingWide
                            ),
                            color = Zinc500
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DesignTokens.InputHeight)
                        .border(
                            DesignTokens.StrokeMedium,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .background(MaterialTheme.colorScheme.background),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(62.dp)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                taskDayOfWeek = if (taskDayOfWeek == 7) 1 else taskDayOfWeek + 1
                            }
                            .testTag("day_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val currentDayAbbr = remember(state.meta.inceptionTimestamp, state.selectedWeekIndex, taskDayOfWeek) {
                                val cellTimeInMillis = state.meta.inceptionTimestamp + (state.selectedWeekIndex * 7L + (taskDayOfWeek - 1)) * 24L * 60L * 60L * 1000L
                                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                                sdf.format(Date(cellTimeInMillis)).uppercase()
                            }
                            Text(
                                text = currentDayAbbr,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = DesignTokens.FontSizeMedium
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "DAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(DesignTokens.StrokeMedium)
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    TextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = {
                            Text(
                                text = "_ INPUTTING...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = DesignTokens.FontSizeMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = DesignTokens.LetterSpacingWide
                                ),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val title = newTaskTitle.trim()
                                if (title.isNotEmpty()) {
                                    onAddTask(title, state.selectedWeekIndex, taskDayOfWeek)
                                    newTaskTitle = ""
                                } else {
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .onFocusChanged { fState -> isInputFocused = fState.isFocused }
                            .testTag("task_input_field")
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(DesignTokens.InputHeight)
                            .background(GridLevel4) // Neon green accent!
                            .clickable {
                                val title = newTaskTitle.trim()
                                if (title.isNotEmpty()) {
                                    onAddTask(title, state.selectedWeekIndex, taskDayOfWeek)
                                    newTaskTitle = ""
                                }
                            }
                            .testTag("add_task_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MonochromeBlack
                        )
                    }
                }
            }
        }
    }
}
