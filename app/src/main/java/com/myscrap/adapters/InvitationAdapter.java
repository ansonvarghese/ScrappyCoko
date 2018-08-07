package com.myscrap.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.EventDetailActivity;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.UserProfileActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.EventInvitations;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/18/2017.
 */

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ItemViewHolder> {
    private Context mContext;
    private List<EventInvitations.EventInvitationsData> mEventInvitationsDataList = new ArrayList<>();
    private long mLastClickTime = 0;

    public InvitationAdapter(Context context, List<EventInvitations.EventInvitationsData> eventInvitationsData, InvitationAdapter.InvitationAdapterListener contactsFragmentAdapterListener){
        this.mContext = context;
        this.mEventInvitationsDataList = eventInvitationsData;
    }

    @Override
    public InvitationAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.invitation_list_row, parent, false);
        return new InvitationAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final InvitationAdapter.ItemViewHolder itemViewHolder, int position) {
        final EventInvitations.EventInvitationsData eventInvitationsData = mEventInvitationsDataList.get(position);
        if(eventInvitationsData != null) {
            String eventProfilePicture = eventInvitationsData.getEventImage();
            if (!eventProfilePicture.equalsIgnoreCase("")){
                if(eventProfilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || eventProfilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    Uri uri = new Uri.Builder()
                            .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                            .path(String.valueOf(R.drawable.events_calendar))
                            .build();
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    itemViewHolder.mUserProfilePicture.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    itemViewHolder.mUserProfilePicture.getHierarchy().setPlaceholderImage(R.drawable.events_calendar);
                    itemViewHolder.mUserProfilePicture.setImageURI(uri);
                    itemViewHolder.mUserProfilePicture.setColorFilter(null);
                    itemViewHolder.iconText.setVisibility(View.GONE);
                } else  {
                    Uri uri = Uri.parse(eventProfilePicture);
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    itemViewHolder.mUserProfilePicture.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    itemViewHolder.mUserProfilePicture.getHierarchy().setPlaceholderImage(R.drawable.events_calendar);
                    itemViewHolder.mUserProfilePicture.setImageURI(uri);
                    itemViewHolder.mUserProfilePicture.setColorFilter(null);
                    itemViewHolder.iconText.setVisibility(View.GONE);
                }
            } else {
                Uri uri = new Uri.Builder()
                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                        .path(String.valueOf(R.drawable.events_calendar))
                        .build();
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                itemViewHolder.mUserProfilePicture.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                        .setRoundingParams(roundingParams)
                        .build());
                roundingParams.setRoundAsCircle(true);
                itemViewHolder.mUserProfilePicture.getHierarchy().setPlaceholderImage(R.drawable.events_calendar);
                itemViewHolder.mUserProfilePicture.setImageURI(uri);
                itemViewHolder.mUserProfilePicture.setColorFilter(null);
                itemViewHolder.iconText.setVisibility(View.GONE);
            }


            itemViewHolder.mUserProfilePicture.setOnClickListener(v -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                goToEvent(eventInvitationsData);
            });


            itemViewHolder.itemView.setOnClickListener(v -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                goToEvent(eventInvitationsData);
            });

            ClickableSpan nameClickableSpan = new ClickableSpan() {

                @Override
                public void updateDrawState(TextPaint ds) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.setColor(mContext.getResources().getColor(R.color.black, mContext.getTheme()));
                    } else {
                        ds.setColor(mContext.getResources().getColor(R.color.black));
                    }
                    ds.setUnderlineText(false);
                }

                @Override
                public void onClick(View view) {
                    if (eventInvitationsData.getEventInvitedUserId() != null && !eventInvitationsData.getEventInvitedUserId().equalsIgnoreCase("")) {
                        if (eventInvitationsData.getEventInvitedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            goToUserProfile();
                        } else {
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            goToUserFriendProfile(eventInvitationsData.getEventInvitedUserId());
                        }
                    }
                }

            };
            ClickableSpan eventNameClickableSpan = new ClickableSpan() {

                @Override
                public void updateDrawState(TextPaint ds) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.setColor(mContext.getResources().getColor(R.color.black, mContext.getTheme()));
                    } else {
                        ds.setColor(mContext.getResources().getColor(R.color.black));
                    }
                    ds.setUnderlineText(false);
                }

                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    goToEvent(eventInvitationsData);
                }
            };
            ClickableSpan eventNameClickable = new ClickableSpan() {

                @Override
                public void updateDrawState(TextPaint ds) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.setColor(mContext.getResources().getColor(R.color.black, mContext.getTheme()));
                    } else {
                        ds.setColor(mContext.getResources().getColor(R.color.black));
                    }
                    ds.setUnderlineText(false);
                }

                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    goToEvent(eventInvitationsData);
                }
            };


            final StyleSpan boldOne = new StyleSpan(android.graphics.Typeface.BOLD);
            final StyleSpan boldTwo = new StyleSpan(android.graphics.Typeface.BOLD);
            String eventMessage = " invited to an event ";
            String eventName = eventInvitationsData.getEventName();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml("<font color=\"#000000\">"+eventInvitationsData.getEventInvitedUserName().trim() + " "  + "</font>"  + eventMessage + "<font color=\"#000000\">"+eventName + "."+"</font>", Html.FROM_HTML_MODE_LEGACY));
                spanned.setSpan(boldOne, spanned.toString().indexOf(eventInvitationsData.getEventInvitedUserName().trim()), spanned.toString().indexOf(eventInvitationsData.getEventInvitedUserName().trim()) + String.valueOf(eventInvitationsData.getEventInvitedUserName().trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spanned.setSpan(nameClickableSpan, spanned.toString().indexOf(eventInvitationsData.getEventInvitedUserName().trim()), spanned.toString().indexOf(eventInvitationsData.getEventInvitedUserName().trim()) + String.valueOf(eventInvitationsData.getEventInvitedUserName().trim()).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanned.setSpan(boldTwo, spanned.toString().indexOf(eventName.trim()), spanned.toString().indexOf(eventName.trim()) + String.valueOf(eventName.trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spanned.setSpan(eventNameClickable, spanned.toString().indexOf(eventMessage.trim()), spanned.toString().indexOf(eventMessage.trim()) + String.valueOf(eventMessage.trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spanned.setSpan(eventNameClickableSpan, spanned.toString().indexOf(eventName.trim()), spanned.toString().indexOf(eventName.trim()) + String.valueOf(eventName.trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                itemViewHolder.mNotificationUserName.setText(spanned);
            } else {
                final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml("<font color=\"#000000\">"+eventInvitationsData.getEventInvitedUserName().trim() +  " " + "</font>" + eventMessage + "<font color=\"#000000\">"+eventName+ "."+"</font>"));
                spanned.setSpan(boldOne, spanned.toString().indexOf(eventInvitationsData.getEventInvitedUserName().trim()), spanned.toString().indexOf(eventInvitationsData.getEventInvitedUserName().trim()) + String.valueOf(eventInvitationsData.getEventInvitedUserName().trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spanned.setSpan(nameClickableSpan, spanned.toString().indexOf(eventInvitationsData.getEventInvitedUserName().trim()), spanned.toString().indexOf(eventInvitationsData.getEventInvitedUserName().trim()) + String.valueOf(eventInvitationsData.getEventInvitedUserName().trim()).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanned.setSpan(boldTwo, spanned.toString().indexOf(eventName.trim()), spanned.toString().indexOf(eventName.trim()) + String.valueOf(eventName.trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spanned.setSpan(eventNameClickable, spanned.toString().indexOf(eventMessage.trim()), spanned.toString().indexOf(eventMessage.trim()) + String.valueOf(eventMessage.trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spanned.setSpan(eventNameClickableSpan, spanned.toString().indexOf(eventName.trim()), spanned.toString().indexOf(eventName.trim()) + String.valueOf(eventName.trim()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                itemViewHolder.mNotificationUserName.setText(spanned);
            }
            itemViewHolder.mNotificationUserName.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void goToEvent(EventInvitations.EventInvitationsData eventInvitationsData) {
        if (eventInvitationsData != null && eventInvitationsData.getEventId() != null && !eventInvitationsData.getEventId().equalsIgnoreCase(""))
            eventDetailActivity(eventInvitationsData.getEventId());
    }

    @Override
    public int getItemCount() {
        return mEventInvitationsDataList.size();
    }


    private void eventDetailActivity(String eventId) {
        Intent i = new Intent(mContext, EventDetailActivity.class);
        i.putExtra("eventId", eventId);
        mContext.startActivity(i);
    }


    private void goToUserProfile() {
        Intent i = new Intent(mContext, UserProfileActivity.class);
        mContext.startActivity(i);
    }

    private void goToUserFriendProfile(String postedUserId) {
        final Intent intent = new Intent(mContext, UserFriendProfileActivity.class);
        intent.putExtra("friendId", postedUserId);
        mContext.startActivity(intent);
    }


    class ItemViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView mUserProfilePicture;
        TextView mNotificationUserName, iconText;
        public ItemViewHolder(View itemView) {
            super(itemView);
            mUserProfilePicture = (SimpleDraweeView) itemView.findViewById(R.id.icon_profile);
            mNotificationUserName = (TextView) itemView.findViewById(R.id.user_name_text);
            iconText = (TextView) itemView.findViewById(R.id.icon_text);
        }

    }

    public interface InvitationAdapterListener {
        void onStarClicked(EventInvitations.EventInvitationsData mEventInvitationsData, boolean isStarred);
    }

}
