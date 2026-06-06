package com.locket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.locket.backend.domain.auth.AuthViewModel
import com.locket.backend.domain.auth.AuthViewModelFactory
import com.locket.backend.domain.database.MDatabase
import com.locket.backend.domain.friend.FriendRepository
import com.locket.backend.domain.friend.FriendViewModel
import com.locket.backend.domain.friend.FriendsViewModelFactory
import com.locket.backend.domain.photo.PhotoRepository
import com.locket.backend.domain.photo.PhotoViewModel
import com.locket.backend.domain.photo.PhotoViewModelFactory
import com.locket.backend.domain.user.UserRepository
import com.locket.frontend.screens.auth.AuthScreen
import com.locket.frontend.screens.main.MainScreen
import com.locket.frontend.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mDatabase = MDatabase.getDatabase(this)

        // Photo

        val photoDao = mDatabase.photoDao()
        val photoRepository = PhotoRepository(photoDao)
        val photoViewModel: PhotoViewModel by viewModels {
            PhotoViewModelFactory(application, photoRepository)
        }

        // Friend

        val friendDao = mDatabase.friendDao()
        val friendRepository = FriendRepository(friendDao)
        val friendViewModel: FriendViewModel by viewModels {
            FriendsViewModelFactory(friendRepository)
        }

        // User

        val userDao = mDatabase.userDao()
        val userRepository = UserRepository(userDao)

        // Auth

        val authViewModel: AuthViewModel by viewModels {
            AuthViewModelFactory(userRepository)
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = if (authViewModel.isUserLoggedIn()) "main" else "auth"
                ) {
                    composable("auth") {
                        AuthScreen(
                            viewModel = authViewModel,
                            onAuthSuccess = {
                                navController.navigate("main") {
                                    popUpTo("auth") {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                    composable("main") {
                        MainScreen(
                            photoViewModel = photoViewModel,
                            friendViewModel = friendViewModel
                        )
                    }
                }
            }
        }
    }
}