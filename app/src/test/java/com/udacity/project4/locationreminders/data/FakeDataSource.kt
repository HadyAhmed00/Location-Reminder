package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {


//     Create a fake data source to act as a double to the real data source
    var dataSource :HashMap<String,ReminderDTO> = HashMap()

    var loadData : Boolean = true
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if(!dataSource.isEmpty() && loadData) {
            val tmpList : MutableList<ReminderDTO> = ArrayList()
            dataSource.forEach{
                tmpList.add(it.value)
            }
            Result.Success(tmpList)
        }else{
            Result.Error("The Data is not good!!")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        dataSource[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if(!dataSource.isEmpty() && loadData) {
            dataSource[id]?.let {
                Result.Success(it)
            }
            return Result.Error("item not found")

        }else{
            Result.Error("The Data is not good!!")
        }
    }

    override suspend fun deleteAllReminders() {
        dataSource.clear()
    }
}