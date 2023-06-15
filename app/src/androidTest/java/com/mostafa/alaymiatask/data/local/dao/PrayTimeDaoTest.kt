package com.mostafa.alaymiatask.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.mostafa.alaymiatask.data.local.database.AppDatabase
import com.mostafa.alaymiatask.data.remote.dto.AladhanData
import com.mostafa.alaymiatask.data.remote.dto.AladhanResponseDTO
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named


@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class PrayTimeDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_database")
    lateinit var database: AppDatabase
    private lateinit var dao: PrayTimeDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = database.prayDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testAddAndGetPrayTime() {

        runTest {
            val aladhanResponseDTO = createSampleAladhanResponseDTO()



            dao.addPrayTime(aladhanResponseDTO)


            val prayTimeData = dao.getPrayTime()


            assertThat(prayTimeData).isEqualTo(aladhanResponseDTO)
        }
    }

    @Test
    fun testDeletePrayTime() {
        runTest {
            val aladhanResponseDTO = createSampleAladhanResponseDTO()


            dao.addPrayTime(aladhanResponseDTO)


            dao.deletePrayTime()


            val prayTimeData = dao.getPrayTime()


            assertThat(prayTimeData).isNull()
        }
    }

    private fun createSampleAladhanResponseDTO(): AladhanResponseDTO {
        return AladhanResponseDTO(200, listOf<AladhanData>(), "OK")
    }


}