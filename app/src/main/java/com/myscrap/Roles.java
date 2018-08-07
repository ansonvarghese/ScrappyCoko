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
public class Roles extends Fragment {

    private List<String> selectedRolesList = new ArrayList<>();
    private List<String> interestRolesData = new ArrayList<>();
    private Tracker mTracker;

    public Roles() {
    }

    @SuppressLint("ValidFragment")
    public Roles(List<String> mSelectedRolesList, List<String> mInterestRoleData) {
        selectedRolesList = mSelectedRolesList;
        interestRolesData= mInterestRoleData;
        }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_roles, container, false);
        LinearLayout roles = (LinearLayout) mView.findViewById(R.id.role);
        FlowLayout flowLayout = new FlowLayout(getContext());
        int padding= DeviceUtils.dp2px(getContext(),13);
        flowLayout.setPadding(padding,padding,padding,padding);
        if(interestRolesData != null && selectedRolesList != null && selectedRolesList.size() > 0){
            for (int i=0; i < selectedRolesList.size(); i++ ){
                TextView textView = new TextView(getContext());
                textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.interest_selected));
                textView.setText(interestRolesData.get(Integer.parseInt(UserUtils.parsingInteger(selectedRolesList.get(i).trim()))));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.WHITE);
                textView.setPadding(10,15,10,15);
                flowLayout.addView(textView);
            }
            roles.removeAllViews();
            roles.addView(flowLayout);
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Roles Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
