package com.myscrap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.myscrap.adapters.PeopleNearByAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.PeopleNearBy;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.ProgressBarTransparentDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.DividerItemDecoration;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ms3 on 4/27/2017.
 */

public class PeopleNearByFragment extends Fragment implements PeopleNearByAdapter.PeopleNearByAdapterListener{

    private LinearLayout enableLocationLayout;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Status status;
    private String PEOPLE_NEARBY_FRAGMENT = "PEOPLE_NEARBY_FRAGMENT";
    private BroadcastReceiver mBroadcastReceiver;
    private boolean isPeopleNearByFragment = false;
    private PeopleNearByAdapter mPeopleNearByAdapter;
    private List<PeopleNearBy.PeopleNearByData> mShakeFriendList = new ArrayList<>();
    public static final int REQUEST_CHECK_SETTINGS_PROXIMITY = 0x2;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mPeopleNearByFragmentView = inflater.inflate(R.layout.fragment_people_near_by, container, false);
        enableLocationLayout = (LinearLayout) mPeopleNearByFragmentView.findViewById(R.id.enableLocationLayout);
        Button searchButton = (Button) mPeopleNearByFragmentView.findViewById(R.id.search_button);
        mRecyclerView = (RecyclerView) mPeopleNearByFragmentView.findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        PeopleNearByAdapter.PeopleNearByAdapterListener listener = this;
        mPeopleNearByAdapter = new PeopleNearByAdapter(getActivity(), mShakeFriendList, listener);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(AppController.getInstance(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mPeopleNearByAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mPeopleNearByFragmentView.findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
            loadPeopleNearBy();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()){
                settingsRequest();
            }
        } else {
            settingsRequest();
        }


        searchButton.setOnClickListener(v -> {
            if(status != null){
                try {
                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS_PROXIMITY);
                } catch (IntentSender.SendIntentException e) {
                    Log.e("REQUEST_CHECK_SETTINGS", "Error " +  e);
                }
            }

        });


        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if(intent.getAction().equalsIgnoreCase(PEOPLE_NEARBY_FRAGMENT)){
                        if(mRecyclerView != null && !mRecyclerView.isShown())
                            mRecyclerView.setVisibility(View.VISIBLE);
                        if(enableLocationLayout != null)
                            enableLocationLayout.setVisibility(View.GONE);
                        if(mSwipeRefreshLayout != null)
                            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                        if(!isPeopleNearByFragment)
                            loadPeopleNearBy();
                    }
                }
            }
        };
        return mPeopleNearByFragmentView;
    }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                if (getActivity() == null)
                    return;

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        settingsRequest();
                    }
                }else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                    settingsRequest();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                    settingsRequest();
                }
            }
        }
    }

    private void loadPeopleNearBy() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            isPeopleNearByFragment = true;
            ProgressBarTransparentDialog.showLoader(getActivity(),"Searching...");
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getActivity());
            Log.d("peopleNearByUserId", ""+userId);
            apiService.peopleNearBy(userId, apiKey)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<PeopleNearBy>() {
                @Override
                public void onCompleted() {
                    Log.d("peopleNearBy", "onCompleted");
                }

                @Override
                public void onError(Throwable e) {
                    ProgressBarTransparentDialog.dismissLoader();
                    Log.d("peopleNearBy", "onFailure");
                    isPeopleNearByFragment = false;
                }

                @Override
                public void onNext(PeopleNearBy mPeopleNearBy) {
                    ProgressBarTransparentDialog.dismissLoader();
                    isPeopleNearByFragment = false;
                    Log.d("peopleNearBy", "onSuccess");
                    if (mPeopleNearBy != null) {
                        if (mShakeFriendList != null)
                            mShakeFriendList.clear();
                        if(!mPeopleNearBy.isErrorStatus()){
                            mShakeFriendList = mPeopleNearBy.getData();
                            if(mShakeFriendList != null && mPeopleNearByAdapter != null){
                                mPeopleNearByAdapter.swap(mShakeFriendList);
                            }
                        } else {
                            if(mRecyclerView != null)
                                SnackBarDialog.show(mRecyclerView, mPeopleNearBy.getStatus());
                        }
                    }
                }
            });
        } else {
            isPeopleNearByFragment = false;
            if(mRecyclerView != null)
                SnackBarDialog.show(mRecyclerView, "No internet connection available");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("People NearBy Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        if (getActivity() != null)
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(PEOPLE_NEARBY_FRAGMENT));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    public void settingsRequest()
    {

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getContext()).addApi(LocationServices.API).build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);  // it was 30*1000
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 ->
        {
            status = result1.getStatus();
            final LocationSettingsStates state = result1.getLocationSettingsStates();
            switch (status.getStatusCode())
            {
                case LocationSettingsStatusCodes.SUCCESS:
                    if(mRecyclerView != null)
                        mRecyclerView.setVisibility(View.VISIBLE);
                    if(enableLocationLayout != null)
                        enableLocationLayout.setVisibility(View.GONE);
                    if(!isPeopleNearByFragment)
                        loadPeopleNearBy();
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    if(mRecyclerView != null)
                        mRecyclerView.setVisibility(View.GONE);
                    if(enableLocationLayout != null)
                        enableLocationLayout.setVisibility(View.VISIBLE);
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    if(mRecyclerView != null)
                        mRecyclerView.setVisibility(View.GONE);
                    break;
            }
        });
    }

    @Override
    public void onPeopleNearByAdapterClicked(int position)
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(mShakeFriendList != null && mShakeFriendList.size() > 0)
        {
            if(mShakeFriendList.get(position).getUserid().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId()))
            {
                goToUserProfile();
            }
            else
            {
                goToUserFriendProfile(mShakeFriendList.get(position).getUserid());
            }
        }
    }

    private void goToUserProfile()
    {
        Intent i = new Intent(getActivity(), UserProfileActivity.class);
        startActivity(i);
        if (CheckOsVersion.isPreLollipop())
        {
            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToUserFriendProfile(String postedUserId)
    {
        final Intent intent = new Intent(getActivity(), UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(getActivity() != null)
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

}
