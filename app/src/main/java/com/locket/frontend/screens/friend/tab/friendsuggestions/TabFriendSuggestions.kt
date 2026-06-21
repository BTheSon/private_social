package com.locket.frontend.screens.friend.tab.friendsuggestions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.locket.backend.domain.friend.FriendViewModel

@Composable
fun TabFriendSuggestions(viewModel: FriendViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Gợi ý kết bạn từ danh bạ sẽ xuất hiện ở đây.", color = Color.DarkGray, textAlign = TextAlign.Center, fontSize = 15.sp)
    }
}