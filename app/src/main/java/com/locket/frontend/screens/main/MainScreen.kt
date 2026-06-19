package com.locket.frontend.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.locket.backend.domain.friend.FriendViewModel
import com.locket.backend.domain.photo.PhotoViewModel
import com.locket.backend.domain.profile.ProfileViewModel
import com.locket.backend.domain.post.PostViewModel
import com.locket.backend.domain.post.SharedPostViewModel
import com.locket.frontend.screens.camera.CameraScreen
import com.locket.frontend.screens.friend.FriendScreen
import com.locket.frontend.screens.gallery.GalleryScreen
import com.locket.frontend.screens.profile.ProfileScreen
import com.locket.frontend.screens.feed.FeedScreen
import com.locket.frontend.screens.post.CreatePostScreen
import com.locket.frontend.screens.post.SongSearchDialog

@Composable
fun MainScreen(
    photoViewModel: PhotoViewModel,
    friendViewModel: FriendViewModel,
    profileViewModel: ProfileViewModel,
    postViewModel: PostViewModel,
    sharedPostViewModel: SharedPostViewModel,
    onNavigateToAuth: () -> Unit
) {
    val activeTab by photoViewModel.activeTab.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (activeTab) {
            0 -> CameraScreen(viewModel = photoViewModel, modifier = Modifier.fillMaxSize())
            1 -> GalleryScreen(viewModel = photoViewModel, modifier = Modifier.fillMaxSize())
            2 -> FriendScreen(viewModel = friendViewModel, modifier = Modifier.fillMaxSize())
            3 -> {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutClick = onNavigateToAuth,
                    modifier = Modifier.fillMaxSize()
                )
            }
            4 -> {
                val posts by postViewModel.posts.collectAsState()
                val currentUserId = profileViewModel.currentUser.collectAsState().value?.phoneNumber ?: "unknown"
                FeedScreen(
                    posts = posts,
                    currentUserId = currentUserId,
                    onLikeToggle = { id, liked -> postViewModel.toggleLike(id, liked, currentUserId) },
                    onClickSyncContacts = { /* Request Contacts Permission here */ },
                    modifier = Modifier.fillMaxSize()
                )
            }
            5 -> {
                val initialUri by sharedPostViewModel.selectedImageUri.collectAsState()
                var showMusicDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                var selectedSong by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.locket.backend.domain.music.SongModel?>(null) }
                val searchResults by postViewModel.searchResults.collectAsState()
                val currentUserId = profileViewModel.currentUser.collectAsState().value?.phoneNumber ?: "unknown"

                if (showMusicDialog) {
                    SongSearchDialog(
                        onDismissRequest = { showMusicDialog = false },
                        onSongSelected = { song ->
                            selectedSong = song
                            showMusicDialog = false
                        },
                        searchSongs = { query -> postViewModel.searchMusic(query) },
                        searchResults = searchResults
                    )
                }

                CreatePostScreen(
                    initialImageUri = initialUri,
                    onImagePicked = { uri -> sharedPostViewModel.setImageUri(uri) },
                    onPost = { uri, caption, song ->
                        postViewModel.createPost(
                            uri = uri,
                            caption = caption,
                            song = song,
                            currentUserId = currentUserId,
                            bytes = null, // Lấy ByteArray từ URI thực tế nếu cần thiết
                            onSuccess = { photoViewModel.setActiveTab(4) } // Chuyển về Feed
                        )
                    },
                    onSearchMusic = { showMusicDialog = true },
                    selectedSong = selectedSong,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(0.98f) // Tăng nhẹ width để 4 tab hiển thị thoải mái hơn
                .background(Color(0xDC121212), RoundedCornerShape(28.dp))
                .border(1.dp, Color(0xFF2D2D2D), RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab Camera
                val cameraSelected = activeTab == 0
                IconButton(
                    onClick = { photoViewModel.setActiveTab(0) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = if (cameraSelected) Color(0x33FFCC00) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (cameraSelected) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                            contentDescription = "Máy ảnh",
                            tint = if (cameraSelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        if (cameraSelected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Máy Ảnh",
                                color = Color(0xFFFFCC00),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Tab Gallery
                val gallerySelected = activeTab == 1
                IconButton(
                    onClick = { photoViewModel.setActiveTab(1) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = if (gallerySelected) Color(0x33FFCC00) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (gallerySelected) Icons.Filled.GridView else Icons.Outlined.GridView,
                            contentDescription = "Lưu trữ",
                            tint = if (gallerySelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        if (gallerySelected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Lưu Trữ",
                                color = Color(0xFFFFCC00),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Tab Friend
                val friendsSelected = activeTab == 2
                IconButton(
                    onClick = { photoViewModel.setActiveTab(2) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = if (friendsSelected) Color(0x33FFCC00) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (friendsSelected) Icons.Filled.People else Icons.Outlined.People,
                            contentDescription = "Bạn bè",
                            tint = if (friendsSelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        if (friendsSelected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Bạn bè",
                                color = Color(0xFFFFCC00),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Tab Profile (Cá nhân)
                val profileSelected = activeTab == 3
                IconButton(
                    onClick = { photoViewModel.setActiveTab(3) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = if (profileSelected) Color(0x33FFCC00) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (profileSelected) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Cá nhân",
                            tint = if (profileSelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        if (profileSelected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Cá nhân",
                                color = Color(0xFFFFCC00),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Tab Feed
                val feedSelected = activeTab == 4
                IconButton(
                    onClick = { photoViewModel.setActiveTab(4) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = if (feedSelected) Color(0x33FFCC00) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (feedSelected) Icons.Filled.DynamicFeed else Icons.Outlined.DynamicFeed,
                            contentDescription = "Bảng tin",
                            tint = if (feedSelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        if (feedSelected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Bảng tin",
                                color = Color(0xFFFFCC00),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Tab Create Post
                val postSelected = activeTab == 5
                IconButton(
                    onClick = { photoViewModel.setActiveTab(5) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = if (postSelected) Color(0x33FFCC00) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (postSelected) Icons.Filled.AddBox else Icons.Outlined.AddBox,
                            contentDescription = "Đăng bài",
                            tint = if (postSelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        if (postSelected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Đăng bài",
                                color = Color(0xFFFFCC00),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}