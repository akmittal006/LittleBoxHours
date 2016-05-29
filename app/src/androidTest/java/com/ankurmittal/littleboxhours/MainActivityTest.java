package com.ankurmittal.littleboxhours;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.TimePicker;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.ankurmittal.littleboxhours.R.id.toEditText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by AnkurMittal2 on 28-05-2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest extends InstrumentationTestCase{
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private MockWebServer server;
    private IdlingResource idlingResource;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        server = new MockWebServer();
        server.start();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        MainActivity.API = server.url("/").toString();
    }

    private void registerIdlingResources() {
        //registers main activity as idling resource
        Espresso.registerIdlingResources(
                mActivityRule.getActivity().getCountingIdlingResource());
    }

    public void unregisterIdlingResource() {
        //unregisters
        Espresso.unregisterIdlingResources(
                mActivityRule.getActivity().getCountingIdlingResource());
    }

//    @Test
//    public void isTextDisplayed () {
//        onView(withText("Hello")).check(matches(isDisplayed()));
//    }


    /*
    *
    * TESTS BASED ON GETTING WORKING HOURS
    *
    * */

    //CASE 1 : RETRIEVED SUCCESSFULLY

    @Test
    public void fetchWorkingHoursSuccessfully() throws Exception{
        String fileName = "success.json";
        //set server response code to 200 and send success.json as body
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(RestServiceTestHelper.getStringFromFile(getInstrumentation().getContext(), fileName)));
        //launches Activity intent for testing
        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);

        //should display progess dialog with following string
        onView(withText("Fetching your working hours.")).check(matches(isDisplayed()));
        //should update fromEditText to "1:11 AM" as retrieved from success.json
        // and toEditText to "2:22 AM"
        registerIdlingResources();
        onView(withText("01:11 AM")).check(matches(isDisplayed()));
        onView(withText("02:22 PM")).check(matches(isDisplayed()));
        unregisterIdlingResource();
    }

    //CASE 2 : RESPONSE SENT BUT WITH ERROR

    @Test
    public void fetchWorkingHoursWithError() throws Exception{
        String fileName = "error.json";
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(RestServiceTestHelper.getStringFromFile(getInstrumentation().getContext(), fileName)));

        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);
        //should display progess dialog with following string
        onView(withText("Fetching your working hours.")).check(matches(isDisplayed()));
        registerIdlingResources();
        //should show fromEditText as "9:00 AM"
        //and toEditText as  "5:30 PM" as retrieved from dummy working hours (in Main Activity)
        onView(withText("09:00 AM")).check(matches(isDisplayed()));
        onView(withText("05:30 PM")).check(matches(isDisplayed()));

        //add
//        onView(withText("Sorry an error occurred"))
//                .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
//                .check(matches(isDisplayed()));
        unregisterIdlingResource();
    }

    //CASE 3 : RESPONSE/NETWORK FAILURE

    @Test
    public void fetchWorkingHoursWithFailure() throws Exception{

        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);
        //should display progess dialog with following string
        onView(withText("Fetching your working hours.")).check(matches(isDisplayed()));
        //should update fromEditText to "01:11 AM" as retrieved from success.json
        registerIdlingResources();
        //check if correct toast is displayed
        onView(withText("Network error !"))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        unregisterIdlingResource();

    }

    /*
    *
    * TESTS BASED FAB AND TIME PICKER INTERACTIONS
    *
    * */

    @Test
    public void onEditFabClicked() {
        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);
        registerIdlingResources();
        //editTexts should be disbaled
        onView(withId(R.id.fromEditText)).check(matches(not(isEnabled())));
        onView(withId(toEditText)).check(matches(not(isEnabled())));
        //Click fab button
        onView(withId(R.id.editFab)).perform(click());
        //editTexts should be enabled
        onView(withId(R.id.fromEditText)).check(matches(isEnabled()));
        onView(withId(toEditText)).check(matches(isEnabled()));

    }

    @Test
    public void onToPickerDialogOpened() {
        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);
        registerIdlingResources();
        //on Click fab button
        onView(withId(R.id.editFab)).perform(click());
        //focus on toEditTex
        onView(withId(R.id.toEditText)).perform(click());
        //time picker dialog should open
        onView(Matchers.<View>instanceOf(TimePicker.class)).check(matches(isDisplayed()));
        //set picker time
        onView(Matchers.<View>instanceOf(TimePicker.class)).perform(PickerActions.setTime(19,19));
        //click ok
        onView(withText("OK")).perform(click());
        // verify text on toEditText
        onView(withId(R.id.toEditText)).check(matches(withText("07:19 PM")));
        unregisterIdlingResource();

    }

    @Test
    public void onFromPickerDialogOpened() {
        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);
        registerIdlingResources();
        //on Click fab button
        onView(withId(R.id.editFab)).perform(click());
        //focus on toEditTex
        onView(withId(R.id.fromEditText)).perform(click());
        //time picker dialog should open
        onView(Matchers.<View>instanceOf(TimePicker.class)).check(matches(isDisplayed()));
        //set picker time
        onView(Matchers.<View>instanceOf(TimePicker.class)).perform(PickerActions.setTime(19,19));
        //click ok
        onView(withText("OK")).perform(click());
        // verify text on toEditText
        onView(withId(R.id.fromEditText)).check(matches(withText("07:19 PM")));
        unregisterIdlingResource();

    }

    @Test
    public void onCancelFabClicked() {
        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);
        registerIdlingResources();

        //on Click fab button
        onView(withId(R.id.editFab)).perform(click());
        //focus on fromEditText
        onView(withId(R.id.fromEditText)).perform(click());
        //set from picker time
        onView(Matchers.<View>instanceOf(TimePicker.class)).perform(PickerActions.setTime(19,19));
        //click ok
        onView(withText("OK")).perform(click());
        //focus on toEditText
        onView(withId(R.id.toEditText)).perform(click());
        //set to picker time
        onView(Matchers.<View>instanceOf(TimePicker.class)).perform(PickerActions.setTime(19,19));
        //click ok
        onView(withText("OK")).perform(click());

        // click on cancel fab
        onView(withId(R.id.cancelFab)).perform(click());
        //should isplay dummy working hours from 9:00 AM to 5:30 PM
        onView(withId(R.id.toEditText)).check(matches(withText("05:30 PM")));
        onView(withId(R.id.fromEditText)).check(matches(withText("09:00 AM")));

        unregisterIdlingResource();
    }

    /*
    *
    * TESTS BASED ON UPDATING API
    *
    * */

    //CASE 1  : UPDATING SUCCESSFULLY

    @Test
    public void onSaveFabClicked() {
        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);
        registerIdlingResources();

        try {
            // For fetching working hours
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(RestServiceTestHelper.getStringFromFile(getInstrumentation().getContext(), "success.json")));

            /*
            * File updatedworkinghour.json contains expected data
            * i.e From "07:19 PM"
            *     TO   "05:30 PM"
            * */

            //response for updating working hours
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(RestServiceTestHelper.getStringFromFile(getInstrumentation().getContext(), "updatedworkinghour.json")));
            // update working hours using data send in request body
        } catch (Exception e) {
            e.printStackTrace();
        }
        //on Click fab button
        onView(withId(R.id.editFab)).perform(click());
        //focus on fromEditText
        onView(withId(R.id.fromEditText)).perform(click());
        //set from picker time
        onView(Matchers.<View>instanceOf(TimePicker.class)).perform(PickerActions.setTime(19,19));
        //click ok
        onView(withText("OK")).perform(click());
        //click on save icon fab
        onView(withId(R.id.editFab)).perform(click());
        //should update working hours
        onView(withId(R.id.fromEditText)).check(matches(not(isEnabled())));
        onView(withId(R.id.fromEditText)).check(matches(withText("07:19 PM")));
        unregisterIdlingResource();
    }

    //CASE 2 : RESPONSE SENT BUT WITH ERROR, SHOULD SHOW "SUCCESS.JSON" DATA

    @Test
    public void onUpdateWorkingHoursWithError() {
        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);
        registerIdlingResources();

        try {
            // For fetching working hours
            server.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(RestServiceTestHelper.getStringFromFile(getInstrumentation().getContext(), "success.json")));

            //response for updating working hours
            server.enqueue(new MockResponse()
                    .setResponseCode(400)
                    .setBody(RestServiceTestHelper.getStringFromFile(getInstrumentation().getContext(), "updatedworkinghour.json")));
            // update working hours using data send in request body
        } catch (Exception e) {
            e.printStackTrace();
        }
        //on Click fab button
        onView(withId(R.id.editFab)).perform(click());
        //focus on fromEditText
        onView(withId(R.id.fromEditText)).perform(click());
        //set from picker time
        onView(Matchers.<View>instanceOf(TimePicker.class)).perform(PickerActions.setTime(19,19));
        //click ok
        onView(withText("OK")).perform(click());
        //click on save icon fab
        onView(withId(R.id.editFab)).perform(click());
        //due to error fromEditText should display previous fetched data not "07:19 PM"
        // i.e From "01:11 AM"
        onView(withId(R.id.fromEditText)).check(matches(not(isEnabled())));
        onView(withId(R.id.fromEditText)).check(matches(withText("01:11 AM")));
        unregisterIdlingResource();
    }

    //CASE 3 : RESPONSE / NETWORK FAILURE, SHOULD SHOW "DUMMY (IN MAIN ACTIVITY)" DATA

    @Test
    public void onUpdateWorkingHoursWithFailure() {
        Intent intent = new Intent();
        mActivityRule.launchActivity(intent);
        registerIdlingResources();
        // No need for server response

        //on Click fab button
        onView(withId(R.id.editFab)).perform(click());
        //focus on fromEditText
        onView(withId(R.id.fromEditText)).perform(click());
        //set from picker time
        onView(Matchers.<View>instanceOf(TimePicker.class)).perform(PickerActions.setTime(19,19));
        //click ok
        onView(withText("OK")).perform(click());
        //click on save icon fab
        onView(withId(R.id.editFab)).perform(click());
        //due to error fromEditText should display dummy data not "07:19 PM"
        // i.e From "09:00 AM"
        onView(withId(R.id.fromEditText)).check(matches(not(isEnabled())));
        onView(withId(R.id.fromEditText)).check(matches(withText("09:00 AM")));

        //check for network error toast

        onView(withText("Network error !"))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        unregisterIdlingResource();
    }

    //AFTER TESTS

    @After
    public void shutdown() {

        // shutdown server after , important
        try {
            server.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
