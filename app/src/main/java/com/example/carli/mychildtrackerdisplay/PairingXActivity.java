package com.example.carli.mychildtrackerdisplay;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.carli.mychildtrackerdisplay.ViewModel.PairingViewModel;

public class PairingXActivity extends AppCompatActivity {
    private PairingViewModel pairingViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing_x);
        pairingViewModel = ViewModelProviders.of(this).get(PairingViewModel.class);
        pairingViewModel.getUserType().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String userType) {
                Log.d("MCT", "UserType:"+userType);
            }
        });

    }
}
