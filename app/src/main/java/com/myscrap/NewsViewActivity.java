package com.myscrap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.adapters.CommentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Comment;
import com.myscrap.model.LikedData;
import com.myscrap.model.PictureUrl;
import com.myscrap.model.SingleNews;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.LinkTransformationMethod;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.MultiTouchViewPager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsViewActivity extends AppCompatActivity implements CommentAdapter.CommentAdapterClickListener{

   // @BindView(R.id.ivFeedCenter)
   // SimpleDraweeView ivFeedCenter;
    @BindView(R.id.btnComments)
    ImageButton btnComments;
    @BindView(R.id.btnLike)
    ImageButton btnLike;
    @BindView(R.id.vBgLike)
    View vBgLike;
    @BindView(R.id.ivFeedBottom)
    ImageView ivFeedBottom;
    @BindView(R.id.ivLike)
    ImageView ivLike;
    @BindView(R.id.like_text)
    TextView like;
    @BindView(R.id.comment)
    TextView comment;
    @BindView(R.id.vImageRoot)
    FrameLayout vImageRoot;

    @BindView(R.id.tsLikesCounter)
    TextSwitcher tsLikesCounter;
    @BindView(R.id.dot)
    TextView tsLikesCommentDot;
    @BindView(R.id.tsCommentsCounter)
    TextSwitcher tsCommentCounter;

    @BindView(R.id.view_pager)
    MultiTouchViewPager viewPager;
    @BindView(R.id.left_right_layout)
    RelativeLayout leftRightLayout;
    @BindView(R.id.view_pager_layout)
    FrameLayout viewPagerLayout;
    @BindView(R.id.left)
    ImageView left;
    @BindView(R.id.right)
    ImageView right;
    @BindView(R.id.layoutDots)
    LinearLayout dotsLayout;

    @BindView(R.id.heading)
    TextView heading;
    @BindView(R.id.sub_head_lines)
    TextView subHeading;
    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.author)
    TextView author;
    @BindView(R.id.news_link)
    TextView newsLink;
    @BindView(R.id.author_company)
    TextView authorCompany;
    @BindView(R.id.location)
    TextView location;
    @BindView(R.id.root_bottom_view)
    LinearLayout rootBottomView;


    private  NewsViewActivity mNewsViewActivity;
    private List<SingleNews.SingleNewsData> mNewsDataList = new ArrayList<>();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    private SingleNews.SingleNewsData mFeedItem;
    private RelativeLayout rootLayout;
    private SwipeRefreshLayout swipe;
    private List<PictureUrl> imageList = new ArrayList<>();
    private CommentAdapter mCommentAdapter;
    private List<Comment.CommentData> mCommentList = new ArrayList<>();
    private EmojiconEditText emojiconEditText;
    private String userId;
    private String postedUserId;
    RecyclerView commentsRv;
    private String newsId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_view_latest);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        ButterKnife.bind(this);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        mNewsViewActivity = this;
        CommentAdapter.CommentAdapterClickListener mCommentAdapterClickListener = this;
        userId = AppController.getInstance().getPrefManager().getUser().getId();
        Intent mIntent = getIntent();
        newsId = mIntent.getStringExtra("newsId");
        commentsRv = (RecyclerView) findViewById(R.id.comment_rv);
        mCommentAdapter = new CommentAdapter(this, mCommentList, mCommentAdapterClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        commentsRv.setLayoutManager(linearLayoutManager);
        commentsRv.setNestedScrollingEnabled(false);
        commentsRv.setItemAnimator(new DefaultItemAnimator());
        commentsRv.setAdapter(mCommentAdapter);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);
        swipe.setOnRefreshListener(() -> {
            if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                loadNews(rootLayout, newsId, userId);
            }
        });

        ImageView smileyImageView = (ImageView) findViewById(R.id.emoji_btn);
        final ImageView submitButton = (ImageView) findViewById(R.id.submit_btn);
        if (smileyImageView != null)
            smileyImageView.setColorFilter(Color.parseColor("#388E3C"));
        View rootView = findViewById(R.id.root_bottom_view);
        emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        EmojIconActions smileyIcon = new EmojIconActions(this, rootView, emojiconEditText, smileyImageView);
        smileyIcon.ShowEmojIcon();
        smileyIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        smileyIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
            }
            @Override
            public void onKeyboardClose() {
            }
        });
        UserUtils.hideKeyBoard(mNewsViewActivity, emojiconEditText);
        if(emojiconEditText != null)
            emojiconEditText.setOnEditorActionListener((v, id, event) -> {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    if(!UserUtils.isGuestLoggedIn(mNewsViewActivity)){
                        if (CheckNetworkConnection.isConnectionAvailable(mNewsViewActivity))
                            doComment(newsId, userId);
                        else
                        if(emojiconEditText != null)
                            SnackBarDialog.showNoInternetError(emojiconEditText);
                        return true;
                    } else {
                        GuestLoginDialog.show(mNewsViewActivity);
                    }

                }
                return false;
            });


        emojiconEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0){
                    submitButton.setColorFilter(ContextCompat.getColor(mNewsViewActivity, R.color.colorPrimary));
                } else {
                    submitButton.setColorFilter(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submitButton.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(mNewsViewActivity)){
                GuestLoginDialog.show(mNewsViewActivity);
                return;
            }
            if (TextUtils.isEmpty(emojiconEditText.getText().toString())){
                Toast.makeText(mNewsViewActivity, "Write a comment", Toast.LENGTH_SHORT).show();
            } else {
                if (CheckNetworkConnection.isConnectionAvailable(mNewsViewActivity))
                    doComment(newsId, postedUserId);
                else
                    SnackBarDialog.showNoInternetError(submitButton);
            }
        });
    }


    private void doComment(String postId, String postedUserId) {
        if(emojiconEditText != null){
            if(CheckNetworkConnection.isConnectionAvailable(getBaseContext())){
                if (!TextUtils.isEmpty(emojiconEditText.getText().toString())){
                    String message = emojiconEditText.getText().toString().trim();
                    sendComment(message, postId, postedUserId);
                    emojiconEditText.setText("");
                    UserUtils.hideKeyBoard(mNewsViewActivity, emojiconEditText);
                }
            } else {
                SnackBarDialog.showNoInternetError(emojiconEditText);
            }
        }
    }

    private void sendComment(String comment, String postId, String postedUserId) {
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
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void loadNews(final View v, String newsId, String userId ) {
        if (CheckNetworkConnection.isConnectionAvailable(NewsViewActivity.this)){

            if(swipe != null)
                swipe.setRefreshing(true);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(mNewsViewActivity);
                    apiService.singleNews(userId, newsId, apiKey)
                            .enqueue(new Callback<SingleNews>() {
                                @Override
                                public void onResponse(@NonNull Call<SingleNews> call, @NonNull Response<SingleNews> response) {
                                    Log.d("loadNews", "onSuccess");
                                    if(swipe != null)
                                        swipe.setRefreshing(false);
                                    if (mNewsDataList != null)
                                        mNewsDataList.clear();
                                    if (response.isSuccessful()) {
                                        if (response.body() != null) {
                                            SingleNews mSingleNews = response.body();
                                            if (mSingleNews != null) {
                                                parseSingleNews(v, mSingleNews);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<SingleNews> call, @NonNull Throwable t) {
                                    if(swipe != null)
                                        swipe.setRefreshing(false);

                                    if(rootLayout != null)
                                        rootLayout.setVisibility(View.GONE);
                                    Log.d("loadNews", "onFailure");
                                }
                            });
        } else {
            if(v != null)
                SnackBarDialog.showNoInternetError(v);
        }
    }

    private void parseSingleNews(View v, SingleNews mSingleNews) {
        if (mSingleNews != null) {
            if(!mSingleNews.isErrorStatus()){
                mNewsDataList = mSingleNews.getData();
                if(mNewsDataList != null){
                    mFeedItem = mNewsDataList.get(0);
                    if(mFeedItem != null ){
                        postedUserId = mFeedItem.getPostedUserId();
                        if( mNewsDataList != null && mNewsDataList.size() > 0) {
                            addBottomDots(0);
                            MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
                            viewPager.setAdapter(myViewPagerAdapter);
                            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                                @Override
                                public void onPageSelected(int position) {
                                    addBottomDots(position);
                                }

                                @Override
                                public void onPageScrolled(int arg0, float arg1, int arg2) {

                                }

                                @Override
                                public void onPageScrollStateChanged(int arg0) {

                                }
                            });

                            if(mFeedItem.getHeading() != null && !mFeedItem.getHeading().equalsIgnoreCase("")){
                                heading.setText(mFeedItem.getHeading());
                                heading.setVisibility(View.VISIBLE);
                            } else {
                                heading.setVisibility(View.GONE);
                            }

                            if(mFeedItem.getSubHeading() != null && !mFeedItem.getSubHeading().equalsIgnoreCase("")){
                                subHeading.setText(mFeedItem.getSubHeading());
                                subHeading.setVisibility(View.VISIBLE);
                            } else {
                                subHeading.setVisibility(View.GONE);
                            }

                            if(mFeedItem.getPublisherMagazine() != null && !mFeedItem.getPublisherMagazine().equalsIgnoreCase("") && mFeedItem.getTimeStamp() != null && !mFeedItem.getTimeStamp().equalsIgnoreCase("")){
                                String twoSpaces = getResources().getString(R.string.two_spaces);
                                if(UserUtils.getNewsTime(Long.parseLong(UserUtils.parsingLong(mFeedItem.getTimeStamp()))) != null){
                                    String timeAgo = twoSpaces + UserUtils.getNewsTime(Long.parseLong(UserUtils.parsingLong(mFeedItem.getTimeStamp())));
                                    String dot = getResources().getString(R.string.dot);
                                    SpannableStringBuilder spanned;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        spanned = new SpannableStringBuilder(Html.fromHtml(timeAgo +  dot + mFeedItem.getPublisherMagazine(), Html.FROM_HTML_MODE_LEGACY));
                                    } else {
                                        spanned = new SpannableStringBuilder(Html.fromHtml(timeAgo +  dot + mFeedItem.getPublisherMagazine()));
                                    }

                                    spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(),R.color.black)), 0, spanned.toString().indexOf(dot)+1, 0);
                                    spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary)),spanned.toString().indexOf(dot)+2, spanned.length(), 0);
                                    time.setText(spanned);
                                    time.setVisibility(View.VISIBLE);
                                    time.setOnClickListener(v1 -> {
                                        if(mFeedItem.getPublisherUrl() != null && !mFeedItem.getPublisherUrl().equalsIgnoreCase("")){
                                            goToWeb(mFeedItem.getPublisherUrl());
                                        }
                                    });
                                } else {
                                    time.setVisibility(View.GONE);
                                }
                            } else {
                                time.setVisibility(View.GONE);
                            }

                            if (mFeedItem.getStatus() != null && !mFeedItem.getStatus().equalsIgnoreCase("")){
                                if(mFeedItem.getPublishLocation() != null && !mFeedItem.getPublishLocation().equalsIgnoreCase("")){
                                    String locationWithContent = mFeedItem.getPublishLocation().trim() +": "+ mFeedItem.getStatus();
                                    status.setText(locationWithContent);
                                } else {
                                    status.setText(mFeedItem.getStatus());
                                }
                                status.setTransformationMethod(new LinkTransformationMethod(mNewsViewActivity));
                                status.setMovementMethod(LinkMovementMethod.getInstance());
                                status.setVisibility(View.VISIBLE);
                            } else {
                                status.setVisibility(View.GONE);
                            }


                            List<PictureUrl> mPictureUrl = mFeedItem.getPictureUrl();
                            if (mPictureUrl != null && mPictureUrl.size() != 0) {
                                imageList = mPictureUrl;
                                if(!mPictureUrl.get(0).getImages().equalsIgnoreCase("")){
                                    addBottomDots(0);
                                    viewPagerLayout.setVisibility(View.VISIBLE);
                                }
                                viewPagerLayout.setVisibility(View.VISIBLE);
                                vImageRoot.setVisibility(View.VISIBLE);
                                //myViewPagerAdapter.notifyDataSetChanged();
                                myViewPagerAdapter.swap(imageList, 0);
                            } else {
                                viewPagerLayout.setVisibility(View.GONE);
                                vImageRoot.setVisibility(View.GONE);
                            }
                        }


                        if(mFeedItem.getCommentData() != null){
                            mCommentList.clear();
                            mCommentList.addAll(mFeedItem.getCommentData());
                            mCommentAdapter.notifyDataSetChanged();
                        }

                        btnLike.setImageResource(mFeedItem.isLikeStatus() ? R.drawable.ic_heart_outline_black_filled : R.drawable.ic_heart_outline_black);

                        btnLike.setOnClickListener(v12 -> {
                            int likeCount;
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
                                    tsCommentCounter.setCurrentText(mNewsViewActivity.getString(R.string.feed_comment));
                                } else {
                                    tsCommentCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                            R.plurals.comments_count, mFeedItem.getCommentCount(), mFeedItem.getCommentCount()
                                    ));
                                }
                            }

                            if(tsLikesCounter != null){
                                if(mFeedItem.getLikeCount()==0) {
                                    tsLikesCounter.setCurrentText(mNewsViewActivity.getString(R.string.feed_like));
                                } else {
                                    tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                            R.plurals.likes_count, mFeedItem.getLikeCount(), mFeedItem.getLikeCount()
                                    ));
                                }
                            }
                            if(mFeedItem.getLikeCount() > 0 && mFeedItem.getCommentCount() > 0){
                                tsLikesCounter.setVisibility(View.VISIBLE);
                                tsCommentCounter.setVisibility(View.VISIBLE);
                                tsLikesCommentDot.setVisibility(View.VISIBLE);
                            } else if (mFeedItem.getLikeCount() > 0 && mFeedItem.getCommentCount() ==  0){
                                tsLikesCounter.setVisibility(View.VISIBLE);
                                tsCommentCounter.setVisibility(View.GONE);
                                tsLikesCommentDot.setVisibility(View.GONE);
                            } else if (mFeedItem.getLikeCount() == 0 && mFeedItem.getCommentCount() >  0){
                                tsLikesCounter.setVisibility(View.GONE);
                                tsCommentCounter.setVisibility(View.VISIBLE);
                                tsLikesCommentDot.setVisibility(View.GONE);
                            } else {
                                tsLikesCounter.setVisibility(View.GONE);
                                tsCommentCounter.setVisibility(View.GONE);
                                tsLikesCommentDot.setVisibility(View.GONE);
                            }
                            if(UserUtils.isGuestLoggedIn(mNewsViewActivity)){
                                GuestLoginDialog.show(mNewsViewActivity);
                                return;
                            }
                            if(CheckNetworkConnection.isConnectionAvailable(mNewsViewActivity))
                                doLike(mFeedItem);
                        });



                        if(tsCommentCounter != null){
                            if(mFeedItem.getCommentCount()==0) {
                                tsCommentCounter.setCurrentText(mNewsViewActivity.getString(R.string.feed_comment));
                            } else {
                                tsCommentCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                        R.plurals.comments_count, mFeedItem.getCommentCount(), mFeedItem.getCommentCount()
                                ));
                            }
                        }

                        if(tsLikesCounter != null){
                            if(mFeedItem.getLikeCount()==0) {
                                tsLikesCounter.setCurrentText(mNewsViewActivity.getString(R.string.feed_like));
                            } else {
                                tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                        R.plurals.likes_count, mFeedItem.getLikeCount(), mFeedItem.getLikeCount()
                                ));
                            }
                        }

                        if (tsLikesCounter != null)
                        tsLikesCounter.setOnClickListener(v13 -> {
                            if(UserUtils.isGuestLoggedIn(mNewsViewActivity)){
                                GuestLoginDialog.show(mNewsViewActivity);
                                return;
                            }
                            if(mFeedItem.getLikeCount() > 0)
                                screenMoveToLikeActivity(mFeedItem.getPostId(), mFeedItem.getPostedUserId(), mFeedItem.getLikeCount());
                        });
                        if(mFeedItem.getLikeCount() > 0 && mFeedItem.getCommentCount() > 0){
                            tsLikesCounter.setVisibility(View.VISIBLE);
                            tsCommentCounter.setVisibility(View.VISIBLE);
                            tsLikesCommentDot.setVisibility(View.VISIBLE);
                        } else if (mFeedItem.getLikeCount() > 0 && mFeedItem.getCommentCount() ==  0){
                            tsLikesCounter.setVisibility(View.VISIBLE);
                            tsCommentCounter.setVisibility(View.GONE);
                            tsLikesCommentDot.setVisibility(View.GONE);
                        } else if (mFeedItem.getLikeCount() == 0 && mFeedItem.getCommentCount() >  0){
                            tsLikesCounter.setVisibility(View.GONE);
                            tsCommentCounter.setVisibility(View.VISIBLE);
                            tsLikesCommentDot.setVisibility(View.GONE);
                        } else {
                            tsLikesCounter.setVisibility(View.GONE);
                            tsCommentCounter.setVisibility(View.GONE);
                            tsLikesCommentDot.setVisibility(View.GONE);
                        }
                    }
                }
                if(rootLayout != null)
                    rootLayout.setVisibility(View.VISIBLE);
            } else {
                if(v != null)
                    SnackBarDialog.show(v, mSingleNews.getStatus());
                if(rootLayout != null)
                    rootLayout.setVisibility(View.GONE);
            }
        }
    }

    private void goToAuthorProfile(String editorId) {
        final Intent intent = new Intent(mNewsViewActivity, UserFriendProfileActivity.class);
        intent.putExtra("friendId", editorId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(mNewsViewActivity != null)
                mNewsViewActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void goToWeb(String publisherUrl) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setToolbarColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        } else {
            builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setStartAnimations(mNewsViewActivity, R.anim.slide_up, R.anim.slide_down);
            builder.setExitAnimations(mNewsViewActivity, R.anim.slide_down, R.anim.slide_up);
        }*/
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage("com.android.chrome");
        customTabsIntent.launchUrl(mNewsViewActivity, Uri.parse(publisherUrl));
    }

    private void addBottomDots(int currentPage) {
        if(imageList != null) {
            TextView[] dots = new TextView[imageList.size()];
            dotsLayout.removeAllViews();
            for (int i = 0; i < dots.length; i++) {
                dots[i] = new TextView(mNewsViewActivity);
                dots[i].setText(Html.fromHtml("&#8226;"));
                dots[i].setTextSize(35);
                dots[i].setTextColor(ContextCompat.getColor(mNewsViewActivity,R.color.white));
                dotsLayout.addView(dots[i]);
            }

            if(imageList.size() > 0) {
                if(imageList.size() > 1) {
                    leftRightLayout.setVisibility(View.VISIBLE);
                    dotsLayout.setVisibility(View.VISIBLE);
                } else {
                    leftRightLayout.setVisibility(View.GONE);
                    dotsLayout.setVisibility(View.GONE);
                }
                if(currentPage == 0) {
                    left.setVisibility(View.GONE);
                    if(imageList.size() > 1){
                        right.setVisibility(View.VISIBLE);
                    } else {
                        right.setVisibility(View.GONE);
                    }
                }else if (currentPage == imageList.size() - 1) {
                    left.setVisibility(View.VISIBLE);
                    right.setVisibility(View.GONE);
                } else {
                    left.setVisibility(View.VISIBLE);
                    right.setVisibility(View.VISIBLE);
                }
            }

            left.setOnClickListener(v -> {
                int current = viewPager.getCurrentItem() -1;
                if (current < imageList.size()) {
                    // move to previous screen
                    viewPager.setCurrentItem(current);
                }
            });

            right.setOnClickListener(v -> {
                int current = getItem(+1);
                if (current < imageList.size()) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                }
            });

            if (dots.length > 0)
                dots[currentPage].setTextColor(ContextCompat.getColor(mNewsViewActivity,R.color.colorPrimary));
        }

    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }


    private void animateHeartButton(final boolean likeStatus) {
        AnimatorSet animatorSet = new AnimatorSet();

        /*ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(btnLike, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);*/

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
                // heartAnimationsMap.remove(holder);
                //  dispatchChangeFinishedIfAllAnimationsEnded(holder);
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY);
        animatorSet.start();
    }


    private void doLike(SingleNews.SingleNewsData feedItem) {
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
                                if(tsLikesCounter!= null){
                                    if(mFeedItem.getLikeCount()==0) {
                                        tsLikesCounter.setCurrentText(mNewsViewActivity.getString(R.string.feed_like));
                                    } else {
                                        tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                                R.plurals.likes_count, mFeedItem.getLikeCount(), mFeedItem.getLikeCount()
                                        ));
                                    }
                                }

                                if(tsLikesCounter != null){
                                    if(mFeedItem.getLikeCount()==0) {
                                        tsLikesCounter.setCurrentText(mNewsViewActivity.getString(R.string.feed_like));
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

    @Override
    protected void onResume() {
        super.onResume();
        loadNews(rootLayout, newsId, userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void screenMoveToCommentActivity(String postId, String postedUserId) {
        final Intent intent = new Intent(mNewsViewActivity, CommentActivity.class);
        intent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        intent.putExtra("postId", postId);
        intent.putExtra("postedUserId", postedUserId);
        intent.putExtra("apiKey", UserUtils.getApiKey(mNewsViewActivity));
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop()){
            if(mNewsViewActivity != null)
                mNewsViewActivity.overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onOverFlowClickListener(View v, int position) {
        if(UserUtils.isGuestLoggedIn(mNewsViewActivity)){
            GuestLoginDialog.show(mNewsViewActivity);
            return;
        }
        if(mCommentList != null && mCommentList.size() > 0){
            showPopupMenu(v,  mCommentList.get(position), position);
        }
    }


    private void showPopupMenu(View v, Comment.CommentData feedItem, int itemPosition) {
        PopupMenu popup = new PopupMenu(mNewsViewActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.over_flow, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyCommentMenuItemClickListener(feedItem, itemPosition));
        popup.show();
    }


    private class MyCommentMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private Comment.CommentData feedItem;
        private int itemPosition;
        public MyCommentMenuItemClickListener(Comment.CommentData mFeedItem, int position) {
            this.feedItem = mFeedItem;
            this.itemPosition = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    Comment.CommentData mCommentData = mCommentList.get(itemPosition);
                    if(mCommentList != null && mCommentList.size() > 0){
                        showAlertDialog(commentsRv,mCommentData.getComment(), mCommentData.getPostId(), mCommentData.getUserId(),mCommentData.getCommentId());
                    }
                    return true;
                case R.id.action_delete:
                    if (feedItem != null ) {
                        if(CheckNetworkConnection.isConnectionAvailable(mNewsViewActivity)){
                            Comment.CommentData data = mCommentList.get(itemPosition);
                            if(mCommentList != null && mCommentList.size() > 0){
                                mCommentList.remove(itemPosition);
                                mCommentAdapter.notifyItemRemoved(itemPosition);
                                if( mFeedItem != null) {
                                    mFeedItem.setCommentCount(mCommentList.size());
                                    if(mFeedItem.getLikeCount() > 0 && mFeedItem.getCommentCount() > 0){
                                        tsLikesCounter.setVisibility(View.VISIBLE);
                                        tsCommentCounter.setVisibility(View.VISIBLE);
                                        tsLikesCommentDot.setVisibility(View.VISIBLE);
                                    } else if (mFeedItem.getLikeCount() > 0 && mFeedItem.getCommentCount() ==  0){
                                        tsLikesCounter.setVisibility(View.VISIBLE);
                                        tsCommentCounter.setVisibility(View.GONE);
                                        tsLikesCommentDot.setVisibility(View.GONE);
                                    } else if (mFeedItem.getLikeCount() == 0 && mFeedItem.getCommentCount() >  0){
                                        tsLikesCounter.setVisibility(View.GONE);
                                        tsCommentCounter.setVisibility(View.VISIBLE);
                                        tsLikesCommentDot.setVisibility(View.GONE);
                                    } else {
                                        tsLikesCounter.setVisibility(View.GONE);
                                        tsCommentCounter.setVisibility(View.GONE);
                                        tsLikesCommentDot.setVisibility(View.GONE);
                                    }
                                }

                                deletingComment(data.getPostId(), data.getCommentId());
                            }
                        } else {
                            if(commentsRv != null){
                                SnackBarDialog.showNoInternetError(commentsRv);
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
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mNewsViewActivity);
        @SuppressLint("InflateParams") View mView = layoutInflaterAndroid.inflate(R.layout.forgot_password_edit_text, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mNewsViewActivity);
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
                            if (CheckNetworkConnection.isConnectionAvailable(mNewsViewActivity)) {
                                sendEditComment(postId,friendId,editId, comment1);
                                UserUtils.hideKeyBoard(getApplicationContext(), userInputDialogEditText );
                                Toast.makeText(getApplicationContext(),"Editing comment...", Toast.LENGTH_SHORT).show();
                            } else {
                                SnackBarDialog.show(userInputDialogEditText,"No internet connection available.");
                            }
                        } else {
                            Toast.makeText(mNewsViewActivity, "Comment shouldn't be empty", Toast.LENGTH_SHORT).show();
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

    private class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
        private SimpleDraweeView mPhotoDraweeView;
        MyViewPagerAdapter() {}

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = null;
            if (layoutInflater != null) {
                view = layoutInflater.inflate(R.layout.image_list, container, false);
                mPhotoDraweeView = (SimpleDraweeView) view.findViewById(R.id.image);
                mPhotoDraweeView.setAdjustViewBounds(true);
                Uri uri = Uri.parse(imageList.get(position).getImages());
                mPhotoDraweeView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FOCUS_CROP);
                mPhotoDraweeView.setImageURI(uri);
                try {
                    container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mPhotoDraweeView.setOnClickListener(v -> {
                    if(imageList != null && imageList.size() > 0) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("images", (Serializable) imageList);
                        //bundle.putString("images", feedItem.getPictureUrl().get(0).getImages());
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        FeedImagesSlideshowDialogFragment newFragment = FeedImagesSlideshowDialogFragment.newInstance();
                        newFragment.setArguments(bundle);
                        newFragment.show(ft, "slideshow");
                    }
                });
                mPhotoDraweeView.setOnLongClickListener(v -> {
                    if(imageList != null && imageList.size() > 0) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("images", (Serializable) imageList);
                        //bundle.putString("images", feedItem.getPictureUrl().get(0).getImages());
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        FeedImagesSlideshowDialogFragment newFragment = FeedImagesSlideshowDialogFragment.newInstance();
                        newFragment.setArguments(bundle);
                        newFragment.show(ft, "slideshow");
                    }
                    return false;
                });

                return view;
            }

            return null;
        }

        @Override
        public int getCount() {
            if(imageList != null)
              return imageList.size();
            else
                return 0;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }

        public void swap(List<PictureUrl> mImageList, int i) {
            imageList = mImageList;
            this.notifyDataSetChanged();
        }
    }


    private void screenMoveToLikeActivity(String postId, String postedUserId, int likeCount) {
        Intent mIntent = new Intent(mNewsViewActivity, LikeActivity.class);
        mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        mIntent.putExtra("postedUserId", postedUserId);
        mIntent.putExtra("postId", postId);
        mIntent.putExtra("count", likeCount);
        mIntent.putExtra("apiKey", UserUtils.getApiKey(mNewsViewActivity));
        mNewsViewActivity.startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop()) {
            mNewsViewActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

}
