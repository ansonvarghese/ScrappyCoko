package com.myscrap.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.model.MyItem;
import com.myscrap.utils.UserUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ms3 on 5/18/2017.
 */

public class CompanyFragmentAdapter extends RecyclerView.Adapter<CompanyFragmentAdapter.ItemViewHolder> implements Filterable, SectionIndexer {
    private Context mContext;
    private final List<MyItem> mMarkersDataList;
    private List<MyItem> mMarkersDataListFiltered = new ArrayList<>();
    private CompanyFragmentAdapterListener mCompanyFragmentAdapterListener;
    private CompanyFragmentAdapter mCompanyFragmentAdapter;
    private String whichFilter;
    private ArrayList<Integer> mSectionPositions;
    private boolean isFiltering;

    public CompanyFragmentAdapter(RecyclerView mCompanyRecyclerView, Context context, List<MyItem> mDataList, CompanyFragmentAdapterListener listener){
        this.mContext = context;
        this.mCompanyFragmentAdapter = this;
        this.mMarkersDataList = mDataList;
        Set<MyItem> hs = new HashSet<>();
        hs.addAll(mDataList);
        this.mMarkersDataList.clear();
        this.mMarkersDataList.addAll(hs);
        this.mCompanyFragmentAdapterListener = listener;
    }

    @Override
    public CompanyFragmentAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.company_list_row_item, parent, false);
        return new CompanyFragmentAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CompanyFragmentAdapter.ItemViewHolder holder, int position) {
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

            if(companyData.getIsNew() != null && !companyData.getIsNew().equalsIgnoreCase("")){
                if(companyData.getIsNew().equalsIgnoreCase("true")){
                    holder.isNew.setVisibility(View.VISIBLE);
                } else {
                    holder.isNew.setVisibility(View.GONE);
                }
            } else {
                holder.isNew.setVisibility(View.GONE);
            }

            if(companyData.getCompanyType() != null && !companyData.getCompanyType().equalsIgnoreCase("")){
                holder.companyType.setVisibility(View.VISIBLE);
                holder.companyType.setText(companyData.getCompanyType());
            } else {
                holder.companyType.setVisibility(View.VISIBLE);
                holder.companyType.setText("Recycler");
            }

            if(companyData.getCompanyCountry()!= null && !companyData.getCompanyCountry().equalsIgnoreCase("")){
                String capitalizeFirstChar = UserUtils.capitalizeFirst(companyData.getCompanyCountry().toLowerCase());
                holder.country.setText(capitalizeFirstChar);
                holder.country.setVisibility(View.VISIBLE);
            } else {
                holder.country.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if(mMarkersDataListFiltered == null)
            new ArrayList<MyItem>();
        return mMarkersDataListFiltered.size();
    }

    public void swap(List<MyItem> mFavouriteData, String filter) {
        whichFilter = filter;
        if(mMarkersDataListFiltered != null){
            Set<MyItem> hs = new HashSet<>();
            hs.addAll(mFavouriteData);
            mMarkersDataListFiltered.clear();
            mMarkersDataListFiltered.addAll(hs);
            mCompanyFragmentAdapter.notifyDataSetChanged();
        }
    }

    private Filter fRecords;
    @Override
    public Filter getFilter() {
        if( fRecords == null)
            fRecords = new CompanyFilter(mCompanyFragmentAdapter, mMarkersDataList);
        return fRecords;
    }

    @Override
    public Object[] getSections() {
        List<String> sections = new ArrayList<>(26);
        mSectionPositions = new ArrayList<>(26);
        if(mMarkersDataListFiltered.isEmpty())
            return null;
        if(!isFiltering) {
            if(mMarkersDataListFiltered != null && mMarkersDataListFiltered.size() > 0) {
                    if(mCompanyFragmentAdapter != null && mMarkersDataListFiltered.size() > 0){
                        List<MyItem> dataByName;
                        if(whichFilter != null){
                            if(whichFilter.equalsIgnoreCase("COUNTRY")){
                                dataByName = filterByCountryName(mMarkersDataListFiltered);
                                for (int i = 0, size = dataByName.size(); i < size; i++) {
                                    if(dataByName.get(i).getCompanyName()!= null){
                                        if(!dataByName.get(i).getCompanyCountry().isEmpty()){
                                            String section = String.valueOf(dataByName.get(i).getCompanyCountry().trim().charAt(0)).toUpperCase();
                                            if (!sections.contains(section)) {
                                                sections.add(section);
                                                mSectionPositions.add(i);
                                            }
                                        }
                                    }
                                }
                            } else {
                                dataByName = filterByName(mMarkersDataListFiltered);
                                for (int i = 0, size = dataByName.size(); i < size; i++) {
                                    if(dataByName.get(i).getCompanyName()!= null){
                                        if(!dataByName.get(i).getCompanyName().isEmpty()){
                                            String section = String.valueOf(dataByName.get(i).getCompanyName().trim().charAt(0)).toUpperCase();
                                            if (!sections.contains(section)) {
                                                sections.add(section);
                                                mSectionPositions.add(i);
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            dataByName = filterByName(mMarkersDataListFiltered);
                            for (int i = 0, size = dataByName.size(); i < size; i++) {
                                if(dataByName.get(i).getCompanyName()!= null){
                                    if(!dataByName.get(i).getCompanyName().isEmpty()){
                                        String section = String.valueOf(dataByName.get(i).getCompanyName().trim().charAt(0)).toUpperCase();
                                        if (!sections.contains(section)) {
                                            sections.add(section);
                                            mSectionPositions.add(i);
                                        }
                                    }
                                }
                            }
                        }

                    }
            }

        }

        return sections.toArray(new String[0]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (mSectionPositions == null) {
            return 0;
        }

        // Check index bounds
        if (sectionIndex <= 0) {
            return 0;
        }
        if (sectionIndex >= mSectionPositions.size()) {
            sectionIndex = mSectionPositions.size() - 1;
        }
        return mSectionPositions.get(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
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
            if(mCompanyFragmentAdapterListener != null && mMarkersDataListFiltered != null && !isFiltering)
                mCompanyFragmentAdapterListener.onFavouritesAdapterClicked(mMarkersDataListFiltered, getAdapterPosition(), mMarkersDataListFiltered.get(getAdapterPosition()).getMarkerId());
        }
    }

    public interface CompanyFragmentAdapterListener {
        void onFavouritesAdapterClicked(List<MyItem> originalList, int position, String companyId);
        void onStarClicked(int position, boolean isStarred);
    }

    private class CompanyFilter extends Filter {

        CompanyFragmentAdapter adapter;
        final List<MyItem> originalList;
        List<MyItem> filteredList;

        CompanyFilter(CompanyFragmentAdapter adapter, List<MyItem> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = originalList;
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults results = new FilterResults();
            if(mMarkersDataList == null)
                return null;
            filteredList.clear();
            List<MyItem> dataName = new ArrayList<>();
            List<MyItem> dataByName;

            for(MyItem item : filterByName(mMarkersDataList)){
                if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("")){
                    dataName.add(item);
                }
            }
            dataByName = filterByName(dataName);
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(dataByName);
            } else {
                if(mCompanyFragmentAdapterListener != null)
                    mCompanyFragmentAdapterListener.onStarClicked(0, true);
                filteredList = doFiltering(constraint, dataByName);
            }

            Set<MyItem> hs = new HashSet<>();
            hs.addAll(filteredList);
            filteredList.clear();
            filteredList.addAll(hs);

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, final FilterResults results) {
            isFiltering = false;
            if(results.values != null){
                if(whichFilter != null){
                    if(whichFilter.endsWith("COUNTRY")){
                        mMarkersDataListFiltered = filterByCountryName((List) results.values);
                    } else {
                        mMarkersDataListFiltered = (List) results.values;
                    }
                } else {
                    mMarkersDataListFiltered = (List) results.values;
                }

                AppController.runOnUIThread(() -> {
                    Set<MyItem> hs = new HashSet<>();
                    hs.addAll(mMarkersDataListFiltered);
                    mMarkersDataListFiltered.clear();
                    mMarkersDataListFiltered.addAll(hs);
                    adapter.notifyDataSetChanged();
                });
            }
            if(mCompanyFragmentAdapterListener != null)
                mCompanyFragmentAdapterListener.onStarClicked(0, false);
        }
    }

    private List<MyItem> filterByName(List<MyItem> dataName) {
        Collections.sort(dataName, (o1, o2) -> o1.getCompanyName().trim().compareTo(o2.getCompanyName().trim()));
        return dataName;
    }

    private List<MyItem> filterByCountryName(List<MyItem> dataName) {
        Collections.sort(dataName, (o1, o2) -> o1.getCompanyCountry().trim().compareTo(o2.getCompanyCountry().trim()));
        return dataName;
    }

    private List<MyItem> doFiltering(CharSequence constraint, List<MyItem> dataName) {
        List<MyItem> filteredList = new ArrayList<>();
        for(final MyItem item : dataName){
            if(item.getCompanyName() != null && !item.getCompanyName().equalsIgnoreCase("")){
                final String text = item.getCompanyName().toLowerCase().toLowerCase();
                if (text.contains(constraint.toString().toLowerCase())) {
                    filteredList.add(item);
                }
            }

            if(item.getCompanyCountry()!= null && !item.getCompanyCountry().equalsIgnoreCase("")){
                final String countryName = item.getCompanyCountry().toLowerCase();
                if(countryName.startsWith(constraint.toString().toLowerCase())){
                    filteredList.add(item);
                }
            }
        }
        if(filteredList.size() > 0) {
            Collections.sort(filteredList, (itemOne, itemTwo) -> (itemOne.getCompanyName()).compareTo((itemTwo.getCompanyName())));
        }
        Set<MyItem> hs = new HashSet<>();
        hs.addAll(filteredList);
        filteredList.clear();
        filteredList.addAll(hs);
        return filteredList;
    }
}
