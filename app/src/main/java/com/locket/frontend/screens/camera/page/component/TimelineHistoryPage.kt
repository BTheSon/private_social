package com.locket.frontend.screens.camera.page.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.locket.backend.domain.database.DraftEntity
import com.locket.backend.domain.post.PostModel

@Composable
fun TimelineHistoryPage(
    posts: List<PostModel>,
    failedDrafts: List<DraftEntity> = emptyList(),
    currentUserId: String = "",
    onRetryDraft: (DraftEntity) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onBackClick: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { posts.size + failedDrafts.size })

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
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x1AFFCC00), CircleShape)
                    .border(1.dp, Color(0xFFFFCC00).copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    "Quay về máy ảnh",
                    tint = Color(0xFFFFCC00),
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "${posts.size} khoảnh khắc",
                color = Color.Gray,
                style = MaterialTheme.typography.labelMedium
            )
        }

        HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)

        // Content
        if (posts.isEmpty() && failedDrafts.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoAlbum,
                        "Album trống",
                        tint = Color(0xFF222222),
                        modifier = Modifier.size(92.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Chưa có khoảnh khắc nào",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Hãy chụp ảnh và gửi đi khoảnh khắc đầu tiên của bạn!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                key = { page -> 
                    if (page < failedDrafts.size) failedDrafts[page].id
                    else posts[page - failedDrafts.size].id.ifEmpty { page.toString() }
                }
            ) { page ->
                if (page < failedDrafts.size) {
                    val draft = failedDrafts[page]
                    val mappedPost = PostModel(
                        id = draft.id,
                        userId = "",
                        imageUrl = draft.imageUri,
                        caption = draft.caption,
                        songName = draft.songName,
                        artistName = draft.artistName,
                        previewUrl = draft.previewUrl,
                        createdAt = draft.createdAt
                    )
                    TimelinePhotoItem(
                        post = mappedPost,
                        modifier = Modifier.fillMaxSize(),
                        isDraft = true,
                        draftStatus = draft.status,
                        onRetry = { onRetryDraft(draft) }
                    )
                } else {
                    val post = posts[page - failedDrafts.size]
                    TimelinePhotoItem(
                        post = post,
                        modifier = Modifier.fillMaxSize(),
                        currentUserId = currentUserId,
                        onDelete = { onDeletePost(post.id) }
                    )
                }
            }
        }
    }
}