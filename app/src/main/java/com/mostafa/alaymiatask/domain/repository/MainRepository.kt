package com.mostafa.alaymiatask.domain.repository

import com.mostafa.alaymiatask.data.remote.dto.AladhanResponseDTO
import com.mostafa.alaymiatask.data.remote.dto.ErrorResponse
import com.mostafa.alaymiatask.data.remote.dto.QiblaResponseDTO
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import kotlinx.coroutines.flow.Flow

interface MainRepository {


    suspend fun getPrayTime(
        latitude: Double?,
        longitude: Double?,
        month: Int?,
        year: Int?,
    ): Flow<NetworkResponse<AladhanResponseDTO, ErrorResponse>>

    suspend fun getQiblaDirection(
        latitude: Double?,
        longitude: Double?,
    ): Flow<NetworkResponse<QiblaResponseDTO, ErrorResponse>>
}