package com.myscrap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.model.PeopleNearBy;
import com.myscrap.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 4/30/2017.
 */

public class PeopleNearByAdapter extends RecyclerView.Adapter<PeopleNearByAdapter.ItemViewHolder>{

    private Context mContext;
    private List<PeopleNearBy.PeopleNearByData> mShakeFriendList = new ArrayList<>();
    private PeopleNearByAdapterListener mPeopleNearByAdapterListener;

    public PeopleNearByAdapter(Context context, List<PeopleNearBy.PeopleNearByData> shakeFriendList, PeopleNearByAdapterListener peopleNearByAdapterListener){
        this.mContext = context;
        this.mShakeFriendList = shakeFriendList;
        this.mPeopleNearByAdapterListener = peopleNearByAdapterListener;
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.near_by_list_row, parent, false);
        return new PeopleNearByAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        PeopleNearBy.PeopleNearByData shakeFriend = mShakeFriendList.get(position);
        if(shakeFriend != null) {


            if(shakeFriend.getName() != null){
                holder.profileName.setText(shakeFriend.getName());
            }

            if(shakeFriend.getProfilePic() != null){
                String image = shakeFriend.getProfilePic();
                if (image != null && !image.equalsIgnoreCase("")){
                    if(image.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                            || image.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                        holder.profileImage.setImageResource(R.drawable.bg_circle);
                        if(shakeFriend.getColorCode() != null && !shakeFriend.getColorCode().equalsIgnoreCase("") && !shakeFriend.getColorCode().startsWith("#")){
                            holder.profileImage.setColorFilter(Color.parseColor(shakeFriend.getColorCode()));
                        } else {
                            holder.profileImage.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext, "400"));
                        }
                        holder.iconText.setVisibility(View.VISIBLE);
                        if (!image.equalsIgnoreCase("")){
                            String[] split = shakeFriend.getName().split("\\s+");
                            if (split.length > 1){
                                String first = split[0].substring(0,1);
                                String last = split[1].substring(0,1);
                                String initial = first + ""+ last ;
                                holder.iconText.setText(initial.toUpperCase());
                            } else {
                                if (split[0] != null && split[0].trim().length() >= 1) {
                                    String first = split[0].substring(0, 1);
                                    holder.iconText.setText(first.toUpperCase());
                                }
                            }
                        }
                    } else {
                        Uri uri = Uri.parse(image);
                        RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                        holder.profileImage.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                .setRoundingParams(roundingParams)
                                .build());
                        roundingParams.setRoundAsCircle(true);
                        holder.profileImage.setImageURI(uri);
                        holder.profileImage.setColorFilter(null);
                        holder.iconText.setVisibility(View.GONE);
                    }
                }
            }

            if(shakeFriend.getModerator() == 1) {
                holder.top.setText(R.string.mod);
                holder.top.setVisibility(View.VISIBLE);
                holder.top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_mod));
            } else {
                if (shakeFriend.getRank() >= 1 && shakeFriend.getRank() <=10) {
                    holder.top.setVisibility(View.VISIBLE);
                    holder.top.setText( "TOP " + shakeFriend.getRank());
                    holder.top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top));
                } else {
                    if(shakeFriend.isNewJoined()){
                        holder.top.setVisibility(View.VISIBLE);
                        holder.top.setText(R.string.new_user);
                        holder.top.setBackground(ContextCompat.getDrawable(mContext, R.drawable.top_red));
                    } else {
                        holder.top.setVisibility(View.GONE);
                        holder.top.setBackground(null);
                    }
                }
            }


            if(shakeFriend.isOnline()){
                holder.online.setVisibility(View.VISIBLE);
            } else {
                holder.online.setVisibility(View.GONE);
            }

            if(shakeFriend.getDistance() != null){
                String distance = "Within "+shakeFriend.getDistance()+ shakeFriend.getUnit();
                holder.profileRange.setText(distance);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mShakeFriendList.size();
    }

    public void swap(List<PeopleNearBy.PeopleNearByData> mShakeFriendList) {
        this.mShakeFriendList.clear();
        this.mShakeFriendList = mShakeFriendList;
        this.notifyDataSetChanged();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SimpleDraweeView profileImage;
        private TextView profileName;
        private TextView iconText;
        private TextView online;
        private TextView profileRange;
        private TextView top;
        private TextView points;

        public ItemViewHolder(View itemView) {
            super(itemView);
            profileImage = (SimpleDraweeView) itemView.findViewById(R.id.icon_profile);
            iconText = (TextView) itemView.findViewById(R.id.icon_text);
            online = (TextView) itemView.findViewById(R.id.online);
            profileName = (TextView) itemView.findViewById(R.id.name);
            profileRange = (TextView) itemView.findViewById(R.id.from_range);
            top = (TextView) itemView.findViewById(R.id.top);
            points = (TextView) itemView.findViewById(R.id.points);
            profileImage.setOnClickListener(this);
            profileName.setOnClickListener(this);
            profileRange.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mPeopleNearByAdapterListener != null)
                mPeopleNearByAdapterListener.onPeopleNearByAdapterClicked(getAdapterPosition());
        }
    }

    public interface PeopleNearByAdapterListener {
        void onPeopleNearByAdapterClicked(int position);
    }
}
