package com.bcsv.mobile;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
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
import java.util.List;
import java.util.Objects;

public class ServingTurnFragment extends Fragment {

    private BottomNavigationView navView;
    private ProgressBar mProgressBar;
    private TextView myViewServingTurn;
    private TextView myHeader;
    private ListView myListView;
    private static final String TAG = "DailyBibleFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        String endPoint = this.getArguments().getString("message");
        View view = inflater.inflate(R.layout.fragment_servingturn, null);
        myHeader = view.findViewById(R.id.textView_ListHeader);
        myListView = view.findViewById(R.id.servingturn_listview);
        mProgressBar = view.findViewById(R.id.customlistview_progressBar);
        mProgressBar.bringToFront();
        navView = getActivity().findViewById(R.id.nav_view);
        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
        myHeader.setText("주일 당번 순서");
        LoadContent myDailyBibleText = new LoadContent();

        //get endpoint url for sunday serving turn
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String endpoint_serving_turn = sp.getString("SERVING_TURN", "");
        myDailyBibleText.execute(endpoint_serving_turn, myViewServingTurn, "Bible");
    }

    class LoadContent extends AsyncTask<Object, Integer, String>
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
            List<ServingTurn> listGroup = new ArrayList<>();

            if(getActivity()!=null){
                ServingTurnListAdapter myAdapter = new ServingTurnListAdapter(getActivity(),R.layout.cell_servingturn, listGroup);
                myListView.setAdapter(myAdapter);

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
                            JSONArray jsonArr = jObj.getJSONArray("servingTurns");

                            for (int i = 0; i < jsonArr.length(); i++) {

                                JSONObject jb = jsonArr.getJSONObject(i);
                                ServingTurn myTurn = new ServingTurn(
                                        jb.getString("date"),
                                        jb.getString("prayer"),
                                        jb.getString("food"),
                                        jb.getString("joycorner"),
                                        jb.getString("tuesday_pray_meeting"),
                                        jb.getString("babysitter"));

                                listGroup.add(myTurn);
                            }

                            myAdapter.notifyDataSetChanged();

//                    myListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//                        @Override
//                        public void onGroupExpand(int groupPosition) {
//                            if(lastExpandedPosition != -1 && groupPosition !=lastExpandedPosition){
//                                myListView.collapseGroup(lastExpandedPosition);
//                            }
//                            lastExpandedPosition = groupPosition;
//                        }
//                    });

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
