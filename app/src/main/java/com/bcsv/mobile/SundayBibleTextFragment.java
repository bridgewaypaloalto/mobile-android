package com.bcsv.mobile;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class SundayBibleTextFragment extends Fragment {

    private BottomNavigationView navView;
    private ProgressBar mProgressBar;
    private TextView myViewSundayBibleContent;
    private TextView myHeader;
    private ExpandableListView myExpandableView;
    private int lastExpandedPosition = -1;
    private static final String TAG = "SundayBibleTextFragment";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        String endPoint = this.getArguments().getString("message");
        View view = inflater.inflate(R.layout.fragment_expandablelist, null);
        myHeader = view.findViewById(R.id.textView_expandableListHeader);
        navView = getActivity().findViewById(R.id.nav_view);
        mProgressBar = view.findViewById(R.id.expandableview_progressBar);
        mProgressBar.bringToFront();

        //Set background color
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        myExpandableView = view.findViewById(R.id.expandableListView);
        myExpandableView.setBackgroundColor(Color.parseColor(sp.getString("background_color", "#FFFFFF")));

        myExpandableView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if(mLastFirstVisibleItem<firstVisibleItem)
                {
                    Log.d("SCROLLING DOWN","TRUE");
                    if((firstVisibleItem + visibleItemCount) == totalItemCount){
                        Log.d("Hit the bottom!","TRUE");
                        //Hide the bottom navigation bar
                        Objects.requireNonNull(navView).setVisibility(View.GONE);
                    }
                }
                if(mLastFirstVisibleItem>firstVisibleItem)
                {
                    Log.d("SCROLLING UP","TRUE");
                    Objects.requireNonNull(navView).setVisibility(View.VISIBLE);
                }
                mLastFirstVisibleItem=firstVisibleItem;

            }
        });

        loadPage(view, endPoint);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return view;
    }


    private void loadPage(View view, String title) {
        //myViewSundayBibleContent.setMovementMethod(new ScrollingMovementMethod());
        //Get Sunday Bible Text
        myHeader.setText("주일예배 설교본문");
        LoadContent myDailyBibleText = new LoadContent();

        //get endpoint url for sunday bible text
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String endpoint_bible_text = sp.getString("BIBLE_TEXT", "");
        myDailyBibleText.execute(endpoint_bible_text, myViewSundayBibleContent, "Bible");
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
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
                targetView = (TextView)params[1];
                identifier = params[2].toString();

                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(myUrl)
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
            List<String> listGroup = new ArrayList<>();
            HashMap<String, List<String>> listItem = new HashMap<>();

            if(getActivity()!=null){
                ExpandableListAdapter myAdapter = new ExpandableListAdapter(getActivity(), listGroup, listItem);
                myExpandableView.setAdapter(myAdapter);

                try{
                    if (s.isEmpty()){
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Network Error");
                        alert.setMessage(R.string.message_network_error);
                        alert.setPositiveButton("OK",null);
                        alert.show();
                    }else{

                        JSONObject jObj;

                        try {
                            jObj = new JSONObject(s);
                            JSONArray jsonArr = jObj.getJSONArray("bibleText");

                            String referenceText = "";
                            int groupCnt = 0;
                            String icon_bookmark = getResources().getString(R.string.icon_bookmark);
                            String myIcon_title = getResources().getString(R.string.icon_bible);
                            String myIcon_main = getResources().getString(R.string.icon_openbook);

                            for (int i = jsonArr.length()-1; i >= 0; i--){ //Reverse Order
                                //for (int i = 0; i < jsonArr.length(); i++) {

                                JSONObject jb = jsonArr.getJSONObject(i);
                                String title = "";
                                List<String> contents = new ArrayList<>();

                                if (jb.getString("Title").isEmpty()){
                                    //This means this is a reference Bible context
                                    String myIcon = icon_bookmark;
                                    //int myIcon = 0x1f60A;
                                    //getEmojiByUnicode(myIcon)
                                    //String myIcon = getResources().getString(R.string.icon_bookmark);

                                    referenceText += "<br><br>" + myIcon + "참고본문: " + jb.getString("Bible_chapter");
                                    referenceText += "<br>" + jb.getString("Bible_text").replace("\n", "<br>");
                                }
                                else {
                                    //This means this is a main Bible context
                                    title = myIcon_title + jb.getString("Date") + "\n" +
                                            jb.getString("Title");

                                    if (jb.getString("File_url").isEmpty()){
                                        contents.add(myIcon_main + jb.getString("Bible_chapter") + "<br><br>" +
                                                jb.getString("Bible_text") + referenceText.replace("\n", "<br>"));
                                    }else{
                                        contents.add(myIcon_main + jb.getString("Bible_chapter") + "<br><br>" +
                                                String.format("<a href=\"%s\">Download</a> ",
                                                        jb.getString("File_url")) + "<br><br>" +
                                                jb.getString("Bible_text").replace("\n", "<br>") +
                                                referenceText.replace("\n", "<br>"));
                                    }


                                    listGroup.add(title);
                                    listItem.put(listGroup.get(groupCnt), contents);

                                    referenceText = "";
                                    groupCnt ++;
                                }
                            }

                            myAdapter.notifyDataSetChanged();

                            myExpandableView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                                @Override
                                public void onGroupExpand(int groupPosition) {
                                    if(lastExpandedPosition != -1 && groupPosition !=lastExpandedPosition){
                                        myExpandableView.collapseGroup(lastExpandedPosition);
                                    }
                                    lastExpandedPosition = groupPosition;
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                }catch(NullPointerException e){
                    Log.e(TAG, e.toString());
                    makeText(getActivity(), R.string.message_network_error, LENGTH_SHORT).show();
                }
            }
        }
    }
}
