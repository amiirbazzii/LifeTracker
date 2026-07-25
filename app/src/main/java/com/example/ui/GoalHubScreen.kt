package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Category
import com.example.data.Routine
import com.example.data.Reward
import com.example.data.SubGoal
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalHubScreen(
    grandGoal: String,
    targetYears: Int = 25,
    userPoints: Int,
    categories: List<Category>,
    routines: List<Routine>,
    rewards: List<Reward>,
    subGoals: List<SubGoal> = emptyList(),
    onCreateCategory: (String) -> Unit,
    onCreateRoutine: (String, String, Int) -> Unit,
    onAddReward: (String, Int) -> Unit,
    onClaimReward: (Reward) -> Unit,
    onCreateSubGoal: (String, Int) -> Unit = { _, _ -> },
    onUpdateSubGoal: (String, String, Int) -> Unit = { _, _, _ -> },
    onDeleteSubGoal: (String) -> Unit = {},
    onUpdateCategory: (String, String) -> Unit = { _, _ -> },
    onDeleteCategory: (String) -> Unit = {},
    onUpdateRoutine: (String, String, Int) -> Unit = { _, _, _ -> },
    onDeleteRoutine: (String) -> Unit = {},
    onEditGrandGoal: () -> Unit = {},
    onBack: () -> Unit,
    onSaveGrandGoal: (String) -> Unit = {},
    onReset: () -> Unit = {}
) {
    BackHandler {
        onBack()
    }

    var selectedTab by remember { mutableStateOf(0) }
    
    // Dialog states
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showRoutineDialog by remember { mutableStateOf(false) }
    var activeCategoryIdForRoutine by remember { mutableStateOf<String?>(null) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val activeSectionTitle = when (selectedTab) {
        0 -> "CATEGORIES"
        1 -> "REWARDS"
        2 -> "OBJECTIVES"
        else -> "SETTINGS"
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = DesignTokens.PaddingLarge, vertical = DesignTokens.PaddingMedium)
            ) {
                BaseSystemHeader(
                    mode = SystemHeaderMode.GOAL_HUB,
                    activeSectionTitle = activeSectionTitle,
                    userPoints = userPoints,
                    onBack = onBack
                )
            }
        },
        bottomBar = {
            SharedBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Render active sub-screen based on selectedTab
            when (selectedTab) {
                0 -> {
                    GoalTreeTab(
                        grandGoal = grandGoal,
                        categories = categories,
                        routines = routines,
                        onAddCategoryClick = { showCategoryDialog = true },
                        onAddRoutineClick = { catId ->
                            activeCategoryIdForRoutine = catId
                            showRoutineDialog = true
                        },
                        onUpdateCategory = onUpdateCategory,
                        onDeleteCategory = onDeleteCategory,
                        onUpdateRoutine = onUpdateRoutine,
                        onDeleteRoutine = onDeleteRoutine
                    )
                }
                1 -> {
                    RewardsShopTab(
                        rewards = rewards,
                        userPoints = userPoints,
                        onAddRewardClick = { showRewardDialog = true },
                        onClaimReward = onClaimReward
                    )
                }
                2 -> {
                    ObjectiveTab(
                        currentGoal = grandGoal,
                        targetYears = targetYears,
                        subGoals = subGoals,
                        onSaveGoal = onSaveGrandGoal,
                        onCreateSubGoal = onCreateSubGoal,
                        onUpdateSubGoal = onUpdateSubGoal,
                        onDeleteSubGoal = onDeleteSubGoal
                    )
                }
                3 -> {
                    SettingsTab(
                        userPoints = userPoints,
                        categoriesCount = categories.size,
                        routinesCount = routines.size,
                        rewardsCount = rewards.size,
                        onResetClick = { showResetConfirmDialog = true }
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    // Create Category Dialog
    if (showCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            title = {
                Text(
                    text = "ADD CATEGORY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a category name to organize your sub-goals.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Zinc500
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_name_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        placeholder = { Text("e.g., Soft Skills", fontFamily = FontFamily.Monospace) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (catName.isNotBlank()) {
                            onCreateCategory(catName.trim())
                            showCategoryDialog = false
                        }
                    },
                    modifier = Modifier.testTag("add_category_button")
                ) {
                    Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("CANCEL", fontFamily = FontFamily.Monospace, color = Zinc500)
                }
            }
        )
    }

    // Create Routine Dialog
    if (showRoutineDialog && activeCategoryIdForRoutine != null) {
        var routineTitle by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("4") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showRoutineDialog = false },
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            title = {
                Text(
                    text = "ADD ROUTINE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Set a routine title and target monthly count.",
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
                            .testTag("routine_title_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        placeholder = { Text("e.g., Read 2 books", fontFamily = FontFamily.Monospace) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Target Monthly Count", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("routine_target_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        placeholder = { Text("4", fontFamily = FontFamily.Monospace) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true
                    )

                    errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                        Text(text = msg, color = Color.Red, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val targetVal = targetText.toIntOrNull()
                        if (routineTitle.isBlank()) {
                            errorMessage = "Title cannot be blank"
                        } else if (targetVal == null || targetVal <= 0) {
                            errorMessage = "Enter valid positive count"
                        } else {
                            onCreateRoutine(activeCategoryIdForRoutine!!, routineTitle.trim(), targetVal)
                            showRoutineDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_routine_button")
                ) {
                    Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoutineDialog = false }) {
                    Text("CANCEL", fontFamily = FontFamily.Monospace, color = Zinc500)
                }
            }
        )
    }

    // Create Reward Dialog
    if (showRewardDialog) {
        var rewardName by remember { mutableStateOf("") }
        var costText by remember { mutableStateOf("100") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showRewardDialog = false },
            shape = RoundedCornerShape(DesignTokens.PaddingZero),
            title = {
                Text(
                    text = "ADD REWARD",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a reward title and required points.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Zinc500
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingMedium))

                    OutlinedTextField(
                        value = rewardName,
                        onValueChange = { rewardName = it },
                        label = { Text("Reward Name", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reward_name_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        placeholder = { Text("e.g., 1 hour gaming time", fontFamily = FontFamily.Monospace) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Point Cost", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reward_cost_input"),
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        placeholder = { Text("100", fontFamily = FontFamily.Monospace) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true
                    )

                    errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                        Text(text = msg, color = Color.Red, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val costVal = costText.toIntOrNull()
                        if (rewardName.isBlank()) {
                            errorMessage = "Name cannot be blank"
                        } else if (costVal == null || costVal < 0) {
                            errorMessage = "Enter valid non-negative cost"
                        } else {
                            onAddReward(rewardName.trim(), costVal)
                            showRewardDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_reward_button")
                ) {
                    Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRewardDialog = false }) {
                    Text("CANCEL", fontFamily = FontFamily.Monospace, color = Zinc500)
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
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
                        showResetConfirmDialog = false
                        onReset()
                    },
                    modifier = Modifier.testTag("hub_reset_confirm_btn")
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
                TextButton(onClick = { showResetConfirmDialog = false }) {
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
