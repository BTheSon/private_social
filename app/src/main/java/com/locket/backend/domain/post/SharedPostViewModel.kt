package com.locket.backend.domain.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedPostViewModel : ViewModel() {
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    fun setImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }
    
    fun clearImageUri() {
        _selectedImageUri.value = null
    }
}
