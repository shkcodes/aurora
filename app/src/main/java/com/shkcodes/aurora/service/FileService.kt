package com.shkcodes.aurora.service

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.shkcodes.aurora.util.fileProviderAuthority
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFIX = "copy_"
private const val EXTENSION_GIF = ".gif"

@Singleton
class FileService @Inject constructor(@ApplicationContext private val context: Context) {

    fun getFile(uri: Uri): File {
        return uri.authority.let {
            context.contentResolver.openInputStream(uri).use {
                val photoFile = File(context.externalCacheDir, "$PREFIX${fileName(uri)}").apply {
                    it?.copyTo(outputStream())
                }
                photoFile
            }
        }
    }

    private fun fileName(uri: Uri): String {
        return context.contentResolver.query(uri, null, null, null, null)!!.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        }
    }

    fun downloadGif(id: String, url: String): Uri {
        val file = File(context.externalCacheDir, "$id$EXTENSION_GIF")
        downloadFile(url, file)
        return FileProvider.getUriForFile(context, context.fileProviderAuthority, file)
    }

    private fun downloadFile(url: String, file: File) {
        URL(url).openStream().use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun clearCache() {
        context.externalCacheDir?.deleteRecursively()
    }
}
