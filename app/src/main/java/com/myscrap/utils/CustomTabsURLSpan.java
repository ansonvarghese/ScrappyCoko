package com.myscrap.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Build;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.text.style.URLSpan;
import android.view.View;

import com.myscrap.R;

/**
 * Created by Ms2 on 8/7/2016.
 */
@SuppressLint("ParcelCreator")
public class CustomTabsURLSpan  extends URLSpan{
    private CustomTabsClient mCustomTabsClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsIntent.Builder mCustomTabsIntent;
    private Activity mHomeActivity ;

    CustomTabsURLSpan(String url, Activity homeActivity) {
        super(url);
        this.mHomeActivity = homeActivity;
        CustomTabsServiceConnection mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient = null;
            }
        };

        //String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";
        //CustomTabsClient.bindCustomTabsService(homeActivity, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);
       /* mCustomTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setShowTitle(true)
                .build();*/
        mCustomTabsIntent = new CustomTabsIntent.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCustomTabsIntent.setToolbarColor(mHomeActivity.getResources().getColor(R.color.colorPrimary, mHomeActivity.getTheme()));
        } else {
            mCustomTabsIntent.setToolbarColor(mHomeActivity.getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        if(mHomeActivity != null && mCustomTabsIntent != null && url != null && !url.equalsIgnoreCase("")){
            CustomTabsIntent customTabsIntent = mCustomTabsIntent.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(mHomeActivity, Uri.parse(url));
        }
    }
}
