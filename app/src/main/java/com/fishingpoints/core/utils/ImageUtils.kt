package com.fishingpoints.core.utils

import android.content.Context
import android.net.Uri
import java.io.File

fun saveImageToInternalStorage(
    context: Context,
    uri: Uri
): String? {

    return try {

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return null

        val fileName = "fish_${System.currentTimeMillis()}.jpg"

        val file = File(context.filesDir, fileName)

        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        file.absolutePath

    } catch (e: Exception) {
//        e.printStackTrace()
        null
    }
}