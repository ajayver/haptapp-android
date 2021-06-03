package app.hapt;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.neosensory.neosensoryblessed.NeosensoryBlessed;

public class MainActivity extends AppCompatActivity {

    // set string for filtering output for this activity in Logcat
    private final String TAG = MainActivity.class.getSimpleName();

    // UI Components
//    private TextView neoCliOutput;
//    private TextView neoCliHeader;
    private Button neoConnectButton;
    private Button neoVibrateButton;

    private TextView textToPlay;

    // Constants
    private static final int ACCESS_LOCATION_REQUEST = 2;
    private static final int NUM_MOTORS = 4;

    // Access the library to leverage the Neosensory API
    private NeosensoryBlessed blessedNeo = null;

    // Variable to track whether or not the wristband should be vibrating
    private static boolean vibrating = false;
    private static boolean disconnectRequested =
            false; // used for requesting a disconnect within our thread
    Runnable vibratingPattern;
    Thread vibratingPatternThread;

    private Map<Character,String> patterns;

    private int spikeDuration;
    private int spikeDelay;
    private int letterDelay;
    private int wordDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get a lock on on the UI components (2 Textviews and 2 Buttons)
        setContentView(R.layout.activity_main);
//        neoCliOutput = (TextView) findViewById(R.id.cli_response);
//        neoCliHeader = (TextView) findViewById(R.id.cli_header);
        neoVibrateButton = (Button) findViewById(R.id.pattern_button);
        neoConnectButton = (Button) findViewById(R.id.connection_button);

        textToPlay = (TextView) findViewById(R.id.textToPlay);

        Log.i(TAG, "Activity tag: " + TAG);

        displayInitialUI();
        NeosensoryBlessed.requestBluetoothOn(this);

        if (checkLocationPermissions()) {
            displayInitConnectButton();
        } // Else, this function will have the system request permissions and handle displaying the
        // button in the callback onRequestPermissionsResult


//        spikeDuration = 64;
//        spikeDelay = 64;
        letterDelay = 100;
        wordDelay = 600;

        populatePatters();


    }

    protected String[] textToUnary(String text) {
        Log.i(TAG, "textToUnary needs to convert the following text: " + text);
        char[] charArray = text.toCharArray();
        String[] binaries = new String[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            char character = Character.toLowerCase(charArray[i]);
            if (patterns.containsKey(character)) {
                binaries[i] = patterns.get(character);
            }

        }

        Log.i(TAG, "textToUnary created an unary array: " + Arrays.toString(binaries));
        return binaries;
    }

    private int[][] unaryToMotors(String[] binaries, int amp) {
        Log.i(TAG, "unaryToMotors needs to convert the following array to motor signals: " + Arrays.toString(binaries));
        int[][] commands = new int[binaries.length][];

        for (int i = 0; i < binaries.length; i++) {
            String binary = binaries[i];

            char[] bits = binary.toCharArray();

            int[] letter = new int[] {};

            for (int j = 0; j < bits.length; j++) {
                letter = concatenate(letter, getSignal(bits[j]));
            }
            commands[i] = letter;
        }
        Log.i(TAG, "unaryToMotors created the following array of motor commands: " + Arrays.toString(commands));
        return commands;
    }

    private int[] getSignal( int num ) {

        int[] signal = {};

        for (int i = 1; i <= num; i++) {
            signal = concatenate(signal, getSpike());
            signal = concatenate(signal, getDelay());
        }

        return signal;
    }


    private int[] getSpike() {
        return new int[] {
                255,255,255,255,
                255,255,255,255,
                255,255,255,255,
                255,255,255,255,

        };
    }

    private int[] getDelay() {
        return new int[] {
                0,0,0,0,
                0,0,0,0,
        };
    }

    private void playWord(String word) {
        try {
            Log.i(TAG,"playword will sleep " + wordDelay + " ms.");
            Thread.sleep(wordDelay);
            Log.i(TAG,"playword done sleeping.");

            Log.i(TAG, "playWord got this word to play: " + word);
            String[] sequences = textToUnary(word);

            for (int i = 0; i < sequences.length; i++) {
                String sequence = sequences[i];
                if (sequence != null) {
                    Log.i(TAG, "playWord will play this letter: " + sequence);
                    for (int j = 0; j < sequence.length(); j++) {
                        int num = sequence.charAt(j) - '0';
                        Log.i(TAG,"tap " + num + " times");
                        int[] signal = getSignal(num);
                        int signalDuration = signal.length / 4 * 16;
                        blessedNeo.vibrateMotors(signal);
                        blessedNeo.stopMotors();
                        Thread.sleep(signalDuration);
                        Thread.sleep(6*16);
                    }

                    Thread.sleep(letterDelay);
                }


            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void vibrate() {
        vibrating = true;
        // run the vibrating pattern loop
        String text = textToPlay.getText().toString();
        vibratingPattern = new VibratingPattern(text);
        vibratingPatternThread = new Thread(vibratingPattern);
        vibratingPatternThread.start();
    }

    protected void populatePatters() {
        patterns = new HashMap<Character, String>();
        patterns.put('a', "21");
        patterns.put('b', "14");
        patterns.put('c', "122");
        patterns.put('d', "13");
        patterns.put('e', "2");
        patterns.put('f', "32");
        patterns.put('g', "112");
        patterns.put('h', "5");
        patterns.put('i', "3");
        patterns.put('j', "2111");
        patterns.put('k', "121");
        patterns.put('l', "23");
        patterns.put('m', "111");
        patterns.put('n', "12");
        patterns.put('o', "1111");
        patterns.put('p', "212");
        patterns.put('q', "1121");
        patterns.put('r', "22");
        patterns.put('s', "4");
        patterns.put('t', "11");
        patterns.put('u', "31");
        patterns.put('v', "41");
        patterns.put('w', "211");
        patterns.put('x', "131");
        patterns.put('y', "1211");
        patterns.put('z', "113");

    }

//    protected void populatePatters() {
//        patterns = new HashMap<Character, String>();
//        patterns.put('a', "1101");
//        patterns.put('b', "101111");
//        patterns.put('c', "1011011");
//        patterns.put('d', "10111");
//        patterns.put('e', "11");
//        patterns.put('f', "111011");
//        patterns.put('g', "101011");
//        patterns.put('h', "11111");
//        patterns.put('i', "111");
//        patterns.put('j', "11010101");
//        patterns.put('k', "101101");
//        patterns.put('l', "110111");
//        patterns.put('m', "10101");
//        patterns.put('n', "1011");
//        patterns.put('o', "1010101");
//        patterns.put('p', "10101101");
//        patterns.put('q', "1011011");
//        patterns.put('r', "11011");
//        patterns.put('s', "1111");
//        patterns.put('t', "101");
//        patterns.put('u', "11101");
//        patterns.put('v', "111101");
//        patterns.put('w', "110101");
//        patterns.put('x', "1011101");
//        patterns.put('y', "10110101");
//        patterns.put('z', "1010111");
//
//    }

    // Create a Runnable (thread) to send a repeating vibrating pattern. Should terminate if
    // the variable `vibrating` is False
    class VibratingPattern implements Runnable {
        private int vibrationAmp = NeosensoryBlessed.MAX_VIBRATION_AMP;
        private int delay = 10;
        private String text;

        public VibratingPattern(String inputText) {
            text = inputText;
        }

        public void run() {
            // loop until the thread is interrupted
            int motorID = 0;

            try {
                Log.i(TAG, "class VibratingPattern got the following text to play: " + text);
                int currentVibration = 255;

                String[] words = text.split(" ");

                Log.i(TAG, "class VibratingPattern converted the text to the following array of words: " + Arrays.toString(words));

                for (int i = 0; i < words.length; i++) {
                    playWord(words[i]);
                }


                //blessedNeo.vibrateMotors(new int[] {0,0,0,0});

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

                Thread.sleep(1000);


            } catch (InterruptedException e) {
                blessedNeo.stopMotors();
                blessedNeo.resumeDeviceAlgorithm();
                Log.i(TAG, "Interrupted thread");
                e.printStackTrace();
            }

            vibrating = false;
            blessedNeo.stopMotors();


        }
    }




    //////////////////////////
    // Cleanup on shutdown //
    /////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(BlessedReceiver);
        if (vibrating) {
            vibrating = false;
            disconnectRequested = true;
        }
        blessedNeo = null;
        vibratingPatternThread = null;
    }

    ////////////////////////////////////
    // SDK state change functionality //
    ////////////////////////////////////

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
                            Log.i(TAG, String.format("state message: %s", blessedNeo.getNeoCliResponse()));
                            // Assuming successful authorization, set up a button to run the vibrating pattern
                            // thread above
                            displayVibrateButton();
                            displayDisconnectUI();
                        } else {
                            displayReconnectUI();
                        }
                    }

                    if (intent.hasExtra("com.neosensory.neosensoryblessed.CliMessage")) {
                        String notification_value =
                                intent.getStringExtra("com.neosensory.neosensoryblessed.CliMessage");
                        //neoCliOutput.setText(notification_value);
                    }

                    if (intent.hasExtra("com.neosensory.neosensoryblessed.ConnectedState")) {
                        if (intent.getBooleanExtra("com.neosensory.neosensoryblessed.ConnectedState", false)) {
                            Log.i(TAG, "Connected to Buzz");
                        } else {
                            Log.i(TAG, "Disconnected from Buzz");
                        }
                    }
                }
            };

    ///////////////////////////////////
    // User interface functionality //
    //////////////////////////////////

    private void displayInitialUI() {
        displayReconnectUI();
        neoVibrateButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        blessedNeo.pauseDeviceAlgorithm();
                        if (!vibrating) {
                            vibrate();

                        }
                    }
                });
    }

    private void displayReconnectUI() {
//        neoCliOutput.setVisibility(View.INVISIBLE);
//        neoCliHeader.setVisibility(View.INVISIBLE);
        neoVibrateButton.setVisibility(View.INVISIBLE);
        neoVibrateButton.setClickable(false);
        neoVibrateButton.setText(
                "Start Vibration Pattern"); // Vibration stops on disconnect so reset the button text
        neoConnectButton.setText("Scan and Connect to Neosensory Buzz");
        neoConnectButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        blessedNeo.attemptNeoReconnect();
                        toastMessage("Attempting to reconnect. This may take a few seconds.");
                    }
                });
    }

    private void displayDisconnectUI() {
//        neoCliOutput.setVisibility(View.VISIBLE);
//        neoCliHeader.setVisibility(View.VISIBLE);
        neoConnectButton.setText("Disconnect");
        neoConnectButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (!vibrating) {
                            blessedNeo.disconnectNeoDevice();
                        } else {
                            // If motors are vibrating (in the VibratingPattern thread in this case) and we want
                            // to stop them on disconnect, we need to add a sleep/delay as it's possible for the
                            // disconnect to be processed prior to stopping the motors. See the VibratingPattern
                            // definition.
                            disconnectRequested = true;
                            vibrating = false;
                        }
                    }
                });
    }

    private void displayInitConnectButton() {
        // Display the connect button and create the Bluetooth Handler if so
        neoConnectButton.setClickable(true);
        neoConnectButton.setVisibility(View.VISIBLE);
        neoConnectButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        initBluetoothHandler();
                    }
                });
    }

    public void displayVibrateButton() {
        neoVibrateButton.setVisibility(View.VISIBLE);
        neoVibrateButton.setClickable(true);
    }

    private void toastMessage(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
    //////////////////////////////////////////////
    // Bluetooth and permissions initialization //
    //////////////////////////////////////////////

    private void initBluetoothHandler() {
        // Create an instance of the Bluetooth handler. This uses the constructor that will search for
        // and connect to the first available device with "Buzz" in its name. To connect to a specific
        // device with a specific address, you can use the following pattern:  blessedNeo =
        // NeosensoryBlessed.getInstance(getApplicationContext(), <address> e.g."EB:CA:85:38:19:1D",
        // false);
        blessedNeo =
                NeosensoryBlessed.getInstance(getApplicationContext(), new String[] {"Buzz"}, false);
        // register receivers so that NeosensoryBlessed can pass relevant messages and state changes to MainActivity
        registerReceiver(BlessedReceiver, new IntentFilter("BlessedBroadcast"));
    }

    private boolean checkLocationPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && targetSdkVersion >= Build.VERSION_CODES.Q) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST);
                return false;
            } else {
                return true;
            }
        } else {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_LOCATION_REQUEST);
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ((requestCode == ACCESS_LOCATION_REQUEST)
                && (grantResults.length > 0)
                && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            displayInitConnectButton();
        } else {
            toastMessage("Unable to obtain location permissions, which are required to use Bluetooth.");
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public int[] concatenate(int[] array1, int[] array2) {
        int[] array1and2 = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, array1and2, 0, array1.length);
        System.arraycopy(array2, 0, array1and2, array1.length, array2.length);
        return array1and2;
    }
}
