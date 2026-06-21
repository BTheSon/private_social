package com.locket.frontend.screens.post

import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.locket.backend.domain.music.SongModel

@Composable
fun SongSearchDialog(
    onDismissRequest: () -> Unit,
    onSongSelected: (SongModel) -> Unit,
    searchSongs: (String) -> Unit,
    searchResults: List<SongModel>
) {
    var query by remember { mutableStateOf("") }
    val mediaPlayer = remember { MediaPlayer() }
    var playingUrl by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        searchSongs("") // Trigger initial load
    }

    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Text(
                    text = "Chọn nhạc đính kèm",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        searchSongs(it)
                    },
                    placeholder = { Text("Tìm bài hát, nghệ sĩ...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFCC00),
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFFFCC00)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSongSelected(song) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = song.artworkUrl100,
                                contentDescription = "Artwork",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = song.trackName, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(text = song.artistName, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1)
                            }
                            
                            // Nút nghe thử
                            if (!song.previewUrl.isNullOrEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (playingUrl == song.previewUrl) {
                                            if (mediaPlayer.isPlaying) {
                                                mediaPlayer.pause()
                                                isPlaying = false
                                            } else {
                                                mediaPlayer.start()
                                                isPlaying = true
                                            }
                                        } else {
                                            mediaPlayer.reset()
                                            mediaPlayer.setDataSource(song.previewUrl)
                                            mediaPlayer.prepareAsync()
                                            mediaPlayer.setOnPreparedListener { 
                                                it.start()
                                                isPlaying = true
                                            }
                                            mediaPlayer.setOnCompletionListener {
                                                isPlaying = false
                                                playingUrl = null
                                            }
                                            playingUrl = song.previewUrl
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF333333), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (playingUrl == song.previewUrl && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Nghe thử",
                                        tint = Color(0xFFFFCC00)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
