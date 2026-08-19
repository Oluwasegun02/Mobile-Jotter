package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DateFilterState
import com.example.model.FolderItem
import com.example.model.NoteSortOrder
import com.example.model.NoteType
import com.example.model.ScreenDestination
import com.example.model.ViewMode
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.ManageFoldersDialog
import com.example.ui.components.MiniCalendarWidget
import com.example.ui.components.MoveNoteDialog
import com.example.ui.components.NoteCard
import com.example.ui.components.QuickJotBar
import com.example.ui.components.RenameFolderDialog
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
    val sortOrder by viewModel.sortOrder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val folders by viewModel.folderList.collectAsState()
    val foldersWithCounts by viewModel.foldersWithCounts.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val notesByDate by viewModel.notesByDateKey.collectAsState()

    val moveDialogNote by viewModel.moveDialogNote.collectAsState()
    val showManageFoldersDialog by viewModel.showManageFoldersDialog.collectAsState()
    val folderToRename by viewModel.folderToRename.collectAsState()
    val showCreateFolderDialog by viewModel.showCreateFolderDialog.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Folder Management Dialogs
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { viewModel.dismissCreateFolderDialog() },
            onConfirm = { name, colorHex ->
                viewModel.createFolder(name, colorHex)
                viewModel.dismissCreateFolderDialog()
            }
        )
    }

    folderToRename?.let { folder ->
        RenameFolderDialog(
            folder = folder,
            onDismiss = { viewModel.dismissRenameFolderDialog() },
            onConfirm = { newName, newColorHex ->
                viewModel.renameFolder(folder.name, newName, newColorHex)
                viewModel.dismissRenameFolderDialog()
            }
        )
    }

    moveDialogNote?.let { note ->
        MoveNoteDialog(
            note = note,
            folders = foldersWithCounts,
            onDismiss = { viewModel.dismissMoveNoteDialog() },
            onFolderSelected = { targetFolder ->
                viewModel.moveNoteToFolder(note.id, targetFolder)
                viewModel.dismissMoveNoteDialog()
            },
            onCreateNewFolder = {
                viewModel.dismissMoveNoteDialog()
                viewModel.openCreateFolderDialog()
            }
        )
    }

    if (showManageFoldersDialog) {
        ManageFoldersDialog(
            folders = foldersWithCounts,
            onDismiss = { viewModel.dismissManageFoldersDialog() },
            onCreateFolder = { viewModel.openCreateFolderDialog() },
            onRenameFolder = { folder -> viewModel.openRenameFolderDialog(folder) },
            onDeleteFolder = { folder -> viewModel.deleteFolder(folder.name) },
            onSelectFolder = { folderName -> viewModel.selectFolder(folderName) }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(290.dp)
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
                    selected = selectedFolder == "All",
                    onClick = {
                        viewModel.selectFolder("All")
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                    label = { Text("Daily Journey & Calendar", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.setScreen(ScreenDestination.CALENDAR_JOURNEY)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_calendar_journey")
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

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 20.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Folders Section Header in Drawer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FOLDERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Manage",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                scope.launch { drawerState.close() }
                                viewModel.openManageFoldersDialog()
                            }
                            .padding(4.dp)
                            .testTag("drawer_manage_folders_button")
                    )
                }

                // Dynamic Folder items in Drawer
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(foldersWithCounts, key = { it.name }) { folder ->
                        val isFolderSelected = selectedFolder.equals(folder.name, ignoreCase = true)
                        val folderColor = Color(folder.colorHex)

                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = if (folder.isSystem) Icons.Default.FolderSpecial else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isFolderSelected) MaterialTheme.colorScheme.primary else folderColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = folder.name,
                                    fontWeight = if (isFolderSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            badge = {
                                if (folder.noteCount > 0) {
                                    Text(
                                        text = "${folder.noteCount}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            selected = isFolderSelected,
                            onClick = {
                                viewModel.selectFolder(folder.name)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 20.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    label = { Text("Settings & Backup", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_settings_item")
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = if (isDark) MinimalBgDark else MinimalBgLight,
            contentWindowInsets = WindowInsets.statusBars,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    QuickJotBar(
                        onNewTextNote = { viewModel.createNewNote(NoteType.TEXT) },
                        onNewDiary = { viewModel.createDiaryEntry() },
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sorting Menu Toggle
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.size(36.dp).testTag("toggle_sort_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort Notes: ${sortOrder.label}",
                                    tint = if (sortOrder != NoteSortOrder.NEWEST_FIRST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.testTag("sort_dropdown_menu")
                            ) {
                                NoteSortOrder.values().forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = order.label,
                                                    fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (sortOrder == order) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 14.sp
                                                )
                                                if (sortOrder == order) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(order)
                                            showSortMenu = false
                                        },
                                        modifier = Modifier.testTag("sort_option_${order.name}")
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.setScreen(ScreenDestination.CALENDAR_JOURNEY) },
                            modifier = Modifier.size(36.dp).testTag("calendar_journey_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Daily Journey Calendar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

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

                Spacer(modifier = Modifier.height(4.dp))

                // Mini-Calendar Chronological Overview & Date Range Filter Widget
                MiniCalendarWidget(
                    dateFilter = dateFilter,
                    notesByDate = notesByDate,
                    onSelectDay = { epochMillis ->
                        viewModel.filterBySpecificDate(epochMillis)
                    },
                    onSelectToday = {
                        viewModel.filterByToday()
                    },
                    onSelectThisWeek = {
                        viewModel.filterByThisWeek()
                    },
                    onSelectCustomRange = { start, end ->
                        viewModel.filterByCustomRange(start, end)
                    },
                    onClearFilter = {
                        viewModel.clearDateFilter()
                    },
                    onOpenFullCalendar = {
                        viewModel.setScreen(ScreenDestination.CALENDAR_JOURNEY)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Category Filter Pills: Active `#D6E3FF`, Inactive White with `#C4C6D0` border
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(folders) { folder ->
                        val isSelected = selectedFolder.equals(folder, ignoreCase = true)
                        val folderItem = foldersWithCounts.find { it.name.equals(folder, ignoreCase = true) }
                        val folderColor = folderItem?.let { Color(it.colorHex) }

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
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("folder_chip_$folder")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (folderColor != null && folder != "All") {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(folderColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = folder,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) (if (isDark) MinimalOnPrimaryContainerDark else MinimalOnPrimaryContainerLight)
                                    else (if (isDark) MaterialTheme.colorScheme.onSurface else MinimalTextSecondary)
                                )
                                if (folderItem != null && folderItem.noteCount > 0 && folder != "All") {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${folderItem.noteCount})",
                                        fontSize = 11.sp,
                                        color = if (isSelected) (if (isDark) MinimalOnPrimaryContainerDark.copy(alpha = 0.8f) else MinimalOnPrimaryContainerLight.copy(alpha = 0.8f))
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MinimalSurfaceContainer)
                                .clickable { viewModel.openCreateFolderDialog() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("home_add_folder_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Folder",
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

                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MinimalSurfaceContainer)
                                .clickable { viewModel.openManageFoldersDialog() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("home_manage_folders_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = "Manage Folders",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Manage",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Friendly Vector Illustration Card
                            Card(
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else Color(0xFFE2E7F0)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.size(170.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_empty_notes),
                                        contentDescription = "Empty Notes Illustration",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(20.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = when {
                                    dateFilter.isActive -> "No jots on this date"
                                    searchQuery.isNotBlank() -> "No results found"
                                    selectedTag != null -> "No jots tagged #$selectedTag"
                                    selectedFolder != "All" -> "No jots in $selectedFolder"
                                    else -> "Capture your first thought"
                                },
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = when {
                                    dateFilter.isActive -> "There are no notes recorded for the selected date or range."
                                    searchQuery.isNotBlank() -> "Try searching with different keywords or clear the search query."
                                    selectedTag != null -> "Try selecting another tag or creating a new note with this tag."
                                    selectedFolder != "All" -> "Create your first note in this folder using the button below."
                                    else -> "Jot down ideas, daily reflections, checklists, sketches, or voice memos."
                                },
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Call-To-Action Controls
                            if (dateFilter.isActive) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { viewModel.createJourneyEntry(NoteType.TEXT) },
                                        modifier = Modifier.testTag("empty_create_dated_note_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Jot for Date")
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.clearDateFilter() },
                                        modifier = Modifier.testTag("empty_clear_date_filter_button")
                                    ) {
                                        Text("Show All")
                                    }
                                }
                            } else if (searchQuery.isNotBlank()) {
                                Button(
                                    onClick = { viewModel.setSearchQuery("") },
                                    modifier = Modifier.testTag("empty_clear_search_button")
                                ) {
                                    Text("Clear Search")
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.createNewNote(NoteType.TEXT) },
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                                        modifier = Modifier.testTag("empty_state_create_first_note_btn")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Create First Note", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    }

                                    // Quick entry format shortcuts
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilledTonalButton(
                                            onClick = { viewModel.createNewNote(NoteType.CHECKLIST) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("empty_state_create_checklist_btn")
                                        ) {
                                            Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Checklist", fontSize = 12.sp)
                                        }

                                        FilledTonalButton(
                                            onClick = { viewModel.createNewNote(NoteType.SKETCH) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("empty_state_create_sketch_btn")
                                        ) {
                                            Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sketch", fontSize = 12.sp)
                                        }

                                        FilledTonalButton(
                                            onClick = { viewModel.createNewNote(NoteType.AUDIO) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("empty_state_create_audio_btn")
                                        ) {
                                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Voice", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
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
                                        onDeleteSwipe = { viewModel.moveNoteToTrash(note.id) },
                                        onMoveToFolderClick = { viewModel.openMoveNoteDialog(note) },
                                        onShareClick = { viewModel.shareNoteContent(context, note) },
                                        onExportTxtClick = { viewModel.exportNoteAsTxtFile(context, note) }
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
                                    onDeleteSwipe = { viewModel.moveNoteToTrash(note.id) },
                                    onMoveToFolderClick = { viewModel.openMoveNoteDialog(note) },
                                    onShareClick = { viewModel.shareNoteContent(context, note) },
                                    onExportTxtClick = { viewModel.exportNoteAsTxtFile(context, note) }
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
                                        onDeleteSwipe = { viewModel.moveNoteToTrash(note.id) },
                                        onMoveToFolderClick = { viewModel.openMoveNoteDialog(note) },
                                        onShareClick = { viewModel.shareNoteContent(context, note) },
                                        onExportTxtClick = { viewModel.exportNoteAsTxtFile(context, note) }
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
                                    onDeleteSwipe = { viewModel.moveNoteToTrash(note.id) },
                                    onMoveToFolderClick = { viewModel.openMoveNoteDialog(note) },
                                    onShareClick = { viewModel.shareNoteContent(context, note) },
                                    onExportTxtClick = { viewModel.exportNoteAsTxtFile(context, note) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
