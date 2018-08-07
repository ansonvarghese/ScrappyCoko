package com.myscrap.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.glidebitmappool.GlideBitmapPool;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.R;
import com.myscrap.notification.NotificationPreferenceManager;
import com.myscrap.preference.MyPreferenceManager;
import com.myscrap.service.LocationUpdaterService;
import com.myscrap.service.MessageService;
import com.myscrap.utils.TypefaceUtil;
import com.myscrap.view.URLSpanNoUnderline;
import com.myscrap.xmpp.RoosterConnectionService;

import io.fabric.sdk.android.Fabric;
import io.socket.client.Socket;

/**
 * Created by ms3 on 5/11/2017.
 */

public class AppController extends Application{

    private static final String TAG = AppController.class.getSimpleName();
    private static AppController mInstance;
    private static Socket mSocket;
    private MyPreferenceManager pref;
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    private RequestQueue mRequestQueue;
    private NotificationPreferenceManager preferenceManager;
    public static String FONT_NAME = "fonts/Arial-Regular.ttf";

    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
        sAnalytics = GoogleAnalytics.getInstance(this);
        Fresco.initialize(this);
        GlideBitmapPool.initialize(10 * 1024 * 1024);

        // 10mb max memory size
        /*DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryPath(new File(Environment.getExternalStorageDirectory().getAbsoluteFile(),"MyScrap"))
                .setBaseDirectoryName("_ms_")
                .setMaxCacheSize(200*1024*1024)//200MB
                .build();
        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
                .setMainDiskCacheConfig(diskCacheConfig)
                .build();
        Fresco.initialize(this, imagePipelineConfig);*/



        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //Stetho.initializeWithDefaults(this);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Fabric.with(this, new Crashlytics());
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());
        final Typeface regular = Typeface.createFromAsset(applicationContext.getAssets(), FONT_NAME);
        TypefaceUtil.overrideFont(regular);
        startService();

        Intent i = new Intent(AppController.getInstance(), LocationUpdaterService.class);
        AppController.getInstance().startService(i);



    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }


    public static Socket getSocket(){
        return mSocket;
    }


    public void setSocketInstance(Socket mSocketInstance){
        mSocket = mSocketInstance;
    }


    public MyPreferenceManager getPrefManager() {
        if (pref == null) {
            pref = new MyPreferenceManager(this);
        }
        return pref;
    }

    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
            sTracker.enableExceptionReporting(true);
            sTracker.enableAdvertisingIdCollection(true);
            sTracker.enableAutoActivityTracking(true);
        }

        return sTracker;
    }

    public void trackEvent(String category, String action, String label) {
        if (sTracker == null) {
            getDefaultTracker();
        }
        sTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }


    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) AppController.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    Log.e("isMyServiceRunning", ""+true);
                    return true;
                }
            }
        }
        Log.e("isMyServiceRunning", ""+false);
        return false;
    }

    public static boolean isChatRoomActivityRunning() {
        ActivityManager manager = (ActivityManager) AppController.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningTaskInfo service : manager.getRunningTasks(1)) {
                if (service != null && service.topActivity != null && service.topActivity.getClassName().equals("com.myscrap.ChatRoomActivity")) {
                    Log.e("ChatRoomActivity", ""+true);
                    return true;
                }
            }
        }
        Log.e("ChatRoomActivity", ""+false);
        return false;
    }

    public void startService()
    {
        //Alarm alarm = new Alarm();
        //alarm.setAlarm(AppController.getInstance());
        service();
    }

    public static void service()
    {
        if (!isMyServiceRunning(MessageService.class))
        {
            Intent i = new Intent(AppController.getInstance(), MessageService.class);
            AppController.getInstance(). startService(i);
       //     AppController.getInstance().startService(i);
            GlideBitmapPool.clearMemory();
        }
    }



    public static void startXMPPService()
    {
        if (!isMyServiceRunning(RoosterConnectionService.class))
        {
            Intent i1 = new Intent(AppController.getInstance(), RoosterConnectionService.class);
            AppController.getInstance(). startService(i1);
            AppController.getInstance(). startService(i1);
            GlideBitmapPool.clearMemory();
        }
    }




    public void logout()
    {
        if (pref!= null)
        {
            pref.clear();
        }
    }


    public void stripUnderlines(TextView textView)
    {
        if(textView != null){
            Spannable s = new SpannableString(textView.getText());
            URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
            for (URLSpan span: spans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                s.removeSpan(span);
                span = new URLSpanNoUnderline(span.getURL());
                s.setSpan(span, start, end, 0);
            }
            textView.setText(s);
        }

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public static void runOnUIThread(Runnable runnable)
    {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay)
    {
        if (delay == 0)
        {
            applicationHandler.post(runnable);
        }
        else
        {
            applicationHandler.postDelayed(runnable, delay);
        }
    }

    public NotificationPreferenceManager getPreferenceManager()
    {
        if (preferenceManager == null)
        {
            preferenceManager = new NotificationPreferenceManager(this);
        }
        return preferenceManager;
    }

}
