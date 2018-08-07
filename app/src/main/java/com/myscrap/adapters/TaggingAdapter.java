package com.myscrap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.model.Search;
import com.myscrap.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ms3 on 5/18/2017.
 */

public class TaggingAdapter extends RecyclerView.Adapter<TaggingAdapter.ItemViewHolder>{
    private Context mContext;
    private List<Search.SearchData.Users> mSearchDataDataList = new ArrayList<>();
    private TaggingAdapter.TaggingAdapterListener mTaggingAdapterListener;
    private TaggingAdapter mFavouriteFragmentAdapter;

    public TaggingAdapter(Context context, List<Search.SearchData.Users> mUsersList, TaggingAdapter.TaggingAdapterListener taggingAdapterListener){
        this.mContext = context;
        this.mFavouriteFragmentAdapter = this;
        this.mSearchDataDataList = mUsersList;
        this.mTaggingAdapterListener = taggingAdapterListener;
    }

    @Override
    public TaggingAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagging_list_row, parent, false);
        return new TaggingAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TaggingAdapter.ItemViewHolder holder, int position) {
        Search.SearchData.Users searchData = mSearchDataDataList.get(position);
        if(searchData != null) {
            if(searchData.getProfilePic() != null){
                String profilePicture = searchData.getProfilePic();
                if (!profilePicture.equalsIgnoreCase("")){
                    if(profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                            || profilePicture.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                        holder.profileImage.setImageResource(R.drawable.bg_circle);
                        if(searchData.getColorCode() != null && !searchData.getColorCode().equalsIgnoreCase("") && searchData.getColorCode().startsWith("#")){
                            holder.profileImage.setColorFilter(Color.parseColor(searchData.getColorCode()));
                        } else {
                            holder.profileImage.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                        }

                        holder.iconText.setVisibility(View.VISIBLE);
                    } else  {
                        Uri uri = Uri.parse(profilePicture);
                        RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                        holder.profileImage.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                                .setRoundingParams(roundingParams)
                                .build());
                        roundingParams.setRoundAsCircle(true);
                        holder.profileImage.setImageURI(uri);
                        holder.profileImage.setColorFilter(null);
                        holder.iconText.setVisibility(View.GONE);
                    }
                } else {
                    holder.profileImage.setImageResource(R.drawable.bg_circle);
                    holder.profileImage.setColorFilter(DeviceUtils.getRandomMaterialColor(mContext,"400"));
                    holder.iconText.setVisibility(View.VISIBLE);
                }
            }
            if(searchData.getName() != null){
                holder.profileName.setText(searchData.getName());
                String userName = searchData.getName();
                if (!userName.equalsIgnoreCase("")){
                    String[] split = userName.split("\\s+");
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
            }

            if(searchData.getUserCompany() != null && !searchData.getUserCompany().equalsIgnoreCase("")){
                holder.company.setText(searchData.getUserCompany());
                holder.company.setVisibility(View.VISIBLE);
            } else {
                holder.company.setVisibility(View.GONE);
            }


            if(searchData.getUserDesignation() != null && !searchData.getUserDesignation().equalsIgnoreCase("")){
                holder.profileDesignation.setText(searchData.getUserDesignation());
                holder.profileDesignation.setVisibility(View.VISIBLE);
            } else {
                holder.profileDesignation.setText("Trader");
                holder.profileDesignation.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mSearchDataDataList.size();
    }

    public void swap(List<Search.SearchData.Users> mContactList)
    {


        Set<Search.SearchData.Users> set = new HashSet<>();
        set.addAll(mContactList);
        this.mSearchDataDataList.clear();
        this.mSearchDataDataList.addAll(set);
        this.mFavouriteFragmentAdapter.notifyDataSetChanged();
    }

    public void setFilter(List<Search.SearchData.Users> mFavouriteData) {
        this.mSearchDataDataList = mFavouriteData;
        this.notifyDataSetChanged();
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SimpleDraweeView profileImage;
        private TextView profileName;
        private TextView company;
        private TextView profileDesignation;
        private ImageView badge;
        private TextView iconText;
        private TextView top;
        private TextView points;

        public ItemViewHolder(View itemView) {
            super(itemView);
            profileImage = (SimpleDraweeView) itemView.findViewById(R.id.icon_profile);
            badge = (ImageView) itemView.findViewById(R.id.icon_badge);
            profileName = (TextView) itemView.findViewById(R.id.name);
            iconText = (TextView) itemView.findViewById(R.id.icon_text);
            company = (TextView) itemView.findViewById(R.id.company);
            top = (TextView) itemView.findViewById(R.id.top);
            points = (TextView) itemView.findViewById(R.id.points);
            profileDesignation = (TextView) itemView.findViewById(R.id.designation);
            profileImage.setOnClickListener(this);
            profileName.setOnClickListener(this);
            profileDesignation.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mTaggingAdapterListener != null)
                mTaggingAdapterListener.onAdapterClicked(getAdapterPosition());
        }
    }

    public interface TaggingAdapterListener
    {
        void onAdapterClicked(int position);
    }

}
