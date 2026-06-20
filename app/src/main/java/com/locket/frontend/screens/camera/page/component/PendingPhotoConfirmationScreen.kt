package com.locket.frontend.screens.camera.page.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.locket.backend.domain.music.SongModel
import java.io.File

@Composable
fun PendingPhotoConfirmationScreen(
    pendingPhotoFile: File?,
    isCapturing: Boolean,
    caption: String,
    onCaptionChange: (String) -> Unit,
    selectedSong: SongModel?,
    onAddMusicClick: () -> Unit,
    onRemoveSong: () -> Unit,
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Tiêu đề
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 16.dp)
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
                        text = "Thêm caption và nhạc trước khi gửi",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Ảnh preview 1:1
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
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
                        contentDescription = "Ảnh chờ đăng tải",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(2.dp, Color.Black.copy(alpha = 0.2f)), shape = RoundedCornerShape(46.dp))
                    )
                }

                // Caption & Music
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Ô nhập caption
                    OutlinedTextField(
                        value = caption,
                        onValueChange = onCaptionChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Viết caption...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFCC00),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFCC00),
                            focusedContainerColor = Color(0xFF1A1A1A),
                            unfocusedContainerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        )
                    )

                    // Khu vực nhạc
                    if (selectedSong != null) {
                        // Chip hiển thị bài hát đã chọn
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFFFCC00).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFFFFCC00),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedSong.trackName,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = selectedSong.artistName,
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = onRemoveSong, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Bỏ nhạc",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // Nút thêm nhạc
                        OutlinedButton(
                            onClick = onAddMusicClick,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF333333)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                        ) {
                            Icon(
                                Icons.Default.MusicOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Thêm nhạc (tùy chọn)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Nút HỦY BỎ / GỬI ĐI
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onCancel,
                            enabled = !isCapturing,
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
                            Icon(Icons.Default.Close, "Hủy bỏ", tint = Color.LightGray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("HỦY BỎ", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = onConfirm,
                            enabled = !isCapturing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFCC00),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1.5f).height(56.dp)
                        ) {
                            Icon(Icons.Default.Check, "Gửi khoảnh khắc", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GỬI ĐI", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            // Loading overlay khi đang upload
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
                            text = "ĐANG ĐĂNG LÊN LOCKET...",
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