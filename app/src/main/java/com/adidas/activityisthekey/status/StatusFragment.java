package com.adidas.activityisthekey.status;

import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.adidas.activityisthekey.R;
import com.adidas.activityisthekey.repository.ActivityRepository;
import com.adidas.activityisthekey.repository.PrefRepository;
import com.lalongooo.Rings;

/**
 * Summary of the user activity. Listen to the repository for changes.
 */
public class StatusFragment extends Fragment {


    private static final String ARG_MAX_WALK = "maxWalk";
    private static final String ARG_MAX_RUN = "maxRun";
    private static final String ARG_MAX_BIKE = "maxBike";


    private ProgressBar mProgress;

    private long mMaxWalk;
    private long mMaxRun;
    private long mMaxBike;

    private ActivityRepository mRepository;
    private ActivityRepository.TimeUpdateListener mUpdateListener = new ActivityRepository.TimeUpdateListener() {
        @Override
        public void onWalkUpdate(long millis) {
            if (millis > 0) {
               int prev =  mProgress.getProgress();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mProgress.setProgress((int) millis, true);
                }
            } else {
                mProgress.setProgress((int) millis);
            }

        }

        @Override
        public void onRunUpdate(long millis) {
            //nothing by now
        }

        @Override
        public void onBikeUpdate(long millis) {
            //nothing by now
        }
    };

    public StatusFragment() {
        // Required empty public constructor
    }

    private ObjectAnimator getRingAnimator(String propertyName, int initialVal, int nextVal){
        return ObjectAnimator.ofInt(mProgress, propertyName, initialVal, nextVal)
                .setDuration(1000);
    }

    public static StatusFragment newInstance(long maxWalk, long maxRun, long maxBike) {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_MAX_WALK, maxWalk);
        args.putLong(ARG_MAX_RUN, maxRun);
        args.putLong(ARG_MAX_BIKE, maxBike);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMaxWalk = getArguments().getLong(ARG_MAX_WALK);
            mMaxRun = getArguments().getLong(ARG_MAX_RUN);
            mMaxBike = getArguments().getLong(ARG_MAX_BIKE);
        }

        mRepository = new PrefRepository(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgress = view.findViewById(R.id.progress);
        mProgress.setMax((int) mMaxWalk);

//        mRingsView.setRingInnerFirstProgress(transformToRingValue(mRepository.getWalkTime(), mMaxWalk));
//        mRingsView.setRingInnerSecondProgress(transformToRingValue(mRepository.getRunTime(), mMaxRun));
//        mRingsView.setRingInnerThirdProgress(transformToRingValue(mRepository.getBikeTime(), mMaxBike));
    }

    @Override
    public void onStart() {
        super.onStart();
        mRepository.registerForUpdates(mUpdateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mRepository.unregisterForUpdates(mUpdateListener);
    }
}
