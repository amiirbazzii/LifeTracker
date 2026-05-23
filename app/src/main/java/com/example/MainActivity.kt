package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DailyTask
import com.example.ui.LifeTrackerUiState
import com.example.ui.LifeTrackerViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    val context = LocalContext.current.applicationContext as android.app.Application
                    val viewModel: LifeTrackerViewModel = viewModel(
                        factory = LifeTrackerViewModel.Factory(context)
                    )
                    LifeTrackerApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LifeTrackerApp(
    viewModel: LifeTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val uiState = state) {
            is LifeTrackerUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is LifeTrackerUiState.Onboarding -> {
                OnboardingScreen(
                    onInitialize = { years ->
                        viewModel.initializeTimeline(years)
                    }
                )
            }
            is LifeTrackerUiState.Dashboard -> {
                DashboardScreen(
                    state = uiState,
                    onSelectWeek = { weekIndex -> viewModel.selectWeek(weekIndex) },
                    onAddTask = { title, weekIndex, day -> viewModel.addTask(title, weekIndex, day) },
                    onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                    onDeleteTask = { taskId -> viewModel.deleteTask(taskId) },
                    onReset = { viewModel.resetTimeline() }
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    onInitialize: (Int) -> Unit
) {
    var yearsText by remember { mutableStateOf("5") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stark Brutalist Minimal Title
        Text(
            text = "LIFETRACKER.sys",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "PROTOCOL v1.0.0 // LOCAL-FIRST TIMELINE MATRIX",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Zinc500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Geometric boundary box (0.dp rounded corners)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(0.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "THE GRAND MATRIX PROMPT",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Specify timeline scope to map out your execution matrix.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Zinc400,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Selector row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(0.dp))
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
                            .size(44.dp)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(0.dp))
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TARGET CONFIGURATION: YEARS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Zinc500
                )

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ERROR: $msg",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val years = yearsText.toIntOrNull()
                        if (years == null || years <= 0) {
                            errorMessage = "Please enter a valid positive duration."
                        } else {
                            onInitialize(years)
                        }
                    },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GridLevel4, // Neon Green contrast button!
                        contentColor = MonochromeBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("initialize_button")
                ) {
                    Text(
                        text = "INITIALIZE TIMELINE MATRIX",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: LifeTrackerUiState.Dashboard,
    onSelectWeek: (Int) -> Unit,
    onAddTask: (String, Int, Int) -> Unit,
    onToggleTask: (DailyTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onReset: () -> Unit
) {
    val currentCalendarDay = remember {
        val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
    
    var newTaskTitle by remember { mutableStateOf("") }
    var taskDayOfWeek by remember { mutableStateOf(currentCalendarDay) }

    LaunchedEffect(state.selectedWeekIndex) {
        if (state.selectedWeekIndex != state.currentWeekIndex) {
            taskDayOfWeek = 1
        } else {
            taskDayOfWeek = currentCalendarDay
        }
    }

    val isHistorical = state.selectedWeekIndex < state.currentWeekIndex
    val isKeyboardOpen = WindowInsets.isImeVisible

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // App Header conforming to Clean Minimalism HTML mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), RoundedCornerShape(0.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "PROTOCOL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Zinc500,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "LIFETRACKER.sys",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "100% LOCAL-FIRST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GridLevel4,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "v1.0.0-PROD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Zinc400,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                }

                // Clean Reset Box
                var showResetAlert by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(0.dp))
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
                        shape = RoundedCornerShape(0.dp),
                        title = {
                            Text(
                                text = "RESET TIMELINE CONFIG?",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        },
                        text = {
                            Text(
                                text = "This will permanently remove your timeline matrix configuration and delete all logged tasks across all weeks.",
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
                                    "RESET",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetAlert = false }) {
                                Text(
                                    "CANCEL",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    )
                }
            }
        }

        if (!isKeyboardOpen) {
            Spacer(modifier = Modifier.height(12.dp))

            // --- TOP HALF: MACRO GRID WINDOW ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(0.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${state.meta.targetYears}Y GOAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 9.sp
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
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(0.dp))
                        .background(if (isSystemInDarkTheme()) GridLevel0_Dark else Color(0xFFFCFCFC))
                        .padding(4.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(13),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
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
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    BorderStroke(1.5.dp, GridLevel4)
                                }

                                Row(
                                    modifier = Modifier
                                        .aspectRatio(7f)
                                        .clip(RoundedCornerShape(0.dp))
                                        .background(if (isDark) GridLevel0_Dark else Color(0xFFF0F0F0))
                                        .border(itemStroke, RoundedCornerShape(0.dp))
                                        .clickable {
                                            onSelectWeek(weekIdx)
                                        }
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
                                                sr <= 25f -> 1
                                                sr <= 50f -> 2
                                                sr <= 75f -> 3
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

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(dayColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = d.toString(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (dayLevel > 0) MonochromeWhite 
                                                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                                )
                                            )
                                        }

                                        if (d < 7) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .width(1.dp)
                                                    .background(
                                                        if (isDark) Color(0xFF1F1F1F) else Color(0xFFDDDDDD)
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
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                }

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(0.dp))
                                        .background(cellColor)
                                        .border(itemStroke, RoundedCornerShape(0.dp))
                                        .clickable {
                                            onSelectWeek(weekIdx)
                                        }
                                        .testTag("week_box_$weekIdx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (weekIdx + 1).toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 7.5.sp,
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

                Spacer(modifier = Modifier.height(4.dp))

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
                            fontSize = 9.sp,
                            color = Zinc500,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    LegendPanel()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- BOTTOM HALF: MICRO PERSISTENT TASK MANAGER ---
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxWidth()
        ) {
            val completedCount = state.selectedWeekTasks.count { it.isCompleted == 1 }
            val totalCount = state.selectedWeekTasks.size
            val completionRate = if (totalCount == 0) 0 else (completedCount * 100 / totalCount)

            // Dynamic header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "WEEK ${state.selectedWeekIndex + 1} : EXECUTION",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$completedCount OF $totalCount TASKS COMPLETED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
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
                            letterSpacing = (-1).sp
                        )
                    )
                    val level = when {
                        totalCount == 0 -> 0
                        else -> {
                            val sr = (completedCount.toFloat() / totalCount.toFloat()) * 100f
                            when {
                                sr == 0f -> 0
                                sr <= 25f -> 1
                                sr <= 50f -> 2
                                sr <= 75f -> 3
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
                if (state.selectedWeekTasks.isEmpty()) {
                    EmptyStatePanel(isHistorical = isHistorical)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            count = state.selectedWeekTasks.size,
                            key = { index -> state.selectedWeekTasks[index].taskId }
                        ) { index ->
                            val task = state.selectedWeekTasks[index]
                            TaskItemRow(
                                task = task,
                                isReadOnly = isHistorical,
                                onToggle = onToggleTask,
                                onDelete = onDeleteTask
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task creating panel
            if (isHistorical) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(0.dp))
                        .padding(12.dp),
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HISTORICAL RECORD IS READ-ONLY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = Zinc500
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(0.dp))
                        .background(MaterialTheme.colorScheme.background),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dayAbbrs = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    
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
                            Text(
                                text = dayAbbrs[taskDayOfWeek - 1],
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
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
                            .width(1.dp)
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
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
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
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("task_input_field")
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(52.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        // Systemic status footer matching the minimal mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SQLITE_PERSISTENCE: ON",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            )
            Text(
                text = "SECURE_LOCAL_ENCRYPTION_ACTIVE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
fun TaskItemRow(
    task: DailyTask,
    isReadOnly: Boolean,
    onToggle: (DailyTask) -> Unit,
    onDelete: (String) -> Unit
) {
    val isCompleted = task.isCompleted == 1
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .background(
                if (isCompleted) MaterialTheme.colorScheme.primary 
                else if (isDark) Zinc900 else Color(0xFFF9F9F9)
            )
            .border(
                border = BorderStroke(
                    1.dp,
                    if (isCompleted) Color.Transparent
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .clickable(enabled = !isReadOnly) {
                onToggle(task)
            }
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .testTag("task_row_${task.taskId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Geometric checkbox
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(
                    BorderStroke(2.dp, if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary),
                    RoundedCornerShape(0.dp)
                )
                .background(
                    if (isCompleted) MaterialTheme.colorScheme.onPrimary else Color.Transparent
                )
                .clickable(enabled = !isReadOnly) {
                    onToggle(task)
                }
                .testTag("checkbox_${task.taskId}"),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Stark minimalist day badge prefix
        val dayLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val dayLabel = if (task.dayOfWeek in 1..7) dayLabels[task.dayOfWeek - 1] else "MON"
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            ),
            color = if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else GridLevel4,
            modifier = Modifier
                .background(
                    if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f) 
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    RoundedCornerShape(0.dp)
                )
                .border(
                    1.dp,
                    if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f) 
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    RoundedCornerShape(0.dp)
                )
                .padding(horizontal = 5.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = task.taskTitle.uppercase(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        if (!isReadOnly) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDelete(task.taskId) }
                    .testTag("delete_button_${task.taskId}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Task",
                    tint = if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun LegendPanel() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "0% ",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Zinc500
        )

        val isDark = isSystemInDarkTheme()
        val legendColors = listOf(
            if (isDark) GridLevel0_Dark else GridLevel0_Light,
            GridLevel1,
            GridLevel2,
            GridLevel3,
            GridLevel4
        )

        legendColors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color)
                    .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(0.dp))
            )
            Spacer(modifier = Modifier.width(2.dp))
        }

        Text(
            text = " 100%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Zinc500
        )
    }
}

@Composable
fun EmptyStatePanel(
    isHistorical: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isHistorical) Icons.Outlined.Info else Icons.Outlined.CheckCircle,
            contentDescription = "Empty list",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isHistorical) "NO HISTORY RECORDED" else "MOMENTUM EMPTY",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isHistorical) {
                "No tasks were logged during this historical period."
            } else {
                "Enter objectives below to start logging execution details."
            },
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            textAlign = TextAlign.Center
        )
    }
}

fun formatWeekRange(inceptionTimestamp: Long, weekIndex: Int): String {
    val weekStartMillis = inceptionTimestamp + weekIndex * 7L * 24L * 60L * 60L * 1000L
    val weekEndMillis = weekStartMillis + 6L * 24L * 60L * 60L * 1000L
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
    return "${sdf.format(Date(weekStartMillis))} — ${sdf.format(Date(weekEndMillis))}"
}
