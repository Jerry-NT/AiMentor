package com.example.aisupabase.models

import kotlinx.serialization.Serializable
@Serializable
enum class UserRole {
    client,admin
}
@Serializable
data class Users(
    val id: Int? = null,
    val username: String,
    val email: String,
    val phone:String,
    val index_image: Int,
    val id_type_account: Int,
    val role: UserRole
)
