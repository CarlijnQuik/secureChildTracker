package com.example.carli.mychildtrackerdisplay;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * The adapter of the listview, handles all changes made to the location objects
 */


public class LocationAdapter  extends BaseAdapter {

    Activity activity;
    Context context;
    ArrayList<Location> locations;

    public LocationAdapter(Activity activity, ArrayList<Location> books) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.locations = books;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.location_item_list, null);
        }

        // iterate over the items in achievement arraylist
        Location location = locations.get(position);

        if (locations != null) {

            // initialize layout components for the listitem
            TextView tvTimestamp = (TextView) convertView.findViewById(R.id.tvTimestamp);
            TextView tvLatitude = (TextView) convertView.findViewById(R.id.tvLatitude);
            ImageView ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
            TextView tvLongitude = (TextView) convertView.findViewById(R.id.tvLongitude);

            // set textview to show achievement name
            if (location.getId_timestamp() != null) {
                tvTimestamp.setText(location.getId_timestamp());
            }
            if (location.getLatitude() != null) {
                tvLatitude.setText(location.getLatitude());
            }
            if (location.getLongitude() != null) {
                tvLongitude.setText(location.getLongitude());
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
