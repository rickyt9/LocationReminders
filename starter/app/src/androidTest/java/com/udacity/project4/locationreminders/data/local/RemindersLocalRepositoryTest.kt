package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var repository : RemindersLocalRepository
    private lateinit var database : RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminderById() = runBlocking {
        val reminder = ReminderDTO("title","description","location",20.0,20.0, id = "reminder")
        repository.saveReminder(reminder)

        val result = repository.getReminder(reminder.id)

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminder_retrieveWithNonExistentId_returnError() = runBlocking {
        val reminder = ReminderDTO("title","description","location",20.0,20.0, id = "reminder")
        repository.saveReminder(reminder)

        val result = repository.getReminder("nonExistentId")

        assertThat(result.succeeded, `is`(false))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun saveReminders_retrieveRemindersList() = runBlocking {
        val reminder1 = ReminderDTO("title", "description", "location", 20.0, 20.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 40.0, 40.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 60.0, 60.0)

        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)
        repository.saveReminder(reminder3)

        var remindersList = repository.getReminders()

        assertThat(remindersList.succeeded, `is`(true))
        remindersList as Result.Success
        assertThat(remindersList.data.size, `is`(3))
        assertThat(remindersList.data[0], `is`(reminder1))
        assertThat(remindersList.data[1], `is`(reminder2))
        assertThat(remindersList.data[2], `is`(reminder3))

        repository.deleteAllReminders()

        remindersList = repository.getReminders() as Result.Success

        assertThat(remindersList.data, `is`(listOf()))
    }



}
