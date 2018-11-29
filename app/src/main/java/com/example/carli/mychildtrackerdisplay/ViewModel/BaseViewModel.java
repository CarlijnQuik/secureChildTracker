package com.example.carli.mychildtrackerdisplay.ViewModel;

import android.animation.Animator;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.carli.mychildtrackerdisplay.Repository.FirebaseDatabaseRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.List;

public class BaseViewModel extends ViewModel {

private FirebaseDatabaseRepository repository;
private FirebaseUser user;


private MutableLiveData<String> userType;
private FirebaseAuth firebaseAuth;
private DatabaseReference database;

    public BaseViewModel() {
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            if (user == null)
                user = firebaseAuth.getCurrentUser();
            if (user != null) {
                database = FirebaseDatabase.getInstance().getReference(user.getUid());
                loadUserType();
            }
        }
        catch (Exception e){
            Log.d("MCT", e.getMessage());
        }
    }

    public String getUserID(){
        if (user != null)
            return user.getUid();
        else {
            Log.d("MCT", "Error getting userid. Variable user is null.");
            return null;
        }
    }
    public void loadUserType(){
         database.child("userType").
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userType.setValue(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MCT", "Error getting userType from database.");
            }
        });
    }

    public LiveData<String> getUserType(){
        if (userType == null) {
            userType = new MutableLiveData<String>();
        }
        return userType;
    }



}
