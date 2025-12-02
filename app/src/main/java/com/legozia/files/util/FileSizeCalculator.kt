package com.legozia.files.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object FileSizeCalculator {
    
    suspend fun calculateFolderSize(
        folder: File,
        onProgress: ((Long) -> Unit)? = null
    ): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L
        
        fun calculateRecursive(file: File) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    calculateRecursive(child)
                }
            } else {
                totalSize += file.length()
                onProgress?.invoke(totalSize)
            }
        }
        
        calculateRecursive(folder)
        totalSize
    }
    
    suspend fun countItems(folder: File): Int = withContext(Dispatchers.IO) {
        var count = 0
        
        fun countRecursive(file: File) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    count++
                    countRecursive(child)
                }
            }
        }
        
        countRecursive(folder)
        count
    }
}
