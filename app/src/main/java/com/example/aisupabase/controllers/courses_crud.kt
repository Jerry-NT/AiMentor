package com.example.aisupabase.controllers

import comment_course
import courses
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock.System.now
import lessons

sealed class CourseResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : CourseResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : CourseResult<Nothing>()
}

class CourseRepository(private val supabase: SupabaseClient) {

    // Lấy danh sách khóa học
    suspend fun getCourses(): CourseResult<List<courses>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("courses").select()
            val coursesList = result.decodeList<courses>()
            return@withContext CourseResult.Success(coursesList, result)
        } catch (e: Exception) {
            return@withContext CourseResult.Error(e)
        }
    }

    // lay ra 4 khóa học mới nhất
    suspend fun getLatestCourses(): CourseResult<List<courses>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("courses").select()
            val coursesList = result.decodeList<courses>().sortedByDescending { it.created_at }
            val latestCourses = coursesList.take(4)
            return@withContext CourseResult.Success(latestCourses, result)
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
                    courses(null,title_course, des_course, public_id_image,url_image, is_private,id_roadmap, user_create, now().toString())
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
        id_roadmap: Int,
        created_at:String

    ): CourseResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["courses"]
                .update(
                    courses(id, title_course, des_course, public_id_image, url_image, is_private = false, id_roadmap, user_create, created_at)
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
            // comment_course , user_course, lesson_course, roadmap_course
            // Xóa các bình luận liên quan đến khóa học
            val comments = supabase.postgrest["comment_course"].select{
                filter { eq("id_course", id) }
            }.decodeList<comment_course>()
            comments.forEach { comment ->
                    supabase.from("comment_course").delete {
                        filter { eq("id", comment.id?:0) }
                }
            }

            // Xóa nguoi dung dang ky khóa học
            val userCourses = supabase.postgrest["user_course"].select{
                filter { eq("id_course", id) }
            }.decodeList<courses>()
            userCourses.forEach { userCourse ->
                    supabase.from("user_course").delete {
                        filter { eq("id", userCourse.id ?: 0) }
                }
            }

            // Xóa các bài học liên quan đến khóa học // goi ham xoa lesson tu lesson_crud.kt
            val lessonCourses = supabase.postgrest["lesson_course"].select{
                filter { eq("id_course", id) }
            }.decodeList<lessons>()
            lessonCourses.forEach { lessonCourse ->
                    LessonRepository(supabase).deleteLesson(id)
            }

            val result = supabase.from("courses").delete {
                filter { eq("id", id) }
            }
            return@withContext CourseResult.Success(Unit, result)
        } catch (e: Exception) {
            return@withContext CourseResult.Error(e)
        }
    }

    // ham tim kiem theo tieu de va noi dung
    suspend fun searchCourse(query: String): CourseResult<List<courses>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("courses").select()
            val coursesList = result.decodeList<courses>()
                .filter {
                    it.title_course.contains(query, ignoreCase = true) ||
                            it.des_course.contains(query, ignoreCase = true)
                }
            return@withContext CourseResult.Success(coursesList, result)
        } catch (e: Exception) {
            return@withContext CourseResult.Error(e)
        }
    }

    suspend fun checkCourseExist(title_course: String, case: String = "update", id: Int? = null): Boolean = withContext(Dispatchers.IO){
        try {
            val existingCourse = supabase.postgrest["courses"]
                .select {
                    filter {
                        ilike("title_course", title_course)
                    }
                }.decodeList<courses>()

            return@withContext when (case) {
                "update" -> {
                    existingCourse.any { it.id != id }
                }
                else -> {
                    existingCourse.isNotEmpty()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
