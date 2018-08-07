package com.myscrap;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.ViewersAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.ViewerCounts;
import com.myscrap.model.Viewers;
import com.myscrap.service.MessageService;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.DividerItemDecoration;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewersFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Viewers.ViewersData> mViewersList = new ArrayList<>();
    private ViewersAdapter mViewersAdapter;
    private Tracker mTracker;
    private View emptyView;

    public ViewersFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        View mViewersFragmentView = inflater.inflate(R.layout.fragment_viewers, container, false);
        mViewersAdapter = new ViewersAdapter(getContext(), mViewersList);
        mRecyclerView = (RecyclerView) mViewersFragmentView.findViewById(R.id.recycler_view);
        emptyView = mViewersFragmentView.findViewById(R.id.empty);
        UserUtils.setEmptyView(emptyView, R.drawable.ic_visitor_empty, "No Viewers", true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(AppController.getInstance(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mViewersAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mViewersFragmentView.findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);
        mSwipeRefreshLayout.setOnRefreshListener(this::load);


        UserUtils.setViewersCount(getContext(), "0");
        UserUtils.setFRNotificationCount(getActivity(), "0");
        UserUtils.setNotificationCount(getActivity(), "0");

        HomeActivity.notification();

        load();
        return mViewersFragmentView;
    }

    private void load() {
        if(CheckNetworkConnection.isConnectionAvailable(getContext())){
            loadViewers();
        } else {
            SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }
    }

    private void loadViewers() {

        if (AppController.getInstance().getPrefManager().getUser() == null)
                return;
            if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                if(mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(true);
                ApiInterface apiService =
                        ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
                String userId = AppController.getInstance().getPrefManager().getUser().getId();
                String apiKey = UserUtils.getApiKey(getActivity());
                Log.d("ViewersList", ""+userId);
                apiService.visitors(userId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Viewers>() {
                    @Override
                    public void onCompleted()
                    {
                        update();
                        viewersResponseProcessed();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("ViewersList", "FAILED");
                        if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(Viewers mViewers) {
                        if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);
                        if(mViewers != null){
                            if (mViewersList != null)
                                mViewersList.clear();
                            parseData(mViewers);
                        } else {
                            emptyView.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setVisibility(View.GONE);
                        }

                        Log.d("ViewersList", "onSuccess");
                    }
                });
            } else {
                if(mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
                    mSwipeRefreshLayout.setRefreshing(false);
                if(mRecyclerView != null)
                    SnackBarDialog.show(mRecyclerView, "No internet connection available");
            }
    }

    private void parseData(Viewers mViewers) {
        if(!mViewers.isErrorStatus() && mViewers.getData() != null){
            mViewersList = mViewers.getData();
            if(mViewersAdapter != null ){
                if(mViewersList != null && mViewersList.size() > 0){
                    mViewersAdapter.swap(mViewersList);
                    emptyView.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                } else{
                    emptyView.setVisibility(View.VISIBLE);
                    mSwipeRefreshLayout.setVisibility(View.GONE);
                }
            }
        } else {
            emptyView.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
            if(mRecyclerView != null)
                SnackBarDialog.show(mRecyclerView, mViewers.getStatus());
        }
    }

    private void update() {
        if(mRecyclerView != null){
            mRecyclerView.post(() -> {
                UserUtils.setViewersCount(getContext(), "0");
                HomeActivity.notification();
            });
        }
    }

    private void viewersResponseProcessed(){
        Intent i = new Intent();
        i.setAction(MessageService.ACTION_LOAD_VIEWERS_ACK);
        if (getActivity() != null)
            getActivity().sendBroadcast(i);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Visitor Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMessage(ViewerCounts counts) {
        load();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

}
