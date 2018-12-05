package com.example.carli.mychildtrackerdisplay;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.carli.mychildtrackerdisplay.ViewModel.BaseViewModel;
import com.example.carli.mychildtrackerdisplay.ViewModel.PairingViewModel;
import com.example.carli.mychildtrackerdisplay.ViewModel.SOSViewModel;

public class SOSActivity extends AppCompatActivity {

    SOSViewModel SOSViewModel;
    ImageView buttonSOS;
    TextView tvSOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SOSViewModel = ViewModelProviders.of(this).get(SOSViewModel.class);
        setContentView(R.layout.activity_sos);

        initializeUI();

    }

    public void initializeUI(){

        buttonSOS = findViewById(R.id.SOSButton);
        tvSOS = findViewById(R.id.tvSOS);
        buttonSOS.setVisibility(View.INVISIBLE);
        tvSOS.setVisibility(View.VISIBLE);

        buttonSOS.setOnClickListener(v -> {
            SOSViewModel.setSOS(true);
            buttonSOS.setVisibility(View.INVISIBLE);
            tvSOS.setVisibility(View.VISIBLE);
        });

    }
}
