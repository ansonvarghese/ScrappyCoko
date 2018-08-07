package com.myscrap;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.NewsFragmentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.News;

import java.util.ArrayList;
import java.util.List;


public class NewsFragment extends Fragment implements NewsFragmentAdapter.NewsFragmentAdapterListener{

    private List<News.NewsData> mNewsDataList = new ArrayList<>();

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Tracker mTracker;

    public NewsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mNewsFragment = inflater.inflate(R.layout.fragment_news, container, false);
        viewPager = (ViewPager) mNewsFragment.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) mNewsFragment.findViewById(R.id.tabs);
        setupViewpager();
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
        return mNewsFragment;
    }

    public void setupViewpager(){
        if(viewPager != null && getActivity() != null){
            viewPager.setAdapter(new ViewPagerAdapter(getActivity().getSupportFragmentManager()));
            tabLayout.setupWithViewPager(viewPager);
            viewPager.setOffscreenPageLimit(1);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        }

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
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("News Fragment Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAdapterClicked(int position) {
        if(mNewsDataList != null && mNewsDataList.size() > 0){
            News.NewsData mNewsData = mNewsDataList.get(position);
            if(mNewsData != null) {
                Intent i = new Intent(getActivity(), NewsViewActivity.class);
                i.putExtra("newsId", mNewsData.getPostId());
                startActivity(i);
            }
        }
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new NewsFragmentOne();
                case 1:
                    return new NewsFragmentTwo();
                /*case 2:
                    return new NewsFragmentThree();*/
                default:
                    return new NewsFragmentOne();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)
                return "Waste & Recycling";
                //return "Recycling Middle-East";
            /*else if(position==1)
                return "Recycling International";*/
            else if(position==1)
                return "Recycling Today";
            return "";
        }
    }


}
