package com.example.aisupabase.controllers

import courses
import lessons
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.from

sealed class LessonResult {
    data class Success(val data: List<lessons>?) : LessonResult()
    data class Error(val exception: Throwable) : LessonResult()
}

class LessonRepository(private val supabase: SupabaseClient) {

    suspend fun getLessons(): LessonResult {
        return try {
            val result = supabase.from("lessons").select().decodeList<lessons>()
            LessonResult.Success(result)
        } catch (e: Exception) {
            LessonResult.Error(e)
        }
    }

    suspend fun addLesson(
        id_course: Int,
        title_lession: String,
        content_lession: String,
        duration: String
    ): LessonResult {
        return try {
            val data = mapOf(
                "id_course" to id_course,
                "title_lession" to title_lession,
                "content_lession" to content_lession,
                "duration" to duration
            )
            supabase.from("lessons").insert(data)
            getLessons()
        } catch (e: Exception) {
            LessonResult.Error(e)
        }
    }

    suspend fun updateLesson(
        id: Int,
        id_course: Int,
        title_lession: String,
        content_lession: String,
        duration: String
    ): LessonResult {
        return try {
            val data = mapOf(
                "id_course" to id_course,
                "title_lession" to title_lession,
                "content_lession" to content_lession,
                "duration" to duration
            )
            supabase.from("lessons").update(data) {
                filter { eq("id", id) }
            }
            getLessons()
        } catch (e: Exception) {
            LessonResult.Error(e)
        }
    }

    suspend fun deleteLesson(id: Int): LessonResult {
        return try {
            supabase.from("lessons").delete {
                filter { eq("id", id) }
            }
            getLessons()
        } catch (e: Exception) {
            LessonResult.Error(e)
        }
    }


}
