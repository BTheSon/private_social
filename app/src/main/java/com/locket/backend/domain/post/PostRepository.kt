package com.locket.backend.domain.post

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import io.github.jan.supabase.storage.storage
import com.locket.backend.service.FirebaseClientService
import com.locket.backend.service.SupabaseClientService
import java.util.UUID

class PostRepository {
    private val supabase = SupabaseClientService.client
    private val database = FirebaseClientService.database

    // Upload ảnh lên Supabase Storage từ ByteArray (dùng khi có File)
    suspend fun uploadImageToSupabase(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "${UUID.randomUUID()}.jpg"
            Log.d("PostRepository", "Uploading to Supabase bucket 'post', file=$fileName, size=${bytes.size}")
            supabase.storage.from("post").upload(fileName, bytes)
            val url = supabase.storage.from("post").publicUrl(fileName)
            Log.d("PostRepository", "Supabase upload SUCCESS: $url")
            url
        } catch (e: Exception) {
            Log.e("PostRepository", "Supabase upload FAILED: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    // Overload giữ tương thích cũ — không dùng Uri nữa nhưng giữ để tránh break nếu còn chỗ gọi
    suspend fun uploadImageToSupabase(uri: Uri, bytes: ByteArray): String? =
        uploadImageToSupabase(bytes)

    suspend fun savePost(post: PostModel): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val postId = if (post.id.isEmpty()) UUID.randomUUID().toString() else post.id
            val finalPost = post.copy(id = postId)
            database.getReference("posts").child(postId).setValue(finalPost).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getPosts(): List<PostModel> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = database.getReference("posts").get().await()
            snapshot.children.mapNotNull { child ->
                child.getValue(PostModel::class.java)
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
