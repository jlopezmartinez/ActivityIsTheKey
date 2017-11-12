package com.adidas.activityisthekey.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * User's activity repository based in SharedPreferences
 */
public class PrefRepository implements ActivityRepository {


    private static final String PREF_WALK_TIME = "walkTime";
    private static final String PREF_RUN_TIME = "runTime";
    private static final String PREF_BIKE_TIME = "bikeTime";

    private SharedPreferences mPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;
    public PrefRepository (Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void addWalkTime(long time) {
        addValue(PREF_WALK_TIME, time);
    }

    @Override
    public void addRunTime(long time) {
        addValue(PREF_RUN_TIME, time);
    }

    @Override
    public void addBikeTime(long time) {
        addValue(PREF_BIKE_TIME, time);
    }

    @Override
    public void setWalkTime(long time) {
        mPrefs.edit().putLong(PREF_WALK_TIME, time).commit();
    }

    @Override
    public void setRunTime(long time) {
        mPrefs.edit().putLong(PREF_RUN_TIME, time).commit();
    }

    @Override
    public void setBikeTime(long time) {
        mPrefs.edit().putLong(PREF_BIKE_TIME, time).commit();
    }

    @Override
    public long getWalkTime() {
        return mPrefs.getLong(PREF_WALK_TIME, 0);
    }

    @Override
    public long getRunTime() {
        return mPrefs.getLong(PREF_RUN_TIME, 0);
    }

    @Override
    public long getBikeTime() {
        return mPrefs.getLong(PREF_BIKE_TIME, 0);
    }

    private void addValue(String prefName, long value) {
        long prev = mPrefs.getLong(prefName, 0);
        mPrefs.edit()
                .putLong(prefName, prev + value)
                .commit();
    }

    /**
     * Register for updates in the database. When registering, a first call is made providing the
     * current values
     * @param listener
     */
    @Override
    public void registerForUpdates(final TimeUpdateListener listener) {

        //We are only accepting one listener by now,
        //this prevents to have leaking listener
        if (mListener != null) {
            unregisterForUpdates(listener);
        }

        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                long value = sharedPreferences.getLong(s, 0);
                if (s.equals(PREF_WALK_TIME)) {
                    listener.onWalkUpdate(value);
                } else if (s.equals(PREF_RUN_TIME)) {
                    listener.onRunUpdate(value);
                } else if (s.equals(PREF_BIKE_TIME)) {
                    listener.onBikeUpdate(value);
                }
            }
        };

        mPrefs.registerOnSharedPreferenceChangeListener(mListener);

        //call first with the current value
        listener.onWalkUpdate(mPrefs.getLong(PREF_WALK_TIME, 0));
        listener.onRunUpdate(mPrefs.getLong(PREF_RUN_TIME, 0));
        listener.onBikeUpdate(mPrefs.getLong(PREF_BIKE_TIME, 0));

    }

    @Override
    public void unregisterForUpdates(TimeUpdateListener listener) {
        mPrefs.unregisterOnSharedPreferenceChangeListener(mListener);
    }

}
