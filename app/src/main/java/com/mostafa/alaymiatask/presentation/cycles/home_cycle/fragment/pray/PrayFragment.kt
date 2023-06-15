package com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.pray

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mostafa.alaymiatask.R
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import com.mostafa.alaymiatask.databinding.FragmentPrayBinding
import com.mostafa.alaymiatask.di.NetworkUtils
import com.mostafa.alaymiatask.domain.model.PrayerTime
import com.mostafa.alaymiatask.presentation.base.BaseFragment
import com.mostafa.alaymiatask.presentation.cycles.home_cycle.adapter.PrayerAdapter
import com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.api.PrayViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class PrayFragment : BaseFragment<FragmentPrayBinding>(R.layout.fragment_pray) {


    private val viewModel: PrayViewModel by activityViewModels()

    @Inject
    lateinit var networkUtils: NetworkUtils


    private val adapter = PrayerAdapter()
    override val defineBindingVariables: ((FragmentPrayBinding) -> Unit)?
        get() = { binding ->
            binding.lifecycleOwner = viewLifecycleOwner
        }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            rvSchedule.adapter = adapter
            btnBack.setOnClickListener {
                viewModel.previousDay()
            }
            btnForward.setOnClickListener {
                viewModel.nextDay()
            }
            btnNavQibla.setOnClickListener {
                findNavController().navigate(R.id.action_prayFragment_to_qiblaFragment)
            }

        }


        collectFlows(
            listOf(
                ::collectCurrentDayState,
                ::collectRemaingTimeState,
                ::collectCurrentDateState,
                ::collectNextPrayState,
                ::collectAddressState
            )
        )


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun collectCurrentDayState() {
        viewModel.currentDayStateFlow.collectLatest {

            collectTimingsState(it - 1)

        }

    }

    private suspend fun collectNextPrayState() {

        viewModel.nextPrayerStateFlow.collectLatest {
            binding.apply {
                tvNearestScheduleName.text = " To $it" ?: ""
                tvNearestPrayer.text = it ?: ""
            }
        }

    }

    private suspend fun collectCurrentDateState() {
        viewModel.currentDateStateFlow.collectLatest {
            binding.tvDate.text = it
        }

    }

    private suspend fun collectRemaingTimeState() {

        viewModel.remainingTime.collectLatest {
            binding.tvNextPray.text = it
        }

    }


    private suspend fun collectAddressState() {
        binding.tvAddress.text = viewModel.city.toString() ?: ""
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun collectTimingsState(index: Int) {
        lifecycleScope.launch {
            viewModel.prayTime.collectLatest {
                when (it) {
                    is NetworkResponse.ApiError -> {
                        Toast.makeText(requireContext(), it.body.data.toString(), Toast.LENGTH_LONG)
                            .show()
                        binding.shimmerLayout.stopShimmer()

                    }

                    NetworkResponse.Loading -> {
                        binding.shimmerLayout.visibility = View.VISIBLE
                    }

                    is NetworkResponse.NetworkError -> {
                        Toast.makeText(
                            requireContext(),
                            it.error.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                        binding.shimmerLayout.stopShimmer()
                    }

                    is NetworkResponse.Success -> {
                        binding.shimmerLayout.stopShimmer()
                        val timing = it.body.data?.get(index)!!.timings!!

                        val allPrayerTimesInMillis = listOf<Long>(
                            viewModel.timeStringToMillis(timing.fajr.toString()),
                            viewModel.timeStringToMillis(timing.sunrise.toString()),
                            viewModel.timeStringToMillis(timing.dhuhr.toString()),
                            viewModel.timeStringToMillis(timing.asr.toString()),
                            viewModel.timeStringToMillis(timing.maghrib.toString()),
                            viewModel.timeStringToMillis(timing.isha.toString()),
                        )
                        viewModel.getNextPrayer(allPrayerTimesInMillis)
                        val prayerTimes = listOf(
                            PrayerTime(
                                1,
                                "Fajr",
                                viewModel.convertToAmPmFormat(time = timing.fajr.toString()),
                                viewModel.timeStringToMillis(timing.fajr.toString()),
                                R.drawable.fajr
                            ),
                            PrayerTime(
                                1,
                                "SunRise",
                                viewModel.convertToAmPmFormat(time = timing.sunrise.toString()),
                                viewModel.timeStringToMillis(timing.sunrise.toString()),
                                R.drawable.sunrise
                            ),
                            PrayerTime(
                                1,
                                "Dhuhr",
                                viewModel.convertToAmPmFormat(time = timing.dhuhr.toString()),
                                viewModel.timeStringToMillis(timing.dhuhr.toString()),
                                R.drawable.sunrise
                            ),
                            PrayerTime(
                                1,
                                "Asr",
                                viewModel.convertToAmPmFormat(time = timing.asr.toString()),
                                viewModel.timeStringToMillis(timing.asr.toString()),
                                R.drawable.asr
                            ), PrayerTime(
                                1,
                                "Maghrib",
                                viewModel.convertToAmPmFormat(time = timing.maghrib.toString()),
                                viewModel.timeStringToMillis(timing.maghrib.toString()),
                                R.drawable.maghrab
                            ), PrayerTime(
                                1,
                                "Isha",
                                viewModel.convertToAmPmFormat(time = timing.isha.toString()),
                                viewModel.timeStringToMillis(timing.isha.toString()),
                                R.drawable.isha
                            )

                        )
                        adapter.submitList(prayerTimes)
                    }

                    is NetworkResponse.UnknownError -> {
                        Toast.makeText(
                            requireContext(),
                            it.error!!.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()

                        binding.shimmerLayout.stopShimmer()
                    }

                    else -> {
                        binding.shimmerLayout.stopShimmer()
                    }
                }
            }
        }

    }


}