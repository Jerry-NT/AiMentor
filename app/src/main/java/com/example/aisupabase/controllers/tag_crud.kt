package com.example.aisupabase.controllers

import com.example.aisupabase.models.Tags
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 2. Repository
class TagRepository(private val supabase: SupabaseClient) {
//lay
    suspend fun getTags(): List<Tags> = withContext(Dispatchers.IO) {
        try {
            return@withContext supabase.postgrest["tag"]
                .select()
                .decodeList<Tags>()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
//xoa_id
    suspend fun deleteTag(id: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["tag"]
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
//update_id
    suspend fun updateTag(id: String, title: String) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["tag"]
                .update({
                    set("title_tag", title)
                }) {
                    filter {
                        eq("id", id)
                    }
                }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
//them
    suspend fun addTag(title: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["tag"]
                .insert(mapOf("title_tag" to title))

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

}