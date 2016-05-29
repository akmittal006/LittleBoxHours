package com.ankurmittal.littleboxhours;

import android.support.test.espresso.IdlingResource;



/**
 * Created by AnkurMittal2 on 28-05-2016.
 */
public class EspressoIdlingResource {

    /*
    *
    * Implementation of MainActivityIdlingResource
    * Maintains static instance of MainActivityIdlingResource
    * */

    private static final String RESOURCE = "GLOBAL";

    private static MainActivityIdlingResource mCountingIdlingResource =
            new MainActivityIdlingResource(RESOURCE);

    // To indicate background task to espresso
    public static void increment() {
        mCountingIdlingResource.increment();
    }

    // To indicate background task is over (Idle state )to espresso
    public static void decrement() {
        mCountingIdlingResource.decrement();
    }

    public static IdlingResource getIdlingResource() {
        return mCountingIdlingResource;
    }
}
