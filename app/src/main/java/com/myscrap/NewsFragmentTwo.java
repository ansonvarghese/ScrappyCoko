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
public class NewsFragmentTwo extends Fragment implements NewsFragmentAdapter.NewsFragmentAdapterListener{

    private SwipeRefreshLayout swipe;
    private NewsFragmentAdapter mNewsFragmentAdapter;
    private List<News.NewsData> mNewsDataList = new ArrayList<>();
    private List<News.NewsData> mNewsDataListRI = new ArrayList<>();

    private View mNewsFragmentTwo;
    private TextView emptyNews;
    private String COMPANY_ID = "5041";
    private FloatingActionButton fab;
    private boolean isEditShow;

    public NewsFragmentTwo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mNewsFragmentTwo = inflater.inflate(R.layout.fragment_news_fragment_two, container, false);
        RecyclerView mRecyclerView = (RecyclerView) mNewsFragmentTwo.findViewById(R.id.recycler_view);
        swipe = (SwipeRefreshLayout) mNewsFragmentTwo.findViewById(R.id.swipe);
        emptyNews = (TextView) mNewsFragmentTwo.findViewById(R.id.empty_news);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);//
        swipe.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                if(swipe != null)
                    swipe.setRefreshing(true);
                loadNews();
            }
        });
        fab = (FloatingActionButton) mNewsFragmentTwo.findViewById(R.id.fab);
        fab.setOnClickListener(view -> goToCreateNews());
        NewsFragmentAdapter.NewsFragmentAdapterListener listener = this;
        mNewsFragmentAdapter = new NewsFragmentAdapter(getActivity(), mNewsDataList, listener);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(AppController.getInstance(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mNewsFragmentAdapter);
        if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
            loadNews();
        } else {
            if(mNewsFragmentTwo != null) {
                SnackBarDialog.showNoInternetError(mNewsFragmentTwo);
            }
        }
        return mNewsFragmentTwo;
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
            String apiKey = UserUtils.getApiKey(getActivity());
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
                   if (mNewsDataList != null)
                       mNewsDataList.clear();
                   if (mNewsDataListRI != null)
                       mNewsDataListRI.clear();
                   if(swipe != null)
                       swipe.setRefreshing(false);
                   Log.d("loadNews", "onSuccess");
                   parseNews(news);
               }
           });
        } else {
            if(mNewsFragmentTwo != null)
                SnackBarDialog.showNoInternetError(mNewsFragmentTwo);
        }
    }

    private void parseNews(News news) {
        if (news != null) {
            if(news.getEditor() == 2) {
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
            if(!news.isErrorStatus()){
                mNewsDataList = news.getData();

                if(mNewsDataList != null) {
                    if(mNewsDataList.size() > 0) {
                        emptyNews.setVisibility(View.GONE);
                    } else {
                        emptyNews.setVisibility(View.VISIBLE);
                    }
                    for(News.NewsData mData : new ArrayList<>(mNewsDataList)){
                        if(mData.getPostedUserId().equalsIgnoreCase(COMPANY_ID)){
                            mData.setEditShow(isEditShow);
                            mNewsDataListRI.add(mData);
                        }
                    }

                    if(mNewsFragmentAdapter != null){
                        mNewsFragmentAdapter.swap(mNewsDataListRI);
                    }
                } else {
                    emptyNews.setVisibility(View.VISIBLE);
                }

            } else {
                if(mNewsFragmentTwo != null)
                    SnackBarDialog.show(mNewsFragmentTwo, news.getStatus());
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
            if(mNewsFragmentTwo != null) {
                SnackBarDialog.showNoInternetError(mNewsFragmentTwo);
            }
        }
    }

    @Override
    public void onAdapterClicked(int position) {}

}
