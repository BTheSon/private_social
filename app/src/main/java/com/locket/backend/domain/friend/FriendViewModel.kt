package com.locket.backend.domain.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FriendViewModel(private val repository: FriendRepository) : ViewModel() {

    // Sub-tab active trong màn hình Bạn bè: 0 = Bạn bè, 1 = Lời mời, 2 = Gợi ý
    private val _activeSubTab = MutableStateFlow(0)
    val activeSubTab: StateFlow<Int> = _activeSubTab

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Trạng thái hiển thị danh sách động tùy thuộc vào Sub-tab hoặc Search Query
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiFriendsList: StateFlow<List<FriendModel>> = combine(_activeSubTab, _searchQuery) { tab, query ->
        Pair(tab, query)
    }.flatMapLatest { (tab, query) ->
        if (query.isNotEmpty()) {
            repository.searchUsers(query)
        } else {
            when (tab) {
                0 -> repository.friends
                1 -> combine(repository.receivedInvites, repository.sentInvites) { rec, sent -> rec + sent }
                else -> repository.suggestions
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Đổ dữ liệu mẫu ngay khi khởi tạo để test giao diện
        viewModelScope.launch {
            repository.prepopulateMockData()
        }
    }

    fun setSubTab(tab: Int) {
        _activeSubTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun sendRequest(phone: String) = viewModelScope.launch { repository.sendFriendRequest(phone) }
    fun acceptRequest(phone: String) = viewModelScope.launch { repository.acceptFriendRequest(phone) }
    fun removeFriendship(phone: String) = viewModelScope.launch { repository.cancelOrDeleteFriendship(phone) }
}

// Factory cho ViewModel
class FriendsViewModelFactory(private val repository: FriendRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}