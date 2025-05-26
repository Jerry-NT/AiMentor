package com.example.aisupabase.models

import kotlinx.serialization.Serializable

@Serializable
data class Tags(
    val id: Int,
    val title_tag: String
)
