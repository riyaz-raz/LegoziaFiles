package com.legozia.files.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.legozia.files.model.FileItem
import com.legozia.files.model.ViewMode
import com.legozia.files.ui.components.FileGridView
import com.legozia.files.ui.components.FileItemRow
import com.legozia.files.util.FileShareHelper
import com.legozia.files.viewmodel.FileManagerViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FileManagerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFile: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Get favorite files
    val favoriteFiles = uiState.favorites.mapNotNull { path ->
        try {
            val file = File(path)
            if (file.exists()) {
                FileItem(file, isFavorite = true)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // View mode toggle
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (uiState.viewMode == ViewMode.LIST) {
                                Icons.Default.GridView
                            } else {
                                Icons.Default.ViewList
                            },
                            contentDescription = "Toggle view mode"
                        )
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
            if (favoriteFiles.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No favorites yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Long press on any file or folder to add it to favorites",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Show favorites in list or grid view
                if (uiState.viewMode == ViewMode.LIST) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = favoriteFiles,
                            key = { it.path }
                        ) { fileItem ->
                            FileItemRow(
                                fileItem = fileItem,
                                onClick = {
                                    if (fileItem.isDirectory) {
                                        onNavigateToFile(fileItem.path)
                                    } else {
                                        // Open file
                                        FileShareHelper.shareFile(context, fileItem.file)
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleFavorite(fileItem)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                } else {
                    FileGridView(
                        files = favoriteFiles,
                        selectedFiles = emptySet(),
                        onFileClick = { fileItem ->
                            if (fileItem.isDirectory) {
                                onNavigateToFile(fileItem.path)
                            } else {
                                FileShareHelper.shareFile(context, fileItem.file)
                            }
                        },
                        onFileLongClick = { fileItem ->
                            viewModel.toggleFavorite(fileItem)
                        }
                    )
                }
            }
        }
    }
}
