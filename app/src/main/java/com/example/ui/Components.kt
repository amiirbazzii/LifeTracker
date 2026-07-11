package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyTask
import com.example.data.Category
import com.example.data.Routine
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class SystemHeaderMode {
    DASHBOARD, GOAL_HUB
}

data class NavigationItemData(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)

@Composable
fun BaseSystemHeader(
    mode: SystemHeaderMode,
    targetYears: Int = 5,
    userGoal: String = "",
    onEditGoalClick: () -> Unit = {},
    activeSectionTitle: String = "",
    userPoints: Int = 0,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val primaryLabelColor = if (isDark) MonochromeWhite else MonochromeBlack
    val descriptorColor = Zinc500
    val highContrastBorderColor = if (isDark) MonochromeWhite else MonochromeBlack
    val borderAlphaColor = highContrastBorderColor.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                BorderStroke(DesignTokens.StrokeMedium, borderAlphaColor),
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .clickable(enabled = mode == SystemHeaderMode.DASHBOARD) { onEditGoalClick() }
            .padding(DesignTokens.PaddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mode == SystemHeaderMode.DASHBOARD) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = DesignTokens.PaddingLarge)
                ) {
                    Text(
                        text = "YOUR NEXT $targetYears YEAR GOAL",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = descriptorColor,
                            fontSize = 12.sp,
                            letterSpacing = DesignTokens.LetterSpacingExtraWide,
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
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            letterSpacing = DesignTokens.LetterSpacingWide,
                            color = if (userGoal.isBlank()) GridLevel4 else primaryLabelColor
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

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(
                            DesignTokens.StrokeMedium,
                            highContrastBorderColor.copy(alpha = 0.3f),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .clickable { onEditGoalClick() }
                        .testTag("more_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = primaryLabelColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Goal/Hub Mode
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(BorderStroke(DesignTokens.StrokeMedium, highContrastBorderColor))
                        .clickable { onBack() }
                        .testTag("hub_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close/Dismiss",
                        tint = primaryLabelColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = DesignTokens.PaddingMedium)
                ) {
                    Text(
                        text = "ACTIVE SYSTEM MODULE",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = descriptorColor,
                            letterSpacing = DesignTokens.LetterSpacingExtraWide
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = activeSectionTitle.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = primaryLabelColor,
                            letterSpacing = DesignTokens.LetterSpacingWide
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(BorderStroke(DesignTokens.StrokeMedium, GridLevel4))
                        .background(GridLevel4.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = userPoints.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = GridLevel4
                            )
                        )
                        Text(
                            text = "PTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 7.sp,
                                color = GridLevel4
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SharedBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Distinct Top Border Only matching the header's border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.StrokeMedium)
                .background(MaterialTheme.colorScheme.primary)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val navItems = listOf(
                NavigationItemData("GOAL TREE", Icons.AutoMirrored.Filled.List, "tab_goal_tree"),
                NavigationItemData("REWARDS", Icons.Default.Star, "tab_rewards"),
                NavigationItemData("OBJECTIVE", Icons.Default.Edit, "tab_objective"),
                NavigationItemData("SETTINGS", Icons.Default.Settings, "tab_settings")
            )

            navItems.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onTabSelected(index) }
                        .testTag(item.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = DesignTokens.LetterSpacingCondensed
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
                    .border(
                        DesignTokens.StrokeThin,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(DesignTokens.PaddingZero)
                    )
            )
            Spacer(modifier = Modifier.width(DesignTokens.PaddingMicro))
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
            .padding(DesignTokens.PaddingExtraLarge),
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

        val titleText = if (isHistorical) DesignTokens.NO_HISTORY_TITLE else DesignTokens.MOMENTUM_EMPTY
        Text(
            text = titleText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = DesignTokens.LetterSpacingWide
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(DesignTokens.PaddingTiny))

        val descText = if (isHistorical) DesignTokens.NO_HISTORY_DESC else DesignTokens.MOMENTUM_EMPTY_DESC
        Text(
            text = descText,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TaskItemRowComponent(
    task: DailyTask,
    inceptionTimestamp: Long,
    isReadOnly: Boolean,
    onToggle: (DailyTask) -> Unit,
    onDelete: (String) -> Unit,
    categories: List<Category> = emptyList(),
    routines: List<Routine> = emptyList()
) {
    val isCompleted = task.isCompleted == 1
    val isDark = isSystemInDarkTheme()
    val hasRoutine = task.routineId != null

    val routine = remember(task.routineId, routines) {
        routines.find { it.id == task.routineId }
    }
    val category = remember(routine, categories) {
        categories.find { it.id == routine?.categoryId }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(DesignTokens.PaddingZero))
            .background(
                if (isCompleted) MaterialTheme.colorScheme.primary 
                else if (isDark) Zinc900 else Color(0xFFF9F9F9)
            )
            .border(
                border = BorderStroke(
                    DesignTokens.StrokeMedium,
                    if (isCompleted) Color.Transparent
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .clickable(enabled = !isReadOnly) {
                onToggle(task)
            }
            .testTag("task_row_${task.taskId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = DesignTokens.PaddingMedium, vertical = DesignTokens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Geometric checkbox
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(
                        BorderStroke(
                            DesignTokens.StrokeExtraThick,
                            if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        ),
                        RoundedCornerShape(DesignTokens.PaddingZero)
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
                        fontSize = DesignTokens.FontSizeMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(DesignTokens.PaddingMedium))

            // Stark minimalist day badge prefix
            val dayLabel = remember(inceptionTimestamp, task.weekIndex, task.dayOfWeek) {
                val cellTimeInMillis = inceptionTimestamp + (task.weekIndex * 7L + (task.dayOfWeek - 1)) * 24L * 60L * 60L * 1000L
                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                sdf.format(Date(cellTimeInMillis)).uppercase()
            }
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = DesignTokens.FontSizeTiny,
                    fontWeight = FontWeight.Black
                ),
                color = if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else GridLevel4,
                modifier = Modifier
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f) 
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        RoundedCornerShape(DesignTokens.PaddingZero)
                    )
                    .border(
                        DesignTokens.StrokeMedium,
                        if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f) 
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(DesignTokens.PaddingZero)
                    )
                    .padding(horizontal = 5.dp, vertical = DesignTokens.PaddingMicro)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.taskTitle.uppercase(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )

                if (hasRoutine && routine != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val catName = category?.name?.uppercase() ?: "SYS"
                    val routineName = routine.title.uppercase()

                    Box(
                        modifier = Modifier
                            .background(
                                if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                            .border(
                                DesignTokens.StrokeThin,
                                if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                RoundedCornerShape(DesignTokens.PaddingZero)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "[$catName // $routineName]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else GridLevel4
                            )
                        )
                    }
                }
            }

            if (!isReadOnly) {
                Box(
                    modifier = Modifier
                        .size(DesignTokens.PaddingExtraLarge)
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
}
