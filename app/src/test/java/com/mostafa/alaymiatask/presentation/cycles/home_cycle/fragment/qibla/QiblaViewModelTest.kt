package com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.qibla

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mostafa.alaymiatask.MainCoroutineRule
import com.mostafa.alaymiatask.data.remote.dto.QiblaData
import com.mostafa.alaymiatask.data.remote.dto.QiblaResponseDTO
import com.mostafa.alaymiatask.data.remote.response.NetworkResponse
import com.mostafa.alaymiatask.domain.repository.MainRepository
import com.mostafa.alaymiatask.domain.usecase.GetQiblaDirectionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
class QiblaViewModelTest {

    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutine = MainCoroutineRule()

    @Mock
    private lateinit var repository: MainRepository


    private lateinit var useCase: GetQiblaDirectionUseCase


    private lateinit var viewModel: QiblaViewModel

    @Before
    fun setup() {
        useCase = GetQiblaDirectionUseCase(repository = repository)
        viewModel = QiblaViewModel(useCase = useCase, null)
    }


    @Test
    fun `get qibla direction with location then should emit response`() {
        mainCoroutine.runTest {


            val latitude = 12.26
            val longitude = 20.54

            //Arrange

            val fakeResponse = flowOf(
                NetworkResponse.Success(
                    QiblaResponseDTO(
                        data = QiblaData(direction = 256.35, latitude = 20.26, longitude = 222.55),
                        code = 200,
                        status = "OK"
                    )
                )
            )


            Mockito.`when`(useCase.invoke(latitude = latitude, longitude = longitude))
                .thenReturn(fakeResponse)


            // Act
            val result = useCase.invoke(latitude = latitude, longitude = longitude)

            // Assert
            assertEquals(fakeResponse, result)


        }
    }

}