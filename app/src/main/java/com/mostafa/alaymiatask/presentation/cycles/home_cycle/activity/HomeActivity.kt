package com.mostafa.alaymiatask.presentation.cycles.home_cycle.activity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mostafa.alaymiatask.R
import com.mostafa.alaymiatask.di.NetworkUtils
import com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.api.PrayViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "HomeActivity"

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val viewModel: PrayViewModel by viewModels()


    private val locationManager: LocationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Inject
    lateinit var networkUtils: NetworkUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            var isGranted = true
            for (key in it.keys) {
                isGranted = isGranted && it[key]!!
            }

            if (isGranted) {
                if (networkUtils.isNetworkConnected()) {
                    checkGpsStatus()
                } else {
                    lifecycleScope.launch {
                        viewModel.fetchPrayTime(
                            latitude = 0.0,
                            longitude = 0.0
                        )
                    }
                }

            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    provideExplainPermissionDialog().show()
                }
            }
        }
    }


//    private fun checkGpsStatus()
//    {
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            lifecycleScope.launch {
//                repeatOnLifecycle(Lifecycle.State.STARTED) {
//                    while (isActive) {
//                        viewModel.getCurrentLocation().let { location ->
//                            if (location != null) {
//                                viewModel.fetchPrayTime(
//                                    latitude = location.latitude,
//                                    longitude = location.longitude
//                                )
//
//                                viewModel.getLocationAddress(
//                                    this@HomeActivity,
//                                    latitude = location.latitude,
//                                    longitude = location.longitude
//                                )
//
//                            } else {
//                                delay(5000)
//                                Log.d(TAG, "checkGpsStatus: $location")
//                            }
//
//                        }
//                    }
//
//                }
//            }
//        } else {
//            buildAlertMessageNoGps()
//        }
//
//
//    }

    private fun checkGpsStatus() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lifecycleScope.launchWhenResumed {
                while (isActive) {
                    viewModel.getCurrentLocation()?.let { location ->
                        viewModel.fetchPrayTime(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )

                        viewModel.getLocationAddress(
                            this@HomeActivity,
                            latitude = location.latitude,
                            longitude = location.longitude
                        )

                    } ?: run {
                        delay(5000)
                        Log.d(TAG, "checkGpsStatus: Location is null")
                    }
                }
            }
        } else {
            buildAlertMessageNoGps()
        }
    }

    fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.enableGPS))
            .setIcon(R.drawable.ic_location)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.enable)) { _, _ ->
                val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(enableGpsIntent)
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }


    private fun showAlertOpenGps() {
        MaterialAlertDialogBuilder(this)
            .setTitle("GPS is disabled")
            .setMessage("Please enable GPS to get your location")
            .setIcon(R.drawable.ic_location)
            .setCancelable(false)
            .setPositiveButton(R.string.enable) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setCancelable(false)
            .create().show()
    }


    private fun provideExplainPermissionDialog(): Dialog {
        val dialog = Dialog(this)
        with(dialog) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.explain_permission_dialog_layout)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val closeDialog = findViewById<AppCompatImageView>(R.id.img_close)
            closeDialog.setOnClickListener { hide() }
            val openPermissionSettings = findViewById<AppCompatButton>(R.id.openPermissionSettings)
            openPermissionSettings.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
            create()
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

    }


    override fun onResume() {
        super.onResume()
    }


}