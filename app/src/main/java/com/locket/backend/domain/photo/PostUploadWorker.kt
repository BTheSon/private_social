package com.locket.backend.domain.photo

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.locket.backend.domain.database.MDatabase
import com.locket.backend.domain.database.DraftEntity
import com.locket.backend.domain.photo.PhotoEntity
import com.locket.backend.domain.post.PostModel
import com.locket.backend.domain.post.PostRepository
import com.locket.backend.service.FirebaseClientService
import kotlinx.coroutines.tasks.await
import java.io.File

class PostUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val draftId = inputData.getString(KEY_DRAFT_ID) ?: return Result.failure()

        val database = MDatabase.getDatabase(applicationContext)
        val draftDao = database.draftDao()
        val photoDao = database.photoDao()
        val postRepository = PostRepository()

        val draft = draftDao.getDraftById(draftId) ?: return Result.success()

        // Nếu file không tồn tại, đánh dấu lỗi và không retry nữa
        val file = File(draft.imageUri)
        if (!file.exists()) {
            Log.e("PostUploadWorker", "File not found: ${draft.imageUri}")
            draftDao.saveDraft(draft.copy(status = "FAILED"))
            return Result.failure()
        }

        try {
            // 1. Chuyển trạng thái sang UPLOADING trên UI
            draftDao.saveDraft(draft.copy(status = "UPLOADING"))

            // 2. Upload ảnh lên Supabase
            val bytes = file.readBytes()
            val imageUrl = postRepository.uploadImageToSupabase(bytes)
            
            if (imageUrl.isNullOrEmpty()) {
                throw Exception("Supabase upload returned null")
            }

            // 3. Lấy thông tin user hiện tại
            val currentUser = FirebaseClientService.auth.currentUser
            val currentUserId = currentUser?.phoneNumber ?: currentUser?.uid ?: "unknown"
            var currentUserName = "User"
            var currentUserAvatar = ""

            try {
                val snapshot = FirebaseClientService.database.reference.child("users").child(currentUserId).get().await()
                if (snapshot.exists()) {
                    currentUserName = snapshot.child("displayName").getValue(String::class.java) ?: "User"
                    currentUserAvatar = snapshot.child("avatarUrl").getValue(String::class.java) ?: ""
                }
            } catch (e: Exception) {
                Log.w("PostUploadWorker", "Could not fetch user profile", e)
            }

            // 4. Lưu bài đăng lên Firebase
            val post = PostModel(
                userId = currentUserId,
                imageUrl = imageUrl,
                caption = draft.caption,
                authorName = currentUserName,
                authorAvatar = currentUserAvatar,
                songName = draft.songName,
                artistName = draft.artistName,
                previewUrl = draft.previewUrl
            )

            val isSaved = postRepository.savePost(post)
            if (isSaved) {
                // Thành công: Xóa bản nháp, thêm vào thư viện ảnh cục bộ
                draftDao.deleteDraft(draftId)
                photoDao.insertPhoto(PhotoEntity(filePath = file.absolutePath))
                Log.d("PostUploadWorker", "Upload thành công bản nháp: $draftId")
                return Result.success()
            } else {
                throw Exception("Firebase RTDB savePost failed")
            }
        } catch (e: Exception) {
            Log.e("PostUploadWorker", "Lỗi upload bản nháp: ${e.message}", e)
            // Đánh dấu lỗi trên DB để hiển thị UI
            draftDao.saveDraft(draft.copy(status = "FAILED"))
            // Trả về retry để WorkManager tự động chạy lại khi có mạng
            return Result.retry()
        }
    }

    companion object {
        const val KEY_DRAFT_ID = "DRAFT_ID"
    }
}
