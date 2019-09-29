package com.uakgul.fireuserapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.uakgul.fireuserapp.R;
import com.uakgul.fireuserapp.utils.PrefOperations;
import com.uakgul.fireuserapp.utils.Utils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "Login";

    Context appContext;

    Utils utils;
    PrefOperations pref;

    EditText etUser;
    EditText etPass;

    Button btnLogin;
    Button btnRegister;

    CheckBox cbRememberMe;

    private String userName;
    private String userPassword;


    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseAuth.AuthStateListener authListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        declareAndInitializeVariables();

        getRememberedUser();

        getAuthenticatedFireUser();

        if(firebaseUser != null) { // check user session

            utils.customToastMessage("Welcome back firebaseUser " + firebaseUser.getEmail() + " - " + firebaseUser.getDisplayName() , "OK", "S");

            startActivity(new Intent(LoginActivity.this, MainActivity.class)
                        .putExtra("uID",firebaseUser.getUid() )
                        .putExtra("userName",firebaseUser.getEmail() )
                        .putExtra("userPassword","authenticatedPass"));

        }else{
            Log.d( TAG, "LoginActivity.getAuthenticatedFireUser() : firebaseUser is null" );
        }

    }//end of onCreate


    private void getRememberedUser() {

        if( pref.isAnyRememberUser() ){
            userName = pref.getPrefUserName();
            userPassword = pref.getPrefPassword();
            etUser.setText( userName );
            etPass.setText( userPassword );
            cbRememberMe.setChecked(true);
            utils.customToastMessage("Welcome back " + userName, "OK", "S");
        }

    }//end of onCreate


    private void getAuthenticatedFireUser() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser(); // authenticated user

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) { // when auth state change
                    Log.d( TAG, "LoginActivity.getAuthenticatedFireUser() : onAuthStateChanged use is NULL" );
                }
            }
        };



    }//end of onCreate


    private void declareAndInitializeVariables() {

        appContext = LoginActivity.this;

        utils = new Utils( appContext );

        pref = new PrefOperations( appContext );


        etUser = findViewById(R.id.editTextUserName);
        etPass = findViewById(R.id.editTextPassword);

        btnLogin = findViewById(R.id.buttonLogin);
        btnRegister = findViewById(R.id.buttonRegister);

        cbRememberMe = findViewById(R.id.checkBox);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        cbRememberMe.setOnClickListener(this);


    }//end of declareAndInitializeVariables



    @Override
    public void onClick(View view) {

        if( view == btnLogin ) {

            userName = etUser.getText().toString();
            userPassword = etPass.getText().toString();

            if (userName.isEmpty() || userPassword.isEmpty()) {
                utils.customToastMessage("Please enter user information!", "Fail", "S");
            } else {

                if (cbRememberMe.isChecked()) {
                    pref.setUserPreferences( userName , userPassword );
                }else{
                    pref.clearPreferences();
                }

                validateAndSign( userName , userPassword );
            }

        }else if( view == btnRegister ) {

            etUser.setText("");
            etPass.setText("");
            cbRememberMe.setChecked(false);

            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

        }else if( view == cbRememberMe ) {

            boolean checked = ((CheckBox) view).isChecked();

            userName = etUser.getText().toString();
            userPassword = etPass.getText().toString();

            if (checked) {

                utils.customToastMessage("will be remembered " + userName + " / " + userPassword, "OK", "S");

                if (userName.isEmpty() || userPassword.isEmpty()) {

                    pref.setUserPreferences( userName , etPass.getText().toString() );
                }

            }else{
                utils.customToastMessage("will not be remembered " + userName + " / " + userPassword, "Fail", "S");
                pref.clearPreferences();
            }

        }

    }//end of onClick


    private void validateAndSign(final String userName, final String userPassword) {

        firebaseAuth.signInWithEmailAndPassword(userName, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    getAuthenticatedFireUser();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                .putExtra("uID",firebaseUser.getUid()).putExtra("userName",userName).putExtra("userPassword",userPassword));
                }else{
                    utils.customToastMessage("Login Failed! " + userName + " : " + task.getException().getMessage(), "Fail", "L");
                }
            }
        });

    }//end of validateAndSign



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        firebaseAuth.signOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            firebaseAuth.removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseAuth.signOut();
    }


}
