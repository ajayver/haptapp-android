package app.hapt.game;

import android.app.Activity;

import androidx.recyclerview.widget.RecyclerView;

import app.hapt.R;
import app.hapt.adapters.ItemAdapter;

public class Game {

    private final Activity currentActivity;

    private final Story story;

    public Game(Activity activity) {
        currentActivity = activity;
        story = new Story(currentActivity);
    }

    public void listLevels() {
        RecyclerView recyclerView = currentActivity.findViewById(R.id.recycler_view);
        ItemAdapter itemAdapter = new ItemAdapter(currentActivity, story.levels);
        recyclerView.setAdapter(itemAdapter);
    }

}
