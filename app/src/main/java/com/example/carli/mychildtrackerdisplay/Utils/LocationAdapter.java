package com.example.carli.mychildtrackerdisplay.Utils;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.carli.mychildtrackerdisplay.Model.Location;
import com.example.carli.mychildtrackerdisplay.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * The adapter of the listview, handles all changes made to the location objects
 */


public class LocationAdapter extends BaseAdapter {

    Activity activity;
    Context context;
    ArrayList<Location> locations;

    public LocationAdapter(Activity activity, ArrayList<Location> locations) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.locations = locations;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.location_item_list, null);
        }

        if (locations != null) {

            // iterate over the items in achievement arraylist
            Location location = locations.get(position);

            // initialize layout components for the listitem
            TextView tvTimestamp = (TextView) convertView.findViewById(R.id.tvTimestamp);
            TextView tvLatitude = (TextView) convertView.findViewById(R.id.tvLatitude);
            TextView tvLongitude = (TextView) convertView.findViewById(R.id.tvLongitude);

            ImageView ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
            ivImage.setImageResource(R.drawable.blue_marker);

            if(location == locations.get(0)){
                ivImage.setImageResource(R.drawable.location);
            }

            // set textview to show achievement name
            if (location.getTimestamp() != Double.MIN_VALUE) {
                SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                tvTimestamp.setText(s.format(location.getTimestamp()));
            }
            if (location.getLatitude() != Double.MIN_VALUE) {
                tvLatitude.setText(""+location.getLatitude());
            }
            if (location.getLongitude() != Double.MIN_VALUE) {
                tvLongitude.setText(""+location.getLongitude());
            }


        }

        return convertView;

    }

    @Override
    public int getCount(){
        return locations.size();
    }

    public Object getItem(int position){
        return locations.get(position);
    }

    public long getItemId(int i){
        return locations.indexOf(getItem(i));
    }

}
