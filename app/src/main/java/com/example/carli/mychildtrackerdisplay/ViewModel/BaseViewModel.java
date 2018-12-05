package com.example.carli.mychildtrackerdisplay.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.carli.mychildtrackerdisplay.Constants;
import com.example.carli.mychildtrackerdisplay.Model.UserEntry;
import com.example.carli.mychildtrackerdisplay.Repository.FirebaseDatabaseRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class BaseViewModel extends ViewModel {

private FirebaseDatabaseRepository repository;
private FirebaseUser user;
private MutableLiveData<UserEntry> currentUser;

private FirebaseAuth firebaseAuth;
private DatabaseReference database;

    public BaseViewModel() {
        init();
    }

    public void init(){
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            //if (user == null)
                user = firebaseAuth.getCurrentUser();
            if (user != null) {
                database = FirebaseDatabase.getInstance().getReference(user.getUid());
                if (currentUser == null) {
                    currentUser = new MutableLiveData<>();
                    loadUserData();
                }
            }

        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG, e.getMessage());
        }
    }

    public String getUserID(){
        if (user != null)
            return user.getUid();
        else {
            Log.d(Constants.LOG_TAG, "Error getting userid. Variable user is null.");
            return null;
        }
    }
    public void loadUserData(){
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser.setValue(dataSnapshot.getValue(UserEntry.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(Constants.LOG_TAG, "Error getting userType from database.");
            }
        });
    }

    public LiveData<UserEntry> getCurrentUser(){
        if (currentUser == null)
            currentUser = new MutableLiveData<>();
        return currentUser;
    }

    public FirebaseUser getFirebaseUser(){
        return user;
    }

    public void setSecurityCheck(String val){
        database.child(Constants.DB_ENTRY_SECURITYCHECK).setValue(val);
    }
    public void setPartnerID(String val){
        database.child(Constants.DB_ENTRY_PARTNERID).setValue(val);
    }
    public void setSOS(boolean val){
        database.child(Constants.DB_ENTRY_SOS).setValue(val);
    }

    public void setUserType(String val){
        database.child(Constants.DB_ENTRY_USERTYPE).setValue(val);
    }

    public void signOut(){
        firebaseAuth.signOut();
    }

    public DatabaseReference getDatabase(){
        return database;
    }

    public FirebaseAuth getFirebaseAuth(){
        return firebaseAuth;
    }

}
