package app.hapt.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class User {

    private static SharedPreferences sharedPreferences;

    public User(Activity activity) {
        sharedPreferences = activity.getSharedPreferences("app.hapt", 0);
    }

    public static int getScore() {
        return Integer.parseInt(get("score", "0"));
    }

    public static void setScore(int score) {
        set("score", String.valueOf(score));
    }

    public static int getStreak() {
        Log.i("haptapplog",get("streak", "0"));
        return Integer.parseInt(get("streak", "0"));
    }

    public static void setStreak(int streak) {
        set("streak", String.valueOf(streak));
    }


    public static void addStreak(int i) {
        setStreak(getStreak() + i);
    }

    public static void addScore(int i) {
        setScore(getScore() + i);
    }

    public static String getCurrentLevel() {
        return get("currentLevel", "level_0");
    }

    public static void setCurrentLevel(String currentLevel) {
        set("currentLevel", currentLevel);
    }

    public static void nextLevel() {
        String[] parts = getCurrentLevel().split("_");
        set("currentLevel", "level_" + (Integer.parseInt(parts[1]) + 1));
        User.addRevealLevels(1);
    }

    public static String getCompletedLevels() {
        Log.i("haptapplog", "Completed Levels: " + get("completedLevels", ""));
        return get("completedLevels", "");
    }

    public static void setCompletedLevels(String completedLevels) {
        set("completedLevels", completedLevels);
    }

    public static void addCompletedLevel(String completedLevel) {
        String completedLevels = getCompletedLevels();
        if (!completedLevels.contains(completedLevel)) {
            completedLevels += completedLevel + ",";
        }
        setCompletedLevels(completedLevels);
    }

    public static void setTapSoundsSetting(boolean enabled) {
        set("tapSounds", Boolean.toString(enabled));
    }

    public static boolean getTapSoundsSetting() {
        return Boolean.parseBoolean(get("tapSounds", "true"));
    }

    public static void setPhoneVibrationSetting(boolean enabled) {
        set("phoneVibration", Boolean.toString(enabled));
    }

    public static boolean getPhoneVibrationSetting() {
        return Boolean.parseBoolean(get("phoneVibration", "true"));
    }

    public static void setSoundEffectsSetting(boolean enabled) {
        set("soundEffects", Boolean.toString(enabled));
    }

    public static boolean getSoundEffectsSetting() {
        return Boolean.parseBoolean(get("soundEffects", "true"));
    }

    public static int getRevealLevels() {
        return Integer.parseInt(get("revealLevels", "0"));
    }

    public static void setRevealLevels(int revealLevels) {
        set("revealLevels", String.valueOf(revealLevels));
    }

    public static void addRevealLevels(int i) {
        setRevealLevels(getRevealLevels() + i);
    }


    public static void set(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String get(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    public static void reset(Activity activity) {
        User.setCompletedLevels("");
        User.setStreak(0);
        activity.finish();
    }

}
