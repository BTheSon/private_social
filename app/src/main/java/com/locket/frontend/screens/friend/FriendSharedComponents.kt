package com.locket.frontend.screens.friend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.locket.backend.domain.friend.FriendModel

// 1. Item hiển thị 1 User (Dùng chung cho mọi Tab)
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
        if (item.avatarUrl.isNotEmpty()) {
            AsyncImage(
                model = item.avatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
            )
        } else {
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
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = item.phoneNumber, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }

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
                ) { Text("Hủy", color = Color(0xFFE57373), fontSize = 12.sp) }
            }
            "RECEIVED" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onAcceptRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) { Text("Nhận", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

                    OutlinedButton(
                        onClick = onDeclineRequestClick,
                        border = BorderStroke(1.dp, Color(0xFFE57373)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) { Text("Xóa", color = Color(0xFFE57373), fontSize = 11.sp) }
                }
            }
            "FRIEND" -> {
                OutlinedButton(
                    onClick = onUnfriendClick,
                    border = BorderStroke(1.dp, Color(0xFFE57373)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Hủy kết bạn", color = Color(0xFFE57373), fontSize = 12.sp) }
            }
        }
    }
}

// 2. Dialog Xác nhận dùng chung
@Composable
fun FriendActionConfirmDialog(
    dialogType: String,
    targetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val dialogTitle = when (dialogType) {
        "UNFRIEND" -> "Xóa kết bạn?"
        "CANCEL_INVITE" -> "Hủy lời mời?"
        else -> "Xóa lời mời?"
    }
    val dialogMessage = when (dialogType) {
        "UNFRIEND" -> "Bạn có chắc muốn hủy kết bạn với $targetName? Hành động này không thể hoàn tác."
        "CANCEL_INVITE" -> "Bạn có chắc muốn rút lại lời mời kết bạn gửi tới $targetName?"
        else -> "Bạn có chắc muốn xóa lời mời kết bạn từ $targetName? Đối phương sẽ không biết bạn đã xóa."
    }
    val actionButtonText = when (dialogType) {
        "UNFRIEND" -> "Xóa bạn"
        "CANCEL_INVITE" -> "Xác nhận hủy"
        else -> "Xóa lời mời"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = dialogTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = { Text(text = dialogMessage, color = Color.LightGray, fontSize = 14.sp, lineHeight = 20.sp) },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Quay lại", color = Color.Gray, fontWeight = FontWeight.SemiBold) }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                shape = RoundedCornerShape(12.dp)
            ) { Text(text = actionButtonText, color = Color.White, fontWeight = FontWeight.Bold) }
        }
    )
}