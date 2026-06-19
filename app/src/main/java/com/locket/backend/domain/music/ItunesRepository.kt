package com.locket.backend.domain.music

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItunesRepository(private val apiService: ItunesApiService) {
    suspend fun searchSongs(query: String): List<SongModel> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchSongs(query)
                response.results
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
