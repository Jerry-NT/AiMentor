package com.example.aisupabase.controllers

import course_roadmaps
import courses
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Result wrapper for CRUD operations
sealed class RoadMapResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : RoadMapResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : RoadMapResult<Nothing>()
}

// 2. Repository
class RoadMapRepository(private val supabase: SupabaseClient) {
    // Lấy danh sách RoadMaps
    suspend fun getRoadMaps(): RoadMapResult<List<course_roadmaps>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["course_roadmaps"].select()
            val RoadMaps = result.decodeList<course_roadmaps>()
            return@withContext RoadMapResult.Success(RoadMaps, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext RoadMapResult.Error(e)
        }
    }

    // Xóa theo id
    suspend fun deleteRoadMap(id: Int): RoadMapResult<Unit> = withContext(Dispatchers.IO) {
        try {
            // xoa course
           val courseList = supabase.postgrest["courses"]
                .select {
                    filter {
                        eq("id_roadmap", id)
                    }
                }.decodeList<courses>()
            courseList.forEach { course ->
                CourseRepository(supabase).deleteCourse(course.id?:0)
            }

            // Xóa roadmap theo id
            val result = supabase.postgrest["course_roadmaps"]
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            return@withContext RoadMapResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext RoadMapResult.Error(e)
        }
    }

    // Cập nhật theo id
    suspend fun updateRoadMap(id: Int, title: String): RoadMapResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["course_roadmaps"]
                .update({
                    set("title", title)
                }) {
                    filter {
                        eq("id", id)
                    }
                }
            return@withContext RoadMapResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext RoadMapResult.Error(e)
        }
    }

    // Thêm mới
    suspend fun addRoadMap(title: String): RoadMapResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["course_roadmaps"]
                .insert(mapOf("title" to title))
            return@withContext RoadMapResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext RoadMapResult.Error(e)
        }
    }

    suspend fun checkRoadMapExists(title: String,case: String = "update", id: Int? = null):Boolean = withContext(Dispatchers.IO) {
        try {
            val existingRoadMap = supabase.postgrest["course_roadmaps"]
                .select {
                    filter {
                        ilike("title", title)
                    }
                }.decodeList<course_roadmaps>()
            return@withContext when (case) {
                "update" -> {
                    existingRoadMap.any { it.id != id }
                }
                else -> {
                    existingRoadMap.isNotEmpty()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}



