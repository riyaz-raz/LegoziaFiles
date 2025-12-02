package com.legozia.files.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legozia.files.model.FileType
import com.legozia.files.repo.FileRepository
import com.legozia.files.repo.StorageStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StorageUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val storageStats: StorageStats? = null,
    val categoryStats: Map<FileType, Long> = emptyMap()
)

class StorageViewModel(
    private val repository: FileRepository = FileRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()
    
    fun loadStorageStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val storageResult = repository.getStorageStats()
            val categoryResult = repository.getCategoryStats(repository.getStorageRoot())
            
            storageResult.fold(
                onSuccess = { stats ->
                    categoryResult.fold(
                        onSuccess = { categories ->
                            _uiState.value = StorageUiState(
                                isLoading = false,
                                storageStats = stats,
                                categoryStats = categories
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = StorageUiState(
                                isLoading = false,
                                storageStats = stats,
                                error = exception.message
                            )
                        }
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
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
