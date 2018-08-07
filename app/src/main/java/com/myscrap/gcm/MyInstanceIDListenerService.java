package com.myscrap.gcm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.myscrap.application.AppController;
import com.myscrap.model.User;
import com.myscrap.notification.Config;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ms3 on 5/11/2017.
 */

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = MyInstanceIDListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        UserUtils.saveFireBaseInstanceId(AppController.getInstance(),refreshedToken);
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // Get updated InstanceID token.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token;
        try {
            token = FirebaseInstanceId.getInstance().getToken();
            UserUtils.saveFireBaseInstanceId(AppController.getInstance(),token);
            User user = AppController.getInstance().getPrefManager().getUser();
            if (user != null) {
                if (user.getId() != null && !user.getId().equalsIgnoreCase("")){
                    sendRegistrationToServer(user.getId(), token);
                }
                Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
                registrationComplete.putExtra("token", token);
                LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
                sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, true).apply();
                UserUtils.saveFireBaseInstanceId(AppController.getInstance(),"");
                UserUtils.saveFireBaseInstanceId(AppController.getInstance(),token);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void sendRegistrationToServer(String userId, final String gcmCode) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(this);
        apiService.updateGcmId(userId, gcmCode, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d("sendGCM", "onSuccess");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("sendGCM", "onFailure");
                        String token = UserUtils.getFireBaseInstanceId(AppController.getInstance());
                        if (AppController.getInstance().getPrefManager().getUser() == null)
                            return;
                        if(token != null)
                            sendRegistrationToServer(AppController.getInstance().getPrefManager().getUser().getId(), token);
                    }

                    @Override
                    public void onNext(String res) {
                        if ( res != null) {
                            if(res.equalsIgnoreCase("success")){
                                Intent registrationComplete = new Intent(Config.SENT_TOKEN_TO_SERVER);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(registrationComplete);
                            }
                        }
                    }
                });
    }

}
