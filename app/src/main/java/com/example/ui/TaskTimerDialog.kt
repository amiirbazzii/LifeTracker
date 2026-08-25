package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
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
import com.example.data.DailyTask
import com.example.ui.theme.*
import java.util.Calendar
import java.util.Locale

@Composable
fun TaskTimerDialog(
    task: DailyTask,
    onDismiss: () -> Unit,
    onSaveTimer: (startTime: String?, endTime: String?) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val cal = remember { Calendar.getInstance() }

    // Parse existing or set defaults
    val initialStartHour = remember(task.startTime) {
        task.startTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.HOUR_OF_DAY)
    }
    val initialStartMinute = remember(task.startTime) {
        task.startTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: ((cal.get(Calendar.MINUTE) / 5) * 5)
    }

    var startHour by remember { mutableIntStateOf(initialStartHour) }
    var startMinute by remember { mutableIntStateOf(initialStartMinute) }

    val initialEndHour = remember(task.endTime, initialStartHour) {
        task.endTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: ((initialStartHour + 1) % 24)
    }
    val initialEndMinute = remember(task.endTime, initialStartMinute) {
        task.endTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: initialStartMinute
    }

    var endHour by remember { mutableIntStateOf(initialEndHour) }
    var endMinute by remember { mutableIntStateOf(initialEndMinute) }

    val formatTime = { h: Int, m: Int ->
        String.format(Locale.US, "%02d:%02d", h.coerceIn(0, 23), m.coerceIn(0, 59))
    }

    val startTimeString = formatTime(startHour, startMinute)
    val endTimeString = formatTime(endHour, endMinute)

    // Calculate approximate duration in minutes
    val startTotalMin = startHour * 60 + startMinute
    val endTotalMin = endHour * 60 + endMinute
    val durationMin = if (endTotalMin >= startTotalMin) endTotalMin - startTotalMin else (1440 - startTotalMin + endTotalMin)
    val durationText = "${durationMin / 60}h ${durationMin % 60}m"

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(DesignTokens.PaddingZero),
        containerColor = if (isDark) Color(0xFF121212) else Color(0xFFF9F9F9),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰ [TASK TIMER]",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = DesignTokens.LetterSpacingWide,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .border(
                            DesignTokens.StrokeThin,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Task Title Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .border(
                            DesignTokens.StrokeThin,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .padding(DesignTokens.PaddingSmall)
                ) {
                    Text(
                        text = task.taskTitle.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Schedule Summary Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF))
                        .border(
                            DesignTokens.StrokeMedium,
                            GridLevel4,
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$startTimeString  ➔  $endTimeString",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp,
                                color = GridLevel4
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "DURATION: $durationText",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Zinc500
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // START TIME SELECTOR
                Text(
                    text = "START TIME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Zinc400,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour adjustment
                    TimePickerNumberBox(
                        label = "HOUR",
                        value = startHour,
                        onIncrement = { startHour = (startHour + 1) % 24 },
                        onDecrement = { startHour = if (startHour == 0) 23 else startHour - 1 },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Minute adjustment (5-min intervals)
                    TimePickerNumberBox(
                        label = "MIN",
                        value = startMinute,
                        onIncrement = { startMinute = (startMinute + 5) % 60 },
                        onDecrement = { startMinute = if (startMinute < 5) 55 else startMinute - 5 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // END TIME SELECTOR
                Text(
                    text = "END TIME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Zinc400,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour adjustment
                    TimePickerNumberBox(
                        label = "HOUR",
                        value = endHour,
                        onIncrement = { endHour = (endHour + 1) % 24 },
                        onDecrement = { endHour = if (endHour == 0) 23 else endHour - 1 },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Minute adjustment
                    TimePickerNumberBox(
                        label = "MIN",
                        value = endMinute,
                        onIncrement = { endMinute = (endMinute + 5) % 60 },
                        onDecrement = { endMinute = if (endMinute < 5) 55 else endMinute - 5 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Duration Presets for End Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "+30M" to 30,
                        "+45M" to 45,
                        "+1H" to 60,
                        "+1.5H" to 90,
                        "+2H" to 120
                    ).forEach { (label, minutes) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .border(
                                    BorderStroke(DesignTokens.StrokeThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                    RoundedCornerShape(DesignTokens.PaddingZero)
                                )
                                .clickable {
                                    val total = startHour * 60 + startMinute + minutes
                                    endHour = (total / 60) % 24
                                    endMinute = total % 60
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .background(GridLevel4)
                    .border(BorderStroke(DesignTokens.StrokeMedium, MonochromeBlack), RoundedCornerShape(DesignTokens.PaddingZero))
                    .clickable {
                        onSaveTimer(startTimeString, endTimeString)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("save_timer_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SAVE TIMER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = MonochromeBlack,
                        fontSize = 11.sp
                    )
                )
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!task.startTime.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                            .border(
                                BorderStroke(DesignTokens.StrokeThin, MaterialTheme.colorScheme.error),
                                RoundedCornerShape(DesignTokens.PaddingZero)
                            )
                            .clickable {
                                onSaveTimer(null, null)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("remove_timer_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLEAR",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .border(
                            BorderStroke(DesignTokens.StrokeThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            RoundedCornerShape(DesignTokens.PaddingZero)
                        )
                        .clickable { onDismiss() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("cancel_timer_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CANCEL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    )
}

@Composable
private fun TimePickerNumberBox(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = modifier
            .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFEBEBEB))
            .border(
                BorderStroke(DesignTokens.StrokeThin, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                RoundedCornerShape(DesignTokens.PaddingZero)
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .clickable { onIncrement() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▲",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Value display
        Text(
            text = String.format(Locale.US, "%02d", value),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Down button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .clickable { onDecrement() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▼",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
