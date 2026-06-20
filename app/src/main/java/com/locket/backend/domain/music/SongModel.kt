package com.locket.backend.domain.music

import com.google.gson.annotations.SerializedName

data class SongModel(
    @SerializedName("trackName") val trackName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("artworkUrl100") val artworkUrl100: String,
    @SerializedName("previewUrl") val previewUrl: String?
)

data class ItunesSearchResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<SongModel>
)
