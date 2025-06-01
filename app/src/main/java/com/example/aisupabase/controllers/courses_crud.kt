package com.example.aisupabase.controllers

import android.R.id
import courses
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock.System.now
import kotlin.toString

sealed class CourseResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : CourseResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : CourseResult<Nothing>()
}

class CourseRepository(private val supabase: SupabaseClient) {

    suspend fun getCourses(): CourseResult<List<courses>> = withContext(Dispatchers.IO) {
        try {

            val result = supabase.from("courses").select()
            val coursesList = result.decodeList<courses>()
            return@withContext CourseResult.Success(coursesList, result)
        } catch (e: Exception) {
            return@withContext CourseResult.Error(e)
        }
    }

    suspend fun addCourse(
        title_course: String,
        des_course: String,
        public_id_image: String,
        url_image: String,
        is_private: Boolean,
        user_create: Int,
        id_roadmap: Int
    ): CourseResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["courses"]
                .insert(
                    courses(null,title_course, des_course, url_image, public_id_image, is_private,id_roadmap, user_create, now().toString())
                )
            return@withContext CourseResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext CourseResult.Error(e)
        }
    }

    suspend fun updateCourse(
        id: Int,
        title_course: String,
        des_course: String,
        public_id_image: String,
        url_image: String,
        user_create: Int,
        id_roadmap: Int

    ): CourseResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["courses"]
                .update(
                    courses(id, title_course, des_course, public_id_image, url_image, is_private = false, id_roadmap, user_create, now().toString())
                ){
                    filter { eq("id", id) }
                }
            return@withContext CourseResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext CourseResult.Error(e)
        }
    }

    suspend fun deleteCourse(id: Int): CourseResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("courses").delete {
                filter { eq("id", id) }
            }
            return@withContext CourseResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext CourseResult.Error(e)
        }
    }

}
