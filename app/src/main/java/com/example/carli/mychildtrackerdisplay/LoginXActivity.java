package com.example.carli.mychildtrackerdisplay;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.carli.mychildtrackerdisplay.Model.UserEntry;
import com.example.carli.mychildtrackerdisplay.ViewModel.LoginViewModel;

import static android.widget.Toast.LENGTH_SHORT;

public class LoginXActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private LoginViewModel loginViewModel;
    private String userType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        if (loginViewModel.getFirebaseUser() != null)   // if user is logged in, redirect him directly
            onLoginSuccessful();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.bLogIn).setOnClickListener(view -> logIn());
        findViewById(R.id.bCreateAccount).setOnClickListener(view -> selectUserTypeAndCreate());
    }

    private void forwardTo(Class classname){
        try {
            Intent intent = new Intent(this, classname);
            startActivity(intent);
        }
        catch (Exception e){
            Log.d(Constants.LOG_TAG,"Class of name "+classname.toString()+" not found, couldnt start activity." + e.getMessage());
        }
    }

    private void logIn(){
        if (!validateForm())
            return;

        loginViewModel.getFirebaseAuth().signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(LoginXActivity.this, R.string.auth_failed, LENGTH_SHORT).show();
            }
            else if (task.isSuccessful()) {
                loginViewModel.init();
                onLoginSuccessful();

            }
        });
    }

    private void onLoginSuccessful(){
        loginViewModel.getCurrentUser().observe(this, new Observer<UserEntry>() {
            @Override
            public void onChanged(@Nullable UserEntry userEntry) {
                if (userEntry != null) {
                    try {
                        if (userEntry.getUserType() != null) {
                            loginViewModel.getCurrentUser().removeObserver(this);
                            if (userEntry.getPartnerID() != null) {
                                if (userEntry.getUserType().equals(Constants.USERTYPE_PARENT))
                                    forwardTo(DisplayXActivity.class);
                                else if (userEntry.getUserType().equals(Constants.USERTYPE_CHILD))
                                    forwardTo(TrackerActivity.class);
                            } else {   // forward to Pairing
                                forwardTo(PairingXActivity.class);
                            }
                        }
                    } catch (Exception e) {
                        Log.d(Constants.LOG_TAG, "Exception " + e.getMessage());
                    }
                }
                else{
                    loginViewModel.getCurrentUser().removeObserver(this);
                    loginViewModel.signOut();
                }
            }
        });
    }

    public void selectUserTypeAndCreate(){
        if (!validateForm())
            return;


        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    // parent button clicked
                    userType = Constants.USERTYPE_PARENT;
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //child button clicked
                    userType = Constants.USERTYPE_CHILD;
                    break;
            }
            createAccount(userType);
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.parent_or_child).setPositiveButton(R.string.parent, dialogClickListener)
                .setNegativeButton(R.string.child, dialogClickListener).show();
    }

    private void createAccount(String userType){
        if (!validateForm())
            return;
        if (userType == null)
            return;
        if (!(userType.equals(Constants.USERTYPE_CHILD) || userType.equals(Constants.USERTYPE_PARENT)))
            return;

        loginViewModel.getFirebaseAuth().createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(LoginXActivity.this, R.string.auth_failed, LENGTH_SHORT).show();
                    }
                    else if (task.isSuccessful()) {
                        loginViewModel.init();
                        loginViewModel.setUserType(userType);
                        forwardTo(PairingXActivity.class);
                    }
                });
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = etEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(""+R.string.required);
            valid = false;
        } else {
            etEmail.setError(null);
        }

        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etPassword.setError(""+R.string.required);
            valid = false;

        } else {
            etPassword.setError(null);
        }

        return valid;
    }




}
