package com.legozia.files.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legozia.files.model.FileItem
import com.legozia.files.model.FileType
import com.legozia.files.repo.FileRepository
import com.legozia.files.util.FileIconProvider
import com.legozia.files.util.FileSizeCalculator
import com.legozia.files.viewmodel.StorageViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeStorageScreen(
    onNavigateBack: () -> Unit,
    viewModel: StorageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val repository = remember { FileRepository() }
    
    // State for largest files/folders
    var largestItems by remember { mutableStateOf<List<Pair<FileItem, Long>>>(emptyList()) }
    var isCalculating by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadStorageStats()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Analysis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isCalculating = true
                                // Find largest files and folders
                                val rootPath = repository.getStorageRoot()
                                val allItems = mutableListOf<Pair<FileItem, Long>>()
                                
                                repository.getFilesInDirectory(rootPath).getOrNull()?.forEach { item ->
                                    val size = if (item.isDirectory) {
                                        FileSizeCalculator.calculateFolderSize(item.file)
                                    } else {
                                        item.size
                                    }
                                    allItems.add(item to size)
                                }
                                
                                // Sort by size descending and take top 20
                                largestItems = allItems.sortedByDescending { it.second }.take(20)
                                isCalculating = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Overview Card
            item {
                uiState.storageStats?.let { stats ->
                    StorageOverviewCard(
                        totalSpace = stats.totalSpace,
                        usedSpace = stats.usedSpace,
                        freeSpace = stats.freeSpace,
                        usedPercentage = stats.usedPercentage
                    )
                }
            }
            
            // Category Breakdown Card
            item {
                if (uiState.categoryStats.isNotEmpty()) {
                    CategoryBreakdownCard(
                        categoryStats = uiState.categoryStats,
                        totalUsed = uiState.storageStats?.usedSpace ?: 0L
                    )
                }
            }
            
            // Largest Files and Folders
            item {
                Text(
                    text = "Largest Files & Folders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (isCalculating) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (largestItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap refresh to analyze largest files",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(largestItems) { (item, size) ->
                    LargestItemCard(
                        fileItem = item,
                        calculatedSize = size
                    )
                }
            }
            
            // Loading/Error states
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            uiState.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageOverviewCard(
    totalSpace: Long,
    usedSpace: Long,
    freeSpace: Long,
    usedPercentage: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Storage Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { usedPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = if (usedPercentage > 90) {
                    MaterialTheme.colorScheme.error
                } else if (usedPercentage > 70) {
                    Color(0xFFFFA726) // Orange
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StorageStatItem(
                    label = "Used",
                    value = formatSize(usedSpace),
                    color = MaterialTheme.colorScheme.primary
                )
                StorageStatItem(
                    label = "Free",
                    value = formatSize(freeSpace),
                    color = MaterialTheme.colorScheme.tertiary
                )
                StorageStatItem(
                    label = "Total",
                    value = formatSize(totalSpace),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$usedPercentage% used",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StorageStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CategoryBreakdownCard(
    categoryStats: Map<FileType, Long>,
    totalUsed: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Storage by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            categoryStats
                .toList()
                .filter { it.second > 0 }
                .sortedByDescending { it.second }
                .forEach { (type, size) ->
                    CategoryItem(
                        fileType = type,
                        size = size,
                        percentage = if (totalUsed > 0) ((size * 100) / totalUsed).toInt() else 0
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
        }
    }
}

@Composable
private fun CategoryItem(
    fileType: FileType,
    size: Long,
    percentage: Int
) {
    val icon = FileIconProvider.getIconForFileType(fileType)
    val color = FileIconProvider.getColorForFileType(fileType)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = fileType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatSize(size),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = color,
            )

        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LargestItemCard(
    fileItem: FileItem,
    calculatedSize: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = FileIconProvider.getIconForFileType(fileItem.getFileType()),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = FileIconProvider.getColorForFileType(fileItem.getFileType())
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (fileItem.isDirectory) "Folder" else fileItem.extension.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = formatSize(calculatedSize),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}