package com.myscrap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.myscrap.adapters.GridLayoutAdapter;
import com.myscrap.adapters.LinearLayoutAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.CompanyProfile;
import com.myscrap.model.PictureUrl;
import com.myscrap.model.Report;
import com.myscrap.model.UserFriendProfile;
import com.myscrap.utils.CheckOsVersion;
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

public class PhotosActivity extends AppCompatActivity implements LinearLayoutAdapter.OnFeedItemClickListener{

    private ImageView grid, list;
    private RecyclerView gridRecyclerView;
    private LinearLayoutAdapter linearLayoutAdapter;
    private LinearLayoutAdapter.OnFeedItemClickListener listener;
    private List<PictureUrl> pictureUrlList = new ArrayList<>();
    private UserFriendProfile.UserFriendProfileData  userFriendProfileData;
    CompanyProfile.CompanyData mCompanyProfileData;
    CompanyProfile mProfile;
    private String companyId;
    private boolean isMyCompany;
    private boolean isGrid;
    private boolean isCamera;
 //   TextView noPhotos;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        setSupportActionBar(toolbar);
        listener = this;
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        Intent mIntent = getIntent();
        if(mIntent != null) {
            String pageName = mIntent.getStringExtra("pageName");
            companyId = mIntent.getStringExtra("companyId");
            isMyCompany = mIntent.getBooleanExtra("isMyCompany", false);
            isCamera = mIntent.getBooleanExtra("isCamera", false);
            Gson gson = new Gson();
            UserFriendProfile data = gson.fromJson(getIntent().getStringExtra("userData"), UserFriendProfile.class);
            mProfile = gson.fromJson(getIntent().getStringExtra("companyData"), CompanyProfile.class);
            if(data != null){
                if (data.getPictureUrl() != null){
                    pictureUrlList = data.getPictureUrl();
                }
                if (data.getUserProfileData() != null && data.getUserProfileData().get(0) != null)
                    userFriendProfileData = data.getUserProfileData().get(0);
                else {
                    userFriendProfileData = null;
                }

            } else {
                userFriendProfileData = null;
            }

            if (mProfile != null) {
                if (mProfile.getPictureUrl() != null){
                    pictureUrlList = mProfile.getPictureUrl();
                }
                if (mProfile.getCompanyData() != null) {
                    mCompanyProfileData = mProfile.getCompanyData();
                }else {
                    mCompanyProfileData = null;
                }
            } else {
                mCompanyProfileData = null;
            }
        }
        if(isMyCompany){
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }

        if (isCamera) {
            upload();
        }


        emptyView = findViewById(R.id.empty);
        UserUtils.setEmptyView(emptyView, R.drawable.ic_no_photo, "No Photos", true);
        grid = (ImageView) findViewById(R.id.grid);
        list = (ImageView) findViewById(R.id.list);
        gridRecyclerView = (RecyclerView) findViewById(R.id.grid_recycler_view);
        gridRecyclerView.setHasFixedSize(true);
        gridRecyclerView.setNestedScrollingEnabled(false);
        setGridLayoutManager();
        grid.setOnClickListener(v -> setGridLayoutManager());

        list.setOnClickListener(v -> setLinearLayoutManager());

        fab.setOnClickListener(view -> upload());
    }

    private void upload() {
        Intent intent = new Intent(getApplicationContext(), ImageUploadActivity.class);
        intent.putExtra("companyId", companyId);
        intent.putExtra("isMyCompany", isMyCompany);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if(resultCode == Activity.RESULT_OK){
                if (data != null) {
                    Gson gson = new Gson();
                    PictureUrl mPictureUrl = gson.fromJson(data.getStringExtra("pictureUrl"), PictureUrl.class);
                    if (mPictureUrl != null && pictureUrlList != null){
                        pictureUrlList.add(mPictureUrl);
                        if (isGrid){
                            setGridLayoutManager();
                        } else {
                            setLinearLayoutManager();
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserOnlineStatus.setUserOnline(this, UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(this, UserOnlineStatus.OFFLINE);
    }

    private void setGridLayoutManager() {
        isGrid = true;
        grid.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        list.setColorFilter(null);
        gridRecyclerView.setHasFixedSize(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        gridRecyclerView.setLayoutManager(mLayoutManager);
        GridLayoutAdapter gridAdapter = new GridLayoutAdapter(this, pictureUrlList);
        gridRecyclerView.setAdapter(gridAdapter);
        if (pictureUrlList.isEmpty()){
            emptyView.setVisibility(View.VISIBLE);
            gridRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            gridRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setLinearLayoutManager() {
        isGrid = false;
        list.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        grid.setColorFilter(null);
        gridRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        gridRecyclerView.setLayoutManager(mLinearLayoutManager);
        linearLayoutAdapter = new LinearLayoutAdapter(this,pictureUrlList, userFriendProfileData, mCompanyProfileData, listener);
        gridRecyclerView.setAdapter(linearLayoutAdapter);
        if (pictureUrlList.isEmpty()){
            emptyView.setVisibility(View.VISIBLE);
            gridRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            gridRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onReport(View v, int position) {

    }

    @Override
    public void onInActiveReport(View v, int position) {
        if(pictureUrlList != null && pictureUrlList.size() > 0) {
            PictureUrl item = pictureUrlList.get(position);
            if(item != null){
                if(CheckNetworkConnection.isConnectionAvailable(getApplicationContext()))
                    showInactiveReportPopupMenu(v, item, position);
                else
                    SnackBarDialog.showNoInternetError(v);
            }
        }
    }


    private void showInactiveReportPopupMenu(final View v, final PictureUrl feedItem, final int itemPosition) {
        PopupMenu popup = new PopupMenu(AppController.getInstance(), v);
        MenuInflater inflater = popup.getMenuInflater();
        if(feedItem.getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
            inflater.inflate(R.menu.un_report, popup.getMenu());
        } else {
            inflater.inflate(R.menu.report_contact_moderator, popup.getMenu());
        }

        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.action_un_report){
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AppController.getInstance());
                dialogBuilder.setMessage("Are you sure you want to un report this post?");
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                    undoReport(feedItem.getReportId());
                    if(pictureUrlList != null && pictureUrlList.size() > 0 &&  linearLayoutAdapter != null){
                        Toast.makeText(AppController.getInstance(), "Post Un Reported", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onMoreClick(View v, int adapterPosition) {

    }

    @Override
    public void onTagClick(View view, String taggedId) {
        if(taggedId != null && !taggedId.equalsIgnoreCase("")){
            if (taggedId.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                goToUserProfile();
            } else {
                goToUserFriendProfile(taggedId);
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

    private void goToUserFriendProfile(String postedUserId) {
        final Intent intent = new Intent(this, UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
                this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
