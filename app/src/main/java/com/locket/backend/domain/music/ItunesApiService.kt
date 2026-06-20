package com.locket.backend.domain.music

import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApiService {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("limit") limit: Int = 10
    ): ItunesSearchResponse
}
