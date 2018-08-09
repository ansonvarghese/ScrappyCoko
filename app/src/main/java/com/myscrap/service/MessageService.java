package com.myscrap.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.ActiveFriends;
import com.myscrap.model.Bumped;
import com.myscrap.model.ChatRoom;
import com.myscrap.model.ChatRoomResponse;
import com.myscrap.model.Count;
import com.myscrap.model.EnableNotification;
import com.myscrap.model.Markers;
import com.myscrap.model.MyItem;
import com.myscrap.model.Online;
import com.myscrap.model.Viewers;
import com.myscrap.notification.Config;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ms3 on 3/28/2017.
 */

public class MessageService extends Service {

    public static final String ACTION_SEND_MSG = "send_msg";
    public static final String ACTION_START_COMPOSING = "start_composing";
    public static final String ACTION_STOP_COMPOSING = "stop_composing";
    public static final String ACTION_SHOW_ONLINE = "show_online";
    public static final String ACTION_SHOW_OFFLINE = "show_offline";
    public static final String ACTION_LOAD_FRIEND_LIST = "friend_list";
    public static final String ACTION_LOAD_CHAT = "chat_list";
    public static final String ACTION_ENABLE_NOTIFICATION = "notification_enable";
    public static final String ACTION_LOAD_NOTIFICATIONS = "notifications";
    public static final String ACTION_LOAD_COUNT = "count";
    public static final String ACTION_LOAD_VIEWERS_ACK = "ack";
    public static final String ACTION_UPDATE_MARKERS = "markers_update";
    public static final String ACTION_SEND_FCM = "send_fcm";
    public static final String ACTION_SEND_READ = "send_read";
    public static final String ACTION_SEND_IMAGE = "send_image";
    public static final String ACTION_GET_AVATAR = "get_avatar";
    public static final String ACTION_INTERNET_CONNECTED = "connected";
    public static final String ACTION_MESSAGE_DELIVERED = "delivered";
    public static final String ACTION_LOAD_BUMPER_ACK = "bumper_ack";
    public static final String ACTION_RESTART_XMPP_SERVICE = "xmpp_service";

    private SocketAPI sa;
    private Socket mSocket;
    private static volatile boolean mBounded;
    private String TAG = "MessageService";

    private static MyScrapSQLiteDatabase dbHelper;

    private ServiceHandler mServiceHandler;
    private MyScrapSQLiteDatabase mMyScrapSQLiteDatabase;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            startService();
        }
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() == null)
                return;

            if (intent.getAction().equalsIgnoreCase(ACTION_SEND_MSG)) {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                sendMessage(intent);
            } else if (intent.getAction().equalsIgnoreCase(ACTION_LOAD_NOTIFICATIONS)) {
                loadNotification();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_START_COMPOSING)) {

            } else if (intent.getAction().equalsIgnoreCase(ACTION_STOP_COMPOSING)) {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                stopComposing(intent);
            } else if (intent.getAction().equalsIgnoreCase(ACTION_SHOW_ONLINE)) {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                online(intent);
            } else if (intent.getAction().equalsIgnoreCase(ACTION_SHOW_OFFLINE)) {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                offline(intent);
            } else if (intent.getAction().equalsIgnoreCase(ACTION_LOAD_COUNT)) {

            } else if (intent.getAction().equalsIgnoreCase(ACTION_LOAD_VIEWERS_ACK)) {
                sendViewedAck();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_LOAD_BUMPER_ACK)) {
                sendBumperAck();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_RESTART_XMPP_SERVICE)) {
                restartXMPPService();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_UPDATE_MARKERS)) {
                updateMarkers(intent.getStringExtra("companyId"));
            } else if (intent.getAction().equalsIgnoreCase(ACTION_LOAD_FRIEND_LIST)) {
                friendList();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_LOAD_CHAT)) {
                fetchChat();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_ENABLE_NOTIFICATION)) {
                enableNotification();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_SEND_FCM)) {
                sendRegistrationToServer(AppController.getInstance().getPrefManager().getUser().getId(), intent.getStringExtra("token"));
            } else if (intent.getAction().equalsIgnoreCase(ACTION_SEND_READ)) {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                sendRead(intent);
            } else if (intent.getAction().equalsIgnoreCase(ACTION_SEND_IMAGE)) {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                sendImage(intent);
            } else if (intent.getAction().equalsIgnoreCase(ACTION_GET_AVATAR)) {

            } else if (intent.getAction().equalsIgnoreCase(ACTION_INTERNET_CONNECTED)) {

            } else if (intent.getAction().equalsIgnoreCase(ACTION_MESSAGE_DELIVERED)) {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                sendDelivery(intent);
            }
        }
    };


    private void restartXMPPService() {
        //   RoosterConnectionService.getConnection().disconnect();
        AppController.startXMPPService();
    }


    private void sendViewedAck() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())) {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getApplicationContext());
            Log.d("viewersProcessed", "" + userId);
            apiService.visitorsSeen(userId, apiKey)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Subscriber<Viewers>() {
                        @Override
                        public void onCompleted() {
                            Log.d("viewersProcessed", "onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("viewersProcessed", "onFailure");
                        }

                        @Override
                        public void onNext(Viewers viewers) {
                            Log.d("viewersProcessed", "onSuccess");
                        }
                    });
        }
    }


    private void sendBumperAck() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())) {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getApplicationContext());
            Log.d("viewersProcessed", "" + userId);
            apiService.bumperSeen(userId, apiKey)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Subscriber<Bumped>() {
                        @Override
                        public void onCompleted() {
                            Log.d("viewersProcessed", "onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("viewersProcessed", "onFailure");
                        }

                        @Override
                        public void onNext(Bumped viewers) {
                            Log.d("viewersProcessed", "onSuccess");
                        }
                    });
        }
    }


    private void sendDelivery(Intent intent) {
        if (mSocket == null)
            new SocketAPI(AppController.getInstance()).connect();

        mSocket = SocketAPI.getSocket();

        if (mSocket != null) {
            if (mSocket.connected()) {
                JSONObject sendDelivered = new JSONObject();
                try {
                    sendDelivered.put("to", intent.getStringExtra("to"));
                    sendDelivered.put("from", AppController.getInstance().getPrefManager().getUser().getId());
                    mSocket.emit("messageDelivered", sendDelivered);
                } catch (JSONException e) {
                    Log.d("messageDelivered", e.toString());
                }
            } else {
                Log.e("Socket.connected()", String.valueOf(mSocket.connected()));
            }
        }
    }

    private void sendImage(Intent intent) {
        SocketAPI.sendUserAuthentication();
        JSONObject sendText = new JSONObject();
        JSONObject sendNotify = new JSONObject();
        try {
            String time = String.valueOf(System.currentTimeMillis());
            sendNotify.put("to", intent.getStringExtra("to"));
            sendNotify.put("from", intent.getStringExtra("from"));
            sendText.put("to", intent.getStringExtra("to"));
            sendText.put("from", intent.getStringExtra("from"));
            sendText.put("msg", intent.getStringExtra("msg"));
            sendText.put("time", time);
            sendText.put("bitmap", intent.getStringExtra("bitmap"));
            sendText.put("color", intent.getStringExtra("color"));
            sendText.put("uImage", intent.getStringExtra("uImage"));
            sendText.put("uName", intent.getStringExtra("uName"));
            sendText.put("fName", intent.getStringExtra("fromName"));
            sendText.put("fImage", intent.getStringExtra("fromImage"));
            String filePath;
            String message;
            String messageType;
            String profile;
            if (intent.hasExtra("filePath")) {
                filePath = intent.getStringExtra("filePath");
                messageType = "12";
            } else {
                filePath = "";
                messageType = "";
            }

            profile = intent.getStringExtra("profile");
            if (intent.hasExtra("msg")) {
                message = intent.getStringExtra("msg");
            } else {
                message = "";
            }

            if (dbHelper == null) {
                dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
            }

            if (mSocket == null)
                new SocketAPI(AppController.getInstance()).connect();
            mSocket = SocketAPI.getSocket();
            if (mSocket != null) {
                if (mSocket.connected()) {
                    dbHelper.addSingleMessage(intent.getStringExtra("from"), intent.getStringExtra("to"), message, messageType, filePath, "", "", "", "", "", "", time, "", "", "", profile, "", 0);
                    Log.e("attemptSendImage", "Local");
                    mSocket.emit("uploadFileStart", sendText);
                    mSocket.emit("chatnotification", sendNotify);
                    Log.e("SocketImage", "emitted");
                } else {
                    Log.e("Socket.connected()", String.valueOf(mSocket.connected()));
                }
            }
        } catch (JSONException e) {
            Log.e("attemptSendImage", e.toString());
        }
    }

    private void sendRead(Intent intent) {
        if (mSocket == null)
            new SocketAPI(AppController.getInstance()).connect();
        mSocket = SocketAPI.getSocket();

        if (mSocket != null) {
            if (mSocket.connected()) {
                JSONObject sendTypingStop = new JSONObject();
                try {
                    sendTypingStop.put("to", intent.getStringExtra("to"));
                    sendTypingStop.put("from", AppController.getInstance().getPrefManager().getUser().getId());
                    mSocket.emit("messageSeen", sendTypingStop);
                    Log.d("messageSeen", "SEND");

                    new Handler().postDelayed(() -> {
                        if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                            sendMessageSeen(intent.getStringExtra("from"), intent.getStringExtra("to"));
                        }
                    }, 1000);
                } catch (JSONException e) {
                    Log.d("messageSeen", e.toString());
                }
            } else {
                Log.e("Socket.connected()", String.valueOf(mSocket.connected()));
            }
        }
    }

    private void offline(Intent intent) {
        if (mSocket == null)
            new SocketAPI(AppController.getInstance()).connect();
        mSocket = SocketAPI.getSocket();

        if (mSocket != null) {
            if (mSocket.connected()) {
                JSONObject userOnline = new JSONObject();
                try {
                    userOnline.put("status", intent.getStringExtra("status"));
                    userOnline.put("uId", AppController.getInstance().getPrefManager().getUser().getId());
                    mSocket.emit("userOnline", userOnline);
                } catch (JSONException e) {
                    Log.d("userOnline", e.toString());
                }
            } else {
                Log.e("Socket.connected()", String.valueOf(mSocket.connected()));
            }
        }
    }

    private void online(Intent intent) {
        if (mSocket == null)
            new SocketAPI(AppController.getInstance()).connect();
        mSocket = SocketAPI.getSocket();

        if (mSocket != null) {
            if (mSocket.connected()) {
                JSONObject userOnline = new JSONObject();
                try {
                    userOnline.put("status", intent.getStringExtra("status"));
                    userOnline.put("name", intent.getStringExtra("name"));
                    userOnline.put("img", intent.getStringExtra("img"));
                    userOnline.put("color", intent.getStringExtra("color"));
                    userOnline.put("uId", AppController.getInstance().getPrefManager().getUser().getId());
                    mSocket.emit("userOnline", userOnline);
                } catch (JSONException e) {
                    Log.d("userOnline", e.toString());
                }
            } else {
                Log.e("Socket.connected()", String.valueOf(mSocket.connected()));
            }
        }
        setNotify(intent.getStringExtra("status"));
    }

    private void stopComposing(Intent intent) {
        if (mSocket == null)
            new SocketAPI(AppController.getInstance()).connect();

        mSocket = SocketAPI.getSocket();

        if (mSocket != null) {
            if (mSocket.connected()) {
                JSONObject sendTypingStop = new JSONObject();
                try {
                    sendTypingStop.put("to", intent.getStringExtra("to"));
                    sendTypingStop.put("from", AppController.getInstance().getPrefManager().getUser().getId());
                    mSocket.emit("stopTyping", sendTypingStop);
                } catch (JSONException e) {
                    Log.d("typing", e.toString());
                }
            } else {
                Log.e("Socket.connected()", String.valueOf(mSocket.connected()));
            }
        }
    }

    private void sendMessage(Intent intent) {
        JSONObject sendText = new JSONObject();
        JSONObject sendNotify = new JSONObject();
        try {
            sendNotify.put("to", intent.getStringExtra("to"));
            sendNotify.put("from", intent.getStringExtra("from"));
            sendText.put("to", intent.getStringExtra("to"));
            sendText.put("from", intent.getStringExtra("from"));
            sendText.put("msg", intent.getStringExtra("msg"));
            sendText.put("color", intent.getStringExtra("color"));
            sendText.put("time", intent.getStringExtra("time"));
            sendText.put("uImage", intent.getStringExtra("uImage"));
            sendText.put("uName", intent.getStringExtra("uName"));
            sendText.put("fName", intent.getStringExtra("fromName"));
            sendText.put("fImage", intent.getStringExtra("fromImage"));
            if (mSocket == null)
                new SocketAPI(AppController.getInstance()).connect();
            mSocket = SocketAPI.getSocket();

            if (mSocket != null) {
                if (mSocket.connected()) {
                    SocketAPI.sendUserAuthentication();
                    Log.d("data Tx", sendText.toString());
                    mSocket.emit("chat-message", sendText);
                    mSocket.emit("chatnotification", sendNotify);
                } else {
                    Log.e("Socket.connected()", String.valueOf(mSocket.connected()));
                }
            }

        } catch (JSONException e) {
            Log.d("attemptSend", e.toString());
        }
    }

    private void updateMarkers(String companyId) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        apiService.updateMarkers(companyId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Markers>() {
                    @Override
                    public void onCompleted() {
                        Log.e("MarkerList", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("MarkerList", "Failed to update");
                    }

                    @Override
                    public void onNext(Markers markers) {
                        if (markers != null) {
                            if (!markers.isErrorStatus()) {
                                final List<Markers.MarkerData> data = markers.getData();
                                parseData(data);
                                getMarkersUpdatingList(AppController.getInstance().getPrefManager().getUser().getId());
                            }
                        }
                    }
                });
    }

    private void getMarkersUpdatingList(final String id) {
        if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(getApplicationContext());
            apiService.updatingMarkers(id, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Markers>() {
                        @Override
                        public void onCompleted() {
                            Log.d("MarkerList", "onCompleted");
                            sendAcknowledgeToServer(id);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("MarkerList", "Failed to update");
                        }

                        @Override
                        public void onNext(Markers markers) {
                            if (markers != null) {
                                if (!markers.isErrorStatus()) {
                                    final List<Markers.MarkerData> data = markers.getData();
                                    update(data);
                                }
                            }
                        }
                    });
        }
    }

    private void sendAcknowledgeToServer(String id) {
        if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(getApplicationContext());
            Call<Markers> call = apiService.updatedMarkersAck(id, apiKey);
            call.enqueue(new Callback<Markers>() {
                @Override
                public void onResponse(@NonNull Call<Markers> call, @NonNull Response<Markers> response) {
                    if (response.body() != null && response.isSuccessful()) {
                        Markers markers = response.body();
                        if (markers != null) {
                            if (!markers.isErrorStatus()) {
                                Log.d("sendAckToServer", "success");
                            } else {
                                Log.d("sendAckToServer", "false");
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Markers> call, @NonNull Throwable t) {
                    Log.e("sendAcknowledgeToServer", "failed");
                }
            });
        }
    }

    private void update(List<Markers.MarkerData> data) {
        new Handler().post(() -> {
            if (data != null && data.size() > 0) {
                if (mMyScrapSQLiteDatabase == null)
                    mMyScrapSQLiteDatabase = new MyScrapSQLiteDatabase(getApplicationContext());

                for (Markers.MarkerData marker : data) {
                    if (marker.getLatitude() != null && !marker.getLatitude().equalsIgnoreCase("") && marker.getLongitude() != null && !marker.getLongitude().equalsIgnoreCase("")) {
                        double offsetItemLatitude = Double.parseDouble(marker.getLatitude());
                        double offsetItemLongitude = Double.parseDouble(marker.getLongitude());
                        MyItem offsetItem = new MyItem(offsetItemLatitude, offsetItemLongitude, marker.getName(), marker.getCompanyType(), marker.getIsNew(), marker.getState(), marker.getCountry(), marker.getImage(), marker.getId());
                        mMyScrapSQLiteDatabase.updateMarker(offsetItem);
                    }
                }
                if (mMyScrapSQLiteDatabase != null) {
                    mMyScrapSQLiteDatabase.close();
                }
                Log.d("Marker", "Updated");

            } else {
                Log.d("Marker", "Not updated");
            }
        });
    }

    private void parseData(List<Markers.MarkerData> data) {
        new Handler().post(() -> {
            if (data != null && data.size() > 0) {
                if (mMyScrapSQLiteDatabase == null)
                    mMyScrapSQLiteDatabase = new MyScrapSQLiteDatabase(getApplicationContext());

                SQLiteDatabase mSQLiteDatabase = mMyScrapSQLiteDatabase.getWritableDatabase();
                if (mSQLiteDatabase != null) {
                    mSQLiteDatabase.beginTransaction();
                }

                for (Markers.MarkerData marker : data) {
                    if (marker.getLatitude() != null && !marker.getLatitude().equalsIgnoreCase("") && marker.getLongitude() != null && !marker.getLongitude().equalsIgnoreCase("")) {
                        MyItem offsetItem = new MyItem(Double.parseDouble(marker.getLatitude()), Double.parseDouble(marker.getLongitude()), marker.getName(), marker.getCompanyType(), marker.getIsNew(), marker.getState(), marker.getCountry(), marker.getImage(), marker.getId());
                        mMyScrapSQLiteDatabase.addMarker(offsetItem);
                    }
                }

                if (mSQLiteDatabase != null) {
                    mSQLiteDatabase.setTransactionSuccessful();
                    mSQLiteDatabase.endTransaction();
                    mSQLiteDatabase.close();
                }

                Log.e("MarkerList", "Updated");
            } else {
                Log.e("MarkerList", "Not updated");
            }
        });
    }

    private void enableNotification() {
        if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(getApplicationContext());
            apiService.enableNotification(AppController.getInstance().getPrefManager().getUser().getId(), null, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<EnableNotification>() {
                        @Override
                        public void onCompleted() {
                            Log.d("EnableNotification", "onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("EnableNotification", "Failed to update");
                        }

                        @Override
                        public void onNext(EnableNotification notification) {
                            if (notification != null) {
                                if (!notification.isErrorStatus()) {
                                    if (notification.isNotificationEnabled()) {
                                        UserUtils.saveNotificationEnable(getApplicationContext(), "1");
                                        Log.d("EnableNotification", "true");
                                    } else {
                                        UserUtils.saveNotificationEnable(getApplicationContext(), "0");
                                        Log.d("EnableNotification", "false");
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private void fetchChat() throws ArrayIndexOutOfBoundsException {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(this);
        apiService.getChatRoom(userId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ChatRoomResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.d("Chat", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Chat", "updated fails");
                    }

                    @Override
                    public void onNext(ChatRoomResponse response) {
                        if (response != null) {
                            if (!response.isErrorStatus()) {
                                if (response.getResults() != null)
                                    chatRooms(response.getResults());
                            }
                        } else {
                            Log.e("Chat", "updated fails");
                        }
                    }
                });
    }

    private void chatRooms(List<ChatRoom> chatRooms) {
        if (chatRooms != null && !chatRooms.isEmpty()) {
            if (mMyScrapSQLiteDatabase == null)
                mMyScrapSQLiteDatabase = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());

            if (mMyScrapSQLiteDatabase != null)
                mMyScrapSQLiteDatabase.deleteChatRoomTable();

            Log.d("Chat", "deleted");
            Log.d("Chat", "updating");
            AppController.getInstance().getPreferenceManager().clearMessageRead();
            int k;
            for (k = 0; k < chatRooms.size(); k++) {
                String profilePicture = chatRooms.get(k).getProfilePic();
                String color = chatRooms.get(k).getColor();
                String profileName = chatRooms.get(k).getName();
                int unreadMessageCount = chatRooms.get(k).getUnReadMessageCount();
                List<ChatRoom> mChatRoomMessagesCopy = chatRooms.get(k).getData();
                if (mChatRoomMessagesCopy != null && !mChatRoomMessagesCopy.isEmpty()) {
                    for (int i = 0; i < mChatRoomMessagesCopy.size(); i++) {
                        mMyScrapSQLiteDatabase.addMessage(mChatRoomMessagesCopy.get(i).getFrom(), mChatRoomMessagesCopy.get(i).getTo(), mChatRoomMessagesCopy.get(i).getMessage() != null ? mChatRoomMessagesCopy.get(i).getMessage() : "", mChatRoomMessagesCopy.get(i).getMessageType(), mChatRoomMessagesCopy.get(i).getMessageChatImage() != null ? mChatRoomMessagesCopy.get(i).getMessageChatImage() : "", String.valueOf(mChatRoomMessagesCopy.get(i).getId()), "", "", "", "", String.valueOf(mChatRoomMessagesCopy.get(i).getId()), mChatRoomMessagesCopy.get(i).getTimeStamp(), mChatRoomMessagesCopy.get(i).getStatus(), mChatRoomMessagesCopy.get(i).getSeenT(), profileName, profilePicture, color, unreadMessageCount);
                    }
                }
            }
            Log.d("Chat", "updated success");
        }
    }

    private void loadNotification() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())) {
            Log.e("LOAD NOTIFICATION", "CALLED: ");
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            apiService.notificationsCount(userId, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Count>() {
                        @Override
                        public void onCompleted() {
                            Log.d("loadNotificationCount", "onCompleted: ");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("loadNotificationCount", "onError: ");
                        }

                        @Override
                        public void onNext(Count mCount) {
                            Log.d("loadNotificationCount", "onNext: ");
                            if (mCount != null) {
                                updateCount(mCount);
                            }

                        }
                    });
        }
    }

    private void updateCount(Count mCount) {
        if (mCount != null) {
            if (!mCount.isErrorStatus()) {
                int count = mCount.getViewersCount();
                Log.d("ViewersCount", "" + mCount.getViewersCount());
                if (count != -1) {
                    UserUtils.setViewersCount(AppController.getInstance(), String.valueOf(count));
                }

                String messageCount;
                Log.d("MessageCount", "" + mCount.getMessageCount());
                if (mCount.getMessageCount() > 9) {
                    messageCount = "9+";
                } else {
                    messageCount = String.valueOf(mCount.getMessageCount());
                    if (messageCount.equalsIgnoreCase(""))
                        messageCount = "0";
                }
                UserUtils.setProfileCompleteness(AppController.getInstance(), String.valueOf(mCount.getProfilePercentage()));
                UserUtils.setMSGNotificationCount(AppController.getInstance(), messageCount);

                String bumpedCount;
                Log.d("BumpedCount", "" + mCount.getBumpedCount());
                if (mCount.getBumpedCount() > 9) {
                    bumpedCount = "9+";
                } else {
                    bumpedCount = String.valueOf(mCount.getBumpedCount());
                    if (bumpedCount.equalsIgnoreCase(""))
                        bumpedCount = "0";
                }
                UserUtils.setBumpedCount(AppController.getInstance(), bumpedCount);

                int notificationCount;
                Log.d("notificationCount", "" + mCount.getNotificationCount());
                notificationCount = mCount.getNotificationCount();
                if (notificationCount != -1) {
                    UserUtils.setNotificationCount(AppController.getInstance(), String.valueOf(notificationCount));
                    Log.d("SaveNotification3", "" + mCount.getNotificationCount());
                }


                int moderatorCount;
                Log.d("moderatorCount", "" + mCount.getModeratorCount());
                moderatorCount = mCount.getModeratorCount();
                if (moderatorCount != -1) {
                    UserUtils.setModeratorNotificationCount(AppController.getInstance(), String.valueOf(moderatorCount));
                }
            }
        }
    }

    private void friendList() {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(getApplicationContext());
        apiService.getFriendsList(userId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ActiveFriends>() {
                    @Override
                    public void onCompleted() {
                        Log.d("FriendsList", "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("FriendsList", "onError: ");
                    }

                    @Override
                    public void onNext(ActiveFriends activeFriends) {
                        if (activeFriends != null) {
                            Gson gson = new Gson();
                            String mFriendList = gson.toJson(activeFriends);
                            UserUtils.saveUserFriendLists(getApplicationContext(), mFriendList);
                        }
                        Log.d("FriendsList", "onNext: ");
                    }
                });
    }

    private static void setNotify(final String onlineStatus) {

        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())) {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            Call<Online> call = apiService.online(AppController.getInstance().getPrefManager().getUser().getId(), onlineStatus, apiKey);
            call.enqueue(new Callback<Online>() {
                @Override
                public void onResponse(@NonNull Call<Online> call, @NonNull retrofit2.Response<Online> response) {
                    if (response.body() != null && response.isSuccessful()) {
                        Online mOnline = response.body();
                        if (mOnline != null) {
                            if (mOnline.getOnlineStatus() != null && !mOnline.getOnlineStatus().equalsIgnoreCase("")) {
                                if (mOnline.getOnlineStatus().equalsIgnoreCase("success")) {
                                    Log.d("Online", "ONLINE");
                                } else {
                                    Log.d("Online", "OFFLINE");
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Online> call, @NonNull Throwable t) {
                    Log.e("Online", "Failed to update");
                }
            });
        }
    }

    private static void sendRegistrationToServer(String userId, final String gcmCode) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        apiService.updateGcmId(userId, gcmCode, apiKey)
                .retry(3)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("sendGCM", "onFailure");
                        /*String token = UserUtils.getFireBaseInstanceId(AppController.getInstance());
                        if (AppController.getInstance().getPrefManager().getUser() == null)
                            return;
                        sendRegistrationToServer(AppController.getInstance().getPrefManager().getUser().getId(), token);*/
                    }

                    @Override
                    public void onNext(String string) {
                        if (string != null) {
                            if (string.equalsIgnoreCase("success")) {
                                Intent registrationComplete = new Intent(Config.SENT_TOKEN_TO_SERVER);
                                LocalBroadcastManager.getInstance(AppController.getInstance()).sendBroadcast(registrationComplete);
                            }
                        }
                    }
                });
    }

    private void sendMessageSeen(final String from, final String to) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        apiService.sendSeen(from, to, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d("MessageSeen", "onSuccess");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("MessageSeen", "onFailure: Unable to fetch json");
                    }

                    @Override
                    public void onNext(String s) {
                    }
                });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SEND_MSG);
        filter.addAction(ACTION_START_COMPOSING);
        filter.addAction(ACTION_STOP_COMPOSING);
        filter.addAction(ACTION_SHOW_ONLINE);
        filter.addAction(ACTION_SHOW_OFFLINE);
        filter.addAction(ACTION_LOAD_FRIEND_LIST);
        filter.addAction(ACTION_LOAD_CHAT);
        filter.addAction(ACTION_LOAD_NOTIFICATIONS);
        filter.addAction(ACTION_ENABLE_NOTIFICATION);
        filter.addAction(ACTION_UPDATE_MARKERS);
        filter.addAction(ACTION_LOAD_VIEWERS_ACK);
        filter.addAction(ACTION_SEND_FCM);
        filter.addAction(ACTION_SEND_READ);
        filter.addAction(ACTION_SEND_IMAGE);
        filter.addAction(ACTION_GET_AVATAR);
        filter.addAction(ACTION_INTERNET_CONNECTED);
        filter.addAction(ACTION_MESSAGE_DELIVERED);
        filter.addAction(ACTION_RESTART_XMPP_SERVICE);
        filter.addAction(ACTION_LOAD_BUMPER_ACK);
        registerReceiver(broadcastReceiver, filter);
//

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);



            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("MyScrap")
                    .setContentText("Connected").build();

                    startForeground(1, notification);
        }catch (Exception e) {
    // This will catch any exception, because they are all descended from Exception
                Log.e( "NOtexp ",e.toString() );
      }

        }


        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        if (mMyScrapSQLiteDatabase == null)
            mMyScrapSQLiteDatabase = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());

        // Get the HandlerThread's Looper and use it for our Handler
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("MessageService :", "onStartCommand");
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return START_NOT_STICKY;

    }

    public void startService() {
        if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
            if (mSocket == null) {
                Log.d("Socket: ", "null");
                sa = new SocketAPI(getApplicationContext());
                sa.connect();
                mSocket = SocketAPI.getSocket();
                Log.d("Service: ", "Started");
            } else {
                if (sa == null)
                    sa = new SocketAPI(getApplicationContext());
                sa.connect();
                Log.d("Socket: ", "Already connected " + mSocket.connected());
            }
        } else {
            Log.d("Service", "Service not started due to Bad Internet");
        }

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(broadcastReceiver);
        if (sa != null)
            sa.disconnect();
        Log.d("Service: ", "Destroy");
        AppController.service();
        super.onDestroy();
    }
}
