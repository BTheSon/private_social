package com.locket.frontend.screens.feed.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.locket.backend.domain.post.PostModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedPostItem(
    post: PostModel,
    currentUserId: String,
    onLikeToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLiked by remember { mutableStateOf(post.likedBy.contains(currentUserId)) }
    var showHeartAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (!isLiked) {
                                isLiked = true
                                onLikeToggle(post.id, true)
                            }
                            showHeartAnimation = true
                            coroutineScope.launch {
                                delay(1000)
                                showHeartAnimation = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(post.imageUrl),
                contentDescription = "Post Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = showHeartAnimation,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Like Heart",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                isLiked = !isLiked
                onLikeToggle(post.id, isLiked)
            }) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            if (post.songName != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎵 ${post.songName} - ${post.artistName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (post.caption.isNotEmpty()) {
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
