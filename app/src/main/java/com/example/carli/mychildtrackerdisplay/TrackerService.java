package com.example.carli.mychildtrackerdisplay;

import com.example.carli.mychildtrackerdisplay.Utils.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackerService extends LifecycleService {

    private static final String TAG = TrackerService.class.getSimpleName();
    private ChildTracker tracker;
    FirebaseUser user;
    FusedLocationProviderClient client;
    LocationRequest request;
    LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        user = FirebaseAuth.getInstance().getCurrentUser();
        tracker = new ChildTracker();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                tracker.setLocation(location);
            }
        };

        requestLocationAndIntervalUpdates();
        buildNotification();

    }


    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_tracker);
        startForeground(1, builder.build());

    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            FirebaseAuth.getInstance().signOut();

            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
            stopSelf();


        }
    };

    private void requestLocationAndIntervalUpdates() {
        request = LocationRequest.create();
        request.setInterval(10000);
        request.setFastestInterval(3000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client = LocationServices.getFusedLocationProviderClient(this);
        requestIntervalUpdates();
    }

    private void requestLocationUpdate(){
        Log.d(Constants.LOG_TAG,"requestLocationUpdate called");
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, locationCallback, null);

        }
    }


    private void requestIntervalUpdates() {
        tracker.getInterval().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer intval) {
                Log.d(Constants.LOG_TAG, "Interval onChange called."+intval);
                if (intval != null) {
                    if (intval.equals(0)){
                        client.removeLocationUpdates(locationCallback);
                        Log.d(Constants.LOG_TAG, "Location Updates stopped by parent");
                    }
                    else if (intval.equals(Constants.SOS_INTERVAL)){
                        client.removeLocationUpdates(locationCallback);
                        stopSelf();
                    }
                    else {
                        request.setInterval(intval);
                        request.setFastestInterval(intval);
                        Log.d(Constants.LOG_TAG, "Interval changed to: " + intval);
                        requestLocationUpdate();
                    }
                }
                else{
                    requestLocationUpdate();
                }
            }
        });
    }



    public String getFormatTime(long timeStamp){
        Date date = new Date(timeStamp);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String time = formatter.format(date);
        Log.d(TAG, "time update" + time);

        return time;

    }



}

