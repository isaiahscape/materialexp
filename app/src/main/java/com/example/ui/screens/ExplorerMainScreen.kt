package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ArchiveFormat
import com.example.data.model.ArchiveOptions
import com.example.data.model.CompressionLevel
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.BreadcrumbBar
import com.example.ui.components.ClipboardBanner
import com.example.ui.components.FileDetailsDialog
import com.example.ui.components.FileGridItem
import com.example.ui.components.FileListItem
import com.example.ui.components.ImageViewerDialog
import com.example.ui.components.InstallPermissionDialog
import com.example.ui.components.PermissionDialog
import com.example.ui.components.SearchAndFilterHeader
import com.example.ui.components.TabBar
import com.example.ui.components.TextEditorSheet
import com.example.ui.components.ZipViewerDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.viewmodel.ExplorerViewModel
import com.example.ui.viewmodel.NavigationScreen
import com.example.ui.viewmodel.SortMode
import com.example.ui.viewmodel.ViewMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerMainScreen(
    viewModel: ExplorerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    var isNavBarVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -15f && isNavBarVisible) {
                    isNavBarVisible = false
                } else if (available.y > 15f && !isNavBarVisible) {
                    isNavBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Dialog state controllers
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderNameInput by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf<FileItem?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var showZipDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var openWithFile by remember { mutableStateOf<FileItem?>(null) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showInstallPermissionDialog by remember { mutableStateOf(false) }

    BackHandler(
        enabled = drawerState.isOpen || isSearchVisible || showAddMenu || state.currentScreen != NavigationScreen.EXPLORER || viewModel.canNavigateBack()
    ) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            showAddMenu -> showAddMenu = false
            isSearchVisible -> {
                isSearchVisible = false
                viewModel.setSearchQuery("")
            }
            state.currentScreen != NavigationScreen.EXPLORER -> viewModel.switchScreen(NavigationScreen.EXPLORER)
            else -> viewModel.navigateBack()
        }
    }

    LaunchedEffect(state.userNotice) {
        state.userNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    val activeTab = state.tabs.getOrNull(state.activeTabIndex)
    val currentPath = activeTab?.currentPath ?: ""
    val isBookmarked = state.bookmarks.any { it.path == currentPath }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Material Explorer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") },
                        selected = state.currentScreen == NavigationScreen.HOME,
                        onClick = {
                            viewModel.switchScreen(NavigationScreen.HOME)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.testTag("drawer_item_home")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        label = { Text("Explorer") },
                        selected = state.currentScreen == NavigationScreen.EXPLORER && activeTab?.currentPath != "/",
                        onClick = {
                            viewModel.switchScreen(NavigationScreen.EXPLORER)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.testTag("drawer_item_explorer")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        label = { Text("Root System (/)") },
                        selected = state.currentScreen == NavigationScreen.EXPLORER && activeTab?.currentPath == "/",
                        onClick = {
                            viewModel.switchScreen(NavigationScreen.EXPLORER)
                            viewModel.navigateToRoot()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.testTag("drawer_item_root")
                    )

                    val externalDrives = state.storageDrives.filter { it.iconType == "SD_CARD" || it.iconType == "USB_OTG" }
                    if (externalDrives.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "EXTERNAL STORAGE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        externalDrives.forEach { drive ->
                            val isDriveActive = state.currentScreen == NavigationScreen.EXPLORER && activeTab?.currentPath == drive.path
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = if (drive.iconType == "USB_OTG") Icons.Default.Usb else Icons.Default.SdCard,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(drive.label) },
                                selected = isDriveActive,
                                onClick = {
                                    viewModel.switchScreen(NavigationScreen.EXPLORER)
                                    viewModel.navigateTo(drive.path)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.testTag("drawer_item_${drive.iconType.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.PieChart, contentDescription = null) },
                        label = { Text("Storage Analyzer") },
                        selected = state.currentScreen == NavigationScreen.STORAGE_ANALYZER,
                        onClick = {
                            viewModel.switchScreen(NavigationScreen.STORAGE_ANALYZER)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.testTag("drawer_item_analyzer")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                        label = { Text("Pinned Bookmarks") },
                        selected = state.currentScreen == NavigationScreen.BOOKMARKS,
                        onClick = {
                            viewModel.switchScreen(NavigationScreen.BOOKMARKS)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.testTag("drawer_item_bookmarks")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Recycling, contentDescription = null) },
                        label = { Text("Recycle Bin") },
                        selected = state.currentScreen == NavigationScreen.TRASH_BIN,
                        onClick = {
                            viewModel.switchScreen(NavigationScreen.TRASH_BIN)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.testTag("drawer_item_trash")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                        selected = state.currentScreen == NavigationScreen.SETTINGS,
                        onClick = {
                            viewModel.switchScreen(NavigationScreen.SETTINGS)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.testTag("drawer_item_settings")
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (state.currentScreen) {
                                NavigationScreen.HOME -> "Material Explorer"
                                NavigationScreen.EXPLORER -> {
                                    if (state.categoryFilter != FileCategory.ALL && state.categoryFilter != FileCategory.FOLDER) {
                                        "Searching: ${state.categoryFilter.name.lowercase().replaceFirstChar { it.uppercase() }}"
                                    } else {
                                        "Material Explorer"
                                    }
                                }
                                NavigationScreen.STORAGE_ANALYZER -> "Storage Analyzer"
                                NavigationScreen.BOOKMARKS -> "Bookmarks"
                                NavigationScreen.TRASH_BIN -> "Recycle Bin"
                                NavigationScreen.SETTINGS -> "Settings"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("btn_open_drawer")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Navigation Drawer")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Surface(
                        modifier = Modifier.padding(16.dp).padding(bottom = 64.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        tonalElevation = 6.dp
                    ) {
                        Text(
                            text = data.visuals.message,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = state.currentScreen,
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.95f)).togetherWith(fadeOut() + scaleOut(targetScale = 0.95f))
                    },
                    label = "screenTransition"
                ) { targetScreen ->
                    when (targetScreen) {
                        NavigationScreen.HOME -> {
                            HomeScreen(
                                storageStats = state.storageStats,
                                storageDrives = state.storageDrives,
                                onCategoryClick = { category ->
                                    viewModel.setCategoryFilter(category)
                                    viewModel.switchScreen(NavigationScreen.EXPLORER)
                                },
                                onDriveClick = { path ->
                                    viewModel.navigateTo(path)
                                    viewModel.switchScreen(NavigationScreen.EXPLORER)
                                },
                                onStorageAnalyzerClick = {
                                    viewModel.switchScreen(NavigationScreen.STORAGE_ANALYZER)
                                }
                            )
                        }

                        NavigationScreen.EXPLORER -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                TabBar(
                                    tabs = state.tabs,
                                    activeTabIndex = state.activeTabIndex,
                                    onSelectTab = { viewModel.selectTab(it) },
                                    onCloseTab = { viewModel.closeTab(it) },
                                    onNewTab = { viewModel.addNewTab() }
                                )

                                BreadcrumbBar(
                                    currentPath = currentPath,
                                    rootStoragePath = viewModel.getApplicationsRootPath(),
                                    isBookmarked = isBookmarked,
                                    isDualPane = state.isDualPaneEnabled,
                                    onNavigateBack = { viewModel.navigateBack() },
                                    onNavigateToSegment = { viewModel.navigateTo(it) },
                                    onToggleBookmark = { viewModel.toggleBookmark(currentPath, activeTab?.title ?: "Folder") },
                                    onToggleDualPane = { viewModel.toggleDualPane() },
                                    onOpenSearch = { isSearchVisible = !isSearchVisible }
                                )

                                SearchAndFilterHeader(
                                    searchQuery = state.searchQuery,
                                    activeCategory = state.categoryFilter,
                                    isSearchVisible = isSearchVisible,
                                    onQueryChange = { viewModel.setSearchQuery(it) },
                                    onCategoryChange = { viewModel.setCategoryFilter(it) },
                                    onCloseSearch = { isSearchVisible = false; viewModel.setSearchQuery("") }
                                )

                                if (state.isDualPaneEnabled) {
                                    Row(modifier = Modifier.weight(1f)) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            DirectoryContentView(
                                                files = state.files,
                                                selectedPaths = state.selectedFilePaths,
                                                viewMode = state.viewMode,
                                                openWithPromptOnTap = state.openWithPromptOnTap,
                                                viewModel = viewModel,
                                                onOpenWithFile = { openWithFile = it },
                                                onRequireInstallPermission = { showInstallPermissionDialog = true }
                                            )
                                        }
                                        Spacer(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .fillMaxHeight()
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                        Box(modifier = Modifier.weight(1f)) {
                                            DirectoryContentView(
                                                files = state.secondPaneFiles,
                                                selectedPaths = emptySet(),
                                                viewMode = state.viewMode,
                                                openWithPromptOnTap = state.openWithPromptOnTap,
                                                viewModel = viewModel,
                                                onOpenWithFile = { openWithFile = it },
                                                onRequireInstallPermission = { showInstallPermissionDialog = true }
                                            )
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f)) {
                                        DirectoryContentView(
                                            files = state.files,
                                            selectedPaths = state.selectedFilePaths,
                                            viewMode = state.viewMode,
                                            openWithPromptOnTap = state.openWithPromptOnTap,
                                            viewModel = viewModel,
                                            onOpenWithFile = { openWithFile = it },
                                            onRequireInstallPermission = { showInstallPermissionDialog = true }
                                        )
                                    }
                                }
                            }
                        }

                        NavigationScreen.STORAGE_ANALYZER -> {
                            StorageAnalyzerScreen(
                                stats = state.storageStats,
                                onCategoryClick = { catName ->
                                    viewModel.switchScreen(NavigationScreen.EXPLORER)
                                    runCatching { viewModel.setCategoryFilter(FileCategory.valueOf(catName)) }
                                }
                            )
                        }

                        NavigationScreen.BOOKMARKS -> {
                            BookmarksScreen(
                                bookmarks = state.bookmarks,
                                onOpenBookmarkPath = { path ->
                                    viewModel.switchScreen(NavigationScreen.EXPLORER)
                                    viewModel.navigateTo(path)
                                },
                                onRemoveBookmark = { path ->
                                    viewModel.toggleBookmark(path, "")
                                }
                            )
                        }

                        NavigationScreen.TRASH_BIN -> {
                            TrashBinScreen(
                                trashItems = state.trashItems,
                                onRestore = { viewModel.restoreTrashItem(it) },
                                onDeletePermanent = { viewModel.permanentlyDeleteTrashItem(it) },
                                onEmptyTrash = { viewModel.emptyTrashBin() }
                            )
                        }

                        NavigationScreen.SETTINGS -> {
                            SettingsScreen(
                                showHiddenFiles = state.showHiddenFiles,
                                isDualPaneEnabled = state.isDualPaneEnabled,
                                openWithPromptOnTap = state.openWithPromptOnTap,
                                themeMode = state.themeMode,
                                viewMode = state.viewMode,
                                rootStatus = state.rootStatus,
                                onToggleShowHidden = { viewModel.toggleShowHiddenFiles() },
                                onToggleDualPane = { viewModel.toggleDualPane() },
                                onToggleOpenWithPrompt = { viewModel.toggleOpenWithPromptOnTap() },
                                onSetThemeMode = { viewModel.setThemeMode(it) },
                                onSelectViewMode = { viewModel.setViewMode(it) },
                                onTestRoot = { viewModel.runRootTestCommand() }
                            )
                        }
                    }
                }

                // Clipboard Overlay
                ClipboardBanner(
                    clipboardCount = state.clipboardPaths.size,
                    isCopy = state.isCopyOperation,
                    onPaste = { viewModel.pasteClipboard() },
                    onClear = { viewModel.clearClipboard() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // Speed Dial Backdrop
                AnimatedVisibility(
                    visible = showAddMenu,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showAddMenu = false }
                    )
                }

                // BOTTOM FLOATING AREA
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Audio Player (Small, only if triggered)
                    AudioPlayerBar(
                        audioFile = state.activeAudioFile,
                        isPlaying = state.isAudioPlaying,
                        progress = state.audioProgress,
                        onTogglePlay = { viewModel.toggleAudioPlayback() },
                        onClose = { viewModel.closeAudioPlayer() }
                    )

                    // Navigation & Selection Pill
                    AnimatedVisibility(
                        visible = isNavBarVisible,
                        enter = slideInVertically(initialOffsetY = { it * 2 }),
                        exit = slideOutVertically(targetOffsetY = { it * 2 })
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Selection Action Pill
                            AnimatedVisibility(
                                visible = state.selectedFilePaths.isNotEmpty(),
                                enter = scaleIn() + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shadowElevation = 8.dp,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { viewModel.copySelectedToClipboard(isCopy = true) }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                        }
                                        IconButton(onClick = { viewModel.copySelectedToClipboard(isCopy = false) }) {
                                            Icon(Icons.Default.ContentCut, contentDescription = "Cut")
                                        }
                                        IconButton(onClick = { showZipDialog = true }) {
                                            Icon(Icons.Default.FolderZip, contentDescription = "Zip")
                                        }
                                        IconButton(onClick = { viewModel.deleteSelectedItems() }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Trash", tint = MaterialTheme.colorScheme.error)
                                        }
                                        IconButton(onClick = { viewModel.deleteSelectedPermanently() }) {
                                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete Forever", tint = MaterialTheme.colorScheme.error)
                                        }
                                        IconButton(onClick = { viewModel.clearSelection() }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                }
                            }

                            // Speed Dial Options
                            AnimatedVisibility(
                                visible = showAddMenu && state.currentScreen == NavigationScreen.EXPLORER,
                                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn() + scaleIn(initialScale = 0.85f),
                                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut() + scaleOut(targetScale = 0.85f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
                                ) {
                                    SpeedDialItem("New Folder", Icons.Default.CreateNewFolder, MaterialTheme.colorScheme.primaryContainer) {
                                        showAddMenu = false
                                        showNewFolderDialog = true
                                    }
                                    SpeedDialItem("New File", Icons.Default.NoteAdd, MaterialTheme.colorScheme.secondaryContainer) {
                                        showAddMenu = false
                                        showNewFileDialog = true
                                    }
                                    SpeedDialItem("Layout", Icons.Default.GridView, MaterialTheme.colorScheme.tertiaryContainer) {
                                        val nextMode = when (state.viewMode) {
                                            ViewMode.DETAILED_LIST -> ViewMode.GRID_2
                                            ViewMode.GRID_2 -> ViewMode.GRID_3
                                            ViewMode.GRID_3 -> ViewMode.COMPACT_LIST
                                            ViewMode.COMPACT_LIST -> ViewMode.DETAILED_LIST
                                        }
                                        viewModel.setViewMode(nextMode)
                                    }
                                    SpeedDialItem("Sort By", Icons.Default.FilterList, MaterialTheme.colorScheme.surfaceVariant) {
                                        showAddMenu = false
                                        showSortDialog = true
                                    }
                                    SpeedDialItem(if (state.showHiddenFiles) "Hide Hidden" else "Show Hidden", if (state.showHiddenFiles) Icons.Default.Visibility else Icons.Default.VisibilityOff, MaterialTheme.colorScheme.surfaceVariant) {
                                        viewModel.toggleShowHiddenFiles()
                                    }
                                }
                            }

                            // Main Navigation Bar (Capsule)
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 8.dp,
                                tonalElevation = 6.dp,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NavIconButton(Icons.Default.Home, "Home", state.currentScreen == NavigationScreen.HOME) {
                                        viewModel.switchScreen(NavigationScreen.HOME)
                                    }
                                    NavIconButton(Icons.Default.Folder, "Explorer", state.currentScreen == NavigationScreen.EXPLORER) {
                                        viewModel.switchScreen(NavigationScreen.EXPLORER)
                                    }
                                    NavIconButton(Icons.Default.PieChart, "Storage", state.currentScreen == NavigationScreen.STORAGE_ANALYZER) {
                                        viewModel.switchScreen(NavigationScreen.STORAGE_ANALYZER)
                                    }
                                    NavIconButton(Icons.Default.Bookmark, "Bookmarks", state.currentScreen == NavigationScreen.BOOKMARKS) {
                                        viewModel.switchScreen(NavigationScreen.BOOKMARKS)
                                    }
                                    NavIconButton(Icons.Default.Recycling, "Recycle Bin", state.currentScreen == NavigationScreen.TRASH_BIN) {
                                        viewModel.switchScreen(NavigationScreen.TRASH_BIN)
                                    }

                                    if (state.currentScreen == NavigationScreen.EXPLORER) {
                                        VerticalDivider()
                                        AddButton(showAddMenu) { showAddMenu = !showAddMenu }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs & Sheets ---
    state.activeEditorFile?.let { editorFile ->
        TextEditorSheet(
            file = editorFile,
            content = state.activeEditorContent,
            isModified = state.isEditorModified,
            onContentChange = { viewModel.updateEditorContent(it) },
            onSave = { viewModel.saveEditorChanges() },
            onClose = { viewModel.closeTextEditor() }
        )
    }

    state.activeImageViewerFile?.let { imgFile ->
        ImageViewerDialog(file = imgFile, onDismiss = { viewModel.closeImageViewer() })
    }

    state.activeZipFile?.let { zipFile ->
        ZipViewerDialog(file = zipFile, entries = state.zipEntries, onExtract = { viewModel.extractZipArchive(zipFile) }, onDismiss = { viewModel.closeZipViewer() })
    }

    state.inspectedFile?.let { inspectedFile ->
        FileDetailsDialog(file = inspectedFile, md5 = state.inspectedMd5, sha256 = state.inspectedSha256, onDismiss = { viewModel.closeFileDetails() })
    }

    if (showNewFolderDialog) {
        GenericInputDialog("Create New Folder", "Folder Name", newFolderNameInput, { newFolderNameInput = it }, { viewModel.createNewFolder(newFolderNameInput); newFolderNameInput = ""; showNewFolderDialog = false }, { showNewFolderDialog = false })
    }

    if (showNewFileDialog) {
        GenericInputDialog("Create New File", "File Name", newFileNameInput, { newFileNameInput = it }, { viewModel.createNewFile(newFileNameInput); newFileNameInput = ""; showNewFileDialog = false }, { showNewFileDialog = false })
    }

    showRenameDialog?.let { fileToRename ->
        GenericInputDialog("Rename File", "New Name", renameInput, { renameInput = it }, { viewModel.renameItem(fileToRename, renameInput); showRenameDialog = null }, { showRenameDialog = null })
    }

    if (showZipDialog) {
        ArchiveDialog(onDismiss = { showZipDialog = false }, onConfirm = { name, options -> viewModel.compressSelectedToArchive(name, options); showZipDialog = false })
    }

    if (showSortDialog) {
        SortDialog(state.sortMode, onSort = { viewModel.setSortMode(it); showSortDialog = false }, onDismiss = { showSortDialog = false })
    }

    openWithFile?.let { file ->
        OpenWithDialog(file = file, onDismiss = { openWithFile = null }, viewModel = viewModel, onRequireInstallPermission = { showInstallPermissionDialog = true })
    }

    if (!state.isStoragePermissionGranted) {
        PermissionDialog(onDismiss = { })
    }

    if (showInstallPermissionDialog) {
        InstallPermissionDialog(onDismiss = { showInstallPermissionDialog = false })
    }
}

// --- Composable Sub-components ---

@Composable
private fun SpeedDialItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun NavIconButton(icon: ImageVector, contentDescription: String, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.padding(horizontal = 4.dp).width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
}

@Composable
private fun AddButton(isOpen: Boolean, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (isOpen) 135f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "plusRotate"
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isOpen) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = if (isOpen) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(22.dp).graphicsLayer(rotationZ = rotation)
        )
    }
}

@Composable
private fun GenericInputDialog(title: String, label: String, value: String, onValueChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, singleLine = true) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ArchiveDialog(onDismiss: () -> Unit, onConfirm: (String, ArchiveOptions) -> Unit) {
    var archiveName by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf(ArchiveFormat.ZIP) }
    var selectedLevel by remember { mutableStateOf(CompressionLevel.NORMAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compress Items", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = archiveName, onValueChange = { archiveName = it }, label = { Text("Archive Name") }, suffix = { Text(selectedFormat.extension) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Select Format:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ArchiveFormat.entries.forEach { format ->
                        Surface(
                            onClick = { selectedFormat = format },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedFormat == format) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (selectedFormat == format) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Text(text = format.label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Text("Compression Level:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompressionLevel.entries.forEach { level ->
                        Surface(
                            onClick = { selectedLevel = level },
                            shape = CircleShape,
                            color = if (selectedLevel == level) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f),
                            border = if (selectedLevel == level) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary) else null
                        ) {
                            Text(text = level.label, modifier = Modifier.padding(vertical = 6.dp), style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (archiveName.isNotBlank()) onConfirm(archiveName, ArchiveOptions(selectedFormat, selectedLevel)) }) { Text("Compress") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SortDialog(currentMode: SortMode, onSort: (SortMode) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Files By", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val sortOptions = listOf("Name (A-Z)" to SortMode.NAME_ASC, "Name (Z-A)" to SortMode.NAME_DESC, "Size (Smallest First)" to SortMode.SIZE_ASC, "Size (Largest First)" to SortMode.SIZE_DESC, "Date (Newest First)" to SortMode.DATE_DESC, "File Type" to SortMode.TYPE)
                sortOptions.forEach { (label, mode) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onSort(mode) }.padding(vertical = 6.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentMode == mode, onClick = { onSort(mode) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun DirectoryContentView(
    files: List<FileItem>,
    selectedPaths: Set<String>,
    viewMode: ViewMode,
    openWithPromptOnTap: Boolean,
    viewModel: ExplorerViewModel,
    onOpenWithFile: (FileItem) -> Unit,
    onRequireInstallPermission: () -> Unit
) {
    val context = LocalContext.current
    if (files.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Directory is empty", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    when (viewMode) {
        ViewMode.DETAILED_LIST, ViewMode.COMPACT_LIST -> {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(files, key = { it.path }) { item ->
                    FileListItem(
                        file = item,
                        isSelected = selectedPaths.contains(item.path),
                        onItemClick = { if (selectedPaths.isNotEmpty()) viewModel.toggleFileSelection(item.path) else if (item.isDirectory) viewModel.navigateTo(item.path) else if (openWithPromptOnTap) onOpenWithFile(item) else handleFileOpen(context, item, viewModel, onRequireInstallPermission) },
                        onItemLongClick = { viewModel.toggleFileSelection(item.path) },
                        onInspectDetails = { viewModel.inspectFileDetails(item) },
                        onRename = { viewModel.inspectFileDetails(item) },
                        onDelete = { viewModel.deleteItemToTrash(item) },
                        onDeletePermanently = { viewModel.deletePermanently(item) },
                        onZip = { viewModel.toggleFileSelection(item.path) }
                    )
                }
            }
        }

        ViewMode.GRID_2, ViewMode.GRID_3 -> {
            val cols = if (viewMode == ViewMode.GRID_2) 2 else 3
            LazyVerticalGrid(columns = GridCells.Fixed(cols), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(files, key = { it.path }) { item ->
                    FileGridItem(
                        file = item,
                        isSelected = selectedPaths.contains(item.path),
                        onItemClick = { if (selectedPaths.isNotEmpty()) viewModel.toggleFileSelection(item.path) else if (item.isDirectory) viewModel.navigateTo(item.path) else if (openWithPromptOnTap) onOpenWithFile(item) else handleFileOpen(context, item, viewModel, onRequireInstallPermission) },
                        onItemLongClick = { viewModel.toggleFileSelection(item.path) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenWithDialog(file: FileItem, onDismiss: () -> Unit, viewModel: ExplorerViewModel, onRequireInstallPermission: () -> Unit) {
    val context = LocalContext.current
    val mimeType = remember(file) { getMimeType(file) }
    val externalApps = remember(file, mimeType) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val javaFile = java.io.File(file.path)
            val uri = try { androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", javaFile) } catch (e: Exception) { Uri.fromFile(javaFile) }
            setDataAndType(uri, mimeType)
        }
        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).filter { it.activityInfo.packageName != context.packageName }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Column { Text(text = "Open With...", fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(2.dp)); Text(text = file.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (file.category == FileCategory.CODE || file.category == FileCategory.DOCUMENT) OpenWithOptionRow(Icons.Default.EditNote, "Code / Text Editor", "Open in built-in editor") { onDismiss(); viewModel.openTextEditor(file) }
                if (file.category == FileCategory.IMAGE) OpenWithOptionRow(Icons.Default.Image, "Image Viewer", "View in built-in image viewer") { onDismiss(); viewModel.openImageViewer(file) }
                if (file.category == FileCategory.AUDIO) OpenWithOptionRow(Icons.Default.MusicNote, "Audio Player", "Play in built-in audio player") { onDismiss(); viewModel.openAudioPlayer(file) }
                if (file.category == FileCategory.ARCHIVE) OpenWithOptionRow(Icons.Default.FolderZip, "Zip / Archive Inspector", "Inspect zip entries & extract") { onDismiss(); viewModel.inspectZipArchive(file) }
                OpenWithOptionRow(Icons.Default.Info, "File Details & Hashes", "View size, path, MD5 & SHA-256") { onDismiss(); viewModel.inspectFileDetails(file) }
                if (externalApps.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(text = "External Applications", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
                    externalApps.forEach { resolveInfo ->
                        Surface(onClick = { onDismiss(); launchExternalApp(context, file, resolveInfo, onRequireInstallPermission) }, shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AsyncImage(model = resolveInfo.loadIcon(context.packageManager), contentDescription = null, modifier = Modifier.size(32.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = resolveInfo.loadLabel(context.packageManager).toString(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = resolveInfo.activityInfo.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                OpenWithOptionRow(Icons.Default.OpenInNew, "System Default / Chooser", "Select from all compatible apps") { onDismiss(); openWithSystemDefault(context, file, viewModel, onRequireInstallPermission) }
                OpenWithOptionRow(Icons.Default.FilterList, "Open as any type (*/*)", "Force system to show all handlers") { onDismiss(); openWithSystemDefault(context, file, viewModel, onRequireInstallPermission, mimeOverride = "*/*") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun OpenWithOptionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun openWithSystemDefault(context: Context, file: FileItem, viewModel: ExplorerViewModel, onRequireInstallPermission: () -> Unit, mimeOverride: String? = null) {
    if (file.extension.lowercase() == "apk") {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) { onRequireInstallPermission(); return }
        }
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val javaFile = java.io.File(file.path)
            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", javaFile)
            val mime = mimeOverride ?: getMimeType(file)
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: Exception) {
        viewModel.showNotice("No external app available for ${file.name}")
    }
}

private fun launchExternalApp(context: Context, file: FileItem, resolveInfo: ResolveInfo, onRequireInstallPermission: () -> Unit) {
    if (file.extension.lowercase() == "apk") {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) { onRequireInstallPermission(); return }
        }
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val javaFile = java.io.File(file.path)
            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", javaFile)
            setDataAndType(uri, getMimeType(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to launch ${resolveInfo.loadLabel(context.packageManager)}", Toast.LENGTH_SHORT).show()
    }
}

private fun getMimeType(file: FileItem): String {
    return when (file.extension.lowercase()) {
        "apk" -> "application/vnd.android.package-archive"
        "dng" -> "image/x-adobe-dng"
        else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"
    }
}

private fun handleFileOpen(context: Context, item: FileItem, viewModel: ExplorerViewModel, onRequireInstallPermission: () -> Unit) {
    when (item.category) {
        FileCategory.APK -> openWithSystemDefault(context, item, viewModel, onRequireInstallPermission)
        FileCategory.CODE, FileCategory.DOCUMENT -> viewModel.openTextEditor(item)
        FileCategory.IMAGE -> viewModel.openImageViewer(item)
        FileCategory.AUDIO -> viewModel.openAudioPlayer(item)
        FileCategory.ARCHIVE -> viewModel.inspectZipArchive(item)
        else -> openWithSystemDefault(context, item, viewModel, onRequireInstallPermission)
    }
}

fun ExplorerViewModel.getApplicationsRootPath(): String {
    return uiState.value.tabs.firstOrNull()?.currentPath?.substringBefore("/Documents")
        ?: uiState.value.tabs.firstOrNull()?.currentPath ?: ""
}
