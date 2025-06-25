package com.example.aisupabase.config

import com.example.aisupabase.models.Content
import com.example.aisupabase.models.GeminiRequest
import com.example.aisupabase.models.GeminiResponse
import com.example.aisupabase.models.Part
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class GeminiService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun generateText(prompt: String): String {
        val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent") {
            contentType(ContentType.Application.Json)
            parameter("key", getGeminiKey.returnkey())
            setBody(
                GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = prompt)
                            )
                        )
                    )
                )
            )
        }.body()

        return response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "Không có phản hồi"
    }
}