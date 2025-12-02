package com.legozia.files.data

import com.legozia.files.model.ClipboardItem
import com.legozia.files.model.ClipboardOperationType
import com.legozia.files.model.FileItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClipboardManager {
    
    private val _clipboardState = MutableStateFlow<ClipboardItem?>(null)
    val clipboardState: StateFlow<ClipboardItem?> = _clipboardState.asStateFlow()
    
    fun copy(files: List<FileItem>) {
        _clipboardState.value = ClipboardItem(
            files = files,
            operationType = ClipboardOperationType.COPY
        )
    }
    
    fun cut(files: List<FileItem>) {
        _clipboardState.value = ClipboardItem(
            files = files,
            operationType = ClipboardOperationType.CUT
        )
    }
    
    fun clear() {
        _clipboardState.value = null
    }
    
    fun hasItems(): Boolean = _clipboardState.value != null
    
    fun isCutOperation(): Boolean = 
        _clipboardState.value?.operationType == ClipboardOperationType.CUT
}
