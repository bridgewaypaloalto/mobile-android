package com.bcsv.mobile;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "SettingsActivity";
    ImageButton myDefaultSettingButton;
    ImageButton myCloseSettingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        myDefaultSettingButton = findViewById(R.id.buttonDefaultSave);
        myDefaultSettingButton.setOnClickListener(this);

        myCloseSettingButton = findViewById(R.id.buttonSettingsClose);
        myCloseSettingButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.buttonDefaultSave:
                setDefaultSettings();
                Toast.makeText(this, "Default Setting is saved", Toast.LENGTH_SHORT).show();
                this.finish();
                break;
            case R.id.buttonSettingsClose:
                this.finish();
                break;
        }
    }

    private void setDefaultSettings() {

        //Check if there's previously stored endpoint entries.
        //If it is empty, store the default value to it
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("font_color", "#000000");
        editor.putString("background_color", "#FFFFFF");
        editor.putInt("font_size_seek_bar_key",this.getResources().getInteger(R.integer.default_font_size));

        editor.apply();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private String myFCMToken = "";

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getFCMToken();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());

            EditTextPreference myFCMPreference = findPreference("fcm_token");
            if (myFCMPreference != null) {
                myFCMPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setText(myFCMToken);
                            }
                        });
            }

            final Preference myFontColorPref = findPreference("font_color");
            myFontColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    int font_color = Color.parseColor(sp.getString("font_color", "#000000"));

                    final ColorPicker cp = new ColorPicker(getActivity(),
                            Color.red(font_color),
                            Color.green(font_color),
                            Color.blue(font_color));

                    /* Show color picker dialog */
                    cp.show();
                    cp.enableAutoClose(); // Enable auto-dismiss for the dialog

                    /* Set a new Listener called when user click "select" */
                    cp.setCallback(new ColorPickerCallback() {
                        @Override
                        public void onColorChosen(@ColorInt int color) {
                            // Do whatever you want
                            // Examples
                            Log.d("Alpha", Integer.toString(Color.alpha(color)));
                            Log.d("Red", Integer.toString(Color.red(color)));
                            Log.d("Green", Integer.toString(Color.green(color)));
                            Log.d("Blue", Integer.toString(Color.blue(color)));

                            Log.d("Pure Hex", Integer.toHexString(color));
                            Log.d("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
                            Log.d("#Hex with alpha", String.format("#%08X", (0xFFFFFFFF & color)));

                            // If the auto-dismiss option is not enable (disabled as default) you have to manually dimiss the dialog
                            // cp.dismiss();

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("font_color", String.format("#%06X", (0xFFFFFF & color)));
                            editor.apply();
                        }
                    });
                    return true;
                }
            });

            final Preference myBackgroundColorPref = findPreference("background_color");
            myBackgroundColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    int background_color = Color.parseColor(sp.getString("background_color", "#000000"));

                    final ColorPicker cp = new ColorPicker(getActivity(),
                            Color.red(background_color),
                            Color.green(background_color),
                            Color.blue(background_color));

                    /* Show color picker dialog */
                    cp.show();
                    cp.enableAutoClose(); // Enable auto-dismiss for the dialog

                    /* Set a new Listener called when user click "select" */
                    cp.setCallback(new ColorPickerCallback() {
                        @Override
                        public void onColorChosen(@ColorInt int color) {
                            // Do whatever you want
                            // Examples
                            Log.d("Alpha", Integer.toString(Color.alpha(color)));
                            Log.d("Red", Integer.toString(Color.red(color)));
                            Log.d("Green", Integer.toString(Color.green(color)));
                            Log.d("Blue", Integer.toString(Color.blue(color)));

                            Log.d("Pure Hex", Integer.toHexString(color));
                            Log.d("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
                            Log.d("#Hex with alpha", String.format("#%08X", (0xFFFFFFFF & color)));

                            // If the auto-dismiss option is not enable (disabled as default) you have to manually dimiss the dialog
                            // cp.dismiss();

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("background_color", String.format("#%06X", (0xFFFFFF & color)));
                            editor.apply();
                        }
                    });
                    return true;
                }
            });
        }

        private String getFCMToken(){

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            myFCMToken = task.getResult().getToken();

                            // Log and toast
                            //String msg = getString(R.string.msg_token_fmt, token);
                            Log.d(TAG, myFCMToken);
                            //Toast.makeText(getActivity(), myFCMToken, Toast.LENGTH_SHORT).show();
                        }
                    });

            return myFCMToken;
        }
    }
}