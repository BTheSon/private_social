package com.locket.backend.domain.post

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.util.UUID

class PostRepository(
    private val supabase: SupabaseClient
    // private val database: FirebaseDatabase // Uncomment and use if needed
) {
    suspend fun uploadImageToSupabase(uri: Uri, bytes: ByteArray): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "${UUID.randomUUID()}.jpg"
                val bucket = supabase.storage.from("posts")
                bucket.upload(fileName, bytes)
                bucket.publicUrl(fileName)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun savePost(post: PostModel): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Here you would use Firebase Database to save the post.
                // val ref = database.getReference("posts").child(post.id.ifEmpty { UUID.randomUUID().toString() })
                // ref.setValue(post).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getPosts(): List<PostModel> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch from Firebase Database here
                // val snapshot = database.getReference("posts").get().await()
                // snapshot.children.mapNotNull { it.getValue(PostModel::class.java) }
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
