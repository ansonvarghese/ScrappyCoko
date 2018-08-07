package com.myscrap;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.EnableNotification;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AboutFragment extends Fragment {

    private Tracker mTracker;
    private Switch mSwitch;

    public AboutFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        TextView version = (TextView) v.findViewById(R.id.version);
        LinearLayout editProfile = (LinearLayout) v.findViewById(R.id.edit_profile_layout);
        LinearLayout changePassword = (LinearLayout) v.findViewById(R.id.change_password_layout);
        LinearLayout tips = (LinearLayout) v.findViewById(R.id.tips_layout);
        LinearLayout generalInformation = (LinearLayout) v.findViewById(R.id.general_information_layout);
        View editView = v.findViewById(R.id.edit_profile_view);
        View passwordView = v.findViewById(R.id.change_password_view);
        final LinearLayout privacy = (LinearLayout) v.findViewById(R.id.privacy_layout);
        final LinearLayout termsAndConditions = (LinearLayout) v.findViewById(R.id.terms_and_condition_layout);
        String versionName = BuildConfig.VERSION_NAME;
        version.setText("Version " +versionName);
        mTracker = AppController.getInstance().getDefaultTracker();
        if(UserUtils.isGuestLoggedIn(getContext())){
            editProfile.setVisibility(View.GONE);
            editView.setVisibility(View.GONE);
            changePassword.setVisibility(View.GONE);
            passwordView.setVisibility(View.GONE);
        }

        mSwitch = (Switch) v.findViewById(R.id.notification_switcher);
        if(UserUtils.isNotificationEnabled(AppController.getInstance())){
            mSwitch.setChecked(true);
        } else {
            mSwitch.setChecked(false);
        }


        mSwitch.post(() -> mSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if(UserUtils.isGuestLoggedIn(AppController.getInstance()))
                return;
            if(CheckNetworkConnection.isConnectionAvailable(getContext())){
                mSwitch.setChecked(isChecked);
                if(isChecked){
                    enableNotification("1");
                    UserUtils.saveNotificationEnable(AppController.getInstance(), "1");
                    Toast.makeText(getContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    enableNotification("0");
                    UserUtils.saveNotificationEnable(AppController.getInstance(), "0");
                    Toast.makeText(getContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
                }
            } else {
                SnackBarDialog.showNoInternetError(mSwitch);
            }
        }));

        tips.setOnClickListener(v1 -> goToTips());
        generalInformation.setOnClickListener(v12 -> goToGeneralInformation());
        privacy.setOnClickListener(v13 -> {
            if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                goToPrivacy();
            else
                SnackBarDialog.showNoInternetError(privacy);
        });
        termsAndConditions.setOnClickListener(v14 -> {
            if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                goToTermsAndCondition();
            else
                SnackBarDialog.showNoInternetError(termsAndConditions);
        });
        return v;
    }

    private void enableNotification(String notification){
        if (CheckNetworkConnection.isConnectionAvailable(getContext())) {
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(getContext());
            apiService.enableNotification(AppController.getInstance().getPrefManager().getUser().getId(), notification, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<EnableNotification>() {
                        @Override
                        public void onCompleted() {
                            Log.d("EnableNotification", "onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("EnableNotification", "Failed to update");
                        }

                        @Override
                        public void onNext(EnableNotification notification) {
                            if (notification != null) {
                                if (!notification.isErrorStatus()) {
                                    if (notification.isNotificationEnabled()) {
                                        UserUtils.saveNotificationEnable(AppController.getInstance(), "1");
                                        Log.d("EnableNotification", "true");
                                    } else {
                                        UserUtils.saveNotificationEnable(AppController.getInstance(), "0");
                                        Log.d("EnableNotification", "false");
                                    }
                                    if(mSwitch != null)
                                        mSwitch.post(() -> mSwitch.setChecked(notification.isNotificationEnabled()));
                                }
                            }
                        }
                    });
        }
    }

    private void goToPrivacy() {
        final Intent intent = new Intent(getContext(), PrivacyPolicyActivity.class);
        getContext().startActivity(intent);
    }

    private void goToTermsAndCondition() {
        final Intent intent = new Intent(getContext(), TermsAndConditionsActivity.class);
        getContext().startActivity(intent);

    }

    private void  goToTips() {
        final Intent intent = new Intent(getContext(), TipsActivity.class);
        intent.putExtra("page", "tips");
        getContext().startActivity(intent);
    }

    private void  goToGeneralInformation() {
        final Intent intent = new Intent(getContext(), TipsActivity.class);
        intent.putExtra("page", "generalInformation");
        getContext().startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
       // UserOnlineStatus.setUserOnline(AppController.getInstance(),UserOnlineStatus.OFFLINE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("About Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        //UserOnlineStatus.setUserOnline(AppController.getInstance(),UserOnlineStatus.ONLINE);
    }
}
