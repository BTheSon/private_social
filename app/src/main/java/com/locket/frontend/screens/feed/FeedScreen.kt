package com.locket.frontend.screens.feed

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.locket.backend.domain.post.PostModel
import com.locket.frontend.screens.feed.component.FeedFriendSuggest
import com.locket.frontend.screens.feed.component.FeedPostItem

@Composable
fun FeedScreen(
    posts: List<PostModel>,
    currentUserId: String,
    onLikeToggle: (String, Boolean) -> Unit,
    onClickSyncContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(posts) { index, post ->
            FeedPostItem(
                post = post,
                currentUserId = currentUserId,
                onLikeToggle = onLikeToggle
            )
            
            if (index == 3) { // Vị trí thứ 4 (0-indexed)
                FeedFriendSuggest(
                    onPermissionRequested = onClickSyncContacts
                )
            }
        }
    }
}
