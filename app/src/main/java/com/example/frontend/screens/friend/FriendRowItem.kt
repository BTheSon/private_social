package com.example.frontend.screens.friend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backend.domain.friend.FriendModel

@Composable
fun FriendRowItem(
    item: FriendModel,
    onSendRequest: () -> Unit,
    onAcceptRequest: () -> Unit,
    onDeclineRequestClick: () -> Unit,
    onCancelRequestClick: () -> Unit,
    onUnfriendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thiết kế Avatar vòng tròn nghệ thuật ký tự đầu
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color(0xFF2D2D2D), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.displayName.take(1).uppercase(),
                color = Color(0xFFFFCC00),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Chi tiết văn bản hiển thị
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.displayName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = item.phoneNumber,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Các nút hành động tương tác thông minh
        when (item.relationStatus) {
            "NONE" -> {
                Button(
                    onClick = onSendRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kết bạn", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            "SENT" -> {
                OutlinedButton(
                    onClick = onCancelRequestClick,
                    border = BorderStroke(1.dp, Color(0xFFE57373)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hủy", color = Color(0xFFE57373), fontSize = 12.sp)
                }
            }
            "RECEIVED" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onAcceptRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Nhận", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDeclineRequestClick,
                        border = BorderStroke(1.dp, Color(0xFFE57373)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Xóa", color = Color(0xFFE57373), fontSize = 11.sp)
                    }
                }
            }
            "FRIEND" -> {
                OutlinedButton(
                    onClick = onUnfriendClick,
                    border = BorderStroke(1.dp, Color(0xFFE57373)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hủy kết bạn", color = Color(0xFFE57373), fontSize = 12.sp)
                }
            }
        }
    }
}