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
import com.locket.backend.domain.music.ItunesRepository
import com.locket.backend.domain.photo.PhotoRepository
import com.locket.backend.domain.photo.PhotoViewModel
import com.locket.backend.domain.photo.PhotoViewModelFactory
import com.locket.backend.domain.post.PostRepository
import com.locket.backend.domain.post.PostViewModel
import com.locket.backend.domain.post.PostViewModelFactory
import com.locket.backend.domain.profile.ProfileViewModel
import com.locket.backend.domain.profile.ProfileViewModelFactory
import com.locket.backend.domain.user.UserRepository
import com.locket.frontend.screens.auth.AuthScreen
import com.locket.frontend.screens.main.MainScreen
import com.locket.frontend.theme.MyApplicationTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mDatabase = MDatabase.getDatabase(this)

        // Post & Music (khởi tạo trước vì PhotoViewModel cần PostRepository)
        val postRepository = PostRepository()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val itunesApiService = retrofit.create(com.locket.backend.domain.music.ItunesApiService::class.java)
        val itunesRepository = ItunesRepository(itunesApiService)

        // Photo (inject PostRepository để thực hiện upload sau khi chụp)
        val photoDao = mDatabase.photoDao()
        val photoRepository = PhotoRepository(photoDao)
        val photoViewModel: PhotoViewModel by viewModels {
            PhotoViewModelFactory(application, photoRepository, postRepository)
        }

        // Post (music search + timeline posts)
        val postViewModel: PostViewModel by viewModels {
            PostViewModelFactory(postRepository, itunesRepository)
        }

        // Friend
        val friendRepository = FriendRepository()
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

        // Profile
        val profileViewModel: ProfileViewModel by viewModels {
            ProfileViewModelFactory(userRepository)
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
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("main") {
                        MainScreen(
                            photoViewModel = photoViewModel,
                            postViewModel = postViewModel,
                            friendViewModel = friendViewModel,
                            profileViewModel = profileViewModel,
                            onNavigateToAuth = {
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}