package com.example.carli.mychildtrackerdisplay;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carli.mychildtrackerdisplay.Model.DataWrapper;
import com.example.carli.mychildtrackerdisplay.Model.Location;
import com.example.carli.mychildtrackerdisplay.Utils.Constants;
import com.example.carli.mychildtrackerdisplay.Utils.LocationAdapter;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

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
    Button unPairButton;
    Button dismissSOSButton;
    LinearLayout SOSLayout;
    ListView locationListView;
    TextView SOSText;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_x);
        displayViewModel = ViewModelProviders.of(this).get(DisplayViewModel.class);

        // initialize buttons and adapter
        initializeButtons();
        initializeAdapter();
        initializeDropdown();
        initializeSOSListener();

        // initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    public void initializeSOSListener(){
        dismissSOSButton = findViewById(R.id.sosDismiss);
        SOSLayout = findViewById(R.id.SOSLayout);
        SOSText = findViewById(R.id.sosText);
        displayViewModel.getSOS().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean trigger) {
                if (trigger != null){
                    if (trigger.equals(true)){
                        Log.d(Constants.LOG_TAG, "SOS alert triggered");
                        SOSText.setText(R.string.sos_alert);
                        SOSLayout.setVisibility(View.VISIBLE);
                        if (vibrator != null && vibrator.hasVibrator()) {
                            long[] timings = {0, 400, 200, 400, 200};
                            vibrator.vibrate(VibrationEffect.createWaveform(timings, 2));
                        }
                    }
                }
            }
        });
        dismissSOSButton.setOnClickListener(view -> {
            vibrator.cancel();
            SOSLayout.setVisibility(View.INVISIBLE);
        });
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
        unPairButton = findViewById(R.id.bUnpair);
        unPairButton.setOnClickListener(view -> {
            displayViewModel.setInterval(Constants.SOS_INTERVAL);
            displayViewModel.unPair();
            logOutButton.callOnClick();
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
        String[] intervals = new String[]{"0 s", "10 s", "30 s", "1 min", "5 min", "15 min", "30 min", "1 hour"};
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, intervals);

        //set the spinners adapter to the previously created one
        dropdown.setAdapter(intervalAdapter);
        dropdown.setSelection(4);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String interval = (String) parent.getAdapter().getItem(position);
                String[] intervalSplit = interval.split("\\s+");
                Integer seconds = 0;
                switch(intervalSplit[1]){
                    case "s":
                        seconds = Integer.decode(intervalSplit[0])*1000;
                        break;
                    case "min":
                        seconds = Integer.decode(intervalSplit[0])*1000*60;
                        break;
                    case "hour":
                        seconds = Integer.decode(intervalSplit[0])*1000*3600;
                        break;
                    default:
                        seconds = Integer.decode(intervalSplit[0])*1000;
                }
                if (displayViewModel.setInterval(seconds))
                    Toast.makeText(DisplayXActivity.this, "Refreshing interval set to "+interval, LENGTH_SHORT).show();
                else
                    Toast.makeText(DisplayXActivity.this, "Error setting refresh interval.", LENGTH_SHORT).show();
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
        displayViewModel.getCurrentLocation().observe(this, currentLocation -> {
            if (currentLocation.getError() != null){
                SOSText.setText(currentLocation.getError().getMessage());
                SOSLayout.setVisibility(View.VISIBLE);
                return;
            }

            Log.d(Constants.LOG_TAG,"Received new location: "+currentLocation.getData().getLatitude()+"/"+currentLocation.getData().getLongitude());
            locationList.add(0, currentLocation.getData());
            if (locationList.size()>20)
                locationList.remove(20);
            locationAdapter.notifyDataSetChanged();
            setNowMarker(currentLocation.getData(), Color.RED, BitmapDescriptorFactory.HUE_RED, "Current location");
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
