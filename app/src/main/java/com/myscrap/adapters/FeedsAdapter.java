package com.myscrap.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.myscrap.FeedImagesSlideshowDialogFragment;
import com.myscrap.LikeActivity;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.EventInterest;
import com.myscrap.model.Feed;
import com.myscrap.model.PictureUrl;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.LinkPreview;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.LoadingFeedItemView;
import com.myscrap.view.MultiTouchViewPager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

/**
 * Created by ms3 on 5/15/2017.
 */

public class FeedsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements LinkPreview.PreviewListener{
    public static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    static final String ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button";
    private RecyclerView.RecycledViewPool mSharedPool = new RecyclerView.RecycledViewPool();
    static final int VIEW_TYPE_DEFAULT = 1;
    private static final int VIEW_TYPE_LOADER = 2;

    private static int EVENT_INTEREST_OR_NOT;

    private List<Feed.FeedItem> feedItems = new ArrayList<>();

    private Context context;
    private OnFeedItemClickListener onFeedItemClickListener;

    private boolean showLoadingView = false;
    private FeedsAdapter mFeedsAdapter;

    private LinkPreview.PreviewListener mPreviewListener;
    private SimpleDateFormat sDFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
    private SimpleDateFormat sDFormatDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private RecyclerView recyclerView;

    public FeedsAdapter(Context context, List<Feed.FeedItem> feedItemsList) {
        this.context = context;
        this.mFeedsAdapter = this;
        this.mPreviewListener = this;
        this.feedItems = feedItemsList;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DEFAULT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
            CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);
            this.recyclerView.setRecycledViewPool(mSharedPool);
            setupClickableViews(view, cellFeedViewHolder);
            return cellFeedViewHolder;
        } else if (viewType == VIEW_TYPE_LOADER) {
            LoadingFeedItemView view = new LoadingFeedItemView(context);
            view.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            );
            return new LoadingCellFeedViewHolder(view);
        }

        return null;
    }

    private void setupClickableViews(final View view, final CellFeedViewHolder cellFeedViewHolder) {
        cellFeedViewHolder.btnComments.setOnClickListener(v ->
        {
            if(UserUtils.isGuestLoggedIn(view.getContext()))
            {
                GuestLoginDialog.show(view.getContext());
                return;
            }
            onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());
        });
        cellFeedViewHolder.inActiveCommentLayout.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(view.getContext())){
                GuestLoginDialog.show(view.getContext());
                return;
            }
            onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());
        });
        cellFeedViewHolder.tsCommentCounter.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(view.getContext())){
                GuestLoginDialog.show(view.getContext());
                return;
            }
            onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());
        });

        cellFeedViewHolder.comment.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(view.getContext())){
                GuestLoginDialog.show(view.getContext());
                return;
            }
            if (feedItems != null && !feedItems.isEmpty() && feedItems.get(cellFeedViewHolder.getAdapterPosition()) != null){
                if(feedItems.get(cellFeedViewHolder.getAdapterPosition()).getPostType() != null && feedItems.get(cellFeedViewHolder.getAdapterPosition()).getPostType().equalsIgnoreCase("eventPost")) {
                    onFeedItemClickListener.onEventClick(view, cellFeedViewHolder.getAdapterPosition());
                } else {
                    onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());
                }
            }

        });

        cellFeedViewHolder.btnLike.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(view.getContext())){
                GuestLoginDialog.show(view.getContext());
                return;
            }
            onFeedItemClickListener.onHeartClick(view, cellFeedViewHolder.getAdapterPosition());
        });


        cellFeedViewHolder.likeText.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(view.getContext())){
                GuestLoginDialog.show(view.getContext());
                return;
            }
            onFeedItemClickListener.onHeartClick(view, cellFeedViewHolder.getAdapterPosition());
        });


        cellFeedViewHolder.inActiveLikeLayout.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(view.getContext())){
                GuestLoginDialog.show(view.getContext());
                return;
            }
            onFeedItemClickListener.onHeartClick(view, cellFeedViewHolder.getAdapterPosition());
        });

        cellFeedViewHolder.iconProfile.setOnClickListener(v -> onFeedItemClickListener.onProfileClick(view, cellFeedViewHolder.getAdapterPosition()));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((CellFeedViewHolder) viewHolder).bindView(feedItems.get(position));
        /*if (position == this.getItemCount() - 1){
            onFeedItemClickListener.onLoadMore(position);
        }*/
        if (getItemViewType(position) == VIEW_TYPE_LOADER) {
            bindLoadingFeedItem((LoadingCellFeedViewHolder) viewHolder);
        }
    }

    private void bindLoadingFeedItem(final LoadingCellFeedViewHolder holder) {
        holder.loadingFeedItemView.setOnLoadingFinishedListener(() -> {
            showLoadingView = false;
            notifyItemChanged(0);
        });
        holder.loadingFeedItemView.startLoading();
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoadingView && position == 0) {
            return VIEW_TYPE_LOADER;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public void showLoadingView() {
        showLoadingView = true;
        notifyItemChanged(0);
    }

    public void swap(List<Feed.FeedItem> feedItems) {
        if(feedItems != null){
            this.feedItems = feedItems;
        }
    }

    public void swap(List<Feed.FeedItem> feedItems, String designation) {
        if(feedItems != null && feedItems.size() > 0){
            this.feedItems.clear();
            this.feedItems.addAll(feedItems);
            this.notifyDataSetChanged();
        }
    }

    @Override
    public void onDataReady(final LinkPreview preview) {
        if (preview != null && mFeedsAdapter != null) {
            AppController.runOnUIThread(() -> {
                if(preview.getLink() != null && !preview.getLink().equalsIgnoreCase(""))
                    preview.setMessage(preview.getLink());
            });
        }
    }

    public  class CellFeedViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ic_report)
        ImageView btnReport;
        @BindView(R.id.btnComments)
        ImageButton btnComments;
        @BindView(R.id.btnLike)
        ImageButton btnLike;
        @BindView(R.id.tsLikesCounter)
        TextSwitcher tsLikesCounter;
        @BindView(R.id.like_text)
        TextView likeText;
        @BindView(R.id.dot)
        TextView tsLikesCommentDot;
        @BindView(R.id.tsCommentsCounter)
        TextSwitcher tsCommentCounter;
        @BindView(R.id.comment)
        TextView comment;
        @BindView(R.id.vImageRoot)
        LinearLayout vImageRoot;
        @BindView(R.id.event_layout)
        LinearLayout eventLayout;

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
        @BindView(R.id.icon_front)
        RelativeLayout iconFront;
        @BindView(R.id.icon_profile)
        SimpleDraweeView iconProfile;
        @BindView(R.id.icon_badge)
        ImageView iconBadge;
        @BindView(R.id.overflow)
        ImageView overflow;
        @BindView(R.id.favourite)
        ImageView favourite;
        @BindView(R.id.icon_text)
        TextView iconText;
        @BindView(R.id.news)
        TextView news;

        @BindView(R.id.top)
        TextView top;

        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.month)
        TextView month;

        @BindView(R.id.event_name)
        TextView eventName;
        @BindView(R.id.description)
        TextView description;

        @BindView(R.id.interestTv)
        TextView interestTv;

        @BindView(R.id.interestIv)
        ImageView interestIv;

        @BindView(R.id.event_image)
        SimpleDraweeView mSimpleDraweeView;


        @BindView(R.id.heading)
        TextView heading;

        @BindView(R.id.sub_head_lines)
        TextView subHeadlines;

        @BindView(R.id.news_time)
        TextView newsTime;

        @BindView(R.id.news_status)
        TextView newsStatus;

        @BindView(R.id.news_layout)
        LinearLayout newsLayout;

        @BindView(R.id.points)
        TextView points;

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

        @BindView(R.id.feeds_active_layout)
        RelativeLayout inActiveFeedsLayout;

        @BindView(R.id.overall_active_layout)
        RelativeLayout inActiveEntireLayout;

        @BindView(R.id.user_status_like_icon_layout)
        RelativeLayout inActiveLikeLayout;

        @BindView(R.id.user_status_comment_icon_layout)
        RelativeLayout inActiveCommentLayout;

        @BindView(R.id.ic_report_bottom)
        ImageView icReportBottom;


        @BindView(R.id.cardViewPreview)
        CardView cardViewPreview;

        @BindView(R.id.preview)
        LinkPreview mLinkPreview;

        @BindView(R.id.feeds_main)
        LinearLayout feedsMainLayout;

        @BindView(R.id.new_join_layout)
        LinearLayout newJoinLayout;

        @BindView(R.id.new_join_icon_profile)
        SimpleDraweeView newJoinIconProfile;

        @BindView(R.id.new_join_icon_text)
        TextView newJoinIconText;

        @BindView(R.id.new_join_top)
        TextView newJoinTop;

        @BindView(R.id.new_join_profile_name)
        TextView newJoinProfileName;

        @BindView(R.id.joined_time)
        TextView newJoinedTime;

        @BindView(R.id.head)
        LinearLayout head;

        @BindView(R.id.new_join_designation)
        TextView newJoinDesignation;


        Feed.FeedItem feedItem;
        private MyViewPagerAdapter myViewPagerAdapter;

        public CellFeedViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @SuppressLint("ClickableViewAccessibility")
        public void bindView(final Feed.FeedItem feedItem) {
            this.feedItem = feedItem;
            if(this.feedItem != null) {
                final int adapterPosition = getAdapterPosition();
                String[] split = feedItem.getPostedUserName().split("\\s+");

                if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost") && feedItem.getCompanyName() != null &&  !feedItem.getCompanyName().equalsIgnoreCase("") && feedItem.getCompanyId() != null &&  !feedItem.getCompanyId().equalsIgnoreCase("")){
                    if(context != null && feedItem.getCompanyImage() != null && !feedItem.getCompanyImage().equalsIgnoreCase("")){
                        Uri uri = Uri.parse(feedItem.getCompanyImage());
                        RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                        iconProfile.setHierarchy(new GenericDraweeHierarchyBuilder(context.getResources())
                                .setRoundingParams(roundingParams)
                                .build());
                        roundingParams.setRoundAsCircle(true);
                        iconProfile.setImageURI(uri);
                    }
                    iconProfile.setColorFilter(null);
                    iconText.setVisibility(View.GONE);
                } else {
                    if (feedItem.getProfilePic() != null && !feedItem.getProfilePic().equalsIgnoreCase("")){
                        if(feedItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || feedItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            iconProfile.setImageResource(R.drawable.bg_circle);
                            if(feedItem.getColorCode() != null && !feedItem.getColorCode().equalsIgnoreCase("") && feedItem.getColorCode().startsWith("#")){
                                iconProfile.setColorFilter(Color.parseColor(feedItem.getColorCode()));
                            } else {
                                iconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(context, "400"));
                            }

                            iconText.setVisibility(View.VISIBLE);
                            if (feedItem.getPostedUserName() != null && !feedItem.getPostedUserName().trim().equalsIgnoreCase("")){
                                if (split.length > 1){
                                    String first = split[0].trim().substring(0,1);
                                    String last = split[1].trim().substring(0,1);
                                    String initial = first + ""+ last ;
                                    iconText.setText(initial.toUpperCase().trim());
                                } else {
                                    String first = split[0].trim().substring(0,1);
                                    iconText.setText(first.toUpperCase().trim());
                                }
                            }
                        } else {
                            if(context != null && !feedItem.getProfilePic().equalsIgnoreCase("")){
                                Uri uri = Uri.parse(feedItem.getProfilePic());
                                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                                iconProfile.setHierarchy(new GenericDraweeHierarchyBuilder(context.getResources())
                                        .setRoundingParams(roundingParams)
                                        .build());
                                roundingParams.setRoundAsCircle(true);
                                iconProfile.setImageURI(uri);
                            }
                            iconProfile.setColorFilter(null);
                            iconText.setVisibility(View.GONE);
                        }
                    }
                }


                if (feedItem.isPostFavourited()) {
                    favourite.setTag("favourite");
                    favourite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_black_24dp));
                    favourite.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                } else {
                    favourite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_border_black_24dp));
                    favourite.setColorFilter(null);
                    favourite.setTag("favourited");
                }

                icReportBottom.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(context)){
                        GuestLoginDialog.show(context);
                        return;
                    }
                    onFeedItemClickListener.onInActiveReport(v, getAdapterPosition());
                });

                if(feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost")){
                    news.setVisibility(View.VISIBLE);
                    head.setVisibility(View.VISIBLE);
                    newJoinedTime.setVisibility(View.GONE);
                    news.setText("News");
                    if(feedItem.getCompanyName() != null && !feedItem.getCompanyName().equalsIgnoreCase("") && feedItem.getTimeStamp() != null && !feedItem.getTimeStamp().equalsIgnoreCase("")
                            && !feedItem.getTimeStamp().equalsIgnoreCase("true")
                            && !feedItem.getTimeStamp().equalsIgnoreCase("false")){
                        String twoSpaces = context.getResources().getString(R.string.two_spaces);
                        if(UserUtils.getNewsTime(Long.parseLong(UserUtils.parsingLong(feedItem.getTimeStamp()))) != null){
                            String timeAgo = twoSpaces + UserUtils.getNewsTime(Long.parseLong(UserUtils.parsingLong(feedItem.getTimeStamp())));
                            //time.setText("Published: "+timeAgo);
                            //time.setText(timeAgo);
                            //time.setVisibility(View.VISIBLE);
                            String dot = context.getResources().getString(R.string.dot);
                            SpannableStringBuilder spanned;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                spanned = new SpannableStringBuilder(Html.fromHtml(timeAgo +  dot + feedItem.getCompanyName(), Html.FROM_HTML_MODE_LEGACY));
                            } else {
                                spanned = new SpannableStringBuilder(Html.fromHtml(timeAgo +  dot + feedItem.getCompanyName()));
                            }

                            spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.black)), 0, spanned.toString().indexOf(dot)+1, 0);
                            spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorPrimary)),spanned.toString().indexOf(dot)+2, spanned.length(), 0);
                            newsTime.setText(spanned);
                            newsTime.setVisibility(View.VISIBLE);
                            newsTime.setOnClickListener(v -> {
                                if(feedItem.getPublisherUrl() != null && !feedItem.getPublisherUrl().equalsIgnoreCase("")){
                                    goToWeb(feedItem.getPublisherUrl());
                                }
                            });
                        } else {
                            newsTime.setVisibility(View.GONE);
                        }
                    } else {
                        newsTime.setVisibility(View.GONE);
                    }
                    timeStamp.setVisibility(View.GONE);
                } else {
                    news.setVisibility(View.GONE);
                    head.setVisibility(View.GONE);
                    newJoinedTime.setVisibility(View.GONE);
                    if (feedItem.getTimeStamp() != null && !feedItem.getTimeStamp().equalsIgnoreCase("")
                            && !feedItem.getTimeStamp().equalsIgnoreCase("true")
                            && !feedItem.getTimeStamp().equalsIgnoreCase("false")){
                        String timeAGO = UserUtils.getTimeAgo(Long.parseLong(UserUtils.parsingLong(feedItem.getTimeStamp())));
                        timeStamp.setText(timeAGO);
                        timeStamp.setVisibility(View.VISIBLE);
                    } else {
                        timeStamp.setVisibility(View.GONE);
                    }
                }

                if(feedItem.getPageName() != null && !feedItem.getPageName().equalsIgnoreCase("") && feedItem.getPageName().equalsIgnoreCase("modReportClear")){
                    String wordOne = "Reported By" + AppController.getInstance().getString(R.string.dot);
                    String reportedBy = feedItem.getReportBy();
                    String totalString = wordOne + " " + reportedBy;

                    SpannableStringBuilder spanned;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        spanned = new SpannableStringBuilder(Html.fromHtml(totalString, Html.FROM_HTML_MODE_LEGACY));
                        int s1 = wordOne.trim().length();
                        int total = spanned.length();
                        spanned.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s1, 0);
                        spanned.setSpan(new StyleSpan(Typeface.NORMAL), 0, s1, SPAN_INCLUSIVE_INCLUSIVE);
                        spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimary)), s1+1, total, 0);
                        spanned.setSpan(new StyleSpan(Typeface.BOLD), s1+1, total, SPAN_INCLUSIVE_INCLUSIVE);
                    }  else {
                        spanned = new SpannableStringBuilder(Html.fromHtml(totalString));
                        int s1 = wordOne.trim().length();
                        int total = spanned.length();
                        spanned.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s1, 0);
                        spanned.setSpan(new StyleSpan(Typeface.NORMAL), 0, s1, SPAN_INCLUSIVE_INCLUSIVE);
                        spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimary)), s1+1, total, 0);
                        spanned.setSpan(new StyleSpan(Typeface.BOLD), s1+1, total, SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    news.setVisibility(View.VISIBLE);
                    head.setVisibility(View.VISIBLE);
                    newJoinedTime.setVisibility(View.GONE);
                    news.setText(spanned);
                    news.setOnClickListener(v -> onFeedItemClickListener.onProfileClick(v, getAdapterPosition()));
                } else {
                    if(feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost")) {
                        news.setVisibility(View.VISIBLE);
                        news.setText("News");
                        head.setVisibility(View.VISIBLE);
                        newJoinedTime.setVisibility(View.GONE);
                    } else if(feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newUserJoined")) {
                        news.setVisibility(View.VISIBLE);
                        head.setVisibility(View.VISIBLE);
                        if(feedItem.getJoinedTime() != null && !feedItem.getJoinedTime().equalsIgnoreCase("")
                                && !feedItem.getJoinedTime().equalsIgnoreCase("true")
                                && !feedItem.getJoinedTime().equalsIgnoreCase("false")){
                            String timeAGO = UserUtils.getTimeAgo(Long.parseLong(UserUtils.parsingLong(feedItem.getJoinedTime())));
                            news.setText("Joined MyScrap");
                            newJoinedTime.setText(timeAGO);
                            newJoinedTime.setVisibility(View.VISIBLE);
                            news.setVisibility(View.VISIBLE);
                        } else{
                            news.setText("Joined MyScrap");
                            news.setVisibility(View.VISIBLE);
                            newJoinedTime.setVisibility(View.GONE);
                        }
                    } else {
                        news.setVisibility(View.GONE);
                        head.setVisibility(View.GONE);
                        newJoinedTime.setVisibility(View.GONE);
                    }
                    if(feedItem.isReported()){
                        if(feedItem.getReportedUserId() != null && feedItem.getPostedUserId() != null){
                            if(feedItem.getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                inActiveEntireLayout.setVisibility(View.VISIBLE);
                                inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                                icReportBottom.setVisibility(View.VISIBLE);
                            } else if(feedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                                inActiveEntireLayout.setVisibility(View.VISIBLE);
                                inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                                icReportBottom.setVisibility(View.VISIBLE);
                            } else {
                                inActiveEntireLayout.setVisibility(View.VISIBLE);
                                inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                                icReportBottom.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        inActiveEntireLayout.setVisibility(View.GONE);
                        inActiveFeedsLayout.setVisibility(View.GONE);
                    }
                }

                if(feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newUserJoined")){
                    newJoinLayout.setVisibility(View.VISIBLE);
                    feedsMainLayout.setVisibility(View.GONE);

                    if (feedItem.getProfilePic() != null && !feedItem.getProfilePic().equalsIgnoreCase("")){
                        if(feedItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || feedItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            newJoinIconProfile.setImageResource(R.drawable.bg_circle);
                            if(feedItem.getColorCode() != null && !feedItem.getColorCode().equalsIgnoreCase("")){
                                newJoinIconProfile.setColorFilter(Color.parseColor(feedItem.getColorCode()));
                            } else {
                                newJoinIconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(context, "400"));
                            }

                            newJoinIconText.setVisibility(View.VISIBLE);
                            if (feedItem.getPostedUserName() != null && !feedItem.getPostedUserName().trim().equalsIgnoreCase("")){
                                if (split.length > 1){
                                    String first = split[0].trim().substring(0,1);
                                    String last = split[1].trim().substring(0,1);
                                    String initial = first + ""+ last ;
                                    newJoinIconText.setText(initial.toUpperCase().trim());
                                } else {
                                    if (split[0] != null && split[0].trim().length() >= 1) {
                                        String first = split[0].trim().substring(0, 1);
                                        newJoinIconText.setText(first.toUpperCase().trim());
                                    }
                                }
                            }
                        } else {
                            if(context != null && !feedItem.getProfilePic().equalsIgnoreCase("")){
                                Uri uri = Uri.parse(feedItem.getProfilePic());
                                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                                newJoinIconProfile.setHierarchy(new GenericDraweeHierarchyBuilder(context.getResources())
                                        .setRoundingParams(roundingParams)
                                        .build());
                                roundingParams.setRoundAsCircle(true);
                                newJoinIconProfile.setImageURI(uri);
                            }
                            newJoinIconProfile.setColorFilter(null);
                            newJoinIconText.setVisibility(View.GONE);
                        }
                    }

                    newJoinLayout.setOnClickListener(v -> onFeedItemClickListener.onProfileClick(v, getAdapterPosition()));


                    if(feedItem.getPostedUserName() != null && !feedItem.getPostedUserName().equalsIgnoreCase("")){
                        newJoinProfileName.setText(feedItem.getPostedUserName());
                        newJoinProfileName.setVisibility(View.VISIBLE);
                    } else {
                        newJoinProfileName.setVisibility(View.GONE);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        SpannableStringBuilder spannedDetails;
                        if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost") && feedItem.getCompanyName() != null &&  !feedItem.getCompanyName().equalsIgnoreCase("") && feedItem.getCompanyId() != null &&  !feedItem.getCompanyId().equalsIgnoreCase("")){
                            if(feedItem.getPostBy() != null){
                                newJoinDesignation.setText(feedItem.getPostBy());
                                newJoinDesignation.setOnClickListener(v -> {
                                    if(feedItem.getPostedUserId() != null && !feedItem.getPostedUserId().equalsIgnoreCase(""))
                                        goToAuthorProfile(feedItem.getPostedUserId());
                                });
                            } else {
                                newJoinDesignation.setText("Admin");
                            }
                            newJoinDesignation.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("") && feedItem.getPostedUserDesignation() != null && !feedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                                String position = feedItem.getPostedUserDesignation() + ", " +feedItem.getUserCompany();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +feedItem.getPostedUserDesignation()+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+feedItem.getUserCompany()+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("") && feedItem.getPostedUserDesignation() == null ){
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +feedItem.getUserCompany()+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            }  else if(feedItem.getUserCompany() == null && feedItem.getPostedUserDesignation() != null && !feedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                                String position = feedItem.getPostedUserDesignation();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +position+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("")){
                                String position = feedItem.getUserCompany();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +position+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                newJoinDesignation.setText("TRADER");
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    else {
                        SpannableStringBuilder spannedDetails;
                        if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost") && feedItem.getCompanyName() != null &&  !feedItem.getCompanyName().equalsIgnoreCase("") && feedItem.getCompanyId() != null &&  !feedItem.getCompanyId().equalsIgnoreCase("")){
                            if(feedItem.getPostBy() != null){
                                newJoinDesignation.setText(feedItem.getPostBy());
                                newJoinDesignation.setOnClickListener(v -> {
                                    if(feedItem.getPostedUserId() != null && !feedItem.getPostedUserId().equalsIgnoreCase(""))
                                        goToAuthorProfile(feedItem.getPostedUserId());
                                });
                            } else {
                                newJoinDesignation.setText("Admin");
                            }
                            newJoinDesignation.setVisibility(View.VISIBLE);
                        } else {
                            if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("") && feedItem.getPostedUserDesignation() != null && !feedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +feedItem.getPostedUserDesignation()+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+feedItem.getUserCompany()+"&#160" + "</font>"));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("") && feedItem.getPostedUserDesignation() == null ){
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +feedItem.getUserCompany()+ "</font>"));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            }  else if(feedItem.getUserCompany() == null && feedItem.getPostedUserDesignation() != null && !feedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                                String position = feedItem.getPostedUserDesignation();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +position+ "</font>"));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("")){
                                String position = feedItem.getUserCompany();
                                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#FFFFFF\">" +position+ "</font>"));
                                newJoinDesignation.setText(spannedDetails);
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            } else {
                                newJoinDesignation.setText("TRADER");
                                newJoinDesignation.setVisibility(View.VISIBLE);
                            }
                        }

                    }

                    if(feedItem.getModerator() == 1) {
                        newJoinTop.setText(R.string.mod);
                        newJoinTop.setVisibility(View.VISIBLE);
                        newJoinTop.setBackground(ContextCompat.getDrawable(context, R.drawable.top_mod));
                        iconBadge.setVisibility(View.GONE);
                    } else {
                        if(feedItem.getRank() != 0){
                            if (feedItem.getRank() >= 1 && feedItem.getRank() <=10) {
                                iconBadge.setVisibility(View.GONE);
                                newJoinTop.setVisibility(View.VISIBLE);
                                newJoinTop.setText( "TOP " + feedItem.getRank());
                                newJoinTop.setTextColor(ContextCompat.getColor(context, R.color.white));
                                newJoinTop.setBackground(ContextCompat.getDrawable(context, R.drawable.top));
                            } else {
                                if(feedItem.isNewJoined()){
                                    newJoinTop.setText(R.string.new_user);
                                    newJoinTop.setVisibility(View.VISIBLE);
                                    newJoinTop.setTextColor(ContextCompat.getColor(context, R.color.white));
                                    newJoinTop.setBackground(ContextCompat.getDrawable(context, R.drawable.top_red));
                                } else {
                                    newJoinTop.setVisibility(View.GONE);
                                    newJoinTop.setBackground(null);
                                }
                                iconBadge.setVisibility(View.GONE);
                            }
                        }
                    }
                } else {
                    newJoinLayout.setVisibility(View.GONE);
                    feedsMainLayout.setVisibility(View.VISIBLE);
                }

                if(feedItem.getPostType() != null && !feedItem.getPostType().equalsIgnoreCase("newsPost")){

                    if(feedItem.getModerator() == 1) {
                        top.setText(R.string.mod);
                        top.setVisibility(View.VISIBLE);
                        top.setBackground(ContextCompat.getDrawable(context, R.drawable.top_mod));
                        iconBadge.setVisibility(View.GONE);
                    } else {
                        if(feedItem.getRank() != 0){
                            if (feedItem.getRank() >= 1 && feedItem.getRank() <=10) {
                                iconBadge.setVisibility(View.GONE);
                                top.setVisibility(View.VISIBLE);
                                top.setText( "TOP " + feedItem.getRank());
                                top.setTextColor(ContextCompat.getColor(context, R.color.white));
                                top.setBackground(ContextCompat.getDrawable(context, R.drawable.top));
                            } else {
                                if(feedItem.isNewJoined()){
                                    top.setText(R.string.new_user);
                                    top.setVisibility(View.VISIBLE);
                                    top.setTextColor(ContextCompat.getColor(context, R.color.white));
                                    top.setBackground(ContextCompat.getDrawable(context, R.drawable.top_red));
                                } else {
                                    top.setVisibility(View.GONE);
                                    top.setBackground(null);
                                }
                                iconBadge.setVisibility(View.GONE);
                            }
                        }
                    }
                } else {
                    iconBadge.setVisibility(View.GONE);
                    top.setVisibility(View.GONE);
                }

                String word1;
                String word2;
                String name;
                if (feedItem.getPostedUserName() != null && feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("userProfilePost") || feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("friendProfilePost")){
                    word1 = feedItem.getPostedUserName().trim();
                    word2 = " updated profile picture.";
                    name = word1+ word2;

                    ClickableSpan nameClickableSpan = new ClickableSpan() {

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ds.setColor(ContextCompat.getColor(context, R.color.black));
                            } else {
                                ds.setColor(ContextCompat.getColor(context, R.color.black));
                            }
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(View view) {
                            onFeedItemClickListener.onProfileClick(view, adapterPosition);
                        }
                    };

                    ForegroundColorSpan spanColorBlack = new ForegroundColorSpan(Color.BLACK);
                    ForegroundColorSpan spanColorGray = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.updated_profile_pic));
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
                    sBuilder.setSpan(new StyleSpan(Typeface.BOLD),  name.indexOf(word1), name.indexOf(word1) + String.valueOf(word1).length(), SPAN_INCLUSIVE_INCLUSIVE);
                    sBuilder.setSpan(
                            spanColorGray,
                            sBuilder.length()-word2.length(),
                            sBuilder.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    profileName.setText(sBuilder);
                    profileName.setMovementMethod(LinkMovementMethod.getInstance());
                } else if (feedItem.getPostedUserName() != null && feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("eventPost")){
                    word1 = feedItem.getPostedUserName().trim();
                    word2 = " has posted an event.";
                    name = word1+ word2;

                    ClickableSpan nameClickableSpan = new ClickableSpan() {

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ds.setColor(ContextCompat.getColor(context, R.color.black));
                            } else {
                                ds.setColor(ContextCompat.getColor(context, R.color.black));
                            }
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(View view) {
                            onFeedItemClickListener.onProfileClick(view, adapterPosition);
                        }
                    };

                    ForegroundColorSpan spanColorBlack = new ForegroundColorSpan(Color.BLACK);
                    ForegroundColorSpan spanColorGray = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.updated_profile_pic));
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
                    sBuilder.setSpan(new StyleSpan(Typeface.BOLD),  name.indexOf(word1), name.indexOf(word1) + String.valueOf(word1).length(), SPAN_INCLUSIVE_INCLUSIVE);
                    sBuilder.setSpan(
                            spanColorGray,
                            sBuilder.length()-word2.length(),
                            sBuilder.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    profileName.setText(sBuilder);
                    profileName.setMovementMethod(LinkMovementMethod.getInstance());
                } else if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("friendUserPost") && feedItem.getPostedUserName() != null &&  feedItem.getPostedFriendName() != null){
                    final String userName = feedItem.getPostedUserName().trim();
                    final String rightArrow = "&#9654";
                    final String friendName = feedItem.getPostedFriendName().trim();
                    String postedName = userName+ rightArrow+ friendName;
                    ClickableSpan nameClickableSpan = new ClickableSpan() {

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ds.setColor(ContextCompat.getColor(context, R.color.black));
                            } else {
                                ds.setColor(ContextCompat.getColor(context, R.color.black));
                            }
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(View view) {
                            onFeedItemClickListener.onPostFromClick(view, adapterPosition);
                        }
                    };

                    ClickableSpan friendNameClickableSpan = new ClickableSpan() {

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ds.setColor(ContextCompat.getColor(context, R.color.black));
                            } else {
                                ds.setColor(ContextCompat.getColor(context, R.color.black));
                            }
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(View view) {
                            onFeedItemClickListener.onPostToClick(view, adapterPosition);
                        }
                    };

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml(userName +  " &#9658 "  + friendName  , Html.FROM_HTML_MODE_LEGACY));
                        int s1 = userName.length();
                        int total = spanned.length();
                        spanned.setSpan(nameClickableSpan, 0, s1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanned.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s1, 0);
                        spanned.setSpan(new StyleSpan(Typeface.BOLD), 0, s1, SPAN_INCLUSIVE_INCLUSIVE);
                        spanned.setSpan(friendNameClickableSpan, s1+1, total, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanned.setSpan(new ForegroundColorSpan(Color.BLACK), s1+1, total, 0);
                        spanned.setSpan(new StyleSpan(Typeface.BOLD), s1+1, total, SPAN_INCLUSIVE_INCLUSIVE);
                        profileName.setText(spanned);
                    } else {
                        final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml(userName +  " &#9658 "  + friendName));
                        int s1 = userName.trim().length();
                        int total = spanned.length();
                        spanned.setSpan(nameClickableSpan, 0, s1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanned.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s1, 0);
                        spanned.setSpan(new StyleSpan(Typeface.BOLD), 0, s1, SPAN_INCLUSIVE_INCLUSIVE);
                        spanned.setSpan(friendNameClickableSpan, s1+1, total, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanned.setSpan(new ForegroundColorSpan(Color.BLACK), s1+1, total, 0);
                        spanned.setSpan(new StyleSpan(Typeface.BOLD), s1+1, total, SPAN_INCLUSIVE_INCLUSIVE);
                        profileName.setText(spanned);
                    }
                    profileName.setMovementMethod(LinkMovementMethod.getInstance());
                } else if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost") && feedItem.getCompanyName() != null &&  !feedItem.getCompanyName().equalsIgnoreCase("") && feedItem.getCompanyId() != null &&  !feedItem.getCompanyId().equalsIgnoreCase("")){
                    if(feedItem.getHeading() != null && !feedItem.getHeading().equalsIgnoreCase("")) {
                        SpannableStringBuilder spanned;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            spanned = new SpannableStringBuilder(Html.fromHtml(feedItem.getHeading().trim(), Html.FROM_HTML_MODE_LEGACY));
                        }  else {
                            spanned = new SpannableStringBuilder(Html.fromHtml(feedItem.getHeading().trim()));
                        }
                        int s1 = feedItem.getHeading().trim().length();
                        spanned.setSpan(new StyleSpan(Typeface.BOLD), 0, s1, SPAN_INCLUSIVE_INCLUSIVE);
                        profileName.setText(spanned);
                        profileName.setOnClickListener(v -> {
                            if(UserUtils.isGuestLoggedIn(v.getContext())){
                                GuestLoginDialog.show(v.getContext());
                                return;
                            }
                            onFeedItemClickListener.onCommentsClick(v, adapterPosition);
                        });
                    }
                } else if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("eventUserPost")){
                    if(feedItem.getPostedUserName() != null){
                        SpannableStringBuilder spanned;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            spanned = new SpannableStringBuilder(Html.fromHtml(feedItem.getPostedUserName().trim(), Html.FROM_HTML_MODE_LEGACY));
                        }  else {
                            spanned = new SpannableStringBuilder(Html.fromHtml(feedItem.getPostedUserName().trim()));
                        }
                        int s1 = feedItem.getPostedUserName().trim().length();
                        spanned.setSpan(new StyleSpan(Typeface.BOLD), 0, s1, SPAN_INCLUSIVE_INCLUSIVE);
                        profileName.setText(spanned);
                        profileName.setOnClickListener(v -> onFeedItemClickListener.onProfileClick(v, adapterPosition));
                    }

                } else {
                    if(feedItem.getPostedUserName() != null){
                        SpannableStringBuilder spanned;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            spanned = new SpannableStringBuilder(Html.fromHtml(feedItem.getPostedUserName().trim(), Html.FROM_HTML_MODE_LEGACY));
                        }  else {
                            spanned = new SpannableStringBuilder(Html.fromHtml(feedItem.getPostedUserName().trim()));
                        }
                        int s1 = feedItem.getPostedUserName().trim().length();
                        spanned.setSpan(new StyleSpan(Typeface.BOLD), 0, s1, SPAN_INCLUSIVE_INCLUSIVE);
                        profileName.setText(spanned);
                        profileName.setOnClickListener(v -> onFeedItemClickListener.onProfileClick(v, adapterPosition));
                    }

                }


                btnReport.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(context)){
                        GuestLoginDialog.show(context);
                        return;
                    }
                    if(feedItem.getPageName() != null && !feedItem.getPageName().equalsIgnoreCase("") && feedItem.getPageName().equalsIgnoreCase("modReportClear")){
                        onFeedItemClickListener.onInActiveReport(v, getAdapterPosition());
                    } else {
                        onFeedItemClickListener.onReport(v, inActiveFeedsLayout, getAdapterPosition());
                    }

                });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SpannableStringBuilder spannedDetails;
                    if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost") && feedItem.getCompanyName() != null &&  !feedItem.getCompanyName().equalsIgnoreCase("") && feedItem.getCompanyId() != null &&  !feedItem.getCompanyId().equalsIgnoreCase("")){
                        if(feedItem.getPostBy() != null){
                            designation.setText(feedItem.getPostBy());
                            designation.setOnClickListener(v -> {
                                if(feedItem.getPostedUserId() != null && !feedItem.getPostedUserId().equalsIgnoreCase(""))
                                    goToAuthorProfile(feedItem.getPostedUserId());
                            });
                        } else {
                            designation.setText("Admin");
                        }
                        designation.setVisibility(View.VISIBLE);
                    } else {
                        if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("") && feedItem.getPostedUserDesignation() != null && !feedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                            String position = feedItem.getPostedUserDesignation() + ", " +feedItem.getUserCompany();
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +feedItem.getPostedUserDesignation()+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+feedItem.getUserCompany()+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                            designation.setText(spannedDetails);
                            designation.setVisibility(View.VISIBLE);
                        } else if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("") && feedItem.getPostedUserDesignation() == null ){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +feedItem.getUserCompany()+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                            designation.setText(spannedDetails);
                            designation.setVisibility(View.VISIBLE);
                        }  else if(feedItem.getUserCompany() == null && feedItem.getPostedUserDesignation() != null && !feedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                            String position = feedItem.getPostedUserDesignation();
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +position+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                            designation.setText(spannedDetails);
                            designation.setVisibility(View.VISIBLE);
                        } else if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("")){
                            String position = feedItem.getUserCompany();
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +position+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                            designation.setText(spannedDetails);
                            designation.setVisibility(View.VISIBLE);
                        } else {
                            designation.setText("TRADER");
                            designation.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    SpannableStringBuilder spannedDetails;
                    if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost") && feedItem.getCompanyName() != null &&  !feedItem.getCompanyName().equalsIgnoreCase("") && feedItem.getCompanyId() != null &&  !feedItem.getCompanyId().equalsIgnoreCase("")){
                        if(feedItem.getPostBy() != null){
                            designation.setText(feedItem.getPostBy());
                            designation.setOnClickListener(v -> {
                                if(feedItem.getPostedUserId() != null && !feedItem.getPostedUserId().equalsIgnoreCase(""))
                                    goToAuthorProfile(feedItem.getPostedUserId());
                            });
                        } else {
                            designation.setText("Admin");
                        }
                        designation.setVisibility(View.VISIBLE);
                    } else {
                        if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("") && feedItem.getPostedUserDesignation() != null && !feedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +feedItem.getPostedUserDesignation()+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+feedItem.getUserCompany()+"&#160" + "</font>"));
                            designation.setText(spannedDetails);
                            designation.setVisibility(View.VISIBLE);
                        } else if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("") && feedItem.getPostedUserDesignation() == null ){
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +feedItem.getUserCompany()+ "</font>"));
                            designation.setText(spannedDetails);
                            designation.setVisibility(View.VISIBLE);
                        }  else if(feedItem.getUserCompany() == null && feedItem.getPostedUserDesignation() != null && !feedItem.getPostedUserDesignation().equalsIgnoreCase("")){
                            String position = feedItem.getPostedUserDesignation();
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +position+ "</font>"));
                            designation.setText(spannedDetails);
                            designation.setVisibility(View.VISIBLE);
                        } else if(feedItem.getUserCompany() != null && !feedItem.getUserCompany().equalsIgnoreCase("")){
                            String position = feedItem.getUserCompany();
                            spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +position+ "</font>"));
                            designation.setText(spannedDetails);
                            designation.setVisibility(View.VISIBLE);
                        } else {
                            designation.setText("TRADER");
                            designation.setVisibility(View.VISIBLE);
                        }
                    }

                }

                if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost")){
                    newsLayout.setVisibility(View.VISIBLE);
                    newsTime.setVisibility(View.VISIBLE);
                    if(feedItem.getSubHeading() != null && !feedItem.getSubHeading().equalsIgnoreCase("")){
                        subHeadlines.setText(feedItem.getSubHeading());
                        subHeadlines.setVisibility(View.VISIBLE);
                    } else {
                        subHeadlines.setVisibility(View.GONE);
                    }

                    heading.setVisibility(View.GONE);
                    designation.setVisibility(View.GONE);
                    if (feedItem.getStatus() != null && !feedItem.getStatus().equalsIgnoreCase("")){
                        if (feedItem.getStatus().length() > 350) {
                            final String statusString;
                            if(feedItem.getNewsLocation() != null && !feedItem.getNewsLocation().equalsIgnoreCase("")){
                                String loc = feedItem.getNewsLocation()+ " : ";
                                statusString = loc + feedItem.getStatus().trim();
                            } else {
                                statusString = feedItem.getStatus().trim();
                            }
                            String splitString = statusString.substring(0, 350);
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
                                    SpannableStringBuilder sBuilder = new SpannableStringBuilder(statusString);
                                    if(feedItem.getTagList() != null){
                                        for (Feed.FeedItem.TagList mData : feedItem.getTagList()){
                                            Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                                            Matcher matcher = pattern.matcher(feedItem.getStatus());
                                            while (matcher.find()) {
                                                sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), getAdapterPosition()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            }
                                        }
                                    }
                                    newsStatus.setText(sBuilder);
                                    Linkify.addLinks(status, Linkify.WEB_URLS);
                                    AppController.getInstance().stripUnderlines(newsStatus);
                                    newsStatus.setMovementMethod(LinkMovementMethod.getInstance());
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


                            if(feedItem.getTagList() != null){
                                for (Feed.FeedItem.TagList mData : feedItem.getTagList()){
                                    Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                                    Matcher matcher = pattern.matcher(feedItem.getStatus());
                                    while (matcher.find()) {
                                        if(matcher.start() < 370 && matcher.end() < 370){
                                            sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), getAdapterPosition()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                }
                            }
                            newsStatus.setText(sBuilder);
                            Linkify.addLinks(status, Linkify.WEB_URLS);
                            AppController.getInstance().stripUnderlines(newsStatus);
                            newsStatus.setMovementMethod(LinkMovementMethod.getInstance());
                        } else {
                            final String statusString;
                            if(feedItem.getNewsLocation() != null && !feedItem.getNewsLocation().equalsIgnoreCase("")){
                                String loc = feedItem.getNewsLocation()+ " : ";
                                statusString = loc + feedItem.getStatus().trim();
                            } else {
                                statusString = feedItem.getStatus().trim();
                            }
                            SpannableStringBuilder sBuilder = new SpannableStringBuilder(statusString);
                            if(feedItem.getTagList() != null) {
                                for (Feed.FeedItem.TagList mData : feedItem.getTagList()) {
                                    Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                                    Matcher matcher = pattern.matcher(feedItem.getStatus());
                                    while (matcher.find()) {
                                        if(matcher.start() < 370 && matcher.end() < 370){
                                            sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), getAdapterPosition()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                }
                            }
                            newsStatus.setText(sBuilder);
                            Linkify.addLinks(status, Linkify.WEB_URLS);
                            AppController.getInstance().stripUnderlines(newsStatus);
                            newsStatus.setMovementMethod(LinkMovementMethod.getInstance());
                        }
                        newsStatus.setVisibility(View.VISIBLE);
                    } else {
                        newsStatus.setVisibility(View.GONE);
                    }
                    status.setVisibility(View.GONE);
                } else {
                    subHeadlines.setVisibility(View.GONE);
                    heading.setVisibility(View.GONE);
                    newsLayout.setVisibility(View.GONE);
                    newsTime.setVisibility(View.GONE);
                    newsStatus.setVisibility(View.GONE);
                    if (feedItem.getStatus() != null && !feedItem.getStatus().equalsIgnoreCase("")){
                        if (feedItem.getStatus().length() > 350) {
                            final String statusString = feedItem.getStatus().trim();
                            String splitString = feedItem.getStatus().substring(0, 350);
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
                                    SpannableStringBuilder sBuilder = new SpannableStringBuilder(statusString);
                                    if(feedItem.getTagList() != null){
                                        for (Feed.FeedItem.TagList mData : feedItem.getTagList()){
                                            Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                                            Matcher matcher = pattern.matcher(feedItem.getStatus());
                                            while (matcher.find()) {
                                                sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), getAdapterPosition()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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


                            if(feedItem.getTagList() != null){
                                for (Feed.FeedItem.TagList mData : feedItem.getTagList()){
                                    Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                                    Matcher matcher = pattern.matcher(feedItem.getStatus());
                                    while (matcher.find()) {
                                        if(matcher.start() < 370 && matcher.end() < 370){
                                            sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), getAdapterPosition()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                }
                            }
                            status.setText(sBuilder);
                            Linkify.addLinks(status, Linkify.WEB_URLS);
                            AppController.getInstance().stripUnderlines(status);
                            status.setMovementMethod(LinkMovementMethod.getInstance());
                        } else {
                            SpannableStringBuilder sBuilder = new SpannableStringBuilder(feedItem.getStatus());
                            if(feedItem.getTagList() != null) {
                                for (Feed.FeedItem.TagList mData : feedItem.getTagList()) {
                                    Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                                    Matcher matcher = pattern.matcher(feedItem.getStatus());
                                    while (matcher.find()) {
                                        sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), getAdapterPosition()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                }
                            }
                            status.setText(sBuilder);
                            Linkify.addLinks(status, Linkify.WEB_URLS);
                            AppController.getInstance().stripUnderlines(status);
                            status.setMovementMethod(LinkMovementMethod.getInstance());
                        }
                        status.setVisibility(View.VISIBLE);
                    } else {
                        status.setVisibility(View.GONE);
                    }
                }

                boolean isShowFeedImages = false;
                if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("eventPost")) {
                    eventLayout.setVisibility(View.VISIBLE);
                    viewPagerLayout.setVisibility(View.GONE);
                    vImageRoot.setVisibility(View.GONE);
                    if(feedItem.getEventPicture() != null && !feedItem.getEventPicture().equalsIgnoreCase("")) {
                        Uri uri = Uri.parse(feedItem.getEventPicture());

                        mSimpleDraweeView.setVisibility(View.VISIBLE);
                        mSimpleDraweeView.post(() -> {
                            ImageRequest imgReq = ImageRequestBuilder.newBuilderWithSource(uri)
                                    .setProgressiveRenderingEnabled(true)
                                    .build();
                            DraweeController controller = Fresco.newDraweeControllerBuilder()
                                    .setImageRequest(imgReq)
                                    .setTapToRetryEnabled(true)
                                    .setOldController(mSimpleDraweeView.getController())
                                    .build();
                            mSimpleDraweeView.getHierarchy().setPlaceholderImage(R.drawable.no_events_image_pink_blue_cover, ScalingUtils.ScaleType.CENTER_CROP);
                            mSimpleDraweeView.getHierarchy().setFadeDuration(0);
                            mSimpleDraweeView.getHierarchy().setProgressBarImage(new ProgressBarDrawable(), ScalingUtils.ScaleType.CENTER);
                            mSimpleDraweeView.setController(controller);
                        });
                    } else {
                        Uri uri = new Uri.Builder()
                                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                                .path(String.valueOf(R.drawable.no_events_image_pink_blue_cover))
                                .build();
                        mSimpleDraweeView.getHierarchy().setFadeDuration(0);
                        mSimpleDraweeView.getHierarchy().setProgressBarImage(new ProgressBarDrawable(), ScalingUtils.ScaleType.CENTER);
                        mSimpleDraweeView.setImageURI(uri);
                    }


                    if(feedItem.isInterested()) {
                        interestIv.setTag("interested");
                        interestIv.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                        interestTv.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        EVENT_INTEREST_OR_NOT = 1;
                    } else {
                        EVENT_INTEREST_OR_NOT = 0;
                        interestIv.setTag("interest");
                        interestIv.setColorFilter(ContextCompat.getColor(context, R.color.black));
                        interestTv.setTextColor(ContextCompat.getColor(context, R.color.black));
                    }


                    interestIv.setOnClickListener(view -> {
                        if (interestIv.getTag().equals("interested")){
                            EVENT_INTEREST_OR_NOT = 0;
                            interestIv.setTag("interest");
                            interestIv.setColorFilter(ContextCompat.getColor(context, R.color.black));
                            interestTv.setTextColor(ContextCompat.getColor(context, R.color.black));
                        } else {
                            interestIv.setTag("interested");
                            interestIv.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                            interestTv.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                            EVENT_INTEREST_OR_NOT = 1;
                        }
                        sendInterest(EVENT_INTEREST_OR_NOT, feedItem.getEventId(), interestIv);
                    });


                    eventLayout.setOnClickListener(view -> onFeedItemClickListener.onEventClick(view, getAdapterPosition()));

                    interestTv.setOnClickListener(view -> {
                        if (interestIv.getTag().equals("interested")){
                            EVENT_INTEREST_OR_NOT = 0;
                            interestIv.setTag("interest");
                            interestIv.setColorFilter(ContextCompat.getColor(context, R.color.black));
                            interestTv.setTextColor(ContextCompat.getColor(context, R.color.black));
                        } else {
                            interestIv.setTag("interested");
                            interestIv.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
                            interestTv.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                            EVENT_INTEREST_OR_NOT = 1;
                        }
                        sendInterest(EVENT_INTEREST_OR_NOT, feedItem.getEventId(), interestIv);
                    });

                    String formatDateFormat;

                    if(feedItem.getStartDate() != null && !feedItem.getStartDate().equalsIgnoreCase("")){
                        formatDateFormat = convertToDateFormat(feedItem.getStartDate());
                        String[] splitDate = formatDateFormat.split(" ");
                        String dateString = splitDate[0];
                        date.setText(dateString);
                        String monthString = splitDate[1];
                        month.setText(monthString);
                    }

                    if(feedItem.getEventName() != null && !feedItem.getEventName().equalsIgnoreCase("")){
                        eventName.setText(feedItem.getEventName());
                    }

                    if(feedItem.getEventDetail() != null && !TextUtils.isEmpty(feedItem.getEventDetail())){
                        description.setText(feedItem.getEventDetail());
                    }

                } else {
                    eventLayout.setVisibility(View.GONE);
                    List<PictureUrl> mPictureUrl = feedItem.getPictureUrl();
                    if (mPictureUrl != null && mPictureUrl.size() != 0) {
                        myViewPagerAdapter = new MyViewPagerAdapter();
                        viewPager.setAdapter(myViewPagerAdapter);
                        myViewPagerAdapter.swap(mPictureUrl, adapterPosition);
                        addBottomDots(0);
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
                        viewPagerLayout.setVisibility(View.VISIBLE);
                        vImageRoot.setVisibility(View.VISIBLE);
                        isShowFeedImages = true;
                    } else {
                        isShowFeedImages = false;
                        viewPagerLayout.setVisibility(View.GONE);
                        vImageRoot.setVisibility(View.GONE);
                    }
                }

                if(isShowFeedImages){
                    cardViewPreview.setVisibility(View.GONE);
                } else {
                    if (feedItem.getPostType() != null && !feedItem.getPostType().equalsIgnoreCase("newsPost")){
                        final List<String> extractedUrls = UserUtils.extractUrls(feedItem.getStatus());
                        if (extractedUrls != null && extractedUrls.size() > 0) {
                            mLinkPreview.setListener(mPreviewListener);
                            cardViewPreview.setVisibility(View.VISIBLE);
                            mLinkPreview.setData(extractedUrls.get((extractedUrls.size()-1)));
                            mLinkPreview.setOnClickListener(v -> UserUtils.launchCustomTabURL(context, extractedUrls.get((extractedUrls.size()-1))));
                            newsStatus.setVisibility(View.GONE);
                        } else {
                            cardViewPreview.setVisibility(View.GONE);
                        }
                    } else {
                        cardViewPreview.setVisibility(View.GONE);
                    }
                }

                btnLike.setImageResource(feedItem.isLikeStatus() ? R.drawable.ic_heart_outline_black_filled : R.drawable.ic_heart_outline_black);
                if(tsCommentCounter != null){
                    if(feedItem.getCommentCount()==0) {
                        tsCommentCounter.setCurrentText(context.getString(R.string.feed_comment));
                    } else {
                        tsCommentCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                R.plurals.comments_count, feedItem.getCommentCount(), feedItem.getCommentCount()
                        ));
                    }
                }

                if(tsLikesCounter != null){
                    if(feedItem.getLikeCount()==0) {
                        tsLikesCounter.setCurrentText(context.getString(R.string.feed_like));
                    } else {
                        tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                                R.plurals.likes_count, feedItem.getLikeCount(), feedItem.getLikeCount()
                        ));
                    }
                }


                if(feedItem.getLikeCount() > 0 && feedItem.getCommentCount() > 0){
                    tsLikesCounter.setVisibility(View.VISIBLE);
                    tsCommentCounter.setVisibility(View.VISIBLE);
                    tsLikesCommentDot.setVisibility(View.VISIBLE);
                } else if (feedItem.getLikeCount() > 0 && feedItem.getCommentCount() ==  0){
                    tsLikesCounter.setVisibility(View.VISIBLE);
                    tsCommentCounter.setVisibility(View.GONE);
                    tsLikesCommentDot.setVisibility(View.GONE);
                } else if (feedItem.getLikeCount() == 0 && feedItem.getCommentCount() >  0){
                    tsLikesCounter.setVisibility(View.GONE);
                    tsCommentCounter.setVisibility(View.VISIBLE);
                    tsLikesCommentDot.setVisibility(View.GONE);
                } else {
                    tsLikesCounter.setVisibility(View.GONE);
                    tsCommentCounter.setVisibility(View.GONE);
                    tsLikesCommentDot.setVisibility(View.GONE);
                }

                tsLikesCounter.setOnClickListener(v -> {
                    if(UserUtils.isGuestLoggedIn(context)){
                        GuestLoginDialog.show(context);
                        return;
                    }
                    if(feedItem.getLikeCount() > 0)
                        screenMoveToLikeActivity(feedItem.getPostId(), feedItem.getPostedUserId(), feedItem.getLikeCount());
                });

                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;

                if(!UserUtils.isGuestLoggedIn(context)){

                    if (feedItem.getPostType() != null && feedItem.getPostType().equalsIgnoreCase("newsPost") && feedItem.getCompanyName() != null &&  !feedItem.getCompanyName().equalsIgnoreCase("") && feedItem.getCompanyId() != null &&  !feedItem.getCompanyId().equalsIgnoreCase("")){
                        overflow.setVisibility(View.GONE);
                        btnReport.setVisibility(View.INVISIBLE);
                    } else {
                        if(feedItem.getPostedUserId() != null && feedItem.getPostedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                            final boolean isEditShow;
                            isEditShow = feedItem.getStatus() != null && !feedItem.getStatus().equalsIgnoreCase("");
                            overflow.setVisibility(View.VISIBLE);
                            btnReport.setVisibility(View.INVISIBLE);
                            overflow.setOnClickListener(v -> onFeedItemClickListener.onMoreClick(v, getAdapterPosition(), isEditShow));
                            UserUtils.saveUserProfilePicture(context, feedItem.getProfilePic());
                        } else {
                            overflow.setVisibility(View.GONE);
                            btnReport.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    overflow.setVisibility(View.GONE);
                    btnReport.setVisibility(View.INVISIBLE);
                }


                if(favourite != null) {
                    favourite.setVisibility(View.VISIBLE);
                    favourite.setOnClickListener(v -> {
                        if(UserUtils.isGuestLoggedIn(context)){
                            GuestLoginDialog.show(context);
                            return;
                        }
                        if(favourite.getTag().equals("favourite")){
                            favourite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_border_black_24dp));
                            favourite.setColorFilter(null);
                            favourite.setTag("favourited");
                        } else {
                            favourite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_black_24dp));
                            favourite.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                            favourite.setTag("favourite");
                        }
                        onFeedItemClickListener.onFavouriteClick(v, getAdapterPosition());
                    });
                }
            }
        }


        private void addBottomDots(int currentPage)
        {
            if(myViewPagerAdapter != null && myViewPagerAdapter.mImageList != null) {
                TextView[] dots = new TextView[myViewPagerAdapter.mImageList.size()];
                dotsLayout.removeAllViews();
                for (int i = 0; i < dots.length; i++) {
                    dots[i] = new TextView(context);
                    dots[i].setText(Html.fromHtml("&#8226;"));
                    dots[i].setTextSize(35);
                    dots[i].setTextColor(ContextCompat.getColor(context,R.color.white));
                    dotsLayout.addView(dots[i]);
                }

                if(myViewPagerAdapter.mImageList.size() > 0) {
                    if(myViewPagerAdapter.mImageList.size() > 1) {
                        leftRightLayout.setVisibility(View.VISIBLE);
                        dotsLayout.setVisibility(View.VISIBLE);
                    } else {
                        leftRightLayout.setVisibility(View.GONE);
                        dotsLayout.setVisibility(View.GONE);
                    }
                    if(currentPage == 0) {
                        left.setVisibility(View.GONE);
                        if(myViewPagerAdapter.mImageList.size() > 1){
                            right.setVisibility(View.VISIBLE);
                        } else {
                            right.setVisibility(View.GONE);
                        }
                    }else if (currentPage == myViewPagerAdapter.mImageList.size() - 1) {
                        left.setVisibility(View.VISIBLE);
                        right.setVisibility(View.GONE);
                    } else {
                        left.setVisibility(View.VISIBLE);
                        right.setVisibility(View.VISIBLE);
                    }
                }

                left.setOnClickListener(v -> {
                    int current = viewPager.getCurrentItem() -1;
                    if (current < myViewPagerAdapter.mImageList.size()) {
                        // move to previous screen
                        viewPager.setCurrentItem(current);
                        myViewPagerAdapter.notifyDataSetChanged();
                    }
                });

                right.setOnClickListener(v -> {
                    int current = getItem(+1);
                    if (current < myViewPagerAdapter.mImageList.size()) {
                        // move to next screen
                        viewPager.setCurrentItem(current);
                        myViewPagerAdapter.notifyDataSetChanged();
                    }
                });

                if (dots.length > 0)
                    dots[currentPage].setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
            }

        }

        private int getItem(int i) {
            return viewPager.getCurrentItem() + i;
        }

        public Feed.FeedItem getFeedItem() {
            return feedItem;
        }
    }

    private String convertToDate(String startDay){
        Date date;
        String strDate = null;
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        try {
            date = format.parse(startDay);
            strDate = sDFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    private String convertToDateFormat(String startDay){
        Date date;
        String strDate = null;
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        try {
            date = format.parse(startDay);
            strDate = sDFormatDate.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    private void goToWeb(String publisherUrl) {
        if (context == null)
            return;
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage("com.android.chrome");
        customTabsIntent.launchUrl(context, Uri.parse(publisherUrl));
    }

    private class MyViewPagerAdapter extends PagerAdapter {
        private int itemPosition;
        private List<PictureUrl> mImageList = new ArrayList<>();

        MyViewPagerAdapter() {}

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            //final SimpleDraweeView mPhotoDraweeView = new SimpleDraweeView(container.getContext());
            final ImageView mPhotoDraweeView = new ImageView(container.getContext());
            Uri uri = Uri.parse(mImageList.get(position).getImages());
            /*ImageRequest imgReq = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .setProgressiveRenderingEnabled(true)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(imgReq)
                    .setTapToRetryEnabled(true)
                    .setOldController(mPhotoDraweeView.getController())
                    .build();
            mPhotoDraweeView.setAdjustViewBounds(true);
            mPhotoDraweeView.getHierarchy().setFadeDuration(0);
            mPhotoDraweeView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FOCUS_CROP);
            mPhotoDraweeView.getHierarchy().setProgressBarImage(R.drawable.progress_circular, ScalingUtils.ScaleType.CENTER);
            mPhotoDraweeView.setController(controller);*/

            Picasso.with(context).load(uri).fit().noFade().centerCrop()
                    .placeholder(R.color.fb_view_bg)
                    .into(mPhotoDraweeView);


            try {
                container.addView(mPhotoDraweeView, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mPhotoDraweeView.setOnClickListener(v -> {
                if(feedItems != null && feedItems.size() > 0) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("images", (Serializable) feedItems.get(itemPosition).getPictureUrl());
                    FragmentTransaction ft = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
                    FeedImagesSlideshowDialogFragment newFragment = FeedImagesSlideshowDialogFragment.newInstance();
                    newFragment.setArguments(bundle);
                    newFragment.show(ft, "slideshow");
                }
            });

            mPhotoDraweeView.setOnLongClickListener(v -> {
                if(feedItems != null && feedItems.size() > 0) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("images", (Serializable) feedItems.get(itemPosition).getPictureUrl());
                    FragmentTransaction ft = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
                    FeedImagesSlideshowDialogFragment newFragment = FeedImagesSlideshowDialogFragment.newInstance();
                    newFragment.setArguments(bundle);
                    newFragment.show(ft, "slideshow");
                }
                return false;
            });
            return mPhotoDraweeView;
        }

        @Override
        public int getCount() {
            if(mImageList != null)
                return mImageList.size();
            else
                return  0;
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

        public void swap(List<PictureUrl>mImageLists, int adapterPosition) {
            mImageList = mImageLists;
            itemPosition = adapterPosition;
            this.notifyDataSetChanged();
        }
    }

    private void showError(String errorMessage) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void goToAuthorProfile(String editorId) {
        final Intent intent = new Intent(context, UserFriendProfileActivity.class);
        intent.putExtra("friendId", editorId);
        context.startActivity(intent);
        if (CheckOsVersion.isPreLollipop()) {
            ((Activity)context).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void sendInterest(int interest, String eventId, ImageView interestIv) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(AppController.getInstance());

            apiService.eventInterest(userId, eventId, interest, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<EventInterest>() {
                        @Override
                        public void onCompleted() {
                            Log.d("mEventInterest", "onCompleted: ");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("mEventInterest", "onError: ");
                        }

                        @Override
                        public void onNext(EventInterest mEventInterest) {
                            Log.d("mEventInterest", "onNext: ");
                        }
                    });
        } else {
            if(interestIv != null)
                SnackBarDialog.showNoInternetError(interestIv);
        }
    }

    private void screenMoveToLikeActivity(String postId, String postedUserId, int likeCount) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        Intent mIntent = new Intent(context, LikeActivity.class);
        mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        mIntent.putExtra("postedUserId", postedUserId);
        mIntent.putExtra("postId", postId);
        mIntent.putExtra("count", likeCount);
        mIntent.putExtra("apiKey", UserUtils.getApiKey(context));
        context.startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop()) {
            ((Activity)context).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    public  class LoadingCellFeedViewHolder extends CellFeedViewHolder {

        LoadingFeedItemView loadingFeedItemView;

        public LoadingCellFeedViewHolder(LoadingFeedItemView view) {
            super(view);
            this.loadingFeedItemView = view;
        }

        @Override
        public void bindView(Feed.FeedItem feedItem) {
            super.bindView(feedItem);
        }
    }

    public interface OnFeedItemClickListener {
        void onCommentsClick(View v, int position);

        void onImageCenterClick(View v, int position);

        void onEventClick(View v, int position);

        void onHeartClick(View v, int position);

        void onLoadMore(int position);

        void onReport(View v, ViewGroup inActiveLayout, int position);

        void onInActiveReport(View v, int position);

        void onMoreClick(View v, int position, boolean isEditShow);

        void onProfileClick(View v, int position);

        void onTagClick(View v, String taggedId);

        void onPostFromClick(View v, int position);

        void onPostToClick(View v, int position);

        void onFavouriteClick(View v, int position);
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
            ds.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        }


        @Override
        public void onClick(View view) {
            Selection.setSelection((Spannable) ((TextView)view).getText(), 0);
            if (onFeedItemClickListener != null && taggedId != null && !taggedId.equalsIgnoreCase("") && position != -1) {
                onFeedItemClickListener.onTagClick(view, taggedId);
            }
        }
    }
}
