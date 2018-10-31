package com.example.carli.mychildtrackerdisplay;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.WriterException;

import org.json.JSONException;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = DisplayActivity.class.getSimpleName();
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private GoogleMap mMap;
    FirebaseUser user;
    ImageView QRCode;

    DatabaseReference database;
    ListView listOfLocations;
    ArrayList<Location> locations;
    LocationAdapter locationAdapter;
    AlertDialog.Builder builder;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        // initialize buttons and adapter
        initializeButtons();
        initializeAdapter();

        // initialize Firebase
        user =  FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference(user.getUid()).child("locations");

        // initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void initializeAdapter(){
        // set the list adapter
        locations = new ArrayList<>();
        listOfLocations = (ListView) findViewById(R.id.listView1);
        locationAdapter = new LocationAdapter(this, locations);
        listOfLocations.setAdapter(locationAdapter);
    }

    public void initializeButtons(){
        // decide what clicking the logout button does
        findViewById(R.id.bLogOut).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                FirebaseAuth.getInstance().signOut();
                signOut();
            }
        });;

        // decide what clicking the generate QR code button does
        findViewById(R.id.pairPhone).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                generateQR();
            }
        });;

    }

    // decides what the back button does
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    // generates a QR code and displays it on the screen
    private void generateQR(){

        String randomString = random();

        QRGEncoder qrgEncoder = new QRGEncoder(randomString,
                null,
                QRGContents.Type.TEXT,
                (int) 400d);

        database.child("QRKey").setValue(randomString);

        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();

            // Setting Bitmap to ImageView
            QRCode = (ImageView) findViewById(R.id.qrDisplay);
            QRCode.setImageBitmap(bitmap);
            //QRCode.setVisibility(View.VISIBLE);
            //QRGSaver.save(savePath, edtValue.getText().toString().trim(), bitmap, QRGContents.ImageType.IMAGE_JPEG);

        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }

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
        subscribeToUpdates();

    }

    // makes sure the location is updates
    private void subscribeToUpdates() {

        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // check value in database snapshot
                Log.d(TAG, "datasnapshot " + dataSnapshot.getValue());
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

    // extract the current location from the hashmap
    public Location newLocation(HashMap<String, Object> hashMap){
        location = new Location();
        location.longitude = hashMap.get("longitude").toString();
        location.latitude = hashMap.get("latitude").toString();
        long timeStamp = Long.parseLong(hashMap.get("time").toString());
        location.id_timestamp = getFormatTime(timeStamp);

        return location;
    }

    // generate a random string
    public static String random() {
        int MAX_LENGTH = 10;
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


}