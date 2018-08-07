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

public class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.ItemViewHolder> {

    public InterestAdapter.InterestAdapterClickListener mListener;
    private List<String> mInterestData = new ArrayList<>();
    private Context mContext;
    private List<Integer> mSelectedArray;
    private boolean mClickable;

    public InterestAdapter(Context context, List<String> interestData, InterestAdapterClickListener listener, List<Integer> selectedArray, boolean clickable) {
        this.mContext = context;
        this.mInterestData = interestData;
        this.mListener = listener;
        this.mClickable = clickable;
        this.mSelectedArray = selectedArray;
    }


    public void swap(List<Integer> selectedArray){
        this.mSelectedArray = selectedArray;
        this.notifyDataSetChanged();
    }



    @Override
    public InterestAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_interests, parent, false);
        return new ItemViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final InterestAdapter.ItemViewHolder holder, int position) {
            holder.interest.setText(mInterestData.get(position));

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
                    mListener.onInterestClicked(holder.interest, holder.getAdapterPosition());
                } else {
                    v.setTag("click");
                    mListener.onInterestClicked(v, holder.getAdapterPosition());
                    holder.interest.setBackground(ContextCompat.getDrawable(mContext, R.drawable.interest));
                    holder.interest.setTextColor(ContextCompat.getColor(mContext, R.color.subPrimaryText));
                }
            }


        });
    }

    @Override
    public int getItemCount() {
        return mInterestData.size();
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

    public interface  InterestAdapterClickListener {
        void onInterestClicked(View v, int position);
    }
}

