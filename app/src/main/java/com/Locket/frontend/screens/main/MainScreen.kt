package com.Locket.frontend.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Locket.backend.domain.friend.FriendsViewModel
import com.Locket.backend.domain.photo.PhotoViewModel
import com.Locket.frontend.screens.camera.CameraScreen
import com.Locket.frontend.screens.friend.FriendScreen
import com.Locket.frontend.screens.gallery.GalleryScreen

@Composable
fun MainScreen(
    photoViewModel: PhotoViewModel,
    friendsViewModel: FriendsViewModel
) {
    val activeTab by photoViewModel.activeTab.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content viewport
        when (activeTab) {
            0 -> CameraScreen(viewModel = photoViewModel, modifier = Modifier.fillMaxSize())
            1 -> GalleryScreen(viewModel = photoViewModel, modifier = Modifier.fillMaxSize())
            2 -> FriendScreen(viewModel = friendsViewModel, modifier = Modifier.fillMaxSize())
        }

        // Floating Glass Dock at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(0.9f)
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
                    modifier = Modifier.weight(1f).height(48.dp).background(
                        color = if (cameraSelected) Color(0x33FFCC00) else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = if (cameraSelected) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                            contentDescription = "Máy ảnh",
                            tint = if (cameraSelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        if (cameraSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Máy Ảnh", color = Color(0xFFFFCC00), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Tab Gallery
                val gallerySelected = activeTab == 1
                IconButton(
                    onClick = { photoViewModel.setActiveTab(1) },
                    modifier = Modifier.weight(1f).height(48.dp).background(
                        color = if (gallerySelected) Color(0x33FFCC00) else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = if (gallerySelected) Icons.Filled.GridView else Icons.Outlined.GridView,
                            contentDescription = "Lưu trữ",
                            tint = if (gallerySelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        if (gallerySelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lưu Trữ", color = Color(0xFFFFCC00), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Tab Friends
                val friendsSelected = activeTab == 2
                IconButton(
                    onClick = { photoViewModel.setActiveTab(2) },
                    modifier = Modifier.weight(1f).height(48.dp).background(
                        color = if (friendsSelected) Color(0x33FFCC00) else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = if (friendsSelected) Icons.Filled.People else Icons.Outlined.People,
                            contentDescription = "Bạn bè",
                            tint = if (friendsSelected) Color(0xFFFFCC00) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        if (friendsSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bạn bè", color = Color(0xFFFFCC00), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}