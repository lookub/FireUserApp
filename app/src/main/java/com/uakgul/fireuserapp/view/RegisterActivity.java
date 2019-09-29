package com.uakgul.fireuserapp.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uakgul.fireuserapp.R;
import com.uakgul.fireuserapp.model.User;
import com.uakgul.fireuserapp.utils.Utils;

import java.io.IOException;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "Register";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    DatabaseReference databaseReferenceUsers;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Context appContext;

    Utils utils;

    EditText etUserName;
    EditText etPassword;
    EditText etNameSurname;
    EditText etAge;
    EditText etCity;
    ImageView ivProfile;

    String imgProfileUrl = "gs://fireuserapp.appspot.com/";
    Uri filePath;
    final int PICK_IMAGE_REQUEST = 71;

    Button btnRegister;

    TextView tvUserID;


    String fireUserID;
    String fireUserName;
    String fireUserPass;

    User fireUser;

    boolean userStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        declareAndInitializeVariables();



    }//end of onCreate


    private void declareAndInitializeVariables() {

        appContext = RegisterActivity.this;

        utils = new Utils( appContext );

        ivProfile = findViewById(R.id.imageProfile);

        tvUserID = findViewById(R.id.tvUserID);

        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        etNameSurname = findViewById(R.id.etNameSurname);
        etAge = findViewById(R.id.etAge);
        etCity = findViewById(R.id.etCity);

        btnRegister = findViewById(R.id.buttonRegister);

        ivProfile.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        setFireBaseAuthAndUser();

        firebaseStorage  = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

    }//end of declareAndInitializeVariables


    @Override
    public void onClick(View view) {

        if( view == btnRegister ) {

            fireUserName = etUserName.getText().toString();
            fireUserPass = etPassword.getText().toString();

            if (fireUserName.isEmpty() || fireUserPass.isEmpty()) {

                utils.customToastMessage("Please Enter mail and password!", "Fail", "S");

            } else {
                createUser( fireUserName, fireUserPass );
            }

        }else if( view == ivProfile ) {

            if( userStatus ){

                chooseImage();

            }else{
                utils.customToastMessage("Plase before create user", "Fail", "S");
            }

        }

    }//end of onClick

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    private void createUser(final String userName, final String userPassword) {

        firebaseAuth.createUserWithEmailAndPassword(userName, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    utils.customToastMessage("Successfully Create User : " + userName + " : " + userPassword, "OK", "S");

                    setFireBaseAuthAndUser();

                    if( firebaseUser == null ){

                        utils.customToastMessage("Failed! Create User : getting Some error firebaseUser is NULL " , "Fail", "L");

                    }else{

                        fireUserID = firebaseUser.getUid();

                        tvUserID.setText( fireUserID );

                        getFireDB();
                    }


                }else{

                    utils.customToastMessage("Failed! Create User : " + task.getException().getMessage() , "Fail", "L");
                }
            }
        });

    }//end of createUser


    private void setFireBaseAuthAndUser() {
        try{
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser(); // authenticated user
        }catch (Exception e){
            Log.e( TAG, "RegisterActivity.setFireBaseAuthAndUser() : Exception : " + e.getMessage() );
            utils.customToastMessage("Failed! getCurrentUser : firebaseUser is NULL : " + e.getMessage(), "Fail", "L");
        }
    }//end of setFireBaseAuthAndUser

    private void getFireDB() {

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("users").child( fireUserID );

        saveUserToFireDB();

    }//end of getFireDB



    private void saveUserToFireDB() {

        //TODO set profile image
        imgProfileUrl = imgProfileUrl + fireUserID;
        User user = new User( etUserName.getText().toString(),
                                etPassword.getText().toString(),
                                etNameSurname.getText().toString(),
                                etAge.getText().toString(),
                                etCity.getText().toString(),
                                imgProfileUrl );

        databaseReferenceUsers.setValue( user )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        utils.customToastMessage("Successfully Saved User values", "OK", "S");
                        userStatus = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        utils.customToastMessage("Save Failed! " + e.getMessage(), "Fail", "L");
                        Log.e( TAG, "RegisterActivity.saveUserToFireDB()... Save Failed! : " + e.getMessage() );
                    }
                });

    }//end of saveUserToFireDB



    private void updateUserAfterImage() {

        databaseReferenceUsers.child( "image").setValue( imgProfileUrl )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        utils.customToastMessage("databaseReferenceUsers.child( \"image\")", "OK", "S");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        utils.customToastMessage("databaseReferenceUsers.child( \\\"image\\\")\" Save Failed! " + e.getMessage(), "Fail", "L");
                        Log.e( TAG, "RegisterActivity.updateUserAfterImage()... Save Failed! : " + e.getMessage() );
                    }
                });

    }//end of updateUserAfterImage



    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }//end of chooseImage

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
                filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivProfile.setImageBitmap(bitmap);

                uploadImage();

            } catch (IOException e) {
                Log.e( TAG, "RegisterActivity.onActivityResult()... IOException! " + e.getMessage() );
            }
        }
    }//end of onActivityResult

    private void uploadImage() {

        if (filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loading...");
            progressDialog.show();

            String randomUID = UUID.randomUUID().toString();
//            StorageReference ref = storageReference.child("images/" + randomUID );

            final StorageReference ref = storageReference.child( fireUserID );

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            utils.customToastMessage("Image Uploaded ", "OK", "L");

//                            updateUserAfterImage();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            utils.customToastMessage("Image Upload Failed! " + e.getMessage(), "Fail", "L");
                            Log.e( TAG, "RegisterActivity.uploadImage()... Image Upload Failed! " + e.getMessage() );
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }//end of uploadImage


}
