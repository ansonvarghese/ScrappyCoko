package com.myscrap.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.UserProfileActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.EventInterestList;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/16/2017.
 */

public class EventInterestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<EventInterestList.EventInterestListData> mEventInterestListDataList = new ArrayList<>();

    public EventInterestAdapter(Context context, List<EventInterestList.EventInterestListData> eventInterestListDataList){
        this.mContext = context;
        this.mEventInterestListDataList = eventInterestListDataList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_event_interest, parent, false);
        return new EventInterestListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final EventInterestList.EventInterestListData eventInterestListData = mEventInterestListDataList.get(position);
        if(holder instanceof EventInterestListViewHolder){
            ((EventInterestListViewHolder) holder).profileName.setText(eventInterestListData.getName());
            String userName = eventInterestListData.getName();
            if (!userName.equalsIgnoreCase("")){
                String[] split = userName.split("\\s+");
                if (split.length > 1){
                    String first = split[0].substring(0,1);
                    String last = split[1].substring(0,1);
                    String initial = first + ""+ last ;
                    ((EventInterestListViewHolder) holder).iconText.setText(initial.toUpperCase());
                } else {
                    if (split[0] != null && split[0].trim().length() >= 1) {
                        String first = split[0].substring(0, 1);
                        ((EventInterestListViewHolder) holder).iconText.setText(first.toUpperCase());
                    }
                }
            }

            String userPosition;
            if(eventInterestListData.getDesignation() != null && !eventInterestListData.getDesignation().equalsIgnoreCase("")){
                userPosition = eventInterestListData.getDesignation();
            } else {
                userPosition = "Trader";
            }
            String userCompany;
            if(eventInterestListData.getUserCompany() != null && !eventInterestListData.getUserCompany().equalsIgnoreCase("")){
                userCompany = eventInterestListData.getUserCompany();
            } else {
                userCompany = "";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SpannableStringBuilder spannedDetails;
                if(userPosition != null && !userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if (userPosition != null && !userPosition.equalsIgnoreCase("") && userCompany != null && userCompany.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"</font>", Html.FROM_HTML_MODE_LEGACY));
                }else if (userPosition != null && userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if(userPosition != null && !userPosition.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if(userCompany != null && !userCompany.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));
                }
                ((EventInterestListViewHolder) holder).designation.setText(spannedDetails);
                ((EventInterestListViewHolder) holder).designation.setVisibility(View.VISIBLE);
            } else {
                SpannableStringBuilder spannedDetails;
                if(userPosition != null && !userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+ "&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>"));
                } else if (userPosition != null && !userPosition.equalsIgnoreCase("") && userCompany != null && userCompany.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                }else if (userPosition != null && userPosition.equalsIgnoreCase("") && userCompany != null && !userCompany.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                } else if(userPosition != null && !userPosition.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                } else if(userCompany != null && !userCompany.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
                } else {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml(""));
                }
                ((EventInterestListViewHolder) holder).designation.setText(spannedDetails);
                ((EventInterestListViewHolder) holder).designation.setVisibility(View.VISIBLE);
            }


            if(eventInterestListData.getLikeProfilePic() != null){
                String profilePicture = eventInterestListData.getLikeProfilePic();
                if (!profilePicture.equalsIgnoreCase("")){
                    if(profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                            || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                        ((EventInterestListViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                        if(eventInterestListData.getColorCode() != null && !eventInterestListData.getColorCode().equalsIgnoreCase("") && eventInterestListData.getColorCode().startsWith("#")){
                            ((EventInterestListViewHolder) holder).profile.setColorFilter(Color.parseColor(eventInterestListData.getColorCode()));
                        } else {
                            ((EventInterestListViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                        }

                        ((EventInterestListViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                    } else  {
                        Uri uri = Uri.parse(profilePicture);
                        RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                        ((EventInterestListViewHolder) holder).profile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                .setRoundingParams(roundingParams)
                                .build());
                        roundingParams.setRoundAsCircle(true);
                        ((EventInterestListViewHolder) holder).profile.setImageURI(uri);
                        ((EventInterestListViewHolder) holder).profile.setColorFilter(null);
                        ((EventInterestListViewHolder) holder).iconText.setVisibility(View.GONE);
                    }
                } else {
                    ((EventInterestListViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                    ((EventInterestListViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                    ((EventInterestListViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                }
            }

            if(eventInterestListData.getModerator() == 1) {
                ((EventInterestListViewHolder) holder).top.setText(R.string.mod);
                ((EventInterestListViewHolder) holder).top.setVisibility(View.VISIBLE);
                ((EventInterestListViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_mod));
            } else {
                if (eventInterestListData.getRank() >= 1 && eventInterestListData.getRank() <=10) {
                    ((EventInterestListViewHolder) holder).top.setVisibility(View.VISIBLE);
                    ((EventInterestListViewHolder) holder).top.setText( "TOP " + eventInterestListData.getRank());
                    ((EventInterestListViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top));
                } else {
                    if(eventInterestListData.isNewJoined()){
                        ((EventInterestListViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_red));
                        ((EventInterestListViewHolder) holder).top.setVisibility(View.VISIBLE);
                    } else {
                        ((EventInterestListViewHolder) holder).top.setVisibility(View.GONE);
                    }
                }
            }

            if(eventInterestListData.getCountry() != null && !eventInterestListData.getCountry().equalsIgnoreCase("")){
                ((EventInterestListViewHolder) holder).company.setText(eventInterestListData.getCountry());
                ((EventInterestListViewHolder) holder).company.setVisibility(View.VISIBLE);
            } else {
                ((EventInterestListViewHolder) holder).company.setVisibility(View.GONE);
            }

            ((EventInterestListViewHolder) holder).profileName.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(eventInterestListData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(eventInterestListData.getUserId());
                }
            });
            ((EventInterestListViewHolder) holder).designation.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(eventInterestListData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(eventInterestListData.getUserId());
                }
            });((EventInterestListViewHolder) holder).profile.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(eventInterestListData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(eventInterestListData.getUserId());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mEventInterestListDataList.size();
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

    private class EventInterestListViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView profile;
        private TextView iconText, top, points, profileName, designation, company;
        public EventInterestListViewHolder(View view) {
            super(view);
            profile = (SimpleDraweeView)view.findViewById(R.id.icon_profile);
            iconText = (TextView)view.findViewById(R.id.icon_text);
            profileName = (TextView)view.findViewById(R.id.name);
            top = (TextView)view.findViewById(R.id.top);
            points = (TextView)view.findViewById(R.id.points);
            designation = (TextView)view.findViewById(R.id.designation);
            company = (TextView)view.findViewById(R.id.company);
        }
    }
}
