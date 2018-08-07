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
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.EmployeeAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Employee;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import retrofit2.Call;
import retrofit2.Callback;

public class EmployeeActivity extends AppCompatActivity implements EmployeeAdapter.OnItemClickListener{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String companyId;
    private boolean isMyCompany;
    private EmployeeAdapter mEmployeeAdapter;
    private Employee.EmployeeData employeeData;
    private EmployeeActivity mEmployeeActivity;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mEmployeeActivity = this;
        EmployeeAdapter.OnItemClickListener mListener = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        final Intent mIntent = getIntent();
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_employee);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mEmployeeAdapter = new EmployeeAdapter(this, new Employee().getEmployeeData(), mListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mEmployeeActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mEmployeeAdapter);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);//
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (mIntent != null){
                if(mIntent.hasExtra("companyId")){
                    if(CheckNetworkConnection.isConnectionAvailable(mEmployeeActivity)) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        loadEmployeeDetails();
                    } else {
                        SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
                    }
                }
            }
        });

        if (mIntent != null){
            companyId = mIntent.getStringExtra("companyId");
            isMyCompany = mIntent.getBooleanExtra("isMyCompany", false);
            mSwipeRefreshLayout.post(() -> {
                if(mIntent.hasExtra("companyId")){
                    if(CheckNetworkConnection.isConnectionAvailable(mEmployeeActivity)) {
                        mSwipeRefreshLayout.setRefreshing(true);
                        loadEmployeeDetails();
                    } else {
                        SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
                    }
                }
            });
        }

    }

    private void loadEmployeeDetails() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            Call<Employee> call = apiService.companyEmployee(userId, companyId, apiKey);
            call.enqueue(new Callback<Employee>() {
                @Override
                public void onResponse(@NonNull Call<Employee> call, @NonNull retrofit2.Response<Employee> response) {
                    Log.d("loadEmployeeDetails", "onSuccess");
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                    if(response.body() != null && response.isSuccessful()){
                        Employee mEmployee = response.body();
                        if (mEmployee != null && !mEmployee.isErrorStatus()) {
                            employeeData = mEmployee.getEmployeeData();
                            mEmployeeAdapter.swap(employeeData, isMyCompany);
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Employee> call, @NonNull Throwable t) {
                    Log.d("loadEmployeeDetails", "onFailure");
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
        if(mTracker != null){
            mTracker.setScreenName("Employee Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(EmployeeActivity.this,UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(EmployeeActivity.this,UserOnlineStatus.OFFLINE);
    }


    @Override
    public void onOverFlow(View v, int position) {
        if(employeeData != null){
            if(employeeData.getEmployees() != null && employeeData.getEmployees().size() > 0 ){
                doRemoveEmployee(employeeData.getEmployees().get(position-1).getUserId());
                employeeData.getEmployees().remove(position-1);
                mEmployeeAdapter.swap(employeeData, isMyCompany);
            }
        }
    }

    private void doRemoveEmployee(String empId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Employee> call = apiService.removeEmployee(userId, companyId, empId, apiKey);
        call.enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(@NonNull Call<Employee> call, @NonNull retrofit2.Response<Employee> response) {

                if(response.body() != null && response.isSuccessful()){
                    Log.d("removeEmployee", "onSuccess");
                }
            }
            @Override
            public void onFailure(@NonNull Call<Employee> call, @NonNull Throwable t) {
                Log.d("removeEmployee", "onFailure");
            }
        });
    }
}
