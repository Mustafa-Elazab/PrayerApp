package com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.pray

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mostafa.alaymiatask.MainCoroutineRule
import com.mostafa.alaymiatask.data.remote.dto.AladhanResponseDTO
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import com.mostafa.alaymiatask.domain.repository.MainRepository
import com.mostafa.alaymiatask.domain.usecase.GetPrayTimeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PrayViewModelTest {


    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutine = MainCoroutineRule()


    @Mock
    private lateinit var repository: MainRepository

    private lateinit var useCase: GetPrayTimeUseCase

    private lateinit var viewModel: PrayViewModel


    @Before
    fun setup() {
        useCase = GetPrayTimeUseCase(repository)
        viewModel = PrayViewModel(useCase = useCase, null, null)
    }


    @Test
    fun `get timings response with location and date then should emit response`() =
        mainCoroutine.runBlockingTest {
            val latitude = 12.26
            val longitude = 20.54
            val month = 5
            val year = 2023

            // Arrange
            val fakeResponse = NetworkResponse.Success(
                AladhanResponseDTO(
                    data = listOf(),
                    code = 200,
                    status = "OK"
                )
            )
            val flowResponse = flowOf(fakeResponse)

            Mockito.`when`(
                useCase.invoke(
                    latitude = latitude,
                    longitude = longitude,
                    month = month,
                    year = year
                )
            ).thenReturn(flowResponse)

            // Act
            val result = useCase.invoke(
                latitude = latitude,
                longitude = longitude,
                month = month,
                year = year
            ).single()

            // Assert
            assertEquals(fakeResponse, result)
        }
}