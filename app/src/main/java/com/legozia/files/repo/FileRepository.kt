package com.legozia.files.repo

import android.os.Environment
import com.legozia.files.model.FileItem
import com.legozia.files.model.FileType
import com.legozia.files.model.SortDirection
import com.legozia.files.model.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository {

    suspend fun getFilesInDirectory(
        path: String,
        showHidden: Boolean = false,
        sortType: SortType = SortType.NAME,
        sortDirection: SortDirection = SortDirection.ASCENDING,
        filterType: FileType? = null,
        searchQuery: String? = null,
        favorites: Set<String> = emptySet()
    ): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val directory = File(path)

            if (!directory.exists()) {
                return@withContext Result.failure(Exception("Directory does not exist"))
            }

            if (!directory.isDirectory) {
                return@withContext Result.failure(Exception("Path is not a directory"))
            }

            val files = directory.listFiles()?.mapNotNull { file ->
                try {
                    // Skip hidden files if not showing them
                    if (!showHidden && (file.name.startsWith(".") || file.isHidden)) {
                        return@mapNotNull null
                    }
                    
                    val fileItem = FileItem(
                        file = file,
                        isFavorite = favorites.contains(file.absolutePath)
                    )
                    
                    // Apply search filter
                    if (searchQuery != null && !file.name.contains(searchQuery, ignoreCase = true)) {
                        return@mapNotNull null
                    }
                    
                    // Apply type filter
                    if (filterType != null && fileItem.getFileType() != filterType) {
                        return@mapNotNull null
                    }
                    
                    fileItem
                } catch (e: Exception) {
                    // Skip files that can't be read
                    null
                }
            } ?: emptyList()

            // Apply sorting
            val sortedFiles = sortFiles(files, sortType, sortDirection)

            Result.success(sortedFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchFiles(
        rootPath: String,
        query: String,
        showHidden: Boolean = false,
        favorites: Set<String> = emptySet()
    ): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<FileItem>()
            val rootDir = File(rootPath)
            
            fun searchRecursive(dir: File, depth: Int = 0) {
                // Limit search depth to prevent performance issues
                if (depth > 5) return
                
                dir.listFiles()?.forEach { file ->
                    try {
                        if (!showHidden && (file.name.startsWith(".") || file.isHidden)) {
                            return@forEach
                        }
                        
                        if (file.name.contains(query, ignoreCase = true)) {
                            results.add(FileItem(
                                file = file,
                                isFavorite = favorites.contains(file.absolutePath)
                            ))
                        }
                        
                        if (file.isDirectory) {
                            searchRecursive(file, depth + 1)
                        }
                    } catch (e: Exception) {
                        // Skip files that can't be read
                    }
                }
            }
            
            searchRecursive(rootDir)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getParentPath(currentPath: String): String? {
        val file = File(currentPath)
        return file.parent
    }

    fun getStorageRoot(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }
    
    fun getQuickAccessPaths(): Map<String, String> {
        val root = Environment.getExternalStorageDirectory()
        return mapOf(
            "Internal Storage" to root.absolutePath,
            "Downloads" to File(root, Environment.DIRECTORY_DOWNLOADS).absolutePath,
            "Documents" to File(root, Environment.DIRECTORY_DOCUMENTS).absolutePath,
            "Pictures" to File(root, Environment.DIRECTORY_PICTURES).absolutePath,
            "DCIM" to File(root, Environment.DIRECTORY_DCIM).absolutePath,
            "Movies" to File(root, Environment.DIRECTORY_MOVIES).absolutePath,
            "Music" to File(root, Environment.DIRECTORY_MUSIC).absolutePath,
        )
    }
    
    suspend fun getStorageStats(): Result<StorageStats> = withContext(Dispatchers.IO) {
        try {
            val root = Environment.getExternalStorageDirectory()
            val totalSpace = root.totalSpace
            val freeSpace = root.freeSpace
            val usedSpace = totalSpace - freeSpace
            
            Result.success(StorageStats(
                totalSpace = totalSpace,
                freeSpace = freeSpace,
                usedSpace = usedSpace
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCategoryStats(rootPath: String): Result<Map<FileType, Long>> = withContext(Dispatchers.IO) {
        try {
            val stats = mutableMapOf<FileType, Long>()
            val root = File(rootPath)
            
            fun calculateRecursive(dir: File, depth: Int = 0) {
                if (depth > 3) return // Limit depth for performance
                
                dir.listFiles()?.forEach { file ->
                    try {
                        if (file.isDirectory) {
                            calculateRecursive(file, depth + 1)
                        } else {
                            val fileItem = FileItem(file)
                            val type = fileItem.getFileType()
                            stats[type] = (stats[type] ?: 0L) + file.length()
                        }
                    } catch (e: Exception) {
                        // Skip files that can't be read
                    }
                }
            }
            
            calculateRecursive(root)
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun sortFiles(
        files: List<FileItem>,
        sortType: SortType,
        sortDirection: SortDirection
    ): List<FileItem> {
        val comparator: Comparator<FileItem> = when (sortType) {
            SortType.NAME -> compareBy { it.name.lowercase() }
            SortType.SIZE -> compareBy { it.size }
            SortType.DATE -> compareBy { it.lastModified }
            SortType.TYPE -> compareBy { it.extension }
        }
        
        val dirComparator = compareByDescending<FileItem> { it.isDirectory }
        val finalComparator = if (sortDirection == SortDirection.ASCENDING) {
            dirComparator.then(comparator)
        } else {
            dirComparator.then(comparator.reversed())
        }
        
        return files.sortedWith(finalComparator)
    }
}

data class StorageStats(
    val totalSpace: Long,
    val freeSpace: Long,
    val usedSpace: Long
) {
    val usedPercentage: Int
        get() = if (totalSpace > 0) ((usedSpace * 100) / totalSpace).toInt() else 0
        
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}