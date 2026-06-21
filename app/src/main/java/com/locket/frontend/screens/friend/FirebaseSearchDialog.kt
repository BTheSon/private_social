package com.locket.frontend.screens.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.locket.backend.domain.friend.FriendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseSearchDialog(
    viewModel: FriendViewModel,
    onDismiss: () -> Unit
) {
    var phoneInput by remember { mutableStateOf("") }
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
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFFFCC00)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                if (searchResult != null) {
                    if (searchResult!!.phoneNumber.isEmpty()) {
                        Text(
                            text = "Không tìm thấy người dùng này trên hệ thống.",
                            color = Color(0xFFE57373),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F0F0F), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF2D2D2D), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

                            // --- LOGIC MỚI: Hiển thị UI Tương ứng với trạng thái quan hệ ---
                            when (searchResult!!.relationStatus) {
                                "NONE" -> {
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
                                "FRIEND" -> {
                                    Text("Đã là bạn", color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                "SENT" -> {
                                    Text("Đã gửi lời mời", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                "RECEIVED" -> {
                                    Text("Chờ xác nhận", color = Color(0xFFFFCC00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}