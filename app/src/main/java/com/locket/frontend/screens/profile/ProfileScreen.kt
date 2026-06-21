package com.locket.frontend.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.locket.backend.domain.profile.ProfileViewModel
import com.locket.frontend.screens.profile.dialog.EditNameDialog
import com.locket.frontend.screens.profile.dialog.LogoutDialog

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isUploading by viewModel.isUploadingAvatar.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }

    // Launcher mở thư viện ảnh
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
            if (bytes != null) {
                viewModel.uploadAndChangeAvatar(bytes)
            }
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFF0F0F0F))
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Text(
            text = "HỒ SƠ CỦA TÔI",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 48.dp) // Tăng khoảng cách dưới
        )

        // --- AVATAR SECTION ---
        Box(
            modifier = Modifier
                .size(130.dp)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            val avatarUrl = currentUser?.avatarUrl ?: ""
            val displayName = currentUser?.displayName ?: "U"

            if (avatarUrl.isNotEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFFFFCC00), CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0xFF1E1E1E), CircleShape)
                        .border(2.dp, Color(0xFFFFCC00), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(1).uppercase(),
                        color = Color(0xFFFFCC00),
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp
                    )
                }
            }

            // Nút Camera nhỏ ở góc dưới
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-8).dp, y = (-8).dp)
                    .size(36.dp)
                    .background(Color(0xFFFFCC00), CircleShape)
                    .border(2.dp, Color(0xFF0F0F0F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Đổi ảnh", tint = Color.Black, modifier = Modifier.size(20.dp))
            }

            // Lớp phủ loading khi đang up ảnh
            if (isUploading) {
                Box(
                    modifier = Modifier.size(120.dp).background(Color(0x88000000), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFCC00))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- NAME SECTION ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { showEditNameDialog = true }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = currentUser?.displayName ?: "Đang tải...",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Edit, contentDescription = "Đổi tên", tint = Color.Gray, modifier = Modifier.size(18.dp))
        }

        // --- SỐ ĐIỆN THOẠI ---
        Text(
            text = currentUser?.phoneNumber ?: "",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 64.dp) // Đẩy nút Đăng xuất xuống dưới một chút
        )

        // --- NÚT ĐĂNG XUẤT ---
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(0.6f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2D2D)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Đăng xuất", tint = Color(0xFFE57373), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Đăng xuất", color = Color(0xFFE57373), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    // --- CÁC HỘP THOẠI ---
    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout(onLogoutSuccess = onLogoutClick)
            }
        )
    }

    if (showEditNameDialog) {
        EditNameDialog(
            currentName = currentUser?.displayName ?: "",
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                viewModel.updateDisplayName(newName)
                showEditNameDialog = false
            }
        )
    }
}