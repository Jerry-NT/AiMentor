package com.example.aisupabase.models

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long
)