package com.myscrap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.myscrap.adapters.FeedItemAnimator;
import com.myscrap.adapters.FeedsAdapter;
import com.myscrap.adapters.GridLayoutAdapter;
import com.myscrap.adapters.LinearLayoutAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.CompanyProfile;
import com.myscrap.model.DeletePost;
import com.myscrap.model.Employee;
import com.myscrap.model.Favourite;
import com.myscrap.model.Feed;
import com.myscrap.model.LikedData;
import com.myscrap.model.OwnIt;
import com.myscrap.model.PictureUrl;
import com.myscrap.model.Report;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.EndlessParentScrollListener;
import com.myscrap.view.FeedContextMenu;
import com.myscrap.view.PreCachingLayoutManager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.myscrap.adapters.FeedsAdapter.ACTION_LIKE_BUTTON_CLICKED;

public class CompanyProfileActivity extends AppCompatActivity implements OnMapReadyCallback,  LinearLayoutAdapter.OnFeedItemClickListener,  FeedsAdapter.OnFeedItemClickListener,
        FeedContextMenu.OnFeedContextMenuItemClickListener{

    private SimpleDraweeView mCompanyProfileImage;
    private ImageButton favourite;
    private TextView mCompanyName;
    private TextView mCompanySubName;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout ownItLayout;
    private TextView ownIt;
    private TextView like;
    private TextView interest;
    private TextView about;
    private TextView photos;
    private TextView seeMorePhotos;
    private LinearLayout likeLayout;
    private LinearLayout visitorLayout;
    private TextSwitcher likeCounter;
    private TextSwitcher visitorCounter;
    private LinearLayout employeeLayout;
    private TextSwitcher employeeCounter;
    private LinearLayout workingHoursLayout;

    private String companyId;
    private String notId = "0";
    private CompanyProfileActivity mCompanyProfileActivity;
    private MapView mMapView;
    private GoogleMap googleMap;
    private Double lat;
    private Double lng;
    private CompanyProfile mProfile;
    private CompanyProfile.CompanyData mCompanyProfileData;
    private boolean isMyCompany = false;
    private RecyclerView feeds;
    private NestedScrollView nested;
    private FrameLayout locationLayout;
    private LinearLayout totalLayout;
    private LinearLayout locationTextLayout;
    private TextView location;
    private TextView show_hide_map;
    //private MyGridView mGridView;
    private List<PictureUrl> pictureUrlList = new ArrayList<>();
    //private ImageAdapter mImageAdapter;
    public static boolean isSeeMore = false;
    private RecyclerView gridRecyclerView;
    private LinearLayoutAdapter linearLayoutAdapter;
    private LinearLayoutAdapter.OnFeedItemClickListener listener;
    private ImageView grid, list;
    private Tracker mTracker;
    private FeedsAdapter feedAdapter;
    private List<Feed.FeedItem> feedItems = new ArrayList<>();
    private PreCachingLayoutManager linearLayoutManager;
    private String pageLoad;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCompanyProfileActivity = this;
        listener = this;
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mCompanyProfileImage = (SimpleDraweeView) findViewById(R.id.companyProfile);
        grid = (ImageView) findViewById(R.id.grid);
        list = (ImageView) findViewById(R.id.list);
        feeds = (RecyclerView) findViewById(R.id.recycler_view_feeds);
        nested = (NestedScrollView) findViewById(R.id.nested);
        setupFeed();

        nested.setOnScrollChangeListener(new EndlessParentScrollListener(linearLayoutManager)
        {
            @Override
            public void onLoadMore(int page, int totalItemsCount)
            {
                Log.d("onLoadMore: ", "" + totalItemsCount);
                pageLoad = String.valueOf(totalItemsCount);

                if(mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing() )
                {
                    if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity)){
                        loadMoreFeeds(pageLoad);
                    }
                    else
                    {
                        SnackBarDialog.showNoInternetError(feeds);
                    }
                }
            }

            @Override
            public void onScrollUp() {

            }

            @Override
            public void onScrollDown() {

            }

            @Override
            public void onScrollTopReached() {

            }

            @Override
            public void onScrollBottomReached() {

            }


        });

        gridRecyclerView = (RecyclerView) findViewById(R.id.grid_recycler_view);
        gridRecyclerView.setHasFixedSize(true);
        gridRecyclerView.setNestedScrollingEnabled(false);
        ImageView mCompanyProfileEdit = (ImageView) findViewById(R.id.companyProfileEdit);
        favourite = (ImageButton) findViewById(R.id.btnContacts);
        mCompanyName = (TextView) findViewById(R.id.name);
        mCompanySubName = (TextView) findViewById(R.id.subName);
        totalLayout = (LinearLayout) findViewById(R.id.total_layout);
        ownItLayout = (LinearLayout) findViewById(R.id.own_it_layout);
        locationTextLayout = (LinearLayout) findViewById(R.id.location_layout);
        ownIt = (TextView) findViewById(R.id.own_it);
        LinearLayout afterOwnItLayout = (LinearLayout) findViewById(R.id.after_own_it_layout);
        like = (TextView) findViewById(R.id.like);
        TextView join = (TextView) findViewById(R.id.join);
        interest = (TextView) findViewById(R.id.interest);
        about = (TextView) findViewById(R.id.about);
        photos = (TextView) findViewById(R.id.photos);
        seeMorePhotos = (TextView) findViewById(R.id.see_more_photos);
        location = (TextView) findViewById(R.id.location);
        locationLayout = (FrameLayout) findViewById(R.id.map);
        show_hide_map = (TextView) findViewById(R.id.show_hide_map);
        show_hide_map.setTag("show");
        likeLayout = (LinearLayout) findViewById(R.id.like_layout);
        visitorLayout = (LinearLayout) findViewById(R.id.visitor_layout);
        likeCounter = (TextSwitcher) findViewById(R.id.tsCompanyLikesCounter);
        visitorCounter = (TextSwitcher) findViewById(R.id.tsCompanyVisitorsCounter);
        employeeLayout = (LinearLayout) findViewById(R.id.employee_layout);
        employeeCounter = (TextSwitcher) findViewById(R.id.tsCompanyEmployeeCounter);
        LinearLayout employeeJoinLayout = (LinearLayout) findViewById(R.id.employee_join_layout);
        LinearLayout galleryLayout = (LinearLayout) findViewById(R.id.gallery_layout);
        TextSwitcher employeeJoinCounter = (TextSwitcher) findViewById(R.id.tsCompanyEmployeeJoinCounter);
        workingHoursLayout = (LinearLayout) findViewById(R.id.working_hours_layout);
        TextView workingHours = (TextView) findViewById(R.id.companyBusinessHours);
        RelativeLayout postLayoutTop = (RelativeLayout) findViewById(R.id.post_layout);
        RelativeLayout postLayout = (RelativeLayout) findViewById(R.id.post);
        ImageView postLayoutImageView = (ImageView) findViewById(R.id.post_profile);
        TextView postTextBox = (TextView) findViewById(R.id.status_edit_box);
        setGridLayoutManager();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);//
        mMapView = (MapView) findViewById(R.id.mapView);
        if (mMapView != null) {
            mMapView.getMapAsync(this);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
        }
        if(mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setOnRefreshListener(() ->
            {
                if(companyId != null && !companyId.equalsIgnoreCase(""))
                {
                    if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity))
                        loadCompanyProfile(companyId, notId);
                    else
                        SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
                }
            });
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> goToPhotos(true));

        grid.setOnClickListener(v -> setGridLayoutManager());

        list.setOnClickListener(v -> setLinearLayoutManager());

        mTracker = AppController.getInstance().getDefaultTracker();

        Intent mIntent = getIntent();
        if(mIntent != null)
        {
            companyId = mIntent.getStringExtra("companyId");
            notId = mIntent.getStringExtra("notId");
            if(notId == null)
                notId = "0";
            if(companyId != null && !companyId.equalsIgnoreCase(""))
            {
                if(mSwipeRefreshLayout != null)
                {
                    mSwipeRefreshLayout.post(() ->
                    {
                        if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity))
                            loadCompanyProfile(companyId, notId);
                        else
                            SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
                    });
                }

            }
        }

    }

    private void loadMoreFeeds(String pageLoad)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        Log.d("Company Profile", "called");
        if(notId == null)
            notId = "0";

        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        if(mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        apiService.companyProfile(pageLoad, userId, companyId, notId, apiKey)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<CompanyProfile>() {
            @Override
            public void onCompleted()
            {

            }

            @Override
            public void onError(Throwable e)
            {
                Log.d("Company Profile", "onFailure");
                if(mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onNext(CompanyProfile mCompanyProfile)
            {
                Log.d("Company Profile", "onSuccess");
                if(mCompanyProfile != null && mCompanyProfile.getCompanyData() != null)
                {
                    if(mCompanyProfile.getData()!= null){
                        List<PictureUrl> url = new ArrayList<>();
                        if (mProfile.getPictureUrl() != null) {
                            url = mProfile.getPictureUrl();
                        }

                        if (mCompanyProfile.getPictureUrl() != null) {
                            url.addAll(mCompanyProfile.getPictureUrl());
                        }

                        if (url != null)
                            mProfile.setPictureUrl(url);
                        feedItems.addAll(mCompanyProfile.getData());
                        if(mCompanyProfileActivity != null){
                            runOnUiThread(() -> {
                                if (mSwipeRefreshLayout.isRefreshing()) {
                                    feedAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                    Log.d("CompanyProfileLoadMore", "onResponse: ");
                }
                if(mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupFeed()
    {
        feedAdapter = new FeedsAdapter(mCompanyProfileActivity, feedItems);
        feedAdapter.setOnFeedItemClickListener(this);
        feeds.setNestedScrollingEnabled(false);
        feeds.setItemAnimator(new FeedItemAnimator());
        linearLayoutManager = new PreCachingLayoutManager(mCompanyProfileActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(mCompanyProfileActivity));
        feeds.setLayoutManager(linearLayoutManager);
        feeds.setAdapter(feedAdapter);
    }

    private void loadCompanyProfile(String companyId, String notId)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        Log.d("Company Profile", "called");
        if(notId == null)
            notId = "0";
        String userId = AppController.getInstance().getPrefManager().getUser().getId();


            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);
                apiService.companyProfile("0", userId, companyId, notId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CompanyProfile>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Company Profile", "onFailure");
                        if(mSwipeRefreshLayout != null)
                            mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(CompanyProfile mCompanyProfile) {
                        if(mCompanyProfile != null && mCompanyProfile.getCompanyData() != null){
                            mProfile = mCompanyProfile;
                            totalLayout.setVisibility(View.VISIBLE);
                            initValues(mCompanyProfile);
                            Log.d("CompanyProfile", "onResponse: ");
                        }

                        if(mSwipeRefreshLayout != null)
                            mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void setGridLayoutManager() {
        grid.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        list.setColorFilter(null);
        gridRecyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        gridRecyclerView.setLayoutManager(mLayoutManager);
        GridLayoutAdapter gridAdapter = new GridLayoutAdapter(this, pictureUrlList);
        gridRecyclerView.setAdapter(gridAdapter);
    }

    private void setLinearLayoutManager() {
        list.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        grid.setColorFilter(null);
        gridRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        gridRecyclerView.setLayoutManager(mLinearLayoutManager);
        linearLayoutAdapter = new LinearLayoutAdapter(this, pictureUrlList, null, mCompanyProfileData, listener);
        gridRecyclerView.setAdapter(linearLayoutAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (isMyCompany) {
            inflater.inflate(R.menu.user_profile, menu);
            MenuItem item = menu.findItem(R.id.action_edit);
            item.setVisible(true);
            MenuItem itemPassword = menu.findItem(R.id.action_change_password);
            itemPassword.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }else if(item.getItemId() == R.id.action_edit){
            goToEditProfile();
        } else if(item.getItemId() == R.id.action_report){
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mCompanyProfileActivity);
            dialogBuilder.setMessage("Are you sure you want to report this company?");
            dialogBuilder.setCancelable(false);
            dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity)) {
                    doReport(companyId);
                    Toast.makeText(mCompanyProfileActivity, "Reported", Toast.LENGTH_SHORT).show();
                }else{
                    if(mSwipeRefreshLayout != null)
                        SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
                }
            });
            dialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void doReport(String postId, String postedUserId)
    {
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


    private void doReport(String companyId)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService = ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Report> call = apiService.reportPost(userId, "", "", companyId, apiKey);
        call.enqueue(new Callback<Report>()
        {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull retrofit2.Response<Report> response)
            {
                if(response.body() != null && response.isSuccessful())
                {
                    Report mReportPost = response.body();
                    if(mReportPost != null && !mReportPost.isErrorStatus())
                    {
                        Log.d("doReportCompany", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t)
            {
                Log.d("doReportCompany", "onFailure");
            }
        });
    }

    private void undoReport(String reportId)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        Call<Report> call = apiService.undoReportPost(reportId, apiKey);
        call.enqueue(new Callback<Report>()
        {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull retrofit2.Response<Report> response)
            {
                if(response.body() != null && response.isSuccessful())
                {
                    Report mReportPost = response.body();
                    if(mReportPost != null && !mReportPost.isErrorStatus())
                    {
                        Log.d("undoReport", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t)
            {
                Log.d("undoReport", "onFailure");
            }
        });
    }

    private void goToEditProfile()
    {
        Intent intent = new Intent(mCompanyProfileActivity, CompanyEditProfileActivity.class);
        intent.putExtra("companyId", companyId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(mCompanyProfileActivity != null)
                mCompanyProfileActivity.overridePendingTransition(0, 0);
    }

    private void initValues(final CompanyProfile mCompanyProfile)
    {
        if(mCompanyProfile != null)
        {
            final CompanyProfile.CompanyData mData = mCompanyProfile.getCompanyData();
            if(mData != null)
            {

                mCompanyProfileData = mData;

                if(mData.getCompanyImage() != null && !mData.getCompanyImage().equalsIgnoreCase("")) {
                    /*Glide.with(getBaseContext()).load(mData.getCompanyImage())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(mCompanyProfileImage);*/

                    mCompanyProfileImage.post(() ->
                    {
                        Uri uri = Uri.parse(mData.getCompanyImage());
                        mCompanyProfileImage.setImageURI(uri);
                    });

                }

                if(mData.getCompanyName() != null && !mData.getCompanyName().equalsIgnoreCase("")){
                    mCompanyName.setText(mData.getCompanyName().toUpperCase());
                }

                if(mData.getCompanyType() != null && !mData.getCompanyType().equalsIgnoreCase("")){
                    mCompanySubName.setText(mData.getCompanyType());
                } else {
                    mCompanySubName.setText("Trading");
                }

                ownItLayout.setVisibility(View.VISIBLE);


                if(mData.isFollowing()){
                    like.setText("LIKED");
                    like.setTag("like");
                } else {
                    like.setText("LIKE");
                    like.setTag("liked");
                }

                if(mData.isFavourite()){
                    favourite.setColorFilter(ContextCompat.getColor(mCompanyProfileActivity,R.color.colorPrimaryDark));
                    favourite.setImageDrawable(ContextCompat.getDrawable(mCompanyProfileActivity, R.drawable.ic_star_black_24dp));
                    favourite.setTag("favourite");
                } else {
                    favourite.setColorFilter(null);
                    favourite.setTag("favourited");
                    favourite.setImageDrawable(ContextCompat.getDrawable(mCompanyProfileActivity, R.drawable.ic_star_border_black_24dp));
                }

                favourite.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mCompanyProfileActivity)){
                        GuestLoginDialog.show(mCompanyProfileActivity);
                        return;
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity)){
                        if(favourite.getTag().equals("favourite")){
                            favourite.setTag("favourited");
                            favourite.setColorFilter(null);
                            favourite.setImageDrawable(ContextCompat.getDrawable(mCompanyProfileActivity, R.drawable.ic_star_border_black_24dp));
                            Toast.makeText(mCompanyProfileActivity, "Removed from favourites", Toast.LENGTH_SHORT).show();
                        } else {
                            favourite.setTag("favourite");
                            favourite.setImageDrawable(ContextCompat.getDrawable(mCompanyProfileActivity, R.drawable.ic_star_black_24dp));
                            favourite.setColorFilter(ContextCompat.getColor(mCompanyProfileActivity,R.color.colorPrimaryDark));
                            Toast.makeText(mCompanyProfileActivity, "Added to favourites", Toast.LENGTH_SHORT).show();
                        }
                        doFavourite(companyId);
                    } else {
                        SnackBarDialog.showNoInternetError(favourite);
                    }

                });



                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;

                if(mData.getOwnerUserId() != null && mData.getOwnerUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    ownIt.setVisibility(View.VISIBLE);
                    ownIt.setText("DISOWN");
                    isMyCompany = true;
                    invalidateOptionsMenu();
                }  else if(mData.getOwnerUserId() != null && !mData.getOwnerUserId().equalsIgnoreCase("") && mData.getOwnerUserId().equalsIgnoreCase("0") && mData.isEmployee()){
                    ownIt.setVisibility(View.VISIBLE);
                } else {
                    ownIt.setVisibility(View.GONE);
                }

                /*if(mData.getOwnerUserId() != null && mData.getOwnerUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.GONE);
                }*/

                ownIt.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mCompanyProfileActivity)){
                        GuestLoginDialog.show(mCompanyProfileActivity);
                        return;
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity)){
                        if (AppController.getInstance().getPrefManager().getUser() == null)
                            return;
                        if(mData.getOwnerUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                            doDisOwnIt();
                        }  else {
                            doOwnIt();

                        }
                    } else
                        SnackBarDialog.showNoInternetError(v);
                });
                pictureUrlList.clear();

                if(mData.getIsPartner() == 0){
                    /*if(mData.getPictureUrl() != null && mData.getPictureUrl().size() > 0){
                        pictureUrlList = mData.getPictureUrl();
                        setGridLayoutManager();
                        grid.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                        list.setColorFilter(null);
                        galleryLayout.setVisibility(View.VISIBLE);
                    } else {
                        galleryLayout.setVisibility(View.GONE);
                    }*/
                    feeds.setVisibility(View.GONE);
                } else {
                    //galleryLayout.setVisibility(View.GONE);
                    feeds.setVisibility(View.VISIBLE);
                    feedItems.clear();
                    if(mCompanyProfile.getData()!= null){
                        feedItems.addAll(mCompanyProfile.getData());
                        if(mCompanyProfileActivity != null){
                            runOnUiThread(() -> {
                                if (mSwipeRefreshLayout.isRefreshing()) {
                                    feedAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }

                if(mData.getOwnerUserId() != null)
                isMyCompany = mData.getOwnerUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId());

                seeMorePhotos.setOnClickListener(v -> {
                    isSeeMore = false;
                    goToGallery(companyId, isMyCompany);
                });

                like.setOnClickListener(v ->
                {
                    if(UserUtils.isGuestLoggedIn(mCompanyProfileActivity)){
                        GuestLoginDialog.show(mCompanyProfileActivity);
                        return;
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity)) {
                        if (like.getTag().equals("like")) {
                            like.setText("LIKE");
                            like.setTag("liked");
                            int count = Integer.parseInt(UserUtils.parsingInteger(mData.getCompanyLike()))-1;
                            mData.setCompanyLike(String.valueOf(count));
                            likeCounter.setCurrentText(likeLayout.getResources().getQuantityString(
                                    R.plurals.likes_count, count, count
                            ));
                        } else {
                            like.setText("LIKED");
                            like.setTag("like");
                            int count = Integer.parseInt(UserUtils.parsingInteger(mData.getCompanyLike()))+1;
                            mData.setCompanyLike(String.valueOf(count));
                            likeCounter.setCurrentText(likeLayout.getResources().getQuantityString(
                                    R.plurals.likes_count, count, count
                            ));
                        }
                        doCompanyLike(mData.getCompanyId());
                    } else {
                        SnackBarDialog.showNoInternetError(like);
                    }
                });

                if(mData.getCompanyLatitude() != null && mData.getCompanyLongitude() != null){
                    lat  = Double.valueOf(mData.getCompanyLatitude());
                    lng  = Double.valueOf(mData.getCompanyLongitude());
                    show_hide_map.setOnClickListener(v -> {
                        if(show_hide_map.getTag().equals("show")){
                            show_hide_map.setTag("hide");
                            show_hide_map.setText(R.string.hide_map);
                            locationLayout.setVisibility(View.VISIBLE);
                        } else {
                            show_hide_map.setText(R.string.show_map);
                            show_hide_map.setTag("show");
                            locationLayout.setVisibility(View.GONE);
                        }
                    });
                }

                if(mData.getCompanyCountry() != null && !mData.getCompanyCountry().equalsIgnoreCase("")){
                    location.setText(mData.getCompanyCountry());
                    location.setOnClickListener(v -> {
                        if (lat != null && lng != null ) {
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q="+String.valueOf(lat)+","+String.valueOf(lng));
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                    });
                    locationTextLayout.setVisibility(View.VISIBLE);
                } else {
                    locationTextLayout.setVisibility(View.GONE);
                }

                initializeMap();
                if(lat != null && lng != null && googleMap != null){
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().draggable(false).position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.myscrap_map_pin_1)));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),6));
                }

                if(mData.getCompanyLike() != null && !mData.getCompanyLike().equalsIgnoreCase("") &&  !mData.getCompanyLike().equalsIgnoreCase("0")){
                    likeLayout.setVisibility(View.VISIBLE);

                        likeCounter.setCurrentText(likeLayout.getResources().getQuantityString(
                                R.plurals.likes_count, Integer.parseInt(UserUtils.parsingInteger(mData.getCompanyLike())), Integer.parseInt(UserUtils.parsingInteger(mData.getCompanyLike()))
                        ));

                    likeCounter.setOnClickListener(v -> screenMoveToLikeActivity(mData.getCompanyId()));

                    likeLayout.setOnClickListener(v -> screenMoveToLikeActivity(mData.getCompanyId()));

                } else {
                    likeLayout.setVisibility(View.GONE);
                }

                if(isMyCompany) {
                    if(mData.getViewerCount() != 0 ){
                        visitorLayout.setVisibility(View.VISIBLE);

                        visitorCounter.setCurrentText(likeLayout.getResources().getQuantityString(
                                R.plurals.viewers_count, mData.getViewerCount(), mData.getViewerCount()
                        ));

                        visitorCounter.setOnClickListener(v -> screenMoveToViewersActivity(mData.getCompanyId()));

                        visitorLayout.setOnClickListener(v -> screenMoveToViewersActivity(mData.getCompanyId()));

                    } else {
                        visitorLayout.setVisibility(View.GONE);
                    }
                } else {
                    visitorLayout.setVisibility(View.GONE);
                }


                if(mData.getCompanyEmployees() !=0){
                    employeeLayout.setVisibility(View.VISIBLE);
                    if(mData.getCompanyEmployees() == 1)
                        employeeCounter.setText(mData.getCompanyEmployees() + " Employee");
                    else
                        employeeCounter.setText(mData.getCompanyEmployees() + " Employees");
                } else  {
                    employeeLayout.setVisibility(View.GONE);
                }

                employeeLayout.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mCompanyProfileActivity)){
                        GuestLoginDialog.show(mCompanyProfileActivity);
                        return;
                    }
                    goToEmployeeActivity(mData.getCompanyId(), isMyCompany);
                });
                employeeCounter.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mCompanyProfileActivity)){
                        GuestLoginDialog.show(mCompanyProfileActivity);
                        return;
                    }
                    goToEmployeeActivity(mData.getCompanyId(), isMyCompany);
                });

                workingHoursLayout.setVisibility(View.GONE);

                interest.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mCompanyProfileActivity)){
                        GuestLoginDialog.show(mCompanyProfileActivity);
                        return;
                    }
                    goToCompanyAbout("Company Interests", mData);
                });

                about.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mCompanyProfileActivity)){
                        GuestLoginDialog.show(mCompanyProfileActivity);
                        return;
                    }
                    goToCompanyAbout("About", mData);
                });

                photos.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mCompanyProfileActivity)){
                        GuestLoginDialog.show(mCompanyProfileActivity);
                        return;
                    }
                    goToPhotos(false);
                    //goToGallery(companyId, isMyCompany);
                });
            }
        }
    }

    private void goToGallery(String companyId, boolean isMyCompany) {
        Intent i = new Intent(this, GalleryActivity.class);
        i.putExtra("companyId", companyId);
        i.putExtra("isMyCompany", isMyCompany);
        startActivity(i);
        this.finish();
    }

    private void goToPhotos(boolean isCamera) {
        Intent intent = new Intent(AppController.getInstance(), PhotosActivity.class);
        Gson gson = new Gson();
        String userData = gson.toJson(mProfile);
        intent.putExtra("companyData", userData);
        intent.putExtra("isMyCompany", isMyCompany);
        intent.putExtra("companyId", companyId);
        intent.putExtra("isCamera", isCamera);
        intent.putExtra("pageName", "photo");
        startActivity(intent);
    }

    private void doFavourite(String companyId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Favourite> call = apiService.insertCompanyFavourite(userId, companyId,apiKey);
        call.enqueue(new Callback<Favourite>() {
            @Override
            public void onResponse(@NonNull Call<Favourite> call, @NonNull retrofit2.Response<Favourite> response) {

                if(response.body() != null && response.isSuccessful()){
                    Log.d("doFavourite", "onSuccess");
                }
            }
            @Override
            public void onFailure(@NonNull Call<Favourite> call, @NonNull Throwable t) {
                Log.d("doFavourite", "onFailure");
            }
        });
    }

    private void doOwnIt() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        ProgressBarDialog.showLoader(mCompanyProfileActivity, false);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<OwnIt> call = apiService.companyOwnIt(userId, companyId, apiKey);
        call.enqueue(new Callback<OwnIt>() {
            @Override
            public void onResponse(@NonNull Call<OwnIt> call, @NonNull retrofit2.Response<OwnIt> response) {
                Log.d("doOwnIt", "onSuccess");
                ProgressBarDialog.dismissLoader();
                if(response.body() != null && response.isSuccessful()){

                    OwnIt mOwnIt = response.body();
                    if(mOwnIt != null){
                        if(!mOwnIt.isErrorStatus()){
                            if(mOwnIt.isOwned()){
                               ownIt.setText("OWNED");
                                ownIt.setTag("own");
                                loadCompanyProfile(companyId, notId);
                            } else {
                                ownIt.setText("OWN IT");
                                ownIt.setTag("owned");
                            }
                        }
                    }

                }
            }
            @Override
            public void onFailure(@NonNull Call<OwnIt> call, @NonNull Throwable t) {
                Log.d("doOwnIt", "onFailure");
                ProgressBarDialog.dismissLoader();
            }
        });
    }

    private void doDisOwnIt() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        ProgressBarDialog.showLoader(mCompanyProfileActivity, false);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<OwnIt> call = apiService.companyDisOwnIt(userId, companyId, apiKey);
        call.enqueue(new Callback<OwnIt>() {
            @Override
            public void onResponse(@NonNull Call<OwnIt> call, @NonNull retrofit2.Response<OwnIt> response) {
                Log.d("doOwnIt", "onSuccess");
                ProgressBarDialog.dismissLoader();
                if(response.body() != null && response.isSuccessful()){

                    OwnIt mOwnIt = response.body();
                    if(mOwnIt != null){
                        if(!mOwnIt.isErrorStatus()){
                            if(mOwnIt.isOwned()){
                                ownIt.setText("OWN IT");
                                ownIt.setTag("own");
                                loadCompanyProfile(companyId, notId);
                            } else {
                                ownIt.setText("DISOWN");
                                ownIt.setTag("owned");
                                loadCompanyProfile(companyId, notId);
                            }
                        }
                    }

                }
            }
            @Override
            public void onFailure(@NonNull Call<OwnIt> call, @NonNull Throwable t) {
                Log.d("doOwnIt", "onFailure");
                ProgressBarDialog.dismissLoader();
            }
        });
    }


    private void doCompanyLike(String companyId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Employee> call = apiService.doCompanyLike(userId, companyId, apiKey);
        call.enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(@NonNull Call<Employee> call, @NonNull retrofit2.Response<Employee> response) {
                Log.d("doCompanyLike", "onSuccess");
                if(response.body() != null && response.isSuccessful()){
                    Employee mEmployee = response.body();
                    if(mEmployee != null){
                        if(!mEmployee.isErrorStatus()){
                            if(mEmployee.isLikeStatus()){
                                like.setText("Liked");
                                like.setTag("like");
                            } else {
                                like.setText("Like");
                                like.setTag("liked");
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Employee> call, @NonNull Throwable t) {
                Log.d("doCompanyLike", "onFailure");
            }
        });
    }

    private void screenMoveToLikeActivity(String companyId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        Intent mIntent = new Intent(mCompanyProfileActivity, LikeActivity.class);
        mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        mIntent.putExtra("companyId", companyId);
        mIntent.putExtra("apiKey", UserUtils.getApiKey(mCompanyProfileActivity));
        mCompanyProfileActivity.startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop()) {
            mCompanyProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void screenMoveToViewersActivity(String companyId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        Intent mIntent = new Intent(mCompanyProfileActivity, CompanyVisitorsActivity.class);
        mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        mIntent.putExtra("companyId", companyId);
        mIntent.putExtra("apiKey", UserUtils.getApiKey(mCompanyProfileActivity));
        mCompanyProfileActivity.startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop()) {
            mCompanyProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToEmployeeActivity(String companyId, boolean isMyCompany) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        Intent mIntent = new Intent(mCompanyProfileActivity, EmployeeActivity.class);
        mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        mIntent.putExtra("companyId", companyId);
        mIntent.putExtra("isMyCompany", isMyCompany);
        mIntent.putExtra("apiKey", UserUtils.getApiKey(mCompanyProfileActivity));
        mCompanyProfileActivity.startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop()) {
            mCompanyProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToCompanyAbout(String interest, CompanyProfile.CompanyData data) {
        Intent mIntent = new Intent(mCompanyProfileActivity, CompanyInterestActivity.class);
        Gson gson = new Gson();
        String userData = gson.toJson(data);
        mIntent.putExtra("companyData", userData);
        mIntent.putExtra("pageName", interest);
        startActivity(mIntent);
        if(CheckOsVersion.isPreLollipop())
            if(mCompanyProfileActivity != null)
                mCompanyProfileActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mMapView != null)
            mMapView.onResume();

        UserOnlineStatus.setUserOnline(CompanyProfileActivity.this,UserOnlineStatus.ONLINE);
        if(mTracker != null){
            mTracker.setScreenName("Company Profile Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        UserOnlineStatus.setUserOnline(CompanyProfileActivity.this,UserOnlineStatus.OFFLINE);
    }

    private void initializeMap() {
        if(mMapView != null)
            mMapView.onResume();
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public void onReport(View v, int position) {
        if(pictureUrlList != null && pictureUrlList.size() > 0) {
            PictureUrl item = pictureUrlList.get(position);
            if(item != null){
                if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity))
                    showReportPopupMenu(v, item, position);
                else
                    SnackBarDialog.showNoInternetError(v);
            }
        }
    }

    @Override
    public void onCommentsClick(View v, int position) {
        if(!mSwipeRefreshLayout.isRefreshing()) {
            if (feedItems != null && feedItems.size() > 0) {
                Feed.FeedItem feedItem = feedItems.get(position);
                if (feedItem != null) {
                    if(feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost")){
                        goToNewsPage(feedItem.getPostId());
                    } else {
                        if (CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity))
                            goToDetailPage(feedItem.getPostedUserId(), feedItem.getPostId());
                        else
                            SnackBarDialog.showNoInternetError(v);
                    }
                }
            }
        }
    }


    private void goToDetailPage(String postedUserId, String postId) {
        Intent intent = new Intent(mCompanyProfileActivity, DetailedPostActivity.class);
        intent.putExtra("postId", postId);
        intent.putExtra("userId", postedUserId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop()){
            if(mCompanyProfileActivity != null)
                mCompanyProfileActivity.overridePendingTransition(0, 0);
        }
    }
    private void goToNewsPage(String postId)
    {
        Intent i = new Intent(mCompanyProfileActivity, NewsViewActivity.class);
        i.putExtra("newsId", postId);
        startActivity(i);
        if(CheckOsVersion.isPreLollipop()){
            if(mCompanyProfileActivity != null)
                mCompanyProfileActivity.overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onImageCenterClick(View v, int position)
    {

    }

    @Override
    public void onEventClick(View v, int position)
    {

    }

    @Override
    public void onHeartClick(View v, int position) {
        if(!mSwipeRefreshLayout.isRefreshing()){
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
                        feedAdapter.notifyItemChanged(position, ACTION_LIKE_BUTTON_CLICKED);
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity))
                        doLike(feedItem);
                }
            }
        }
    }


    private void doLike(Feed.FeedItem feedItem)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService = ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<LikedData> call = apiService.insertLike(userId, feedItem.getPostId(), feedItem.getPostedUserId(),apiKey);
        call.enqueue(new Callback<LikedData>()
        {
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
                                for(Feed.FeedItem  feedItem : feedItems)
                                {
                                    if(feedItem.getPostId().equalsIgnoreCase(data.getPostId()))
                                    {
                                        Log.d("XLL","We are here");
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
    public void onLoadMore(int position) {

    }

    @Override
    public void onReport(View v, ViewGroup inActiveLayout, int position) {

    }

    @Override
    public void onInActiveReport(View v, int position) {
        if(pictureUrlList != null && pictureUrlList.size() > 0) {
            PictureUrl item = pictureUrlList.get(position);
            if(item != null){
                if(CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity))
                    showInactiveReportPopupMenu(v, item, position);
                else
                    SnackBarDialog.showNoInternetError(v);
            }
        }
    }

    @Override
    public void onMoreClick(View v, int position, boolean isEditShow) {

    }

    @Override
    public void onProfileClick(View v, int position) {

    }

    @Override
    public void onTagClick(View v, String taggedId) {

    }

    @Override
    public void onPostFromClick(View v, int position) {

    }

    @Override
    public void onPostToClick(View v, int position) {

    }

    @Override
    public void onFavouriteClick(View v, int position) {
        if(!mSwipeRefreshLayout.isRefreshing()) {
            if (feedItems != null && feedItems.size() > 0) {
                Feed.FeedItem feedItem = feedItems.get(position);
                if (feedItem != null) {
                    if (feedItem.isPostFavourited()) {
                        feedItem.setPostFavourited(false);
                        Toast.makeText(mCompanyProfileActivity, "Removed from favourite posts", Toast.LENGTH_SHORT).show();
                    } else {
                        feedItem.setPostFavourited(true);
                        Toast.makeText(mCompanyProfileActivity, "Added to favourite posts", Toast.LENGTH_SHORT).show();
                    }
                    feedItems.set(position, feedItem);
                    if (feedAdapter != null) {
                        feedAdapter.notifyItemChanged(position);
                    }
                    if (CheckNetworkConnection.isConnectionAvailable(mCompanyProfileActivity)) {
                        doFavourite(feedItem, position);
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

    @Override
    public void onMoreClick(View v, int adapterPosition) {
        if(!mSwipeRefreshLayout.isRefreshing()){
            if(pictureUrlList != null && pictureUrlList.size() > 0){
                showPopupMenu(v,  pictureUrlList.get(adapterPosition), adapterPosition);
            }
        }
    }

    private void showPopupMenu(View v, PictureUrl feedItem, int itemPosition) {
        PopupMenu popup = new PopupMenu(mCompanyProfileActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.over_flow, popup.getMenu());
        Menu popupMenu = popup.getMenu();
        popupMenu.findItem(R.id.action_edit).setVisible(false);
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(feedItem, itemPosition));
        popup.show();
    }

    @Override
    public void onReportClick(int feedItem) {

    }

    @Override
    public void onSharePhotoClick(int feedItem) {

    }

    @Override
    public void onCopyShareUrlClick(int feedItem) {

    }

    @Override
    public void onCancelClick(int feedItem) {

    }

    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private PictureUrl feedItem;
        private int itemPosition;
        public MyMenuItemClickListener(PictureUrl mFeedItem, int position) {
            this.feedItem = mFeedItem;
            this.itemPosition = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    if (feedItem != null ) {
                        if(pictureUrlList != null && pictureUrlList.size() > 0){
                            showDeletePostDialog(mCompanyProfileActivity, feedItem.getUserId(), feedItem.getPostid(), "0", itemPosition);
                        }

                    }
                    return true;
                default:
            }
            return false;
        }
    }

    private void showDeletePostDialog(Context context, final String id, final String postId, final String albumID, final int itemPosition){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mCompanyProfileActivity);
        dialogBuilder.setMessage("Are you sure you want to delete this post?");
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("DELETE", (dialog, which) -> {
            pictureUrlList.remove(itemPosition);
            linearLayoutAdapter.notifyDataSetChanged();
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

    private void showReportPopupMenu(final View v, final PictureUrl feedItem, final int itemPosition) {
        PopupMenu popup = new PopupMenu(mCompanyProfileActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.report, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.action_report){
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mCompanyProfileActivity);
                dialogBuilder.setMessage("Are you sure you want to report this post?");
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                    doReport(feedItem.getPostid(), feedItem.getUserId());
                    if(pictureUrlList != null && pictureUrlList.size() > 0 &&  linearLayoutAdapter != null){
                        Toast.makeText(mCompanyProfileActivity, "Post Reported", Toast.LENGTH_SHORT).show();
                        feedItem.setReported(true);
                        feedItem.setReportedUserId(AppController.getInstance().getPrefManager().getUser().getId());
                        linearLayoutAdapter.notifyItemChanged(itemPosition, feedItem);
                        linearLayoutAdapter.notifyDataSetChanged();
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

    private void showInactiveReportPopupMenu(final View v, final PictureUrl feedItem, final int itemPosition) {
        PopupMenu popup = new PopupMenu(mCompanyProfileActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        if(feedItem.getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
            inflater.inflate(R.menu.un_report, popup.getMenu());
        } else {
            inflater.inflate(R.menu.report_contact_moderator, popup.getMenu());
        }

        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.action_un_report){
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mCompanyProfileActivity);
                dialogBuilder.setMessage("Are you sure you want to un report this post?");
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                    undoReport(feedItem.getReportId());
                    if(pictureUrlList != null && pictureUrlList.size() > 0 &&  linearLayoutAdapter != null){
                        Toast.makeText(mCompanyProfileActivity, "Post Un Reported", Toast.LENGTH_SHORT).show();
                        feedItem.setReported(false);
                        feedItem.setReportedUserId(AppController.getInstance().getPrefManager().getUser().getId());
                        linearLayoutAdapter.notifyItemChanged(itemPosition, feedItem);
                        linearLayoutAdapter.notifyDataSetChanged();
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

    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        public ImageAdapter(Context context) {
            this.mContext = context;
        }

        public int getCount() {

            if(isSeeMore){
                return 9;
            } else{
                return pictureUrlList.size();
            }
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setAdjustViewBounds(true);
                imageView.setLayoutParams(new GridView.LayoutParams(240, 240));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            /*AppController.runOnUIThread(() -> Glide.with(mContext).load(pictureUrlList.get(position).getImages()).placeholder(R.drawable.icon_add_photo).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView));*/

            Picasso.with(mContext).load(pictureUrlList.get(position).getImages()).placeholder(R.drawable.icon_add_photo).into(imageView);


            imageView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("images", (Serializable) pictureUrlList);
                bundle.putInt("position", position);
                FragmentTransaction ft = ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction();
                CompanyImagesSlideshowDialogFragment newFragment = CompanyImagesSlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            });
            return imageView;
        }

    }
}
