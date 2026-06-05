package com.example.frontend.screens.camera.page.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.backend.domain.photo.PhotoEntity
import com.example.frontend.screens.camera.TimelinePhotoItem

@Composable
fun TimelineHistoryPage(
    photos: List<PhotoEntity>,
    onBackClick: () -> Unit,
    onPhotoClick: (PhotoEntity) -> Unit,
    onPhotoLongClick: (PhotoEntity) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x1AFFCC00), CircleShape)
                        .border(1.dp, Color(0xFFFFCC00).copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.PhotoCamera, "Quay về máy ảnh", tint = Color(0xFFFFCC00), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "KHOẢNH KHẮC GẦN ĐÂY", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 0.5.sp
                    )
                    Text("Nhật ký Locket cá nhân cực chất", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("${photos.size} KHOẢNH KHẮC", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFCC00), fontWeight = FontWeight.Black)
            }
        }

        HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)

        // Content
        if (photos.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Default.PhotoAlbum, "Album trống", tint = Color(0xFF222222), modifier = Modifier.size(92.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Chưa có ảnh nào chụp", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Hãy chạm vào biểu tượng Máy ảnh góc trái để mở camera chụp và gửi khoảnh khắc của bạn ngay nhé!",
                        style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 22.sp
                    )
                }
            }
        } else {
            LazyColumn(
                state = lazyListState,
                flingBehavior = snapFlingBehavior,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(photos, key = { it.id }) { photo ->
                    TimelinePhotoItem(
                        photo = photo,
                        onClick = { onPhotoClick(photo) },
                        onLongClick = { onPhotoLongClick(photo) },
                        modifier = Modifier.fillParentMaxHeight()
                    )
                }
            }
        }
    }
}