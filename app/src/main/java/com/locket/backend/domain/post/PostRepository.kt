package com.locket.backend.domain.post

import android.net.Uri
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
                val ref = database.getReference("posts").child(post.id.ifEmpty { UUID.randomUUID().toString() })
                val finalPost = post.copy(id = ref.key ?: post.id)
                ref.setValue(finalPost).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getPosts(): List<PostModel> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = database.getReference("posts").get().await()
                snapshot.children.mapNotNull { it.getValue(PostModel::class.java) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
