package com.myscrap.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.myscrap.CompanyProfileActivity;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.Online;
import com.myscrap.notification.Config;
import com.myscrap.notification.NotificationUtils;
import com.myscrap.service.OnlineNotifierService;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.xmpp.RoosterConnectionService;
import com.myscrap.xmppmodel.XMPPChatMessageModel;
import com.myscrap.xmppresources.Constant;
import com.myscrap.xmppresources.XMPPChatAdapter;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class XMPPChatRoomActivity extends AppCompatActivity implements XMPPChatAdapter.OnInformRecyclerViewToScrollDownListener
{

    private ImageButton submitBtn;
    private LinearLayout submitLayout;
    private EmojiconEditText emojiconEditText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private View toolbarView;
    private ImageView toolbarHomeUp;


    private XMPPConnection connection ;
    private ChatManager chatManager;
    private Chat chat = null;
    private EntityBareJid friendsBareJid = null;


    private XMPPChatAdapter chatAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView chatRecycler;

    private String profile_url;
    private String title;
    private TextView mPosition;
    private LinearLayout appBarLayout;

    public String userJID;
    public String friendsJID;
    public String currentUserId;
    public String friendsUserId;
    public String currentUserName;
    public String friendsName;
    public String currentUserImage;
    public String friendsImage;
    public String currentUserColor;
    public String friendsColor;

    public String friendsCompany;
    public String friendsPosition;
    public String friendsLocation;

    private TextView toolbarTitle;
    private TextView presenceTitle;
    private TextView iconText;
    private SimpleDraweeView toolbarContactImage;
    private BroadcastReceiver mReceiveMessageBroadcastReceiver;
    private Context mApplicationContext;



    public List<XMPPChatMessageModel> chatList = new ArrayList<>();
    private View emptyView;
    private FrameLayout messageLayout;





    public static final String SET_NOTIFY = "set_notify";
    public static final String SET_UPDATE_NOTIFY = "set_update_notify";
    public static final IntentFilter INTENT_FILTER = createIntentFilter();



    private BroadcastReceiver setNotifyReceiver;
    private String userOnline ="";
    private String userLastSeen ="";
    private String userPosition ="";
    private String userCompany ="";
    private String userCountry ="";
    private String userCompanyId ="";
    private Handler mClearNotificationHandler = new Handler();
    private Intent serviceIntent;
    private boolean isVisible = false;
    private View shadow;
    private LinearLayout topLayout;

    private CountDownTimer counter;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xmppchat_room);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        friendsJID = bundle.getString(Constant.FRIENDS_JID);
        friendsUserId = bundle.getString(Constant.FRIENDS_ID);
        friendsName = bundle.getString(Constant.FRIENDS_NAME);
        friendsImage = bundle.getString(Constant.FRIENDS_URL);
        friendsColor = bundle.getString(Constant.FRIENDS_COLOR);


        userJID = UserUtils.getUserJid(XMPPChatRoomActivity.this);
        currentUserId = UserUtils.getLoggedUserId(XMPPChatRoomActivity.this);
        currentUserName = UserUtils.getFirstName(XMPPChatRoomActivity.this)+" "+UserUtils.getLastName(XMPPChatRoomActivity.this);
        currentUserImage = UserUtils.getUserProfilePicture(XMPPChatRoomActivity.this);
        currentUserColor = UserUtils.getUserColor(XMPPChatRoomActivity.this);






        mApplicationContext = XMPPChatRoomActivity.this;
        toolbar = findViewById(R.id.toolbar);
        toolbarView = findViewById(R.id.chat_room_toolbar);
        LinearLayout toolbarLayout = toolbar.findViewById(R.id.action_bar_title);
        toolbarHomeUp = toolbar.findViewById(R.id.home_indicator);
        toolbarContactImage= toolbar.findViewById(R.id.conversation_contact_photo);
        iconText = toolbarView.findViewById(R.id.icon_text);
        toolbarTitle = toolbarView.findViewById(R.id.action_bar_title_1);
        presenceTitle = toolbarView.findViewById(R.id.action_bar_title_2);
        topLayout = findViewById(R.id.topLayout);
        shadow = findViewById(R.id.shadow);


        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                goToProfile(friendsUserId);
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mPosition = findViewById(R.id.position);
        appBarLayout = findViewById(R.id.appBarLayout);



        serviceIntent = new Intent(this, OnlineNotifierService.class);
        // hard coded value
        mPosition.setVisibility(View.VISIBLE);
        appBarLayout.setVisibility(View.VISIBLE);




        //   back press
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        if(toolbarHomeUp != null)
        {
            toolbarHomeUp.setOnClickListener(v -> onBackPressed());
        }









     //   setPosition();
        setFriendsIcon();







        // swipe refresh layout handling
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                chatAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });




        submitBtn = findViewById(R.id.submit_btn);
        submitLayout = findViewById(R.id.submit_btn_layout);
        emojiconEditText = findViewById(R.id.emojicon_edit_text);


        chatRecycler = findViewById(R.id.messages);
        chatAdapter = new XMPPChatAdapter(getApplicationContext(),friendsJID);
        chatAdapter.setmOnInformRecyclerViewToScrollDownListener(this);
        chatRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatRecycler.setAdapter(chatAdapter);


        try
        {
            friendsBareJid =  JidCreate.entityBareFrom(friendsJID+"@192.169.189.223");
        }
        catch (XmppStringprepException e)
        {
            e.printStackTrace();
        }




        submitBtn.setOnClickListener(new View.OnClickListener() { //to avoid crash in oreo  paused chat
            @Override
            public void onClick(View view)
            {

                if(CheckNetworkConnection.isConnectionAvailable(XMPPChatRoomActivity.this))
                {
                    String text = emojiconEditText.getText().toString();
                    if ( text != null && !text.equalsIgnoreCase(""))
                    {

                        RoosterConnectionService.getConnection().sendMessage(text, userJID, friendsJID, currentUserId, friendsUserId,
                                currentUserName, friendsName, currentUserImage, friendsImage,
                                currentUserColor, friendsColor);

                        //     chatAdapter.onMessageAdd();
                        emojiconEditText.getText().clear();

                    }
                }
                else
                {
                    emojiconEditText.getText().clear();
                    if(submitLayout != null)
                    {
                        SnackBarDialog.showNoInternetError(submitLayout);
                    }
                }

            }
        });

        submitLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
            }
        });






        emojiconEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0){
                    submitBtn.setColorFilter(ContextCompat.getColor(XMPPChatRoomActivity.this, R.color.colorPrimary));
                } else {
                    submitBtn.setColorFilter(null);
                }
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

                if(intent.getAction() == null)
                    return;

                if (intent.getAction().equals(OnlineNotifierService.BROADCAST_ACTION))
                {
                    Gson gson = new Gson();
                    Online.OnlineData mData =  gson.fromJson(intent.getStringExtra("response"), Online.OnlineData.class);
                    if(mData != null)
                    {

                        if (mData.isOnline())
                        {
                            userOnline = "Online";
                            presenceTitle.setText(userOnline);
                            presenceTitle.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            userOnline = "";
                            if(mData.getTimeStamp() != null && !mData.getTimeStamp().equalsIgnoreCase(""))
                            {
                                userLastSeen = mData.getTimeStamp();
                                presenceTitle.setText(userLastSeen);
                                presenceTitle.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                userLastSeen = "";
                                presenceTitle.setVisibility(View.GONE);
                            }
                        }

                        if(mData.getUserCountry() != null && !mData.getUserCountry().equalsIgnoreCase(""))
                        {
                            userCountry = mData.getUserCountry();
                        }
                        else
                        {
                            userCountry = "";
                        }

                        if (mData.getUserCompany() != null && !mData.getUserCompany().equalsIgnoreCase(""))
                        {
                            userCompany = mData.getUserCompany();
                        }
                        else
                        {
                            userCompany = "";
                        }

                        if (mData.getCompanyId() != null && !mData.getCompanyId().equalsIgnoreCase(""))
                        {
                            userCompanyId = mData.getCompanyId();
                        }
                        else
                        {
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




        /*counter =  new CountDownTimer(3000, 1000)
        {

            public void onTick(long millisUntilFinished)
            {


            }

            public void onFinish()
            {
                resetPresence();
                counter.start();
            }
        };
       counter.start();*/



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
            if(chatRecycler !=null)
                SnackBarDialog.show(chatRecycler, "No internet connection available");
        }
    }







    private void setFriendsIcon()
    {

        presenceTitle.setText("online");
        if (friendsName != null && !friendsName.equalsIgnoreCase(""))
        {
            toolbarTitle.setText(friendsName);
        }
        else
        {
            toolbarTitle.setText("Contact");
        }



        if (friendsImage == null )
        {
            toolbarContactImage.setImageResource(R.drawable.bg_circle);
            if (friendsColor != null && !friendsColor.equalsIgnoreCase("") && friendsColor.startsWith("#"))
            {
                toolbarContactImage.setColorFilter(Color.parseColor(friendsColor));
            }
            else
            {
                toolbarContactImage.setColorFilter(DeviceUtils.getRandomMaterialColor(XMPPChatRoomActivity.this, "400"));
            }

            String acronyms = getFriendsNameAcronyms(friendsName);
            iconText.setText(acronyms);
            iconText.setVisibility(View.VISIBLE);
        }

            else if( friendsImage.equalsIgnoreCase("") ||
                    friendsImage.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                    || friendsImage.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png"))
            {
                toolbarContactImage.setImageResource(R.drawable.bg_circle);
                if (friendsColor != null && !friendsColor.equalsIgnoreCase("") && friendsColor.startsWith("#"))
                {
                    toolbarContactImage.setColorFilter(Color.parseColor(friendsColor));
                }
                else
                {
                    toolbarContactImage.setColorFilter(DeviceUtils.getRandomMaterialColor(XMPPChatRoomActivity.this, "400"));
                }

                String acronyms = getFriendsNameAcronyms(friendsName);
                iconText.setText(acronyms);
                iconText.setVisibility(View.VISIBLE);
            }

            else if (friendsImage != null && !friendsImage.equalsIgnoreCase(""))
            {

                    Uri uri = Uri.parse(friendsImage);
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    toolbarContactImage.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    toolbarContactImage.setImageURI(uri);
                    toolbarContactImage.setColorFilter(null);
            }
    }



    private String getFriendsNameAcronyms(String friendsName)
    {
        String initial = "";
        String[] split = friendsName.split("\\s+");
        if (split.length > 1){
            String first = split[0].substring(0,1);
            String last = split[1].substring(0,1);
             initial = first + ""+ last ;
        }
        else
        {
            if (split[0] != null && split[0].trim().length() == 1)
            {
                 initial = split[0].substring(0,1);
            }
        }

        return initial;
    }


    public void online()
    {
        if (friendsName != null && !friendsName.equalsIgnoreCase(""))
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


    @Override
    protected void onPause()
    {
        super.onPause();
        isVisible = false;
        unregisterReceiver(mReceiveMessageBroadcastReceiver);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(setNotifyReceiver);
        UserOnlineStatus.setUserOnline(XMPPChatRoomActivity.this, UserOnlineStatus.OFFLINE);
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

    private void exitApp()
    {
        finish();
        System.exit(0);
    }

    @Override
    protected void onResume()
    {
        super.onResume();


        resetPresence();
        chatAdapter.informRecyclerViewToScrollDown();
        mReceiveMessageBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                switch (action)
                {
                    case Constant.BroadCastMessages.UI_NEW_MESSAGE_FLAG:
                        chatAdapter.onMessageAdd();
                        return;
                }

            }
        };

        IntentFilter filter = new IntentFilter(Constant.BroadCastMessages.UI_NEW_MESSAGE_FLAG);
        registerReceiver(mReceiveMessageBroadcastReceiver,filter);
    }

    private void resetPresence()
    {

        isVisible = true;
        if (serviceIntent != null)
            stopService(serviceIntent);

        LocalBroadcastManager.getInstance(this).registerReceiver(setNotifyReceiver,INTENT_FILTER);
        if (mClearNotificationHandler != null)
        {
            mClearNotificationHandler.removeCallbacks(clear);
            mClearNotificationHandler.postDelayed(clear, 000);
        }

        //  AppController.service();
        AppController.startXMPPService();

        UserOnlineStatus.setUserOnline(XMPPChatRoomActivity.this, UserOnlineStatus.ONLINE);

        AppController.getInstance().getPreferenceManager().clear();
        if ( CheckNetworkConnection.isConnectionAvailable(mApplicationContext) && friendsUserId != null)
        {
            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;
            serviceIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
            serviceIntent.putExtra("chatRoomId",friendsUserId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                startForegroundService(serviceIntent);
            }
            else
            {
                startService(serviceIntent);
            }
        }
     /*   if (!isMyServiceRunning(RoosterConnectionService.class))
        {

            // start service to get server connection
            Intent i1 = new Intent(XMPPChatRoomActivity.this, RoosterConnectionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                startForegroundService(i1);
            }
            else
            {
                startService(i1);
            }
        }*/



    }


    private  Runnable clear = new Runnable()
    {
        @Override
        public void run()
        {
            if (isVisible)
            {
                if(friendsUserId != null && !friendsUserId.equalsIgnoreCase(""))
                    NotificationUtils.clearNotificationByID(Config.NOTIFICATION_ID);
                NotificationUtils.clearNotificationByID(Integer.parseInt(UserUtils.parsingInteger(friendsUserId)));
           //     dbHelper.updateMessageCount(friendsUserId, 0);
            }
            if (mClearNotificationHandler != null) {
                mClearNotificationHandler.removeCallbacks(clear);
                mClearNotificationHandler.postDelayed(clear, 1000);
            }
        }
    };



    private void goToCompany(String companyId)
    {
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
        {
            Intent i = new Intent(this, CompanyProfileActivity.class);
            i.putExtra("companyId", companyId);
            startActivity(i);
        }
        else
        {
            if(chatRecycler != null)
            {
                SnackBarDialog.showNoInternetError(chatRecycler);
            }
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
    public void onBackPressed()
    {
            this.finish();
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        if (!isFinishing())
        {
            // Don't hang around.
            finish();
        }
    }


    @Override
    public void onInformRecyclerViewToScrollDown(int size)
    {
        chatRecycler.scrollToPosition(size-1);
    }




    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
