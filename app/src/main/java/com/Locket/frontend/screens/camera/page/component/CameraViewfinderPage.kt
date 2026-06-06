package com.Locket.frontend.screens.camera.page.component

import androidx.camera.core.ImageCapture
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.Locket.frontend.screens.camera.CameraViewfinder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CameraViewfinderPage(
    isCameraActive: Boolean,
    lensFacing: Int,
    firstPhotoPath: String?,
    isCapturing: Boolean,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCaptureClick: () -> Unit,
    onSwitchLensClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top bar - Xóa các text thừa như LOCKET 1:1 LIVE, chỉ chừa Spacer statusBarsPadding
        Spacer(
            modifier = Modifier
                .statusBarsPadding()
                .height(16.dp)
        )

        // Camera Preview Box
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(52.dp))
                    .background(Color(0xFF191919))
                    .border(6.dp, Brush.linearGradient(listOf(Color(0xFFFFCC00), Color(0xFFFF9100))), RoundedCornerShape(52.dp))
                    .padding(6.dp)
                    .clip(RoundedCornerShape(46.dp))
            ) {
                if (isCameraActive) {
                    CameraViewfinder(
                        lensFacing = lensFacing,
                        onImageCaptureReady = onImageCaptureReady,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        Text("Kính ngắm tạm nghỉ", color = Color.DarkGray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Box(modifier = Modifier.fillMaxSize().border(BorderStroke(2.dp, Color.Black.copy(alpha = 0.2f)), RoundedCornerShape(46.dp)))
            }
        }

        // Shutter & Controls
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // History Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(2.dp, Color(0xFFFFCC00).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .combinedClickable(onClick = onHistoryClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (firstPhotoPath != null) {
                        AsyncImage(
                            model = firstPhotoPath, contentDescription = "Xem lịch sử",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.PhotoLibrary, "Kho ảnh trống", tint = Color.LightGray.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
                    }
                }

                // Shutter Button
                val animatedScale by animateFloatAsState(
                    targetValue = if (isCapturing) 0.82f else 1.0f,
                    animationSpec = tween(durationMillis = 100), label = "ShutterScale"
                )

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(animatedScale)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) Color.DarkGray else Color(0xFFFFCC00))
                        .combinedClickable(onClick = onCaptureClick)
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                    } else {
                        Box(modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.35f), CircleShape))
                    }
                }

                // Switch Camera Lens Button
                IconButton(
                    onClick = onSwitchLensClick,
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF1E1E1E), CircleShape)
                        .border(1.dp, Color(0xFF333333), CircleShape)
                        .testTag("camera_flip_button")
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, "Chuyển đổi camera", tint = Color(0xFFFFCC00), modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scroll Helper Icon tối giản (không có text thừa)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Cuộn lên xem lịch sử",
                tint = Color(0xFFFFCC00).copy(alpha = 0.6f),
                modifier = Modifier
                    .size(24.dp)
                    .combinedClickable(onClick = onHistoryClick)
            )
        }
    }
}