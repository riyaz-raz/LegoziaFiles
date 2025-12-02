package com.legozia.files.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.legozia.files.model.ViewMode
import com.legozia.files.ui.components.*
import com.legozia.files.util.FileShareHelper
import com.legozia.files.viewmodel.FileManagerViewModel
import com.legozia.files.viewmodel.OperationDialogType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel,
    onNavigateToAnalyzeStorage: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    var showSortFilterSheet by remember { mutableStateOf(false) }
    var showFileOperationSheet by remember { mutableStateOf(false) }
    var selectedFileForSheet by remember { mutableStateOf<com.legozia.files.model.FileItem?>(null) }
    
    // Permission handling
    val hasStoragePermission = remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        hasStoragePermission.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == 
                PackageManager.PERMISSION_GRANTED
        }
    }
    
    val legacyPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        rememberMultiplePermissionsState(
            permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        )
    } else {
        null
    }
    
    LaunchedEffect(legacyPermissions?.allPermissionsGranted) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            hasStoragePermission.value = legacyPermissions?.allPermissionsGranted == true
        }
    }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasStoragePermission.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else {
                    context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == 
                        PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    LaunchedEffect(hasStoragePermission.value) {
        if (hasStoragePermission.value) {
            viewModel.loadInitialDirectory()
        }
    }
    
    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionModeTopBar(
                    selectedCount = uiState.selectedFiles.size,
                    onClose = { viewModel.exitSelectionMode() },
                    onSelectAll = { viewModel.selectAll() },
                    onCopy = { viewModel.copyFiles() },
                    onCut = { viewModel.cutFiles() },
                    onDelete = { viewModel.showDeleteDialog() }
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "File Manager",
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (uiState.currentPath.isNotEmpty() && uiState.selectedFile == null) {
                                Text(
                                    text = uiState.currentPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
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
                        
                        // Sort/Filter
                        IconButton(onClick = { showSortFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort and filter"
                            )
                        }
                        
                        // Storage analysis
                        IconButton(onClick = onNavigateToAnalyzeStorage) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Analyze Storage"
                            )
                        }
                        
                        // Settings
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    },
                    navigationIcon = {
                        if (uiState.canNavigateBack && uiState.selectedFile == null) {
                            IconButton(onClick = { viewModel.navigateBack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Navigate back"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode && uiState.selectedFile == null && hasStoragePermission.value) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Paste FAB (shown when clipboard has items)
                    if (viewModel.hasClipboardItems()) {
                        SmallFloatingActionButton(
                            onClick = { viewModel.pasteFiles() }
                        ) {
                            Icon(Icons.Default.ContentPaste, "Paste")
                        }
                    }
                    
                    // New folder FAB
                    FloatingActionButton(
                        onClick = { viewModel.showCreateFolderDialog() }
                    ) {
                        Icon(Icons.Default.CreateNewFolder, "New folder")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show preview screen if file is selected
            uiState.selectedFile?.let { selectedFile ->
                FilePreviewScreen(
                    fileItem = selectedFile,
                    onNavigateBack = { viewModel.clearSelectedFile() }
                )
            } ?: run {
                when {
                    !hasStoragePermission.value -> {
                        PermissionRequestScreen(
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    intent.data = "package:${context.packageName}".toUri()
                                    context.startActivity(intent)
                                } else {
                                    legacyPermissions?.launchMultiplePermissionRequest()
                                }
                            }
                        )
                    }
                    
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    uiState.error != null -> {
                        ErrorScreen(
                            error = uiState.error ?: "Unknown error",
                            onRetry = { viewModel.loadInitialDirectory() }
                        )
                    }
                    
                    uiState.files.isEmpty() -> {
                        EmptyFolderScreen()
                    }
                    
                    else -> {
                        // Show files in list or grid view
                        if (uiState.viewMode == ViewMode.LIST) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = uiState.files,
                                    key = { it.path }
                                ) { fileItem ->
                                    FileItemRow(
                                        fileItem = fileItem,
                                        isSelected = uiState.selectedFiles.contains(fileItem.path),
                                        onClick = { viewModel.onFileClick(fileItem) },
                                        onLongClick = { 
                                            if (!uiState.isSelectionMode) {
                                                selectedFileForSheet = fileItem
                                                showFileOperationSheet = true
                                            } else {
                                                viewModel.onFileClick(fileItem)
                                            }
                                        }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        } else {
                            FileGridView(
                                files = uiState.files,
                                selectedFiles = uiState.selectedFiles,
                                onFileClick = { viewModel.onFileClick(it) },
                                onFileLongClick = { 
                                    if (!uiState.isSelectionMode) {
                                        selectedFileForSheet = it
                                        showFileOperationSheet = true
                                    } else {
                                        viewModel.onFileClick(it)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Operation progress overlay
            uiState.operationProgress?.let { progress ->
                if (!progress.isComplete) {
                    FileOperationProgressDialog(
                        operationType = when (progress.operationType) {
                            is com.legozia.files.model.FileOperationType.Copy -> "Copying"
                            is com.legozia.files.model.FileOperationType.Move -> "Moving"
                            is com.legozia.files.model.FileOperationType.Delete -> "Deleting"
                            is com.legozia.files.model.FileOperationType.Compress -> "Compressing"
                            is com.legozia.files.model.FileOperationType.Extract -> "Extracting"
                        },
                        currentFile = progress.currentFile,
                        progress = progress.progressPercentage,
                        onDismiss = { }
                    )
                }
            }
        }
    }
    
    // Dialogs
    when (val dialog = uiState.showOperationDialog) {
        is OperationDialogType.CreateFolder -> {
            CreateFolderDialog(
                onDismiss = { viewModel.dismissDialog() },
                onCreate = { name ->
                    viewModel.createFolder(name)
                    viewModel.dismissDialog()
                }
            )
        }
        is OperationDialogType.Rename -> {
            RenameDialog(
                currentName = dialog.fileItem.name,
                onDismiss = { viewModel.dismissDialog() },
                onRename = { newName ->
                    viewModel.renameFile(dialog.fileItem, newName)
                    viewModel.dismissDialog()
                }
            )
        }
        is OperationDialogType.Delete -> {
            DeleteConfirmationDialog(
                fileCount = dialog.files.size,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = {
                    viewModel.deleteFiles(dialog.files)
                    viewModel.dismissDialog()
                }
            )
        }
        is OperationDialogType.FileProperties -> {
            // Navigate to properties screen or show in dialog
            // For now, dismiss
            viewModel.dismissDialog()
        }
        null -> { /* No dialog */ }
    }
    
    // Bottom Sheets
    if (showSortFilterSheet) {
        SortFilterBottomSheet(
            currentSortType = uiState.sortType,
            currentSortDirection = uiState.sortDirection,
            showHiddenFiles = uiState.showHiddenFiles,
            onDismiss = { showSortFilterSheet = false },
            onSortTypeChange = { viewModel.setSortType(it) },
            onToggleSortDirection = { viewModel.toggleSortDirection() },
            onToggleHiddenFiles = { viewModel.toggleShowHiddenFiles() }
        )
    }
    
    if (showFileOperationSheet && selectedFileForSheet != null) {
        FileOperationBottomSheet(
            fileItem = selectedFileForSheet!!,
            onDismiss = { 
                showFileOperationSheet = false
                selectedFileForSheet = null
            },
            onCopy = {
                viewModel.onFileLongClick(selectedFileForSheet!!)
                viewModel.copyFiles()
            },
            onCut = {
                viewModel.onFileLongClick(selectedFileForSheet!!)
                viewModel.cutFiles()
            },
            onRename = {
                viewModel.showRenameDialog(selectedFileForSheet!!)
            },
            onDelete = {
                viewModel.onFileLongClick(selectedFileForSheet!!)
                viewModel.showDeleteDialog()
            },
            onShare = {
                FileShareHelper.shareFile(context, selectedFileForSheet!!.file)
            },
            onProperties = {
                viewModel.showFilePropertiesDialog(selectedFileForSheet!!)
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(selectedFileForSheet!!)
            },
            onCompress = if (selectedFileForSheet!!.isDirectory) {
                {
                    viewModel.onFileLongClick(selectedFileForSheet!!)
                    viewModel.compressFiles(
                        listOf(selectedFileForSheet!!),
                        "${selectedFileForSheet!!.name}.zip"
                    )
                }
            } else null,
            isFavorite = selectedFileForSheet!!.isFavorite
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionModeTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close selection mode")
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, "Select all")
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, "Copy")
            }
            IconButton(onClick = onCut) {
                Icon(Icons.Default.ContentCut, "Cut")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun PermissionRequestScreen(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Storage Permission Required",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This app needs storage permission to browse your files.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun EmptyFolderScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This folder is empty",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
