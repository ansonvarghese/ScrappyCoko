package com.myscrap;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.ChatRoom;
import com.myscrap.model.LogOut;
import com.myscrap.model.Moderator;
import com.myscrap.model.MyItem;
import com.myscrap.model.ViewerCounts;
import com.myscrap.notification.Config;
import com.myscrap.notification.NotificationUtils;
import com.myscrap.service.DeviceModelService;
import com.myscrap.service.MarkerListFetchService;
import com.myscrap.service.MessageService;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.CircularTextView;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.ProgressBarTransparentDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.xmpp.RoosterConnectionService;
import com.myscrap.xmppdata.ChatMessagesTable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.myscrap.FeedsFragment.ACTION_SHOW_LOADING_ITEM;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    private DrawerLayout drawer;
    private TextView mTitle;
    private static HomeActivity mHomeActivity;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final int REQUEST_CHECK_SETTINGS_PROXIMITY = 0x2;
    private static CircularTextView notCount;
    private static CircularTextView messageCount;
    private static CircularTextView profileInfo;
    public static Toolbar toolbar;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static MyScrapSQLiteDatabase myScrapSQLiteDatabase;
    private SimpleDraweeView profile;
    private TextView iconText;
    private String userName;
    private boolean isFirstTime;
    boolean doubleBackToExitPressedOnce = false;
    private static NavigationView navigationView;
    public static Menu menu;
    public static String clickedItem;
    public static boolean isContactFavourites = false;
    private List<ChatRoom> chatRooms = new ArrayList<>();
    private Tracker mTracker;
    private TextView profileTextView;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressDialog progressDialog;
    private Class fragmentClass;
    private Fragment fragment;
    private Handler mHandler;
    private static Subscription getLogOutSubscription;
    private Timer timer;
    private AlertDialog lmeAlert;


    private Intent sendIntent;
    private String intentAction;
    public static String staticUserId;
    public static String staticKeyId;
    public static Context staticContext;


    public static ChatMessagesTable chatMessagesTable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        statusBarColor();
        mHomeActivity = this;
        isFirstTime = true;
        mHandler = new Handler();


        chatMessagesTable = new ChatMessagesTable(getApplicationContext());

        DeviceUtils.init(HomeActivity.this);

        if(!UserUtils.isApiKeyAlreadySent(HomeActivity.this))
        {
            Intent mIntent = new Intent(HomeActivity.this, DeviceModelService.class);
            mIntent.putExtra("apiKey", DeviceUtils.getUUID(HomeActivity.this));
            mIntent.putExtra("mobileDevice", DeviceUtils.getDeviceName());
            mIntent.putExtra("mobileBrand", DeviceUtils.getDeviceBrand());
            startService(mIntent);
        }








            // new field is added in login response
            updateSubscriptionField();
            // verify jid of existing user
            verifyJidOfUser();






        // Update the notificatio count by calling the api
        staticUserId = UserUtils.getLoggedUserId(HomeActivity.this);
        staticKeyId = UserUtils.getApiKey(HomeActivity.this);
        staticContext = HomeActivity.this;




        mTracker = AppController.getInstance().getDefaultTracker();
        myScrapSQLiteDatabase = new MyScrapSQLiteDatabase(AppController.getInstance());
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {

            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                updateMessageCount();
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp);
        toolbar.setNavigationOnClickListener(v ->
        {
            drawer.openDrawer(Gravity.START);
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        setupDrawerContent(navigationView);
        ImageView notification = (ImageView) header.findViewById(R.id.notification);
        ImageView message = (ImageView) header.findViewById(R.id.message);
        FrameLayout notificationLayout = (FrameLayout) header.findViewById(R.id.notificationLayout);
        FrameLayout messageLayout = (FrameLayout) header.findViewById(R.id.messageLayout);
        profile = (SimpleDraweeView) header.findViewById(R.id.icon_profile);
        iconText = (TextView) header.findViewById(R.id.icon_text);
        profileTextView = (TextView) header.findViewById(R.id.profileName);
        notCount = (CircularTextView) header.findViewById(R.id.notification_count);
        messageCount = (CircularTextView) header.findViewById(R.id.message_count);
        profileInfo = (CircularTextView) header.findViewById(R.id.profile_info);
        profileInfo.setVisibility(View.GONE);
        LinearLayout searchLayout = (LinearLayout) header.findViewById(R.id.search_layout);







        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            startForegroundService(new Intent(this, MessageService.class));
        }
        else
        {
            startService(new Intent(this, MessageService.class));
        }



        if(UserUtils.isGuestLoggedIn(HomeActivity.this))
        {
            UserUtils.saveFirstName(HomeActivity.this, "Guest");
            UserUtils.saveLastName(HomeActivity.this, "User");
        }

        EventBus.getDefault().register(this);

        notification.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(HomeActivity.this))
            {
                GuestLoginDialog.show(this);
                return;
            }
            UserUtils.setNotificationCount(HomeActivity.this, "0");
            HomeActivity.notification();
            openNotificationFragment();
        });

        notificationLayout.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(HomeActivity.this))
            {
                GuestLoginDialog.show(this);
                return;
            }
            UserUtils.setNotificationCount(HomeActivity.this, "0");
            HomeActivity.notification();
            openNotificationFragment();
        });


        message.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(HomeActivity.this))
            {
                GuestLoginDialog.show(this);
                return;
            }
            closeDrawers();
            UserUtils.setMSGNotificationCount(HomeActivity.this, "0");
            HomeActivity.notification();

            //startActivity(new Intent(getApplicationContext(), XMPPChatContactActivity.class));
      //      goToChat();


        });
        messageLayout.setOnClickListener(v ->
        {

            if(UserUtils.isGuestLoggedIn(HomeActivity.this)){
                GuestLoginDialog.show(this);
                return;
            }
            closeDrawers();
            UserUtils.setMSGNotificationCount(HomeActivity.this, "0");
            HomeActivity.notification();

           // startActivity(new Intent(getApplicationContext(), XMPPChatContactActivity.class));

    //        goToChat();

        });

        profile.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(HomeActivity.this))
            {
                GuestLoginDialog.show(this);
                return;
            }
            goToUserProfile();
            closeDrawers();

        });

        profileTextView.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(HomeActivity.this)){
                GuestLoginDialog.show(this);
                return;
            }
            goToUserProfile();
            closeDrawers();
        });

        searchLayout.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(HomeActivity.this)){
                GuestLoginDialog.show(this);
                return;
            }
            closeDrawers();
        });

        if (checkPlayServices())
        {
            if(!UserUtils.isGuestLoggedIn(HomeActivity.this)){
                AppController.runOnUIThread(HomeActivity::registerGCM);
            }

        }

        friendsList();

        List<MyItem> markerList = myScrapSQLiteDatabase.getMarkerList();
        Log.e("markerList", "" + markerList.size());
        if (markerList.size() == 0) {
            Intent serviceIntent = new Intent(HomeActivity.this, MarkerListFetchService.class);
            startService(serviceIntent);
            Log.e("Markers Download ", "started " + System.currentTimeMillis());
        } else {
            if(markerList.size() > 0) {
                int markerSize = markerList.size();
                final MyItem lastItem = markerList.get(markerSize-1);
                Log.e("last marker", "" + lastItem.getCompanyName());
                getMarkerList(lastItem.getMarkerId());
            } else {
                Intent serviceIntent = new Intent(HomeActivity.this, MarkerListFetchService.class);
                startService(serviceIntent);
                Log.e("Markers Download ", "started " + System.currentTimeMillis());
            }
        }

        if(myScrapSQLiteDatabase == null)
            myScrapSQLiteDatabase = MyScrapSQLiteDatabase.getInstance(HomeActivity.this);

        if(myScrapSQLiteDatabase.getChatRoomList() != null)
            chatRooms = myScrapSQLiteDatabase.getChatRoomList();

        if (chatRooms != null && chatRooms.isEmpty())
            if(CheckNetworkConnection.isConnectionAvailable(HomeActivity.this))
                getChatRoom();

        if(!UserUtils.isGuestLoggedIn(AppController.getInstance())){
            enableNotification();
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent != null && intent.getAction() != null)
                {
                    if (intent.getAction().equalsIgnoreCase(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST))
                    {
                        dismiss();
                        Log.e("Markers Download ", "ended " + System.currentTimeMillis());
                    }
                    else if (intent.getAction().equalsIgnoreCase(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST_STARTS_DOWNLOAD))
                    {
                        new Handler().post(() -> showProgressDialog());
                    }
                    else if (intent.getAction().equalsIgnoreCase(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST_ENDS_DOWNLOAD))
                    {
                        dismiss();
                    }

                }
            }
        };
        selectDefaultDrawerItem();

        runOnUiThread(() ->
        {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                    update();
                }
            }, 0, 30 * 1000);
        });


        // solved
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
        {
           forceUpdate();
        }


    }

    private void updateMessageCount()
    {

        if(UserUtils.isGuestLoggedIn(HomeActivity.this))
        {
            GuestLoginDialog.show(this);
            return;
        }

        String msgCt = chatMessagesTable.getUserCount();
        if (msgCt != null && !msgCt.equalsIgnoreCase("") )
        {
            if (msgCt.equalsIgnoreCase("0"))
            {
                messageCount.setVisibility(View.GONE);
            }
            else
            {
                messageCount.setVisibility(View.VISIBLE);
                messageCount.setText(msgCt);
            }
        }
    }


    private void verifyJidOfUser()
    {

        String userJid = UserUtils.getUserJid(HomeActivity.this);
        if (userJid == null || userJid.equalsIgnoreCase(""))
        {
            UserUtils.saveUserJID(HomeActivity.this,"");

            /*finish();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));*/
        }
        else
        {
try{

            // start service to get server connection
            Intent i1 = new Intent(HomeActivity.this, RoosterConnectionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                startForegroundService(i1);
            }
            else
            {
               startService(i1); //crashes when this intent is called
            }}

catch (Exception e) {
    // This will catch any exception, because they are all descended from Exception
    Log.e( "NOtexp ",e.toString() );
}


    }}


    private void updateSubscriptionField()
    {

        if(UserUtils.isGuestLoggedIn(HomeActivity.this))
        {

        }

        String subscription = UserUtils.getPriceSubscription(HomeActivity.this);
        if (!subscription.equalsIgnoreCase("1"))
        {
           if(!subscription.equalsIgnoreCase("0"))
           {
               if (subscription.equalsIgnoreCase("") || subscription == null || subscription.equalsIgnoreCase("0"))
               {
                   UserUtils.savePriceSubscription(HomeActivity.this, "0");
               }
           }
        }


    }

    private void update()
    {
        Log.d("calling", "update");
        runVisitorCounts();
        runBumpedCounts();
        runModeratorCounts();
        updateProfilePicture();
        updateProfileCompleteness();
        loadNotificationCount();
        notification();
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Initializing, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private  void  dismiss(){
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private static void setNavItemCount(final int itemId, final int count)
    {
        AppController.runOnUIThread(() ->
        {
            if(navigationView != null){
                TextView view = (TextView) navigationView.getMenu().findItem(itemId).getActionView().findViewById(R.id.counterView);
                if(count > 0 && count <= 9)
                {
                    view.setText(count > 0 ? String.valueOf(count) : null);
                    view.setVisibility(View.VISIBLE);
                }
                else if(count > 9)
                {
                    String countText = "9+";
                    view.setText(countText);
                    view.setVisibility(View.VISIBLE);
                }
                else
                {
                    view.setText(null);
                    view.setVisibility(View.GONE);
                }

                if(count > 0){
                    if(count > 0){
                        if(toolbar != null)
                            toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp_red_bottom);
                    } else{
                        if(toolbar != null)
                            toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp);
                    }
                }

            }
        });
    }

    private void friendsList()
    {
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())
                && AppController.getInstance().getPrefManager().getUser()!= null)
        {
            Intent i = new Intent();
            i.setAction(MessageService.ACTION_LOAD_FRIEND_LIST);
            if (mHomeActivity != null)
              mHomeActivity.sendBroadcast(i);
        }
    }

    private void getMarkerList(String companyId){
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())
                && AppController.getInstance().getPrefManager().getUser()!= null){
            Intent i = new Intent();
            i.setAction(MessageService.ACTION_UPDATE_MARKERS);
            i.putExtra("companyId", companyId);
            if (mHomeActivity != null)
                mHomeActivity.sendBroadcast(i);
        }
    }

    private void enableNotification(){
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())
                && AppController.getInstance().getPrefManager().getUser()!= null){
            Intent i = new Intent();
            i.setAction(MessageService.ACTION_ENABLE_NOTIFICATION);
            if (mHomeActivity != null)
                mHomeActivity.sendBroadcast(i);
        }
    }

    private void moderator(String id)
    {
        if (CheckNetworkConnection.isConnectionAvailable(HomeActivity.this))
        {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(HomeActivity.this);
            Call<Moderator> call = apiService.moderator(id, apiKey);
            call.enqueue(new Callback<Moderator>() {
                @Override
                public void onResponse(@NonNull Call<Moderator> call, @NonNull Response<Moderator> response) {
                    if (response.body() != null && response.isSuccessful()) {
                        Moderator mModerator = response.body();
                        if(mModerator != null) {
                            if(!mModerator.isErrorStatus()) {
                                Log.d("moderator", "success");
                                    if(navigationView != null) {
                                        Menu logoutMenu = navigationView.getMenu();
                                        if(mModerator.isMod()){
                                            Log.d("isModerator", "true");
                                            logoutMenu.findItem(R.id.nav_report).setVisible(true);
                                        } else {
                                            Log.d("isModerator", "false");
                                            logoutMenu.findItem(R.id.nav_report).setVisible(false);
                                        }
                                    }
                            } else {
                                Log.d("moderator", "false");
                                if(navigationView != null) {
                                    Menu logoutMenu = navigationView.getMenu();
                                    logoutMenu.findItem(R.id.nav_report).setVisible(false);
                                }
                            }
                        }
                    } else {
                        if(navigationView != null) {
                            Menu logoutMenu = navigationView.getMenu();
                            logoutMenu.findItem(R.id.nav_report).setVisible(false);
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Moderator> call, @NonNull Throwable t) {
                    Log.e("moderator", "failed");
                    if(navigationView != null) {
                        Menu logoutMenu = navigationView.getMenu();
                        logoutMenu.findItem(R.id.nav_report).setVisible(false);
                    }
                }
            });
        }
        else
        {
            if(navigationView != null)
            {
                Menu logoutMenu = navigationView.getMenu();
                logoutMenu.findItem(R.id.nav_report).setVisible(false);
            }
        }
    }

    @Subscribe
    public void getMessage(String  load)
    {
      //  selectDefaultDrawerItem();
    }

    @Override
    protected void onDestroy()
    {
        logoutSub();
        clearTimer();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void logoutSub()
    {
        if (getLogOutSubscription != null && !getLogOutSubscription.isUnsubscribed())
        {
            getLogOutSubscription.unsubscribe();
        }
    }

    private void clearTimer()
    {
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            Log.d("Timer ", "KILLED");
        }
    }

    private void updateProfilePicture()
    {

        AppController.runOnUIThread(() ->
        {
            String firstName = UserUtils.getFirstName(HomeActivity.this);
            String lastName = UserUtils.getLastName(HomeActivity.this);
            userName = firstName + " " + lastName;
            if (!userName.trim().equalsIgnoreCase(""))
                profileTextView.setText(userName);
            String profilePicture = UserUtils.getUserProfilePicture(HomeActivity.this);
            if(!profilePicture.equalsIgnoreCase(""))
            {
                if (profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png"))
                {


                    profile.setImageResource(R.drawable.bg_circle);
                    profile.setColorFilter(R.color.guest);
                    iconText.setVisibility(View.VISIBLE);
                    if (!userName.equalsIgnoreCase(""))
                    {
                        String[] split = userName.split("\\s+");
                        if (split.length > 1) {
                            String first = split[0].substring(0, 1);
                            String last = split[1].substring(0, 1);
                            String initial = first + "" + last;
                            iconText.setText(initial.toUpperCase());
                        }
                        else
                        {
                            String first = split[0].substring(0, 1);
                            iconText.setText(first.toUpperCase());
                        }
                    }
                }
                else
                    {
                    Uri uriB = Uri.parse(profilePicture);
                    RoundingParams roundingParamsB = RoundingParams.fromCornersRadius(30f);
                    profile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                            .setRoundingParams(roundingParamsB)
                            .build());
                    roundingParamsB.setRoundAsCircle(true);
                    profile.setImageURI(uriB);
                    profile.setColorFilter(null);
                    iconText.setVisibility(View.GONE);
                }
            }
            else
            {
                profile.setImageResource(R.drawable.bg_circle);
                profile.setColorFilter(R.color.guest);
                iconText.setVisibility(View.VISIBLE);
                if (!userName.equalsIgnoreCase(""))
                {
                    String[] split = userName.split("\\s+");
                    if (split.length > 1)
                    {
                        String first = split[0].substring(0,1);
                        String last = split[1].substring(0,1);
                        String initial = first + ""+ last ;
                        iconText.setText(initial.toUpperCase());
                    }
                    else
                    {
                        if (split.length == 1)
                        {
                            String first = split[0].substring(0,1);
                            iconText.setText(first.toUpperCase());
                        }
                    }
                }
            }
        }, 500);
    }

    public static void registerGCM()
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        String token = UserUtils.getFireBaseInstanceId(AppController.getInstance());
        if (token != null && !token.equalsIgnoreCase(""))
        {
            if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()) && AppController.getInstance().getPrefManager().getUser()!= null){
                Intent i = new Intent();
                i.setAction(MessageService.ACTION_SEND_FCM);
                i.putExtra("token",token);
                if (mHomeActivity != null)
                mHomeActivity.sendBroadcast(i);
            }
        }
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        UserUtils.saveFireBaseInstanceId(AppController.getInstance(),refreshedToken);
    }

    private static void doLogoutFromServer(String userId)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        getLogOutSubscription = apiService.logout(userId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .retry(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<LogOut>()
                {
                    @Override
                    public void onCompleted()
                    {
                        ProgressBarTransparentDialog.dismissLoader();
                        goToLogin();
                        Log.d("Logout", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Logout", "onError");
                    }

                    @Override
                    public void onNext(LogOut mLogOut) {}
                });
    }

    private boolean checkPlayServices()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (apiAvailability.isUserResolvableError(resultCode))
            {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void closeDrawers()
    {
        if(drawer != null)
        {
            if (drawer.isDrawerOpen(GravityCompat.START))
            {

                drawer.closeDrawer(GravityCompat.START);
            }
        }
    }


    private void goToChat()
    {
        Intent i = new Intent(this, MainChatRoomActivity.class);
        startActivity(i);
        if (CheckOsVersion.isPreLollipop())
        {
            HomeActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToChat(String id, String from, String chatRoomProfilePic, String online, String color) {
        Intent intent = new Intent(AppController.getInstance(), ChatRoomActivity.class);
        intent.putExtra("page", "push");
        intent.putExtra("chatRoomId", id);
        intent.putExtra("color", color);
        intent.putExtra("chatRoomName", from);
        intent.putExtra("chatRoomProfilePic", chatRoomProfilePic);
        startActivity(intent);
    }

    private void goToUserProfile()
    {
        Intent i = new Intent(this, UserProfileActivity.class);
        startActivity(i);
        if (CheckOsVersion.isPreLollipop())
        {
            HomeActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }


    private void setToolBarTitle(TextView mTitle, String title)
    {
        if(mTitle != null)
          mTitle.setText(title);
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        if(intent.getAction() == null)
            return;

        if (ACTION_SHOW_LOADING_ITEM.equals(intent.getAction()))
        {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_SHOW_LOADING_ITEM));
        }

        else if (intent.getAction().equalsIgnoreCase("notification"))
        {
            if(UserUtils.isGuestLoggedIn(AppController.getInstance()))
            {
                return;
            }
            try
            {
                String jsonString = intent.getStringExtra("object");
                JSONObject jsonObj = new JSONObject(jsonString);
                String type = jsonObj.optString("type");
                String notId = jsonObj.optString("notId");
                String postId = jsonObj.optString("postId");
                String friendId = jsonObj.optString("friendId");
                String companyId = jsonObj.optString("companyId");
                if(jsonObj.has("type")){
                    if(type != null && !type.equalsIgnoreCase("")){
                        if(type.equalsIgnoreCase("post")){
                            if(notId != null && !notId.equalsIgnoreCase("") && postId != null && !postId.equalsIgnoreCase("")){
                                Intent mIntent = new Intent(getApplicationContext(), DetailedPostActivity.class);
                                mIntent.putExtra("notId", notId);
                                mIntent.putExtra("postId", postId);
                                mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
                                startActivity(mIntent);
                            } else {
                                openNotificationFragment();
                            }
                        } else if(type.equalsIgnoreCase("user")){
                            if(friendId != null && !friendId.equalsIgnoreCase("") && notId != null && !notId.equalsIgnoreCase("")){
                                Intent i = new Intent(AppController.getInstance(), UserFriendProfileActivity.class);
                                i.putExtra("friendId", friendId);
                                i.putExtra("notId", notId);
                                startActivity(i);
                            }  else {
                                openNotificationFragment();
                            }
                        }
                        else if(type.equalsIgnoreCase("company"))
                        {
                            if(companyId != null && !companyId.equalsIgnoreCase("") && !companyId.equalsIgnoreCase("0") && notId != null && !notId.equalsIgnoreCase("")){
                                Intent i = new Intent(AppController.getInstance(), CompanyProfileActivity.class);
                                i.putExtra("companyId", companyId);
                                i.putExtra("notId", notId);
                                startActivity(i);
                            } else {
                                openNotificationFragment();
                            }
                        } else if(type.equalsIgnoreCase("bumped")){
                            selectBumpedPostDrawerItem();
                        } else if(type.equalsIgnoreCase("event")){
                            eventDetailActivity(postId);
                        } else if(type.equalsIgnoreCase("missedActivity")){
                            selectDefaultDrawerItem();
                        }
                    } else {
                        openNotificationFragment();
                    }
                }  else {
                    selectDefaultDrawerItem();
                }
                Log.d("JsonObj", jsonObj.toString());
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

      /*  else if (intent.getAction().equalsIgnoreCase("chat"))
        {
            if(UserUtils.isGuestLoggedIn(AppController.getInstance()))
            {
                return;
            }
            String id = intent.getStringExtra("chatRoomId");
            String from = intent.getStringExtra("chatRoomName");
            String chatRoomProfilePic = intent.getStringExtra("chatRoomProfilePic");
            String color = intent.getStringExtra("color");
            if(id != null && !id.equalsIgnoreCase("") && from != null && !from.equalsIgnoreCase("")){
                if (chatRoomProfilePic != null && !chatRoomProfilePic.equalsIgnoreCase(""))
                {
                    goToChat(id, from, chatRoomProfilePic,"", color);
                }
                else if (color != null && !color.equalsIgnoreCase(""))
                {
                    goToChat(id, from, chatRoomProfilePic,"", color);
                }
                else
                {
                    goToChat();
                }
            }
            else
            {
                goToChat();
            }
        }  */

    }

    private void eventDetailActivity(String eventId)
    {
        Intent i = new Intent(AppController.getInstance(), EventDetailActivity.class);
        i.putExtra("eventId", eventId);
        startActivity(i);
    }

    private void setupDrawerContent(NavigationView navigationView)
    {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }

    public void openNotificationFragment()
    {
        closeDrawers();
        Intent i = new Intent(this, NotificationActivity.class);
        startActivity(i);
        if (CheckOsVersion.isPreLollipop())
        {
            HomeActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    public void selectDrawerItem(final MenuItem menuItem)
    {

        switch(menuItem.getItemId())
        {
            case R.id.nav_feeds:
                fragmentClass = FeedsFragment.class;
                HomeActivity.isContactFavourites = true;
                break;
            case R.id.nav_contacts:
                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance())){
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                fragmentClass = ContactsFragment.class;
                HomeActivity.isContactFavourites = false;
                clickedItem = "contact";
                break;
            case R.id.nav_companies:
                fragmentClass = CompanyFragment.class;
                HomeActivity.isContactFavourites = true;
                break;

            case R.id.nav_viewers:
                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance())){
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                fragmentClass = ViewersFragment.class;
                HomeActivity.isContactFavourites = true;
                break;
            case R.id.nav_bumped:
                if(UserUtils.isGuestLoggedIn(AppController.getInstance()))
                {
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                fragmentClass = BumpedPost.class;
                //setToolBarTitle(mTitle, menuItem.getTitle().toString());
                HomeActivity.isContactFavourites = true;
                break;
            case R.id.nav_near_by:
                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance())){
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                fragmentClass = PeopleNearByFragment.class;
                HomeActivity.isContactFavourites = false;
                break;
            case R.id.nav_favourite:
                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance())){
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                fragmentClass = FavouritesFragment.class;
                HomeActivity.isContactFavourites = true;
                clickedItem = "favourite";
                break;
            case R.id.nav_report:
                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance())){
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                fragmentClass = ReportedUserPosts.class;
                HomeActivity.isContactFavourites = true;
                clickedItem = "favourite";
                break;

            /*case R.id.nav_shake:
                fragmentClass = ShakeFragment.class;
                HomeActivity.isContactFavourites = false;
                break;*/

            case R.id.nav_news:
                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance()))
                {
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                fragmentClass = NewsFragment.class;
                HomeActivity.isContactFavourites = false;
                break;
            case R.id.nav_event:
                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance()))
                {
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                fragmentClass = EventFragment.class;
                HomeActivity.isContactFavourites = false;
                break;
            case R.id.nav_discover:
                fragmentClass = DiscoverFragment.class;
                HomeActivity.isContactFavourites = false;
                break;
            case R.id.nav_share:
                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance()))
                {
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                inviteFriends();
                HomeActivity.isContactFavourites = false;
                if(drawer != null)
                    drawer.closeDrawers();
                return;
                //break;

            case R.id.nav_lme:

                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance()))
                {
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                else
                {
                    String subscription = UserUtils.getPriceSubscription(HomeActivity.this);
                if (subscription.equalsIgnoreCase("1"))
                {
                    fragmentClass = LMEFragment.class;
                    HomeActivity.isContactFavourites = false;
                    break;

                }
                else if (subscription != null) {

                    if (subscription.equalsIgnoreCase(""))
                    {
                        UserUtils.savePriceSubscription(HomeActivity.this, "0");
                        showSharePopup();
                        drawer.closeDrawers();
                        return;
                    }
                    else if (subscription.equalsIgnoreCase("0"))
                    {
                        showSharePopup();
                        drawer.closeDrawers();
                        return;
                    }
                    else if (subscription.equalsIgnoreCase("1"))
                    {
                        fragmentClass = LMEFragment.class;
                        HomeActivity.isContactFavourites = false;
                        break;
                    }
                }
                }


            case R.id.nav_about:
                if(AppController.getInstance() != null && UserUtils.isGuestLoggedIn(AppController.getInstance()))
                {
                    GuestLoginDialog.show(this);
                    closeDrawers();
                    return;
                }
                fragmentClass = AboutFragment.class;
                HomeActivity.isContactFavourites = false;
                break;
            case R.id.nav_logout:
                if(UserUtils.isGuestLoggedIn(AppController.getInstance()))
                {
                    GuestLoginDialog.show(HomeActivity.this);
                    return;
                }
                showLogoutDialog();
                HomeActivity.isContactFavourites = false;

                if(drawer != null)
                    drawer.closeDrawers();
                return;

            default:
                fragmentClass = FeedsFragment.class;
                HomeActivity.isContactFavourites = false;
                break;
        }

        if (navigationView.getMenu().findItem(menuItem.getItemId()).isChecked() && !menuItem.getTitle().toString().equalsIgnoreCase("Prices"))
        {
            drawer.closeDrawers();
            return;
        }

        if(drawer != null)
            drawer.closeDrawers();

        navigationView.getMenu().findItem(menuItem.getItemId()).setChecked(true);

        if(mHandler == null)
            mHandler = new Handler();

        Runnable mPendingRunnable = () ->
        {
            try
            {

                if (fragmentClass != null)
                {

                    fragment = (Fragment) fragmentClass.newInstance();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commitAllowingStateLoss();
                    setToolBarTitle(mTitle, menuItem.getTitle().toString());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        };

        mHandler.postDelayed(mPendingRunnable, 1000);
    }

    private void showSharePopup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.custom_lme_popup,null);
        builder.setView(view);
        TextView negative = view.findViewById(R.id.share_negative);
        TextView positive = view.findViewById(R.id.share_positive);

        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                lmeAlert.dismiss();
            }
        });

        positive.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                inviteFriends();
                lmeAlert.dismiss();
            }
        });

        lmeAlert = builder.create();
        lmeAlert.setCancelable(false);
        lmeAlert.show();

    }



    private void inviteFriends()
    {

        sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.shareContent));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share via"));

        String[] params = {UserUtils.getApiKey(HomeActivity.this),UserUtils.getLoggedUserId(HomeActivity.this)};
        new UpdateSubscriptionTask().execute(params);

    }













    private void handleSendText(Intent intent)
    {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null)
        {
            // Update UI to reflect text being shared

            Toast.makeText(getApplicationContext(), "Content send success", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog()
    {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Log Out");
        dialogBuilder.setMessage("Log out now?");
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("LOG OUT", (dialog, which) ->
        {
            if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
            {
  //              RoosterConnectionService.getConnection().disconnect();
                UserOnlineStatus.setUserOnline(HomeActivity.this, "0");
                runOnUiThread(() -> ProgressBarTransparentDialog.showLoader(mHomeActivity, "Logging out..."));
                doLogoutFromServer(AppController.getInstance().getPrefManager().getUser().getId());


            }
            else
            {
                goToLogin();
            }
        });

        dialogBuilder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        final AlertDialog dialog = dialogBuilder.create();
        final String positiveButtonColor = "#118D24";
        dialog.setOnShowListener(arg0 -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(positiveButtonColor));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
        });
        dialog.show();
    }

    public static void goToLogin()
    {

        //disconnect user from xmpp
        if (RoosterConnectionService.getConnection()!=null) {
            RoosterConnectionService.getConnection().disconnect();
        }

        UserUtils.saveLoginStatus(AppController.getInstance(), "0");
        UserUtils.saveNotificationEnable(AppController.getInstance(), "0");
        UserUtils.saveUserFeeds(AppController.getInstance(),"",AppController.getInstance().getPrefManager().getUser().getId());
        UserUtils.saveGuestLoginStatus(AppController.getInstance(), "0");
        UserUtils.saveLoggedUserId(AppController.getInstance(), "");
        UserUtils.savePhone(AppController.getInstance(), "");
        UserUtils.saveUserCity(AppController.getInstance(), "");
        UserUtils.saveCountry(AppController.getInstance(), "");
        UserUtils.saveUserDOB(AppController.getInstance(), "");
        UserUtils.saveUserWeb(AppController.getInstance(),"");
        UserUtils.saveUserProfilePicture(AppController.getInstance(),"");
        UserUtils.saveLoggedUserId(AppController.getInstance(),"");
        AppController.getInstance().logout();
        NotificationUtils.clearNotifications();
        deleteChat();
        Intent intent = new Intent(AppController.getInstance(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP);
        AppController.getInstance().startActivity(intent);

    }

    private static void deleteChat()
    {
        if (myScrapSQLiteDatabase == null)
            myScrapSQLiteDatabase = new MyScrapSQLiteDatabase(AppController.getInstance());
        myScrapSQLiteDatabase.deleteChatRoomTable();
    }

    public void selectDefaultDrawerItem()
    {
        if(navigationView != null)
            // we are setting default fragment here.
        selectDrawerItem(navigationView.getMenu().getItem(0));
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    public void selectFeedsDrawerItem(){
        if(navigationView != null)
           selectDrawerItem(navigationView.getMenu().getItem(0));
    }

    public void selectBumpedPostDrawerItem(){
        if(navigationView != null)
           selectDrawerItem(navigationView.getMenu().findItem(R.id.nav_bumped));
    }

    private void statusBarColor()
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                window.setStatusBarColor(getResources().getColor(R.color.transparent, getTheme()));
            }
            else
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    window.setStatusBarColor(getResources().getColor(R.color.transparent));
                }
            }
        }
    }

    private int getCheckedItem(NavigationView navigationView)
    {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++)
        {
            MenuItem item = menu.getItem(i);
            if (item.isChecked())
            {
                return i;
            }
        }
        return -1;
    }



    @Override
    protected void onResume()
    {
        super.onResume();

        if(UserUtils.isGuestLoggedIn(AppController.getInstance()))
        {
            hideItem();
        }

        if(AppController.getInstance().getPrefManager().getUser() != null)
        {
            moderator(AppController.getInstance().getPrefManager().getUser().getId());
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.NOTIFICATION);
        filter.addAction(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST);
        filter.addAction(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST_STARTS_DOWNLOAD);
        filter.addAction(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST_ENDS_DOWNLOAD);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, filter);

        mTracker.setScreenName("Home Screen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        UserOnlineStatus.setUserOnline(HomeActivity.this, UserOnlineStatus.ONLINE);
        Log.e("onResume", "onResume");
        loadNotificationCount();


    }



    private void hideItem()
    {
        if(navigationView != null)
        {
            Menu logoutMenu = navigationView.getMenu();
            logoutMenu.findItem(R.id.nav_logout).setVisible(false);
            logoutMenu.findItem(R.id.nav_viewers).setVisible(false);
            logoutMenu.findItem(R.id.nav_share).setVisible(false);
            logoutMenu.findItem(R.id.nav_report).setVisible(false);
        }
    }

    private void forceUpdate()
    {
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packageInfo;
        try
        {
            packageInfo =  packageManager.getPackageInfo(getPackageName(),0);
            String currentVersion = packageInfo.versionName;
            new ForceUpdateAsync(currentVersion).execute();
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

    }


    public class ForceUpdateAsync extends AsyncTask<String, String, JSONObject>
    {
        private String latestVersion;
        private String currentVersion;
        private ForceUpdateAsync(String currentVersion)
        {
            this.currentVersion = currentVersion;
        }

        @Override
        protected JSONObject doInBackground(String... params)
        {
            String packageName = AppController.getInstance().getPackageName();
            if (packageName != null && !packageName.isEmpty() && !packageName.equalsIgnoreCase(""))
            {
                try
                {
                    latestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id="+packageName+"&hl=en")
                            .timeout(30000)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://www.google.com")
                            .get()
                            .select(".hAyfc .htlgb")
                            .get(7)
                            .ownText();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject)
        {
            if (mHomeActivity != null && !mHomeActivity.isFinishing())
            {
                if(latestVersion!=null && currentVersion != null)
                {
                    if(!currentVersion.equalsIgnoreCase(latestVersion))
                    {
                        if (!UserUtils.getUpdateCancelVersion(AppController.getInstance()).equalsIgnoreCase(latestVersion))
                        {
                            showForceUpdateDialog();
                        }
                    }
                }
                super.onPostExecute(jsonObject);
            }
        }


        public void showForceUpdateDialog()
        {

            if(mHomeActivity == null)
                return;
           AppController.runOnUIThread(() ->
           {
              if (mHomeActivity != null)
              {
                  AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mHomeActivity);
                  alertDialogBuilder.setTitle(mHomeActivity.getString(R.string.UpdatedTitle));
                  alertDialogBuilder.setMessage(mHomeActivity.getString(R.string.UpdatedMessageOne) + " " + latestVersion + " " +mHomeActivity.getString(R.string.UpdatedMessageTwo));
                  alertDialogBuilder.setCancelable(false);
                  alertDialogBuilder.setPositiveButton(R.string.update, (dialog, id) ->
                  {
                      try
                      {
                          mHomeActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AppController.getInstance().getPackageName())));
                      }
                      catch (android.content.ActivityNotFoundException exception)
                      {
                          mHomeActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + AppController.getInstance().getPackageName())));
                      }
                      dialog.cancel();
                  });
                  alertDialogBuilder.setNegativeButton(R.string.no_thanks, (dialog, which) ->
                  {
                      UserUtils.saveUpdateCancelVersion(mHomeActivity,latestVersion);
                      dialog.cancel();
                  });
                  if (!mHomeActivity.isFinishing())
                      alertDialogBuilder.show();
              }
           });
        }
    }



/*

    public void showForceUpdateDialog()
    {
        if(mHomeActivity == null)
            return;
        AppController.runOnUIThread(() ->
        {
            if (mHomeActivity != null) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mHomeActivity);
                alertDialogBuilder.setTitle(mHomeActivity.getString(R.string.UpdatedTitle));
                alertDialogBuilder.setMessage("This is an old version application, Please update application for better experience.");
          //      alertDialogBuilder.setMessage(mHomeActivity.getString(R.string.UpdatedMessageOne) + " " + latestVersion + " " +mHomeActivity.getString(R.string.UpdatedMessageTwo));
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton(R.string.update, (dialog, id) -> {
                    try {
                        mHomeActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AppController.getInstance().getPackageName())));
                    } catch (android.content.ActivityNotFoundException exception) {
                        mHomeActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + AppController.getInstance().getPackageName())));
                    }
                    dialog.cancel();
                });
                alertDialogBuilder.setNegativeButton(R.string.no_thanks, (dialog, which) ->
                {
         //           UserUtils.saveUpdateCancelVersion(mHomeActivity,latestVersion);
                    dialog.cancel();
                });
                if (!mHomeActivity.isFinishing())
                    alertDialogBuilder.show();
            }
        });
    }
*/


    public static void loadNotificationCount()
    {
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())
                && AppController.getInstance().getPrefManager().getUser()!= null){
            Intent i = new Intent();
            i.setAction(MessageService.ACTION_LOAD_NOTIFICATIONS);
            if (mHomeActivity != null)
                mHomeActivity.sendBroadcast(i);
        }
    }

    private static void updateProfileCompleteness()
    {
        if (profileInfo != null) {
            profileInfo.post(() -> {

                int profilePercentage = Integer.parseInt(UserUtils.parsingInteger(UserUtils.getProfileCompleteness(AppController.getInstance())));

                if (profilePercentage != -1) {
                    if (profilePercentage > 0) {
                        profileInfo.setText(String.valueOf(profilePercentage));
                        Log.d("Profile Info", String.valueOf(profilePercentage));
                        profileInfo.setVisibility(View.VISIBLE);
                    } else {
                        profileInfo.setVisibility(View.GONE);
                    }

                    if (profilePercentage > 0){
                        if(toolbar != null)
                            toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp_red_bottom);
                    } else{
                        if(toolbar != null)
                            toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp);
                    }
                }
            });
        }
    }

    private static void runVisitorCounts()
    {
        int viewersCount;
        if(UserUtils.getViewersCount(AppController.getInstance()) != null && !UserUtils.getViewersCount(AppController.getInstance()).equalsIgnoreCase(""))
        {
            viewersCount = Integer.parseInt(UserUtils.parsingInteger(UserUtils.getViewersCount(AppController.getInstance())));
        }
        else
        {
            viewersCount = 0;
        }

        setNavItemCount(R.id.nav_viewers, viewersCount);

    }






    private static void runBumpedCounts()
    {
        AppController.runOnUIThread(() ->
        {
            int bumpedCount;
            if(UserUtils.getBumpedCount(AppController.getInstance()) != null && !UserUtils.getBumpedCount(AppController.getInstance()).equalsIgnoreCase("")){
                bumpedCount = Integer.parseInt(UserUtils.parsingInteger(UserUtils.getBumpedCount(AppController.getInstance())));
            } else {
                bumpedCount = 0;
            }
            setNavItemCount(R.id.nav_bumped, bumpedCount);
            if(bumpedCount > 0){
                if(bumpedCount > 0){
                    if(toolbar != null)
                        toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp_red_bottom);
                } else{
                    if(toolbar != null)
                        toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp);
                }
            }
        });
    }

    private static void runModeratorCounts()
    {
        AppController.runOnUIThread(() ->
        {
            int moderatorCount;
            if(UserUtils.getModeratorNotificationCount(AppController.getInstance()) != null && !UserUtils.getModeratorNotificationCount(AppController.getInstance()).equalsIgnoreCase("")){
                moderatorCount = Integer.parseInt(UserUtils.parsingInteger(UserUtils.getModeratorNotificationCount(AppController.getInstance())));
            } else {
                moderatorCount = 0;
            }
            setNavItemCount(R.id.nav_report, moderatorCount);
            if(moderatorCount > 0){
                if(moderatorCount > 0){
                    if(toolbar != null)
                        toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp_red_bottom);
                } else{
                    if(toolbar != null)
                        toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp);
                }
            }
        });

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        UserOnlineStatus.setUserOnline(HomeActivity.this,UserOnlineStatus.OFFLINE);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }


    @Override
    protected void onRestart()
    {
        super.onRestart();

        if (isMyServiceRunning(RoosterConnectionService.class))
        {
            // start service to get server connection
            Intent i1 = new Intent(HomeActivity.this, RoosterConnectionService.class);
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

    @Override
    public void onBackPressed()
    {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        invalidateOptionsMenu();

        if(mHomeActivity == null)
            return;

        if(navigationView != null && !navigationView.getMenu().findItem(R.id.nav_feeds).isChecked()){
            selectFeedsDrawerItem();
        }
        else
        {
            if (doubleBackToExitPressedOnce)
            {
                moveTaskToBack(true);
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT).show();
        }


        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_feeds) {
        } else if (id == R.id.nav_contacts) {
        } else if (id == R.id.nav_near_by) {
        } else if (id == R.id.nav_favourite) {
        } else if (id == R.id.nav_shake) {
        }/*else if (id == R.id.nav_news) {
        }*/ else {
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CHECK_SETTINGS)
        {
            switch (requestCode)
            {
                case REQUEST_CHECK_SETTINGS:
                    switch (resultCode)
                    {
                        case Activity.RESULT_OK:
                            UserUtils.saveLocationSetting(AppController.getInstance(),"1");
                            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("DISCOVER_FRAGMENT"));
                            break;
                        case Activity.RESULT_CANCELED:
                            UserUtils.saveLocationSetting(AppController.getInstance(),"1");
                            break;

                        default:
                            break;
                    }
                    break;

            }
        }
        else if (requestCode == REQUEST_CHECK_SETTINGS_PROXIMITY)
        {
            switch (requestCode)
            {
                case REQUEST_CHECK_SETTINGS_PROXIMITY:
                    switch (resultCode)
                    {
                        case Activity.RESULT_OK:
                            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("PEOPLE_NEARBY_FRAGMENT"));
                            break;
                        case Activity.RESULT_CANCELED:
                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }





    public static void notification()
    {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() ->
        {
            if(UserUtils.isGuestLoggedIn(AppController.getInstance()))
            {
                return;
            }

            String getNotification = UserUtils.getNotificationCount(AppController.getInstance());
            Log.d("thisnotification", getNotification);

            String getMSGNotification = UserUtils.getMSGNotificationCount(AppController.getInstance());
            Log.d("MSGNotification", getMSGNotification);

            String getViewCount = UserUtils.getViewersCount(AppController.getInstance());
            Log.d("ViewCountNotification", getViewCount);

            String getBumpedCount = UserUtils.getBumpedCount(AppController.getInstance());
            Log.d("BumpedCountNotification", getBumpedCount);

            String getModeratorCount = UserUtils.getModeratorNotificationCount(AppController.getInstance());
            Log.d("ModeratorCount", getModeratorCount);

            int count = 0;
            int bumpedCount = 0;
            int viewCount = 0;
            int msgCount = 0;
            int modCount = 0;

            int totalNotificationCount = 0;

            if (getNotification != null && !getNotification.equalsIgnoreCase("") && !getNotification.equalsIgnoreCase("0"))
            {
                count = Integer.parseInt(UserUtils.parsingInteger(getNotification));
                if(notCount != null)
                {
                    if(count > 9)
                    {
                        notCount.setText("9+");
                    }
                    else
                    {
                        notCount.setText(getNotification);
                    }
                    notCount.setVisibility(View.VISIBLE);
                }

            }
            else
            {
                if(notCount != null)
                    notCount.setVisibility(View.GONE);
            }

            if(getViewCount  != null && !getViewCount.equalsIgnoreCase(""))
            {
                viewCount = Integer.parseInt(UserUtils.parsingInteger(getViewCount));
                if(viewCount > 0)
                    EventBus.getDefault().post(new ViewerCounts());
            }
            if(getBumpedCount  != null && !getBumpedCount.equalsIgnoreCase("")){
                bumpedCount = Integer.parseInt(UserUtils.parsingInteger(getBumpedCount));
            }
            if(getModeratorCount  != null && !getModeratorCount.equalsIgnoreCase(""))
            {
                modCount = Integer.parseInt(UserUtils.parsingInteger(getModeratorCount));
            }


            /*if (getMSGNotification != null && !getMSGNotification.equalsIgnoreCase("")&& !getMSGNotification.equalsIgnoreCase("0"))
            {
                msgCount = Integer.parseInt(UserUtils.parsingInteger(getMSGNotification));
                if(messageCount != null)
                {
                    if(msgCount > 9)
                    {
                     //   messageCount.setText("9+");
                        messageCount.setText(msgCount);
                    }
                    else
                    {
                        messageCount.setText(getMSGNotification);
                    }
           //         messageCount.setVisibility(View.VISIBLE);

                    // disabling msg count for now  we will on this later
                    messageCount.setVisibility(View.GONE);
                }

                //ShortcutBadger.applyCount(AppController.getInstance(), msgCount);
            }*/


            /*else
            {
                if(messageCount != null)
                    messageCount.setVisibility(View.VISIBLE);
            }*/





       //     int totalNotificationCount = count +  msgCount + viewCount + bumpedCount + modCount;
       //    int totalNotificationCount = count +  msgCount + viewCount + bumpedCount ;

            ChatMessagesTable chatMessagesTable = new ChatMessagesTable(AppController.getInstance());
            String msgCt = chatMessagesTable.getUserCount();
            if (msgCt != null && !msgCt.equalsIgnoreCase("") && !msgCt.equalsIgnoreCase("0"))
            {
                totalNotificationCount = count + Integer.parseInt(msgCt)+ viewCount + bumpedCount ;
            }
            else
            {
                totalNotificationCount = count + viewCount + bumpedCount ;
            }


            Log.d("thisnotification tot", String.valueOf(totalNotificationCount));
            Log.d("thisnotification count", String.valueOf(count));
            Log.d("thisnotification msg", String.valueOf(msgCount));
            Log.d("thisnotification view", String.valueOf(viewCount));
            Log.d("thisnotification bump", String.valueOf(bumpedCount));
            Log.d("thisnotification mod", String.valueOf(modCount));


            if(totalNotificationCount > 0)
            {
                if(toolbar != null)
                    toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp_red_bottom);
            }
            else
            {
                if(toolbar != null)
                    toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp);
            }

            Log.d("Short cut Badge :", String.valueOf(totalNotificationCount));
            ShortcutBadger.applyCount(AppController.getInstance(), totalNotificationCount);
        });
    }


    public static void clearALLNotification()
    {
        if(UserUtils.isGuestLoggedIn(AppController.getInstance()))
        {
            return;
        }

        AppController.runOnUIThread(() ->
        {
            UserUtils.setNotificationCount(AppController.getInstance(), "0");
            String getNotification = UserUtils.getNotificationCount(AppController.getInstance());
            String getMSGNotification = UserUtils.getMSGNotificationCount(AppController.getInstance());
            UserUtils.setViewersCount(AppController.getInstance(), "0");
            String getViewCount = UserUtils.getViewersCount(AppController.getInstance());
            int count = 0;
            int viewCount = 0;
            int msgCount = 0;

            if (getNotification != null && !getNotification.equalsIgnoreCase("") && !getNotification.equalsIgnoreCase("0"))
            {
                if(notCount != null)
                {
                    notCount.setText(getNotification);
                    notCount.setVisibility(View.VISIBLE);
                }

                count = Integer.parseInt(UserUtils.parsingInteger(getNotification));
                ShortcutBadger.applyCount(AppController.getInstance(), count);
            }
            else
            {
                if(notCount != null)
                    notCount.setVisibility(View.GONE);
            }
            if(getViewCount  != null && !getViewCount.equalsIgnoreCase(""))
            {
                viewCount = Integer.parseInt(UserUtils.parsingInteger(getViewCount));
                if(viewCount > 0)
                    EventBus.getDefault().post(new ViewerCounts());
            }


          /*  if (getMSGNotification != null && !getMSGNotification.equalsIgnoreCase("")&& !getMSGNotification.equalsIgnoreCase("0"))
            {
                if(messageCount != null)
                {
                    messageCount.setText(getMSGNotification);
                    messageCount.setVisibility(View.VISIBLE);
                }
                msgCount = Integer.parseInt(UserUtils.parsingInteger(getMSGNotification));
                ShortcutBadger.applyCount(AppController.getInstance(), msgCount);
            }*/


           /*else
            {
                if(messageCount != null)
                    messageCount.setVisibility(View.GONE);
            }*/



            int totalNotificationCount = count + viewCount;
            if(totalNotificationCount > 0)
            {
                if(toolbar != null)
                    toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp_red_bottom);
            }
            else
            {
                if(toolbar != null)
                    toolbar.setNavigationIcon(R.drawable.ic_dehaze_white_24dp);
            }
            ShortcutBadger.applyCount(AppController.getInstance(), totalNotificationCount);

        });

    }

    private void getChatRoom()
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())
                && AppController.getInstance().getPrefManager().getUser()!= null){
            Intent i = new Intent();
            i.setAction(MessageService.ACTION_LOAD_CHAT);
            if (mHomeActivity != null)
                mHomeActivity.sendBroadcast(i);
        }
    }




    public class UpdateSubscriptionTask extends AsyncTask<String,Void,Void>
    {

        @Override
        protected Void doInBackground(String... strs)
        {


            com.android.volley.Response.Listener<String> jsonListener = new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {

                    try
                    {
                        JSONObject responseObject = new JSONObject(response);

                        if (responseObject != null)
                        {

                                Boolean isError = responseObject.getBoolean("error");
                                String status = responseObject.getString("status");
                                if (!isError && status.equalsIgnoreCase("success"))
                                {
                                    //  save the subscription status of user
                                    Boolean subscription = responseObject.getBoolean("isShared");
                                    if (subscription != null && subscription == true)
                                    {
                                        UserUtils.updatePriceSubscription(HomeActivity.this, "1");
                                    }

                                }

                                else
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run()
                                        {
                                            Toast.makeText(getApplicationContext(), "Subscription fail !", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                        }


                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }


                }

            };

            com.android.volley.Response.ErrorListener errorListener = new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("ERROR SERVER ", error.toString());
                }
            };


            StringRequest loginStringRequest = new StringRequest(Request.Method.POST, "https://myscrap.com/android/priceShare", jsonListener, errorListener) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("share", "1");
                    params.put("apiKey",strs[0]);
                    params.put("userId",strs[1]);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    return headers;
                }


            };
            Volley.newRequestQueue(getApplicationContext()).add(loginStringRequest);
            return null;
        }
    }






    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }


}
