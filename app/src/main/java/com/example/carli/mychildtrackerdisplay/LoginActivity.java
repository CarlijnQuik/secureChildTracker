package com.example.carli.mychildtrackerdisplay;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initializeFirebase();

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        findViewById(R.id.bLogIn).setOnClickListener(this);
        findViewById(R.id.bCreateAccount).setOnClickListener(this);

    }

    public void initializeFirebase(){
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // user is logged in
                    forwardUser();
                    Toast.makeText(LoginActivity.this, "Logged in", LENGTH_SHORT).show();
                }

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
                        } else if (task.isSuccessful()) {
                            forwardUser();
                        }

                        hideProgressDialog();
                    }
                });

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
                            forwardUser();
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

    public void forwardUser() {
        Intent intent = new Intent(this, DisplayActivity.class);
        startActivity(intent);
        //finish();

    }

    private void signOut(){
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);

    };

}

