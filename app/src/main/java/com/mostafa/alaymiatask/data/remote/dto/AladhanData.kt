package com.mostafa.alaymiatask.data.remote.dto


import com.google.gson.annotations.SerializedName

data class AladhanData(
    @SerializedName("timings")
    val timings: TimingResponse?
)