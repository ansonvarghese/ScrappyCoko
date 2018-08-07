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
import com.myscrap.model.Online;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.Calendar;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Ms2 on 10/6/2016.
 */

public class OnlineNotifierService extends Service
{

    public static final String BROADCAST_ACTION = "online_updater";
    Intent intent;
    String chatRoomId;
    private Handler handler = new Handler();

    @Override
    public void onCreate()
    {

        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d("Service " ," onStartCommand");
        if (intent != null)
        {
            chatRoomId = intent.getStringExtra("chatRoomId");
            setNotify(chatRoomId);
            handler.removeCallbacks(sendUpdatesToUI);
            handler.postDelayed(sendUpdatesToUI, 1000*60); // 1 second
        }
        return START_NOT_STICKY;
    }

    Runnable sendUpdatesToUI = new Runnable() {
        @Override
        public void run() {
            if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                if (chatRoomId != null)
                    setNotify(chatRoomId);
                handler.postDelayed(sendUpdatesToUI, 1000 * 60 * 2);
            }
        }
    };

    private void setNotify(final String chatRoomId)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getApplicationContext());
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();

            String timeZone = tz.getID();
            Log.d("TimeZone", timeZone);
            Call<Online> call = apiService.userOnlineStatus(userId,chatRoomId, timeZone, apiKey);
            call.enqueue(new Callback<Online>()
            {
                @Override
                public void onResponse(@NonNull Call<Online> call, @NonNull retrofit2.Response<Online> response)
                {
                    ProgressBarDialog.dismissLoader();
                    if (response.body() != null && response.isSuccessful()) {
                        Online mOnline = response.body();
                        if(mOnline != null && !mOnline.isErrorStatus()) {
                            if(mOnline.getOnlineData() != null){
                                Online.OnlineData mData = mOnline.getOnlineData();
                                Gson gson = new Gson();
                                String userData = gson.toJson(mData);
                                intent.putExtra("response", userData);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Online> call, @NonNull Throwable t) {
                    ProgressBarDialog.dismissLoader();
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        chatRoomId = null;
        if(handler != null && sendUpdatesToUI != null)
            handler.removeCallbacks(sendUpdatesToUI);
    }
}
