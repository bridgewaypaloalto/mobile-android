package com.bcsv.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    List<String> listGroup;
    HashMap<String, List<String>> listItem;


    public ExpandableListAdapter(Context context, List<String> listGroup, HashMap<String, List<String>> listItem) {
        this.context = context;
        this.listGroup = listGroup;
        this.listItem = listItem;
    }

    @Override
    public int getGroupCount() {
        return listGroup.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listItem.get(this.listGroup.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listGroup.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listItem.get(this.listGroup.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View convertView, ViewGroup viewGroup) {
        String group = (String) getGroup(groupPosition);
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expandable_group, viewGroup, false);
        }

        TextView textView = convertView.findViewById(R.id.textViewTitle);
        textView.setText(group);

        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animation.setDuration(1000);
        convertView.startAnimation(animation);

        setGroupAttributeSettings(convertView);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View convertView, ViewGroup parent) {
        String child = (String) getChild(groupPosition, childPosition);

        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expandable_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.textViewContent);
        textView.setText(Html.fromHtml(child));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        //textView.setText(child);

        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animation.setDuration(500);
        convertView.startAnimation(animation);

        setCellAttributeSettings(convertView);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private void setCellAttributeSettings(View view) {

        //Check if there's previously stored endpoint entries.
        //If it is empty, store the default value to it
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(view.getContext());

        //Set font, background colors
        LinearLayout myLinearLayout = view.findViewById(R.id.expandable_linearlayout);
        TextView myTextView = view.findViewById(R.id.textViewContent);

        myLinearLayout.setBackgroundColor(Color.parseColor(sp.getString("background_color", "#FFFFFF")));
        myTextView.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        myTextView.setTextSize(sp.getInt("font_size_seek_bar_key",
                view.getContext().getResources().getInteger(R.integer.default_font_size)));
    }

    private void setGroupAttributeSettings(View view) {

        //Check if there's previously stored endpoint entries.
        //If it is empty, store the default value to it
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(view.getContext());

        //Set font, background colors
        TextView myGroupTextView = view.findViewById(R.id.textViewTitle);
        myGroupTextView.setBackgroundColor(Color.parseColor(sp.getString("background_color", "#FFFFFF")));
        myGroupTextView.setTextColor(Color.parseColor(sp.getString("font_color", "#000000")));
        myGroupTextView.setTextSize(sp.getInt("font_size_seek_bar_key",
                view.getContext().getResources().getInteger(R.integer.default_font_size)));
    }
}
