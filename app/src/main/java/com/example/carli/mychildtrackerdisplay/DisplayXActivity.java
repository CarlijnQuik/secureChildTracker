package com.example.carli.mychildtrackerdisplay;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.carli.mychildtrackerdisplay.Model.Location;
import com.example.carli.mychildtrackerdisplay.ViewModel.DisplayViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class DisplayXActivity extends FragmentActivity implements OnMapReadyCallback {

    private DisplayViewModel displayViewModel;
    private ArrayList<Location> locationList = new ArrayList<>();
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private GoogleMap mMap;
    private Marker nowMarker;
    private Circle nowCircle;
    private Marker clickedMarker;
    private Circle clickedCircle;

    private LocationAdapter locationAdapter;

    AlertDialog.Builder builder;
    Location location;
    Button logOutButton;
    ListView locationListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        displayViewModel = ViewModelProviders.of(this).get(DisplayViewModel.class);

        // initialize buttons and adapter
        initializeButtons();
        initializeAdapter();
        initializeDropdown();

        // initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void initializeButtons(){
        // decide what clicking the logout button does
        logOutButton = findViewById(R.id.bLogOut);
        logOutButton.setOnClickListener(v -> {
            displayViewModel.signOut();
            Toast.makeText(this, R.string.signed_out, LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginXActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

    }

    public void initializeAdapter(){
        locationAdapter = new LocationAdapter(this, locationList);
        locationListView = findViewById(R.id.locationListView);
        locationListView.setAdapter(locationAdapter);

        locationListView.setOnItemClickListener((parent, view, position, id) -> {
            Location clickedLocation = (Location) parent.getAdapter().getItem(position);
            SimpleDateFormat s = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            setClickedMarker(clickedLocation, Color.BLUE, BitmapDescriptorFactory.HUE_BLUE, s.format(clickedLocation.getTimestamp()));

        });

    }

    public void initializeDropdown(){
        // set the drop down view
        Spinner dropdown = findViewById(R.id.dropdownInterval);
        String[] intervals = new String[]{"10 s", "30 s", "1 min", "5 min", "15 min", "30 min", "1 hour"};
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, intervals);

        //set the spinners adapter to the previously created one
        dropdown.setAdapter(intervalAdapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String interval = (String) parent.getAdapter().getItem(position);
                String[] intervalSplit = interval.split("\\s+");
                interval = intervalSplit[0];
                if(intervalSplit[1].equals("s")) displayViewModel.setInterval(Integer.decode(interval)*1000);
                if(intervalSplit[1].equals("min")) displayViewModel.setInterval(Integer.decode(interval)*1000*60);
                if(intervalSplit[1].equals("hour")) displayViewModel.setInterval(Integer.decode(interval)*1000*3600);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
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
                locationList.add(0, currentLocation);
                if (locationList.size()>20)
                    locationList.remove(20);
                locationAdapter.notifyDataSetChanged();
                setNowMarker(currentLocation, Color.RED, BitmapDescriptorFactory.HUE_RED, "Current location");
            }
        });
    }

    private void setNowMarker(Location currentLocation, int circleColor, float markerColor, String markerTitle){
        boolean first = true;
        if (nowMarker != null) {
            nowMarker.remove();
            first = false;
        }
        if (nowCircle != null)
            nowCircle.remove();

        LatLng latLong = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        nowMarker = mMap.addMarker(new MarkerOptions().title(markerTitle).position(latLong).icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        nowCircle = mMap.addCircle(new CircleOptions().center(latLong).radius(currentLocation.accuracy).strokeColor(circleColor));
        mMarkers.put(markerTitle, nowMarker);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));

    }

    private void setClickedMarker(Location currentLocation, int circleColor, float markerColor, String markerTitle){
        boolean first = true;
        if (clickedMarker != null) {
            clickedMarker.remove();
            first = false;
        }
        if (clickedCircle != null)
            clickedCircle.remove();

        LatLng latLong = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        clickedMarker = mMap.addMarker(new MarkerOptions().title(markerTitle).position(latLong).icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        clickedCircle = mMap.addCircle(new CircleOptions().center(latLong).radius(currentLocation.accuracy).strokeColor(circleColor));
        mMarkers.put(markerTitle, clickedMarker);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));

    }

}
