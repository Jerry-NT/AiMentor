//package com.example.aisupabase.controllers
//
//import lessons
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.postgrest.from
//
//sealed class LessonResult {
//    data class Success(val data: List<lessons>?) : LessonResult()
//    data class Error(val exception: Throwable) : LessonResult()

package com.example.aisupabase.controllers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lessons

sealed class LessonResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : LessonResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : LessonResult<Nothing>()
}

class LessonRepository(private val supabase: SupabaseClient) {

    suspend fun getLessons(): LessonResult<List<lessons>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("lessons").select()
            val lessonsList = result.decodeList<lessons>()
            return@withContext LessonResult.Success(lessonsList, result)
        } catch (e: Exception) {
            return@withContext LessonResult.Error(e)
        }
    }

    suspend fun addLesson(
        id_course: Int,
        title_lesson: String,
        content_lesson: String,
        duration: String
    ): LessonResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["lessons"]
                .insert(
                    lessons(null, id_course, title_lesson, content_lesson, duration)
                )
            return@withContext LessonResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext LessonResult.Error(e)
        }
    }

    suspend fun updateLesson(
        id: Int,
        id_course: Int,
        title_lesson: String,
        content_lesson: String,
        duration: String
    ): LessonResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["lessons"]
                .update(
                    lessons(id, id_course, title_lesson, content_lesson, duration)
                ){
                filter { eq("id", id) }
            }
            return@withContext LessonResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext LessonResult.Error(e)
        }
    }

    suspend fun deleteLesson(id: Int): LessonResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("lessons").delete {
                filter { eq("id", id) }
            }
            return@withContext LessonResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext LessonResult.Error(e)
        }
    }
}
