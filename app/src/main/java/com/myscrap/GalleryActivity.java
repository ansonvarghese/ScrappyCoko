package com.myscrap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.GridLayoutAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.CompanyProfile;
import com.myscrap.model.PictureUrl;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView gridRecyclerView;
    private List<PictureUrl> pictureUrlList = new ArrayList<>();
    private GalleryActivity mGalleryActivity;
    private String companyId;
    private boolean isMyCompany;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mGalleryActivity = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);//
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        gridRecyclerView = (RecyclerView) findViewById(R.id.grid_recycler_view);
        setGridLayoutManager();
        Intent i = getIntent();
        if( i != null){
            companyId = i.getStringExtra("companyId");
            isMyCompany = i.getBooleanExtra("isMyCompany", false);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(mGalleryActivity, ImageUploadActivity.class);
            intent.putExtra("companyId", companyId);
            intent.putExtra("isMyCompany", isMyCompany);
            startActivity(intent);
        });

        if(mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setOnRefreshListener(() -> {
                if(companyId != null && !companyId.equalsIgnoreCase("")) {
                    if(CheckNetworkConnection.isConnectionAvailable(mGalleryActivity))
                        loadCompanyImages(companyId);
                    else
                        SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
                }
            });
        }


        if(isMyCompany){
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }

    }

    private void loadCompanyImages(String companyId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<CompanyProfile> call = apiService.companyGallery(userId, companyId, apiKey);
        call.enqueue(new Callback<CompanyProfile>() {
            @Override
            public void onResponse(@NonNull Call<CompanyProfile> call, @NonNull retrofit2.Response<CompanyProfile> response) {
                Log.d("Company Images", "onSuccess");
                if(mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
                CompanyProfileActivity.isSeeMore = false;
                if(response.body() != null && response.isSuccessful()){
                    CompanyProfile mCompanyProfile = response.body();
                    if(mCompanyProfile != null){
                        final CompanyProfile.CompanyData mData = mCompanyProfile.getCompanyData();
                        pictureUrlList.clear();
                        if(mData != null && mData.getPictureUrl() != null && mData.getPictureUrl().size() > 0){
                            pictureUrlList = mData.getPictureUrl();
                            setGridLayoutManager();
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<CompanyProfile> call, @NonNull Throwable t) {
                Log.d("Company Images", "onFailure");
                if(mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setGridLayoutManager() {
        if(gridRecyclerView != null){
            gridRecyclerView.setHasFixedSize(true);
            gridRecyclerView.setNestedScrollingEnabled(false);
            GridLayoutManager mLayoutManager = new GridLayoutManager(this, 3);
            gridRecyclerView.setLayoutManager(mLayoutManager);
            GridLayoutAdapter adapter = new GridLayoutAdapter(this, pictureUrlList);
            gridRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(CheckNetworkConnection.isConnectionAvailable(mGalleryActivity)){
            if(companyId != null && !companyId.equalsIgnoreCase("")){
                loadCompanyImages(companyId);
            }
        } else {
            SnackBarDialog.showNoInternetError(gridRecyclerView);
        }
        if(mTracker != null){
            mTracker.setScreenName("Company Gallery Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
