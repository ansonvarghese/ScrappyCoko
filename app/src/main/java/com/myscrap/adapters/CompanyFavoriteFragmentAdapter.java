package com.myscrap.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.model.MyItem;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ms3 on 5/18/2017.
 */

public class CompanyFavoriteFragmentAdapter extends RecyclerView.Adapter<CompanyFavoriteFragmentAdapter.ItemViewHolder> {
    private Context mContext;
    private List<MyItem> mMarkersDataListFiltered = new ArrayList<>();
    private CompanyFragmentAdapterListener mCompanyFragmentAdapterListener;
    private CompanyFavoriteFragmentAdapter mCompanyFragmentAdapter;
    private boolean isFiltering;

    public CompanyFavoriteFragmentAdapter(RecyclerView mCompanyRecyclerView, Context context, List<MyItem> mDataList, CompanyFragmentAdapterListener listener){
        this.mContext = context;
        this.mCompanyFragmentAdapter = this;
        this.mCompanyFragmentAdapterListener = listener;
    }

    @Override
    public CompanyFavoriteFragmentAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.company_fav_list_row_item, parent, false);
        return new CompanyFavoriteFragmentAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CompanyFavoriteFragmentAdapter.ItemViewHolder holder, int position) {
        if(mMarkersDataListFiltered.isEmpty())
            return;
        final MyItem companyData = mMarkersDataListFiltered.get(position);
        if(companyData != null) {
            if(companyData.getCompanyImage() != null){
                String profilePicture = companyData.getCompanyImage();
                Uri uri = Uri.parse(profilePicture);
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                holder.profileImage.setHierarchy(new GenericDraweeHierarchyBuilder(mContext.getResources())
                        .setRoundingParams(roundingParams)
                        .build());
                roundingParams.setRoundAsCircle(true);
                holder.profileImage.setImageURI(uri);
            }
            if(companyData.getCompanyName() != null){
                holder.profileName.setText(companyData.getCompanyName().toUpperCase());
            }
            holder.companyType.setVisibility(View.VISIBLE);
            holder.companyType.setText("Recycler");
            if(companyData.getCompanyCountry()!= null && !companyData.getCompanyCountry().equalsIgnoreCase("")){
                holder.country.setText(companyData.getCompanyCountry());
                holder.country.setVisibility(View.VISIBLE);
            } else {
                holder.country.setVisibility(View.GONE);
            }

            if(companyData.getIsNew() != null && !companyData.getIsNew().equalsIgnoreCase("")){
                if(companyData.getIsNew().equalsIgnoreCase("true")){
                    holder.isNew.setVisibility(View.VISIBLE);
                } else {
                    holder.isNew.setVisibility(View.GONE);
                }
            } else {
                holder.isNew.setVisibility(View.GONE);
            }

            if(companyData.isFavourite()){
                holder.iconStar.setTag("favourite");
                holder.iconStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_black_24dp));
                holder.iconStar.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            } else {
                holder.iconStar.setTag("favourited");
                holder.iconStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_border_black_24dp));
                holder.iconStar.setColorFilter(null);

            }
            holder.iconStar.setVisibility(View.VISIBLE);
            holder.iconStar.setOnClickListener(v -> {
                if(UserUtils.isGuestLoggedIn(mContext)){
                    GuestLoginDialog.show(mContext);
                    return;
                }

                if (holder.iconStar.getTag().equals("favourite")) {
                    holder.iconStar.setTag("favourited");
                    holder.iconStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_border_black_24dp));
                    holder.iconStar.setColorFilter(null);
                    Toast.makeText(mContext, "Removed from favourites", Toast.LENGTH_SHORT).show();
                    mCompanyFragmentAdapterListener.onStarClicked(companyData, holder.getAdapterPosition(), true);
                } else {
                    holder.iconStar.setTag("favourite");
                    holder.iconStar.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_star_black_24dp));
                    holder.iconStar.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    Toast.makeText(mContext, "Added to favourites", Toast.LENGTH_SHORT).show();
                    mCompanyFragmentAdapterListener.onStarClicked(companyData, holder.getAdapterPosition(), false);
                }

            });

        }
    }

    @Override
    public int getItemCount() {
        if(mMarkersDataListFiltered == null)
            new ArrayList<MyItem>();
        return mMarkersDataListFiltered.size();
    }


    public void swap(List<MyItem> mFavouriteData) {
        this.mMarkersDataListFiltered = new ArrayList<>();
        this.mMarkersDataListFiltered = mFavouriteData;
        this.mCompanyFragmentAdapter.notifyDataSetChanged();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SimpleDraweeView profileImage;
        private ImageView iconStar;
        private TextView profileName;
        private TextView companyType;
        private TextView country;
        private TextView iconText;
        private TextView isNew;

        public ItemViewHolder(View itemView) {
            super(itemView);
            profileImage = (SimpleDraweeView) itemView.findViewById(R.id.icon_profile);
            iconStar = (ImageView) itemView.findViewById(R.id.icon_star);
            iconText = (TextView) itemView.findViewById(R.id.icon_text);
            isNew = (TextView) itemView.findViewById(R.id.is_new);
            profileName = (TextView) itemView.findViewById(R.id.company_name);
            companyType = (TextView) itemView.findViewById(R.id.company_type);
            country = (TextView) itemView.findViewById(R.id.country);
            profileImage.setOnClickListener(this);
            profileName.setOnClickListener(this);
            companyType.setOnClickListener(this);
            country.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mCompanyFragmentAdapterListener != null && !isFiltering)
                mCompanyFragmentAdapterListener.onFavouritesAdapterClicked(mMarkersDataListFiltered, getAdapterPosition());
        }
    }

    public interface CompanyFragmentAdapterListener {
        void onFavouritesAdapterClicked(List<MyItem> originalList, int position);
        void onStarClicked(MyItem mData ,int position, boolean isStarred);
    }
}
