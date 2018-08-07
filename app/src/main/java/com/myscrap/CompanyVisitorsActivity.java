package com.myscrap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.ViewersAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Viewers;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.DividerItemDecoration;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompanyVisitorsActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Viewers.ViewersData> mViewersList = new ArrayList<>();
    private ViewersAdapter mViewersAdapter;
    private CompanyVisitorsActivity mCompanyVisitorsActivity;
    private String companyId;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_visitors);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mCompanyVisitorsActivity = this;
        mViewersAdapter = new ViewersAdapter(mCompanyVisitorsActivity, mViewersList);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mCompanyVisitorsActivity, DividerItemDecoration.VERTICAL_LIST));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mCompanyVisitorsActivity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(AppController.getInstance(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mViewersAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);
        Intent mIntent = getIntent();
        mTracker = AppController.getInstance().getDefaultTracker();
        if(mIntent != null){
            if(mIntent.hasExtra("companyId")){
                companyId = mIntent.getStringExtra("companyId");
            }
        }
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(mCompanyVisitorsActivity)){
                mSwipeRefreshLayout.setRefreshing(true);
                loadViewers();
            } else {
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
            }

        });
        load();
    }


    private void load() {
        if(CheckNetworkConnection.isConnectionAvailable(mCompanyVisitorsActivity)){
            loadViewers();
        } else {
            SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }
    }

    private void loadViewers() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(mCompanyVisitorsActivity);
            Log.d("ViewersList", ""+userId);
            Call<Viewers> call = apiService.companyVisitors(userId, companyId, apiKey);
            call.enqueue(new Callback<Viewers>() {
                @Override
                public void onResponse(@NonNull Call<Viewers> call, @NonNull Response<Viewers> response) {
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                    if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
                        mSwipeRefreshLayout.setRefreshing(false);
                    Log.d("ViewersList", "onSuccess");
                    if (response.body() != null) {
                        if (mViewersList != null)
                            mViewersList.clear();
                        Viewers mViewers = response.body();
                        if (mViewers != null) {
                            if(!mViewers.isErrorStatus()){
                                mViewersList = mViewers.getData();
                                if(mViewersAdapter != null){
                                    mViewersAdapter.swap(mViewersList);
                                }
                            } else {
                                if(mRecyclerView != null)
                                    SnackBarDialog.show(mRecyclerView, mViewers.getStatus());
                            }
                        }
                        viewersResponseProcessed(companyId);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Viewers> call, @NonNull Throwable t) {
                    if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
                        mSwipeRefreshLayout.setRefreshing(false);
                    Log.d("Viewers", "onFailure");
                }
            });
        } else {
            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);
            if(mRecyclerView != null)
                SnackBarDialog.show(mRecyclerView, "No internet connection available");
        }
    }


    private void viewersResponseProcessed(String companyId){
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(mCompanyVisitorsActivity);
            Log.d("viewersProcessed", ""+userId);
            Call<Viewers> call = apiService.companyVisitorsSeen(userId, companyId, apiKey);
            call.enqueue(new Callback<Viewers>() {
                @Override
                public void onResponse(@NonNull Call<Viewers> call, @NonNull Response<Viewers> response) {
                    Log.d("viewersProcessed", "onSuccess");
                }
                @Override
                public void onFailure(@NonNull Call<Viewers> call, @NonNull Throwable t) {
                    Log.d("viewersProcessed", "onFailure");
                }
            });
        } else {
            if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);
            if(mRecyclerView != null)
                SnackBarDialog.show(mRecyclerView, "No internet connection available");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Company Visitor Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

}
