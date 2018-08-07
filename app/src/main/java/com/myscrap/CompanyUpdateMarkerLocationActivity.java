package com.myscrap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.CompanyEditProfile;
import com.myscrap.model.CurrentLocationDetails;
import com.myscrap.model.MyItem;
import com.myscrap.service.FetchAddressIntentService;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.webservice.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompanyUpdateMarkerLocationActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>, GoogleMap.OnMyLocationButtonClickListener {

    private CompanyUpdateMarkerLocationActivity mCompanyUpdateMarkerLocationActivity;
    private MapView mMapView;
    private GoogleMap googleMap;
    private RelativeLayout mapLayout;
    private double lat;
    private double lng;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationSettingsRequest mLocationSettingsRequest;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private Location mLocation;
    private String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    private Location mFromMarkerDragLocation;
    private String companyId;
    private MyScrapSQLiteDatabase myScrapSQLiteDatabase;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_update_marker_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCompanyUpdateMarkerLocationActivity = this;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapLayout = (RelativeLayout)findViewById(R.id.small_map);
        mMapView = (MapView) findViewById(R.id.mapView);
        mapLayout = (RelativeLayout)findViewById(R.id.small_map);
        mMapView = (MapView) findViewById(R.id.mapView);
        mResultReceiver = new AddressResultReceiver(new Handler());
        mAddressOutput = "";
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        myScrapSQLiteDatabase = new MyScrapSQLiteDatabase(this);
        Intent mIntent = getIntent();
        if(mIntent != null) {
            if(mIntent.hasExtra("companyId")){
                companyId = mIntent.getStringExtra("companyId");
                String latitude = mIntent.getStringExtra("lat");
                String longitude = mIntent.getStringExtra("lng");
                lat = Double.valueOf(latitude);
                lng = Double.valueOf(longitude);
            }
        }
        if (mMapView != null) {
            mMapView.getMapAsync(this);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
        }
        initializeMap();
        mTracker = AppController.getInstance().getDefaultTracker();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {




                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mLocation = location;
                        CurrentLocationDetails.setCurrentLocation(mLocation);
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        UserUtils.saveLastLocationLat(mCompanyUpdateMarkerLocationActivity,Double.toString(lat));
                        UserUtils.saveLastLocationLng(mCompanyUpdateMarkerLocationActivity,Double.toString(lng));
                        Log.d("location", location.toString());
                    }

                }
            }
        };
    }

    private void goToHome() {
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        UserOnlineStatus.setUserOnline(CompanyUpdateMarkerLocationActivity.this,UserOnlineStatus.ONLINE);
        if(mTracker != null){
            mTracker.setScreenName("Company Update Marker Location Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();

        if (mGoogleApiClient.isConnected() && mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
           // mFusedLocationClient.removeLocationUpdates(mGoogleApiClient, this);
        }
        UserOnlineStatus.setUserOnline(CompanyUpdateMarkerLocationActivity.this,UserOnlineStatus.OFFLINE);
    }

    @Override
    public void onStart() {
        super.onStart();
        buildGoogleApiClient();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void initializeMap() {
        if (mapLayout != null && mMapView != null) {
            mapLayout.setVisibility(View.VISIBLE);
            mMapView.onResume();
            try {
                MapsInitializer.initialize(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            buildLocationSettingsRequest();
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkLocationPermission())
                    checkLocationSettings();
            } else {
                checkLocationSettings();
            }
        }
    }

    private synchronized void buildGoogleApiClient() {
        if (mCompanyUpdateMarkerLocationActivity != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            mGoogleApiClient.connect();
        }
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void checkLocationSettings() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
        } else {
            if (mGoogleApiClient != null) {
                PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
                result.setResultCallback(this);
            }
        }
    }


    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i("Map", "All location settings are satisfied.");
                if (mGoogleApiClient == null)
                    buildGoogleApiClient();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i("Map", "location settings are not satisfied. Show the user a dialog to" + "upgrade location settings ");
                try {
                    status.startResolutionForResult(mCompanyUpdateMarkerLocationActivity, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i("Map", "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i("Map", "location settings are inadequate, and cannot be fixed here. Dialog " + "not created.");
                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mGoogleApiClient.isConnected()) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            if (ContextCompat.checkSelfPermission(mCompanyUpdateMarkerLocationActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
               // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                if (mFusedLocationClient != null) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback,
                            null /* Looper */);
                }

            }
        } else {
            buildGoogleApiClient();
        }
    }


    private void startLocationUpdates() {

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient == null)
            buildGoogleApiClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mGoogleApiClient == null)
            buildGoogleApiClient();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mLocation = location;
            CurrentLocationDetails.setCurrentLocation(mLocation);
            lat = location.getLatitude();
            lng = location.getLongitude();
            UserUtils.saveLastLocationLat(mCompanyUpdateMarkerLocationActivity,Double.toString(lat));
            UserUtils.saveLastLocationLng(mCompanyUpdateMarkerLocationActivity,Double.toString(lng));
        }
    }

    private boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                            googleMap.setMyLocationEnabled(true);
                        } else {
                            googleMap.setMyLocationEnabled(true);
                        }
                        if (mFusedLocationClient != null && mLocationCallback != null) {
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback,
                                    null /* Looper */);
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (mLocation != null && googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 12));
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if ((googleMap != null) && (lat != 0) && (lng != 0)) {
            googleMap.addMarker(new MarkerOptions().draggable(true).position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.myscrap_pin)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12));
        }
        if (googleMap != null) {
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {}
                @Override
                public void onMarkerDrag(Marker marker) {}
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    mFromMarkerDragLocation = new Location("test");
                    mFromMarkerDragLocation.setLatitude(marker.getPosition().latitude);
                    mFromMarkerDragLocation.setLongitude(marker.getPosition().longitude);

                    if (mFromMarkerDragLocation != null) {
                        startIntentService();
                    }

                }
            });
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (mGoogleApiClient == null)
                    buildGoogleApiClient();
                if (googleMap != null)
                    googleMap.setMyLocationEnabled(true);
            }
        } else {
            if (mGoogleApiClient == null)
                buildGoogleApiClient();
            if (googleMap != null)
                googleMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    @SuppressLint("ParcelCreator")
    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            String lat = String.valueOf(mFromMarkerDragLocation.getLatitude());
            String lng = String.valueOf(mFromMarkerDragLocation.getLongitude());
            if (!lat.equalsIgnoreCase("") && !lng.equalsIgnoreCase("")) {
                 updateCompanyLocation(lat, lng);
            }
        }
    }

    private void updateCompanyLocation(String lat, String lng) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ProgressBarDialog.showLoader(mCompanyUpdateMarkerLocationActivity, false);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(mCompanyUpdateMarkerLocationActivity);
            Call<CompanyEditProfile> call = apiService.companyEditProfileLocation(userId,companyId, lat,lng,apiKey);
            call.enqueue(new Callback<CompanyEditProfile>() {
                @Override
                public void onResponse(@NonNull Call<CompanyEditProfile> call, @NonNull Response<CompanyEditProfile> response) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("editProfile", "onSuccess");
                    goToHome();
                    if (response.body() != null && response.isSuccessful()) {
                        CompanyEditProfile editProfile = response.body();
                        if(editProfile != null && !editProfile.isErrorStatus()){
                            if(editProfile.getData() != null) {
                            CompanyEditProfile.CompanyEditProfileData data = editProfile.getData();
                                MyItem item = new MyItem();
                                item.setMarkerId(companyId);
                                item.setCompanyName(data.getCompanyName());
                                item.setCompanyType(data.getCompanyType());
                                item.setCompanyAddress(data.getCompanyLocation());
                                item.setCompanyImage(data.getCompanyProfilePic());
                                item.setLatitude(Double.parseDouble(data.getCompanyLatitude()));
                                item.setLongitude(Double.parseDouble(data.getCompanyLongitude()));
                                myScrapSQLiteDatabase.updateMarker(item);
                                if(mapLayout != null)
                                    SnackBarDialog.show(mapLayout, "Location updated successfully.");
                            }
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<CompanyEditProfile> call, @NonNull Throwable t) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("editProfile", "onFailure");

                }
            });
        } else {
            if(mapLayout != null)
                SnackBarDialog.showNoInternetError(mapLayout);
        }
    }


    private void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mFromMarkerDragLocation);
        startService(intent);
    }

}
