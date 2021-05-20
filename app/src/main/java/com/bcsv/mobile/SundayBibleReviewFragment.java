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
import android.widget.Toast;

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

public class SundayBibleReviewFragment extends Fragment {

    private BottomNavigationView navView;
    private ProgressBar mProgressBar;
    private TextView myViewSundayBibleContent;
    private TextView myHeader;
    private ExpandableListView myExpandableView;
    private int lastExpandedPosition = -1;
    private static final String TAG = "BibleReviewFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        String endPoint = this.getArguments().getString("message");
        View view = inflater.inflate(R.layout.fragment_expandablelist, null);
        myHeader = view.findViewById(R.id.textView_expandableListHeader);
        mProgressBar = view.findViewById(R.id.expandableview_progressBar);
        mProgressBar.bringToFront();
        navView = getActivity().findViewById(R.id.nav_view);

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
        myHeader.setText("주일설교 본문리뷰");
        LoadContent myDailyBibleText = new LoadContent();

        //get endpoint url for sunday bible Review
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String endpoint_bible_review = sp.getString("BIBLE_REVIEW", "");
        myDailyBibleText.execute(endpoint_bible_review, myViewSundayBibleContent, "Bible");
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

            if(getActivity()!=null){
                //Fixme: It is very clumsy logic, but an easy fix.
                List<String> finalListGroup = new ArrayList<>();
                HashMap<String, List<String>> finalListItem = new HashMap<>();

                //Intermediate variables for reverse order
                List<String> listGroup = new ArrayList<>();
                HashMap<String, List<String>> listItem = new HashMap<>();

                ExpandableListAdapter myAdapter = new ExpandableListAdapter(getActivity(), finalListGroup, finalListItem);
                myExpandableView.setAdapter(myAdapter);

                try{
                    if (s.isEmpty()){
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Network Error.");
                        alert.setMessage(R.string.message_network_error);
                        alert.setPositiveButton("OK",null);
                        alert.show();
                    }else{

                        JSONObject jObj;

                        try {
                            jObj = new JSONObject(s);
                            JSONArray jsonArr = jObj.getJSONArray("sundayReview");

                            BibleReview myReview = new BibleReview();
                            String title = "";

                            int groupCnt = 0;
                            String myIcon_memo = getString(R.string.icon_memo);
                            String bible_icon = getString(R.string.icon_bible);

                            for (int i = 0; i < jsonArr.length(); i++) {

                                JSONObject jb = jsonArr.getJSONObject(i);
                                title = jb.getString("title");

                                if(title.isEmpty()){
                                    //This is one of sub items.
                                    //Populate sub items.

                                    if(!jb.getString("review").isEmpty()){
                                        myReview.addReview(jb.getString("review"));
                                    }
                                    if(!jb.getString("application").isEmpty()){
                                        myReview.addApplication(jb.getString("application"));
                                    }
                                    if(!jb.getString("in_depth").isEmpty()){
                                        myReview.addIndepth(jb.getString("in_depth"));
                                    }
                                }
                                else{
                                    //This is a new group title.
                                    //Take care of the group being generated, then create a new group
                                    //Build contents using MyReview's member variables

                                    if (!myReview.getChapter().isEmpty()){
                                        List<String> contents = new ArrayList<>();

                                        //This is not the first record
                                        String content = bible_icon + myReview.getChapter() + "<br><br>";

                                        content += "🔘 본문 복습 질문" + "<br>";
                                        for(int j=0; j< myReview.review.size(); j++){
                                            content += myReview.review.get(j) + "<br><br>";
                                        }

                                        content += "🔘 적용 질문" + "<br>";
                                        for(int j=0; j< myReview.application.size(); j++){
                                            content += myReview.application.get(j) + "<br><br>";
                                        }

                                        content += "🔘 심화 학습 질문" + "<br>";
                                        for(int j=0; j< myReview.in_depth.size(); j++){
                                            content += myReview.in_depth.get(j) + "<br><br>";
                                        }

                                        contents.add(content);
                                        listGroup.add(myIcon_memo + myReview.getMyDate() + "\n" + myReview.getMyTitle());
                                        listItem.put(listGroup.get(groupCnt), contents);

                                        groupCnt ++;

                                        //Clear myReview ...
                                        myReview.clearContent();
                                    }


                                    //This is the first record
                                    if(!jb.getString("date").isEmpty()){
                                        myReview.setDate(jb.getString("date"));
                                    }
                                    if(!jb.getString("title").isEmpty()){
                                        myReview.setMyTitle(jb.getString("title"));
                                    }

                                    if(!jb.getString("chapter").isEmpty()){
                                        myReview.setChapter(jb.getString("chapter"));
                                    }

                                    if(!jb.getString("review").isEmpty()){
                                        myReview.addReview(jb.getString("review"));
                                    }
                                    if(!jb.getString("application").isEmpty()){
                                        myReview.addApplication(jb.getString("application"));
                                    }
                                    if(!jb.getString("in_depth").isEmpty()){
                                        myReview.addIndepth(jb.getString("in_depth"));
                                    }

                                }
                            }

                            //This is the last record.
                            //Fixme: There should be a better way to take care of it.

                            List<String> contents = new ArrayList<>();

                            //This is not the first record
                            String content = "✝️" + myReview.getChapter() + "<br>";

                            content += "🔘 본문 복습 질문" + "<br>";
                            for(int j=0; j< myReview.review.size(); j++){
                                content += myReview.review.get(j) + "<br><br>";
                            }

                            content += "🔘 적용 질문" + "<br>";
                            for(int j=0; j< myReview.application.size(); j++){
                                content += myReview.application.get(j) + "<br><br>";
                            }

                            content += "🔘 심화 학습 질문" + "<br>";
                            for(int j=0; j< myReview.in_depth.size(); j++){
                                content += myReview.in_depth.get(j) + "<br><br>";
                            }

                            contents.add(content);
                            listGroup.add(myIcon_memo + myReview.getMyDate() + "\n" + myReview.getMyTitle());
                            listItem.put(listGroup.get(groupCnt), contents);

                            //Final touch
                            for(int j= listGroup.size() -1; j >= 0; j--){
                                finalListGroup.add(listGroup.get(j));
                                finalListItem.put(listGroup.get(j), listItem.get(listGroup.get(j)));
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
                    Toast.makeText(getActivity(), R.string.message_network_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
