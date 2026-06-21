package com.locket.frontend.screens.friend.tab.friendsuggestions

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.locket.backend.domain.contact.ContactProvider
import com.locket.backend.domain.friend.FriendModel
import com.locket.backend.domain.friend.FriendViewModel
import com.locket.backend.domain.friend.SuggestionUiState

@Composable
fun TabFriendSuggestions(viewModel: FriendViewModel) {
    val context = LocalContext.current
    val state by viewModel.suggestionState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.loadFriendSuggestions() else viewModel.onContactPermissionDenied()
    }

    LaunchedEffect(Unit) {
        viewModel.initContactRepository(ContactProvider(context.applicationContext))
        
        viewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val s = state) {
            is SuggestionUiState.Idle -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Đồng bộ danh bạ để tìm bạn bè.",
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        if (viewModel.hasContactPermission()) {
                            viewModel.loadFriendSuggestions()
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    }) {
                        Text("Tải danh bạ")
                    }
                }
            }

            is SuggestionUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is SuggestionUiState.NoPermission -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Cho phép truy cập danh bạ để hiển thị danh sách.",
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }) {
                        Text("Cấp quyền")
                    }
                }
            }

            is SuggestionUiState.Error -> {
                Text(
                    s.message,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
            }

            is SuggestionUiState.Success -> {
                if (s.suggestions.isEmpty()) {
                    Text(
                        "Không có dữ liệu danh bạ.",
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(s.suggestions, key = { it.phoneNumber }) { friend ->
                            SuggestionRow(
                                friend = friend,
                                onAddClick = { viewModel.onAddContactFriendClicked(friend.phoneNumber) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(friend: FriendModel, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color.LightGray, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(friend.displayName.firstOrNull()?.uppercase() ?: "?", fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(friend.displayName, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.Gray)
            Text(friend.phoneNumber, color = Color.Gray, fontSize = 13.sp)
        }
        OutlinedButton(onClick = onAddClick) {
            Text("Kết bạn")
        }
    }
}