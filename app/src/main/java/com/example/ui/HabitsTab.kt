package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DailyHabit
import com.example.data.DailyTask
import com.example.ui.theme.DesignTokens
import com.example.ui.theme.GridLevel1
import com.example.ui.theme.GridLevel4
import com.example.ui.theme.MonochromeBlack
import com.example.ui.theme.MonochromeWhite
import com.example.ui.theme.Zinc500

@Composable
fun HabitsTab(
    habits: List<DailyHabit>,
    tasks: List<DailyTask> = emptyList(),
    onCreateHabit: (String) -> Unit,
    onUpdateHabit: (String, String) -> Unit,
    onDeleteHabit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val primaryLabelColor: Color = if (isDark) MonochromeWhite else MonochromeBlack

    var newHabitTitle by remember { mutableStateOf("") }
    var editingHabit by remember { mutableStateOf<DailyHabit?>(null) }
    var deletingHabit by remember { mutableStateOf<DailyHabit?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = DesignTokens.PaddingLarge)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMedium),
            contentPadding = PaddingValues(top = DesignTokens.PaddingSmall, bottom = 80.dp)
        ) {
            // --- HEADER INFO ---
            item {
                Text(
                    text = "RECURRING DAILY PROTOCOLS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = DesignTokens.LetterSpacingWide
                    ),
                    color = Zinc500
                )
                Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))
                Text(
                    text = "Habits are automatically injected into your daily active schedule every day.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Zinc500
                    )
                )
                Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                // --- ADD HABIT INPUT ROW ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.PaddingSmall)
                ) {
                    OutlinedTextField(
                        value = newHabitTitle,
                        onValueChange = { newHabitTitle = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("habit_title_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        placeholder = {
                            Text(
                                "NEW HABIT TITLE...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = DesignTokens.FontSizeSmall,
                                color = Zinc500
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GridLevel4,
                            unfocusedBorderColor = primaryLabelColor.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Button(
                        onClick = {
                            if (newHabitTitle.isNotBlank()) {
                                onCreateHabit(newHabitTitle.trim())
                                newHabitTitle = ""
                            }
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("add_habit_button")
                    ) {
                        Text(
                            "+ ADD HABIT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = DesignTokens.FontSizeSmall
                        )
                    }
                }
            }

            // --- SECTION TITLE ---
            item {
                Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                Text(
                    text = "ACTIVE HABITS (${habits.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = DesignTokens.LetterSpacingWide
                    ),
                    color = Zinc500
                )
            }

            // --- HABIT ITEMS ---
            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                DesignTokens.StrokeThin,
                                primaryLabelColor.copy(alpha = 0.15f),
                                RoundedCornerShape(DesignTokens.PaddingZero)
                            )
                            .padding(DesignTokens.PaddingExtraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NO DAILY HABITS CONFIGURED",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Zinc500
                            )
                            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                            Text(
                                text = "Add habits above to automatically populate your daily task list every morning.",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Zinc500
                            )
                        }
                    }
                }
            } else {
                items(habits, key = { it.id }) { habit ->
                    val habitTasks = remember(tasks, habit.id) { tasks.filter { it.habitId == habit.id } }
                    val completedTasksCount = remember(habitTasks) { habitTasks.count { it.isCompleted == 1 } }
                    val uncompletedTasksCount = remember(habitTasks) { habitTasks.count { it.isCompleted == 0 } }

                    HabitCard(
                        habit = habit,
                        completedCount = completedTasksCount,
                        uncompletedCount = uncompletedTasksCount,
                        onEdit = { editingHabit = habit },
                        onDelete = { deletingHabit = habit }
                    )
                }
            }
        }
    }

    // --- EDIT HABIT DIALOG ---
    if (editingHabit != null) {
        var editedTitle by remember { mutableStateOf(editingHabit!!.title) }
        AlertDialog(
            onDismissRequest = { editingHabit = null },
            title = {
                Text(
                    "EDIT HABIT",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_habit_title_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        label = { Text("HABIT TITLE", fontFamily = FontFamily.Monospace) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GridLevel4,
                            unfocusedBorderColor = primaryLabelColor.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedTitle.isNotBlank()) {
                            onUpdateHabit(editingHabit!!.id, editedTitle.trim())
                            editingHabit = null
                        }
                    },
                    shape = RoundedCornerShape(DesignTokens.PaddingZero),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("save_edit_habit_button")
                ) {
                    Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingHabit = null },
                    shape = RoundedCornerShape(DesignTokens.PaddingZero)
                ) {
                    Text("CANCEL", fontFamily = FontFamily.Monospace, color = Zinc500)
                }
            },
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.border(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary, RoundedCornerShape(DesignTokens.PaddingZero))
        )
    }

    // --- DELETE HABIT CONFIRMATION DIALOG ---
    if (deletingHabit != null) {
        AlertDialog(
            onDismissRequest = { deletingHabit = null },
            title = {
                Text(
                    "DELETE HABIT",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete '${deletingHabit!!.title}'? It will also be removed from today's uncompleted daily tasks.",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHabit(deletingHabit!!.id)
                        deletingHabit = null
                    },
                    shape = RoundedCornerShape(DesignTokens.PaddingZero),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("confirm_delete_habit_button")
                ) {
                    Text("DELETE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingHabit = null },
                    shape = RoundedCornerShape(DesignTokens.PaddingZero)
                ) {
                    Text("CANCEL", fontFamily = FontFamily.Monospace, color = Zinc500)
                }
            },
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.border(DesignTokens.StrokeMedium, MaterialTheme.colorScheme.primary, RoundedCornerShape(DesignTokens.PaddingZero))
        )
    }
}

@Composable
private fun HabitCard(
    habit: DailyHabit,
    completedCount: Int,
    uncompletedCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val primaryLabelColor: Color = if (isDark) MonochromeWhite else MonochromeBlack
    val totalCount = completedCount + uncompletedCount

    // Short summary text
    val shortSummaryText = when {
        totalCount == 0 -> "NO LOGS YET"
        uncompletedCount == 0 -> "ALL COMPLETED"
        completedCount == 0 -> "0 DONE · $uncompletedCount PENDING"
        else -> "$completedCount DONE · $uncompletedCount PENDING"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(
                DesignTokens.StrokeMedium,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .padding(DesignTokens.PaddingMedium)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Title, Recurring tag, and Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "↻",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = GridLevel4,
                        modifier = Modifier.padding(end = DesignTokens.PaddingSmall)
                    )
                    Column {
                        Text(
                            text = habit.title.uppercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "DAILY RECURRING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = DesignTokens.FontSizeTiny,
                                color = Zinc500
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).testTag("edit_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit habit",
                            tint = primaryLabelColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete habit",
                            tint = primaryLabelColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

            // --- TASK PROGRESS STATS SECTION ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        DesignTokens.StrokeThin,
                        primaryLabelColor.copy(alpha = 0.15f),
                        RoundedCornerShape(DesignTokens.PaddingZero)
                    )
                    .background(if (isDark) GridLevel1.copy(alpha = 0.4f) else GridLevel1.copy(alpha = 0.2f))
                    .padding(horizontal = DesignTokens.PaddingMedium, vertical = DesignTokens.PaddingSmall)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Completed tasks metric
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GridLevel4
                            )
                            Text(
                                text = "COMPLETED: $completedCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = DesignTokens.FontSizeTiny
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "|",
                            color = Zinc500.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace
                        )

                        // Uncompleted tasks metric
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "○",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Zinc500
                            )
                            Text(
                                text = "UNCOMPLETED: $uncompletedCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = DesignTokens.FontSizeTiny
                                ),
                                color = Zinc500
                            )
                        }
                    }

                    // Short text badge
                    Text(
                        text = shortSummaryText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = DesignTokens.FontSizeTiny,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (totalCount > 0 && uncompletedCount == 0) GridLevel4 else Zinc500
                    )
                }
            }
        }
    }
}
