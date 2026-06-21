package com.locket.frontend.screens.friend.tab.friendinvites

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.locket.backend.domain.friend.FriendModel
import com.locket.backend.domain.friend.FriendViewModel
import com.locket.frontend.screens.friend.FriendActionConfirmDialog
import com.locket.frontend.screens.friend.FriendRowItem
import com.locket.frontend.screens.friend.clickableWithoutRipple

@Composable
fun TabFriendInvites(
    viewModel: FriendViewModel,
    invitesList: List<FriendModel>
) {
    // State độc lập cho sub-tab và dialog
    var inviteSubTab by remember { mutableIntStateOf(0) } // 0: Đã nhận, 1: Đã gửi
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingTargetItem by remember { mutableStateOf<FriendModel?>(null) }
    var dialogType by remember { mutableStateOf("") }

    // Tự lọc dữ liệu nội bộ
    val filteredList = if (inviteSubTab == 0) {
        invitesList.filter { it.relationStatus == "RECEIVED" }
    } else {
        invitesList.filter { it.relationStatus == "SENT" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // UI chuyển đổi Sub-tab
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Đã nhận", "Đã gửi").forEachIndexed { index, title ->
                val isSelected = inviteSubTab == index
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0x22FFFFCC) else Color.Transparent)
                        .border(1.dp, if (isSelected) Color(0xFFFFCC00) else Color(0xFF2D2D2D), RoundedCornerShape(20.dp))
                        .clickableWithoutRipple { inviteSubTab = index }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(title, color = if (isSelected) Color(0xFFFFCC00) else Color.Gray, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                }
            }
        }

        // Danh sách hiển thị
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Không có lời mời nào.", color = Color.DarkGray, textAlign = TextAlign.Center, fontSize = 15.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
                items(filteredList, key = { it.phoneNumber }) { item ->
                    FriendRowItem(
                        item = item,
                        onSendRequest = {}, onUnfriendClick = {},
                        onAcceptRequest = { viewModel.acceptRequest(item.phoneNumber) },
                        onDeclineRequestClick = {
                            pendingTargetItem = item
                            dialogType = "DECLINE_INVITE"
                            showConfirmDialog = true
                        },
                        onCancelRequestClick = {
                            pendingTargetItem = item
                            dialogType = "CANCEL_INVITE"
                            showConfirmDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showConfirmDialog && pendingTargetItem != null) {
        FriendActionConfirmDialog(
            dialogType = dialogType,
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