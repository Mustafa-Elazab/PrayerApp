package com.mostafa.alaymiatask.data.repository

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.mostafa.alaymiatask.domain.repository.LocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val application: Application
) : LocationRepository {


    override suspend fun getCurrentLocation(): Location? {
        val hasAccessFineLocation = ContextCompat.checkSelfPermission(
            application, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasAccessCoarseLocation = ContextCompat.checkSelfPermission(
            application, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAccessCoarseLocation && !hasAccessFineLocation) {
            Log.d("TAG", "getCurrentLocation: No permissions")
            return null
        }

        val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationManager.getProviders(true)

        var location: Location? = null

        for (provider in providers) {
            try {
                val lastKnownLocation = locationManager.getLastKnownLocation(provider)
                if (lastKnownLocation != null && (location == null || lastKnownLocation.accuracy < location.accuracy)) {
                    location = lastKnownLocation
                }
            } catch (e: SecurityException) {
                Log.e("TAG", "getCurrentLocation: SecurityException", e)
            }
        }

        return location
    }



}



