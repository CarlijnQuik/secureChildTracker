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
    Button generateQR;
    Button scanQR;
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
        generateQR = (Button) findViewById(R.id.generateQR);
        scanQR = (Button) findViewById(R.id.scanQR);

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
        generateQR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (QRVisible == 1) {
                    displayQR.setVisibility(View.INVISIBLE);
                    generateQR.setText("Generate QR code");
                    QRVisible = 0;
                } else if (QRVisible == 0) {
                    generateQR();
                    displayQR.setVisibility(View.VISIBLE);
                    generateQR.setText("Close QR code");
                    QRVisible = 1;

                }
            }
        });

    }

    public void scanQR(View view){
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

        QRCode = result.toString();
        Log.d("Scanning result:", QRCode);

    }

}
