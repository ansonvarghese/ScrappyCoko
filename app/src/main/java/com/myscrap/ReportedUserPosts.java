package com.myscrap;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.myscrap.adapters.FeedItemAnimator;
import com.myscrap.adapters.FeedsAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.DeletePost;
import com.myscrap.model.Favourite;
import com.myscrap.model.Feed;
import com.myscrap.model.LikedData;
import com.myscrap.model.Report;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.FeedContextMenu;
import com.myscrap.view.FeedContextMenuManager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.myscrap.adapters.FeedsAdapter.ACTION_LIKE_BUTTON_CLICKED;

public class ReportedUserPosts extends Fragment implements  FeedsAdapter.OnFeedItemClickListener,
        FeedContextMenu.OnFeedContextMenuItemClickListener{

    private SwipeRefreshLayout swipe;
    private FeedsAdapter feedAdapter;
    private  FeedsAdapter.OnFeedItemClickListener listener;
    private List<Feed.FeedItem> feedItems = new ArrayList<>();
    RecyclerView mRecyclerView;
    private Tracker mTracker;

    public ReportedUserPosts() {}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View postFragment = inflater.inflate(R.layout.fragment_user_reported_post, container, false);
        mRecyclerView = (RecyclerView) postFragment.findViewById(R.id.recycler_view);
        listener = this;
        setupFeed();
        swipe = (SwipeRefreshLayout) postFragment.findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);
        swipe.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                loadFeed();
            } else {
                SnackBarDialog.showNoInternetError(swipe);
            }
        });
        return postFragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTracker = AppController.getInstance().getDefaultTracker();
    }


    @Override
    public void onResume() {
        super.onResume();
        UserUtils.setModeratorNotificationCount(getActivity(), "0");
        HomeActivity.notification();
        loadFeed();
        if(mTracker != null){
            mTracker.setScreenName("Reported User Post Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }


    private void  setupFeed(){
        feedAdapter = new FeedsAdapter(getActivity(), feedItems);
        feedAdapter.setOnFeedItemClickListener(listener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemAnimator(new FeedItemAnimator());
        mRecyclerView.setAdapter(feedAdapter);

    }

    private void loadFeed() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(CheckNetworkConnection.isConnectionAvailable(getActivity())) {
            if(AppController.getInstance().getPrefManager().getUser() != null){
                String userId = AppController.getInstance().getPrefManager().getUser().getId();
                String apiKey = UserUtils.getApiKey(getActivity());
                if(swipe != null && !swipe.isRefreshing())
                    swipe.setRefreshing(true);
                getFeeds(userId, apiKey);
            }
        } else {
            if(swipe != null)
                SnackBarDialog.showNoInternetError(swipe);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    public void getFeeds(String userId, String apiKey){
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
                apiService.getReportedUserPosts(userId, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Feed>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.d("getFeeds", "Failure");
                        ProgressBarDialog.dismissLoader();
                        if(swipe != null)
                            swipe.setRefreshing(false);
                    }

                    @Override
                    public void onNext(Feed mFeed) {
                        if(swipe != null)
                            swipe.setRefreshing(false);
                        ProgressBarDialog.dismissLoader();
                        if( mFeed != null){
                            if(!mFeed.isErrorStatus()){
                                if(mFeed.getData()!= null){
                                    feedItems.clear();
                                    feedItems = mFeed.getData();
                                    feedAdapter.swap(feedItems);
                                    if(feedItems.size() == 0)
                                        if(swipe != null){
                                            SnackBarDialog.show(swipe, "No reported posts.");
                                        }
                                } else {
                                    feedItems.clear();
                                    feedAdapter.swap(feedItems);
                                    if(swipe != null){
                                        SnackBarDialog.show(swipe, "No reported posts.");
                                    }
                                }
                                Log.d("getFeeds", "Success");
                            } else {
                                if(swipe != null){
                                    SnackBarDialog.show(swipe, "Please, try again later!");
                                }
                                Log.d("getFeeds", "failure");
                            }
                        }
                    }
                });
    }


    private void screenMoveToCommentActivity(String postId, String postedUserId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        final Intent intent = new Intent(getActivity(), CommentActivity.class);
        intent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        intent.putExtra("postId", postId);
        intent.putExtra("postedUserId", postedUserId);
        intent.putExtra("apiKey", UserUtils.getApiKey(getActivity()));
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop()){
            if(getActivity() != null)
                getActivity().overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onCommentsClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0){
            Feed.FeedItem feedItem = feedItems.get(position);
            screenMoveToCommentActivity(feedItem.getPostId(), feedItem.getPostedUserId());
        } else {
            final Intent intent = new Intent(getActivity(), CommentActivity.class);
            startActivity(intent);
            if(CheckOsVersion.isPreLollipop()){
                if(getActivity() != null)
                    getActivity().overridePendingTransition(0, 0);
            }
        }
    }

    @Override
    public void onImageCenterClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            Bundle bundle = new Bundle();
            bundle.putSerializable("images", (Serializable) feedItem.getPictureUrl());
            //bundle.putString("images", feedItem.getPictureUrl().get(0).getImages());
            if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                FeedImagesSlideshowDialogFragment newFragment = FeedImagesSlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }

        }
    }

    @Override
    public void onEventClick(View v, int position) {
        if (!swipe.isRefreshing()) {
            if (feedItems != null && feedItems.size() > 0) {
                Feed.FeedItem feedItem = feedItems.get(position);
                if (feedItem != null) {
                    goToEventDetailActivity(feedItem.getEventId());
                }
            }
        }
    }

    private void goToEventDetailActivity(String eventId) {
        if (getActivity() != null) {
            Intent i = new Intent(getActivity(), EventDetailActivity.class);
            i.putExtra("eventId", eventId);
            getActivity().startActivity(i);
            if(CheckOsVersion.isPreLollipop()){
                if(getActivity() != null)
                    getActivity().overridePendingTransition(0, 0);
            }
        }

    }

    @Override
    public void onHeartClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            int likeCount;
            if(feedItem != null){
                if(feedItem.isLikeStatus()){
                    likeCount = feedItem.getLikeCount();
                    if(likeCount > 0){
                        feedItem.setLikeCount(likeCount-1);
                    }
                    feedItem.setLikeStatus(false);
                } else {
                    likeCount = feedItem.getLikeCount();
                    feedItem.setLikeCount(likeCount+1);
                    feedItem.setLikeStatus(true);
                }
                feedItems.set(position, feedItem );
                if(feedAdapter != null){
                    feedAdapter.notifyItemChanged(position, ACTION_LIKE_BUTTON_CLICKED);
                }
                if(CheckNetworkConnection.isConnectionAvailable(getActivity()))
                    doLike(feedItem);
            }
        }
    }

    @Override
    public void onLoadMore(int position) {}

    @Override
    public void onReport(View v, ViewGroup inActiveLayout, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                showReportPopupMenu(v,  feedItems.get(position), position);
            }
        }
    }

    @Override
    public void onInActiveReport(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                    showInactiveReportPopupMenu(v, feedItems.get(position), position);
                else
                    SnackBarDialog.showNoInternetError(v);
            }
        }
    }

    private void showInactiveReportPopupMenu(final View v, final Feed.FeedItem feedItem, final int itemPosition) {
        if (getActivity() == null)
            return;

        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        /*if(feedItem.getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
            inflater.inflate(R.menu.un_report, popup.getMenu());
        } else {
            inflater.inflate(R.menu.report_contact_moderator, popup.getMenu());
            inflater.inflate(R.menu.report_moderator, popup.getMenu());
        }*/

        inflater.inflate(R.menu.report_moderator, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.action_un_report){
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setMessage("Are you sure you want to un report this post?");
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        undoReport(feedItem.getReportId());
                        if(feedItems != null && feedItems.size() > 0 &&  feedAdapter != null){
                            Toast.makeText(getActivity(), "Post Un Reported", Toast.LENGTH_SHORT).show();
                            feedItems.remove(itemPosition);
                            feedAdapter.notifyItemRemoved(itemPosition);
                            feedAdapter.swap(feedItems);
                            feedAdapter.notifyDataSetChanged();
                        }
                    }
                });

                dialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            } else if(item.getItemId() == R.id.action_delete){
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setMessage("Are you sure you want to delete this post?");
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                    deleteReport(feedItem.getPostId(), feedItem.getReportId());
                    if(feedItems != null && feedItems.size() > 0 &&  feedAdapter != null){
                        Toast.makeText(getActivity(), "Report deleted", Toast.LENGTH_SHORT).show();
                        feedItems.remove(itemPosition);
                        feedAdapter.notifyItemRemoved(itemPosition);
                        feedAdapter.swap(feedItems);
                        feedAdapter.notifyDataSetChanged();
                    }
                });

                dialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
            return true;
        });
        popup.show();
    }


    private void undoReport(String reportId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        Call<Report> call = apiService.undoReportPost(reportId, apiKey);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull Response<Report> response) {
                if(response.body() != null && response.isSuccessful()){
                    Report mReportPost = response.body();
                    if(mReportPost != null && !mReportPost.isErrorStatus()){
                        Log.d("undoReport", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                Log.d("undoReport", "onFailure");
            }
        });
    }

    private void deleteReport(String postId, String reportId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Report> call = apiService.deleteReportPost(postId, reportId,userId, apiKey);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull Response<Report> response) {
                if(response.body() != null && response.isSuccessful()){
                    Report mReportPost = response.body();
                    if(mReportPost != null && !mReportPost.isErrorStatus()){
                        Log.d("deleteReportPost", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                Log.d("deleteReportPost", "onFailure");
            }
        });
    }

    private void showReportPopupMenu(View v, final Feed.FeedItem feedItem, final int itemPosition) {
        if (getActivity() == null)
            return;

        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.report, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.action_report){

                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setMessage("Are you sure you want to report this post?");
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                    doReport(feedItem.getPostId(), feedItem.getPostedUserId());
                    if(feedItems != null && feedItems.size() > 0 &&  feedAdapter != null){
                        Toast.makeText(getActivity(), "Post Reported", Toast.LENGTH_SHORT).show();
                        feedItems.remove(itemPosition);
                        feedAdapter.notifyItemRemoved(itemPosition);
                        feedAdapter.notifyDataSetChanged();
                    }
                });

                dialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            }
            return true;
        });
        popup.show();
    }

    private void doReport(String postId, String postedUserId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Report> call = apiService.reportPost(userId, postId, postedUserId, "", apiKey);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull Response<Report> response) {
                if(response.body() != null && response.isSuccessful()){
                    Report mReportPost = response.body();
                    if(mReportPost != null && !mReportPost.isErrorStatus()){
                        Log.d("doReport", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                Log.d("doReport", "onFailure");
            }
        });
    }

    private void doLike(Feed.FeedItem feedItem) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<LikedData> call = apiService.insertLike(userId, feedItem.getPostId(), feedItem.getPostedUserId(),apiKey);
        call.enqueue(new Callback<LikedData>() {
            @Override
            public void onResponse(@NonNull Call<LikedData> call, @NonNull Response<LikedData> response) {
                Log.d("doLike", "onSuccess");
                if(response.body() != null && response.isSuccessful()){
                    LikedData mLikedData = response.body();
                    if(mLikedData != null && !mLikedData.getError()){
                        LikedData.InsertLikeData  data = mLikedData.getInsertLikeData();
                        if(data != null) {
                            if(feedItems != null) {
                                int i = 0;
                                for(Feed.FeedItem  feedItem : feedItems){
                                    if(feedItem.getPostId().equalsIgnoreCase(data.getPostId())){
                                        feedItem.setLikeStatus(data.getLikeStatus());
                                        feedItem.setLikeCount(Integer.parseInt(UserUtils.parsingInteger(data.getLikeCount())));
                                        feedItems.set(i, feedItem);
                                        if(feedAdapter != null){
                                            feedAdapter.notifyItemChanged(i);
                                        }
                                    }
                                    i++;
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<LikedData> call, @NonNull Throwable t) {
                Log.d("doLike", "onFailure");
            }
        });
    }

    @Override
    public void onMoreClick(View v, int itemPosition, boolean isEditShow) {
        if(feedItems != null && feedItems.size() > 0){
            showPopupMenu(v,  feedItems.get(itemPosition), itemPosition, isEditShow);
        }
    }

    private void showPopupMenu(View v, Feed.FeedItem feedItem, int itemPosition, boolean isEditShow) {
        if (getActivity() == null)
            return;

        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.over_flow, popup.getMenu());
        Menu popupMenu = popup.getMenu();
        if(!isEditShow)
            popupMenu.findItem(R.id.action_edit).setVisible(false);
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(feedItem, itemPosition));
        popup.show();
    }

    private void showDeletePostDialog(Context context, final String id, final String postId, final String albumID, final int itemPosition){
        if (getActivity() == null)
            return;

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage("Are you sure you want to delete this post?");
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("DELETE", (dialog, which) -> {
            feedItems.remove(itemPosition);
            feedAdapter.notifyDataSetChanged();
            deletingPost(id, postId, albumID);
        });

        dialogBuilder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void deletingPost(String id, String postId, String albumID) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<DeletePost> call = apiService.deletePost(userId, postId, albumID,apiKey);
        call.enqueue(new Callback<DeletePost>() {
            @Override
            public void onResponse(@NonNull Call<DeletePost> call, @NonNull Response<DeletePost> response) {

                if(response.body() != null && response.isSuccessful()){
                    DeletePost mDeletePost = response.body();
                    if(mDeletePost != null && !mDeletePost.isErrorStatus()){
                        Log.d("deletingPost", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<DeletePost> call, @NonNull Throwable t) {
                Log.d("deletingPost", "onFailure");
            }
        });
    }

    @Override
    public void onProfileClick(View v, int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(feedItem.getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(feedItem.getReportedUserId());
                }
            }
        }

    }

    @Override
    public void onTagClick(View v, String taggedId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(taggedId != null && !taggedId.equalsIgnoreCase("")){
            if(taggedId.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                goToUserProfile();
            } else {
                goToUserFriendProfile(taggedId);
            }
        }
    }

    @Override
    public void onPostFromClick(View v, int position) {
    }

    @Override
    public void onPostToClick(View v, int position) {}

    @Override
    public void onFavouriteClick(View v, int position) {
        if(feedItems != null && feedItems.size() > 0) {
            Feed.FeedItem feedItem = feedItems.get(position);
            if(feedItem != null){
                if(feedItem.isPostFavourited()){
                    feedItem.setPostFavourited(false);
                    Toast.makeText(getActivity(), "Removed from favourite posts", Toast.LENGTH_SHORT).show();
                }

                if(CheckNetworkConnection.isConnectionAvailable(getActivity())){
                    doFavourite(feedItem, position);
                } else {
                    SnackBarDialog.showNoInternetError(v);
                }
                if(feedAdapter != null){
                    feedItems.remove(position);
                    feedAdapter.notifyItemRemoved(position);
                }

            }
        }
    }


    private void doFavourite(Feed.FeedItem feedItem, int position) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Favourite> call = apiService.insertFavourite(userId, feedItem.getPostId(), feedItem.getPostedUserId(),apiKey);
        call.enqueue(new Callback<Favourite>() {
            @Override
            public void onResponse(@NonNull Call<Favourite> call, @NonNull Response<Favourite> response) {
                Log.d("doFavourite", "onSuccess");
            }
            @Override
            public void onFailure(@NonNull Call<Favourite> call, @NonNull Throwable t) {
                Log.d("doFavourite", "onFailure");
            }
        });
    }

    private void goToUserProfile() {
        Intent i = new Intent(getActivity(), UserProfileActivity.class);
        startActivity(i);
        if (getActivity() == null)
            return;
        if (CheckOsVersion.isPreLollipop()) {
            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToUserFriendProfile(String postedUserId) {
        final Intent intent = new Intent(getActivity(), UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(getActivity() != null)
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onReportClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onSharePhotoClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCopyShareUrlClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCancelClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private Feed.FeedItem feedItem;
        private int itemPosition;
        public MyMenuItemClickListener(Feed.FeedItem mFeedItem, int position) {
            this.feedItem = mFeedItem;
            this.itemPosition = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    Intent i = new Intent(getActivity(), StatusActivity.class);
                    i.putExtra("page", "userFavouritePost");
                    i.putExtra("editPost", feedItem.getStatus());
                    i.putExtra("postId", ""+feedItem.getPostId());
                    Gson gson = new Gson();
                    String userData = gson.toJson(feedItem);
                    i.putExtra("tagData", userData);
                    if (getActivity()!= null){
                        getActivity().startActivity(i);
                        if (CheckOsVersion.isPreLollipop())
                            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                    return true;
                case R.id.action_delete:
                    if (feedItem != null ) {
                        if(feedItems != null && feedItems.size() > 0){
                            showDeletePostDialog(getActivity(), feedItem.getPostedUserId(), feedItem.getPostId(), feedItem.getAlbumId(), itemPosition);
                        }

                    }
                    return true;
                default:
            }
            return false;
        }
    }


}
