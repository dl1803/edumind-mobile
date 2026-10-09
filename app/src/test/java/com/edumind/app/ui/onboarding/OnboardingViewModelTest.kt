package com.edumind.app.ui.onboarding

import com.edumind.app.data.local.datastore.AppPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val appPreferences: AppPreferences = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(appPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun completeOnboarding_savesFlagAndEmitsNavigation() = runTest(testDispatcher) {
        coEvery { appPreferences.setSeenOnboarding(true) } returns Unit

        var receivedNavigation = false
        val job = launch {
            viewModel.navigateToLogin.first()
            receivedNavigation = true
        }

        viewModel.completeOnboarding()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { appPreferences.setSeenOnboarding(true) }
        assertEquals(true, receivedNavigation)
        job.cancel()
    }

    @Test
    fun complete_alias_worksIdentically() = runTest(testDispatcher) {
        coEvery { appPreferences.setSeenOnboarding(true) } returns Unit

        var receivedNavigation = false
        val job = launch {
            viewModel.navigateToLogin.first()
            receivedNavigation = true
        }

        viewModel.complete()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { appPreferences.setSeenOnboarding(true) }
        assertEquals(true, receivedNavigation)
        job.cancel()
    }
}
