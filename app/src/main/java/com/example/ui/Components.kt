package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyTask
import com.example.data.Category
import com.example.data.Routine
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
fun TaskItemRow(
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
