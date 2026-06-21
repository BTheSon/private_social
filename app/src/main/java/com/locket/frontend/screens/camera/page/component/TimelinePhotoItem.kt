package com.locket.frontend.screens.camera.page.component

import android.media.MediaPlayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.locket.backend.common.util.DateTimeUtils
import com.locket.backend.domain.post.PostModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelinePhotoItem(
    post: PostModel,
    modifier: Modifier = Modifier,
    isDraft: Boolean = false,
    draftStatus: String? = null,
    onRetry: () -> Unit = {}
) {
    val formattedDateTime = run {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(post.createdAt))
    }
    val parts = formattedDateTime.split(" ")
    val dateStr = parts.getOrNull(0) ?: ""
    val timeStr = parts.getOrNull(1) ?: ""

    val mediaPlayer = remember { MediaPlayer() }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(post.id) {
        onDispose {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Tên người đăng
            val displayAuthorName = post.authorName.ifEmpty {
                val shortId =
                    if (post.userId.length >= 5) post.userId.takeLast(3) else post.userId.padEnd(3, '0')
                "noname-$shortId"
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF333333), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = displayAuthorName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Ảnh bài đăng (load từ URL Supabase hoặc local draft)
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = if (isDraft) java.io.File(post.imageUrl) else post.imageUrl,
                    contentDescription = "Khoảnh khắc Locket",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(52.dp))
                        .background(Color(0xFF191919))
                        .border(
                            6.dp,
                            Brush.linearGradient(listOf(Color(0xFFFFCC00), Color(0xFFFF9100))),
                            RoundedCornerShape(52.dp)
                        )
                        .padding(6.dp)
                        .clip(RoundedCornerShape(46.dp)),
                    contentScale = ContentScale.Crop
                )

                if (isDraft) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(52.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (draftStatus == "FAILED") {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Đăng bài thất bại", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00))
                            ) {
                                Text("Thử lại", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            CircularProgressIndicator(color = Color(0xFFFFCC00))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Đang tải lên...", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                // Caption
                if (post.caption.isNotEmpty()) {
                    Text(
                        text = post.caption,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Bài hát đính kèm
                if (!post.songName.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = post.songName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            if (!post.artistName.isNullOrEmpty()) {
                                Text(
                                    text = post.artistName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        if (!post.previewUrl.isNullOrEmpty()) {
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        mediaPlayer.pause()
                                        isPlaying = false
                                    } else {
                                        if (mediaPlayer.duration > 0) {
                                            mediaPlayer.start()
                                            isPlaying = true
                                        } else {
                                            mediaPlayer.reset()
                                            mediaPlayer.setDataSource(post.previewUrl)
                                            mediaPlayer.prepareAsync()
                                            mediaPlayer.setOnPreparedListener {
                                                it.start()
                                                isPlaying = true
                                            }
                                            mediaPlayer.setOnCompletionListener {
                                                isPlaying = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF333333), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Nghe nhạc",
                                    tint = Color(0xFFFFCC00),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Thời gian đăng
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Gửi lúc $timeStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("•", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
