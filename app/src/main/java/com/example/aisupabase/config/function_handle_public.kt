package com.example.aisupabase.config

import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aisupabase.models.Tags
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object function_handle_public {
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
        return trimmed.isNotEmpty() && trimmed == title
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

    @Composable
    fun TagName(supabase: SupabaseClient, id: Int) {
        var typeAccount by remember { mutableStateOf<Tags?>(null) }

        LaunchedEffect(id) {
            typeAccount = getTagName(supabase, id)
        }

        Text(
            text = "${typeAccount?.title_tag ?: "Unknown"}",
            fontSize = 14.sp,
            color = Color(0xFF4ECDC4),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }

    suspend fun getTagName(supabase: SupabaseClient, id: Int): Tags? {
        val typeList = supabase.postgrest["tags"]
            .select {
                filter { eq("id", id) }
            }
            .decodeList<Tags>()
        return typeList.firstOrNull()
    }
}