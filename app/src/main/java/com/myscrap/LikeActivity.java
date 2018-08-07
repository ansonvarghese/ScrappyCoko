package com.myscrap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.LikeAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Like;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LikeActivity extends AppCompatActivity {

    private LikeActivity mLikeActivity;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String userId, postId, postedUserId, apiKey, companyId;
    private LikeAdapter mLikeAdapter;
    private List<Like.LikeData> mLikeList = new ArrayList<>();
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        final Intent mIntent = getIntent();
        mLikeActivity = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_likes);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mLikeAdapter = new LikeAdapter(this, mLikeList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mLikeActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mLikeAdapter);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);//
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (mIntent != null){
                if(mIntent.hasExtra("companyId")){
                    loadCompanyLikes();
                } else {
                    load();
                }
            }
        });


        if (mIntent != null){
            userId = mIntent.getStringExtra("userId");
            postId = mIntent.getStringExtra("postId");
            if(mIntent.hasExtra("count")){
                int count = mIntent.getIntExtra("count", 0);

                if(count == 0){
                    if(getSupportActionBar() != null)
                        getSupportActionBar().setTitle("Like");
                } else if(count == 1){
                    if(getSupportActionBar() != null)
                        getSupportActionBar().setTitle(count +" Like");
                } else if(count > 1){
                    if(getSupportActionBar() != null)
                        getSupportActionBar().setTitle(count +" Likes");
                }
            }

            postedUserId = mIntent.getStringExtra("postedUserId");
            apiKey = mIntent.getStringExtra("apiKey");
            companyId = mIntent.getStringExtra("companyId");
            mSwipeRefreshLayout.post(() -> {
                if(mIntent.hasExtra("companyId")){
                    if(getSupportActionBar() != null)
                        getSupportActionBar().setTitle("Company Like");
                    loadCompanyLikes();

                } else {
                    load();
                }

            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return false;
    }

    private void load() {
        if(CheckNetworkConnection.isConnectionAvailable(mLikeActivity)){
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            Call<Like> call = apiService.loadLikeDetails(userId, postId, postedUserId, apiKey);
            call.enqueue(new Callback<Like>() {
                @Override
                public void onResponse(@NonNull Call<Like> call, @NonNull Response<Like> response) {
                    if (response.body() != null) {
                        if(mLikeList != null){
                            mLikeList.clear();
                            Like mLike = response.body();
                            if(mLike != null){
                                if (!mLike.isErrorStatus()){
                                    mLikeList.addAll(mLike.getData());
                                    mLikeAdapter.notifyDataSetChanged();
                                } else {
                                    if(mSwipeRefreshLayout != null)
                                        SnackBarDialog.show(mSwipeRefreshLayout, mLike.getStatus());
                                }
                            }
                        }

                        Log.d("LikeDetails", "success");
                    }
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                }
                @Override
                public void onFailure(@NonNull Call<Like> call, @NonNull Throwable t) {
                    Log.d("LikeDetails", "onFailure");
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);
            if(mSwipeRefreshLayout != null)
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }

    }

    private void loadCompanyLikes() {
        if(CheckNetworkConnection.isConnectionAvailable(mLikeActivity)){
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            Call<Like> call = apiService.loadCompanyLikesDetails(userId, companyId, apiKey);
            call.enqueue(new Callback<Like>() {
                @Override
                public void onResponse(@NonNull Call<Like> call, @NonNull Response<Like> response) {
                    if (response.body() != null) {
                        mLikeList.clear();
                        Like mLike = response.body();
                        if(mLike != null){
                            if (!mLike.isErrorStatus()){
                                mLikeList.addAll(mLike.getData());
                                mLikeAdapter.notifyDataSetChanged();
                                if(mLikeList.size() == 0){
                                    if(getSupportActionBar() != null)
                                        getSupportActionBar().setTitle("Company Like");
                                } else {
                                    if(getSupportActionBar() != null)
                                        getSupportActionBar().setTitle("Company Likes");
                                }
                            }else {
                                if(mSwipeRefreshLayout != null)
                                    SnackBarDialog.show(mSwipeRefreshLayout, mLike.getStatus());
                            }
                        }
                        Log.d("LikeDetails", "success");
                    }
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                }
                @Override
                public void onFailure(@NonNull Call<Like> call, @NonNull Throwable t) {
                    Log.d("LikeDetails", "onFailure");
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                    if(mLikeList != null && mLikeList.size() == 0){
                        if(getSupportActionBar() != null)
                            getSupportActionBar().setTitle("Company Like");
                    } else {
                        if(getSupportActionBar() != null)
                            getSupportActionBar().setTitle("Company Likes");
                    }
                }
            });
        } else {
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);
            if(mSwipeRefreshLayout != null)
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Likes Activity Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.OFFLINE);
    }

}
