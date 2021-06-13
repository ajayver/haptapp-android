package app.hapt.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import androidx.appcompat.app.AppCompatActivity;

import app.hapt.R;
import app.hapt.utils.User;
import app.hapt.game.Game;
import app.hapt.interfaces.Buzz;
import app.hapt.interfaces.Phone;

public class MainActivity extends AppCompatActivity {

    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new User(this);
        game = new Game(this);
        game.listLevels();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        game.listLevels();
    }

    public void settings_button_click(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

}
