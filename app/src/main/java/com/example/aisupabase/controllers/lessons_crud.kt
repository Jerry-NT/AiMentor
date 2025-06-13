

package com.example.aisupabase.controllers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lessons
import user_lesson
import io.github.jan.supabase.postgrest.query.Order
sealed class LessonResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : LessonResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : LessonResult<Nothing>()
}

class LessonRepository(private val supabase: SupabaseClient) {

    suspend fun getLessons(): LessonResult<List<lessons>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("lessons").select{
                order("id",order = Order.ASCENDING)
            }
            val lessonsList = result.decodeList<lessons>()
            return@withContext LessonResult.Success(lessonsList, result)
        } catch (e: Exception) {
            return@withContext LessonResult.Error(e)
        }
    }

    suspend fun getLessonsByIdCourse(id:Int): LessonResult<List<lessons>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("lessons").select {
            filter { eq("id_course", id) }
                order("id",order = Order.ASCENDING)
            }

            val lessonsList = result.decodeList<lessons>()
            return@withContext LessonResult.Success(lessonsList, result)
        } catch (e: Exception) {
            return@withContext LessonResult.Error(e)
        }
    }

    suspend fun getLessonsByID(id:Int): LessonResult<List<lessons>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("lessons").select{
                filter { eq("id",id) }
                order("id",order = Order.ASCENDING)
            }
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
        duration: Int
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
        duration: Int
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
            // lay danh sach user _ lesson theo id lesson
            val userLessons = supabase.from("user_lesson").select().decodeList<user_lesson>()
            userLessons.forEach { userLesson ->
                // xoa user_lesson theo id lesson
                if (userLesson.id_lesson == id) {
                    supabase.from("user_lesson").delete {
                        filter { eq("id", userLesson.id?:0) }
                    }
                }
            }
            val result = supabase.from("lessons").delete {
                filter { eq("id", id) }
            }
            return@withContext LessonResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext LessonResult.Error(e)
        }
    }

// chi tinh tren course
    suspend fun checkLessonExists(title_lesson: String, case: String = "update", id: Int? = null,id_course: Int):Boolean = withContext(Dispatchers.IO) {
        try {
            val existingLesson = supabase.postgrest["lessons"]
                .select {
                    filter {
                        ilike("title_lesson", title_lesson)
                        eq("id_course",id_course)
                    }
            }.decodeList<lessons>()

            return@withContext when (case) {
                "update" -> {
                    existingLesson.any { it.id != id}
                }
                else -> {
                    existingLesson.isNotEmpty()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
