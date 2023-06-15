package com.mostafa.alaymiatask.domain.usecase

import com.mostafa.alaymiatask.domain.repository.MainRepository
import javax.inject.Inject

 class GetQiblaDirectionUseCase @Inject constructor(private val repository: MainRepository) {

    suspend operator fun invoke(latitude: Double, longitude: Double) =

        repository.getQiblaDirection(
            latitude = latitude,
            longitude = longitude,
        )

}
