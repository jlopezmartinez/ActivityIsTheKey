package com.adidas.activityisthekey.inActivity;


import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adidas.activityisthekey.R;


/**
 * Display indicator of the current actiivty being done
 */
public class InActivityFragment extends Fragment {

    public interface OnInActivtyFragmentListener {
        void onMaxReached(int userActivity, long max);
    }

    private static final String ARG_CURRENT_SECS = "current";
    private static final String ARG_MAX_SECS = "max";
    private static final String ARG_ACTIVITY_KIND = "activityKind";

    public static final int ACT_WALK = 1;
    public static final int ACT_RUN = 2;
    public static final int ACT_BIKE = 3;

    private int mActivityKind;
    private long mCurrentMillis;
    private long mMaxMillis;
    private OnInActivtyFragmentListener mListener;

    private TextView mActivityView;
    private TextView mTimerView;
    private CountDownTimer mCountrDownTimer;


    public static Fragment newInstance(int activityKind, long currentSecs, long maxSecs) {
        InActivityFragment fr = new InActivityFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTIVITY_KIND, activityKind);
        args.putLong(ARG_CURRENT_SECS, currentSecs);
        args.putLong(ARG_MAX_SECS, maxSecs);
        fr.setArguments(args);

        return fr;
    }

    public InActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityKind = getArguments().getInt(ARG_ACTIVITY_KIND);
        mCurrentMillis = getArguments().getLong(ARG_CURRENT_SECS);
        mMaxMillis = getArguments().getLong(ARG_MAX_SECS);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnInActivtyFragmentListener) {
            mListener = (OnInActivtyFragmentListener) context;
        } else {
            throw new RuntimeException("parent activity must implement OnInActiivtyFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_in_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mActivityView = view.findViewById(R.id.current_activity);
        mTimerView = view.findViewById(R.id.timer);

        switch (mActivityKind) {
            case ACT_WALK:
                mActivityView.setText(R.string.activity_walking);
                view.setBackgroundColor(getResources().getColor(R.color.walking));
                break;
            case ACT_RUN:
                mActivityView.setText(R.string.activity_running);
                view.setBackgroundColor(getResources().getColor(R.color.running));
                break;
            case ACT_BIKE:
                mActivityView.setText(R.string.activity_biking);
                view.setBackgroundColor(getResources().getColor(R.color.biking));
                break;
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        mCountrDownTimer = new CountDownTimer((mMaxMillis - mCurrentMillis) , 1000) {

            long secsLeft = (mMaxMillis - mCurrentMillis)/1000;
            public void onTick(long millisUntilFinished) {
                secsLeft = secsLeft > 0?secsLeft-1:0;
                mTimerView.setText("" + secsLeft);

            }

            public void onFinish() {
                //finished, notify activity
                mListener.onMaxReached(mActivityKind, mMaxMillis);
            }

        }.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCountrDownTimer.cancel();
    }
}
