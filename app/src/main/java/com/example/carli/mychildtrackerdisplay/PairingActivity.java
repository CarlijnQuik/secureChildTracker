package com.example.carli.mychildtrackerdisplay;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class PairingActivity extends AppCompatActivity {

    ImageView QRCode;
    Button pairButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        // define views
        QRCode = (ImageView) findViewById(R.id.qrDisplay);
        pairButton = (Button) findViewById(R.id.pairPhone);
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

            QRCode.setImageBitmap(bitmap);
            //QRCode.setVisibility(View.VISIBLE);
            //QRGSaver.save(savePath, edtValue.getText().toString().trim(), bitmap, QRGContents.ImageType.IMAGE_JPEG);




        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }

    }

    // decide what clicking the generate QR code button does
    findViewById(R.id.pairPhone).setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            if (QRVisible == 1) {
                QRCode.setVisibility(View.INVISIBLE);
                pairButton.setText("Generate QR code");
                QRVisible = 0;
            } else if (QRVisible == 0) {
                generateQR();
                QRCode.setVisibility(View.VISIBLE);
                pairButton.setText("Close QR code");
                QRVisible = 1;

            }
        }
    });
}
