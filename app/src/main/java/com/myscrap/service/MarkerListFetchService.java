package com.myscrap.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.Markers;
import com.myscrap.model.MyItem;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ms2 on 10/6/2016.
 */

public class MarkerListFetchService extends IntentService {


    public static final String BROADCAST_ACTION_MARKER_LIST = "marker_list_updater";
    public static final String BROADCAST_ACTION_MARKER_LIST_STARTS_DOWNLOAD = "updating";
    public static final String BROADCAST_ACTION_MARKER_LIST_ENDS_DOWNLOAD = "failed";
    private Intent intent;
    private MyScrapSQLiteDatabase mMyScrapSQLiteDatabase;


    public MarkerListFetchService() {
        super("MarkerListFetchService");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION_MARKER_LIST);
        if(mMyScrapSQLiteDatabase == null)
            mMyScrapSQLiteDatabase = new MyScrapSQLiteDatabase(AppController.getInstance());


    }


    private void getMarkerList()
    {
        if (CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
            ProgressBarDialog.showLoader(getApplicationContext(), false);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(getApplicationContext());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST_STARTS_DOWNLOAD));
            Call<Markers> call = apiService.discover(apiKey);
            call.enqueue(new Callback<Markers>() {
                @Override
                public void onResponse(@NonNull Call<Markers> call, @NonNull Response<Markers> response) {
                    if (response.body() != null) {
                        Markers markers = response.body();
                        if(markers != null) {
                            if(!markers.isErrorStatus()) {
                                List<Markers.MarkerData> data = markers.getData();
                                if(data != null && data.size() > 0){
                                    if(mMyScrapSQLiteDatabase == null)
                                        mMyScrapSQLiteDatabase = new MyScrapSQLiteDatabase(getApplicationContext());
                                    mMyScrapSQLiteDatabase.deleteMarkerList();

                                    SQLiteDatabase mSQLiteDatabase = mMyScrapSQLiteDatabase.getWritableDatabase();
                                    if (mSQLiteDatabase != null){
                                        mSQLiteDatabase.beginTransaction();
                                    }
                                    for(Markers.MarkerData marker  : data){
                                        if (!marker.getLatitude().equalsIgnoreCase("") && !marker.getLongitude().equalsIgnoreCase("")) {
                                            MyItem offsetItem = new MyItem(Double.parseDouble(marker.getLatitude()), Double.parseDouble(marker.getLongitude()),marker.getName(), marker.getCompanyType(), marker.getIsNew(), marker.getState(),marker.getCountry(),marker.getImage(),marker.getId());
                                            mMyScrapSQLiteDatabase.addMarker(offsetItem);
                                        }
                                    }
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST));
                                    if (mSQLiteDatabase != null){
                                        mSQLiteDatabase.setTransactionSuccessful();
                                        mSQLiteDatabase.endTransaction();
                                        mSQLiteDatabase.close();
                                    }

                                }
                            }
                        }
                        Log.d("MarkerList", "onSuccess");
                    }
                    ProgressBarDialog.dismissLoader();
                }
                @Override
                public void onFailure(@NonNull Call<Markers> call, @NonNull Throwable t) {
                    Log.d("MarkerList", "onFailure");
                    ProgressBarDialog.dismissLoader();
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(MarkerListFetchService.BROADCAST_ACTION_MARKER_LIST_ENDS_DOWNLOAD));

                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            if(mMyScrapSQLiteDatabase == null)
                mMyScrapSQLiteDatabase = new MyScrapSQLiteDatabase(getApplicationContext());

            new Handler().post(this::getMarkerList);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
