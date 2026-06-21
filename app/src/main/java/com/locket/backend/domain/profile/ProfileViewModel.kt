package com.locket.backend.domain.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.locket.backend.domain.user.UserEntity
import com.locket.backend.domain.user.UserRepository
import com.locket.backend.service.FirebaseClientService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = userRepository.getMyProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar = _isUploadingAvatar.asStateFlow()

    // Đổi tên
    fun updateDisplayName(newName: String) = viewModelScope.launch {
        userRepository.updateProfile(displayName = newName, avatarUrl = null)
    }

    // Đổi ảnh đại diện
    fun uploadAndChangeAvatar(imageBytes: ByteArray) = viewModelScope.launch {
        _isUploadingAvatar.value = true
        val uploadedUrl = userRepository.uploadAvatarToSupabase(imageBytes)

        if (uploadedUrl != null) {
            userRepository.updateProfile(displayName = null, avatarUrl = uploadedUrl)
        }
        _isUploadingAvatar.value = false
    }

    fun logout(onLogoutSuccess: () -> Unit) = viewModelScope.launch {
        FirebaseClientService.auth.signOut()
        userRepository.clearMyProfile()
        onLogoutSuccess()
    }
}

class ProfileViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}