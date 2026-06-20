package com.locket.frontend.screens.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.locket.backend.domain.music.SongModel

@Composable
fun SongSearchDialog(
    onDismissRequest: () -> Unit,
    onSongSelected: (SongModel) -> Unit,
    searchSongs: (String) -> Unit,
    searchResults: List<SongModel>
) {
    var query by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        searchSongs(it)
                    },
                    label = { Text("Tìm bài hát...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(searchResults) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSongSelected(song) }
                                .padding(vertical = 8.dp)
                        ) {
                            Column {
                                Text(text = song.trackName, style = MaterialTheme.typography.bodyLarge)
                                Text(text = song.artistName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
