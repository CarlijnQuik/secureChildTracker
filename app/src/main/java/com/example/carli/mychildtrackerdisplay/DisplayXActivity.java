package com.example.carli.mychildtrackerdisplay;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.carli.mychildtrackerdisplay.Model.Location;
import com.example.carli.mychildtrackerdisplay.ViewModel.DisplayViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import static android.widget.Toast.LENGTH_SHORT;

public class DisplayXActivity extends FragmentActivity implements OnMapReadyCallback {

    private DisplayViewModel displayViewModel;
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private GoogleMap mMap;
    private Marker nowMarker;
    private Circle nowCircle;

    AlertDialog.Builder builder;
    Location location;
    Button pairButton;
    Button logOutButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        displayViewModel = ViewModelProviders.of(this).get(DisplayViewModel.class);

        // define view
        pairButton = (Button) findViewById(R.id.pairPhoneWithChild);
        logOutButton = (Button) findViewById(R.id.bLogOut);


        // initialize buttons and adapter
        initializeButtons();
       // initializeAdapter();
        // initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    public void initializeButtons(){
        // decide what clicking the logout button does
        logOutButton.setOnClickListener(v -> {
            displayViewModel.signOut();
            Toast.makeText(this, R.string.signed_out, LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Authenticate with Firebase when the Google map is loaded
        mMap = googleMap;
        mMap.setMaxZoomPreference(50);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        subscribeToUpdates();

    }

    private void subscribeToUpdates() {
        displayViewModel.getCurrentLocation().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location currentLocation) {
                Log.d(Constants.LOG_TAG,"Received new location: "+currentLocation.getLatitude()+"/"+currentLocation.getLongitude());
                setMarker(currentLocation);
            }
        });
    }

    private void setMarker(Location currentLocation) {
        boolean first = true;
        if (nowMarker != null) {
            nowMarker.remove();
            first = false;
        }
        if (nowCircle != null)
            nowCircle.remove();

        LatLng latLong = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        nowMarker = mMap.addMarker(new MarkerOptions().title("Current position").position(latLong));
        nowCircle = mMap.addCircle(new CircleOptions().center(latLong).radius(currentLocation.accuracy).strokeColor(Color.RED));
        if (first)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15.0f));
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong));

    }




}
