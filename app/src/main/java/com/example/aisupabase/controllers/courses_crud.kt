//package com.example.aisupabase.controllers
//
//import courses
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.postgrest.from
//
//sealed class CourseResult {
//    data class Success(val data: List<courses>?) : CourseResult()
//    data class Error(val exception: Throwable) : CourseResult()
//}
//
//class CourseRepository(private val supabase: SupabaseClient) {
//
//    suspend fun getCourses(): CourseResult {
//        return try {
//            val result = supabase.from("courses").select().decodeList<courses>()
//            CourseResult.Success(result)
//        } catch (e: Exception) {
//            CourseResult.Error(e)
//        }
//    }
//
//    suspend fun addCourse(
//        title_course: String,
//        des_course: String,
//        public_id_image: String,
//        url_image: String,
//        is_private: Boolean,
//        user_create: Int
//    ): CourseResult {
//        return try {
//            val data = mapOf(
//                "title_course" to title_course,
//                "des_course" to des_course,
//                "public_id_image" to public_id_image,
//                "url_image" to url_image,
//                "is_private" to is_private,
//                "user_create" to user_create
//            )
//            supabase.from("courses").insert(data)
//            getCourses()
//        } catch (e: Exception) {
//            CourseResult.Error(e)
//        }
//    }
//
//    suspend fun updateCourse(
//        id: Int,
//        title_course: String,
//        des_course: String,
//        public_id_image: String,
//        url_image: String,
//    ): CourseResult {
//        return try {
//            val data = mapOf(
//                "title_course" to title_course,
//                "des_course" to des_course,
//                "public_id_image" to public_id_image,
//                "url_image" to url_image
//            )
//            supabase.from("courses").update(data) {
//                filter { eq("id", id) }
//            }
//            getCourses()
//        } catch (e: Exception) {
//            CourseResult.Error(e)
//        }
//    }
//
//    suspend fun deleteCourse(id: Int): CourseResult {
//        return try {
//            supabase.from("courses").delete {
//                filter { eq("id", id) }
//            }
//            getCourses()
//        } catch (e: Exception) {
//            CourseResult.Error(e)
//        }
//    }
//}


package com.example.aisupabase.controllers

import courses
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        user_create: Int
    ): CourseResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "title_course" to title_course,
                "des_course" to des_course,
                "public_id_image" to public_id_image,
                "url_image" to url_image,
                "is_private" to is_private,
                "user_create" to user_create
            )
            val result = supabase.from("courses").insert(data)
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
        url_image: String
    ): CourseResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "title_course" to title_course,
                "des_course" to des_course,
                "public_id_image" to public_id_image,
                "url_image" to url_image
            )
            val result = supabase.from("courses").update(data) {
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

    suspend fun getAllCourses(supabase: SupabaseClient): List<courses> {
        return supabase.postgrest["courses"]
            .select()
            .decodeList<courses>()
    }

}
