package com.myscrap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.myscrap.adapters.FeedItemAnimator;
import com.myscrap.adapters.FeedsAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.DeletePost;
import com.myscrap.model.Event;
import com.myscrap.model.EventDelete;
import com.myscrap.model.EventGoing;
import com.myscrap.model.EventInterest;
import com.myscrap.model.EventReport;
import com.myscrap.model.Favourite;
import com.myscrap.model.Feed;
import com.myscrap.model.LikedData;
import com.myscrap.model.Report;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.FeedContextMenu;
import com.myscrap.view.FeedContextMenuManager;
import com.myscrap.view.PreCachingLayoutManager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.myscrap.adapters.FeedsAdapter.ACTION_LIKE_BUTTON_CLICKED;

public class EventDetailActivity extends AppCompatActivity  implements AppBarLayout.OnOffsetChangedListener{

    private static final int EVENT_GOING = 1;
    private static final int EVENT_NOT_GOING = 0;
    private static int EVENT_INTEREST_OR_NOT;
    private CoordinatorLayout main;
    private TabLayout mTabLayout;
    public static String eventId;
    private LinearLayout interestLayout;
    private ImageView interestIv;
    private ImageView goingIv;
    private TextView interestTv;
    private TextView goingTv;
    private TextView date, month;
    private TextView eventName;
    private TextView description;
    private TextView eventTime;
    private TextView eventWeek;
    private TextView eventPlace;
    private List<Event.EventData> mEventDataList = new ArrayList<>();
    private static Event.EventData eventData;
    private SimpleDateFormat sDFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    private SimpleDateFormat sDFormatDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private SimpleDateFormat sDTimeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private SimpleDraweeView mSimpleDraweeView;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AppBarLayout mAppBarLayout;
    private ViewPagerAdapter mViewPagerAdapter;
    private Subscription getEventDetailsSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        /*AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppBarLayout.setElevation(0);
        }*/
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(50);
        main = (CoordinatorLayout) findViewById(R.id.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        interestLayout = (LinearLayout) findViewById(R.id.interestLayout);
        LinearLayout goingLayout = (LinearLayout) findViewById(R.id.goingLayout);
        LinearLayout shareLayout = (LinearLayout) findViewById(R.id.shareLayout);
        LinearLayout moreLayout = (LinearLayout) findViewById(R.id.moreLayout);

        interestIv = (ImageView) findViewById(R.id.interestIv);
        interestIv.setTag("interest");
        goingIv = (ImageView) findViewById(R.id.goingIv);
        goingIv.setTag("go");
        ImageView shareIv = (ImageView) findViewById(R.id.shareIv);
        ImageView moreIv = (ImageView) findViewById(R.id.moreIv);

        interestTv = (TextView) findViewById(R.id.interestTv);
        goingTv = (TextView) findViewById(R.id.goingTv);
        TextView shareTv = (TextView) findViewById(R.id.shareTv);
        TextView moreTv = (TextView) findViewById(R.id.moreTv);


        date = (TextView) findViewById(R.id.date);
        month = (TextView) findViewById(R.id.month);

        eventName = (TextView) findViewById(R.id.event_name);
        description = (TextView) findViewById(R.id.description);

        eventTime = (TextView) findViewById(R.id.event_time);
        eventWeek = (TextView) findViewById(R.id.event_week);

        eventPlace = (TextView) findViewById(R.id.event_place);


        SimpleDraweeView mSmallSimpleDraweeView = findViewById(R.id.user_profile);
        TextView status = findViewById(R.id.status);
        status.setOnClickListener(v -> goToStatusActivity(""));
        ImageView photo = findViewById(R.id.photo);
        photo.setOnClickListener(v -> goToStatusActivity("camera"));

        String profilePicture = UserUtils.getUserProfilePicture(AppController.getInstance());
        if(profilePicture != null && !profilePicture.equalsIgnoreCase("")) {
            Uri uri = Uri.parse(profilePicture);
            com.facebook.imagepipeline.request.ImageRequest imgReq = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setProgressiveRenderingEnabled(true)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(imgReq)
                    .setTapToRetryEnabled(true)
                    .setOldController(mSmallSimpleDraweeView.getController())
                    .build();
            mSmallSimpleDraweeView.setController(controller);
        } else {
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                    .path(String.valueOf(R.drawable.no_profiles))
                    .build();
            mSmallSimpleDraweeView.setImageURI(uri);
        }


        if(getIntent() != null){
            eventId = getIntent().getStringExtra("eventId");
        }

        mSimpleDraweeView = (SimpleDraweeView) findViewById(R.id.event_image);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setupWithViewPager(mViewPager);
        setTabBold(0);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabBold(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTabNormal(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        interestLayout.setOnClickListener(v -> interest());
        goingLayout.setOnClickListener(v -> going());
        shareLayout.setOnClickListener(v -> shareEvents());
        moreLayout.setOnClickListener(v -> moreBottomMenu());

        interestIv.setOnClickListener(v -> interest());
        goingIv.setOnClickListener(v -> going());
        shareIv.setOnClickListener(v -> shareEvents());
        moreIv.setOnClickListener(v -> moreBottomMenu());

        interestTv.setOnClickListener(v -> interest());
        goingTv.setOnClickListener(v -> going());
        shareTv.setOnClickListener(v -> shareEvents());
        moreTv.setOnClickListener(v -> moreBottomMenu());

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mSwipeRefreshLayout.setRefreshing(true);

            new Handler().postDelayed(this::getEventDetails, 2000);
        });

    }


    private void goToStatusActivity(String click) {
        Intent intent = new Intent(AppController.getInstance(), StatusActivity.class);
        intent.putExtra("page", "eventDetail");
        intent.putExtra("eventName", eventData.getEventName());
        intent.putExtra("eventId", eventData.getEventId());
        intent.putExtra("click", click);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        //The Refresh must be only active when the offset is zero :
        if (mSwipeRefreshLayout != null)
           mSwipeRefreshLayout.setEnabled(i == 0);
    }

    private void interest() {
        if(interestIv.getTag().equals("interest")){
            interestIv.setTag("interested");
            interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            EVENT_INTEREST_OR_NOT = 1;
        } else {
            EVENT_INTEREST_OR_NOT = 0;
            interestIv.setTag("interest");
            interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black));
            interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        }
        sendInterest(EVENT_INTEREST_OR_NOT);
    }

    private void sendInterest(int interest) {
            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;
            if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                ApiInterface apiService =
                        ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
                String userId = AppController.getInstance().getPrefManager().getUser().getId();
                String apiKey = UserUtils.getApiKey(AppController.getInstance());

                apiService.eventInterest(userId, eventId, interest, apiKey)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<EventInterest>() {
                            @Override
                            public void onCompleted() {
                                Log.d("mEventInterest", "onCompleted: ");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("mEventInterest", "onError: ");
                            }

                            @Override
                            public void onNext(EventInterest mEventInterest) {
                                Log.d("mEventInterest", "onNext: ");
                                if(mEventInterest != null ){
                                    if(!mEventInterest.isErrorStatus()){
                                        if(eventData != null){
                                            eventData.setInterested(mEventInterest.isInterested());
                                            if(eventData.isGoing()) {
                                                goingIv.setTag("going");
                                                goingIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                                                goingTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                                            } else {
                                                goingIv.setTag("go");
                                            }
                                        }
                                    } else {
                                        if(main != null)
                                            SnackBarDialog.show(main, mEventInterest.getStatus());
                                    }
                                }

                            }
                        });
            } else {
                if(main != null)
                    SnackBarDialog.showNoInternetError(main);
            }
    }

    private void going() {
        interestLayout.setVisibility(View.GONE);
        if(goingIv.getTag().equals("go")){
            goingIv.setTag("going");
            goingIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            goingTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            sendEventGoing(EVENT_GOING);
        } else {
            goingIv.setTag("go");
            goingBottomMenu();
        }

    }

    private void sendEventGoing(int going) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());

            apiService.eventGoing(userId, eventId, going, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<EventGoing>() {
                        @Override
                        public void onCompleted() {
                            Log.d("mEventGoing", "onCompleted: ");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("mEventGoing", "onError: ");
                        }

                        @Override
                        public void onNext(EventGoing mEventGoing) {
                            Log.d("mEventGoing", "onNext: ");
                            if(mEventGoing != null ){
                                if(!mEventGoing.isErrorStatus()){
                                    if(eventData != null){
                                        eventData.setGoing(mEventGoing.isGoing());
                                        /*if(eventData.isInterested()) {
                                            interestIv.setTag("interested");
                                            interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                                            interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                                            EVENT_INTEREST_OR_NOT = 1;
                                        } else {
                                            EVENT_INTEREST_OR_NOT = 0;
                                            interestIv.setTag("interest");
                                            interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black));
                                            interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                                        }*/
                                    }
                                } else {
                                    if(main != null)
                                        SnackBarDialog.show(main, mEventGoing.getStatus());
                                }
                            }

                        }
                    });
        } else {
            if(main != null)
                SnackBarDialog.showNoInternetError(main);
        }
    }

    private void deleteEvent() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());

            apiService.eventDelete(userId, eventId, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<EventDelete>() {
                        @Override
                        public void onCompleted() {
                            Log.d("mEventDelete", "onCompleted: ");
                            Toast.makeText(EventDetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("mEventDelete", "onError: ");
                        }

                        @Override
                        public void onNext(EventDelete mEventDelete) {
                            Log.d("mEventDelete", "onNext: ");
                            if(mEventDelete != null ){
                                if(!mEventDelete.isErrorStatus()){

                                } else {
                                    if(main != null)
                                        SnackBarDialog.show(main, mEventDelete.getStatus());
                                }
                            }

                        }
                    });
        } else {
            if(main != null)
                SnackBarDialog.showNoInternetError(main);
        }
    }

    private void reportEvent() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());

            apiService.eventReport(userId, eventId, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<EventReport>() {
                        @Override
                        public void onCompleted() {
                            Toast.makeText(EventDetailActivity.this, "Reported", Toast.LENGTH_SHORT).show();
                            Log.d("mEventReport", "onCompleted: ");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("mEventReport", "onError: ");
                        }

                        @Override
                        public void onNext(EventReport mEventReport) {
                            Log.d("mEventReport", "onNext: ");
                            if(mEventReport != null ){
                                if(!mEventReport.isErrorStatus()){

                                } else {
                                    if(main != null)
                                        SnackBarDialog.show(main, mEventReport.getStatus());
                                }
                            }

                        }
                    });
        } else {
            if(main != null)
                SnackBarDialog.showNoInternetError(main);
        }
    }

    private void eventInviteActivity(String eventId) {
        Intent i = new Intent(this, EventInviteActivity.class);
        i.putExtra("eventId", eventId);
        startActivity(i);
        if(CheckOsVersion.isPreLollipop()){
            this.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTabBold(int tabBold) {
        if(mTabLayout != null) {
            TextView boldTextView = (TextView)(((LinearLayout)((LinearLayout)mTabLayout.getChildAt(0)).getChildAt(tabBold)).getChildAt(1));
            if(boldTextView != null)
                boldTextView.setTypeface(null, Typeface.BOLD);
        }

    }

    public void setTabNormal(int tabBold) {
        if(mTabLayout != null) {
            TextView normalTextView = (TextView) (((LinearLayout) ((LinearLayout) mTabLayout.getChildAt(0)).getChildAt(tabBold)).getChildAt(1));
            if(normalTextView != null)
                normalTextView.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void shareEvents() {
        if (eventData != null) {
            if (eventData.getEventShare() != null && !eventData.getEventShare().equalsIgnoreCase("")) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, eventData.getEventShare());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share event via"));
            }
        }

    }

    public void goingBottomMenu() {

        new BottomSheet.Builder(this).sheet(R.menu.event_going_bottom_menu_item).listener((dialog, which) -> {
            switch (which) {
                case R.id.menu_interest:
                    interestLayout.setVisibility(View.VISIBLE);
                    interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    goingIv.setColorFilter(null);
                    goingTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    Toast.makeText(EventDetailActivity.this, "Interested", Toast.LENGTH_SHORT).show();
                    if(eventData.isInterested()) {
                        interestIv.setTag("interest");
                        interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black));
                        interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                        EVENT_INTEREST_OR_NOT = 0;
                    } else {
                        EVENT_INTEREST_OR_NOT = 1;
                        interestIv.setTag("interested");
                        interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                        interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    }
                    sendInterest(EVENT_INTEREST_OR_NOT);
                    break;
                case R.id.menu_not_going:
                    interestLayout.setVisibility(View.VISIBLE);
                    interestIv.setColorFilter(null);
                    interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    goingIv.setColorFilter(null);
                    goingTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    Toast.makeText(EventDetailActivity.this, "Not Going", Toast.LENGTH_SHORT).show();
                    /*if(eventData.isInterested()) {
                        interestIv.setTag("interested");
                        interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                        interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                        EVENT_INTEREST_OR_NOT = 1;
                    } else {
                        EVENT_INTEREST_OR_NOT = 0;
                        interestIv.setTag("interest");
                        interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black));
                        interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    }*/
                    sendEventGoing(EVENT_NOT_GOING);
                    sendInterest(0);
                    break;
            }
        }).show();
    }

    public void moreBottomMenu() {
        BottomSheet.Builder mBuilder = new BottomSheet.Builder(this);
        mBuilder.sheet(R.menu.event_more_bottom_menu_item).listener((dialog, which) -> {
            switch (which) {
                case R.id.menu_invite:
                    invite();
                    break;
                    case R.id.menu_edit:
                    edit();
                    break;
                case R.id.menu_report:
                    showReportDialog();
                    break;
                case R.id.menu_delete:
                    showDeleteEventDialog();
                    break;
            }
        });
        if(eventData != null && eventData.getEventPostedId() != null && !eventData.getEventPostedId().equalsIgnoreCase("") && !eventData.getEventPostedId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
            mBuilder.remove(R.id.menu_invite);
            mBuilder.remove(R.id.menu_edit);
            mBuilder.remove(R.id.menu_delete);
        }
        mBuilder.show();
    }

    private void edit() {
        Intent i = new Intent(this, EventCreateActivity.class);
        i.putExtra("page", "editEvent");
        Gson gson = new Gson();
        String eventDetails = gson.toJson(eventData);
        i.putExtra("eventDetails", eventDetails);
        startActivity(i);
        if(CheckOsVersion.isPreLollipop()){
            this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void invite() {
        if (eventId != null && !eventId.equalsIgnoreCase("")) eventInviteActivity(eventId);
    }

    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Report").setMessage("Are you sure you want to report this event?")
        .setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            reportEvent();

        })
        .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).create();
        builder.show();

    }

    private void showDeleteEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Report").setMessage("Are you sure you want to delete this event?")
        .setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            deleteEvent();
        })
        .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).create();
        builder.show();

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAppBarLayout != null)
           mAppBarLayout.addOnOffsetChangedListener(this);
        if(CheckNetworkConnection.isConnectionAvailable(getApplicationContext())){
                if(eventId != null && !eventId.equalsIgnoreCase("")){
                    if(mSwipeRefreshLayout != null){
                        mSwipeRefreshLayout.setRefreshing(true);
                        getEventDetails();
                    }
                }
        } else {
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);
            SnackBarDialog.showNoInternetError(mTabLayout);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAppBarLayout != null)
           mAppBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        if (getEventDetailsSubscription != null && !getEventDetailsSubscription.isUnsubscribed())
            getEventDetailsSubscription.unsubscribe();
        super.onDestroy();
    }

    private void getEventDetails(){
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        getEventDetailsSubscription = apiService.getEventDetails(userId,eventId, apiKey)
        .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onCompleted() {
                        Log.d("SingleEvent", "onSuccess");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("SingleEvent", "onFailure");
                        if(mSwipeRefreshLayout != null){
                            mSwipeRefreshLayout.setRefreshing(false);
                            SnackBarDialog.show(mSwipeRefreshLayout, "Try again later!");
                        }
                        main.setVisibility(View.GONE);
                        onBackPressed();
                    }

                    @Override
                    public void onNext(Event mEventList) {
                        main.setVisibility(View.VISIBLE);
                        if(mSwipeRefreshLayout != null)
                            mSwipeRefreshLayout.setRefreshing(false);
                        if(mEventList != null && !mEventList.isErrorStatus()){
                            if(mEventList.getEventDataList() != null && mEventList.getEventDataList().size() > 0) {
                                mEventDataList = mEventList.getEventDataList();
                                if(mEventDataList != null && !mEventDataList.isEmpty()){
                                    eventData = mEventDataList.get(0);
                                    if(eventData != null){
                                        parseEventData(eventData);
                                    }
                                }
                            }
                            Log.d("SingleEvent", "onSuccess");
                        }
                    }
                });
    }

    private void parseEventData(Event.EventData eventData) {

        String formatDate = null, formatDateTwo = null,formatDateFormat, formatStartTime = null, formatEndTime= null, formattedTime;

        if(eventData.getStartDate() != null && !eventData.getStartDate().equalsIgnoreCase("")){
            formatDate = convertToDate(eventData.getStartDate());
            formatDateFormat = convertToDateFormat(eventData.getStartDate());
            String[] split = formatDateFormat.split(" ");
            String dateString = split[0];
            date.setText(dateString);
            String monthString = split[1];
            month.setText(monthString);
        }


        if (eventData.getEndDate() != null && !eventData.getEndDate().equalsIgnoreCase("")) {
            formatDateTwo = convertToDate(eventData.getEndDate());
        }

        if (eventData.getStartDate() != null && !eventData.getStartDate().equalsIgnoreCase("") &&
                eventData.getEndDate() != null && !eventData.getEndDate().equalsIgnoreCase("")){

            String week = UserUtils.getDateDifference(eventData.getStartDate(), eventData.getEndDate());
            if (week != null && !week.equalsIgnoreCase("")){
                if(week.equalsIgnoreCase("Expired")){
                    eventWeek.setText(week);
                    eventWeek.setTypeface((Typeface.defaultFromStyle(Typeface.BOLD)));
                    eventWeek.setTextColor(ContextCompat.getColor(this, R.color.notification_red));
                    eventWeek.setVisibility(View.VISIBLE);
                } else {
                    eventWeek.setText(week);
                    eventWeek.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    eventWeek.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                    eventWeek.setVisibility(View.VISIBLE);
                }
            } else {
                eventWeek.setVisibility(View.GONE);
            }
        }

        if(eventData.getEventPicture() != null && !eventData.getEventPicture().equalsIgnoreCase("")) {
            Uri uri = Uri.parse(eventData.getEventPicture());

            mSimpleDraweeView.setVisibility(View.VISIBLE);
            mSimpleDraweeView.post(new Runnable() {
                @Override
                public void run() {
                    com.facebook.imagepipeline.request.ImageRequest imgReq = ImageRequestBuilder.newBuilderWithSource(uri)
                            .setProgressiveRenderingEnabled(true)
                            .build();
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setImageRequest(imgReq)
                            .setTapToRetryEnabled(true)
                            .setOldController(mSimpleDraweeView.getController())
                            .build();
                    //mSimpleDraweeView.getHierarchy().setPlaceholderImage(R.drawable.no_events_image_blue, ScalingUtils.ScaleType.CENTER_CROP);
                    mSimpleDraweeView.getHierarchy().setPlaceholderImage(R.drawable.no_events_image_pink_blue_cover, ScalingUtils.ScaleType.CENTER_CROP);
                    mSimpleDraweeView.setController(controller);
                }
            });
        } else {
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                    .path(String.valueOf(R.drawable.no_events_image_pink_blue_cover))
                    .build();
            mSimpleDraweeView.setImageURI(uri);
        }


        if(eventData.isInterested()) {
            interestIv.setTag("interested");
            interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            EVENT_INTEREST_OR_NOT = 1;
        } else {
            EVENT_INTEREST_OR_NOT = 0;
            interestIv.setTag("interest");
            interestIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black));
            interestTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        }


        if(eventData.isGoing()) {
            goingIv.setTag("going");
            goingIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            goingTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        } else {
            goingIv.setTag("go");
            goingIv.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black));
            goingTv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        }

        if(eventData.getStartTime() != null && !eventData.getStartTime().equalsIgnoreCase("")){
            formatStartTime = eventData.getStartTime();
            //formatStartTime = convertToTime(eventData.getStartTime());
        }

        if(eventData.getEndTime() != null && !eventData.getEndTime().equalsIgnoreCase("")){
            formatEndTime = eventData.getEndTime();
            //formatEndTime = convertToTime(eventData.getEndTime());
        }

        formattedTime = formatDate +" - "+ formatDateTwo + " at " + formatStartTime+ " to "+formatEndTime;


        if(!TextUtils.isEmpty(formattedTime)){
            eventTime.setText(formattedTime);
            eventTime.setVisibility(View.VISIBLE);
        } else {
            eventTime.setVisibility(View.GONE);
        }

        if(eventData.getEventName() != null && !eventData.getEventName().equalsIgnoreCase("")){
            eventName.setText(eventData.getEventName());
        }

        if(eventData.getEventDetail() != null && !TextUtils.isEmpty(eventData.getEventDetail())){
            description.setText(eventData.getEventDetail());
        }

        if(eventData.getEventLocation() != null && !eventData.getEventLocation().equalsIgnoreCase("")){
            eventPlace.setText(eventData.getEventLocation());
            eventPlace.setVisibility(View.VISIBLE);
        } else {
            eventPlace.setVisibility(View.GONE);
        }


        if(mViewPagerAdapter != null){
            Fragment fr = mViewPagerAdapter.getItem(0);
            if(fr instanceof EventAbout) {
                ((EventAbout) fr).update();
            }
        }

    }

    private String convertToDate(String startDay){
        Date date;
        String strDate = null;
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        try {
            date = format.parse(startDay);
            strDate = sDFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    private String convertToDateFormat(String startDay){
        Date date;
        String strDate = null;
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        try {
            date = format.parse(startDay);
            strDate = sDFormatDate.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    private String convertToTime(String startDay){
        Date date;
        String strTime = null;
        DateFormat format = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
        try {
            date = format.parse(startDay);
            strTime = sDTimeFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strTime;
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        EventAbout mEventAbout;
        EventDiscussion mEventDiscussion;

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if(mEventAbout == null)
                        mEventAbout = new EventAbout();
                    return mEventAbout;
                case 1:
                    if(mEventDiscussion == null)
                        mEventDiscussion = new EventDiscussion();
                    return mEventDiscussion;
                default:
                    if(mEventAbout == null)
                        mEventAbout = new EventAbout();
                    return mEventAbout;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)
                return "ABOUT";
            else if(position==1)
                return "DISCUSSION";
            return "";
        }
    }

    public static class EventAbout extends Fragment {

        TextView status;
        TextView goingCount;
        TextView interestedCount;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.about_event, container, false);
            goingCount = v.findViewById(R.id.going);
            interestedCount = v.findViewById(R.id.interested);

            if (goingCount != null && interestedCount != null){
                goingCount.setOnClickListener(v1 -> {
                    if(eventData.getGoingCount() != 0) goToGuestListActivity(1);
                });
                interestedCount.setOnClickListener(v12 -> {
                    if(eventData.getInterestedCount() != 0) goToGuestListActivity(0);
                });
            }

            return v;
        }

        private void goToGuestListActivity(int tab) {
            if(eventData != null && !eventData.getEventId().equalsIgnoreCase("")){
                Intent intent = new Intent(AppController.getInstance(), GuestListActivity.class);
                intent.putExtra("eventId", eventData.getEventId());
                intent.putExtra("tab",tab);
                startActivity(intent);
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        public void update(){
            if(eventData != null && goingCount != null && interestedCount != null) {
                goingCount.setText(String.valueOf(eventData.getGoingCount()));
                interestedCount.setText(String.valueOf(eventData.getInterestedCount()));
            }
        }

    }
    public static class EventDiscussion extends Fragment implements  FeedsAdapter.OnFeedItemClickListener,
            FeedContextMenu.OnFeedContextMenuItemClickListener{

        private FeedsAdapter feedAdapter;
        private List<Feed.FeedItem> feedItems = new ArrayList<>();
        private RecyclerView rvFeed;
        private SwipeRefreshLayout swipe;
        private String pageLoad = "0";
        private boolean isLoadMore = false;
        private boolean isBottomRefresh = false;
        private PreCachingLayoutManager linearLayoutManager;
        private View emptyView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.discussion_event, container, false);
            rvFeed = (RecyclerView) view.findViewById(R.id.recycler_view_feeds);
            swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
            emptyView = view.findViewById(R.id.empty);
            UserUtils.setEmptyView(emptyView, R.drawable.ic_activity_empty, "No Discussions", false);
            swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
            swipe.setDistanceToTriggerSync(30);//
            swipe.setOnRefreshListener(this::loadFeeds);

            if(getActivity() != null){
                linearLayoutManager = new PreCachingLayoutManager(getActivity());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                linearLayoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
            }

            if(linearLayoutManager != null)
                rvFeed.setLayoutManager(linearLayoutManager);
            return view;
        }


        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setupFeed();
        }

        @Override
        public void onResume() {
            super.onResume();
            loadFeeds();
        }

        private void loadFeeds() {
            if(getActivity() == null)
                return;
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                pageLoad = "0";
                AppController.runOnUIThread(() -> {
                    if(swipe != null)
                        swipe.setRefreshing(true);
                    getFeeds(pageLoad);
                }, 1000);
            } else {
                SnackBarDialog.showNoInternetError(swipe);
            }
        }

        private void setupFeed() {
            if(getActivity() != null){
                feedAdapter = new FeedsAdapter(getActivity(), feedItems);
                feedAdapter.setOnFeedItemClickListener(this);
                rvFeed.setNestedScrollingEnabled(false);
                rvFeed.setItemAnimator(new FeedItemAnimator());
                rvFeed.setAdapter(feedAdapter);
            }
        }

        public void getFeeds(String pageLoad){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            apiService.getEventDetailFeeds(pageLoad, userId, eventId, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Event>() {
                        @Override
                        public void onCompleted() {
                            Log.d("Feeds", "onCompleted");
                            if(swipe != null)
                                swipe.setRefreshing(false);
                        }

                        @Override
                        public void onError(Throwable e) {
                            if(swipe != null)
                                swipe.setRefreshing(false);
                            Log.d("getFeeds", "Failure");
                            if(e != null  && e.getMessage() != null && e.getMessage().equalsIgnoreCase("SSL handshake timed out"))
                                SnackBarDialog.show(rvFeed, "Please try again later.");
                        }

                        @Override
                        public void onNext(final Event mFeed) {

                            if(!isLoadMore ){
                                feedItems.clear();
                                rvFeed.getRecycledViewPool().clear();
                                feedAdapter.notifyDataSetChanged();
                            }
                            if( mFeed != null){
                                if(!mFeed.isErrorStatus()){
                                    if(mFeed.getData()!= null){
                                        if (swipe.isRefreshing()) {
                                            feedItems.clear();
                                            feedItems.addAll(mFeed.getData());
                                            rvFeed.getRecycledViewPool().clear();
                                            //feedAdapter.notifyDataSetChanged();
                                            feedAdapter.swap(feedItems);
                                        } else if (isLoadMore) {
                                            isLoadMore = false;
                                            feedItems.addAll(mFeed.getData());
                                            rvFeed.getRecycledViewPool().clear();
                                            feedAdapter.swap(feedItems);
                                            //feedAdapter.notifyDataSetChanged();
                                        } else {
                                            feedItems.clear();
                                            feedItems.addAll(mFeed.getData());
                                            rvFeed.getRecycledViewPool().clear();
                                            //feedAdapter.notifyDataSetChanged();
                                            feedAdapter.swap(feedItems);
                                            //feedAdapter.notifyItemRangeInserted(0, feedItems.size());
                                        }
                                        if (!feedItems.isEmpty()){
                                            swipe.setVisibility(View.VISIBLE);
                                            emptyView.setVisibility(View.GONE);
                                        } else {
                                            swipe.setVisibility(View.GONE);
                                            emptyView.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        swipe.setVisibility(View.GONE);
                                        emptyView.setVisibility(View.VISIBLE);
                                    }
                                    Log.d("getFeeds", "Success");
                                } else {
                                    if(swipe != null){
                                        SnackBarDialog.show(emptyView, mFeed.getStatus());
                                        swipe.setVisibility(View.GONE);
                                        emptyView.setVisibility(View.VISIBLE);
                                    }
                                    Log.d("getFeeds", "failure");
                                }
                            }

                        }
                    });
        }

        @Override
        public void onCommentsClick(View v, int position) {
            if(!swipe.isRefreshing()) {
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
        public void onHeartClick(View v, int position) {
            if(!swipe.isRefreshing()){
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
                            //feedAdapter.swap(feedItems);
                        }
                        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
                            doLike(feedItem);
                    }
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
        public void onMoreClick(View v, int itemPosition, boolean isEditShow) {
            if(!swipe.isRefreshing()){
                if(feedItems != null && feedItems.size() > 0){
                    showPopupMenu(v,  feedItems.get(itemPosition), itemPosition, isEditShow);
                }
            }
        }

        private void showPopupMenu(View v, Feed.FeedItem feedItem, int itemPosition, boolean isEditShow) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.over_flow, popup.getMenu());
            Menu popupMenu = popup.getMenu();
            if(!isEditShow)
                popupMenu.findItem(R.id.action_edit).setVisible(false);
            popup.setOnMenuItemClickListener(new MyMenuItemClickListener(feedItem, itemPosition));
            popup.show();
        }

        private void showReportPopupMenu(final View v, final ViewGroup inActiveLayout, final Feed.FeedItem feedItem, final int itemPosition) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.report, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.action_report){
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setMessage("Are you sure you want to report this post?");
                    dialogBuilder.setCancelable(true);
                    dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                        doReport(feedItem.getPostId(), feedItem.getPostedUserId());
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

        private void showInactiveReportPopupMenu(final View v, final Feed.FeedItem feedItem, final int itemPosition) {
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
                        undoReport(feedItem.getReportId());
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
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setMessage("Are you sure you want to delete this post?");
            dialogBuilder.setCancelable(true);
            dialogBuilder.setPositiveButton("DELETE", (dialog, which) -> {
                feedItems.remove(itemPosition);
                feedAdapter.notifyDataSetChanged();
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
            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;
            if(!swipe.isRefreshing()) {
                if (feedItems != null && feedItems.size() > 0) {
                    Feed.FeedItem feedItem = feedItems.get(position);
                    if (feedItem != null) {
                        if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("eventPost")){
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

        private void goToUserProfile() {
            Intent i = new Intent(getActivity(), UserProfileActivity.class);
            startActivity(i);
            if (CheckOsVersion.isPreLollipop()) {
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
            if(CheckOsVersion.isPreLollipop())
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
                        Intent i = new Intent(getActivity(), StatusActivity.class);
                        i.putExtra("editPost", feedItem.getStatus());
                        Gson gson = new Gson();
                        String userData = gson.toJson(feedItem);
                        i.putExtra("tagData", userData);
                        i.putExtra("page", "eventDetail");
                        i.putExtra("eventName", eventData.getEventName());
                        i.putExtra("eventId", eventData.getEventId());
                        i.putExtra("postId", ""+feedItem.getPostId());
                        getActivity().startActivity(i);
                        if (CheckOsVersion.isPreLollipop())
                            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        return true;
                    case R.id.action_delete:
                        if (feedItem != null ) {
                            if(feedItems != null && feedItems.size() > 0){
                                showDeletePostDialog(getActivity(), feedItem.getPostedUserId(), feedItem.getPostId(), feedItem.getAlbumId(), itemPosition);
                            }

                        }
                        return true;
                    default:
                }
                return false;
            }
        }

    }
}
