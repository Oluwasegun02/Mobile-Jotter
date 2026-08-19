package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerHelper
import com.example.audio.AudioRecorderHelper
import com.example.audio.SpeechToTextHelper
import com.example.data.AppDatabase
import com.example.data.FolderEntity
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import com.example.model.ChecklistItem
import com.example.model.DateFilterState
import com.example.model.FolderColorPresets
import com.example.model.FolderItem
import com.example.model.NoteFontStyle
import com.example.model.NoteSortOrder
import com.example.model.NoteType
import com.example.model.ScreenDestination
import com.example.model.SketchStroke
import com.example.model.ViewMode
import com.example.reminder.ReminderManager
import com.example.security.SecurityManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class EditorState(
    val noteId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val noteType: NoteType = NoteType.TEXT,
    val folder: String = "General",
    val tags: List<String> = emptyList(),
    val colorIndex: Int = 0,
    val fontStyle: NoteFontStyle = NoteFontStyle.SANS,
    val fontSize: Float = 16f,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val isArchived: Boolean = false,
    val reminderEpochMillis: Long? = null,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val sketchStrokes: List<SketchStroke> = emptyList(),
    val sketchUndoStack: List<List<SketchStroke>> = emptyList(),
    val audioFilePath: String? = null,
    val audioDurationSeconds: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val autosaveStatus: String = "All changes saved"
)

class JotterViewModel(
    application: Application,
    private val repository: NoteRepository = NoteRepository(
        AppDatabase.getDatabase(application).noteDao(),
        AppDatabase.getDatabase(application).folderDao()
    ),
    val securityManager: SecurityManager = SecurityManager(application),
    val reminderManager: ReminderManager = ReminderManager(application),
    val audioRecorder: AudioRecorderHelper = AudioRecorderHelper(application),
    val audioPlayer: AudioPlayerHelper = AudioPlayerHelper(),
    val speechToText: SpeechToTextHelper = SpeechToTextHelper(application)
) : AndroidViewModel(application) {

    // Navigation & UI View Modes
    private val _currentScreen = MutableStateFlow(ScreenDestination.HOME)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFolder = MutableStateFlow("All")
    val selectedFolder: StateFlow<String> = _selectedFolder.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val themePrefs = application.getSharedPreferences("jotter_theme_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (themePrefs.contains("is_dark_mode")) {
            themePrefs.getBoolean("is_dark_mode", false)
        } else {
            null
        }
    )
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _isStartupLockEnabled = MutableStateFlow(securityManager.isAppLockEnabled())
    val isStartupLockEnabled: StateFlow<Boolean> = _isStartupLockEnabled.asStateFlow()

    // Folder Dialog States
    private val _moveDialogNote = MutableStateFlow<NoteEntity?>(null)
    val moveDialogNote: StateFlow<NoteEntity?> = _moveDialogNote.asStateFlow()

    private val _showManageFoldersDialog = MutableStateFlow(false)
    val showManageFoldersDialog: StateFlow<Boolean> = _showManageFoldersDialog.asStateFlow()

    private val _folderToRename = MutableStateFlow<FolderItem?>(null)
    val folderToRename: StateFlow<FolderItem?> = _folderToRename.asStateFlow()

    private val _showCreateFolderDialog = MutableStateFlow(false)
    val showCreateFolderDialog: StateFlow<Boolean> = _showCreateFolderDialog.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // App PIN startup lock state
    private val _isAppLocked = MutableStateFlow(!securityManager.isAppUnlocked())
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // PIN lock dialog state
    private val _pinDialogNoteToUnlock = MutableStateFlow<NoteEntity?>(null)
    val pinDialogNoteToUnlock: StateFlow<NoteEntity?> = _pinDialogNoteToUnlock.asStateFlow()

    private val _isSettingUpPin = MutableStateFlow(false)
    val isSettingUpPin: StateFlow<Boolean> = _isSettingUpPin.asStateFlow()

    // Active Editor State
    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private var autosaveJob: Job? = null

    // Date Range & Calendar Filters
    private val _dateFilter = MutableStateFlow<DateFilterState>(DateFilterState.All)
    val dateFilter: StateFlow<DateFilterState> = _dateFilter.asStateFlow()

    // Sorting State
    private val _sortOrder = MutableStateFlow(NoteSortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<NoteSortOrder> = _sortOrder.asStateFlow()

    // Notes Data Streams
    val rawActiveNotes = repository.activeNotes
    val archivedNotes = repository.archivedNotes
    val deletedNotes = repository.deletedNotes
    val favoriteNotes = repository.favoriteNotes
    val allDbFolders = repository.allDbFolders
    val activeNotesCount = repository.activeNotesCount
    val deletedNotesCount = repository.deletedNotesCount

    // Data class for combined filter parameters
    private data class HomeFilterCriteria(
        val query: String,
        val folder: String,
        val tag: String?,
        val dateFilter: DateFilterState,
        val sortOrder: NoteSortOrder
    )

    private val filterCriteria = combine(
        _searchQuery,
        _selectedFolder,
        _selectedTag,
        _dateFilter,
        _sortOrder
    ) { query, folder, tag, dateFilterState, sort ->
        HomeFilterCriteria(query, folder, tag, dateFilterState, sort)
    }

    // Combined filtered & sorted notes for Home
    val homeFilteredNotes: StateFlow<List<NoteEntity>> = rawActiveNotes.combine(filterCriteria) { notes, criteria ->
        var list = notes

        if (criteria.folder != "All") {
            list = list.filter { it.folder.equals(criteria.folder, ignoreCase = true) }
        }

        if (criteria.tag != null) {
            list = list.filter { it.tags.any { t -> t.equals(criteria.tag, ignoreCase = true) } }
        }

        // Apply Date Filtering
        val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        when (val dateState = criteria.dateFilter) {
            is DateFilterState.All -> { /* No date filtering */ }
            is DateFilterState.Today -> {
                val todayKey = sdfDay.format(Date())
                list = list.filter { sdfDay.format(Date(it.createdAt)) == todayKey }
            }
            is DateFilterState.ThisWeek -> {
                val cal = Calendar.getInstance()
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfWeek = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val endOfWeek = cal.timeInMillis
                list = list.filter { it.createdAt in startOfWeek..endOfWeek }
            }
            is DateFilterState.SpecificDate -> {
                val targetKey = sdfDay.format(Date(dateState.epochMillis))
                list = list.filter { sdfDay.format(Date(it.createdAt)) == targetKey }
            }
            is DateFilterState.CustomRange -> {
                list = list.filter { it.createdAt in dateState.startMillis..dateState.endMillis }
            }
        }

        if (criteria.query.isNotBlank()) {
            val q = criteria.query.trim().lowercase(Locale.getDefault())
            list = list.filter { note ->
                note.title.lowercase(Locale.getDefault()).contains(q) ||
                note.content.lowercase(Locale.getDefault()).contains(q) ||
                note.tags.any { it.lowercase(Locale.getDefault()).contains(q) } ||
                note.parseChecklist().any { it.text.lowercase(Locale.getDefault()).contains(q) }
            }
        }

        // Apply Sorting
        list = when (criteria.sortOrder) {
            NoteSortOrder.NEWEST_FIRST -> list.sortedWith(
                compareByDescending<NoteEntity> { it.isPinned }
                    .thenByDescending { it.updatedAt }
            )
            NoteSortOrder.OLDEST_FIRST -> list.sortedWith(
                compareByDescending<NoteEntity> { it.isPinned }
                    .thenBy { it.createdAt }
            )
            NoteSortOrder.ALPHABETICAL -> list.sortedWith(
                compareByDescending<NoteEntity> { it.isPinned }
                    .thenBy { (if (it.title.isNotBlank()) it.title else it.content).lowercase(Locale.getDefault()) }
            )
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All distinct tags across all active notes
    val allTags: StateFlow<List<String>> = rawActiveNotes.combine(_searchQuery) { notes, _ ->
        notes.flatMap { it.tags }.distinct().sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Folder items with real-time note counts
    val foldersWithCounts: StateFlow<List<FolderItem>> = combine(
        allDbFolders,
        rawActiveNotes
    ) { dbFolders, notes ->
        val defaultFolders = listOf(
            FolderItem(name = "General", noteCount = 0, colorHex = 0xFF64748B, isSystem = true),
            FolderItem(name = "Personal", noteCount = 0, colorHex = 0xFF10B981, isSystem = false),
            FolderItem(name = "Work", noteCount = 0, colorHex = 0xFF3B82F6, isSystem = false),
            FolderItem(name = "Ideas", noteCount = 0, colorHex = 0xFFF59E0B, isSystem = false),
            FolderItem(name = "Study", noteCount = 0, colorHex = 0xFF8B5CF6, isSystem = false),
            FolderItem(name = "Journal", noteCount = 0, colorHex = 0xFFEC4899, isSystem = false)
        )

        val map = linkedMapOf<String, FolderItem>()
        // Defaults
        defaultFolders.forEach { map[it.name.lowercase()] = it }

        // Overlay DB folders
        dbFolders.forEach { entity ->
            val isSys = entity.name.equals("General", ignoreCase = true)
            map[entity.name.lowercase()] = FolderItem(
                name = entity.name,
                noteCount = 0,
                colorHex = entity.colorHex,
                isSystem = isSys
            )
        }

        // Count notes in each folder
        notes.forEach { note ->
            val folderKey = note.folder.ifBlank { "General" }.lowercase()
            val existing = map[folderKey]
            if (existing != null) {
                map[folderKey] = existing.copy(noteCount = existing.noteCount + 1)
            } else {
                map[folderKey] = FolderItem(
                    name = note.folder.ifBlank { "General" },
                    noteCount = 1,
                    colorHex = 0xFF3B82F6,
                    isSystem = false
                )
            }
        }

        map.values.sortedWith(compareBy({ !it.isSystem }, { it.name.lowercase() }))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(
            FolderItem("General", 0, 0xFF64748B, true),
            FolderItem("Personal", 0, 0xFF10B981, false),
            FolderItem("Work", 0, 0xFF3B82F6, false),
            FolderItem("Ideas", 0, 0xFFF59E0B, false),
            FolderItem("Study", 0, 0xFF8B5CF6, false),
            FolderItem("Journal", 0, 0xFFEC4899, false)
        )
    )

    // Available folders list
    val folderList: StateFlow<List<String>> = foldersWithCounts.map { folderItems ->
        val names = folderItems.map { it.name }
        listOf("All") + names
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("All", "General", "Personal", "Work", "Ideas", "Study", "Journal")
    )

    // Calendar & Daily Journey States
    private val _selectedJourneyDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedJourneyDateMillis: StateFlow<Long> = _selectedJourneyDateMillis.asStateFlow()

    // Notes grouped by "yyyy-MM-dd"
    val notesByDateKey: StateFlow<Map<String, List<NoteEntity>>> = rawActiveNotes.map { notes ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        notes.groupBy { sdf.format(Date(it.createdAt)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Notes on the currently selected calendar journey date
    val selectedJourneyDateNotes: StateFlow<List<NoteEntity>> = combine(
        rawActiveNotes,
        _selectedJourneyDateMillis
    ) { notes, selectedMillis ->
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedKey = sdf.format(Date(selectedMillis))
        notes.filter { sdf.format(Date(it.createdAt)) == selectedKey }
            .sortedByDescending { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Daily streak calculation (consecutive active days)
    val journeyStreak: StateFlow<Int> = rawActiveNotes.map { notes ->
        if (notes.isEmpty()) return@map 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val activeDays = notes.map { sdf.format(Date(it.createdAt)) }.toSet()
        val cal = Calendar.getInstance()
        var streak = 0
        val todayKey = sdf.format(cal.time)

        if (!activeDays.contains(todayKey)) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = sdf.format(cal.time)
            if (!activeDays.contains(yesterdayKey)) {
                return@map 0
            }
        }

        while (true) {
            val key = sdf.format(cal.time)
            if (activeDays.contains(key)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        streak
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun selectJourneyDate(epochMillis: Long) {
        _selectedJourneyDateMillis.value = epochMillis
    }

    fun selectJourneyDateToday() {
        _selectedJourneyDateMillis.value = System.currentTimeMillis()
    }

    fun setDateFilter(filter: DateFilterState) {
        _dateFilter.value = filter
    }

    fun filterBySpecificDate(epochMillis: Long) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        _dateFilter.value = DateFilterState.SpecificDate(epochMillis, sdf.format(Date(epochMillis)))
        _selectedJourneyDateMillis.value = epochMillis
    }

    fun filterByToday() {
        _dateFilter.value = DateFilterState.Today
        _selectedJourneyDateMillis.value = System.currentTimeMillis()
    }

    fun filterByThisWeek() {
        _dateFilter.value = DateFilterState.ThisWeek
    }

    fun filterByCustomRange(startMillis: Long, endMillis: Long) {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        val label = "${sdf.format(Date(startMillis))} – ${sdf.format(Date(endMillis))}"
        _dateFilter.value = DateFilterState.CustomRange(startMillis, endMillis, label)
    }

    fun clearDateFilter() {
        _dateFilter.value = DateFilterState.All
    }

    fun setSortOrder(order: NoteSortOrder) {
        _sortOrder.value = order
    }

    fun cycleSortOrder() {
        _sortOrder.value = when (_sortOrder.value) {
            NoteSortOrder.NEWEST_FIRST -> NoteSortOrder.OLDEST_FIRST
            NoteSortOrder.OLDEST_FIRST -> NoteSortOrder.ALPHABETICAL
            NoteSortOrder.ALPHABETICAL -> NoteSortOrder.NEWEST_FIRST
        }
    }

    fun buildNotePlainText(note: NoteEntity): String {
        return buildString {
            if (note.title.isNotBlank()) {
                appendLine(note.title)
                appendLine("═".repeat(note.title.length.coerceAtLeast(10)))
                appendLine()
            }
            if (note.noteType == "CHECKLIST") {
                val items = note.parseChecklist()
                items.forEach { item ->
                    appendLine(if (item.isDone) "[✓] ${item.text}" else "[ ] ${item.text}")
                }
            } else {
                appendLine(note.content)
            }
            if (note.tags.isNotEmpty()) {
                appendLine()
                appendLine(note.tags.joinToString(" ") { "#$it" })
            }
            appendLine()
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(note.updatedAt))
            appendLine("— Exported from Jotter • $dateStr")
        }
    }

    fun buildEditorPlainText(editor: EditorState): String {
        return buildString {
            if (editor.title.isNotBlank()) {
                appendLine(editor.title)
                appendLine("═".repeat(editor.title.length.coerceAtLeast(10)))
                appendLine()
            }
            if (editor.noteType == NoteType.CHECKLIST) {
                editor.checklistItems.forEach { item ->
                    appendLine(if (item.isDone) "[✓] ${item.text}" else "[ ] ${item.text}")
                }
            } else {
                appendLine(editor.content)
            }
            if (editor.tags.isNotEmpty()) {
                appendLine()
                appendLine(editor.tags.joinToString(" ") { "#$it" })
            }
            appendLine()
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis()))
            appendLine("— Exported from Jotter • $dateStr")
        }
    }

    fun shareNoteContent(context: Context, note: NoteEntity) {
        val text = buildNotePlainText(note)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Jotter Note" })
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Share Note via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    fun shareEditorNoteContent(context: Context) {
        val text = buildEditorPlainText(_editorState.value)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, _editorState.value.title.ifBlank { "Jotter Note" })
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Share Note via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    fun exportNoteAsTxtFile(context: Context, note: NoteEntity) {
        try {
            val text = buildNotePlainText(note)
            val cleanTitle = note.title.ifBlank { "Jotter_Note" }.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val fileName = "${cleanTitle}_${System.currentTimeMillis()}.txt"
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName).apply {
                writeText(text)
            }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Jotter Note" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Save or Export Note as .txt").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            shareNoteContent(context, note)
        }
    }

    fun exportEditorNoteAsTxtFile(context: Context) {
        try {
            val text = buildEditorPlainText(_editorState.value)
            val cleanTitle = _editorState.value.title.ifBlank { "Jotter_Note" }.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val fileName = "${cleanTitle}_${System.currentTimeMillis()}.txt"
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName).apply {
                writeText(text)
            }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, _editorState.value.title.ifBlank { "Jotter Note" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Save or Export Note as .txt").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            shareEditorNoteContent(context)
        }
    }

    fun createJourneyEntry(type: NoteType = NoteType.TEXT, dateMillis: Long = _selectedJourneyDateMillis.value) {
        flushAutosaveNow()
        val dateFormatted = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dateMillis))
        _editorState.value = EditorState(
            noteId = 0L,
            title = "Journey — $dateFormatted",
            content = "",
            noteType = type,
            folder = "Journal",
            tags = listOf("DailyJourney", "Journal"),
            colorIndex = 1, // Lavender
            fontStyle = NoteFontStyle.SERIF,
            fontSize = 16f,
            checklistItems = if (type == NoteType.CHECKLIST) listOf(
                ChecklistItem(UUID.randomUUID().toString(), "Morning Reflection", false),
                ChecklistItem(UUID.randomUUID().toString(), "Key Objective of the Day", false),
                ChecklistItem(UUID.randomUUID().toString(), "Gratitude / Evening Note", false)
            ) else emptyList(),
            createdAt = dateMillis,
            autosaveStatus = "New Journey Entry"
        )
        _currentScreen.value = ScreenDestination.EDITOR
    }

    fun setScreen(destination: ScreenDestination) {
        if (_currentScreen.value == ScreenDestination.EDITOR && destination != ScreenDestination.EDITOR) {
            // Save before leaving
            flushAutosaveNow()
        }
        _currentScreen.value = destination
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFolder(folder: String) {
        _selectedFolder.value = folder
    }

    fun createFolder(name: String, colorHex: Long = 0xFF3B82F6) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.createFolder(trimmed, colorHex)
            _snackbarMessage.value = "Created folder \"$trimmed\""
        }
    }

    fun renameFolder(oldName: String, newName: String, newColorHex: Long? = null) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.renameFolder(oldName, trimmed, newColorHex)
            if (_selectedFolder.value.equals(oldName, ignoreCase = true)) {
                _selectedFolder.value = trimmed
            }
            if (_editorState.value.folder.equals(oldName, ignoreCase = true)) {
                _editorState.value = _editorState.value.copy(folder = trimmed)
            }
            _snackbarMessage.value = "Renamed folder to \"$trimmed\""
        }
    }

    fun deleteFolder(folderName: String, destinationFolder: String = "General") {
        if (folderName.equals("General", ignoreCase = true)) return
        viewModelScope.launch {
            repository.deleteFolder(folderName, destinationFolder)
            if (_selectedFolder.value.equals(folderName, ignoreCase = true)) {
                _selectedFolder.value = "All"
            }
            if (_editorState.value.folder.equals(folderName, ignoreCase = true)) {
                _editorState.value = _editorState.value.copy(folder = destinationFolder)
            }
            _snackbarMessage.value = "Deleted folder \"$folderName\""
        }
    }

    fun moveNoteToFolder(noteId: Long, folderName: String) {
        val targetFolder = folderName.ifBlank { "General" }
        viewModelScope.launch {
            repository.moveNoteToFolder(noteId, targetFolder)
            if (_editorState.value.noteId == noteId) {
                _editorState.value = _editorState.value.copy(folder = targetFolder)
            }
            _snackbarMessage.value = "Moved note to \"$targetFolder\""
        }
    }

    fun openMoveNoteDialog(note: NoteEntity) {
        _moveDialogNote.value = note
    }

    fun dismissMoveNoteDialog() {
        _moveDialogNote.value = null
    }

    fun openManageFoldersDialog() {
        _showManageFoldersDialog.value = true
    }

    fun dismissManageFoldersDialog() {
        _showManageFoldersDialog.value = false
    }

    fun openRenameFolderDialog(folder: FolderItem) {
        _folderToRename.value = folder
    }

    fun dismissRenameFolderDialog() {
        _folderToRename.value = null
    }

    fun openCreateFolderDialog() {
        _showCreateFolderDialog.value = true
    }

    fun dismissCreateFolderDialog() {
        _showCreateFolderDialog.value = false
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun setDarkMode(isDark: Boolean?) {
        _isDarkMode.value = isDark
        if (isDark == null) {
            themePrefs.edit().remove("is_dark_mode").apply()
        } else {
            themePrefs.edit().putBoolean("is_dark_mode", isDark).apply()
        }
    }

    fun toggleDarkMode() {
        val next = when (_isDarkMode.value) {
            null -> false
            false -> true
            true -> null
        }
        setDarkMode(next)
    }

    fun setStartupLockEnabled(enabled: Boolean) {
        securityManager.setAppLockEnabled(enabled)
        _isStartupLockEnabled.value = enabled
    }

    fun removeMasterPin() {
        securityManager.removeMasterPin()
        _isStartupLockEnabled.value = false
        _isAppLocked.value = false
    }

    // Note Creation & Editing
    fun createNewNote(type: NoteType = NoteType.TEXT) {
        flushAutosaveNow()
        _editorState.value = EditorState(
            noteId = 0L,
            title = "",
            content = "",
            noteType = type,
            folder = if (_selectedFolder.value != "All") _selectedFolder.value else "General",
            tags = if (_selectedTag.value != null) listOf(_selectedTag.value!!) else emptyList(),
            colorIndex = 0,
            fontStyle = NoteFontStyle.SANS,
            fontSize = 16f,
            checklistItems = if (type == NoteType.CHECKLIST) listOf(
                ChecklistItem(UUID.randomUUID().toString(), "", false)
            ) else emptyList(),
            autosaveStatus = "New note"
        )
        _currentScreen.value = ScreenDestination.EDITOR
    }

    fun createDiaryEntry(dateMillis: Long = System.currentTimeMillis()) {
        flushAutosaveNow()
        val dateFormatted = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(dateMillis))
        val defaultDiaryContent = """
🌿 Daily Living Reflection
• Today's Highlights: 

🙏 Gratitude & Mindfulness:
1. 
2. 

🌱 Daily Living Thoughts & Lessons:

""".trimIndent()

        _editorState.value = EditorState(
            noteId = 0L,
            title = "Diary — $dateFormatted",
            content = defaultDiaryContent,
            noteType = NoteType.DIARY,
            folder = "Diary",
            tags = listOf("Diary", "DailyLiving"),
            colorIndex = 3, // Warm Peach / Comforting Theme
            fontStyle = NoteFontStyle.SERIF,
            fontSize = 16f,
            createdAt = dateMillis,
            autosaveStatus = "New Diary Entry"
        )
        _currentScreen.value = ScreenDestination.EDITOR
    }

    fun openNote(note: NoteEntity) {
        if (note.isLocked && !securityManager.isNoteUnlocked(note.id)) {
            _pinDialogNoteToUnlock.value = note
            return
        }
        loadNoteIntoEditor(note)
    }

    private fun loadNoteIntoEditor(note: NoteEntity) {
        val font = try {
            NoteFontStyle.valueOf(note.fontStyle)
        } catch (e: Exception) {
            NoteFontStyle.SANS
        }
        val type = try {
            NoteType.valueOf(note.noteType)
        } catch (e: Exception) {
            NoteType.TEXT
        }

        _editorState.value = EditorState(
            noteId = note.id,
            title = note.title,
            content = note.content,
            noteType = type,
            folder = note.folder,
            tags = note.getTagList(),
            colorIndex = note.colorIndex,
            fontStyle = font,
            fontSize = note.fontSize,
            isPinned = note.isPinned,
            isFavorite = note.isFavorite,
            isLocked = note.isLocked,
            isArchived = note.isArchived,
            reminderEpochMillis = note.reminderEpochMillis,
            checklistItems = note.parseChecklist(),
            sketchStrokes = note.parseSketchStrokes(),
            audioFilePath = note.audioFilePath,
            audioDurationSeconds = note.audioDurationSeconds,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            autosaveStatus = "Saved"
        )
        _currentScreen.value = ScreenDestination.EDITOR
    }

    fun dismissPinDialog() {
        _pinDialogNoteToUnlock.value = null
        _isSettingUpPin.value = false
    }

    fun unlockAppWithPin(pin: String): Boolean {
        val success = securityManager.unlockApp(pin)
        if (success) {
            _isAppLocked.value = false
        }
        return success
    }

    fun lockAppNow() {
        securityManager.lockApp()
        _isAppLocked.value = true
    }

    fun setupMasterPin(pin: String): Boolean {
        val success = securityManager.setMasterPin(pin)
        if (success) {
            _isAppLocked.value = false
            _isSettingUpPin.value = false
        }
        return success
    }

    fun onPinEnteredForNote(pin: String): Boolean {
        val note = _pinDialogNoteToUnlock.value ?: return false
        if (securityManager.verifyMasterPin(pin)) {
            securityManager.markNoteUnlocked(note.id)
            _pinDialogNoteToUnlock.value = null
            loadNoteIntoEditor(note)
            return true
        }
        return false
    }

    fun startPinSetup() {
        _isSettingUpPin.value = true
    }

    fun onPinSetupCompleted(pin: String): Boolean {
        val success = securityManager.setMasterPin(pin)
        if (success) {
            _isSettingUpPin.value = false
            _isAppLocked.value = false
        }
        return success
    }

    // Editor field updates with Debounced Autosave
    fun updateTitle(title: String) {
        _editorState.value = _editorState.value.copy(title = title, autosaveStatus = "Saving...")
        triggerDebouncedAutosave()
    }

    fun updateContent(content: String) {
        _editorState.value = _editorState.value.copy(content = content, autosaveStatus = "Saving...")
        triggerDebouncedAutosave()
    }

    fun appendSpeechText(text: String) {
        val current = _editorState.value.content
        val newContent = if (current.isBlank()) text else "$current $text"
        _editorState.value = _editorState.value.copy(content = newContent, autosaveStatus = "Saving...")
        triggerDebouncedAutosave()
    }

    fun updateNoteType(type: NoteType) {
        _editorState.value = _editorState.value.copy(noteType = type)
        triggerDebouncedAutosave()
    }

    fun updateFolder(folder: String) {
        _editorState.value = _editorState.value.copy(folder = folder)
        triggerDebouncedAutosave()
    }

    fun addTag(tag: String) {
        val clean = tag.trim().removePrefix("#")
        if (clean.isNotBlank() && !_editorState.value.tags.contains(clean)) {
            _editorState.value = _editorState.value.copy(tags = _editorState.value.tags + clean)
            triggerDebouncedAutosave()
        }
    }

    fun removeTag(tag: String) {
        _editorState.value = _editorState.value.copy(tags = _editorState.value.tags - tag)
        triggerDebouncedAutosave()
    }

    fun updateColor(colorIndex: Int) {
        _editorState.value = _editorState.value.copy(colorIndex = colorIndex)
        triggerDebouncedAutosave()
    }

    fun updateFontStyle(fontStyle: NoteFontStyle) {
        _editorState.value = _editorState.value.copy(fontStyle = fontStyle)
        triggerDebouncedAutosave()
    }

    fun updateFontSize(fontSize: Float) {
        _editorState.value = _editorState.value.copy(fontSize = fontSize)
        triggerDebouncedAutosave()
    }

    fun togglePinned() {
        _editorState.value = _editorState.value.copy(isPinned = !_editorState.value.isPinned)
        triggerDebouncedAutosave()
    }

    fun toggleFavorite() {
        _editorState.value = _editorState.value.copy(isFavorite = !_editorState.value.isFavorite)
        triggerDebouncedAutosave()
    }

    fun toggleLocked() {
        val targetLocked = !_editorState.value.isLocked
        if (targetLocked && !securityManager.hasMasterPin()) {
            _isSettingUpPin.value = true
            return
        }
        _editorState.value = _editorState.value.copy(isLocked = targetLocked)
        if (targetLocked) {
            securityManager.markNoteUnlocked(_editorState.value.noteId)
        }
        triggerDebouncedAutosave()
    }

    fun toggleArchived() {
        _editorState.value = _editorState.value.copy(isArchived = !_editorState.value.isArchived)
        triggerDebouncedAutosave()
    }

    fun setReminder(epochMillis: Long?) {
        _editorState.value = _editorState.value.copy(reminderEpochMillis = epochMillis)
        val noteId = _editorState.value.noteId
        if (epochMillis != null) {
            reminderManager.scheduleReminder(
                noteId = if (noteId == 0L) System.currentTimeMillis() else noteId,
                title = _editorState.value.title,
                content = _editorState.value.content,
                epochMillis = epochMillis
            )
        } else if (noteId > 0) {
            reminderManager.cancelReminder(noteId)
        }
        triggerDebouncedAutosave()
    }

    // Checklist operations
    fun addChecklistItem(text: String = "") {
        val newItem = ChecklistItem(UUID.randomUUID().toString(), text, false)
        _editorState.value = _editorState.value.copy(
            checklistItems = _editorState.value.checklistItems + newItem
        )
        triggerDebouncedAutosave()
    }

    fun updateChecklistItemText(id: String, text: String) {
        val updated = _editorState.value.checklistItems.map {
            if (it.id == id) it.copy(text = text) else it
        }
        _editorState.value = _editorState.value.copy(checklistItems = updated)
        triggerDebouncedAutosave()
    }

    fun toggleChecklistItemDone(id: String) {
        val updated = _editorState.value.checklistItems.map {
            if (it.id == id) it.copy(isDone = !it.isDone) else it
        }
        _editorState.value = _editorState.value.copy(checklistItems = updated)
        triggerDebouncedAutosave()
    }

    fun deleteChecklistItem(id: String) {
        val updated = _editorState.value.checklistItems.filter { it.id != id }
        _editorState.value = _editorState.value.copy(checklistItems = updated)
        triggerDebouncedAutosave()
    }

    // Sketch operations
    fun addSketchStroke(stroke: SketchStroke) {
        val currentStrokes = _editorState.value.sketchStrokes
        _editorState.value = _editorState.value.copy(
            sketchStrokes = currentStrokes + stroke,
            sketchUndoStack = _editorState.value.sketchUndoStack + listOf(currentStrokes)
        )
        triggerDebouncedAutosave()
    }

    fun undoSketch() {
        val undoStack = _editorState.value.sketchUndoStack
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.last()
            _editorState.value = _editorState.value.copy(
                sketchStrokes = previous,
                sketchUndoStack = undoStack.dropLast(1)
            )
            triggerDebouncedAutosave()
        }
    }

    fun clearSketch() {
        val currentStrokes = _editorState.value.sketchStrokes
        if (currentStrokes.isNotEmpty()) {
            _editorState.value = _editorState.value.copy(
                sketchStrokes = emptyList(),
                sketchUndoStack = _editorState.value.sketchUndoStack + listOf(currentStrokes)
            )
            triggerDebouncedAutosave()
        }
    }

    // Audio operations
    fun attachAudioFile(file: File, durationSec: Int) {
        _editorState.value = _editorState.value.copy(
            audioFilePath = file.absolutePath,
            audioDurationSeconds = durationSec,
            noteType = NoteType.AUDIO
        )
        triggerDebouncedAutosave()
    }

    fun removeAudio() {
        _editorState.value.audioFilePath?.let { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {
                // ignore
            }
        }
        _editorState.value = _editorState.value.copy(
            audioFilePath = null,
            audioDurationSeconds = 0
        )
        triggerDebouncedAutosave()
    }

    // Autosave Debounce Engine
    private fun triggerDebouncedAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(600) // debounce 600ms
            performSave()
        }
    }

    fun flushAutosaveNow() {
        autosaveJob?.cancel()
        viewModelScope.launch {
            performSave()
        }
    }

    private suspend fun performSave() {
        val state = _editorState.value

        // If completely empty new note, don't save
        if (state.noteId == 0L && state.title.isBlank() && state.content.isBlank() &&
            state.checklistItems.isEmpty() && state.sketchStrokes.isEmpty() && state.audioFilePath == null
        ) {
            _editorState.value = _editorState.value.copy(autosaveStatus = "Draft")
            return
        }

        val noteEntity = NoteEntity(
            id = state.noteId,
            title = state.title,
            content = state.content,
            noteType = state.noteType.name,
            folder = state.folder.ifBlank { "General" },
            tags = state.tags,
            colorIndex = state.colorIndex,
            fontStyle = state.fontStyle.name,
            fontSize = state.fontSize,
            isPinned = state.isPinned,
            isFavorite = state.isFavorite,
            isArchived = state.isArchived,
            isLocked = state.isLocked,
            isDeleted = false,
            reminderEpochMillis = state.reminderEpochMillis,
            checklistJson = NoteEntity.serializeChecklist(state.checklistItems),
            sketchDataJson = NoteEntity.serializeSketchStrokes(state.sketchStrokes),
            audioFilePath = state.audioFilePath,
            audioDurationSeconds = state.audioDurationSeconds,
            createdAt = state.createdAt,
            updatedAt = System.currentTimeMillis()
        )

        val newId = repository.saveNote(noteEntity)
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _editorState.value = _editorState.value.copy(
            noteId = newId,
            updatedAt = System.currentTimeMillis(),
            autosaveStatus = "Autosaved at $timeStr"
        )
    }

    // Note list actions
    fun togglePinFromCard(note: NoteEntity) {
        viewModelScope.launch {
            repository.setPinned(note.id, !note.isPinned)
        }
    }

    fun toggleFavoriteFromCard(note: NoteEntity) {
        viewModelScope.launch {
            repository.setFavorite(note.id, !note.isFavorite)
        }
    }

    fun archiveNoteFromCard(note: NoteEntity) {
        viewModelScope.launch {
            repository.setArchived(note.id, true)
        }
    }

    fun unarchiveNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.setArchived(note.id, false)
        }
    }

    fun moveNoteToTrash(noteId: Long) {
        viewModelScope.launch {
            repository.moveToTrash(noteId)
            if (_editorState.value.noteId == noteId) {
                _currentScreen.value = ScreenDestination.HOME
            }
        }
    }

    fun restoreNoteFromTrash(noteId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(noteId)
        }
    }

    fun deletePermanently(noteId: Long) {
        viewModelScope.launch {
            repository.deletePermanently(noteId)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    // Export & Share
    fun shareNote(context: Context, noteId: Long) {
        viewModelScope.launch {
            val note = repository.getNoteByIdSync(noteId) ?: return@launch
            val sb = StringBuilder()
            if (note.title.isNotBlank()) {
                sb.append(note.title).append("\n\n")
            }
            if (note.content.isNotBlank()) {
                sb.append(note.content).append("\n\n")
            }
            val checklist = note.parseChecklist()
            if (checklist.isNotEmpty()) {
                sb.append("Checklist:\n")
                for (item in checklist) {
                    val mark = if (item.isDone) "[x]" else "[ ]"
                    sb.append("$mark ${item.text}\n")
                }
                sb.append("\n")
            }
            if (note.tags.isNotEmpty()) {
                sb.append("Tags: ").append(note.tags.joinToString(" ") { "#$it" }).append("\n")
            }
            sb.append("— Shared via Jotter")

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Jotter Note" })
                putExtra(Intent.EXTRA_TEXT, sb.toString())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "Share Note via").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    // ==================== BACKUP & RESTORE ====================
    fun exportBackupJsonToUri(context: Context, uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val notes = repository.getAllNotesForBackup()
                val jsonString = com.example.data.BackupManager.exportToJson(notes)
                val success = com.example.data.BackupManager.writeTextToUri(context, uri, jsonString)
                if (success) {
                    onResult(true, "Exported ${notes.size} entries to JSON backup.")
                } else {
                    onResult(false, "Failed to write backup file.")
                }
            } catch (e: Exception) {
                onResult(false, "Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun exportBackupTxtToUri(context: Context, uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val notes = repository.getAllNotesForBackup()
                val txtString = com.example.data.BackupManager.exportToPlainText(notes)
                val success = com.example.data.BackupManager.writeTextToUri(context, uri, txtString)
                if (success) {
                    onResult(true, "Exported ${notes.size} entries to text archive.")
                } else {
                    onResult(false, "Failed to write text export.")
                }
            } catch (e: Exception) {
                onResult(false, "Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun shareBackupJson(context: Context) {
        viewModelScope.launch {
            try {
                val notes = repository.getAllNotesForBackup()
                val jsonString = com.example.data.BackupManager.exportToJson(notes)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_SUBJECT, "Jotter Backup - ${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}")
                    putExtra(Intent.EXTRA_TEXT, jsonString)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(intent, "Share JSON Backup via").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun shareBackupTxt(context: Context) {
        viewModelScope.launch {
            try {
                val notes = repository.getAllNotesForBackup()
                val txtString = com.example.data.BackupManager.exportToPlainText(notes)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Jotter Text Backup - ${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}")
                    putExtra(Intent.EXTRA_TEXT, txtString)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(intent, "Share Notes Text Backup via").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun importBackupFromUri(context: Context, uri: android.net.Uri, overwrite: Boolean, onResult: (Boolean, Int, String) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonString = com.example.data.BackupManager.readTextFromUri(context, uri)
                if (jsonString.isBlank()) {
                    onResult(false, 0, "Selected file is empty or could not be read.")
                    return@launch
                }
                val notes = com.example.data.BackupManager.parseJsonBackup(jsonString)
                if (notes.isEmpty()) {
                    onResult(false, 0, "No valid Jotter notes found in the selected file.")
                    return@launch
                }
                repository.restoreNotes(notes, overwrite = overwrite)
                onResult(true, notes.size, "Successfully restored ${notes.size} entries.")
            } catch (e: Exception) {
                onResult(false, 0, "Import failed: ${e.localizedMessage}")
            }
        }
    }
}
