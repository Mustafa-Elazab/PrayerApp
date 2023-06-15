package com.mostafa.alaymiatask.domain.model

data class PrayerTime(
    val id: Int,
    val name: String,
    val time: String,
    val timeInMillis: Long,
    val image: Int
)
