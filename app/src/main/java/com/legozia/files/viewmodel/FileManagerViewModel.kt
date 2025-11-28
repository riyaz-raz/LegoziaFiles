package com.legozia.files.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legozia.files.model.FileItem
import com.legozia.files.repo.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FileManagerUiState(
    val currentPath: String = "",
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val canNavigateBack: Boolean = false,
    val selectedFile: FileItem? = null
)

class FileManagerViewModel(
    private val repository: FileRepository = FileRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()
    
    private val navigationStack = mutableListOf<String>()
    
    fun loadInitialDirectory() {
        val rootPath = repository.getStorageRoot()
        navigateToDirectory(rootPath, clearStack = true)
    }
    
    fun navigateToDirectory(path: String, clearStack: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            if (clearStack) {
                navigationStack.clear()
            } else {
                // Add current path to stack before navigating
                _uiState.value.currentPath.takeIf { it.isNotEmpty() }?.let {
                    navigationStack.add(it)
                }
            }
            
            val result = repository.getFilesInDirectory(path)
            
            result.fold(
                onSuccess = { files ->
                    _uiState.value = FileManagerUiState(
                        currentPath = path,
                        files = files,
                        isLoading = false,
                        error = null,
                        canNavigateBack = navigationStack.isNotEmpty()
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }
    
    fun navigateBack() {
        if (navigationStack.isNotEmpty()) {
            val previousPath = navigationStack.removeAt(navigationStack.lastIndex)
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = repository.getFilesInDirectory(previousPath)
                
                result.fold(
                    onSuccess = { files ->
                        _uiState.value = FileManagerUiState(
                            currentPath = previousPath,
                            files = files,
                            isLoading = false,
                            error = null,
                            canNavigateBack = navigationStack.isNotEmpty()
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                )
            }
        }
    }
    
    fun onFileClick(fileItem: FileItem) {
        if (fileItem.isDirectory) {
            navigateToDirectory(fileItem.path)
        } else {
            // Show preview for files
            _uiState.value = _uiState.value.copy(selectedFile = fileItem)
        }
    }
    
    fun clearSelectedFile() {
        _uiState.value = _uiState.value.copy(selectedFile = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
