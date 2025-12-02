package com.legozia.files.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.legozia.files.model.FileType

object FileIconProvider {
    
    fun getIconForFileType(fileType: FileType): ImageVector {
        return when (fileType) {
            FileType.FOLDER -> Icons.Default.Folder
            FileType.IMAGE -> Icons.Default.Image
            FileType.VIDEO -> Icons.Default.VideoFile
            FileType.AUDIO -> Icons.Default.AudioFile
            FileType.PDF -> Icons.Default.PictureAsPdf
            FileType.DOCUMENT -> Icons.Default.Description
            FileType.SPREADSHEET -> Icons.Default.TableChart
            FileType.PRESENTATION -> Icons.Default.Slideshow
            FileType.ARCHIVE -> Icons.Default.FolderZip
            FileType.APK -> Icons.Default.Android
            FileType.CODE -> Icons.Default.Code
            FileType.UNKNOWN -> Icons.Default.InsertDriveFile
        }
    }
    
    fun getColorForFileType(fileType: FileType): Color {
        return when (fileType) {
            FileType.FOLDER -> Color(0xFF42A5F5) // Blue
            FileType.IMAGE -> Color(0xFFEF5350) // Red
            FileType.VIDEO -> Color(0xFFAB47BC) // Purple
            FileType.AUDIO -> Color(0xFFFF7043) // Deep Orange
            FileType.PDF -> Color(0xFFF44336) // Red
            FileType.DOCUMENT -> Color(0xFF42A5F5) // Blue
            FileType.SPREADSHEET -> Color(0xFF66BB6A) // Green
            FileType.PRESENTATION -> Color(0xFFFF9800) // Orange
            FileType.ARCHIVE -> Color(0xFF8D6E63) // Brown
            FileType.APK -> Color(0xFF66BB6A) // Green
            FileType.CODE -> Color(0xFF5C6BC0) // Indigo
            FileType.UNKNOWN -> Color(0xFF78909C) // Blue Grey
        }
    }
}
