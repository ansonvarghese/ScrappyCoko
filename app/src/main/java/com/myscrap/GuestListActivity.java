package com.myscrap;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myscrap.adapters.EventGoingAdapter;
import com.myscrap.adapters.EventInterestAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.EventInterestList;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GuestListActivity extends AppCompatActivity {


    private TabLayout mTabLayout;
    private static String eventId;
    private int tab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null) {
            eventId = getIntent().getStringExtra("eventId");
            tab = getIntent().getIntExtra("tab", 0);
        }

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(tab);
        setTabBold(tab);
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
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        EventInterestFragment mEventInterestFragment;
        EventGoingFragment mEventGoingFragment;

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if(mEventInterestFragment == null)
                        mEventInterestFragment = new EventInterestFragment();
                    return mEventInterestFragment;
                case 1:
                    if(mEventGoingFragment == null)
                        mEventGoingFragment = new EventGoingFragment();
                    return mEventGoingFragment;
                default:
                    if(mEventInterestFragment == null)
                        mEventInterestFragment = new EventInterestFragment();
                    return mEventInterestFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)
                return "INTERESTED";
            else if(position==1)
                return "GOING";
            return "";
        }
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

    public static class EventInterestFragment extends Fragment{

        private RecyclerView mRecyclerView;
        private SwipeRefreshLayout swipe;
        private View emptyView;
        private List<EventInterestList.EventInterestListData> mEventInterestList = new ArrayList<>();
        private EventInterestAdapter mEventInterestAdapter;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.event_interest, container, false);
            mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
            swipe = (SwipeRefreshLayout) v.findViewById(R.id.swipe);
            emptyView = v.findViewById(R.id.empty);
            UserUtils.setEmptyView(emptyView, R.drawable.ic_people_empty, "No Event Interest", false);
            swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
            swipe.setDistanceToTriggerSync(30);//
            swipe.setOnRefreshListener(() -> {
                if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                    loadInterestedList();
                } else {
                    SnackBarDialog.showNoInternetError(swipe);
                }
            });
            mEventInterestAdapter = new EventInterestAdapter(getActivity(), mEventInterestList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            linearLayoutManager.setInitialPrefetchItemCount(4);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
            mRecyclerView.setAdapter(mEventInterestAdapter);
            return v;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            loadInterestedList();
        }

        private void loadInterestedList() {
            if(eventId != null && !eventId.equalsIgnoreCase("")){
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                    if(swipe != null)
                        swipe.setRefreshing(true);
                    ApiInterface apiService =
                            ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
                    String userId = AppController.getInstance().getPrefManager().getUser().getId();
                    String apiKey = UserUtils.getApiKey(AppController.getInstance());

                    apiService.getEventInterestList(userId, eventId, apiKey)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<EventInterestList>() {
                                @Override
                                public void onCompleted() {
                                    Log.e("EventInterest", "onCompleted: ");
                                    if(swipe != null)
                                        swipe.setRefreshing(false);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e("EventInterest", "onError: ");
                                    if(swipe != null)
                                        swipe.setRefreshing(false);
                                }

                                @Override
                                public void onNext(EventInterestList mEIList) {
                                    Log.d("EventInterest", "onNext: ");
                                    if(mEIList != null ){
                                        if(!mEIList.isErrorStatus()){
                                            mEventInterestList.clear();
                                            if(mEIList.getData() != null){
                                                mEventInterestList.addAll(mEIList.getData());
                                                mEventInterestAdapter.notifyDataSetChanged();

                                                if (!mEventInterestList.isEmpty()) {
                                                    swipe.setVisibility(View.VISIBLE);
                                                    emptyView.setVisibility(View.GONE);
                                                } else {
                                                    swipe.setVisibility(View.GONE);
                                                    emptyView.setVisibility(View.VISIBLE);
                                                }
                                            } else {
                                                swipe.setVisibility(View.GONE);
                                                emptyView.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            swipe.setVisibility(View.GONE);
                                            emptyView.setVisibility(View.VISIBLE);
                                        }
                                    }

                                }
                            });
                } else {
                    if(swipe != null)
                        SnackBarDialog.showNoInternetError(swipe);
                }
            }
        }

    }

    public static class EventGoingFragment extends Fragment{
        private RecyclerView mRecyclerView;
        private SwipeRefreshLayout swipe;
        private View emptyView;
        private List<EventInterestList.EventInterestListData> mEventGoingList = new ArrayList<>();
        private EventGoingAdapter mEventGoingAdapter;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.event_going, container, false);
            mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
            emptyView = v.findViewById(R.id.empty);
            UserUtils.setEmptyView(emptyView, R.drawable.ic_people_empty, "No Event Going", false);
            swipe = (SwipeRefreshLayout) v.findViewById(R.id.swipe);
            swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
            swipe.setDistanceToTriggerSync(30);//
            swipe.setOnRefreshListener(() -> {
                if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                    loadGuestGoingList();
                } else {
                    SnackBarDialog.showNoInternetError(swipe);
                }
            });
            mEventGoingAdapter = new EventGoingAdapter(getActivity(), mEventGoingList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            linearLayoutManager.setInitialPrefetchItemCount(4);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
            mRecyclerView.setAdapter(mEventGoingAdapter);
            return v;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            loadGuestGoingList();
        }

        private void loadGuestGoingList() {
            if(eventId != null && !eventId.equalsIgnoreCase("")){
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                    if(swipe != null)
                        swipe.setRefreshing(true);
                    ApiInterface apiService =
                            ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
                    String userId = AppController.getInstance().getPrefManager().getUser().getId();
                    String apiKey = UserUtils.getApiKey(AppController.getInstance());

                    apiService.getEventGoingList(userId, eventId, apiKey)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<EventInterestList>() {
                                @Override
                                public void onCompleted() {
                                    Log.e("EventGoing", "onCompleted: ");
                                    if(swipe != null)
                                        swipe.setRefreshing(false);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e("EventGoing", "onError: ");
                                    if(swipe != null)
                                        swipe.setRefreshing(false);
                                }

                                @Override
                                public void onNext(EventInterestList mEGList) {
                                    Log.d("EventGoing", "onNext: ");
                                    if(mEGList != null ){
                                        if(!mEGList.isErrorStatus()){
                                            mEventGoingList.clear();
                                            if(mEGList.getData() != null){
                                                mEventGoingList.addAll(mEGList.getData());
                                                mEventGoingAdapter.notifyDataSetChanged();
                                                if (!mEventGoingList.isEmpty()) {
                                                    swipe.setVisibility(View.VISIBLE);
                                                    emptyView.setVisibility(View.GONE);
                                                } else {
                                                    swipe.setVisibility(View.GONE);
                                                    emptyView.setVisibility(View.VISIBLE);
                                                }
                                            } else {
                                                swipe.setVisibility(View.GONE);
                                                emptyView.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            if(swipe != null){
                                                swipe.setVisibility(View.GONE);
                                                emptyView.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }

                                }
                            });
                } else {
                    if(swipe != null)
                        SnackBarDialog.showNoInternetError(swipe);
                }
            }
        }

    }
}
