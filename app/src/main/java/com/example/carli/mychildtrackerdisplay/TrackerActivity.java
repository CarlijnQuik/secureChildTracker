package com.example.carli.mychildtrackerdisplay;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.xml.transform.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class TrackerActivity extends Activity {

    private static final int PERMISSIONS_REQUEST = 1;
    private static final String TAG = TrackerService.class.getSimpleName();
    FirebaseUser user;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        // initialize Firebase
        user =  FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference(user.getUid());

        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            //finish();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }

        findViewById(R.id.pairPhoneWithParent).setOnClickListener(new View.OnClickListener() {
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

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public void onBackPressed() {
        moveTaskToBack(true);


    }

    private void startTrackerService() {
        startService(new Intent(this, TrackerService.class));
        Toast.makeText(this, "Tracker service started", Toast.LENGTH_SHORT).show();
        //finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Start the service when the permission is granted
            startTrackerService();
            //Log.d(TAG, "Tracking started...");
        } else {
            Toast.makeText(this, "No permission to start service", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


}