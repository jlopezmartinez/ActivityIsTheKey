package com.adidas.activityisthekey;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements InActivityFragment.OnInActivtyFragmentListener{

    private static final String TAG = "MainActivity";
    private static final int REQ_PERMISSION_LOCATION = 1001;

    private static final String FENCE_RECEIVER_ACTION = "fences";
    private static final String FENCE_KEY_WALKING = "walkingFence";
    private static final String FENCE_KEY_RUN = "runningFence";

    private static final String ERROR_NO_PERMISSIONS = "no permissions";
    private static final String ERROR_FENCES_NOT_REGISTERED = "no fences";
    private static final int MAX_SECS_WALK = 60;
    private static final int MAX_SECS_RUN = 30;
    private static final int MAX_SECS_BIKE = 30;

    // Declare variables for pending intent and fence receiver.
    private PendingIntent myPendingIntent;
    private MyFenceReceiver myFenceReceiver;
    private GoogleApiClient mGoogleApiClient;

    private TextView mCurrentActivityText;
    //private MainPresenter mPresenter;

    private long mStartMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        //mPresenter = new MainPresenter();

        mCurrentActivityText = findViewById(R.id.current_activity);
        showActivitySummary();

        Button fakeButton = findViewById(R.id.fake);
        fakeButton.setOnClickListener(new View.OnClickListener() {
            boolean walking = false;
            @Override
            public void onClick(View view) {
                if (walking) {
                    onUserActivityStopped(FENCE_KEY_WALKING);
                } else {
                    onUserActivityStarted(FENCE_KEY_WALKING);
                }
                walking = !walking;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //register broadcast for fence conditions
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        myPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        myFenceReceiver = new MyFenceReceiver();
        registerReceiver(myFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));


    }

    @Override
    protected void onStop(){
        super.onStop();

        unregisterReceiver(myFenceReceiver);
    }

    private void onReadyToStart() {
        //mPresenter.onReadyToStart();
        showActivitySummary();
    }

    public void showActivitySummary() {
        mCurrentActivityText.setText("Ready");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, StatusFragment.newInstance())
                .commit();
    }

    // Handle the callback on the Intent.
    private void onUnknownState() {
        //mPresenter.onUnknownError();
        showError("unknown");
    }

    private void onUserActivityStopped(String fenceKey) {
        mCurrentActivityText.setText("Stopped " + fenceKey);
        onBackPressed();

        if (mStartMillis > 0) {
            updateProgress(fenceKey, System.currentTimeMillis() - mStartMillis);
            mStartMillis = 0;
        }
    }

    private void updateProgress(String fenceKey, long time) {
        //TODO save in BD so it is updated in fragment
    }

    private void onUserActivityStarted(String fenceKey) {
        mCurrentActivityText.setText("Started " + fenceKey);

        //save initial system time to count the amount of time during activity
        mStartMillis = System.currentTimeMillis();

        int activity;
        int maxSecs;
        int currentSecs = 0;
        if (fenceKey.equals(FENCE_KEY_WALKING)) {
            activity = InActivityFragment.ACT_WALK;
            maxSecs = MAX_SECS_WALK;

        } else if (fenceKey.equals(FENCE_KEY_RUN)){
            activity = InActivityFragment.ACT_RUN;
            maxSecs = MAX_SECS_RUN;

        } else {
            activity = InActivityFragment.ACT_BIKE;
            maxSecs = MAX_SECS_BIKE;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, InActivityFragment.newInstance(activity, currentSecs, maxSecs))
                .commit();

    }

    private void showError(String errorKey) {
        //TODO show screen in error mode
        mCurrentActivityText.setText("Error:" + errorKey);
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Show dialog with explanation
                new AlertDialog.Builder(this)
                        .setMessage(R.string.alert_permission_location_needed)
                        .setPositiveButton(R.string.alert_permission_location_positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //request permission again
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQ_PERMISSION_LOCATION);
                            }
                        })
                        .setNegativeButton(R.string.alert_permission_location_negative_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO presenter
                                //stubborn user, show error
                                //mPresenter.onPermissionError();
                                showError("permissions");
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQ_PERMISSION_LOCATION);
            }
        }
    }

    private void registerCells() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        mGoogleApiClient.connect();

        //TODO get from db which fences has been completed

        //create fences
        // Create a fence.
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);
        //TODO create and add more fences


        // Register the fence to receive callbacks.
        // The fence key uniquely identifies the fence.
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(FENCE_KEY_WALKING, walkingFence, myPendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                            onReadyToStart();
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                            showError(ERROR_FENCES_NOT_REGISTERED);
                        }
                    }
                });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //continue being happy
                    registerCells();

                } else {
                    //mPresenter.onPermissionError();
                    showError("permissions");
                }
                return;
            }
        }
    }

    @Override
    public void onMaxReached(int max) {
        //TODO save activity and update progress

        onBackPressed();
    }

    public class MyFenceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            FenceState fenceState = FenceState.extract(intent);
            String fenceKey = fenceState.getFenceKey();
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.i(TAG, "User is walking");
                    onUserActivityStarted(fenceKey);
                    break;
                case FenceState.FALSE:
                    Log.i(TAG, "User stopped walking");
                    onUserActivityStopped(fenceKey);
                    break;
                case FenceState.UNKNOWN:
                    Log.i(TAG, "The headphone fence is in an unknown state.");
                    onUnknownState();
                    break;
            }

        }

    }
}
