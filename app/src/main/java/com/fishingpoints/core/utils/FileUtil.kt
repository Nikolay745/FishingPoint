package com.fishingpoints.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.IOException

object FileUtil {

    fun copyToInternalStorage(
        context: Context,
        sourceUri: Uri
    ): String? {
        return try {
            val inputStream =
                context.contentResolver.openInputStream(sourceUri)
                    ?: return null

            val fileName = "${System.currentTimeMillis()}.jpg"
            val internalFile = File(context.filesDir, fileName)

            inputStream.use { input ->
                internalFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(
                "FileUtil",
                "Фото сохранено: ${internalFile.absolutePath}"
            )

            internalFile.absolutePath

        } catch (e: IOException) {

            Log.e(
                "FileUtil",
                "Ошибка копирования фото",
                e
            )
            null
        }
    }


    /**  * Проверяет, существует ли файл. */
    fun fileExists(path: String?): Boolean {
        if (path.isNullOrBlank()) {
            return false
        }

        return File(path).exists()
    }


    /**   * Удаляет файл. *
     * Возвращает true, если файл был удалён
     * или файла уже не существовало.
     */
    fun deleteFile(path: String?): Boolean {

        if (path.isNullOrBlank()) {
            return true
        }

        return try {
            val file = File(path)

            if (file.exists()) {
                file.delete()
            } else {
                true
            }

        } catch (e: Exception) {
            Log.e("FileUtil", "Ошибка удаления файла: $path", e)
            false
        }
    }
}
