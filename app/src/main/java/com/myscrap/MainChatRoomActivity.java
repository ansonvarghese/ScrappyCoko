package com.myscrap;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.myscrap.adapters.ChatRoomAdapter;
import com.myscrap.adapters.NearestRecyclerViewDataAdapter;
import com.myscrap.adapters.TaggingAdapter;
import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.ActiveFriends;
import com.myscrap.model.ChatRoom;
import com.myscrap.model.ChatRoomResponse;
import com.myscrap.model.NearFriends;
import com.myscrap.model.PeopleYouMayKnowItem;
import com.myscrap.notification.Config;
import com.myscrap.notification.NotificationUtils;
import com.myscrap.service.ActiveUsersUpdaterService;
import com.myscrap.service.SocketAPI;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainChatRoomActivity extends AppCompatActivity implements TaggingAdapter.TaggingAdapterListener,SwipeRefreshLayout.OnRefreshListener, ChatRoomAdapter.MessageAdapterListener, SocketAPI.SocketAPIListener, SearchView.OnQueryTextListener{

    private static List<ChatRoom> chatRooms;
    private RecyclerView recyclerView;
    private LinearLayout activeLayout;
    private static ChatRoomAdapter mAdapter;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private ActiveFriends mActiveFriends, mFriendLists;
    private List<NearFriends.NearFriendsData> mNearFriendsLists = new ArrayList<>();
    private NearestRecyclerViewDataAdapter mScrollRecyclerViewAdapter;
    public static final String SET_NOTIFY = "set_notify_main";
    public static final String SET_UPDATE_NOTIFY = "set_update_notify";
    public static final IntentFilter INTENT_FILTER = createIntentFilter();
    private BroadcastReceiver setNotifyReceiver;
    private static MyScrapSQLiteDatabase dbHelper;
    private FloatingActionMenu menuLabelsRight;
    private SearchView searchView;
    private Handler mTypingHandler = new Handler();
    private Handler mClearNotificationHandler = new Handler();
    private static final int TYPING_TIMER_LENGTH = 1000 * 5;
    private ChatRoomAdapter.MessageAdapterListener listener;
    private boolean isGetChatRoom = false;
    private Tracker mTracker;
    private MainChatRoomActivity mMainChatRoomActivity;
    private View emptyView;
    private TextView activeSeeAllLayout;
    private RecyclerView recyclerViewHeader;
    private boolean isRefreshing;
    private MenuItem actionViewItem;
    private Subscription activeFriendsSubscription, updateProfilePicturesSubscription;
    private Subscription friendsListSubscription;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mTracker = AppController.getInstance().getDefaultTracker();
        dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
        menuLabelsRight = (FloatingActionMenu) findViewById(R.id.menu_labels_right);
        FloatingActionButton search = (FloatingActionButton) menuLabelsRight.findViewById(R.id.search);
        FloatingActionButton friends = (FloatingActionButton) menuLabelsRight.findViewById(R.id.friends);
        search.setOnClickListener(v ->
        {
            if (searchView != null) {
                openSearch();
            }
            if (menuLabelsRight != null){
                menuLabelsRight.close(true);
            }
        });
        friends.setOnClickListener(v ->
        {
            if (menuLabelsRight != null){
                menuLabelsRight.close(true);
                String getFriendsList = UserUtils.getUserFriendLists(mMainChatRoomActivity);
                Gson gson = new Gson();
                mFriendLists = gson.fromJson(getFriendsList, ActiveFriends.class);
                if (mFriendLists != null)
                    goToSearchActivity(mFriendLists, "Members");
                else
                    activeFriends();
            }
        });
        listener = this;
        mMainChatRoomActivity = this;
        emptyView = findViewById(R.id.empty);
        UserUtils.setEmptyView(emptyView, R.drawable.ic_pm_empty, "No Chats", false);

        activeLayout = (LinearLayout) findViewById(R.id.active_now_layout);
        activeSeeAllLayout = (TextView) findViewById(R.id.active_now_see_all_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setNestedScrollingEnabled(false);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
        swipeRefreshLayout.setDistanceToTriggerSync(30);// in dips
        recyclerViewHeader = (RecyclerView) findViewById(R.id.active_now_recycler_view);
        recyclerViewHeader.setHasFixedSize(true);
        recyclerViewHeader.setNestedScrollingEnabled(false);
        recyclerViewHeader.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppController.getInstance(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        recyclerViewHeader.setLayoutManager(linearLayoutManager);
        mNearFriendsLists = new ArrayList<>();
        chatRooms = new ArrayList<>();
        mScrollRecyclerViewAdapter = new NearestRecyclerViewDataAdapter(AppController.getInstance(), mNearFriendsLists, true, NearFriends.VIEW_TYPE_ACTIVE);
        mScrollRecyclerViewAdapter.setHasStableIds(true);
        recyclerViewHeader.setAdapter(mScrollRecyclerViewAdapter);
        setupAdapter();
        actionModeCallback = new ActionModeCallback();
        chatRooms.clear();

        if(dbHelper == null)
            dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());

        if(dbHelper.getChatRoomList() != null)
        {
            chatRooms.addAll(dbHelper.getChatRoomList());
        }

        if (chatRooms != null && chatRooms.isEmpty()){
            if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                new Handler().post(this::getChatRoom);
            }
        } else {
            if (chatRooms != null && !chatRooms.isEmpty())
                new Handler().post(this::updateProfilePictures);

        }

        if (activeSeeAllLayout != null) {
            activeSeeAllLayout.setOnClickListener(v -> goToList(mActiveFriends, "Active Users"));
        }

        clearMessageNotification();

        setNotifyReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent.getAction() == null)
                    return;

                if (intent.getAction().equals(SET_NOTIFY))
                {
                    setNotifyChanges();
                }
                else if (intent.getAction().equals(Config.PUSH_NOTIFICATION))
                {
                    String fromId = intent.getStringExtra("fromId");
                    AppController.getInstance().getPreferenceManager().addMessageRead("unread",fromId);
                    setNotifyChanges();
                }
                else if (intent.getAction().equals(Config.MESSAGE_TYPING))
                {
                    String typingFrom = intent.getStringExtra("fromId");
                    if (mAdapter != null)
                    {
                        runOnUiThread(() -> {
                            if (!chatRooms.isEmpty())
                                mAdapter.showTyping(chatRooms, typingFrom);
                        });
                    }
                    mTypingHandler.removeCallbacks(onTypingTimeout);
                    mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
                } else if (intent.getAction().equals(Config.MESSAGE_STOP_TYPING)) {
                    if(mTypingHandler !=null)
                        mTypingHandler.removeCallbacks(onTypingTimeout);
                    setNotifyChanges();
                } else if (intent.getAction().equals(Config.MESSAGE_SEEN)) {
                    setNotifyChanges();
                } else if (intent.getAction().equals(Config.MESSAGE_RECEIVED)) {
                    setNotifyChanges();
                }
            }
        };
    }


    private void friendsList()
    {
        if (!CheckNetworkConnection.isConnectionAvailable(mMainChatRoomActivity))
            return;

        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        friendsListSubscription = apiService.getFriendsList(userId, apiKey)
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
                            mFriendLists = activeFriends;
                            Gson gson = new Gson();
                            String mFriendList = gson.toJson(mFriendLists);
                            UserUtils.saveUserFriendLists(mMainChatRoomActivity, mFriendList);
                        }
                        Log.d("FriendsList", "onNext: ");
                    }
                });
    }

    private void setScrollAdapter(List<NearFriends.NearFriendsData> mNearFriendsLists)
    {
        if (recyclerViewHeader != null && mNearFriendsLists != null)
        {
            mScrollRecyclerViewAdapter = new NearestRecyclerViewDataAdapter(AppController.getInstance(), mNearFriendsLists, true, NearFriends.VIEW_TYPE_ACTIVE);
            mScrollRecyclerViewAdapter.setHasStableIds(true);
            recyclerViewHeader.setAdapter(mScrollRecyclerViewAdapter);
            doLayoutChanges(mNearFriendsLists);
        }

    }

    private void doLayoutChanges(List<NearFriends.NearFriendsData> mNearFriendsLists) {
        if (mNearFriendsLists == null)
            return;

        if (mNearFriendsLists.size() > 0) {
            if (activeLayout!= null && !activeLayout.isShown())
                activeLayout.setVisibility(View.VISIBLE);
            if (mNearFriendsLists.size() >= 10 ) {
                activeSeeAllLayout.setVisibility(View.VISIBLE);
            } else {
                activeSeeAllLayout.setVisibility(View.GONE);
            }
        } else {
            mNearFriendsLists.clear();
            activeLayout.setVisibility(View.GONE);
            activeSeeAllLayout.setVisibility(View.GONE);
        }
        if (mScrollRecyclerViewAdapter != null)
            mScrollRecyclerViewAdapter.notifyDataSetChanged();
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    private void goToList(ActiveFriends mActiveFriends, String mPageName) {
        Intent intent = new Intent(AppController.getInstance(), ActiveListActivity.class);
        Gson gson = new Gson();
        String activeUsersData = gson.toJson(mActiveFriends, ActiveFriends.class);
        intent.putExtra("activeUsersList", activeUsersData);
        intent.putExtra("pageName", mPageName);
        startActivity(intent);
        if (CheckOsVersion.isPreLollipop()) {
            this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToSearchActivity(ActiveFriends mActiveFriends, String mPageName)
    {
        Intent intent = new Intent(AppController.getInstance(), SearchViewActivity.class);
        Gson gson = new Gson();
        String activeUsersData = gson.toJson(mActiveFriends, ActiveFriends.class);
        intent.putExtra("activeUsersList", activeUsersData);
        intent.putExtra("pageName", mPageName);
        startActivity(intent);
        if (CheckOsVersion.isPreLollipop())
        {
            this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }

    }

    private void setupAdapter()
    {
       if (recyclerView != null)
            recyclerView.post(() -> {
                    mAdapter = new ChatRoomAdapter(mMainChatRoomActivity, chatRooms, mNearFriendsLists, listener);
                    mAdapter.setHasStableIds(true);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mMainChatRoomActivity);
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(mAdapter);
                 });
    }

    private void openSearch()
    {
        if(searchView != null && actionViewItem != null)
        {
            actionViewItem.expandActionView();
            searchView.requestFocus();
        }
    }

    private void activeFriends()
    {
        if (!CheckNetworkConnection.isConnectionAvailable(mMainChatRoomActivity))
            return;

        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        activeFriendsSubscription = apiService.getActiveFriends(userId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ActiveFriends>() {
                    @Override
                    public void onCompleted() {
                        Log.d("activeFriends", "onCompleted: ");
                        if (isRefreshing) {
                            isRefreshing = false;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("activeFriends", "onError: ");
                        if (isRefreshing) {
                            isRefreshing = false;
                        }
                    }

                    @Override
                    public void onNext(ActiveFriends activeFriends) {
                        if (activeFriends != null) {
                            mActiveFriends = activeFriends;
                            List<NearFriends.NearFriendsData> activeFriendsData = activeFriends.getActiveFriendsData();
                            if (activeFriendsData != null){
                                if (isRefreshing) {
                                    isRefreshing = false;
                                    update(activeFriendsData);
                                } else {
                                    mNearFriendsLists.clear();
                                    mNearFriendsLists.addAll(activeFriendsData);
                                    doLayoutChanges(mNearFriendsLists);
                                }
                            }

                        }
                        Log.d("activeFriends", "onNext: ");
                    }
                });
    }

    private void updateProfilePictures(){
        if (!CheckNetworkConnection.isConnectionAvailable(mMainChatRoomActivity))
            return;

        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        updateProfilePicturesSubscription = apiService.getProfilePictures(userId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ChatRoomResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.d("updateProfilePictures", "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("updateProfilePictures", "onError: ");
                    }

                    @Override
                    public void onNext(ChatRoomResponse chatRoomResponse) {
                        Log.d("updateProfilePictures", "onNext: ");
                        updateProfile(chatRoomResponse);
                    }
                });
    }

    private void updateProfile(ChatRoomResponse chatRoomResponse) {
        List<ChatRoom> chatRoomList = new ArrayList<>();
        if (chatRoomResponse != null) {
            if (!chatRoomResponse.isErrorStatus()) {
                if (chatRoomResponse.getResults() != null)
                    chatRoomList = chatRoomResponse.getResults();
                if (chatRoomList != null && !chatRoomList.isEmpty()) {
                    for (int k=0; k < chatRoomList.size(); k++) {
                        String profilePicture = chatRoomList.get(k).getProfilePic();
                        String color = chatRoomList.get(k).getColor();
                        String name = chatRoomList.get(k).getName();
                        int id = chatRoomList.get(k).getId();
                        if (id != 0 && profilePicture != null && color != null && name != null) {
                            if(dbHelper == null)
                                dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
                            dbHelper.updateProfilePicture(String.valueOf(id), profilePicture, color);
                        }
                    }
                    setNotifyChanges();
                }
            }
        }
    }

    private void update(List<NearFriends.NearFriendsData> activeFriends)
    {
        new Handler().post(() -> {
            if (mScrollRecyclerViewAdapter != null){
                if (activeLayout != null) {
                    if (activeFriends != null) {
                        mNearFriendsLists.clear();
                        mNearFriendsLists.addAll(activeFriends);
                        setScrollAdapter(mNearFriendsLists);
                    } else {
                        activeLayout.setVisibility(View.GONE);
                        activeSeeAllLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }


    boolean contains(List<PeopleYouMayKnowItem> itemList, String id)
    {
        for (PeopleYouMayKnowItem item : itemList)
        {
            if (item.getFriendId().equals(id))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void clearMessageNotification()
    {
        HomeActivity.notification();
    }

    private void setNotifyChanges()
    {

        if (chatRooms != null && mAdapter != null && recyclerView != null){
            recyclerView.post(() -> {
                if(dbHelper == null)
                    dbHelper = MyScrapSQLiteDatabase.getInstance(mMainChatRoomActivity);

                if (dbHelper.getChatRoomList() != null) {
                    chatRooms.clear();
                    chatRooms.addAll(dbHelper.getChatRoomList());
                }

                if(chatRooms != null && !chatRooms.isEmpty()){
                    for (int i=0; i <chatRooms.size(); i++){
                        if(chatRooms.get(i).getFrom() != null && !chatRooms.get(i).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                            if (chatRooms.get(i).getStatus().equalsIgnoreCase("3")){
                                chatRooms.get(i).setRead(true);
                            } else {
                                chatRooms.get(i).setRead(false);
                            }
                        } else {
                            chatRooms.get(i).setRead(true);
                        }
                    }
                    swipeRefreshLayout.post(() -> {
                        mAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    });
                } else {
                    swipeRefreshLayout.post(() -> {
                        swipeRefreshLayout.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    });
                }

            });
            if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        }

    }

    @Override
    public void onAdapterClicked(int position) {

    }

    private static class MyAsyncTask extends AsyncTask<Void, Void, Void>{

        private WeakReference<MainChatRoomActivity> activityReference;

        MyAsyncTask(MainChatRoomActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("AsyncTask", "Started: ");
        }

        @Override
        protected Void doInBackground(Void... params) {

            if(chatRooms != null)
               chatRooms.clear();

            if(dbHelper.getChatRoomList() != null)
                chatRooms.addAll(dbHelper.getChatRoomList());

            if(chatRooms != null && chatRooms.size() > 0) {
                for (int i=0; i <chatRooms.size(); i++){
                    if(chatRooms.get(i).getFrom() != null && !chatRooms.get(i).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        if (chatRooms.get(i).getStatus().equalsIgnoreCase("3")){
                            chatRooms.get(i).setRead(true);
                        } else {
                            chatRooms.get(i).setRead(false);
                        }
                    } else {
                        chatRooms.get(i).setRead(true);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            MainChatRoomActivity activity = activityReference.get();
            if (activity == null) return;


            if(mAdapter != null && chatRooms != null)
                mAdapter.notifyDataSetChanged();
               // mAdapter.swap(chatRooms);

            Log.d("AsyncTask", "Completed: ");
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
        filter.addAction(ActiveUsersUpdaterService.BROADCAST_ACTIVE_USER_UPDATER_ACTION);
        return filter;
    }

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if(mTypingHandler !=null)
                mTypingHandler.removeCallbacks(onTypingTimeout);
            setNotifyChanges();
        }
    };

    private  Runnable clear = new Runnable() {
        @Override
        public void run() {
                clearMessageNotification();
            if (mClearNotificationHandler != null) {
                mClearNotificationHandler.removeCallbacks(clear);
                mClearNotificationHandler.postDelayed(clear, 1000 * 2);
            }
        }
    };

    private void getChatRoom() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        isGetChatRoom = true;
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(true);
        if(dbHelper == null)
            dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
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
                Log.d("getChatRoom", "onCompleted: ");
                if(swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("getChatRoom", "onError: ");
                isGetChatRoom = false;
                if(swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onNext(ChatRoomResponse chatRoomResponse) {
                Log.d("getChatRoom", "onNext: ");
                updateResponse(chatRoomResponse);
            }
        });
    }

    private void updateResponse(final ChatRoomResponse chatRoomResponse){
        if(chatRooms != null)
            chatRooms.clear();
        isGetChatRoom = false;
        if (chatRoomResponse != null) {
            if(!chatRoomResponse.isErrorStatus()){
                if (chatRoomResponse.getResults() != null)
                    chatRooms.addAll(chatRoomResponse.getResults());
                if (chatRooms != null && !chatRooms.isEmpty()) {
                    if (dbHelper != null)
                        dbHelper.deleteChatRoomTable();
                    AppController.getInstance().getPreferenceManager().clearMessageRead();
                    int k;
                    for (k=0; k < chatRooms.size(); k++) {
                        String profilePicture = chatRooms.get(k).getProfilePic();
                        String color = chatRooms.get(k).getColor();
                        String profileName = chatRooms.get(k).getName();
                        String lastSeenTime = ""/*chatRooms.get(k).getSeenTime()*/;
                        int unreadMessageCount = chatRooms.get(k).getUnReadMessageCount();
                        List<ChatRoom> mChatRoomMessagesCopy  = chatRooms.get(k).getData();
                        if(mChatRoomMessagesCopy != null && !mChatRoomMessagesCopy.isEmpty()){
                            for (int i=0; i < mChatRoomMessagesCopy.size(); i++) {
                                dbHelper.addMessage(mChatRoomMessagesCopy.get(i).getFrom(),mChatRoomMessagesCopy.get(i).getTo(),mChatRoomMessagesCopy.get(i).getMessage() != null ? mChatRoomMessagesCopy.get(i).getMessage() :"",  mChatRoomMessagesCopy.get(i).getMessageType(), mChatRoomMessagesCopy.get(i).getMessageChatImage() != null ? mChatRoomMessagesCopy.get(i).getMessageChatImage() :"", String.valueOf(mChatRoomMessagesCopy.get(i).getId()),"","","","", String.valueOf(mChatRoomMessagesCopy.get(i).getId()),mChatRoomMessagesCopy.get(i).getTimeStamp(),mChatRoomMessagesCopy.get(i).getStatus(),mChatRoomMessagesCopy.get(i).getSeenT(), profileName, profilePicture, color, unreadMessageCount);
                            }
                        }
                    }
                    setNotifyChanges();
                    swipeRefreshLayout.post(() -> {
                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    });

                } else {
                    if(swipeRefreshLayout != null) {
                        swipeRefreshLayout.post(() -> {
                            SnackBarDialog.show(swipeRefreshLayout, "Start a new chat.");
                            swipeRefreshLayout.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        });
                    }
                }
            } else {
                if(swipeRefreshLayout != null){
                    swipeRefreshLayout.post(() -> {
                        SnackBarDialog.show(swipeRefreshLayout, chatRoomResponse.getStatus());
                        emptyView.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                    });
                }
            }
        } else {
            if(swipeRefreshLayout != null)
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
            if (recyclerView != null)
                recyclerView.post(() -> Snackbar.make(recyclerView, "Something went wrong.", Snackbar.LENGTH_LONG)
                        .show());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setOnQueryTextListener(this);
            if (searchManager != null) {
                searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));
            }
            return true;
        } else if (id == android.R.id.home) {
                this.finish();
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        actionViewItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(actionViewItem);
        searchView.setOnQueryTextListener(this);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onRefresh()
    {
        AppController.getInstance().getPreferenceManager().clearMessageRead();
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            new Handler().post(this::getChatRoom);
            isRefreshing = true;
            new Handler().postDelayed(this::activeFriends, 1000);
        } else
            Toast.makeText(AppController.getInstance(), "No internet connections available", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onIconClicked(final int position)
    {
        if (actionMode == null)
        {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        AppController.runOnUIThread(() -> toggleSelection(position));

    }

    @Override
    public void onIconImportantClicked(int position)
    {
    }

    @Override
    public void onMessageRowClicked(final int position)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
            return;

        AppController.runOnUIThread(() -> {
            if (mAdapter.getSelectedItemCount() > 0) {
                enableActionMode(position);
            } else {
                if (!chatRooms.isEmpty()){
                    ChatRoom chatRoom = chatRooms.get(position);
                    if (chatRoom != null){
                        chatRoom.setRead(true);
                        if (chatRoom.getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                            goToChat(chatRoom.getTo(),chatRoom.getMessageFrom(), chatRoom.getProfilePic(), chatRoom.getColor());
                        } else {
                            goToChat(chatRoom.getFrom(),chatRoom.getMessageFrom(), chatRoom.getProfilePic(), chatRoom.getColor());
                        }
                    }
                }
            }
        });

    }

    private void goToChat(String id, String from, String chatRoomProfilePic, String color) {
        if(mAdapter != null && recyclerView != null){
            recyclerView.post(() -> mAdapter.notifyDataSetChanged());
        }

        Intent intent = new Intent(AppController.getInstance(), ChatRoomActivity.class);
        intent.putExtra("chatRoomId", id);
        intent.putExtra("color", color);
        intent.putExtra("chatRoomName", from);
        intent.putExtra("chatRoomProfilePic", chatRoomProfilePic);
        intent.putExtra("online", "0");
        startActivity(intent);
    }

    @Override
    public void onRowLongClicked(int position) {
        enableActionMode(position);
    }

    private void enableActionMode(final int position) {
        AppController.runOnUIThread(() -> {
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback);
            }
            toggleSelection(position);
        });

    }

    private void toggleSelection(final int position) {
        AppController.runOnUIThread(() -> {
            mAdapter.toggleSelection(position);
            int count = mAdapter.getSelectedItemCount();
            if (count == 0) {
                actionMode.finish();
            } else {
                actionMode.setTitle(String.valueOf(count));
                actionMode.invalidate();
            }
        });

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(chatRooms != null && chatRooms.size() > 0) {
            final List<ChatRoom> filteredModelList = filter(chatRooms, newText);
            if (filteredModelList != null && filteredModelList.size() > 0) {
                mAdapter.setFilter(filteredModelList, newText);
                return true;
            } else {
                if(recyclerView != null){
                    SnackBarDialog.show(recyclerView, "No user found");
                }
                return false;
            }
        }
        return true;
    }

    private List<ChatRoom> filter(List<ChatRoom> chatRooms, String query) {

        final List<ChatRoom> chatRoomsCopy = new ArrayList<>();

        if(chatRooms == null)
            return chatRoomsCopy;

        for (ChatRoom chatRoom : chatRooms){
            final String text = chatRoom.getMessageFrom().toLowerCase().toLowerCase();
            if (text.contains(query.toLowerCase())) {
                chatRoomsCopy.add(chatRoom);
            }
        }
        return chatRoomsCopy;
    }

    private class ActionModeCallback implements ActionMode.Callback
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setEnabled(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteMessages();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            swipeRefreshLayout.setEnabled(true);
            actionMode = null;
            recyclerView.post(() -> mAdapter.resetAnimationIndex());
        }
    }

    private void deleteMessages() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(dbHelper == null)
            dbHelper = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
        mAdapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {

            int chatRoomId;
            if(chatRooms != null && !chatRooms.isEmpty()){
                if (chatRooms.get(selectedItemPositions.get(i)).getFrom().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    chatRoomId = Integer.parseInt(UserUtils.parsingInteger(chatRooms.get(selectedItemPositions.get(i)).getTo()));
                    dbHelper.deleteChatRoomMessages(chatRoomId);
                    NotificationUtils.clearNotificationByID(chatRoomId);
                } else {
                    chatRoomId = Integer.parseInt(UserUtils.parsingInteger(chatRooms.get(selectedItemPositions.get(i)).getFrom()));
                    dbHelper.deleteChatRoomMessages(chatRoomId);
                    NotificationUtils.clearNotificationByID(chatRoomId);
                }

                AppController.getInstance().getPreferenceManager().clear();
                mAdapter.removeData(selectedItemPositions.get(i));
                mAdapter.notifyDataSetChanged();
                deleteChatRoom(AppController.getInstance().getPrefManager().getUser().getId(), String.valueOf(chatRoomId));
            }
        }

        setNotifyChanges();
    }

    private void deleteChatRoom(String userId, String chatRoomId) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(this);
        Call<String> call = apiService.deleteChatRooms(userId, chatRoomId, apiKey);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.d("deleteChatRoom", "onSuccess");
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.d("deleteChatRoom", "onFailure");
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mClearNotificationHandler != null) {
            mClearNotificationHandler.removeCallbacks(clear);
        }
        if (mTypingHandler != null) {
            mTypingHandler.removeCallbacks(onTypingTimeout);
        }

        if (activeFriendsSubscription != null && activeFriendsSubscription.isUnsubscribed())
            activeFriendsSubscription.unsubscribe();

        if (friendsListSubscription != null && friendsListSubscription.isUnsubscribed())
            friendsListSubscription.unsubscribe();

        if (updateProfilePicturesSubscription != null && updateProfilePicturesSubscription.isUnsubscribed())
            updateProfilePicturesSubscription.unsubscribe();

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppController.service();
        LocalBroadcastManager.getInstance(this).registerReceiver(setNotifyReceiver,INTENT_FILTER);
        mClearNotificationHandler.postDelayed(clear, 1000 * 2);
        if(menuLabelsRight != null)
            UserUtils.hideKeyBoard(AppController.getInstance(), menuLabelsRight);
        if(mTracker != null){
            mTracker.setScreenName("Main Chat Room Activity Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        if(!isGetChatRoom){
            setNotifyChanges();
        }

        activeFriends();

        UserOnlineStatus.setUserOnline(this, UserOnlineStatus.ONLINE);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(setNotifyReceiver);
        if (mClearNotificationHandler != null) {
            mClearNotificationHandler.removeCallbacks(clear);
        }
        UserOnlineStatus.setUserOnline(this, UserOnlineStatus.OFFLINE);
    }

}
