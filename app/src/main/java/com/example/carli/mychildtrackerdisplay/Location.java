package com.example.carli.mychildtrackerdisplay;

import com.google.firebase.database.DataSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Location object
 */

public class Location implements Serializable {

    public long id_timestamp = 0;
    public double latitude = 0;
    public double longitude = 0;
    public float accuracy = 0;


    public long getId_timestamp() {
        return id_timestamp;
    }

    public void setId_timestamp(long id_timestamp) {
        this.id_timestamp = id_timestamp;
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

    public Location(long id_timestamp, double latitude, double longitude, float accuracy) {
        this.id_timestamp = id_timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }



}

