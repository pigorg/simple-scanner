package com.alessandrognola.docscanner.files

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FileFormat(val extension: String, val mimeType: String) {
    JPG("jpg", "image/jpeg"),
    PDF("pdf", "application/pdf")
}

class SaveShareHelper(private val context: Context) {

    fun saveToGallery(sourceUri: Uri, format: FileFormat): Uri? = when (format) {
        FileFormat.JPG -> saveJpgToGallery(sourceUri)
        FileFormat.PDF -> savePdfToGallery(sourceUri)
    }

    fun shareFile(sourceUri: Uri, format: FileFormat): Intent? = when (format) {
        FileFormat.JPG -> shareJpg(sourceUri)
        FileFormat.PDF -> sharePdf(sourceUri)
    }

    private fun saveJpgToGallery(sourceUri: Uri): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName(FileFormat.JPG))
            put(MediaStore.Images.Media.MIME_TYPE, FileFormat.JPG.mimeType)
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

    private fun savePdfToGallery(sourceUri: Uri): Uri? {
        val pdfBytes = createPdfBytes(sourceUri) ?: return null
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName(FileFormat.PDF))
            put(MediaStore.MediaColumns.MIME_TYPE, FileFormat.PDF.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/DocScanner")
        }
        val destUri = resolver.insert(MediaStore.Files.getContentUri("external"), values) ?: return null
        return try {
            resolver.openOutputStream(destUri)?.use { output -> output.write(pdfBytes) }
                ?: throw IOException("Impossibile aprire il file di destinazione")
            destUri
        } catch (e: Exception) {
            resolver.delete(destUri, null, null)
            null
        }
    }

    private fun shareJpg(sourceUri: Uri): Intent {
        val cacheDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val shareFile = File(cacheDir, fileName(FileFormat.JPG))
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(shareFile).use { output -> input.copyTo(output) }
        }
        return buildShareIntent(shareFile, FileFormat.JPG)
    }

    private fun sharePdf(sourceUri: Uri): Intent? {
        val pdfBytes = createPdfBytes(sourceUri) ?: return null
        val cacheDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val shareFile = File(cacheDir, fileName(FileFormat.PDF))
        FileOutputStream(shareFile).use { output -> output.write(pdfBytes) }
        return buildShareIntent(shareFile, FileFormat.PDF)
    }

    private fun buildShareIntent(file: File, format: FileFormat): Intent {
        val shareUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun createPdfBytes(sourceUri: Uri): ByteArray? {
        val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) }
            ?: return null
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = document.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)
        val output = ByteArrayOutputStream()
        document.writeTo(output)
        document.close()
        return output.toByteArray()
    }

    private fun fileName(format: FileFormat): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "scan_$timestamp.${format.extension}"
    }
}
