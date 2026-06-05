package com.example.frontend.screens.gallery

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.backend.common.util.DateTimeUtils
import com.example.backend.domain.photo.PhotoEntity
import com.example.backend.domain.photo.PhotoViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    viewModel: PhotoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photos by viewModel.allPhotos.collectAsState()

    var photoToPreview by remember { mutableStateOf<PhotoEntity?>(null) }
    var photoToDelete by remember { mutableStateOf<PhotoEntity?>(null) }

    // Confirm Delete Dialog
    if (photoToDelete != null) {
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = {
                Text(
                    text = "Xóa khỏi bộ sưu tập",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc muốn xóa vĩnh viễn bức ảnh vuông này không?",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        photoToDelete?.let { viewModel.deletePhoto(it) }
                        photoToDelete = null
                        Toast.makeText(context, "Đã xóa ảnh", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("XÓA", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("HỦY", color = Color.White)
                }
            },
            containerColor = Color(0xFF222222),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // High fidelity Fullscreen image viewer Modal
    if (photoToPreview != null) {
        Dialog(
            onDismissRequest = { photoToPreview = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                // Main Square Picture
                AsyncImage(
                    model = photoToPreview?.filePath,
                    contentDescription = "Full square projection",
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )

                // Bottom Floating Control Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ảnh Chụp ${photoToPreview?.let { DateTimeUtils.formatTimestamp(it.timestamp) }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                photoToDelete = photoToPreview
                                photoToPreview = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("XÓA ẢNH")
                        }

                        Button(
                            onClick = { photoToPreview = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Đóng")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ĐÓNG")
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)) // True Locket cinematic dark background
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x1AFFCC00), CircleShape)
                    .border(1.dp, Color(0xFFFFCC00).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = Color(0xFFFFCC00),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "BỘ SƯU TẬP LOCKET",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Đã lưu trữ ${photos.size} khoảnh khắc vuông 1:1",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
            }
        }

        HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)

        if (photos.isEmpty()) {
            // Elegant empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoAlbum,
                    contentDescription = "Empty Album",
                    tint = Color(0xFF2C2C2C),
                    modifier = Modifier.size(92.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Lưu trữ trống trải",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Các bức ảnh tỉ lệ Locket 1:1 bạn chụp bằng Camera sẽ xuất hiện tại đây. Hãy chuyển sang tab Máy ảnh bên dưới và bắt đầu!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            // Premium 3-column Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("gallery_grid"),
                contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E1E1E))
                            .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(18.dp))
                            .combinedClickable(
                                onClick = { photoToPreview = photo },
                                onLongClick = { photoToDelete = photo }
                            )
                            .testTag("gallery_photo_item_${photo.id}")
                    ) {
                        AsyncImage(
                            model = photo.filePath,
                            contentDescription = "Gallery square representation",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Subtle golden corner touch to signify premium widget sync
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .size(6.dp)
                                .background(Color(0xFFFFCC00), CircleShape)
                        )
                    }
                }
            }
        }
    }
}
