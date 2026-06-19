package com.locket.backend.domain.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.locket.backend.domain.user.UserEntity
import com.locket.backend.domain.user.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Tự động lắng nghe thông tin User hiện tại từ Room Database
    val currentUser: StateFlow<UserEntity?> = userRepository.getMyProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun logout(onLogoutSuccess: () -> Unit) = viewModelScope.launch {
        // 1. Đăng xuất khỏi Firebase Auth
        FirebaseAuth.getInstance().signOut()

        // 2. Xóa trạng thái đăng nhập cục bộ trong Room
        userRepository.clearMyProfile()

        // 3. Kích hoạt Callback để chuyển màn hình
        onLogoutSuccess()
    }
}

// Factory khởi tạo ViewModel
class ProfileViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}