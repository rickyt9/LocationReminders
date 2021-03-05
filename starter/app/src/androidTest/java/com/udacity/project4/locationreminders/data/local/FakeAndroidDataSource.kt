package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeAndroidDataSource(var reminders : LinkedHashMap<String, ReminderDTO> = LinkedHashMap())
    : ReminderDataSource {
    //    TODO: Create a fake data source to act as a double to the real data source
    companion object {
        val ERROR_MESSAGE = "Could not get reminders"
    }

    private var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error(ERROR_MESSAGE)
        }
        return Result.Success(reminders.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error(ERROR_MESSAGE)
        }
        reminders[id]?.let {
            return Result.Success(it)
        }
        return Result.Error(ERROR_MESSAGE)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
}