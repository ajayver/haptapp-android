package app.hapt.activities;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import app.hapt.R;
import app.hapt.utils.User;
import app.hapt.interfaces.Buzz;
import app.hapt.interfaces.Phone;


public class SettingsActivity extends AppCompatActivity {
    private static final int ACCESS_LOCATION_REQUEST = 2;

    private Phone phone;
    Button connectBuzzButton, resetProgress;
    Switch tapSoundSwitch, soundEffectsSwitch, phoneVibrationSwitch;

    Buzz buzz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        buzz = Buzz.getInstance();
        refreshUI();

        tapSoundSwitch = (Switch) findViewById(R.id.tap_sound_switch);
        tapSoundSwitch.setChecked(User.getTapSoundsSetting());

        phoneVibrationSwitch = (Switch) findViewById(R.id.phone_vibration_switch);
        phoneVibrationSwitch.setChecked(User.getPhoneVibrationSetting());

        soundEffectsSwitch = (Switch) findViewById(R.id.sound_effects_switch);
        soundEffectsSwitch.setChecked(User.getSoundEffectsSetting());

        resetProgress = (Button) findViewById(R.id.reset_progress_button);

        resetProgress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetProgressLongClick(v);
                return false;
            }
        });

    }

    public void refreshUI() {

        connectBuzzButton = findViewById(R.id.connect_buzz_button);

        if (buzz.blessedNeo != null && buzz.buzzConnected) {
            connectBuzzButton.setText("Disconnect");
        } else {
            connectBuzzButton.setText("Connect");
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ((requestCode == ACCESS_LOCATION_REQUEST)
                && (grantResults.length > 0)
                && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Buzz buzz = Buzz.getInstance();
            buzz.connect(SettingsActivity.this);
        } else {
            Toast.makeText(this, "Unable to obtain location permissions, which are required to use Bluetooth.", Toast.LENGTH_LONG).show();
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static class AreYouSureDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_fire_missiles)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            User.reset(getActivity());
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public void connectBuzzButtonClick(View view) {
        Log.i("haptapplog", "Connect Buzz button click");
        if (!buzz.buzzConnected) {
            connectBuzzButton.setText("Connecting...");
            buzz.connect(SettingsActivity.this);
        } else {
            connectBuzzButton.setText("Disconnecting...");
            buzz.disconnect(SettingsActivity.this);
        }
    }

    public void resetProgressClick(View view) {
        Log.i("haptapplog", "Reset progress button click");
        AreYouSureDialog dialog = new AreYouSureDialog();
        dialog.show(getSupportFragmentManager(),"Reset all progress");
    }
    public void resetProgressLongClick(View view) {
        Log.i("haptapplog", "Reset progress button long click");
        User.setRevealLevels(999);
    }


    public void tapSoundSwitchClick(View view) {
        Switch button = (Switch) findViewById(view.getId());
        User.setTapSoundsSetting(button.isChecked());
    }

    public void phoneVibrationSwitchClick(View view) {
        Switch button = (Switch) findViewById(view.getId());
        User.setPhoneVibrationSetting(button.isChecked());
    }

    public void soundEffectsSwitchClick(View view) {
        Switch button = (Switch) findViewById(view.getId());
        User.setSoundEffectsSetting(button.isChecked());

    }

}