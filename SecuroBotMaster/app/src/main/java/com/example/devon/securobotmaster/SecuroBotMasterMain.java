package com.example.devon.securobotmaster;

import com.example.devon.securobotmaster.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import java.util.Random;


public class SecuroBotMasterMain extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    //**********************************************
    ImageView leftEye;
    ImageView rightEye;
    int lEResource;
    int rEResource;
    ImageView eyesView;
    private Handler mHandler;
    Random r = new Random();
    int eyeChooser = 0;
    int openEyeResource = R.drawable.ava_eyes_open;
    int closedEyeResource = R.drawable.ava_eyes_closed;
    //**********************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_securo_bot_master_main);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("eyes", "touch");
                changeEyes();
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });


        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        if(actionBar!=null) actionBar.hide();

//**************************************************************************************************
                                //SecuroBot setup stuff
//**************************************************************************************************
        /*
        //left
        leftEye = (ImageView) findViewById(R.id.leftEye);
        lEResource = R.drawable.blueeyesopenleft;
        leftEye.setImageResource(lEResource);
        //right
        rightEye = (ImageView) findViewById(R.id.rightEye);
        rEResource = R.drawable.blueeyesopenright;
        rightEye.setImageResource(rEResource);
        */
        eyesView = (ImageView) findViewById(R.id.eyes);

        mHandler = new Handler();

        startRepeatingTask();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    //Touch listener to use for in-layout UI controls to delay hiding the
    //system UI. This is to prevent the jarring behavior of controls going away
    //while interacting with activity UI.
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            Log.d("eyes", "touch");
            changeEyes();
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    //Schedules a call to hide() in [delay] milliseconds, canceling any
    //previously scheduled calls.
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

//**************************************************************************************************
                                        //Threads
//**************************************************************************************************
    void startRepeatingTask() {
        eyesOpen.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(eyesOpen);
    }

    Runnable eyesOpen = new Runnable() {
        @Override
        public void run() {
            eyesView.setImageResource(openEyeResource);   //eyesOpenChanger()

            int blinkEyes = r.nextInt(100-0);
            if(blinkEyes>=90) blink.run();
            else mHandler.postDelayed(eyesOpen, 1000);
        }
    };

    Runnable blink = new Runnable(){
        @Override
        public void run() {
            mHandler.removeCallbacks(eyesOpen);
            eyesView.setImageResource(closedEyeResource);
            mHandler.postDelayed(eyesOpen, 100);
        }
    };

    public void changeEyes(){
        Log.d("eyes", "eyeChooser: " + eyeChooser);
        if(eyeChooser==5) {
            eyeChooser = 0;
        }
        else eyeChooser++;

        switch(eyeChooser) {
            case 0:
                openEyeResource = R.drawable.ava_eyes_open;
                closedEyeResource = R.drawable.ava_eyes_closed;
                break;
            case 1:
                openEyeResource = R.drawable.ed_eyes_open;
                closedEyeResource = R.drawable.ed_eyes_closed;
                break;
            case 2:
                openEyeResource = R.drawable.blueeyesopen;
                closedEyeResource = R.drawable.blueeyesclosed;
                break;
            case 3:
                openEyeResource = R.drawable.center_blue;
                closedEyeResource = R.drawable.closed_blue;
                break;
            case 4:
                openEyeResource = R.drawable.center_grey;
                closedEyeResource = R.drawable.closed_grey;
                break;
            case 5:
                openEyeResource = R.drawable.center_white;
                closedEyeResource = R.drawable.closed_white;
                break;
            default:
                openEyeResource = R.drawable.ava_eyes_open;
                closedEyeResource = R.drawable.ava_eyes_closed;
                break;
        }
        eyesView.setImageResource(openEyeResource);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("eyes", "touch");
        changeEyes();
        return false;
    }
}
