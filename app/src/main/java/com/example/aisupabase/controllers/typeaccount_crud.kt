package com.example.aisupabase.controllers

import com.example.aisupabase.models.type_accounts
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Result wrapper for CRUD operations
sealed class TypeAccountResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : TypeAccountResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : TypeAccountResult<Nothing>()
}

// 2. Repository
class TypeAccountRepository(private val supabase: SupabaseClient) {
    // Lấy danh sách TypeAccounts
    suspend fun getTypeAccounts(): TypeAccountResult<List<type_accounts>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["type_accounts"].select()
            val TypeAccounts = result.decodeList<type_accounts>()
            return@withContext TypeAccountResult.Success(TypeAccounts, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext TypeAccountResult.Error(e)
        }
    }

    // Xóa theo id
    suspend fun deleteTypeAccount(id: String): TypeAccountResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["type_accounts"]
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            return@withContext TypeAccountResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext TypeAccountResult.Error(e)
        }
    }

    // Cập nhật theo id
    suspend fun updateTypeAccount(id: String, type: String,des:String,max_course: Int, price: Double): TypeAccountResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["type_accounts"]
                .update(
                    mapOf(
                        "type" to type,
                        "description" to des,
                        "max_course" to max_course,
                        "price" to price
                    )
                )  {
                    filter {
                        eq("id", id)
                    }
                }
            return@withContext TypeAccountResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext TypeAccountResult.Error(e)
        }
    }

    // Thêm mới
    suspend fun addTypeAccount(type: String,des:String,max_course: Int, price: Double): TypeAccountResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["type_accounts"]
                .insert(mapOf(
                    "type" to type,
                    "des" to des,
                    "max_course" to max_course,
                    "price" to price

                ))
            return@withContext TypeAccountResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext TypeAccountResult.Error(e)
        }
    }
}

