package com.myscrap;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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
import com.myscrap.model.CompanyProfile;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.view.FlowLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompanyInterestActivity extends AppCompatActivity {


    private LinearLayout phoneLayout;
    private TextView phoneNumber;
    private LinearLayout webAddressLayout;
    private TextView website;
    private LinearLayout locationLayout;
    private TextView location;
    private TextView bio;
    private LinearLayout bioLayout;

    private LinearLayout aboutLayout;
    private LinearLayout tabbing;
    private CompanyInterestActivity mCompanyInterestActivity;
    private List<String> mInterestData = new ArrayList<>();
    private List<String> mInterestRoleData = new ArrayList<>();
    private List<String> mInterestAffiliationData = new ArrayList<>();
    private LinearLayout interestLayoutChild;
    private String companyLocation;
    private boolean isMyCompany;
    private String companyId;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_interest);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCompanyInterestActivity = this;
        aboutLayout = (LinearLayout) findViewById(R.id.about_layout);
        tabbing = (LinearLayout) findViewById(R.id.tabbing);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        FlowLayout flowLayout = new FlowLayout(this);
        FlowLayout flowRolesLayout = new FlowLayout(this);
        int padding= DeviceUtils.dp2px(this,13);
        flowLayout.setPadding(padding,padding,padding,padding);
        flowRolesLayout.setPadding(padding,padding,padding,padding);
        phoneLayout = (LinearLayout) findViewById(R.id.phoneLayout);
        phoneNumber = (TextView) findViewById(R.id.number);
        webAddressLayout = (LinearLayout) findViewById(R.id.webAddressLayout);
        website = (TextView) findViewById(R.id.website);
        locationLayout = (LinearLayout) findViewById(R.id.locationLayout);
        location = (TextView) findViewById(R.id.location);
        bioLayout = (LinearLayout) findViewById(R.id.bioLayout);
        bio = (TextView) findViewById(R.id.bio);
        interestLayoutChild = (LinearLayout) findViewById(R.id.interestLayout);
        LinearLayout interestRolesLayout = (LinearLayout) findViewById(R.id.interestRolesLayout);
        LinearLayout interest = (LinearLayout) findViewById(R.id.interest);
        LinearLayout interestRoles = (LinearLayout) findViewById(R.id.role);

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
        mInterestRoleData.add("Indentor");
        mInterestRoleData.add("Recycler");
        mInterestRoleData.add("Exporter");
        mInterestRoleData.add("Stocker");
        mInterestRoleData.add("Equipment");
        mInterestRoleData.add("Service");
        mInterestRoleData.add("Consumer");
        mInterestRoleData.add("Consultant");
        mInterestRoleData.add("Importer");
        mInterestRoleData.add("Press");
        mInterestRoleData.add("Supplier");
        mInterestRoleData.add("Media");
        mInterestRoleData.add("Others");

        mInterestAffiliationData.add("BIR");
        mInterestAffiliationData.add("ISRI");
        mInterestAffiliationData.add("BMR");
        mInterestAffiliationData.add("CMRA");
        mInterestAffiliationData.add("MRAI");

        mTracker = AppController.getInstance().getDefaultTracker();
        Intent mIntent = getIntent();
        if(mIntent != null) {
            String pageName = mIntent.getStringExtra("pageName");
            if(getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                if(pageName != null && !pageName.equalsIgnoreCase("")){
                    getSupportActionBar().setTitle(pageName);
                }
            }
            Gson gson = new Gson();
            CompanyProfile.CompanyData data = gson.fromJson(getIntent().getStringExtra("companyData"), CompanyProfile.CompanyData.class);
            if(data != null){
                initValues(data, pageName);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (isMyCompany) {
            inflater.inflate(R.menu.user_profile, menu);
            MenuItem item = menu.findItem(R.id.action_edit);
            item.setVisible(true);
            MenuItem itemPassword = menu.findItem(R.id.action_change_password);
            itemPassword.setVisible(false);
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
        }else if(item.getItemId() == R.id.action_edit){
            if(companyId != null && !companyId.equalsIgnoreCase(""))
                goToEditProfile(companyId);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initValues(final CompanyProfile.CompanyData data, String pageName) {
        if (data != null) {
            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;

            if(data.getOwnerUserId() != null && !data.getOwnerUserId().equalsIgnoreCase("") && !data.getOwnerUserId().equalsIgnoreCase("0") && data.getOwnerUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                isMyCompany =true;

                invalidateOptionsMenu();
            }
            companyId = data.getCompanyId();
            if(!pageName.equalsIgnoreCase("") && pageName.equalsIgnoreCase("Company Interests")){
                tabbing.setVisibility(View.VISIBLE);
                viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), data.getCompanyInterests(), data.getUserInterestRoles(), data.getCompanyAffiliations() ));
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
            } else {
                tabbing.setVisibility(View.GONE);
                if(data.getCompanyPhoneNumber() != null && !data.getCompanyPhoneNumber().equalsIgnoreCase("")){
                    phoneNumber.setText(data.getCompanyPhoneNumber());
                    phoneLayout.setVisibility(View.VISIBLE);
                    phoneNumber.setOnClickListener(v -> {
                        Uri number = Uri.parse("tel:"+data.getCompanyPhoneNumber());
                        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                        startActivity(callIntent);
                    });
                    phoneLayout.setOnClickListener(v -> {
                        Uri number = Uri.parse("tel:"+data.getCompanyPhoneNumber());
                        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                        startActivity(callIntent);
                    });
                } else {
                    phoneLayout.setVisibility(View.GONE);
                }

                if(data.getCompanyWebsite() != null && !data.getCompanyWebsite().equalsIgnoreCase("")){
                    website.setText(data.getCompanyWebsite());
                    website.setOnClickListener(v -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            builder.setToolbarColor(getResources().getColor(R.color.colorPrimary, getTheme()));
                        } else {
                            builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                        }
                        builder.setStartAnimations(mCompanyInterestActivity, R.anim.slide_in_right, R.anim.slide_out_left);
                        builder.setExitAnimations(mCompanyInterestActivity, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        Uri webPage = Uri.parse(data.getCompanyWebsite());

                        if (!data.getCompanyWebsite().startsWith("http://") && !data.getCompanyWebsite().startsWith("https://")) {
                            webPage = Uri.parse("http://" + data.getCompanyWebsite());
                        }

                        customTabsIntent.launchUrl(mCompanyInterestActivity, webPage);
                    });
                    webAddressLayout.setVisibility(View.VISIBLE);
                } else {
                    webAddressLayout.setVisibility(View.GONE);
                }


                if(data.getCompanyBio() != null && !data.getCompanyBio().equalsIgnoreCase("")){
                    bio.setText(data.getCompanyBio());
                    bioLayout.setVisibility(View.VISIBLE);
                } else {
                    bioLayout.setVisibility(View.GONE);
                }

                if(data.getCompanyAddress() != null && !data.getCompanyAddress().equalsIgnoreCase("") && data.getCompanyCountry() != null && !data.getCompanyCountry().equalsIgnoreCase("")){
                    companyLocation = data.getCompanyAddress()+ ", " + data.getCompanyCountry();
                    location.setText(companyLocation);
                    locationLayout.setVisibility(View.VISIBLE);
                } else if(data.getCompanyAddress() != null && !data.getCompanyAddress().equalsIgnoreCase("") && data.getCompanyCountry() != null && data.getCompanyCountry().equalsIgnoreCase("")){
                    companyLocation = data.getCompanyAddress();
                    location.setText(companyLocation);
                    locationLayout.setVisibility(View.VISIBLE);
                } else if(data.getCompanyAddress() != null && data.getCompanyAddress().equalsIgnoreCase("") && data.getCompanyCountry() != null && !data.getCompanyCountry().equalsIgnoreCase("")){
                    companyLocation = data.getCompanyCountry();
                    location.setText(companyLocation);
                    locationLayout.setVisibility(View.VISIBLE);
                } else {
                    locationLayout.setVisibility(View.GONE);
                }


                if(locationLayout != null) {
                    locationLayout.setOnClickListener(v -> {
                        if (companyLocation != null && !companyLocation.equalsIgnoreCase("") && !companyLocation.startsWith("null")) {
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q="+companyLocation);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                    });
                }
                if(location != null) {
                    location.setOnClickListener(v -> {
                        if (companyLocation != null && !companyLocation.equalsIgnoreCase("") && !companyLocation.startsWith("null")) {
                            Uri gmmIntentUri = Uri.parse("geo:0,0?q="+companyLocation);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        }
                    });
                }

                aboutLayout.setVisibility(View.VISIBLE);
                interestLayoutChild.setVisibility(View.GONE);
            }
        }
    }


    private void goToEditProfile(String companyId) {
        Intent intent = new Intent(mCompanyInterestActivity, CompanyEditProfileActivity.class);
        intent.putExtra("companyId", companyId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(mCompanyInterestActivity != null)
                mCompanyInterestActivity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Company Interest Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(CompanyInterestActivity.this,UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(CompanyInterestActivity.this,UserOnlineStatus.OFFLINE);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        List<String> selectedList = new ArrayList<>();
        List<String> selectedRolesList = new ArrayList<>();
        List<String> selectedAffiliationList = new ArrayList<>();
        public ViewPagerAdapter(FragmentManager manager, String userInterest, String userInterestRoles, String companyAffiliations) {
            super(manager);
            if(userInterest == null || userInterest.equalsIgnoreCase(""))
                userInterest = "0";
            selectedList = Arrays.asList(userInterest.trim().split(","));

            if(userInterestRoles == null || userInterestRoles.equalsIgnoreCase(""))
                userInterestRoles = "0";
            selectedRolesList = Arrays.asList(userInterestRoles.trim().split(","));

            if(companyAffiliations == null || companyAffiliations.equalsIgnoreCase(""))
                companyAffiliations = "0";
             selectedAffiliationList = Arrays.asList(companyAffiliations.trim().split(","));
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Commodity(selectedList, mInterestData);
                case 1:
                    return new Roles(selectedRolesList, mInterestRoleData);
                case 2:
                    return new Affiliation(selectedAffiliationList, mInterestAffiliationData);
                default:
                    return new Commodity(selectedList, mInterestData);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)
                return "Commodities";
            else if(position==1)
                return "Industry";
            else if(position==2)
                return "Affiliation";
            else
                return "";
        }
    }

}
