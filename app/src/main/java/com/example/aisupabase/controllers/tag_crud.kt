package com.example.aisupabase.controllers

import blogs
import com.example.aisupabase.cloudinary.CloudinaryService
import com.example.aisupabase.models.Tags
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Result wrapper for CRUD operations
sealed class TagResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : TagResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : TagResult<Nothing>()
}

// 2. Repository
class TagRepository(private val supabase: SupabaseClient) {
    // Lấy danh sách tags
    suspend fun getTags(): TagResult<List<Tags>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["tags"].select()
            val tags = result.decodeList<Tags>()
            return@withContext TagResult.Success(tags, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext TagResult.Error(e)
        }
    }

    // Xóa theo id
    suspend fun deleteTag(id: Int): TagResult<Unit> = withContext(Dispatchers.IO) {
        try {
            // lay danh sach blog theo id tag
            val blogs = supabase.postgrest["blogs"]
                .select()
            blogs.decodeList<blogs>().forEach { blog ->
                // xoa blog theo id tag
                if (blog.id_tag == id) {
                    CloudinaryService.deleteImage(blog.public_id_image?: "")
                    supabase.postgrest["blogs"]
                        .delete {
                            filter {
                                eq("id", blog.id?: 0)
                            }
                        }
                }
            }

            // thuc hien xoa tag theo id
            val result = supabase.postgrest["tags"]
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            return@withContext TagResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext TagResult.Error(e)
        }
    }

    // Cập nhật theo id
    suspend fun updateTag(id: Int, title: String): TagResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["tags"]
                .update({
                    set("title_tag", title)
                }) {
                    filter {
                        eq("id", id)
                    }
                }
            return@withContext TagResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext TagResult.Error(e)
        }
    }

    // Thêm mới
    suspend fun addTag(title: String): TagResult<Unit> = withContext(Dispatchers.IO) {
        try {

            val result = supabase.postgrest["tags"]
                .insert(mapOf("title_tag" to title))
            return@withContext TagResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext TagResult.Error(e)
        }
    }

    suspend fun checkTagExists(title: String, case: String = "add", id: Int? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val existingTags = supabase.postgrest["tags"]
                .select {
                    filter {
                        ilike("title_tag", title)
                    }
                }.decodeList<Tags>()

            return@withContext when (case) {
                "update" -> {
                    existingTags.any { it.id != id }
                }
                else -> {
                    existingTags.isNotEmpty()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

}
