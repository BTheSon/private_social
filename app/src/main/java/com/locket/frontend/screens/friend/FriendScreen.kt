package com.locket.frontend.screens.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.locket.backend.domain.friend.FriendModel
import com.locket.backend.domain.friend.FriendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendScreen(
    viewModel: FriendViewModel,
    modifier: Modifier = Modifier
) {
    val activeSubTab by viewModel.activeSubTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val displayList by viewModel.uiFriendsList.collectAsState()

    var inviteSubTab by remember { mutableIntStateOf(0) }

    // State điều khiển các hộp thoại
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingTargetItem by remember { mutableStateOf<FriendModel?>(null) }
    var dialogType by remember { mutableStateOf("") }

    // State điều khiển Hộp thoại tìm kiếm Firebase
    var showFirebaseSearchDialog by remember { mutableStateOf(false) }

    val filteredList = remember(displayList, activeSubTab, inviteSubTab, searchQuery) {
        if (searchQuery.isNotEmpty()) {
            displayList
        } else {
            when (activeSubTab) {
                1 -> {
                    if (inviteSubTab == 0) {
                        displayList.filter { it.relationStatus == "RECEIVED" }
                    } else {
                        displayList.filter { it.relationStatus == "SENT" }
                    }
                }
                else -> displayList
            }
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFF0F0F0F))
            .statusBarsPadding()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // THANH TIÊU ĐỀ MỚI: Chứa Text và Nút tìm kiếm Firebase
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KẾT NỐI BẠN BÈ",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            // Nút mở Dialog tìm kiếm toàn cầu
            IconButton(
                onClick = { showFirebaseSearchDialog = true },
                modifier = Modifier
                    .background(Color(0xFF1E1E1E), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Thêm bạn mới",
                    tint = Color(0xFFFFCC00),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Thanh tìm kiếm nội bộ (Local)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Tìm trong danh sách của bạn...", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFFCC00)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFCC00),
                unfocusedBorderColor = Color(0xFF2D2D2D),
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFFFCC00)
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Phân đoạn chọn Tab chính
        if (searchQuery.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabs = listOf("Danh sách", "Lời mời", "Gợi ý")
                tabs.forEachIndexed { index, title ->
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
                        Text(
                            text = title,
                            color = if (isSelected) Color.Black else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Thanh điều hướng Tab con
            if (activeSubTab == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val inviteTabs = listOf("Đã nhận", "Đã gửi")
                    inviteTabs.forEachIndexed { index, title ->
                        val isInnerSelected = inviteSubTab == index
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isInnerSelected) Color(0x22FFFFCC) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isInnerSelected) Color(0xFFFFCC00) else Color(0xFF2D2D2D),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickableWithoutRipple { inviteSubTab = index }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isInnerSelected) Color(0xFFFFCC00) else Color.Gray,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Vùng hiển thị danh sách
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "Không tìm thấy người dùng!" else "Danh sách trống rỗng...",
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredList, key = { it.phoneNumber }) { item ->
                    FriendRowItem(
                        item = item,
                        onSendRequest = { viewModel.sendRequest(item.phoneNumber) },
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
                        },
                        onUnfriendClick = {
                            pendingTargetItem = item
                            dialogType = "UNFRIEND"
                            showConfirmDialog = true
                        }
                    )
                }
            }
        }
    }

    // --- CÁC HỘP THOẠI (DIALOGS) ---

    // 1. Hộp thoại tìm kiếm Firebase
    if (showFirebaseSearchDialog) {
        FirebaseSearchDialog(
            viewModel = viewModel,
            onDismiss = { showFirebaseSearchDialog = false }
        )
    }

    // 2. Hộp thoại xác nhận (Xóa/Hủy)
    if (showConfirmDialog && pendingTargetItem != null) {
        val displayName = pendingTargetItem?.displayName ?: ""

        val dialogTitle = when (dialogType) {
            "UNFRIEND" -> "Xóa kết bạn?"
            "CANCEL_INVITE" -> "Hủy lời mời?"
            else -> "Xóa lời mời?"
        }

        val dialogMessage = when (dialogType) {
            "UNFRIEND" -> "Bạn có chắc muốn hủy kết bạn với $displayName? Hành động này không thể hoàn tác."
            "CANCEL_INVITE" -> "Bạn có chắc muốn rút lại lời mời kết bạn gửi tới $displayName?"
            else -> "Bạn có chắc muốn xóa lời mời kết bạn từ $displayName? Đối phương sẽ không biết bạn đã xóa."
        }

        val actionButtonColor = Color(0xFFE57373)
        val actionTextColor = Color.White
        val actionButtonText = when (dialogType) {
            "UNFRIEND" -> "Xóa bạn"
            "CANCEL_INVITE" -> "Xác nhận hủy"
            else -> "Xóa lời mời"
        }

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = dialogTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = dialogMessage,
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Quay lại", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingTargetItem?.let { viewModel.removeFriendship(it.phoneNumber) }
                        showConfirmDialog = false
                        pendingTargetItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = actionButtonColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = actionButtonText,
                        color = actionTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

// COMPONENT: Hộp thoại Tìm kiếm Firebase
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseSearchDialog(
    viewModel: FriendViewModel,
    onDismiss: () -> Unit
) {
    var phoneInput by remember { mutableStateOf("") }

    // Đọc state từ ViewModel
    val searchResult by viewModel.firebaseSearchResult.collectAsState()
    val isSearching by viewModel.isSearchingFirebase.collectAsState()

    Dialog(onDismissRequest = {
        viewModel.clearFirebaseSearch()
        onDismiss()
    }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TÌM BẠN MỚI",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Ô nhập số điện thoại
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    placeholder = { Text("Nhập số điện thoại (+84...)", color = Color.Gray, fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFCC00),
                        unfocusedBorderColor = Color(0xFF2D2D2D),
                        focusedContainerColor = Color(0xFF0F0F0F),
                        unfocusedContainerColor = Color(0xFF0F0F0F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nút tìm kiếm
                Button(
                    onClick = {
                        if (phoneInput.isNotEmpty()) {
                            viewModel.searchUserOnFirebase(phoneInput)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isSearching
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Tìm Kiếm", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Vùng hiển thị kết quả
                if (searchResult != null) {
                    if (searchResult!!.phoneNumber.isEmpty()) {
                        // Kết quả trả về rỗng -> Không tìm thấy
                        Text(
                            text = "Không tìm thấy người dùng này trên hệ thống.",
                            color = Color(0xFFE57373),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Tìm thấy user -> Hiển thị Card User
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F0F0F), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF2D2D2D), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF2D2D2D), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = searchResult!!.displayName.take(1).uppercase(),
                                    color = Color(0xFFFFCC00),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = searchResult!!.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = searchResult!!.phoneNumber, color = Color.Gray, fontSize = 12.sp)
                            }

                            // Nút kết bạn
                            IconButton(
                                onClick = {
                                    viewModel.sendRequest(searchResult!!.phoneNumber)
                                    viewModel.clearFirebaseSearch()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .background(Color(0xFFFFCC00), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Thêm", tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}