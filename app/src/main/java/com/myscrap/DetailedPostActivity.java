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
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.myscrap.adapters.CommentAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Comment;
import com.myscrap.model.DeletePost;
import com.myscrap.model.Favourite;
import com.myscrap.model.Feed;
import com.myscrap.model.LikedData;
import com.myscrap.model.PictureUrl;
import com.myscrap.model.Report;
import com.myscrap.model.SinglePostDetails;
import com.myscrap.notification.NotificationUtils;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.LinkPreview;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DetailedPostActivity extends AppCompatActivity implements CommentAdapter.CommentAdapterClickListener, LinkPreview.PreviewListener{

    private EmojiconEditText emojiconEditText;
    private DetailedPostActivity mDetailedPostActivity;

    @BindView(R.id.ivFeedCenter)
    SimpleDraweeView ivFeedCenter;

    @BindView(R.id.ic_report)
    ImageView btnReport;

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

    @BindView(R.id.whoLike)
    ImageView ivwhoLike;
    @BindView(R.id.tsWhoLikes)
    TextSwitcher tsWhoLikes;

    @BindView(R.id.tsWhoComments)
    TextSwitcher tsWhoComments;

    @BindView(R.id.whose_like)
    LinearLayout whoLikeLayout;

    @BindView(R.id.comments)
    LinearLayout commentsLayout;

    @BindView(R.id.root_bottom_view)
    LinearLayout rootBottomView;

    @BindView(R.id.comment_rv)
    RecyclerView commentsRv;

    @BindView(R.id.nested)
    NestedScrollView nestedScrollView;

    @BindView(R.id.icon_badge)
    ImageView iconBadge;


    @BindView(R.id.top)
    TextView top;

    @BindView(R.id.points)
    TextView points;

    @BindView(R.id.vImageRoot)
    FrameLayout vImageRoot;

    @BindView(R.id.has_comments)
    RelativeLayout hasComments;
    @BindView(R.id.icon_front)
    RelativeLayout iconFront;
    @BindView(R.id.icon_profile)
    SimpleDraweeView iconProfile;
    @Nullable
    @BindView(R.id.overflow)
    ImageView overflow;
    @BindView(R.id.favourite)
    ImageView favourite;
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

    @BindView(R.id.cardViewPreview)
    CardView cardViewPreview;
    @BindView(R.id.preview)
    LinkPreview mLinkPreview;



    @BindView(R.id.feeds_main)
    LinearLayout feedsMainLayout;

    @BindView(R.id.new_join_layout)
    LinearLayout newJoinLayout;

    @BindView(R.id.head)
    LinearLayout head;

    @BindView(R.id.joined_time)
    TextView newJoinedTime;

    @BindView(R.id.new_join_icon_profile)
    SimpleDraweeView newJoinIconProfile;

    @BindView(R.id.new_join_icon_text)
    TextView newJoinIconText;

    @BindView(R.id.new_join_top)
    TextView newJoinTop;

    @BindView(R.id.news)
    TextView news;

    @BindView(R.id.new_join_profile_name)
    TextView newJoinProfileName;

    @BindView(R.id.new_join_designation)
    TextView newJoinDesignation;



    private Feed.FeedItem mFeedItem;

    private SwipeRefreshLayout swipe;

    private CommentAdapter mCommentAdapter;
    private List<Comment.CommentData> mCommentList = new ArrayList<>();

    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    private String postId;
    private String postedUserId;
    private String userId;
    private String notId;
    private Tracker mTracker;
    private boolean isShowFeedImages;
    private LinkPreview.PreviewListener mPreviewListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mDetailedPostActivity =this;
        CommentAdapter.CommentAdapterClickListener mCommentAdapterClickListener = this;
        mPreviewListener = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        ButterKnife.bind(this);
        mCommentAdapter = new CommentAdapter(this, mCommentList, mCommentAdapterClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setInitialPrefetchItemCount(4);
        commentsRv.setLayoutManager(linearLayoutManager);
        commentsRv.setNestedScrollingEnabled(false);
        commentsRv.setItemAnimator(new DefaultItemAnimator());
        commentsRv.setAdapter(mCommentAdapter);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorPrimary);
        swipe.setDistanceToTriggerSync(30);//
        swipe.setOnRefreshListener(() ->
        {

            if(CheckNetworkConnection.isConnectionAvailable(mDetailedPostActivity))
            {
                loadFeeds(postId, userId, notId);
            }
            else
            {
                SnackBarDialog.showNoInternetError(swipe);
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
        smileyIcon.setKeyboardListener(new EmojIconActions.KeyboardListener()
        {
            @Override
            public void onKeyboardOpen()
            {
            }
            @Override
            public void onKeyboardClose()
            {
            }
        });
            UserUtils.hideKeyBoard(mDetailedPostActivity, emojiconEditText);
        if(emojiconEditText != null)
            emojiconEditText.setOnEditorActionListener((v, id, event) -> {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    if(!UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                        if (CheckNetworkConnection.isConnectionAvailable(mDetailedPostActivity))
                            doComment(postId, userId);
                        else
                        if(emojiconEditText != null)
                            SnackBarDialog.showNoInternetError(emojiconEditText);
                        return true;
                    } else {
                        GuestLoginDialog.show(mDetailedPostActivity);
                    }

                }
                return false;
            });


        emojiconEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(s.length() > 0){
                    submitButton.setColorFilter(ContextCompat.getColor(mDetailedPostActivity, R.color.colorPrimary));
                } else {
                    submitButton.setColorFilter(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submitButton.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                GuestLoginDialog.show(mDetailedPostActivity);
                return;
            }
            if (TextUtils.isEmpty(emojiconEditText.getText().toString()))
            {
                Toast.makeText(mDetailedPostActivity, "Write a comment", Toast.LENGTH_SHORT).show();
            } else {
                if (CheckNetworkConnection.isConnectionAvailable(mDetailedPostActivity))
                {
                    doComment(postId, postedUserId);
                }
                else
                    SnackBarDialog.showNoInternetError(submitButton);
            }
        });



        if(swipe != null){
            swipe.post(() -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                Intent mIntent = getIntent();
                postId = mIntent.getStringExtra("postId");
                notId = mIntent.getStringExtra("notId");
                userId = AppController.getInstance().getPrefManager().getUser().getId();
                swipe.setRefreshing(true);
                loadFeeds(postId, userId, notId);
            });
        }


    }




    private void doComment(String postId, String postedUserId)
    {
        if(emojiconEditText != null){
            if(CheckNetworkConnection.isConnectionAvailable(getBaseContext())){
                if (!TextUtils.isEmpty(emojiconEditText.getText().toString())){
                    String message = emojiconEditText.getText().toString().trim();
                    sendComment(message, postId, postedUserId);
                    emojiconEditText.setText("");
                    UserUtils.hideKeyBoard(mDetailedPostActivity, emojiconEditText);
                }
            } else {
                SnackBarDialog.showNoInternetError(emojiconEditText);
            }
        }
    }



    //  this method is called when post a comment

    private void sendComment(String comment, String postId, String postedUserId)
    {

        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        long NOW = System.currentTimeMillis() / 1000L;
        String timeStamp = Long.toString(NOW);


        Toast.makeText(this,"Posting comment...", Toast.LENGTH_SHORT).show();


    //    sendNotification(comment);
        Intent resultIntent = new Intent(AppController.getInstance(), HomeActivity.class);

       // correction required here for action
        resultIntent.setAction("chat");
        resultIntent.putExtra("open", "chat");
        resultIntent.putExtra("color", "");
        resultIntent.putExtra("chatRoomName", "");
        resultIntent.putExtra("sendSeen", true);
        resultIntent.putExtra("chatRoomProfilePic", "");
        resultIntent.putExtra("message", "");


        Call<Comment> call = apiService.insertComment(userId, postId, postedUserId,comment,timeStamp, apiKey);
        call.enqueue(new Callback<Comment>()
        {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response)
            {
                Log.d("sendComment", "onSuccess");
                if(response.body() != null && response.isSuccessful()){
                    mCommentList.clear();
                    Comment mComment = response.body();
                    if (mComment != null && !mComment.isErrorStatus())
                    {
                        if (mCommentList != null && mCommentAdapter != null)
                        {

                            mCommentList.clear();
                            mCommentList.addAll(mComment.getData());
                            mCommentAdapter.notifyDataSetChanged();
                            if (mCommentAdapter.getItemCount() >= 1)
                            {
                                commentsLayout.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                hasComments.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t)
            {
                Log.d("sendComment", "onFailure");
            }
        });
    }




    private void loadFeeds(String postId, String userId, String notId)
    {
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
        {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(mDetailedPostActivity);
            apiService.singlePostDetails(userId, postId, notId, apiKey)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<SinglePostDetails>()
            {
                @Override
                public void onCompleted()
                {

                }

                @Override
                public void onError(Throwable e)
                {
                    ProgressBarDialog.dismissLoader();
                    Log.d("loadFeeds", "onFailure");
                    nestedScrollView.setVisibility(View.GONE);
                    rootBottomView.setVisibility(View.GONE);
                    if(swipe != null)
                        swipe.setRefreshing(false);
                }

                @Override
                public void onNext(SinglePostDetails mSinglePost) {
                    ProgressBarDialog.dismissLoader();
                    if(swipe != null)
                        swipe.setRefreshing(false);
                    Log.d("loadFeeds", "onSuccess");
                    nestedScrollView.setVisibility(View.VISIBLE);
                    rootBottomView.setVisibility(View.VISIBLE);
                    if (mSinglePost != null) {
                        if(!mSinglePost.isErrorStatus()){
                            if(mSinglePost.getCommentData() != null){
                                mCommentList.clear();
                                mCommentList.addAll(mSinglePost.getCommentData());
                                mCommentAdapter.notifyDataSetChanged();
                                if(mCommentList.size() > 0){
                                    commentsLayout.setVisibility(View.VISIBLE);
                                } else {
                                    commentsLayout.setVisibility(View.GONE);
                                }
                            }
                            parseData(mSinglePost);
                        } else {
                            if(emojiconEditText != null)
                                SnackBarDialog.show(emojiconEditText, mSinglePost.getStatus());
                        }
                    }
                }
            });
        } else {
            if(emojiconEditText != null)
                SnackBarDialog.showNoInternetError(emojiconEditText);
        }
    }

    private void parseData(SinglePostDetails mSinglePost)
    {
        List<Feed.FeedItem> mFeedItems = mSinglePost.getData();
        if(mFeedItems != null && mFeedItems.size() > 0){
            mFeedItem = mFeedItems.get(0);
            if(mFeedItem != null) {
                postedUserId = mFeedItem.getPostedUserId();
                String[] split = mFeedItem.getPostedUserName().split("\\s+");
                if (!mFeedItem.getProfilePic().equalsIgnoreCase("")){
                    if(mFeedItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                            || mFeedItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                        iconProfile.setImageResource(R.drawable.bg_circle);
                        if(mFeedItem.getColorCode() != null && !mFeedItem.getColorCode().equalsIgnoreCase("") && mFeedItem.getColorCode().startsWith("#")){
                            iconProfile.setColorFilter(Color.parseColor(mFeedItem.getColorCode()));
                        } else {
                            iconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(mDetailedPostActivity, "400"));
                        }
                        iconText.setVisibility(View.VISIBLE);
                        if (!mFeedItem.getPostedUserName().equalsIgnoreCase("")){
                            if (split.length > 1)
                            {
                                String first = split[0].substring(0,1);
                                String last = split[1].substring(0,1);
                                String initial = first + " "+ last;
                                iconText.setText(initial);
                            }
                        }
                    } else {
                        Uri uri = Uri.parse(mFeedItem.getProfilePic());
                        RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                        iconProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getBaseContext().getResources())
                                .setRoundingParams(roundingParams)
                                .build());
                        roundingParams.setRoundAsCircle(true);
                        iconProfile.setImageURI(uri);
                        iconProfile.setColorFilter(null);
                        iconText.setVisibility(View.GONE);
                    }
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


                if(mFeedItem.getModerator() == 1) {
                    top.setText(R.string.mod);
                    top.setVisibility(View.VISIBLE);
                    top.setBackground(ContextCompat.getDrawable(mDetailedPostActivity, R.drawable.top_mod));
                    //top.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    iconBadge.setVisibility(View.GONE);
                } else {
                    if(mFeedItem.getRank() != 0){
                        if (mFeedItem.getRank() >= 1 && mFeedItem.getRank() <=10) {
                            iconBadge.setVisibility(View.GONE);
                            top.setVisibility(View.VISIBLE);
                            top.setText( "TOP " + mFeedItem.getRank());

                            if(mFeedItem.getPoints() == 0 || mFeedItem.getPoints() == 1){
                                points.setText(String.valueOf(mFeedItem.getPoints()));
                            } else if(mFeedItem.getPoints() > 1){
                                points.setText(String.valueOf(mFeedItem.getPoints()));
                            }
                            points.setVisibility(View.VISIBLE);
                        } else {
                            iconBadge.setVisibility(View.GONE);
                            top.setVisibility(View.GONE);
                            points.setVisibility(View.GONE);
                        }
                    }
                }





                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    SpannableStringBuilder spannedDetails;
                    if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("") && mFeedItem.getPostedUserDesignation() != null && !mFeedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                        String position = mFeedItem.getPostedUserDesignation() + ", " +mFeedItem.getUserCompany();
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +mFeedItem.getPostedUserDesignation()+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+mFeedItem.getUserCompany()+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                        designation.setText(spannedDetails);
                        designation.setVisibility(View.VISIBLE);
                    } else if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("") && mFeedItem.getPostedUserDesignation() == null ){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +mFeedItem.getUserCompany()+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                        designation.setText(spannedDetails);
                        designation.setVisibility(View.VISIBLE);
                    }  else if(mFeedItem.getUserCompany() == null && mFeedItem.getPostedUserDesignation() != null && !mFeedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                        String position = mFeedItem.getPostedUserDesignation();
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +position+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                        designation.setText(spannedDetails);
                        designation.setVisibility(View.VISIBLE);
                    } else if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("")){
                        String position = mFeedItem.getUserCompany();
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +position+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                        designation.setText(spannedDetails);
                        designation.setVisibility(View.VISIBLE);
                    } else {
                        designation.setText("TRADER");
                        designation.setVisibility(View.VISIBLE);
                    }
                }
                else
                    {
                    SpannableStringBuilder spannedDetails;
                    if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("") && mFeedItem.getPostedUserDesignation() != null && !mFeedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +mFeedItem.getPostedUserDesignation()+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+mFeedItem.getUserCompany()+"&#160" + "</font>"));
                        designation.setText(spannedDetails);
                        designation.setVisibility(View.VISIBLE);
                    }
                    else if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("") && mFeedItem.getPostedUserDesignation() == null ){
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +mFeedItem.getUserCompany()+ "</font>"));
                        designation.setText(spannedDetails);
                        designation.setVisibility(View.VISIBLE);
                    }
                    else if(mFeedItem.getUserCompany() == null && mFeedItem.getPostedUserDesignation() != null && !mFeedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                        String position = mFeedItem.getPostedUserDesignation();
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +position+ "</font>"));
                        designation.setText(spannedDetails);
                        designation.setVisibility(View.VISIBLE);
                    }
                    else if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase(""))
                    {
                        String position = mFeedItem.getUserCompany();
                        spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +position+ "</font>"));
                        designation.setText(spannedDetails);
                        designation.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        designation.setText("TRADER");
                        designation.setVisibility(View.VISIBLE);
                    }
                }


                if(mSinglePost.getLikeData() != null){
                    if(mSinglePost.getLikeData().size() > 0) {
                        if(mFeedItem.isLikeStatus()){
                            if(mSinglePost.getLikeData().size() == 1){
                                tsWhoLikes.setCurrentText("You liked this");
                                ivwhoLike.setImageResource(R.drawable.ic_heart_outline_black_filled);
                            } else {
                                int count = mSinglePost.getLikeData().size()-1;
                                ivwhoLike.setImageResource(R.drawable.ic_heart_outline_black_filled);
                                tsWhoLikes.setCurrentText("You and "+ count +" people liked this");
                            }
                        } else {
                            int count = mSinglePost.getLikeData().size();
                            if(mSinglePost.getLikeData().size() == 1){
                                tsWhoLikes.setCurrentText("1 person liked this");
                                ivwhoLike.setImageResource(R.drawable.ic_heart_outline_black_filled);
                            } else {
                                ivwhoLike.setImageResource(R.drawable.ic_heart_outline_black);
                                tsWhoLikes.setCurrentText(count +" people liked this");
                            }
                        }
                        whoLikeLayout.setVisibility(View.VISIBLE);
                    } else {
                        whoLikeLayout.setVisibility(View.GONE);
                    }
                }

                if (mFeedItem.isPostFavourited()) {
                    favourite.setTag("favourite");
                    favourite.setImageDrawable(ContextCompat.getDrawable(mDetailedPostActivity, R.drawable.ic_star_black_24dp));
                    favourite.setColorFilter(ContextCompat.getColor(mDetailedPostActivity, R.color.colorPrimaryDark));
                } else {
                    favourite.setImageDrawable(ContextCompat.getDrawable(mDetailedPostActivity, R.drawable.ic_star_border_black_24dp));
                    favourite.setColorFilter(null);
                    favourite.setTag("favourited");
                }

                if(favourite != null) {
                    favourite.setOnClickListener(v -> {
                        if(UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                            GuestLoginDialog.show(mDetailedPostActivity);
                            return;
                        }
                        if(favourite.getTag().equals("favourite")){
                            favourite.setImageDrawable(ContextCompat.getDrawable(mDetailedPostActivity, R.drawable.ic_star_border_black_24dp));
                            favourite.setColorFilter(null);
                            favourite.setTag("favourited");
                        } else {
                            favourite.setImageDrawable(ContextCompat.getDrawable(mDetailedPostActivity, R.drawable.ic_star_black_24dp));
                            favourite.setColorFilter(ContextCompat.getColor(mDetailedPostActivity, R.color.colorPrimaryDark));
                            favourite.setTag("favourite");
                        }

                        if (CheckNetworkConnection.isConnectionAvailable(mDetailedPostActivity)) {
                            doFavourite(mFeedItem, 0);
                        } else {
                            SnackBarDialog.showNoInternetError(v);
                        }
                    });
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
                        }
                    };

                    ForegroundColorSpan spanColorBlack = new ForegroundColorSpan(Color.BLACK);
                    ForegroundColorSpan spanColorGray = new ForegroundColorSpan(ContextCompat.getColor(mDetailedPostActivity, R.color.updated_profile_pic));
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
                            sBuilder.length(),
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
                                ds.setColor(mDetailedPostActivity.getResources().getColor(R.color.black, mDetailedPostActivity.getTheme()));
                            } else {
                                ds.setColor(mDetailedPostActivity.getResources().getColor(R.color.black));
                            }
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getBaseContext(), word1, Toast.LENGTH_SHORT).show();
                        }
                    };

                    ClickableSpan friendNameClickableSpan = new ClickableSpan() {

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ds.setColor(mDetailedPostActivity.getResources().getColor(R.color.black, mDetailedPostActivity.getTheme()));
                            } else {
                                ds.setColor(mDetailedPostActivity.getResources().getColor(R.color.black));
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
                    profileName.setOnClickListener(v -> Toast.makeText(getBaseContext(), word1, Toast.LENGTH_SHORT).show());
                }

                if (mFeedItem.getStatus() != null && !mFeedItem.getStatus().equalsIgnoreCase("")){
                    if (mFeedItem.getStatus().length() > 350) {
                        final String statusString = mFeedItem.getStatus();
                        String splitString = mFeedItem.getStatus().substring(0, 350);
                        final String continueReading = "...Continue Reading";
                        String mergedString = splitString + continueReading;
                        ClickableSpan nameClickableSpan = new ClickableSpan() {

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                ds.setColor(ds.linkColor);    // you can use custom color
                                ds.setUnderlineText(false);    // this remove the underline
                            }

                            @Override
                            public void onClick(View view) {
                                //status.setText(statusString);
                                SpannableStringBuilder sBuilder = new SpannableStringBuilder(statusString);
                                if(mFeedItem.getTagList() != null){
                                    for (Feed.FeedItem.TagList mData : mFeedItem.getTagList()){
                                        Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                                        Matcher matcher = pattern.matcher(mFeedItem.getStatus());
                                        while (matcher.find()) {
                                            sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), 0), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                }
                                status.setText(sBuilder);
                                Linkify.addLinks(status, Linkify.WEB_URLS);
                                AppController.getInstance().stripUnderlines(status);
                                status.setMovementMethod(LinkMovementMethod.getInstance());
                            }
                        };
                        ForegroundColorSpan spanColorBlack = new ForegroundColorSpan(Color.BLACK);
                        ForegroundColorSpan spanColorGray = new ForegroundColorSpan(Color.GRAY);
                        SpannableStringBuilder sBuilder = new SpannableStringBuilder(splitString);
                        sBuilder.append(" ");
                        sBuilder.append(continueReading);
                        sBuilder.setSpan(
                                spanColorBlack,
                                0,
                                splitString.length()+1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        sBuilder.setSpan(nameClickableSpan,
                                mergedString.indexOf(continueReading),
                                mergedString.indexOf(continueReading) + String.valueOf(continueReading).length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sBuilder.setSpan(
                                spanColorGray,
                                sBuilder.length()-continueReading.length(),
                                sBuilder.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );



                        for (Feed.FeedItem.TagList mData : mFeedItem.getTagList()){
                            Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                            Matcher matcher = pattern.matcher(mFeedItem.getStatus());
                            while (matcher.find()) {
                                System.out.print("Start index: " + matcher.start());
                                System.out.print(" End index: " + matcher.end() + " ");
                                System.out.println(matcher.group());
                                if(matcher.start() < 370 && matcher.end() < 370){
                                    sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), 0), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                //sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), 0), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                        status.setText(sBuilder);
                        Linkify.addLinks(status, Linkify.WEB_URLS);
                        AppController.getInstance().stripUnderlines(status);
                        //status.setTransformationMethod(new LinkTransformationMethod(mDetailedPostActivity));
                        status.setMovementMethod(LinkMovementMethod.getInstance());
                    } else {
                        SpannableStringBuilder sBuilder = new SpannableStringBuilder(mFeedItem.getStatus());
                        for (Feed.FeedItem.TagList mData : mFeedItem.getTagList()){
                            Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                            Matcher matcher = pattern.matcher(mFeedItem.getStatus());
                            while (matcher.find()) {
                                System.out.print("Start index: " + matcher.start());
                                System.out.print(" End index: " + matcher.end() + " ");
                                System.out.println(matcher.group());
                                if(matcher.start() < 370 && matcher.end() < 370){
                                    sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), 0), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                //sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(),mData.getTaggedId(), 0), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                        status.setText(sBuilder);
                        Linkify.addLinks(status, Linkify.WEB_URLS);
                        AppController.getInstance().stripUnderlines(status);
                        //status.setTransformationMethod(new LinkTransformationMethod(mDetailedPostActivity));
                        status.setMovementMethod(LinkMovementMethod.getInstance());
                    }
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
                if(ivFeedCenter != null){
                    if (mPictureUrl != null && mPictureUrl.size() != 0) {
                        isShowFeedImages = true;
                        Uri uri = Uri.parse(mPictureUrl.get(0).getImages());
                        ivFeedCenter.setAdjustViewBounds(true);
                        ivFeedCenter.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FOCUS_CROP);
                        ivFeedCenter.setImageURI(uri);
                        ivFeedCenter.setVisibility(View.VISIBLE);
                        vImageRoot.setVisibility(View.VISIBLE);
                    } else {
                        isShowFeedImages = false;
                        ivFeedCenter.setVisibility(View.GONE);
                        vImageRoot.setVisibility(View.GONE);
                    }
                }


                if(!isShowFeedImages){
                    if (mFeedItem.getStatus() != null && !mFeedItem.getStatus().equalsIgnoreCase("")) {
                        final List<String> extractedUrls = UserUtils.extractUrls(mFeedItem.getStatus());
                        if (extractedUrls != null && extractedUrls.size() > 0) {
                            mLinkPreview.setListener(mPreviewListener);
                            cardViewPreview.setVisibility(View.VISIBLE);
                            mLinkPreview.setData(extractedUrls.get((extractedUrls.size()-1)));
                            mLinkPreview.setOnClickListener(v -> UserUtils.launchCustomTabURL(mDetailedPostActivity, extractedUrls.get((extractedUrls.size()-1))));
                        } else {
                            cardViewPreview.setVisibility(View.GONE);
                        }
                    } else {
                        cardViewPreview.setVisibility(View.GONE);
                    }
                } else {
                    cardViewPreview.setVisibility(View.GONE);
                }

                profileName.setOnClickListener(v -> {
                    if (AppController.getInstance().getPrefManager().getUser() == null)
                        return;
                    if(mFeedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(mFeedItem.getPostedUserId());
                    }
                });

                iconProfile.setOnClickListener(v -> {
                    if (AppController.getInstance().getPrefManager().getUser() == null)
                        return;
                    if(mFeedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(mFeedItem.getPostedUserId());
                    }
                });


                if(overflow != null) {
                    if (AppController.getInstance().getPrefManager().getUser() == null)
                        return;
                    if(!UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                        if(mFeedItem.getPostedUserId() != null && mFeedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                            final boolean isEditShow;
                            isEditShow = mFeedItem.getStatus() != null && !mFeedItem.getStatus().equalsIgnoreCase("");
                            overflow.setVisibility(View.VISIBLE);
                            overflow.setOnClickListener(v -> showPopupMenu(overflow, mFeedItem, isEditShow));
                        } else {
                            overflow.setVisibility(View.GONE);
                        }
                    }


                }


                ivFeedCenter.setOnClickListener(v -> {
                    final List<PictureUrl> mPictureUrl1 = mFeedItem.getPictureUrl();
                    if (mPictureUrl1 != null && mPictureUrl1.size() != 0) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("images", (Serializable) mPictureUrl1);
                        //bundle.putString("images", mPictureUrl.get(0).getImages());
                        FragmentTransaction ft = mDetailedPostActivity.getSupportFragmentManager().beginTransaction();
                        FeedImagesSlideshowDialogFragment newFragment = FeedImagesSlideshowDialogFragment.newInstance();
                        newFragment.setArguments(bundle);
                        newFragment.show(ft, "slideshow");
                    }

                });

                ivFeedCenter.setOnLongClickListener(v -> {
                    final List<PictureUrl> mPictureUrl12 = mFeedItem.getPictureUrl();
                    if (mPictureUrl12 != null && mPictureUrl12.size() != 0) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("images", (Serializable) mPictureUrl12);
                        //bundle.putString("images", mPictureUrl.get(0).getImages());
                        FragmentTransaction ft = mDetailedPostActivity.getSupportFragmentManager().beginTransaction();
                        FeedImagesSlideshowDialogFragment newFragment = FeedImagesSlideshowDialogFragment.newInstance();
                        newFragment.setArguments(bundle);
                        newFragment.show(ft, "slideshow");
                    }
                    return false;
                });

                btnReport.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                        GuestLoginDialog.show(mDetailedPostActivity);
                        return;
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(mDetailedPostActivity))
                        showReportPopupMenu(v,  mFeedItem);
                    else
                        SnackBarDialog.showNoInternetError(v);
                });


                btnLike.setImageResource(mFeedItem.isLikeStatus() ? R.drawable.ic_heart_outline_black_filled : R.drawable.ic_heart_outline_black);

                btnLike.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                        GuestLoginDialog.show(mDetailedPostActivity);
                        return;
                    }
                    like();
                });

                tsLikesCounter.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                        GuestLoginDialog.show(mDetailedPostActivity);
                        return;
                    }
                    like();
                                            /*if(mFeedItem.getLikeCount() > 0)
                                                screenMoveToLikeActivity(mFeedItem.getPostId(), mFeedItem.getPostedUserId(), mFeedItem.getLikeCount());*/
                });

                whoLikeLayout.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                        GuestLoginDialog.show(mDetailedPostActivity);
                        return;
                    }
                    if(mFeedItem.getLikeCount() > 0)
                        screenMoveToLikeActivity(mFeedItem.getPostId(), mFeedItem.getPostedUserId(), mFeedItem.getLikeCount());
                });

                tsCommentCounter.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                        GuestLoginDialog.show(mDetailedPostActivity);
                        return;
                    }
                    if(emojiconEditText != null){
                        emojiconEditText.requestFocus();
                        UserUtils.showKeyBoard(mDetailedPostActivity, emojiconEditText);
                    }

                });
                btnComments.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
                        GuestLoginDialog.show(mDetailedPostActivity);
                        return;
                    }
                    if(emojiconEditText != null){
                        emojiconEditText.requestFocus();
                        UserUtils.showKeyBoard(mDetailedPostActivity, emojiconEditText);
                    }

                });


                if(mFeedItem.getPostType() != null && mFeedItem.getPostType().equalsIgnoreCase("newUserJoined")){
                    newJoinLayout.setVisibility(View.VISIBLE);
                    head.setVisibility(View.VISIBLE);
                    feedsMainLayout.setVisibility(View.GONE);
                    news.setVisibility(View.VISIBLE);
                    news.setText("Joined MyScrap");
                    String timeAGO;
                    if(mFeedItem.getJoinedTime() != null && !mFeedItem.getJoinedTime().equalsIgnoreCase("")){
                        timeAGO = UserUtils.getTimeAgo(Long.parseLong(UserUtils.parsingLong(mFeedItem.getJoinedTime())));
                        newJoinedTime.setText(timeAGO);
                        newJoinedTime.setVisibility(View.VISIBLE);
                    } else{
                        newJoinedTime.setText("Joined MyScrap");
                        newJoinedTime.setVisibility(View.GONE);
                    }
                    if (mFeedItem.getProfilePic() != null && !mFeedItem.getProfilePic().equalsIgnoreCase("")){
                        if(mFeedItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || mFeedItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            newJoinIconProfile.setImageResource(R.drawable.bg_circle);
                            if(mFeedItem.getColorCode() != null && !mFeedItem.getColorCode().equalsIgnoreCase("") && mFeedItem.getColorCode().startsWith("#")){
                                newJoinIconProfile.setColorFilter(Color.parseColor(mFeedItem.getColorCode()));
                            } else {
                                newJoinIconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(mDetailedPostActivity, "400"));
                            }

                            newJoinIconText.setVisibility(View.VISIBLE);
                            if (mFeedItem.getPostedUserName() != null && !mFeedItem.getPostedUserName().equalsIgnoreCase("")){
                                if (split.length > 1){
                                    String first = split[0].substring(0,1);
                                    String last = split[1].substring(0,1);
                                    String initial = first + ""+ last ;
                                    newJoinIconText.setText(initial.toUpperCase().trim());
                                } else {
                                    if (split[0] != null && split[0].trim().length() == 1) {
                                        String first = split[0].substring(0, 1);
                                        newJoinIconText.setText(first.toUpperCase().trim());
                                    }
                                }
                            }
                        } else {
                            if(mDetailedPostActivity != null && !mFeedItem.getProfilePic().equalsIgnoreCase("")){
                                Uri uri = Uri.parse(mFeedItem.getProfilePic());
                                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                                newJoinIconProfile.setHierarchy(new GenericDraweeHierarchyBuilder(mDetailedPostActivity.getResources())
                                        .setRoundingParams(roundingParams)
                                        .build());
                                roundingParams.setRoundAsCircle(true);
                                newJoinIconProfile.setImageURI(uri);
                            }
                            newJoinIconProfile.setColorFilter(null);
                            newJoinIconText.setVisibility(View.GONE);
                        }
                    }

                    newJoinLayout.setOnClickListener(v -> {
                        if (AppController.getInstance().getPrefManager().getUser() == null)
                            return;
                        if(mFeedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            goToUserProfile();
                        } else {
                            goToUserFriendProfile(mFeedItem.getPostedUserId());
                        }
                    });


                    if(mFeedItem.getPostedUserName() != null && !mFeedItem.getPostedUserName().equalsIgnoreCase("")){
                        newJoinProfileName.setText(mFeedItem.getPostedUserName());
                        newJoinProfileName.setVisibility(View.VISIBLE);
                    } else {
                        newJoinProfileName.setVisibility(View.GONE);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        SpannableStringBuilder spannedDetails;
                        if (mFeedItem.getPostType() != null && mFeedItem.getPostType().equalsIgnoreCase("newsPost") && mFeedItem.getCompanyName() != null &&  !mFeedItem.getCompanyName().equalsIgnoreCase("") && mFeedItem.getCompanyId() != null &&  !mFeedItem.getCompanyId().equalsIgnoreCase("")){
                            if(mFeedItem.getPostBy() != null){
                                newJoinDesignation.setText(mFeedItem.getPostBy());
                                newJoinDesignation.setOnClickListener(v -> {
                                    if(mFeedItem.getPostedUserId() != null && !mFeedItem.getPostedUserId().equalsIgnoreCase(""))
                                        if (AppController.getInstance().getPrefManager().getUser() == null)
                                            return;
                                    if(mFeedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                                        goToUserProfile();
                                    } else {
                                        goToUserFriendProfile(mFeedItem.getPostedUserId());
                                    }
                                });
                            } else {
                                newJoinDesignation.setText("Admin");
                            }
                            newJoinDesignation.setVisibility(View.VISIBLE);
                        } else {
                            if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("") && mFeedItem.getPostedUserDesignation() != null && !mFeedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                                String position = mFeedItem.getPostedUserDesignation() + ", " +mFeedItem.getUserCompany();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +mFeedItem.getPostedUserDesignation()+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+mFeedItem.getUserCompany()+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("") && mFeedItem.getPostedUserDesignation() == null ){
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +mFeedItem.getUserCompany()+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            }  else if(mFeedItem.getUserCompany() == null && mFeedItem.getPostedUserDesignation() != null && !mFeedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                                String position = mFeedItem.getPostedUserDesignation();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +position+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("")){
                                String position = mFeedItem.getUserCompany();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +position+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else {
                                newJoinDesignation.setText("TRADER");
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        SpannableStringBuilder spannedDetails;
                        if (mFeedItem.getPostType() != null && mFeedItem.getPostType().equalsIgnoreCase("newsPost") && mFeedItem.getCompanyName() != null &&  !mFeedItem.getCompanyName().equalsIgnoreCase("") && mFeedItem.getCompanyId() != null &&  !mFeedItem.getCompanyId().equalsIgnoreCase("")){
                            if(mFeedItem.getPostBy() != null){
                                newJoinDesignation.setText(mFeedItem.getPostBy());
                                newJoinDesignation.setOnClickListener(v -> {
                                    if(mFeedItem.getPostedUserId() != null && !mFeedItem.getPostedUserId().equalsIgnoreCase("")) {
                                        if (AppController.getInstance().getPrefManager().getUser() == null)
                                            return;
                                        if (mFeedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                                            goToUserProfile();
                                        } else {
                                            goToUserFriendProfile(mFeedItem.getPostedUserId());
                                        }
                                    }
                                });
                            } else {
                                newJoinDesignation.setText("Admin");
                            }
                            newJoinDesignation.setVisibility(View.VISIBLE);
                        } else {
                            if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("") && mFeedItem.getPostedUserDesignation() != null && !mFeedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +mFeedItem.getPostedUserDesignation()+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+mFeedItem.getUserCompany()+"&#160" + "</font>"));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("") && mFeedItem.getPostedUserDesignation() == null ){
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +mFeedItem.getUserCompany()+ "</font>"));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            }  else if(mFeedItem.getUserCompany() == null && mFeedItem.getPostedUserDesignation() != null && !mFeedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                                String position = mFeedItem.getPostedUserDesignation();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +position+ "</font>"));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else if(mFeedItem.getUserCompany() != null && !mFeedItem.getUserCompany().equalsIgnoreCase("")){
                                String position = mFeedItem.getUserCompany();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +position+ "</font>"));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else {
                                newJoinDesignation.setText("TRADER");
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            }
                        }

                    }

                    if(mFeedItem.getModerator() == 1) {
                        newJoinTop.setText(R.string.mod);
                        newJoinTop.setVisibility(View.VISIBLE);
                        newJoinTop.setBackground(ContextCompat.getDrawable(mDetailedPostActivity, R.drawable.top_mod));
                        //top.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        iconBadge.setVisibility(View.GONE);
                    } else {
                        if(mFeedItem.getRank() != 0){
                            if (mFeedItem.getRank() >= 1 && mFeedItem.getRank() <=10) {
                                iconBadge.setVisibility(View.GONE);
                                newJoinTop.setVisibility(View.VISIBLE);
                                newJoinTop.setText( "TOP " + mFeedItem.getRank());
                                newJoinTop.setTextColor(ContextCompat.getColor(mDetailedPostActivity, R.color.white));
                                newJoinTop.setBackground(ContextCompat.getDrawable(mDetailedPostActivity, R.drawable.top));
                            } else {
                                if(mFeedItem.isNewJoined()){
                                    newJoinTop.setText(R.string.new_user);
                                    newJoinTop.setVisibility(View.VISIBLE);
                                    newJoinTop.setTextColor(ContextCompat.getColor(mDetailedPostActivity, R.color.white));
                                    newJoinTop.setBackground(ContextCompat.getDrawable(mDetailedPostActivity, R.drawable.top_red));
                                } else {
                                    newJoinTop.setVisibility(View.GONE);
                                    newJoinTop.setBackground(null);
                                }
                                iconBadge.setVisibility(View.GONE);
                            }
                        }
                    }
                } else {
                    head.setVisibility(View.GONE);
                    newJoinedTime.setVisibility(View.GONE);
                    news.setVisibility(View.GONE);
                    newJoinLayout.setVisibility(View.GONE);
                    feedsMainLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void like() {

        if (mFeedItem == null)
            return;

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
        //animateHeartButton(mFeedItem.isLikeStatus());
        if(mFeedItem.isLikeStatus()){
            if(mFeedItem.getLikeCount() == 1){
                tsWhoLikes.setCurrentText("You liked this");
                ivwhoLike.setImageResource(R.drawable.ic_heart_outline_black_filled);
            } else {
                int count = mFeedItem.getLikeCount()-1;
                ivwhoLike.setImageResource(R.drawable.ic_heart_outline_black_filled);
                tsWhoLikes.setCurrentText("You and "+ count +" people liked this");
            }
        } else {
            int count = mFeedItem.getLikeCount();
            if(count == 1){
                tsWhoLikes.setCurrentText("1 person liked this");
                ivwhoLike.setImageResource(R.drawable.ic_heart_outline_black_filled);
            } else {
                ivwhoLike.setImageResource(R.drawable.ic_heart_outline_black);
                tsWhoLikes.setCurrentText(count +" people liked this");
            }
        }
        if(mFeedItem.getLikeCount() > 0)
            whoLikeLayout.setVisibility(View.VISIBLE);
        else
            whoLikeLayout.setVisibility(View.VISIBLE);

        if(CheckNetworkConnection.isConnectionAvailable(mDetailedPostActivity))
            doLike(mFeedItem);
    }

    private void goToUserProfile() {
        Intent i = new Intent(mDetailedPostActivity, UserProfileActivity.class);
        startActivity(i);
        if (CheckOsVersion.isPreLollipop()) {
            mDetailedPostActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToUserFriendProfile(String postedUserId) {
        final Intent intent = new Intent(mDetailedPostActivity, UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(mDetailedPostActivity != null)
                mDetailedPostActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }


    private void showPopupMenu(View v, Feed.FeedItem feedItem, boolean isEditShow) {
        PopupMenu popup = new PopupMenu(mDetailedPostActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.over_flow, popup.getMenu());
        Menu popupMenu = popup.getMenu();
        if(!isEditShow)
            popupMenu.findItem(R.id.action_edit).setVisible(false);
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(feedItem));
        popup.show();
    }

    private void showDeletePostDialog(Context context, final String id, final String postId, final String albumID){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mDetailedPostActivity);
        dialogBuilder.setMessage("Are you sure you want to delete this post?");
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("DELETE", (dialog, which) -> {
            deletingPost(id, postId, albumID);
            onBackPressed();
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
            public void onResponse(@NonNull Call<DeletePost> call, @NonNull retrofit2.Response<DeletePost> response) {

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
            public void onResponse(@NonNull Call<Favourite> call, @NonNull retrofit2.Response<Favourite> response) {
                Log.d("doFavourite", "onSuccess");
            }
            @Override
            public void onFailure(@NonNull Call<Favourite> call, @NonNull Throwable t) {
                Log.d("doFavourite", "onFailure");
            }
        });
    }

    private void showReportPopupMenu(View v, final Feed.FeedItem feedItem) {
        PopupMenu popup = new PopupMenu(mDetailedPostActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.report, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.action_report){
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mDetailedPostActivity);
                dialogBuilder.setMessage("Are you sure you want to report this post?");
                dialogBuilder.setCancelable(true);
                dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                    doReport(feedItem.getPostId(), feedItem.getPostedUserId());
                    Toast.makeText(mDetailedPostActivity, "Post Reported", Toast.LENGTH_SHORT).show();
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
            public void onResponse(@NonNull Call<Report> call, @NonNull retrofit2.Response<Report> response) {
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

    @Override
    public void onDataReady(final LinkPreview preview) {
        if (preview != null ) {
            AppController.runOnUIThread(() -> {
                if(preview.getLink() != null && !preview.getLink().equalsIgnoreCase(""))
                    preview.setMessage(preview.getLink());
            });
        }
    }


    private class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private Feed.FeedItem feedItem;
        private int itemPosition;
        public MyMenuItemClickListener(Feed.FeedItem mFeedItem) {
            this.feedItem = mFeedItem;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    Intent i = new Intent(mDetailedPostActivity, StatusActivity.class);
                    i.putExtra("page", "postDetail");
                    i.putExtra("editPost", feedItem.getStatus());
                    i.putExtra("postId", ""+feedItem.getPostId());
                    Gson gson = new Gson();
                    String userData = gson.toJson(feedItem);
                    i.putExtra("tagData", userData);
                    mDetailedPostActivity.startActivity(i);
                    if (CheckOsVersion.isPreLollipop())
                        mDetailedPostActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    return true;
                case R.id.action_delete:
                    if (feedItem != null ) {
                        showDeletePostDialog(mDetailedPostActivity, feedItem.getPostedUserId(), feedItem.getPostId(), feedItem.getAlbumId());
                    }
                    return true;
                default:
            }
            return false;
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
    protected void onResume()
    {

        super.onResume();
        if(swipe != null)
        {
            swipe.post(() ->
            {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                Intent mIntent = getIntent();
                postId = mIntent.getStringExtra("postId");
                notId = mIntent.getStringExtra("notId");
                userId = AppController.getInstance().getPrefManager().getUser().getId();
                swipe.setRefreshing(true);
                if(postId != null && !postId.equalsIgnoreCase(""))
                    NotificationUtils.clearNotificationByID(Integer.parseInt(UserUtils.parsingInteger(postId)));
                //loadFeeds(postId, userId, notId);
            });
        }
        if(mTracker != null){
            mTracker.setScreenName("Detail Post Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(DetailedPostActivity.this,UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(DetailedPostActivity.this,UserOnlineStatus.OFFLINE);
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
                            if(mFeedItem.getPostId().equalsIgnoreCase(data.getPostId())){
                                mFeedItem.setLikeStatus(data.getLikeStatus());
                                mFeedItem.setLikeCount(Integer.parseInt(UserUtils.parsingInteger(data.getLikeCount())));
                                btnLike.setImageResource(mFeedItem.isLikeStatus() ? R.drawable.ic_heart_outline_black_filled : R.drawable.ic_heart_outline_black);
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

    private void screenMoveToLikeActivity(String postId, String postedUserId, int likeCount) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        Intent mIntent = new Intent(mDetailedPostActivity, LikeActivity.class);
        mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        mIntent.putExtra("postedUserId", postedUserId);
        mIntent.putExtra("postId", postId);
        mIntent.putExtra("count", likeCount);
        mIntent.putExtra("apiKey", UserUtils.getApiKey(mDetailedPostActivity));
        mDetailedPostActivity.startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop()) {
            mDetailedPostActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOverFlowClickListener(View v, int position) {
        if(UserUtils.isGuestLoggedIn(mDetailedPostActivity)){
            GuestLoginDialog.show(mDetailedPostActivity);
            return;
        }
        if(mCommentList != null && mCommentList.size() > 0){
            showPopupMenu(v,  mCommentList.get(position), position);
        }
    }


    private void showPopupMenu(View v, Comment.CommentData feedItem, int itemPosition) {
        PopupMenu popup = new PopupMenu(mDetailedPostActivity, v);
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
                        if(CheckNetworkConnection.isConnectionAvailable(mDetailedPostActivity)){
                            Comment.CommentData data = mCommentList.get(itemPosition);
                            if(mCommentList != null && mCommentList.size() > 0){
                                mCommentList.remove(itemPosition);
                                mCommentAdapter.notifyItemRemoved(itemPosition);
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
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mDetailedPostActivity);
        @SuppressLint("InflateParams") View mView = layoutInflaterAndroid.inflate(R.layout.forgot_password_edit_text, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mDetailedPostActivity);
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
                            if (CheckNetworkConnection.isConnectionAvailable(mDetailedPostActivity)) {
                                sendEditComment(postId,friendId,editId, comment1);
                                UserUtils.hideKeyBoard(getApplicationContext(), userInputDialogEditText );
                                Toast.makeText(getApplicationContext(),"Editing comment...", Toast.LENGTH_SHORT).show();
                            } else {
                                SnackBarDialog.show(userInputDialogEditText,"No internet connection available.");
                            }
                        } else {
                            Toast.makeText(mDetailedPostActivity, "Comment shouldn't be empty", Toast.LENGTH_SHORT).show();
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

    private class InternalClickableSpan extends ClickableSpan {

        private String taggedName;
        private String taggedId;
        private int position;

        public InternalClickableSpan(String mTaggedName, String mTaggedId, int mPosition) {
            taggedName = mTaggedName;
            taggedId = mTaggedId;
            position = mPosition;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
            ds.setColor(ContextCompat.getColor(mDetailedPostActivity, R.color.colorPrimary));
        }


        @Override
        public void onClick(View view) {
            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;
            Selection.setSelection((Spannable) ((TextView)view).getText(), 0);
            if (taggedId != null && !taggedId.equalsIgnoreCase("") && position != -1) {
                if(taggedId.equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(taggedId);
                }
            }
        }
    }

}
