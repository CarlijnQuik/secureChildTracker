package com.example.carli.mychildtrackerdisplay.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.util.Base64;
import android.util.Log;

import com.example.carli.mychildtrackerdisplay.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PairingViewModel extends BaseViewModel {

    private Integer nonce1, nonce2;
    private String partnerID;
    private Integer step = 1;
    private KeyStore keyStore = null;
    private MutableLiveData<String> partnersSecurityCheck;

    public Integer getNonce1() {
        return nonce1;
    }

    public void setNonce1(Integer nonce1) {
        this.nonce1 = nonce1;
    }

    public void incStep(){
        this.step++;
    }

    public Integer getStep(){
        return step;
    }

    public Integer getNonce2() {
        return nonce2;
    }

    public void setNonce2(Integer nonce2) {
        this.nonce2 = nonce2;
    }

    public Integer generateNonce(){
        SecureRandom random = new SecureRandom();
        return random.nextInt();
    }

    public PairingViewModel(){
        try {
            keyStore = KeyStore.getInstance(Constants.KEYSTORE);
            keyStore.load(null);
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG,"Error initiating " + Constants.KEYSTORE + ": "+e.getMessage());
        }
    }
    private void saveKeyIntoKeystore(byte[] bytes){
        try {
            keyStore.setEntry(
                    Constants.KEY_ALIAS,
                    new KeyStore.SecretKeyEntry(new SecretKeySpec(bytes, 0, bytes.length, KeyProperties.KEY_ALGORITHM_AES)),
            new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());

        } catch (Exception ex) {
            Log.d(Constants.LOG_TAG, ex.getMessage());
        }
    }

    public boolean processQR(String userType, String data) {
        String[] result = data.split(Constants.DATA_DELIM);

        if (userType.equals(Constants.USERTYPE_CHILD)){
            if (result.length != 4)
                return false;
            try{
                Integer fromtononce = Integer.decode(result[1]);
                if (!fromtononce.equals(nonce1+1))
                    return false;
                nonce2 = Integer.decode(result[2]);
                partnerID = result[3];
                byte[] bytes = Base64.decode(result[0], Base64.DEFAULT);

                saveKeyIntoKeystore(bytes);
                encryptSecCheck();
                setPartnerID(partnerID);
                return true;
            }
            catch (Exception e){
                return false;
            }

        }
        else if (userType.equals(Constants.USERTYPE_PARENT)) {
            if (result.length != 2)
                return false;
            try {
                nonce1 = Integer.decode(result[1]);
                partnerID = result[0];
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    public String generateQR(String userType){
        String QR = new String();
        if (userType.equals(Constants.USERTYPE_CHILD)) {
            nonce1 = generateNonce();
            QR = this.getUserID();
            QR += Constants.DATA_DELIM;
            QR += nonce1;
        }
        else if (userType.equals(Constants.USERTYPE_PARENT)){
            SecretKey tempkey = null;
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);
                keyGenerator.init(128);
                tempkey = keyGenerator.generateKey();
                saveKeyIntoKeystore(tempkey.getEncoded());

            } catch (Exception e) {
                Log.d(Constants.LOG_TAG, "Error generating/saving the key. "+e.getMessage());
            }
            Integer fromnonce = nonce1+1;
            nonce2 = this.generateNonce();
            try {
                byte[] bytes = tempkey.getEncoded();
                // TODO destroy tempkey
                QR = Base64.encodeToString(bytes, Base64.DEFAULT);
            }
            catch (Exception e){
                Log.d(Constants.LOG_TAG, "Error getting key from keystore.");
            }
            QR = QR.replaceAll("\n", "");
            QR += Constants.DATA_DELIM;
            QR += fromnonce.toString();
            QR += Constants.DATA_DELIM;
            QR += nonce2.toString();
            QR += Constants.DATA_DELIM;
            QR += this.getUserID();
            Log.d(Constants.LOG_TAG, "Parent->Child key: "+QR);
        }
        return QR;
    }

    private void encryptSecCheck() {
        Cipher cipher = null;
        try {
            Integer finalnonce = nonce2+1;
            Log.d(Constants.LOG_TAG, "FINALNONCE: "+finalnonce);
            cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, (SecretKey) keyStore.getKey(Constants.KEY_ALIAS, null));
            byte[] iv = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(finalnonce.toString().getBytes());
            Log.d(Constants.LOG_TAG, "TOENCRYPT: "+finalnonce.toString().getBytes().toString());
            setSecurityCheck(Base64.encodeToString(iv,Base64.DEFAULT)+Constants.DATA_DELIM+Base64.encodeToString(ciphertext,Base64.DEFAULT));
            Log.d(Constants.LOG_TAG, new String(ciphertext));
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG,"Error encrypting securityCheck: "+e.getMessage());
            e.printStackTrace();
        }

    }
    public void checkPartnersSecurityCheck(){
        DatabaseReference refx = FirebaseDatabase.getInstance().getReference(getPartnerID()).child(Constants.DB_ENTRY_SECURITYCHECK);
        refx.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                partnersSecurityCheck.setValue(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    public boolean decryptSecCheck(String val){
        if (val == null)
            return false;

        Cipher cipher = null;
        String[] result = val.split(Constants.DATA_DELIM);
        if (result.length != 2){
            Log.d(Constants.LOG_TAG, "Incorrect securityCheck length.");
            return false;
        }
        try {
            cipher = Cipher.getInstance(Constants.CIPHER_TYPE);
            GCMParameterSpec ivSpec = new GCMParameterSpec(128, Base64.decode(result[0], Base64.DEFAULT));
            cipher.init(Cipher.DECRYPT_MODE, (SecretKey) keyStore.getKey(Constants.KEY_ALIAS, null), ivSpec);
            byte[] bytes = cipher.doFinal(Base64.decode(result[1].getBytes(), Base64.DEFAULT));
            String noncex = new String(bytes);
            Integer noncey = Integer.parseInt(noncex);
            Log.d(Constants.LOG_TAG, "RECEIVED NONCE: "+noncey);
            Log.d(Constants.LOG_TAG, "Nonce received from DB: "+noncey.toString());
            Log.d(Constants.LOG_TAG, "Nonce saved: "+nonce2.toString());
            if (noncey.equals(nonce2+1)) {
                setPartnerID(partnerID);
                return true;
            }
            else
                return false;
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG, "Error decrypting securityCheck. probably bad value in DB." + "; Exception: "+e.getMessage());
            return false;
        }
    }

    public String getPartnerID(){
        return partnerID;
    }

    public LiveData<String> getPartnersSecurityCheck(){
        if (partnersSecurityCheck == null)
            partnersSecurityCheck = new MutableLiveData<>();
        return partnersSecurityCheck;
    }
}
