package com.udacity.project4

import android.app.Application
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository : ReminderDataSource

    @get:Rule
    var activityRule = object : ActivityTestRule<RemindersActivity>(RemindersActivity::class.java) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()
            Log.d(RemindersActivityTest::class.java.simpleName, "beforeActivityLaunched")
            stopKoin()
            val appContext = getApplicationContext<Application>()
            val myModule = module {
                viewModel {
                    RemindersListViewModel(
                        appContext,
                        get() as ReminderDataSource
                    )
                }
                single {
                    SaveReminderViewModel(
                        appContext,
                        get() as ReminderDataSource
                    )
                }
                single { RemindersLocalRepository(get()) as ReminderDataSource }
                single { LocalDB.createRemindersDao(appContext) }
            }
            startKoin {
                modules(myModule)
            }

            repository = get()
            runBlocking {
                repository.deleteAllReminders()
            }
        }
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */

    // add End to End testing to the app
    @Test
    fun createReminder() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        val saveReminderViewModel : SaveReminderViewModel = get()
        val activity = activityRule.activity

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Tickets"), closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(
            typeText("Buy two tickets"),
            closeSoftKeyboard()
        )
        saveReminderViewModel.reminderSelectedLocationStr.postValue("Cinema")
        saveReminderViewModel.latitude.postValue(20.0)
        saveReminderViewModel.longitude.postValue(20.0)

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText("Tickets")).check(matches(isDisplayed()))
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(activity.window.decorView))))
                .check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun createReminder_missingTitle() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        val saveReminderViewModel : SaveReminderViewModel = get()

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderDescription)).perform(
            typeText("Buy two tickets"),
            closeSoftKeyboard()
        )
        saveReminderViewModel.reminderSelectedLocationStr.postValue("Cinema")
        saveReminderViewModel.latitude.postValue(20.0)
        saveReminderViewModel.longitude.postValue(20.0)

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Please enter title")))

        activityScenario.close()
    }

    @Test
    fun createReminder_missingLocation() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Tickets"), closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(
            typeText("Buy two tickets"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Please select location")))

        activityScenario.close()
    }

    @Test
    fun loadReminderDetailFragment_displayReminderData() {
        val reminder = ReminderDataItem("Tickets", "Buy two tickets", "Cinema", 20.0, 20.0)
        val intent = ReminderDescriptionActivity.newIntent(getApplicationContext(), reminder)
        val activityScenario = ActivityScenario.launch<ReminderDescriptionActivity>(intent)

        onView(withId(R.id.reminder_title_tv)).check(matches(withText(
            containsString(reminder.title))))

        onView(withId(R.id.reminder_description_tv)).check(matches(withText(
            containsString(reminder.description))))

        onView(withId(R.id.selected_location_tv)).check(matches(withText(
            containsString(reminder.location))))

        activityScenario.close()
    }
}
