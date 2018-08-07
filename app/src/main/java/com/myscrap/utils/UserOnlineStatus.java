package com.myscrap.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.myscrap.application.AppController;
import com.myscrap.service.MessageService;

/**
 * Created by Ms2 on 6/11/2016.
 */
public class UserOnlineStatus {
    //private static Context mContext;
    public static String ONLINE = "1";
    public static String OFFLINE = "0";
    private static Activity activity;

    public static void setUserOnline(Activity act, final String online) {
       // mContext = context;
        activity = act;
        AppController.service();
        new Handler().postDelayed(() -> userOnlineStatus(online), 1000);
    }

    private static void userOnlineStatus(final String onlineStatus) {
        if (AppController.getInstance().getPrefManager().getUser() == null || activity == null)
            return;

        String img = UserUtils.getUserProfilePicture(activity);
        String firstName = UserUtils.getFirstName(activity);
        String lastName = UserUtils.getLastName(activity);
        String color = UserUtils.getColorCode(activity);
        if(color == null || color.equalsIgnoreCase("") || color.equalsIgnoreCase("null"))
            color = null;
        String name = firstName + " " + lastName;
        Intent i = new Intent();
        i.setAction(MessageService.ACTION_SHOW_ONLINE);
        i.putExtra("status",onlineStatus);
        i.putExtra("name",name);
        i.putExtra("img",img);
        i.putExtra("color",color);
        i.putExtra("from",AppController.getInstance().getPrefManager().getUser().getId());
        activity.sendBroadcast(i);
    }
}
