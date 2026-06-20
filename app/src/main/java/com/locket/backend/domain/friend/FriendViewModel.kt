package com.locket.backend.domain.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FriendViewModel(private val repository: FriendRepository) : ViewModel() {

    private val _activeSubTab = MutableStateFlow(0)
    val activeSubTab: StateFlow<Int> = _activeSubTab

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Lắng nghe trực tiếp luồng bạn bè từ Cloud Firebase
    private val firebaseFriendships = repository.observeFriendships()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lọc danh sách động cho giao diện Local dựa vào cấu trúc Firebase biến động
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiFriendsList: StateFlow<List<FriendModel>> = combine(_activeSubTab, _searchQuery, firebaseFriendships) { tab, query, friendships ->
        Triple(tab, query, friendships)
    }.map { (tab, query, friendships) ->
        if (query.isNotEmpty()) {
            friendships.filter { it.displayName.contains(query, ignoreCase = true) || it.phoneNumber.contains(query) }
        } else {
            when (tab) {
                0 -> friendships.filter { it.relationStatus == "FRIEND" }
                1 -> friendships.filter { it.relationStatus == "RECEIVED" || it.relationStatus == "SENT" }
                else -> emptyList() // Chừa chỗ cho tab gợi ý nếu có
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State phục vụ việc tìm kiếm trên Firebase thông qua Dialog
    private val _isSearchingFirebase = MutableStateFlow(false)
    val isSearchingFirebase: StateFlow<Boolean> = _isSearchingFirebase

    private val _firebaseSearchResult = MutableStateFlow<FriendModel?>(null)
    val firebaseSearchResult: StateFlow<FriendModel?> = _firebaseSearchResult

    fun setSubTab(tab: Int) { _activeSubTab.value = tab }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun searchUserOnFirebase(phone: String) {
        viewModelScope.launch {
            _isSearchingFirebase.value = true
            val result = repository.findUserOnFirebase(phone)
            _firebaseSearchResult.value = result ?: FriendModel("", "", "")
            _isSearchingFirebase.value = false
        }
    }

    fun clearFirebaseSearch() {
        _firebaseSearchResult.value = null
        _isSearchingFirebase.value = false
    }

    // Các hàm tương tác đẩy thẳng lệnh lên mây Firebase
    fun sendRequest(phone: String) = viewModelScope.launch { repository.sendFriendRequest(phone) }
    fun acceptRequest(phone: String) = viewModelScope.launch { repository.acceptFriendRequest(phone) }
    
    fun removeFriendship(phone: String) = viewModelScope.launch { repository.removeFriendship(phone) }

    fun syncContactsAndFindFriends(contactList: List<String>) {
        // Chỗ này bạn của bạn sẽ viết code so khớp list số điện thoại 
        // từ danh bạ với tài khoản Firestore để gợi ý kết bạn.
    }
}

class FriendsViewModelFactory(private val repository: FriendRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}