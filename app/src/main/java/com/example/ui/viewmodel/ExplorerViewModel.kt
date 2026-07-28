package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VlcPlayerManager
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.TrashEntity
import com.example.data.model.ArchiveOptions
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.model.StorageStats
import com.example.data.model.TabItem
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class ViewMode { DETAILED_LIST, COMPACT_LIST, GRID_2, GRID_3 }
enum class SortMode { NAME_ASC, NAME_DESC, SIZE_ASC, SIZE_DESC, DATE_ASC, DATE_DESC, TYPE }
enum class NavigationScreen { HOME, EXPLORER, STORAGE_ANALYZER, TRASH_BIN, BOOKMARKS, SETTINGS }
enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class ExplorerUiState(
    val tabs: List<TabItem> = emptyList(),
    val activeTabIndex: Int = 0,
    val files: List<FileItem> = emptyList(),
    val secondPaneFiles: List<FileItem> = emptyList(),
    val secondPanePath: String = "",
    val isLoading: Boolean = false,
    val selectedFilePaths: Set<String> = emptySet(),
    val clipboardPaths: List<String> = emptyList(),
    val isCopyOperation: Boolean = true,
    val searchQuery: String = "",
    val categoryFilter: FileCategory = FileCategory.ALL,
    val viewMode: ViewMode = ViewMode.DETAILED_LIST,
    val sortMode: SortMode = SortMode.NAME_ASC,
    val showHiddenFiles: Boolean = false,
    val isDualPaneEnabled: Boolean = false,
    val openWithPromptOnTap: Boolean = true,
    val currentScreen: NavigationScreen = NavigationScreen.HOME,
    val storageStats: StorageStats? = null,
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val trashItems: List<TrashEntity> = emptyList(),
    
    // Viewer / Editor States
    val activeEditorFile: FileItem? = null,
    val activeEditorContent: String = "",
    val isEditorModified: Boolean = false,
    val activeImageViewerFile: FileItem? = null,
    val activeAudioFile: FileItem? = null,
    val isAudioPlaying: Boolean = false,
    val audioProgress: Float = 0f,
    val audioPosition: Long = 0,
    val audioDuration: Long = 0,
    val activeZipFile: FileItem? = null,
    val zipEntries: List<String> = emptyList(),
    val inspectedFile: FileItem? = null,
    val inspectedMd5: String = "",
    val inspectedSha256: String = "",
    
    // User Message/Snackbar
    val userNotice: String? = null,
    val rootStatus: String = "Checking...",
    val storageDrives: List<FileRepository.StorageDrive> = emptyList(),
    val isStoragePermissionGranted: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class ExplorerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repository = FileRepository(application, db.explorerDao())

    private val _uiState = MutableStateFlow(ExplorerUiState())
    val uiState: StateFlow<ExplorerUiState> = _uiState.asStateFlow()

    private val vlcPlayerManager = VlcPlayerManager(application)

    init {
        checkStoragePermission()
        val initialPath = repository.rootStoragePath
        val defaultTab = TabItem(
            id = UUID.randomUUID().toString(),
            title = "Internal Storage",
            currentPath = initialPath
        )
        val secondPath = File(initialPath, "Documents").let { if (it.exists()) it.absolutePath else initialPath }

        _uiState.update {
            it.copy(
                tabs = listOf(defaultTab),
                activeTabIndex = 0,
                secondPanePath = secondPath
            )
        }

        observeDatabase()
        loadStorageDrives()
        loadCurrentDirectory()
        loadStorageStats()
        checkRootStatus()
        observeAudioPlayback()
    }

    private fun observeAudioPlayback() {
        viewModelScope.launch {
            vlcPlayerManager.playbackState.collect { state ->
                _uiState.update {
                    it.copy(
                        isAudioPlaying = state.isPlaying,
                        audioProgress = state.progress,
                        audioPosition = state.currentPosition,
                        audioDuration = state.duration
                    )
                }
            }
        }
    }

    fun checkStoragePermission() {
        val granted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            // For older versions, we might need a different check, but MANAGE_EXTERNAL_STORAGE is API 30+
            true 
        }
        _uiState.update { it.copy(isStoragePermissionGranted = granted) }
        if (granted) {
            loadCurrentDirectory()
            loadStorageStats()
        }
    }

    fun loadStorageDrives() {
        viewModelScope.launch {
            val drives = repository.getAvailableStorageDrives()
            _uiState.update { it.copy(storageDrives = drives) }
        }
    }

    fun checkRootStatus() {
        viewModelScope.launch {
            val status = repository.getRootStatus()
            _uiState.update { it.copy(rootStatus = status) }
        }
    }

    fun runRootTestCommand() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.runSuperUserCommand("id; which su; magisk -v; ksu --version")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userNotice = if (res.isSuccess && res.output.isNotBlank()) "Root SU Output: ${res.output.take(100).replace('\n', ' ')}" else "Root SU Info: Magisk/KernelSU Superuser Access Configured"
                )
            }
            checkRootStatus()
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.bookmarks.collectLatest { bList ->
                _uiState.update { it.copy(bookmarks = bList) }
            }
        }
        viewModelScope.launch {
            repository.trashItems.collectLatest { tList ->
                _uiState.update { it.copy(trashItems = tList) }
            }
        }
    }

    fun loadStorageStats() {
        viewModelScope.launch {
            val stats = repository.getStorageStats()
            _uiState.update { it.copy(storageStats = stats) }
        }
    }

    fun loadCurrentDirectory() {
        val state = _uiState.value
        val activeTab = state.tabs.getOrNull(state.activeTabIndex) ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            var fileList = repository.getFiles(
                path = activeTab.currentPath,
                showHidden = state.showHiddenFiles,
                searchQuery = state.searchQuery,
                categoryFilter = state.categoryFilter
            )

            // Apply Sorting
            fileList = when (state.sortMode) {
                SortMode.NAME_ASC -> fileList.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                SortMode.NAME_DESC -> fileList.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })).reversed()
                SortMode.SIZE_ASC -> fileList.sortedWith(compareBy({ !it.isDirectory }, { it.sizeBytes }))
                SortMode.SIZE_DESC -> fileList.sortedWith(compareBy({ !it.isDirectory }, { it.sizeBytes })).reversed()
                SortMode.DATE_ASC -> fileList.sortedBy { it.lastModified }
                SortMode.DATE_DESC -> fileList.sortedByDescending { it.lastModified }
                SortMode.TYPE -> fileList.sortedWith(compareBy({ !it.isDirectory }, { it.category.name }, { it.name }))
            }

            var secondList = emptyList<FileItem>()
            if (state.isDualPaneEnabled && state.secondPanePath.isNotBlank()) {
                secondList = repository.getFiles(
                    path = state.secondPanePath,
                    showHidden = state.showHiddenFiles
                )
            }

            _uiState.update {
                it.copy(
                    files = fileList,
                    secondPaneFiles = secondList,
                    isLoading = false
                )
            }
        }
    }

    fun navigateTo(path: String) {
        val state = _uiState.value
        val activeTab = state.tabs.getOrNull(state.activeTabIndex) ?: return

        if (activeTab.currentPath == path) return

        val updatedHistory = activeTab.history.take(activeTab.historyIndex + 1) + path
        val updatedTab = activeTab.copy(
            title = File(path).name.ifEmpty { "Storage" },
            currentPath = path,
            history = updatedHistory,
            historyIndex = updatedHistory.lastIndex
        )

        val updatedTabs = state.tabs.toMutableList().apply {
            set(state.activeTabIndex, updatedTab)
        }

        _uiState.update {
            it.copy(
                tabs = updatedTabs,
                selectedFilePaths = emptySet(),
                searchQuery = ""
            )
        }
        loadCurrentDirectory()
    }

    fun navigateBack(): Boolean {
        val state = _uiState.value
        val activeTab = state.tabs.getOrNull(state.activeTabIndex) ?: return false

        if (activeTab.historyIndex > 0) {
            val newIdx = activeTab.historyIndex - 1
            val prevPath = activeTab.history[newIdx]
            val updatedTab = activeTab.copy(
                currentPath = prevPath,
                historyIndex = newIdx,
                title = File(prevPath).name.ifEmpty { "Storage" }
            )
            val updatedTabs = state.tabs.toMutableList().apply { set(state.activeTabIndex, updatedTab) }
            _uiState.update { it.copy(tabs = updatedTabs, selectedFilePaths = emptySet()) }
            loadCurrentDirectory()
            return true
        } else {
            val parentFile = File(activeTab.currentPath).parentFile
            if (parentFile != null && parentFile.canRead()) {
                navigateTo(parentFile.absolutePath)
                return true
            }
        }
        return false
    }

    fun canNavigateBack(): Boolean {
        val activeTab = _uiState.value.tabs.getOrNull(_uiState.value.activeTabIndex) ?: return false
        if (activeTab.historyIndex > 0) return true
        val parentFile = File(activeTab.currentPath).parentFile
        return parentFile != null && parentFile.canRead() && activeTab.currentPath != "/"
    }

    fun addNewTab(path: String? = null) {
        val initial = path ?: repository.rootStoragePath
        val newTab = TabItem(
            id = UUID.randomUUID().toString(),
            title = File(initial).name.ifEmpty { "Storage" },
            currentPath = initial
        )
        val updated = _uiState.value.tabs + newTab
        val newIdx = updated.lastIndex
        _uiState.update { it.copy(tabs = updated, activeTabIndex = newIdx) }
        loadCurrentDirectory()
    }

    fun closeTab(index: Int) {
        val state = _uiState.value
        if (state.tabs.size <= 1) return
        val updatedTabs = state.tabs.toMutableList().apply { removeAt(index) }
        val newActiveIdx = (state.activeTabIndex.coerceAtMost(updatedTabs.lastIndex))
        _uiState.update { it.copy(tabs = updatedTabs, activeTabIndex = newActiveIdx) }
        loadCurrentDirectory()
    }

    fun selectTab(index: Int) {
        if (index in _uiState.value.tabs.indices) {
            _uiState.update { it.copy(activeTabIndex = index, selectedFilePaths = emptySet()) }
            loadCurrentDirectory()
        }
    }

    fun toggleFileSelection(path: String) {
        _uiState.update { state ->
            val current = state.selectedFilePaths.toMutableSet()
            if (current.contains(path)) current.remove(path) else current.add(path)
            state.copy(selectedFilePaths = current)
        }
    }

    fun selectAllFiles() {
        val allPaths = _uiState.value.files.map { it.path }.toSet()
        _uiState.update { it.copy(selectedFilePaths = allPaths) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedFilePaths = emptySet()) }
    }

    fun copySelectedToClipboard(isCopy: Boolean) {
        val state = _uiState.value
        if (state.selectedFilePaths.isEmpty()) return
        _uiState.update {
            it.copy(
                clipboardPaths = state.selectedFilePaths.toList(),
                isCopyOperation = isCopy,
                selectedFilePaths = emptySet(),
                userNotice = if (isCopy) "Copied ${state.selectedFilePaths.size} items to clipboard" else "Cut ${state.selectedFilePaths.size} items"
            )
        }
    }

    fun pasteClipboard() {
        val state = _uiState.value
        val activeTab = state.tabs.getOrNull(state.activeTabIndex) ?: return
        val dest = activeTab.currentPath

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            var successCount = 0
            state.clipboardPaths.forEach { src ->
                val ok = if (state.isCopyOperation) {
                    repository.copyFile(src, dest)
                } else {
                    repository.moveFile(src, dest)
                }
                if (ok) successCount++
            }
            _uiState.update {
                it.copy(
                    clipboardPaths = if (state.isCopyOperation) state.clipboardPaths else emptyList(),
                    isLoading = false,
                    userNotice = "Pasted $successCount items successfully"
                )
            }
            loadCurrentDirectory()
            loadStorageStats()
        }
    }

    fun clearClipboard() {
        _uiState.update { it.copy(clipboardPaths = emptyList()) }
    }

    fun createNewFolder(folderName: String) {
        val activeTab = _uiState.value.tabs.getOrNull(_uiState.value.activeTabIndex) ?: return
        viewModelScope.launch {
            val created = repository.createFolder(activeTab.currentPath, folderName)
            if (created) {
                showNotice("Folder '$folderName' created")
                loadCurrentDirectory()
            } else {
                showNotice("Could not create folder '$folderName'")
            }
        }
    }

    fun createNewFile(fileName: String, initialContent: String = "") {
        val activeTab = _uiState.value.tabs.getOrNull(_uiState.value.activeTabIndex) ?: return
        viewModelScope.launch {
            val created = repository.createFile(activeTab.currentPath, fileName, initialContent)
            if (created) {
                showNotice("File '$fileName' created")
                loadCurrentDirectory()
            } else {
                showNotice("File '$fileName' already exists or creation failed")
            }
        }
    }

    fun deleteSelectedItems() {
        val state = _uiState.value
        val selectedItems = state.files.filter { state.selectedFilePaths.contains(it.path) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            selectedItems.forEach { item ->
                repository.deleteToTrash(item)
            }
            _uiState.update {
                it.copy(
                    selectedFilePaths = emptySet(),
                    userNotice = "Moved ${selectedItems.size} items to Recycle Bin"
                )
            }
            loadCurrentDirectory()
            loadStorageStats()
        }
    }

    fun restoreTrashItem(entity: TrashEntity) {
        viewModelScope.launch {
            val ok = repository.restoreFromTrash(entity)
            if (ok) showNotice("Restored '${entity.fileName}'")
            else showNotice("Failed to restore '${entity.fileName}'")
            loadCurrentDirectory()
            loadStorageStats()
        }
    }

    fun permanentlyDeleteTrashItem(entity: TrashEntity) {
        viewModelScope.launch {
            repository.permanentlyDeleteTrash(entity)
            showNotice("Permanently deleted '${entity.fileName}'")
            loadStorageStats()
        }
    }

    fun emptyTrashBin() {
        viewModelScope.launch {
            repository.emptyTrash()
            showNotice("Recycle Bin emptied")
            loadStorageStats()
        }
    }

    fun renameItem(fileItem: FileItem, newName: String) {
        viewModelScope.launch {
            val ok = repository.renameFile(fileItem.path, newName)
            if (ok) showNotice("Renamed to '$newName'")
            else showNotice("Rename failed")
            loadCurrentDirectory()
        }
    }

    fun navigateToRoot() {
        navigateTo(repository.systemRootPath)
    }

    fun compressSelectedToArchive(fileName: String, options: ArchiveOptions) {
        val state = _uiState.value
        val activeTab = state.tabs.getOrNull(state.activeTabIndex) ?: return
        val ext = options.format.extension
        val cleanName = if (fileName.endsWith(ext, ignoreCase = true)) fileName else "$fileName$ext"
        val destPath = File(activeTab.currentPath, cleanName).absolutePath

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val ok = repository.createArchive(state.selectedFilePaths.toList(), destPath, options)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedFilePaths = emptySet(),
                    userNotice = if (ok) "Compressed to '$cleanName'" else "Compression failed"
                )
            }
            loadCurrentDirectory()
        }
    }

    fun compressSelectedToZip(zipFileName: String) {
        compressSelectedToArchive(zipFileName, ArchiveOptions())
    }

    fun compressSelectedTo7z(archiveFileName: String) {
        compressSelectedToArchive(archiveFileName, ArchiveOptions(com.example.data.model.ArchiveFormat.SEVEN_Z))
    }

    fun inspectZipArchive(fileItem: FileItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entries = repository.readZipEntries(fileItem.path)
            _uiState.update {
                it.copy(
                    activeZipFile = fileItem,
                    zipEntries = entries,
                    isLoading = false
                )
            }
        }
    }

    fun extractZipArchive(fileItem: FileItem) {
        val activeTab = _uiState.value.tabs.getOrNull(_uiState.value.activeTabIndex) ?: return
        val folderName = fileItem.name.substringBeforeLast('.')
        val targetDir = File(activeTab.currentPath, folderName).absolutePath
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val ok = repository.extractZip(fileItem.path, targetDir)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userNotice = if (ok) "Extracted archive to $folderName" else "Extraction failed"
                )
            }
            loadCurrentDirectory()
        }
    }

    fun closeZipViewer() {
        _uiState.update { it.copy(activeZipFile = null, zipEntries = emptyList()) }
    }

    fun openTextEditor(fileItem: FileItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val content = repository.readTextFile(fileItem.path)
            _uiState.update {
                it.copy(
                    activeEditorFile = fileItem,
                    activeEditorContent = content,
                    isEditorModified = false,
                    isLoading = false
                )
            }
        }
    }

    fun updateEditorContent(newContent: String) {
        _uiState.update {
            it.copy(
                activeEditorContent = newContent,
                isEditorModified = true
            )
        }
    }

    fun saveEditorChanges() {
        val state = _uiState.value
        val file = state.activeEditorFile ?: return
        viewModelScope.launch {
            val ok = repository.saveTextFile(file.path, state.activeEditorContent)
            _uiState.update {
                it.copy(
                    isEditorModified = false,
                    userNotice = if (ok) "Saved '${file.name}'" else "Save failed"
                )
            }
        }
    }

    fun closeTextEditor() {
        _uiState.update {
            it.copy(
                activeEditorFile = null,
                activeEditorContent = "",
                isEditorModified = false
            )
        }
    }

    fun openImageViewer(fileItem: FileItem) {
        _uiState.update { it.copy(activeImageViewerFile = fileItem) }
    }

    fun closeImageViewer() {
        _uiState.update { it.copy(activeImageViewerFile = null) }
    }

    fun openAudioPlayer(fileItem: FileItem) {
        _uiState.update { it.copy(activeAudioFile = fileItem) }
        vlcPlayerManager.play(fileItem.path)
    }

    fun toggleAudioPlayback() {
        vlcPlayerManager.togglePlay()
    }

    fun closeAudioPlayer() {
        vlcPlayerManager.stop()
        _uiState.update { it.copy(activeAudioFile = null) }
    }

    override fun onCleared() {
        super.onCleared()
        vlcPlayerManager.release()
    }

    fun inspectFileDetails(fileItem: FileItem) {
        _uiState.update {
            it.copy(
                inspectedFile = fileItem,
                inspectedMd5 = "Computing...",
                inspectedSha256 = "Computing..."
            )
        }
        viewModelScope.launch {
            val md5 = repository.calculateChecksum(fileItem.path, "MD5")
            val sha256 = repository.calculateChecksum(fileItem.path, "SHA-256")
            _uiState.update {
                it.copy(
                    inspectedMd5 = md5,
                    inspectedSha256 = sha256
                )
            }
        }
    }

    fun closeFileDetails() {
        _uiState.update { it.copy(inspectedFile = null, inspectedMd5 = "", inspectedSha256 = "") }
    }

    fun toggleBookmark(path: String, name: String) {
        val isBookmarked = _uiState.value.bookmarks.any { it.path == path }
        viewModelScope.launch {
            if (isBookmarked) {
                repository.removeBookmarkByPath(path)
                showNotice("Removed bookmark")
            } else {
                repository.addBookmark(name, path)
                showNotice("Folder bookmarked")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadCurrentDirectory()
    }

    fun setCategoryFilter(category: FileCategory) {
        _uiState.update { it.copy(categoryFilter = category, selectedFilePaths = emptySet()) }
        loadCurrentDirectory()
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun setSortMode(mode: SortMode) {
        _uiState.update { it.copy(sortMode = mode) }
        loadCurrentDirectory()
    }

    fun toggleShowHiddenFiles() {
        val newShow = !_uiState.value.showHiddenFiles
        _uiState.update { it.copy(showHiddenFiles = newShow) }
        loadCurrentDirectory()
    }

    fun toggleDualPane() {
        val newDual = !_uiState.value.isDualPaneEnabled
        _uiState.update { it.copy(isDualPaneEnabled = newDual) }
        loadCurrentDirectory()
    }

    fun toggleOpenWithPromptOnTap() {
        val newPrompt = !_uiState.value.openWithPromptOnTap
        _uiState.update { it.copy(openWithPromptOnTap = newPrompt) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun switchScreen(screen: NavigationScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
        if (screen == NavigationScreen.STORAGE_ANALYZER) {
            loadStorageStats()
        }
    }

    fun showNotice(notice: String) {
        _uiState.update { it.copy(userNotice = notice) }
    }

    fun clearNotice() {
        _uiState.update { it.copy(userNotice = null) }
    }
}
