package com.shkcodes.aurora.service

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileService @Inject constructor(@ApplicationContext private val context: Context) {

    fun getFile(uri: Uri): File {
        return uri.authority.let {
            context.contentResolver.openInputStream(uri).use {
                val photoFile = File(context.externalCacheDir, fileName(uri)).apply {
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
}
