package com.skriv.app.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.skriv.app.util.EncodingHelper
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FileTooLargeException : IOException("File is larger than 5MB")

class FileRepository(private val context: Context) {

    suspend fun readFile(uri: Uri): Result<Pair<String, Boolean>> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            // 1. Size check
            val statSize = resolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: throw IOException("Could not open file descriptor")

            if (statSize > 5 * 1024 * 1024) {
                throw FileTooLargeException()
            }

            // 2. Read content and check for BOM
            resolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val content = EncodingHelper.decodeUtf8(bytes)
                val hadBom = content.startsWith("\uFEFF")
                val cleanContent = if (hadBom) content.substring(1) else content
                Pair(cleanContent, hadBom)
            } ?: throw FileNotFoundException("Could not open input stream")
        }
    }

    suspend fun writeFile(uri: Uri, content: String, hadBom: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            resolver.openFileDescriptor(uri, "w")?.use { pfd ->
                FileOutputStream(pfd.fileDescriptor).use { fos ->
                    fos.channel.use { channel ->
                        channel.truncate(0) // Truncate existing content
                        if (hadBom) {
                            val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
                            fos.write(bom)
                        }
                        fos.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
            } ?: throw IOException("Could not open file descriptor")
        }
    }

    fun persistPermission(uri: Uri) {
        if (uri.scheme != "content") return
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        try {
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            // Log and do not crash (handles SecurityException, IllegalArgumentException, etc.)
        }
    }

    fun hasWritePermission(uri: Uri): Boolean {
        val permissions = context.contentResolver.persistedUriPermissions
        return permissions.any { it.uri == uri && it.isWritePermission }
    }

    fun getDisplayName(uri: Uri): String {
        val resolver = context.contentResolver
        try {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1 && !cursor.isNull(index)) {
                           return cursor.getString(index)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore and fallback
        }
        return uri.lastPathSegment ?: "untitled.txt"
    }

    fun getLastModified(uri: Uri): Long? {
        val resolver = context.contentResolver
        try {
            resolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                    if (index != -1 && !cursor.isNull(index)) {
                           val time = cursor.getLong(index)
                           if (time > 0) return time
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore and fallback
        }
        return null
    }

    fun deleteFile(uri: Uri): Boolean {
        return try {
            DocumentsContract.deleteDocument(context.contentResolver, uri)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isUriAvailable(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use {
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
