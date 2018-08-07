package com.myscrap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.myscrap.activity.XMPPChatRoomActivity;
import com.myscrap.adapters.FeedItemAnimator;
import com.myscrap.adapters.FeedsAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.DeletePost;
import com.myscrap.model.Favourite;
import com.myscrap.model.Feed;
import com.myscrap.model.LikedData;
import com.myscrap.model.PictureUrl;
import com.myscrap.model.Report;
import com.myscrap.model.UserFriendProfile;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.EndlessParentScrollListener;
import com.myscrap.view.PreCachingLayoutManager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.xmppresources.ConnectionClass;
import com.myscrap.xmppresources.Constant;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserFriendProfileActivity extends AppCompatActivity implements FeedsAdapter.OnFeedItemClickListener{
    private UserFriendProfileActivity mUserProfileActivity;
    private RecyclerView recyclerViewFeeds;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FeedsAdapter feedAdapter;
    private List<Feed.FeedItem> feedItems = new ArrayList<>();
    private String notId = "0";
    private String friendId = "0";
    private String friendName = "";
    private SimpleDraweeView profilePhoto;
    private TextView profileName;
    private TextView profilePhotoText;
    private TextView designation;
    private TextView companyName;

    private LinearLayout mChat;
    private LinearLayout basicLayout;
    //private LinearLayout chatLayout;
    //private RelativeLayout mChatLayout;
    private FloatingActionButton addToContacts, removeFromContacts;
    private UserFriendProfile mUserFriendProfile;
    private RelativeLayout interestLayout;
    private int layoutCount = 0;
    private ImageButton favourite;
    private ImageButton chat;
    private TextView online;

   // private RelativeLayout statusLayout;
   // private RelativeLayout cameraLayout;


    private TextView interestTextView;
    private TextView aboutTextView;
    private TextView photoTextView;

    private LinearLayout rootEmptyFeedsLayout;
    private LinearLayout rootStatusBoxLayout;
    private LinearLayout rootStatusLayout;
    private SimpleDraweeView bottomProfile;
    private TextView bottomStatusTextView;

    private TextView rank;
    private TextView points;
    private String TAG = "UserFriend";
    private String pageLoad = "0";
    private boolean isLoadMore;
    private TextView iconBottomText;
    private Tracker mTracker;
    private SimpleDraweeView userProfileMod;




    public String friendsJID;
    public String friendUserId;
    public String friendsName;
    public String friendsUrl;
    public String friendsCompany;
    public String friendsPosition;
    public String friendsLocation;
    public String friendsColor;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_friend_profile);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mTracker = AppController.getInstance().getDefaultTracker();

        NestedScrollView mNestedScrollView = (NestedScrollView) findViewById(R.id.nested_scroll_view);

        basicLayout = (LinearLayout) findViewById(R.id.basic_layout);
        mUserProfileActivity = this;
        online = (TextView) findViewById(R.id.online);
        points = (TextView) findViewById(R.id.points);
        rank = (TextView) findViewById(R.id.rank);
        FloatingActionMenu menuLabelsRight = (FloatingActionMenu) findViewById(R.id.menu_labels_right);
        addToContacts = (FloatingActionButton) menuLabelsRight.findViewById(R.id.add_contacts);
        removeFromContacts = (FloatingActionButton) menuLabelsRight.findViewById(R.id.remove_contacts);
        profilePhoto = (SimpleDraweeView) findViewById(R.id.userProfile);
        userProfileMod = (SimpleDraweeView) findViewById(R.id.userProfileMod);
        ImageView userProfileOnline = (ImageView) findViewById(R.id.userProfileOnline);
        profilePhotoText = (TextView) findViewById(R.id.icon_text);
        profileName = (TextView) findViewById(R.id.name);
        designation = (TextView) findViewById(R.id.designation);
        companyName = (TextView) findViewById(R.id.company);
        rootEmptyFeedsLayout = (LinearLayout) findViewById(R.id.empty_feeds);
        rootStatusLayout = (LinearLayout) findViewById(R.id.root_status_layout);

        LinearLayout interestLayoutChild = (LinearLayout) findViewById(R.id.interestLayout);

        interestTextView = (TextView) findViewById(R.id.interestTextView);
        aboutTextView = (TextView) findViewById(R.id.aboutTextView);
        photoTextView = (TextView) findViewById(R.id.photoTextView);
        favourite = (ImageButton) findViewById(R.id.btnContacts);
        mChat = (LinearLayout) findViewById(R.id.btnChatLayout);
        chat = (ImageButton) findViewById(R.id.btnChat);
        bottomProfile = (SimpleDraweeView) findViewById(R.id.user_profile);
        iconBottomText = (TextView) findViewById(R.id.icon_bottom_text);
        ImageView bottomCamera = (ImageView) findViewById(R.id.camera);
        bottomStatusTextView = (TextView) findViewById(R.id.status);
        rootStatusBoxLayout = (LinearLayout) findViewById(R.id.root_status_box_layout);
        RelativeLayout bottomLayout = (RelativeLayout) findViewById(R.id.status_layout);




  //      resetRequiredUserUtils();







        bottomLayout.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(mUserProfileActivity))
            {
                GuestLoginDialog.show(mUserProfileActivity);
                return;
            }
            goToStatusActivity("");
        });
        bottomCamera.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(mUserProfileActivity))
            {
                GuestLoginDialog.show(mUserProfileActivity);
                return;
            }
            goToStatusActivity("camera");
        });
        bottomStatusTextView.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                GuestLoginDialog.show(mUserProfileActivity);
                return;
            }
            goToStatusActivity("");
        });
        bottomProfile.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                GuestLoginDialog.show(mUserProfileActivity);
                return;
            }
            goToUserProfile();
        });
        recyclerViewFeeds = (RecyclerView) findViewById(R.id.recyclerViewFeeds);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            pageLoad = "0";
            mSwipeRefreshLayout.setRefreshing(true);
            loadUserProfile();
        });
        PreCachingLayoutManager linearLayoutManager = new PreCachingLayoutManager(mUserProfileActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(mUserProfileActivity));
        recyclerViewFeeds.setLayoutManager(linearLayoutManager);
        recyclerViewFeeds.setNestedScrollingEnabled(false);


        mNestedScrollView.setOnScrollChangeListener(new EndlessParentScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d("onLoadMore: ", "" + totalItemsCount);
                if(totalItemsCount < 10){
                    totalItemsCount = 0;
                }
                pageLoad = String.valueOf(totalItemsCount);
                if(rootStatusLayout != null)
                    rootStatusLayout.setVisibility(View.VISIBLE);
                if(rootStatusBoxLayout != null)
                    rootStatusBoxLayout.setVisibility(View.GONE);
                isLoadMore = true;
                loadUserProfile();
            }

            @Override
            public void onScrollUp() {
                if(rootStatusBoxLayout != null)
                    rootStatusBoxLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScrollDown() {
                if(rootStatusBoxLayout != null)
                    rootStatusBoxLayout.setVisibility(View.GONE);
            }

            @Override
            public void onScrollTopReached() {
                if(rootStatusBoxLayout != null)
                    rootStatusBoxLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScrollBottomReached() {
                if(rootStatusBoxLayout != null)
                    rootStatusBoxLayout.setVisibility(View.GONE);
            }
        });

        setupFeed();
        updateProfilePicture();




    }

    private void resetRequiredUserUtils()
    {
        UserUtils.saveFriendsJID(UserFriendProfileActivity.this,"");
        UserUtils.saveUserFriendsName(UserFriendProfileActivity.this,"");
        UserUtils.saveFriendsPicture(UserFriendProfileActivity.this,"");
        UserUtils.saveFriendsCompany(UserFriendProfileActivity.this,"");
        UserUtils.saveFriendsPosition(UserFriendProfileActivity.this, "");
        UserUtils.saveFriendsLocation(UserFriendProfileActivity.this,"");
        UserUtils.saveFriendsColor(UserFriendProfileActivity.this, "");
    }





    private void updateProfilePicture()
    {
        String profilePicture = UserUtils.getUserProfilePicture(mUserProfileActivity);
        String firstName = UserUtils.getFirstName(mUserProfileActivity);
        String lastName = UserUtils.getLastName(mUserProfileActivity);
        String userName = firstName + " " + lastName;
        if(!profilePicture.equalsIgnoreCase(""))
        {
            if (profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                    || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")) {
                bottomProfile.setImageResource(R.drawable.bg_circle);
                bottomProfile.setColorFilter(R.color.guest);
                iconBottomText.setVisibility(View.VISIBLE);
                if (!userName.equalsIgnoreCase("")) {
                    String[] split = userName.trim().split("\\s+");
                    if (split.length > 1) {
                        String first = split[0].trim().substring(0, 1);
                        String last = split[1].trim().substring(0, 1);
                        String initial = first + "" + last;
                        iconBottomText.setText(initial.toUpperCase());
                    } else {
                        if (split[0] != null && split[0].trim().length() >= 1) {
                            String first = split[0].trim().substring(0, 1);
                            iconBottomText.setText(first.toUpperCase());
                        }
                    }
                }
            }
            else
            {
                Uri uriB = Uri.parse(profilePicture);
                RoundingParams roundingParamsB = RoundingParams.fromCornersRadius(30f);
                bottomProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                        .setRoundingParams(roundingParamsB)
                        .build());
                roundingParamsB.setRoundAsCircle(true);
                bottomProfile.setImageURI(uriB);
                bottomProfile.setColorFilter(null);
                iconBottomText.setVisibility(View.GONE);
            }
        }
        else
        {
            bottomProfile.setImageResource(R.drawable.bg_circle);
            bottomProfile.setColorFilter(R.color.guest);
            iconBottomText.setVisibility(View.VISIBLE);
            if (!userName.equalsIgnoreCase("")){
                String[] split = userName.trim().split("\\s+");
                if (split.length > 1){
                    String first = split[0].trim().substring(0,1);
                    String last = split[1].trim().substring(0,1);
                    String initial = first + " "+ last ;
                    iconBottomText.setText(initial.toUpperCase());
                } else {
                    if (split[0] != null && split[0].trim().length() >= 1) {
                        String first = split[0].trim().substring(0, 1);
                        iconBottomText.setText(first.toUpperCase());
                    }
                }
            }
        }
    }

    private void goToStatusActivity(String click)
    {
        Intent intent = new Intent(AppController.getInstance(), StatusActivity.class);
        intent.putExtra("page", "userFriendProfile");
        intent.putExtra("friendId", friendId);
        intent.putExtra("friendName", friendName);
        intent.putExtra("click", click);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            mUserProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }


    // this is the method you were looking for
    private void goToChat(String id, String from, String chatRoomProfilePic, String online, String color)
    {
        Intent intent = new Intent(AppController.getInstance(), ChatRoomActivity.class);
        intent.putExtra("chatRoomId", id);
        intent.putExtra("chatRoomName", from);
        intent.putExtra("color", color);
        intent.putExtra("chatRoomProfilePic", chatRoomProfilePic);
        if (online != null)
            intent.putExtra("online", "0");
        else
            intent.putExtra("online", "0");
        startActivity(intent);
    }

    private void setupFeed() {
        feedAdapter = new FeedsAdapter(this, feedItems);
        feedAdapter.setOnFeedItemClickListener(this);
        recyclerViewFeeds.setAdapter(feedAdapter);
        recyclerViewFeeds.setItemAnimator(new FeedItemAnimator());
    }

    private void loadUserProfile()
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(CheckNetworkConnection.isConnectionAvailable(mUserProfileActivity)){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String userView = "0";
            String[] strings = {userId, friendId, pageLoad, "", userView, apiKey};
    //        Log.d(Constant.LOGTAG,userId+"\n"+friendId+"\n"+pageLoad+"\n"+notId+"\n"+userView+"\n"+apiKey);
    //            new FriendsJIDTask().execute(strings);
             apiService.userFriendProfile(userId, friendId, pageLoad, notId, userView, apiKey)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<UserFriendProfile>()
            {
                @Override
                public void onCompleted()
                {

                }

                @Override
                public void onError(Throwable e)
                {
                    Log.d("loadUserProfile", "onFailure");
                    isLoadMore = false;
                    recyclerViewFeeds.setVisibility(View.GONE);
                    rootEmptyFeedsLayout.setVisibility(View.VISIBLE);
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                    if(rootStatusLayout != null)
                        rootStatusLayout.setVisibility(View.GONE);
                    if(rootStatusBoxLayout != null)
                        rootStatusBoxLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNext(UserFriendProfile userFriendProfile)
                {

                    Log.d("loadUserProfile", "onSuccess");
                    mUserFriendProfile = userFriendProfile;
                    if(mUserFriendProfile != null && !mUserFriendProfile.isErrorStatus())
                    {
                        if(mUserFriendProfile.getUserProfileData() != null && mUserFriendProfile.getUserProfileData().size() > 0){
                            UserFriendProfile.UserFriendProfileData data = mUserFriendProfile.getUserProfileData().get(0);
                            if(data != null)
                            {

                               friendUserId = data.getUserid();
                               friendsName = data.getName();
                               friendsJID = data.getjId();
                               friendsUrl = data.getProfilePic();
                               friendsCompany = data.getUserCompany();
                               friendsPosition = data.getPostedUserDesignation();
                               friendsLocation = data.getUserLocation();
                               friendsColor = data.getColorCode();



                             /*   UserUtils.saveUserFriendsName(UserFriendProfileActivity.this,data.getName());
                                UserUtils.saveFriendsPicture(UserFriendProfileActivity.this,data.getProfilePic());
                                UserUtils.saveFriendsCompany(UserFriendProfileActivity.this,data.getCompany());
                                UserUtils.saveFriendsPosition(UserFriendProfileActivity.this, data.getPostedUserDesignation());
                                UserUtils.saveFriendsLocation(UserFriendProfileActivity.this,data.getUserLocation());
                                UserUtils.saveFriendsColor(UserFriendProfileActivity.this, data.getColorCode());*/


                                initValues(data);
                                if(basicLayout != null)
                                    basicLayout.setVisibility(View.VISIBLE);
                                if(favourite != null)
                                    favourite.setVisibility(View.VISIBLE);
                                List<Feed.FeedItem> items = data.getData();
                                if(items != null){
                                    if(!isLoadMore || mSwipeRefreshLayout.isRefreshing()){
                                        feedItems.clear();
                                    } else {
                                        if(isLoadMore){
                                            if(feedItems.size() < 10)
                                                feedItems.clear();
                                        }
                                    }
                                    feedItems.addAll(items);
                                    if(feedItems.size() > 0){
                                        recyclerViewFeeds.setVisibility(View.VISIBLE);
                                        rootEmptyFeedsLayout.setVisibility(View.GONE);
                                    } else {
                                        recyclerViewFeeds.setVisibility(View.GONE);
                                        rootEmptyFeedsLayout.setVisibility(View.VISIBLE);
                                    }
                                    feedAdapter.notifyDataSetChanged();
                                } else {
                                    recyclerViewFeeds.setVisibility(View.GONE);
                                    rootEmptyFeedsLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                    if(rootStatusLayout != null)
                        rootStatusLayout.setVisibility(View.GONE);
                    if(rootStatusBoxLayout != null)
                        rootStatusBoxLayout.setVisibility(View.VISIBLE);
                    isLoadMore = false;
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                }
            })
            ;
        }
        else
        {
            if(mSwipeRefreshLayout != null)
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }

    }


    private void addToFavourites(String friendId)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(this);
            Call<JSONObject> call = apiService.addToContacts(userId,friendId, apiKey);
            call.enqueue(new Callback<JSONObject>() {
                @Override
                public void onResponse(@NonNull Call<JSONObject> call, @NonNull Response<JSONObject> response) {
                    if(mUserFriendProfile != null) {
                        UserFriendProfile.UserFriendProfileData data = mUserFriendProfile.getUserProfileData().get(0);
                        if (data.getFriendstatus() != null && !data.getFriendstatus().equalsIgnoreCase("") && data.getFriendstatus().equalsIgnoreCase("3")){
                            addToContacts.setVisibility(View.VISIBLE);
                            removeFromContacts.setVisibility(View.GONE);
                        } else {
                            removeFromContacts.setVisibility(View.VISIBLE);
                            addToContacts.setVisibility(View.GONE);
                        }
                    }
                    Log.d("addToFavourites", "onSuccess");
                }
                @Override
                public void onFailure(@NonNull Call<JSONObject> call, @NonNull Throwable t) {
                    Log.d("addToFavourites", "onFailure");

                }
            });
        } else {
            if(mSwipeRefreshLayout != null)
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("User Friend Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        Intent intent = getIntent();
        if(intent != null){
            pageLoad = "0";
            friendId = intent.getStringExtra("friendId");
            notId = intent.getStringExtra("notId");
            if(mSwipeRefreshLayout != null){
                mSwipeRefreshLayout.post(() -> {
                    loadUserProfile();
                    mSwipeRefreshLayout.setRefreshing(true);
                });
            }
        }
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.ONLINE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        } else if(item.getItemId() == R.id.action_report){
            if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                GuestLoginDialog.show(mUserProfileActivity);
                return false;
            }
            if(CheckNetworkConnection.isConnectionAvailable(mUserProfileActivity)) {
                doReport("", friendId);
                Toast.makeText(mUserProfileActivity, "Profile Reported", Toast.LENGTH_SHORT).show();
                this.finish();
            } else{
                if(mSwipeRefreshLayout != null)
                    SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void initValues(final UserFriendProfile.UserFriendProfileData data) {
        if(data != null){
            if(data.getProfilePic() != null && !data.getProfilePic().equalsIgnoreCase("")){
                if(mUserProfileActivity != null){
                    String userProfile = UserUtils.getUserProfilePicture(mUserProfileActivity);
                    if(userProfile != null && !userProfile.equalsIgnoreCase("")){
                        Uri uriB = Uri.parse(userProfile);
                        RoundingParams roundingParamsB = RoundingParams.fromCornersRadius(30f);
                        bottomProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                                .setRoundingParams(roundingParamsB)
                                .build());
                        roundingParamsB.setRoundAsCircle(true);
                        bottomProfile.setImageURI(uriB);
                    }
                }

                bottomProfile.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                        GuestLoginDialog.show(mUserProfileActivity);
                        return;
                    }
                    goToUserProfile();
                });
            }

            if(data.getModerator() == 1){
                userProfileMod.setVisibility(View.GONE);
                rank.setText(R.string.mod);
                rank.setBackground(ContextCompat.getDrawable(mUserProfileActivity, R.drawable.top_mod));
                rank.setVisibility(View.VISIBLE);
            } else {
                userProfileMod.setVisibility(View.GONE);
                if(data.getRank() >= 1 && data.getRank() <= 10){
                    rank.setText("TOP "+data.getRank());
                    rank.setBackground(ContextCompat.getDrawable(mUserProfileActivity, R.drawable.top));
                    rank.setVisibility(View.VISIBLE);
                } else {
                    if(data.isNewJoined()){
                        rank.setText(R.string.new_user);
                        rank.setBackground(ContextCompat.getDrawable(mUserProfileActivity, R.drawable.top_red));
                        rank.setVisibility(View.VISIBLE);
                    } else {
                        rank.setVisibility(View.GONE);
                        rank.setBackground(null);
                    }
                }
            }

            if(data.getName() != null && !data.getName().equalsIgnoreCase("")){
                profileName.setText(data.getName());
                friendName = data.getName();
                bottomStatusTextView.setText("Write something to " + data.getName());
                String[] split = data.getName().trim().split("\\s+");
                if (split.length > 1){
                    String first = split[0].trim().substring(0,1);
                    String last = split[1].trim().substring(0,1);
                    String initial = first + ""+ last ;
                    profilePhotoText.setText(initial.toUpperCase());
                } else {
                    if(split[0] != null && split[0].trim().length() >= 1) {
                        String first = split[0].trim().substring(0, 1);
                        profilePhotoText.setText(first.toUpperCase());
                    }
                }
            }


            if(data.isOnline()){
                online.setVisibility(View.VISIBLE);
            } else {
                online.setVisibility(View.GONE);
            }



            points.setText(data.getPoints() + " Score");
            if(data.getProfilePic() != null && !data.getProfilePic().equalsIgnoreCase("")) {
                if(data.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || data.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    profilePhoto.setImageResource(R.drawable.bg_circle);
                    if(data.getColorCode() != null && !data.getColorCode().equalsIgnoreCase("") && data.getColorCode().startsWith("#")){
                        profilePhoto.setColorFilter(Color.parseColor(data.getColorCode()));
                    } else {
                        profilePhoto.setColorFilter(DeviceUtils.getRandomMaterialColor(mUserProfileActivity, "400"));
                    }
                    profilePhotoText.setVisibility(View.VISIBLE);
                } else {
                    Uri uri = Uri.parse(data.getProfilePic());
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    profilePhoto.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    profilePhoto.setImageURI(uri);
                    profilePhoto.setColorFilter(null);
                    profilePhotoText.setVisibility(View.GONE);
                    /*profilePhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString("images", data.getProfilePic());
                            FragmentTransaction ft = mUserProfileActivity.getSupportFragmentManager().beginTransaction();
                            FeedImagesSlideshowDialogFragment newFragment = FeedImagesSlideshowDialogFragment.newInstance();
                            newFragment.setArguments(bundle);
                            newFragment.show(ft, "slideshow");
                        }
                    });*/
                }
            } else {
                profilePhoto.setImageResource(R.drawable.bg_circle);
                profilePhoto.setColorFilter(DeviceUtils.getRandomMaterialColor(mUserProfileActivity, "400"));
                profilePhotoText.setVisibility(View.VISIBLE);
            }

            if(data.getTagList() != null && data.getTagList().size() > 0){
                final UserFriendProfile.UserFriendProfileData.CompanyTagList tagData = data.getTagList().get(0);
                if(tagData != null){
                    if(tagData.getCompany() != null && !tagData.getCompany().equalsIgnoreCase("")){
                        companyName.setText(tagData.getCompany());
                        companyName.setTextColor(ContextCompat.getColor(mUserProfileActivity, R.color.colorPrimary));
                        companyName.setVisibility(View.VISIBLE);
                        companyName.setOnClickListener(v -> {
                            if(tagData.getCompanyId() != 0)
                                goToCompany(String.valueOf(tagData.getCompanyId()));
                        });
                    } else {
                        companyName.setVisibility(View.GONE);
                    }
                }
            }


            if(data.getName() != null && !data.getName().equalsIgnoreCase("") && data.getUserid() != null && !data.getUserid().equalsIgnoreCase("")){

                if(mChat != null){
                    mChat.setVisibility(View.VISIBLE);

                    mChat.setOnClickListener(v ->
                    {
                        if(UserUtils.isGuestLoggedIn(mUserProfileActivity))
                        {
                            GuestLoginDialog.show(mUserProfileActivity);
                            return;
                        }


                        startChatRoom();
         //             goToChat(data.getUserid(), data.getName(), data.getProfilePic(), "", data.getColorCode());


                    });
                }


                chat.setOnClickListener(v ->
                {
                    if(UserUtils.isGuestLoggedIn(mUserProfileActivity))
                    {
                        GuestLoginDialog.show(mUserProfileActivity);
                        return;
                    }



                    startChatRoom();
  //                goToChat(data.getUserid(), data.getName(), data.getProfilePic(), "", data.getColorCode());


                });
                if(favourite != null) {
                    if(data.getFriendstatus() != null && !data.getFriendstatus().equalsIgnoreCase("") && data.getFriendstatus().equalsIgnoreCase("3")){
                        favourite.setColorFilter(ContextCompat.getColor(mUserProfileActivity,R.color.colorPrimaryDark));
                        favourite.setImageDrawable(ContextCompat.getDrawable(mUserProfileActivity, R.drawable.ic_star_black_24dp));
                        favourite.setTag("favourite");
                    } else {
                        favourite.setColorFilter(null);
                        favourite.setTag("favourited");
                        favourite.setImageDrawable(ContextCompat.getDrawable(mUserProfileActivity, R.drawable.ic_star_border_black_24dp));
                    }
                    favourite.setOnClickListener(v -> {
                        if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                            GuestLoginDialog.show(mUserProfileActivity);
                            return;
                        }
                        if(favourite.getTag().equals("favourite")){
                            favourite.setTag("favourited");
                            favourite.setColorFilter(null);
                            favourite.setImageDrawable(ContextCompat.getDrawable(mUserProfileActivity, R.drawable.ic_star_border_black_24dp));
                            Toast.makeText(mUserProfileActivity, "Removed from favourites", Toast.LENGTH_SHORT).show();
                        } else {
                            favourite.setTag("favourite");
                            favourite.setImageDrawable(ContextCompat.getDrawable(mUserProfileActivity, R.drawable.ic_star_black_24dp));
                            favourite.setColorFilter(ContextCompat.getColor(mUserProfileActivity,R.color.colorPrimaryDark));
                            Toast.makeText(mUserProfileActivity, "Added to favourites", Toast.LENGTH_SHORT).show();
                        }
                        addToFavourites(data.getUserid());
                    });

                }
            }

            interestTextView.setOnClickListener(v -> {
                if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                    GuestLoginDialog.show(mUserProfileActivity);
                    return;
                }
                goToInterest(data, "interest");
            });

            aboutTextView.setOnClickListener(v -> {
                if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                    GuestLoginDialog.show(mUserProfileActivity);
                    return;
                }
                goToInterest(data, "about");
            });

            photoTextView.setOnClickListener(v -> {
                if(mUserProfileActivity != null && UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                    GuestLoginDialog.show(mUserProfileActivity);
                    return;
                }
                goToPhotos(mUserFriendProfile);
            });



            if(data.getPostedUserDesignation() != null && !data.getPostedUserDesignation().equalsIgnoreCase("")){
                designation.setText(data.getPostedUserDesignation());
            } else {
                designation.setText("TRADER");
            }
        }
    }




    private void startChatRoom()
    {

        if (friendsJID != null)
        {
            Intent intent = new Intent(UserFriendProfileActivity.this, XMPPChatRoomActivity.class);

            UserUtils.saveUserFriendsName(UserFriendProfileActivity.this,friendsName);
            UserUtils.saveFriendsID(UserFriendProfileActivity.this,friendUserId);
            UserUtils.saveFriendsPicture(UserFriendProfileActivity.this,friendsUrl);
            UserUtils.saveFriendsCompany(UserFriendProfileActivity.this,friendsCompany);
            UserUtils.saveFriendsPosition(UserFriendProfileActivity.this,friendsPosition);
            UserUtils.saveFriendsLocation(UserFriendProfileActivity.this,friendsLocation);
            UserUtils.saveFriendsColor(UserFriendProfileActivity.this,friendsColor);

            intent.putExtra(Constant.FRIENDS_JID, friendsJID);
            intent.putExtra(Constant.FRIENDS_ID, friendUserId);
            intent.putExtra(Constant.FRIENDS_NAME, friendsName);
            intent.putExtra(Constant.FRIENDS_URL, friendsUrl);
            intent.putExtra(Constant.FRIENDS_COLOR, friendsColor);
            startActivity(intent);
        }


    }


    private void goToCompany(String companyId) {
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            Intent i = new Intent(mUserProfileActivity, CompanyProfileActivity.class);
            i.putExtra("companyId", companyId);
            startActivity(i);
        } else {
            if(aboutTextView != null){
                SnackBarDialog.showNoInternetError(aboutTextView);
            }
        }
    }

    private void goToInterest(UserFriendProfile.UserFriendProfileData data, String pageName) {
        Intent intent = new Intent(AppController.getInstance(), InterestActivity.class);
        Gson gson = new Gson();
        String userData = gson.toJson(data);
        intent.putExtra("userData", userData);
        intent.putExtra("pageName", pageName);
        startActivity(intent);
    }

    private void goToPhotos(UserFriendProfile data) {
        Intent intent = new Intent(AppController.getInstance(), PhotosActivity.class);
        Gson gson = new Gson();
        String userData = gson.toJson(data);
        intent.putExtra("userData", userData);
        intent.putExtra("pageName", "photo");
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.OFFLINE);
    }



    @Override
    public void onCommentsClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0){
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(CheckNetworkConnection.isConnectionAvailable(mUserProfileActivity))
                    goToDetailPage(feedItem.getPostedUserId(), feedItem.getPostId());
                else
                    SnackBarDialog.showNoInternetError(v);
            }
        }
    }


    private void goToDetailPage(String postedUserId, String postId) {
        Intent intent = new Intent(mUserProfileActivity, DetailedPostActivity.class);
        intent.putExtra("postId", postId);
        intent.putExtra("userId", postedUserId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop()){
            if(mUserProfileActivity != null)
                mUserProfileActivity.overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onImageCenterClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(CheckNetworkConnection.isConnectionAvailable(mUserProfileActivity))
                goToDetailPage(feedItem.getPostedUserId(), feedItem.getPostId());
            else
                SnackBarDialog.showNoInternetError(v);
        }
    }

    @Override
    public void onEventClick(View v, int position) {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            if (feedItems != null && feedItems.size() > 0) {
                Feed.FeedItem feedItem = feedItems.get(position);
                if (feedItem != null) {
                    goToEventDetailActivity(feedItem.getEventId());
                }
            }
        }
    }

    private void goToEventDetailActivity(String eventId) {
        Intent i = new Intent(this, EventDetailActivity.class);
        i.putExtra("eventId", eventId);
        startActivity(i);
        if(CheckOsVersion.isPreLollipop())
            if(mUserProfileActivity != null)
                mUserProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

    }

    @Override
    public void onHeartClick(View v, int position) {
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
                }
                if (mUserFriendProfile.getPictureUrl() != null) {
                    int i = 0;
                    for (PictureUrl pictureUrl : mUserFriendProfile.getPictureUrl()){
                        if (pictureUrl.getPostid() != null && feedItem.getPostId() != null) {
                            if (pictureUrl.getPostid().equalsIgnoreCase(feedItem.getPostId())) {
                                mUserFriendProfile.getPictureUrl().get(i).setLikeStatus(feedItem.isLikeStatus());
                                mUserFriendProfile.getPictureUrl().get(i).setLikeCount(feedItem.getLikeCount());
                            }
                        }
                        i++;
                    }
                }
                if(CheckNetworkConnection.isConnectionAvailable(mUserProfileActivity))
                    doLike(feedItem);
            }
        }
    }

    @Override
    public void onLoadMore(int position) {

    }

    @Override
    public void onReport(View v, ViewGroup inActiveLayout, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                showReportPopupMenu(v,  feedItems.get(position), position);
            }
        }
    }

    @Override
    public void onInActiveReport(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(CheckNetworkConnection.isConnectionAvailable(mUserProfileActivity))
                    showInactiveReportPopupMenu(v, feedItems.get(position), position);
                else
                    SnackBarDialog.showNoInternetError(v);
            }
        }
    }

    private void showInactiveReportPopupMenu(final View v, final Feed.FeedItem feedItem, final int itemPosition) {
        PopupMenu popup = new PopupMenu(mUserProfileActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        if(feedItem.getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
            inflater.inflate(R.menu.un_report, popup.getMenu());
        } else {
            inflater.inflate(R.menu.report_contact_moderator, popup.getMenu());
        }

        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.action_un_report){
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mUserProfileActivity);
                dialogBuilder.setMessage("Are you sure you want to un report this post?");
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                    undoReport(feedItem.getReportId());
                    if(feedItems != null && feedItems.size() > 0 &&  feedAdapter != null){
                        Toast.makeText(mUserProfileActivity, "Post Un Reported", Toast.LENGTH_SHORT).show();
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

    private void showReportPopupMenu(View v, final Feed.FeedItem feedItem, final int itemPosition) {
        PopupMenu popup = new PopupMenu(mUserProfileActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.report, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.action_report){
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mUserProfileActivity);
                dialogBuilder.setMessage("Are you sure you want to report this post?");
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                    doReport(feedItem.getPostId(), feedItem.getPostedUserId());
                    if(feedItems != null && feedItems.size() > 0 &&  feedAdapter != null){
                        Toast.makeText(mUserProfileActivity, "Post Reported", Toast.LENGTH_SHORT).show();
                        feedItems.remove(itemPosition);
                        feedAdapter.notifyItemRemoved(itemPosition);
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

    private void doLike(Feed.FeedItem feedItem) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<LikedData> call = apiService.insertLike(userId, feedItem.getPostId(), feedItem.getPostedUserId(),apiKey);
        call.enqueue(new Callback<LikedData>() {
            @Override
            public void onResponse(@NonNull Call<LikedData> call, @NonNull retrofit2.Response<LikedData> response) {
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
                                        if(feedAdapter != null){
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
    public void onMoreClick(View v, int position, boolean isEditShow) {
        if(feedItems != null && feedItems.size() > 0){
            showPopupMenu(v,  feedItems.get(position), position, isEditShow);
        }
    }

    private void showPopupMenu(View v, Feed.FeedItem feedItem, int itemPosition, boolean isEditShow) {
        PopupMenu popup = new PopupMenu(mUserProfileActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.over_flow, popup.getMenu());
        Menu popupMenu = popup.getMenu();
        if(!isEditShow)
            popupMenu.findItem(R.id.action_edit).setVisible(false);
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(feedItem, itemPosition));
        popup.show();
    }

    private void showDeletePostDialog(Context context, final String id, final String postId, final String albumID, final int itemPosition){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mUserProfileActivity);
        dialogBuilder.setMessage("Are you sure you want to delete this post?");
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("DELETE", (dialog, which) -> {
            if(feedItems != null && feedItems.size() > 0) {
                feedItems.remove(itemPosition);
                feedAdapter.notifyDataSetChanged();
            }
            deletingPost(id, postId, albumID);
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
                    Intent i = new Intent(mUserProfileActivity, StatusActivity.class);
                    i.putExtra("page", "userFriendProfile");
                    i.putExtra("editPost", feedItem.getStatus());
                    Gson gson = new Gson();
                    String userData = gson.toJson(feedItem);
                    i.putExtra("tagData", userData);
                    i.putExtra("postId", ""+feedItem.getPostId());
                    mUserProfileActivity.startActivity(i);
                    if (CheckOsVersion.isPreLollipop())
                        mUserProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    return true;
                case R.id.action_delete:
                    if (feedItem != null ) {
                        if(feedItems != null && feedItems.size() > 0){
                            showDeletePostDialog(mUserProfileActivity, feedItem.getPostedUserId(), feedItem.getPostId(), feedItem.getAlbumId(), itemPosition);
                        }

                    }
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public void onProfileClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null && friendId != null && !friendId.equalsIgnoreCase("")){
                if(!feedItem.getPostedUserId().equalsIgnoreCase(friendId)){
                    goToUserFriendProfile(feedItem.getPostedUserId());
                }
            }
        }
    }


    @Override
    public void onTagClick(View v, String taggedId) {
        if (taggedId != null && !taggedId.equalsIgnoreCase("")) {
            if(!taggedId.equalsIgnoreCase(friendId))
                goToUserFriendProfile(taggedId);
        }
    }

    @Override
    public void onPostFromClick(View v, int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null && !feedItem.getPostedUserId().equalsIgnoreCase(friendId)){
                if(feedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(feedItem.getPostedUserId());
                }
            }
        }
    }

    @Override
    public void onPostToClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(!feedItem.getPostedFriendId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserFriendProfile(feedItem.getPostedFriendId());
                } else {
                    goToUserProfile();
                }
            }
        }
    }

    @Override
    public void onFavouriteClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(feedItem.isPostFavourited()){
                    feedItem.setPostFavourited(false);
                    Toast.makeText(mUserProfileActivity, "Removed from favourite posts", Toast.LENGTH_SHORT).show();
                } else {
                    feedItem.setPostFavourited(true);
                    Toast.makeText(mUserProfileActivity, "Added to favourite posts", Toast.LENGTH_SHORT).show();
                }
                feedItems.set(position, feedItem );
                if(feedAdapter != null){
                    feedAdapter.notifyItemChanged(position);
                }
                if(CheckNetworkConnection.isConnectionAvailable(mUserProfileActivity)){
                    doFavourite(feedItem, position);
                } else {
                    SnackBarDialog.showNoInternetError(v);
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

                if(response.body() != null && response.isSuccessful()){
                    Favourite mFavouriteData = response.body();
                    if(mFavouriteData != null && !mFavouriteData.isErrorStatus()){
                        Log.d("doFavourite", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Favourite> call, @NonNull Throwable t) {
                Log.d("doFavourite", "onFailure");
            }
        });
    }

    private void goToUserProfile() {
        Intent i = new Intent(mUserProfileActivity, UserProfileActivity.class);
        startActivity(i);
        this.finish();
        if (CheckOsVersion.isPreLollipop()) {
            mUserProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToUserFriendProfile(String postedUserId) {
        final Intent intent = new Intent(mUserProfileActivity, UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        startActivity(intent);
        this.finish();
        if(CheckOsVersion.isPreLollipop())
            if(mUserProfileActivity != null)
                mUserProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }



    public class XMPPLoginTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... strs)
        {
            try {
                XMPPTCPConnectionConfiguration connectionConfiguration = null;

                try {
                    connectionConfiguration = XMPPTCPConnectionConfiguration.builder()
                            .setUsernameAndPassword(strs[0], strs[1])
                            .setXmppDomain("myscrap.com")
                            .setPort(5222)
                            .setKeystoreType(null)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .build();
                }
                catch (XmppStringprepException e)
                {
                    e.printStackTrace();
                    Log.d(Constant.LOGTAG, e.toString());
                }


                AbstractXMPPConnection connection = null;
                connection = new XMPPTCPConnection(connectionConfiguration);

                connection.addConnectionListener(new ConnectionListener() {
                    @Override
                    public void connected(XMPPConnection connection)
                    {
                        Log.d(Constant.LOGTAG, "Connected");
                        ConnectionClass.getInstance().setConnection(connection);
                        UserUtils.saveXMPPLoginStatus(UserFriendProfileActivity.this,"1");

                        startActivity(new Intent(getApplicationContext(), XMPPChatRoomActivity.class));

                    }

                    @Override
                    public void authenticated(XMPPConnection connection, boolean resumed)
                    {
                        Log.d(Constant.LOGTAG, "Authenticated");

                    }

                    @Override
                    public void connectionClosed()
                    {
                        Log.d(Constant.LOGTAG, "connectionClosed");
                    }

                    @Override
                    public void connectionClosedOnError(Exception e)
                    {
                        Log.d(Constant.LOGTAG, "Rock - connectionClosedOnError");

                    }

                    @Override
                    public void reconnectionSuccessful()
                    {
                        Log.d(Constant.LOGTAG, "Rock - ReconnectionSuccessful");

                    }

                    @Override
                    public void reconnectingIn(int seconds)
                    {
                        Log.d(Constant.LOGTAG, "Rock - ReconnectingIn");

                    }

                    @Override
                    public void reconnectionFailed(Exception e)
                    {
                        Log.d(Constant.LOGTAG, "Rock - ReconnectionFailed");

                    }
                });



               // As we don't need listener here

      /*          // this listener we are going to use in chat activity
                ChatManager chatManager = ChatManager.getInstanceFor(connection);
                chatManager.addIncomingListener(new IncomingChatMessageListener() {
                    @Override
                    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                        Log.d(Constant.LOGTAG, "Incomming message : " + message.getBody());
                    }
                });
*/
                try
                {
                    connection.connect().login();
                }
                catch (XMPPException e)
                {
                    e.printStackTrace();
                    Log.d(Constant.LOGTAG, e.toString());

                } catch (SmackException e) {
                    e.printStackTrace();
                    Log.d(Constant.LOGTAG, e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(Constant.LOGTAG, e.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(Constant.LOGTAG, e.toString());
                }

            }
            catch (Exception exception)
            {

            }

            return null;
        }
    }







    public class FriendsJIDTask extends AsyncTask<String, Void, Void>
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
                        Boolean isError = responseObject.getBoolean("error");
                        String status = responseObject.getString("status");

/*

                            JSONArray profileArray = responseObject.getJSONArray("userProfileData");
                            JSONObject profileObject = profileArray.getJSONObject(0);
                            friendsJID = profileObject.getString("jId");
                            friendsId = profileObject.getString("userid");

                            String friendsName = profileObject.getString("name");
                            String friendsUrl = profileObject.getString("profilePic");
                            String friendsCompany = profileObject.getString("userCompany");
                            String friendsPosition = profileObject.getString("postedUserDesignation");
                            String friendsLocation = profileObject.getString("userLocation");
                            String friendsColor = profileObject.getString("colorCode");


                             Log.d(Constant.LOGTAG,friendsJID);
                             UserUtils.saveFriendsJID(UserFriendProfileActivity.this,friendsJID);
                             UserUtils.saveFriendsID(UserFriendProfileActivity.this,friendsId);
                             UserUtils.saveUserFriendsName(UserFriendProfileActivity.this,friendsName);
                             UserUtils.saveFriendsPicture(UserFriendProfileActivity.this,friendsUrl);
                             UserUtils.saveFriendsCompany(UserFriendProfileActivity.this,friendsCompany);
                             UserUtils.saveFriendsPosition(UserFriendProfileActivity.this, friendsPosition);
                             UserUtils.saveFriendsLocation(UserFriendProfileActivity.this,friendsLocation);
                             UserUtils.saveFriendsColor(UserFriendProfileActivity.this, friendsColor);
*/


                     /*   String userJID = model.getUserJID();
                        String friendsJID = model.getFriendsJID();
                        String friendsName = model.getFriendsName();
                        String friendsUrl = model.getFriendsUrl();
                        String friendsCompany = model.getFriendsCompany();
                        String friendsPosition = model.getFriendsPosition();
                        String friendsLocation = model.getFriendsLocation();
                        String color = model.getColor();*/




                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        Log.d(Constant.LOGTAG,e.toString());
                    }


                }

            };

            com.android.volley.Response.ErrorListener errorListener = new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e(Constant.LOGTAG, error.toString());
                }
            };


            StringRequest friendsJidRequest = new StringRequest(Request.Method.POST, "https://myscrap.com/android/msFriendProfileFeeds", jsonListener, errorListener) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("userId", strs[0]);
                    params.put("friendId", strs[1]);
                    params.put("pageLoad", strs[2]);
                    params.put("notId", strs[3]);
                    params.put("userView", strs[4]);
                    params.put("apiKey",strs[5]);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    return headers;
                }


            };

            friendsJidRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Volley.newRequestQueue(getApplicationContext()).add(friendsJidRequest);

            return null;
        }
    }





}
