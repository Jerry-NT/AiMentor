package com.example.aisupabase.config

import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object handle {
    // Hàm chuyển Uri thành File (tạm thời copy file vào cache)
    fun uriToFile(context: android.content.Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(context, uri) ?: "temp_image"
            val tempFile = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    // Lấy tên file từ Uri
    fun getFileName(context: android.content.Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }

    // Lấy publicId từ url Cloudinary
    fun getPublicIdFromUrl(url: String): String {
        // Ví dụ url: https://res.cloudinary.com/demo/image/upload/v1234567890/blogs/abcxyz.jpg
        // publicId: blogs/abcxyz
        val regex = Regex("/upload/(?:v\\d+/)?(.+)\\.[a-zA-Z0-9]+\$")
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: ""
    }

    fun isValidTitle(title: String): Boolean {
        val trimmed = title.trim() // Loại bỏ khoảng trắng đầu và cuối
        val regex = Regex("^[a-zA-Z0-9\\sÀ-ỹ]+$") // Chỉ cho phép chữ cái, số, khoảng trắng và ký tự tiếng Việt
        return trimmed.isNotEmpty() && trimmed == title && regex.matches(title)
    }

    fun formatTransactionDate(dateString: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss") // adjust if needed
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val dateTime = LocalDateTime.parse(dateString, inputFormatter)
            outputFormatter.format(dateTime)
        } catch (e: Exception) {
            dateString // fallback if parsing fails
        }
    }
}