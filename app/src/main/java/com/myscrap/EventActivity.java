package com.myscrap;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myscrap.utils.CheckOsVersion;

public class EventActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getIntent() != null) {
            eventId = getIntent().getStringExtra("scrollTo");
        }
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(1);
        mTabLayout.setupWithViewPager(mViewPager);
        setTabBold(0);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabBold(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                setTabNormal(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> goToEventCreate());
    }

    private void goToEventCreate() {
        Intent i = new Intent(this, EventCreateActivity.class);
        startActivity(i);
        if (CheckOsVersion.isPreLollipop()) {
            EventActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTabBold(int tabBold) {
        if(mTabLayout != null) {
            TextView boldTextView = (TextView)(((LinearLayout)((LinearLayout)mTabLayout.getChildAt(0)).getChildAt(tabBold)).getChildAt(1));
            if(boldTextView != null)
                boldTextView.setTypeface(null, Typeface.BOLD);
        }

    }

    public void setTabNormal(int tabBold) {
        if(mTabLayout != null) {
            TextView normalTextView = (TextView) (((LinearLayout) ((LinearLayout) mTabLayout.getChildAt(0)).getChildAt(tabBold)).getChildAt(1));
            if(normalTextView != null)
                normalTextView.setTypeface(null, Typeface.NORMAL);
        }
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        EventUpComingFragment mEventUpComingFragment;

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if(mEventUpComingFragment == null){
                        mEventUpComingFragment = new EventUpComingFragment();
                        Bundle args = new Bundle();
                        args.putString("eventId", eventId);
                        mEventUpComingFragment.setArguments(args);
                    }
                    return mEventUpComingFragment;
                case 1:
                    return new EventInvitationsFragment();
                default:
                    if(mEventUpComingFragment == null){
                        mEventUpComingFragment = new EventUpComingFragment();
                        Bundle args = new Bundle();
                        args.putString("eventId", eventId);
                        mEventUpComingFragment.setArguments(args);
                    }
                    return mEventUpComingFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)
                return "UPCOMING";
            else if(position==1)
                return "INVITATIONS";
            return "";
        }
    }



}
