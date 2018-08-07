package com.myscrap;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myscrap.adapters.NewsFragmentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.News;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragmentOne extends Fragment implements NewsFragmentAdapter.NewsFragmentAdapterListener{

    private SwipeRefreshLayout swipe;
    private NewsFragmentAdapter mNewsFragmentAdapter;
    private List<News.NewsData> mNewsDataList = new ArrayList<>();
    private List<News.NewsData> mNewsDataListRT = new ArrayList<>();
    private View mNewsFragmentOne;
    private TextView emptyNews;
    private String COMPANY_ID = "5040";
    private FloatingActionButton fab;
    private boolean isEditShow;

    public NewsFragmentOne(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mNewsFragmentOne = inflater.inflate(R.layout.fragment_news_fragment_one, container, false);
        RecyclerView mRecyclerView = (RecyclerView) mNewsFragmentOne.findViewById(R.id.recycler_view);
        swipe = (SwipeRefreshLayout) mNewsFragmentOne.findViewById(R.id.swipe);
        emptyNews = (TextView) mNewsFragmentOne.findViewById(R.id.empty_news);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);//
        swipe.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                if(swipe != null)
                    swipe.setRefreshing(true);
                loadNews();
            }
        });

        fab = (FloatingActionButton) mNewsFragmentOne.findViewById(R.id.fab);
        fab.setOnClickListener(view -> goToCreateNews());
        NewsFragmentAdapter.NewsFragmentAdapterListener listener = this;
        mNewsFragmentAdapter = new NewsFragmentAdapter(getActivity(), mNewsDataList, listener);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(AppController.getInstance(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mNewsFragmentAdapter);
        return mNewsFragmentOne;
    }

    private void goToCreateNews() {
        Intent i = new Intent(getContext(), CreateNewsActivity.class);
        i.putExtra("companyId", COMPANY_ID);
        startActivity(i);
    }

    private void loadNews() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());

                    apiService.news(userId, COMPANY_ID, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<News>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("loadNews", "onFailure");
                            if(swipe != null)
                                swipe.setRefreshing(false);
                        }

                        @Override
                        public void onNext(News news) {
                            Log.d("loadNews", "onSuccess");
                            if (mNewsDataList != null)
                                mNewsDataList.clear();
                            if (mNewsDataListRT != null)
                                mNewsDataListRT.clear();
                            if(swipe != null)
                                swipe.setRefreshing(false);
                            parseNews(news);
                        }
                    })
                    ;
        } else {
            if(mNewsFragmentOne != null)
                SnackBarDialog.showNoInternetError(mNewsFragmentOne);
        }
    }

    private void parseNews(News news) {
        if (news != null) {
            if(!news.isErrorStatus()){
                mNewsDataList = news.getData();
                if(news.getEditor() == 1) {
                    isEditShow = true;
                    if(fab != null){
                        fab.setVisibility(View.VISIBLE);
                    }
                } else {
                    isEditShow = false;
                    if(fab != null){
                        fab.setVisibility(View.GONE);
                    }
                }
                if(mNewsDataList != null) {
                    if(mNewsDataList.size() > 0) {
                        emptyNews.setVisibility(View.GONE);
                    } else {
                        emptyNews.setVisibility(View.VISIBLE);
                    }

                    for(News.NewsData mData : new ArrayList<>(mNewsDataList)){
                        if(mData.getPostedUserId().equalsIgnoreCase(COMPANY_ID)){
                            mData.setEditShow(isEditShow);
                            mNewsDataListRT.add(mData);
                        }
                    }

                    if(mNewsFragmentAdapter != null){
                        mNewsFragmentAdapter.swap(mNewsDataListRT);
                    }
                } else {
                    emptyNews.setVisibility(View.VISIBLE);
                }

            } else {
                if(mNewsFragmentOne != null)
                    SnackBarDialog.show(mNewsFragmentOne, news.getStatus());
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            if(swipe != null){
                swipe.setRefreshing(true);
                swipe.post(this::loadNews);
            }else {
                loadNews();
            }
        } else {
            if(mNewsFragmentOne != null) {
                SnackBarDialog.showNoInternetError(mNewsFragmentOne);
            }
        }
    }

    @Override
    public void onAdapterClicked(int position) {}

}
