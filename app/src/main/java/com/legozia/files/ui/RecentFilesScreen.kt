package com.legozia.files.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.legozia.files.model.FileItem
import com.legozia.files.ui.components.FileItemRow
import com.legozia.files.util.FileShareHelper
import com.legozia.files.viewmodel.FileManagerViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentFilesScreen(
    viewModel: FileManagerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFile: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Get recent files (reversed to show most recent first)
    val recentFiles = uiState.favorites.mapNotNull { path ->
        try {
            val file = File(path)
            if (file.exists() && !file.isDirectory) {
                FileItem(file)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }.reversed()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recent Files") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (recentFiles.isNotEmpty()) {
                        TextButton(
                            onClick = { /* viewModel.clearRecentFiles() */ }
                        ) {
                            Text("Clear")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (recentFiles.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No recent files",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Files you open will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Show recent files
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = recentFiles,
                        key = { it.path }
                    ) { fileItem ->
                        FileItemRow(
                            fileItem = fileItem,
                            onClick = {
                                FileShareHelper.shareFile(context, fileItem.file)
                            },
                            onLongClick = {
                                // Navigate to parent folder
                                fileItem.file.parent?.let { parentPath ->
                                    onNavigateToFile(parentPath)
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
