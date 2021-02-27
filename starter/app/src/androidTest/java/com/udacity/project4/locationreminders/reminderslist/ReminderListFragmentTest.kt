package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource.Companion.ERROR_MESSAGE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var fakeDataSource: FakeAndroidDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                        appContext,
                        get() as FakeAndroidDataSource
                )
            }
            single { FakeAndroidDataSource() }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        fakeDataSource = get()
    }

    //    TODO: test the displayed data on the UI.
    @Test
    fun nonEmptyReminderList_itemsDisplayedInUi() = runBlockingTest {
        val reminder = ReminderDTO("title", "description", "location", 20.0, 20.0)
        fakeDataSource.saveReminder(reminder)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withText(reminder.title)).check(matches(isDisplayed()))
    }

    //    TODO: test the navigation of the fragments.
    @Test
    fun clickAddReminder_navigateToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
                ReminderListFragmentDirections.toSaveReminder()
        )
    }

    //    TODO: add testing for the error messages.
    @Test
    fun loadingError_displayErrorSnackBar() {
        fakeDataSource.setReturnError(true)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(ERROR_MESSAGE)))
    }

}
