package com.example.carli.mychildtrackerdisplay;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class PairingActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    ImageView displayQR;
    Button parentGenerateQR;
    Button parentScanQR;
    Button childGenerateQR;
    Button childScanQR;
    int QRVisible = 0;
    String QRCode;
    private ZXingScannerView zXingScannerView;
    private static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        // define views
        displayQR = (ImageView) findViewById(R.id.displayQR);
        parentGenerateQR = (Button) findViewById(R.id.parentGenerateQR);
        parentScanQR = (Button) findViewById(R.id.parentScanQR);
        childGenerateQR = (Button) findViewById(R.id.childGenerateQR);
        childScanQR = (Button) findViewById(R.id.childScanQR);

        initializeButtons();

        int permissionCamera = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA);
        if (permissionCamera == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST);
        }
    }

    public void onBackPressed() {
        moveTaskToBack(true);


    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(zXingScannerView != null) {
            zXingScannerView.stopCamera();
        }

    }

    public void initializeButtons(){

        // decide what clicking the generate QR code button does
        childGenerateQR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if (QRVisible == 1) {
//                    displayQR.setVisibility(View.INVISIBLE);
//                    childGenerateQR.setText("Generate QR code");
//                    QRVisible = 0;
//                } else if (QRVisible == 0) {
//                    generateQR();
//                    displayQR.setVisibility(View.VISIBLE);
//                    childGenerateQR.setText("Close QR code");
//                    QRVisible = 1;
//
//                }
                Log.d("clicked", "childGenerateQR clicked");
                generateQR();
            }
        });

        // decide what clicking the generate QR code button does
        parentGenerateQR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                if (QRVisible == 1) {
//                    displayQR.setVisibility(View.INVISIBLE);
//                    parentGenerateQR.setText("Generate QR code");
//                    QRVisible = 0;
//                } else if (QRVisible == 0) {
//                    generateQR();
//                    displayQR.setVisibility(View.VISIBLE);
//                    parentGenerateQR.setText("Close QR code");
//                    QRVisible = 1;
//
//                }
                Log.d("clicked", "parentGenerateQR clicked");
                generateQR();
            }
        });

        childScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("clicked", "childScanQR clicked");
                scanQR();
            }
        });

        parentScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("clicked", "parentScanQR clicked");
                scanQR();
            }
        });

    }

    public void scanQR(){
        // Code here executes on main thread after user presses button
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();

    }



    // generates a QR code and displays it on the screen
    private void generateQR(){
        String randomString = "Hola!";

        QRGEncoder qrgEncoder = new QRGEncoder(randomString,
                null,
                QRGContents.Type.TEXT,
                (int) 400d);

        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();

            // Setting Bitmap to ImageView

            displayQR.setImageBitmap(bitmap);
            //QRCode.setVisibility(View.VISIBLE);
            //QRGSaver.save(savePath, edtValue.getText().toString().trim(), bitmap, QRGContents.ImageType.IMAGE_JPEG);




        } catch (WriterException e) {
            Log.v("TAG", e.toString());
        }

    }

    @Override
    public void handleResult(com.google.zxing.Result result) {
        Toast.makeText(getApplicationContext(),result.getText(),Toast.LENGTH_SHORT).show();
        zXingScannerView.resumeCameraPreview(this);
        //zXingScannerView.removeAllViews(); //<- here remove all the views, it will make an Activity having no View
        zXingScannerView.stopCameraPreview();
        zXingScannerView.stopCamera(); //<- then stop the camera
        setContentView(R.layout.activity_pairing); //<- and set the View again.
        QRCode = result.toString();
        Log.d("Scanning result:", QRCode);

    }

}