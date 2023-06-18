package com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.pray

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mostafa.alaymiatask.data.remote.dto.AladhanResponseDTO
import com.mostafa.alaymiatask.data.remote.dto.ErrorResponse
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import com.mostafa.alaymiatask.domain.reminder.AzanWorker
import com.mostafa.alaymiatask.domain.repository.LocationRepository
import com.mostafa.alaymiatask.domain.usecase.GetPrayTimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PrayViewModel @Inject constructor(
    private val useCase: GetPrayTimeUseCase,
    private val repository: LocationRepository?,
    private val dataStore: DataStore<Preferences>?,
    private val app: Application
) : AndroidViewModel(app) {


    private val _prayTime =
        MutableStateFlow<NetworkResponse<AladhanResponseDTO, ErrorResponse>?>(null)
    val prayTime get() = _prayTime.asStateFlow()


    private var _locationAddress = MutableStateFlow<String?>("")
    val locationAddress get() = _locationAddress.asStateFlow()

    private val minDay = 1
    private val maxDay = 30

    private val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
    private val currentDate = Calendar.getInstance()

    private var countdownJob: Job? = null


    private val _remainingTime = MutableStateFlow<String>("")
    val remainingTime get() = _remainingTime.asStateFlow()


    private val _currentDateStateFlow = MutableStateFlow<String>("")
    val currentDateStateFlow get() = _currentDateStateFlow.asStateFlow()

    private val _currentDayStateFlow = MutableStateFlow<Int>(0)
    val currentDayStateFlow get() = _currentDayStateFlow.asStateFlow()


    private val _nextPrayerStateFlow = MutableStateFlow<String>("")
    val nextPrayerStateFlow get() = _nextPrayerStateFlow.asStateFlow()

    private val _locationStateFlow = MutableStateFlow<Location?>(null)
    val locationStateFlow get() = _locationStateFlow.asStateFlow()

    val location: MutableLiveData<Location> = MutableLiveData()

    var city: String? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0

    init {

        updateCurrentDate()
        viewModelScope.launch {
            dataStore?.data?.firstOrNull()?.let { preferences ->
                city = preferences[CITY]
                latitude = preferences[LATITUDE]
                longitude = preferences[LONGITUDE]
            }

        }
    }


    fun fetchPrayTime(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            useCase.invoke(
                latitude = latitude,
                longitude = longitude,
                month = currentDate.get(Calendar.MONTH) + 1,
                year = currentDate.get(Calendar.YEAR)
            )
                .collect {
                    _prayTime.emit(it)
                }
        }
    }


    private fun updateCurrentDate() {
        viewModelScope.launch {
            _currentDateStateFlow.emit(dateFormat.format(currentDate.time))

            _currentDayStateFlow.emit(currentDate.get(Calendar.DAY_OF_MONTH))
        }

    }

    fun previousDay() {
        if (currentDate.get(Calendar.DAY_OF_MONTH) > minDay) {
            currentDate.add(Calendar.DAY_OF_YEAR, -1)
            updateCurrentDate()
        }
    }

    fun nextDay() {
        if (currentDate.get(Calendar.DAY_OF_MONTH) < maxDay) {
            currentDate.add(Calendar.DAY_OF_YEAR, 1)
            updateCurrentDate()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun timeStringToMillis(time: String): Long {
        val timePattern = """\d{2}:\d{2}""".toRegex()
        val matchResult = timePattern.find(time)
        val prayerTime = matchResult?.value
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val localTime = LocalTime.parse(prayerTime, formatter)
        val currentDateTime = LocalTime.now().atDate(LocalDate.now())
        val timeOfDay =
            currentDateTime.withHour(localTime.hour).withMinute(localTime.minute).withSecond(0)
                .withNano(0)
        return timeOfDay.toInstant(ZoneOffset.UTC).toEpochMilli()
    }


    fun convertToAmPmFormat(time: String): String {
        val timePattern = """\d{2}:\d{2}""".toRegex()
        val matchResult = timePattern.find(time)
        val prayerTime = matchResult?.value!!
        val parts = prayerTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1]
        val amPm = if (hour >= 12) "PM" else "AM"
        val hourInAmPm = if (hour > 12) hour - 12 else hour
        return "$hourInAmPm:$minute $amPm"
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getNextPrayer(prayerTimesInMillis: List<Long>) {
        val currentTime = LocalDateTime.now().withNano(0).toInstant(ZoneOffset.UTC).toEpochMilli()
        val nextPrayerTime = prayerTimesInMillis.firstOrNull { it > currentTime }
            ?: prayerTimesInMillis.firstOrNull()
        val nextPrayerIndex = prayerTimesInMillis.indexOf(nextPrayerTime)
        val timeDifference = nextPrayerTime?.minus(currentTime) ?: 0L
        val prayerNames = listOf("Fajr", "SunRise", "Dhuhr", "Asr", "Maghrib", "Isha")
        val nextPrayer = prayerNames.getOrNull(nextPrayerIndex) ?: "Fajr"


        val prayerTimesInMillis = listOf(
            prayerTimesInMillis[0],
            prayerTimesInMillis[2],
            prayerTimesInMillis[3],
            prayerTimesInMillis[4],
            prayerTimesInMillis[5],
        )

        val inputData = Data.Builder()
            .putLongArray(AzanWorker.KEY_PRAYER_TIMES_IN_MILLIS, prayerTimesInMillis.toLongArray())
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AzanWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(app).enqueue(workRequest)






        _nextPrayerStateFlow.value = nextPrayer

        startCountdown(durationMillis = timeDifference)

    }


    private fun startCountdown(durationMillis: Long) {
        countdownJob?.cancel() // Cancel any ongoing countdown job

        countdownJob = viewModelScope.launch {
            var remainingTimeMillis = durationMillis
            while (remainingTimeMillis > 0) {
                _remainingTime.emit(formatTime(remainingTimeMillis))
                delay(1000) // Delay for 1 second
                remainingTimeMillis -= 1000
            }
            _remainingTime.emit("00:00")   // Countdown finished
        }
    }


    private fun formatTime(timeMillis: Long): String {
        val hours = (timeMillis / (1000 * 60 * 60)) % 24
        val minutes = (timeMillis / (1000 * 60)) % 60
        val seconds = (timeMillis / 1000) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation() {
        repository!!.getCurrentLocation()?.let { location ->
            _locationStateFlow.emit(location)
        }

    }

    fun reloadLocation() {
        viewModelScope.launch {
            getCurrentLocation()
        }
    }

    fun getLocationAddress(context: Context, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            Geocoder(context.applicationContext, Locale.getDefault()).apply {
                getFromLocation(latitude, longitude, 1)?.first()
                    ?.let { address ->
                        _locationAddress.emit(buildString {
                            append(address.locality).append(", ")
                            append(address.subAdminArea)
                        })
                        dataStore!!.edit { preferences ->
                            preferences[LATITUDE] = latitude
                            preferences[LONGITUDE] = longitude
                            preferences[CITY] = buildString {
                                append(address.locality).append(", ")
                                append(address.subAdminArea)
                            }
                        }
                    }

            }
        }
    }


    companion object {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CITY = stringPreferencesKey("city")

    }
}