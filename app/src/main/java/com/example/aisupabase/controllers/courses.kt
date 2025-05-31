package com.example.aisupabase.controllers

import courses
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

sealed class CourseResult {
    data class Success(val data: List<courses>?) : CourseResult()
    data class Error(val exception: Throwable) : CourseResult()
}

class CourseRepository(private val supabase: SupabaseClient) {

    suspend fun getCourses(): CourseResult {
        return try {
            val result = supabase.from("courses").select().decodeList<courses>()
            CourseResult.Success(result)
        } catch (e: Exception) {
            CourseResult.Error(e)
        }
    }

    suspend fun addCourse(
        title_course: String,
        des_course: String,
        lession_total: Int,
        public_id_image: String,
        url_image: String,
        is_private: Boolean,
        user_create: Int
    ): CourseResult {
        return try {
            val data = mapOf(
                "title_course" to title_course,
                "des_course" to des_course,
                "lession_total" to lession_total,
                "public_id_image" to public_id_image,
                "url_image" to url_image,
                "is_private" to is_private,
                "user_create" to user_create
            )
            supabase.from("courses").insert(data)
            getCourses()
        } catch (e: Exception) {
            CourseResult.Error(e)
        }
    }

    suspend fun updateCourse(
        id: Int,
        title_course: String,
        des_course: String,
        lession_total: Int,
        public_id_image: String,
        url_image: String,
        is_private: Boolean,
        user_create: Int
    ): CourseResult {
        return try {
            val data = mapOf(
                "title_course" to title_course,
                "des_course" to des_course,
                "lession_total" to lession_total,
                "public_id_image" to public_id_image,
                "url_image" to url_image,
                "is_private" to is_private,
                "user_create" to user_create
            )
            supabase.from("courses").update(data) {
                filter { eq("id", id) }
            }
            getCourses()
        } catch (e: Exception) {
            CourseResult.Error(e)
        }
    }

    suspend fun deleteCourse(id: Int): CourseResult {
        return try {
            supabase.from("courses").delete {
                filter { eq("id", id) }
            }
            getCourses()
        } catch (e: Exception) {
            CourseResult.Error(e)
        }
    }
}
