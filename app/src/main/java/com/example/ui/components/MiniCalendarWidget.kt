package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NoteEntity
import com.example.model.DateFilterState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniCalendarWidget(
    dateFilter: DateFilterState,
    notesByDate: Map<String, List<NoteEntity>>,
    onSelectDay: (Long) -> Unit,
    onSelectToday: () -> Unit,
    onSelectThisWeek: () -> Unit,
    onSelectCustomRange: (Long, Long) -> Unit,
    onClearFilter: () -> Unit,
    onOpenFullCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var weekOffset by remember { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }

    val sdfKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val sdfMonthYear = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val sdfDayName = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

    val todayCal = remember { Calendar.getInstance() }
    val todayKey = remember { sdfKey.format(todayCal.time) }

    // Compute the days for the current week slice
    val weekDays = remember(weekOffset) {
        computeWeekDays(weekOffset)
    }

    val currentVisibleMonth = remember(weekDays) {
        val midDay = weekDays[weekDays.size / 2]
        sdfMonthYear.format(midDay.time)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF6F8FD)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color(0xFFE2E7F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Header Row: Month title, navigation, full calendar button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentVisibleMonth,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (weekOffset != 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { weekOffset = 0 }
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { weekOffset-- },
                        modifier = Modifier.size(30.dp).testTag("mini_cal_prev_week")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Week",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { weekOffset++ },
                        modifier = Modifier.size(30.dp).testTag("mini_cal_next_week")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Week",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenFullCalendar,
                        modifier = Modifier.size(30.dp).testTag("mini_cal_open_full")
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Open Full Calendar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Week Day Cells Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (dayCal in weekDays) {
                    val dayKey = sdfKey.format(dayCal.time)
                    val isToday = dayKey == todayKey
                    val notesOnDay = notesByDate[dayKey] ?: emptyList()
                    val isSelected = when (dateFilter) {
                        is DateFilterState.SpecificDate -> {
                            val selectedKey = sdfKey.format(Date(dateFilter.epochMillis))
                            dayKey == selectedKey
                        }
                        is DateFilterState.Today -> isToday
                        is DateFilterState.ThisWeek -> {
                            // Highlight if within this calendar week
                            weekOffset == 0
                        }
                        is DateFilterState.CustomRange -> {
                            dayCal.timeInMillis in dateFilter.startMillis..dateFilter.endMillis
                        }
                        else -> false
                    }

                    MiniDayCell(
                        dayName = sdfDayName.format(dayCal.time).take(1),
                        dayNumber = dayCal.get(Calendar.DAY_OF_MONTH),
                        notesCount = notesOnDay.size,
                        noteTypes = notesOnDay.map { it.noteType }.distinct(),
                        isToday = isToday,
                        isSelected = isSelected,
                        onClick = {
                            if (dateFilter is DateFilterState.SpecificDate && sdfKey.format(Date(dateFilter.epochMillis)) == dayKey) {
                                onClearFilter()
                            } else {
                                onSelectDay(dayCal.timeInMillis)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Date Range Filter Chips
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    DateFilterChip(
                        label = "All Time",
                        isSelected = dateFilter is DateFilterState.All,
                        onClick = onClearFilter
                    )
                }
                item {
                    DateFilterChip(
                        label = "Today",
                        isSelected = dateFilter is DateFilterState.Today,
                        onClick = onSelectToday
                    )
                }
                item {
                    DateFilterChip(
                        label = "This Week",
                        isSelected = dateFilter is DateFilterState.ThisWeek,
                        onClick = onSelectThisWeek
                    )
                }
                item {
                    DateFilterChip(
                        label = "Date Range 📅",
                        isSelected = dateFilter is DateFilterState.CustomRange,
                        onClick = { showDateRangeDialog = true }
                    )
                }
            }

            // Active Filter Banner (if filter is active)
            AnimatedVisibility(
                visible = dateFilter.isActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val filterLabel = when (dateFilter) {
                    is DateFilterState.Today -> "Showing notes created today"
                    is DateFilterState.ThisWeek -> "Showing notes from this week"
                    is DateFilterState.SpecificDate -> "Showing notes from ${dateFilter.label}"
                    is DateFilterState.CustomRange -> "Showing notes: ${dateFilter.label}"
                    else -> ""
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🗓️",
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = filterLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        IconButton(
                            onClick = onClearFilter,
                            modifier = Modifier.size(22.dp).testTag("clear_date_filter_button")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear Date Filter",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Material 3 Date Range Picker Dialog
    if (showDateRangeDialog) {
        DateRangePickerDialog(
            onDismiss = { showDateRangeDialog = false },
            onConfirm = { startMillis, endMillis ->
                showDateRangeDialog = false
                onSelectCustomRange(startMillis, endMillis)
            }
        )
    }
}

@Composable
private fun MiniDayCell(
    dayName: String,
    dayNumber: Int,
    notesCount: Int,
    noteTypes: List<String>,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color(0xFFD6E3FF)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.dp else 0.dp,
                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = dayNumber.toString(),
            fontSize = 13.5.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Visual indicator dots for notes
        Row(
            modifier = Modifier.height(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (notesCount > 0) {
                val dotColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    noteTypes.contains("AUDIO") -> Color(0xFFE65100)
                    noteTypes.contains("SKETCH") -> Color(0xFF7B1FA2)
                    noteTypes.contains("CHECKLIST") -> Color(0xFF2E7D32)
                    else -> MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                if (notesCount > 1) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(dotColor.copy(alpha = 0.7f))
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(4.dp))
            }
        }
    }
}

@Composable
private fun DateFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color.White
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color.Transparent else (if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFD0D5DD))
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.5.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis ?: start
                    if (start != null) {
                        // Normalize start to 00:00:00 and end to 23:59:59
                        val startCal = Calendar.getInstance().apply {
                            timeInMillis = start
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val endCal = Calendar.getInstance().apply {
                            timeInMillis = end ?: start
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        onConfirm(startCal.timeInMillis, endCal.timeInMillis)
                    }
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null
            ) {
                Text("Apply Range", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Filter Notes by Date Range",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            headline = {
                Text(
                    text = "Select Start & End Date",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            showModeToggle = false,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun computeWeekDays(weekOffset: Int): List<Calendar> {
    val cal = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        add(Calendar.WEEK_OF_YEAR, weekOffset)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    val days = mutableListOf<Calendar>()
    for (i in 0..6) {
        val day = Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            add(Calendar.DAY_OF_YEAR, i)
        }
        days.add(day)
    }
    return days
}
