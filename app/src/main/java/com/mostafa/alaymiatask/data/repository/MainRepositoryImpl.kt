package com.mostafa.alaymiatask.data.repository

import androidx.room.withTransaction
import com.mostafa.alaymiatask.data.local.database.AppDatabase
import com.mostafa.alaymiatask.data.remote.api.ApiServices
import com.mostafa.alaymiatask.data.remote.dto.AladhanResponseDTO
import com.mostafa.alaymiatask.data.remote.dto.ErrorResponse
import com.mostafa.alaymiatask.data.remote.dto.QiblaResponseDTO
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import com.mostafa.alaymiatask.di.NetworkUtils
import com.mostafa.alaymiatask.domain.repository.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okio.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject constructor(
    private val api: ApiServices,
    private val database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher,
    private val networkUtils: NetworkUtils,
) : MainRepository {


    override suspend fun getPrayTime(
        latitude: Double?,
        longitude: Double?,
        month: Int?,
        year: Int?
    ): Flow<NetworkResponse<AladhanResponseDTO, ErrorResponse>> {


        val cachedData = withContext(ioDispatcher) {
            database.prayDao().getPrayTime()
        }

        if (networkUtils.isNetworkConnected()) {
            return flow {
                emit(NetworkResponse.Loading)
                when (val get =
                    api.getPrayTime(
                        year = year,
                        month = month,
                        latitude = latitude,
                        longitude = longitude
                    )) {
                    is NetworkResponse.ApiError -> if (get.code == 500) emit(
                        NetworkResponse.ApiError(
                            ErrorResponse(
                                code = 500,
                                data = "Internal Server Error",
                                status = "Internal Server Error"
                            ),
                            get.code
                        )
                    ) else emit(NetworkResponse.ApiError(get.body, get.code))

                    NetworkResponse.Loading -> emit(NetworkResponse.Loading)
                    is NetworkResponse.NetworkError -> emit(NetworkResponse.NetworkError(get.error))
                    is NetworkResponse.Success -> {
                        database.withTransaction {
                            database.prayDao().deletePrayTime()
                            database.prayDao().addPrayTime(get.body)
                        }

                        emit(NetworkResponse.Success(get.body))
                    }

                    is NetworkResponse.UnknownError -> emit(NetworkResponse.UnknownError(get.error))
                }
            }

        } else {

            return flow {
                if (cachedData != null && cachedData.data != null && cachedData.data!!.isNotEmpty()) {
                    emit(
                        NetworkResponse.Success(
                            AladhanResponseDTO(
                                cachedData.code,
                                cachedData.data,
                                cachedData.status
                            )
                        )
                    )
                } else {
                    emit(NetworkResponse.NetworkError(error = IOException("Open Internet for first Time To Handle Request and Cache It .")))
                }
            }
        }


    }

    override suspend fun getQiblaDirection(
        latitude: Double?,
        longitude: Double?
    ): Flow<NetworkResponse<QiblaResponseDTO, ErrorResponse>> = flow {
        emit(NetworkResponse.Loading)
        when (val get =
            api.getQiblaDirection(
                latitude = latitude,
                longitude = longitude
            )) {
            is NetworkResponse.ApiError -> if (get.code == 500) emit(
                NetworkResponse.ApiError(
                    ErrorResponse(
                        code = 500,
                        data = "Internal Server Error",
                        status = "Internal Server Error"
                    ),
                    get.code
                )
            ) else emit(NetworkResponse.ApiError(get.body, get.code))

            NetworkResponse.Loading -> emit(NetworkResponse.Loading)
            is NetworkResponse.NetworkError -> emit(NetworkResponse.NetworkError(get.error))
            is NetworkResponse.Success -> {
                emit(NetworkResponse.Success(get.body))
            }

            is NetworkResponse.UnknownError -> emit(NetworkResponse.UnknownError(get.error))
        }
    }




}