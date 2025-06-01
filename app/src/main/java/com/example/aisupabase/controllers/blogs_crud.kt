//package com.example.aisupabase.controllers
//
//import blogs
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.postgrest.from
//
//sealed class BlogResult {
//    data class Success(val data: List<blogs>?) : BlogResult()
//    data class Error(val exception: Throwable) : BlogResult()
//}
//
//class BlogRepository(private val supabase: SupabaseClient) {
//
//    suspend fun getBlogs(): BlogResult {
//        return try {
//            val result = supabase.from("blogs").select().decodeList<blogs>()
//            BlogResult.Success(result)
//        } catch (e: Exception) {
//            BlogResult.Error(e)
//        }
//    }
//
//    suspend fun addBlog(
//        title_blog: String,
//        public_id_image: String,
//        url_image: String,
//        id_tag: Int,
//        content_blog: String
//    ): BlogResult {
//        return try {
//            val data = mapOf(
//                "title_blog" to title_blog,
//                "public_id_image" to public_id_image,
//                "url_image" to url_image,
//                "id_tag" to id_tag,
//                "content_blog" to content_blog
//            )
//            supabase.from("blogs").insert(data)
//            getBlogs()
//        } catch (e: Exception) {
//            BlogResult.Error(e)
//        }
//    }
//
//    suspend fun updateBlog(
//        id: String,
//        title_blog: String,
//        public_id_image: String,
//        url_image: String,
//        id_tag: Int,
//        content_blog: String
//    ): BlogResult {
//        return try {
//            val data = mapOf(
//                "title_blog" to title_blog,
//                "public_id_image" to public_id_image,
//                "url_image" to url_image,
//                "id_tag" to id_tag,
//                "content_blog" to content_blog
//            )
//            supabase.from("blogs").update(data) {
//                filter { eq("id", id) }
//            }
//            getBlogs()
//        } catch (e: Exception) {
//            BlogResult.Error(e)
//        }
//    }
//
//    suspend fun deleteBlog(id: String): BlogResult {
//        return try {
//            supabase.from("blogs").delete {
//                filter { eq("id", id) }
//            }
//            getBlogs()
//        } catch (e: Exception) {
//            BlogResult.Error(e)
//        }
//    }
//}
package com.example.aisupabase.controllers

import blogs
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class BlogResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : BlogResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : BlogResult<Nothing>()
}

class BlogRepository(private val supabase: SupabaseClient) {

    suspend fun getBlogs(): BlogResult<List<blogs>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("blogs").select()
            val blogsList = result.decodeList<blogs>()
            return@withContext BlogResult.Success(blogsList, result)
        } catch (e: Exception) {
            return@withContext BlogResult.Error(e)
        }
    }

    suspend fun addBlog(
        title_blog: String,
        public_id_image: String,
        url_image: String,
        id_tag: Int,
        content_blog: String
    ): BlogResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "title_blog" to title_blog,
                "public_id_image" to public_id_image,
                "url_image" to url_image,
                "id_tag" to id_tag,
                "content_blog" to content_blog
            )
            val result = supabase.from("blogs").insert(data)
            return@withContext BlogResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext BlogResult.Error(e)
        }
    }

    suspend fun updateBlog(
        id: String,
        title_blog: String,
        public_id_image: String,
        url_image: String,
        id_tag: Int,
        content_blog: String
    ): BlogResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "title_blog" to title_blog,
                "public_id_image" to public_id_image,
                "url_image" to url_image,
                "id_tag" to id_tag,
                "content_blog" to content_blog
            )
            val result = supabase.from("blogs").update(data) {
                filter { eq("id", id) }
            }
            return@withContext BlogResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext BlogResult.Error(e)
        }
    }

    suspend fun deleteBlog(id: String): BlogResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("blogs").delete {
                filter { eq("id", id) }
            }
            return@withContext BlogResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext BlogResult.Error(e)
        }
    }
}
