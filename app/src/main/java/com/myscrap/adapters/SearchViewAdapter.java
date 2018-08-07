package com.myscrap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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
import com.myscrap.application.AppController;
import com.myscrap.model.NearFriends;
import com.myscrap.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 12/31/2017.
 */

public class SearchViewAdapter extends RecyclerView.Adapter<SearchViewAdapter.SingleItemActiveListRowHolder>{

    private Context mContext;
    private SearchViewAdapter mSearchViewAdapter;
    private List<NearFriends.NearFriendsData> mNearFriendsDataList = new ArrayList<>();
    private SearchViewAdapter.SearchViewAdapterListener listener;

    public SearchViewAdapter(Context context, List<NearFriends.NearFriendsData> mDataList, SearchViewAdapter.SearchViewAdapterListener searchViewAdapterListener){
        this.mSearchViewAdapter = this;
        this.mContext = context;
        this.mNearFriendsDataList = mDataList;
        this.listener = searchViewAdapterListener;
    }

    @Override
    public SearchViewAdapter.SingleItemActiveListRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.active_friend_list_row_card, null);
        return new SingleItemActiveListRowHolder(v);
    }

    @Override
    public void onBindViewHolder(SingleItemActiveListRowHolder holder, int position) {
        final NearFriends.NearFriendsData mNearFriendsDataItem = mNearFriendsDataList.get(position);
        if(mContext == null)
            mContext = AppController.getInstance();
        if (mNearFriendsDataItem.getProfilePic() != null){
            if(mNearFriendsDataItem.getProfilePic().equalsIgnoreCase("") || mNearFriendsDataItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                    || mNearFriendsDataItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                holder.iconProfile.setImageResource(R.drawable.bg_circle);
                if(mNearFriendsDataItem.getColorCode() != null && !mNearFriendsDataItem.getColorCode().equalsIgnoreCase("")){
                    holder.iconProfile.setColorFilter(Color.parseColor(mNearFriendsDataItem.getColorCode()));
                } else {
                    holder.iconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                }

                holder.iconText.setVisibility(View.VISIBLE);
                if (mNearFriendsDataItem.getName() != null && !mNearFriendsDataItem.getName().equalsIgnoreCase("")){
                    String[] split = mNearFriendsDataItem.getName().trim().split("\\s+");
                    if (split.length > 1){
                        String first = split[0].trim().substring(0,1);
                        String last = split[1].trim().substring(0,1);
                        String initial = first + ""+ last ;
                        holder.iconText.setText(initial.toUpperCase().trim());
                    } else {
                        if (split[0] != null) {
                            String first = split[0].trim().substring(0,1);
                            holder.iconText.setText(first.toUpperCase().trim());
                        }
                    }
                }
            } else {
                if(holder.iconProfile.getContext() != null){
                    Uri uri = Uri.parse(mNearFriendsDataItem.getProfilePic());
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                    holder.iconProfile.setHierarchy(new GenericDraweeHierarchyBuilder(holder.iconProfile.getContext().getResources())
                            .setRoundingParams(roundingParams)
                            .build());
                    roundingParams.setRoundAsCircle(true);
                    holder.iconProfile.setImageURI(uri);
                }

                holder.iconProfile.setColorFilter(null);
                holder.iconText.setVisibility(View.GONE);
            }
        }

        if (mNearFriendsDataItem.getName() != null)
            holder.name.setText(mNearFriendsDataItem.getName());

        if(mNearFriendsDataItem.isOnline()){
            holder.online.setVisibility(View.VISIBLE);
            holder.top.setVisibility(View.GONE);
        } else {
            holder.online.setVisibility(View.GONE);
            if (mNearFriendsDataItem.getLastActive() != null && !mNearFriendsDataItem.getLastActive().isEmpty()){
                holder.top.setVisibility(View.VISIBLE);
                holder.top.setText(" " + mNearFriendsDataItem.getLastActive() + " ");
            } else {
                holder.top.setVisibility(View.GONE);
            }
        }

        String userPosition;
        if(mNearFriendsDataItem.getDesignation() != null && !mNearFriendsDataItem.getDesignation().equalsIgnoreCase("")){
            userPosition = mNearFriendsDataItem.getDesignation().trim();
        } else {
            userPosition = "Trader";
        }

        String userCompany;
        if(mNearFriendsDataItem.getCompany() != null && !mNearFriendsDataItem.getCompany().equalsIgnoreCase("")){
            userCompany = mNearFriendsDataItem.getCompany().trim();
        } else {
            userCompany = "";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SpannableStringBuilder spannedDetails;
            if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+"&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>", Html.FROM_HTML_MODE_LEGACY));
            } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "&#160"+"</font>", Html.FROM_HTML_MODE_LEGACY));
            }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
            } else if(!userPosition.equalsIgnoreCase("")){
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>", Html.FROM_HTML_MODE_LEGACY));
            } else if(!userCompany.equalsIgnoreCase("")){
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>", Html.FROM_HTML_MODE_LEGACY));
            } else {
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("", Html.FROM_HTML_MODE_LEGACY));
            }
            holder.desComp.setText(spannedDetails);
            holder.desComp.setVisibility(View.VISIBLE);
        } else {
            SpannableStringBuilder spannedDetails;
            if(!userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")){
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+  "&#160"+"&#160"+ "&#8226"+"&#160"+"&#160"+userCompany+"&#160" + "</font>"));
            } else if (!userPosition.equalsIgnoreCase("") && userCompany.equalsIgnoreCase("")) {
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
            }else if (userPosition.equalsIgnoreCase("") && !userCompany.equalsIgnoreCase("")) {
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
            } else if(!userPosition.equalsIgnoreCase("")){
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userPosition+ "</font>"));
            } else if(!userCompany.equalsIgnoreCase("")){
                spannedDetails = new SpannableStringBuilder(Html.fromHtml("<font color=\"#403f3f\">" +userCompany+ "</font>"));
            } else {
                spannedDetails = new SpannableStringBuilder(Html.fromHtml(""));
            }
            holder.desComp.setText(spannedDetails);
            holder.desComp.setVisibility(View.VISIBLE);
        }


        if(mNearFriendsDataItem.getCountry() != null && !mNearFriendsDataItem.getCountry().equalsIgnoreCase("")){
            holder.country.setText(mNearFriendsDataItem.getCountry().trim());
            holder.country.setVisibility(View.VISIBLE);
        } else {
            holder.country.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (mNearFriendsDataList == null)
            return 0;
        return mNearFriendsDataList.size();
    }

    public void setFilter(List<NearFriends.NearFriendsData> filteredModelList) {
        this.mNearFriendsDataList = filteredModelList;
        this.mSearchViewAdapter.notifyDataSetChanged();
    }

    public void swap(List<NearFriends.NearFriendsData> nearFriendsDataList) {
        this.mNearFriendsDataList = nearFriendsDataList;
        this.mSearchViewAdapter.notifyDataSetChanged();
    }

    class SingleItemActiveListRowHolder extends RecyclerView.ViewHolder {
        private TextView online;
        private  TextView top;
        private SimpleDraweeView iconProfile;
        private  TextView iconText, name, desComp, country;
        SingleItemActiveListRowHolder(final View view) {
            super(view);
            this.iconProfile = (SimpleDraweeView) view.findViewById(R.id.icon_profile);
            this.iconText = (TextView) view.findViewById(R.id.icon_text);
            this.online = (TextView) view.findViewById(R.id.online);
            this.top = (TextView) view.findViewById(R.id.top);
            this.name = (TextView) view.findViewById(R.id.name);
            this.desComp = (TextView) view.findViewById(R.id.des_comp);
            this.country = (TextView) view.findViewById(R.id.country);
            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if(mNearFriendsDataList != null && mNearFriendsDataList.size() > 0) {
                    listener.onContactsAdapterClicked(mNearFriendsDataList.get(position), position);
                }
            });
        }
    }

    public interface SearchViewAdapterListener {
        void onContactsAdapterClicked(NearFriends.NearFriendsData nearFriendsData, int position);
    }
}
