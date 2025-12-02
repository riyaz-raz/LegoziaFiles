package com.legozia.files.model

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isDirectory) 0 else file.length(),
    val lastModified: Long = file.lastModified(),
    val extension: String = if (file.isDirectory) "" else file.extension.lowercase(),
    val isSelected: Boolean = false,
    val isFavorite: Boolean = false
) {
    val canRead: Boolean get() = file.canRead()
    val canWrite: Boolean get() = file.canWrite()
    val canExecute: Boolean get() = file.canExecute()
    val isHidden: Boolean get() = file.isHidden || name.startsWith(".")
    

    fun getFormattedSize(): String {
        if (isDirectory) return ""
        
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format("%.2f KB", size / 1024.0)
            size < 1024 * 1024 * 1024 -> String.format("%.2f MB", size / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
        }
    }
    
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(lastModified))
    }
    
    fun getFileType(): FileType {
        if (isDirectory) return FileType.FOLDER
        
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg" -> FileType.IMAGE
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm" -> FileType.VIDEO
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma" -> FileType.AUDIO
            "pdf" -> FileType.PDF
            "doc", "docx", "txt", "rtf", "odt" -> FileType.DOCUMENT
            "xls", "xlsx", "csv", "ods" -> FileType.SPREADSHEET
            "ppt", "pptx", "odp" -> FileType.PRESENTATION
            "zip", "rar", "7z", "tar", "gz", "bz2" -> FileType.ARCHIVE
            "apk" -> FileType.APK
            "java", "kt", "py", "js", "html", "css", "xml", "json", "cpp", "c", "h" -> FileType.CODE
            else -> FileType.UNKNOWN
        }
    }
}

enum class FileType {
    FOLDER,
    IMAGE,
    VIDEO,
    AUDIO,
    PDF,
    DOCUMENT,
    SPREADSHEET,
    PRESENTATION,
    ARCHIVE,
    APK,
    CODE,
    UNKNOWN
}
