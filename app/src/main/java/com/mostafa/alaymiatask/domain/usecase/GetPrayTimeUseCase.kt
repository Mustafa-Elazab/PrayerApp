package com.mostafa.alaymiatask.domain.usecase

import com.mostafa.alaymiatask.domain.repository.MainRepository
import javax.inject.Inject

class GetPrayTimeUseCase @Inject constructor(private val repository: MainRepository) {

    suspend operator fun invoke(latitude: Double, longitude: Double, month: Int, year: Int) =

        repository.getPrayTime(
            latitude = latitude,
            longitude = longitude,
            month = month,
            year = year
        )


}