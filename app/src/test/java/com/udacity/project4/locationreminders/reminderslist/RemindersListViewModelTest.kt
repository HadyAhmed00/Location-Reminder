package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var remindersRepository: FakeDataSource

    @Before
    fun setupViewModel() {
        stopKoin()

        remindersRepository = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), remindersRepository
        )
    }

    @Test
    fun loadReminders_loading() = runBlockingTest{
        // GIVEN - We will load the reminders

        mainCoroutineRule.stop() // pausing the dispatcher
        remindersListViewModel.loadReminders()

        // WHEN - the working thread is Paused we should see the Loader
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true)
        )

        mainCoroutineRule.reRun() // streaming the dispatcher

        // THEN - The Loader is gun
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadRemindersWhenUnavailable_causesError()= runBlockingTest {
        // GIVEN - load the data form the Repo and the data corrupted
        remindersRepository.loadData=false

        // WHEN - loading the data
        remindersListViewModel.loadReminders()

        // THEN - the snack bar should be announced with an error massage
        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(), CoreMatchers.`is`("Fail To Load Data")
        )
    }
}