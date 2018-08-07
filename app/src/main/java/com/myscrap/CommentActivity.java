package com.myscrap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.adapters.CommentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Comment;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.util.ArrayList;
import java.util.List;

import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends AppCompatActivity implements CommentAdapter.CommentAdapterClickListener{

    private  CommentActivity mCommentActivity;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String userId, postId, postedUserId, apiKey;
    private CommentAdapter mCommentAdapter;
    private List<Comment.CommentData> mCommentList = new ArrayList<>();
    private EmojiconEditText emojiconEditText;
    private RecyclerView mRecyclerView;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mCommentActivity = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        CommentAdapter.CommentAdapterClickListener mCommentAdapterClickListener = this;
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_likes);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mCommentAdapter = new CommentAdapter(this, mCommentList, mCommentAdapterClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mCommentActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mCommentAdapter);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        mSwipeRefreshLayout.setDistanceToTriggerSync(30);//
        mSwipeRefreshLayout.setOnRefreshListener(() -> load());
        emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        final ImageView submitButton = (ImageView) findViewById(R.id.submit_btn);
        if(emojiconEditText != null)
            emojiconEditText.setOnEditorActionListener((v, id, event) -> {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    if (CheckNetworkConnection.isConnectionAvailable(getBaseContext()))
                        doComment();
                    else
                    if(emojiconEditText != null)
                        SnackBarDialog.showNoInternetError(emojiconEditText);
                    return true;
                }
                return false;
            });
        Intent mIntent = getIntent();
        if (mIntent != null){
            userId = mIntent.getStringExtra("userId");
            postId = mIntent.getStringExtra("postId");
            postedUserId = mIntent.getStringExtra("postedUserId");
            apiKey = mIntent.getStringExtra("apiKey");
            mSwipeRefreshLayout.post(this::load);
        }

        if(submitButton != null){
            submitButton.setOnClickListener(v -> {
                if (TextUtils.isEmpty(emojiconEditText.getText().toString())){
                    Toast.makeText(mCommentActivity, "Write a comment..", Toast.LENGTH_SHORT).show();
                } else {
                    if (CheckNetworkConnection.isConnectionAvailable(mCommentActivity))
                        doComment();
                    else
                        SnackBarDialog.showNoInternetError(submitButton);
                }
            });
        }


    }

    private void doComment() {
        if(emojiconEditText != null){
            if(CheckNetworkConnection.isConnectionAvailable(getBaseContext())){
                if (!TextUtils.isEmpty(emojiconEditText.getText().toString())){
                    String message = emojiconEditText.getText().toString().trim();
                    sendComment(message);
                    emojiconEditText.setText("");
                    UserUtils.hideKeyBoard(mCommentActivity, emojiconEditText);
                }
            } else {
                SnackBarDialog.showNoInternetError(emojiconEditText);
            }
        }
    }

    private void sendComment(String comment) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        long NOW = System.currentTimeMillis() / 1000L;
        String timeStamp = Long.toString(NOW);
        Toast.makeText(this,"Posting comment...", Toast.LENGTH_SHORT).show();
        Call<Comment> call = apiService.insertComment(userId, postId, postedUserId,comment,timeStamp, apiKey);
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                Log.d("sendComment", "onSuccess");
                if(response.body() != null && response.isSuccessful()){
                    mCommentList.clear();
                    Comment mComment = response.body();
                    if (mComment != null && !mComment.isErrorStatus()) {
                        if (mCommentList != null && mCommentAdapter != null) {
                            mCommentList.clear();
                            mCommentList.addAll(mComment.getData());
                            mCommentAdapter.notifyDataSetChanged();
                            if (mCommentAdapter.getItemCount() > 1) {
                                mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, mCommentAdapter.getItemCount() - 1);
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                Log.d("sendComment", "onFailure");
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return false;
    }

    private void load() {
        if(CheckNetworkConnection.isConnectionAvailable(mCommentActivity)){
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            Call<Comment> call = apiService.loadCommentDetails(userId, postId, postedUserId, apiKey);
            call.enqueue(new Callback<Comment>() {
                @Override
                public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                    if (response.body() != null) {
                        mCommentList.clear();
                        Comment mComment = response.body();
                        if(mComment != null){
                            if (!mComment.isErrorStatus()){
                                mCommentList.addAll(mComment.getData());
                                mCommentAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    Log.d("CommentDetails", "success");
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                }
                @Override
                public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                    Log.d("CommentDetails", "onFailure");
                    if(mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);
            if(mSwipeRefreshLayout != null)
                SnackBarDialog.showNoInternetError(mSwipeRefreshLayout);
        }

    }

    @Override
    public void onOverFlowClickListener(View v, int position) {
        if(mCommentList != null && mCommentList.size() > 0){
            showPopupMenu(v,  mCommentList.get(position), position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Comment Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(CommentActivity.this,UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(CommentActivity.this,UserOnlineStatus.OFFLINE);
    }

    private void showPopupMenu(View v, Comment.CommentData feedItem, int itemPosition) {
        PopupMenu popup = new PopupMenu(mCommentActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.over_flow, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(feedItem, itemPosition));
        popup.show();
    }


    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private Comment.CommentData feedItem;
        private int itemPosition;
        public MyMenuItemClickListener(Comment.CommentData mFeedItem, int position) {
            this.feedItem = mFeedItem;
            this.itemPosition = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    Comment.CommentData mCommentData = mCommentList.get(itemPosition);
                    if(mCommentList != null && mCommentList.size() > 0){
                        showAlertDialog(mRecyclerView,mCommentData.getComment(), mCommentData.getPostId(), mCommentData.getUserId(),mCommentData.getCommentId());
                    }
                    return true;
                case R.id.action_delete:
                    if (feedItem != null ) {
                        if(CheckNetworkConnection.isConnectionAvailable(mCommentActivity)){
                            Comment.CommentData data = mCommentList.get(itemPosition);
                            if(mCommentList != null && mCommentList.size() > 0){
                                mCommentList.remove(itemPosition);
                                mCommentAdapter.notifyItemRemoved(itemPosition);
                                deletingComment(data.getPostId(), data.getCommentId());
                            }
                        } else {
                            if(mRecyclerView != null){
                                SnackBarDialog.showNoInternetError(mRecyclerView);
                            }
                        }
                    }
                    return true;
                default:
            }
            return false;
        }
    }


    private void showAlertDialog(final View view, final String comment, final String postId, final String friendId, final String editId) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mCommentActivity);
        @SuppressLint("InflateParams") View mView = layoutInflaterAndroid.inflate(R.layout.forgot_password_edit_text, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mCommentActivity);
        alertDialogBuilderUserInput.setView(mView);

        final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
        if(userInputDialogEditText != null){
            userInputDialogEditText.setText(comment);
            userInputDialogEditText.setSelection(userInputDialogEditText.getText().length());
            alertDialogBuilderUserInput
                    .setCancelable(false)
                    .setPositiveButton("Send", (dialogBox, id) -> {
                        String comment1 = userInputDialogEditText.getText().toString();
                        if (!comment1.equalsIgnoreCase("")) {
                            if (CheckNetworkConnection.isConnectionAvailable(mCommentActivity)) {
                                sendEditComment(postId,friendId,editId, comment1);
                                UserUtils.hideKeyBoard(getApplicationContext(), userInputDialogEditText );
                                Toast.makeText(getApplicationContext(),"Editing comment...", Toast.LENGTH_SHORT).show();
                            } else {
                                SnackBarDialog.show(userInputDialogEditText,"No internet connection available.");
                            }
                        } else {
                            Toast.makeText(mCommentActivity, "Comment shouldn't be empty", Toast.LENGTH_SHORT).show();
                        }

                    })

                    .setNegativeButton("Cancel",
                            (dialogBox, id) -> dialogBox.cancel());

            AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
            alertDialogAndroid.show();
        }

    }

    private void sendEditComment(String postId, String friendId, String editId, String comment) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Comment> call = apiService.editComment(userId, postId, friendId, editId, comment,apiKey);
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull retrofit2.Response<Comment> response) {
                if(response.body() != null && response.isSuccessful()){
                    Comment mComment = response.body();
                    if(mComment != null && !mComment.isErrorStatus()){
                        mCommentList.clear();
                        if (!mComment.isErrorStatus()){
                            mCommentList.addAll(mComment.getData());
                            mCommentAdapter.notifyDataSetChanged();
                        }
                        Log.d("sendEditComment", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                Log.d("sendEditComment", "onFailure");
            }
        });
    }


    private void deletingComment(String postId, String commentId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Comment> call = apiService.deleteComment(userId, postId, commentId,apiKey);
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull retrofit2.Response<Comment> response) {

                if(response.body() != null && response.isSuccessful()){
                    Comment mDeletePost = response.body();
                    if(mDeletePost != null && !mDeletePost.isErrorStatus()){
                        Log.d("deletingComment", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                Log.d("deletingComment", "onFailure");
            }
        });
    }

}
