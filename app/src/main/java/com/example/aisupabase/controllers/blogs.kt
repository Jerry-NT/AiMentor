package com.example.aisupabase.controllers

import blogs
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult

sealed class BlogResult {
    data class Success(val data: List<blogs>?) : BlogResult()
    data class Error(val exception: Throwable) : BlogResult()
}

class BlogRepository(private val supabase: SupabaseClient) {

    suspend fun getBlogs(): BlogResult {
        return try {
            val result = supabase.from("blogs").select().decodeList<blogs>()
            BlogResult.Success(result)
        } catch (e: Exception) {
            BlogResult.Error(e)
        }
    }

    suspend fun addBlog(
        title_blog: String,
        public_id_image: String,
        url_image: String,
        id_tag: Int,
        content_blog: String
    ): BlogResult {
        return try {
            val data = mapOf(
                "title_blog" to title_blog,
                "public_id_image" to public_id_image,
                "url_image" to url_image,
                "id_tag" to id_tag,
                "content_blog" to content_blog
            )
            supabase.from("blogs").insert(data)
            getBlogs()
        } catch (e: Exception) {
            BlogResult.Error(e)
        }
    }

    suspend fun updateBlog(
        id: String,
        title_blog: String,
        public_id_image: String,
        url_image: String,
        id_tag: Int,
        content_blog: String
    ): BlogResult {
        return try {
            val data = mapOf(
                "title_blog" to title_blog,
                "public_id_image" to public_id_image,
                "url_image" to url_image,
                "id_tag" to id_tag,
                "content_blog" to content_blog
            )
            supabase.from("blogs").update(data) {
                filter { eq("id", id) }
            }
            getBlogs()
        } catch (e: Exception) {
            BlogResult.Error(e)
        }
    }

    suspend fun deleteBlog(id: String): BlogResult {
        return try {
            supabase.from("blogs").delete {
                filter { eq("id", id) }
            }
            getBlogs()
        } catch (e: Exception) {
            BlogResult.Error(e)
        }
    }
}
