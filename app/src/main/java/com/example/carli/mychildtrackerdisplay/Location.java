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

    public String id_timestamp = null;
    public String latitude = null;
    public String longitude = null;

    public String getId_timestamp() {
        return this.id_timestamp;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }


}

