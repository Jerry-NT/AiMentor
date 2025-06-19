package com.example.aisupabase.config

import com.cloudinary.Cloudinary
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest



object NativeKeys {
    init {
        System.loadLibrary("aisupabase")
    }
    external fun SupabaseKey(): String
    external fun SupabaseUrl(): String
    external fun CloudinaryKey(): String
    external fun CloudinarySecret(): String
    external fun CloudinaryCloudName(): String
}

object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = NativeKeys.SupabaseUrl(),
        supabaseKey = NativeKeys.SupabaseKey()
    ) {
        install(Auth)
        install(Postgrest)
    }
}

object CloudinaryClientProvider {
    val config: HashMap<String, String> = hashMapOf(
        "cloud_name" to  NativeKeys.CloudinaryCloudName(),
        "api_key" to NativeKeys.CloudinaryKey(),
        "api_secret" to NativeKeys.CloudinarySecret()
    )

    fun getCloudinaryInstance() = Cloudinary(config)
}

