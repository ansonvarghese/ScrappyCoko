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

public class EventGoingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<EventInterestList.EventInterestListData> mEventInterest = new ArrayList<>();

    public EventGoingAdapter(Context context, List<EventInterestList.EventInterestListData> eventInterest){
        this.mContext = context;
        this.mEventInterest = eventInterest;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_event_going, parent, false);
        return new EventGoingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final EventInterestList.EventInterestListData interestData = mEventInterest.get(position);
        if(holder instanceof EventGoingViewHolder){
            ((EventGoingViewHolder) holder).profileName.setText(interestData.getName());
            String userName = interestData.getName();
            if (userName != null && !userName.equalsIgnoreCase("")){
                String[] split = userName.split("\\s+");
                if (split.length > 1){
                    String first = split[0].substring(0,1);
                    String last = split[1].substring(0,1);
                    String initial = first + ""+ last ;
                    ((EventGoingViewHolder) holder).iconText.setText(initial.toUpperCase());
                } else {
                    if (split[0] != null && split[0].trim().length() >= 1) {
                        String first = split[0].substring(0, 1);
                        ((EventGoingViewHolder) holder).iconText.setText(first.toUpperCase());
                    }
                }
            }

            String userPosition;
            if(interestData.getDesignation() != null && !interestData.getDesignation().equalsIgnoreCase("")){
                userPosition = interestData.getDesignation();
            } else {
                userPosition = "Trader";
            }
            String userCompany;
            if(interestData.getUserCompany() != null && !interestData.getUserCompany().equalsIgnoreCase("")){
                userCompany = interestData.getUserCompany();
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
                ((EventGoingViewHolder) holder).designation.setText(spannedDetails);
                ((EventGoingViewHolder) holder).designation.setVisibility(View.VISIBLE);
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
                ((EventGoingViewHolder) holder).designation.setText(spannedDetails);
                ((EventGoingViewHolder) holder).designation.setVisibility(View.VISIBLE);
            }


            if(interestData.getLikeProfilePic() != null){
                String profilePicture = interestData.getLikeProfilePic();
                if (!profilePicture.equalsIgnoreCase("")){
                    if(profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                            || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                        ((EventGoingViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                        if(interestData.getColorCode() != null && !interestData.getColorCode().equalsIgnoreCase("") && interestData.getColorCode().startsWith("#")){
                            ((EventGoingViewHolder) holder).profile.setColorFilter(Color.parseColor(interestData.getColorCode()));
                        } else {
                            ((EventGoingViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                        }

                        ((EventGoingViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                    } else  {
                        Uri uri = Uri.parse(profilePicture);
                        RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                        ((EventGoingViewHolder) holder).profile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                .setRoundingParams(roundingParams)
                                .build());
                        roundingParams.setRoundAsCircle(true);
                        ((EventGoingViewHolder) holder).profile.setImageURI(uri);
                        ((EventGoingViewHolder) holder).profile.setColorFilter(null);
                        ((EventGoingViewHolder) holder).iconText.setVisibility(View.GONE);
                    }
                } else {
                    ((EventGoingViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                    ((EventGoingViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                    ((EventGoingViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                }
            }

            if(interestData.getModerator() == 1) {
                ((EventGoingViewHolder) holder).top.setText(R.string.mod);
                ((EventGoingViewHolder) holder).top.setVisibility(View.VISIBLE);
                ((EventGoingViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_mod));
            } else {
                if (interestData.getRank() >= 1 && interestData.getRank() <=10) {
                    ((EventGoingViewHolder) holder).top.setVisibility(View.VISIBLE);
                    ((EventGoingViewHolder) holder).top.setText( "TOP " + interestData.getRank());
                    ((EventGoingViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top));
                } else {
                    if(interestData.isNewJoined()){
                        ((EventGoingViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_red));
                        ((EventGoingViewHolder) holder).top.setVisibility(View.VISIBLE);
                    } else {
                        ((EventGoingViewHolder) holder).top.setVisibility(View.GONE);
                    }
                }
            }

            if(interestData.getCountry() != null && !interestData.getCountry().equalsIgnoreCase("")){
                ((EventGoingViewHolder) holder).company.setText(interestData.getCountry());
                ((EventGoingViewHolder) holder).company.setVisibility(View.VISIBLE);
            } else {
                ((EventGoingViewHolder) holder).company.setVisibility(View.GONE);
            }

            ((EventGoingViewHolder) holder).profileName.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(interestData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(interestData.getUserId());
                }
            });
            ((EventGoingViewHolder) holder).designation.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(interestData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(interestData.getUserId());
                }
            });((EventGoingViewHolder) holder).profile.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(interestData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(interestData.getUserId());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mEventInterest.size();
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

    private class EventGoingViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView profile;
        private TextView iconText, top, points, profileName, designation, company;
        public EventGoingViewHolder(View view) {
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
