package com.myscrap.notification;

import android.content.Context;
import android.content.SharedPreferences;

import com.myscrap.utils.UserUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ms3 on 3/29/2017.
 */

public class NotificationPreferenceManager{


    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private static final String PREF_NAME = "socket";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_NOTIFICATIONS_COUNT = "notifications_count";
    private static final String KEY_NOTIFICATIONS_TIME = "notifications_when";
    private static final String KEY_MESSAGE_READ = "message_read";
    private static final String KEY_MESSAGE_READ_FROM_ID = "message_read_from_id";

    // Constructor
    public NotificationPreferenceManager(Context context) {
        this.context = context;
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
       // editor.commit();
    }

    public void addNotification(String notification, String fromId) {
        // get old notifications
        String oldNotifications = getNotifications();
        String oldNotificationsFromId = getNotificationsFromId();
        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }
        if (oldNotificationsFromId != null)
        {
            oldNotificationsFromId += "|" + fromId;
        } else {
            oldNotificationsFromId = fromId;
        }
        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.putString(KEY_NOTIFICATIONS_COUNT, oldNotificationsFromId);
        editor.putString(KEY_NOTIFICATIONS_TIME, time());
        editor.apply();

    }

    public void addMessageRead(String message, String fromId)
    {
        // get old notifications
        if (!message.equalsIgnoreCase(""))
        {
            String oldMessageRead = getMessageRead();
            String oldMessageReadFromId = getMessageReadFromId();
            if (oldMessageRead != null) {
                oldMessageRead += "|" + message;
            } else {
                oldMessageRead = message;
            }
            if (oldMessageReadFromId != null)
            {
                oldMessageReadFromId += "|" + fromId;
            } else {
                oldMessageReadFromId = fromId;
            }

            editor.putString(KEY_MESSAGE_READ, oldMessageRead);
            editor.putString(KEY_MESSAGE_READ_FROM_ID, oldMessageReadFromId);
            editor.apply();
        }

    }

    public String getMessageRead()
    {
        return pref.getString(KEY_MESSAGE_READ, null);
    }

    public String getMessageReadFromId() {
        return pref.getString(KEY_MESSAGE_READ_FROM_ID, null);
    }

    public void clearMessageRead() {
        editor.remove(KEY_MESSAGE_READ);
        editor.remove(KEY_MESSAGE_READ_FROM_ID);
        editor.apply();
    }


    private static String time()
    {
        Date now = new Date();
        long dateValue = Long.parseLong(UserUtils.parsingLong(Long.toString(now.getTime() / 1000)));
        return getChatRoomTime(dateValue);
    }

    private static String getChatRoomTime(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }
        SimpleDateFormat mSimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        return mSimpleDateFormat.format(time);
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public String getNotificationsLastTime() {
        return pref.getString(KEY_NOTIFICATIONS_TIME, null);
    }

    public String getNotificationsFromId() {
        return pref.getString(KEY_NOTIFICATIONS_COUNT, null);
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}
