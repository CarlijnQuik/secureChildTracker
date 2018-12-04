package com.example.carli.mychildtrackerdisplay;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.carli.mychildtrackerdisplay.ViewModel.BaseViewModel;
import com.example.carli.mychildtrackerdisplay.ViewModel.PairingViewModel;
import com.example.carli.mychildtrackerdisplay.ViewModel.SOSViewModel;

public class SOSActivity extends AppCompatActivity {

    SOSViewModel SOSViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SOSViewModel = ViewModelProviders.of(this).get(SOSViewModel.class);
        setContentView(R.layout.activity_sos);

        initializeUI();

    }

    public void initializeUI(){

        findViewById(R.id.SOSButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SOSViewModel.setSOS(true);
            }
        });

    }
}
