package com.locket.frontend.screens.profile.dialog

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E), // Đồng bộ nền thẻ xám cao cấp
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Đăng xuất?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này không? Bạn sẽ phải xác thực lại mã OTP để vào lại.",
                color = Color.LightGray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Quay lại", color = Color.Gray, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)), // Tone đỏ đồng bộ
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Đăng xuất",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}