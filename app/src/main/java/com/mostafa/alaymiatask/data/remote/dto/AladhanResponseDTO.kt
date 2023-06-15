package com.mostafa.alaymiatask.data.remote.dto


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity
data class AladhanResponseDTO(
    @PrimaryKey
    @SerializedName("code")
    val code: Int?,
    @SerializedName("data")
    val `data`: List<AladhanData>?,
    @SerializedName("status")
    val status: String?
)