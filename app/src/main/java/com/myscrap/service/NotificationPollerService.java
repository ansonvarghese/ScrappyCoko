package com.myscrap.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by ms3 on 4/1/2017.
 */

public class NotificationPollerService extends Service
{

    Alarm alarm = new Alarm();
    public void onCreate()
    {
        super.onCreate();
        alarm.setAlarm(this);

     

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarm.setAlarm(this);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
