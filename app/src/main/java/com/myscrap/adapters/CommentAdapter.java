package com.myscrap.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.UserProfileActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.Comment;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.UserUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ms3 on 5/16/2017.
 */

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<Comment.CommentData> mCommentList = new ArrayList<>();
    private CommentAdapterClickListener mCommentAdapterClickListener;

    public CommentAdapter(Context context, List<Comment.CommentData> likeList, CommentAdapterClickListener commentAdapterClickListener){
        this.mContext = context;
        this.mCommentList = likeList;
        this.mCommentAdapterClickListener = commentAdapterClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Comment.CommentData mCommentData = mCommentList.get(position);
        if(holder instanceof CommentViewHolder){
             final CommentViewHolder commentHolderView = ((CommentViewHolder) holder);
            if(mCommentData != null){
                commentHolderView.profileName.setText(mCommentData.getName());

                String userName = mCommentData.getName();
                if (!userName.equalsIgnoreCase("")){
                    String[] split = userName.split("\\s+");
                    if (split.length > 1){
                        String first = split[0].substring(0,1);
                        String last = split[1].substring(0,1);
                        String initial = first + ""+ last ;
                        commentHolderView.iconText.setText(initial.toUpperCase());
                    } else {
                        if (split[0] != null && split[0].trim().length() >= 1) {
                            String first = split[0].substring(0, 1);
                            commentHolderView.iconText.setText(first.toUpperCase());
                        }
                    }
                }

                if(mCommentData.getComment() != null && !mCommentData.getComment().equalsIgnoreCase("")){
                    if(mCommentData.getComment().length() > 150){
                        final String statusString = mCommentData.getComment();
                        String splitString = mCommentData.getComment().substring(0, 150);
                        final String continueReading = "...Continue Reading";
                        String mergedString = splitString + continueReading;
                        ClickableSpan nameClickableSpan = new ClickableSpan() {

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    ds.setColor(mContext.getResources().getColor(R.color.black, mContext.getTheme()));
                                } else {
                                    ds.setColor(mContext.getResources().getColor(R.color.black));
                                }
                            }

                            @Override
                            public void onClick(View view) {
                                commentHolderView.comment.setText(statusString);
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
                        commentHolderView.comment.setText(sBuilder);
                        commentHolderView.comment.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    else
                    {
                        commentHolderView.comment.setText(mCommentData.getComment());
                    }
                }

                if(mCommentData.getTimeStamp() != null && !mCommentData.getTimeStamp().equalsIgnoreCase("")){
                    long time = Long.parseLong(UserUtils.parsingLong(mCommentData.getTimeStamp()));
                    if (time < 1000000000000L)
                    {
                        time *= 1000;
                    }
                    commentHolderView.timeStamp.setText(getChatRoomTime(time));
                    commentHolderView.timeStamp.setVisibility(View.VISIBLE);
                } else {
                    commentHolderView.timeStamp.setVisibility(View.GONE);
                }
                if(mCommentData.getProfilePic() != null){
                    String profilePicture = mCommentData.getProfilePic();
                    if (!profilePicture.equalsIgnoreCase("")){
                        if(profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            commentHolderView.profile.setImageResource(R.drawable.bg_circle);
                            if(mCommentData.getColorCode() != null && !mCommentData.getColorCode().equalsIgnoreCase("") && mCommentData.getColorCode().startsWith("#")){
                                commentHolderView.profile.setColorFilter(Color.parseColor(mCommentData.getColorCode()));
                            } else {
                                commentHolderView.profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                            }

                            commentHolderView.iconText.setVisibility(View.VISIBLE);
                        } else  {
                            Uri uri = Uri.parse(profilePicture);
                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                            commentHolderView.profile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                    .setRoundingParams(roundingParams)
                                    .build());
                            roundingParams.setRoundAsCircle(true);
                            commentHolderView.profile.setImageURI(uri);
                            commentHolderView.profile.setColorFilter(null);
                            commentHolderView.iconText.setVisibility(View.GONE);
                        }
                    } else {
                        commentHolderView.profile.setImageResource(R.drawable.bg_circle);
                        commentHolderView.profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                        commentHolderView.iconText.setVisibility(View.VISIBLE);
                    }
                }
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;

                if(mCommentData.getUserId()!= null && mCommentData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    commentHolderView.overFlow.setVisibility(View.VISIBLE);
                } else {
                    commentHolderView.overFlow.setVisibility(View.GONE);
                }

                commentHolderView.overFlow.setOnClickListener(v -> {
                    if(mCommentAdapterClickListener != null)
                        mCommentAdapterClickListener.onOverFlowClickListener(v, holder.getAdapterPosition());
                });

                commentHolderView.profile.setOnClickListener(v -> {
                    if(mCommentData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(mCommentData.getUserId());
                    }
                });

                commentHolderView.profileName.setOnClickListener(v -> {
                    if(mCommentData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(mCommentData.getUserId());
                    }
                });

                holder.itemView.setOnClickListener(v -> {
                    if(mCommentData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        goToUserProfile();
                    } else {
                        goToUserFriendProfile(mCommentData.getUserId());
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }


    private void goToUserProfile() {
        Intent i = new Intent(mContext, UserProfileActivity.class);
        mContext.startActivity(i);
        if (CheckOsVersion.isPreLollipop()) {
            if(mContext != null)
            ((Activity)mContext).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void goToUserFriendProfile(String postedUserId) {
        final Intent intent = new Intent(mContext, UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        mContext.startActivity(intent);
        if(CheckOsVersion.isPreLollipop())
            if(mContext != null)
                ((Activity)mContext).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private static String getChatRoomTime(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        SimpleDateFormat mSimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        mSimpleDateFormat.setTimeZone(tz);
        String dateString = mSimpleDateFormat.format(time);
        return getChatTimeStamp(dateString);
    }

    private static String getChatTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = "";
        Calendar calendar = Calendar.getInstance();
        String today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        today = today.length() < 2 ? "0" + today : today;
        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("dd MMM", Locale.getDefault());
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    private class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView overFlow;
        private SimpleDraweeView profile;
        private TextView iconText, profileName, timeStamp, comment;
        public CommentViewHolder(View view) {
            super(view);
            profile = (SimpleDraweeView)view.findViewById(R.id.icon_profile);
            iconText = (TextView)view.findViewById(R.id.icon_text);
            overFlow = (ImageView)view.findViewById(R.id.overflow);
            profileName = (TextView)view.findViewById(R.id.name);
            timeStamp = (TextView)view.findViewById(R.id.timeStamp);
            comment = (TextView)view.findViewById(R.id.comment);
        }
    }

    public interface CommentAdapterClickListener {
        void onOverFlowClickListener(View v, int position);
    }


}
