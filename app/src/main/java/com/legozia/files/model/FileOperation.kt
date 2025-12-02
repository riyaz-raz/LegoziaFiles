package com.legozia.files.model

sealed class FileOperationType {
    data object Copy : FileOperationType()
    data object Move : FileOperationType()
    data object Delete : FileOperationType()
    data object Compress : FileOperationType()
    data object Extract : FileOperationType()
}

data class FileOperationProgress(
    val operationType: FileOperationType,
    val totalItems: Int,
    val processedItems: Int,
    val currentFile: String = "",
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val progressPercentage: Int
        get() = if (totalItems > 0) (processedItems * 100) / totalItems else 0
}

data class ClipboardItem(
    val files: List<FileItem>,
    val operationType: ClipboardOperationType
)

enum class ClipboardOperationType {
    COPY,
    CUT
}
