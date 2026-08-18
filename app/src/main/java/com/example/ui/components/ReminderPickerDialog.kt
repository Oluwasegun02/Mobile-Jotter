package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerDialog(
    currentReminderEpoch: Long?,
    onReminderSet: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showCustomTimePicker by remember { mutableStateOf(false) }
    var customSelectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0)

    if (showCustomDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        customSelectedDateMillis = it
                    }
                    showCustomDatePicker = false
                    showCustomTimePicker = true
                }) {
                    Text("Next (Set Time)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCustomTimePicker) {
        Dialog(onDismissRequest = { showCustomTimePicker = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Alert Time",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomTimePicker = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = customSelectedDateMillis
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                            }
                            onReminderSet(cal.timeInMillis)
                            showCustomTimePicker = false
                            onDismiss()
                        }) {
                            Text("Set Reminder")
                        }
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Set Note Reminder",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (currentReminderEpoch != null) {
                            val currentStr = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
                                .format(Date(currentReminderEpoch))
                            Text(
                                text = "Current: $currentStr",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Get notified at your scheduled time",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quick presets
                val now = Calendar.getInstance()

                // Preset 1: In 1 Hour
                val in1HourCal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
                val in1HourTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(in1HourCal.time)
                ReminderPresetRow(
                    icon = Icons.Default.Schedule,
                    title = "In 1 hour",
                    subtitle = in1HourTimeStr,
                    onClick = {
                        onReminderSet(in1HourCal.timeInMillis)
                        onDismiss()
                    }
                )

                // Preset 2: Today Evening (18:00) or Tomorrow Evening
                val eveningCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 18)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                val eveningSubtitle = SimpleDateFormat("MMM d, 18:00", Locale.getDefault()).format(eveningCal.time)
                ReminderPresetRow(
                    icon = Icons.Default.WbSunny,
                    title = "Evening reminder",
                    subtitle = eveningSubtitle,
                    onClick = {
                        onReminderSet(eveningCal.timeInMillis)
                        onDismiss()
                    }
                )

                // Preset 3: Tomorrow Morning (09:00)
                val tomorrowMorningCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val tomorrowSubtitle = SimpleDateFormat("EEE, MMM d, 09:00", Locale.getDefault()).format(tomorrowMorningCal.time)
                ReminderPresetRow(
                    icon = Icons.Default.AccessTime,
                    title = "Tomorrow morning",
                    subtitle = tomorrowSubtitle,
                    onClick = {
                        onReminderSet(tomorrowMorningCal.timeInMillis)
                        onDismiss()
                    }
                )

                // Preset 4: Custom Date & Time
                ReminderPresetRow(
                    icon = Icons.Default.CalendarMonth,
                    title = "Pick custom date & time",
                    subtitle = "Select exact calendar date and hour",
                    onClick = {
                        showCustomDatePicker = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom actions: Clear Reminder (if set) & Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentReminderEpoch != null) {
                        TextButton(
                            onClick = {
                                onReminderSet(null)
                                onDismiss()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove Alert", fontSize = 13.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderPresetRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
