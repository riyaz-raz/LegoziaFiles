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
import com.legozia.files.model.SortDirection
import com.legozia.files.model.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterBottomSheet(
    currentSortType: SortType,
    currentSortDirection: SortDirection,
    showHiddenFiles: Boolean,
    onDismiss: () -> Unit,
    onSortTypeChange: (SortType) -> Unit,
    onToggleSortDirection: () -> Unit,
    onToggleHiddenFiles: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Sort & Filter",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            HorizontalDivider()
            
            // Sort Type
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            SortOption(
                icon = Icons.Default.SortByAlpha,
                text = "Name",
                isSelected = currentSortType == SortType.NAME,
                onClick = {
                    onSortTypeChange(SortType.NAME)
                }
            )
            
            SortOption(
                icon = Icons.Default.Storage,
                text = "Size",
                isSelected = currentSortType == SortType.SIZE,
                onClick = {
                    onSortTypeChange(SortType.SIZE)
                }
            )
            
            SortOption(
                icon = Icons.Default.Schedule,
                text = "Date modified",
                isSelected = currentSortType == SortType.DATE,
                onClick = {
                    onSortTypeChange(SortType.DATE)
                }
            )
            
            SortOption(
                icon = Icons.Default.Category,
                text = "Type",
                isSelected = currentSortType == SortType.TYPE,
                onClick = {
                    onSortTypeChange(SortType.TYPE)
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Sort Direction
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleSortDirection)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentSortDirection == SortDirection.ASCENDING) {
                            Icons.Default.ArrowUpward
                        } else {
                            Icons.Default.ArrowDownward
                        },
                        contentDescription = "Sort direction"
                    )
                    Text(
                        text = if (currentSortDirection == SortDirection.ASCENDING) {
                            "Ascending"
                        } else {
                            "Descending"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Show Hidden Files
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleHiddenFiles)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Show hidden files"
                    )
                    Text(
                        text = "Show hidden files",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Switch(
                    checked = showHiddenFiles,
                    onCheckedChange = { onToggleHiddenFiles() }
                )
            }
        }
    }
}

@Composable
private fun SortOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
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
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        if (isSelected) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
