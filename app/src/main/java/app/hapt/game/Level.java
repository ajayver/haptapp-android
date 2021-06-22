package app.hapt.game;

import android.app.Activity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import app.hapt.utils.App;
import app.hapt.utils.User;

public class Level {
    public String name;
    public String title;
    public String teaser;
    public String desc;
    public String imagePath;
    public String correctAnswer;
    public int requiredStreak;
    public String failText;
    public int tapDuration;
    public int tapDelay;
    public Boolean shuffle = false;
    public HashMap<String, String> patterns;
    public JSONObject levelData;
    public int maxButtons = 4;

    public Level(Activity activity, String levelName) {

        this.name = levelName;
        User.setStreak(0);

        Log.i("haptapplog", "Loading level levels/" + name + ".json");

        levelData = load_json(activity, "levels/" + name + ".json" );

        try {
            teaser = levelData.getString("teaser");
            title = levelData.getString("title");
            desc = levelData.getString("desc");
            requiredStreak = levelData.getInt("requiredStreak");
            failText = levelData.getString("failText");
            tapDuration = levelData.getInt("tapDuration");
            tapDelay = levelData.getInt("tapDelay");
            if (levelData.has("image")) {
                imagePath = levelData.getString("image");
            }

            if (levelData.has("shuffle")) {
                shuffle = levelData.getBoolean("shuffle");
            }


            get_patterns();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void set_correct_answer() {
        try {
            correctAnswer = levelData.getString("correctAnswer");
            if (correctAnswer.equals("random")) {
                correctAnswer = get_random_pattern();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String get_random_pattern() {
        if (shuffle) {
            get_patterns();
        }
        Random random = new Random();
        Log.i("haptapplog", "get_random_pattern: " + patterns.keySet().toString());
        List<String> keys      = new ArrayList<String>(patterns.keySet());
        return keys.get( random.nextInt(keys.size()) );
    }

    public void get_patterns() {

        JSONObject patternsJSON = null;
        try {

            patternsJSON = levelData.getJSONObject("patterns");

            if (patternsJSON.length() < maxButtons) {
                maxButtons = patternsJSON.length();
            }

            patterns = new HashMap<String, String>();

            int counter = 0;

            JSONArray temp = patternsJSON.names();

            Random random = new Random();

            Boolean [] previousIndexes = new Boolean[patternsJSON.length()];

            for (int i = 0; i < maxButtons; i++) {
                int index = i;
                if (shuffle != null && shuffle) {
                    do {
                        index = random.nextInt(patternsJSON.length());
                    } while (previousIndexes[index] != null);

                    previousIndexes[index] = true;
                }
                Log.i("haptapplog", "random index: " + index);
                assert temp != null;
                String key = temp.get(index).toString();
                Log.i("haptapplog", "random key: " + key);
                String value = patternsJSON.getString(key);
                patterns.put(key, value);

            }

            Log.i("haptapplog", "get_patterns: " + patterns.keySet().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public JSONObject load_json(Activity activity, String filename) {
        String json = null;
        try {
            InputStream is = activity.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            return new JSONObject(json);
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
            return null;
        }

    }


}
