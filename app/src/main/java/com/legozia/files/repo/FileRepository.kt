package com.legozia.files.repo

import android.os.Environment
import com.legozia.files.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository {

    suspend fun getFilesInDirectory(path: String): Result<List<FileItem>> = withContext(Dispatchers.IO) {
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
                    // Skip hidden files that start with .
                    if (file.name.startsWith(".")) return@mapNotNull null
                    FileItem(file)
                } catch (e: Exception) {
                    // Skip files that can't be read
                    null
                }
            } ?: emptyList()

            // Sort: directories first, then by name
            val sortedFiles = files.sortedWith(
                compareByDescending<FileItem> { it.isDirectory }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            )

            Result.success(sortedFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getParentPath(currentPath: String): String? {
        val file = File(currentPath)
        return file.parent
    }

    fun getStorageRoot(): String {
        // Returns the external storage directory
        return Environment.getExternalStorageDirectory().absolutePath
    }
}