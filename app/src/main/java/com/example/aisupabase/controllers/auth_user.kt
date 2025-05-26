package com.example.aisupabase.controllers

import android.content.Context
import com.example.aisupabase.config.SupabaseClientProvider

public class authUser{
    private val client = SupabaseClientProvider.client

    fun saveUserSession(
        context: Context,
        username: String,
        email: String,
        id: Int?,
        indexImage: Int,
        role: String,
        typeAccount: String
    ) {
        val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("username", username)
            putString("email", email)
            putInt("id", id ?: -1)
            putInt("index_image", indexImage)
            putString("role", role)
            putString("type_account", typeAccount)
            apply()
        }
    }

    fun getUserSession(context: Context): Map<String, Any?> {
        val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return mapOf(
            "username" to sharedPref.getString("username", null),
            "email" to sharedPref.getString("email", null),
            "id" to if (sharedPref.contains("id")) sharedPref.getInt("id", -1) else null,
            "index_image" to if (sharedPref.contains("index_image")) sharedPref.getInt("index_image", -1) else null,
            "role" to sharedPref.getString("role", null),
            "type_account" to sharedPref.getString("type_account", null)
        )
    }

    fun clearUserSession(context: Context) {
        val sharedPref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }
}
