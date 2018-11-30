package com.example.carli.mychildtrackerdisplay;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.carli.mychildtrackerdisplay.Model.UserEntry;
import com.example.carli.mychildtrackerdisplay.ViewModel.PairingViewModel;
import com.example.carli.mychildtrackerdisplay.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.WriterException;

import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class PairingXActivity extends AppCompatActivity {

    private PairingViewModel pairingViewModel;
    private String userType;
    private Integer nonce1;

    // UI components
    Button showQRbutton;
    Button scanQRbutton;
    TextView introductionText;
    ImageView displayQRimageview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing_x);
        showQRbutton = findViewById(R.id.showQRbutton);
        scanQRbutton = findViewById(R.id.scanQRbutton);
        introductionText = findViewById(R.id.introductionText);
        displayQRimageview = findViewById(R.id.displayQRimageview);
        pairingViewModel = ViewModelProviders.of(this).get(PairingViewModel.class);
    }

    @Override
    public void onStart(){
        super.onStart();

        pairingViewModel.getCurrentUser().observe(this, new Observer<UserEntry>() {
            @Override
            public void onChanged(@Nullable UserEntry currentUser) {
                pairingViewModel.getCurrentUser().removeObserver(this);     // remove from observer list, so it won't get called by every change

                Log.d(Constants.LOG_TAG, "onStart observer called");
                userType = currentUser.getUserType();

                if (currentUser.getUserType().equals(Constants.USERTYPE_CHILD)){
                    showQRbutton.setVisibility(View.VISIBLE);
                    showQRbutton.setOnClickListener(view -> generateQRandWait());
                    scanQRbutton.setVisibility(View.INVISIBLE);
                    introductionText.setText(R.string.pairing_child_1);
                }
                else if (currentUser.getUserType().equals(Constants.USERTYPE_CHILD)){
                    showQRbutton.setVisibility(View.INVISIBLE);
                    scanQRbutton.setVisibility(View.VISIBLE);
                    introductionText.setText(R.string.pairing_parent_1);
                }
                else{
                    Log.d(Constants.LOG_TAG, "Error initiating PairingActivity UI, userType is "+ currentUser.getUserType());
                }
            }
        });
    }

    private void generateQRandWait(){
        if (userType.equals(Constants.USERTYPE_CHILD)){
            generateQRCode();
            pairingViewModel.getCurrentUser().observe(this, new Observer<UserEntry>() {
                @Override
                public void onChanged(@Nullable UserEntry currentUser) {
                    Log.d(Constants.LOG_TAG, "generateQRandWait observer called.");
                    String[] secCheckTest = currentUser.getSecurityCheck().split(Constants.DATA_DELIM);
                    if (Integer.decode(secCheckTest[1]).equals(nonce1)) {
                        pairingViewModel.getCurrentUser().removeObserver(this);
                        displayQRimageview.setImageResource(R.drawable.check);
                        Log.d(Constants.LOG_TAG, "Child's UID transmitted correctly.");
                    }
                }
            });
        }

    }
    private void generateQRCode() {
        String stringToShow = "";
        if (userType.equals(Constants.USERTYPE_CHILD)){
            Random random = new Random();
            nonce1 = random.nextInt();
            Log.d(Constants.LOG_TAG, "Child's NONCE1 is "+nonce1);
            stringToShow = pairingViewModel.getUserID();
            stringToShow += "///";
            stringToShow += nonce1.toString();
        }
        QRGEncoder qrgEncoder = new QRGEncoder(stringToShow, null, QRGContents.Type.TEXT, (int) 400d);

        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            displayQRimageview.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v("TAG", e.toString());
        }
    }

}