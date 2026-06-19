package com.locket.frontend.screens.camera.page.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun PendingPhotoConfirmationScreen(
    pendingPhotoFile: File?,
    isCapturing: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    AnimatedVisibility(
        visible = pendingPhotoFile != null,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F0F))
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Tiêu đề
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.statusBarsPadding().padding(top = 16.dp)
                ) {
                    Text(
                        text = "XÁC NHẬN KHOẢNH KHẮC",
                        color = Color(0xFFFFCC00),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Xem trước ảnh 1x1 trước khi đăng tải",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Khung viền ảnh Polaroid
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(52.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(
                            width = 6.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFCC00), Color(0xFFFF9100))
                            ),
                            shape = RoundedCornerShape(52.dp)
                        )
                        .padding(6.dp)
                        .clip(RoundedCornerShape(46.dp))
                ) {
                    AsyncImage(
                        model = pendingPhotoFile,
                        contentDescription = "Chờ đăng tải",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(2.dp, Color.Black.copy(alpha = 0.2f)), shape = RoundedCornerShape(46.dp))
                    )
                }

                // Hàng nút điều khiển (Cancel / Confirm)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onCancel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF222222),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                        ) {
                            Icon(Icons.Default.Close, "Hủy bỏ ảnh chụp", tint = Color.LightGray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("HỦY BỎ", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFCC00),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1.5f).height(56.dp)
                        ) {
                            Icon(Icons.Default.Check, "Gửi khoảnh khắc này", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GỬI ĐI", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            // Màn hình loading khi đang gửi
            if (isCapturing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFFFCC00), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ĐANG ĐĂNG Lên LOCKET...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}