package com.example.aisupabase.controllers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lessons
import user_course
import user_lesson
import io.github.jan.supabase.postgrest.query.Order
sealed class LearnResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : LearnResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : LearnResult<Nothing>()
}

class LearnRepository(private val supabase: SupabaseClient){

    // dang ky khoa hoc ? id_user - id_course ?
    suspend fun SubCourse(id_user:Int,id_course:Int): LearnResult<Unit> = withContext(Dispatchers.IO){
        try {
            val result = supabase.postgrest["user_course"]
                .insert(
                    user_course(null,id_user,id_course)
                )
            return@withContext LearnResult.Success(Unit,result)
        }catch (e: Exception) {
            return@withContext LearnResult.Error(e)
        }
    }

    // kiem tra xem da dki chua ? id_user - id_course
    suspend fun checkSub(id_user: Int,id_course: Int): Boolean = withContext(Dispatchers.IO){
        try {
            val result = supabase.postgrest["user_course"]
                .select {
                    filter {
                        eq("id_user", id_user)
                        eq("id_course", id_course)
                    }
                }
                .decodeList<user_course>()
            return@withContext result.isNotEmpty()
        }catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    // size ? sluong hoc vien da dang ky khoa hoc id_course
    suspend fun countByCourse(id_course: Int): Int = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["user_course"]
                .select {
                    filter { eq("id_course", id_course) }
                }
                .decodeList<user_course>()
            return@withContext result.size
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0
        }
    }

    suspend fun continueLesson(id_user: Int, id_course: Int):lessons? = withContext(Dispatchers.IO) {
        try {
            val lessonsList = supabase.postgrest["lessons"].select {
                filter { eq("id_course", id_course) }
                order("id",order = Order.ASCENDING)
            }
                .decodeList<lessons>()
            // lọc lesson - user_lesson(id_lesson, id_user,checked)
            // kiểm tra xem user_lesson so với lesson đã có bao nhiêu ? -> vd lessons[1,2,3,4], ở user_lesson có [1] thì trả id_lesson 2
            val userLessons = supabase.postgrest["user_lesson"].select {
                filter {
                    eq("id_user", id_user)
                }
            }.decodeList<user_lesson>()

            val completedLessonIds = userLessons.filter { it.checked == true }.map { it.id_lesson }
            return@withContext lessonsList.firstOrNull { it.id !in completedLessonIds }
        }catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun completeLesson(id_user: Int, id_lesson: Int): LearnResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["user_lesson"]
                .insert(user_lesson(null, id_user,id_lesson, true)) // dùng upsert thay insert
            return@withContext LearnResult.Success(Unit, result)
         } catch (e: Exception) {
             e.printStackTrace()
            return@withContext LearnResult.Error(e)
        }
    }

    suspend fun checkLessonCompleted(id_user: Int, id_lesson: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["user_lesson"]
                .select {
                    filter {
                      eq("id_user", id_user)
                     eq("id_lesson", id_lesson)
                        eq("checked", true)
                }
            }
            .decodeList<user_lesson>()
        return@withContext result.isNotEmpty()
    } catch (e: Exception) {
        return@withContext false
    }
    }

    suspend fun getCountSub(id_course: Int): Int = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["user_course"]
                .select {
                    filter { eq("id_course", id_course) }
                }
                .decodeList<user_course>()
            return@withContext result.size
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0
        }
    }
}