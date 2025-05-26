package com.example.aisupabase.cloudinary

import com.cloudinary.utils.ObjectUtils

import com.example.aisupabase.config.CloudinaryClientProvider
import java.io.File

object CloudinaryService {
    private val cloudinary = CloudinaryClientProvider.cloudinary

    // Upload file
    fun uploadFile(file: File, folder: String? = null): Map<*, *> {
        val options = if (folder != null) ObjectUtils.asMap("folder", folder) else ObjectUtils.emptyMap()
        return cloudinary.uploader().upload(file, options)
    }

    // Get file info by publicId
    fun getFile(publicId: String): Map<*, *> {
        return cloudinary.api().resource(publicId, ObjectUtils.emptyMap())
    }

    // Delete file
    fun deleteFile(publicId: String): Map<*, *> {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
    }
}
