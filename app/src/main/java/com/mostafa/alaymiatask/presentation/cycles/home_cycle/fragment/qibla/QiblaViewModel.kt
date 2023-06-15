package com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.qibla

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mostafa.alaymiatask.data.remote.dto.ErrorResponse
import com.mostafa.alaymiatask.data.remote.dto.QiblaResponseDTO
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import com.mostafa.alaymiatask.domain.usecase.GetQiblaDirectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val useCase: GetQiblaDirectionUseCase,
    private val dataStore: DataStore<Preferences>?,
) :
    ViewModel() {

    private var userMarker: Marker? = null

    private val _qiblaDirection =
        MutableStateFlow<NetworkResponse<QiblaResponseDTO, ErrorResponse>?>(null)
    val qiblaDirection get() = _qiblaDirection.asStateFlow()


    var direction: Double? = null

    init {
        viewModelScope.launch {
            dataStore?.data?.firstOrNull()?.let { preferences ->
                direction = preferences[DIRECTION]
            }
        }
    }


     fun addOrUpdateUserMarker(locationLatLng: LatLng, mGoogleMap: GoogleMap) {
        if (userMarker == null) {
            val markerOptions = MarkerOptions()
                .position(locationLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title("My Location")
            userMarker = mGoogleMap.addMarker(markerOptions)
        } else {
            userMarker?.position = locationLatLng
        }
    }


    fun getQiblaDirection(location: LatLng) {
        viewModelScope.launch {
            useCase.invoke(latitude = location.latitude, longitude = location.longitude).collect {
                when (it) {
                    is NetworkResponse.Success -> {
                        dataStore!!.edit { preferences ->
                            preferences[DIRECTION] = it.body.data!!.direction!!
                        }
                    }

                }
                _qiblaDirection.emit(it)
            }
        }
    }


    companion object {
        val DIRECTION = doublePreferencesKey("direction")

    }
}




