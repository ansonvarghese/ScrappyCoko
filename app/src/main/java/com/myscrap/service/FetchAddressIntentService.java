package com.myscrap.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.myscrap.R;
import com.myscrap.webservice.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ms2 on 8/9/2016.
 */
public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "FetchAddressIS";
    private ResultReceiver mReceiver;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name) {
        super(name);
    }
    public FetchAddressIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        if (mReceiver == null) {
            Log.d(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        if (location == null) {
            errorMessage = getString(R.string.no_location_data_provided);
            Log.wtf(TAG, errorMessage);
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage, "", "", "");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage, "", "", "");
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments = new ArrayList<>();
                if (address.getFeatureName() != null && !address.getFeatureName().equalsIgnoreCase("null")&& !address.getFeatureName().equalsIgnoreCase(""))
                    addressFragments.add(address.getFeatureName());
                if (address.getAdminArea() != null && !address.getAdminArea().equalsIgnoreCase("null")&& !address.getAdminArea().equalsIgnoreCase(""))
                    addressFragments.add(address.getAdminArea());
                if (address.getCountryName() != null && !address.getCountryName().equalsIgnoreCase("null")&& !address.getCountryName().equalsIgnoreCase(""))
                    addressFragments.add(address.getCountryName());
            }
            Log.i(TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT, TextUtils.join(",", addressFragments),address.getFeatureName(),address.getSubAdminArea(),address.getCountryName());
        }
    }
    private void deliverResultToReceiver(int resultCode, String join, String featureName, String subAdminArea, String countryName) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, join);
        bundle.putString(Constants.LOCATION_DATA_FEATURE, featureName);
        bundle.putString(Constants.LOCATION_DATA_SUB_ADMIN, subAdminArea);
        bundle.putString(Constants.LOCATION_DATA_COUNTRY, countryName);
        mReceiver.send(resultCode, bundle);
    }
}
