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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.adidas.activityisthekey.inActivity.InActivityFragment;
import com.adidas.activityisthekey.repository.ActivityRepository;
import com.adidas.activityisthekey.status.StatusFragment;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.view.View.GONE;
import static com.adidas.activityisthekey.inActivity.InActivityFragment.ACT_WALK;

public class MainActivity extends AppCompatActivity implements Contract.View, InActivityFragment.OnInActivtyFragmentListener, ErrorFragment.OnErrorFragmentListener {

    private static final String TAG = "MainActivity";
    private static final int REQ_PERMISSION_LOCATION = 1001;

    private static final String FENCE_RECEIVER_ACTION = "fences";
    private static final String FENCE_KEY_WALKING_START = "walkingFenceStart";
    private static final String FENCE_KEY_WALKING_END = "walkingFenceEnd";
    private static final String FENCE_KEY_RUN_START = "runningFenceStart";
    private static final String FENCE_KEY_RUN_END = "runFenceEnd";
    private static final String FENCE_KEY_BIKE = "bikeFence";


    private static final String IN_ACTIVITY_TAG = "InActivityTag";

    // Declare variables for pending intent and fence receiver.
    private PendingIntent mPendingIntent;
    private MyFenceReceiver myFenceReceiver;
    private GoogleApiClient mGoogleApiClient;

    private ActivityRepository mRepository;

    private Contract.Presenter mPresenter;

    private long mStartMillis = 0;

    private Button mWalkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Not needed in this final version
        //requestPermissions();

        mPresenter = new MainPresenter(this, this);

        //buttons to test behaviour without actually running or walk
        mWalkButton = findViewById(R.id.fake);
        mWalkButton.setVisibility(GONE);
        mWalkButton.setOnClickListener(new View.OnClickListener() {
            boolean walking = false;

            @Override
            public void onClick(View view) {
                if (walking) {
                    onUserActivityStopped(FENCE_KEY_WALKING_START);
                } else {
                    onUserActivityStarted(FENCE_KEY_WALKING_START);
                }
                walking = !walking;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.extra:
                boolean visible = mWalkButton.getVisibility() == View.VISIBLE;
                mWalkButton.setVisibility(visible ? View.GONE : View.VISIBLE);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //register broadcast for fence conditions
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        myFenceReceiver = new MyFenceReceiver();
        registerReceiver(myFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));

        setupCells();
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(myFenceReceiver);
        unregisterFences();
    }

    @Override
    public void showStatus(long maxMillis) {
        Fragment fr = StatusFragment.newInstance(maxMillis, 0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            fr.setEnterTransition(new Fade());
            fr.setExitTransition(new Fade());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fr, IN_ACTIVITY_TAG)
                .commit();
    }

    @Override
    public void showInActivity(long currentMillis, long maxmillis) {
        Fragment fr = InActivityFragment.newInstance(ACT_WALK, currentMillis, maxmillis);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fr.setEnterTransition(new Slide(Gravity.BOTTOM));
            fr.setExitTransition(new Slide(Gravity.BOTTOM));
        }
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, fr)
                .commit();
    }


    @Override
    public void showError(String errorMsg) {
        Log.d(TAG, "error: " + errorMsg);

        Fragment fr = ErrorFragment.newInstance(errorMsg);
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, fr)
                .commit();
    }

    private void onUserActivityStopped(String fenceKey) {

       mPresenter.onWalkStopped();

    }

    private void onUserActivityStarted(String fenceKey) {
        Log.d(TAG, "user activity started: " + fenceKey);

        if (fenceKey.equals(FENCE_KEY_WALKING_START)) {
            mPresenter.onWalkStarted();
        }
    }

    @Override
    public void hideInActivity() {
        if (getSupportFragmentManager().findFragmentByTag(IN_ACTIVITY_TAG) != null) {
            getSupportFragmentManager().popBackStack();
        }
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

//    private void initGoogleApiClient() {
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Awareness.API)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(@Nullable Bundle bundle) {
//                        Log.d(TAG, "Google client registered correctly");
//                        setupCells();
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int i) {
//                        Log.d(TAG, "error connecting to GoogleApiClient");
//                        showError("init");
//                    }
//                })
//                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//                        Log.d(TAG, "error connecting to GoogleApiClient");
//                        showError("init");
//                    }
//                })
//                .build();
//        mGoogleApiClient.connect();
//    }

    private void setupCells() {

        //TODO get from db which fences has been completed

        // Create a fence.
        AwarenessFence activityFence = DetectedActivityFence.during(DetectedActivityFence.ON_FOOT);
        //TODO create and add more fences

        // Register the fence to receive callbacks.
        Awareness.getFenceClient(this)
                .updateFences(new FenceUpdateRequest.Builder()
                .addFence(FENCE_KEY_WALKING_START, activityFence, mPendingIntent)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Fence was successfully registered.");
                        mPresenter.onInitSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Fence could not be registered: " + e);
                        mPresenter.onInitError();
                    }
                });


//        Awareness.FenceApi.updateFences(
//                mGoogleApiClient,
//                new FenceUpdateRequest.Builder()
//                        .addFence(FENCE_KEY_WALKING_START, activityFence, mPendingIntent)
//                        //.addFence(FENCE_KEY_WALKING_END, walkingFenceEnd, mPendingIntent)
//                        //.addFence(FENCE_KEY_RUN_START, runningFenceStart, mPendingIntent)
//                        //.addFence(FENCE_KEY_RUN_END, runningFenceEnd, mPendingIntent)
//
//                        //.addFence(FENCE_KEY_RUN_START, runningFence, mPendingIntent)
//                        //.addFence(FENCE_KEY_BIKE, bikeFence, mPendingIntent)
//                        .build())
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(@NonNull Status status) {
//                        if (status.isSuccess()) {
//                            Log.i(TAG, "Fence was successfully registered.");
//                            onReadyToStart();
//                        } else {
//                            Log.e(TAG, "Fence could not be registered: " + status);
//                            showError(ERROR_FENCES_NOT_REGISTERED);
//                        }
//                    }
//                });
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
                    Log.d(TAG, "Permissions granted, init google client");

                } else {
                    //mPresenter.onPermissionError();
                    showError("permissions");
                }
                return;
            }
        }
    }

    @Override
    public void onMaxReached(int userActivity, long max) {
        mPresenter.onMaxActivityReached();
    }

    @Override
    public void onRetryClick() {
        //retrying the easy way
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    public class MyFenceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive()");

            FenceState fenceState = FenceState.extract(intent);
            String fenceKey = fenceState.getFenceKey();
            Log.d(TAG, "fence:" + fenceKey + ", status:" + fenceState.getCurrentState());
            if (fenceKey != null) {
                if (fenceKey.equals(FENCE_KEY_WALKING_START)) {
                    if (fenceState.getCurrentState() == FenceState.TRUE) {
                        onUserActivityStarted(FENCE_KEY_WALKING_START);
                    } else {
                        onUserActivityStopped(FENCE_KEY_WALKING_START);
                    }
                }

                if (fenceKey.equals(FENCE_KEY_WALKING_END) && fenceState.getCurrentState() == FenceState.TRUE) {
                    onUserActivityStopped(FENCE_KEY_WALKING_START);
                }

                if (fenceKey.equals(FENCE_KEY_RUN_START) && fenceState.getCurrentState() == FenceState.TRUE) {
                    onUserActivityStarted(FENCE_KEY_RUN_START);
                }

                if (fenceKey.equals(FENCE_KEY_RUN_END) && fenceState.getCurrentState() == FenceState.TRUE) {
                    onUserActivityStopped(FENCE_KEY_RUN_START);
                }
            }
//            switch (fenceState.getCurrentState()) {
//                case FenceState.TRUE:
//                    Log.i(TAG, "User is walking");
//                    onUserActivityStarted(fenceKey);
//                    break;
//                case FenceState.FALSE:
//                    Log.i(TAG, "User stopped walking");
//                    onUserActivityStopped(fenceKey);
//                    break;
//                case FenceState.UNKNOWN:
//                    Log.i(TAG, "The " + fenceKey + " fence is in an unknown state.");
//                    onUnknownState();
//                    break;
//            }

        }

    }

    private void unregisterFences() {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(FENCE_KEY_WALKING_START)
                        .build())
                .setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                // Fence removed!
            }

            @Override
            public void onFailure(@NonNull Status status) {
                // Oops, the fence wasn't removed...
            }
        });
    }

}
