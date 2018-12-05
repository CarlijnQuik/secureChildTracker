package com.example.carli.mychildtrackerdisplay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.carli.mychildtrackerdisplay.Model.UserEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Source: https://github.com/firebase/quickstart-android/tree/master/auth/app/src/main
 * Controls the log in activity so multiple users can use the app
 */

public class LoginActivity extends LoginProgressDialog implements View.OnClickListener{

    private EditText etEmail;
    private EditText etPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference database;
    private FirebaseUser user;

    String userType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initializeFirebase();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.bLogIn).setOnClickListener(this);
        findViewById(R.id.bCreateAccount).setOnClickListener(this);

    }

    public void initializeFirebase(){
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null)
                    database = FirebaseDatabase.getInstance().getReference(user.getUid());

            }
        };

    }

    public void onBackPressed() {
        moveTaskToBack(true);

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);

        }

    }

    // decide what clicking a button does
    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.bCreateAccount) {
            createAccount(etEmail.getText().toString(), etPassword.getText().toString());

        } else if (i == R.id.bLogIn) {
            logIn(etEmail.getText().toString(), etPassword.getText().toString());

        }

    }

    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // start create user with email
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // if sign in fails display a message, if sign in succeeds notify auth state listener
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.auth_failed, LENGTH_SHORT).show();

                        }
                        else if (task.isSuccessful()) {
                            checkUserType();
                        }

                        hideProgressDialog();
                    }
                });

    }

    private void forwardToPairing(){
        Intent intent = new Intent(this, PairingXActivity.class);
        startActivity(intent);
    }

    private void logIn(String email, String password) {
        if (!validateForm()) {
            return;

        }

        showProgressDialog();

        // start log in with email
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message, if sign in succeeds notify auth state listener
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.auth_failed, LENGTH_SHORT).show();

                        } else if (task.isSuccessful()) {
                            getValueFromDatabase();

                        }

                        hideProgressDialog();

                    }
                });

    }

    private boolean validateForm() {
        boolean valid = true;

        String email = etEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Required.");
            valid = false;

        } else {
            etEmail.setError(null);

        }

        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Required.");
            valid = false;

        } else {
            etPassword.setError(null);

        }

        return valid;

    }

    public void checkUserType(){
        // ask user whether the user is a parent or child
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // parent button clicked
                        userType = "parent";

                        database.child("userType").setValue(userType);

                        forwardToPairing();

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //child button clicked
                        userType = "child";

                        database.child("userType").setValue(userType);

                        forwardToPairing();

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you a parent or a child?").setPositiveButton("Parent", dialogClickListener)
                .setNegativeButton("Child", dialogClickListener).show();

    }

    public void checkUser(){
        if (userType.equals(Constants.USERTYPE_PARENT)){
            Intent intent = new Intent(this, DisplayXActivity.class);
            Log.d(Constants.LOG_TAG,"Starting DisplayXActivity");
            startActivity(intent);
        }
        if (userType.equals(Constants.USERTYPE_CHILD)){
            Intent intent = new Intent(this, TrackerActivity.class);
            startActivity(intent);
        }

    }

    public void forwardParent(){
        //UserEntry ue = new UserEntry();
        //ue.setInterval(5);
        //ue.setSecurity_check("blabla");
        //database.setValue(ue);

        //FirebaseRepository firebaseRepository = new FirebaseRepository();
        //UserEntry userEntry = firebaseRepository.getUserEntry(); ////
        Intent intent = new Intent(this, DisplayXActivity.class);
        startActivity(intent);

    }

    public void forwardChild(){

        Intent intent = new Intent(this, TrackerActivity.class);
        startActivity(intent);


    }

    public void getValueFromDatabase(){
        Log.d("current user", user.getUid());
        //database = FirebaseDatabase.getInstance().getReference(user.getUid());
        database.child("userType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userType = dataSnapshot.getValue(String.class);
                checkUser();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
