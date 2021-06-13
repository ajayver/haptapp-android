package app.hapt.game;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import app.hapt.game.Level;

public class Story {
    
    public HashMap<String, Level> levels;

    private String[] fileNames;
    
    public Story(Activity activity) {
        fileNames = loadLevelFiles(activity);
        levels = initiateLevels(activity);
    }
    
    private HashMap<String, Level>  initiateLevels(Activity activity) {
        HashMap<String, Level> levels = new HashMap<String, Level>();
        for (String fileName : fileNames) {
            String levelName = fileName.replace(".json","");
            Level level = new Level(activity, levelName);
            levels.put(levelName, level);
            Log.i("haptapplog",levelName);
        }
        return levels;
    }

    public String[] loadLevelFiles(Activity activity) {
        String[] fileNames = new String[] {};
        try {
            AssetManager assets = activity.getAssets();
            fileNames = assets.list("levels");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("haptapplog", Arrays.toString(fileNames));
        return fileNames;
    }

    public String[] getLevelNames() {
        return levels.keySet().toArray(new String[0]);
    }

}
