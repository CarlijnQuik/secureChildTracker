package com.example.carli.mychildtrackerdisplay;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carli.mychildtrackerdisplay.Model.UserEntry;
import com.example.carli.mychildtrackerdisplay.ViewModel.PairingViewModel;

import com.google.zxing.Result;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class PairingXActivity extends AppCompatActivity {

    private PairingViewModel pairingViewModel;
    private String userType;
    private static final int PERMISSIONS_REQUEST = 1;

    // UI components
    Button showQRbutton;
    Button scanQRbutton;
    TextView introductionText;
    ImageView displayQRimageview;
    ZXingScannerView zXingScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pairingViewModel = ViewModelProviders.of(this).get(PairingViewModel.class);
        setContentView(R.layout.activity_pairing_x);

        int permissionCamera = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA);
        if (permissionCamera == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST);
        }
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
                initUI(pairingViewModel.getStep());
            }
        });
    }

    private void initUI(Integer step){
        showQRbutton = findViewById(R.id.showQRbutton);
        scanQRbutton = findViewById(R.id.scanQRbutton);
        introductionText = findViewById(R.id.introductionText);
        displayQRimageview = findViewById(R.id.displayQRimageview);

        if (userType.equals(Constants.USERTYPE_CHILD)){
            switch (step){
                case 1:
                    showQRbutton.setVisibility(View.VISIBLE);
                    showQRbutton.setOnClickListener(view -> generateQRandWait());
                    scanQRbutton.setVisibility(View.INVISIBLE);
                    introductionText.setText(R.string.pairing_child_1);
                    break;
                case 2:
                    showQRbutton.setVisibility(View.INVISIBLE);
                    scanQRbutton.setOnClickListener(view -> scanQRandProcess());
                    scanQRbutton.setVisibility(View.VISIBLE);
                    introductionText.setText(R.string.pairing_child_2);
                    break;
                case 3:
                    introductionText.setText(R.string.pairing_success);
                    displayQRimageview.setImageResource(R.drawable.check);
                    scanQRbutton.setVisibility(View.INVISIBLE);
                    showQRbutton.setVisibility(View.INVISIBLE);

                    // go to the SOS activity
                    Intent intent = new Intent(this, SOSActivity.class);
                    startActivity(intent);
                    break;
                default:
                    introductionText.setText("Stepping error");

            }

        }
        else if (userType.equals(Constants.USERTYPE_PARENT)){
            switch (step){
                case 1:
                    showQRbutton.setVisibility(View.INVISIBLE);
                    scanQRbutton.setVisibility(View.VISIBLE);
                    scanQRbutton.setOnClickListener(view -> scanQRandProcess());
                    introductionText.setText(R.string.pairing_parent_1);
                    break;
                case 2:
                    showQRbutton.setVisibility(View.VISIBLE);
                    scanQRbutton.setVisibility(View.INVISIBLE);
                    showQRbutton.setOnClickListener(view -> generateQRandWait());
                    introductionText.setText(R.string.pairing_parent_2);
                    break;
                case 3:
                    scanQRbutton.setVisibility(View.INVISIBLE);
                    showQRbutton.setVisibility(View.INVISIBLE);
                    introductionText.setText(R.string.pairing_success);
                    displayQRimageview.setImageResource(R.drawable.check);
                    break;
                default:
                    introductionText.setText("Stepping error");
            }
        }
        else{
            Log.d(Constants.LOG_TAG, "Error initiating PairingActivity UI, userType is "+ userType);
        }
    }



    private void generateQRandWait(){
            if (userType.equals(Constants.USERTYPE_CHILD)) {
                showQR();
                pairingViewModel.incStep();
                initUI(pairingViewModel.getStep());
            }
            else if (userType.equals(Constants.USERTYPE_PARENT)){
                showQR();
                pairingViewModel.checkPartnersSecurityCheck();
                pairingViewModel.getPartnersSecurityCheck().observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                    Log.d(Constants.LOG_TAG, "getPartnersSecurityCheck observer called, string: "+s);
                        if (pairingViewModel.decryptSecCheck(s)){
                            pairingViewModel.getPartnersSecurityCheck().removeObserver(this);
                            pairingViewModel.incStep();
                            initUI(pairingViewModel.getStep());
                        }
                    }
                });
            }

    }
    private void showQR() {
        QRGEncoder qrgEncoder = new QRGEncoder(pairingViewModel.generateQR(userType), null, QRGContents.Type.TEXT, (int) 400d);
        Log.d(Constants.LOG_TAG, userType+"'s NONCE1 is "+pairingViewModel.getNonce1());

        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            displayQRimageview.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v("TAG", e.toString());
        }
    }
    private void scanQRandProcess() {

        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
            @Override
            public void handleResult(Result result) {
                if (pairingViewModel.processQR(userType, result.getText())){
                    zXingScannerView.stopCameraPreview();
                    zXingScannerView.stopCamera(); //<- then stop the camera
                    setContentView(R.layout.activity_pairing_x);
                    pairingViewModel.incStep();
                    initUI(pairingViewModel.getStep());
                    displayQRimageview.setImageResource(R.drawable.check);

                    Log.d(Constants.LOG_TAG, userType+"'s UID transmitted correctly.");
                    Log.d(Constants.LOG_TAG, "QR scanned result: "+ result.getText());
                }
                else{
                    Toast.makeText(getApplicationContext(),"Incorrect QR code scanned.",Toast.LENGTH_SHORT).show();
                    zXingScannerView.resumeCameraPreview(this);
                }

            }
        });
        zXingScannerView.startCamera();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(zXingScannerView != null) {
            zXingScannerView.stopCamera();
        }

    }

}