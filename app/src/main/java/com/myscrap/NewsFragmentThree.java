package com.myscrap;


import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myscrap.adapters.NewsFragmentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.News;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.RecyclerTouchListener;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragmentThree extends Fragment implements NewsFragmentAdapter.NewsFragmentAdapterListener{


    public NewsFragmentThree() {
        // Required empty public constructor
    }

    private SwipeRefreshLayout swipe;
    private List<News.NewsData> mNewsDataList = new ArrayList<>();
    private List<News.NewsData> mNewsDataListRME = new ArrayList<>();

    private View mNewsFragmentThree;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mNewsFragmentThree = inflater.inflate(R.layout.fragment_news_fragment_three, container, false);
        RecyclerView mRecyclerView = (RecyclerView) mNewsFragmentThree.findViewById(R.id.recycler_view);
        swipe = (SwipeRefreshLayout) mNewsFragmentThree.findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);//
        swipe.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                if(swipe != null)
                    swipe.setRefreshing(true);
                loadNews();
            }
        });
        NewsFragmentAdapter.NewsFragmentAdapterListener listener = this;
        NewsFragmentAdapter mNewsFragmentAdapter = new NewsFragmentAdapter(getActivity(), mNewsDataList, listener);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(AppController.getInstance(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mNewsFragmentAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(mNewsDataListRME != null && mNewsDataListRME.size() > 0 &&  getActivity() != null){
                    News.NewsData mNewsData = mNewsDataListRME.get(position);
                    if(mNewsData != null) {
                        if(mNewsData.getNewsUrl() != null && !mNewsData.getNewsUrl().equalsIgnoreCase("")){
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                builder.setToolbarColor(getResources().getColor(R.color.colorPrimary, getActivity().getTheme()));
                            } else {
                                builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                            }
                            builder.setStartAnimations(getActivity(), R.anim.slide_in_right, R.anim.slide_out_left);
                            builder.setExitAnimations(getActivity(), R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.intent.setPackage("com.android.chrome");
                            customTabsIntent.launchUrl(getActivity(), Uri.parse(mNewsData.getNewsUrl()));
                        } else {
                            if(swipe != null)
                                SnackBarDialog.show(swipe, "Link not found.");
                        }

                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        /*if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
            loadNews();
        } else {
            if(mNewsFragmentThree != null) {
                SnackBarDialog.showNoInternetError(mNewsFragmentThree);
            }
        }*/
        return mNewsFragmentThree;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                if(swipe != null){
                    swipe.setRefreshing(true);
                    swipe.post(this::loadNews);
                }
            } else {
                if(mNewsFragmentThree != null) {
                    SnackBarDialog.showNoInternetError(mNewsFragmentThree);
                }
            }
        }
    }


    private void loadNews() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            //ProgressBarDialog.showLoader(getActivity());
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getActivity());
            /*Call<News> call = apiService.news(userId, "0", apiKey);
            call.enqueue(new Callback<News>() {
                @Override
                public void onResponse(@NonNull Call<News> call, @NonNull Response<News> response) {
                   // ProgressBarDialog.dismissLoader();
                    Log.d("loadNews", "onSuccess");
                    if (response.body() != null) {
                        if (mNewsDataList != null)
                            mNewsDataList.clear();
                        if (mNewsDataListRME != null)
                            mNewsDataListRME.clear();
                        if(swipe != null)
                            swipe.setRefreshing(false);
                        News mFavourite = response.body();
                        if (mFavourite != null) {
                            if(!mFavourite.isErrorStatus()){
                                mNewsDataList = mFavourite.getData();

                                if(mNewsDataList != null) {
                                    for(News.NewsData mData : new ArrayList<>(mNewsDataList)){
                                        if(mData.getPostedUserName().equalsIgnoreCase("Recycling Middle-East")){
                                            mNewsDataListRME.add(mData);
                                        }
                                    }

                                    if(mNewsFragmentAdapter != null){
                                        mNewsFragmentAdapter.swap(mNewsDataListRME);
                                    }
                                }

                            } else {
                                if(mNewsFragmentThree != null)
                                    SnackBarDialog.show(mNewsFragmentThree, mFavourite.getStatus());
                            }
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<News> call, @NonNull Throwable t) {
                   // ProgressBarDialog.dismissLoader();
                    Log.d("loadNews", "onFailure");
                    if(swipe != null)
                        swipe.setRefreshing(false);
                }
            });*/
        } else {
            if(mNewsFragmentThree != null)
                SnackBarDialog.showNoInternetError(mNewsFragmentThree);
        }
    }

    @Override
    public void onAdapterClicked(int position) {
        if(mNewsDataListRME != null && mNewsDataListRME.size() > 0){
            News.NewsData mNewsData = mNewsDataListRME.get(position);
            if(mNewsData != null) {
                /*Intent i = new Intent(getActivity(), NewsViewActivity.class);
                i.putExtra("newsId", mNewsData.getPostId());
                i.putExtra("newsUrl", mNewsData.getNewsUrl());
                startActivity(i);*/
                /*if(mNewsData.getNewsUrl() != null && !mNewsData.getNewsUrl().equalsIgnoreCase("")){
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary, getActivity().getTheme()));
                    } else {
                        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                    }
                    builder.setStartAnimations(getActivity(), R.anim.slide_in_right, R.anim.slide_out_left);
                    builder.setExitAnimations(getActivity(), R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.intent.setPackage("com.android.chrome");
                    customTabsIntent.launchUrl(getContext(), Uri.parse(mNewsData.getNewsUrl()));
                } else {
                    if(swipe != null)
                        SnackBarDialog.show(swipe, "Link not found.");
                }*/
            }
        }
    }

}
