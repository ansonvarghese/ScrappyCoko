package com.myscrap.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myscrap.R;
import com.myscrap.model.MyItem;
import com.myscrap.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

public class MarkerFilterRecyclerViewAdapter extends RecyclerView.Adapter<MarkerFilterRecyclerViewAdapter.MarkerFilterItemViewHolder>{

    private MarkerFilterRecyclerViewAdapter mMarkerFilterRecyclerViewAdapter;
    private List<MyItem> mUserSearchModel;
    private Context context;
    private OnItemTouchListener mOnItemTouchListener;
    private List<MyItem> userSearchModelsList = new ArrayList<>();
    public Context mContext;

    public MarkerFilterRecyclerViewAdapter(List<MyItem> mUserSearchModel, Context context, OnItemTouchListener itemTouchListener) {
        this.mUserSearchModel = mUserSearchModel;
        this.context = context;
        mMarkerFilterRecyclerViewAdapter = this;
        mOnItemTouchListener = itemTouchListener;
    }

    @Override
    public void onBindViewHolder( final MarkerFilterItemViewHolder itemViewHolder, int i) {
        final MyItem userSearchModel = mUserSearchModel.get(i);
        mContext = context;
        userSearchModelsList = mUserSearchModel;
        itemViewHolder.searchUserName.setText(userSearchModel.getCompanyName());
        String cityName = UserUtils.capitalize(userSearchModel.getCompanyAddress());
        String countryName = UserUtils.capitalize(userSearchModel.getCompanyCountry());

        if (!cityName.equalsIgnoreCase("") && !countryName.equalsIgnoreCase("")) {
            itemViewHolder.searchUserCountry.setText(cityName+ ", "+countryName);
        } else if (!cityName.equalsIgnoreCase("") && countryName.equalsIgnoreCase("")) {
            itemViewHolder.searchUserCountry.setText(cityName);
        } else if (cityName.equalsIgnoreCase("") && !countryName.equalsIgnoreCase("")) {
            itemViewHolder.searchUserCountry.setText(countryName);
        } else {
            itemViewHolder.searchUserCountry.setText("");
        }

        Uri uri = Uri.parse(userSearchModel.getCompanyImage());
        itemViewHolder.searchUserProfile.setImageURI(uri);
    }

    @Override
    public MarkerFilterRecyclerViewAdapter.MarkerFilterItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.company_list_row, viewGroup, false);
        return new MarkerFilterItemViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mUserSearchModel.size();
    }

    public void setFilter(List<MyItem> userSearchModels){
        mUserSearchModel = new ArrayList<>();
        mUserSearchModel.addAll(userSearchModels);
        mMarkerFilterRecyclerViewAdapter.notifyDataSetChanged();
    }

    class MarkerFilterItemViewHolder extends RecyclerView.ViewHolder{
        private TextView searchUserName, searchUserCountry;
        private ImageView searchUserProfile;
        private RelativeLayout mRelativeLayout;
        private MarkerFilterItemViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            searchUserName = (TextView) itemView.findViewById(R.id.user_search_name_text);
            searchUserCountry = (TextView) itemView.findViewById(R.id.user_search_country_text);
            searchUserProfile = (ImageView) itemView.findViewById(R.id.user_search_profile_photo);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.user_search_layout);

           mRelativeLayout.setOnClickListener(v -> {
              int position = getAdapterPosition();
               MyItem item = new MyItem(userSearchModelsList.get(position).getPosition(),userSearchModelsList.get(position).getCompanyName(),userSearchModelsList.get(position).getCompanyType(),userSearchModelsList.get(position).getIsNew(), userSearchModelsList.get(position).getCompanyAddress(),userSearchModelsList.get(position).getCompanyCountry(),userSearchModelsList.get(position).getMarkerId(),userSearchModelsList.get(position).getCompanyImage());
               mOnItemTouchListener.onViewTouch(v,getAdapterPosition(),item);
           });searchUserProfile.setOnClickListener(v -> {
               int position = getAdapterPosition();
               MyItem item = new MyItem(userSearchModelsList.get(position).getPosition(),userSearchModelsList.get(position).getCompanyName(),userSearchModelsList.get(position).getCompanyType(),userSearchModelsList.get(position).getIsNew(), userSearchModelsList.get(position).getCompanyAddress(),userSearchModelsList.get(position).getCompanyCountry(),userSearchModelsList.get(position).getMarkerId(),userSearchModelsList.get(position).getCompanyImage());
               mOnItemTouchListener.onViewTouch(v,getAdapterPosition(),item);
           });searchUserCountry.setOnClickListener(v -> {
              int position = getAdapterPosition();
               MyItem item = new MyItem(userSearchModelsList.get(position).getPosition(),userSearchModelsList.get(position).getCompanyName(),userSearchModelsList.get(position).getCompanyType(),userSearchModelsList.get(position).getIsNew(), userSearchModelsList.get(position).getCompanyAddress(),userSearchModelsList.get(position).getCompanyCountry(),userSearchModelsList.get(position).getMarkerId(),userSearchModelsList.get(position).getCompanyImage());
               mOnItemTouchListener.onViewTouch(v,getAdapterPosition(),item);
           });searchUserName.setOnClickListener(v -> {
              int position = getAdapterPosition();
               MyItem item = new MyItem(userSearchModelsList.get(position).getPosition(),userSearchModelsList.get(position).getCompanyName(),userSearchModelsList.get(position).getCompanyType(),userSearchModelsList.get(position).getIsNew(), userSearchModelsList.get(position).getCompanyAddress(),userSearchModelsList.get(position).getCompanyCountry(),userSearchModelsList.get(position).getMarkerId(),userSearchModelsList.get(position).getCompanyImage());
               mOnItemTouchListener.onViewTouch(v,getAdapterPosition(),item);
           });
        }
    }
}
