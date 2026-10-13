package com.example.schlaftagebuch_vers_1.api.report

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object PdfUtils {

    fun savePdfToAppDownloads(context: Context, bytes: ByteArray, filename: String): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir

        if (!dir.exists()) dir.mkdirs()

        val safeName = filename
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .let { if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf" }

        val out = File(dir, safeName)
        out.writeBytes(bytes)
        return out
    }

    fun openPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "PDF öffnen")
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Keine PDF-App gefunden", Toast.LENGTH_LONG).show()
        }
    }
}
