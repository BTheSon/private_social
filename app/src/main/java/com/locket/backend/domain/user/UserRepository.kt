package com.locket.backend.domain.user

import com.google.firebase.database.FirebaseDatabase
import com.locket.backend.service.SupabaseClientService
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class UserRepository(private val userDao: UserDao) {

    suspend fun saveUser(user: UserEntity) {
        userDao.clearUserFlag()
        userDao.insertUser(user.copy(isMe = true))
    }

    suspend fun getMyProfile(): UserEntity? {
        return userDao.getUser()
    }

    fun getMyProfileFlow() = userDao.getUserFlow()

    suspend fun clearMyProfile() {
        userDao.clearUserFlag() // Hàm này đã được khai báo ở UserDao trong các bước trước
    }

    suspend fun uploadAvatarToSupabase(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "${UUID.randomUUID()}.jpg"
            // Lưu ý: Cần tạo một bucket tên là "avatars" trên trang quản trị Supabase
            val bucket = SupabaseClientService.client.storage.from("avatars")

            bucket.upload(fileName, bytes)
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 2. Hàm cập nhật thông tin cá nhân (Đồng bộ Firebase & Room)
    suspend fun updateProfile(displayName: String?, avatarUrl: String?) = withContext(Dispatchers.IO) {
        val myProfile = getMyProfile() ?: return@withContext
        val phone = myProfile.phoneNumber

        val updates = mutableMapOf<String, Any>()
        if (displayName != null) updates["displayName"] = displayName
        if (avatarUrl != null) updates["avatarUrl"] = avatarUrl

        try {
            // Cập nhật lên Firebase Realtime Database
            FirebaseDatabase.getInstance().reference.child("users").child(phone).updateChildren(updates).await()

            // Cập nhật xuống Room Database nội bộ để UI tự thay đổi ngay lập tức
            val updatedUser = myProfile.copy(
                displayName = displayName ?: myProfile.displayName,
                avatarUrl = avatarUrl ?: myProfile.avatarUrl
            )
            userDao.insertUser(updatedUser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}