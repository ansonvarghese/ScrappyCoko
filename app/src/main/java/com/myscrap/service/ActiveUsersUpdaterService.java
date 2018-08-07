package com.myscrap.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.myscrap.application.AppController;
import com.myscrap.model.ActiveUser;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Ms2 on 10/6/2016.
 */

public class ActiveUsersUpdaterService extends Service
{

    public static final String BROADCAST_ACTIVE_USER_UPDATER_ACTION = "active_users_updater";
    private final Handler handler = new Handler();
    private Intent intent;

    @Override
    public void onCreate()
    {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTIVE_USER_UPDATER_ACTION);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null) {
            String activeUserPage = intent.getStringExtra("page");
            if (activeUserPage != null && !activeUserPage.equalsIgnoreCase("") && activeUserPage.equalsIgnoreCase("active")){
                loadActiveUsers();
            } else {
                handler.removeCallbacks(sendUpdatesToUI);
                handler.postDelayed(sendUpdatesToUI, 1000*15); // 1 second
            }
        }
        return START_NOT_STICKY;
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            if(CheckNetworkConnection.isConnectionAvailable(getApplicationContext())){
                loadActiveUsers();
            }
            handler.postDelayed(this, 1000 * 30); // 15 seconds
        }
    };

    private void loadActiveUsers(){
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(getApplicationContext());
        Call<ActiveUser> call = apiService.activeUsers(userId,apiKey);
        call.enqueue(new Callback<ActiveUser>() {
            @Override
            public void onResponse(@NonNull Call<ActiveUser> call, @NonNull Response<ActiveUser> response) {
                if(response.isSuccessful()){
                    ActiveUser mActiveUser = response.body();
                    if(mActiveUser != null) {
                        List<ActiveUser.ActiveUserData> mData = mActiveUser.getActiveUserData();
                        Gson gson = new Gson();
                        String userData = gson.toJson(mData);
                        intent.putExtra("response", userData);
                    }

                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
                Log.d("loadActiveUsers", "Success");
            }
            @Override
            public void onFailure(@NonNull Call<ActiveUser> call, @NonNull Throwable t) {
                Log.d("loadActiveUsers", "Failure");

            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
    }
}
