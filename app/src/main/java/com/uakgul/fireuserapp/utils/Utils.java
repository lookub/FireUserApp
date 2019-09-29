package com.uakgul.fireuserapp.utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.uakgul.fireuserapp.R;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("Registered")
public class Utils extends AppCompatActivity {

    private Context appCoontext;


    public Utils(Context context) {
        appCoontext = context;
    }



    public static int versionCode = 0;      // for control device
    public static String versionName = "";  // for control device

    public void getVersionCodeAndName() {

        versionCode =  Build.VERSION.SDK_INT;
        versionName =  Build.VERSION.CODENAME;
        Log.i("Utils", "API versionCode : " + versionCode  + " .. API versionName : " + versionName  + " .. " );

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Log.i("Utils", "API versionCode : " + Build.VERSION.BASE_OS );
//        }

        Log.i("Utils", "API version : " + Build.VERSION.RELEASE  );

    }

    public static int getVersionCode() {
        return versionCode;
    }

    public static void setVersionCode(int versionCode) {
        Utils.versionCode = versionCode;
    }

    public static String getVersionName() {
        return versionName;
    }

    public static void setVersionName(String versionName) {
        Utils.versionName = versionName;
    }



    /** İnternet kontrolü ------------------------------------------------------------------------*/
    public boolean haveNetworkConnection() {

        boolean haveConnected  = false;
        ConnectivityManager cm = (ConnectivityManager) appCoontext.getSystemService( CONNECTIVITY_SERVICE );
        NetworkInfo[] networkInfos = cm.getAllNetworkInfo();

        for( NetworkInfo ni : networkInfos ){

            if( ni != null && ni.isConnected() ){
                haveConnected = true;
            }
        }

        return haveConnected ;
    }// end of haveNetworkConnection


    /** CustomToast mesajı -----------------------------------------------------------------------*/
    /**
     *
     * @param message
     * @param ctg OK for success
     * @param length L for long
     *
     */
    public void customToastMessage(String message, String ctg, String length ){

//        sp.play( soundId2 , 1 , 1 , 0 , 0 , 1  );   //  ( soundID, leftVolume , rightVolume, priority, loop , rate )

        LayoutInflater inflater = (LayoutInflater) appCoontext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View customToastLayout = inflater.inflate( R.layout.dialog_custom_toast_fail, null );

        if( ctg.equals( "OK" ) ){

            customToastLayout = inflater.inflate( R.layout.dialog_custom_toast_ok, null );

        }

        TextView textView = (TextView) customToastLayout.findViewById( R.id.tV_toast_warning );
        textView.setText( message );

        Toast customtoast = new Toast( appCoontext );
        customtoast.setView( customToastLayout );
        customtoast.setGravity( Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0 );
        customtoast.setDuration( Toast.LENGTH_SHORT );
        if( length.equals( "L" ) )
            customtoast.setDuration( Toast.LENGTH_LONG );

        customtoast.show();

    }// end of customToastMessage


    /**
     *
     * @param message
     * @param ctg OK for success
     * @param length L for long
     *
     */
    public static void customToastMessage(String message, String ctg, String length, Context ctx){

//        sp.play( soundId2 , 1 , 1 , 0 , 0 , 1  );   //  ( soundID, leftVolume , rightVolume, priority, loop , rate )

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View customToastLayout = inflater.inflate( R.layout.dialog_custom_toast_fail, null );

        if( ctg.equals( "OK" ) ){

            customToastLayout = inflater.inflate( R.layout.dialog_custom_toast_ok, null );

        }

        TextView textView = customToastLayout.findViewById( R.id.tV_toast_warning );
        textView.setText( message );

        Toast customtoast = new Toast( ctx );
        customtoast.setView( customToastLayout );
        customtoast.setGravity( Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0 );
        customtoast.setDuration( Toast.LENGTH_SHORT );
        if( length.equals( "L" ) )
            customtoast.setDuration( Toast.LENGTH_LONG );

        customtoast.show();

    }// end of customToastMessage



    /** Warning when connecting through unauthorized network -------------------------------------*/
    public void showWifiAlert( String alertMessage ) {

        AlertDialog.Builder wifiAlert = new AlertDialog.Builder( appCoontext );

        wifiAlert.setMessage( alertMessage )
                .setCancelable(false)
                .setNeutralButton(
                        "AYARLA",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity( new Intent( Settings.ACTION_WIFI_SETTINGS ) );
                            }
                        }
                )
                .setPositiveButton(
                        "TAMAM",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
        wifiAlert.show();

    }//end of showWifiAlert




    public void showAlert( String mesaj ) {

        AlertDialog.Builder myAlert = new  AlertDialog.Builder(appCoontext);

        myAlert.setMessage( mesaj + "\n" )
                .setCancelable(false)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                        dialog.dismiss();

                    }
                }).create();
        myAlert.show();

    }// end showAlert





    public void makeFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
    }//end of makeFullScreen



    public void hideKeyboard(InputMethodManager imm){
        if (imm != null) {
            imm.toggleSoftInput( 0 , 0 );
        }
    }//end of hideKeyboard


    public void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN );

        Log.i("Utils", "hideSystemUI ...." );
    }

}
