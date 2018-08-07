package com.myscrap.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.CommentActivity;
import com.myscrap.CompanyImagesSlideshowDialogFragment;
import com.myscrap.LikeActivity;
import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.model.CompanyProfile;
import com.myscrap.model.Feed;
import com.myscrap.model.LikedData;
import com.myscrap.model.PictureUrl;
import com.myscrap.model.UserFriendProfile;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.SquaredFrameLayout;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by ms3 on 7/6/2017.
 */

public class LinearLayoutAdapter extends CustomRecyclerViewAdapter {
    private Activity activity;
    private List<PictureUrl> images;
    private CompanyProfile.CompanyData mCompanyProfileData;
    private UserFriendProfile.UserFriendProfileData mUserFriendProfileData;
    private LinearLayoutAdapter mLinearLayoutAdapter;
    private LinearLayoutAdapter.OnFeedItemClickListener mListener;

    public LinearLayoutAdapter(Activity activity, List<PictureUrl> images, UserFriendProfile.UserFriendProfileData userFriendProfileData, CompanyProfile.CompanyData companyProfileData, OnFeedItemClickListener listener) {
        this.activity = activity;
        this.images = images;
        this.mListener = listener;
        this.mLinearLayoutAdapter = this;
        this.mCompanyProfileData = companyProfileData;
        this.mUserFriendProfileData = userFriendProfileData;

    }

    @Override
    public CustomRecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.linear_layout_images, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final CustomRecycleViewHolder holder, int position) {
        final Holder myHolder = (Holder) holder;
        final PictureUrl mPictureUrl = images.get(position);
        final String userId = AppController.getInstance().getPrefManager().getUser().getId();


        Uri uri = Uri.parse(mPictureUrl.getImages());
        myHolder.images.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FOCUS_CROP);
        myHolder.images.setImageURI(uri);

        myHolder.images.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("images", (Serializable) images);
            bundle.putInt("position", myHolder.getAdapterPosition());
            FragmentTransaction ft = ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction();
            CompanyImagesSlideshowDialogFragment newFragment = CompanyImagesSlideshowDialogFragment.newInstance();
            newFragment.setArguments(bundle);
            newFragment.show(ft, "slideshow");
        });


        if (mCompanyProfileData != null){
            if(mCompanyProfileData.getCompanyImage() != null){

                Uri mUri = Uri.parse(mCompanyProfileData.getCompanyImage());
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                myHolder.companyProfile.setHierarchy(new GenericDraweeHierarchyBuilder(activity.getResources())
                        .setRoundingParams(roundingParams)
                        .build());
                roundingParams.setRoundAsCircle(true);
                myHolder.companyProfile.setImageURI(mUri);
                myHolder.companyProfile.setColorFilter(null);
            }


            if(mCompanyProfileData.getCompanyName() != null){
                myHolder.companyName.setText(mCompanyProfileData.getCompanyName());
            }

            if(mCompanyProfileData.getCompanyType() != null){
                if(!mCompanyProfileData.getCompanyType().equalsIgnoreCase("")){
                    myHolder.companyType.setText(mCompanyProfileData.getCompanyType());
                } else {
                    myHolder.companyType.setText("Recycling");
                }
            }
            myHolder.companyProfileText.setVisibility(View.GONE);
        }

        if (mUserFriendProfileData != null){
            if (mUserFriendProfileData.getProfilePic() != null && !mUserFriendProfileData.getProfilePic().equalsIgnoreCase("")){
                if(mUserFriendProfileData.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        ||mUserFriendProfileData.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    myHolder.companyProfile.setImageResource(R.drawable.bg_circle);

                    if(mUserFriendProfileData.getColorCode() != null && !mUserFriendProfileData.getColorCode().equalsIgnoreCase("") && mUserFriendProfileData.getColorCode().startsWith("#")){
                        myHolder.companyProfile.setColorFilter(Color.parseColor(mUserFriendProfileData.getColorCode()));
                    } else {
                        myHolder.companyProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(activity, "400"));
                    }

                    String[] split = mUserFriendProfileData.getName().split("\\s+");
                    myHolder.companyProfileText.setVisibility(View.VISIBLE);
                    if (mUserFriendProfileData.getName() != null && !mUserFriendProfileData.getName().trim().equalsIgnoreCase("")){
                        if (split.length > 1){
                            String first = split[0].trim().substring(0,1);
                            String last = split[1].trim().substring(0,1);
                            String initial = first + ""+ last ;
                            myHolder.companyProfileText.setText(initial.toUpperCase().trim());
                        } else {
                            if (split[0] != null && split[0].trim().length() >= 1) {
                                String first = split[0].trim().substring(0, 1);
                                myHolder.companyProfileText.setText(first.toUpperCase().trim());
                            }
                        }
                    }
                } else {
                    if( activity!= null && !mUserFriendProfileData.getProfilePic().equalsIgnoreCase("")){
                        Uri mCompanyProfile = Uri.parse(mUserFriendProfileData.getProfilePic());
                        RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                        myHolder.companyProfile.setHierarchy(new GenericDraweeHierarchyBuilder(activity.getResources())
                                .setRoundingParams(roundingParams)
                                .build());
                        roundingParams.setRoundAsCircle(true);
                        myHolder.companyProfile.setImageURI(mCompanyProfile);
                    }
                    myHolder.companyProfile.setColorFilter(null);
                    myHolder.companyProfileText.setVisibility(View.GONE);
                }
            }


            if(mUserFriendProfileData.getName() != null){
                myHolder.companyName.setText(mUserFriendProfileData.getName());
            }

            if(mUserFriendProfileData.getPostedUserDesignation() != null){
                if(!mUserFriendProfileData.getPostedUserDesignation().equalsIgnoreCase("")){
                    myHolder.companyType.setText(mUserFriendProfileData.getPostedUserDesignation());
                } else {
                    myHolder.companyType.setText("Trader");
                }
            }
        }


        if (mPictureUrl.getTimeStamp() != null && !mPictureUrl.getTimeStamp().equalsIgnoreCase("")
                && !mPictureUrl.getTimeStamp().equalsIgnoreCase("true")
                && !mPictureUrl.getTimeStamp().equalsIgnoreCase("false")) {
            String timeAGO = UserUtils.getTimeAgo(Long.parseLong(UserUtils.parsingLong(mPictureUrl.getTimeStamp())));
            myHolder.timeStamp.setText(timeAGO);
            myHolder.timeStamp.setVisibility(View.VISIBLE);
        } else {
            myHolder.timeStamp.setVisibility(View.GONE);
        }



        if (mPictureUrl.getStatus() != null && !mPictureUrl.getStatus().equalsIgnoreCase("")){
            if (mPictureUrl.getStatus().length() > 350) {
                final String statusString = mPictureUrl.getStatus().trim();
                String splitString = mPictureUrl.getStatus().substring(0, 350);
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
                        if(mPictureUrl.getTagList() != null){
                            for (Feed.FeedItem.TagList mData : mPictureUrl.getTagList()){
                                Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                                Matcher matcher = pattern.matcher(mPictureUrl.getStatus());
                                while (matcher.find()) {
                                    sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), myHolder.getAdapterPosition()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                        }
                        myHolder.status.setText(sBuilder);
                        Linkify.addLinks(myHolder.status, Linkify.WEB_URLS);
                        AppController.getInstance().stripUnderlines(myHolder.status);
                        myHolder.status.setMovementMethod(LinkMovementMethod.getInstance());
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


                if(mPictureUrl.getTagList() != null){
                    for (Feed.FeedItem.TagList mData : mPictureUrl.getTagList()){
                        Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                        Matcher matcher = pattern.matcher(mPictureUrl.getStatus());
                        while (matcher.find()) {
                            if(matcher.start() < 370 && matcher.end() < 370){
                                sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), myHolder.getAdapterPosition()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }
                }
                myHolder.status.setText(sBuilder);
                Linkify.addLinks(myHolder.status, Linkify.WEB_URLS);
                AppController.getInstance().stripUnderlines(myHolder.status);
                myHolder.status.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                SpannableStringBuilder sBuilder = new SpannableStringBuilder(mPictureUrl.getStatus());
                if(mPictureUrl.getTagList() != null) {
                    for (Feed.FeedItem.TagList mData : mPictureUrl.getTagList()) {
                        Pattern pattern = Pattern.compile(mData.getTaggedUserName());
                        Matcher matcher = pattern.matcher(mPictureUrl.getStatus());
                        while (matcher.find()) {
                            sBuilder.setSpan(new InternalClickableSpan(mData.getTaggedUserName(), mData.getTaggedId(), myHolder.getAdapterPosition()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                myHolder.status.setText(sBuilder);
                Linkify.addLinks(myHolder.status, Linkify.WEB_URLS);
                AppController.getInstance().stripUnderlines(myHolder.status);
                myHolder.status.setMovementMethod(LinkMovementMethod.getInstance());
            }
            myHolder.status.setVisibility(View.VISIBLE);
        } else {
            myHolder.status.setVisibility(View.GONE);
        }


        myHolder.btnLike.setImageResource(mPictureUrl.isLikeStatus() ? R.drawable.ic_heart_outline_black_filled : R.drawable.ic_heart_outline_black);

        if(myHolder.tsCommentsCounter != null){
            if(mPictureUrl.getCommentCount()==0) {
                myHolder.tsCommentsCounter.setCurrentText(activity.getString(R.string.feed_comment));
            } else {
                myHolder.tsCommentsCounter.setCurrentText(activity.getResources().getQuantityString(
                        R.plurals.comments_count, mPictureUrl.getCommentCount(), mPictureUrl.getCommentCount()
                ));
            }
        }

        if(myHolder.tsLikesCounter != null){
            if(mPictureUrl.getLikeCount()==0) {
                myHolder.tsLikesCounter.setCurrentText(activity.getString(R.string.feed_like));
            } else {
                myHolder.tsLikesCounter.setCurrentText(activity.getResources().getQuantityString(
                        R.plurals.likes_count, mPictureUrl.getLikeCount(), mPictureUrl.getLikeCount()
                ));
            }
        }

        if(mPictureUrl.getLikeCount() > 0 && mPictureUrl.getCommentCount() > 0){
            myHolder.tsLikesCounter.setVisibility(View.VISIBLE);
            myHolder.tsCommentsCounter.setVisibility(View.VISIBLE);
            myHolder.dot.setVisibility(View.VISIBLE);
        } else if (mPictureUrl.getLikeCount() > 0 && mPictureUrl.getCommentCount() ==  0){
            myHolder.tsLikesCounter.setVisibility(View.VISIBLE);
            myHolder.tsCommentsCounter.setVisibility(View.GONE);
            myHolder.dot.setVisibility(View.GONE);
        } else if (mPictureUrl.getLikeCount() == 0 && mPictureUrl.getCommentCount() >  0){
            myHolder.tsLikesCounter.setVisibility(View.GONE);
            myHolder.tsCommentsCounter.setVisibility(View.VISIBLE);
            myHolder.dot.setVisibility(View.GONE);
        } else {
            myHolder.tsLikesCounter.setVisibility(View.GONE);
            myHolder.tsCommentsCounter.setVisibility(View.GONE);
            myHolder.dot.setVisibility(View.GONE);
        }


        myHolder.overflow.setVisibility(View.GONE);

        myHolder.btnReport.setVisibility(View.INVISIBLE);

        myHolder.overflow.setOnClickListener(v -> mListener.onMoreClick(v, myHolder.getAdapterPosition()));

        myHolder.btnReport.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(activity)){
                GuestLoginDialog.show(activity);
                return;
            }
            mListener.onReport(v, myHolder.getAdapterPosition());
        });

        myHolder.btnReportBottom.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(activity)){
                GuestLoginDialog.show(activity);
                return;
            }
            mListener.onInActiveReport(v, myHolder.getAdapterPosition());
        });


        if(mPictureUrl.isReported()){
            if(mPictureUrl.getReportedUserId() != null && mPictureUrl.getUserId() != null){
                if(mPictureUrl.getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    myHolder.inActiveEntireLayout.setVisibility(View.VISIBLE);
                    myHolder.inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                    myHolder.btnReportBottom.setVisibility(View.VISIBLE);
                } else if(mPictureUrl.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    myHolder.inActiveEntireLayout.setVisibility(View.VISIBLE);
                    myHolder.inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                    myHolder.btnReportBottom.setVisibility(View.VISIBLE);
                } else {
                    myHolder.inActiveEntireLayout.setVisibility(View.VISIBLE);
                    myHolder.inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                    myHolder.btnReportBottom.setVisibility(View.GONE);
                }
            }
        } else {
            myHolder.inActiveEntireLayout.setVisibility(View.GONE);
        }


        myHolder.tsLikesCounter.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(activity)){
                GuestLoginDialog.show(activity);
                return;
            }
            if(mPictureUrl.getLikeCount() > 0)
                if(CheckNetworkConnection.isConnectionAvailable(activity))
                    screenMoveToLikeActivity(mPictureUrl.getPostid(), userId, mPictureUrl.getLikeCount());
                else
                    SnackBarDialog.showNoInternetError(myHolder.tsLikesCounter);
        });

        myHolder.likeText.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(activity)){
                GuestLoginDialog.show(activity);
                return;
            }
            if(mPictureUrl.getLikeCount() > 0)
                if(CheckNetworkConnection.isConnectionAvailable(activity))
                    screenMoveToLikeActivity(mPictureUrl.getPostid(), userId, mPictureUrl.getLikeCount());
                else
                    SnackBarDialog.showNoInternetError(myHolder.tsLikesCounter);
        });

        myHolder.tsCommentsCounter.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(activity))
                screenMoveToCommentActivity(mPictureUrl.getPostid(), userId);
            else
                SnackBarDialog.showNoInternetError(myHolder.tsCommentsCounter);
        });

        myHolder.comment.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(activity))
                screenMoveToCommentActivity(mPictureUrl.getPostid(), userId);
            else
                SnackBarDialog.showNoInternetError(myHolder.tsCommentsCounter);
        });


        myHolder.btnLike.setOnClickListener(v -> {
            if (images != null && images.size() > 0) {
                if (!mPictureUrl.isLikeStatus()) {
                    myHolder.btnLike.setImageResource(R.drawable.ic_heart_outline_black_filled);
                    mPictureUrl.setLikeStatus(true);

                    int count = mPictureUrl.getLikeCount() + 1;
                    mPictureUrl.setLikeCount(count);

                    if (mPictureUrl.getLikeCount() != 0 && mPictureUrl.getLikeCount() > 0 ) {
                        if (mPictureUrl.getLikeCount() == 1) {
                            myHolder.tsLikesCounter.setText(mPictureUrl.getLikeCount() +" Like" );
                            myHolder.likeText.setText(" LIKE");
                        } else if (mPictureUrl.getLikeCount() > 1) {
                            myHolder.tsLikesCounter.setText(mPictureUrl.getLikeCount() +" Likes" );
                            myHolder.likeText.setText(" LIKE");
                        }
                        myHolder.tsLikesCounter.setVisibility(View.VISIBLE);
                    } else {
                        myHolder.tsLikesCounter.setVisibility(View.GONE);
                        myHolder.likeText.setText(" LIKE");
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(activity))
                        doLike(mPictureUrl);
                    else
                        SnackBarDialog.showNoInternetError(myHolder.btnLike);
                } else {
                    mPictureUrl.setLikeStatus(false);
                    myHolder.btnLike.setImageResource(R.drawable.ic_heart_outline_black);
                    int count = mPictureUrl.getLikeCount() - 1;
                    mPictureUrl.setLikeCount(count);
                    if (mPictureUrl.getLikeCount() != 0 && mPictureUrl.getLikeCount() > 0 ) {
                        if (mPictureUrl.getLikeCount() == 1) {
                            myHolder.tsLikesCounter.setText(mPictureUrl.getLikeCount() + " Like");
                            myHolder.likeText.setText(" LIKE");
                        } else if (mPictureUrl.getLikeCount() > 1) {
                            myHolder.tsLikesCounter.setText(mPictureUrl.getLikeCount() + " Likes");
                            myHolder.likeText.setText(" LIKE");
                        }
                        myHolder.tsLikesCounter.setVisibility(View.VISIBLE);
                    } else {
                        myHolder.tsLikesCounter.setVisibility(View.GONE);
                        myHolder.likeText.setText(" LIKE");
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(activity))
                        doLike(mPictureUrl);
                    else
                        SnackBarDialog.showNoInternetError(myHolder.btnLike);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    private void screenMoveToLikeActivity(String postId, String postedUserId, int likeCount) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        Intent mIntent = new Intent(activity, LikeActivity.class);
        mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        mIntent.putExtra("postedUserId", postedUserId);
        mIntent.putExtra("postId", postId);
        mIntent.putExtra("count", likeCount);
        mIntent.putExtra("apiKey", UserUtils.getApiKey(activity));
        activity.startActivity(mIntent);
    }

    private void screenMoveToCommentActivity(String postId, String postedUserId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        final Intent intent = new Intent(activity, CommentActivity.class);
        intent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        intent.putExtra("postId", postId);
        intent.putExtra("postedUserId", postedUserId);
        intent.putExtra("apiKey", UserUtils.getApiKey(activity));
        activity.startActivity(intent);

    }


    private void doLike(PictureUrl pictureUrl) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<LikedData> call = apiService.insertLike(userId, pictureUrl.getPostid(), userId,apiKey);
        call.enqueue(new Callback<LikedData>() {
            @Override
            public void onResponse(@NonNull Call<LikedData> call, @NonNull retrofit2.Response<LikedData> response) {
                Log.d("doLike", "onSuccess");
                if(response.body() != null && response.isSuccessful()){
                    LikedData mLikedData = response.body();
                    if(mLikedData != null && !mLikedData.getError()){
                        LikedData.InsertLikeData  data = mLikedData.getInsertLikeData();
                        if(data != null) {
                            if(images != null) {
                                int i = 0;
                                for(PictureUrl feedItem : images){
                                    if(feedItem.getPostid().equalsIgnoreCase(data.getPostId())){
                                        feedItem.setLikeStatus(data.getLikeStatus());
                                        feedItem.setLikeCount(Integer.parseInt(UserUtils.parsingInteger(data.getLikeCount())));
                                        images.set(i, feedItem);
                                        if(mLinearLayoutAdapter != null){
                                            mLinearLayoutAdapter.notifyDataSetChanged();
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

    public class Holder extends CustomRecycleViewHolder {
        private SimpleDraweeView images, companyProfile;
        private ImageView  overflow, btnReport, btnReportBottom;
        private TextView companyName, companyProfileText, companyType, top, dot, timeStamp, status;
        private TextSwitcher tsLikesCounter, tsCommentsCounter;
        private ImageButton btnLike, btnComments;
        private TextView likeText,comment;
        private SquaredFrameLayout imageLayout;
        private RelativeLayout inActiveEntireLayout;

        public Holder(View itemView) {
            super(itemView);
            images = (SimpleDraweeView) itemView.findViewById(R.id.ivItemGridImage);
            companyProfile = (SimpleDraweeView) itemView.findViewById(R.id.icon_profile);
            companyProfileText = (TextView) itemView.findViewById(R.id.icon_text);
            overflow = (ImageView) itemView.findViewById(R.id.overflow);
            companyName = (TextView) itemView.findViewById(R.id.profileName);
            companyType = (TextView) itemView.findViewById(R.id.designation);
            timeStamp = (TextView) itemView.findViewById(R.id.time);
            status = (TextView) itemView.findViewById(R.id.status);
            top = (TextView) itemView.findViewById(R.id.top);
            dot = (TextView) itemView.findViewById(R.id.dot);
            imageLayout = (SquaredFrameLayout) itemView.findViewById(R.id.vImageRoot);
            tsLikesCounter = (TextSwitcher) itemView.findViewById(R.id.tsLikesCounter);
            tsCommentsCounter = (TextSwitcher) itemView.findViewById(R.id.tsCommentsCounter);
            btnLike = (ImageButton) itemView.findViewById(R.id.btnLike);
            btnComments = (ImageButton) itemView.findViewById(R.id.btnComments);
            btnReport = (ImageView) itemView.findViewById(R.id.ic_report);
            btnReportBottom = (ImageView) itemView.findViewById(R.id.ic_report_bottom);
            likeText = (TextView) itemView.findViewById(R.id.like_text);
            comment = (TextView) itemView.findViewById(R.id.comment);
            inActiveEntireLayout = (RelativeLayout) itemView.findViewById(R.id.overall_active_layout);
        }
    }

    public interface OnFeedItemClickListener {
        void onReport(View v, int position);

        void onInActiveReport(View v, int position);

        void onMoreClick(View v, int adapterPosition);

        void onTagClick(View view, String taggedId);
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
            ds.setColor(ContextCompat.getColor(AppController.getInstance(), R.color.colorPrimaryDark));
        }


        @Override
        public void onClick(View view) {
            Selection.setSelection((Spannable) ((TextView)view).getText(), 0);
            if (mListener != null && taggedId != null && !taggedId.equalsIgnoreCase("") && position != -1) {
                mListener.onTagClick(view, taggedId);
            }
        }
    }
}
