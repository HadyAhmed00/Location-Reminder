package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Class under test
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    private val NUMBER_OF_TEST_REMINDERS =10

    @Before
    fun setup() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersDAO = remindersDatabase.reminderDao()
        repository =
            RemindersLocalRepository(
                remindersDAO
            )
    }

    @After
    fun closeDb() {
        remindersDatabase.close()
    }



    private fun creteReminder():MutableList<ReminderDTO>{

        val list : MutableList<ReminderDTO> = ArrayList()
        for(i in 0..NUMBER_OF_TEST_REMINDERS){
            list.add(
                ReminderDTO(title = "titleNo${i}",
                    description = "some randomTxt of Reminder NO${i}",
                    location = "loc of Reminder NO${i}",
                    latitude = i.toDouble(),
                    longitude = i.toDouble(),
                    id = "id${i}")
            )
        }
        return list

    }

    private fun chickReminder(rem1:ReminderDTO,rem2:ReminderDTO) :Boolean
    {
        return rem1==rem2
    }


    @Test
    fun InsertAllTest_putmoreThenOneRmider_chickAllThem() = runBlocking {

        //GIVEN - NUMBER_OF_REMINDERS  of valid Reminders
        val inputRemindersList = creteReminder()

        for (reminderDTO in inputRemindersList) {
            remindersDatabase.reminderDao().saveReminder(reminderDTO)
        }

        val retrievedRemindersList = remindersDAO.getReminders()

        for (i in 0 until NUMBER_OF_TEST_REMINDERS)
        {
            val result = chickReminder(retrievedRemindersList[i],inputRemindersList[i])
            MatcherAssert.assertThat(result,`is`(true))
        }

    }
}