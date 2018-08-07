package com.myscrap;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;

public class TipsActivity extends AppCompatActivity {

    private Tracker mTracker;
    private LinearLayout tips, generalInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTracker = AppController.getInstance().getDefaultTracker();
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        tips = (LinearLayout) findViewById(R.id.tips);
        generalInformation = (LinearLayout) findViewById(R.id.general_information);

        if(getIntent() != null) {
            String page = getIntent().getStringExtra("page");
            if(page != null && !page.equalsIgnoreCase("")){
                if(page.equalsIgnoreCase("tips")){
                    if(getSupportActionBar() != null){
                        getSupportActionBar().setTitle("Tips");
                    }
                    showTips();
                } else {
                    if(getSupportActionBar() != null){
                        getSupportActionBar().setTitle("General Information");
                    }
                    showGeneralInformation();
                }
            }
        }

    }

    private void showGeneralInformation() {
        tips.setVisibility(View.GONE);
        generalInformation.setVisibility(View.VISIBLE);
    }

    private void showTips() {
        tips.setVisibility(View.VISIBLE);
        generalInformation.setVisibility(View.GONE);
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
            mTracker.setScreenName("Tips Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
