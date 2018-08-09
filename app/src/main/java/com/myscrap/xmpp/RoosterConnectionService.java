package com.myscrap.xmpp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.myscrap.service.MessageService;
import com.myscrap.xmppresources.Constant;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;

import java.io.IOException;

/**
 * Created by gakwaya on 2018/1/11.
 */

public class RoosterConnectionService extends Service
{
    private static final String LOGTAG ="Rock";

    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to

    public static RoosterConnection getConnection()
    {
        return mConnection;
    }


    //the background thread.
    private static RoosterConnection mConnection;


    public RoosterConnectionService()
    {

    }

    private void initConnection()
    {
        Log.d(LOGTAG,"initConnection()");
        if( mConnection == null)
        {
            mConnection = new RoosterConnection(this);
        }
        try
        {
            mConnection.connect();
        }
        catch (IOException |SmackException |XMPPException e)
        {

            Log.d(LOGTAG,"Something went wrong while connecting ,make sure the credentials are right and try again");

            Intent i = new Intent(Constant.BroadCastMessages.UI_CONNECTION_ERROR);
            i.setPackage(getApplicationContext().getPackageName());
            getApplicationContext().sendBroadcast(i);


            //Stop the service all together if user is not logged in already.
            boolean logged_in_state = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean("xmpp_logged_in",false);

            if(!logged_in_state)
            {
                Log.d(LOGTAG,"Logged in state :"+ logged_in_state + "calling stopself()");
                stopSelf();
            }
            else
            {
                Log.d(LOGTAG,"Logged in state :"+ logged_in_state);
            }

            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("MyScrap")
                    .setContentText("Connected").build();
            startForeground(1, notification);

        }
        ServerPingWithAlarmManager.onCreate(getApplicationContext());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        start();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopSelf();
     //   stop();
        Intent broadcastIntent = new Intent(MessageService.ACTION_RESTART_XMPP_SERVICE);
        sendBroadcast(broadcastIntent);

    }

    public void start()
    {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        Looper.loop();
                    }
                });
                mThread.start();

    }









    public void stop()
    {
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if( mConnection != null)
                {
                    mConnection.disconnect();
                }
            }
        });

    }






}
