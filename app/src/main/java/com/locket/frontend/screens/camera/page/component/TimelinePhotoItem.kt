package com.locket.frontend.screens.camera.page.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.locket.backend.common.util.DateTimeUtils
import com.locket.backend.domain.photo.PhotoEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelinePhotoItem(
    photo: PhotoEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDateTime = DateTimeUtils.formatTimestamp(photo.timestamp)
    val parts = formattedDateTime.split(" ")
    val dateStr = parts.getOrNull(0) ?: ""
    val timeStr = parts.getOrNull(1)?.let {
        val timeParts = it.split(":")
        if (timeParts.size >= 2) "${timeParts[0]}:${timeParts[1]}" else it
    } ?: ""

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_photo_item_${photo.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            AsyncImage(
                model = photo.filePath,
                contentDescription = "Timeline snapshot file",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(52.dp))
                    .background(Color(0xFF191919))
                    .border(6.dp, Brush.linearGradient(listOf(Color(0xFFFFCC00), Color(0xFFFF9100))), RoundedCornerShape(52.dp))
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
                Text(
                    text = "Khoảnh khắc Locket #${photo.id}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gửi lúc $timeStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
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

