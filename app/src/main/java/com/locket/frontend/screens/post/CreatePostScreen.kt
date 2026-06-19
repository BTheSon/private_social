package com.locket.frontend.screens.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.locket.backend.domain.music.SongModel

@Composable
fun CreatePostScreen(
    initialImageUri: Uri?,
    onImagePicked: (Uri?) -> Unit,
    onPost: (Uri?, String, SongModel?) -> Unit,
    onSearchMusic: () -> Unit,
    selectedSong: SongModel?,
    modifier: Modifier = Modifier
) {
    var caption by remember { mutableStateOf("") }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImagePicked(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (initialImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(initialImageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Chưa chọn ảnh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { galleryLauncher.launch("image/*") }) {
            Text("Chọn ảnh từ thư viện")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = { Text("Thêm caption...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onSearchMusic) {
                Text(selectedSong?.trackName ?: "Thêm nhạc")
            }

            Button(onClick = { onPost(initialImageUri, caption, selectedSong) }) {
                Text("Đăng bài")
            }
        }
    }
}
