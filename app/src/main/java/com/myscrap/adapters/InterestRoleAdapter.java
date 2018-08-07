package com.myscrap.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myscrap.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ms3 on 5/19/2017.
 */

public class InterestRoleAdapter extends RecyclerView.Adapter<InterestRoleAdapter.ItemViewHolder> {

    public InterestRoleAdapter.InterestRoleAdapterClickListener mListener;
    private List<String> mInterestRolesData = new ArrayList<>();
    private Context mContext;
    private List<Integer> mSelectedArray;
    private boolean mClickable;

    public InterestRoleAdapter(Context context, List<String> interestData, InterestRoleAdapterClickListener listener, List<Integer> selectedArray, boolean clickable) {
        this.mContext = context;
        this.mInterestRolesData = interestData;
        this.mListener = listener;
        this.mClickable = clickable;
        this.mSelectedArray = selectedArray;
    }


    public void swap(List<Integer> selectedArray){
        this.mSelectedArray = selectedArray;
        this.notifyDataSetChanged();
    }



    @Override
    public InterestRoleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_interests, parent, false);
        return new ItemViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final InterestRoleAdapter.ItemViewHolder holder, int position) {
            holder.interest.setText(mInterestRolesData.get(position));

        if (mSelectedArray.size() > 0){
            if(mSelectedArray.contains(position)){
                holder.interest.setTag("clicked");
                holder.interest.setBackground(ContextCompat.getDrawable(mContext, R.drawable.interest_selected));
                holder.interest.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                holder.interest.setVisibility(View.VISIBLE);
            } else {
                if(!mClickable){
                    holder.interest.setVisibility(View.GONE);
                }
            }
        }

        holder.interest.setOnClickListener(v -> {
            if(mClickable){
                if(v.getTag().equals("click")){
                    holder.interest.setTag("clicked");
                    holder.interest.setBackground(ContextCompat.getDrawable(mContext, R.drawable.interest_selected));
                    holder.interest.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mListener.onInterestRoleClicked(holder.interest, holder.getAdapterPosition());
                } else {
                    v.setTag("click");
                    mListener.onInterestRoleClicked(v, holder.getAdapterPosition());
                    holder.interest.setBackground(ContextCompat.getDrawable(mContext, R.drawable.interest));
                    holder.interest.setTextColor(ContextCompat.getColor(mContext, R.color.subPrimaryText));
                }
            }


        });
    }

    @Override
    public int getItemCount() {
        return mInterestRolesData.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView interest;
        public ItemViewHolder(View itemView) {
            super(itemView);
            interest = (TextView) itemView.findViewById(R.id.item);
        }
    }

    public boolean contains(final int[] array, final int key) {
        Arrays.sort(array);
        return Arrays.binarySearch(array, key) >= 0;
    }

    public interface  InterestRoleAdapterClickListener {
        void onInterestRoleClicked(View v, int position);
    }
}

