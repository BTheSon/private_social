package com.locket.frontend.screens.friend.tab.friendlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.locket.backend.domain.friend.FriendModel
import com.locket.backend.domain.friend.FriendViewModel
import com.locket.frontend.screens.friend.FriendActionConfirmDialog
import com.locket.frontend.screens.friend.FriendRowItem

@Composable
fun TabFriendList(
    viewModel: FriendViewModel,
    friendsList: List<FriendModel>
) {
    // State độc lập cho tab này
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingTargetItem by remember { mutableStateOf<FriendModel?>(null) }

    if (friendsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bạn chưa có người bạn nào.", color = Color.DarkGray, textAlign = TextAlign.Center, fontSize = 15.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(friendsList, key = { it.phoneNumber }) { item ->
                FriendRowItem(
                    item = item,
                    onSendRequest = {}, onAcceptRequest = {}, onDeclineRequestClick = {}, onCancelRequestClick = {},
                    onUnfriendClick = {
                        pendingTargetItem = item
                        showConfirmDialog = true
                    }
                )
            }
        }
    }

    if (showConfirmDialog && pendingTargetItem != null) {
        FriendActionConfirmDialog(
            dialogType = "UNFRIEND",
            targetName = pendingTargetItem!!.displayName,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                viewModel.removeFriendship(pendingTargetItem!!.phoneNumber)
                showConfirmDialog = false
                pendingTargetItem = null
            }
        )
    }
}