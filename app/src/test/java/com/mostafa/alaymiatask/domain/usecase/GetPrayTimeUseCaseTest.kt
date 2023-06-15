package com.mostafa.alaymiatask.domain.usecase

import com.mostafa.alaymiatask.data.remote.dto.AladhanResponseDTO
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

class GetPrayTimeUseCaseTest {


    @Mock
    private lateinit var repository: MainRepository

    private lateinit var useCase: GetPrayTimeUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        useCase = GetPrayTimeUseCase(repository)
    }


    @Test
    fun `invoke fun with location and date should emit success response`() = runBlocking {

        val latitude = 12.26
        val longitude = 20.54
        val month = 5
        val year = 2023
        //Arrange

        val fakeResponse = flowOf(
            NetworkResponse.Success(
                AladhanResponseDTO(
                    data = listOf(),
                    code = 200,
                    status = "OK"
                )
            )
        )
        Mockito.`when`(
            repository.getPrayTime(
                latitude = latitude,
                longitude = longitude,
                year = year,
                month = month
            )
        ).thenReturn(fakeResponse)


        //Act
        val resultFlow = useCase(latitude = latitude,longitude=longitude,year=year, month = month)
        val result = resultFlow.toList()

        //Assert
        assert(result.size == 1)
        assert(result[0] is NetworkResponse.Success)


    }
}