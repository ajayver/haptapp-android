package app.hapt.interfaces;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.neosensory.neosensoryblessed.NeosensoryBlessed;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;

import app.hapt.utils.App;
import app.hapt.utils.Broadcast;
import app.hapt.activities.SettingsActivity;
import app.hapt.utils.User;

public class Buzz {
    private static Buzz instance = null;
    private static final int ACCESS_LOCATION_REQUEST = 2;
    private static final int NUM_MOTORS = 4;

    // Access the library to leverage the Neosensory API
    public NeosensoryBlessed blessedNeo = null;

    // Variable to track whether or not the wristband should be vibrating
    private static boolean vibrating = false;
    private static boolean disconnectRequested = false; // used for requesting a disconnect within our thread

    Runnable vibratingPattern;
    Thread vibratingPatternThread;
    User user;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public boolean buzzConnected = false;
    int spikeWidth;
    int spikeDelay;
    private SettingsActivity activity;


    public Buzz() {
        EventBus.getDefault().register(this);
    }

    public static Buzz getInstance() {
        if (instance == null)
            instance = new Buzz();
        return instance;
    }

    @Subscribe
    public void onBroadcast(Broadcast message) {
        Log.i("haptapplog","Buzz received a message from the EventBus: " + message.pattern);
        if (blessedNeo != null && buzzConnected) {
            vibrate(message.pattern, message.duration, message.delay);
        }

    }

    public void vibrate(String pattern, int duration, int delay) {

        spikeWidth = duration;
        spikeDelay = delay;

        vibratingPattern = new VibratingPattern(pattern);
        vibratingPatternThread = new Thread(vibratingPattern);
        vibratingPatternThread.start();

    }

    // Create a Runnable (thread) to send a repeating vibrating pattern. Should terminate if
    // the variable `vibrating` is False
    class VibratingPattern implements Runnable {

        private final String pattern;

        public VibratingPattern(String pattern) {
            this.pattern = pattern;
        }

        public void run() {
            // loop until the thread is interrupted
            int motorID = 0;

            try {
                int currentVibration = 255;
                binaryToMotors(pattern, currentVibration);

//                Log.i("haptapplog", "Going to vibrate these commands: " + Arrays.toString(motorCommands));
//                blessedNeo.stopMotors();
//                blessedNeo.vibrateMotors(motorCommands);
//                blessedNeo.stopMotors();
                // Letter A is 1101 in binary, "bz-bz...bz"
//                int[] letterA = new int[] {
//                        255,255,255,255,
//                        255,255,255,255,
//                        255,255,255,255,
//                        255,255,255,255,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        255,255,255,255,
//                        255,255,255,255,
//                        255,255,255,255,
//                        255,255,255,255,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        255,255,255,255,
//                        255,255,255,255,
//                        255,255,255,255,
//                        255,255,255,255,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//                        0,0,0,0,
//
//                };
//
//                Log.i(TAG, "Playing A: " + Arrays.toString(letterA));
//                blessedNeo.vibrateMotors(letterA);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                blessedNeo.stopMotors();
                blessedNeo.resumeDeviceAlgorithm();
                Log.i("haptapplog", "Interrupted thread");
                e.printStackTrace();
            }

            if (disconnectRequested) {
                Log.i("haptapplog", "Disconnect requested while thread active");
                blessedNeo.stopMotors();
                blessedNeo.resumeDeviceAlgorithm();
                // When disconnecting: it is possible for the device to process the disconnection request
                // prior to processing the request to resume the onboard algorithm, which causes the last
                // sent motor command to "stick"
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                blessedNeo.disconnectNeoDevice();
                disconnectRequested = false;
            }
        }

        private void binaryToMotors(String pattern, int amp) {
            Log.i("haptapplog", "binaryToMotors needs to convert the following array to motor signals: " + pattern);

                int[] commands = new int[] {};
                int batch = 0;

                for (int i = 0; i < pattern.length(); i++) {
                    Log.i("haptapplog", "Char: " + pattern.charAt(i));
                    if (pattern.charAt(i) == '1') {
                        batch++;
                        Log.i("haptapplog", "Batch: " + batch);
                        if (batch > 2) {
                            sendBatch(batch, amp);
                            batch = 0;
                        }
                    } else {
                        sendBatch(batch, amp);
                        batch = 0;
                        try {
                            Thread.sleep(spikeWidth + spikeDelay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                if (batch > 0) {
                    sendBatch(batch, amp);
                }

        }

        private void sendBatch(int batch, int amp) {
            Log.i("haptapplog", "Sending a batch to Buzz: " + batch);
            int[] commands = new int[] {};
            for (int i = 0; i < batch; i++) {
                commands = concatenate(commands, getSignal(amp));
            }
            Log.i("haptapplog", "Sending a batch to Buzz: " + Arrays.toString(commands));
            blessedNeo.vibrateMotors(commands);
            try {
                Thread.sleep(commands.length/4*16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private int[] getSignal( int amp ) {

            int[] signal = {};

            signal = concatenate(signal, getSpike(amp));

            signal = concatenate(signal, getDelay());

            return signal;
        }


        private int[] getSpike(int amp) {
            int[] signal = {};
            int frameWidth = spikeWidth / 16;
            for (int i = 0; i < frameWidth; i++) {
                signal = concatenate(signal, new int[] {amp,amp,amp,amp});
            }
            return signal;
        }

        private int[] getDelay() {
            int[] signal = {};
            int frameWidth = spikeDelay / 16;
            for (int i = 0; i < frameWidth; i++) {
                signal = concatenate(signal, new int[] {0,0,0,0});
            }
            return signal;
        }

        private int[] concatenate(int[] array1, int[] array2) {
            int[] array1and2 = new int[array1.length + array2.length];
            System.arraycopy(array1, 0, array1and2, 0, array1.length);
            System.arraycopy(array2, 0, array1and2, array1.length, array2.length);
            return array1and2;
        }
    }

    public void connect(SettingsActivity activity) {
        this.activity = activity;
        Log.i("haptapplog", "Connect Buzz!");
        if (checkLocationPermissions(activity)) {
            initBluetoothHandler(activity);
        } // Else, this function will have the system request permissions and handle displaying the
        // button in the callback onRequestPermissionsResult

    }

    public void disconnect(SettingsActivity activity) {
        Log.i("haptapplog", "Disconnect Buzz!");
        Log.i("haptapplog", BlessedReceiver.toString());
        if (blessedNeo != null) {

            blessedNeo.stopMotors();
            blessedNeo.resumeDeviceAlgorithm();
            blessedNeo.disconnectNeoDevice();
            vibratingPatternThread = null;
        }

    }

//    public void updateSetting() {
//        editor = sharedPreferences.edit();
//        if (blessedNeo != null) {
//            editor.putBoolean("app.hapt.buzz_connected", false);
//            editor.apply();
//        } else {
//            editor.putBoolean("app.hapt.buzz_connected", true);
//        }
//        editor.apply();
//    }

    private boolean checkLocationPermissions(Activity activity) {
        int targetSdkVersion = activity.getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && targetSdkVersion >= Build.VERSION_CODES.Q) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST);
                return false;
            } else {
                return true;
            }
        } else {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(
                        new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_LOCATION_REQUEST);
                return false;
            } else {
                return true;
            }
        }
    }

    public void initBluetoothHandler(SettingsActivity activity) {
        // Create an instance of the Bluetooth handler. This uses the constructor that will search for
        // and connect to the first available device with "Buzz" in its name. To connect to a specific
        // device with a specific address, you can use the following pattern:  blessedNeo =
        // NeosensoryBlessed.getInstance(getApplicationContext(), <address> e.g."EB:CA:85:38:19:1D",
        // false);
        NeosensoryBlessed.requestBluetoothOn(activity);
        blessedNeo = NeosensoryBlessed.getInstance(activity, new String[] {"Buzz"}, false);
        // register receivers so that NeosensoryBlessed can pass relevant messages and state changes to MainActivity
        activity.registerReceiver(BlessedReceiver, new IntentFilter("BlessedBroadcast"));

    }

    // A Broadcast Receiver is responsible for conveying important messages/information from our
    // NeosensoryBlessed instance. There are 3 types of messages we can receive:
    //
    // 1. "com.neosensory.neosensoryblessed.CliReadiness": conveys a change in state for whether or
    // not a connected Buzz is ready to accept commands over its command line interface. Note: If the
    // CLI is ready, then it is currently a prerequisite that a compliant device is connected.
    //
    // 2. "com.neosensory.neosensoryblessed.ConnectedState": conveys a change in state for whether or
    // not we're connected to a device. True == connected, False == not connected. In this example, we
    // don't actually need this, because we can use the CLI's readiness by proxy.
    //
    // 3. "com.neosensory.neosensoryblessed.CliMessage": conveys a message sent to Android from a
    // connected Neosensory device's command line interface
    private final BroadcastReceiver BlessedReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.hasExtra("com.neosensory.neosensoryblessed.CliReadiness")) {
                        // Check the message from NeosensoryBlessed to see if a Neosensory Command Line
                        // Interface
                        // has become ready to accept commands
                        // Prior to calling other API commands we need to accept the Neosensory API ToS
                        if (intent.getBooleanExtra("com.neosensory.neosensoryblessed.CliReadiness", false)) {
                            // request developer level access to the connected Neosensory device
                            blessedNeo.sendDeveloperAPIAuth();
                            // sendDeveloperAPIAuth() will then transmit a message back requiring an explicit
                            // acceptance of Neosensory's Terms of Service located at
                            // https://neosensory.com/legal/dev-terms-service/
                            blessedNeo.acceptApiTerms();
                            Log.i("haptapplog", String.format("state message: %s", blessedNeo.getNeoCliResponse()));
                            // Assuming successful authorization, set up a button to run the vibrating pattern
                            // thread above
                            buzzConnected = true;
                            buzzConnectedChange();
                        } else {
                            buzzConnected = false;
                            buzzConnectedChange();
                        }
                    }

                    if (intent.hasExtra("com.neosensory.neosensoryblessed.CliMessage")) {
                        String notification_value =
                                intent.getStringExtra("com.neosensory.neosensoryblessed.CliMessage");
//                        neoCliOutput.setText(notification_value);
                    }

                    if (intent.hasExtra("com.neosensory.neosensoryblessed.ConnectedState")) {
                        if (intent.getBooleanExtra("com.neosensory.neosensoryblessed.ConnectedState", false)) {
                            Log.i("haptapplog", "Connected to Buzz");
                            buzzConnected = true;
                        } else {
                            Log.i("haptapplog", "Disconnected from Buzz");
                            buzzConnected = false;
                        }


                    }

                }
            };

    private void buzzConnectedChange() {
        this.activity.refreshUI();
        if (buzzConnected) {
            blessedNeo.pauseDeviceAlgorithm();
            blessedNeo.stopAudio();
            vibrate("1010101000101101", 16, 16);
        }
    }


}
