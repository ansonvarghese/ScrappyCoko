package com.myscrap;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.InviteEventAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Contact;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.DividerItemDecoration;
import com.myscrap.view.PreCachingLayoutManager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventInviteActivity extends AppCompatActivity implements InviteEventAdapter.InviteEventAdapterListener, SearchView.OnQueryTextListener{


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Contact.ContactData> mContactDataList = new ArrayList<>();
    private InviteEventAdapter mInviteEventAdapter;
    private Tracker mTracker;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_invite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (getIntent() != null) {
            eventId = getIntent().getStringExtra("eventId");
        }
        mTracker = AppController.getInstance().getDefaultTracker();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.invitations_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        PreCachingLayoutManager linearLayoutManager = new PreCachingLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(this));
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(50);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        InviteEventAdapter.InviteEventAdapterListener listener = this;
        mInviteEventAdapter = new InviteEventAdapter(this, mContactDataList, listener);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(AppController.getInstance(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mInviteEventAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager1 = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisibleItem = linearLayoutManager1.findFirstCompletelyVisibleItemPosition();
                if (firstVisibleItem == 0) {
                    mSwipeRefreshLayout.setEnabled(true);
                } else {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                if (eventId != null && !eventId.equalsIgnoreCase(""))
                    loadContactsToAdd();
            } else {
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mContactDataList != null && !mContactDataList.isEmpty())
            getMenuInflater().inflate(R.menu.invite_search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
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

    private void loadContactsToAdd() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);

            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(this);
            Call<Contact> call = apiService.contacts(userId, eventId, apiKey);
            call.enqueue(new Callback<Contact>() {
                @Override
                public void onResponse(@NonNull Call<Contact> call, @NonNull Response<Contact> response) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("loadContactsToInvite", "onSuccess");
                    if (response.body() != null) {
                        if (mContactDataList != null)
                            mContactDataList.clear();
                        if(mSwipeRefreshLayout != null){
                            mSwipeRefreshLayout.setRefreshing(false);
                            Contact mContact = response.body();
                            if (mContact != null) {
                                if(!mContact.isErrorStatus() && mContact.getData() != null){
                                    mContactDataList = mContact.getData();
                                    if(mInviteEventAdapter != null){
                                        mSwipeRefreshLayout.post(() -> {
                                            mInviteEventAdapter.swap(mContactDataList);
                                            invalidateOptionsMenu();
                                        });

                                    }

                                } else {
                                    SnackBarDialog.show(mSwipeRefreshLayout, mContact.getStatus());
                                }
                            }
                        }

                    }
                }
                @Override
                public void onFailure(@NonNull Call<Contact> call, @NonNull Throwable t) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("loadContactsToInvite", "onFailure");
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            if(mSwipeRefreshLayout != null)
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        new Handler().post(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                if (eventId != null && !eventId.equalsIgnoreCase(""))
                    loadContactsToAdd();
            } else {
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
            }
        });
        if(mTracker != null){
            mTracker.setScreenName("Event Invite Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(this, UserOnlineStatus.ONLINE);
    }

    @Override
    public void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(this, UserOnlineStatus.OFFLINE);
    }

    @Override
    public void onContactsAdapterClicked(Contact.ContactData contactData) {
        if (contactData != null) {
            if (contactData.getUserId() != null && !contactData.getUserId().equalsIgnoreCase("")){
                if (contactData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(contactData.getUserId());
                }
            }
        }
    }


    private void goToUserProfile() {
        Intent i = new Intent(this, UserProfileActivity.class);
        startActivity(i);
        if (CheckOsVersion.isPreLollipop()) {
            this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToUserFriendProfile(String friendId) {
        final Intent intent = new Intent(this, UserFriendProfileActivity.class);
        intent.putExtra("friendId", friendId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onStarClicked(Contact.ContactData contactData, boolean isStarred) {
        if (contactData != null) {
            inviteToEvent(contactData);
            if(isStarred)
               Toast.makeText(this, "Invited", Toast.LENGTH_SHORT).show();
        }
    }

    private void inviteToEvent(Contact.ContactData mData) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            String friendId = mData.getUserId();
            Call<JSONObject> call = apiService.inviteToEvent(userId,friendId, eventId, apiKey);
            call.enqueue(new Callback<JSONObject>() {
                @Override
                public void onResponse(@NonNull Call<JSONObject> call, @NonNull Response<JSONObject> response) {
                    Log.d("inviteToEvent", "onSuccess");
                }
                @Override
                public void onFailure(@NonNull Call<JSONObject> call, @NonNull Throwable t) {
                    Log.d("inviteToEvent", "onFailure");
                }
            });
        } else {
            if(mSwipeRefreshLayout != null)
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (mContactDataList != null && !mContactDataList.isEmpty())
            mInviteEventAdapter.getFilter().filter(newText);
        return true;
    }


}
