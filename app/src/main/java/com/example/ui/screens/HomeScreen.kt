package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NoteType
import com.example.model.ScreenDestination
import com.example.model.ViewMode
import com.example.ui.components.NoteCard
import com.example.ui.components.QuickJotBar
import com.example.ui.theme.MinimalBgDark
import com.example.ui.theme.MinimalBgLight
import com.example.ui.theme.MinimalBorderLight
import com.example.ui.theme.MinimalOnPrimaryContainerDark
import com.example.ui.theme.MinimalOnPrimaryContainerLight
import com.example.ui.theme.MinimalPrimaryContainerDark
import com.example.ui.theme.MinimalPrimaryContainerLight
import com.example.ui.theme.MinimalSurfaceContainer
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.viewmodel.JotterViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JotterViewModel,
    onOpenSettings: () -> Unit
) {
    val notes by viewModel.homeFilteredNotes.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val folders by viewModel.folderList.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    val isDark = isSystemInDarkTheme()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Create Folder", fontWeight = FontWeight.SemiBold) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_folder_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.selectFolder(newFolderName.trim())
                            showNewFolderDialog = false
                            newFolderName = ""
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_folder_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(280.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                drawerContainerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFDFBFF)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                // Minimalist Brand Header
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MinimalPrimaryContainerLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Jotter",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Clean Minimalism",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    label = { Text("All Jots", fontWeight = FontWeight.Medium) },
                    selected = true,
                    onClick = {
                        viewModel.selectFolder("All")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                    label = { Text("Archive", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.setScreen(ScreenDestination.ARCHIVE)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                    label = { Text("Trash", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.setScreen(ScreenDestination.TRASH)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 24.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    label = { Text("Settings & PIN", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = if (isDark) MinimalBgDark else MinimalBgLight,
            contentWindowInsets = WindowInsets.statusBars,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    QuickJotBar(
                        onNewTextNote = { viewModel.createNewNote(NoteType.TEXT) },
                        onNewChecklist = { viewModel.createNewNote(NoteType.CHECKLIST) },
                        onNewSketch = { viewModel.createNewNote(NoteType.SKETCH) },
                        onNewAudio = { viewModel.createNewNote(NoteType.AUDIO) }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header Bar with "Jotter" Title, Menu, and User Avatar Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Jotter",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleViewMode() },
                            modifier = Modifier.size(36.dp).testTag("toggle_view_mode_button")
                        ) {
                            Icon(
                                imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                contentDescription = "Toggle Grid/List",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color(0xFFE0E2EC))
                                .clickable { onOpenSettings() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "JD",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF44474E)
                            )
                        }
                    }
                }

                // Search Bar: Soft `#EFF1F9` background, rounded-full, 50dp height
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(26.dp),
                    color = if (isDark) MaterialTheme.colorScheme.surfaceContainer else MinimalSurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE0E2EC)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔍",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search your jots...",
                                        fontSize = 14.sp,
                                        color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF44474E)
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_search_input"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Pills: Active `#D6E3FF`, Inactive White with `#C4C6D0` border
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(folders) { folder ->
                        val isSelected = selectedFolder.equals(folder, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) (if (isDark) MinimalPrimaryContainerDark else MinimalPrimaryContainerLight)
                                    else (if (isDark) MaterialTheme.colorScheme.surface else Color.White)
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = if (isSelected) Color.Transparent else (if (isDark) MaterialTheme.colorScheme.outlineVariant else MinimalBorderLight),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectFolder(folder) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("folder_chip_$folder")
                        ) {
                            Text(
                                text = folder,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) (if (isDark) MinimalOnPrimaryContainerDark else MinimalOnPrimaryContainerLight)
                                else (if (isDark) MaterialTheme.colorScheme.onSurface else MinimalTextSecondary)
                            )
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MinimalSurfaceContainer)
                                .clickable { showNewFolderDialog = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Folder",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Tag Filter Row (if present)
                if (allTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allTags) { tag ->
                            val isSelected = selectedTag == tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .clickable { viewModel.selectTag(tag) }
                                    .padding(horizontal = 10.dp, vertical = 3.5.dp)
                                    .testTag("tag_chip_$tag")
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes Content Grid or List
                val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
                val otherNotes = remember(notes) { notes.filter { !it.isPinned } }

                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MinimalPrimaryContainerLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✏️", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedTag != null) "No matching jots" else "No jots yet",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the quick capture bar below to start jotting.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    if (viewMode == ViewMode.GRID) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalItemSpacing = 10.dp
                        ) {
                            if (pinnedNotes.isNotEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.PushPin,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PINNED",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                items(pinnedNotes, key = { it.id }) { note ->
                                    NoteCard(
                                        note = note,
                                        onClick = { viewModel.openNote(note) },
                                        onPinClick = { viewModel.togglePinFromCard(note) },
                                        onFavoriteClick = { viewModel.toggleFavoriteFromCard(note) },
                                        onArchiveSwipe = { viewModel.archiveNoteFromCard(note) },
                                        onDeleteSwipe = { viewModel.moveNoteToTrash(note.id) }
                                    )
                                }

                                if (otherNotes.isNotEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        Text(
                                            text = "JOTS",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                                        )
                                    }
                                }
                            }

                            items(otherNotes, key = { it.id }) { note ->
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
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (pinnedNotes.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.PushPin,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PINNED",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                items(pinnedNotes, key = { it.id }) { note ->
                                    NoteCard(
                                        note = note,
                                        onClick = { viewModel.openNote(note) },
                                        onPinClick = { viewModel.togglePinFromCard(note) },
                                        onFavoriteClick = { viewModel.toggleFavoriteFromCard(note) },
                                        onArchiveSwipe = { viewModel.archiveNoteFromCard(note) },
                                        onDeleteSwipe = { viewModel.moveNoteToTrash(note.id) }
                                    )
                                }

                                if (otherNotes.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "JOTS",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                                        )
                                    }
                                }
                            }

                            items(otherNotes, key = { it.id }) { note ->
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
    }
}
