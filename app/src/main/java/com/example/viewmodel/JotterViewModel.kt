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
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import com.example.model.ChecklistItem
import com.example.model.NoteFontStyle
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
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
    private val repository: NoteRepository = NoteRepository(AppDatabase.getDatabase(application).noteDao()),
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

    private val _isDarkMode = MutableStateFlow<Boolean?>(null) // null = system default
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    // PIN lock dialog state
    private val _pinDialogNoteToUnlock = MutableStateFlow<NoteEntity?>(null)
    val pinDialogNoteToUnlock: StateFlow<NoteEntity?> = _pinDialogNoteToUnlock.asStateFlow()

    private val _isSettingUpPin = MutableStateFlow(false)
    val isSettingUpPin: StateFlow<Boolean> = _isSettingUpPin.asStateFlow()

    // Active Editor State
    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private var autosaveJob: Job? = null

    // Notes Data Streams
    val rawActiveNotes = repository.activeNotes
    val archivedNotes = repository.archivedNotes
    val deletedNotes = repository.deletedNotes
    val favoriteNotes = repository.favoriteNotes
    val allDbFolders = repository.allFolders
    val activeNotesCount = repository.activeNotesCount
    val deletedNotesCount = repository.deletedNotesCount

    // Combined filtered notes for Home
    val homeFilteredNotes: StateFlow<List<NoteEntity>> = combine(
        rawActiveNotes,
        _searchQuery,
        _selectedFolder,
        _selectedTag
    ) { notes, query, folder, tag ->
        var list = notes

        if (folder != "All") {
            list = list.filter { it.folder.equals(folder, ignoreCase = true) }
        }

        if (tag != null) {
            list = list.filter { it.getTagList().contains(tag) }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase(Locale.getDefault())
            list = list.filter { note ->
                note.title.lowercase(Locale.getDefault()).contains(q) ||
                note.content.lowercase(Locale.getDefault()).contains(q) ||
                note.tags.lowercase(Locale.getDefault()).contains(q) ||
                note.parseChecklist().any { it.text.lowercase(Locale.getDefault()).contains(q) }
            }
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All distinct tags across all active notes
    val allTags: StateFlow<List<String>> = rawActiveNotes.combine(_searchQuery) { notes, _ ->
        notes.flatMap { it.getTagList() }.distinct().sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Available folders list
    val folderList: StateFlow<List<String>> = allDbFolders.combine(_selectedFolder) { dbFolders, _ ->
        val defaultList = listOf("All", "Personal", "Work", "Ideas", "Study", "Journal")
        (defaultList + dbFolders).distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("All", "Personal", "Work", "Ideas", "Study", "Journal")
    )

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

    fun selectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun setDarkMode(isDark: Boolean?) {
        _isDarkMode.value = isDark
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
            tags = state.tags.joinToString(","),
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
            if (note.tags.isNotBlank()) {
                sb.append("Tags: ").append(note.tags.split(",").joinToString(" ") { "#$it" }).append("\n")
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
}
