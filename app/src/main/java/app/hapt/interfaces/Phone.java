package app.hapt.interfaces;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import app.hapt.R;
import app.hapt.utils.Broadcast;
import app.hapt.utils.User;

public class Phone {
    private static Phone instance = null;
    Vibrator v = null;
    Activity activity;

    static SoundPool soundPool;
    static int tapSound;
    static int coinSound;
    static int nextLevelSound;
    static int failSound;

    public Phone(Activity activity) {
        this.activity = activity;

        v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

        EventBus.getDefault().register(this);

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(10)
                .build();
        tapSound = soundPool.load(activity, R.raw.tap, 1);
        coinSound = soundPool.load(activity, R.raw.coin, 1);
        nextLevelSound = soundPool.load(activity, R.raw.next_level, 1);
        failSound = soundPool.load(activity, R.raw.fail, 1);


    }

    public static Phone getInstance(Activity activity) {
        if (instance == null) {
            instance = new Phone(activity);
        }
        return instance;
    }

    @Subscribe
    public void onBroadcast(Broadcast message) {
        Log.i("haptapplog","Phone received a message from the EventBus: " + message.pattern + " sound is " + message.sound);
        play(message.pattern, message.duration, message.delay, message.sound);

    }

    public void spike(int duration, boolean sound) {
        if (User.getTapSoundsSetting() && sound) {
            playTap();
        }

        if (User.getPhoneVibrationSetting()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(duration);
            }
        }

        delay(duration);
    }

    public void delay(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void play(String pattern, int duration, int delay, boolean sound) {

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){

                for (int i = 0; i < pattern.length(); i++) {
                    char bit = pattern.charAt(i);
                    if (bit == '1') {
                        spike(duration, sound);
                    } else {
                        delay(duration);
                    }
                    delay(delay);
                }
            }
        });
        thread.start();
    }

    public static void playSound(String name) {

        if (User.getSoundEffectsSetting()) {

            int sound;
            switch (name){
                case "next_level":
                    sound = nextLevelSound;
                    break;
                case "coin":
                    sound = coinSound;
                    break;
                case "fail":
                    sound = failSound;
                    break;
                default:
                    sound = 0;
            }

            if (sound != 0) {
                soundPool.play(sound,1,1,1,0,1);
            }
        }

    }

    public static void playTap() {
        soundPool.play(tapSound,1,1,1,0,1);
    }

}
