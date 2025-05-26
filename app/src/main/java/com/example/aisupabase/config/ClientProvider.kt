package com.example.aisupabase.config

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
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
    val cloudinary: Cloudinary by lazy {
        Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", NativeKeys.CloudinaryCloudName(),
                "api_key", NativeKeys.CloudinaryKey(),
                "api_secret", NativeKeys.CloudinarySecret()
            )
        )
    }
}
