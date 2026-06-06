package com.locket.backend.domain.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.locket.backend.domain.user.UserEntity
import com.locket.backend.domain.user.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object CodeSent : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance().reference }

    private var storedVerificationId: String? = null
    private var currentInputPhone: String = ""

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading

        val formattedNumber = if (phoneNumber.startsWith("0")) {
            "+84${phoneNumber.substring(1)}"
        } else if (!phoneNumber.startsWith("+")) {
            "+84$phoneNumber"
        } else {
            phoneNumber
        }
        currentInputPhone = formattedNumber

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _authState.value = AuthState.Error(e.message ?: "Lỗi xác thực số điện thoại")
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    storedVerificationId = verificationId
                    _authState.value = AuthState.CodeSent
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(code: String) {
        _authState.value = AuthState.Loading
        val verificationId = storedVerificationId ?: return
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) = viewModelScope.launch {
        try {
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                val phone = user.phoneNumber ?: currentInputPhone
                syncUserToDatabase(phone)
            } else {
                _authState.value = AuthState.Error("Không lấy được thông tin người dùng")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Mã OTP không hợp lệ")
        }
    }

    // ĐỒNG BỘ: Kéo từ Firebase Realtime DB và lưu vào Room bằng UserRepository
    private suspend fun syncUserToDatabase(phone: String) {
        try {
            val userRef = database.child("users").child(phone)
            val snapshot = userRef.get().await()

            val userEntity: UserEntity

            if (snapshot.exists()) {
                // TÀI KHOẢN CŨ
                val displayName = snapshot.child("displayName").getValue(String::class.java) ?: "User"
                val avatarUrl = snapshot.child("avatarUrl").getValue(String::class.java) ?: ""
                val fcmToken = snapshot.child("fcmToken").getValue(String::class.java) ?: ""

                userEntity = UserEntity(phone, displayName, isMe = true)
            } else {
                // TÀI KHOẢN MỚI
                val newDisplayName = "Locket_$phone".substring(0, 13)
                val newUserMap = mapOf(
                    "displayName" to newDisplayName,
                    "avatarUrl" to "",
                    "fcmToken" to ""
                )
                userRef.setValue(newUserMap).await()

                userEntity = UserEntity(phone, newDisplayName, isMe = true)
            }

            // LƯU XUỐNG ROOM
            userRepository.saveUser(userEntity)

            _authState.value = AuthState.Success
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Lỗi đồng bộ dữ liệu: ${e.message}")
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

// Factory
class AuthViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}