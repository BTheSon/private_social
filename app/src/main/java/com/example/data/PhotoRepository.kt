package com.example.data

import android.util.Log
import java.io.File
import kotlinx.coroutines.flow.Flow

class PhotoRepository(private val photoDao: PhotoDao) {
    val allPhotos: Flow<List<PhotoEntity>> = photoDao.getAllPhotos()

    suspend fun insertPhoto(photo: PhotoEntity): Long {
        return photoDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: PhotoEntity) {
        try {
            val file = File(photo.filePath)
            if (file.exists()) {
                val deleted = file.delete()
                Log.d("PhotoRepository", "Physical file deleted: $deleted, path: ${photo.filePath}")
            } else {
                Log.d("PhotoRepository", "File does not exist: ${photo.filePath}")
            }
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error deleting physical file", e)
        }
        photoDao.deletePhoto(photo)
    }
}
