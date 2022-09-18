package com.udacity.project4.locationreminders.data.local

import androidx.appcompat.widget.ResourceManagerInternal
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    COMPLETE: Add testing implementation to the RemindersDao.kt

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb()
    {

        database.close()
    }

    private fun creteReminder():ReminderDTO{
        return ReminderDTO(title = "title",
        description = "some randomTxt",
        location = "loc",
        latitude = 1.1,
        longitude = 2.2,
        id = "id1")

    }

    private fun chickReminder(rem1:ReminderDTO,rem2:ReminderDTO) :Boolean
    {
        return rem1==rem2
    }

    @Test
    fun insertFunction_putValidDataIndDataset_retriveThesame() = runBlockingTest {

        //GIVEN - a Valid Reminder
        val testReminderItem = creteReminder()

        //WHEN - set or save the Reminder in the database and retrieve it
        database.reminderDao().saveReminder(testReminderItem)

        val result =
            database.reminderDao().getReminderById("id1")
                ?.let { chickReminder(testReminderItem, it) }
        //THEN - 2 reminders are the same
        assertThat(result,`is`(true))


    }
}