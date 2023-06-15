package com.mostafa.alaymiatask.domain.usecase

import com.mostafa.alaymiatask.data.remote.dto.AladhanResponseDTO
import com.mostafa.alaymiatask.data.remote.dto.QiblaData
import com.mostafa.alaymiatask.data.remote.dto.QiblaResponseDTO
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import com.mostafa.alaymiatask.domain.repository.MainRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class GetQiblaDirectionUseCaseTest{



    @Mock
    private lateinit var repository: MainRepository

    private lateinit var useCase: GetQiblaDirectionUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        useCase = GetQiblaDirectionUseCase(repository)
    }


    @Test
    fun `invoke fun with location should emit success response`() = runBlocking {

        val latitude = 12.26
        val longitude = 20.54

        //Arrange

        val fakeResponse = flowOf(
            NetworkResponse.Success(
                QiblaResponseDTO(
                    data = QiblaData(direction = 256.35, latitude = 20.26,longitude=222.55),
                    code = 200,
                    status = "OK"
                )
            )
        )
        Mockito.`when`(
            repository.getQiblaDirection(
                latitude = latitude,
                longitude = longitude,

            )
        ).thenReturn(fakeResponse)


        //Act
        val resultFlow = useCase(latitude = latitude,longitude=longitude)
        val result = resultFlow.toList()

        //Assert
        assert(result.size == 1)
        assert(result[0] is NetworkResponse.Success)


    }


}