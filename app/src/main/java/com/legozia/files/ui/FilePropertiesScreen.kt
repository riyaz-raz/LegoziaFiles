package com.legozia.files.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.legozia.files.model.FileItem
import com.legozia.files.util.FileSizeCalculator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePropertiesScreen(
    fileItem: FileItem,
    onNavigateBack: () -> Unit
) {
    var folderSize by remember { mutableStateOf<Long?>(null) }
    var itemCount by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(fileItem) {
        if (fileItem.isDirectory) {
            scope.launch {
                folderSize = FileSizeCalculator.calculateFolderSize(fileItem.file)
                itemCount = FileSizeCalculator.countItems(fileItem.file)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Properties") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PropertyItem(
                label = "Name",
                value = fileItem.name
            )
            
            PropertyItem(
                label = "Type",
                value = if (fileItem.isDirectory) "Folder" else fileItem.getFileType().name
            )
            
            PropertyItem(
                label = "Location",
                value = fileItem.file.parent ?: ""
            )
            
            PropertyItem(
                label = "Size",
                value = if (fileItem.isDirectory) {
                    folderSize?.let { FileItem(fileItem.file.apply { }).getFormattedSize() } ?: "Calculating..."
                } else {
                    fileItem.getFormattedSize()
                }
            )
            
            if (fileItem.isDirectory && itemCount != null) {
                PropertyItem(
                    label = "Contains",
                    value = "$itemCount items"
                )
            }
            
            PropertyItem(
                label = "Modified",
                value = fileItem.getFormattedDate()
            )
            
            HorizontalDivider()
            
            Text(
                text = "Permissions",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionChip(
                    label = "Read",
                    granted = fileItem.canRead
                )
                PermissionChip(
                    label = "Write",
                    granted = fileItem.canWrite
                )
                PermissionChip(
                    label = "Execute",
                    granted = fileItem.canExecute
                )
            }
            
            if (fileItem.isHidden) {
                AssistChip(
                    onClick = { },
                    label = { Text("Hidden file") },
                    enabled = false
                )
            }
        }
    }
}

@Composable
private fun PropertyItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun PermissionChip(
    label: String,
    granted: Boolean
) {
    FilterChip(
        selected = granted,
        onClick = { },
        label = { Text(label) },
        enabled = false
    )
}
