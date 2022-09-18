package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.hamcrest.core.Is.`is`
import org.koin.core.context.stopKoin

@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var remindersLocalRepository: ReminderDataSource
    lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun initTheRpoAndViewMode(){
        stopKoin()
        remindersLocalRepository = FakeDataSource()

        saveReminderViewModel= SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),remindersLocalRepository
        )
    }

    fun createReminder(isInvalid:Boolean): ReminderDataItem {

        return if(isInvalid){
            ReminderDataItem(null,"Desc","loc",1.2,1.3,"someid")

        }else{
            ReminderDataItem("title","Desc","loc",1.2,1.3,"someid")
        }


    }
    @Test
    fun validateReminder_InvalidReminder_False(){
        // GIVEN - invalid reminder

        val tmpReminder = createReminder(true)
        // WHEN - chick the valid Reminder
        val result = saveReminderViewModel.validateEnteredData(tmpReminder)
        //That - Return true
        assertThat(result, `is`(false))
    }

    @Test
    fun validateReminder_ValidReminder_True(){
        // GIVEN - valid reminder
        val tmpReminder = createReminder(false)
        // WHEN - chick the valid Reminder
        val result = saveReminderViewModel.validateEnteredData(tmpReminder)

        //That - Return true
        assertThat(result, `is`(true))
    }
}