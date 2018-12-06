package com.example.carli.mychildtrackerdisplay.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.example.carli.mychildtrackerdisplay.Constants;
import com.example.carli.mychildtrackerdisplay.Model.Location;
import com.example.carli.mychildtrackerdisplay.Model.UserEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class DisplayViewModel extends BaseViewModel {
    private MutableLiveData<Location> currentLocation;
    private KeyStore keyStore = null;
    private String partnerID;
    private long lastTimestamp = 0;

    public DisplayViewModel(){
        try {
            keyStore = KeyStore.getInstance(Constants.KEYSTORE);
            keyStore.load(null);
            loadPartnerId();
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG,"Error initiating " + Constants.KEYSTORE + ": "+e.getMessage());
        }
    }
    private void loadPartnerId(){
        this.getDatabase().child(Constants.DB_ENTRY_PARTNERID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                partnerID = dataSnapshot.getValue(String.class);
                loadLocations();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    public void loadLocations(){
        if (partnerID == null)
        {
            Log.d(Constants.LOG_TAG, "parnerID in DisplayViewModel null.");
            return;
        }
        FirebaseDatabase.getInstance().getReference(partnerID).child(Constants.DB_ENTRY_DATA).limitToLast(20).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Cipher cipher = null;
                ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.decode(dataSnapshot.getValue(String.class), Base64.DEFAULT));
                int ivLength = byteBuffer.getInt();
                if(ivLength < 12 || ivLength >= 16) { // check input parameter
                    Log.d(Constants.LOG_TAG,"invalid iv length");
                    return;
                }
                byte[] iv = new byte[ivLength];
                byteBuffer.get(iv);
                byte[] cipherText = new byte[byteBuffer.remaining()];
                byteBuffer.get(cipherText);
                try {
                    cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
                    GCMParameterSpec ivSpec = new GCMParameterSpec(128, iv);
                    cipher.init(Cipher.DECRYPT_MODE, (SecretKey) keyStore.getKey(Constants.KEY_ALIAS, null), ivSpec);
                    byte[] bytes = cipher.doFinal(cipherText);

                    String json = new String(bytes);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Location location = objectMapper.readValue(json, Location.class);
                    if (location.getTimestamp()>getLastTimestamp()){
                        setLastTimestamp(location.getTimestamp());
                        currentLocation.setValue(location);
                    }
                }
                catch (Exception e){
                    Log.d(Constants.LOG_TAG,"Error decrypting location: "+e.getMessage());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public LiveData<Location> getCurrentLocation(){
        if (currentLocation == null)
            currentLocation = new MutableLiveData<>();
        return currentLocation;
    }

    private long getLastTimestamp(){
        return this.lastTimestamp;
    }

    private void setLastTimestamp(long val){
        this.lastTimestamp = val;
    }

}
