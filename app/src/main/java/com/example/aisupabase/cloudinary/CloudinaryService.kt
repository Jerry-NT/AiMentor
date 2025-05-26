package com.example.aisupabase.cloudinary

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.aisupabase.config.CloudinaryClientProvider
import java.io.File

object CloudinaryService {
    private val cloudinary: Cloudinary = CloudinaryClientProvider.getCloudinaryInstance()

    // Upload ảnh (có thể truyền file hoặc đường dẫn file)
    fun uploadImage(file: File, folder: String = "default_folder"): String? {
        return try {
            val uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                "folder", folder
            ))
            uploadResult["secure_url"] as String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Xoá ảnh theo publicId
    fun deleteImage(publicId: String): Boolean {
        return try {
            val result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
            "ok" == result["result"]
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
