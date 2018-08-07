package com.myscrap;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.service.DeviceModelService;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.UserUtils;

public class SplashActivity extends AppCompatActivity
{
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        DeviceUtils.init(this);
        mTracker = AppController.getInstance().getDefaultTracker();
        if(!UserUtils.isApiKeyAlreadySent(this))
        {
            Intent mIntent = new Intent(this, DeviceModelService.class);
            mIntent.putExtra("apiKey", DeviceUtils.getUUID(AppController.getInstance()));
            mIntent.putExtra("mobileDevice", DeviceUtils.getDeviceName());
            mIntent.putExtra("mobileBrand", DeviceUtils.getDeviceBrand());
            startService(mIntent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()){
                new android.os.Handler().postDelayed(
                        this::screenMoveToLogin, 2000);
            }
        } else {
            new android.os.Handler().postDelayed(
                    this::screenMoveToLogin, 2000);
        }


    }
    protected void onNewIntent(Intent mIntent)
    {
        String action = mIntent.getAction();
        String data = mIntent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            if (mIntent.getData() != null && !mIntent.getData().toString().equalsIgnoreCase(""))
            {
                Log.d("uri", String.valueOf(mIntent.getData()));
                if (String.valueOf(mIntent.getData()).contains("https://myscrap.com/")){
                    screenMoveToLogin();
                }
            }
        } else {
            screenMoveToLogin();
        }
    }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        new android.os.Handler().postDelayed(
                                this::screenMoveToLogin, 2000);
                    }
                }else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                    new android.os.Handler().postDelayed(
                            this::screenMoveToLogin, 2000);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                    new android.os.Handler().postDelayed(
                            this::screenMoveToLogin, 2000);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Splash Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    private void screenMoveToLogin() {
        Intent mIntent;
        if(UserUtils.isAlreadyLoggedIn(AppController.getInstance())){
            mIntent = new Intent(this, HomeActivity.class);
            startActivity(mIntent);
            finish();
        } else {
            mIntent = new Intent(this, LoginActivity.class);
            startActivity(mIntent);
            finish();
        }
    }





    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
