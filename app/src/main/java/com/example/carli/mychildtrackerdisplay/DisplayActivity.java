package com.example.carli.mychildtrackerdisplay;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.carli.mychildtrackerdisplay.Model.Location;
import com.example.carli.mychildtrackerdisplay.Model.UserEntry;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.widget.Toast.LENGTH_SHORT;

public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "logged";
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private GoogleMap mMap;

    FirebaseUser user;
    DatabaseReference database;

    ListView listOfLocations;
    ArrayList<Location> locations;
    LocationAdapter locationAdapter;
    AlertDialog.Builder builder;
    Location location;
    Button pairButton;
    Button logOutButton;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        // define view
        //pairButton = (Button) findViewById(R.id.pairPhoneWithChild);
        logOutButton = (Button) findViewById(R.id.bLogOut);


        // initialize buttons and adapter
        initializeButtons();
        initializeAdapter();

        // initialize Firebase
        user =  FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference(user.getUid());

        // initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    // decides what the back button does
    public void onBackPressed() {
        moveTaskToBack(true);
    }



    public void initializeAdapter(){
        // set the list adapter
        locations = new ArrayList<>();
        listOfLocations = (ListView) findViewById(R.id.locationListView);
        locationAdapter = new LocationAdapter(this, locations);
        listOfLocations.setAdapter(locationAdapter);
    }

    public void initializeButtons(){
        // decide what clicking the logout button does
        logOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                FirebaseAuth.getInstance().signOut();
                signOut();
                Toast.makeText(DisplayActivity.this, "Signed out", LENGTH_SHORT).show();
            }
        });

        pairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPairingActivity();
            }
        });



    }

    public void goToPairingActivity(){
        Intent intent = new Intent(this, PairingXActivity.class);
        startActivity(intent);
    }

    // manages the log out of the user
    private void signOut(){
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);

    };

    public String getFormatTime(long timeStamp){
        Date date = new Date(timeStamp);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, yyyy, HH:mm:ss");
        String time = formatter.format(date);
        Log.d(TAG, "time update" + time);

        return time;

    }

    // decides what happens when the map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Authenticate with Firebase when the Google map is loaded
        mMap = googleMap;
        mMap.setMaxZoomPreference(10);
        if(!database.child("locations").equals(null) ){
            subscribeToUpdates();
        }
    }

    // makes sure the location is updates
    private void subscribeToUpdates() {

        database.child("locations").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // check value in database snapshot
                Log.d(TAG, "datasnapshot " + dataSnapshot.toString());
                setMarker(dataSnapshot);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

                setMarker(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // sets the marker on the map according to the child's location
    private void setMarker(DataSnapshot dataSnapshot) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all at once

        if (dataSnapshot.getValue() != null) {
            String key = dataSnapshot.getKey();
            HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();

            double lat = Double.parseDouble(value.get("latitude").toString());
            double lng = Double.parseDouble(value.get("longitude").toString());
            LatLng latLong = new LatLng(lat, lng);


            // create a location object with the current location's values
            Location currentLocation = newLocation(value);
            locations.add(currentLocation);
            locationAdapter.notifyDataSetChanged();

            // set the markers
            if (!mMarkers.containsKey(key)) {
                mMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(latLong)));
            } else {
                mMarkers.get(key).setPosition(latLong);
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : mMarkers.values()) {
                builder.include(marker.getPosition());
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
        }
    }

    // extract the current location from the hashmap
    public Location newLocation (HashMap < String, Object > hashMap){
        location = new Location();
        location.longitude = Double.parseDouble(hashMap.get("longitude").toString());
        location.latitude = Double.parseDouble(hashMap.get("latitude").toString());
        location.timestamp = Long.parseLong(hashMap.get("time").toString());

        return location;
    }

    private void SOSNotification() {
        String SOS = "Stop SOS notification";
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(SOS), PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("SOS: Your child is in danger!")
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_stat_name);
        builder.build();

    }

}
