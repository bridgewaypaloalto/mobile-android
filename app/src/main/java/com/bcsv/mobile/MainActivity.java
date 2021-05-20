package com.bcsv.mobile;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    Endpoint myEndpoint = new Endpoint(this);
    SharedPreferences mySharedPref;
    private static final String TAG = "MainActivity";
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    BottomNavigationView navView;
    LinearLayout myBackgroundLayout;

    // Remote Config keys
    private static final String MESSAGE_TITLE = "message_title";
    private static final String MESSAGE_CONTENT = "message_content";
    private static final String IS_THERE_A_NEW_MSG = "is_there_a_new_message";
    private static final String OVERWRITE_ENDPOINT = "overwrite_endpoints";

    private boolean loadFragment(Fragment myFragment){
        if(myFragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, myFragment)
                    .addToBackStack(null)
                    .commit();

            Objects.requireNonNull(getSupportActionBar()).hide();

            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        myBackgroundLayout = findViewById(R.id.backgroundLayout);
        final ImageView myLogoImageView;
        LinearLayout imgButtonHome;
        LinearLayout imgButtonBulletin;
        LinearLayout imgButtonSermon;
        LinearLayout imgButtonContact;
        LinearLayout imgButtonShare;
        LinearLayout imgButtonBible;
        Toolbar myToolbar;

        navView = findViewById(R.id.nav_view);
        //myFrameLayout = findViewById(R.id.fragment_container);
        myLogoImageView = findViewById(R.id.mainLogo);

        //The bottom action bar is defined in activity_main.xml (BottomNavigationView)
        //The top toolbar menu item is inflated in onCreateOptionsMenu
        myToolbar = findViewById(R.id.my_actionbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.gradation, null));

        }

        //Binding ImageButtons
        imgButtonHome = findViewById(R.id.imageButtonHome);
        imgButtonHome.setOnClickListener(this);

        imgButtonSermon = findViewById(R.id.imageButtonSermon);
        imgButtonSermon.setOnClickListener(this);

        imgButtonContact = findViewById(R.id.imageButtonOffering);
        imgButtonContact.setOnClickListener(this);

        imgButtonBulletin = findViewById(R.id.imageButtonBulletin);
        imgButtonBulletin.setOnClickListener(this);

        imgButtonShare = findViewById(R.id.imageButtonSundayBible);
        imgButtonShare.setOnClickListener(this);

        imgButtonBible = findViewById(R.id.imageButtonBible);
        imgButtonBible.setOnClickListener(this);

        navView.setOnNavigationItemSelectedListener(this);
        handleAnimation(myLogoImageView);

        //Global Settings
        setGlobalSettings();

        FirebaseMessaging.getInstance().subscribeToTopic("general")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Successful";
                        if (!task.isSuccessful()) {
                            msg = "Failed";
                        }
                        Log.d(TAG, msg);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//        .setDeveloperModeEnabled(true)
//        .build();

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if(task.isSuccessful()){
                    boolean updated = task.getResult();
                    Log.d(TAG, "Config params updated: " + updated);
                    Toast.makeText(MainActivity.this, "Fetch and activate succeeded",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Fetch failed",
                            Toast.LENGTH_SHORT).show();
                }

                displayMessage();
            }
        });

        backgrounds(myBackgroundLayout);
    }

    public void backgrounds(View myBackgroundLayout) {
        Resources res = getResources();
        final TypedArray myImages = res.obtainTypedArray(R.array.background);
        final Random random = new Random();
        int randomInt = random.nextInt(myImages.length());
        int drawableID = myImages.getResourceId(randomInt, -1);
        myBackgroundLayout.setBackgroundResource(drawableID);
    }

    private void displayMessage() {

        if(mFirebaseRemoteConfig.getBoolean(IS_THERE_A_NEW_MSG)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Add the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button

                }
            });
            // Set other dialog properties
            builder.setMessage(mFirebaseRemoteConfig.getString(MESSAGE_CONTENT))
                    .setTitle(mFirebaseRemoteConfig.getString(MESSAGE_TITLE));

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void setGlobalSettings() {

        //Check if there's previously stored endpoint entries.
        //If it is empty, store the default value to it
        SharedPreferences.Editor editor = mySharedPref.edit();

        String font_color = mySharedPref.getString("font_color", "#000000");
        String background_color = mySharedPref.getString("background_color", "#FFFFFF");

        if (font_color.isEmpty()){
            editor.putString("font_color", "#000000");
        }

        if (background_color.isEmpty()){
            editor.putString("background_color","#FFFFFF");
        }

        editor.apply();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        if (menuItem.getItemId() == R.id.navigation_home) {
            //Crashlytics.getInstance().crash();
            //Do not start activity if the current activity is main.
            Fragment f = this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (f == null) {
                Toast.makeText(this, "You are at home now.", Toast.LENGTH_SHORT).show();
            } else {
                getSupportFragmentManager().popBackStack();
                Objects.requireNonNull(getSupportActionBar()).show();
                Objects.requireNonNull(navView).setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dot_nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_settings:
                //fragment = new DailyBibleFragment();
                Intent nextActivityIntent = new Intent(this, SettingsActivity.class);
                startActivity(nextActivityIntent);
                break;
            case R.id.navigation_about:
//                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//                String endpoint_bible_text = sp.getString("endpoint_bible_text", "");
//                String endpoint_serving_turn = sp.getString("endpoint_serving_turn", "");
                Intent aboutActivityIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutActivityIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void handleAnimation(View myView){

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(myView, "alpha",1f, 0f);
        fadeOut.setDuration(2000);
        ObjectAnimator mover = ObjectAnimator.ofFloat(myView,
                "translationY", 100f, 10f);
        mover.setDuration(2000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(myView, "alpha",
                0f, 1f);
        fadeIn.setDuration(2000);
        AnimatorSet animatorSet = new AnimatorSet();

        //animatorSet.play(mover).with(fadeIn).after(fadeOut);
        animatorSet.play(mover).with(fadeIn);
        animatorSet.start();
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        String myMessage;
        Fragment fragment;

        if (isConnected(this)){
            switch (v.getId()) {
                case R.id.imageButtonSermon: {
                    String[] items = {"주일설교", "Youtube Live", "오디오 듣기"};
                    String title = "선택해 주세요.";
                    showPopUpMenuForSundaySermon(items, title);
                    break;
                }
                case R.id.imageButtonBulletin: {
                    String[] items = {"주일주보", "당번순서"};
                    String title = "선택해 주세요.";
                    showPopUpMenuForAnnouncement(items, title);
                    break;
                }
                case  R.id.imageButtonHome: {
                    String[] items = {"Visit bcsv.org", "Contact"};
                    String title = "선택해 주세요.";
                    showPopUpMenuForWebContact(items, title);
                    break;
                }
                case R.id.imageButtonSundayBible: {
                    String[] items = {"주일 설교 본문", "주일 설교 Review"};
                    String title = "선택해 주세요.";
                    showPopUpMenuForSundayBible(items, title);
                    break;
                }
                case R.id.imageButtonBible: {
                    //Toast.makeText(this,"Coming soon...", Toast.LENGTH_LONG).show();
                    fragment = new DailyBibleFragment();
                    myMessage = "Today's Daily Bible";
                    bundle.putString("message", myMessage );
                    fragment.setArguments(bundle);
                    loadFragment(fragment);
                    break;
                }
                case R.id.imageButtonOffering: {
                    fragment = new WebViewFragment();
                    myMessage = mySharedPref.getString("OFFERING", "https://bcsv.org/offering");
                    bundle.putString("message", myMessage );
                    fragment.setArguments(bundle);
                    loadFragment(fragment);
                    break;
                }
            }
        }else{
            Toast.makeText(this, "Network Connection is not established", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopUpMenuForWebContact(String[] items, String title){

        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle(title);
        mBuilder.setIcon(R.drawable.ic_touch_app_black_24dp);
        mBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Bundle bundle = new Bundle();
                Fragment fragment = null;
                String myMessage = "";

                if( i == 0){
                    //Show Weekly report
                    fragment = new WebViewFragment();
                    myMessage = mySharedPref.getString("HOMEPAGE", "https://bcsv.org/");
                }else if( i == 1){
                    fragment = new WebViewFragment();
                    myMessage = mySharedPref.getString("CONTACT", "https://bcsv.org/contact");
                }

                bundle.putString("message", myMessage);
                if (fragment != null) {
                    fragment.setArguments(bundle);
                }

                loadFragment(fragment);
                dialogInterface.dismiss();
            }
        });

        mBuilder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //Show Dialog window
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void showPopUpMenuForAnnouncement(String[] items, String title){

        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle(title);
        mBuilder.setIcon(R.drawable.ic_touch_app_black_24dp);
        mBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Bundle bundle = new Bundle();
                Fragment fragment = null;

                if( i == 0){
                    //Show Weekly report
                    fragment = new SundayReportFragment();
                }else if( i == 1){
                    //Show Sunday Serving Turn
                    fragment = new ServingTurnFragment();
                }

                bundle.putString("message", "" );
                if (fragment != null) {
                    fragment.setArguments(bundle);
                }

                loadFragment(fragment);
                dialogInterface.dismiss();
            }
        });

        mBuilder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //Show Dialog window
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void showPopUpMenuForSundayBible(String[] items, String title){

        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle(title);
        mBuilder.setIcon(R.drawable.ic_touch_app_black_24dp);
        mBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Bundle bundle = new Bundle();
                Fragment fragment = null;

                if( i == 0){
                    //Show Sunday Bible Text
                    fragment = new SundayBibleTextFragment();
                }else if( i == 1){
                    //Show Sunday Bible Review
                    fragment = new SundayBibleReviewFragment();
                }

                bundle.putString("message", "" );
                if (fragment != null) {
                    fragment.setArguments(bundle);
                }
                loadFragment(fragment);
                dialogInterface.dismiss();
            }
        });

        mBuilder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //Show Dialog window
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void showPopUpMenuForSundaySermon(String[] items, String title){

        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle(title);
        mBuilder.setIcon(R.drawable.ic_touch_app_black_24dp);
        mBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Bundle bundle = new Bundle();
                Fragment fragment = null;

                if( i == 0){
                    //Show Sunday Sermon - Youtube
                    fragment = new WebViewFragment();
                    bundle.putString("message", mySharedPref.getString("SERMON_YOUTUBE", "https://bcsv.org/"));
                }else if( i == 1){
                    //Show Sunday Sermon - Youtube Live
                    fragment = new WebViewFragment();
                    bundle.putString("message", mySharedPref.getString("YOUTUBE_LIVE", "https://bcsv.org/"));
                }else if( i == 2){
                    //Show Sunday Sermon - Audio Archive
                    fragment = new WebViewFragment();
                    bundle.putString("message", mySharedPref.getString("SERMON_AUDIO", "https://bcsv.org/"));
                }

                if (fragment != null) {
                    fragment.setArguments(bundle);
                }
                loadFragment(fragment);
                dialogInterface.dismiss();
            }
        });

        mBuilder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //Show Dialog window
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private boolean isConnected(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            //super.onBackPressed();
            this.finish();
            //additional code
        } else {
            getSupportFragmentManager().popBackStack();
        }

        Objects.requireNonNull(getSupportActionBar()).show();
        Objects.requireNonNull(navView).setVisibility(View.VISIBLE);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) myFrameLayout.getLayoutParams();

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Objects.requireNonNull(getSupportActionBar()).hide();
            navView.setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

            Objects.requireNonNull(getSupportActionBar()).show();
            navView.setVisibility(View.VISIBLE);
        }
    }
}
