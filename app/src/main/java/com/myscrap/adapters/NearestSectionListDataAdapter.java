package com.myscrap.adapters;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.ChatRoomActivity;
import com.myscrap.R;
import com.myscrap.UserFriendProfileActivity;
import com.myscrap.activity.XMPPChatRoomActivity;
import com.myscrap.application.AppController;
import com.myscrap.model.NearFriends;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.xmppresources.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ms2 on 6/5/2016.
 */
public class NearestSectionListDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NearFriends.NearFriendsData> mNearFriendsDataList = new ArrayList<>();
    private Context mContext;
    private int VIEW_TYPE;

    NearestSectionListDataAdapter(List<NearFriends.NearFriendsData> peopleYouMayKnowItemList, Context context, boolean isActiveList, int VIEW_TYPE) {
        this.mNearFriendsDataList.clear();
        this.mNearFriendsDataList = peopleYouMayKnowItemList;
        this.mContext = context;
        this.VIEW_TYPE = VIEW_TYPE;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (this.VIEW_TYPE == 0) {
            @SuppressLint("InflateParams")
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.nearest_friend_list_card, null);
            return new SingleItemRowHolder(v);
        } else if (this.VIEW_TYPE == 1) {
            @SuppressLint("InflateParams")
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.active_friend_list_card, null);
            return new SingleItemActiveRowHolder(v);
        } else if (this.VIEW_TYPE == 2){
            @SuppressLint("InflateParams")
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.active_friend_list_row_card, null);
            return new SingleItemActiveListRowHolder(v);
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final NearFriends.NearFriendsData mPeopleYouMayKnowItem = mNearFriendsDataList.get(position);
        if (viewHolder instanceof SingleItemRowHolder) {

            SingleItemRowHolder holder = ((SingleItemRowHolder) viewHolder);

            if(mContext == null)
                mContext = AppController.getInstance();
            if (mPeopleYouMayKnowItem.getProfilePic() != null){
                if(mPeopleYouMayKnowItem.getProfilePic().equalsIgnoreCase("") || mPeopleYouMayKnowItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || mPeopleYouMayKnowItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    holder.iconProfile.setImageResource(R.drawable.bg_circle);
                    if(mPeopleYouMayKnowItem.getColorCode() != null && !mPeopleYouMayKnowItem.getColorCode().equalsIgnoreCase("") && mPeopleYouMayKnowItem.getColorCode().startsWith("#")){
                        holder.iconProfile.setColorFilter(Color.parseColor(mPeopleYouMayKnowItem.getColorCode()));
                    } else {
                        holder.iconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                    }

                    holder.iconText.setVisibility(View.VISIBLE);
                    if (mPeopleYouMayKnowItem.getName() != null && !mPeopleYouMayKnowItem.getName().equalsIgnoreCase("")){
                        String[] split = mPeopleYouMayKnowItem.getName().trim().split("\\s+");
                        if (split.length > 1){
                            String first = split[0].trim().substring(0,1);
                            String last = split[1].trim().substring(0,1);
                            String initial = first + ""+ last ;
                            holder.iconText.setText(initial.toUpperCase().trim());
                        } else {
                            if (split[0] != null && split[0].trim().length() >= 1) {
                                String first = split[0].trim().substring(0, 1);
                                holder.iconText.setText(first.toUpperCase().trim());
                            }
                        }
                    }
                } else {
                    if(holder.iconProfile.getContext() != null){
                        Uri uri = Uri.parse(mPeopleYouMayKnowItem.getProfilePic());
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

            if(mPeopleYouMayKnowItem.getModerator() == 1) {
                holder.top.setText(R.string.mod);
                holder.top.setVisibility(View.VISIBLE);
                holder.top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_mod));
            } else {
                if (mPeopleYouMayKnowItem.getRank() >= 1 && mPeopleYouMayKnowItem.getRank() <=10) {
                    holder.top.setVisibility(View.VISIBLE);
                    holder.top.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    holder.top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top));
                    holder.top.setText( "TOP " + mPeopleYouMayKnowItem.getRank());
                } else {
                    if(mPeopleYouMayKnowItem.isNewJoined()){
                        holder.top.setText(R.string.new_user);
                        holder.top.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                        holder.top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_red));
                        holder.top.setVisibility(View.VISIBLE);
                    } else {
                        holder.top.setVisibility(View.GONE);
                        holder.top.setBackground(null);
                    }
                }
            }

            if(mPeopleYouMayKnowItem.isOnline()){
                holder.online.setVisibility(View.VISIBLE);
            } else {
                holder.online.setVisibility(View.GONE);
            }

        } else if (viewHolder instanceof SingleItemActiveRowHolder){

            SingleItemActiveRowHolder holder = ((SingleItemActiveRowHolder) viewHolder);
            if(mContext == null)
                mContext = AppController.getInstance();
            if (mPeopleYouMayKnowItem.getProfilePic() != null)
            {
                if(mPeopleYouMayKnowItem.getProfilePic().equalsIgnoreCase("") || mPeopleYouMayKnowItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || mPeopleYouMayKnowItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    holder.iconProfile.setImageResource(R.drawable.bg_circle);
                    if(mPeopleYouMayKnowItem.getColorCode() != null && !mPeopleYouMayKnowItem.getColorCode().equalsIgnoreCase("")){
                        holder.iconProfile.setColorFilter(Color.parseColor(mPeopleYouMayKnowItem.getColorCode()));
                    } else {
                        holder.iconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                    }

                    holder.iconText.setVisibility(View.VISIBLE);
                    if (mPeopleYouMayKnowItem.getName() != null && !mPeopleYouMayKnowItem.getName().equalsIgnoreCase("")){
                        String[] split = mPeopleYouMayKnowItem.getName().trim().split("\\s+");
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
                        Uri uri = Uri.parse(mPeopleYouMayKnowItem.getProfilePic());
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

            if(mPeopleYouMayKnowItem.isOnline()){
                holder.online.setVisibility(View.VISIBLE);
                holder.top.setVisibility(View.GONE);
            } else {
                holder.online.setVisibility(View.GONE);
                if (mPeopleYouMayKnowItem.getLastActive() != null && !mPeopleYouMayKnowItem.getLastActive().isEmpty()){
                    holder.top.setVisibility(View.VISIBLE);
                    holder.top.setText(" " + mPeopleYouMayKnowItem.getLastActive() + " ");
                } else {
                    holder.top.setVisibility(View.GONE);
                }
            }

        } else if (viewHolder instanceof SingleItemActiveListRowHolder){

            SingleItemActiveListRowHolder holder = ((SingleItemActiveListRowHolder) viewHolder);
            if(mContext == null)
                mContext = AppController.getInstance();
            if (mPeopleYouMayKnowItem.getProfilePic() != null){
                if(mPeopleYouMayKnowItem.getProfilePic().equalsIgnoreCase("") || mPeopleYouMayKnowItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                        || mPeopleYouMayKnowItem.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                    holder.iconProfile.setImageResource(R.drawable.bg_circle);
                    if(mPeopleYouMayKnowItem.getColorCode() != null && !mPeopleYouMayKnowItem.getColorCode().equalsIgnoreCase("")){
                        holder.iconProfile.setColorFilter(Color.parseColor(mPeopleYouMayKnowItem.getColorCode()));
                    } else {
                        holder.iconProfile.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                    }

                    holder.iconText.setVisibility(View.VISIBLE);
                    if (mPeopleYouMayKnowItem.getName() != null && !mPeopleYouMayKnowItem.getName().equalsIgnoreCase("")){
                        String[] split = mPeopleYouMayKnowItem.getName().trim().split("\\s+");
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
                        Uri uri = Uri.parse(mPeopleYouMayKnowItem.getProfilePic());
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

            if (mPeopleYouMayKnowItem.getName() != null)
                holder.name.setText(mPeopleYouMayKnowItem.getName());

            if(mPeopleYouMayKnowItem.isOnline()){
                holder.online.setVisibility(View.VISIBLE);
                holder.top.setVisibility(View.GONE);
            } else {
                holder.online.setVisibility(View.GONE);
                if (mPeopleYouMayKnowItem.getLastActive() != null && !mPeopleYouMayKnowItem.getLastActive().isEmpty()){
                    holder.top.setVisibility(View.VISIBLE);
                    holder.top.setText(" " + mPeopleYouMayKnowItem.getLastActive() + " ");
                } else {
                    holder.top.setVisibility(View.GONE);
                }
            }

            String userPosition;
            if(mPeopleYouMayKnowItem.getDesignation() != null && !mPeopleYouMayKnowItem.getDesignation().equalsIgnoreCase("")){
                userPosition = mPeopleYouMayKnowItem.getDesignation().trim();
            } else {
                userPosition = "Trader";
            }

            String userCompany;
            if(mPeopleYouMayKnowItem.getCompany() != null && !mPeopleYouMayKnowItem.getCompany().equalsIgnoreCase("")){
                userCompany = mPeopleYouMayKnowItem.getCompany().trim();
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


            if(mPeopleYouMayKnowItem.getCountry() != null && !mPeopleYouMayKnowItem.getCountry().equalsIgnoreCase("")){
                holder.country.setText(mPeopleYouMayKnowItem.getCountry().trim());
                holder.country.setVisibility(View.VISIBLE);
            } else {
                holder.country.setVisibility(View.GONE);
            }

        }
    }


    @Override
    public int getItemCount() {
        if (this.VIEW_TYPE == NearFriends.VIEW_TYPE_ACTIVE) {
            if (null != mNearFriendsDataList){
                if (mNearFriendsDataList.size() >= 10) {
                    return 9;
                } else {
                    return mNearFriendsDataList.size();
                }
            } else {
                return 0;
            }
        } else if (this.VIEW_TYPE == NearFriends.VIEW_TYPE_FEED) {
            if (null != mNearFriendsDataList){
                if (mNearFriendsDataList.size() >= 10) {
                    return 9;
                } else {
                    return mNearFriendsDataList.size();
                }
            } else {
                return 0;
            }
        } else {
            return (null != mNearFriendsDataList ? mNearFriendsDataList.size() : 0);
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class SingleItemRowHolder extends RecyclerView.ViewHolder {
        private  TextView online;
        private  TextView top;
        private  ImageView profile;
        private SimpleDraweeView iconProfile;
        private  TextView iconText;
        SingleItemRowHolder(final View view) {
            super(view);
            this.iconProfile = (SimpleDraweeView) view.findViewById(R.id.icon_profile);
            this.iconText = (TextView) view.findViewById(R.id.icon_text);
            this.online = (TextView) view.findViewById(R.id.online);
            this.top = (TextView) view.findViewById(R.id.top);
            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if(mNearFriendsDataList != null && mNearFriendsDataList.size() > 0) {
                    if(CheckNetworkConnection.isConnectionAvailable(mContext))
                        goToUserFriendProfile(mNearFriendsDataList.get(position).getUserid());
                    else
                        SnackBarDialog.showNoInternetError(view);
                }

            });
        }
    }

    class SingleItemActiveRowHolder extends RecyclerView.ViewHolder {
        private  TextView online;
        private  TextView top;
        private SimpleDraweeView iconProfile;
        private  TextView iconText;
        SingleItemActiveRowHolder(final View view) {
            super(view);
            this.iconProfile = (SimpleDraweeView) view.findViewById(R.id.icon_profile);
            this.iconText = (TextView) view.findViewById(R.id.icon_text);
            this.online = (TextView) view.findViewById(R.id.online);
            this.top = (TextView) view.findViewById(R.id.top);
            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if(mNearFriendsDataList != null && mNearFriendsDataList.size() > 0) {
                    goToXMPPChat(mNearFriendsDataList.get(position).getjId(),mNearFriendsDataList.get(position).getUserid(),mNearFriendsDataList.get(position).getName(), mNearFriendsDataList.get(position).getProfilePic(), mNearFriendsDataList.get(position).getColorCode());
                }
            });
        }
    }

    class SingleItemActiveListRowHolder extends RecyclerView.ViewHolder
    {
        private  TextView online;
        private  TextView top;
        private SimpleDraweeView iconProfile;
        private  TextView iconText, name, desComp, country;
        SingleItemActiveListRowHolder(final View view)
        {
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
                if(mNearFriendsDataList != null && mNearFriendsDataList.size() > 0)
                {
                    goToXMPPChat(mNearFriendsDataList.get(position).getjId(),mNearFriendsDataList.get(position).getUserid(),mNearFriendsDataList.get(position).getName(), mNearFriendsDataList.get(position).getProfilePic(), mNearFriendsDataList.get(position).getColorCode());
                }
            });
        }
    }

    private void goToUserFriendProfile(String postedUserId)
    {
        if(mContext != null)
        {
            final Intent intent = new Intent(mContext, UserFriendProfileActivity.class);
            intent.putExtra("friendId", postedUserId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }

    }




    private void goToXMPPChat(String jid,String id, String from, String chatRoomProfilePic, String color)
    {
        if(mContext != null && jid != null)
        {
            Intent intent = new Intent(AppController.getInstance(), XMPPChatRoomActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra(Constant.FRIENDS_JID, jid);
            intent.putExtra(Constant.FRIENDS_ID, id);
            intent.putExtra(Constant.FRIENDS_NAME, from);
            intent.putExtra(Constant.FRIENDS_URL, chatRoomProfilePic);
            intent.putExtra(Constant.FRIENDS_COLOR, color);

            mContext.startActivity(intent);
        }
    }

    private void goToChat(String jid,String id, String from, String chatRoomProfilePic, String color)
    {

        if(mContext != null)
        {
            Intent intent = new Intent(AppController.getInstance(), ChatRoomActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("chatRoomId", id);
            intent.putExtra("color", color);
            intent.putExtra("chatRoomName", from);
            intent.putExtra("chatRoomProfilePic", chatRoomProfilePic);
            intent.putExtra("online", "0");
            mContext.startActivity(intent);
        }
    }
}
