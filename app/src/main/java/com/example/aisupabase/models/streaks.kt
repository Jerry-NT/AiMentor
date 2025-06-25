package com.example.aisupabase.models

import kotlinx.serialization.Serializable

@Serializable
data class streaks (
    val id: Int? = null,
    val count:Int,
    val id_user:Int,
    val created_at: String
)