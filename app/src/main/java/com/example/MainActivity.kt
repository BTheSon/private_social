package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.PhotoDatabase
import com.example.data.PhotoRepository
import com.example.ui.PhotoViewModel
import com.example.ui.PhotoViewModelFactory
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = PhotoDatabase.getDatabase(this)
        val dao = database.photoDao()
        val repository = PhotoRepository(dao)

        val viewModel: PhotoViewModel by viewModels {
            PhotoViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme(darkTheme = true) { // Force cinematic dark theme for camera app
                val activeTab by viewModel.activeTab.collectAsState()

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Main content viewport
                    if (activeTab == 0) {
                        CameraScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        GalleryScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Floating Glass Dock at the bottom
                    Box(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                            .fillMaxWidth(0.85f)
                            .background(
                                color = Color(0xDC121212), // Deep pitch black glass
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF2D2D2D),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            // Camera Screen Tab Button
                            val cameraSelected = activeTab == 0
                            androidx.compose.material3.IconButton(
                                onClick = { viewModel.setActiveTab(0) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(
                                        color = if (cameraSelected) Color(0x33FFCC00) else Color.Transparent,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (cameraSelected) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                                        contentDescription = "Máy ảnh 1x1",
                                        tint = if (cameraSelected) Color(0xFFFFCC00) else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (cameraSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Máy Ảnh",
                                            color = Color(0xFFFFCC00),
                                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Gallery Screen Tab Button
                            val gallerySelected = activeTab == 1
                            androidx.compose.material3.IconButton(
                                onClick = { viewModel.setActiveTab(1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(
                                        color = if (gallerySelected) Color(0x33FFCC00) else Color.Transparent,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (gallerySelected) Icons.Filled.GridView else Icons.Outlined.GridView,
                                        contentDescription = "Bộ sưu tập",
                                        tint = if (gallerySelected) Color(0xFFFFCC00) else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (gallerySelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Lưu Trữ",
                                            color = Color(0xFFFFCC00),
                                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
