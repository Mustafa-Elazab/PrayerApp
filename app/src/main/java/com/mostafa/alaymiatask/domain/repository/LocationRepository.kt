package com.mostafa.alaymiatask.domain.repository
import android.app.Activity
import android.location.Location

interface LocationRepository {

    suspend fun getCurrentLocation(): Location?
}