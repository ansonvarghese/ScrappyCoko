package com.myscrap.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.myscrap.CompanyProfileActivity;
import com.myscrap.DetailedPostActivity;
import com.myscrap.EventDetailActivity;
import com.myscrap.HomeActivity;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.application.AppController;
import com.myscrap.notification.Config;
import com.myscrap.notification.NotificationUtils;
import com.myscrap.utils.UserUtils;
import com.myscrap.xmpp.RoosterConnectionService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ms3 on 5/11/2017.
 */

public class MyGcmPushReceiver extends FirebaseMessagingService
{

    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();
    private String flag;
    private String notificationCount;
    private JSONObject object;
    private String postId;
    private Intent intent;


    @Override
    public void onMessageReceived(final RemoteMessage message)
    {
        super.onMessageReceived(message);

/*
                String typ =  message.getData().get("type");
                Toast.makeText(AppController.getInstance(),typ.toString(),Toast.LENGTH_SHORT).show();
                String messageId = message.getData().get("messageId");
                String friendsJid = message.getData().get("friendsJid");
                String friendsUserId = message.getData().get("friendsUserId");
                String friendsName = message.getData().get("friendsName");
                String friendsImage = message.getData().get("friendsImage");
                String content = message.getData().get("content");
                String friendsColor = message.getData().get("friendsColor");

                if (messageId != null && friendsJid != null && friendsUserId != null
                        && friendsName != null && friendsImage != null && content != null
                        && friendsColor != null)
                {
                    if (!UserUtils.getLoginStatus(getApplicationContext()).equalsIgnoreCase("0"))
                    {
                        Intent notificationIntent = new Intent(getApplicationContext(), XMPPChatRoomActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.FRIENDS_JID, friendsJid.split("@")[0]);
                        bundle.putString(Constant.FRIENDS_ID, friendsUserId);
                        bundle.putString(Constant.FRIENDS_NAME, friendsName);
                        bundle.putString(Constant.FRIENDS_URL, friendsImage);
                        bundle.putString(Constant.FRIENDS_COLOR, friendsColor);
                        notificationIntent.putExtras(bundle);
                        NotificationUtils.showNotifications(friendsName, content, notificationIntent);

                        ChatMessagesModel.get(getApplicationContext()).addMessage(new XMPPChatMessageModel(messageId, friendsJid,
                                friendsUserId, friendsName, friendsImage, content, "receive", String.valueOf(System.currentTimeMillis()), "", friendsColor));

                        Intent intent = new Intent(Constant.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
                        intent.setPackage(getApplicationContext().getPackageName());
                        getApplicationContext().sendBroadcast(intent);
                    }
                }

*/

                flag = UserUtils.parsingInteger(message.getData().get("flag"));
                notificationCount = UserUtils.parsingInteger(message.getData().get("notificationcount"));
                String notifiedUserName = message.getData().get("name");
                String notifiedUserNameId = message.getData().get("id");
                String notifiedUserProfilePicture = message.getData().get("profilepic");
                String notifiedContent = message.getData().get("content");
                String notId = message.getData().get("notId");
                postId = message.getData().get("postId");
                String friendId = message.getData().get("friendId");
                String companyId = message.getData().get("companyId");
                String type = message.getData().get("type");

                if (type != null)
                {
                    if (type.equalsIgnoreCase("xmppOfflineMessage"))
                    {
                        // start service to get server connection
                        Intent i1 = new Intent(getApplicationContext(), RoosterConnectionService.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            startForegroundService(i1);
                        }
                        else
                        {
                            startService(i1);
                        }
                    }
                }

                Log.d(TAG, "push received " + notificationCount);

                object = new JSONObject();
                try
                {
                    object.put("id", notifiedUserNameId);
                    object.put("name", notifiedUserName);
                    object.put("content", notifiedContent);
                    object.put("picture", notifiedUserProfilePicture);
                    object.put("postId", postId);
                    object.put("notId", notId);
                    object.put("friendId", friendId);
                    object.put("companyId", companyId);
                    object.put("type", type);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }


                if (AppController.getInstance().getPrefManager().getUser() == null)
                {
                    Log.e(TAG, "user is not logged in, skipping push notification");
                    return;
                }

                if (flag == null)
                    return;

                if (!UserUtils.isInteger(flag))
                {
                    return;
                }


                if (UserUtils.isGuestLoggedIn(getApplicationContext()))
                {
                    return;
                }

                if (!UserUtils.isNotificationEnabled(getApplicationContext()))
                    return;

                if (!flag.equalsIgnoreCase(""))
                {
                    if (Integer.parseInt(flag) == 3)
                    {
                        UserUtils.setNotificationCount(getApplicationContext(), String.valueOf(notificationCount));
                        Log.d("SaveNotification", String.valueOf(notificationCount));
                    }
                    else if (Integer.parseInt(flag) == 4)
                    {
                        UserUtils.setFRNotificationCount(getApplicationContext(), String.valueOf(notificationCount));
                    }
                    else if (Integer.parseInt(flag) == 5)
                    {
                        UserUtils.setNotificationCount(getApplicationContext(), String.valueOf(notificationCount));
                        Log.d("SaveNotification2", String.valueOf(notificationCount));
                    }
                    else if (Integer.parseInt(flag) == 6)
                    {
                        UserUtils.setBumpedCount(getApplicationContext(), String.valueOf(notificationCount));
                    }
                    HomeActivity.loadNotificationCount();
                    HomeActivity.notification();
                }

                if (!flag.equalsIgnoreCase(""))
                {
                    switch (Integer.parseInt(flag))
                    {
                        case Config.PUSH_TYPE_NOTIFICATION_COUNT:
                            if (object != null && object.has("id"))
                                processNotificationCount(notificationCount, object, postId);
                            break;

                        case Config.PUSH_TYPE_BUMPED_COUNT:
                            if (object != null && object.has("id"))
                                processNotificationCount(notificationCount, object, postId);
                            break;
                    }
                }




    }

    private void processNotificationCount(String notificationCount, JSONObject object, String postId)
    {

        if (notificationCount != null && object != null && postId != null) {
            if ( !notificationCount.equalsIgnoreCase(""))
            {

     /*       Intent resultIntent = new Intent(getApplicationContext(), HomeActivity.class);
            resultIntent.setAction("notification");
            resultIntent.putExtra("flag", "notification");
            resultIntent.putExtra("object", object.toString());*/

                try
                {
                    if (object.getString("type").equalsIgnoreCase("post"))
                    {
                        intent = new Intent(getApplicationContext(), DetailedPostActivity.class);
                        intent.putExtra("notId", object.getString("notId"));
                        intent.putExtra("postId",object.getString("postId"));
                        intent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
                    }

                    else if (object.getString("type").equalsIgnoreCase("doubleComment"))
                    {
                        intent = new Intent(getApplicationContext(), DetailedPostActivity.class);
                        intent.putExtra("notId", object.getString("notId"));
                        intent.putExtra("postId", object.getString("postId"));
                        intent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());

                    }
                    else if (object.getString("type").equalsIgnoreCase("user"))
                    {
                        intent = new Intent(AppController.getInstance(), UserFriendProfileActivity.class);
                        intent.putExtra("friendId",  object.getString("friendId"));
                        intent.putExtra("notId", object.getString("notId"));

                    }
                    else if (object.getString("type").equalsIgnoreCase("bumped"))
                    {
                        intent = new Intent(AppController.getInstance(), UserFriendProfileActivity.class);
                        intent.putExtra("friendId", object.getString("friendId"));
                        intent.putExtra("notId", object.getString("notId"));

                    }
                    else if (object.getString("type").equalsIgnoreCase("company"))
                    {
                        if(object.getString("companyId") != null && !object.getString("companyId").equalsIgnoreCase("") && !object.getString("companyId").equalsIgnoreCase("0")){
                            intent = new Intent(AppController.getInstance(), CompanyProfileActivity.class);
                            intent.putExtra("companyId", object.getString("companyId"));
                            intent.putExtra("notId", object.getString("notId"));

                        }
                    }
                    else if (object.getString("type").equalsIgnoreCase("event"))
                    {
                        if(object.getString("postId") != null && !object.getString("postId").equalsIgnoreCase("") && !object.getString("postId").equalsIgnoreCase("0")){
                            Intent i = new Intent(AppController.getInstance(), EventDetailActivity.class);
                            i.putExtra("eventId",object.getString("eventId"));
                            i.putExtra("notId", object.getString("notId"));

                        }
                    }

                }
                catch (JSONException ex) {

                }
                showOtherNotifications(getApplicationContext(), object , postId, intent);
            }
        }

    }

    private void showOtherNotifications(Context context, JSONObject object, String postId, Intent intent)
    {

        if (context != null && object != null && intent != null)
        {
            NotificationUtils notificationUtils = new NotificationUtils(context);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            int notificationId = (int) System.currentTimeMillis();
            notificationUtils.showNotifications(object, postId, intent, notificationId);
        }

    }

    public static String getTime()
    {
        long time = System.currentTimeMillis() / 1000L;
        if (time < 1000000000000L)
        {
            time *= 1000;
        }
        SimpleDateFormat mSimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        return mSimpleDateFormat.format(time);
    }


}
