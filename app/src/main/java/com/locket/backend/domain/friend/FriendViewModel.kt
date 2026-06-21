package com.locket.backend.domain.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.locket.backend.domain.contact.ContactProvider
import com.locket.backend.domain.contact.ContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SuggestionUiState {
    object Idle : SuggestionUiState
    object Loading : SuggestionUiState
    object NoPermission : SuggestionUiState
    data class Success(val suggestions: List<FriendModel>) : SuggestionUiState
    data class Error(val message: String) : SuggestionUiState
}

class FriendViewModel(private val repository: FriendRepository) : ViewModel() {

    private val _activeSubTab = MutableStateFlow(0)
    val activeSubTab: StateFlow<Int> = _activeSubTab

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val firebaseFriendships = repository.observeFriendships()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                else -> emptyList()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSearchingFirebase = MutableStateFlow(false)
    val isSearchingFirebase: StateFlow<Boolean> = _isSearchingFirebase

    private val _firebaseSearchResult = MutableStateFlow<FriendModel?>(null)
    val firebaseSearchResult: StateFlow<FriendModel?> = _firebaseSearchResult

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    fun setSubTab(tab: Int) { _activeSubTab.value = tab }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun searchUserOnFirebase(phone: String) {
        viewModelScope.launch {
            _isSearchingFirebase.value = true
            val result = repository.findUserOnFirebase(phone)
            _firebaseSearchResult.value = result ?: FriendModel("", "", "", "")
            _isSearchingFirebase.value = false
        }
    }

    fun clearFirebaseSearch() {
        _firebaseSearchResult.value = null
        _isSearchingFirebase.value = false
    }

    fun sendRequest(phone: String) = viewModelScope.launch { repository.sendFriendRequest(phone) }
    fun acceptRequest(phone: String) = viewModelScope.launch { repository.acceptFriendRequest(phone) }
    
    fun removeFriendship(phone: String) = viewModelScope.launch { repository.removeFriendship(phone) }

    fun syncContactsAndFindFriends(contactList: List<String>) {
        loadFriendSuggestions()
    }

    @Volatile
    private var contactRepository: ContactRepository? = null

    private val _suggestionState = MutableStateFlow<SuggestionUiState>(SuggestionUiState.Idle)
    val suggestionState: StateFlow<SuggestionUiState> = _suggestionState

    fun initContactRepository(contactProvider: ContactProvider) {
        if (contactRepository == null) {
            contactRepository = ContactRepository(contactProvider)
        }
    }

    fun hasContactPermission(): Boolean = contactRepository?.hasContactPermission() ?: false

    fun loadFriendSuggestions() {
        val repo = contactRepository ?: return
        if (!repo.hasContactPermission()) {
            _suggestionState.value = SuggestionUiState.NoPermission
            return
        }

        viewModelScope.launch {
            _suggestionState.value = SuggestionUiState.Loading
            runCatching {
                val myPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
                repo.getFriendSuggestions(myPhoneNumber)
            }.onSuccess { list ->
                _suggestionState.value = SuggestionUiState.Success(list)
            }.onFailure { e ->
                _suggestionState.value = SuggestionUiState.Error(e.message ?: "Đã có lỗi xảy ra")
            }
        }
    }

    fun onAddContactFriendClicked(phone: String) {
        viewModelScope.launch {
            val existingFriendships = firebaseFriendships.value
            val existingRelation = existingFriendships.find { it.phoneNumber == phone }

            if (existingRelation != null) {
                when (existingRelation.relationStatus) {
                    "FRIEND" -> {
                        _toastEvent.emit("Người này đã là bạn bè của bạn")
                        return@launch
                    }
                    "SENT", "RECEIVED" -> {
                        _toastEvent.emit("Đã có lời mời kết bạn đang chờ")
                        return@launch
                    }
                }
            }

            val repo = contactRepository ?: return@launch
            val existsOnFirebase = repo.checkUserExistsOnFirebase(phone)
            
            if (!existsOnFirebase) {
                _toastEvent.emit("Số điện thoại chưa đăng ký app")
            } else {
                sendRequest(phone)
                _toastEvent.emit("Đã gửi lời mời kết bạn")
            }
        }
    }

    fun onContactPermissionDenied() {
        _suggestionState.value = SuggestionUiState.NoPermission
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