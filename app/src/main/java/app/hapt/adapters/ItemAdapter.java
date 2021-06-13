package app.hapt.adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

import app.hapt.game.GameScreen;
import app.hapt.game.Level;
import app.hapt.R;
import app.hapt.utils.User;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private HashMap<String, Level> levels;
    private int revealLevels;
    private String completedLevels;
    private Activity activity;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView title, description;
        private final ConstraintLayout mainLayout;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            mainLayout = (ConstraintLayout) view.findViewById(R.id.mainLayout);
            image = (ImageView) view.findViewById(R.id.cardImage);
            title = (TextView) view.findViewById(R.id.cardTitle);
            description = (TextView) view.findViewById(R.id.cardDescription);
        }
        public ImageView getImage() {
            return image;
        }
        public TextView getTitle() {
            return title;
        }
        public TextView getDescription() {
            return description;
        }
        public ConstraintLayout getMainLayout() {
            return mainLayout;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param currentActivity
     * @param levels HashMap<String, Level> containing the data to populate views to be used
     */
    public ItemAdapter(Activity currentActivity, HashMap<String, Level> levels) {
        activity = currentActivity;
        this.levels = levels;
        this.completedLevels = User.getCompletedLevels();
        this.revealLevels = User.getRevealLevels();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String levelName = "level_";
        levelName = levelName + String.valueOf(position);
        Log.i("haptapplog", levelName);
        Level level = levels.get(levelName);
        assert level != null;
        if (completedLevels.contains(levelName)) {
            viewHolder.getImage().setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            viewHolder.getImage().setImageResource(android.R.drawable.btn_star_big_off);
        }

        viewHolder.getTitle().setText(level.title);
        viewHolder.getDescription().setText(level.teaser);

        if (position > revealLevels) {
            viewHolder.getMainLayout().setAlpha((float) 0.5);
        } else {
            viewHolder.getMainLayout().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, GameScreen.class);
                    User.setCurrentLevel(level.name);
                    activity.startActivity(intent);
                }
            });
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return levels.size();
    }
}
