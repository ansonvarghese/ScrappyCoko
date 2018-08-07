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
import com.myscrap.adapters.EmployeeRequestAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.EmployeeRequest;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class EmployeeRequestActivity extends AppCompatActivity {


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String companyId;
    private EmployeeRequestAdapter mEmployeeAdapter;
    private List<EmployeeRequest.EmployeeRequestData> employeeData = new ArrayList<>();
    private EmployeeRequestActivity mEmployeeActivity;
    private Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mEmployeeActivity = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_employee);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mEmployeeAdapter = new EmployeeRequestAdapter(mEmployeeActivity, companyId, employeeData);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mEmployeeActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mEmployeeAdapter);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);//
        final Intent mIntent = getIntent();
        if (mIntent != null){
            companyId = mIntent.getStringExtra("companyId");
            mSwipeRefreshLayout.post(() -> {
                if(mIntent.hasExtra("companyId")){
                    if(CheckNetworkConnection.isConnectionAvailable(mEmployeeActivity)) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        loadEmployeeRequestDetails();
                    } else {
                        SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
                    }
                }
            });
        }

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (mIntent != null){
                if(mIntent.hasExtra("companyId")){
                    if(CheckNetworkConnection.isConnectionAvailable(mEmployeeActivity)) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        loadEmployeeRequestDetails();
                    } else {
                        SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
                    }
                }
            }
        });


    }

    private void loadEmployeeRequestDetails() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<EmployeeRequest> call = apiService.companyEmployeeRequest(userId, companyId, apiKey);
        call.enqueue(new Callback<EmployeeRequest>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeRequest> call, @NonNull retrofit2.Response<EmployeeRequest> response) {
                Log.d("loadEmployeeRequest", "onSuccess");
                if(mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
                if(response.body() != null && response.isSuccessful()){
                    EmployeeRequest mEmployee = response.body();
                    if (mEmployee != null) {
                        if(!mEmployee.isErrorStatus() && mEmployee.getEmployeeRequestData() != null) {
                            employeeData = mEmployee.getEmployeeRequestData();
                            mEmployeeAdapter.swap(employeeData, companyId);
                        } else {
                            if(mSwipeRefreshLayout != null)
                                SnackBarDialog.show(mSwipeRefreshLayout, "No request found.");
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<EmployeeRequest> call, @NonNull Throwable t) {
                Log.d("loadEmployeeRequest", "onFailure");
                if(mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
            }
        });
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


    @Override
    protected void onResume() {
        super.onResume();
        UserOnlineStatus.setUserOnline(EmployeeRequestActivity.this,UserOnlineStatus.ONLINE);
        if(mTracker != null){
            mTracker.setScreenName("Employee Request Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(EmployeeRequestActivity.this,UserOnlineStatus.OFFLINE);
    }

}
