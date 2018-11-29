package com.example.carli.mychildtrackerdisplay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.WriterException;

import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

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
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final String KEY_ALIAS = "parentchildkey";
    private SecretKey key;
    private Integer nonce = 0;
    private String userType;
    FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference(user.getUid());
    private String partnerid;
    private String securityCheck;
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

        //initializeButtons();

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
        initializeButtons();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(zXingScannerView != null) {
            zXingScannerView.stopCamera();
        }

    }



    public void initializeButtons(){

        findViewById(R.id.securitycheck).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    securityCheck();
            }
        });




        // decide what clicking the generate QR code button does
        childGenerateQR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userType = "child";
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

                generateQR(user.getUid());
            }
        });



        // decide what clicking the generate QR code button does
        parentGenerateQR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userType = "parent";
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

                try {
                    KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);
                    keyGenerator.init(128);
                    key = keyGenerator.generateKey();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                Random random = new Random();
                nonce = random.nextInt();

                    Log.d("clicked", "parentGenerateQR clicked");
                    byte[] bytes = key.getEncoded();
                    String output = Base64.encodeToString(bytes, Base64.DEFAULT);
                    output = output.replaceAll("\n", "");
                    output += "////";
                    output += nonce.toString();
                    output += "////";
                    output += user.getUid();
                    Log.d("PARENTCHILDkey", output);
                    generateQR(output);

            }
        });


        childScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userType = "child";
                Log.d("clicked", "childScanQR clicked");
                scanQR(false);
            }
        });

        parentScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userType = "parent";
                Log.d("clicked", "parentScanQR clicked");///
                scanQR(true);
            }
        });

    }

    public void scanQR(boolean parent){
        // Code here executes on main thread after user presses button
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();

    }



    // generates a QR code and displays it on the screen
    private void generateQR(String string){

        QRGEncoder qrgEncoder = new QRGEncoder(string,
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

        QRCode = result.toString();
        if (userType.equals("child"))
            processQRchild(QRCode);
        else
            processQRparent(QRCode);

        Log.d("Scanning result:", QRCode);

    }

    private void processQRchild(String qrCode) {
        String[]data = qrCode.split("////");
        nonce = Integer.decode(data[1])+1;
        byte[] bytes = Base64.decode(data[0], Base64.DEFAULT);
        key = new SecretKeySpec(bytes, 0, bytes.length, KeyProperties.KEY_ALGORITHM_AES);
        database.child("partnerID").setValue(data[2]);

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ciphertext = cipher.doFinal(nonce.toString().getBytes());
            database.child("securityCheck").setValue(new String(Base64.encode(ciphertext,Base64.DEFAULT)));
            Log.d("PARENTCHILDkey", new String(ciphertext));
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    private void processQRparent(String qrCode){
        database.child("partnerID").setValue(qrCode);
        partnerid = qrCode;
    }

    private void securityCheck(){

Log.d("PARENTCHILDkey", "calling securityCheck");
        database.child("securityCheck").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                securityCheck = dataSnapshot.getValue(String.class);
                Log.d("PARENTCHILDkey", "datasnapshot " + dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytes = cipher.doFinal(Base64.decode(securityCheck.getBytes(), Base64.DEFAULT));
            String noncex = new String(bytes);
            Integer noncey = Integer.parseInt(noncex);
            Log.d("PARENTCHILDkey", "Nonce received from DB: "+noncey.toString());
            Log.d("PARENTCHILDkey", "Nonce saved: "+nonce.toString());

        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

}