package com.myscrap.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.UserProfileActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.Viewers;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/16/2017.
 */

public class ViewersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private Context mContext;
    private List<Viewers.ViewersData> mViewersDataList = new ArrayList<>();

    public ViewersAdapter(Context context, List<Viewers.ViewersData> viewersData){
        this.mContext = context;
        this.mViewersDataList = viewersData;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_viewers, parent, false);
        return new ViewersHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewersHolder) {

            final Viewers.ViewersData viewersData = mViewersDataList.get(position);
            ((ViewersHolder) holder).profileName.setText(viewersData.getName());

            String userPosition;
            if(viewersData.getDesignation() != null && !viewersData.getDesignation().equalsIgnoreCase("")){
                userPosition = viewersData.getDesignation().trim();
            } else {
                userPosition = "Trader";
            }

            String userCountry;
            if(viewersData.getCountry() != null && !viewersData.getCountry().equalsIgnoreCase("")){
                userCountry = viewersData.getCountry().trim();
            } else {
                userCountry = "";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SpannableStringBuilder spannedDetails;
                if(!userPosition.equalsIgnoreCase("") && !userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+userCountry+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if (!userPosition.equalsIgnoreCase("") && userCountry.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"</font>", Html.FROM_HTML_MODE_LEGACY));
                }else if (userPosition.equalsIgnoreCase("") && !userCountry.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCountry+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if(!userPosition.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else if(!userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCountry+ "</font>", Html.FROM_HTML_MODE_LEGACY));
                } else {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));
                }
                ((ViewersHolder) holder).designation.setText(spannedDetails);
                ((ViewersHolder) holder).designation.setVisibility(View.VISIBLE);
            } else {
                SpannableStringBuilder spannedDetails;
                if(!userPosition.equalsIgnoreCase("") && !userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+ "&#8226"+"&#160"+"&#160"+userCountry+"&#160" + "</font>"));
                } else if (!userPosition.equalsIgnoreCase("") && userCountry.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                }else if (userPosition.equalsIgnoreCase("") && !userCountry.equalsIgnoreCase("")) {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCountry+ "</font>"));
                } else if(!userPosition.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
                } else if(!userCountry.equalsIgnoreCase("")){
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCountry+ "</font>"));
                } else {
                    spannedDetails = new SpannableStringBuilder(Html.fromHtml(""));
                }
                ((ViewersHolder) holder).designation.setText(spannedDetails);
                ((ViewersHolder) holder).designation.setVisibility(View.VISIBLE);
            }

            if(viewersData.getModerator() == 1) {
                ((ViewersHolder) holder).top.setText(R.string.mod);
                ((ViewersHolder) holder).top.setVisibility(View.VISIBLE);
                ((ViewersHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_mod));
            } else {
                if (viewersData.getRank() >= 1 && viewersData.getRank() <=10) {
                    ((ViewersHolder) holder).top.setVisibility(View.VISIBLE);
                    ((ViewersHolder) holder).top.setText("TOP "+viewersData.getRank());
                    ((ViewersHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top));
                } else {
                    if(viewersData.isNewJoined()){
                        ((ViewersHolder) holder).top.setText(R.string.new_user);
                        ((ViewersHolder) holder).top.setVisibility(View.VISIBLE);
                        ((ViewersHolder) holder).top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_red));
                    } else {
                        ((ViewersHolder) holder).top.setVisibility(View.GONE);
                        ((ViewersHolder) holder).top.setBackground(null);
                    }
                }
            }

            if(viewersData.getViewDate() != null && !viewersData.getViewDate().equalsIgnoreCase("")){
                ((ViewersHolder) holder).viewDateLayout.setVisibility(View.VISIBLE);
                String timeAGO = UserUtils.getFeedsTimeAgo(Long.parseLong(UserUtils.parsingLong(viewersData.getViewDate())));
                ((ViewersHolder) holder).viewDate.setText(timeAGO);
            } else {
                ((ViewersHolder) holder).viewDateLayout.setVisibility(View.INVISIBLE);
            }

            if(viewersData.isNew()){
                ((ViewersHolder) holder).isNew.setVisibility(View.VISIBLE);
            } else {
                ((ViewersHolder) holder).isNew.setVisibility(View.GONE);
            }


            String[] split = viewersData.getName().split("\\s+");
            String profilePic = viewersData.getProfilePic();
            if (profilePic != null && !profilePic.equalsIgnoreCase("")){
                if(profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    ((ViewersHolder) holder).profile.setImageResource(R.drawable.bg_circle);
                    ((ViewersHolder) holder).profile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                    ((ViewersHolder) holder).iconText.setVisibility(View.VISIBLE);
                    if (viewersData.getName() != null && !viewersData.getName().equalsIgnoreCase("")){
                        if (split.length > 1){
                            String first = split[0].substring(0,1);
                            String last = split[1].substring(0,1);
                            String initial = first + ""+ last ;
                            ((ViewersHolder) holder).iconText.setText(initial.toUpperCase());
                        } else {
                            if (split[0] != null && split[0].trim().length() >= 1) {
                                String first = split[0].substring(0, 1);
                                ((ViewersHolder) holder).iconText.setText(first.toUpperCase());
                            }
                        }
                    }
                } else {
                    Uri uri = Uri.parse(profilePic);
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    ((ViewersHolder) holder).profile.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    ((ViewersHolder) holder).profile.setImageURI(uri);
                    ((ViewersHolder) holder).profile.setColorFilter(null);
                    ((ViewersHolder) holder).iconText.setVisibility(View.GONE);
                }
            }


            ((ViewersHolder) holder).itemView.setOnClickListener(v -> {
                if (viewersData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(viewersData.getUserId());
                }
            });

            ((ViewersHolder) holder).profileName.setOnClickListener(v -> {
                if (viewersData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(viewersData.getUserId());
                }
            });
            ((ViewersHolder) holder).designation.setOnClickListener(v -> {
                if (viewersData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(viewersData.getUserId());
                }
            });
            ((ViewersHolder) holder).profile.setOnClickListener(v -> {
                if (viewersData.getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())) {
                    goToUserProfile();
                } else {
                    goToUserFriendProfile(viewersData.getUserId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mViewersDataList.size();
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

    public void swap(List<Viewers.ViewersData> mViewersList) {
        if(mViewersList != null){
            this.mViewersDataList = mViewersList;
            this.notifyDataSetChanged();
        }
    }

    private class ViewersHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView profile;
        private  LinearLayout viewDateLayout;
        private TextView profileName, isNew, viewDate, iconText, top, points, designation, company;
        public ViewersHolder(View view) {
            super(view);
            profile = (SimpleDraweeView)view.findViewById(R.id.profile_photo);
            iconText = (TextView)view.findViewById(R.id.icon_text);
            top = (TextView)view.findViewById(R.id.top);
            points = (TextView)view.findViewById(R.id.points);
            profileName = (TextView)view.findViewById(R.id.name);
            designation = (TextView)view.findViewById(R.id.designation);
            company = (TextView)view.findViewById(R.id.company);
            viewDateLayout = (LinearLayout)view.findViewById(R.id.view_date);
            viewDate = (TextView)view.findViewById(R.id.date);
            isNew = (TextView)view.findViewById(R.id.is_new);
        }
    }
}
