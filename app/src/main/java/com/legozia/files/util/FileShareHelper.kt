package com.legozia.files.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object FileShareHelper {
    
    fun shareFiles(context: Context, files: List<File>) {
        if (files.isEmpty()) return
        
        val uris = files.mapNotNull { file ->
            try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                null
            }
        }
        
        if (uris.isEmpty()) return
        
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uris.first())
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
        }
        
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        
        val chooser = Intent.createChooser(intent, "Share files")
        context.startActivity(chooser)
    }
    
    fun shareFile(context: Context, file: File) {
        shareFiles(context, listOf(file))
    }
}
