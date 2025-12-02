package com.legozia.files.repo

import com.legozia.files.model.FileItem
import com.legozia.files.model.FileOperationProgress
import com.legozia.files.model.FileOperationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileOperations {
    
    suspend fun copyFiles(
        files: List<FileItem>,
        destinationPath: String
    ): Flow<FileOperationProgress> = flow {
        withContext(Dispatchers.IO) {
            val totalItems = files.size
            var processedItems = 0
            var error: String? = null
            
            for (fileItem in files) {
                try {
                    val destination = File(destinationPath, fileItem.name)
                    
                    emit(FileOperationProgress(
                        operationType = FileOperationType.Copy,
                        totalItems = totalItems,
                        processedItems = processedItems,
                        currentFile = fileItem.name
                    ))
                    
                    if (fileItem.isDirectory) {
                        copyDirectoryRecursively(fileItem.file, destination)
                    } else {
                        copyFile(fileItem.file, destination)
                    }
                    
                    processedItems++
                } catch (e: Exception) {
                    error = "Failed to copy ${fileItem.name}: ${e.message}"
                    break
                }
            }
            
            emit(FileOperationProgress(
                operationType = FileOperationType.Copy,
                totalItems = totalItems,
                processedItems = processedItems,
                isComplete = true,
                error = error
            ))
        }
    }
    
    suspend fun moveFiles(
        files: List<FileItem>,
        destinationPath: String
    ): Flow<FileOperationProgress> = flow {
        withContext(Dispatchers.IO) {
            val totalItems = files.size
            var processedItems = 0
            var error: String? = null
            
            for (fileItem in files) {
                try {
                    val destination = File(destinationPath, fileItem.name)
                    
                    emit(FileOperationProgress(
                        operationType = FileOperationType.Move,
                        totalItems = totalItems,
                        processedItems = processedItems,
                        currentFile = fileItem.name
                    ))
                    
                    val success = fileItem.file.renameTo(destination)
                    if (!success) {
                        // If rename fails, try copy and delete
                        if (fileItem.isDirectory) {
                            copyDirectoryRecursively(fileItem.file, destination)
                        } else {
                            copyFile(fileItem.file, destination)
                        }
                        deleteRecursively(fileItem.file)
                    }
                    
                    processedItems++
                } catch (e: Exception) {
                    error = "Failed to move ${fileItem.name}: ${e.message}"
                    break
                }
            }
            
            emit(FileOperationProgress(
                operationType = FileOperationType.Move,
                totalItems = totalItems,
                processedItems = processedItems,
                isComplete = true,
                error = error
            ))
        }
    }
    
    suspend fun deleteFiles(files: List<FileItem>): Flow<FileOperationProgress> = flow {
        withContext(Dispatchers.IO) {
            val totalItems = files.size
            var processedItems = 0
            var error: String? = null
            
            for (fileItem in files) {
                try {
                    emit(FileOperationProgress(
                        operationType = FileOperationType.Delete,
                        totalItems = totalItems,
                        processedItems = processedItems,
                        currentFile = fileItem.name
                    ))
                    
                    deleteRecursively(fileItem.file)
                    processedItems++
                } catch (e: Exception) {
                    error = "Failed to delete ${fileItem.name}: ${e.message}"
                    break
                }
            }
            
            emit(FileOperationProgress(
                operationType = FileOperationType.Delete,
                totalItems = totalItems,
                processedItems = processedItems,
                isComplete = true,
                error = error
            ))
        }
    }
    
    suspend fun renameFile(file: FileItem, newName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val newFile = File(file.file.parent, newName)
            if (newFile.exists()) {
                return@withContext Result.failure(Exception("A file with this name already exists"))
            }
            
            val success = file.file.renameTo(newFile)
            if (success) {
                Result.success(newFile)
            } else {
                Result.failure(Exception("Failed to rename file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createFolder(parentPath: String, folderName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val newFolder = File(parentPath, folderName)
            if (newFolder.exists()) {
                return@withContext Result.failure(Exception("A folder with this name already exists"))
            }
            
            val success = newFolder.mkdir()
            if (success) {
                Result.success(newFolder)
            } else {
                Result.failure(Exception("Failed to create folder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun compressToZip(
        files: List<FileItem>,
        destinationPath: String,
        zipName: String
    ): Flow<FileOperationProgress> = flow {
        withContext(Dispatchers.IO) {
            val totalItems = files.size
            var processedItems = 0
            var error: String? = null
            
            try {
                val zipFile = File(destinationPath, zipName)
                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                    for (fileItem in files) {
                        emit(FileOperationProgress(
                            operationType = FileOperationType.Compress,
                            totalItems = totalItems,
                            processedItems = processedItems,
                            currentFile = fileItem.name
                        ))
                        
                        addToZip(fileItem.file, fileItem.name, zos)
                        processedItems++
                    }
                }
            } catch (e: Exception) {
                error = "Failed to create ZIP: ${e.message}"
            }
            
            emit(FileOperationProgress(
                operationType = FileOperationType.Compress,
                totalItems = totalItems,
                processedItems = processedItems,
                isComplete = true,
                error = error
            ))
        }
    }
    
    suspend fun extractZip(
        zipFile: File,
        destinationPath: String
    ): Flow<FileOperationProgress> = flow {
        withContext(Dispatchers.IO) {
            var error: String? = null
            var processedItems = 0
            
            try {
                ZipInputStream(FileInputStream(zipFile)).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    
                    while (entry != null) {
                        emit(FileOperationProgress(
                            operationType = FileOperationType.Extract,
                            totalItems = 0, // Unknown total
                            processedItems = processedItems,
                            currentFile = entry.name
                        ))
                        
                        val file = File(destinationPath, entry.name)
                        
                        if (entry.isDirectory) {
                            file.mkdirs()
                        } else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        
                        processedItems++
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            } catch (e: Exception) {
                error = "Failed to extract ZIP: ${e.message}"
            }
            
            emit(FileOperationProgress(
                operationType = FileOperationType.Extract,
                totalItems = processedItems,
                processedItems = processedItems,
                isComplete = true,
                error = error
            ))
        }
    }
    
    // Private helper methods
    
    private fun copyFile(source: File, destination: File) {
        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }
    
    private fun copyDirectoryRecursively(source: File, destination: File) {
        if (!destination.exists()) {
            destination.mkdirs()
        }
        
        source.listFiles()?.forEach { file ->
            val destFile = File(destination, file.name)
            if (file.isDirectory) {
                copyDirectoryRecursively(file, destFile)
            } else {
                copyFile(file, destFile)
            }
        }
    }
    
    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                deleteRecursively(child)
            }
        }
        file.delete()
    }
    
    private fun addToZip(file: File, entryName: String, zos: ZipOutputStream) {
        if (file.isDirectory) {
            val children = file.listFiles() ?: return
            for (child in children) {
                addToZip(child, "$entryName/${child.name}", zos)
            }
        } else {
            FileInputStream(file).use { fis ->
                zos.putNextEntry(ZipEntry(entryName))
                fis.copyTo(zos)
                zos.closeEntry()
            }
        }
    }
}
