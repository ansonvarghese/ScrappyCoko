package com.myscrap.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.myscrap.HomeActivity;
import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.Connect;
import com.myscrap.model.MessageCount;
import com.myscrap.notification.Config;
import com.myscrap.notification.NotificationUtils;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.webservice.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ms3 on 3/28/2017.
 */

public class SocketAPI
{

    private static Socket mSocket;
    private static MyScrapSQLiteDatabase dbHelper;
    private static NotificationUtils notificationUtils;
    private static int i = 1;
    private static boolean isLoadMessageCount = false;
    private static Subscription connectedSubscription;

    public SocketAPI(Context context) {
        dbHelper = MyScrapSQLiteDatabase.getInstance(context);
    }

    private final TrustManager[] trustAllCerts= new TrustManager[] { new X509TrustManager() {

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    } };

    public void  connect()
    {
        if (mSocket == null) {
            try {
                SSLContext sc = null;
                try {
                    sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, null);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    e.printStackTrace();
                }
                IO.Options opts = new IO.Options();
                opts.secure = true;
                opts.reconnection=true;
                opts.sslContext = sc;

                // on the socket services here
                mSocket = IO.socket(Constants.CHAT_SERVER_URL, opts);
                mSocket.on(Socket.EVENT_CONNECT, SocketAPIListener.onConnect);
                mSocket.on(Socket.EVENT_DISCONNECT, SocketAPIListener.onDisconnect);
                mSocket.on(Socket.EVENT_CONNECT_ERROR, SocketAPIListener.onConnectError);
                mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, SocketAPIListener.onConnectError);
                mSocket.on("uploadFileStart", SocketAPIListener.uploadFileStart);
                mSocket.on("my-message-ms", SocketAPIListener.mine);
                mSocket.on("friend-message-ms", SocketAPIListener.other);
                mSocket.on("startTyping", SocketAPIListener.onTyping);
                mSocket.on("stopTyping", SocketAPIListener.onStopTyping);
                mSocket.on("messageSeen", SocketAPIListener.onMessageSeen);
                mSocket.on("isMustLogout", SocketAPIListener.onMustLogout);
                mSocket.on("messageDelivered", SocketAPIListener.onMessageDelivered);
                mSocket.on("userOnline", SocketAPIListener.onUserOnline);

                if (AppController.getInstance().getPrefManager().getUser()!= null)
                {
                    mSocket.connect();
                    AppController.getInstance().setSocketInstance(mSocket);
                    Log.e("Socket", "connected");
                }
                else
                {
                    Log.e("User", "null");
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (AppController.getInstance().getPrefManager().getUser()!= null){
                if (!mSocket.connected()){
                    mSocket.connect();
                    Log.e("Socket", "connected");
                } else {
                    Log.e("Socket", "Already connected");
                }
            } else {
                Log.e("User", "null");
            }
        }
    }

    public static Socket getSocket(){
        return mSocket;
    }


    // disconnection of socket here
    void  disconnect() {
        if (mSocket != null)
        {
            mSocket.off(Socket.EVENT_CONNECT, SocketAPIListener.onConnect);
            mSocket.off(Socket.EVENT_DISCONNECT, SocketAPIListener.onDisconnect);
            mSocket.off(Socket.EVENT_CONNECT_ERROR, SocketAPIListener.onConnectError);
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, SocketAPIListener.onConnectError);
            mSocket.off("uploadFileStart", SocketAPIListener.uploadFileStart);
            mSocket.off("my-message-ms", SocketAPIListener.mine);
            mSocket.off("friend-message-ms", SocketAPIListener.other);
            mSocket.off("startTyping", SocketAPIListener.onTyping);
            mSocket.off("stopTyping", SocketAPIListener.onStopTyping);
            mSocket.off("messageSeen", SocketAPIListener.onMessageSeen);
            mSocket.off("messageDelivered", SocketAPIListener.onMessageDelivered);
            mSocket.off("isMustLogout", SocketAPIListener.onMustLogout);
            mSocket.off("userOnline", SocketAPIListener.onUserOnline);
            mSocket.close();
            mSocket.disconnect();
            Log.e("Socket", "disconnected");
            Log.d("disconnect", "No Internet Connection");
        }
    }

    public interface SocketAPIListener {

        Emitter.Listener onConnect = args -> {
            if (AppController.getInstance() == null)
                return;
            Log.e("Socket", "onConnected");
            if (AppController.getInstance().getPrefManager().getUser()!= null){
                AsyncTask.execute(() -> {
                    sendUserAuthentication();
                    sendNetWorkConnected(AppController.getInstance().getPrefManager().getUser().getId());
                });
            }
        };

        Emitter.Listener onDisconnect = args -> {
            if (AppController.getInstance() == null)
                return;
            Log.e("Socket", "onDisconnect");
        };

        Emitter.Listener onConnectError = args -> {
        };

        Emitter.Listener onTyping = args -> {
            if (AppController.getInstance() == null)
                return;
            AsyncTask.execute(() -> {
                JSONObject data = (JSONObject) args[0];
                String to,from;
                to = data.optString("userid");
                from = data.optString("fromid");
                if (AppController.getInstance().getPrefManager().getUser()!= null && AppController.getInstance().getPrefManager().getUser().getId() != null) {
                    if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                        Intent typing = new Intent(Config.MESSAGE_TYPING);
                        typing.putExtra("fromId", from);
                        LocalBroadcastManager.getInstance(AppController.getInstance()).sendBroadcast(typing);
                    }
                }});
        };

        Emitter.Listener mine = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                if (AppController.getInstance() == null)
                    return;
                AsyncTask.execute(() -> {
                    JSONObject data = (JSONObject) args[0];

                    Log.d("data Rx", data.toString());
                    String id;String to;String from;String time;String msgTime;String seen;String message;String messageType;String messageChat; String messageId; String messageLinkImage; String messageLinkTitle; String messageLinkSubTitle; String messageLinkContent; String name;String profile;String color;String chatName;String chatProfile; int count;
                    String notificationItem;

                    if (dbHelper == null){
                        dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
                    }

                    messageType = data.optString("type");
                    message = data.optString("message");
                    messageChat = data.optString("image");
                    messageId = data.optString("msgId");
                    messageLinkImage = data.optString("messageLinkImage");
                    messageLinkTitle = data.optString("messageLinkTitle");
                    messageLinkSubTitle = data.optString("messageLinkSubTitle");
                    messageLinkContent = data.optString("messageLinkContent");
                    to = data.optString("userid");
                    from = data.optString("fromid");
                    id = messageId;
                    time = data.optString("time");
                    msgTime = data.optString("msgTime");
                    seen = "1";
                    count = 1;
                    name = data.optString("userName");
                    profile = data.optString("userDP");
                    chatName = data.optString("chatName");
                    color = data.optString("color");
                    chatProfile = data.optString("chatProfile");
                    if(messageType != null && !messageType.equalsIgnoreCase("") && messageType.equalsIgnoreCase("1")){

                        if (AppController.getInstance().getPrefManager().getUser() != null && AppController.getInstance().getPrefManager().getUser().getId() != null){
                            if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) || from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {

                                int oldUnReadCount = dbHelper.getChatRoomMessageCount(from);
                                if (from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                    dbHelper.updateMessageCount(from, 0);
                                    dbHelper.addSingleMessage(from, to, message, messageType, messageChat, messageId, messageLinkImage, messageLinkTitle, messageLinkSubTitle, messageLinkContent,  id, msgTime, seen, "",name, profile, color, 0);
                                } else {
                                    if(oldUnReadCount != -1){
                                        count = oldUnReadCount + count;
                                        dbHelper.updateMessageCount(from, count);
                                    }
                                    dbHelper.addSingleMessage(from, to, message, messageType ,messageChat, messageId, messageLinkImage, messageLinkTitle, messageLinkSubTitle, messageLinkContent, id, msgTime, seen, "",chatName, chatProfile, color, count);
                                }

                                if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                    sendMessageReceivedToServer(from, to);
                                    sendAcknowledge(from, to);
                                }


                                long dateValue = Long.parseLong(UserUtils.parsingLong(time));
                                String notificationTime = getChatRoomTime(dateValue);

                                if (!from.equalsIgnoreCase("") && !from.equalsIgnoreCase("null")&& !from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                    notificationItem = chatName +" : "+ message;
                                    if(!UserUtils.isNotificationEnabled(AppController.getInstance()))
                                        return;
                                    AppController.getInstance().getPreferenceManager().addNotification(notificationItem, from);
                                    AppController.getInstance().getPreferenceManager().addMessageRead("unread",from);
                                    Intent resultIntent = new Intent(AppController.getInstance(), HomeActivity.class);
                                    resultIntent.setAction("chat");
                                    resultIntent.putExtra("open", "chat");
                                    if(!from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                        resultIntent.putExtra("chatRoomId", from);
                                    } else {
                                        resultIntent.putExtra("chatRoomId", to);
                                    }
                                    resultIntent.putExtra("color", color);
                                    resultIntent.putExtra("chatRoomName", chatName);
                                    resultIntent.putExtra("sendSeen", true);
                                    resultIntent.putExtra("chatRoomProfilePic", chatProfile);
                                    resultIntent.putExtra("message", message);
                                    //if (!TextUtils.isEmpty(chatProfile)) {
                                    showNotificationMessage(AppController.getInstance(), chatName, message, notificationTime, resultIntent, from, chatProfile, 0);
                                    //}
                                    loadMessageCount();
                                    Log.e("Background", ""+message );
                                }
                            }
                        }
                    } else if(messageType != null && !messageType.equalsIgnoreCase("") && messageType.equalsIgnoreCase("2")){

                        if (AppController.getInstance().getPrefManager().getUser() != null && AppController.getInstance().getPrefManager().getUser().getId() != null){
                            if (from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                dbHelper.updateMessageCount(from, 0);
                                dbHelper.updateSingleMessage(from, to, message, messageType, messageChat, messageId, messageLinkImage, messageLinkTitle, messageLinkSubTitle, messageLinkContent,  id, time, seen, "",name, profile, color, 0);
                                if(messageId != null && !messageId.equalsIgnoreCase("") &&
                                        msgTime != null && !msgTime.equalsIgnoreCase(""))
                                    dbHelper.updateMessageTime(messageId, msgTime);
                            }
                            Log.e("attemptSendImage", "Server");

                            long dateValue = Long.parseLong(UserUtils.parsingLong(time));
                            String notificationTime = getChatRoomTime(dateValue);

                            if (!from.equalsIgnoreCase("") && !from.equalsIgnoreCase("null")&& !from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                notificationItem = chatName +" : "+ message;
                                if(!UserUtils.isNotificationEnabled(AppController.getInstance()))
                                    return;
                                AppController.getInstance().getPreferenceManager().addNotification(notificationItem, from);
                                AppController.getInstance().getPreferenceManager().addMessageRead("unread",from);
                                Intent resultIntent = new Intent(AppController.getInstance(), HomeActivity.class);
                                resultIntent.setAction("chat");
                                resultIntent.putExtra("open", "chat");
                                if(!from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                    resultIntent.putExtra("chatRoomId", from);
                                } else {
                                    resultIntent.putExtra("chatRoomId", to);
                                }
                                resultIntent.putExtra("color", color);
                                resultIntent.putExtra("chatRoomName", chatName);
                                resultIntent.putExtra("sendSeen", true);
                                resultIntent.putExtra("chatRoomProfilePic", chatProfile);
                                resultIntent.putExtra("message", message);
                                //if (!TextUtils.isEmpty(chatProfile)) {
                                showNotificationMessage(AppController.getInstance(), chatName, message, notificationTime, resultIntent, from, chatProfile, 0);
                                //}
                                loadMessageCount();
                                Log.e("Background", ""+message );
                            }
                        }
                    }
                });
            }
        };

        Emitter.Listener other = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                if (AppController.getInstance() == null)
                    return;

                AsyncTask.execute(() -> {
                    JSONObject data = (JSONObject) args[0];
                    String id;String to;String from;String time;String msgTime;String seen;String message;String messageType;String messageChat; String messageId; String messageLinkImage; String messageLinkTitle; String messageLinkSubTitle; String messageLinkContent; String name;String profile;String color;String chatName;String chatProfile; int count;
                    String notificationItem;

                    if (dbHelper == null){
                        dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
                    }

                    messageType = data.optString("type");
                    message = data.optString("message");
                    messageChat = data.optString("image");
                    messageId = data.optString("msgId");
                    messageLinkImage = data.optString("messageLinkImage");
                    messageLinkTitle = data.optString("messageLinkTitle");
                    messageLinkSubTitle = data.optString("messageLinkSubTitle");
                    messageLinkContent = data.optString("messageLinkContent");
                    to = data.optString("userid");
                    from = data.optString("fromid");
                    id = messageId;
                    time = data.optString("time");
                    msgTime = data.optString("msgTime");
                    seen = "1";
                    count = 1;

                    name = data.optString("userName");
                    profile = data.optString("userDP");
                    chatName = data.optString("chatName");
                    color = data.optString("color");
                    chatProfile = data.optString("chatProfile");

                    if(messageType != null && !messageType.equalsIgnoreCase("") && messageType.equalsIgnoreCase("1")){
                        if (AppController.getInstance().getPrefManager().getUser() != null && AppController.getInstance().getPrefManager().getUser().getId() != null){
                            if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) || from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {

                                long dateValue = Long.parseLong(UserUtils.parsingLong(time));
                                String notificationTime = getChatRoomTime(dateValue);

                                if (!from.equalsIgnoreCase("") && !from.equalsIgnoreCase("null")&& !from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                    notificationItem = chatName +" : "+ message;
                                    if(!UserUtils.isNotificationEnabled(AppController.getInstance()))
                                        return;
                                    AppController.getInstance().getPreferenceManager().addNotification(notificationItem, from);
                                    AppController.getInstance().getPreferenceManager().addMessageRead("unread",from);
                                    Intent resultIntent = new Intent(AppController.getInstance(), HomeActivity.class);
                                    resultIntent.setAction("chat");
                                    resultIntent.putExtra("open", "chat");
                                    if(!from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                        resultIntent.putExtra("chatRoomId", from);
                                    } else {
                                        resultIntent.putExtra("chatRoomId", to);
                                    }
                                    resultIntent.putExtra("color", color);
                                    resultIntent.putExtra("chatRoomName", chatName);
                                    resultIntent.putExtra("sendSeen", true);
                                    resultIntent.putExtra("chatRoomProfilePic", chatProfile);
                                    resultIntent.putExtra("message", message);
                                    //if (TextUtils.isEmpty(chatProfile)) {
                                    if (!AppController.isChatRoomActivityRunning())
                                        showNotificationMessage(AppController.getInstance(), chatName, message, notificationTime, resultIntent, from, chatProfile, 0);
                                    else
                                        NotificationUtils.playNotificationSound();
                                    // }

                                    int oldUnReadCount = dbHelper.getChatRoomMessageCount(from);
                                    if (from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                        dbHelper.updateMessageCount(from, 0);
                                        dbHelper.addSingleMessage(from, to, message, messageType, messageChat, messageId, messageLinkImage, messageLinkTitle, messageLinkSubTitle, messageLinkContent,  id, msgTime, seen, "",name, profile, color, 0);
                                    } else {
                                        if(oldUnReadCount != -1){
                                            count = oldUnReadCount + count;
                                            dbHelper.updateMessageCount(from, count);
                                        }
                                        dbHelper.addSingleMessage(from, to, message, messageType ,messageChat, messageId, messageLinkImage, messageLinkTitle, messageLinkSubTitle, messageLinkContent, id, msgTime, seen, "",chatName, chatProfile, color, count);
                                    }

                                    if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                        sendMessageReceivedToServer(from, to);
                                        sendAcknowledge(from, to);
                                    }

                                    loadMessageCount();
                                    Log.e("Background", ""+message );
                                }
                            }
                        }
                    } else if(messageType != null && !messageType.equalsIgnoreCase("") && messageType.equalsIgnoreCase("2")){
                        int oldUnReadCount = dbHelper.getChatRoomMessageCount(from);
                        if (AppController.getInstance().getPrefManager().getUser() != null && AppController.getInstance().getPrefManager().getUser().getId() != null){
                            if (AppController.getInstance().getPrefManager().getUser() != null && AppController.getInstance().getPrefManager().getUser().getId() != null){

                                long dateValue = Long.parseLong(UserUtils.parsingLong(time));
                                String notificationTime = getChatRoomTime(dateValue);
                                if (!from.equalsIgnoreCase("") && !from.equalsIgnoreCase("null")&& !from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                    notificationItem = chatName +" : "+ message;
                                    if(!UserUtils.isNotificationEnabled(AppController.getInstance()))
                                        return;
                                    AppController.getInstance().getPreferenceManager().addNotification(notificationItem, from);
                                    AppController.getInstance().getPreferenceManager().addMessageRead("unread",from);
                                    Intent resultIntent = new Intent(AppController.getInstance(), HomeActivity.class);
                                    resultIntent.setAction("chat");
                                    resultIntent.putExtra("open", "chat");
                                    if(!from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                        resultIntent.putExtra("chatRoomId", from);
                                    } else {
                                        resultIntent.putExtra("chatRoomId", to);
                                    }
                                    resultIntent.putExtra("color", color);
                                    resultIntent.putExtra("chatRoomName", chatName);
                                    resultIntent.putExtra("sendSeen", true);
                                    resultIntent.putExtra("chatRoomProfilePic", chatProfile);
                                    resultIntent.putExtra("message", message);
                                    //if (!TextUtils.isEmpty(chatProfile)) {
                                    if (!AppController.isChatRoomActivityRunning())
                                        showNotificationMessage(AppController.getInstance(), chatName, "sent an image.", notificationTime, resultIntent, from, chatProfile, 1);
                                    else
                                        NotificationUtils.playNotificationSound();
                                    //}

                                    Log.e("Background", ""+message );
                                }
                                if(oldUnReadCount != -1){
                                    count = oldUnReadCount + count;
                                    dbHelper.updateMessageCount(from, count);
                                }
                                dbHelper.addSingleMessage(from, to, message, messageType ,messageChat, messageId, messageLinkImage, messageLinkTitle, messageLinkSubTitle, messageLinkContent, id, msgTime, seen, "",chatName, chatProfile, color, count);
                                if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                    sendMessageReceivedToServer(from, to);
                                    sendAcknowledge(from, to);
                                }
                                loadMessageCount();
                            }
                        }
                    }
                });
            }
        };

        Emitter.Listener onMustLogout = args -> {
            if (AppController.getInstance() == null)
                return;
            AppController.runOnUIThread(HomeActivity::goToLogin);
        };


        Emitter.Listener onStopTyping = args -> {
            if (AppController.getInstance() == null)
                return;
            /*AppController.runOnUIThread(() -> {

            });*/

            AsyncTask.execute(() -> {
                JSONObject data = (JSONObject) args[0];
                String to,from;
                to = data.optString("userid");
                from = data.optString("fromid");
                if (AppController.getInstance().getPrefManager().getUser()!= null && AppController.getInstance().getPrefManager().getUser().getId() != null) {
                    if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) || from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                        Intent stopTyping = new Intent(Config.MESSAGE_STOP_TYPING);
                        stopTyping.putExtra("fromId", from);
                        LocalBroadcastManager.getInstance(AppController.getInstance()).sendBroadcast(stopTyping);
                    }
                }
            });

        };

        Emitter.Listener onMessageSeen = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                if (AppController.getInstance() == null)
                    return;
               /* AppController.runOnUIThread(() -> {


                });*/

                AsyncTask.execute(() -> {
                    JSONObject data = (JSONObject) args[0];
                    String to,from, messageSeen;
                    to = data.optString("userid");
                    from = data.optString("fromid");
                    messageSeen = data.optString("seenTime");
                    if (AppController.getInstance().getPrefManager().getUser()!= null && AppController.getInstance().getPrefManager().getUser().getId() != null){
                        if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            if(dbHelper == null)
                                dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
                            dbHelper.updateSeenStatus(from, messageSeen);
                            Intent msgSeen = new Intent(Config.MESSAGE_SEEN);
                            LocalBroadcastManager.getInstance(AppController.getInstance()).sendBroadcast(msgSeen);
                        }
                    }
                });

            }
        };

        Emitter.Listener onMessageDelivered = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                if (AppController.getInstance() == null)
                    return;
                /*AppController.runOnUIThread(() -> {


                });*/

                AsyncTask.execute(() -> {
                    JSONObject data = (JSONObject) args[0];
                    String to,from;
                    to = data.optString("userid");
                    from = data.optString("fromid");
                    if (AppController.getInstance().getPrefManager().getUser()!= null && AppController.getInstance().getPrefManager().getUser().getId() != null){
                        if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) || from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            if (!from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {

                                if (dbHelper != null) {
                                    dbHelper.updateMessageDeliveredStatus(from);
                                    Intent msgSeen = new Intent(Config.MESSAGE_RECEIVED);
                                    msgSeen.putExtra("fromId", from);
                                    msgSeen.putExtra("toId", to);
                                    LocalBroadcastManager.getInstance(AppController.getInstance()).sendBroadcast(msgSeen);
                                }
                                sendMessageReceivedToServer(to, from);
                            }
                        }
                    }
                });

            }
        };

        Emitter.Listener onUserOnline = args -> {
            if (AppController.getInstance() == null)
                return;
            /*AppController.runOnUIThread(() -> {


            });*/


            AsyncTask.execute(() -> {
                JSONObject data = (JSONObject) args[0];
                String onlineUserId,onlineStatus, color,onlineUserName, onlineUserProfilePicture;
                onlineUserId = data.optString("userID");
                onlineStatus = data.optString("status");
                onlineUserName = data.optString("name");
                color = data.optString("color");
                onlineUserProfilePicture = data.optString("img");
                if (AppController.getInstance().getPrefManager().getUser()!= null && AppController.getInstance().getPrefManager().getUser().getId() != null && !AppController.getInstance().getPrefManager().getUser().getId().equalsIgnoreCase(onlineUserId)){
                    Intent msgSeen = new Intent(Config.USER_ONLINE);
                    msgSeen.putExtra("onlineStatus", onlineStatus);
                    msgSeen.putExtra("onlineUserId", onlineUserId);
                    msgSeen.putExtra("onlineUserName", onlineUserName);
                    msgSeen.putExtra("color", color);
                    msgSeen.putExtra("onlineUserProfilePicture", onlineUserProfilePicture);
                    LocalBroadcastManager.getInstance(AppController.getInstance()).sendBroadcast(msgSeen);
                }
            });

        };

        Emitter.Listener uploadFileStart = args -> {
            JSONObject data = (JSONObject) args[0];
        };
    }

    private static void sendAcknowledge(String from, String to) {
        if (mSocket == null)
            mSocket = SocketAPI.getSocket();

        if (mSocket != null &&  !mSocket.connected())
            mSocket.connect();

        if (mSocket != null && mSocket.connected() && AppController.getInstance().getPrefManager().getUser()!= null){
            JSONObject sendDelivered = new JSONObject();
            try {
                sendDelivered.put("to", from);
                sendDelivered.put("from", to);
                mSocket.emit("messageDelivered", sendDelivered);
            } catch(JSONException e){
                Log.d("messageDelivered", e.toString());
            }
        }

    }

    private static void loadMessageCount() {

        if (isLoadMessageCount)
            return;

        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            isLoadMessageCount =true;
            apiService.messageCount(userId, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<MessageCount>() {
                        @Override
                        public void onCompleted() {
                            Log.d("Message Count", "onCompleted");
                            isLoadMessageCount = false;
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("Message Count", "onError");
                            isLoadMessageCount = false;
                        }

                        @Override
                        public void onNext(MessageCount mMessageCount) {
                            Log.d("Message", "onNext");
                            if(mMessageCount != null){
                                if(!mMessageCount.isErrorStatus()){
                                    String count;
                                    Log.d("Message Count", ""+mMessageCount.getMessageCount());
                                    if(mMessageCount.getMessageCount() > 9){
                                        count = "9+";
                                    } else {
                                        count = String.valueOf(mMessageCount.getMessageCount());
                                        if(count.equalsIgnoreCase(""))
                                            count = "0";
                                    }
                                    UserUtils.setMSGNotificationCount(AppController.getInstance(), count);
                                }
                            }

                        }
                    });
        }
    }

    public static void sendUserAuthentication() {
        if(mSocket == null)
            new SocketAPI(AppController.getInstance()).connect();
            mSocket = SocketAPI.getSocket();

        if (mSocket != null &&  !mSocket.connected())
            mSocket.connect();

        if (mSocket != null && mSocket.connected() && AppController.getInstance().getPrefManager().getUser()!= null){
            JSONObject userAuth = new JSONObject();
            try {
                userAuth.put("username", AppController.getInstance().getPrefManager().getUser().getId());
                mSocket.emit("add-user-ms", userAuth);
                Log.d("sendUserAuthentication", "true");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("sendUserAuthentication", "false");
            }
        }
    }

    public static void sendNetWorkConnected(String from) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        connectedSubscription = apiService.connected(from, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Connect>() {
                    @Override
                    public void onCompleted() {
                        Log.d("ConnectedToApi", "onSuccess");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("ConnectedToApi", "onFailure");
                    }

                    @Override
                    public void onNext(Connect mConnect) {
                        if(mConnect != null)
                            updateResponse(mConnect);
                    }
                });
    }

    @NonNull
    private static void updateResponse(@NonNull Connect mConnect ){
        if (dbHelper == null){
            dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
        }

        if (!mConnect.isErrorStatus()) {
            List<Connect.ConnectData> mConnectData = mConnect.getData();
            if (mConnectData != null && mConnectData.size() > 0) {
                updateResponseData(mConnectData);
            }
            //Connect.UpdatedChat updatedChat = mConnect.getUpdatedchat();
            /*if (updatedChat != null) {
                List<Connect.UpdatedChat.UpdatedChatData> updatedChatData = updatedChat.getUpdatedChat();
            }*/
        }
    }

    private static void updateResponseData(List<Connect.ConnectData> mConnectData) {

        for (Connect.ConnectData msg : mConnectData) {
            String to = msg.getTo()!= null ? msg.getTo() : "";
            String from = msg.getFrom()!= null ? msg.getFrom() : "";
            String time = msg.getTimeStamp()!= null ? msg.getTimeStamp() : "";
            String seenTime = msg.getSeenT() != null ? msg.getSeenT() : "";
            String seen = msg.getMessageStatus() != null ? msg.getMessageStatus() : "";
            String id = String.valueOf(msg.getId()) != null ? String.valueOf(msg.getId()) : "";
            String messageType = msg.getMessageType();
            String messageChat = msg.getMessageChatImage() != null ? msg.getMessageChatImage() : "";
            String messageId = String.valueOf(msg.getId());
            String messageLinkImage = msg.getMessageLinkImage() != null ? msg.getMessageLinkImage() : "";
            String messageLinkTitle = msg.getMessageLinkTitle() != null ? msg.getMessageLinkTitle() : "";
            String messageLinkSubTitle = msg.getMessageLinkSubTitle() != null ? msg.getMessageLinkSubTitle() : "";
            String messageLinkContent = msg.getMessageLinkContent() != null ? msg.getMessageLinkContent() : "";
            String message = msg.getMessage() != null ? msg.getMessage() : "";
            String name = msg.getUserName();
            String profile = msg.getUserProfile();
            String color = msg.getColor();
            String notificationItem = null;
            int count = 1;
            if (dbHelper == null){
                dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
            }
            int oldUnReadCount = dbHelper.getChatRoomMessageCount(from);
            if (AppController.getInstance().getPrefManager().getUser().getId() != null){
                if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) || from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    if (!from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        if(oldUnReadCount != -1){
                            count = oldUnReadCount + count;
                            dbHelper.updateMessageCount(from, count);
                        }
                        dbHelper.addSingleMessage(from, to, message, messageType, messageChat, messageId, messageLinkImage, messageLinkTitle, messageLinkSubTitle, messageLinkContent, id, time, seen, seenTime, name, profile, color, count);
                        //notificationItem = name +" : "+ message;
                    }

                    if (to.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        sendMessageReceivedToServer(from, to);
                        sendAcknowledge(from, to);
                    }

                    String notificationTime = String.valueOf(System.currentTimeMillis());
                                            /*if (notificationItem != null && !from.equalsIgnoreCase("") && !from.equalsIgnoreCase("null")&& !from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                                AppController.getInstance().getPreferenceManager().addNotification(notificationItem, from);
                                                AppController.getInstance().getPreferenceManager().addMessageRead("unread",from);
                                            }*/
                    if(!UserUtils.isNotificationEnabled(AppController.getInstance()))
                        return;
                    Log.e("Background", ""+message );
                    if (i++ == mConnectData.size()) {
                        //AppController.getInstance().getPreferenceManager().addNotification(notificationItem, from);
                        //AppController.getInstance().getPreferenceManager().addMessageRead("unread",from);
                        //Intent resultIntent = new Intent(AppController.getInstance(), MainChatRoomActivity.class);
                        Intent resultIntent = new Intent(AppController.getInstance(), HomeActivity.class);
                        resultIntent.setAction("chat");
                        resultIntent.putExtra("open", "chat");
                        if(!from.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                            resultIntent.putExtra("chatRoomId", from);
                        } else {
                            resultIntent.putExtra("chatRoomId", to);
                        }
                        resultIntent.putExtra("color", color);
                        resultIntent.putExtra("chatRoomName", name);
                        resultIntent.putExtra("sendSeen", true);
                        resultIntent.putExtra("chatRoomProfilePic", profile);
                        resultIntent.putExtra("message", message);
                                                /*if (!TextUtils.isEmpty(profile)) {
                                                    showNotificationMessage(AppController.getInstance(), name, message, notificationTime, resultIntent, from, profile, 0);
                                                }*/

                        if(messageType != null && !messageType.equalsIgnoreCase("")){
                            if(messageType.equalsIgnoreCase("1")){
                                if (!TextUtils.isEmpty(message)) {
                                    showNotificationMessage(AppController.getInstance(), name, message, notificationTime, resultIntent, from, profile, 0);
                                }
                            } else if(messageType.equalsIgnoreCase("2")){
                                if (!TextUtils.isEmpty(profile)) {
                                    showNotificationMessage(AppController.getInstance(), name, "sent an image.", notificationTime, resultIntent, from, profile, 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void sendMessageReceivedToServer(String from, String to) {

        if (!CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
            return;

        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        apiService.sendMessageReceived(from, to, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d("MessageReceived", "onSuccess");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("MessageReceived", "onFailure: Unable to fetch json");
                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
    }

    private static String time() {
        Date now = new Date();
        return Long.toString(now.getTime() / 1000);
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

    /**
     * Showing notification with text only
     */

    // this is the method for receiving messages

    private static void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent, String from, String profile, int notificationType)
    {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, from, intent, profile, notificationType);
        /*if(notificationType == 0){
            notificationUtils.showNotificationMessage(title, message, timeStamp, from, intent, profile);
        } else {
            notificationUtils.showNotificationMessageInboxStyle(title, message, timeStamp, from, intent, profile);
        }*/
    }

    /**
     * Showing notification with text and image
     */
    private static void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String from, String imageUrl)
    {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, "", intent, imageUrl,0);
    }

}
