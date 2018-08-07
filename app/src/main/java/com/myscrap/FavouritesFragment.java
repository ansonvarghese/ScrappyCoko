package com.myscrap;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;

public class FavouritesFragment extends Fragment {

    private TabLayout tabLayout;
    private Tracker mTracker;

    public FavouritesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mFavouritesFragment = inflater.inflate(R.layout.fragment_favourites, container, false);
        ViewPager viewPager = (ViewPager) mFavouritesFragment.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) mFavouritesFragment.findViewById(R.id.tabs);
        if (getActivity() != null) {
            viewPager.setAdapter(new ViewPagerAdapter(getActivity().getSupportFragmentManager()));
            viewPager.setOffscreenPageLimit(3);
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

        return mFavouritesFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Favourite Fragments Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ContactFavourites();
                case 1:
                    return new FavouriteCompanyFragment();
                case 2:
                    return new ContactFavouritePosts();
                case 3:
                    return new ContactModerators();
                default:
                    return new ContactFavourites();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)
                return "Members";
            else if(position==1)
                return "Companies";
            else if(position==2)
                return "Posts";
            else if(position==3)
                return "Moderators";
            return "";
        }
    }

}
