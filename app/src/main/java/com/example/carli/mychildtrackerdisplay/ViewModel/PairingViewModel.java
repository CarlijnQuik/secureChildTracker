package com.example.carli.mychildtrackerdisplay.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.example.carli.mychildtrackerdisplay.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PairingViewModel extends BaseViewModel {

    private Integer nonce1, nonce2;
    private String partnerID;
    private Integer step = 1;
    private SecretKey key;
    private byte[] iv;
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
                key = new SecretKeySpec(bytes, 0, bytes.length, KeyProperties.KEY_ALGORITHM_AES);
                encryptSecCheck();
                setPartnerID(partnerID);
                return true;
            }
            catch (Exception e){
                return false;
            }

        }
        else if (userType.equals(Constants.USERTYPE_PARENT)) {
            if (result.length != 3)
                return false;
            try {
                nonce1 = Integer.decode(result[1]);
                partnerID = result[0];
                iv = Base64.decode(result[2], Base64.DEFAULT);
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
            SecureRandom random = new SecureRandom();
            iv = new byte[16];
            random.nextBytes(iv);
            nonce1 = generateNonce();
            QR = this.getUserID();
            QR += Constants.DATA_DELIM;
            QR += nonce1;
            QR += Constants.DATA_DELIM;
            QR += Base64.encodeToString(iv, Base64.DEFAULT);
        }
        else if (userType.equals(Constants.USERTYPE_PARENT)){
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);
                keyGenerator.init(128);
                key = keyGenerator.generateKey();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            Integer fromnonce = nonce1+1;
            nonce2 = this.generateNonce();
            byte[] bytes = key.getEncoded();
            QR = Base64.encodeToString(bytes, Base64.DEFAULT);
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
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] ciphertext = cipher.doFinal(finalnonce.toString().getBytes());
            setSecurityCheck(new String(Base64.encode(ciphertext,Base64.DEFAULT)));
            Log.d(Constants.LOG_TAG, new String(ciphertext));
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    public void checkPartnersSecurityCheck(){
        DatabaseReference refx = FirebaseDatabase.getInstance().getReference(getPartnerID()).child(Constants.DB_CHILD_SECURITYCHECK);
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
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] bytes = cipher.doFinal(Base64.decode(val.getBytes(), Base64.DEFAULT));
            String noncex = new String(bytes);
            Integer noncey = Integer.parseInt(noncex);
            Log.d(Constants.LOG_TAG, "RECEIVED NONCE: "+noncey);
            Log.d(Constants.LOG_TAG, "Nonce received from DB: "+noncey.toString());
            Log.d(Constants.LOG_TAG, "Nonce saved: "+nonce2.toString());
            if (noncey.equals(nonce2+1))
                return true;
            else
                return false;
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG, "Error decrypting securityCheck. Key: " + key.getEncoded().toString() + "; Exception: "+e.getMessage());
            e.printStackTrace();
        }
        return false;
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
