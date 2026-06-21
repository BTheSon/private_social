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

    /**
     * Tải bài đăng cho Feed: chỉ lấy bài của mình + bạn bè (status = ACCEPTED).
     * 1. Lấy userId hiện tại (phoneNumber hoặc uid)
     * 2. Lấy danh sách bạn bè đã ACCEPTED từ /friendships/{myPhone}
     * 3. Lọc toàn bộ /posts, chỉ giữ lại bài có userId thuộc set {mình, bạn bè}
     */
    suspend fun getPostsForFeed(): List<PostModel> = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentUser = FirebaseClientService.auth.currentUser
            val myId = currentUser?.phoneNumber
                ?: currentUser?.uid
                ?: return@withContext emptyList()

            // Lấy danh sách bạn bè đã ACCEPTED
            val friendIds = mutableSetOf(myId)
            try {
                val friendshipsSnapshot = database.getReference("friendships")
                    .child(myId).get().await()
                for (child in friendshipsSnapshot.children) {
                    val status = child.child("status").getValue(String::class.java)
                    if (status == "ACCEPTED") {
                        child.key?.let { friendIds.add(it) }
                    }
                }
            } catch (e: Exception) {
                Log.w("PostRepository", "Could not load friendships, showing only own posts", e)
            }

            Log.d("PostRepository", "Feed filter: loading posts for ${friendIds.size} users (me + ${friendIds.size - 1} friends)")

            // Tải tất cả bài đăng rồi lọc theo danh sách bạn bè
            val postsSnapshot = database.getReference("posts").get().await()
            postsSnapshot.children.mapNotNull { child ->
                child.getValue(PostModel::class.java)
            }.filter { post ->
                post.userId in friendIds
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("PostRepository", "getPostsForFeed FAILED", e)
            emptyList()
        }
    }
}
