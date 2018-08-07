package com.myscrap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.webservice.CheckNetworkConnection;

public class TermsAndConditionsActivity extends AppCompatActivity {

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTracker = AppController.getInstance().getDefaultTracker();
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        final TextView support = (TextView)findViewById(R.id.support);
        support.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                goToSupportProfile();
            } else {
                if(support != null){
                    SnackBarDialog.showNoInternetError(support);
                }
            }
        });
    }


    private void goToSupportProfile() {
        final Intent intent = new Intent(AppController.getInstance(), UserFriendProfileActivity.class);
        intent.putExtra("friendId", "32");
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
           this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Terms And Conditions Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
