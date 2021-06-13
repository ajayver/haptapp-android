package app.hapt.game;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;
import java.util.Map;

import app.hapt.utils.Broadcast;
import app.hapt.R;
import app.hapt.activities.SettingsActivity;
import app.hapt.utils.User;
import app.hapt.interfaces.Phone;

public class GameScreen extends AppCompatActivity {

    TextView levelTitle;
    TextView levelDesc;

    ProgressBar levelProgress;

    LinearLayout buttonsLayout;
    Phone phone;

    private Level level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        levelTitle = (TextView) findViewById(R.id.lesson_title);
        levelDesc = (TextView) findViewById(R.id.lesson_text);
        levelProgress = (ProgressBar) findViewById(R.id.progress_bar);
        buttonsLayout = (LinearLayout) findViewById(R.id.buttons_layout);

        phone = Phone.getInstance(this);

        getData();
        displayLevelUI();
    }

    public void settings_button_click(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    private void getData() {
        level = new Level(this, User.getCurrentLevel());
    }

    public void displayLevelUI() {
        levelTitle.setText(level.title);
        levelDesc.setText(level.desc);
        levelProgress.setMax(level.requiredStreak);
        levelProgress.setProgress(0);
        generate_buttons();
        newRound();
    }

    public void generate_buttons() {
        clearButtons();
        addButton("play_pattern", getString(R.string.play_pattern_button_label));
        Log.i("haptapplog", level.patterns.toString());
        Iterator it = level.patterns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            addButton(pair.getKey().toString(), pair.getValue().toString());
        }
    }

    public void clearButtons() {
        buttonsLayout.removeAllViews();
    }


    public void addButton(String pattern, String label) {
        Button button = new Button(this);
        button.setTextSize(30);
        button.setAllCaps(false);
        button.setText(label);
        button.setTag(pattern);
        button.setTextColor(getColor(R.color.white));
        //button.setBackground(getDrawable(R.drawable.light_noise_texture));
        button.setBackgroundResource(R.drawable.rounded_button);
        Typeface face = ResourcesCompat.getFont(this, R.font.proxon);
        button.setTypeface(face);
        button.setPadding(5,5,5,5);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
        layoutParams.setMargins(0, 0, 0, 24);
        button.setLayoutParams(layoutParams);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked((Button) v);
            }
        });
        buttonsLayout.addView(button);
    }

    public void buttonClicked(Button button) {

        String tag = (String) button.getTag();

        Log.i("haptapplog", "button tag: " + tag);

        if (tag.equals("play_pattern")) {
            scheduleMessage(new Broadcast(level.correctAnswer, level.tapDuration, level.tapDelay, true));
        } else {
            if (level.correctAnswer.equals(tag)) {
                treat();
            } else {
                punish();
            }
        }

    }

    public void treat() {
        Log.i("haptapplog", "Treat!");
        Phone.playSound("coin");
        scheduleMessage(new Broadcast("1", 500,16, false));
        User.addStreak(1);
        User.addScore(1);
        refreshUI();

        if (User.getStreak() >= level.requiredStreak) {
           newLevel();
        } else {
            newRound();
            scheduleMessage(new Broadcast(level.correctAnswer, level.tapDuration, level.tapDelay, true), 1500);
        }

    }

    public void punish() {

        User.setStreak(0);

        refreshUI();
        Log.i("haptapplog", "Wrong!");
        Phone.playSound("fail");
        scheduleMessage(new Broadcast("1111111111", 16,16, false));

        scheduleMessage(new Broadcast(level.correctAnswer, level.tapDuration, level.tapDelay, true), 1500);

    }

    public void newRound() {
        Log.i("haptapplog", "New Round!");
        level.set_correct_answer();
        Log.i("haptapplog", "New round. Correct answer is " + level.correctAnswer);

    }

    public void newLevel() {
        Log.i("haptapplog", "New Level!");
        Phone.playSound("next_level");
        User.setStreak(0);
        User.addCompletedLevel(level.name);
        User.nextLevel();
        finish();
        startActivity(getIntent());
    }

    public void scheduleMessage(Broadcast message, int delay) {

        new CountDownTimer(delay, 1000) {
            public void onFinish() {
                EventBus.getDefault().post(message);
            }

            public void onTick(long millisUntilFinished) {

            }
        }.start();
    }

    public void scheduleMessage(Broadcast message) {
        scheduleMessage(message, 0);
    }

    public void refreshUI() {
        levelProgress.setProgress(User.getStreak(),true);
    }

}