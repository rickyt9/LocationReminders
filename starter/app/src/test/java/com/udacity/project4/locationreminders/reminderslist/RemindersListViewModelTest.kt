package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.FakeDataSource.Companion.ERROR_MESSAGE
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(maxSdk = Build.VERSION_CODES.P, minSdk = Build.VERSION_CODES.P)
class RemindersListViewModelTest {
    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource : FakeDataSource

    @get:Rule
    var mainCoroutinesRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @After
    fun finishKoin() {
        stopKoin()
    }

    @Test
    fun loadReminders_RemindersListLoaded() = mainCoroutinesRule.runBlockingTest {
        val reminder = ReminderDTO("title", "description", "location", 20.0, 20.0)
        fakeDataSource.saveReminder(reminder)
        remindersListViewModel.loadReminders()

        val loaded = remindersListViewModel.remindersList.getOrAwaitValue()[0]

        assertThat(loaded, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun loadReminders_ReturnException() = mainCoroutinesRule.runBlockingTest {
        fakeDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(ERROR_MESSAGE))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_loading() = mainCoroutinesRule.runBlockingTest {
        mainCoroutinesRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutinesRule.resumeDispatcher()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun invalidateShowNoData_EmptyRemindersList() {
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun invalidateShowNoData_NonEmptyRemindersList() = mainCoroutinesRule.runBlockingTest {
        val reminder = ReminderDTO("title", "description", "location", 20.0, 20.0)
        fakeDataSource.saveReminder(reminder)
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }
}