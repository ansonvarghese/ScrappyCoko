package com.myscrap.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.myscrap.application.AppController;


/**
 * Created by ms3 on 3/29/2017.
 */

public class BootReceiver extends BroadcastReceiver
{
    Alarm alarm = new Alarm();
    @Override
    public void onReceive(final Context context, Intent intent)
    {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            AppController.runOnUIThread(() ->
            {
                Log.e("BootReceiver", "Started");
                alarm.setAlarm(context);
            });
        }
    }
}
