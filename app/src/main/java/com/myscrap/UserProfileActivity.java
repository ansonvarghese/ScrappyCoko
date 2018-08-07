package com.myscrap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
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
import com.myscrap.view.FlowLayout;
import com.myscrap.view.PreCachingLayoutManager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.myscrap.adapters.FeedsAdapter.ACTION_LIKE_BUTTON_CLICKED;

public class UserProfileActivity extends AppCompatActivity implements FeedsAdapter.OnFeedItemClickListener{
    private UserProfileActivity mUserProfileActivity;
    private RecyclerView recyclerViewFeeds;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FeedsAdapter feedAdapter;
    private List<Feed.FeedItem> feedItems = new ArrayList<>();
    private SimpleDraweeView profilePhoto;
    private TextView profileName;
    private TextView designation;
    private TextView companyName;
    private List<String> mInterestData = new ArrayList<>();
    private BroadcastReceiver broadCastReceiver;
    private TextView interestTextView;
    private TextView aboutTextView;
    private TextView photoTextView;

    private LinearLayout basicLayout;

    private LinearLayout rootEmptyFeedsLayout;
    private LinearLayout rootStatusBoxLayout;
    private LinearLayout rootProgressLayout;
    private SimpleDraweeView bottomProfile;
    private TextView profilePhotoText;
    private TextView percentage;
    private TextView rank;
    private TextView points;
    private String pageLoad = "0";
    private boolean isLoadMore;
    private TextView iconBottomText;
    private Tracker mTracker;
    private SimpleDraweeView userProfileMod;
    private UserFriendProfile mUserFriendProfile;
    private ProgressBar mProgressBar;
    private LinearLayout progressLayout;
    String[] descriptionData = {"Details", "Status", "Photo", "Confirm"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mTracker = AppController.getInstance().getDefaultTracker();
        NestedScrollView mNestedScrollView = (NestedScrollView) findViewById(R.id.nested_scroll_view);
        mInterestData.add("Non-Ferrous metals");
        mInterestData.add("Ferrous metals");
        mInterestData.add("Stainless Steel");
        mInterestData.add("Tyres");
        mInterestData.add("Paper");
        mInterestData.add("Textiles");
        mInterestData.add("Plastic");
        mInterestData.add("E-scrap");
        mInterestData.add("Red Metals");
        mInterestData.add("Aluminum");
        mInterestData.add("Zinc");
        mInterestData.add("Magnesium");
        mInterestData.add("Lead");
        mInterestData.add("Nickel/Stainless/Hi Temp");
        mInterestData.add("Mixed Metals");
        mInterestData.add("Others");
        mInterestData.add("Electric Furnace Casting and Foundry");
        mInterestData.add("Specially Processed Grades");
        mInterestData.add("Cast Iron Grades");
        mInterestData.add("Special Boring Grades");
        mInterestData.add("Steel From Scrap Tiles");
        mInterestData.add("Railroad Ferrous Scrap");
        mInterestData.add("Stainless Alloy");
        mInterestData.add("Special Alloy");
        mInterestData.add("Copper");
        mUserProfileActivity = this;
        FlowLayout flowLayout = new FlowLayout(this);
        int padding= DeviceUtils.dp2px(this,13);
        flowLayout.setPadding(padding,padding,padding,padding);
        profilePhoto = (SimpleDraweeView) findViewById(R.id.userProfile);
        userProfileMod = (SimpleDraweeView) findViewById(R.id.userProfileMod);
        profilePhotoText = (TextView) findViewById(R.id.icon_text);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        points = (TextView) findViewById(R.id.points);
        rank = (TextView) findViewById(R.id.rank);
        profileName = (TextView) findViewById(R.id.name);
        designation = (TextView) findViewById(R.id.designation);
        companyName = (TextView) findViewById(R.id.company);
        percentage = (TextView) findViewById(R.id.percentage);
        rootProgressLayout = (LinearLayout) findViewById(R.id.root_status_layout);
        progressLayout = (LinearLayout) findViewById(R.id.progressLayout);
        interestTextView = (TextView) findViewById(R.id.interestTextView);
        aboutTextView = (TextView) findViewById(R.id.aboutTextView);
        photoTextView = (TextView) findViewById(R.id.photoTextView);
        rootEmptyFeedsLayout = (LinearLayout) findViewById(R.id.empty_feeds);
        basicLayout = (LinearLayout) findViewById(R.id.basic_layout);
        LinearLayout interestLayoutChild = (LinearLayout) findViewById(R.id.interestLayout);
        recyclerViewFeeds = (RecyclerView) findViewById(R.id.recyclerViewFeeds);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);//
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                GuestLoginDialog.show(mUserProfileActivity);
                mSwipeRefreshLayout.setRefreshing(false);
                return;
            }
            if(CheckNetworkConnection.isConnectionAvailable(mUserProfileActivity)){
                pageLoad = "0";
                mSwipeRefreshLayout.setRefreshing(true);
                loadUserProfile();
            }
        });
        PreCachingLayoutManager linearLayoutManager = new PreCachingLayoutManager(mUserProfileActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(mUserProfileActivity));
        recyclerViewFeeds.setNestedScrollingEnabled(false);
        recyclerViewFeeds.setLayoutManager(linearLayoutManager);
        bottomProfile = (SimpleDraweeView) findViewById(R.id.user_profile);
        iconBottomText = (TextView) findViewById(R.id.icon_bottom_text);
        mNestedScrollView.setOnScrollChangeListener(new EndlessParentScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d("onLoadMore: ", "" + totalItemsCount);
                pageLoad = String.valueOf(totalItemsCount);
                if(rootProgressLayout != null)
                    rootProgressLayout.setVisibility(View.VISIBLE);
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

        String name = UserUtils.getFirstName(mUserProfileActivity) + " " +
                UserUtils.getLastName(mUserProfileActivity);

        if(!name.equalsIgnoreCase("")){
            profileName.setText(name);
        }


        String profilePic = UserUtils.getUserProfilePicture(mUserProfileActivity);
        if(profilePic != null && !profilePic.equalsIgnoreCase("")) {
            if(profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                    || profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                profilePhoto.setImageResource(R.drawable.bg_circle);
                profilePhoto.setColorFilter(R.color.guest);
                bottomProfile.setColorFilter(R.color.guest);
                bottomProfile.setImageResource(R.drawable.bg_circle);
                profilePhotoText.setVisibility(View.VISIBLE);
                iconBottomText.setVisibility(View.VISIBLE);
                bottomProfile.setVisibility(View.VISIBLE);
                if (!name.equalsIgnoreCase("")) {
                    String[] split = name.trim().split("\\s+");
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
            } else {
                Uri uri = Uri.parse(profilePic);
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                profilePhoto.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                        .setRoundingParams(roundingParams)
                        .build());
                roundingParams.setRoundAsCircle(true);
                profilePhoto.setImageURI(uri);
                Uri uriB = Uri.parse(profilePic);
                RoundingParams roundingParamsB = RoundingParams.fromCornersRadius(30f);
                bottomProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                        .setRoundingParams(roundingParamsB)
                        .build());
                roundingParamsB.setRoundAsCircle(true);
                bottomProfile.setImageURI(uriB);
                profilePhoto.setColorFilter(null);
                bottomProfile.setColorFilter(null);
                profilePhotoText.setVisibility(View.GONE);
                iconBottomText.setVisibility(View.GONE);
            }
        } else {
            profilePhoto.setImageResource(R.drawable.bg_circle);
            profilePhoto.setColorFilter(R.color.guest);
            profilePhotoText.setVisibility(View.VISIBLE);
            bottomProfile.setImageResource(R.drawable.bg_circle);
            bottomProfile.setColorFilter(R.color.guest);
            iconBottomText.setVisibility(View.VISIBLE);
            if (!name.equalsIgnoreCase("")) {
                String[] split = name.trim().split("\\s+");
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


        if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
            if (!name.equalsIgnoreCase("")) {
                String[] split = name.trim().split("\\s+");
                if (split.length > 1) {
                    String first = split[0].trim().substring(0, 1);
                    String last = split[1].trim().substring(0, 1);
                    String initial = first + "" + last;
                    iconBottomText.setText(initial.toUpperCase());
                } else {
                    String first = split[0].trim().substring(0, 1);
                    iconBottomText.setText(first.toUpperCase());
                }
            }
            iconBottomText.setVisibility(View.VISIBLE);
        }

        setupFeed();
        ImageView bottomCamera = (ImageView) findViewById(R.id.camera);
        TextView bottomStatusTextView = (TextView) findViewById(R.id.status);
        rootStatusBoxLayout = (LinearLayout) findViewById(R.id.root_status_box_layout);
        RelativeLayout bottomLayout = (RelativeLayout) findViewById(R.id.status_layout);

        bottomLayout.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                GuestLoginDialog.show(mUserProfileActivity);
                return;
            }
            goToStatusActivity("");
        });
        bottomCamera.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
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
        broadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent != null && intent.getAction() != null) {
                    if(intent.getAction().equalsIgnoreCase("refresh")){
                        if(mSwipeRefreshLayout != null){
                            mSwipeRefreshLayout.post(() -> {
                                if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                                    GuestLoginDialog.show(mUserProfileActivity);
                                    return;
                                }
                                loadUserProfile();
                            });
                        }
                    }
                }
            }
        };

        if(mSwipeRefreshLayout != null){
            mSwipeRefreshLayout.post(() -> {
                if(UserUtils.isGuestLoggedIn(mUserProfileActivity)){
                    GuestLoginDialog.show(mUserProfileActivity);
                    return;
                }
                mSwipeRefreshLayout.setRefreshing(true);
                loadUserProfile();
            });
        }

    }


    private void goToStatusActivity(String click) {
        Intent intent = new Intent(AppController.getInstance(), StatusActivity.class);
        intent.putExtra("page", "userProfile");
        intent.putExtra("click", click);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            mUserProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        if(!UserUtils.isGuestLoggedIn(mUserProfileActivity))
            inflater.inflate(R.menu.user_profile, menu);
        return true;
    }


    private void setupFeed()
    {
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
            if (pageLoad == null || pageLoad.equalsIgnoreCase(""))
                pageLoad = "0";
            apiService.userProfile(userId, pageLoad, apiKey)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<UserFriendProfile>() {
                @Override
                public void onCompleted()
                {

                }

                @Override
                public void onError(Throwable e)
                {
                    Log.d("Post", "onFailure");
                    isLoadMore = false;
                    recyclerViewFeeds.setVisibility(View.GONE);
                    rootEmptyFeedsLayout.setVisibility(View.VISIBLE);
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                    if(rootProgressLayout != null)
                        rootProgressLayout.setVisibility(View.GONE);

                    if(rootStatusBoxLayout != null)
                        rootStatusBoxLayout.setVisibility(View.VISIBLE);
                }


                @Override
                public void onNext(UserFriendProfile mUserFriendProfile)
                {
                    parse(mUserFriendProfile);
                    if(rootProgressLayout != null && rootProgressLayout.isShown())
                        rootProgressLayout.setVisibility(View.GONE);
                    if(rootStatusBoxLayout != null)
                        rootStatusBoxLayout.setVisibility(View.VISIBLE);
                    isLoadMore = false;
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                    Log.d("Post", "onSuccess");
                }
            });
        }
        else
        {
            if(mSwipeRefreshLayout != null)
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }
    }

    private void parse(UserFriendProfile userFriendProfile)
    {
        mUserFriendProfile = userFriendProfile;
        if(mUserFriendProfile != null && !mUserFriendProfile.isErrorStatus()){
            if(mUserFriendProfile.getUserProfileData() != null && mUserFriendProfile.getUserProfileData().size() > 0){

                UserFriendProfile.UserFriendProfileData data = mUserFriendProfile.getUserProfileData().get(0);
                if(data != null){
                    initiateValues(data);
                    if(basicLayout != null)
                        basicLayout.setVisibility(View.VISIBLE);
                    List<Feed.FeedItem> items = data.getData();
                    if(items != null){
                        if(!isLoadMore || mSwipeRefreshLayout.isRefreshing()){
                            feedItems.clear();
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
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if(mTracker != null)
        {
            mTracker.setScreenName("User Profile Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.ONLINE);
        LocalBroadcastManager.getInstance(mUserProfileActivity).registerReceiver(broadCastReceiver, new IntentFilter("refresh"));
    }


    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.OFFLINE);
        LocalBroadcastManager.getInstance(mUserProfileActivity).unregisterReceiver(broadCastReceiver);
    }

    private void initiateValues(final UserFriendProfile.UserFriendProfileData data) {
        if(data != null && mUserProfileActivity != null ){


            if(data.getProfilePic() != null ){

                if (data.getProfilePic().equalsIgnoreCase("") || data.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png") || data.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")) {
                    bottomProfile.setColorFilter(R.color.guest);
                    bottomProfile.setImageResource(R.drawable.bg_circle);
                    iconBottomText.setVisibility(View.VISIBLE);
                } else {
                    iconBottomText.setVisibility(View.GONE);
                    Uri uriB = Uri.parse(data.getProfilePic());
                    RoundingParams roundingParamsB = RoundingParams.fromCornersRadius(30f);
                    bottomProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                            .setRoundingParams(roundingParamsB)
                            .build());
                    roundingParamsB.setRoundAsCircle(true);
                    bottomProfile.setImageURI(uriB);
                    UserUtils.saveUserProfilePicture(mUserProfileActivity, data.getProfilePic());
                }
            }

            if(data.getName() != null && !data.getName().equalsIgnoreCase("")){
                profileName.setText(data.getName());
                String[] split = data.getName().split("\\s+");
                if (split.length > 1){
                    String first = split[0].trim().substring(0,1);
                    String last = split[1].trim().substring(0,1);
                    String initial = first + ""+ last ;
                    profilePhotoText.setText(initial.toUpperCase().trim());
                    iconBottomText.setText(initial.toUpperCase().trim());
                }
                else
                {
                    if(split[0] != null && split[0].length() == 1)
                    {
                        String first = split[0].trim().substring(0,1);
                        profilePhotoText.setText(first.toUpperCase().trim());
                        iconBottomText.setText(first.toUpperCase().trim());
                    }
                }
            }

            if (data.getProfilePercentage() != 100) {
                progressLayout.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(data.getProfilePercentage());
                String progress = data.getProfilePercentage() +"% profile completeness.";
                percentage.setText(progress);
                progressLayout.setOnClickListener(v -> showUpdateDialog(data.getProfilePercentage()));
            }
            else
            {
                progressLayout.setVisibility(View.GONE);
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
                    rank.setVisibility(View.VISIBLE);
                    rank.setBackground(ContextCompat.getDrawable(mUserProfileActivity, R.drawable.top));
                } else {
                    rank.setVisibility(View.GONE);
                }
            }


            if(data.getColorCode() != null && !data.getColorCode().equalsIgnoreCase("")){
                UserUtils.saveColorCode(mUserProfileActivity, data.getColorCode());
            }


            if(data.getFirstName() != null && !data.getFirstName().equalsIgnoreCase("")){
                UserUtils.saveFirstName(mUserProfileActivity, data.getFirstName());
            }

            if(data.getLastName() != null && !data.getLastName().equalsIgnoreCase("")){
                UserUtils.saveLastName(mUserProfileActivity, data.getLastName());
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

            if(data.getPostedUserDesignation() != null && !data.getPostedUserDesignation().equalsIgnoreCase("")){
                designation.setText(data.getPostedUserDesignation());
            } else {
                designation.setText("TRADER");
            }

            interestTextView.setOnClickListener(v -> goToInterest(data, "interest"));
            aboutTextView.setOnClickListener(v -> goToInterest(data, "about"));

            photoTextView.setOnClickListener(v -> goToPhotos(mUserFriendProfile));
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


    private void showUpdateDialog(int percentage) {
        String message = "Your profile is "+percentage+"% completeness. Update more details to your profile.";
        AlertDialog.Builder builder = new AlertDialog.Builder(mUserProfileActivity);
        builder.setTitle("Information Required");
        builder.setMessage(message);
        builder.setPositiveButton("UPDATE", (dialog, which) -> goToEditProfile());
        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        if (dialog != null && !dialog.isShowing())
            dialog.show();
    }


    private void goToPhotos(UserFriendProfile data) {
        Intent intent = new Intent(AppController.getInstance(), PhotosActivity.class);
        Gson gson = new Gson();
        String userData = gson.toJson(data);
        intent.putExtra("userData", userData);
        intent.putExtra("pageName", "photo");
        startActivity(intent);
    }

    private void goToInterest(UserFriendProfile.UserFriendProfileData data, String pageName) {
        Intent intent = new Intent(AppController.getInstance(), InterestActivity.class);
        Gson gson = new Gson();
        String userData = gson.toJson(data);
        intent.putExtra("userData", userData);
        intent.putExtra("pageName", pageName);
        intent.putExtra("isShowOption", true);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        } else if(item.getItemId() == R.id.action_edit){
            goToEditProfile();
        }else if(item.getItemId() == R.id.action_change_password){
            goToChangePasswordActivity();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void goToEditProfile()
    {
        final Intent intent = new Intent(mUserProfileActivity, EditProfileActivity.class);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(mUserProfileActivity != null)
                mUserProfileActivity.overridePendingTransition(0, 0);
    }

    private void  goToChangePasswordActivity() {
        final Intent intent = new Intent(mUserProfileActivity, ChangePasswordActivity.class);
        mUserProfileActivity.startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(mUserProfileActivity != null)
                mUserProfileActivity.overridePendingTransition(0, 0);
    }

    @Override
    public void onCommentsClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0){
            Feed.FeedItem feedItem = feedItems.get(position);
            if(CheckNetworkConnection.isConnectionAvailable(mUserProfileActivity))
                goToDetailPage(feedItem.getPostedUserId(), feedItem.getPostId());
            else
                SnackBarDialog.showNoInternetError(v);
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
    public void onHeartClick(View v, int position)
    {
        if(feedItems != null && feedItems.size() > 0)
        {
            Feed.FeedItem feedItem = feedItems.get(position);
            int likeCount;
            if(feedItem != null)
            {
                if(feedItem.isLikeStatus())
                {
                    likeCount = feedItem.getLikeCount();
                    if(likeCount > 0)
                    {
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
                    feedAdapter.notifyItemChanged(position, ACTION_LIKE_BUTTON_CLICKED);
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
                    i.putExtra("page", "userProfile");
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
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(!feedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserFriendProfile(feedItem.getPostedUserId());
                }
            }
        }
    }

    @Override
    public void onTagClick(View v, String taggedId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (taggedId != null && !taggedId.equalsIgnoreCase("")) {
            if(!taggedId.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()))
                goToUserFriendProfile(taggedId);
        }
    }

    @Override
    public void onPostFromClick(View v, int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(!feedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserFriendProfile(feedItem.getPostedUserId());
                }
            }
        }
    }

    @Override
    public void onPostToClick(View v, int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(!feedItem.getPostedFriendId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserFriendProfile(feedItem.getPostedFriendId());
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
                Log.d("doFavourite", "onSuccess");
            }
            @Override
            public void onFailure(@NonNull Call<Favourite> call, @NonNull Throwable t) {
                Log.d("doFavourite", "onFailure");
            }
        });
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

    private void goToUserProfile() {
        Intent i = new Intent(mUserProfileActivity, UserProfileActivity.class);
        startActivity(i);
        this.finish();
        if (CheckOsVersion.isPreLollipop()) {
            mUserProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

}
