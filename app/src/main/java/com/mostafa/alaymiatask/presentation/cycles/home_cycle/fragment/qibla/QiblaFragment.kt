package com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mostafa.alaymiatask.R
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import com.mostafa.alaymiatask.databinding.FragmentQiblaBinding
import com.mostafa.alaymiatask.di.NetworkUtils
import com.mostafa.alaymiatask.presentation.base.BaseFragment
import com.mostafa.alaymiatask.presentation.cycles.home_cycle.activity.TAG
import com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.pray.PrayViewModel
import com.mostafa.alaymiatask.utils.bitmapDescriptorFromVector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class QiblaFragment : BaseFragment<FragmentQiblaBinding>(R.layout.fragment_qibla),
    SensorEventListener {


    @Inject
    lateinit var networkUtils: NetworkUtils

    private val viewModel: QiblaViewModel by viewModels()
    private val prayViewModel: PrayViewModel by activityViewModels()
    private lateinit var mGoogleMap: GoogleMap
    private var meccaMarker: Marker? = null
    private val meccaLocation = LatLng(21.422487, 39.826206)
    var direction: Float? = null

    private val mSensorManager: SensorManager by lazy {
        requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val orientationSensor: Sensor by lazy {
        mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
    }


    override val defineBindingVariables: ((FragmentQiblaBinding) -> Unit)?
        get() = { binding ->
            binding.lifecycleOwner = viewLifecycleOwner
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        if (networkUtils.isNetworkConnected()) {
            collectQiblaDirection()
        } else {
            if (viewModel.direction == null) {
                Toast.makeText(
                    requireContext(),
                    "Open Network to get Qibla Direction",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                direction = viewModel.direction!!.toFloat()
                startSensor()
                Toast.makeText(
                    requireContext(),
                    "No Network So This is Last Direction For Qibla",
                    Toast.LENGTH_LONG
                ).show()
            }

        }

    }

    private fun collectQiblaDirection() {
        lifecycleScope.launch {
            viewModel.qiblaDirection.collectLatest {
                when (it) {
                    is NetworkResponse.ApiError -> {
                        Toast.makeText(requireContext(), it.body.data.toString(), Toast.LENGTH_LONG)
                            .show()
                    }


                    is NetworkResponse.NetworkError -> {
                        Toast.makeText(
                            requireContext(),
                            it.error.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is NetworkResponse.Success -> {
                        direction = it.body.data!!.direction!!.toFloat()
                        binding.needleInCompass.visibility = View.VISIBLE
                        startSensor()
                    }

                    is NetworkResponse.UnknownError -> {
                        Toast.makeText(
                            requireContext(),
                            it.error!!.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {

                    }
                }
            }
        }
    }

    private fun initMap() {
        binding.mapView.onCreate(null)
        binding.mapView.getMapAsync { googleMap ->
            mGoogleMap = googleMap
            googleMap.uiSettings.apply {
                isZoomControlsEnabled = false
                isCompassEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isScrollGesturesEnabled = false
                isZoomGesturesEnabled = false
            }
            createKaabaLocationMarker(meccaLocation)
            createUserLocationMarker()

        }

    }

    private fun createUserLocationMarker() {
        lifecycleScope.launch {
            prayViewModel.locationStateFlow.collectLatest { location ->
                if (location != null) {
                    var userLng = LatLng(location!!.latitude, location.longitude)
                    viewModel.getQiblaDirection(userLng)
                    mGoogleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLng,
                            4f
                        )
                    )
                    viewModel.addOrUpdateUserMarker(userLng, mGoogleMap)
                }else{
                    delay(5000)
                    Log.d(TAG, "Location is null")
                }
            }

        }
    }

    private fun createKaabaLocationMarker(kaabaLocationOnMap: LatLng) {
        meccaMarker = mGoogleMap.addMarker(MarkerOptions()
            .apply {
                position(kaabaLocationOnMap)
                title("Kaaba")
                icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_compas))
            })!!

    }


    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()

    }


    private fun startSensor() {
        if (mSensorManager != null && orientationSensor != null) {
            mSensorManager.registerListener(
                this,
                orientationSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            Toast.makeText(requireContext(), "Sensor not supported", Toast.LENGTH_LONG).show()
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ORIENTATION) {
            val degree = event.values[0]

            updateCompass(degree)
        }
    }

    private fun updateCompass(degree: Float) {
        try {
            val finalRotation = -degree + direction!!

            RotateAnimation(
                finalRotation,
                finalRotation,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                duration = 200
                fillAfter = true
                binding.needleInCompass.startAnimation(this)
            }
        } catch (e: Exception) {
            Log.d("TAG", "updateCompass: " + e.localizedMessage)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        mSensorManager.unregisterListener(this)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

}
