package com.example.devon.securobotmaster;

import com.example.devon.securobotmaster.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;


public class SecuroBotMasterMain extends IOIOActivity {
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

    private static final String TAG = "Bluetooth";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    //**********************************************
    ImageView leftEye;
    ImageView rightEye;
    int lEResource;
    int rEResource;
    private Handler mHandler;
    Random r = new Random();
    String currentAction = "";
    boolean actionEnable = true;

    public static final int ACTION_TWEET = 0;
    public static final int ACTION_RSS = 1;
    public static final int ACTION_JOKE = 2;
    public static final int ACTION_QUIZ = 3;
    public static final int ACTION_PAGE = 4;
    public static final int ACTION_TIP = 5;
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
                                    //Set up Bluetooth stuff
//**************************************************************************************************
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mChatService = new BluetoothChatService(this, BTHandler);

            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer("");
        }

        //at this point you could make the device discoverable, but lets just assume
        //we are using already paired devices for security reasons.
        //this is a cyber security robot anyway ;)
        //ensureDiscoverable();

        //then prompt the user to select a device tot connect to (secure method)
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

        /* insecure method
        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
         */



//**************************************************************************************************
                                //SecuroBot setup stuff
//**************************************************************************************************
        //left
        leftEye = (ImageView) findViewById(R.id.leftEye);
        lEResource = R.drawable.blueeyesopenleft;
        leftEye.setImageResource(lEResource);
        //right
        rightEye = (ImageView) findViewById(R.id.rightEye);
        rEResource = R.drawable.blueeyesopenright;
        rightEye.setImageResource(rEResource);

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
                                    //Bluetooth stuff (non IOIO related BT)
//**************************************************************************************************
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "No devices connected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler BTHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d("Bluetooth", "Wrote message: " + writeMessage);
                    Toast.makeText(SecuroBotMasterMain.this, "Wrote message: " + writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d("Bluetooth", "Read message: " + readMessage);
                    //Toast.makeText(SecuroBotMasterMain.this, "Read message: " + readMessage, Toast.LENGTH_SHORT).show();
                    processMessage(readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        Toast.makeText(SecuroBotMasterMain.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                        Toast.makeText(SecuroBotMasterMain.this, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    // Initialize the BluetoothChatService to perform bluetooth connections
                    mChatService = new BluetoothChatService(this, BTHandler);

                    // Initialize the buffer for outgoing messages
                    mOutStringBuffer = new StringBuffer("");
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Bluetooth was not enebled. Leaving chat",
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    private void processMessage(String message) {
        if(!actionEnable) {
            switch(message) {
                case "CC":
                    Log.d(TAG, currentAction + " Action Completed.");
                    Toast.makeText(this, currentAction + " Action Completed.",
                            Toast.LENGTH_SHORT).show();
                    actionEnable = true;
                    mHandler.removeCallbacks(interactionTimer);
                    mHandler.removeCallbacks(timerInterrupt);
                    break;
                case "RS":
                    mHandler.removeCallbacks(interactionTimer);
                    mHandler.removeCallbacks(timerInterrupt);
                    interactionTimer.run();
                    Log.d("Timer", "Touch sensed. Timer was reset.");
                    break;
                default:
                    Log.d("Bluetooth", "Unknown Response: " + message);
                    break;
            }
        }
    }

//**************************************************************************************************
                                        //Threads
//**************************************************************************************************
    void startRepeatingTask() {
        runnable.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int il = r.nextInt(0+100);
            int ir = r.nextInt(0+100);

            if(il>50) {
                if(lEResource == R.drawable.blueeyesclosedleft) {
                    lEResource = R.drawable.blueeyesopenleft;
                }
                else {
                    lEResource = R.drawable.blueeyesclosedleft;
                }
                leftEye.setImageResource(lEResource);
            }

            if(ir>50) {
                if(rEResource == R.drawable.blueeyesclosedright) {
                    rEResource = R.drawable.blueeyesopenright;
                }
                else rEResource = R.drawable.blueeyesclosedright;
                rightEye.setImageResource(rEResource);
            }
            mHandler.postDelayed(runnable, 5000);
        }
    };

    Runnable interactionTimer = new Runnable(){
        @Override
        public void run() {
            Log.d("Timer", "Called timer");
            actionEnable = false;
            Log.d("Timer", "Delay Started...");
            mHandler.postDelayed(timerInterrupt, 40000);    //set this timer for a little bit longer in case of delay
        }
    };

    Runnable timerInterrupt = new Runnable() {
        @Override
        public void run() {
            actionEnable = true;
            mHandler.removeCallbacks(interactionTimer);
            Log.d("Timer", "Delay Stopped.");
        }
    };

//**************************************************************************************************
                                //Android IOIO stuff
//**************************************************************************************************
    /**
     * This is the thread on which all the IOIO activity happens. It will be run
     * every time the application is resumed and aborted when it is paused. The
     * method setup() will be called right after a connection with the IOIO has
     * been established (which might happen several times!). Then, loop() will
     * be called repetitively until the IOIO gets disconnected.
     */
    class Looper extends BaseIOIOLooper {
        /** The on-board LED. */
        private DigitalOutput led_;
        private IRSensor iRSensors = new IRSensor(33);
        private PwmOutput pwm;
        int newPos, currentPos;

        /**
         * Called every time a connection with IOIO has been established.
         * Typically used to open pins.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         *
         * @see ioio.lib.util.IOIOLooper
         */
        @Override
        protected void setup() throws ConnectionLostException, InterruptedException {
            showVersions(ioio_, "IOIO connected!");
            led_ = ioio_.openDigitalOutput(0, true);
            iRSensors.input = ioio_.openAnalogInput(iRSensors.pin);
            initIR();

            try {
                pwm= ioio_.openPwmOutput(35, 100);  //new DigitalOutput.Spec(35, DigitalOutput.Spec.Mode.OPEN_DRAIN)
            } catch (ConnectionLostException e) {
                Log.d("Connection Lost", "IO Connection Lost");
            }
        }

        /**
         * Called repetitively while the IOIO is connected.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         * @throws InterruptedException
         * 				When the IOIO thread has been interrupted.
         *
         * @see ioio.lib.util.IOIOLooper#loop()
         */
        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            int re = r.nextInt(100-0); //random number between 0 and 100 for rotation enable
            int ra = r.nextInt(3-0); //random number between 0 and 100 for rotation angle

            if(actionEnable){
                if(re <= 1) {  //% chance that the head will rotate
                    switch(ra){
                        case 0: newPos = 1000; break;    //limit 600
                        case 1: newPos = 1550; break;
                        case 2: newPos = 2000; break;   //limit 2450
                        default: break;
                    }

                    if(newPos != currentPos)
                    {
                        led_.write(true);
                        pwm.setPulseWidth(newPos);
                        currentPos = newPos;
                        Log.d("ROTATE", "Moving to position: " + newPos + "...");
                        Thread.sleep(1000);
                        Log.d("ROTATE", "At position: " + newPos);
                        initIR();
                    }
                }
                else{
                    float measVal = iRSensors.input.read();
                    float measVolt = iRSensors.input.getVoltage();
                    if(iRSensors.motionDetect(measVal, measVolt)) {
                        led_.write(false);
                        Log.d("MOTION", "Detected motion!"
                                        + " BaseVal: " + iRSensors.baseValue + "/" + measVal +
                                        ", BaseVolt: " + iRSensors.baseVolt + "/" + measVolt
                        );

                        int rc = r.nextInt(6-0);
                        switch(rc){
                            case ACTION_PAGE:
                                currentAction = "Webpage";
                                sendMessage(currentAction);
                                interactionTimer.run();
                                break;
                            case ACTION_QUIZ:
                                currentAction = "Quiz";
                                sendMessage(currentAction);
                                interactionTimer.run();
                                break;
                            case ACTION_JOKE:
                                currentAction = "Joke";
                                sendMessage(currentAction);
                                interactionTimer.run();
                                break;
                            case ACTION_TIP:
                                currentAction = "Tip";
                                sendMessage(currentAction);
                                interactionTimer.run();
                                break;
                            case ACTION_RSS:
                                currentAction = "RSS";
                                sendMessage(currentAction);
                                interactionTimer.run();
                                break;
                            case ACTION_TWEET:
                                currentAction = "Tweet";
                                sendMessage(currentAction);
                                interactionTimer.run();
                                break;
                            default: break;
                        }

                        Log.d("IR SENSORS", "reinitializing...");
                        initIR();
                    }
                    else led_.write(true);
                }
            } else initIR();
            Thread.sleep(100);
        }

        public void initIR() throws ConnectionLostException, InterruptedException {
            float baseVal=0f, baseVolt=0f;

            for(int i=0; i<iRSensors.iSamples; i++) {
                baseVal += iRSensors.input.read();
                baseVolt += iRSensors.input.getVoltage();
            }
            iRSensors.initialize(baseVal / iRSensors.iSamples, baseVolt / iRSensors.iSamples);
/*
            Log.d("INIT IR", "Base Val: " + baseVal/iRSensors.iSamples +
                    ", base Volt: " + baseVolt/iRSensors.iSamples);*/
        }

        /**
         * Called when the IOIO is disconnected.
         *
         * @see ioio.lib.util.IOIOLooper#disconnected()
         */
        @Override
        public void disconnected() {
            toast("IOIO disconnected");
        }

        /**
         * Called when the IOIO is connected, but has an incompatible firmware version.
         *
         * @see ioio.lib.util.IOIOLooper#incompatible(IOIO)
         */
        @Override
        public void incompatible() {
            showVersions(ioio_, "Incompatible firmware version!");
        }
    }

    /**
     * A method to create our IOIO thread.
     *
     * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
     */
    @Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }

    private void showVersions(IOIO ioio, String title) {
        toast(String.format("%s\n" +
                        "IOIOLib: %s\n" +
                        "Application firmware: %s\n" +
                        "Bootloader firmware: %s\n" +
                        "Hardware: %s",
                title,
                ioio.getImplVersion(IOIO.VersionType.IOIOLIB_VER),
                ioio.getImplVersion(IOIO.VersionType.APP_FIRMWARE_VER),
                ioio.getImplVersion(IOIO.VersionType.BOOTLOADER_VER),
                ioio.getImplVersion(IOIO.VersionType.HARDWARE_VER)));
    }

    private void toast(final String message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
