package com.example.aisupabase.cloudinary

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.aisupabase.config.CloudinaryClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

object CloudinaryService {

    private val cloudinary = CloudinaryClientProvider.getCloudinaryInstance()

    // Upload ảnh
    suspend fun uploadImage(file: File): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
            result["secure_url"] as? String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Xoá ảnh (theo public_id)
    suspend fun deleteImage(publicId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
            result["result"] == "ok"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
