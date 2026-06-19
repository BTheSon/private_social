package com.locket.backend.domain.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.locket.backend.domain.music.ItunesRepository
import com.locket.backend.domain.music.SongModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel(
    private val postRepository: PostRepository,
    private val itunesRepository: ItunesRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())
    val posts: StateFlow<List<PostModel>> = _posts

    private val _searchResults = MutableStateFlow<List<SongModel>>(emptyList())
    val searchResults: StateFlow<List<SongModel>> = _searchResults

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _posts.value = postRepository.getPosts()
        }
    }

    fun searchMusic(query: String) {
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                _searchResults.value = itunesRepository.searchSongs(query)
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    fun toggleLike(postId: String, isLiked: Boolean, currentUserId: String) {
        // Implement logic to update like in DB here
    }

    fun createPost(uri: Uri?, caption: String, song: SongModel?, currentUserId: String, bytes: ByteArray?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            var imageUrl = ""
            if (uri != null && bytes != null) {
                imageUrl = postRepository.uploadImageToSupabase(uri, bytes) ?: ""
            }
            val post = PostModel(
                userId = currentUserId,
                imageUrl = imageUrl,
                caption = caption,
                songName = song?.trackName,
                artistName = song?.artistName
            )
            val success = postRepository.savePost(post)
            if (success) {
                loadPosts()
                onSuccess()
            }
        }
    }
}

class PostViewModelFactory(
    private val postRepository: PostRepository,
    private val itunesRepository: ItunesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(postRepository, itunesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
