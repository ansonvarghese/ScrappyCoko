package com.myscrap.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.myscrap.model.DeviceModel;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by ms3 on 5/13/2017.
 */

public class DeviceModelService extends Service
{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String apiKey = intent.getStringExtra("apiKey");
            String mobileDevice = intent.getStringExtra("mobileDevice");
            String mobileBrand = intent.getStringExtra("mobileBrand");
            updateDeviceDetails(apiKey, mobileDevice, mobileBrand);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }


    private  void updateDeviceDetails(final String apiKey, String device, String brand){
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            apiService.updateDeviceDetails(apiKey, device, brand)
            .subscribeOn(Schedulers.io())
                    .retry(5)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<DeviceModel>() {
                @Override
                public void onCompleted() {
                    Log.d("updateDeviceDetails", "onCompleted");
                }

                @Override
                public void onError(Throwable e) {
                    Log.d("updateDeviceDetails", "onFailure");
                    UserUtils.saveApiKeySent(getApplicationContext(), "0");
                }

                @Override
                public void onNext(DeviceModel deviceModel) {
                    boolean isApiKey;
                    if (deviceModel != null) {
                        isApiKey = deviceModel.isErrorStatus();
                        String status = deviceModel.getStatus();
                        Log.d("updateDeviceDetails", status);
                        if (!isApiKey) {
                            UserUtils.saveApiKey(getApplicationContext(), apiKey);
                            UserUtils.saveApiKeySent(getApplicationContext(), "1");
                        } else {
                            UserUtils.saveApiKeySent(getApplicationContext(), "0");
                        }
                    }
                    stopSelf();
                }
            })
                    ;
            /*call.enqueue(new Callback<DeviceModel>() {
                @Override
                public void onResponse(@NonNull Call<DeviceModel> call, @NonNull Response<DeviceModel> response) {
                    if (response.body() != null) {
                        DeviceModel deviceModel = response.body();

                    }
                }
                @Override
                public void onFailure(@NonNull Call<DeviceModel> call, @NonNull Throwable t) {

                }
            });*/
    }

}
