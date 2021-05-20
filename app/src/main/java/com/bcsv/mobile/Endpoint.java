package com.bcsv.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Endpoint {

    private static final String TAG = "Endpoint";

    public Endpoint(Context context) {
        String url = "https://script.google.com/macros/s/AKfycbwW_u3urSmxnQrIFsPxwVVzvbNnAtscBZGvxcRfYzJXuLQEWMNB/exec";
        LoadContent myTitle = new LoadContent();
        myTitle.execute(url, context);
    }

    class LoadContent extends AsyncTask<Object, Void, String> {
        Context context;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                URL myUrl = new URL(params[0].toString());
                this.context = (Context) params[1];

                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(myUrl)
                        .build();
                Response response = null;

                try {
                    response = client.newCall(request).execute();
                    return response.body().string();

                } catch (IOException e) {
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
            try {
                if (s.isEmpty()) {
                    Log.d(TAG, "Endpoint returns empty string");
                } else {
                    try {
                        JSONObject jObj;
                        jObj = new JSONObject(s);
                        JSONArray jsonArr = jObj.getJSONArray("endpoints");

                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.context);
                        SharedPreferences.Editor editor = sp.edit();

                        for (int i = 0; i < jsonArr.length(); i++) {

                            JSONObject jb = jsonArr.getJSONObject(i);
                            editor.putString(jb.getString("endpoint"), jb.getString("url"));
                        }

                        editor.apply();

                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
