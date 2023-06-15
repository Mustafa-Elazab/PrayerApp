package com.mostafa.alaymiatask.data.remote.dto


import com.google.gson.annotations.SerializedName

data class QiblaResponseDTO(
    @SerializedName("code")
    val code: Int?,
    @SerializedName("data")
    val `data`: QiblaData?,
    @SerializedName("status")
    val status: String?
)

data class QiblaData(
    @SerializedName("direction")
    val direction: Double?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?
)