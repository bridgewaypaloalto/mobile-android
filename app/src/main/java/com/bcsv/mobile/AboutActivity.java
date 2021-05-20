package com.bcsv.mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    TextView appVersion;
    ImageButton closeButton;
    TextView copyright_year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_screen);

        appVersion = this.findViewById(R.id.app_version);
        closeButton = this.findViewById(R.id.about_close_button);
        closeButton.setOnClickListener(this);

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        String version = versionName + "(" + versionCode + ")";
        appVersion.setText(version);

        copyright_year = findViewById(R.id.copyright_year);
        Calendar calendar = Calendar.getInstance();
        String copyright = copyright_year.getText() + Integer.toString(calendar.get(Calendar.YEAR));
        copyright_year.setText(copyright);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.about_close_button:
                this.finish();
//                Intent nextActivityIntent = new Intent(this, MainActivity.class);
//                startActivity(nextActivityIntent);
                break;
        }
    }
}
