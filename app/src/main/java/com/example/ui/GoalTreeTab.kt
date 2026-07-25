package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Category
import com.example.data.Routine
import com.example.ui.theme.*

@Composable
fun GoalTreeTab(
    grandGoal: String,
    categories: List<Category>,
    routines: List<Routine>,
    onAddCategoryClick: () -> Unit,
    onAddRoutineClick: (String) -> Unit,
    onUpdateCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onUpdateRoutine: (String, String, Int) -> Unit,
    onDeleteRoutine: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val primaryLabelColor = if (isDark) MonochromeWhite else MonochromeBlack

    // State for managing Edit/Delete dialogs
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var editingRoutine by remember { mutableStateOf<Routine?>(null) }

    val routinesByCategory = remember(routines) {
        routines.groupBy { it.categoryId }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (categories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DesignTokens.PaddingExtraLarge),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NO CATEGORIES CREATED",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Zinc500,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                Text(
                    text = "Add categories to organize your routines and track progress.",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Zinc500,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))
                Button(
                    onClick = onAddCategoryClick,
                    shape = RoundedCornerShape(DesignTokens.PaddingZero),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("add_category_button_empty")
                ) {
                    Text("ADD CATEGORY", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.PaddingLarge, vertical = DesignTokens.PaddingSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE CATEGORIES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Zinc500
                    )
                    
                    TextButton(
                        onClick = onAddCategoryClick,
                        modifier = Modifier.testTag("add_category_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Category",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.PaddingTiny))
                        Text("ADD CATEGORY", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = DesignTokens.PaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMedium),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        val categoryRoutines = remember(routinesByCategory, category.id) {
                            routinesByCategory[category.id] ?: emptyList()
                        }
                        CategoryCard(
                            category = category,
                            routines = categoryRoutines,
                            onAddRoutineClick = { onAddRoutineClick(category.id) },
                            onCategoryClick = { editingCategory = it },
                            onRoutineClick = { editingRoutine = it }
                        )
                    }
                }
            }
        }
    }

    // --- Brutalist Styled Popups for Item Management (Edit & Delete Operations) ---

    // Edit/Delete Category Dialog
    editingCategory?.let { category ->
        var catName by remember(category) { mutableStateOf(category.name) }
        var showConfirmDelete by remember { mutableStateOf(false) }

        if (!showConfirmDelete) {
            AlertDialog(
                onDismissRequest = { editingCategory = null },
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .padding(24.dp)
                    .border(DesignTokens.StrokeThick, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero)),
                title = {
                    Text(
                        text = "EDIT CATEGORY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = primaryLabelColor
                        )
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Update the category name or delete it.",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Zinc500
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))
                        
                        OutlinedTextField(
                            value = catName,
                            onValueChange = { catName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_category_name_input"),
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            placeholder = { Text("e.g., Soft Skills", fontFamily = FontFamily.Monospace) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GridLevel4,
                                unfocusedBorderColor = primaryLabelColor.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

                        // Warning Decommission button
                        Button(
                            onClick = { showConfirmDelete = true },
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Red
                            ),
                            border = androidx.compose.foundation.BorderStroke(DesignTokens.StrokeMedium, Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("delete_category_trigger")
                        ) {
                            Text(
                                "DELETE CATEGORY & ROUTINES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (catName.isNotBlank()) {
                                onUpdateCategory(category.id, catName.trim())
                                editingCategory = null
                            }
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryLabelColor,
                            contentColor = if (isDark) MonochromeBlack else MonochromeWhite
                        ),
                        modifier = Modifier.testTag("save_category_changes")
                    ) {
                        Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { editingCategory = null }
                    ) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, color = Zinc500)
                    }
                }
            )
        } else {
            // Symmetrical confirmation of destruction
            AlertDialog(
                onDismissRequest = { showConfirmDelete = false },
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                modifier = Modifier.border(DesignTokens.StrokeThick, Color.Red, RoundedCornerShape(DesignTokens.PaddingZero)),
                title = {
                    Text(
                        text = "DELETE CATEGORY?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete category '${category.name.uppercase()}'? This will permanently delete all associated routines.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = primaryLabelColor
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteCategory(category.id)
                            showConfirmDelete = false
                            editingCategory = null
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = MonochromeWhite
                        ),
                        modifier = Modifier.testTag("confirm_delete_category")
                    ) {
                        Text("DELETE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDelete = false }
                    ) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, color = Zinc500)
                    }
                }
            )
        }
    }

    // Edit/Delete Routine Dialog
    editingRoutine?.let { routine ->
        var routineTitle by remember(routine) { mutableStateOf(routine.title) }
        var targetText by remember(routine) { mutableStateOf(routine.targetCount.toString()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showConfirmDelete by remember { mutableStateOf(false) }

        if (!showConfirmDelete) {
            AlertDialog(
                onDismissRequest = { editingRoutine = null },
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .padding(24.dp)
                    .border(DesignTokens.StrokeThick, primaryLabelColor, RoundedCornerShape(DesignTokens.PaddingZero)),
                title = {
                    Text(
                        text = "EDIT ROUTINE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = primaryLabelColor
                        )
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Update the routine title and target monthly count.",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Zinc500
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                        OutlinedTextField(
                            value = routineTitle,
                            onValueChange = { routineTitle = it },
                            label = { Text("Routine Title", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_routine_title_input"),
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GridLevel4,
                                unfocusedBorderColor = primaryLabelColor.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                        OutlinedTextField(
                            value = targetText,
                            onValueChange = { targetText = it },
                            label = { Text("Target Monthly Count", fontFamily = FontFamily.Monospace) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_routine_target_input"),
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GridLevel4,
                                unfocusedBorderColor = primaryLabelColor.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        errorMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                            Text(text = msg, color = Color.Red, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))

                        // Delete button
                        Button(
                            onClick = { showConfirmDelete = true },
                            shape = RoundedCornerShape(DesignTokens.PaddingZero),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Red
                            ),
                            border = androidx.compose.foundation.BorderStroke(DesignTokens.StrokeMedium, Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("delete_routine_trigger")
                        ) {
                            Text(
                                "DELETE ROUTINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetVal = targetText.toIntOrNull()
                            if (routineTitle.isBlank()) {
                                errorMessage = "Title cannot be blank"
                            } else if (targetVal == null || targetVal <= 0) {
                                errorMessage = "Enter valid positive count"
                            } else {
                                onUpdateRoutine(routine.id, routineTitle.trim(), targetVal)
                                editingRoutine = null
                            }
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryLabelColor,
                            contentColor = if (isDark) MonochromeBlack else MonochromeWhite
                        ),
                        modifier = Modifier.testTag("save_routine_changes")
                    ) {
                        Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { editingRoutine = null }
                    ) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, color = Zinc500)
                    }
                }
            )
        } else {
            // Confirm delete routine
            AlertDialog(
                onDismissRequest = { showConfirmDelete = false },
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                modifier = Modifier.border(DesignTokens.StrokeThick, Color.Red, RoundedCornerShape(DesignTokens.PaddingZero)),
                title = {
                    Text(
                        text = "DELETE ROUTINE?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete routine '${routine.title.uppercase()}'?",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = primaryLabelColor
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteRoutine(routine.id)
                            showConfirmDelete = false
                            editingRoutine = null
                        },
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = MonochromeWhite
                        ),
                        modifier = Modifier.testTag("confirm_delete_routine")
                    ) {
                        Text("DELETE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmDelete = false }
                    ) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, color = Zinc500)
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    routines: List<Routine>,
    onAddRoutineClick: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onRoutineClick: (Routine) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                DesignTokens.StrokeMedium,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(DesignTokens.PaddingMedium)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Symmetrical Category header with clickable manage "⚙️" icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onCategoryClick(category) }
                        .testTag("edit_category_trigger_${category.id}")
                ) {
                    Text(
                        text = category.name.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚙️",
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Box(
                    modifier = Modifier
                        .border(
                            DesignTokens.StrokeMedium,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .clickable { onAddRoutineClick() }
                        .padding(horizontal = DesignTokens.PaddingSmall, vertical = DesignTokens.PaddingTiny)
                        .testTag("add_routine_trigger_${category.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ ADD ROUTINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

            if (routines.isEmpty()) {
                Text(
                    text = "NO ROUTINES IN THIS CATEGORY.",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Zinc500,
                    modifier = Modifier.padding(vertical = DesignTokens.PaddingSmall)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingSmall)) {
                    routines.forEach { routine ->
                        RoutineItem(
                            routine = routine,
                            onRoutineClick = onRoutineClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineItem(
    routine: Routine,
    onRoutineClick: (Routine) -> Unit
) {
    val progress = if (routine.targetCount > 0) {
        (routine.completedCount.toFloat() / routine.targetCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                DesignTokens.StrokeThin,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .clickable { onRoutineClick(routine) }
            .testTag("routine_item_trigger_${routine.id}")
            .padding(DesignTokens.PaddingSmall)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = routine.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${routine.completedCount} / ${routine.targetCount} MO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (progress >= 1f) GridLevel4 else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

            // Brutalist custom linear progress indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(if (progress >= 1f) GridLevel4 else MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
