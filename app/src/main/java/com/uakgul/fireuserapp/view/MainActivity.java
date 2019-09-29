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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uakgul.fireuserapp.R;
import com.uakgul.fireuserapp.model.User;
import com.uakgul.fireuserapp.utils.PrefOperations;
import com.uakgul.fireuserapp.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static String TAG = "Main";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    DatabaseReference databaseReferenceUsers;


    ValueEventListener valueEventListener;

    String fireUserID;
    String fireUserName;
    String fireUserPass;

    User fireUser;

    Context appContext;

    Utils utils;
    PrefOperations pref;

    TextView tvUserID;

    EditText etUserName;
    EditText etPassword;
    EditText etNameSurname;
    EditText etAge;
    EditText etCity;
    ImageView ivProfile;
    String imgProfileUrl;
    Uri filePath;
    final int PICK_IMAGE_REQUEST = 71;

    Button btnUpdate;
    Button btnRemove;
    Button btnLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeIntentVariables();

        declareAndInitializeVariables();



        getFireDB();
        dbListener();

        getAuthenticatedFireUser();

        getStorageAndReference();


    }//end of onCreate

    private void initializeIntentVariables() {
        fireUserID   = getIntent().getStringExtra("uID");
        fireUserName = getIntent().getStringExtra("userName");
        fireUserPass = getIntent().getStringExtra("userPassword");
    }

    private void declareAndInitializeVariables() {

        appContext = MainActivity.this;

        utils = new Utils( appContext );

        pref = new PrefOperations( appContext );

        ivProfile = findViewById(R.id.imageProfile);

        tvUserID = findViewById(R.id.tvUserID);

        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        etNameSurname = findViewById(R.id.etNameSurname);
        etAge = findViewById(R.id.etAge);
        etCity = findViewById(R.id.etCity);

        btnUpdate = findViewById(R.id.buttonUpdate);
        btnRemove = findViewById(R.id.buttonRemove);
        btnLogOut = findViewById(R.id.buttonSignOut);


        ivProfile.setOnClickListener(this);
        ivProfile.setOnLongClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        btnLogOut.setOnClickListener(this);



        tvUserID.setText( "userID : " + fireUserID );

        etUserName.setText( fireUserName );
        etPassword.setText( fireUserPass );

    }

    @Override
    public void onClick(View view) {

        if( view == ivProfile ) {

            getProfileImage();

        }else if( view == btnUpdate ) {

            updateUserToFireDB();

        }else if( view == btnRemove ) {

            removeFireUser();

        }else if( view == btnLogOut ) {

            firebaseAuth.signOut();
            onBackPressed();
        }


    }//end of onClick



    @Override
    public boolean onLongClick(View view) {
        if( view == ivProfile ) {
            chooseImage();
        }
        return false;
    }


    private void getAuthenticatedFireUser() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser(); // authenticated user

    }//end of onCreate



    private void getFireDB() {

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("users").child( fireUserID );

    }//end of getFireDB

    private void dbListener() {

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{

                    fireUser = dataSnapshot.getValue( User.class);

                    getFetchedUserValues();

                }catch(Exception e){
                    //Log the exception and the key
                    dataSnapshot.getKey();
                    Log.e( TAG, "MainActivity.dbListener().onDataChange() : DatabaseException : " + e.getMessage() );
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e( TAG, "MainActivity.dbListener().onCancelled() : DatabaseError : " + databaseError );
            }
        };
        databaseReferenceUsers.addValueEventListener( valueEventListener );

    }//end of dbListener



    private void getStorageAndReference() {

        firebaseStorage  = FirebaseStorage.getInstance();
//        storageReference = firebaseStorage.getReference();

    }//end of getStorageAndReference



    public void getFetchedUserValues() {

        etUserName.setText( fireUser.getUsername() );
        etPassword.setText( fireUser.getPassword() );
        etNameSurname.setText( fireUser.getNameSurname() );
        etAge.setText( fireUser.getAge() );
        etCity.setText( fireUser.getCity() );

        imgProfileUrl = fireUser.getImage();

        if( imgProfileUrl.isEmpty() ){
            getDefaultImageURL();
        }else{
            getProfileImage();
        }

    }//end of getFetchedUserValues



    private void getDefaultImageURL() {
        imgProfileUrl = "gs://fireuserapp.appspot.com/" + fireUserID;
    }

    private void getProfileImage() {

        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl( imgProfileUrl );
//        storageReference = firebaseStorage.getReferenceFromUrl( imgProfileUrl );

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with( appContext )
                        .load( uri )
                        .centerInside()
                        .transition( DrawableTransitionOptions.withCrossFade() )
//                        .apply( RequestOptions.bitmapTransform(new RoundedCorners(10 )) )
                        .apply( RequestOptions.placeholderOf(R.drawable.profile) ) // colorPrimary
                        .into( ivProfile );

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.customToastMessage("Profile Image Download Failed! " + e.getMessage(), "Fail", "L");
                Log.e( TAG, "MainActivity.getProfileImage().getDownloadUrl().onFailure()... Download Failed! : " + e.getMessage() );
            }
        });

    }


    private void updateUserToFireDB() {

        String changePassword = etPassword.getText().toString();
        String changeUserName = etUserName.getText().toString();

        if( changeUserName.isEmpty() || changePassword.isEmpty() ||
            !changeUserName.contains("@") || !changeUserName.contains(".") ||  changePassword.length() <6 ){

            utils.customToastMessage("Please enter a valid username and password!", "Fail", "S");

        }else{

            if( !fireUserName.equalsIgnoreCase( changeUserName ) ){
                changeMailUpdate( changeUserName );
            }

            if( !fireUserPass.equalsIgnoreCase( changePassword ) ){
                changePasswordUpdate( changePassword );
            }

            User user = new User( changeUserName, changePassword,
                                    etNameSurname.getText().toString(),
                                    etAge.getText().toString(),
                                    etCity.getText().toString(),
                                    imgProfileUrl );

            databaseReferenceUsers.setValue( user )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            utils.customToastMessage("Successfully Updated User values", "OK", "S");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            utils.customToastMessage("Update Failed! " + e.getMessage(), "Fail", "L");
                            Log.e( TAG, "MainActivity.updateUserToFireDB()... Save Failed! : " + e.getMessage() );
                        }
                    });

        }

        dbListener();

    }//end of saveUserToFireDB







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
                Log.e( TAG, "MainActivity.onActivityResult()... IOException! " + e.getMessage() );
            }
        }
    }//end of onActivityResult

    private void uploadImage() {

        if (filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loading...");
            progressDialog.show();

            storageReference.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            utils.customToastMessage("Image Uploaded ", "OK", "L");

                            updateUserAfterImage();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            utils.customToastMessage("Image Upload Failed! " + e.getMessage(), "Fail", "L");
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

    private void updateUserAfterImage() {

        getDefaultImageURL();

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





    private void changePasswordUpdate(final String userPasswordChange) {

        firebaseUser.updatePassword( userPasswordChange )
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            utils.customToastMessage("Password has change : -> " + userPasswordChange , "OK", "S");
                        } else {
                            utils.customToastMessage("Failed! Password not change : " + task.getException().getMessage() , "Fail", "L");
                        }
                    }
                });

    }//end of changePasswordUpdate

    private void changeMailUpdate(final String userNameChange ) {

            firebaseUser.updateEmail( userNameChange )
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    utils.customToastMessage("MailAddress has change : -> " + userNameChange , "OK", "S");
                                } else {
                                    utils.customToastMessage("Failed! MailAddress not change : " + task.getException().getMessage() , "Fail", "L");
                                }
                            }
                        });
    }//end of changeMailUpdate


    private void setProfileImage() {

        Uri file = Uri.fromFile(new File("path/to/images/burn.png"));
//        StorageReference riversRef = storageRef.child("images/rivers.jpg");
        StorageReference riversRef = storageReference.child("users").child( fireUserID );

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        Log.e( TAG, "MainActivity.setProfileImage().getUploadFile().onFailure()... Upload Failed! : " + e.getMessage() );
                    }
                });
    }





    private void removeFireUser() {

        databaseReferenceUsers.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        AuthCredential credential = EmailAuthProvider.getCredential(fireUserName, fireUserPass );

                        firebaseUser.reauthenticate( credential ).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                firebaseUser.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    utils.customToastMessage("User Account and Values Deleted!", "OK", "S");

                                                    onBackPressed();
                                                }
                                            }
                                        });
                            }
                        });

                    }//end of onSuccess
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        utils.customToastMessage("Remove Failed! " + e.getMessage(), "Fail", "L");
                        Log.e( TAG, "MainActivity.removeFireUser()... Remove Failed! : " + e.getMessage() );
                    }
                });
    }//ond of removeFireUser

}
