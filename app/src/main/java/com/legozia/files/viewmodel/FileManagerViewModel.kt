package com.legozia.files.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legozia.files.data.ClipboardManager
import com.legozia.files.data.FilePreferences
import com.legozia.files.model.*
import com.legozia.files.repo.FileOperations
import com.legozia.files.repo.FileRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FileManagerUiState(
    val currentPath: String = "",
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val canNavigateBack: Boolean = false,
    val selectedFile: FileItem? = null,
    val isSelectionMode: Boolean = false,
    val selectedFiles: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<FileItem> = emptyList(),
    val viewMode: ViewMode = ViewMode.LIST,
    val sortType: SortType = SortType.NAME,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
    val showHiddenFiles: Boolean = false,
    val favorites: Set<String> = emptySet(),
    val operationProgress: FileOperationProgress? = null,
    val showOperationDialog: OperationDialogType? = null
)

sealed class OperationDialogType {
    data object CreateFolder : OperationDialogType()
    data class Rename(val fileItem: FileItem) : OperationDialogType()
    data class Delete(val files: List<FileItem>) : OperationDialogType()
    data class FileProperties(val fileItem: FileItem) : OperationDialogType()
}

@OptIn(FlowPreview::class)
class FileManagerViewModel(
    private val context: Context,
    private val repository: FileRepository = FileRepository(),
    private val fileOperations: FileOperations = FileOperations(),
    private val clipboardManager: ClipboardManager = ClipboardManager(),
    private val preferences: FilePreferences = FilePreferences(context)
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()
    
    private val navigationStack = mutableListOf<String>()
    
    private val _searchQueryFlow = MutableStateFlow("")
    
    init {
        // Observe preferences
        viewModelScope.launch {
            preferences.viewMode.collect { mode ->
                _uiState.update { it.copy(viewMode = mode) }
            }
        }
        
        viewModelScope.launch {
            preferences.sortType.collect { type ->
                _uiState.update { it.copy(sortType = type) }
                refreshCurrentDirectory()
            }
        }
        
        viewModelScope.launch {
            preferences.sortDirection.collect { direction ->
                _uiState.update { it.copy(sortDirection = direction) }
                refreshCurrentDirectory()
            }
        }
        
        viewModelScope.launch {
            preferences.showHiddenFiles.collect { show ->
                _uiState.update { it.copy(showHiddenFiles = show) }
                refreshCurrentDirectory()
            }
        }
        
        viewModelScope.launch {
            preferences.favorites.collect { favs ->
                _uiState.update { it.copy(favorites = favs) }
                refreshCurrentDirectory()
            }
        }
        
        // Setup search with debouncing
        viewModelScope.launch {
            _searchQueryFlow
                .debounce(300)
                .collect { query ->
                    if (query.isNotEmpty()) {
                        performSearch(query)
                    } else {
                        _uiState.update { it.copy(isSearching = false, searchResults = emptyList()) }
                    }
                }
        }
    }
    
    fun loadInitialDirectory() {
        val rootPath = repository.getStorageRoot()
        navigateToDirectory(rootPath, clearStack = true)
    }
    
    fun navigateToDirectory(path: String, clearStack: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSelectionMode = false, selectedFiles = emptySet()) }
            
            if (clearStack) {
                navigationStack.clear()
            } else {
                _uiState.value.currentPath.takeIf { it.isNotEmpty() }?.let {
                    navigationStack.add(it)
                }
            }
            
            loadFiles(path)
        }
    }
    
    private suspend fun loadFiles(path: String) {
        val state = _uiState.value
        val result = repository.getFilesInDirectory(
            path = path,
            showHidden = state.showHiddenFiles,
            sortType = state.sortType,
            sortDirection = state.sortDirection,
            favorites = state.favorites
        )
        
        result.fold(
            onSuccess = { files ->
                _uiState.update {
                    it.copy(
                        currentPath = path,
                        files = files,
                        isLoading = false,
                        error = null,
                        canNavigateBack = navigationStack.isNotEmpty()
                    )
                }
            },
            onFailure = { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            }
        )
    }
    
    private suspend fun refreshCurrentDirectory() {
        val currentPath = _uiState.value.currentPath
        if (currentPath.isNotEmpty()) {
            loadFiles(currentPath)
        }
    }
    
    fun navigateBack() {
        if (navigationStack.isNotEmpty()) {
            val previousPath = navigationStack.removeAt(navigationStack.lastIndex)
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                loadFiles(previousPath)
            }
        }
    }
    
    fun onFileClick(fileItem: FileItem) {
        if (_uiState.value.isSelectionMode) {
            toggleFileSelection(fileItem)
        } else {
            if (fileItem.isDirectory) {
                navigateToDirectory(fileItem.path)
            } else {
                _uiState.update { it.copy(selectedFile = fileItem) }
                viewModelScope.launch {
                    preferences.addRecentFile(fileItem.path)
                }
            }
        }
    }
    
    fun onFileLongClick(fileItem: FileItem) {
        enterSelectionMode(fileItem)
    }
    
    private fun enterSelectionMode(fileItem: FileItem) {
        _uiState.update {
            it.copy(
                isSelectionMode = true,
                selectedFiles = setOf(fileItem.path)
            )
        }
    }
    
    fun exitSelectionMode() {
        _uiState.update {
            it.copy(
                isSelectionMode = false,
                selectedFiles = emptySet()
            )
        }
    }
    
    private fun toggleFileSelection(fileItem: FileItem) {
        _uiState.update { state ->
            val newSelection = if (state.selectedFiles.contains(fileItem.path)) {
                state.selectedFiles - fileItem.path
            } else {
                state.selectedFiles + fileItem.path
            }
            
            state.copy(
                selectedFiles = newSelection,
                isSelectionMode = newSelection.isNotEmpty()
            )
        }
    }
    
    fun selectAll() {
        _uiState.update { state ->
            state.copy(
                selectedFiles = state.files.map { it.path }.toSet(),
                isSelectionMode = true
            )
        }
    }
    
    // File Operations
    
    fun copyFiles() {
        val selectedFiles = getSelectedFileItems()
        if (selectedFiles.isNotEmpty()) {
            clipboardManager.copy(selectedFiles)
            exitSelectionMode()
        }
    }
    
    fun cutFiles() {
        val selectedFiles = getSelectedFileItems()
        if (selectedFiles.isNotEmpty()) {
            clipboardManager.cut(selectedFiles)
            exitSelectionMode()
        }
    }
    
    fun pasteFiles() {
        val clipboardItem = clipboardManager.clipboardState.value ?: return
        val currentPath = _uiState.value.currentPath
        
        viewModelScope.launch {
            val operation = if (clipboardItem.operationType == ClipboardOperationType.COPY) {
                fileOperations.copyFiles(clipboardItem.files, currentPath)
            } else {
                fileOperations.moveFiles(clipboardItem.files, currentPath)
            }
            
            operation.collect { progress ->
                _uiState.update { it.copy(operationProgress = progress) }
                
                if (progress.isComplete) {
                    if (progress.error == null) {
                        clipboardManager.clear()
                        refreshCurrentDirectory()
                    }
                    // Clear progress after a delay
                    kotlinx.coroutines.delay(1000)
                    _uiState.update { it.copy(operationProgress = null) }
                }
            }
        }
    }
    
    fun deleteFiles(files: List<FileItem>) {
        viewModelScope.launch {
            fileOperations.deleteFiles(files).collect { progress ->
                _uiState.update { it.copy(operationProgress = progress) }
                
                if (progress.isComplete) {
                    if (progress.error == null) {
                        refreshCurrentDirectory()
                    }
                    kotlinx.coroutines.delay(1000)
                    _uiState.update { it.copy(operationProgress = null) }
                }
            }
        }
        exitSelectionMode()
    }
    
    fun renameFile(fileItem: FileItem, newName: String) {
        viewModelScope.launch {
            val result = fileOperations.renameFile(fileItem, newName)
            result.fold(
                onSuccess = {
                    refreshCurrentDirectory()
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(error = exception.message) }
                }
            )
        }
    }
    
    fun createFolder(folderName: String) {
        viewModelScope.launch {
            val result = fileOperations.createFolder(_uiState.value.currentPath, folderName)
            result.fold(
                onSuccess = {
                    refreshCurrentDirectory()
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(error = exception.message) }
                }
            )
        }
    }
    
    fun compressFiles(files: List<FileItem>, zipName: String) {
        viewModelScope.launch {
            fileOperations.compressToZip(
                files,
                _uiState.value.currentPath,
                zipName
            ).collect { progress ->
                _uiState.update { it.copy(operationProgress = progress) }
                
                if (progress.isComplete) {
                    if (progress.error == null) {
                        refreshCurrentDirectory()
                    }
                    kotlinx.coroutines.delay(1000)
                    _uiState.update { it.copy(operationProgress = null) }
                }
            }
        }
        exitSelectionMode()
    }
    
    // Search
    
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQueryFlow.value = query
    }
    
    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            
            val result = repository.searchFiles(
                rootPath = _uiState.value.currentPath,
                query = query,
                showHidden = _uiState.value.showHiddenFiles,
                favorites = _uiState.value.favorites
            )
            
            result.fold(
                onSuccess = { results ->
                    _uiState.update {
                        it.copy(
                            searchResults = results,
                            isSearching = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            error = exception.message
                        )
                    }
                }
            )
        }
    }
    
    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                isSearching = false,
                searchResults = emptyList()
            )
        }
        _searchQueryFlow.value = ""
    }
    
    // Preferences
    
    fun toggleViewMode() {
        viewModelScope.launch {
            val newMode = if (_uiState.value.viewMode == ViewMode.LIST) {
                ViewMode.GRID
            } else {
                ViewMode.LIST
            }
            preferences.setViewMode(newMode)
        }
    }
    
    fun setSortType(type: SortType) {
        viewModelScope.launch {
            preferences.setSortType(type)
        }
    }
    
    fun toggleSortDirection() {
        viewModelScope.launch {
            val newDirection = if (_uiState.value.sortDirection == SortDirection.ASCENDING) {
                SortDirection.DESCENDING
            } else {
                SortDirection.ASCENDING
            }
            preferences.setSortDirection(newDirection)
        }
    }
    
    fun toggleShowHiddenFiles() {
        viewModelScope.launch {
            preferences.setShowHiddenFiles(!_uiState.value.showHiddenFiles)
        }
    }
    
    fun toggleFavorite(fileItem: FileItem) {
        viewModelScope.launch {
            if (_uiState.value.favorites.contains(fileItem.path)) {
                preferences.removeFavorite(fileItem.path)
            } else {
                preferences.addFavorite(fileItem.path)
            }
        }
    }
    
    // Dialog Management
    
    fun showCreateFolderDialog() {
        _uiState.update { it.copy(showOperationDialog = OperationDialogType.CreateFolder) }
    }
    
    fun showRenameDialog(fileItem: FileItem) {
        _uiState.update { it.copy(showOperationDialog = OperationDialogType.Rename(fileItem)) }
    }
    
    fun showDeleteDialog() {
        val selectedFiles = getSelectedFileItems()
        if (selectedFiles.isNotEmpty()) {
            _uiState.update { it.copy(showOperationDialog = OperationDialogType.Delete(selectedFiles)) }
        }
    }
    
    fun showFilePropertiesDialog(fileItem: FileItem) {
        _uiState.update { it.copy(showOperationDialog = OperationDialogType.FileProperties(fileItem)) }
    }
    
    fun dismissDialog() {
        _uiState.update { it.copy(showOperationDialog = null) }
    }
    
    // Helpers
    
    private fun getSelectedFileItems(): List<FileItem> {
        val selectedPaths = _uiState.value.selectedFiles
        return _uiState.value.files.filter { selectedPaths.contains(it.path) }
    }
    
    fun getQuickAccessPaths(): Map<String, String> {
        return repository.getQuickAccessPaths()
    }
    
    fun clearSelectedFile() {
        _uiState.update { it.copy(selectedFile = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun hasClipboardItems(): Boolean {
        return clipboardManager.hasItems()
    }
}
