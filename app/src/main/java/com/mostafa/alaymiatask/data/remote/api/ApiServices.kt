package com.mostafa.alaymiatask.data.remote.api

import com.mostafa.alaymiatask.data.remote.dto.AladhanResponseDTO
import com.mostafa.alaymiatask.data.remote.dto.ErrorResponse
import com.mostafa.alaymiatask.data.remote.dto.QiblaResponseDTO
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServices {

    @GET("calendar/{year}/{month}")
    suspend fun getPrayTime(
        @Path("year") year: Int? = null,
        @Path("month") month: Int? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("method") method: Int = 4
    ): NetworkResponse<AladhanResponseDTO, ErrorResponse>


    @GET("qibla/{latitude}/{longitude}")
    suspend fun getQiblaDirection(
        @Path("latitude") latitude: Double? = null,
        @Path("longitude") longitude: Double? = null,
    ): NetworkResponse<QiblaResponseDTO, ErrorResponse>


}