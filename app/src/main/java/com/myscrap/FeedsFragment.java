package com.myscrap;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myscrap.adapters.FeedsAdapter;
import com.myscrap.adapters.NearestRecyclerViewDataAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.DeletePost;
import com.myscrap.model.Favourite;
import com.myscrap.model.Feed;
import com.myscrap.model.LikedData;
import com.myscrap.model.NearFriends;
import com.myscrap.model.Report;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.FeedContextMenu;
import com.myscrap.view.FeedContextMenuManager;
import com.myscrap.view.PreCachingLayoutManager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.myscrap.model.NearFriends.VIEW_TYPE_FEED;


public class FeedsFragment extends Fragment implements  FeedsAdapter.OnFeedItemClickListener,
        FeedContextMenu.OnFeedContextMenuItemClickListener{

    public static final String ACTION_SHOW_LOADING_ITEM = "action_show_loading_item";
    public static final String ONLINE = "online";
    private FeedsAdapter feedAdapter;
    //private FeedAdapter feedAdapter;
    private List<Feed.FeedItem> feedItems = new ArrayList<>();
    private RecyclerView rvFeed;
    private SwipeRefreshLayout swipe;
    private BroadcastReceiver mBroadcastReceiver;
    private String pageLoad = "0";
    private boolean isLoadMore = false;
    private boolean isBottomRefresh = false;
    private LinearLayout rootStatusLayout;
    private LinearLayout rootStatusBoxLayout;
    private List<NearFriends.NearFriendsData> mNearFriendsLists = new ArrayList<>();
    private RecyclerView recyclerViewHeader;
    private NearestRecyclerViewDataAdapter mScrollRecyclerViewAdapter;
    private int frequency = 3 * 60 * 1000;
    private SimpleDraweeView bottomProfile;
    private TextView iconText;
    private Tracker mTracker;
    private PreCachingLayoutManager linearLayoutManager;
    private Parcelable state;
    private Subscription loadNearestFriendsSubscription, getFeedsSubscription;
    AnimatorSet mSet;

    public FeedsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(linearLayoutManager != null)
            state = linearLayoutManager.onSaveInstanceState();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(linearLayoutManager != null && state != null)
            linearLayoutManager.onRestoreInstanceState(state);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View  view = inflater.inflate(R.layout.fragment_feeds, container, false);


        swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);//
        swipe.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                pageLoad = "0";
                showRefreshingView();
                loadNearestFriends();
                loadFeed();
            } else {
                SnackBarDialog.showNoInternetError(swipe);
            }

        });

        rvFeed = (RecyclerView) view.findViewById(R.id.recycler_view_feeds);
        rvFeed.setHasFixedSize(true);
        rootStatusLayout = (LinearLayout) view.findViewById(R.id.root_status_layout);
        bottomProfile = (SimpleDraweeView) view.findViewById(R.id.user_profile);
        ImageView bottomCamera = (ImageView) view.findViewById(R.id.camera);
        TextView bottomStatusTextView = (TextView) view.findViewById(R.id.status);
        iconText = (TextView) view.findViewById(R.id.icon_text);
        rootStatusBoxLayout = (LinearLayout) view.findViewById(R.id.root_status_box_layout);
        RelativeLayout bottomLayout = (RelativeLayout) view.findViewById(R.id.status_layout);

        bottomLayout.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(getContext())){
                GuestLoginDialog.show(getContext());
                return;
            }
            goToStatusActivity("");
        });
        bottomCamera.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(getContext())){
                GuestLoginDialog.show(getContext());
                return;
            }
            goToStatusActivity("camera");
        });
        bottomStatusTextView.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(getContext())){
                GuestLoginDialog.show(getContext());
                return;
            }
            goToStatusActivity("");
        });
        bottomProfile.setOnClickListener(v -> goToUserProfile());
        recyclerViewHeader = (RecyclerView) view.findViewById(R.id.recycler_active_friends);
        recyclerViewHeader.setHasFixedSize(true);
        recyclerViewHeader.setItemViewCacheSize(20);
        recyclerViewHeader.setDrawingCacheEnabled(true);
        recyclerViewHeader.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerViewHeader.setNestedScrollingEnabled(false);
        recyclerViewHeader.setItemAnimator(new DefaultItemAnimator());
        setHorizontalLayout(mNearFriendsLists);
        if( getActivity() != null){
            linearLayoutManager = new PreCachingLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            linearLayoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
            linearLayoutManager.setInitialPrefetchItemCount(3);
        }

        if(linearLayoutManager != null)
            rvFeed.setLayoutManager(linearLayoutManager);

        rvFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!recyclerView.canScrollVertically(1))
                {
                    int mPage = feedItems.size();
                    pageLoad = String.valueOf(mPage);
                    if (swipe != null && !swipe.isRefreshing() && !isBottomRefresh){
                        if(CheckNetworkConnection.isConnectionAvailable(getActivity()))
                        {
                            loadMoreFeeds();
                        }
                        else
                        {
                            SnackBarDialog.showNoInternetError(rootStatusBoxLayout);
                        }
                    }
                }
                if(rootStatusBoxLayout != null && rootStatusBoxLayout.getVisibility() != View.GONE || rootStatusBoxLayout.getVisibility() != View.INVISIBLE)
                {
                    rootStatusBoxLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view.findViewById(R.id.mImage), "scaleX", 0.5f);//0.5
        scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleXAnimator.setDuration(1000);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view.findViewById(R.id.mImage), "scaleY", 0.5f);//0.5
        scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleYAnimator.setDuration(1000);


        ObjectAnimator rotationAnimation = ObjectAnimator.ofFloat(view.findViewById(R.id.mImage), "rotation", 0f, 360f);
        rotationAnimation.setRepeatMode(ValueAnimator.RESTART);
        rotationAnimation.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimation.setDuration(1000);

        mSet = new AnimatorSet();
        mSet.playTogether(scaleXAnimator, scaleYAnimator, rotationAnimation);
        mSet.start();
        return view;
    }

    Handler mHandler = new Handler();
    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run()
        {
                startFetching();
            mHandler.postDelayed(mHandlerTask, frequency);
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void startFetching() {
        AppController.runOnUIThread(this::loadNearestFriends, 2000);
    }

    private void showRefreshingView(){
        if(swipe != null){
            new Thread(() -> swipe.post(() -> swipe.setRefreshing(true))).start();

        }
    }

    private void hideRefreshingView(){
        if(swipe != null){
            new Thread(() -> swipe.post(() -> swipe.setRefreshing(false))).start();

        }
    }

    private void showBottomRefreshingView(){
        isBottomRefresh = true;
        if(rootStatusLayout != null)
            rootStatusLayout.setVisibility(View.VISIBLE);
        /*if(rootStatusBoxLayout != null)
            rootStatusBoxLayout.setVisibility(View.GONE);*/
    }

    private void hideBottomRefreshingView(){
        isBottomRefresh = false;
        if(rootStatusLayout != null && rootStatusLayout.isShown())
            rootStatusLayout.setVisibility(View.GONE);
        if(rootStatusBoxLayout != null && rootStatusBoxLayout.isShown())
            rootStatusBoxLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupFeed();

        //loadOfflineUserFeeds();

        if(mHandlerTask != null)
            mHandlerTask.run();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        };
    }

    private void loadFeed() {
        final String apiKey = UserUtils.getApiKey(getActivity());
        if(UserUtils.isGuestLoggedIn(getContext())){
            getFeeds(pageLoad, "3", "0", apiKey);
        } else {
            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())) {
                if(AppController.getInstance().getPrefManager().getUser() != null){
                    final String userId = AppController.getInstance().getPrefManager().getUser().getId();
                    final String friendId = "0";
                    getFeeds(pageLoad, userId, friendId, apiKey);
                }
            } else {
                if(swipe != null)
                    SnackBarDialog.showNoInternetError(swipe);
            }
        }


    }

    private void loadNearestFriends(){
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(getActivity());
        loadNearestFriendsSubscription = apiService.nearestFriends(userId,apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<NearFriends>() {
                    @Override
                    public void onCompleted() {
                        Log.d("loadNearestFriends", "Success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("loadNearestFriends", "Error");
                        if(recyclerViewHeader != null) {
                            if(mScrollRecyclerViewAdapter != null && mScrollRecyclerViewAdapter.getItemCount() < 0)
                                recyclerViewHeader.setVisibility(View.GONE);
                            setHorizontalLayout(null);
                        }
                    }

                    @Override
                    public void onNext(NearFriends mNearFriends) {
                        Log.d("loadNearestFriends", "on Next");
                        if(mNearFriends != null && !mNearFriends.isErrorStatus()){
                            final List<NearFriends.NearFriendsData> data = mNearFriends.getData();
                            if(data != null && data.size() > 0) {
                                if(recyclerViewHeader != null) {
                                    recyclerViewHeader.setVisibility(View.VISIBLE);
                                    setHorizontalLayout(data);
                                }
                            } else {
                                if(recyclerViewHeader != null) {
                                    recyclerViewHeader.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });
    }

    private void setHorizontalLayout(List<NearFriends.NearFriendsData> data) {
        if(recyclerViewHeader != null &&  mNearFriendsLists != null){
            if (getActivity() != null) {
                new Handler().post(() -> {
                    if(data != null && data.size() > 0){
                        mNearFriendsLists.clear();
                        mNearFriendsLists.addAll(data);
                    }
                    mScrollRecyclerViewAdapter = new NearestRecyclerViewDataAdapter(AppController.getInstance(), mNearFriendsLists, true,VIEW_TYPE_FEED);
                    final LinearLayoutManager layoutManager = new LinearLayoutManager(AppController.getInstance(), LinearLayoutManager.VERTICAL, true);
                    recyclerViewHeader.setLayoutManager(layoutManager);
                    recyclerViewHeader.setAdapter(mScrollRecyclerViewAdapter);
                });
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void updateProfilePicture() {
        if (getActivity() == null)
            return;

        String profilePicture = UserUtils.getUserProfilePicture(getActivity());
        String firstName = UserUtils.getFirstName(getActivity());
        String lastName = UserUtils.getLastName(getActivity());
        String userName = firstName + " " + lastName;
        if(profilePicture != null && !profilePicture.equalsIgnoreCase("")) {
            if (profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                    || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")) {
                bottomProfile.setImageResource(R.drawable.bg_circle);
                bottomProfile.setColorFilter(R.color.guest);
                iconText.setVisibility(View.VISIBLE);
                if (!userName.equalsIgnoreCase("")) {
                    String[] split = userName.trim().split("\\s+");
                    if (split.length > 1) {
                        String first = split[0].trim().substring(0, 1);
                        String last = split[1].trim().substring(0, 1);
                        String initial = first + "" + last;
                        iconText.setText(initial.toUpperCase());
                    } else {
                        if (split[0] != null && !split[0].isEmpty()) {
                            String first = split[0].trim().substring(0, 1);
                            iconText.setText(first.toUpperCase());
                        }
                    }
                }
            } else {
                Uri uri = Uri.parse(profilePicture);
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                bottomProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getActivity().getResources())
                        .setRoundingParams(roundingParams)
                        .build());
                roundingParams.setRoundAsCircle(true);
                bottomProfile.setImageURI(uri);
                bottomProfile.setColorFilter(null);
                iconText.setVisibility(View.GONE);
            }
        }
        else
        {
            bottomProfile.setImageResource(R.drawable.bg_circle);
            bottomProfile.setColorFilter(R.color.guest);
            iconText.setVisibility(View.VISIBLE);
            if (!userName.equalsIgnoreCase("")){
                String[] split = userName.trim().split("\\s+");
                if (split.length > 1)
                {
                    String first = split[0].trim().substring(0,1);
                    String last = split[1].trim().substring(0,1);
                    String initial = first + ""+ last ;
                    iconText.setText(initial.toUpperCase());
                }
                else
                {
                    if (split[0] != null && !split[0].isEmpty()) {
                        String first = split[0].trim().substring(0,1);
                        iconText.setText(first.toUpperCase());
                    }
                }
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        pageLoad = "0";

        if (linearLayoutManager != null && state != null){
            linearLayoutManager.onRestoreInstanceState(state);

            rvFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if(!recyclerView.canScrollVertically(1))
                    {
                        int mPage = feedItems.size();
                        pageLoad = String.valueOf(mPage);
                        if (swipe != null && !swipe.isRefreshing() && !isBottomRefresh){
                            if(CheckNetworkConnection.isConnectionAvailable(getActivity()))
                            {
                                loadMoreFeeds();
                            }
                            else
                            {
                                SnackBarDialog.showNoInternetError(rootStatusBoxLayout);
                            }
                        }
                    }
                    if(rootStatusBoxLayout != null && rootStatusBoxLayout.getVisibility() != View.GONE || rootStatusBoxLayout.getVisibility() != View.INVISIBLE)
                    {
                        rootStatusBoxLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }


        updateProfilePicture();

        if(mHandler != null) {
            if(mHandlerTask != null){
                mHandler.removeCallbacks(mHandlerTask);
                mHandler.postDelayed(mHandlerTask, frequency);
            }

            new Handler().postDelayed(() -> {
                if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                    showRefreshingView();
                    loadFeed();
                } else {
                    if(rvFeed != null)
                        SnackBarDialog.showNoInternetError(rvFeed);
                }
            }, 1000 * 3);
        }


        if(mTracker != null){
            mTracker.setScreenName("Feeds Fragment Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

        if (getActivity() != null)
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, createIntentFilter());
        //UserOnlineStatus.setUserOnline(AppController.getInstance(),UserOnlineStatus.ONLINE);
    }

    private IntentFilter createIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SHOW_LOADING_ITEM);
        filter.addAction(ONLINE);
        return filter;
    }

    @Override
    public void onPause() {
        super.onPause();

        if(linearLayoutManager != null)
            state = linearLayoutManager.onSaveInstanceState();

        if(getActivity() != null)
           LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);

        if(mHandler != null) {
            if(mHandlerTask != null)
                mHandler.removeCallbacks(mHandlerTask);
        }

        //UserOnlineStatus.setUserOnline(AppController.getInstance(),UserOnlineStatus.OFFLINE);
    }

    @Override
    public void onDestroy() {
        if (loadNearestFriendsSubscription != null && !loadNearestFriendsSubscription.isUnsubscribed())
            loadNearestFriendsSubscription.unsubscribe();

        if (getFeedsSubscription != null && !getFeedsSubscription.isUnsubscribed())
            getFeedsSubscription.unsubscribe();

        super.onDestroy();
    }

    private void setupFeed() {
        if(getActivity() != null){
            feedAdapter = new FeedsAdapter(getActivity(), feedItems);
            feedAdapter.setOnFeedItemClickListener(this);
            rvFeed.setNestedScrollingEnabled(false);
            rvFeed.setHasFixedSize(true);
            rvFeed.setItemViewCacheSize(20);
          //  rvFeed.setDrawingCacheEnabled(true);
          //  rvFeed.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            rvFeed.setAdapter(feedAdapter);
            //feedAdapter.setHasStableIds(true);
        }
    }

    private void loadMoreFeeds() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        final String apiKey = UserUtils.getApiKey(getActivity());
        if(CheckNetworkConnection.isConnectionAvailable(getActivity())) {
            if(AppController.getInstance().getPrefManager().getUser() != null){
                final String userId = AppController.getInstance().getPrefManager().getUser().getId();

                final String friendId = "0";
                isLoadMore = true;
                showBottomRefreshingView();
                getFeeds(pageLoad, userId, friendId, apiKey);
                //new Handler(Looper.getMainLooper()).post(() -> getFeeds(pageLoad, userId, friendId, apiKey));

            } else {
                if(UserUtils.isGuestLoggedIn(getContext())){
                    getFeeds(pageLoad, "3", "0", apiKey);
                    //new Handler(Looper.getMainLooper()).post(() -> getFeeds(pageLoad, "3", "0", apiKey));
                }
            }
        } else {
            if(swipe != null)
                SnackBarDialog.showNoInternetError(swipe);
        }
    }

    private void saveOfflineUserFeeds() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if (feedItems != null && feedItems.size() > 0){
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            Gson gson = new Gson();
            String offlineFeeds = gson.toJson(feedItems);
            UserUtils.saveUserFeeds(AppController.getInstance(), offlineFeeds, userId);
        }
    }

    private void loadOfflineUserFeeds(){
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (AppController.getInstance().getPrefManager().getUser() != null){
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            final String offlineFeeds = UserUtils.getUserOldFeeds(getActivity(), userId);
            if(offlineFeeds != null && !offlineFeeds.equalsIgnoreCase("")){
                if(feedItems != null)
                    feedItems.clear();
                Gson gson = new Gson();
                Type type = new TypeToken<List<Feed.FeedItem>>(){}.getType();
                feedItems = gson.fromJson(offlineFeeds, type);
                if(feedAdapter != null && feedItems.size() > 0){
                    pageLoad = String.valueOf(feedItems.size());
                    new Handler().post(() -> feedAdapter.swap(feedItems));

                }
            }

        }
    }

    public void getFeeds(String pageLoad, String userId, String friendId, String apiKey){
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String companyId = "0";
        getFeedsSubscription = apiService.getFeeds(pageLoad, userId, friendId, companyId,apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Feed>() {
                    @Override
                    public void onCompleted() {
                        Log.d("Feeds", "onCompleted");
                        hideRefreshingView();
                        hideBottomRefreshingView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("getFeeds", "Failure");
                        if(e != null  && e.getMessage() != null && e.getMessage().equalsIgnoreCase("SSL handshake timed out"))
                            SnackBarDialog.show(rvFeed, "Please try again later.");
                        hideRefreshingView();
                        hideBottomRefreshingView();
                    }

                    @Override
                    public void onNext(final Feed mFeed) {
                        if( mFeed != null){
                            if(!mFeed.isErrorStatus()){
                                if(mFeed.getData()!= null){
                                    if (swipe.isRefreshing()) {
                                        feedItems.clear();
                                        feedItems.addAll(mFeed.getData());
                                  //      rvFeed.getRecycledViewPool().clear();
                                        try{
                                            feedAdapter.notifyDataSetChanged();
                                        }catch (IndexOutOfBoundsException ex)
                                        {
                                            ex.printStackTrace();
                                        }
                                    } else if (isLoadMore) {
                                        Thread thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                isLoadMore = false;
                                                feedItems.addAll(mFeed.getData());
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        hideRefreshingView();
                                                        hideBottomRefreshingView();
                                                        try{
                                                            feedAdapter.notifyItemInserted(feedItems.size() - 1);
                                                        }catch (IndexOutOfBoundsException ex)
                                                        {
                                                            ex.printStackTrace();
                                                            feedAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                        thread.start();

                                    } else {
                                        feedItems.clear();
                                        feedItems.addAll(mFeed.getData());
                                        try{
                                            feedAdapter.notifyDataSetChanged();
                                        }catch (IndexOutOfBoundsException ex)
                                        {
                                            ex.printStackTrace();
                                        }
                                    }
                                    //saveOfflineUserFeeds();
                                }
                                Log.d("getFeeds", "Success");
                            } else {
                                if(swipe != null){
                                    SnackBarDialog.show(swipe, mFeed.getStatus());
                                }
                                hideRefreshingView();
                                hideBottomRefreshingView();
                                Log.d("getFeeds", "failure");
                            }
                        }

                    }
                });
    }

    @Override
    public void onCommentsClick(View v, int position)
    {
        if(!swipe.isRefreshing()) {
            if (feedItems != null && feedItems.size() > 0)
            {
                Feed.FeedItem feedItem = feedItems.get(position);
                if (feedItem != null) {
                    if(feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost"))
                    {
                        goToNewsPage(feedItem.getPostId());
                    }
                    else
                    {
                        if (CheckNetworkConnection.isConnectionAvailable(getActivity()))
                            goToDetailPage(feedItem.getPostedUserId(), feedItem.getPostId());
                        else
                            SnackBarDialog.showNoInternetError(v);
                    }
                }
            }
        }
    }

    @Override
    public void onImageCenterClick(View v, int position) {
        if (!swipe.isRefreshing()) {
            if (feedItems != null && feedItems.size() > 0) {
                Feed.FeedItem feedItem = feedItems.get(position);
                if (feedItem != null) {
                    if(feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost")){
                        goToNewsPage(feedItem.getPostId());
                    } else {
                        if (CheckNetworkConnection.isConnectionAvailable(getActivity()))
                            goToDetailPage(feedItem.getPostedUserId(), feedItem.getPostId());
                        else
                            SnackBarDialog.showNoInternetError(v);
                    }
                }
            }
        }
    }

    @Override
    public void onEventClick(View v, int position) {
        if (!swipe.isRefreshing()) {
            if (feedItems != null && feedItems.size() > 0) {
                Feed.FeedItem feedItem = feedItems.get(position);
                if (feedItem != null) {
                    goToEventDetailActivity(feedItem.getEventId());
                }
            }
        }
    }

    private void goToEventDetailActivity(String eventId) {
        if (getActivity() != null) {
            Intent i = new Intent(getActivity(), EventDetailActivity.class);
            i.putExtra("eventId", eventId);
            getActivity().startActivity(i);
            if(CheckOsVersion.isPreLollipop()){
                if(getActivity() != null)
                    getActivity().overridePendingTransition(0, 0);
            }
        }

    }

    private void goToDetailPage(String postedUserId, String postId) {
        Intent intent = new Intent(getActivity(), DetailedPostActivity.class);
        intent.putExtra("postId", postId);
        intent.putExtra("userId", postedUserId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop()){
            if(getActivity() != null)
                getActivity().overridePendingTransition(0, 0);
        }
    }

    private void goToNewsPage(String postId) {
        Intent i = new Intent(getActivity(), NewsViewActivity.class);
        i.putExtra("newsId", postId);
        startActivity(i);
        if(CheckOsVersion.isPreLollipop()){
            if(getActivity() != null)
                getActivity().overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onHeartClick(View v, int position)
    {
        if(!swipe.isRefreshing())
        {
            if(feedItems != null && feedItems.size() > 0) {
                Feed.FeedItem feedItem = feedItems.get(position);
                int likeCount;
                if(feedItem != null){
                    if(feedItem.isLikeStatus()){
                        likeCount = feedItem.getLikeCount();
                        if(likeCount > 0){
                            feedItem.setLikeCount(likeCount-1);
                        }
                        feedItem.setLikeStatus(false);
                    } else {
                        likeCount = feedItem.getLikeCount();
                        feedItem.setLikeCount(likeCount+1);
                        feedItem.setLikeStatus(true);
                    }
                    feedItems.set(position, feedItem );
                    if(feedAdapter != null){
                        feedAdapter.notifyItemChanged(position);
                        //feedAdapter.notifyItemChanged(position, ACTION_LIKE_BUTTON_CLICKED);
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                        doLike(feedItem);
                    }

                }
            }
        }
    }

    @Override
    public void onLoadMore(int position) {
        Toast.makeText(getContext(), "onLoad more", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReport(View v, ViewGroup inActiveLayout, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                    showReportPopupMenu(v, inActiveLayout, feedItems.get(position), position);
                else
                    SnackBarDialog.showNoInternetError(v);
            }
        }
    }

    @Override
    public void onInActiveReport(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                    showInactiveReportPopupMenu(v, feedItems.get(position), position);
                else
                    SnackBarDialog.showNoInternetError(v);
            }
        }
    }

    private void doLike(Feed.FeedItem feedItem)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<LikedData> call = apiService.insertLike(userId, feedItem.getPostId(), feedItem.getPostedUserId(),apiKey);
        call.enqueue(new Callback<LikedData>() {
            @Override
            public void onResponse(@NonNull Call<LikedData> call, @NonNull retrofit2.Response<LikedData> response)
            {
                Log.d("doLike", "onSuccess");
                if(response.body() != null && response.isSuccessful()){
                    LikedData mLikedData = response.body();
                    if(mLikedData != null && !mLikedData.getError()){
                        LikedData.InsertLikeData  data = mLikedData.getInsertLikeData();
                        if(data != null) {
                            if(feedItems != null) {
                                int i = 0;
                                for(Feed.FeedItem  feedItem : feedItems){
                                    if(feedItem.getPostId().equalsIgnoreCase(data.getPostId())){
                                        feedItem.setLikeStatus(data.getLikeStatus());
                                        feedItem.setLikeCount(Integer.parseInt(UserUtils.parsingInteger(data.getLikeCount())));
                                        feedItems.set(i, feedItem);
                                        if(feedAdapter != null)
                                        {
                                            feedAdapter.notifyItemChanged(i);
                                        }
                                    }
                                    i++;
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<LikedData> call, @NonNull Throwable t) {
                Log.d("doLike", "onFailure");
            }
        });
    }

    @Override
    public void onMoreClick(View v, int itemPosition, boolean isEditShow) {
        if(!swipe.isRefreshing()){
            if(feedItems != null && feedItems.size() > 0){
                showPopupMenu(v,  feedItems.get(itemPosition), itemPosition, isEditShow);
            }
        }
    }

    private void showPopupMenu(View v, Feed.FeedItem feedItem, int itemPosition, boolean isEditShow) {

        if (getActivity() != null) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.over_flow, popup.getMenu());
            Menu popupMenu = popup.getMenu();
            if(!isEditShow)
                popupMenu.findItem(R.id.action_edit).setVisible(false);
            popup.setOnMenuItemClickListener(new MyMenuItemClickListener(feedItem, itemPosition));
            popup.show();
        }

    }

    private void showReportPopupMenu(final View v, final ViewGroup inActiveLayout, final Feed.FeedItem feedItem, final int itemPosition) {

        if (getActivity() != null) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.report, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.action_report){
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setMessage("Are you sure you want to report this post?");
                    dialogBuilder.setCancelable(true);
                    dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                doReport(feedItem.getPostId(), feedItem.getPostedUserId());
                            }
                        });

                        if(feedItems != null && feedItems.size() > 0 &&  feedAdapter != null){
                            Toast.makeText(getActivity(), "Post Reported", Toast.LENGTH_SHORT).show();
                            feedItem.setReported(true);
                            feedItem.setReportedUserId(AppController.getInstance().getPrefManager().getUser().getId());
                            feedAdapter.notifyItemChanged(itemPosition, feedItem);
                            feedAdapter.notifyDataSetChanged();
                        }
                    });

                    dialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                }
                return true;
            });
            popup.show();
        }

    }

    private void showInactiveReportPopupMenu(final View v, final Feed.FeedItem feedItem, final int itemPosition) {
        if (getActivity() != null) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            if(feedItem.getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                inflater.inflate(R.menu.un_report, popup.getMenu());
            } else {
                inflater.inflate(R.menu.report_contact_moderator, popup.getMenu());
            }

            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.action_un_report){
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setMessage("Are you sure you want to un report this post?");
                    dialogBuilder.setCancelable(true);
                    dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                undoReport(feedItem.getReportId());
                            }
                        });

                        if(feedItems != null && feedItems.size() > 0 &&  feedAdapter != null){
                            Toast.makeText(getActivity(), "Post Un Reported", Toast.LENGTH_SHORT).show();
                            feedItem.setReported(false);
                            feedItem.setReportedUserId(AppController.getInstance().getPrefManager().getUser().getId());
                            feedAdapter.notifyItemChanged(itemPosition, feedItem);
                            feedAdapter.notifyDataSetChanged();
                        }
                    });

                    dialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                } else if(item.getItemId() == R.id.action_report_contact_moderator){
                    String moderatorEmailAddress = "support@myscrap.com";
                    Intent emailApp = new Intent(Intent.ACTION_SEND);
                    emailApp.putExtra(Intent.EXTRA_EMAIL, new String[]{moderatorEmailAddress});
                    emailApp.putExtra(Intent.EXTRA_SUBJECT, "Your post is hidden, Please contact moderator.");
                    emailApp.putExtra(Intent.EXTRA_TEXT, "");
                    emailApp.setType("message/rfc822");
                    startActivity(Intent.createChooser(emailApp, "Send Email Via"));
                }
                return true;
            });
            popup.show();
        }
    }

    private static void disable(ViewGroup layout) {
        layout.setEnabled(false);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                disable((ViewGroup) child);
            } else {
                child.setEnabled(false);
            }
        }
    }

    public void setClickable(View view) {
        if (view != null) {
            view.setClickable(false);
            if (view instanceof ViewGroup) {
                ViewGroup vg = ((ViewGroup) view);
                for (int i = 0; i < vg.getChildCount(); i++) {
                    setClickable(vg.getChildAt(i));
                }
            }
        }
    }

    private void doReport(String postId, String postedUserId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Report> call = apiService.reportPost(userId, postId, postedUserId, "", apiKey);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull retrofit2.Response<Report> response) {
                if(response.body() != null && response.isSuccessful()){
                    Report mReportPost = response.body();
                    if(mReportPost != null && !mReportPost.isErrorStatus()){
                        Log.d("doReport", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                Log.d("doReport", "onFailure");
            }
        });
    }

    private void undoReport(String reportId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        Call<Report> call = apiService.undoReportPost(reportId, apiKey);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull retrofit2.Response<Report> response) {
                if(response.body() != null && response.isSuccessful()){
                    Report mReportPost = response.body();
                    if(mReportPost != null && !mReportPost.isErrorStatus()){
                        Log.d("undoReport", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                Log.d("undoReport", "onFailure");
            }
        });
    }

    private void showDeletePostDialog(Context context, final String id, final String postId, final String albumID, final int itemPosition){
        if (getActivity() == null)
            return;

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage("Are you sure you want to delete this post?");
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("DELETE", (dialog, which) -> {
            feedItems.remove(itemPosition);
            feedAdapter.notifyDataSetChanged();
            new Handler().post(() -> deletingPost(id, postId, albumID));

        });

        dialogBuilder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void deletingPost(String id, String postId, String albumID) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<DeletePost> call = apiService.deletePost(userId, postId, albumID,apiKey);
        call.enqueue(new Callback<DeletePost>() {
            @Override
            public void onResponse(@NonNull Call<DeletePost> call, @NonNull retrofit2.Response<DeletePost> response) {

                if(response.body() != null && response.isSuccessful()){
                    DeletePost mDeletePost = response.body();
                    if(mDeletePost != null && !mDeletePost.isErrorStatus()){
                        Log.d("deletingPost", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<DeletePost> call, @NonNull Throwable t) {
                Log.d("deletingPost", "onFailure");
            }
        });
    }

    @Override
    public void onProfileClick(View v, int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(!swipe.isRefreshing()) {
            if (feedItems != null && feedItems.size() > 0) {
                Feed.FeedItem feedItem = feedItems.get(position);
                if (feedItem != null) {
                    if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost") && feedItem.getCompanyName() != null &&  !feedItem.getCompanyName().equalsIgnoreCase("") && feedItem.getCompanyId() != null &&  !feedItem.getCompanyId().equalsIgnoreCase("")){
                        if(feedItem.getCompanyId() != null && !feedItem.getCompanyId().equalsIgnoreCase(""))
                            goToCompany(feedItem.getCompanyId());
                    } else {
                        if (feedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            goToUserProfile();
                        } else {
                            goToUserFriendProfile(feedItem.getPostedUserId());
                        }
                    }
                }


            }
        }
    }

    @Override
    public void onTagClick(View v, String taggedId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (!swipe.isRefreshing()){
            if(taggedId != null && !taggedId.equalsIgnoreCase("")){
                if (taggedId.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(taggedId);
                }
            }
        }
    }

    @Override
    public void onPostFromClick(View v, int position) {

    }

    @Override
    public void onPostToClick(View v, int position) {

    }

    @Override
    public void onFavouriteClick(View v, int position) {
        if (!swipe.isRefreshing()) {
            if (feedItems != null && feedItems.size() > 0) {
                Feed.FeedItem feedItem = feedItems.get(position);
                if (feedItem != null) {
                    if (feedItem.isPostFavourited()) {
                        feedItem.setPostFavourited(false);
                        Toast.makeText(AppController.getInstance(), "Removed from favourite posts", Toast.LENGTH_SHORT).show();
                    } else {
                        feedItem.setPostFavourited(true);
                        Toast.makeText(AppController.getInstance(), "Added to favourite posts", Toast.LENGTH_SHORT).show();
                    }
                    feedItems.set(position, feedItem);
                    if (feedAdapter != null) {
                        feedAdapter.notifyItemChanged(position);
                    }
                    if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())) {
                        new Handler().post(() -> doFavourite(feedItem, position));

                    } else {
                        SnackBarDialog.showNoInternetError(v);
                    }

                }
            }
        }
    }


    private void doFavourite(Feed.FeedItem feedItem, int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Favourite> call = apiService.insertFavourite(userId, feedItem.getPostId(), feedItem.getPostedUserId(),apiKey);
        call.enqueue(new Callback<Favourite>() {
            @Override
            public void onResponse(@NonNull Call<Favourite> call, @NonNull retrofit2.Response<Favourite> response) {
                Log.d("doFavourite", "onSuccess");
            }
            @Override
            public void onFailure(@NonNull Call<Favourite> call, @NonNull Throwable t) {
                Log.d("doFavourite", "onFailure");
            }
        });
    }

    private void goToUserProfile() {
        Intent i = new Intent(getActivity(), UserProfileActivity.class);
        startActivity(i);
        if (getActivity() != null && CheckOsVersion.isPreLollipop()) {
            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToUserFriendProfile(String postedUserId) {
        final Intent intent = new Intent(getActivity(), UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(getActivity() != null)
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void goToCompany(String companyId) {
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            Intent i = new Intent(getActivity(), CompanyProfileActivity.class);
            i.putExtra("companyId", companyId);
            startActivity(i);
        } else {
            if(swipe != null)
                SnackBarDialog.showNoInternetError(swipe);
        }
    }

    @Override
    public void onReportClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onSharePhotoClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCopyShareUrlClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCancelClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    private void goToStatusActivity(String click) {
        Intent intent = new Intent(AppController.getInstance(), StatusActivity.class);
        intent.putExtra("page", "feeds");
        intent.putExtra("click", click);
        startActivity(intent);
        if(getActivity() != null && CheckOsVersion.isPreLollipop())
            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private Feed.FeedItem feedItem;
        private int itemPosition;
        public MyMenuItemClickListener(Feed.FeedItem mFeedItem, int position) {
            this.feedItem = mFeedItem;
            this.itemPosition = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    if (getActivity() != null){
                        Intent i = new Intent(getActivity(), StatusActivity.class);
                        i.putExtra("page", "feeds");
                        i.putExtra("editPost", feedItem.getStatus());
                        Gson gson = new Gson();
                        String userData = gson.toJson(feedItem);
                        i.putExtra("tagData", userData);
                        i.putExtra("postId", ""+feedItem.getPostId());
                        getActivity().startActivity(i);
                        if (CheckOsVersion.isPreLollipop())
                            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                    return true;
                case R.id.action_delete:
                    if (feedItem != null ) {
                        if(feedItems != null && feedItems.size() > 0){
                            AppController.runOnUIThread(() -> showDeletePostDialog(getActivity(), feedItem.getPostedUserId(), feedItem.getPostId(), feedItem.getAlbumId(), itemPosition));

                        }

                    }
                    return true;
                default:
            }
            return false;
        }
    }
}
