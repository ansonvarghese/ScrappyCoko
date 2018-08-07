package com.myscrap;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.BumpedPostAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Bumped;
import com.myscrap.service.MessageService;
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

public class BumpedPost extends Fragment implements BumpedPostAdapter.BumpedPostAdapterListener{

    private SwipeRefreshLayout swipe;
    private RecyclerView mRecyclerView;
    private Tracker mTracker;
    Bundle savedInstanceState;
    private List<Bumped.BumpedPostItem> mBumpedPost;
    private BumpedPostAdapter mBumpedPostAdapter;
    private View emptyView;

    public BumpedPost() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setHasOptionsMenu(true);
        mTracker = AppController.getInstance().getDefaultTracker();
        mBumpedPost = new ArrayList<>();
        mBumpedPostAdapter = new BumpedPostAdapter(getActivity(), mBumpedPost, this);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mBumpedPostFragment = inflater.inflate(R.layout.fragment_bumped_post, container, false);
        mRecyclerView = (RecyclerView) mBumpedPostFragment.findViewById(R.id.recycler_view);
        swipe = (SwipeRefreshLayout) mBumpedPostFragment.findViewById(R.id.swipe);
        emptyView = mBumpedPostFragment.findViewById(R.id.empty);
        UserUtils.setEmptyView(emptyView, R.drawable.ic_bumped, "No Bumped Posts", true);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);//
        swipe.setOnRefreshListener(this::doPreparePost);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(5), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mBumpedPostAdapter);
        return mBumpedPostFragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UserUtils.setBumpedCount(AppController.getInstance(), "");
        doPreparePost();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Bumped Post Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        //UserOnlineStatus.setUserOnline(AppController.getInstance(), UserOnlineStatus.ONLINE);
    }

    private void doPreparePost() {
        if(swipe != null){
            if(getActivity() != null && CheckNetworkConnection.isConnectionAvailable(getActivity())){
                swipe.post(() -> swipe.setRefreshing(true));
                loadBumpedPost();
            } else {
                if(swipe != null) {
                    swipe.post(() -> SnackBarDialog.showNoInternetError(swipe));
                }
            }
        }
    }

    private void loadBumpedPost()
    {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
            apiService.bumped(userId,apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Bumped>() {
                        @Override
                        public void onCompleted() {
                            if(swipe != null)
                                swipe.setRefreshing(false);
                            parseData();
                            bumpedResponseProcessed();
                            Log.d("BumpedPost", "onCompleted");
                        }

                        @Override
                        public void onError(Throwable e)
                        {
                            if(swipe != null)
                                swipe.setRefreshing(false);
                            if(e != null  && e.getMessage() != null && e.getMessage().equalsIgnoreCase("SSL handshake timed out"))
                                SnackBarDialog.show(swipe, "Please try again later.");

                            Log.d("BumpedPost", "Failure");
                        }

                        @Override
                        public void onNext(final Bumped mBumped) {
                            if (mBumped != null && !mBumped.isErrorStatus()) {
                                if (mBumped.getData() != null) {
                                    if (mBumpedPost == null)
                                        return;
                                    mBumpedPost.clear();
                                    mBumpedPost.addAll(mBumped.getData());
                                    emptyView.setVisibility(View.GONE);
                                    swipe.setVisibility(View.VISIBLE);
                                } else {
                                    emptyView.setVisibility(View.VISIBLE);
                                    swipe.setVisibility(View.GONE);
                                }
                            }
                            Log.d("BumpedPost", "onNext");
                        }
                    });
    }







    private void bumpedResponseProcessed()
    {
        Intent i = new Intent();
        i.setAction(MessageService.ACTION_LOAD_BUMPER_ACK);
        if (getActivity() != null)
            getActivity().sendBroadcast(i);
    }






    private void parseData() {
        if (mRecyclerView != null) {
            mRecyclerView.post(() -> mBumpedPostAdapter.notifyDataSetChanged());
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //UserOnlineStatus.setUserOnline(AppController.getInstance(), UserOnlineStatus.OFFLINE);
    }

    @Override
    public void onRemovePost(String id, int position) {
        if (position != -1) {
            if (!mBumpedPost.isEmpty()) {
                mBumpedPost.remove(position);
                mBumpedPostAdapter.notifyItemRemoved(position);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                swipe.setVisibility(View.GONE);
            }
        }
        removeFromBumpedPost(id);
    }

    private void removeFromBumpedPost(final String userIdToRemove) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        apiService.bumpedPostRemove(userId, userIdToRemove, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bumped>() {
                    @Override
                    public void onCompleted() {
                        Log.d("BumpedPostRemove", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("BumpedPostRemove", "Failure");
                    }

                    @Override
                    public void onNext(final Bumped mBumped) {
                        if (mBumped != null && !mBumped.isErrorStatus()) {
                            Log.d("BumpedPostRemove"+userIdToRemove, "Success");
                        }
                        Log.d("BumpedPostRemove", "onNext");
                    }
                });
    }


    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }



}
