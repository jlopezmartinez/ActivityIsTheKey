package com.adidas.activityisthekey;

import android.content.Context;

import com.adidas.activityisthekey.repository.ActivityRepository;
import com.adidas.activityisthekey.repository.PrefRepository;


/**
 * Hold the business logic
 */

class MainPresenter implements Contract.Presenter {

    private static final int MAX_SECS_WALK = 60000;
    private static final int MAX_SECS_RUN = 30000;
    private static final int MAX_SECS_BIKE = 30000;

    private Contract.View mView;
    private Context mContext;
    private ActivityRepository mRepository;
    private long mStartMillis;

    public MainPresenter(Context context, Contract.View v) {
        mContext = context;
        mView = v;
        mRepository = new PrefRepository(context);
    }

    @Override
    public void onInitSuccess() {
        mView.showStatus(MAX_SECS_WALK);
    }

    @Override
    public void onInitError() {
        mView.showError(mContext.getString(R.string.error_init));
    }


    @Override
    public void onWalkStarted() {

        //save initial system time to count the amount of time during activity
        mStartMillis = System.currentTimeMillis();

        mView.showInActivity(mRepository.getWalkTime(), MAX_SECS_WALK);
    }

    @Override
    public void onMaxActivityReached() {
        //save max amount in the repository
        mRepository.setWalkTime(MAX_SECS_WALK);

        //dismiss the inActivity status
        mView.hideInActivity();
    }

    @Override
    public void onWalkStopped() {
        if (mStartMillis > 0) {
            long millis = System.currentTimeMillis() - mStartMillis;

            //Save values in db, anyone which is listening will receive the update
            mRepository.addWalkTime(millis);

            mStartMillis = 0;
        }

        //go back to status view again
        mView.hideInActivity();
    }

}
