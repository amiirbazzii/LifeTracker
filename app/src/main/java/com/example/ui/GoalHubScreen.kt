package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Category
import com.example.data.Routine
import com.example.data.Reward
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalHubScreen(
    grandGoal: String,
    userPoints: Int,
    categories: List<Category>,
    routines: List<Routine>,
    rewards: List<Reward>,
    onCreateCategory: (String) -> Unit,
    onCreateRoutine: (String, String, Int) -> Unit,
    onAddReward: (String, Int) -> Unit,
    onClaimReward: (Reward) -> Unit,
    onEditGrandGoal: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // Dialog states
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showRoutineDialog by remember { mutableStateOf(false) }
    var activeCategoryIdForRoutine by remember { mutableStateOf<String?>(null) }
    var showRewardDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = DesignTokens.PaddingLarge, vertical = DesignTokens.PaddingSmall)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(DesignTokens.ControlBoxSize)
                            .border(
                                DesignTokens.StrokeMedium,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(DesignTokens.PaddingZero)
                            )
                            .testTag("hub_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "MATRIX HUB PROTOCOL",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = DesignTokens.LetterSpacingWide
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(DesignTokens.ControlBoxSize))
                }

                Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))

                // Points & Grand Goal Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            DesignTokens.StrokeThick,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onEditGrandGoal() }
                        .padding(DesignTokens.PaddingMedium)
                        .testTag("grand_goal_hub_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CORE OBJECTIVE:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Zinc500,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            
                            // Beautiful Neon points display
                            Box(
                                modifier = Modifier
                                    .border(
                                        DesignTokens.StrokeMedium,
                                        GridLevel4,
                                        RoundedCornerShape(DesignTokens.PaddingZero)
                                    )
                                    .padding(horizontal = DesignTokens.PaddingSmall, vertical = DesignTokens.PaddingMicro)
                            ) {
                                Text(
                                    text = "$userPoints PTS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = GridLevel4,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

                        Text(
                            text = if (grandGoal.isNotBlank()) grandGoal.uppercase() else "NO GRAND TARGET ASSIGNED",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Action button inside tabs handles custom additions beautifully
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {
                    HorizontalDivider(
                        thickness = DesignTokens.DividerThickness,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("tab_goal_tree"),
                    text = {
                        Text(
                            text = "GOAL TREE",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = DesignTokens.LetterSpacingWide
                            )
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("tab_rewards_shop"),
                    text = {
                        Text(
                            text = "REWARDS SHOP",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = DesignTokens.LetterSpacingWide
                            )
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                GoalTreeTab(
                    categories = categories,
                    routines = routines,
                    onAddCategoryClick = { showCategoryDialog = true },
                    onAddRoutineClick = { catId ->
                        activeCategoryIdForRoutine = catId
                        showRoutineDialog = true
                    }
                )
            } else {
                RewardsShopTab(
                    rewards = rewards,
                    userPoints = userPoints,
                    onAddRewardClick = { showRewardDialog = true },
                    onClaimReward = onClaimReward
                )
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
                    text = "INITIALIZE CATEGORY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter category identifier to partition your sub-goals.",
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
                        placeholder = { Text("e.g., SOFT SKILLS", fontFamily = FontFamily.Monospace) },
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
                    Text("COMMIT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("ABORT", fontFamily = FontFamily.Monospace, color = Zinc500)
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
                    text = "ADD RECURRING ROUTINE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Define recurring action item & target frequency per month.",
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
                    Text("COMMIT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoutineDialog = false }) {
                    Text("ABORT", fontFamily = FontFamily.Monospace, color = Zinc500)
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
                    text = "DEFINE CUSTOM REWARD",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Specify incentive naming and corresponding gamification point cost.",
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
                    Text("COMMIT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRewardDialog = false }) {
                    Text("ABORT", fontFamily = FontFamily.Monospace, color = Zinc500)
                }
            }
        )
    }
}

@Composable
fun GoalTreeTab(
    categories: List<Category>,
    routines: List<Routine>,
    onAddCategoryClick: () -> Unit,
    onAddRoutineClick: (String) -> Unit
) {
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
                    text = "GOAL TREE IS EMPTY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Zinc500,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                Text(
                    text = "Establish sub-goal categories to begin monitoring recurring milestones.",
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
                    Text("INITIALIZE CATEGORY", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header section with action button to add categories
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
                    contentPadding = PaddingValues(bottom = 80.dp) // Leave space for Floating action or layout spacing
                ) {
                    items(categories) { category ->
                        val categoryRoutines = routines.filter { it.categoryId == category.id }
                        CategoryCard(
                            category = category,
                            routines = categoryRoutines,
                            onAddRoutineClick = { onAddRoutineClick(category.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    routines: List<Routine>,
    onAddRoutineClick: () -> Unit
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
                Text(
                    text = category.name.uppercase(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Box(
                    modifier = Modifier
                        .border(
                            DesignTokens.StrokeMedium,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .clickable { onAddRoutineClick() }
                        .padding(horizontal = DesignTokens.PaddingSmall, vertical = DesignTokens.PaddingTiny)
                        .testTag("add_routine_trigger_${category.id}")
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
                    text = "NO ROUTINES REGISTERED IN THIS SECTOR.",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Zinc500,
                    modifier = Modifier.padding(vertical = DesignTokens.PaddingSmall)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingSmall)) {
                    routines.forEach { routine ->
                        RoutineItem(routine = routine)
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineItem(routine: Routine) {
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

@Composable
fun RewardsShopTab(
    rewards: List<Reward>,
    userPoints: Int,
    onAddRewardClick: () -> Unit,
    onClaimReward: (Reward) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.PaddingLarge, vertical = DesignTokens.PaddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REWARD CATALOG",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Zinc500
                )

                TextButton(
                    onClick = onAddRewardClick,
                    modifier = Modifier.testTag("add_reward_header_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Reward",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.PaddingTiny))
                    Text("ADD REWARD", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                }
            }

            if (rewards.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignTokens.PaddingExtraLarge),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CATALOG EMPTY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Zinc500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingSmall))
                    Text(
                        text = "Invent incentives (gaming sessions, premium snacks, breaks) and assign point costs to drive effort.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = Zinc500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.PaddingLarge))
                    Button(
                        onClick = onAddRewardClick,
                        shape = RoundedCornerShape(DesignTokens.PaddingZero),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("add_reward_button_empty")
                    ) {
                        Text("INITIALIZE REWARD", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = DesignTokens.PaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.PaddingMedium),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(rewards) { reward ->
                        RewardCard(
                            reward = reward,
                            canClaim = userPoints >= reward.pointCost,
                            onClaimClick = { onClaimReward(reward) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardCard(
    reward: Reward,
    canClaim: Boolean,
    onClaimClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                DesignTokens.StrokeMedium,
                if (canClaim) GridLevel4.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(DesignTokens.PaddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.name.uppercase(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))
                Text(
                    text = "COST: ${reward.pointCost} PTS // CLAIMED: ${reward.claimedCount} TIMES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Zinc500
                    )
                )
            }

            Spacer(modifier = Modifier.width(DesignTokens.PaddingMedium))

            Button(
                onClick = onClaimClick,
                enabled = canClaim,
                shape = RoundedCornerShape(DesignTokens.PaddingZero),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canClaim) GridLevel4 else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    contentColor = if (canClaim) Color.Black else Zinc500,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    disabledContentColor = Zinc500
                ),
                border = BorderStroke(
                    DesignTokens.StrokeMedium,
                    if (canClaim) GridLevel4 else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
                contentPadding = PaddingValues(horizontal = DesignTokens.PaddingMedium, vertical = DesignTokens.PaddingTiny),
                modifier = Modifier.testTag("claim_reward_button_${reward.id}")
            ) {
                Text(
                    text = "CLAIM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
