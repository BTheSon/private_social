package com.locket.frontend.screens.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.locket.backend.domain.friend.FriendViewModel
import com.locket.frontend.screens.friend.tab.friendsuggestions.TabFriendSuggestions
import com.locket.frontend.screens.friend.tab.friendinvites.TabFriendInvites
import com.locket.frontend.screens.friend.tab.friendlist.TabFriendList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendScreen(
    viewModel: FriendViewModel,
    modifier: Modifier = Modifier
) {
    val activeSubTab by viewModel.activeSubTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val displayList by viewModel.uiFriendsList.collectAsState() // List được filter từ ViewModel theo tab chính

    var showFirebaseSearchDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(Color(0xFF0F0F0F))
            .statusBarsPadding()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // THANH TIÊU ĐỀ
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "KẾT NỐI BẠN BÈ", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            IconButton(
                onClick = { showFirebaseSearchDialog = true },
                modifier = Modifier.background(Color(0xFF1E1E1E), CircleShape).size(40.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Thêm bạn", tint = Color(0xFFFFCC00), modifier = Modifier.size(22.dp))
            }
        }

        // THANH TÌM KIẾM
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text("Tìm trong danh sách của bạn...", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFFCC00)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) { Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray) }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFCC00),
                unfocusedBorderColor = Color(0xFF2D2D2D),
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // CHUYỂN HƯỚNG GIAO DIỆN
        if (searchQuery.isNotEmpty()) {
            // Đang tìm kiếm -> Hiển thị kết quả tìm kiếm toàn cục
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(displayList, key = { it.phoneNumber }) { item ->
                    FriendRowItem(
                        item = item,
                        onSendRequest = { viewModel.sendRequest(item.phoneNumber) },
                        onAcceptRequest = { viewModel.acceptRequest(item.phoneNumber) },
                        onDeclineRequestClick = { viewModel.removeFriendship(item.phoneNumber) }, // Bỏ qua dialog khi đang search cho mượt
                        onCancelRequestClick = { viewModel.removeFriendship(item.phoneNumber) },
                        onUnfriendClick = { viewModel.removeFriendship(item.phoneNumber) }
                    )
                }
            }
        } else {
            // Không tìm kiếm -> Hiển thị Thanh Tab chính
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)).padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Danh sách", "Lời mời", "Gợi ý").forEachIndexed { index, title ->
                    val isSelected = activeSubTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color(0xFFFFCC00) else Color.Transparent)
                            .clickableWithoutRipple { viewModel.setSubTab(index) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(title, color = if (isSelected) Color.Black else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Gọi các component độc lập tùy theo tab đang chọn
            when (activeSubTab) {
                0 -> TabFriendList(viewModel = viewModel, friendsList = displayList)
                1 -> TabFriendInvites(viewModel = viewModel, invitesList = displayList)
                2 -> TabFriendSuggestions(viewModel = viewModel)
            }
        }
    }

    if (showFirebaseSearchDialog) {
        FirebaseSearchDialog(
            viewModel = viewModel,
            onDismiss = { showFirebaseSearchDialog = false }
        )
    }
}