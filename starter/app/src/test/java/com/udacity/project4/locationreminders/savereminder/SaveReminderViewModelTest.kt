package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P, minSdk = Build.VERSION_CODES.P)
class SaveReminderViewModelTest {
    //TODO: provide testing to the SaveReminderView and its live data objects
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource : FakeDataSource

    @get:Rule
    var mainCoroutinesRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
                ApplicationProvider.getApplicationContext(),
                fakeDataSource
        )
    }

    @After
    fun finishKoin() {
        stopKoin()
    }

    @Test
    fun onClear_liveDataValuesReset() {
        saveReminderViewModel.reminderTitle.value = "Title"
        saveReminderViewModel.reminderDescription.value = "Description"
        saveReminderViewModel.selectedPOI.value = PointOfInterest(
                LatLng(20.0,20.0), "id", "name")
        saveReminderViewModel.reminderSelectedLocationStr.value = "name"
        saveReminderViewModel.latitude.value = 20.0
        saveReminderViewModel.longitude.value = 20.0

        saveReminderViewModel.onClear()

        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), nullValue())
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), nullValue())
    }

    @Test
    fun insertReminderAndGetById() = mainCoroutinesRule.runBlockingTest {
        val reminder = ReminderDataItem("title", "description", "Location", 20.0, 20.0)
        saveReminderViewModel.saveReminder(reminder)

        val loaded = (fakeDataSource.getReminder(reminder.id) as Result.Success).data

        assertThat(loaded, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminder_loading() {
        val reminder = ReminderDataItem("title", "description", "Location", 20.0, 20.0)
        mainCoroutinesRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),`is`(true))
        mainCoroutinesRule.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateEnteredData_missingReminderTitle_returnFalse() {
        val reminderNullTitle = ReminderDataItem(null, "description", "Location", 20.0,20.0)
        val reminderEmptyTitle = ReminderDataItem("", "description", "Location", 20.0, 20.0)

        assertThat(saveReminderViewModel.validateEnteredData(reminderNullTitle), `is`(false))
        assertThat(saveReminderViewModel.validateEnteredData(reminderEmptyTitle), `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_missingReminderLocation_returnFalse() {
        val reminderNullLocation = ReminderDataItem("title", "description", null, 20.0, 20.0)
        val reminderEmptyLocation = ReminderDataItem("title", "description", "", 20.0, 20.0)

        assertThat(saveReminderViewModel.validateEnteredData(reminderNullLocation), `is`(false))
        assertThat(saveReminderViewModel.validateEnteredData(reminderEmptyLocation), `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @Test
    fun validateEnteredData_validReminder_returnTrue() {
        val reminder = ReminderDataItem("title", "description", "Location", 20.0,20.0)

        assertThat(saveReminderViewModel.validateEnteredData(reminder), `is`(true))
    }

}