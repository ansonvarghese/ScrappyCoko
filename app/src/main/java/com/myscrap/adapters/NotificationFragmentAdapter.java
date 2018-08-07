package com.myscrap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.model.Notification;
import com.myscrap.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/19/2017.
 */

public class NotificationFragmentAdapter extends RecyclerView.Adapter<NotificationFragmentAdapter.ItemViewHolder>{

    private Context mContext;
    private List<Notification.NotificationData> mNotificationList = new ArrayList<>();


    public NotificationFragmentAdapter(Context context, List<Notification.NotificationData> notificationList){
        this.mContext = context;
        this.mNotificationList = notificationList;
    }

    @Override
    public NotificationFragmentAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_row, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NotificationFragmentAdapter.ItemViewHolder itemViewHolder, int position)
    {
            final Notification.NotificationData notificationItem = mNotificationList.get(position);
                if(notificationItem != null)
                {
                    String profilePicture = notificationItem.getProfilePic();
                    if (!profilePicture.equalsIgnoreCase("")){
                        if(profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            itemViewHolder.mUserProfilePicture.setImageResource(R.drawable.bg_circle);
                            if(notificationItem.getColorCode() != null && !notificationItem.getColorCode().equalsIgnoreCase("") && notificationItem.getColorCode().startsWith("#")){
                                itemViewHolder.mUserProfilePicture.setColorFilter(Color.parseColor(notificationItem.getColorCode()));
                            } else {
                                itemViewHolder.mUserProfilePicture.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                            }

                            itemViewHolder.iconText.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            Uri uri = Uri.parse(profilePicture);
                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                            itemViewHolder.mUserProfilePicture.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                    .setRoundingParams(roundingParams)
                                    .build());
                            roundingParams.setRoundAsCircle(true);
                            itemViewHolder.mUserProfilePicture.setImageURI(uri);
                            itemViewHolder.mUserProfilePicture.setColorFilter(null);
                            itemViewHolder.iconText.setVisibility(View.GONE);
                        }
                    }
                    else
                    {
                        itemViewHolder.mUserProfilePicture.setImageResource(R.drawable.bg_circle);
                        itemViewHolder.mUserProfilePicture.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                        itemViewHolder.iconText.setVisibility(View.VISIBLE);
                    }


                    if (notificationItem.isTitle())
                    {
                        itemViewHolder.title.setText(notificationItem.isNew() ? "New" : "Earlier");
                        itemViewHolder.title.setVisibility(View.VISIBLE);
                    } else {
                        itemViewHolder.title.setVisibility(View.GONE);
                    }

                    String userName = notificationItem.getPostUserName();
                    if (!userName.equalsIgnoreCase(""))
                    {
                        String[] split = userName.split("\\s+");
                        if (split.length > 1){
                            String first = split[0].substring(0,1);
                            String last = split[1].substring(0,1);
                            String initial = first + ""+ last ;
                            itemViewHolder.iconText.setText(initial.toUpperCase());
                        } else {
                            if (split[0] != null && split[0].trim().length() >= 1) {
                                String first = split[0].substring(0,1);
                                itemViewHolder.iconText.setText(first.toUpperCase());
                            }

                        }
                    }

                    itemViewHolder.mNotificationUserName.setText(notificationItem.getPostUserName());
                    if (notificationItem.getNotificationTime() != null && !notificationItem.getNotificationTime().equalsIgnoreCase(""))
                    {
                        if (!notificationItem.getNotificationTime().equalsIgnoreCase("true") && !notificationItem.getNotificationTime().equalsIgnoreCase("false")) {
                            String time = notificationItem.getNotificationTime();
                            itemViewHolder.mNotificationTime.setText(time);
                        }
                    }
                    if (notificationItem.getNotificationMessage() != null)
                    {
                        if (notificationItem.getNotificationMessage().contains("likes")||notificationItem.getNotificationMessage().equalsIgnoreCase("likes your post.") || notificationItem.getNotificationMessage().equalsIgnoreCase("likes your Comment.") || notificationItem.getNotificationMessage().equalsIgnoreCase("liked your company.")) {
                            itemViewHolder.mNotificationType.setBackgroundResource(R.drawable.ic_notification_like);
                            itemViewHolder.mNotificationType.setVisibility(View.VISIBLE);
                        } else if (notificationItem.getNotificationMessage().contains("invited")) {
                            itemViewHolder.mNotificationType.setBackgroundResource(R.drawable.ic_notification_event);
                            itemViewHolder.mNotificationType.setVisibility(View.VISIBLE);
                        } else if (notificationItem.getNotificationMessage().equalsIgnoreCase("viewed your profile.")) {
                            itemViewHolder.mNotificationType.setBackgroundResource(R.drawable.ic_notification_viewed);
                            itemViewHolder.mNotificationType.setVisibility(View.VISIBLE);
                        } else if (notificationItem.getNotificationMessage().equalsIgnoreCase("commented on your post.")) {
                            itemViewHolder.mNotificationType.setBackgroundResource(R.drawable.ic_notification_comment);
                            itemViewHolder.mNotificationType.setVisibility(View.VISIBLE);
                        } else if (notificationItem.getNotificationMessage().equalsIgnoreCase("accepted your friend request.")) {
                            itemViewHolder.mNotificationType.setBackgroundResource(R.drawable.ic_notification_accept);
                            itemViewHolder.mNotificationType.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            itemViewHolder.mNotificationType.setVisibility(View.GONE);
                        }
                    }
                    final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);


                    if(notificationItem.getActive().equalsIgnoreCase("unseen"))
                    {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml("<font color=\"#000000\">"+notificationItem.getPostUserName().trim() + " "  + "</font>"  + notificationItem.getNotificationMessage()  , Html.FROM_HTML_MODE_LEGACY));
                            spanned.setSpan(bss, spanned.toString().indexOf(notificationItem.getPostUserName().trim()), spanned.toString().indexOf(notificationItem.getPostUserName().trim()) + String.valueOf(notificationItem.getPostUserName().trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            itemViewHolder.mNotificationUserName.setText(spanned);
                        }
                        else
                        {
                            final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml("<font color=\"#000000\">"+notificationItem.getPostUserName().trim() +  " " + "</font>" +notificationItem.getNotificationMessage()));
                            spanned.setSpan(bss, spanned.toString().indexOf(notificationItem.getPostUserName().trim()), spanned.toString().indexOf(notificationItem.getPostUserName().trim()) + String.valueOf(notificationItem.getPostUserName().trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            itemViewHolder.mNotificationUserName.setText(spanned);
                        }
                        itemViewHolder.mNotificationUserName.setMovementMethod(LinkMovementMethod.getInstance());
                        itemViewHolder.mLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.fb_background));
                    }
                    else
                    {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml("<font color=\"#000000\">"+notificationItem.getPostUserName().trim() + " "  + "</font>"  + notificationItem.getNotificationMessage()  , Html.FROM_HTML_MODE_LEGACY));
                            itemViewHolder.mNotificationUserName.setText(spanned);
                        }
                        else
                        {
                            final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml("<font color=\"#000000\">"+notificationItem.getPostUserName().trim() +  " " + "</font>" +notificationItem.getNotificationMessage()));
                            itemViewHolder.mNotificationUserName.setText(spanned);
                        }
                        itemViewHolder.mNotificationUserName.setMovementMethod(LinkMovementMethod.getInstance());
                        itemViewHolder.mLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                    }
                }


    }


    @Override
    public int getItemCount()
    {
        return mNotificationList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder
    {
        ImageView mNotificationType;
        SimpleDraweeView mUserProfilePicture;
        TextView mNotificationUserName, mNotificationText, mNotificationTime, iconText, title;
        RelativeLayout mLinearLayout;
        public ItemViewHolder(View itemView)
        {
            super(itemView);
            mUserProfilePicture = (SimpleDraweeView) itemView.findViewById(R.id.icon_profile);
            mNotificationType = (ImageView) itemView.findViewById(R.id.notified_type_icon);
            mNotificationUserName = (TextView) itemView.findViewById(R.id.notified_user_name_text);
            mNotificationText = (TextView) itemView.findViewById(R.id.notification_text);
            iconText = (TextView) itemView.findViewById(R.id.icon_text);
            title = (TextView) itemView.findViewById(R.id.title);
            mNotificationTime = (TextView) itemView.findViewById(R.id.notification_time_stamp_text);
            mLinearLayout = (RelativeLayout) itemView.findViewById(R.id.user_status_layout);

        }

    }

}
