package com.myscrap;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.FlowLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Commodity extends Fragment {


    private List<String> selectedList = new ArrayList<>();
    private List<String> interestData = new ArrayList<>();
    private Tracker mTracker;

    public Commodity() {
    }
     @SuppressLint("ValidFragment")
     public Commodity(List<String> mSelectedList, List<String> mInterestData) {
         selectedList = mSelectedList;
         interestData = mInterestData;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_commodity, container, false);
        LinearLayout commodity = (LinearLayout) mView.findViewById(R.id.interest);
        FlowLayout flowLayout = new FlowLayout(getContext());
        int padding= DeviceUtils.dp2px(getContext(),13);
        flowLayout.setPadding(padding,padding,padding,padding);
        if(interestData != null && selectedList != null && selectedList.size() > 0){
            for (int i=0; i < selectedList.size(); i++ ){
                TextView textView = new TextView(getContext());
                textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.interest_selected));
                textView.setText(interestData.get(Integer.parseInt(UserUtils.parsingInteger(selectedList.get(i).trim()))));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.WHITE);
                textView.setPadding(10,15,10,15);
                flowLayout.addView(textView);
            }
            commodity.removeAllViews();
            commodity.addView(flowLayout);
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Commodity Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
