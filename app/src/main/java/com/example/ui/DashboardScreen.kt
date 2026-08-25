package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.data.Category
import com.example.data.Routine
import com.example.data.SubGoal
import com.example.data.sortedWithTimeOrder
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: LifeTrackerUiState.Dashboard,
    userGoal: String,
    categories: List<Category> = emptyList(),
    routines: List<Routine> = emptyList(),
    subGoals: List<SubGoal> = emptyList(),
    onEditGoalClick: () -> Unit,
    onSelectWeek: (Int) -> Unit,
    onAddTask: (String, Int, Int, String?) -> Unit,
    onToggleTask: (DailyTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onReset: () -> Unit,
    onIncrementRoutineCompletion: (String) -> Unit = {},
    onUpdateTaskTimer: (DailyTask, String?, String?) -> Unit = { _, _, _ -> }
) {
    val currentDayOfWeekIndex = remember(state.meta.inceptionTimestamp, state.currentWeekIndex) {
        Utils.getTodayDayIndex(state.meta.inceptionTimestamp, state.currentWeekIndex)
    }
    
    var newTaskTitle by remember { mutableStateOf("") }
    var taskDayOfWeek by remember { mutableStateOf(currentDayOfWeekIndex) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedRoutine by remember { mutableStateOf<Routine?>(null) }
    var activeRoutineIdForMilestoneVerification by remember { mutableStateOf<String?>(null) }
    var activeTaskForTimer by remember { mutableStateOf<DailyTask?>(null) }
    val sortedSubGoals = remember(subGoals) {
        subGoals.sortedWith(compareBy({ it.durationMonths }, { it.createdTimestamp }))
    }
    val groupedSubGoals = remember(sortedSubGoals) {
        sortedSubGoals.groupBy { it.durationMonths }
    }

    // Helper function to find assigned subgoals for a given week index
    fun getGoalsForWeekIndex(weekIdx: Int): List<SubGoal> {
        if (subGoals.isEmpty()) return emptyList()
        val weeksPerMonth = state.meta.totalWeeks.toDouble() / (state.meta.targetYears * 12.0)
        // Find the first group whose deadline (endW) has not passed
        val activeGroupDuration = groupedSubGoals.keys.firstOrNull { durationMonths ->
            val endW = (durationMonths * weeksPerMonth).toInt()
            weekIdx < endW
        }

        return if (activeGroupDuration != null) groupedSubGoals[activeGroupDuration] ?: emptyList() else emptyList()
    }

    var activeSubGoals by remember { mutableStateOf<List<SubGoal>>(emptyList()) }

    // Auto update activeSubGoals whenever selectedWeekIndex changes
    LaunchedEffect(state.selectedWeekIndex, subGoals) {
        activeSubGoals = getGoalsForWeekIndex(state.selectedWeekIndex)
    }

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

    val hasGoal = userGoal.trim().isNotBlank()
    val showMatrix = hasGoal && !isInputFocused

    val topHalfHeight by animateDpAsState(
        targetValue = if (showMatrix) 215.dp else DesignTokens.PaddingZero,
        animationSpec = tween(durationMillis = 400),
        label = "topHalfHeight"
    )
    val topHalfAlpha by animateFloatAsState(
        targetValue = if (showMatrix) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "topHalfAlpha"
    )
    val spacerHeightTop by animateDpAsState(
        targetValue = if (isInputFocused) DesignTokens.PaddingZero else DesignTokens.PaddingMedium,
        animationSpec = tween(durationMillis = 400),
        label = "spacerHeightTop"
    )
    val spacerHeightBot by animateDpAsState(
        targetValue = if (showMatrix) DesignTokens.PaddingSmall else DesignTokens.PaddingZero,
        animationSpec = tween(durationMillis = 400),
        label = "spacerHeightBot"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = DesignTokens.PaddingLarge, vertical = DesignTokens.PaddingMedium)
    ) {
        // App Goal Banner (Hides completely when input is focused)
        if (!isInputFocused) {
            val targetDuration = activeSubGoals.firstOrNull()?.durationMonths
            val headerLabel = if (targetDuration != null) {
                "Your next $targetDuration month ${if (targetDuration == 1) "goal" else "goals"}"
            } else null

            BaseSystemHeader(
                mode = SystemHeaderMode.DASHBOARD,
                targetYears = state.meta.targetYears,
                userGoal = userGoal,
                headerLabelOverride = headerLabel,
                activeSubGoals = activeSubGoals,
                onEditGoalClick = onEditGoalClick
            )
        }

        if (hasGoal) {
            Spacer(modifier = Modifier.height(spacerHeightTop))

            // Macro Timeline Matrix Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topHalfHeight)
                    .alpha(topHalfAlpha)
                    .clip(RoundedCornerShape(DesignTokens.PaddingZero))
            ) {
                if (topHalfHeight > 24.dp) {
                    MacroTimelineMatrix(
                        state = state,
                        taskDayOfWeek = taskDayOfWeek,
                        subGoals = subGoals,
                        onSelectWeek = { weekIdx ->
                            onSelectWeek(weekIdx)
                            activeSubGoals = getGoalsForWeekIndex(weekIdx)
                        },
                        onSelectDay = { d -> taskDayOfWeek = d }
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacerHeightBot))
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f * topHalfAlpha), thickness = DesignTokens.DividerThickness)
            Spacer(modifier = Modifier.height(spacerHeightBot))
        } else {
            Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))
        }

        // --- MICRO PERSISTENT TASK MANAGER (BOTTOM HALF) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val filteredTasks = remember(state.selectedWeekTasks, taskDayOfWeek) {
                state.selectedWeekTasks.filter { it.dayOfWeek == taskDayOfWeek }.sortedWithTimeOrder()
            }
            val completedCount = if (isHistorical) {
                state.selectedWeekTasks.count { it.isCompleted == 1 }
            } else {
                filteredTasks.count { it.isCompleted == 1 }
            }
            val totalCount = if (isHistorical) {
                state.selectedWeekTasks.size
            } else {
                filteredTasks.size
            }
            val completionRate = if (totalCount == 0) 0 else (completedCount * 100 / totalCount)

            // 1. DashboardHeaderComponent
            DashboardHeaderComponent(
                selectedWeekIndex = state.selectedWeekIndex,
                currentWeekIndex = state.currentWeekIndex,
                inceptionTimestamp = state.meta.inceptionTimestamp,
                taskDayOfWeek = taskDayOfWeek,
                completedCount = completedCount,
                totalCount = totalCount,
                completionRate = completionRate,
                isInputFocused = isInputFocused,
                onCloseInputFocus = {
                    focusManager.clearFocus()
                    selectedCategory = null
                    selectedRoutine = null
                    newTaskTitle = ""
                }
            )

            // 4. TaskListSectionComponent
            TaskListSectionComponent(
                filteredTasks = filteredTasks,
                allWeekTasks = state.selectedWeekTasks,
                isHistorical = isHistorical,
                selectedWeekIndex = state.selectedWeekIndex,
                inceptionTimestamp = state.meta.inceptionTimestamp,
                categories = categories,
                routines = routines,
                onToggleTask = { task ->
                    val isCompletedNow = task.isCompleted == 1
                    if (!isCompletedNow && task.routineId != null) {
                        onToggleTask(task)
                        activeRoutineIdForMilestoneVerification = task.routineId
                    } else {
                        onToggleTask(task)
                    }
                },
                onDeleteTask = onDeleteTask,
                onOpenTimerDialog = { task -> activeTaskForTimer = task },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

            // Footer Zone (Read Only indicator or Add Task controllers)
            if (isHistorical) {
                ReadOnlyBanner()
            } else {
                // 3. ContextualChipsComponent
                if (isInputFocused || selectedCategory != null || newTaskTitle.isNotEmpty()) {
                    ContextualChipsComponent(
                        categories = categories,
                        routines = routines,
                        selectedCategory = selectedCategory,
                        selectedRoutine = selectedRoutine,
                        onCategorySelected = { selectedCategory = it },
                        onRoutineSelected = { selectedRoutine = it },
                        onNewTaskTitleChange = { newTaskTitle = it },
                        newTaskTitle = newTaskTitle
                    )
                }

                // 2. TaskInputSectionComponent
                TaskInputSectionComponent(
                    newTaskTitle = newTaskTitle,
                    onNewTaskTitleChange = { newTaskTitle = it },
                    selectedWeekIndex = state.selectedWeekIndex,
                    taskDayOfWeek = taskDayOfWeek,
                    inceptionTimestamp = state.meta.inceptionTimestamp,
                    onDayOfWeekToggle = {
                        taskDayOfWeek = if (taskDayOfWeek == 7) 1 else taskDayOfWeek + 1
                    },
                    onAddTask = {
                        val title = newTaskTitle.trim()
                        if (title.isNotEmpty()) {
                            onAddTask(title, state.selectedWeekIndex, taskDayOfWeek, selectedRoutine?.id)
                            newTaskTitle = ""
                            selectedCategory = null
                            selectedRoutine = null
                        } else {
                            focusManager.clearFocus()
                        }
                    },
                    onFocusChanged = { isInputFocused = it }
                )
            }
        }
    }

    // Milestone completion Dialog box
    if (activeRoutineIdForMilestoneVerification != null) {
        MilestoneVerificationDialog(
            onDismiss = { activeRoutineIdForMilestoneVerification = null },
            onConfirm = {
                activeRoutineIdForMilestoneVerification?.let { routineId ->
                    onIncrementRoutineCompletion(routineId)
                }
                activeRoutineIdForMilestoneVerification = null
            }
        )
    }

    // Task Timer Setting Dialog
    if (activeTaskForTimer != null) {
        TaskTimerDialog(
            task = activeTaskForTimer!!,
            onDismiss = { activeTaskForTimer = null },
            onSaveTimer = { startTime, endTime ->
                activeTaskForTimer?.let { task ->
                    onUpdateTaskTimer(task, startTime, endTime)
                }
                activeTaskForTimer = null
            }
        )
    }
}

// ==========================================
// SUB-COMPONENTS (Component-Based Pass)
// ==========================================

@Composable
fun DashboardHeaderComponent(
    selectedWeekIndex: Int,
    currentWeekIndex: Int,
    inceptionTimestamp: Long,
    taskDayOfWeek: Int,
    completedCount: Int,
    totalCount: Int,
    completionRate: Int,
    isInputFocused: Boolean,
    onCloseInputFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = DesignTokens.PaddingSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (isInputFocused) Alignment.CenterVertically else Alignment.Bottom
    ) {
        Column {
            val headerText = if (selectedWeekIndex == currentWeekIndex) {
                "WEEK ${selectedWeekIndex + 1} : ${Utils.getDayAbsoluteDateString(inceptionTimestamp, selectedWeekIndex, taskDayOfWeek)}"
            } else {
                "WEEK ${selectedWeekIndex + 1} : ${Utils.getWeekRangeString(inceptionTimestamp, selectedWeekIndex)}"
            }
            val isDark = isSystemInDarkTheme()
            val primaryLabelColor = if (isDark) MonochromeWhite else MonochromeBlack
            Text(
                text = headerText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = DesignTokens.LetterSpacingWide
                ),
                color = primaryLabelColor
            )
            if (!isInputFocused) {
                Text(
                    text = "$completedCount OF $totalCount TASKS COMPLETED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = DesignTokens.FontSizeSmall,
                        color = Zinc500
                    )
                )
            }
        }

        if (isInputFocused) {
            Box(
                modifier = Modifier
                    .size(DesignTokens.ControlBoxSize)
                    .border(
                        DesignTokens.StrokeMedium,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(DesignTokens.PaddingZero)
                    )
                    .clickable { onCloseInputFocus() }
                    .testTag("close_input_focus_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Input",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "SR: $completionRate%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = GridLevel4,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = -1.sp,
                        fontSize = 12.sp
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
    }
}

@Composable
fun TaskInputSectionComponent(
    newTaskTitle: String,
    onNewTaskTitleChange: (String) -> Unit,
    selectedWeekIndex: Int,
    taskDayOfWeek: Int,
    inceptionTimestamp: Long,
    onDayOfWeekToggle: () -> Unit,
    onAddTask: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
                .clickable { onDayOfWeekToggle() }
                .testTag("day_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val currentDayAbbr = remember(inceptionTimestamp, selectedWeekIndex, taskDayOfWeek) {
                    val cellTimeInMillis = inceptionTimestamp + (selectedWeekIndex * 7L + (taskDayOfWeek - 1)) * 24L * 60L * 60L * 1000L
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
            onValueChange = onNewTaskTitleChange,
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
                onDone = { onAddTask() }
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .onFocusChanged { fState -> onFocusChanged(fState.isFocused) }
                .testTag("task_input_field")
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(DesignTokens.InputHeight)
                .background(GridLevel4)
                .clickable { onAddTask() }
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

@Composable
fun ContextualChipsComponent(
    categories: List<Category>,
    routines: List<Routine>,
    selectedCategory: Category?,
    selectedRoutine: Routine?,
    onCategorySelected: (Category?) -> Unit,
    onRoutineSelected: (Routine?) -> Unit,
    onNewTaskTitleChange: (String) -> Unit,
    newTaskTitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = DesignTokens.PaddingSmall)
    ) {
        // Category Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("category_chip_row"),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.PaddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedCategory != null) {
                item {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                            .border(
                                BorderStroke(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.error),
                                RoundedCornerShape(DesignTokens.PaddingZero)
                            )
                            .clickable {
                                onCategorySelected(null)
                                onRoutineSelected(null)
                            }
                            .padding(horizontal = DesignTokens.PaddingMedium, vertical = DesignTokens.PaddingSmall)
                            .testTag("clear_category_chip"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLEAR SELECTION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = DesignTokens.FontSizeSmall
                            )
                        )
                    }
                }
            }

            items(categories, key = { it.id }) { category ->
                val isSel = selectedCategory?.id == category.id
                val chipBg = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent
                val chipText = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                val chipBorder = if (isSel) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

                Box(
                    modifier = Modifier
                        .background(chipBg)
                        .border(
                            BorderStroke(DesignTokens.StrokeMedium, if (isSel) MaterialTheme.colorScheme.primary else chipBorder),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .clickable {
                            if (isSel) {
                                onCategorySelected(null)
                                onRoutineSelected(null)
                            } else {
                                onCategorySelected(category)
                                onRoutineSelected(null)
                            }
                        }
                        .padding(horizontal = DesignTokens.PaddingMedium, vertical = DesignTokens.PaddingSmall)
                        .testTag("category_chip_${category.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = chipText,
                            fontSize = DesignTokens.FontSizeSmall
                        )
                    )
                }
            }
        }

        if (selectedCategory != null) {
            val associatedRoutines = routines.filter { it.categoryId == selectedCategory.id }
            if (associatedRoutines.isNotEmpty()) {
                Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_chip_row"),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.PaddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(associatedRoutines, key = { it.id }) { routine ->
                        val isSel = selectedRoutine?.id == routine.id
                        val chipBg = if (isSel) GridLevel4 else Color.Transparent
                        val chipText = if (isSel) MonochromeBlack else MaterialTheme.colorScheme.primary
                        val chipBorder = if (isSel) GridLevel4 else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

                        Box(
                            modifier = Modifier
                                .background(chipBg)
                                .border(BorderStroke(DesignTokens.StrokeMedium, chipBorder), RoundedCornerShape(DesignTokens.PaddingZero))
                                .clickable {
                                    if (isSel) {
                                        onRoutineSelected(null)
                                    } else {
                                        onRoutineSelected(routine)
                                        if (newTaskTitle.isBlank()) {
                                            onNewTaskTitleChange(routine.title)
                                        }
                                    }
                                }
                                .padding(horizontal = DesignTokens.PaddingMedium, vertical = DesignTokens.PaddingSmall)
                                .testTag("routine_chip_${routine.title}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = routine.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = chipText,
                                    fontSize = DesignTokens.FontSizeSmall
                                )
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
    }
}

@Composable
fun DayDateSectionHeader(
    dateLabel: String,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) Color(0xFF141414) else Color(0xFFEEEEEE))
            .border(
                BorderStroke(DesignTokens.StrokeThin, if (isDark) Zinc800 else Color(0xFFD0D0D0)),
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .padding(horizontal = DesignTokens.PaddingMedium, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(GridLevel4)
            )
            Text(
                text = dateLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = DesignTokens.LetterSpacingWide,
                    fontSize = 11.sp
                ),
                color = if (isDark) MonochromeWhite else MonochromeBlack
            )
        }
        Text(
            text = "$completedCount/$totalCount DONE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (completedCount == totalCount && totalCount > 0) GridLevel4 else Zinc500
            )
        )
    }
}

@Composable
fun TaskListSectionComponent(
    filteredTasks: List<DailyTask>,
    allWeekTasks: List<DailyTask>,
    isHistorical: Boolean,
    selectedWeekIndex: Int,
    inceptionTimestamp: Long,
    categories: List<Category>,
    routines: List<Routine>,
    onToggleTask: (DailyTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onOpenTimerDialog: (DailyTask) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        if (isHistorical) {
            if (allWeekTasks.isEmpty()) {
                EmptyStatePanel(isHistorical = true)
            } else {
                val tasksByDay = remember(allWeekTasks) {
                    allWeekTasks.groupBy { it.dayOfWeek }.toSortedMap()
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingTiny)
                ) {
                    tasksByDay.forEach { (dayOfWeek, dayTasks) ->
                        val dateLabel = Utils.getDayFormattedDate(inceptionTimestamp, selectedWeekIndex, dayOfWeek)
                        val dayCompleted = dayTasks.count { it.isCompleted == 1 }
                        val dayTotal = dayTasks.size

                        item(key = "header_${selectedWeekIndex}_$dayOfWeek") {
                            Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))
                            DayDateSectionHeader(
                                dateLabel = dateLabel,
                                completedCount = dayCompleted,
                                totalCount = dayTotal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        items(
                            items = dayTasks.sortedWithTimeOrder(),
                            key = { it.taskId }
                        ) { task ->
                            TaskItemRowComponent(
                                task = task,
                                inceptionTimestamp = inceptionTimestamp,
                                isReadOnly = true,
                                onToggle = onToggleTask,
                                onDelete = onDeleteTask,
                                categories = categories,
                                routines = routines,
                                onOpenTimerDialog = onOpenTimerDialog
                            )
                        }

                        item(key = "spacer_${selectedWeekIndex}_$dayOfWeek") {
                            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                        }
                    }
                }
            }
        } else {
            if (filteredTasks.isEmpty()) {
                EmptyStatePanel(isHistorical = false)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingTiny)
                ) {
                    items(
                        items = filteredTasks,
                        key = { it.taskId }
                    ) { task ->
                        TaskItemRowComponent(
                            task = task,
                            inceptionTimestamp = inceptionTimestamp,
                            isReadOnly = false,
                            onToggle = onToggleTask,
                            onDelete = onDeleteTask,
                            categories = categories,
                            routines = routines,
                            onOpenTimerDialog = onOpenTimerDialog
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReadOnlyBanner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
}

@Composable
fun MilestoneVerificationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(DesignTokens.PaddingZero),
        title = {
            Text(
                text = "[SYSTEM VERIFICATION]",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = DesignTokens.LetterSpacingWide
                ),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = "Great job on today's effort! Did this work result in fully achieving the macro milestone (e.g., Article Published), or is it still a work in progress?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .background(GridLevel4)
                    .border(DesignTokens.StrokeMedium, MonochromeBlack, RoundedCornerShape(DesignTokens.PaddingZero))
                    .clickable { onConfirm() }
                    .padding(horizontal = DesignTokens.PaddingMedium, vertical = DesignTokens.PaddingSmall)
                    .testTag("milestone_achieved_button")
            ) {
                Text(
                    text = "MILESTONE ACHIEVED!",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = MonochromeBlack
                    )
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .border(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary, RoundedCornerShape(DesignTokens.PaddingZero))
                    .clickable { onDismiss() }
                    .padding(horizontal = DesignTokens.PaddingMedium, vertical = DesignTokens.PaddingSmall)
                    .testTag("still_in_progress_button")
            ) {
                Text(
                    text = "STILL IN PROGRESS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    )
}

@Composable
fun MacroTimelineMatrix(
    state: LifeTrackerUiState.Dashboard,
    taskDayOfWeek: Int,
    subGoals: List<SubGoal> = emptyList(),
    onSelectWeek: (Int) -> Unit,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = modifier.fillMaxSize()
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
                .background(if (isDark) GridLevel0_Dark else Color(0xFFFCFCFC))
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
                    key = { index -> index },
                    span = { index ->
                        if (index == state.currentWeekIndex) {
                            GridItemSpan(7)
                        } else {
                            GridItemSpan(1)
                        }
                    }
                ) { weekIdx ->
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
                                            onSelectDay(d)
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

                        val isGoalDeadlineWeek = remember(subGoals, state.meta, weekIdx) {
                            if (subGoals.isEmpty()) false else {
                                val weeksPerMonth = state.meta.totalWeeks.toDouble() / (state.meta.targetYears * 12.0)
                                subGoals.any { sg ->
                                    val targetWeekIdx = ((sg.durationMonths) * weeksPerMonth).toInt() - 1
                                    weekIdx == targetWeekIdx
                                }
                            }
                        }

                        val cellColor = when {
                            isGoalDeadlineWeek -> Color(0xFF222222)
                            level == 1 -> GridLevel1
                            level == 2 -> GridLevel2
                            level == 3 -> GridLevel3
                            level == 4 -> GridLevel4
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
                                    fontWeight = if (isSelected || isGoalDeadlineWeek) FontWeight.Black else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isGoalDeadlineWeek || level > 0) Color(0xFFFFFFFF)
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
