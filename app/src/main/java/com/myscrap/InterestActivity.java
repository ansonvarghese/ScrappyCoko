package com.myscrap;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.myscrap.application.AppController;
import com.myscrap.model.UserFriendProfile;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.FlowLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class InterestActivity extends AppCompatActivity {

    private LinearLayout phoneLayout;
    private TextView phoneNumber;
    private LinearLayout webAddressLayout;
    private TextView website;
    private LinearLayout locationLayout;
    private TextView location;
    private TextView bio;
    private LinearLayout bioLayout;
    private TextView join;
    private LinearLayout joinedLayout;

    private LinearLayout emptyLayout;
    private List<String> mInterestData = new ArrayList<>();
    private List<String> mInterestRoleData = new ArrayList<>();
    private Context mInterestActivity;
    private boolean isShowOption;
    private boolean isAboutHasValues;
    private LinearLayout tabbing;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mInterestActivity = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        FlowLayout flowLayout = new FlowLayout(this);
        FlowLayout flowRolesLayout = new FlowLayout(this);
        int padding= DeviceUtils.dp2px(this,13);
        flowLayout.setPadding(padding,padding,padding,padding);
        flowRolesLayout.setPadding(padding,padding,padding,padding);
        tabbing = (LinearLayout) findViewById(R.id.tabbing);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        LinearLayout aboutLayout = (LinearLayout) findViewById(R.id.aboutLayout);
        emptyLayout = (LinearLayout) findViewById(R.id.empty_layout);

        phoneLayout = (LinearLayout) findViewById(R.id.phoneLayout);
        phoneNumber = (TextView) findViewById(R.id.number);
        webAddressLayout = (LinearLayout) findViewById(R.id.webAddressLayout);
        website = (TextView) findViewById(R.id.website);
        locationLayout = (LinearLayout) findViewById(R.id.locationLayout);
        location = (TextView) findViewById(R.id.location);
        bioLayout = (LinearLayout) findViewById(R.id.bioLayout);
        joinedLayout = (LinearLayout) findViewById(R.id.joinedLayout);
        bio = (TextView) findViewById(R.id.bio);
        join = (TextView) findViewById(R.id.join);
        mInterestData.add("Non-Ferrous Metals");
        mInterestData.add("Ferrous Metals");
        mInterestData.add("Stainless Steel");
        mInterestData.add("Tyres");
        mInterestData.add("Paper");
        mInterestData.add("Textiles");
        mInterestData.add("Plastic");
        mInterestData.add("E-Scrap");
        mInterestData.add("Red Metals");
        mInterestData.add("Aluminum");
        mInterestData.add("Zinc");
        mInterestData.add("Magnesium");
        mInterestData.add("Lead");
        mInterestData.add("Nickel/Stainless/Hi Temp");
        mInterestData.add("Mixed Metals");
        mInterestData.add("Electric Furnace Casting and Foundry");
        mInterestData.add("Specially Processed Grades");
        mInterestData.add("Cast Iron Grades");
        mInterestData.add("Special Boring Grades");
        mInterestData.add("Steel From Scrap Tiles");
        mInterestData.add("Railroad Ferrous Scrap");
        mInterestData.add("Stainless Alloy");
        mInterestData.add("Special Alloy");
        mInterestData.add("Copper");
        mInterestData.add("Finance");
        mInterestData.add("Insurance");
        mInterestData.add("Shipping");
        mInterestData.add("Equipments");
        mInterestData.add("Others");

        mInterestRoleData.add("Trader");
        mInterestRoleData.add("Agent");
        mInterestRoleData.add("Recycler");
        mInterestRoleData.add("Exporter");
        mInterestRoleData.add("Stocker");
        mInterestRoleData.add("Equipment");
        mInterestRoleData.add("Service");
        mInterestRoleData.add("Consumer");
        mInterestRoleData.add("Consultant");
        mInterestRoleData.add("Press");
        mInterestRoleData.add("Importer");
        mInterestRoleData.add("Supplier");
        mInterestRoleData.add("Others");

        Intent mIntent = getIntent();
        if(mIntent != null) {
            Gson gson = new Gson();
            String pageName = mIntent.getStringExtra("pageName");
            if(mIntent.hasExtra("isShowOption")){
                isShowOption = mIntent.getBooleanExtra("isShowOption", false);
                invalidateOptionsMenu();
            }

            UserFriendProfile.UserFriendProfileData data = gson.fromJson(getIntent().getStringExtra("userData"), UserFriendProfile.UserFriendProfileData.class);
            if(data != null){
                if(pageName != null && pageName.equalsIgnoreCase("about")){
                    if(getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("About");
                    }
                    initValues(data, pageName);
                    if(aboutLayout != null)
                        aboutLayout.setVisibility(View.VISIBLE);
                } else  {
                    if(getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Interests");
                    }
                    initValues(data, pageName);
                    if(aboutLayout != null)
                        aboutLayout.setVisibility(View.GONE);
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(isShowOption) {
            inflater.inflate(R.menu.user_profile, menu);
        }
        return true;
    }


    public void setTabBold(int tabBold) {
        if(tabLayout != null) {
            TextView boldTextView = (TextView)(((LinearLayout)((LinearLayout)tabLayout.getChildAt(0)).getChildAt(tabBold)).getChildAt(1));
            if(boldTextView != null)
                boldTextView.setTypeface(null, Typeface.BOLD);
        }
    }

    public void setTabNormal(int tabBold) {
        if(tabLayout != null) {
            TextView normalTextView = (TextView) (((LinearLayout) ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(tabBold)).getChildAt(1));
            if(normalTextView != null)
                normalTextView.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        } else if(item.getItemId() == R.id.action_edit){
            goToEditProfile();
        }
        return super.onOptionsItemSelected(item);
    }


    private void goToEditProfile() {
        Intent intent = new Intent(mInterestActivity, EditProfileActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void initValues(final UserFriendProfile.UserFriendProfileData data, String pageName) {
        if(data != null){
            if(pageName != null && pageName.equalsIgnoreCase("about")){
                if(data.getPhoneNo() != null && !data.getPhoneNo().equalsIgnoreCase("")){
                    phoneNumber.setText(data.getPhoneNo());
                    phoneLayout.setVisibility(View.VISIBLE);
                    setTextViewDrawableColor(phoneNumber);
                    isAboutHasValues = true;
                    phoneNumber.setOnClickListener(v -> {
                        Uri number = Uri.parse("tel:"+data.getPhoneNo());
                        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                        startActivity(callIntent);
                    });
                    phoneLayout.setOnClickListener(v -> {
                        Uri number = Uri.parse("tel:"+data.getPhoneNo());
                        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                        startActivity(callIntent);
                    });
                } else {
                    phoneLayout.setVisibility(View.GONE);
                }

                if(data.getJoinedTime() != null && !data.getJoinedTime().equalsIgnoreCase("")){
                   long time = Long.parseLong(UserUtils.parsingLong(data.getJoinedTime()));
                    if (time < 1000000000000L) {
                        time *= 1000;
                    }
                    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    Calendar cal = Calendar.getInstance();
                    TimeZone tz = cal.getTimeZone();
                    mSimpleDateFormat.setTimeZone(tz);
                    String dateString = mSimpleDateFormat.format(time);
                    join.setText(dateString);
                    joinedLayout.setVisibility(View.VISIBLE);
                    isAboutHasValues = true;
                } else{
                    joinedLayout.setVisibility(View.GONE);
                }

                if(data.getWebsite() != null && !data.getWebsite().equalsIgnoreCase("")){
                    website.setText(data.getWebsite());
                    setTextViewDrawableColor(website);
                    website.setOnClickListener(v -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            builder.setToolbarColor(getResources().getColor(R.color.colorPrimary, getTheme()));
                        } else {
                            builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                        }
                        builder.setStartAnimations(mInterestActivity, R.anim.slide_in_right, R.anim.slide_out_left);
                        builder.setExitAnimations(mInterestActivity, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        Uri webPage = Uri.parse(data.getWebsite());

                        if (!data.getWebsite().startsWith("http://") && !data.getWebsite().startsWith("https://")) {
                            webPage = Uri.parse("http://" + data.getWebsite());
                        }

                        customTabsIntent.launchUrl(mInterestActivity, webPage);
                    });
                    webAddressLayout.setVisibility(View.VISIBLE);
                    isAboutHasValues = true;
                } else {
                    webAddressLayout.setVisibility(View.GONE);
                }


                if(data.getUserBio() != null && !data.getUserBio().equalsIgnoreCase("")){
                    bio.setText(data.getUserBio());
                    bioLayout.setVisibility(View.VISIBLE);
                    isAboutHasValues = true;
                } else {
                    bioLayout.setVisibility(View.GONE);
                }

                if(data.getUserLocation() != null && !data.getUserLocation().equalsIgnoreCase("")){
                    location.setText(data.getUserLocation());
                    locationLayout.setVisibility(View.VISIBLE);
                    isAboutHasValues = true;
                } else {
                    locationLayout.setVisibility(View.GONE);
                }

                if(!isAboutHasValues){
                    emptyLayout.setVisibility(View.VISIBLE);
                } else {
                    emptyLayout.setVisibility(View.GONE);
                }

                tabbing.setVisibility(View.GONE);

            } else {
                tabbing.setVisibility(View.VISIBLE);
                viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), data.getUserInterest(), data.getUserInterestRoles() ));
                tabLayout.setupWithViewPager(viewPager);
                setTabBold(0);
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
                viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            }
        }
    }


    private void setTextViewDrawableColor(TextView textView) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(mInterestActivity, R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Interest Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.ONLINE);
    }

    @Override
    public void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.OFFLINE);
    }


    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        List<String> selectedList = new ArrayList<>();
        List<String> selectedRolesList = new ArrayList<>();
        public ViewPagerAdapter(FragmentManager manager, String userInterest, String userInterestRoles) {
            super(manager);
            selectedList = Arrays.asList(userInterest.trim().split(","));
            selectedRolesList = Arrays.asList(userInterestRoles.trim().split(","));
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Commodity(selectedList, mInterestData);
                case 1:
                    return new Roles(selectedRolesList, mInterestRoleData);
                default:
                    return new Commodity(selectedList, mInterestData);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)
                return "Commodities";
            else if(position==1)
                return "Roles";
            else
            return "";
        }
    }


}
