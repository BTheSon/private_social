package com.locket.backend.domain.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.locket.backend.domain.database.DraftDao
import com.locket.backend.domain.database.DraftEntity
import com.locket.backend.domain.music.ItunesRepository
import com.locket.backend.domain.music.SongModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * PostViewModel: Quản lý:
 * 1. Tìm kiếm nhạc iTunes cho màn hình xác nhận bài đăng
 * 2. Tải danh sách bài đăng từ Firebase cho TimelineHistoryPage
 */
class PostViewModel(
    private val postRepository: PostRepository,
    private val itunesRepository: ItunesRepository,
    private val draftDao: DraftDao
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())
    val posts: StateFlow<List<PostModel>> = _posts

    private val _searchResults = MutableStateFlow<List<SongModel>>(emptyList())
    val searchResults: StateFlow<List<SongModel>> = _searchResults

    val pendingDrafts: StateFlow<List<DraftEntity>> = draftDao.getAllDrafts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
            val searchTerm = if (query.trim().isEmpty()) "top hits" else query
            _searchResults.value = itunesRepository.searchSongs(searchTerm)
        }
    }

    fun toggleLike(postId: String, isLiked: Boolean, currentUserId: String) {
        // TODO: implement like logic on Firebase RTDB
    }
}

class PostViewModelFactory(
    private val postRepository: PostRepository,
    private val itunesRepository: ItunesRepository,
    private val draftDao: DraftDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(postRepository, itunesRepository, draftDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
