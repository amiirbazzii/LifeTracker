package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubGoal
import com.example.ui.theme.*

@Composable
fun ObjectiveTab(
    currentGoal: String,
    targetYears: Int = 25,
    subGoals: List<SubGoal> = emptyList(),
    onSaveGoal: (String) -> Unit,
    onCreateSubGoal: (String, Int) -> Unit = { _, _ -> },
    onUpdateSubGoal: (String, String, Int) -> Unit = { _, _, _ -> },
    onDeleteSubGoal: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalMonthLimit = targetYears * 12
    var mainGoalInput by remember(currentGoal) { mutableStateOf(currentGoal) }
    var subGoalTitleInput by remember { mutableStateOf("") }
    var subGoalDurationInput by remember { mutableStateOf("") }
    var formErrorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog state for editing/deleting items
    var editingSubGoal by remember { mutableStateOf<SubGoal?>(null) }
    var showEditMainGoalDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(DesignTokens.PaddingLarge)
    ) {
        // --- 1. HEADER SECTION ---
        Text(
            text = "GOAL TREE OBJECTIVE PROTOCOL",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = Zinc500,
                fontWeight = FontWeight.Bold,
                letterSpacing = DesignTokens.LetterSpacingWide
            )
        )

        Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

        // --- 2. EMPTY STATE VS MAIN GOAL ROOT NODE ---
        if (currentGoal.isBlank()) {
            // Empty State Prompt
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        DesignTokens.StrokeThick,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        RoundedCornerShape(DesignTokens.PaddingZero)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(DesignTokens.PaddingLarge)
                    .testTag("objective_empty_state_card")
            ) {
                Column {
                    Text(
                        text = "[EMPTY STATE] NO MAIN OBJECTIVE SET",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                    Text(
                        text = "Please enter your main $targetYears-year goal to initialize the root node of your Goal Tree before assigning sub-goals.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Zinc500,
                            lineHeight = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                    OutlinedTextField(
                        value = mainGoalInput,
                        onValueChange = { mainGoalInput = it },
                        placeholder = {
                            Text(
                                text = "e.g., MASTER FULL-STACK ANDROID & AI DEVELOPMENT",
                                style = MaterialTheme.typography.bodyMedium.copy(
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
                            .testTag("main_goal_input_field"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                    Button(
                        onClick = {
                            if (mainGoalInput.isNotBlank()) {
                                onSaveGoal(mainGoalInput.trim())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DesignTokens.ButtonHeight)
                            .testTag("init_main_goal_button"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GridLevel4,
                            contentColor = MonochromeBlack
                        )
                    ) {
                        Text(
                            text = "INITIALIZE ROOT OBJECTIVE",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = DesignTokens.LetterSpacingWide
                            )
                        )
                    }
                }
            }
        } else {
            // Main Goal Rendered as Root Node of Goal Tree
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        DesignTokens.StrokeThick,
                        GridLevel4,
                        RoundedCornerShape(DesignTokens.PaddingZero)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { showEditMainGoalDialog = true }
                    .padding(DesignTokens.PaddingMedium)
                    .testTag("main_goal_root_node")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ROOT GOAL NODE (MAIN OBJECTIVE)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = GridLevel4,
                                letterSpacing = DesignTokens.LetterSpacingWide
                            )
                        )
                        Box(
                            modifier = Modifier
                                .background(GridLevel4, RoundedCornerShape(DesignTokens.PaddingZero))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TAP TO EDIT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    color = MonochromeBlack
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                    Text(
                        text = currentGoal.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TIME HORIZON: $targetYears YEARS ($totalMonthLimit MONTHS)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Zinc500
                            )
                        )
                        Text(
                            text = "${subGoals.size} SUB-GOALS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = GridLevel4
                            )
                        )
                    }
                }
            }

            // Tree stem connector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(GridLevel4)
                )
            }

            // --- 3. SUB-GOAL CREATION FORM ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        DesignTokens.StrokeMedium,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        RoundedCornerShape(DesignTokens.PaddingZero)
                    )
                    .background(MaterialTheme.colorScheme.background)
                    .padding(DesignTokens.PaddingMedium)
                    .testTag("sub_goal_creation_form")
            ) {
                Column {
                    Text(
                        text = "CREATE SUB-GOAL BRANCH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = DesignTokens.LetterSpacingWide
                        )
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                    // Field 1: Goal Title
                    OutlinedTextField(
                        value = subGoalTitleInput,
                        onValueChange = {
                            subGoalTitleInput = it
                            formErrorMessage = null
                        },
                        label = {
                            Text("Goal Title", fontFamily = FontFamily.Monospace)
                        },
                        placeholder = {
                            Text("e.g., Master Jetpack Compose & Clean Architecture", fontFamily = FontFamily.Monospace, color = Zinc500)
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sub_goal_title_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                    // Field 2: Duration in Months
                    OutlinedTextField(
                        value = subGoalDurationInput,
                        onValueChange = {
                            subGoalDurationInput = it
                            formErrorMessage = null
                        },
                        label = {
                            Text("Duration (in months)", fontFamily = FontFamily.Monospace)
                        },
                        placeholder = {
                            Text("e.g., 6 (max $totalMonthLimit)", fontFamily = FontFamily.Monospace, color = Zinc500)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sub_goal_duration_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        singleLine = true
                    )

                    // Form Validation Error Message
                    formErrorMessage?.let { errorMsg ->
                        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                        Text(
                            text = "⚠ $errorMsg",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.testTag("sub_goal_form_error")
                        )
                    }

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                    Button(
                        onClick = {
                            val title = subGoalTitleInput.trim()
                            val duration = subGoalDurationInput.toIntOrNull()

                            if (title.isBlank()) {
                                formErrorMessage = "Goal Title cannot be empty."
                            } else if (duration == null || duration <= 0) {
                                formErrorMessage = "Duration must be a positive number of months."
                            } else if (duration > totalMonthLimit) {
                                formErrorMessage = "Duration ($duration months) exceeds main goal limit ($totalMonthLimit months)."
                            } else {
                                onCreateSubGoal(title, duration)
                                subGoalTitleInput = ""
                                subGoalDurationInput = ""
                                formErrorMessage = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DesignTokens.ButtonHeight)
                            .testTag("add_sub_goal_button"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Sub Goal",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.PaddingSmall))
                            Text(
                                text = "COMMIT SUB-GOAL TO TREE",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

            // --- 4. GOAL TREE BRANCH NODES LIST ---
            Text(
                text = "GOAL TREE BRANCHES (${subGoals.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Zinc500,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = DesignTokens.LetterSpacingWide
                )
            )

            Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

            if (subGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            DesignTokens.StrokeThin,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .padding(DesignTokens.PaddingLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO SUB-GOALS CREATED YET",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Zinc500
                        )
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingSmall)
                ) {
                    subGoals.forEachIndexed { index, subGoal ->
                        val startMonth = subGoal.startMonth
                        val endMonth = startMonth + subGoal.durationMonths
                        val weeksPerMonth = (targetYears * 52).toDouble() / (targetYears * 12.0)
                        val startWeek = (startMonth * weeksPerMonth).toInt() + 1
                        val endWeek = (endMonth * weeksPerMonth).toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tree connector stem label
                            Text(
                                text = if (index == subGoals.size - 1) "└── " else "├── ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = GridLevel4
                                )
                            )

                            // Sub-Goal Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        DesignTokens.StrokeMedium,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        RoundedCornerShape(DesignTokens.PaddingZero)
                                    )
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { editingSubGoal = subGoal }
                                    .padding(DesignTokens.PaddingMedium)
                                    .testTag("sub_goal_card_${subGoal.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subGoal.title.uppercase(),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "MONTH $startMonth - $endMonth  •  WEEKS $startWeek - $endWeek",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                color = Zinc500,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(DesignTokens.PaddingSmall))

                                    Box(
                                        modifier = Modifier
                                            .background(GridLevel4)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${subGoal.durationMonths} MOS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Black,
                                                color = MonochromeBlack,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- EDIT MAIN GOAL DIALOG ---
    if (showEditMainGoalDialog) {
        var editMainInput by remember { mutableStateOf(currentGoal) }
        AlertDialog(
            onDismissRequest = { showEditMainGoalDialog = false },
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            title = {
                Text(
                    text = "RECONFIGURE ROOT GOAL",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Modify your core 25-Year Objective statement.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Zinc500
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))
                    OutlinedTextField(
                        value = editMainInput,
                        onValueChange = { editMainInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_main_goal_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editMainInput.isNotBlank()) {
                            onSaveGoal(editMainInput.trim())
                            showEditMainGoalDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_main_goal_button")
                ) {
                    Text("SAVE CHANGES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditMainGoalDialog = false }) {
                    Text("ABORT", fontFamily = FontFamily.Monospace, color = Zinc500)
                }
            }
        )
    }

    // --- EDIT / DELETE SUB-GOAL DIALOG ---
    editingSubGoal?.let { subGoal ->
        var editTitle by remember { mutableStateOf(subGoal.title) }
        var editDuration by remember { mutableStateOf(subGoal.durationMonths.toString()) }
        var dialogErrorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { editingSubGoal = null },
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            title = {
                Text(
                    text = "EDIT SUB-GOAL ITEM",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Update or delete this goal tree node.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Zinc500
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = {
                            editTitle = it
                            dialogErrorMsg = null
                        },
                        label = { Text("Goal Title", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_sub_goal_title_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                    OutlinedTextField(
                        value = editDuration,
                        onValueChange = {
                            editDuration = it
                            dialogErrorMsg = null
                        },
                        label = { Text("Duration (months)", fontFamily = FontFamily.Monospace) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_sub_goal_duration_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )

                    dialogErrorMsg?.let { error ->
                        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                        Text(
                            text = "⚠ $error",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            onDeleteSubGoal(subGoal.id)
                            editingSubGoal = null
                        },
                        modifier = Modifier.testTag("delete_sub_goal_button")
                    ) {
                        Text("DELETE", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(DesignTokens.PaddingSmall))

                    TextButton(
                        onClick = {
                            val newTitle = editTitle.trim()
                            val newDur = editDuration.toIntOrNull()
                            if (newTitle.isBlank()) {
                                dialogErrorMsg = "Title cannot be blank."
                            } else if (newDur == null || newDur <= 0) {
                                dialogErrorMsg = "Enter valid duration."
                            } else if (newDur > totalMonthLimit) {
                                dialogErrorMsg = "Duration cannot exceed $totalMonthLimit months."
                            } else {
                                onUpdateSubGoal(subGoal.id, newTitle, newDur)
                                editingSubGoal = null
                            }
                        },
                        modifier = Modifier.testTag("update_sub_goal_button")
                    ) {
                        Text("UPDATE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSubGoal = null }) {
                    Text("ABORT", fontFamily = FontFamily.Monospace, color = Zinc500)
                }
            }
        )
    }
}
