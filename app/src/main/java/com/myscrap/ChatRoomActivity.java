package com.myscrap;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.myscrap.adapters.ChatRoomMessageAdapter;
import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.ChatRoom;
import com.myscrap.model.ChatRoomResponse;
import com.myscrap.model.Online;
import com.myscrap.notification.Config;
import com.myscrap.notification.NotificationUtils;
import com.myscrap.service.MessageService;
import com.myscrap.service.OnlineNotifierService;
import com.myscrap.service.SocketAPI;
import com.myscrap.utils.BitmapUtils;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.ImageUtils;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import io.socket.client.Socket;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ms3 on 3/15/2017.
 */

public class ChatRoomActivity extends AppCompatActivity implements SearchView.OnQueryTextListener
{

    private static RecyclerView mMessagesView;
    private ChatRoomMessageAdapter mAdapter;
    private List<ChatRoom> mChatRoomMessages = new ArrayList<>();
    private ChatRoomActivity mChatRoomActivity;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private static final int TYPING_TIMER_LENGTH = 1000 * 10;
    private static String chatRoomId, push, color, chatRoomName, chatRoomImage;
    private EmojiconEditText emojiconEditText;
    public static final String SET_NOTIFY = "set_notify";
    public static final String SET_UPDATE_NOTIFY = "set_update_notify";
    public static final IntentFilter INTENT_FILTER = createIntentFilter();
    private BroadcastReceiver setNotifyReceiver;
    private static MyScrapSQLiteDatabase dbHelper;
    private TextView toolbarSubTitle;
    private List<String> indexes = new ArrayList<>();
    private List<Integer> indexesAfterSeen = new ArrayList<>();
    private String userOnline ="";
    private String userLastSeen ="";
    private String userPosition ="";
    private String userCompany ="";
    private String userCountry ="";
    private String userCompanyId ="";
    private Handler mClearNotificationHandler = new Handler();
    private boolean isVisible = false;
    private static String fromImage;
    private static String fromName;
    private TextView mPosition;
    private View shadow;
    private LinearLayout topLayout;
    //private TextView noMessages;
    private Intent serviceIntent;
    private Tracker mTracker;

    private static final int REQUEST_CODE_READ_PERMISSION = 22;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 23;
    private static final int REQUEST_GALLERY = 21;
    private static final int REQUEST_IMAGE_CAPTURE = 20;
    private static final int REQUEST_IMAGE_GALLERY = 24;

    private static Socket mSocket;
    private String TAG = "CHAT_IMAGE";
    private boolean isUserScrolling = false;
    private boolean isListGoingUp = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;
    private boolean isFetching = false;
    private int lastDisplayedPosition = 0;
    private View emptyView;
    private FrameLayout messageLayout;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        View toolbarView = findViewById(R.id.chat_room_toolbar);


        messageLayout = findViewById(R.id.message_layout);
        emptyView = findViewById(R.id.empty);
        UserUtils.setEmptyView(emptyView, R.drawable.ic_pm_empty, "No Messages", false);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setDistanceToTriggerSync(10);// in dips
        LinearLayout toolbarLayout = toolbar.findViewById(R.id.action_bar_title);
        ImageView toolbarHomeUp = toolbar.findViewById(R.id.home_indicator);
        final SimpleDraweeView toolbarContactImage = toolbar.findViewById(R.id.conversation_contact_photo);
        TextView iconText = toolbarView.findViewById(R.id.icon_text);
        TextView toolbarTitle = toolbarView.findViewById(R.id.action_bar_title_1);
        toolbarSubTitle = toolbarView.findViewById(R.id.action_bar_title_2);
        topLayout = findViewById(R.id.topLayout);
        shadow = findViewById(R.id.shadow);
        mPosition = findViewById(R.id.position);
     //   noMessages = findViewById(R.id.no_messages);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mChatRoomActivity= this;
        mTracker = AppController.getInstance().getDefaultTracker();
        mSocket = SocketAPI.getSocket();
        dbHelper = MyScrapSQLiteDatabase.getInstance(mChatRoomActivity);


        // user Jid
        String userJid = UserUtils.getUserJid(ChatRoomActivity.this);



        //we are authenticating user here
        SocketAPI.sendUserAuthentication();


        mMessagesView = findViewById(R.id.messages);
        mMessagesView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setInitialPrefetchItemCount(4);
        layoutManager.setReverseLayout(false);
        layoutManager.setStackFromEnd(true);
        mMessagesView.setLayoutManager(layoutManager);
        mMessagesView.addOnScrollListener(new RecyclerView.OnScrollListener() {
           @Override
           public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
               super.onScrolled(recyclerView, dx, dy);
               lastDisplayedPosition = layoutManager.findLastVisibleItemPosition();
               if(isUserScrolling){
                   isListGoingUp = dy <= 0;
                   if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                       onHide();
                       controlsVisible = false;
                       scrolledDistance = 0;
                   } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                       onShow();
                       controlsVisible = true;
                       scrolledDistance = 0;
                   }

                   if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                       scrolledDistance += dy;
                   }
               }

           }


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState ==  RecyclerView.SCROLL_STATE_DRAGGING){
                    isUserScrolling = true;

                } else if(newState ==  RecyclerView.SCROLL_STATE_IDLE){
                    if(isListGoingUp){
                        loadMore();
                    }
                }
            }
        });




        //   scroll to bottom
        fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(v -> {
            if(mAdapter != null && mMessagesView != null) {
                mMessagesView.post(() -> {
                    if (mAdapter.getItemCount() > 1) {
                        mMessagesView.getLayoutManager().smoothScrollToPosition(mMessagesView, null, mAdapter.getItemCount() - 1);
                    }
                });

            }
        });





        Intent incomingIntent = getIntent();
        if (incomingIntent != null) {
            chatRoomId = incomingIntent.getStringExtra("chatRoomId");
            color = incomingIntent.getStringExtra("color");
            push = incomingIntent.getStringExtra("page");
            chatRoomName = incomingIntent.getStringExtra("chatRoomName");
            chatRoomImage = incomingIntent.getStringExtra("chatRoomProfilePic");

            if (mClearNotificationHandler != null) {
                mClearNotificationHandler.removeCallbacks(clear);
                mClearNotificationHandler.postDelayed(clear, 1000);
            }

            dbHelper.updateMessageCount(chatRoomId, 0);
            serviceIntent = new Intent(this, OnlineNotifierService.class);

            if (chatRoomImage != null ){
                if(chatRoomImage.equalsIgnoreCase("") ||
                         chatRoomImage.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || chatRoomImage.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    toolbarContactImage.setImageResource(R.drawable.bg_circle);
                    if(color != null && !color.equalsIgnoreCase("") && color.startsWith("#")){
                        toolbarContactImage.setColorFilter(Color.parseColor(color));
                    } else {
                        toolbarContactImage.setColorFilter(DeviceUtils.getRandomMaterialColor(mChatRoomActivity, "400"));
                    }
                    iconText.setVisibility(View.VISIBLE);
                    if (chatRoomName != null && !chatRoomName.equalsIgnoreCase("")){
                        String[] split = chatRoomName.split("\\s+");
                        if (split.length > 1){
                            String first = split[0].substring(0,1);
                            String last = split[1].substring(0,1);
                            String initial = first + ""+ last ;
                            iconText.setText(initial.toUpperCase());
                        } else {
                            if (split[0] != null && split[0].trim().length() == 1) {
                                String first = split[0].substring(0,1);
                                iconText.setText(first.toUpperCase());
                            }
                        }
                    }
                } else {
                    Uri uri = Uri.parse(chatRoomImage);
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    toolbarContactImage.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    toolbarContactImage.setImageURI(uri);
                    toolbarContactImage.setColorFilter(null);
                    iconText.setVisibility(View.GONE);
                }
            }

            getMessages(chatRoomImage);
        }





        fromImage = UserUtils.getUserProfilePicture(mChatRoomActivity);
        String firstName = UserUtils.getFirstName(mChatRoomActivity);
        String lastName = UserUtils.getLastName(mChatRoomActivity);
        fromName = firstName + " " + lastName;
        toolbarTitle.setText(chatRoomName);
        toolbarContactImage.setOnClickListener(v -> goToProfile(chatRoomId));
        toolbarLayout.setOnClickListener(v -> goToProfile(chatRoomId));
        ImageView smileyImageView = findViewById(R.id.emoji_btn);
        final LinearLayout submitButtonLayout = findViewById(R.id.submit_btn_layout);
        final ImageView submitButton = findViewById(R.id.submit_btn);
        final ImageView attachButton = findViewById(R.id.attach_btn);
        if (smileyImageView != null)
            smileyImageView.setColorFilter(Color.parseColor("#388E3C"));

        View rootView = findViewById(R.id.root_bottom_view);
        emojiconEditText = findViewById(R.id.emojicon_edit_text);
        EmojIconActions smileyIcon = new EmojIconActions(this, rootView, emojiconEditText, smileyImageView);
        smileyIcon.ShowEmojIcon();
        smileyIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        smileyIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
            }
            @Override
            public void onKeyboardClose() {
            }
        });
        if(emojiconEditText != null)
        emojiconEditText.setOnEditorActionListener((v, id, event) -> {
            if (id == R.id.send || id == EditorInfo.IME_NULL) {
                if (CheckNetworkConnection.isConnectionAvailable(mChatRoomActivity))
                    attemptSend();
                else
                    if(emojiconEditText != null)
                        SnackBarDialog.showNoInternetError(emojiconEditText);
                return true;
            }
            return false;
        });

        emojiconEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0){
                    submitButton.setColorFilter(ContextCompat.getColor(mChatRoomActivity, R.color.colorPrimary));
                } else {
                    submitButton.setColorFilter(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if(toolbarHomeUp != null) {
            toolbarHomeUp.setOnClickListener(v -> onBackPressed());
        }


        submitButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(emojiconEditText.getText().toString())){
                Toast.makeText(mChatRoomActivity, "Enter a message", Toast.LENGTH_SHORT).show();
            } else {
                if (CheckNetworkConnection.isConnectionAvailable(mChatRoomActivity))
                    attemptSend();
                else
                    Snackbar.make(submitButton, "Check your internet connection", Snackbar.LENGTH_SHORT).show();
            }
        });

        submitButtonLayout.setOnClickListener(v -> {
            if (TextUtils.isEmpty(emojiconEditText.getText().toString())){
                Toast.makeText(mChatRoomActivity, "Enter a message", Toast.LENGTH_SHORT).show();
            } else {
                if (CheckNetworkConnection.isConnectionAvailable(mChatRoomActivity))
                    attemptSend();
                else
                    Snackbar.make(submitButtonLayout, "Check your internet connection", Snackbar.LENGTH_SHORT).show();
            }
        });

        attachButton.setOnClickListener(v -> {
            if (CheckNetworkConnection.isConnectionAvailable(mChatRoomActivity))
                upload();
            else
                Snackbar.make(submitButtonLayout, "Check your internet connection", Snackbar.LENGTH_SHORT).show();
        });

        emojiconEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mSocket != null && !mSocket.connected()) return;
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if (mSocket != null && !mTyping) {
                    mTyping = true;
                    JSONObject sendTypingStart = new JSONObject();
                    try {
                        sendTypingStart.put("to", chatRoomId);
                        sendTypingStart.put("from", AppController.getInstance().getPrefManager().getUser().getId());
                        mSocket.emit("startTyping", sendTypingStart);
                    } catch(JSONException e){
                        Log.d("typing", e.toString());
                    }
                }
                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });









        setNotifyReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;

                if(intent.getAction() == null )
                    return;

                if (intent.getAction().equals(SET_NOTIFY)) {
                    dbHelper.updateMessageCount(chatRoomId, 0);
                     if( mChatRoomMessages != null && intent.hasExtra("updateByTime") && intent.getBundleExtra("updateByTime") != null){

                         /*mChatRoomMessages = dbHelper.getChatRoomMessage(chatRoomId);

                         if (mChatRoomMessages.isEmpty())
                             return;*/

                         Bundle bundle = intent.getBundleExtra("updateByTime");
                         if(bundle != null ){
                             ChatRoom chatRoom = (ChatRoom) bundle.getSerializable("updateByTime");

                             if(chatRoom != null){
                                 mChatRoomMessages.add(chatRoom);
                                 int updatePosition = 0;
                                 int i= 0;
                                 for(ChatRoom cRoom : mChatRoomMessages){
                                     if(cRoom != null){
                                         if(cRoom.equals(chatRoom)){
                                             updatePosition = i;
                                             break;
                                         }
                                     }
                                     i++;
                                 }
                                 if(chatRoom.getMessageType().equalsIgnoreCase("1"))
                                 {
                                     if(!mChatRoomMessages.get(mChatRoomMessages.size()-1).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {

                                         sendMessageSeen();
                                         long NOW = System.currentTimeMillis() / 1000L;
                                         String postingTime = Long.toString(NOW);
                                         if (dbHelper == null)
                                             dbHelper = MyScrapSQLiteDatabase.getInstance(mChatRoomActivity);
                                         dbHelper.updateSeenStatus(chatRoomId, postingTime);
                                     }
                                 }
                                 mAdapter.notifyItemInserted(updatePosition);
                                 setNotifyChanges(true);

                             }
                         }
                     }
                    NotificationUtils.clearNotificationByID(Integer.parseInt(UserUtils.parsingInteger(chatRoomId)));
                }
                else if (mChatRoomMessages != null &&  intent.getAction().equals(SET_UPDATE_NOTIFY))
                {


                    dbHelper.updateMessageCount(chatRoomId, 0);
                    /*mChatRoomMessages = dbHelper.getChatRoomMessage(chatRoomId);
                    if (mChatRoomMessages.isEmpty())
                        return;*/
                    if(intent.hasExtra("updateByTime") && intent.getBundleExtra("updateByTime") != null){
                        Bundle bundle = intent.getBundleExtra("updateByTime");
                        if(bundle != null){
                            ChatRoom chatRoom = (ChatRoom) bundle.getSerializable("updateByTime");
                            if(chatRoom != null){
                                int updatePosition = 0;
                                int i= 0;
                                for(ChatRoom cRoom : mChatRoomMessages){
                                    if(cRoom != null){
                                        if(cRoom.equals(chatRoom)){
                                            updatePosition = i;
                                            break;
                                        }
                                    }
                                    i++;

                                }
                                if(chatRoom.getMessageType().equalsIgnoreCase("2")){
                                    if(mChatRoomMessages.size() > 0 && !mChatRoomMessages.get(mChatRoomMessages.size()-1).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                                        sendMessageSeen();
                                        long NOW = System.currentTimeMillis() / 1000L;
                                        String postingTime = Long.toString(NOW);
                                        if (dbHelper == null)
                                            dbHelper = MyScrapSQLiteDatabase.getInstance(mChatRoomActivity);
                                        dbHelper.updateSeenStatus(chatRoomId, postingTime);
                                    }
                                }
                                mChatRoomMessages.set(updatePosition, chatRoom);
                                mAdapter.notifyItemChanged(updatePosition);
                                setNotifyChanges(true);
                            }
                        }
                    }
                    NotificationUtils.clearNotificationByID(Integer.parseInt(UserUtils.parsingInteger(chatRoomId)));
                } else if ( intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    if (!intent.getStringExtra("fromId").equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        sendMessageSeen();
                        long NOW = System.currentTimeMillis() / 1000L;
                        String postingTime = Long.toString(NOW);
                        if(dbHelper == null)
                            dbHelper = MyScrapSQLiteDatabase.getInstance(mChatRoomActivity);
                        dbHelper.updateSeenStatus(chatRoomId, postingTime);
                        dbHelper.updateMessageCount(chatRoomId, 0);
                        NotificationUtils.clearNotificationByID(Integer.parseInt(UserUtils.parsingInteger(chatRoomId)));
                    }
                } else if (intent.getAction().equals(Config.MESSAGE_TYPING)) {
                    String typingFrom = intent.getStringExtra("fromId");
                    if (chatRoomId.equalsIgnoreCase(typingFrom)){
                        doTyping();
                        mTypingHandler.removeCallbacks(onTypingTimeout);
                        mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
                    }
                } else if (intent.getAction().equals(Config.MESSAGE_STOP_TYPING)) {
                    String typingFrom = intent.getStringExtra("fromId");
                    if (chatRoomId.equalsIgnoreCase(typingFrom)){
                        stopTyping();
                    }
                }
                else if (intent.getAction().equals(Config.MESSAGE_SEEN))
                {
                    setNotifyChanges(true);
                    NotificationUtils.clearNotificationByID(Integer.parseInt(UserUtils.parsingInteger(chatRoomId)));
                }
                else if (intent.getAction().equals(Config.MESSAGE_RECEIVED))
                {
                    setNotifyChanges(true);
                    NotificationUtils.clearNotificationByID(Integer.parseInt(UserUtils.parsingInteger(chatRoomId)));
                }
                else if (intent.getAction().equals(Config.USER_ONLINE))
                {
                    String onlineStatus = intent.getStringExtra("onlineStatus");
                    String onlineUserId = intent.getStringExtra("onlineUserId");
                    if (chatRoomId.equalsIgnoreCase(onlineUserId)){
                        if(onlineStatus.equalsIgnoreCase("1")){
                            userOnline = "Online";
                            if(toolbarSubTitle != null){
                                toolbarSubTitle.setText(userOnline);
                                toolbarSubTitle.setVisibility(View.VISIBLE);
                            }
                        }
                        else
                        {
                            userOnline = "";
                            if(toolbarSubTitle != null)
                            {
                                toolbarSubTitle.setVisibility(View.GONE);
                            }
                        }
                    }
                }
                else if (intent.getAction().equals(OnlineNotifierService.BROADCAST_ACTION))
                {
                    Gson gson = new Gson();
                    Online.OnlineData mData =  gson.fromJson(intent.getStringExtra("response"), Online.OnlineData.class);
                    if(mData != null)
                    {

                        if (mData.isOnline()) {
                            userOnline = "Online";
                            toolbarSubTitle.setText(userOnline);
                            toolbarSubTitle.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            userOnline = "";
                            if(mData.getTimeStamp() != null && !mData.getTimeStamp().equalsIgnoreCase(""))
                            {
                                userLastSeen = mData.getTimeStamp();
                                toolbarSubTitle.setText(userLastSeen);
                                toolbarSubTitle.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                userLastSeen = "";
                                toolbarSubTitle.setVisibility(View.GONE);
                            }
                        }

                        if(mData.getUserCountry() != null && !mData.getUserCountry().equalsIgnoreCase("")){
                            userCountry = mData.getUserCountry();
                        }
                        else
                        {
                            userCountry = "";
                        }

                        if (mData.getUserCompany() != null && !mData.getUserCompany().equalsIgnoreCase("")) {
                            userCompany = mData.getUserCompany();
                        } else {
                            userCompany = "";
                        }

                        if (mData.getCompanyId() != null && !mData.getCompanyId().equalsIgnoreCase("")) {
                            userCompanyId = mData.getCompanyId();
                        } else {
                            userCompanyId = "";
                        }

                        if (mData.getUserDesignation() != null && !mData.getUserDesignation().equalsIgnoreCase("")) {
                            userPosition = mData.getUserDesignation();
                        }
                        else
                        {
                            userPosition = "Trader";
                        }
                        online();
                    }
                }
            }
        };

    }




    private void loadMore() {
        if(layoutManager != null && layoutManager.findFirstCompletelyVisibleItemPosition() == 0){
            if (dbHelper == null)
               dbHelper = MyScrapSQLiteDatabase.getInstance(this);
            List<ChatRoom> newChatRooms = dbHelper.getChatRoomMessage(chatRoomId);
            if(!isFetching && newChatRooms != null){
                if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                    if(!newChatRooms.isEmpty()){
                        showTopRefreshingView();
                        int lastDisplayedPositionById = newChatRooms.get(0).getId();
                        if (lastDisplayedPositionById != -1)
                            loadMoreMessages(lastDisplayedPositionById);
                    }
                }
            }
        }  else {
            hideTopRefreshingView();
        }
    }


    private void showTopRefreshingView(){
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
        }
    }

    private void hideTopRefreshingView(){
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
        }
    }

    private void setToolBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void setToolBarSubTitle(String subTitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subTitle);
        }
    }

    public void online()
    {
        if (chatRoomName != null && !chatRoomName.equalsIgnoreCase(""))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SpannableStringBuilder spannedDetails;
                if(userPosition != null && !userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+userCompany+"&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry + "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if (userPosition != null && !userPosition.equalsIgnoreCase("") && userCompany != null && userCompany.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry +"</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if (userPosition != null && userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry +"</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if (userPosition != null && userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if(userPosition != null && !userPosition.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry +"</font>", Html.FROM_HTML_MODE_LEGACY));
                }else if(userPosition != null && !userPosition.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if(userCompany != null && !userCompany.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry +"</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if(userCompany != null && !userCompany.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if(userCountry != null && !userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCountry+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));
                }
                mPosition.setText(spannedDetails);
                mPosition.setMovementMethod(LinkMovementMethod.getInstance());
                mPosition.setVisibility(View.VISIBLE);

                if(userCompany != null && !userCompany.equalsIgnoreCase("") && userCompanyId != null && !userCompanyId.equalsIgnoreCase("")){
                    mPosition.setOnClickListener(v -> goToCompany(userCompanyId));
                }

            }
            else
            {
                SpannableStringBuilder spannedDetails;
                if(userPosition != null && !userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase(""))
                {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+userCompany+"&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry + "</font>"));
                } else if (userPosition != null && !userPosition.equalsIgnoreCase("") && userCompany != null && userCompany.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry +"</font>"));
                } else if (userPosition != null && userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry +"</font>"));
                } else if (userPosition != null && userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                } else if(userPosition != null && !userPosition.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry +"</font>"));
                }else if(userPosition != null && !userPosition.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                } else if(userCompany != null && !userCompany.equalsIgnoreCase("") && userCountry != null && !userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+ userCountry +"</font>"));
                } else if(userCompany != null && !userCompany.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                } else if(userCountry != null && !userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCountry+ "</font>"));
                }
                else
                {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml(""));
                }
                mPosition.setText(spannedDetails);
                mPosition.setVisibility(View.VISIBLE);
                mPosition.setMovementMethod(LinkMovementMethod.getInstance());
                if(userCompany != null && !userCompany.equalsIgnoreCase("") && userCompanyId != null && !userCompanyId.equalsIgnoreCase("")){
                    mPosition.setOnClickListener(v -> goToCompany(userCompanyId));
                }
            }

                topLayout.setVisibility(View.VISIBLE);
                shadow.setVisibility(View.VISIBLE);
        }
    }

    private void goToProfile(String chatRoomId)
    {
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
        {
            Intent intent = new Intent(this, UserFriendProfileActivity.class);
            intent.putExtra("friendId", chatRoomId);
            startActivity(intent);
            if (CheckOsVersion.isPreLollipop())
                this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        else
        {
            if(mMessagesView !=null)
                SnackBarDialog.show(mMessagesView, "No internet connection available");
        }
    }

    private void goToCompany(String companyId)
    {
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            Intent i = new Intent(this, CompanyProfileActivity.class);
            i.putExtra("companyId", companyId);
            startActivity(i);
        }
        else
        {
            if(mMessagesView != null)
            {
                SnackBarDialog.showNoInternetError(mMessagesView);
            }
        }
    }

    private static IntentFilter createIntentFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SET_NOTIFY);
        filter.addAction(SET_UPDATE_NOTIFY);
        filter.addAction(Config.PUSH_NOTIFICATION);
        filter.addAction(Config.MESSAGE_TYPING);
        filter.addAction(Config.MESSAGE_STOP_TYPING);
        filter.addAction(Config.MESSAGE_SEEN);
        filter.addAction(Config.MESSAGE_RECEIVED);
        filter.addAction(Config.USER_ONLINE);
        filter.addAction(OnlineNotifierService.BROADCAST_ACTION);
        return filter;

    }

    private void onHide()
    {
        if(fab != null)
        {
            fab.post(() -> fab.setVisibility(View.GONE));

        }
    }

    private void onShow()
    {
        if(fab != null){
            fab.post(() -> fab.setVisibility(View.VISIBLE));
        }
    }

    private void loadMoreMessages(final int messageId)
    {
            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;

            if(messageId == 0 || TextUtils.isEmpty(String.valueOf(messageId)) || chatRoomId == null || TextUtils.isEmpty(chatRoomId))
            {
                hideTopRefreshingView();
                return;
            }

            if(dbHelper == null)
                dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());

        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        isFetching = true;
        apiService.getChatRoomMessages(userId, chatRoomId, String.valueOf(messageId), "10", apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ChatRoomResponse>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Log.d("fetching", "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Log.e("fetching", "onError: ");
                        isFetching = false;
                        hideTopRefreshingView();
                    }

                    @Override
                    public void onNext(ChatRoomResponse chatRoomResponse)
                    {
                        hideTopRefreshingView();
                        updateResponse(chatRoomResponse);
                        Log.d("fetching", "onNext: ");
                    }

                });

    }

    private void updateResponse(final ChatRoomResponse chatRoomResponse)
    {
        if (chatRoomResponse != null) {
            if(!chatRoomResponse.isErrorStatus()){
                List<ChatRoom> chatRooms = chatRoomResponse.getResults();
                if (chatRooms != null && !chatRooms.isEmpty()) {
                    AppController.getInstance().getPreferenceManager().clearMessageRead();
                    int k;
                    for (k=0; k < chatRooms.size(); k++) {
                        String profilePicture = chatRooms.get(k).getProfilePic();
                        String color = chatRooms.get(k).getColor();
                        String profileName = chatRooms.get(k).getName();
                        //String lastSeenTime = ""/*chatRooms.get(k).getSeenTime()*/;
                        int unreadMessageCount = chatRooms.get(k).getUnReadMessageCount();
                        List<ChatRoom> mChatRoomMessagesCopy  = chatRooms.get(k).getData();
                        if (mChatRoomMessagesCopy != null && !mChatRoomMessagesCopy.isEmpty()){
                            for (int i=0; i < mChatRoomMessagesCopy.size(); i++) {
                                dbHelper.addMessage(mChatRoomMessagesCopy.get(i).getFrom(),mChatRoomMessagesCopy.get(i).getTo(),mChatRoomMessagesCopy.get(i).getMessage() != null ? mChatRoomMessagesCopy.get(i).getMessage() :"",  mChatRoomMessagesCopy.get(i).getMessageType(),mChatRoomMessagesCopy.get(i).getMessageChatImage() != null ? mChatRoomMessagesCopy.get(i).getMessageChatImage() :"",String.valueOf(mChatRoomMessagesCopy.get(i).getId()),"","","","", String.valueOf(mChatRoomMessagesCopy.get(i).getId()),mChatRoomMessagesCopy.get(i).getTimeStamp(),mChatRoomMessagesCopy.get(i).getStatus(),mChatRoomMessagesCopy.get(i).getSeenT(), profileName, profilePicture, color, unreadMessageCount);
                            }
                        } else {
                            mChatRoomActivity.runOnUiThread(() -> Toast.makeText(mChatRoomActivity, "No more messages!", Toast.LENGTH_SHORT).show());
                            isFetching = false;
                            return;
                        }
                    }
                    setNotifyChanges(false);
                } else {
                    isFetching = false;
                }
            } else {
                isFetching = false;
                if(mMessagesView != null)
                    SnackBarDialog.show(mMessagesView, chatRoomResponse.getStatus());
            }
        } else {
            hideTopRefreshingView();
            isFetching = false;
            if (mMessagesView != null)
                Snackbar.make(mMessagesView, "Something went wrong.", Snackbar.LENGTH_LONG)
                        .show();
        }
    }


    public void getMessages(final String chatRoomImage)
    {
        if(dbHelper == null)
            dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());

        mChatRoomMessages.clear();

        if (dbHelper.getChatRoomMessage(chatRoomId) == null)
            return;
        dbHelper.deleteDuplicates();
        mChatRoomMessages = dbHelper.getChatRoomMessage(chatRoomId);
        if(mChatRoomMessages.size() > 0 && !mChatRoomMessages.get(mChatRoomMessages.size()-1).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
            sendMessageSeen();
            long NOW = System.currentTimeMillis() / 1000L;
            String postingTime = Long.toString(NOW);
            if(dbHelper == null)
                dbHelper = MyScrapSQLiteDatabase.getInstance(mChatRoomActivity);
            dbHelper.updateSeenStatus(chatRoomId, postingTime);
        }

        mChatRoomMessages = groupDataIntoHashMap(mChatRoomMessages);

        mAdapter = new ChatRoomMessageAdapter(mChatRoomActivity, mChatRoomMessages, chatRoomImage, filter());
        mAdapter.setHasStableIds(true);
        mMessagesView.setAdapter(mAdapter);
        mAdapter.preLoadImages();
        if(mChatRoomMessages.size() == 0){
            emptyView.setVisibility(View.VISIBLE);
            messageLayout.setVisibility(View.GONE);
        } else {
            messageLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

    }



    private List<ChatRoom> groupDataIntoHashMap(List<ChatRoom> chatModelList) {
        LinkedHashMap<String, Set<ChatRoom>> groupedHashMap = new LinkedHashMap<>();
        Set<ChatRoom> list;
        String hashMapKey = null;
        for (ChatRoom message : chatModelList) {
            if (message.getTimeStamp() != null && !message.getTimeStamp().equalsIgnoreCase("")){
                if(!message.getTimeStamp().equalsIgnoreCase("false") && !message.getTimeStamp().equalsIgnoreCase("true") ){
                    long dateVal = Long.parseLong(UserUtils.parsingLong(message.getTimeStamp()));
                    hashMapKey = getChatRoomTime(dateVal) != null ? getChatRoomTime(dateVal) : "";
                }
            }
            if (groupedHashMap.containsKey(hashMapKey)) {
                // The key is already in the HashMap; add the pojo object
                // against the existing key.
                groupedHashMap.get(hashMapKey).add(message);
            } else {
                // The key is not there in the HashMap; create a new key-value pair
                list = new LinkedHashSet<>();
                list.add(message);
                groupedHashMap.put(hashMapKey, list);
            }
        }
        //Generate list from map
        return generateListFromMap(groupedHashMap);
    }


    private List<ChatRoom> generateListFromMap(LinkedHashMap<String, Set<ChatRoom>> groupedHashMap) {
        // We linearly add every item into the consolidatedList.
        List<ChatRoom> consolidatedList = new ArrayList<>();
        for (String date : groupedHashMap.keySet()) {
            ChatRoom dateItem = new ChatRoom();
            dateItem.setMessageType("100");
            dateItem.setMessageDateFormatted(date);
            consolidatedList.add(dateItem);
            consolidatedList.addAll(groupedHashMap.get(date));
        }
        return consolidatedList;
    }


    private static String getChatRoomTime(long time)
    {
        if (time < 1000000000000L) {
            time *= 1000;
        }
        Date date;
        String difference;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        SimpleDateFormat mSimpleDateFormat= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        date = new Date(time);
        Date currentDate = new Date();
        long dateExpireOrNot = currentDate.getTime() - date.getTime();
        //int diffExpireOrNotDays = (int) (dateExpireOrNot / (24 * 60 * 60 * 1000));

        long diffExpireOrNotDays = TimeUnit.DAYS.convert(dateExpireOrNot, TimeUnit.MILLISECONDS);
        if (diffExpireOrNotDays == 0) {
            difference = "Today";
        } else if (diffExpireOrNotDays == 1) {
            difference = "Yesterday";
        } else {
            difference = dateFormat.format(time);
        }
        return difference;
    }


    private  Runnable clear = new Runnable()
    {
        @Override
        public void run()
        {
            if (isVisible)
            {
                if(chatRoomId != null && !chatRoomId.equalsIgnoreCase(""))
                NotificationUtils.clearNotificationByID(Config.NOTIFICATION_ID);
                NotificationUtils.clearNotificationByID(Integer.parseInt(UserUtils.parsingInteger(chatRoomId)));
                dbHelper.updateMessageCount(chatRoomId, 0);
            }
            if (mClearNotificationHandler != null) {
                mClearNotificationHandler.removeCallbacks(clear);
                mClearNotificationHandler.postDelayed(clear, 1000);
            }
        }
    };


    public int filter()
    {
        String lastSeen = null;
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return 0;
        if(dbHelper == null)
            dbHelper = MyScrapSQLiteDatabase.getInstance(mChatRoomActivity);

        List<ChatRoom> newChatRooms = dbHelper.getChatRoomMessage(chatRoomId);

        if (indexes == null)
            indexes = new ArrayList<>();

        for(int i=0; i < newChatRooms.size(); i++){
            if(newChatRooms.get(i).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()) && newChatRooms.get(i).getStatus().equalsIgnoreCase("3")) {
                String id = Integer.toString(i);
                indexes.add(id);
            }
        }

        if (indexes != null && !indexes.isEmpty()) {
            lastSeen = indexes.get(indexes.size()-1);
        }

        if(lastSeen == null)
            return -1;

        int lastSeenPosition = Integer.parseInt(UserUtils.parsingInteger(lastSeen));

        if(newChatRooms.size() > lastSeenPosition){
            int diff = lastSeenPosition+1;

            if (indexesAfterSeen == null)
                indexesAfterSeen = new ArrayList<>();

            indexesAfterSeen.clear();
            for(int i = diff; i < newChatRooms.size(); i++){
                if(!newChatRooms.get(i).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    indexesAfterSeen.add(i);
                }
            }

            if(indexesAfterSeen.size() == (newChatRooms.size() - diff)){
                Log.d("filter: ", ""+indexesAfterSeen.size());
                if(newChatRooms.size() != 0){
                    return newChatRooms.size()-1;
                }
            } else {
                return Integer.parseInt(UserUtils.parsingInteger(lastSeen));
            }
        }

        return Integer.parseInt(UserUtils.parsingInteger(lastSeen));
    }


    public void setNotifyChanges(final boolean isScroll)
    {
        new Handler().postDelayed(() -> {
            if(dbHelper == null)
                dbHelper = MyScrapSQLiteDatabase.getInstance(mChatRoomActivity);
            List<ChatRoom> newChatRooms = dbHelper.getChatRoomMessage(chatRoomId);
            if (newChatRooms != null) {
                if(newChatRooms.isEmpty()){
                    emptyView.setVisibility(View.VISIBLE);
                    messageLayout.setVisibility(View.GONE);
                } else {
                    messageLayout.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
                mChatRoomMessages.clear();
                mChatRoomMessages.addAll(groupDataIntoHashMap(dbHelper.getChatRoomMessage(chatRoomId)));
                //mChatRoomMessages = groupDataIntoHashMap(mChatRoomMessages);
                mAdapter.seen = filter();
                mAdapter.notifyDataSetChanged();
                if (isScroll){
                    scroll();
                } else {
                    if (mMessagesView != null && mMessagesView.getLayoutManager() != null) {
                        int scrollPosition = ((LinearLayoutManager) mMessagesView.getLayoutManager())
                                .findFirstCompletelyVisibleItemPosition();
                        Log.d("lastDisPosition", ""+scrollPosition);
                        scrollToPosition(scrollPosition);
                    }
                }

            } else {
                messageLayout.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
            isFetching = false;
        }, 1000);
    }

    private void scrollToPosition(int position) {
        if(mMessagesView != null) {
            mMessagesView.post(() -> {
                if (mAdapter != null && mAdapter.getItemCount() > 1){
                    int pos = position - 1;
                    if (pos > 0)
                    mMessagesView.post(() -> mMessagesView.getLayoutManager().smoothScrollToPosition(mMessagesView,null, pos));
                }
            });
        }
    }

    private void scroll() {
        if(mMessagesView != null) {
            mMessagesView.post(() -> {
                if (mChatRoomMessages != null && mAdapter != null){
                    if (mAdapter.getItemCount() > 1) {
                        int pos = mAdapter.getItemCount() - 1;
                        if (pos > 0)
                            mMessagesView.post(() -> mMessagesView.getLayoutManager().smoothScrollToPosition(mMessagesView, null, pos));
                    }
                    Log.d("setNotifyChanges", "DONE");
                }
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hasGalleryPermission() {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void askForGalleryPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_READ_PERMISSION);
    }

    private void startGalleryIntent(boolean isMultiple) {
        if (!hasGalleryPermission()) {
            askForGalleryPermission();
            return;
        }

        Intent mIntent = new Intent(ChatRoomActivity.this, MultiPhotoSelectActivity.class);
        startActivityForResult(mIntent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent resultIntent) {
        super.onActivityResult(requestCode, responseCode, resultIntent);

        String absPath;
        if (responseCode == RESULT_OK && requestCode == REQUEST_GALLERY) {
            if( resultIntent.getData() != null){
                absPath = BitmapUtils.getFilePathFromUri(this, resultIntent.getData());
                loadImage(absPath);
            }
        } else if(responseCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE){
            absPath = BitmapUtils.getFilePathFromUri(this, resultIntent.getData());
            loadImage(absPath);
        } else if(responseCode == RESULT_OK && requestCode == REQUEST_IMAGE_GALLERY){
            if(resultIntent != null){
                final ArrayList<String> images = resultIntent.getStringArrayListExtra("images");
                if(images != null){
                    Log.d("images", ""+images.size());

                    new Handler().post(() -> {
                        int i = 0;
                        for(String filePath : images){
                            loadImage(filePath);
                            Log.e("Sending ", ""+i);
                            i++;
                        }
                    });
                }

            }
        }
    }

    public void loadImage(final String filePath) {

        if(mMessagesView != null){
            mMessagesView.post(() -> {
                String encodedImage = convertToBitmap(filePath);
                if(mSocket == null){
                    Log.e(TAG, "Socket: NULL");
                    new SocketAPI(AppController.getInstance()).connect();
                    mSocket = SocketAPI.getSocket();
                }

                if(mSocket != null){
                    if (mSocket.connected()){
                        Log.e(TAG, "Socket: CONNECTED");
                        if(encodedImage != null && !encodedImage.equalsIgnoreCase(""))
                           sendPictureMessageIntent(encodedImage, filePath);
                    } else {
                        Log.e("Socket.connected()", String.valueOf(mSocket.connected()));
                    }
                }

            });
        }

    }

    private void goBack(String imageUrl, String encodedImage) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("imageUrl", imageUrl);
        returnIntent.putExtra("imageBitmap", encodedImage);
        setResult(Activity.RESULT_OK,returnIntent);
        this.finish();
    }

    private String convertToBitmap(String realPath) {
        String encodedImage = ImageUtils.compressImage(realPath);
        if (encodedImage != null)
            return encodedImage;
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGalleryIntent(true);
            } else{
                Toast.makeText(this, "Gallery permission not granted", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            /*if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ///startCameraIntent();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale
                        (this, android.Manifest.permission.CAMERA)) {
                   // showAlert();
                }
            }*/
        }
    }

    private void upload() {
        startGalleryIntent(true);
    }

    private void doTyping() {
        if (mChatRoomActivity != null) {
            mChatRoomActivity.runOnUiThread(() -> {
                if (toolbarSubTitle != null ) {
                    toolbarSubTitle.setText("typing..");
                    toolbarSubTitle.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void stopTyping() {
        if (mChatRoomActivity != null) {
            mChatRoomActivity.runOnUiThread(() -> {
                if (toolbarSubTitle != null ) {
                    if (userOnline != null && !userOnline.equalsIgnoreCase("")){
                        toolbarSubTitle.setText(userOnline);
                        toolbarSubTitle.setVisibility(View.VISIBLE);
                    } else if (userLastSeen != null && !userLastSeen.equalsIgnoreCase("")){
                        toolbarSubTitle.setText(userLastSeen);
                        toolbarSubTitle.setVisibility(View.VISIBLE);
                    } else {
                        toolbarSubTitle.setText("");
                        toolbarSubTitle.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTypingHandler != null)
            mTypingHandler.removeCallbacks(onTypingTimeout);
        Log.d("onStop: ", "timer cleared");
        Log.d("onStop: ", "startCheckingUserStatus");
        if (serviceIntent != null)
            stopService(serviceIntent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        isVisible = true;
        if (serviceIntent != null)
            stopService(serviceIntent);
        LocalBroadcastManager.getInstance(this).registerReceiver(setNotifyReceiver,INTENT_FILTER);
        if (mClearNotificationHandler != null) {
            mClearNotificationHandler.removeCallbacks(clear);
            mClearNotificationHandler.postDelayed(clear, 1000);
        }
        if(mTracker != null){
            mTracker.setScreenName("Chat Room Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        AppController.service();

        UserOnlineStatus.setUserOnline(ChatRoomActivity.this, UserOnlineStatus.ONLINE);

        AppController.getInstance().getPreferenceManager().clear();
        if ( CheckNetworkConnection.isConnectionAvailable(mChatRoomActivity) && chatRoomId != null) {
            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;
            serviceIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
            serviceIntent.putExtra("chatRoomId",chatRoomId);
            startService(serviceIntent);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(setNotifyReceiver);
        UserOnlineStatus.setUserOnline(ChatRoomActivity.this, UserOnlineStatus.OFFLINE);
        isVisible = false;
        userOnline ="";
        userLastSeen ="";
        userPosition ="";
        userCompany ="";
        userCountry ="";
        userCompanyId ="";
        if (serviceIntent != null)
            stopService(serviceIntent);
    }

    private void clearChatRoomNotification() {
        String oldNotification = AppController.getInstance().getPreferenceManager().getNotifications();
        String oldNotificationCount = AppController.getInstance().getPreferenceManager().getNotificationsFromId();
        String oldMessageUnRead = AppController.getInstance().getPreferenceManager().getMessageReadFromId();
        List<String> notificationMessages = new ArrayList<>();
        List<String> notificationFromId = new ArrayList<>();
        List<String> messageUnreadFromId = new ArrayList<>();
        if (oldNotification != null && oldNotificationCount != null) {
            ArrayList<String> messages = new ArrayList<>(Arrays.asList(oldNotification.split("\\|")));
            List<String> messagesCount = Arrays.asList(oldNotificationCount.split("\\|"));

            for (int i = 0; i < messagesCount.size(); i++) {
                if (!messagesCount.get(i).contains(chatRoomId)){
                    notificationFromId.add(messagesCount.get(i));
                    notificationMessages.add(messages.get(i));
                }
            }

            AppController.getInstance().getPreferenceManager().clear();

            for (int k=0; k < notificationMessages.size();  k++) {
                AppController.getInstance().getPreferenceManager().addNotification(notificationMessages.get(k), notificationFromId.get(k));
                if(!messageUnreadFromId.isEmpty())
                    AppController.getInstance().getPreferenceManager().addMessageRead("unread",messageUnreadFromId.get(k));
            }
        }
        if (oldMessageUnRead != null) {
            List<String> unReadMessagesCount = Arrays.asList(oldMessageUnRead.split("\\|"));
            Set<String> uniqueId = new HashSet<>();
            uniqueId.addAll(unReadMessagesCount);
            unReadMessagesCount = new ArrayList<>();
            unReadMessagesCount.addAll(uniqueId);
            for (int i = 0; i < unReadMessagesCount.size(); i++) {
                if (!unReadMessagesCount.get(i).contains(chatRoomId)) {
                    messageUnreadFromId.add(unReadMessagesCount.get(i));
                }
            }
            AppController.getInstance().getPreferenceManager().clearMessageRead();
            for (int j=0; j < messageUnreadFromId.size(); j++) {
                AppController.getInstance().getPreferenceManager().addMessageRead("unread",messageUnreadFromId.get(j));
            }
        } else {
            NotificationUtils.clearNotificationByID(Config.NOTIFICATION_ID);
        }
    }



    private void attemptSend() {
        AppController.runOnUIThread(() -> {
            if(mSocket == null)
                new SocketAPI(AppController.getInstance()).connect();
            mSocket = SocketAPI.getSocket();

            if (mSocket != null && !mSocket.connected()){
                Log.e("Socket.connected()", String.valueOf(mSocket.connected()));
                return;
            }

            mTyping = false;
            if(emojiconEditText != null){
                if (!TextUtils.isEmpty(emojiconEditText.getText().toString())){
                    String message = emojiconEditText.getText().toString().trim();
                    sendMessageIntent(message);
                    emojiconEditText.setText("");
                }
                stopTypingIntent();
            }

        });

    }

    private void sendMessageIntent(String message) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        Log.e("attemptSend", "sending");
        String time = String.valueOf(System.currentTimeMillis());
        Intent i = new Intent();
        i.setAction(MessageService.ACTION_SEND_MSG);
        i.putExtra("to",chatRoomId);
        i.putExtra("from", AppController.getInstance().getPrefManager().getUser().getId());
        i.putExtra("fromImage", fromImage);
        i.putExtra("fromName",fromName);
        i.putExtra("msg",message);
        i.putExtra("bitmap","");
        i.putExtra("time",time);
        i.putExtra("color",color);
        i.putExtra("uImage",chatRoomImage);
        i.putExtra("uName",chatRoomName);
        if(TextUtils.isEmpty(chatRoomId))
        {
            return;
        }
        ChatRoomActivity.this.sendBroadcast(i);

    }

    private void sendPictureMessageIntent(final String bitmap, final String absPath)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        new MyAsyncTask(mChatRoomActivity,bitmap, absPath).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.e("attemptSendImage", "Sending");
    }

    private static class MyAsyncTask extends AsyncTask<Void, Void, Void>{

        private WeakReference<ChatRoomActivity> activityReference;
        private String bitmap, absPath;

        MyAsyncTask(ChatRoomActivity context, String mBitmap, final String mAbsPath) {
            activityReference = new WeakReference<>(context);
            bitmap = mBitmap;
            absPath = mAbsPath;
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            ChatRoomActivity activity = activityReference.get();
            if (activity == null) return;

            JSONObject sendText = new JSONObject();
            JSONObject sendNotify = new JSONObject();
            try{
                String time = String.valueOf(System.currentTimeMillis());
                sendNotify.put("to",chatRoomId);
                sendNotify.put("from", AppController.getInstance().getPrefManager().getUser().getId());
                sendText.put("to",chatRoomId);
                sendText.put("from", AppController.getInstance().getPrefManager().getUser().getId());
                sendText.put("msg","");
                sendText.put("time",time);
                sendText.put("bitmap",bitmap);
                sendText.put("color",color);
                sendText.put("uImage",chatRoomImage);
                sendText.put("uName",chatRoomName);
                sendText.put("fName",fromName);
                sendText.put("fImage",fromImage);
                String filePath;
                String message;
                String messageType;
                String profile;
                if(absPath != null && !absPath.equalsIgnoreCase("")){
                    filePath = absPath;
                    messageType = "12";
                } else {
                    filePath = "";
                    messageType = "";
                }

                profile = chatRoomImage;
                message = "";


                if (dbHelper == null){
                    dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
                }


                if(TextUtils.isEmpty(chatRoomId)){
                    return;
                }

                if(mSocket == null)
                    new SocketAPI(AppController.getInstance()).connect();

                mSocket = SocketAPI.getSocket();
                if (mSocket != null && mSocket.connected()){
                    mSocket.emit("uploadFileStart", sendText);
                    mSocket.emit("chatnotification", sendNotify);
                    Log.e("Tx", sendText.toString());
                    Log.e("SocketImage", "emitted");
                    dbHelper.addSingleMessage(AppController.getInstance().getPrefManager().getUser().getId(), chatRoomId, message, messageType, filePath, "", "", "", "", "",  "", time , "0", "",chatRoomName, profile, "", 0);
                    Log.e("attemptSendImage", "Local");
                } else {
                    new SocketAPI(AppController.getInstance()).connect();
                    SnackBarDialog.show(mMessagesView, "Something went wrong! Please try again");
                }
            } catch(JSONException e){
                Log.e("attemptSendImage", e.toString());
            }

        }
    }

    private void stopTypingIntent() {
        Intent typingIntent = new Intent();
        typingIntent.setAction(MessageService.ACTION_STOP_COMPOSING);
        typingIntent.putExtra("to",chatRoomId);
        sendBroadcast(typingIntent);
    }

    private void sendMessageSeen() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        Intent i = new Intent();
        i.setAction(MessageService.ACTION_SEND_READ);
        i.putExtra("to",chatRoomId);
        i.putExtra("from", AppController.getInstance().getPrefManager().getUser().getId());
        sendBroadcast(i);
    }

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {

            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;

            stopTyping();
            if (!mTyping) return;
            mTyping = false;
            if (mSocket != null && mSocket.connected()){
                JSONObject sendTypingStop = new JSONObject();
                try {
                    sendTypingStop.put("to", chatRoomId);
                    sendTypingStop.put("from", AppController.getInstance().getPrefManager().getUser().getId());
                    mSocket.emit("stopTyping", sendTypingStop);
                } catch(JSONException e){
                    Log.d("typing", e.toString());
                }
            }

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_room_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        if (searchManager != null) {
            searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(push != null && push.equalsIgnoreCase("push")){
            goToChat();
        } else {
            this.finish();
        }
    }

    private void goToChat()
    {
        Intent i = new Intent(this, MainChatRoomActivity.class);
        startActivity(i);
        this.finish();
        if (CheckOsVersion.isPreLollipop()) {
           this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }


    @Override
    protected void onDestroy()
    {

        if (mClearNotificationHandler != null)
        {
            mClearNotificationHandler.removeCallbacks(clear);
        }

        userOnline ="";
        userLastSeen ="";
        userPosition ="";
        userCompany ="";
        userCountry ="";
        userCompanyId ="";
        if(serviceIntent != null)
            stopService(serviceIntent);
        super.onDestroy();

    }



    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        final List<ChatRoom> filteredModelList = filter(mChatRoomMessages, newText);
        if (filteredModelList.size() > 0) {
            mAdapter.setFilter(filteredModelList, newText);
            return true;
        }
        else
        {
            if (newText.equalsIgnoreCase(""))
            {
                runOnUiThread(() -> mAdapter.swap(mChatRoomMessages, filter()));
            }
            else
            {
                Toast.makeText(ChatRoomActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    private List<ChatRoom> filter(List<ChatRoom> chatRooms, String query)
    {
        final List<ChatRoom> chatRoomsCopy = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms)
        {
            if (chatRoom.getMessage() != null)
            {
                final String text = chatRoom.getMessage().toLowerCase().toLowerCase();
                if (text.contains(query.toLowerCase()))
                {
                    chatRoomsCopy.add(chatRoom);
                }
            }
        }
        return chatRoomsCopy;
    }

}
