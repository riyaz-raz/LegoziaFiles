package com.legozia.files.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.legozia.files.model.FileItem
import com.legozia.files.model.FileType
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePreviewScreen(
    fileItem: FileItem,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = fileItem.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${fileItem.getFormattedSize()} • ${fileItem.getFormattedDate()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Share button
                    IconButton(
                        onClick = {
                            shareFile(context, fileItem.file)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                    
                    // Open with button
                    IconButton(
                        onClick = {
                            openFileWithExternalApp(context, fileItem.file)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open with"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (fileItem.getFileType()) {
                FileType.IMAGE -> ImagePreview(fileItem)
                FileType.DOCUMENT, FileType.CODE -> TextPreview(fileItem)
                else -> UnsupportedPreview(fileItem)
            }
        }
    }
}

@Composable
private fun ImagePreview(fileItem: FileItem) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(fileItem.file),
            contentDescription = fileItem.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun TextPreview(fileItem: FileItem) {
    var textContent by remember { mutableStateOf("Loading...") }
    var isError by remember { mutableStateOf(false) }
    
    LaunchedEffect(fileItem.path) {
        try {
            // Limit file size to 1MB for preview
            if (fileItem.size > 1024 * 1024) {
                textContent = "File is too large to preview (${fileItem.getFormattedSize()})\nPlease use 'Open With' to view in an external app."
                isError = true
            } else {
                textContent = fileItem.file.readText()
            }
        } catch (e: Exception) {
            textContent = "Error reading file: ${e.message}"
            isError = true
        }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        color = if (isError) 
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = textContent,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = if (isError) 
                MaterialTheme.colorScheme.onErrorContainer
            else 
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun UnsupportedPreview(fileItem: FileItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when (fileItem.getFileType()) {
                FileType.VIDEO -> Icons.Default.VideoFile
                FileType.AUDIO -> Icons.Default.AudioFile
                FileType.PDF -> Icons.Default.PictureAsPdf
                FileType.ARCHIVE -> Icons.Default.FolderZip
                FileType.APK -> Icons.Default.Android
                else -> Icons.Default.InsertDriveFile
            },
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Preview not available",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Use 'Open With' to view this ${fileItem.getFileType().name.lowercase()} file",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shareFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share file"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun openFileWithExternalApp(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun getMimeType(file: File): String {
    return when (file.extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "pdf" -> "application/pdf"
        "mp4" -> "video/mp4"
        "mp3" -> "audio/mpeg"
        "txt" -> "text/plain"
        "zip" -> "application/zip"
        "apk" -> "application/vnd.android.package-archive"
        else -> "*/*"
    }
}
