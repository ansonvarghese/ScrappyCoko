package com.myscrap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.CompanyWorkingHoursAdapter;
import com.myscrap.application.AppController;

import java.util.ArrayList;

public class CompanyWorkingHoursActivity extends AppCompatActivity {

    public static ArrayList<Integer> mSelectedItems;
    public static String mWeekDaysString = "";
    public static String mWeekDaysSubString = "";
    public static String mWeekDaysOpenTime = "";
    public static String mWeekDaysCloseTime = "";
    private ArrayList<String> hoursDay = new ArrayList<>();
    private ArrayList<String> hoursOpen = new ArrayList<>();
    private ArrayList<String> hoursClose = new ArrayList<>();
    private ArrayList<String> hoursOpenCopy = new ArrayList<>();
    private ArrayList<String> hoursCloseCopy = new ArrayList<>();
    private CompanyWorkingHoursAdapter mCompanyWorkingHoursAdapter;
    private String daysString = "";
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_working_hours);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Context mContext = this;
        CompanyWorkingHoursActivity mCompanyWorkingHoursActivity = this;
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.hours_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mCompanyWorkingHoursAdapter);
        Intent intent = getIntent();
        String mCompanyId = intent.getExtras().getString("companyId");
        String mCompanyName = intent.getExtras().getString("companyName");
        String businessHours = intent.getExtras().getString("workingHours");
        mTracker = AppController.getInstance().getDefaultTracker();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Business Hours");
            if (mCompanyName != null && !mCompanyName.equalsIgnoreCase("") && !mCompanyName.equalsIgnoreCase("null")){
                getSupportActionBar().setSubtitle(mCompanyName);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Company Working Hours Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

}
