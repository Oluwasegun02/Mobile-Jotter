package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NoteEntity
import com.example.model.NoteType
import com.example.model.ScreenDestination
import com.example.ui.components.NoteCard
import com.example.viewmodel.JotterViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarJourneyScreen(
    viewModel: JotterViewModel
) {
    val isDark = isSystemInDarkTheme()
    val selectedMillis by viewModel.selectedJourneyDateMillis.collectAsState()
    val notesByDate by viewModel.notesByDateKey.collectAsState()
    val selectedNotes by viewModel.selectedJourneyDateNotes.collectAsState()
    val streak by viewModel.journeyStreak.collectAsState()

    // Calendar view state (which month is currently shown)
    var displayedMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = selectedMillis })
    }

    BackHandler {
        viewModel.setScreen(ScreenDestination.HOME)
    }

    val selectedCal = remember(selectedMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedMillis }
    }

    var journeyTabFilter by remember { mutableStateOf("ALL") }

    val todayCal = remember {
        Calendar.getInstance()
    }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayNameFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val isTodaySelected = remember(selectedCal, todayCal) {
        selectedCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        selectedCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
    }

    val displayedNotes = remember(selectedNotes, journeyTabFilter) {
        val filtered = when (journeyTabFilter) {
            "DIARY" -> selectedNotes.filter { it.noteType == NoteType.DIARY.name }
            "JOTS" -> selectedNotes.filter { it.noteType != NoteType.DIARY.name }
            else -> selectedNotes
        }
        filtered.sortedWith(
            compareByDescending<NoteEntity> { it.isPinned }
                .thenByDescending { it.updatedAt }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Daily Journey",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setScreen(ScreenDestination.HOME) },
                        modifier = Modifier.testTag("journey_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Notes")
                    }
                },
                actions = {
                    // Quick jump to today
                    if (!isTodaySelected) {
                        FilledTonalButton(
                            onClick = {
                                val now = Calendar.getInstance()
                                displayedMonthCalendar = Calendar.getInstance().apply { timeInMillis = now.timeInMillis }
                                viewModel.selectJourneyDateToday()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(34.dp)
                                .testTag("jump_today_button")
                        ) {
                            Icon(
                                Icons.Default.Today,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Today", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Streak & Journey Hero Banner
            item {
                JourneyStreakHero(
                    streak = streak,
                    totalLoggedDays = notesByDate.size,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Interactive Calendar Card
            item {
                CalendarMonthCard(
                    displayedMonth = displayedMonthCalendar,
                    selectedMillis = selectedMillis,
                    notesByDate = notesByDate,
                    onPrevMonth = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = displayedMonthCalendar.timeInMillis
                            add(Calendar.MONTH, -1)
                        }
                        displayedMonthCalendar = newCal
                    },
                    onNextMonth = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = displayedMonthCalendar.timeInMillis
                            add(Calendar.MONTH, 1)
                        }
                        displayedMonthCalendar = newCal
                    },
                    onSelectDate = { dateMillis ->
                        viewModel.selectJourneyDate(dateMillis)
                    },
                    monthYearFormat = monthYearFormat,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Selected Day Feed Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isTodaySelected) "Today's Journey" else dayNameFormat.format(selectedCal.time),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isTodaySelected) {
                                Text(
                                    text = dayNameFormat.format(selectedCal.time),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Badge count
                        Surface(
                            shape = CircleShape,
                            color = if (selectedNotes.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "${selectedNotes.size} ${if (selectedNotes.size == 1) "entry" else "entries"}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedNotes.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Log Action Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            QuickLogChip(
                                icon = Icons.Default.Book,
                                label = "Daily Living Diary",
                                onClick = { viewModel.createDiaryEntry(selectedMillis) },
                                isPrimary = true
                            )
                        }
                        item {
                            QuickLogChip(
                                icon = Icons.Default.Edit,
                                label = "Quick Jot",
                                onClick = { viewModel.createJourneyEntry(NoteType.TEXT, selectedMillis) }
                            )
                        }
                        item {
                            QuickLogChip(
                                icon = Icons.Default.Checklist,
                                label = "Checklist",
                                onClick = { viewModel.createJourneyEntry(NoteType.CHECKLIST, selectedMillis) }
                            )
                        }
                        item {
                            QuickLogChip(
                                icon = Icons.Default.Draw,
                                label = "Sketch",
                                onClick = { viewModel.createJourneyEntry(NoteType.SKETCH, selectedMillis) }
                            )
                        }
                        item {
                            QuickLogChip(
                                icon = Icons.Default.Mic,
                                label = "Audio Note",
                                onClick = { viewModel.createJourneyEntry(NoteType.AUDIO, selectedMillis) }
                            )
                        }
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.filterBySpecificDate(selectedMillis)
                                        viewModel.setScreen(ScreenDestination.HOME)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Today,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Filter Main Feed",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    if (selectedNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Type filter tabs (All, Diary, Other Jots)
                        val diaryCount = selectedNotes.count { it.noteType == NoteType.DIARY.name }
                        val jotsCount = selectedNotes.count { it.noteType != NoteType.DIARY.name }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (journeyTabFilter == "ALL") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { journeyTabFilter = "ALL" }
                            ) {
                                Text(
                                    text = "All (${selectedNotes.size})",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (journeyTabFilter == "ALL") FontWeight.Bold else FontWeight.Normal,
                                    color = if (journeyTabFilter == "ALL") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            if (diaryCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (journeyTabFilter == "DIARY") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { journeyTabFilter = "DIARY" }
                                ) {
                                    Text(
                                        text = "📖 Diary ($diaryCount)",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (journeyTabFilter == "DIARY") FontWeight.Bold else FontWeight.Normal,
                                        color = if (journeyTabFilter == "DIARY") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (jotsCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (journeyTabFilter == "JOTS") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { journeyTabFilter = "JOTS" }
                                ) {
                                    Text(
                                        text = "📝 Jots ($jotsCount)",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (journeyTabFilter == "JOTS") FontWeight.Bold else FontWeight.Normal,
                                        color = if (journeyTabFilter == "JOTS") MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // List of Notes for Selected Date
            if (displayedNotes.isEmpty()) {
                item {
                    EmptyJourneyDateView(
                        isToday = isTodaySelected,
                        onAddDiary = { viewModel.createDiaryEntry(selectedMillis) },
                        onAddJot = { viewModel.createJourneyEntry(NoteType.TEXT, selectedMillis) }
                    )
                }
            } else {
                items(displayedNotes, key = { it.id }) { note ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        NoteCard(
                            note = note,
                            onClick = { viewModel.openNote(note) },
                            onPinClick = { viewModel.togglePinFromCard(note) },
                            onFavoriteClick = { viewModel.toggleFavoriteFromCard(note) },
                            onArchiveSwipe = { viewModel.archiveNoteFromCard(note) },
                            onDeleteSwipe = { viewModel.moveNoteToTrash(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyStreakHero(
    streak: Int,
    totalLoggedDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (streak > 0) Color(0xFFFFECE0) else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (streak > 0) "🔥" else "🌱",
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (streak > 0) "$streak Day Journey Streak!" else "Start Your Journey",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (streak > 0) "Keep logging thoughts to maintain your rhythm." else "Log entries daily to build your personal reflection habit.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthCard(
    displayedMonth: Calendar,
    selectedMillis: Long,
    notesByDate: Map<String, List<NoteEntity>>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (Long) -> Unit,
    monthYearFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val sdfKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val todayCal = remember { Calendar.getInstance() }
    val todayKey = remember { sdfKey.format(todayCal.time) }

    val selectedCal = remember(selectedMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedMillis }
    }
    val selectedKey = remember(selectedMillis) {
        sdfKey.format(selectedCal.time)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Month Header Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.size(36.dp).testTag("prev_month_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = monthYearFormat.format(displayedMonth.time),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.size(36.dp).testTag("next_month_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Headers (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
            val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (day in weekDays) {
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val daysInMonthGrid = remember(displayedMonth.get(Calendar.YEAR), displayedMonth.get(Calendar.MONTH)) {
                computeMonthDays(displayedMonth)
            }

            for (week in daysInMonthGrid.chunked(7)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (calendarDay in week) {
                        val dayCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, calendarDay.year)
                            set(Calendar.MONTH, calendarDay.month)
                            set(Calendar.DAY_OF_MONTH, calendarDay.dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, 12)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }
                        val dayKey = sdfKey.format(dayCal.time)
                        val isSelected = dayKey == selectedKey
                        val isToday = dayKey == todayKey
                        val notesForDay = notesByDate[dayKey] ?: emptyList()

                        DayCell(
                            dayNumber = calendarDay.dayOfMonth,
                            isCurrentMonth = calendarDay.isCurrentMonth,
                            isSelected = isSelected,
                            isToday = isToday,
                            notesCount = notesForDay.size,
                            noteTypes = notesForDay.map { it.noteType }.distinct(),
                            onClick = {
                                onSelectDate(dayCal.timeInMillis)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    dayNumber: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    notesCount: Int,
    noteTypes: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .padding(1.5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = isCurrentMonth) { onClick() }
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayNumber.toString(),
            fontSize = 13.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Dots indicator for entries
        Row(
            modifier = Modifier.height(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (notesCount > 0 && isCurrentMonth) {
                val dotColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else if (noteTypes.contains("DIARY")) {
                    Color(0xFFE91E63) // Distinct Rose for Diary
                } else if (noteTypes.contains("AUDIO")) {
                    Color(0xFFE65100)
                } else if (noteTypes.contains("SKETCH")) {
                    Color(0xFF7B1FA2)
                } else if (noteTypes.contains("CHECKLIST")) {
                    Color(0xFF2E7D32)
                } else {
                    MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier
                        .size(4.5.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                if (notesCount > 1) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.5.dp)
                            .clip(CircleShape)
                            .background(dotColor.copy(alpha = 0.7f))
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(4.5.dp))
            }
        }
    }
}

@Composable
private fun QuickLogChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyJourneyDateView(
    isToday: Boolean,
    onAddDiary: () -> Unit,
    onAddJot: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isToday) "📖" else "✍️",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isToday) "No diary or jots logged today yet" else "No entries on this day",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isToday) "Write your daily living reflection or create a quick jot to keep your streak glowing." else "Log retrospectively or add an entry for this date.",
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAddDiary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("start_daily_diary_button")
                ) {
                    Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Daily Living Diary", fontSize = 12.5.sp)
                }

                OutlinedButton(
                    onClick = onAddJot,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("start_daily_jot_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quick Jot", fontSize = 12.5.sp)
                }
            }
        }
    }
}

private data class CalendarDay(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean
)

private fun computeMonthDays(monthCalendar: Calendar): List<CalendarDay> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = monthCalendar.timeInMillis
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, etc.

    val list = mutableListOf<CalendarDay>()

    // Leading days from previous month
    val prevMonthCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, -1)
    }
    val maxDaysPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val prevMonthDaysCount = firstDayOfWeek - 1 // If Sunday (1), 0 leading days; if Monday (2), 1 leading day

    for (i in (maxDaysPrevMonth - prevMonthDaysCount + 1)..maxDaysPrevMonth) {
        list.add(
            CalendarDay(
                year = prevMonthCal.get(Calendar.YEAR),
                month = prevMonthCal.get(Calendar.MONTH),
                dayOfMonth = i,
                isCurrentMonth = false
            )
        )
    }

    // Days of current month
    for (i in 1..maxDaysInMonth) {
        list.add(
            CalendarDay(
                year = year,
                month = month,
                dayOfMonth = i,
                isCurrentMonth = true
            )
        )
    }

    // Trailing days from next month to complete the 7-day grid
    val nextMonthCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, 1)
    }
    val remainder = list.size % 7
    if (remainder != 0) {
        val nextMonthDaysCount = 7 - remainder
        for (i in 1..nextMonthDaysCount) {
            list.add(
                CalendarDay(
                    year = nextMonthCal.get(Calendar.YEAR),
                    month = nextMonthCal.get(Calendar.MONTH),
                    dayOfMonth = i,
                    isCurrentMonth = false
                )
            )
        }
    }

    return list
}
