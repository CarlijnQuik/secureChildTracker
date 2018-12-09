package com.example.carli.mychildtrackerdisplay.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;

import com.example.carli.mychildtrackerdisplay.Model.DataWrapper;
import com.example.carli.mychildtrackerdisplay.Utils.Constants;
import com.example.carli.mychildtrackerdisplay.Model.Location;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class DisplayViewModel extends BaseViewModel {
    private MutableLiveData<DataWrapper<Location>> currentLocation;
    private MutableLiveData<Boolean> sos;
    private KeyStore keyStore = null;
    private String partnerID;
    private long lastTimestamp;

    public DisplayViewModel(){
        try {
            keyStore = KeyStore.getInstance(Constants.KEYSTORE);
            keyStore.load(null);
            lastTimestamp = 0;
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
                loadSOS();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void loadSOS() {
        if (partnerID == null)
        {
            Log.d(Constants.LOG_TAG, "parnerID in DisplayViewModel null.");
            return;
        }
        FirebaseDatabase.getInstance().getReference(partnerID).child(Constants.DB_ENTRY_SOS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    sos.setValue(dataSnapshot.getValue(Boolean.class));
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
                byte[] iv, cipherText, bytes = {};
                Location location;
                ByteBuffer byteBuffer = null;
                int ivLength = 0;

                try {
                    byteBuffer = ByteBuffer.wrap(Base64.decode(dataSnapshot.getValue(String.class), Base64.DEFAULT));
                    ivLength = byteBuffer.getInt();
                }
                catch (Exception e){
                    currentLocation.setValue(new DataWrapper<>(null, e));
                    return;
                }

                if(ivLength < 12 || ivLength >= 16) { // check input parameter
                    currentLocation.setValue(new DataWrapper<>(null, new InvalidParameterSpecException("Invalid IV length.")));
                    Log.d(Constants.LOG_TAG,"invalid iv length");
                    return;
                }
                iv = new byte[ivLength];
                byteBuffer.get(iv);

                cipherText = new byte[byteBuffer.remaining()];
                byteBuffer.get(cipherText);
                try {
                    cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
                    GCMParameterSpec ivSpec = new GCMParameterSpec(128, iv);
                    cipher.init(Cipher.DECRYPT_MODE, (SecretKey) keyStore.getKey(Constants.KEY_ALIAS, null), ivSpec);
                    bytes = cipher.doFinal(cipherText);
                }
                catch (Exception e){
                    Log.d(Constants.LOG_TAG,"Error decrypting location. Unpair immediately.");
                    currentLocation.setValue(new DataWrapper<>(null, new SecurityException("Error decrypting location: "+e.getMessage())));
                    return;
                }

                try {
                    String json = new String(bytes);
                    ObjectMapper objectMapper = new ObjectMapper();
                    location = objectMapper.readValue(json, Location.class);
                }
                catch (Exception e){
                    Log.d(Constants.LOG_TAG,"Error decoding location from JSON: "+e.getMessage());
                    currentLocation.setValue(new DataWrapper<>(null, new JsonMappingException("Error decoding location from JSON: "+e.getMessage())));
                    return;
                }

                if (location.getTimestamp()>getLastTimestamp()){
                    setLastTimestamp(location.getTimestamp());
                    currentLocation.setValue(new DataWrapper<>(location, null));
                }
                else{
                    currentLocation.setValue(new DataWrapper<>(null, new SecurityException("Received location timestamp is out of flow: possible replay attack.")));
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
    public LiveData<DataWrapper<Location>> getCurrentLocation(){
        if (currentLocation == null)
            currentLocation = new MutableLiveData<>();
        return currentLocation;
    }
    public LiveData<Boolean> getSOS(){
        if (sos == null)
            sos = new MutableLiveData<>();
        return sos;
    }

    private long getLastTimestamp(){
        return this.lastTimestamp;
    }

    private void setLastTimestamp(long val){
        this.lastTimestamp = val;
    }

    public boolean setInterval(Integer val){
        if (partnerID == null)
        {
            Log.d(Constants.LOG_TAG, "partnerID in DisplayViewModel null.");
            return false;
        }
        if ((val!=0 && val!=Constants.SOS_INTERVAL && val<2000) || val>3600000) {
            Log.d(Constants.LOG_TAG, "Interval Value " + val + " out of bounds.");
            return false;
        }
        try {
            Log.d(Constants.LOG_TAG, "Setting the interval value to " + val);
            FirebaseDatabase.getInstance().getReference(partnerID).child(Constants.DB_ENTRY_INTERVAL).setValue(val);
            return true;
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG, "Error setting interval value "+e.getMessage());
            return false;
        }
    }


    public void unPair() {
        try {
            keyStore.deleteEntry(Constants.KEY_ALIAS);
            this.getDatabase().removeValue();
            this.getDatabase().child(Constants.DB_ENTRY_USERTYPE).setValue(Constants.USERTYPE_PARENT);
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG, "Unable to unpair device. Error: "+e.getMessage());
        }

    }

}
