package com.adidas.activityisthekey.repository;

/**
 * Created by mengujua on 11/11/17.
 */

public interface ActivityRepository {

    void addWalkTime(long time);
    void addRunTime(long time);
    void addBikeTime(long time);

    void setWalkTime(long time);
    void setRunTime(long time);
    void setBikeTime(long time);


    long getWalkTime();
    long getRunTime();
    long getBikeTime();

    void registerForUpdates(TimeUpdateListener listener);
    void unregisterForUpdates(TimeUpdateListener listener);

    interface TimeUpdateListener {
        void onWalkUpdate(long secs);
        void onRunUpdate(long secs);
        void onBikeUpdate(long secs);
    }

}

