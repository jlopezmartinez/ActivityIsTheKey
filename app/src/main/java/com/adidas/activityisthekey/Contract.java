package com.adidas.activityisthekey;

/**
 * Created by mengujua on 11/11/17.
 */

public interface Contract {

    interface Presenter {

        /**
         * Called when the view is ready to start, in other words, has setup everything thta needs to be setup.
         */
        void onInitSuccess();

        /**
         * Called when some error happened during setup
         */
        void onInitError();

        /**
         * The user has started its activity
         */
        void onWalkStarted();

        /**
         * User has reached the activity threshold
         */
        void onMaxActivityReached();

        /**
         * User has stopped the activity
         */
        void onWalkStopped();
    }

    interface View {

        /**
         * Display the user is currently performing an activity
         * @param currentMillis
         * @param maxmillis
         */
        void showInActivity(long currentMillis, long maxmillis);

        /**
         * Show the summary of the realized activity
         * @param maxMillis
         */
        void showStatus(long maxMillis);


        /**
         * Display error screen
         * @param string
         */
        void showError(String string);


        /**
         * Hide activity in progress indicator and go back to summary
         */
        void hideInActivity();
    }

}
