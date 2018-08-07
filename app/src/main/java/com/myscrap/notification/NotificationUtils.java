package com.myscrap.notification;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;

import com.myscrap.HomeActivity;
import com.myscrap.MainChatRoomActivity;
import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.utils.UserUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ms3 on 3/29/2017.
 */

public class NotificationUtils
{

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public NotificationUtils() {}

    public NotificationUtils(Context mContext)
    {
        this.mContext = mContext;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void showNotificationMessage(String title, String message, String timeStamp,  String fromId, Intent intent)
    {
        showNotificationMessage(title, message, timeStamp, fromId,intent, null, 0);
    }





    public void showNotificationMessage(final String title, final String message, final String timeStamp, final String fromId, Intent intent, final String imageUrl, final int type)
    {

        if(type == 0)
        {
            if (TextUtils.isEmpty(message))
                return;
        }

        // Check for empty push message

        // notification icon
        final int icon = R.mipmap.ic_launcher;
        int notificationId = Integer.parseInt(UserUtils.parsingInteger(fromId));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        notificationId,
                        intent,
                        0
                );

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext);

        final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (!TextUtils.isEmpty(imageUrl)) {

            if (imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {
                AsyncTask.execute(() -> {
                    Bitmap bitmap = getBitmapFromURL(imageUrl);
                    if (bitmap != null){
                        showSmallNotification(mBuilder, bitmap,icon, title, message, timeStamp, fromId, resultPendingIntent, alarmSound,0);
                    } else {
                        showSmallNotification(mBuilder, null, icon, title, message, timeStamp, fromId, resultPendingIntent, alarmSound,0);
                    }
                });
            }
        } else {
            showSmallNotification(mBuilder, null, icon, title, message, timeStamp, fromId,  resultPendingIntent, alarmSound,0);
            playNotificationSound();
        }
    }




    public void showNotificationMessageInboxStyle(final String title, final String message, final String timeStamp, final String fromId, Intent intent, final String imageUrl) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;
        // notification icon
        final int icon = R.mipmap.ic_launcher;
        int notificationId = Integer.parseInt(UserUtils.parsingInteger(fromId));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        notificationId,
                        intent,
                        0
                );

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                mContext);

        final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (!TextUtils.isEmpty(imageUrl)) {

            if (imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {

                AsyncTask.execute(() -> {
                    Bitmap bitmap = getBitmapFromURL(imageUrl);
                    if (bitmap != null){
                        showSmallNotification(mBuilder, bitmap,icon, title, message, timeStamp, fromId, resultPendingIntent, alarmSound, 1);
                    } else {
                        showSmallNotification(mBuilder, null, icon, title, message, timeStamp, fromId, resultPendingIntent, alarmSound, 1);
                    }
                });
            }
        } else {
            showSmallNotification(mBuilder, null, icon, title, message, timeStamp, fromId,  resultPendingIntent, alarmSound, 1);
            playNotificationSound();
        }
    }





    private void showSmallNotification(NotificationCompat.Builder mBuilder, Bitmap bitmap,
                                       int icon, String title, String message, String timeStamp,
                                       String fromId, PendingIntent resultPendingIntent,
                                       Uri alarmSound, int notificationType)
    {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        int smallIcon = R.mipmap.noti;
        if (Config.appendNotificationMessages)
        {
            String oldNotification = AppController.getInstance().getPreferenceManager().getNotifications();
            String oldNotificationCount = AppController.getInstance().getPreferenceManager().getNotificationsFromId();
            List<String> messages;
            List<String> messagesCount;
            if(oldNotification != null && oldNotificationCount != null)
            {

                messages = Arrays.asList(oldNotification.split("\\|"));
                messagesCount = Arrays.asList(oldNotificationCount.split("\\|"));

                for (int i = messages.size() - 1; i >= 0; i--)
                {
                    inboxStyle.addLine(messages.get(i));
                }


                Set<String> uniqueId = new HashSet<>(messagesCount);
                String chats;
                if (messages.size() > 0)
                {
                    String notificationCount = messages.size() == 1? messages.size()+" message" : messages.size()+" messages";
                    if (uniqueId.size() != 0)
                    {
                        if (uniqueId.size() == 1)
                        {
                            chats = uniqueId.size() +" chat";
                            smallIcon = getNotificationIcon(mBuilder);
                        }
                        else
                            {
                            int icLauncherIcon = R.mipmap.ic_launcher;
                            smallIcon = getNotificationIcon(mBuilder);
                            bitmap = BitmapFactory.decodeResource(AppController.getInstance().getResources(), icLauncherIcon);
                            chats = uniqueId.size() +" chats";
                        }
                        String chatsCount = notificationCount +" from " + chats;
                        inboxStyle.setSummaryText(chatsCount);
                    } else {
                        inboxStyle.setSummaryText(notificationCount);
                    }
                    UserUtils.setMSGNotificationCount(AppController.getInstance(), String.valueOf(uniqueId.size()));
                    HomeActivity.notification();
                } else {
                    UserUtils.setMSGNotificationCount(AppController.getInstance(), "0");
                    HomeActivity.notification();
                }
                inboxStyle.setBigContentTitle("MyScrap");
            }
        }

        HomeActivity.notification();
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = mBuilder.setSmallIcon(smallIcon).setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setSound(alarmSound)
                    //.setStyle(inboxStyle)
                    .setLights(Color.WHITE, 5000, 5000)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    //.setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentText(message)
                    .build();
        } else {
            notification = mBuilder.setSmallIcon(getNotificationIcon(mBuilder)).setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setSound(alarmSound)
                    //.setStyle(inboxStyle)
                    .setLights(Color.WHITE, 5000, 5000)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    //.setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentText(message)
                    .build();
        }



        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {


            String NOTIFICATION_CHANNEL_ID = "myscrap";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, title, importance);
                mChannel.setDescription("");
                mChannel.enableLights(true);
                mChannel.setLightColor(ContextCompat.getColor(AppController.getInstance(),R.color.colorPrimary));
                notificationManager.createNotificationChannel(mChannel);
            }


            if(fromId != null && !fromId.equalsIgnoreCase(""))
            {
                int notificationId = Integer.parseInt(UserUtils.parsingInteger(fromId));
                if(notificationType == 0)
                {
                    notificationManager.notify(notificationId, notification);
                }
                else
                {
                    notificationManager.notify(Config.NOTIFICATION_ID, notification);
                }
            }
            else
            {
                notificationManager.notify(Config.NOTIFICATION_ID, notification);
            }
        }
    }




    public static void offlineShowSmallNotification()
    {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        int icLauncherIcon = R.mipmap.ic_launcher;
        List<String> messages = new ArrayList<>();

        String oldNotification = AppController.getInstance().getPreferenceManager().getNotifications();
        String oldNotificationCount = AppController.getInstance().getPreferenceManager().getNotificationsFromId();
        String oldNotificationLastTime = AppController.getInstance().getPreferenceManager().getNotificationsLastTime();
        String lastMessage = null;
        if (oldNotification != null && oldNotificationCount != null){
            if (Config.appendNotificationMessages) {
                // store the notification in shared pref first
                // get the notifications from shared preferences
                messages = Arrays.asList(oldNotification.split("\\|"));
                List<String> messagesCount = Arrays.asList(oldNotificationCount.split("\\|"));

                for (int i = messages.size() - 1; i >= 0; i--) {
                    inboxStyle.addLine(messages.get(i));
                    lastMessage = messages.get(messages.size() - 1);
                }

                Set<String> uniqueId = new HashSet<>(messagesCount);
                String chats;
                if (messages.size() > 0)
                {
                    String notificationCount = messages.size() == 1? messages.size()+" message" : messages.size()+" messages";
                    if (uniqueId.size() != 0)
                    {
                        if (uniqueId.size() == 1)
                        {
                            chats = uniqueId.size() +" chat";
                        }
                        else
                        {
                            chats = uniqueId.size() +" chats";
                        }
                        String chatsCount = notificationCount +" from " + chats;
                        UserUtils.setMSGNotificationCount(AppController.getInstance(), String.valueOf(uniqueId.size()));
                        inboxStyle.setSummaryText(chatsCount);
                    } else {
                        inboxStyle.setSummaryText(notificationCount);
                        UserUtils.setMSGNotificationCount(AppController.getInstance(), String.valueOf(uniqueId.size()));
                    }
                    HomeActivity.notification();
                }
                else
                {
                    UserUtils.setMSGNotificationCount(AppController.getInstance(), "0");

                    HomeActivity.loadNotificationCount();
                    HomeActivity.notification();
                }
                inboxStyle.setBigContentTitle("MyScrap");

            }

            Intent intent = new Intent(AppController.getInstance(), MainChatRoomActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(AppController.getInstance(), 0 /* Request code */,
                    intent, 0);

            if (messages.size() > 0) {
                final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Notification notification;
                final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        AppController.getInstance());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = mBuilder.setWhen(0)
                            .setAutoCancel(true)
                            .setContentTitle("MyScrap")
                            .setContentIntent(pendingIntent)
                            .setStyle(inboxStyle)
                            .setLights(Color.WHITE, 5000, 5000)
                            .setWhen(getTimeMilliSec(oldNotificationLastTime))
                            .setShowWhen(true)
                            //.setSmallIcon(R.mipmap.ic_launcher)
                            .setLargeIcon(BitmapFactory.decodeResource(AppController.getInstance().getResources(), icLauncherIcon))
                            .setSmallIcon(getNotificationIcon(mBuilder))
                            .setContentText(lastMessage)
                            .build();
                } else {
                    notification = mBuilder.setWhen(0)
                            .setAutoCancel(true)
                            .setContentTitle("MyScrap")
                            .setContentIntent(pendingIntent)
                            .setStyle(inboxStyle)
                            .setLights(Color.WHITE, 5000, 5000)
                            .setWhen(getTimeMilliSec(oldNotificationLastTime))
                            .setShowWhen(true)
                            //.setSmallIcon(R.mipmap.ic_launcher)
                            .setLargeIcon(BitmapFactory.decodeResource(AppController.getInstance().getResources(), icLauncherIcon))
                            .setSmallIcon(getNotificationIcon(mBuilder))
                            .setContentText(lastMessage)
                            .build();
                }

                NotificationManager notificationManager = (NotificationManager) AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.notify(Config.NOTIFICATION_ID, notification);
                }
            }
        }
    }



    private void showBigNotification(Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String title, String message, String timeStamp, String fromId, PendingIntent resultPendingIntent, Uri alarmSound) {
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(bitmap);
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = mBuilder.setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setSound(alarmSound)
                    .setStyle(bigPictureStyle)
                    .setWhen(getTimeMilliSec(timeStamp))
                    .setShowWhen(true)
                    .setPriority(Notification.PRIORITY_MAX)
                    //.setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                    .setSmallIcon(getNotificationIcon(mBuilder))
                    .setContentText(message)
                    .build();
        } else {
            notification = mBuilder.setSmallIcon(getNotificationIcon(mBuilder)).setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setSound(alarmSound)
                    .setStyle(bigPictureStyle)
                    .setWhen(getTimeMilliSec(timeStamp))
                    .setShowWhen(true)
                    //.setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                    .setContentText(message)
                    .build();
        }

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(Config.NOTIFICATION_ID_BIG_IMAGE, notification);
        }
    }



    public void showNotifications(final JSONObject jsonObject, final String postId, Intent intent, int notificationId)
    {
        String title= null;
        String message = null;
        String imageUrl = null;
        String NOTIFICATION_CHANNEL_ID = "myscrap";
        if (jsonObject != null){
            title = jsonObject.optString("name");
            message = jsonObject.optString("content");
            imageUrl = jsonObject.optString("picture");
        }
        // notification icon
        final int icon = R.mipmap.ic_launcher;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        showOtherNotification(mBuilder, imageUrl,icon, title, message, postId, resultPendingIntent, alarmSound, notificationId);
    }

    private void showOtherNotification(NotificationCompat.Builder mBuilder, String imageUrl, int icon, String title, String message, String postId, PendingIntent resultPendingIntent, Uri alarmSound, int notificationId) {
        Bitmap bitmap;
        int smallIcon = R.mipmap.noti;
        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {
                 bitmap = getBitmapFromURL(imageUrl);
                if(bitmap == null)
                    bitmap =  BitmapFactory.decodeResource(mContext.getResources(), icon);
            } else {
                bitmap =  BitmapFactory.decodeResource(mContext.getResources(), icon);
            }
        } else {
            bitmap =  BitmapFactory.decodeResource(mContext.getResources(), icon);
        }

        if(bitmap == null){
            bitmap =  BitmapFactory.decodeResource(mContext.getResources(), icon);
        }

        Notification notification;
        notification = mBuilder.setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                //.setColor(color)
                .setLights(Color.WHITE, 5000, 5000)
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setSmallIcon(smallIcon)
                .setContentText(message)
                .build();
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            if(postId != null && !postId.equalsIgnoreCase(""))
            {
                String NOTIFICATION_CHANNEL_ID = "myscrap";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, title, importance);
                    mChannel.setDescription("");
                    mChannel.enableLights(true);
                    mChannel.setLightColor(ContextCompat.getColor(AppController.getInstance(),R.color.colorPrimary));
                    notificationManager.createNotificationChannel(mChannel);
                }

                notificationManager.notify(Integer.parseInt(UserUtils.parsingInteger(postId)), notification);
            }
            else
            {
                notificationManager.notify(notificationId, notification);
            }
        }
    }




    private static int getNotificationIcon(NotificationCompat.Builder notificationBuilder)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            int color = 0x01A43B;
            notificationBuilder.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            return R.mipmap.noti;
        }
        else
        {
            return R.mipmap.noti;
        }
    }

    private Bitmap getBitmapFromURL(String strURL) {

        if (strURL == null)
            return null;

        if(strURL.equalsIgnoreCase("") ||
                strURL.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                || strURL.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
            return null;
        }

        try
        {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200)
            {
                return makeCircle(BitmapFactory.decodeStream(connection.getInputStream()));
            }
            else
                return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }




    private Bitmap makeCircle(Bitmap bitmap)
    {
        if(bitmap == null)
            return null;
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2,
                bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;

    }


    private Bitmap getCircleBitmap(Bitmap bitmap, Context mContext)
    {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int radius = Math.min(h / 2, w / 2);
        Bitmap output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888);

        Paint p = new Paint();
        p.setAntiAlias(true);

        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setStyle(Paint.Style.FILL);

        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        c.drawBitmap(bitmap, 4, 4, p);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.WHITE);
        p.setStrokeWidth(0);
        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);
        return output;
    }

    public static void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(AppController.getInstance(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean isAppIsInBackground(Context context)
    {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses;
            if (am != null) {
                runningProcesses = am.getRunningAppProcesses();
                if(runningProcesses != null){
                    for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            for (String activeProcess : processInfo.pkgList) {
                                if (activeProcess.equals(context.getPackageName())) {
                                    isInBackground = false;
                                }
                            }
                        }
                    }
                }
            }


        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo;
            if (am != null) {
                taskInfo = am.getRunningTasks(1);
                if(taskInfo != null){
                    ComponentName componentInfo = taskInfo.get(0).topActivity;
                    if (componentInfo.getPackageName().equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }

        return isInBackground;
    }

    public static void clearNotifications() {
        NotificationManager notificationManager = (NotificationManager) AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }
    public static void clearNotificationByID(int id )
    {
        NotificationManager notificationManager = (NotificationManager) AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            notificationManager.cancel(id);
        }
    }

    private static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }




    public static void showNotifications(String title,String message,Intent intent)
    {

        String NOTIFICATION_CHANNEL_ID = "myscrap";
        Bitmap bitmap;
        int smallIcon = R.mipmap.noti;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //  intent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent = PendingIntent.getActivity(AppController.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(AppController.getInstance(), NOTIFICATION_CHANNEL_ID);
        final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        bitmap =  BitmapFactory.decodeResource(AppController.getInstance().getResources(), R.mipmap.ic_launcher);
        Notification notification;
        notification = mBuilder.setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarmSound)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setLights(Color.WHITE, 5000, 5000)
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setLargeIcon(bitmap)
                .setSmallIcon(smallIcon)
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, title, importance);
                mChannel.setDescription("");
                mChannel.enableLights(true);
                mChannel.setLightColor(ContextCompat.getColor(AppController.getInstance(),R.color
                        .colorPrimary));
                notificationManager.createNotificationChannel(mChannel);
            }
            notificationManager.notify(0, notification);
        }

    }



    public static Notification getNotification()
    {

        String NOTIFICATION_CHANNEL_ID = "myscrap";
        Bitmap bitmap;
        int smallIcon = R.mipmap.noti;


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        bitmap =  BitmapFactory.decodeResource(AppController.getInstance().getResources(), R.mipmap.ic_launcher);
        Notification notification;
        notification = mBuilder.setTicker("MyScrap").setWhen(0)
                .setAutoCancel(true)
                .setContentTitle("MyScrap")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Connected"))
                .setLights(Color.WHITE, 5000, 5000)
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setLargeIcon(bitmap)
                .setSmallIcon(smallIcon)
                .setContentText("connected")
                .build();

        NotificationManager notificationManager = (NotificationManager) AppController.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            notificationManager.notify(0, notification);
        }

        return notification;

    }





}
