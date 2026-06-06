package com.Locket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.Locket.backend.domain.auth.AuthViewModel
import com.Locket.backend.domain.database.MDatabase
import com.Locket.backend.domain.friend.FriendRepository
import com.Locket.backend.domain.friend.FriendsViewModel
import com.Locket.backend.domain.friend.FriendsViewModelFactory
import com.Locket.backend.domain.photo.PhotoRepository
import com.Locket.backend.domain.photo.PhotoViewModel
import com.Locket.backend.domain.photo.PhotoViewModelFactory
import com.Locket.frontend.screens.auth.AuthScreen
import com.Locket.frontend.screens.main.MainScreen
import com.Locket.frontend.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Khởi tạo AuthViewModel
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo các Repository và ViewModel cũ
        val mDatabase = MDatabase.getDatabase(this)

        val photoDao = mDatabase.photoDao()
        val photoRepository = PhotoRepository(photoDao)
        val photoViewModel: PhotoViewModel by viewModels {
            PhotoViewModelFactory(application, photoRepository)
        }

        val friendDao = mDatabase.friendDao()
        val friendRepository = FriendRepository(friendDao)
        val friendsViewModel: FriendsViewModel by viewModels {
            FriendsViewModelFactory(friendRepository)
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val navController = rememberNavController()

                // QUẢN LÝ LUỒNG ĐIỀU HƯỚNG
                NavHost(
                    navController = navController,
                    startDestination = if (authViewModel.isUserLoggedIn()) "main" else "auth"
                ) {

                    // 1. Màn hình Auth (Chỉ hiện khi chưa đăng nhập)
                    composable("auth") {
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = {
                                // Xóa auth khỏi stack và bay vào main
                                navController.navigate("main") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. Màn hình Chính (Chứa 3 chức năng cốt lõi)
                    composable("main") {
                        MainScreen(
                            photoViewModel = photoViewModel,
                            friendsViewModel = friendsViewModel
                        )
                    }
                }
            }
        }
    }
}