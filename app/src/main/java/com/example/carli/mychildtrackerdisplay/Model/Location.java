package com.example.carli.mychildtrackerdisplay.Model;

import com.google.firebase.database.DataSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Location object
 */

public class Location implements Serializable {

    public long timestamp = 0;
    public double latitude = Double.MIN_VALUE;
    public double longitude = Double.MIN_VALUE;
    public float accuracy = 0;


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public Location(long timestamp, double latitude, double longitude, float accuracy) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    public Location(){}


}

