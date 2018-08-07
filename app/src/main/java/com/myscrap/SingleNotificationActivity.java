package com.myscrap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.myscrap.application.AppController;
import com.myscrap.model.Feed;
import com.myscrap.model.LikedData;
import com.myscrap.model.PictureUrl;
import com.myscrap.model.SinglePost;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingleNotificationActivity extends AppCompatActivity {


    @BindView(R.id.ivFeedCenter)
    ImageView ivFeedCenter;
    @BindView(R.id.btnComments)
    ImageButton btnComments;
    @BindView(R.id.btnLike)
    ImageButton btnLike;
    @BindView(R.id.vBgLike)
    View vBgLike;
    @BindView(R.id.ivLike)
    ImageView ivLike;
    @BindView(R.id.tsLikesCounter)
    TextSwitcher tsLikesCounter;
    @BindView(R.id.tsCommentsCounter)
    TextSwitcher tsCommentCounter;
    @BindView(R.id.vImageRoot)
    FrameLayout vImageRoot;

    @BindView(R.id.icon_back)
    RelativeLayout iconBack;
    @BindView(R.id.icon_front)
    RelativeLayout iconFront;
    @BindView(R.id.icon_profile)
    ImageView iconProfile;
    @Nullable
    @BindView(R.id.overflow)
    ImageView overflow;
    @BindView(R.id.icon_text)
    TextView iconText;

    @BindView(R.id.profileName)
    TextView profileName;
    @BindView(R.id.company)
    TextView company;
    @BindView(R.id.designation)
    TextView designation;
    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.time)
    TextView timeStamp;

    @BindView(R.id.card_view)
    CardView cardView;

    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    private SingleNotificationActivity mSingleNotificationActivity;

    Feed.FeedItem mFeedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mSingleNotificationActivity = this;
        ButterKnife.bind(this);
        Intent mIntent = getIntent();
        String postId = mIntent.getStringExtra("postId");
        String NotificationId = mIntent.getStringExtra("NotificationId");
        loadFeeds(toolbar, postId, NotificationId);
    }

    private void loadFeeds(final Toolbar v, String postId, String notificationId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ProgressBarDialog.showLoader(mSingleNotificationActivity, false);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(mSingleNotificationActivity);
            Call<SinglePost> call = apiService.singleFeeds(userId, postId, notificationId, apiKey);
            call.enqueue(new Callback<SinglePost>() {
                @Override
                public void onResponse(@NonNull Call<SinglePost> call, @NonNull Response<SinglePost> response) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("loadFeeds", "onSuccess");
                    if (response.body() != null) {

                        SinglePost mSinglePost = response.body();
                        if (mSinglePost != null) {
                            if(!mSinglePost.isErrorStatus()){
                                if(cardView != null)
                                    cardView.setVisibility(View.VISIBLE);
                                final List<Feed.FeedItem> mFeedItems = mSinglePost.getData();
                                if(mFeedItems != null && mFeedItems.size() > 0){
                                    mFeedItem = mFeedItems.get(0);
                                    if(mFeedItem != null) {
                                        String[] split = mFeedItem.getPostedUserName().split("\\s+");
                                        if (!mFeedItem.getProfilePic().equalsIgnoreCase("")){
                                            /*Glide.with(getBaseContext()).load(mFeedItem.getProfilePic())
                                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                    .transform(new CircleTransform(getBaseContext()))
                                                    .into(iconProfile);*/
                                            iconProfile.setColorFilter(null);
                                            iconText.setVisibility(View.GONE);
                                        } else {
                                            iconProfile.setImageResource(R.drawable.bg_circle);
                                            iconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(getBaseContext(), "400"));
                                            iconText.setVisibility(View.VISIBLE);
                                            if (!mFeedItem.getPostedUserName().equalsIgnoreCase("")){
                                                if (split.length > 1){
                                                    String first = split[0].substring(0,1);
                                                    String last = split[1].substring(0,1);
                                                    String initial = first + " "+ last;
                                                    iconText.setText(initial);
                                                }
                                            }
                                        }

                                        final String word1 = mFeedItem.getPostedUserName().trim();
                                        String word2 = " updated profile picture.";
                                        String name = word1+ word2;

                                        if (mFeedItem.getPostType() != null && mFeedItem.getPostType().equalsIgnoreCase("userProfilePost") || mFeedItem.getPostType() != null && mFeedItem.getPostType().equalsIgnoreCase("friendProfilePost")){
                                            ClickableSpan nameClickableSpan = new ClickableSpan() {

                                                @Override
                                                public void updateDrawState(TextPaint ds) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                        ds.setColor(getBaseContext().getResources().getColor(R.color.black, getBaseContext().getTheme()));
                                                    } else {
                                                        ds.setColor(getBaseContext().getResources().getColor(R.color.black));
                                                    }
                                                    ds.setUnderlineText(false);
                                                }

                                                @Override
                                                public void onClick(View view) {
                                                    Toast.makeText(getBaseContext(), word1, Toast.LENGTH_SHORT).show();
                                                }
                                            };

                                            ForegroundColorSpan spanColorBlack = new ForegroundColorSpan(Color.BLACK);
                                            ForegroundColorSpan spanColorGray = new ForegroundColorSpan(Color.GRAY);
                                            SpannableStringBuilder sBuilder = new SpannableStringBuilder(word1);
                                            sBuilder.append(" ");
                                            sBuilder.append(word2);
                                            sBuilder.setSpan(
                                                    spanColorBlack,
                                                    0,
                                                    word1.length()+1,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                            );
                                            sBuilder.setSpan(nameClickableSpan,
                                                    name.indexOf(word1),
                                                    name.indexOf(word1) + String.valueOf(word1).length(),
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            sBuilder.setSpan(
                                                    spanColorGray,
                                                    sBuilder.length()-word2.length(),
                                                    sBuilder.length()-1,
                                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                            );
                                            profileName.setText(sBuilder);
                                            profileName.setMovementMethod(LinkMovementMethod.getInstance());
                                        } else if (mFeedItem.getPostType() != null && mFeedItem.getPostType().equalsIgnoreCase("friendUserPost") && mFeedItem.getPostedUserName() != null &&  mFeedItem.getPostedFriendName() != null){
                                            final String userName = mFeedItem.getPostedUserName().trim();
                                            final String rightArrow = "&#9654";
                                            final String friendName = mFeedItem.getPostedFriendName().trim();
                                            String postedName = userName+ rightArrow+ friendName;
                                            ClickableSpan nameClickableSpan = new ClickableSpan() {

                                                @Override
                                                public void updateDrawState(TextPaint ds) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                        ds.setColor(mSingleNotificationActivity.getResources().getColor(R.color.black, mSingleNotificationActivity.getTheme()));
                                                    } else {
                                                        ds.setColor(mSingleNotificationActivity.getResources().getColor(R.color.black));
                                                    }
                                                    ds.setUnderlineText(false);
                                                }

                                                @Override
                                                public void onClick(View view) {
                                                    Toast.makeText(getBaseContext(), word1, Toast.LENGTH_SHORT).show();
                                                  //  onmFeedItemClickListener.onPostFromClick(view, adapterPosition);
                                                }
                                            };

                                            ClickableSpan friendNameClickableSpan = new ClickableSpan() {

                                                @Override
                                                public void updateDrawState(TextPaint ds) {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                        ds.setColor(mSingleNotificationActivity.getResources().getColor(R.color.black, mSingleNotificationActivity.getTheme()));
                                                    } else {
                                                        ds.setColor(mSingleNotificationActivity.getResources().getColor(R.color.black));
                                                    }
                                                    ds.setUnderlineText(false);
                                                }

                                                @Override
                                                public void onClick(View view) {
                                                }
                                            };

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml(userName +  " &#9658 "  + friendName  , Html.FROM_HTML_MODE_LEGACY));
                                                int s1 = userName.length();
                                                int total = spanned.length();
                                                spanned.setSpan(nameClickableSpan, 0, s1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                spanned.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s1, 0);
                                                spanned.setSpan(friendNameClickableSpan, s1+1, total, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                spanned.setSpan(new ForegroundColorSpan(Color.BLACK), s1+1, total, 0);
                                                profileName.setText(spanned);
                                            } else {
                                                final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml(userName +  " &#9658 "  + friendName));
                                                int s1 = userName.trim().length();
                                                int total = spanned.length();
                                                spanned.setSpan(nameClickableSpan, 0, s1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                spanned.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s1, 0);
                                                spanned.setSpan(friendNameClickableSpan, s1+1, total, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                spanned.setSpan(new ForegroundColorSpan(Color.BLACK), s1+1, total, 0);
                                                profileName.setText(spanned);
                                            }
                                            profileName.setMovementMethod(LinkMovementMethod.getInstance());
                                        }else {
                                            profileName.setText(mFeedItem.getPostedUserName());
                                            profileName.setOnClickListener(v1 -> Toast.makeText(getBaseContext(), word1, Toast.LENGTH_SHORT).show());
                                        }

                                        designation.setVisibility(View.GONE);

                                        if (mFeedItem.getStatus() != null && !mFeedItem.getStatus().equalsIgnoreCase("")){
                                            status.setText(mFeedItem.getStatus());
                                            status.setVisibility(View.VISIBLE);
                                        } else {
                                            status.setVisibility(View.GONE);
                                        }

                                        if (!mFeedItem.getTimeStamp().equalsIgnoreCase("")){
                                            String timeAGO = UserUtils.getTimeAgo(Long.parseLong(UserUtils.parsingLong(mFeedItem.getTimeStamp())));
                                            timeStamp.setText(timeAGO);
                                            timeStamp.setVisibility(View.VISIBLE);
                                        } else {
                                            timeStamp.setVisibility(View.GONE);
                                        }

                                        final List<PictureUrl> mPictureUrl = mFeedItem.getPictureUrl();
                                        if (mPictureUrl != null && mPictureUrl.size() != 0) {
                                           /* Glide.with(getBaseContext()).load(mPictureUrl.get(0).getImages())
                                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                    .into(ivFeedCenter);*/
                                            ivFeedCenter.setVisibility(View.VISIBLE);
                                            vImageRoot.setVisibility(View.VISIBLE);
                                        } else {
                                            ivFeedCenter.setVisibility(View.GONE);
                                            vImageRoot.setVisibility(View.GONE);
                                        }


                                        ivFeedCenter.setOnClickListener(v12 -> {
                                            final List<PictureUrl> mPictureUrl1 = mFeedItem.getPictureUrl();
                                            if (mPictureUrl1 != null && mPictureUrl1.size() != 0) {
                                                Bundle bundle = new Bundle();
                                               //bundle.putString("images", mPictureUrl.get(0).getImages());
                                                bundle.putSerializable("images", (Serializable) mPictureUrl1);
                                                FragmentTransaction ft = mSingleNotificationActivity.getSupportFragmentManager().beginTransaction();
                                                FeedImagesSlideshowDialogFragment newFragment = FeedImagesSlideshowDialogFragment.newInstance();
                                                newFragment.setArguments(bundle);
                                                newFragment.show(ft, "slideshow");
                                            }

                                        });


                                        btnLike.setImageResource(mFeedItem.isLikeStatus() ? R.drawable.ic_heart_outline_black_filled : R.drawable.ic_heart_outline_black);

                                        btnLike.setOnClickListener(v13 -> {
                                            int likeCount = 0;
                                            if(mFeedItem.isLikeStatus()){
                                                likeCount = mFeedItem.getLikeCount();
                                                if(likeCount > 0){
                                                    mFeedItem.setLikeCount(likeCount-1);
                                                }
                                                mFeedItem.setLikeStatus(false);
                                            } else {
                                                likeCount = mFeedItem.getLikeCount();
                                                mFeedItem.setLikeCount(likeCount+1);
                                                mFeedItem.setLikeStatus(true);
                                            }
                                            btnLike.setImageResource(mFeedItem.isLikeStatus() ? R.drawable.ic_heart_outline_black_filled : R.drawable.ic_heart_outline_black);
                                            animateHeartButton(mFeedItem.isLikeStatus());
                                            if(tsCommentCounter != null){
                                                if(mFeedItem.getCommentCount()==0) {
                                                    tsCommentCounter.setCurrentText(mSingleNotificationActivity.getString(R.string.feed_comment));
                                                } else {
                                                    tsCommentCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                                            R.plurals.comments_count, mFeedItem.getCommentCount(), mFeedItem.getCommentCount()
                                                    ));
                                                }
                                            }

                                            if(tsLikesCounter != null){
                                                if(mFeedItem.getLikeCount()==0) {
                                                    tsLikesCounter.setCurrentText(mSingleNotificationActivity.getString(R.string.feed_like));
                                                } else {
                                                    tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                                            R.plurals.likes_count, mFeedItem.getLikeCount(), mFeedItem.getLikeCount()
                                                    ));
                                                }
                                            }
                                            if(CheckNetworkConnection.isConnectionAvailable(mSingleNotificationActivity))
                                                doLike(mFeedItem);
                                        });



                                        if(tsCommentCounter != null){
                                            if(mFeedItem.getCommentCount()==0) {
                                                tsCommentCounter.setCurrentText(mSingleNotificationActivity.getString(R.string.feed_comment));
                                            } else {
                                                tsCommentCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                                        R.plurals.comments_count, mFeedItem.getCommentCount(), mFeedItem.getCommentCount()
                                                ));
                                            }
                                        }

                                        if(tsLikesCounter != null){
                                            if(mFeedItem.getLikeCount()==0) {
                                                tsLikesCounter.setCurrentText(mSingleNotificationActivity.getString(R.string.feed_like));
                                            } else {
                                                tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                                        R.plurals.likes_count, mFeedItem.getLikeCount(), mFeedItem.getLikeCount()
                                                ));
                                            }
                                        }

                                        if (tsLikesCounter != null) {
                                            tsLikesCounter.setOnClickListener(v14 -> {
                                                if(mFeedItem.getLikeCount() > 0)
                                                    screenMoveToLikeActivity(mFeedItem.getPostId(), mFeedItem.getPostedUserId(), mFeedItem.getLikeCount());
                                            });
                                        }
                                        if (tsCommentCounter != null) {
                                            tsCommentCounter.setOnClickListener(v15 -> screenMoveToCommentActivity(mFeedItem.getPostId(), mFeedItem.getPostedUserId()));
                                        }

                                    }
                                }
                            } else {
                                if(v != null)
                                    SnackBarDialog.show(v, mSinglePost.getStatus());
                            }
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<SinglePost> call, @NonNull Throwable t) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("loadFeeds", "onFailure");
                    if(cardView != null)
                        cardView.setVisibility(View.GONE);
                }
            });
        } else {
            if(v != null)
                SnackBarDialog.showNoInternetError(v);
        }
    }


    private void animateHeartButton(final boolean likeStatus) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(btnLike, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(btnLike, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(btnLike, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(likeStatus)
                    btnLike.setImageResource(R.drawable.ic_heart_outline_black_filled);
                else
                    btnLike.setImageResource(R.drawable.ic_heart_outline_black);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
        animatorSet.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.OFFLINE);
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
            public void onResponse(@NonNull Call<LikedData> call, @NonNull retrofit2.Response<LikedData> response) {
                Log.d("doLike", "onSuccess");
                if(response.body() != null && response.isSuccessful()){
                    LikedData mLikedData = response.body();
                    if(mLikedData != null && !mLikedData.getError()){
                        LikedData.InsertLikeData  data = mLikedData.getInsertLikeData();
                        if(data != null) {
                            if(mFeedItem.getPostId().equalsIgnoreCase(data.getPostId())){
                                mFeedItem.setLikeStatus(data.getLikeStatus());
                                mFeedItem.setLikeCount(Integer.parseInt(UserUtils.parsingInteger(data.getLikeCount())));
                                btnLike.setImageResource(mFeedItem.isLikeStatus() ? R.drawable.ic_heart_outline_black_filled : R.drawable.ic_heart_outline_black);
                                if(tsLikesCounter != null){
                                    if(mFeedItem.getLikeCount()==0) {
                                        tsLikesCounter.setCurrentText(mSingleNotificationActivity.getString(R.string.feed_like));
                                    } else {
                                        tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                                R.plurals.likes_count, mFeedItem.getLikeCount(), mFeedItem.getLikeCount()
                                        ));
                                    }
                                }

                                if(tsLikesCounter != null){
                                    if(mFeedItem.getLikeCount()==0) {
                                        tsLikesCounter.setCurrentText(mSingleNotificationActivity.getString(R.string.feed_like));
                                    } else {
                                        tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                                R.plurals.likes_count, mFeedItem.getLikeCount(), mFeedItem.getLikeCount()
                                        ));
                                    }
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


    private void screenMoveToCommentActivity(String postId, String postedUserId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        final Intent intent = new Intent(mSingleNotificationActivity, CommentActivity.class);
        intent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        intent.putExtra("postId", postId);
        intent.putExtra("postedUserId", postedUserId);
        intent.putExtra("apiKey", UserUtils.getApiKey(mSingleNotificationActivity));
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop()){
            if(mSingleNotificationActivity != null)
                mSingleNotificationActivity.overridePendingTransition(0, 0);
        }
    }


    private void screenMoveToLikeActivity(String postId, String postedUserId, int likeCount) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        Intent mIntent = new Intent(mSingleNotificationActivity, LikeActivity.class);
        mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        mIntent.putExtra("postedUserId", postedUserId);
        mIntent.putExtra("count", likeCount);
        mIntent.putExtra("postId", postId);
        mIntent.putExtra("apiKey", UserUtils.getApiKey(mSingleNotificationActivity));
        mSingleNotificationActivity.startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop()) {
            mSingleNotificationActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
