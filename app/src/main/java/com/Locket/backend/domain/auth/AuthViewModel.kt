package com.Locket.backend.domain.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object CodeSent : AuthState() // Chuyển sang màn hình nhập OTP
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private var storedVerificationId: String? = null

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Bước 1: Gửi mã OTP
    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading

        // Chuẩn hóa định dạng số điện thoại Việt Nam (+84)
        val formattedNumber = if (phoneNumber.startsWith("0")) {
            "+84${phoneNumber.substring(1)}"
        } else if (!phoneNumber.startsWith("+")) {
            "+84$phoneNumber"
        } else {
            phoneNumber
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Tự động xác thực thành công (Android tự đọc SMS)
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _authState.value = AuthState.Error(e.message ?: "Lỗi xác thực số điện thoại")
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    storedVerificationId = verificationId
                    _authState.value = AuthState.CodeSent // Chuyển UI sang nhập OTP
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Bước 2: Xác minh mã OTP người dùng nhập vào
    fun verifyCode(code: String) {
        _authState.value = AuthState.Loading
        val verificationId = storedVerificationId ?: return
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) = viewModelScope.launch {
        try {
            auth.signInWithCredential(credential).await()
            _authState.value = AuthState.Success
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Mã OTP không hợp lệ hoặc đã hết hạn")
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}