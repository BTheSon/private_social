package com.locket.frontend.screens.camera.page.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.locket.backend.common.util.DateTimeUtils
import com.locket.backend.domain.post.PostModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelinePhotoItem(
    post: PostModel,
    modifier: Modifier = Modifier
) {
    val formattedDateTime = run {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(post.createdAt))
    }
    val parts = formattedDateTime.split(" ")
    val dateStr = parts.getOrNull(0) ?: ""
    val timeStr = parts.getOrNull(1) ?: ""

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Ảnh bài đăng (load từ URL Supabase)
            AsyncImage(
                model = post.imageUrl,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.songName}${if (!post.artistName.isNullOrEmpty()) " - ${post.artistName}" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFCC00),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
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
