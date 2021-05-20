package com.bcsv.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.List;

public class ServingTurnListAdapter extends ArrayAdapter<ServingTurn> {

    private static final String TAG = "ServingTurnListAdapter";
    private Context mContext;
    private int mResource;

    public ServingTurnListAdapter(@NonNull Context context, int resource, @NonNull List<ServingTurn> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);
        TextView tvSunday_date = convertView.findViewById(R.id.sunday_date);
        TextView tvSunday_food = convertView.findViewById(R.id.sunday_food);
        TextView tvSunday_joycorner = convertView.findViewById(R.id.sunday_joycorner);
        TextView tvSunday_praydate = convertView.findViewById(R.id.sunday_praydate);
        TextView tvSunday_prayer = convertView.findViewById(R.id.sunday_prayer);

        tvSunday_date.setText(getItem(position).getSundayDate());

        tvSunday_food.setText(getItem(position).getFood());
        tvSunday_joycorner.setText(getItem(position).getJoycorner());

        String babysit =  getItem(position).getBabysitter() +
                "(" + getItem(position).getPrayDate().substring(5)+")";

        tvSunday_praydate.setText(babysit);
        tvSunday_prayer.setText(getItem(position).getPrayer());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(convertView.getContext());
        LinearLayout myLinearLayout = convertView.findViewById(R.id.servingturn_linearlayout_cell);

        myLinearLayout.setBackgroundColor(Color.parseColor(sp.getString("background_color", "#FFFFFF")));

        tvSunday_date.setTextSize(sp.getInt("font_size_seek_bar_key",mContext.getResources().getInteger(R.integer.default_font_size)));
        tvSunday_date.setTextSize(sp.getInt("font_size_seek_bar_key",mContext.getResources().getInteger(R.integer.default_font_size)));
        tvSunday_food.setTextSize(sp.getInt("font_size_seek_bar_key",mContext.getResources().getInteger(R.integer.default_font_size)));
        tvSunday_joycorner.setTextSize(sp.getInt("font_size_seek_bar_key",mContext.getResources().getInteger(R.integer.default_font_size)));
        tvSunday_praydate.setTextSize(sp.getInt("font_size_seek_bar_key",mContext.getResources().getInteger(R.integer.default_font_size)));
        tvSunday_prayer.setTextSize(sp.getInt("font_size_seek_bar_key",mContext.getResources().getInteger(R.integer.default_font_size)));

        tvSunday_date.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        tvSunday_date.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        tvSunday_food.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        tvSunday_joycorner.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        tvSunday_praydate.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        tvSunday_prayer.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));

        return convertView;
    }
}