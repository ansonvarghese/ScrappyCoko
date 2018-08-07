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
import com.myscrap.model.Like;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/16/2017.
 */

public class LikeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<Like.LikeData> mLikeList = new ArrayList<>();

    public LikeAdapter(Context context, List<Like.LikeData> likeList){
        this.mContext = context;
        this.mLikeList = likeList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_like, parent, false);
        return new LikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Like.LikeData likeData = mLikeList.get(position);
        if(holder instanceof LikeViewHolder){
            ((LikeViewHolder) holder).profileName.setText(likeData.getName());
            String userName = likeData.getName();
            if (!userName.equalsIgnoreCase("")){
                String[] split = userName.split("\\s+");
                if (split.length > 1){
                    String first = split[0].substring(0,1);
                    String last = split[1].substring(0,1);
                    String initial = first + ""+ last ;
                    ((LikeViewHolder) holder).iconText.setText(initial.toUpperCase());
                } else {
                    if (split[0] != null && split[0].trim().length() >= 1) {
                        String first = split[0].substring(0, 1);
                        ((LikeViewHolder) holder).iconText.setText(first.toUpperCase());
                    }
                }
            }

            String userPosition;
            if(likeData.getDesignation() != null && !likeData.getDesignation().equalsIgnoreCase("")){
                userPosition = likeData.getDesignation();
            } else {
                userPosition = "Trader";
            }
            String userCompany;
            if(likeData.getUserCompany() != null && !likeData.getUserCompany().equalsIgnoreCase("")){
                userCompany = likeData.getUserCompany();
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
                ((LikeViewHolder) holder).designation.setText(spannedDetails);
                ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
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
                ((LikeViewHolder) holder).designation.setText(spannedDetails);
                ((LikeViewHolder) holder).designation.setVisibility(View.VISIBLE);
            }


            if(likeData.getLikeProfilePic() != null){
                String profilePicture = likeData.getLikeProfilePic();
                if (!profilePicture.equalsIgnoreCase("")){
                    if(profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                            || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                        ((LikeViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                        if(likeData.getColorCode() != null && !likeData.getColorCode().equalsIgnoreCase("") && likeData.getColorCode().startsWith("#")){
                            ((LikeViewHolder) holder).profile.setColorFilter(Color.parseColor(likeData.getColorCode()));
                        } else {
                            ((LikeViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                        }

                        ((LikeViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                    } else  {
                        Uri uri = Uri.parse(profilePicture);
                        RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                        ((LikeViewHolder) holder).profile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                .setRoundingParams(roundingParams)
                                .build());
                        roundingParams.setRoundAsCircle(true);
                        ((LikeViewHolder) holder).profile.setImageURI(uri);
                        ((LikeViewHolder) holder).profile.setColorFilter(null);
                        ((LikeViewHolder) holder).iconText.setVisibility(View.GONE);
                    }
                } else {
                    ((LikeViewHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                    ((LikeViewHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                    ((LikeViewHolder) holder).iconText.setVisibility(View.VISIBLE);
                }
            }

            if(likeData.getModerator() == 1) {
                ((LikeViewHolder) holder).top.setText(R.string.mod);
                ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                ((LikeViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_mod));
            } else {
                if (likeData.getRank() >= 1 && likeData.getRank() <=10) {
                    ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                    ((LikeViewHolder) holder).top.setText( "TOP " + likeData.getRank());
                    ((LikeViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top));
                } else {
                    if(likeData.isNewJoined()){
                        ((LikeViewHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_red));
                        ((LikeViewHolder) holder).top.setVisibility(View.VISIBLE);
                        ((LikeViewHolder) holder).top.setText("NEW");
                    } else {
                        ((LikeViewHolder) holder).top.setVisibility(View.GONE);
                    }
                }
            }

            if(likeData.getCountry() != null && !likeData.getCountry().equalsIgnoreCase("")){
                ((LikeViewHolder) holder).company.setText(likeData.getCountry());
                ((LikeViewHolder) holder).company.setVisibility(View.VISIBLE);
            } else {
                ((LikeViewHolder) holder).company.setVisibility(View.GONE);
            }

            ((LikeViewHolder) holder).profileName.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(likeData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(likeData.getUserId());
                }
            });
            ((LikeViewHolder) holder).designation.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(likeData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(likeData.getUserId());
                }
            });((LikeViewHolder) holder).profile.setOnClickListener(v -> {
                if (AppController.getInstance().getPrefManager().getUser() == null)
                    return;
                if(likeData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(likeData.getUserId());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mLikeList.size();
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

    private class LikeViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView profile;
        private TextView iconText, top, points, profileName, designation, company;
        public LikeViewHolder(View view) {
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
