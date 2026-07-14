package com.alessandrognola.docscanner.files

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaveShareHelper(private val context: Context) {

    fun saveToGallery(sourceUri: Uri): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName())
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DocScanner")
        }
        val destUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        return try {
            resolver.openOutputStream(destUri)?.use { output ->
                resolver.openInputStream(sourceUri)?.use { input -> input.copyTo(output) }
                    ?: throw IOException("Impossibile leggere l'immagine scansionata")
            } ?: throw IOException("Impossibile aprire il file di destinazione")
            destUri
        } catch (e: Exception) {
            resolver.delete(destUri, null, null)
            null
        }
    }

    fun shareImage(sourceUri: Uri): Intent {
        val cacheDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val shareFile = File(cacheDir, fileName())
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(shareFile).use { output -> input.copyTo(output) }
        }
        val shareUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", shareFile)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun fileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "scan_$timestamp.jpg"
    }
}
