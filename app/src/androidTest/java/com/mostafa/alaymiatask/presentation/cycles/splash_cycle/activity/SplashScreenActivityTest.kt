package com.mostafa.alaymiatask.presentation.cycles.splash_cycle.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mostafa.alaymiatask.R
import com.mostafa.alaymiatask.presentation.cycles.home_cycle.activity.HomeActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class SplashScreenActivityTest {

    @get:Rule()
    var hiltRule = HiltAndroidRule(this)

    @get: Rule()
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun move_to_home_activity_with_delay_then_return_home_activity_displayed() {
        runTest {
            ActivityScenario.launch(SplashScreenActivity::class.java)
            onView(withId(R.id.splash_screen)).check(matches(isDisplayed()))
            delay(1000)
            ActivityScenario.launch(HomeActivity::class.java)
            onView(withId(R.id.home_screen)).check(matches(isDisplayed()))
        }
    }


}



