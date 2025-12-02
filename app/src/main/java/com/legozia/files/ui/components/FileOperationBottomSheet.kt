package com.legozia.files.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.legozia.files.model.FileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOperationBottomSheet(
    fileItem: FileItem,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onProperties: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCompress: (() -> Unit)? = null,
    isFavorite: Boolean = false
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Text(
                text = fileItem.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            HorizontalDivider()
            
            // Actions
            BottomSheetMenuItem(
                icon = Icons.Default.ContentCopy,
                text = "Copy",
                onClick = {
                    onCopy()
                    onDismiss()
                }
            )
            
            BottomSheetMenuItem(
                icon = Icons.Default.ContentCut,
                text = "Cut",
                onClick = {
                    onCut()
                    onDismiss()
                }
            )
            
            BottomSheetMenuItem(
                icon = Icons.Default.DriveFileRenameOutline,
                text = "Rename",
                onClick = {
                    onRename()
                    onDismiss()
                }
            )
            
            BottomSheetMenuItem(
                icon = Icons.Default.Delete,
                text = "Delete",
                onClick = {
                    onDelete()
                    onDismiss()
                },
                tint = MaterialTheme.colorScheme.error
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            BottomSheetMenuItem(
                icon = Icons.Default.Share,
                text = "Share",
                onClick = {
                    onShare()
                    onDismiss()
                }
            )
            
            BottomSheetMenuItem(
                icon = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                text = if (isFavorite) "Remove from favorites" else "Add to favorites",
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )
            
            if (fileItem.isDirectory && onCompress != null) {
                BottomSheetMenuItem(
                    icon = Icons.Default.FolderZip,
                    text = "Compress to ZIP",
                    onClick = {
                        onCompress()
                        onDismiss()
                    }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            BottomSheetMenuItem(
                icon = Icons.Default.Info,
                text = "Properties",
                onClick = {
                    onProperties()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun BottomSheetMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = tint
        )
    }
}
