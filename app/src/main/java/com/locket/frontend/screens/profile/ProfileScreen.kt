package com.locket.frontend.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.locket.backend.domain.profile.ProfileViewModel
import com.locket.frontend.screens.profile.dialog.LogoutDialog

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    // State điều khiển Hộp thoại xác nhận đăng xuất
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(Color(0xFF0F0F0F)) // Deep Cinema Black
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val displayName = currentUser?.displayName ?: "Đang tải..."
        val initial = if (displayName.isNotEmpty() && displayName != "Đang tải...") {
            displayName.take(1).uppercase()
        } else {
            ""
        }

        // Avatar Card
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFF1E1E1E), CircleShape)
                .border(2.dp, Color(0xFFFFCC00), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color(0xFFFFCC00),
                fontWeight = FontWeight.Black,
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display Name
        Text(
            text = displayName,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Số điện thoại
        Text(
            text = currentUser?.phoneNumber ?: "",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Nút Đăng xuất
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Đăng xuất",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Đăng xuất",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }

    // GỌI COMPONENT DIALOG TỪ FILE VỪA TÁCH
    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout(onLogoutSuccess = onLogoutClick)
            }
        )
    }
}