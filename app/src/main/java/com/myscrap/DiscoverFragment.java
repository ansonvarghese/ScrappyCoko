package com.myscrap;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LruCache;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.myscrap.adapters.MarkerFilterRecyclerViewAdapter;
import com.myscrap.adapters.OnItemTouchListener;
import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.CurrentLocationDetails;
import com.myscrap.model.MyItem;
import com.myscrap.notification.Config;
import com.myscrap.service.MarkerListFetchService;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.MarkerSpider;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DiscoverFragment extends Fragment implements OnMapReadyCallback,LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>, GoogleMap.OnMyLocationButtonClickListener{


    private GoogleMap googleMap;
    private LinearLayout company_layout;
    private Location mLocation = null;
    private Context mMap;
    private String mMarkerID;
    boolean isCompanyLayoutVisible = false;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private boolean firstTime = true;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    //private boolean isVisibleToUserCheck = false;
    private EditText mSearchMarkers ;
    private RecyclerView mRecyclerView;
    private MarkerFilterRecyclerViewAdapter adapter;
    private List<MyItem> mUserSearchModel;
    private List<MyItem>  mUserSearchModelLists = new ArrayList<>();
    private MyScrapSQLiteDatabase mMyScrapSQLiteDatabase;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<MyItem> markerList = new ArrayList<>();
    private MarkerSpider markerSpider;
    private ClusteringSettings clusterSettings;

    //private static final int[] res = {R.drawable.cluster};
    private static final int[] res = {R.drawable.cluster_new_2};
    //private static final int[] resPin = {R.drawable.myscrap_pin_final};
    private static final int[] resPin = {R.drawable.myscrap_map_pin_1};
    private Bitmap[] baseBitmaps;
    private LruCache<Integer, BitmapDescriptor> cache = new LruCache<>(128);
    private LruCache<Integer, BitmapDescriptor> cachePin = new LruCache<>(128);
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect bounds = new Rect();
    private ClusterOptions clusterOptions = new ClusterOptions().anchor(0.5f, 0.5f);
    private SupportMapFragment mapFragment;
    private String DISCOVER_FRAGMENT = "DISCOVER_FRAGMENT";
    private Tracker mTracker;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;


    public DiscoverFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = AppController.getInstance().getDefaultTracker();
        if (getActivity() != null)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mLocation = location;
                        CurrentLocationDetails.setCurrentLocation(mLocation);
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        UserUtils.saveLastLocationLat(mMap,Double.toString(lat));
                        UserUtils.saveLastLocationLng(mMap,Double.toString(lng));
                        if (firstTime) {
                            firstTime = false;
                            if (googleMap != null && mLocation != null) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 12));
                                googleMap.animateCamera(CameraUpdateFactory.zoomIn());
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                            }
                        }
                    }

                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getExtendedMapAsync(this);
        mMap = getActivity();
        mSearchMarkers = (EditText) view.findViewById(R.id.search_markers);
        company_layout = (LinearLayout) view.findViewById(R.id.row_company_layout);
        company_layout.setVisibility(View.GONE);
        TextView name = (TextView) view.findViewById(R.id.row_company_name);
        TextView address = (TextView) view.findViewById(R.id.row_company_city);
        TextView mSignIn = (TextView) view.findViewById(R.id.toolbar_sign_in);
        TextView mSignUp = (TextView) view.findViewById(R.id.toolbar_sign_up);
        ImageView companyImage = (ImageView) view.findViewById(R.id.row_company_icon);
        ImageView gps = (ImageView) view.findViewById(R.id.gps);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mMap));

        OnItemTouchListener itemTouchListener = (view1, position, item) -> {
            if (item != null && googleMap != null) {
                mRecyclerView.setVisibility(View.GONE);
                mSearchMarkers.setText("");
                hideKeyboard();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(item.getPosition(),18.0f));
            }
        };

        adapter = new MarkerFilterRecyclerViewAdapter(mUserSearchModelLists, mMap, itemTouchListener);
        mRecyclerView.setAdapter(adapter);
        mLocation = CurrentLocationDetails.getCurrentLocation();
        company_layout.setOnClickListener(v -> {
            company_layout.setVisibility(View.GONE);
            if (CheckNetworkConnection.isConnectionAvailable(mMap)) {
                Intent myIntent = new Intent(mMap, CompanyProfileActivity.class);
                if (mMarkerID != null && !mMarkerID.equalsIgnoreCase(""))
                    myIntent.putExtra("companyId", mMarkerID);
                mMap.startActivity(myIntent);
                if (CheckOsVersion.isPreLollipop()) {
                    if (mMap != null)
                        ((Activity)mMap).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            } else {
                if (company_layout != null)
                    SnackBarDialog.show(company_layout, "No internet connection available.");
            }
        });

        mSearchMarkers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRecyclerView.setVisibility(View.VISIBLE);
                if (s.length() > 0) {
                    final List<MyItem> filteredModelList = filter(mUserSearchModel, s);
                    adapter.setFilter(filteredModelList);
                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    if (company_layout != null)
                        company_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mSearchMarkers.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus){
                v.setFocusable(false);
                hideKeyboard();
            }
        });
        initializeMap();
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if (intent.getAction().equalsIgnoreCase(Config.NOTIFICATION)) {
                        int count = Integer.parseInt(UserUtils.parsingInteger(intent.getStringExtra("notificationCount")));
                        UserUtils.setNotificationCount(mMap, String.valueOf(count));
                        HomeActivity.notification();
                    } else if (intent.getAction().equalsIgnoreCase(Config.FRIEND_REQUEST_NOTIFICATION)){
                        int count = Integer.parseInt(UserUtils.parsingInteger(intent.getStringExtra("friendNotificationCount")));
                        UserUtils.setFRNotificationCount(mMap, String.valueOf(count));
                        HomeActivity.notification();
                    } else if (intent.getAction().equalsIgnoreCase(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST)) {
                        setUpClustering();
                    } else if(intent.getAction().equalsIgnoreCase(DISCOVER_FRAGMENT)){
                        onMyLocationButtonClick();
                    }
                }
            }
        };
        return view;
    }

    private void resetMyPositionButton() {
        if(mapFragment != null){
            View mMapView = mapFragment.getView();
            if(mMapView != null){
                View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                if(locationButton != null){
                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    rlp.setMargins(0, 0, 30, 30);
                }

            }
        }
    }


    private List<MyItem> filter(List<MyItem> models, CharSequence query) {
        final List<MyItem> filteredModelList = new ArrayList<>();
        final ArrayList<MyItem> filteredModelListCopy = new ArrayList<>();
        if(models != null) {
            filteredModelListCopy.addAll(models);
            query = query.toString().toLowerCase();
            if (query.length() > 0) {
                for (MyItem model : models) {
                    final String text = model.getCompanyName() != null ? model.getCompanyName().toLowerCase() : "";
                    final String textCity = model.getCompanyAddress() != null ? model.getCompanyAddress().toLowerCase() : "";
                    final String textCountry = model.getCompanyCountry() != null ? model.getCompanyCountry().toLowerCase() : "";
                    if (text.startsWith(String.valueOf(query)) || textCity.contains(String.valueOf(query))|| textCountry.contains(String.valueOf(query))) {
                        filteredModelList.add(model);
                    }
                }
            } else {
                filteredModelListCopy.clear();
            }
        }
        return filteredModelList;
    }

    private void hideKeyboard() {
        if(getActivity() != null){
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mSearchMarkers.getWindowToken(), 0);
            }
        }

    }

    private void initializeMap() {
        buildLocationSettingsRequest();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mMap != null) {
                if (checkLocationPermission())
                    checkLocationSettings();
            }
        } else {
            checkLocationSettings();
        }
    }

    @Override
    public void onMapReady(com.androidmapsextensions.GoogleMap map) {
        if (map != null) {
            googleMap = map;
            markerSpider = new MarkerSpider(mMap, googleMap);
            if (mMyScrapSQLiteDatabase == null)
                mMyScrapSQLiteDatabase = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
            markerList = new ArrayList<>();
            markerList = mMyScrapSQLiteDatabase.getMarkerList();
            updateClusteringRadius();
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.setMyLocationEnabled(true);
            setUpClustering();
            googleMap.setOnMapClickListener(position -> {
                if (isCompanyLayoutVisible) {
                    isCompanyLayoutVisible = false;
                    Animation bottomDown = AnimationUtils.loadAnimation(mMap, R.anim.bottom_down);
                    company_layout.startAnimation(bottomDown);
                    company_layout.setVisibility(View.GONE);
                }
            });

            googleMap.setOnMarkerClickListener(marker -> {
                if (marker != null) {
                    if (marker.isCluster()) {
                        if (googleMap.getCameraPosition().zoom >= 15)
                            markerSpider.spiderListener(marker);
                        else {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), googleMap.getCameraPosition().zoom + dynamicZoomLevel()));
                            updateClusteringRadius();
                        }
                        return true;
                    } else {
                        googleMap.setOnInfoWindowClickListener(marker1 -> {
                            if (CheckNetworkConnection.isConnectionAvailable(mMap)) {
                                MyItem item = marker1.getData();
                                mMarkerID = item.getMarkerId();
                                Intent myIntent = new Intent(mMap, CompanyProfileActivity.class);
                                if (mMarkerID != null && !mMarkerID.equalsIgnoreCase(""))
                                    myIntent.putExtra("companyId", mMarkerID);
                                mMap.startActivity(myIntent);
                                if (CheckOsVersion.isPreLollipop()) {
                                    if (mMap != null)
                                        ((Activity)mMap).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                }
                            } else {
                                if (mRecyclerView != null)
                                    SnackBarDialog.showNoInternetError(mRecyclerView);
                            }
                        });

                    }
                }
                return false;
            });

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(final Marker marker) {
                    Context context = getContext();
                    LinearLayout info = new LinearLayout(context);
                    info.setOrientation(LinearLayout.VERTICAL);
                    TextView title = new TextView(context);
                    title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());
                    info.addView(title);
                    info.setOnClickListener(v -> Toast.makeText(getContext(), marker.getTitle(),
                            Toast.LENGTH_SHORT).show());
                    return info;
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(mMap, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (mGoogleApiClient == null)
                        buildGoogleApiClient();
                    googleMap.setMyLocationEnabled(true);
                }
            } else {
                if (mGoogleApiClient == null)
                    buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    private float dynamicZoomLevel() {
        float currZoomLvl = googleMap.getCameraPosition().zoom;
        final float minZoomStepAtZoom = 17.3F, minZoomStep = 1.8F;
        final float maxZoomStepAtZoom = 7F, maxZoomStep = 2.8F;

        if (currZoomLvl >= minZoomStepAtZoom)
            return minZoomStep;
        else if (currZoomLvl <= maxZoomStepAtZoom)
            return maxZoomStep;
        else
            return (currZoomLvl - maxZoomStepAtZoom)
                    * (maxZoomStep - minZoomStep)
                    / (maxZoomStepAtZoom - minZoomStepAtZoom) + maxZoomStep;
    }

    private int clusterRadiusCalculation() {
        final int minRad = 0, maxRad = 150;
        final float minRadZoom = 10F, maxRadZoom = 7.333F;

        if (googleMap.getCameraPosition().zoom >= minRadZoom) {

            return minRad;

        } else if (googleMap.getCameraPosition().zoom <= maxRadZoom)
            return maxRad;
        else
            return (int) (maxRad - (maxRadZoom - googleMap.getCameraPosition().zoom) *
                    (maxRad - minRad) / (maxRadZoom - minRadZoom));
    }

    private void updateClusteringRadius() {
        if (clusterSettings == null) {
            clusterSettings = new ClusteringSettings();
            clusterSettings.addMarkersDynamically(true);
            clusterSettings.clusterSize(clusterRadiusCalculation());
            baseBitmaps = new Bitmap[res.length];
            for (int i = 0; i < res.length; i++) {
                baseBitmaps[i] = BitmapFactory.decodeResource(getResources(), res[i]);
            }
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(24f);
            ClusterOptionsProvider provider = markers -> {
                int markersCount = markers.size();
                BitmapDescriptor cachedIcon = cache.get(0);
                if (cachedIcon != null) {
                    return clusterOptions.icon(cachedIcon);
                }
                Bitmap base;
                base = baseBitmaps[0];
                Bitmap bitmap = base.copy(Bitmap.Config.ARGB_8888, true);
                String text = String.valueOf(markersCount);
                paint.getTextBounds(text, 0, text.length(), bounds);
                float x = bitmap.getWidth() / 2.0f;
                float y = (bitmap.getHeight() - bounds.height()) / 2.0f - bounds.top;
                Canvas canvas = new Canvas(bitmap);
                canvas.drawText(text, x, y, paint);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
                cache.put(markersCount, icon);
                return clusterOptions.icon(icon);                };
            googleMap.setClustering(clusterSettings.clusterOptionsProvider(provider));
        } else {
            clusterSettings.clusterSize(clusterRadiusCalculation());
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if (mMap != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mMap).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            mGoogleApiClient.connect();
        }
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
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
                onResume();
                startUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i("Map", "location settings are not satisfied. Show the user a dialog to" + "upgrade location settings ");
                try {
                    status.startResolutionForResult(((Activity)mMap), REQUEST_CHECK_SETTINGS);
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
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            if (ContextCompat.checkSelfPermission(mMap, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                if (mFusedLocationClient != null) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback,
                            null /* Looper */);
                }
            }
            if (mGoogleApiClient != null && mLocationSettingsRequest != null ) {
                if (!UserUtils.isAlreadyLocationAllowed(mMap)) {
                    PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
                    result.setResultCallback(this);
                }
            }
        } else {
            if (mGoogleApiClient == null)
                buildGoogleApiClient();
        }
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
            CurrentLocationDetails.setCurrentLocation(mLocation);
            mLocation = location;
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            UserUtils.saveLastLocationLat(mMap,Double.toString(lat));
            UserUtils.saveLastLocationLng(mMap,Double.toString(lng));
            if (firstTime) {
                firstTime = false;
                if (googleMap != null && mLocation != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 12));
                    googleMap.animateCamera(CameraUpdateFactory.zoomIn());
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                }
            }
        }
    }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(mMap, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(mMap, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                    ActivityCompat.requestPermissions(((Activity)mMap), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideKeyboard();
        company_layout.setVisibility(View.GONE);
        if(mTracker != null){
            mTracker.setScreenName("Discover Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(DISCOVER_FRAGMENT));
        LocalBroadcastManager.getInstance(mMap).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
        LocalBroadcastManager.getInstance(mMap).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.NOTIFICATION));
        LocalBroadcastManager.getInstance(mMap).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(Config.FRIEND_REQUEST_NOTIFICATION));
        LocalBroadcastManager.getInstance(mMap).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                // mFusedLocationClient.removeLocationUpdates(mGoogleApiClient, this);
            }
        }
        LocalBroadcastManager.getInstance(mMap).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        buildGoogleApiClient();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private void setUpClustering() {
        if (googleMap != null ) {
            googleMap.clear();
        }
        addClusterMarkers();
    }

    private void addClusterMarkers() {
        mUserSearchModelLists = new ArrayList<>();
        if(markerList == null)
            markerList = new ArrayList<>();

        if(mMyScrapSQLiteDatabase == null)
            mMyScrapSQLiteDatabase = new MyScrapSQLiteDatabase(mMap);

        markerList = mMyScrapSQLiteDatabase.getMarkerList();

        if (markerList != null && markerList.size() != 0) {
            if (mMap != null) {
                for (int i =0; i < markerList.size(); i++) {
                    String companyName = markerList.get(i).getCompanyName();
                    String companyType = markerList.get(i).getCompanyType();
                    String isNew = markerList.get(i).getIsNew();
                    String companyAddress = markerList.get(i).getCompanyAddress();
                    String companyCountry = markerList.get(i).getCompanyCountry();
                    String companyImage= markerList.get(i).getCompanyImage();
                    String lat = String.valueOf(markerList.get(i).getLatitude());
                    String lng = String.valueOf(markerList.get(i).getLongitude());
                    String markerId = markerList.get(i).getMarkerId();
                    if (!lat.equalsIgnoreCase("") && !lng.equalsIgnoreCase("")) {
                        double offsetItemLatitude = Double.parseDouble(lat);
                        double offsetItemLongitude = Double.parseDouble(lng);
                        MyItem offsetItem = new MyItem(offsetItemLatitude, offsetItemLongitude,companyName, companyType, isNew, companyAddress,companyCountry,companyImage,markerId);
                        mUserSearchModelLists.add(offsetItem);
                        plotMarkers(googleMap,offsetItem,getBitMap());
                    }
                }
                mUserSearchModel = new ArrayList<>();
                for (MyItem countryCode : mUserSearchModelLists) {
                    mUserSearchModel.add(new MyItem(countryCode.getPosition(),countryCode.getCompanyName(), countryCode.getCompanyType(), countryCode.getIsNew(), countryCode.getCompanyAddress(),countryCode.getCompanyCountry(),countryCode.getMarkerId(),countryCode.getCompanyImage()));
                }
            }
        } else {
            ProgressBarDialog.showLoader(getActivity(), false);
            Intent serviceIntent = new Intent(AppController.getInstance(), MarkerListFetchService.class);
            getActivity().startService(serviceIntent);
            Log.e("serviceIntent", "started" + System.currentTimeMillis());
        }
    }

    private BitmapDescriptor getBitMap() {
        BitmapDescriptor cachedIcon = cachePin.get(0);
        if (cachedIcon != null) {
            return cachedIcon;
        }
        Bitmap[] baseBitmapPin = new Bitmap[resPin.length];
        for (int i = 0; i < resPin.length; i++) {
            baseBitmapPin[i] = BitmapFactory.decodeResource(getResources(), resPin[i]);
        }
        Bitmap base;
        base = baseBitmapPin[0];
        Bitmap bitmap = base.copy(Bitmap.Config.ARGB_8888, true);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        cachePin.put(0, icon);
        return icon;
    }


    private Marker plotMarkers(GoogleMap map, MyItem offsetItem, BitmapDescriptor bitmap) {
        com.androidmapsextensions.MarkerOptions options = new com.androidmapsextensions.MarkerOptions();
        return map.addMarker(options.position(new LatLng(offsetItem.getLatitude(),offsetItem.getLongitude())).title(offsetItem.getCompanyName()).data(offsetItem).icon(bitmap).clusterGroup(ClusterGroup.FIRST_USER));
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if(mGoogleApiClient != null){
            startUpdates();
        }

        if (mLocation != null && googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 12));
        }
        return true;
    }

    private void startUpdates() {
        if (ContextCompat.checkSelfPermission(mMap, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mFusedLocationClient != null) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback,
                        null /* Looper */);
            }
        }
    }

}
