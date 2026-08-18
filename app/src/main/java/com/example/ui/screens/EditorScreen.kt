package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NoteColorThemes
import com.example.model.NoteFontStyle
import com.example.model.NoteType
import com.example.model.ScreenDestination
import com.example.ui.components.AudioNoteRecorder
import com.example.ui.components.ChecklistEditor
import com.example.ui.components.ReminderPickerDialog
import com.example.ui.components.SketchCanvas
import com.example.viewmodel.JotterViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    viewModel: JotterViewModel
) {
    val editorState by viewModel.editorState.collectAsState()
    val folders by viewModel.folderList.collectAsState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val colorTheme = NoteColorThemes.getById(editorState.colorIndex)
    val pageBg = if (isDark) colorTheme.darkBg else colorTheme.lightBg

    var showReminderPicker by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showStyleSheet by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }
    var showFolderMenu by remember { mutableStateOf(false) }

    // Intercept hardware / gesture back to save and exit
    BackHandler {
        viewModel.setScreen(ScreenDestination.HOME)
    }

    if (showReminderPicker) {
        ReminderPickerDialog(
            currentReminderEpoch = editorState.reminderEpochMillis,
            onReminderSet = { epoch -> viewModel.setReminder(epoch) },
            onDismiss = { showReminderPicker = false }
        )
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Add Tag", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    label = { Text("Tag Name (e.g. work, project)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_tag_text_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagInput.isNotBlank()) {
                            viewModel.addTag(newTagInput)
                            newTagInput = ""
                            showTagDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .imePadding(),
        containerColor = pageBg,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = {
                    // Autosave indicator pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = editorState.autosaveStatus,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setScreen(ScreenDestination.HOME) },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Pin toggle
                    IconButton(
                        onClick = { viewModel.togglePinned() },
                        modifier = Modifier.testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (editorState.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (editorState.isPinned) colorTheme.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Reminder button
                    IconButton(
                        onClick = { showReminderPicker = true },
                        modifier = Modifier.testTag("editor_reminder_button")
                    ) {
                        Icon(
                            imageVector = if (editorState.reminderEpochMillis != null) Icons.Default.Notifications else Icons.Outlined.Notifications,
                            contentDescription = "Reminder",
                            tint = if (editorState.reminderEpochMillis != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Lock button
                    IconButton(
                        onClick = { viewModel.toggleLocked() },
                        modifier = Modifier.testTag("editor_lock_button")
                    ) {
                        Icon(
                            imageVector = if (editorState.isLocked) Icons.Default.Lock else Icons.Outlined.LockOpen,
                            contentDescription = "Lock with PIN",
                            tint = if (editorState.isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // More Menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (editorState.isFavorite) "Remove Favorite" else "Add to Favorites") },
                                leadingIcon = {
                                    Icon(
                                        if (editorState.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    viewModel.toggleFavorite()
                                    showMoreMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(if (editorState.isArchived) "Unarchive" else "Archive") },
                                leadingIcon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                                onClick = {
                                    viewModel.toggleArchived()
                                    showMoreMenu = false
                                    viewModel.setScreen(ScreenDestination.HOME)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Share Note") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    viewModel.flushAutosaveNow()
                                    viewModel.shareNote(context, editorState.noteId)
                                    showMoreMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Copy to Clipboard") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val textToCopy = "${editorState.title}\n\n${editorState.content}"
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Jotter Note", textToCopy))
                                    Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                                    showMoreMenu = false
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Move to Trash", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    if (editorState.noteId > 0) {
                                        viewModel.moveNoteToTrash(editorState.noteId)
                                    } else {
                                        viewModel.setScreen(ScreenDestination.HOME)
                                    }
                                    showMoreMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
            )
        },
        bottomBar = {
            // Formatting & Customization Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Color Palette Selector Row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(NoteColorThemes.presets) { preset ->
                            val isSelected = editorState.colorIndex == preset.id
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 30.dp else 24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) preset.darkBg else preset.lightBg)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) preset.accentColor else Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.updateColor(preset.id) }
                                    .testTag("color_picker_${preset.id}")
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.width(4.dp))
                            // Font Style Selector Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .clickable {
                                        val nextFont = when (editorState.fontStyle) {
                                            NoteFontStyle.SANS -> NoteFontStyle.SERIF
                                            NoteFontStyle.SERIF -> NoteFontStyle.MONO
                                            NoteFontStyle.MONO -> NoteFontStyle.CURSIVE
                                            NoteFontStyle.CURSIVE -> NoteFontStyle.SANS
                                        }
                                        viewModel.updateFontStyle(nextFont)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("font_style_toggle_button")
                            ) {
                                Text(
                                    text = "Font: ${editorState.fontStyle.displayName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        item {
                            // Font Size Adjust Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .clickable {
                                        val nextSize = if (editorState.fontSize >= 22f) 14f else editorState.fontSize + 2f
                                        viewModel.updateFontSize(nextSize)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Size: ${editorState.fontSize.toInt()}sp",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Word Count & Stats Footer
                    val words = editorState.content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                    val chars = editorState.content.length
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$words words  •  $chars characters",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        Text(
                            text = "Auto-saving locally (Offline)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            // Folder Selector & Tag Management Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Folder Selector Chip
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showFolderMenu = true },
                        color = colorTheme.accentColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📁 ${editorState.folder}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorTheme.accentColor
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showFolderMenu,
                        onDismissRequest = { showFolderMenu = false }
                    ) {
                        folders.filter { it != "All" }.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder) },
                                onClick = {
                                    viewModel.updateFolder(folder)
                                    showFolderMenu = false
                                }
                            )
                        }
                    }
                }

                // Add Tag Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showTagDialog = true },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Add Tag",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Tags display row
            if (editorState.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    editorState.tags.forEach { tag ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorTheme.accentColor.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorTheme.accentColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove tag",
                                tint = colorTheme.accentColor,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.removeTag(tag) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note Type Selector Tabs (Text, Checklist, Sketch, Audio)
            val noteTypes = listOf(
                NoteType.TEXT to "Text",
                NoteType.CHECKLIST to "Checklist",
                NoteType.SKETCH to "Sketch",
                NoteType.AUDIO to "Voice"
            )
            val selectedTabIndex = noteTypes.indexOfFirst { it.first == editorState.noteType }.coerceAtLeast(0)

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = colorTheme.accentColor,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                noteTypes.forEachIndexed { index, (type, label) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.updateNoteType(type) },
                        text = {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title Text Input
            BasicTextField(
                value = editorState.title,
                onValueChange = { viewModel.updateTitle(it) },
                textStyle = TextStyle(
                    fontFamily = editorState.fontStyle.fontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(colorTheme.accentColor),
                decorationBox = { innerTextField ->
                    if (editorState.title.isEmpty()) {
                        Text(
                            text = "Title",
                            fontFamily = editorState.fontStyle.fontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_title_input"),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Body Area according to Note Type
            when (editorState.noteType) {
                NoteType.CHECKLIST -> {
                    ChecklistEditor(
                        items = editorState.checklistItems,
                        onItemTextChange = { id, text -> viewModel.updateChecklistItemText(id, text) },
                        onItemToggle = { id -> viewModel.toggleChecklistItemDone(id) },
                        onItemDelete = { id -> viewModel.deleteChecklistItem(id) },
                        onAddItem = { text -> viewModel.addChecklistItem(text) },
                        fontFamily = editorState.fontStyle.fontFamily,
                        fontSize = editorState.fontSize
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Optional additional text notes below checklist
                    BasicTextField(
                        value = editorState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        textStyle = TextStyle(
                            fontFamily = editorState.fontStyle.fontFamily,
                            fontSize = editorState.fontSize.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = (editorState.fontSize * 1.5).sp
                        ),
                        cursorBrush = SolidColor(colorTheme.accentColor),
                        decorationBox = { innerTextField ->
                            if (editorState.content.isEmpty()) {
                                Text(
                                    text = "Add extra notes or checklist summary...",
                                    fontFamily = editorState.fontStyle.fontFamily,
                                    fontSize = editorState.fontSize.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("editor_checklist_notes_input")
                    )
                }

                NoteType.SKETCH -> {
                    SketchCanvas(
                        strokes = editorState.sketchStrokes,
                        onStrokeAdded = { stroke -> viewModel.addSketchStroke(stroke) },
                        onUndo = { viewModel.undoSketch() },
                        onClear = { viewModel.clearSketch() }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BasicTextField(
                        value = editorState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        textStyle = TextStyle(
                            fontFamily = editorState.fontStyle.fontFamily,
                            fontSize = editorState.fontSize.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = (editorState.fontSize * 1.5).sp
                        ),
                        cursorBrush = SolidColor(colorTheme.accentColor),
                        decorationBox = { innerTextField ->
                            if (editorState.content.isEmpty()) {
                                Text(
                                    text = "Notes or description for this sketch...",
                                    fontFamily = editorState.fontStyle.fontFamily,
                                    fontSize = editorState.fontSize.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("editor_sketch_notes_input")
                    )
                }

                NoteType.AUDIO -> {
                    AudioNoteRecorder(
                        audioRecorder = viewModel.audioRecorder,
                        audioPlayer = viewModel.audioPlayer,
                        speechToText = viewModel.speechToText,
                        audioFilePath = editorState.audioFilePath,
                        audioDurationSeconds = editorState.audioDurationSeconds,
                        onAudioSaved = { file, duration -> viewModel.attachAudioFile(file, duration) },
                        onAudioRemoved = { viewModel.removeAudio() },
                        onSpeechTranscribed = { text -> viewModel.appendSpeechText(text) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BasicTextField(
                        value = editorState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        textStyle = TextStyle(
                            fontFamily = editorState.fontStyle.fontFamily,
                            fontSize = editorState.fontSize.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = (editorState.fontSize * 1.5).sp
                        ),
                        cursorBrush = SolidColor(colorTheme.accentColor),
                        decorationBox = { innerTextField ->
                            if (editorState.content.isEmpty()) {
                                Text(
                                    text = "Audio transcript or additional thoughts...",
                                    fontFamily = editorState.fontStyle.fontFamily,
                                    fontSize = editorState.fontSize.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("editor_audio_notes_input")
                    )
                }

                else -> {
                    // Standard Text Note
                    BasicTextField(
                        value = editorState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        textStyle = TextStyle(
                            fontFamily = editorState.fontStyle.fontFamily,
                            fontSize = editorState.fontSize.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = (editorState.fontSize * 1.5).sp
                        ),
                        cursorBrush = SolidColor(colorTheme.accentColor),
                        decorationBox = { innerTextField ->
                            if (editorState.content.isEmpty()) {
                                Text(
                                    text = "Start writing your thoughts, ideas, or meeting notes...",
                                    fontFamily = editorState.fontStyle.fontFamily,
                                    fontSize = editorState.fontSize.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_content_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
