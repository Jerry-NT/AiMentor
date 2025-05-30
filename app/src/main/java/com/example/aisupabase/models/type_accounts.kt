package com.example.aisupabase.models

import kotlinx.serialization.Serializable

@Serializable
data class type_accounts(
    val id: Int,
    val type : String,
    val des: String,
    val max_course: Int,
    val price : Double
)
