package com.bcsv.mobile;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class DailyBibleFragment extends Fragment {
    private ProgressBar mProgressBar;
    static String calendarEndpoint = "https://sum.su.or.kr:8888/Ajax/Bible/Calendar";
    static String bibleEndpoint = "https://sum.su.or.kr:8888/Ajax/Bible/BodyBible";
    private TextView myViewBibleTitle;
    private TextView myViewBibleContent;
    private ScrollView myScrollView;
    private BottomNavigationView navView;
    private static final String TAG = "DailyBibleFragment";

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        String endPoint = this.getArguments().getString("message");
        View view = inflater.inflate(R.layout.fragment_dailybible, null);
        navView = getActivity().findViewById(R.id.nav_view);
        myScrollView = view.findViewById(R.id.dailyBible_scrollView);
        mProgressBar = view.findViewById(R.id.progressBar);
        mProgressBar.bringToFront();
        loadPage(view, endPoint);

        myScrollView.setOnScrollChangeListener(new ScrollView.OnScrollChangeListener(){

            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                /* We take the last son in the scrollview */
                View view = myScrollView.getChildAt(myScrollView.getChildCount() - 1);
                int diff = (view.getBottom() - (myScrollView.getHeight() + myScrollView.getScrollY()));

                /* if diff is zero, then the bottom has been reached */
                if (diff == 0) {
                    navView.setVisibility(View.GONE);
                }
                else
                    navView.setVisibility(View.VISIBLE);
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        setGlobalSettings(view);
        return view;
    }

    private void loadPage(View view, String title) {
        myViewBibleTitle = view.findViewById(R.id.bibleTitle);
        myViewBibleContent = view.findViewById(R.id.dailyBibleContent);
        myViewBibleContent.setMovementMethod(new ScrollingMovementMethod());


        //Get Calendar information
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("qt_ty", "QT1");
            jsonObject.put("Base_de", getFirstDayofMonth());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        LoadContent myTitle = new LoadContent();
        myTitle.execute(calendarEndpoint, jsonObject, myViewBibleTitle, "Calendar");

        try{
            jsonObject.put("Base_de", getDate());
        }catch (JSONException e) {
            e.printStackTrace();
        }

        LoadContent myCalendar = new LoadContent();
        myCalendar.execute(bibleEndpoint, jsonObject, myViewBibleContent, "Bible");
    }

    private String getDate(){

        String DATE_FORMAT_2 = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_2);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.add(Calendar.DAY_OF_MONTH, 0);

        Date today = cal.getTime();
        return dateFormat.format(today);
    }

    private String getFirstDayofMonth(){
        String DATE_FORMAT_2 = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_2);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDay = cal.getTime();
        return dateFormat.format(firstDay);
    }

    class LoadContent extends AsyncTask<Object, Void, String>
    {
        TextView targetView;
        String identifier;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                URL myUrl = new URL(params[0].toString());
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, params[1].toString());
                targetView = (TextView)params[2];
                identifier = params[3].toString();

                OkHttpClient client = new OkHttpClient();
                client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
                Request request = new Request
                        .Builder()
                        .url(myUrl)
                        .post(body)
                        .build();
                Response response = null;

                try{
                    response = client.newCall(request).execute();
                    return response.body().string();

                }catch(IOException e){
                    e.printStackTrace();
                }

                return null;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressBar.setVisibility(View.GONE);
            JSONArray json;
            String today = getDate();
            try {
                if (s.isEmpty()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle(getString(R.string.message_network_error));
                    alert.setMessage("Please try again.");
                    alert.setPositiveButton("OK", null);
                    alert.show();
                }else{
                    switch(identifier){
                        case "Calendar":{
                            try {
                                json = new JSONArray(s);

                                for (int i = 0; i < json.length(); i++) {
                                    JSONObject jb = json.getJSONObject(i);

                                    if(jb.getString("Bible_date").equals(today)){
                                        String Bible_date = jb.getString("Bible_date");
                                        String Bible_name = jb.getString("Bible_name");
                                        String Bible_chapter = jb.getString("Bible_chapter");
                                        targetView.append(Bible_date + '\n');
                                        targetView.append(Bible_name + " " + Bible_chapter);
                                        break;
                                    }
                                }

                            } catch (JSONException e) {
                                Log.e(TAG, e.toString());
                                makeText(getContext(), R.string.message_network_error, LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case "Bible":{
                            try {
                                json = new JSONArray(s);

                                for (int i = 0; i < json.length(); i++) {
                                    JSONObject jb = json.getJSONObject(i);
                                    String chapter = jb.getString("Chapter");
                                    String verse = jb.getString("Verse");
                                    String text = jb.getString("Bible_Cn");
                                    targetView.append('\n' + chapter + ":" + verse + "\n");
                                    targetView.append(text + '\n');
                                }

                            } catch (JSONException e) {
                                Log.e(TAG, e.toString());
                                makeText(getContext(), R.string.message_network_error, LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                }
            }catch(NullPointerException e){
                Log.e(TAG, e.toString());
                Toast.makeText(getActivity(), R.string.message_network_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setGlobalSettings(View view) {

        //Check if there's previously stored endpoint entries.
        //If it is empty, store the default value to it
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(view.getContext());

        //Set font, background colors
        TextView myTextView_header = view.findViewById(R.id.bibleHeader);
        TextView myTextView_title = view.findViewById(R.id.bibleTitle);
        TextView myTextView_content = view.findViewById(R.id.dailyBibleContent);
        LinearLayout myLinearLayout = view.findViewById(R.id.dailybible_linearlayout);

        myLinearLayout.setBackgroundColor(Color.parseColor(sp.getString("background_color", "#FFFFFF")));
        myTextView_title.setBackgroundColor(Color.parseColor(sp.getString("background_color", "#FFFFFF")));
        myTextView_header.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        myTextView_title.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        myTextView_content.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        myTextView_content.setTextSize(sp.getInt("font_size_seek_bar_key",
                view.getContext().getResources().getInteger(R.integer.default_font_size)));
    }
}
