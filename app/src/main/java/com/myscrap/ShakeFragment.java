package com.myscrap;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.ShakeFriend;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.ShakeEventManager;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.wang.avi.AVLoadingIndicatorView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ms3 on 4/27/2017.
 */

public class ShakeFragment extends Fragment implements ShakeEventManager.ShakeListener{

    private ShakeEventManager sd;
    private View shakeView;
    private boolean isShaking = false;
    private LinearLayout shakeViewUserLayout;
    private SimpleDraweeView shakeUserImage;
    private ImageView shakeImage;
    private TextView iconText, online, shakeViewTextView, shakeUserName, shakeUserDesignation;
    private ShakeFriend shakeFriend;
    private ShakeFriend.ShakeFriendData shakeFriendData;
    private Vibrator mVibrator;
    private AVLoadingIndicatorView animationView;
    private Tracker mTracker;

    public ShakeFragment() {}


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mTracker = AppController.getInstance().getDefaultTracker();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        shakeView  = inflater.inflate(R.layout.fragment_shake, container, false);
        shakeViewUserLayout = (LinearLayout) shakeView.findViewById(R.id.shakeViewUserLayout);
        shakeUserImage = (SimpleDraweeView) shakeView.findViewById(R.id.icon_profile);
        iconText = (TextView) shakeView.findViewById(R.id.icon_text);
        online = (TextView) shakeView.findViewById(R.id.online);
        shakeImage = (ImageView) shakeView.findViewById(R.id.shakeView);
        animationView = (AVLoadingIndicatorView) shakeView.findViewById(R.id.avi);

        shakeViewTextView = (TextView) shakeView.findViewById(R.id.shakeTextView);
        shakeUserName = (TextView) shakeView.findViewById(R.id.user_name);
        shakeUserDesignation = (TextView) shakeView.findViewById(R.id.user_designation);

        if(shakeViewUserLayout != null) {
            shakeViewUserLayout.setOnClickListener(v -> {
                if(shakeFriendData != null)
                    goToProfile(shakeFriendData.getUserId());
            });
        }

        if(shakeUserImage != null) {
            shakeUserImage.setOnClickListener(v -> {
                if(shakeFriendData != null)
                    goToProfile(shakeFriendData.getUserId());
            });
        }

        if(shakeUserName != null) {
            shakeUserName.setOnClickListener(v -> {
                if(shakeFriendData != null)
                    goToProfile(shakeFriendData.getUserId());
            });
        }

        if(shakeUserDesignation != null) {
            shakeUserDesignation.setOnClickListener(v -> {
                if(shakeFriendData != null)
                    goToProfile(shakeFriendData.getUserId());
            });
        }

        return shakeView;
    }

    private void goToProfile(String friendId) {
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())) {
            Intent intent = new Intent(getContext(), UserFriendProfileActivity.class);
            intent.putExtra("friendId", friendId);
            startActivity(intent);
        }  else {
            if(shakeView != null)
                SnackBarDialog.show(shakeView, "No internet connection available");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sd = new ShakeEventManager();
        sd.setListener(this);
        sd.init(view.getContext());
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onShake() {
            if (CheckNetworkConnection.isConnectionAvailable(getContext())){
                if(!isShaking){
                    if(mVibrator == null)
                        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    if (mVibrator != null) {
                        mVibrator.vibrate(500);
                    }
                    getShake();
                }

            } else {
                if(shakeView != null)
                    SnackBarDialog.showNoInternetError(shakeView);
            }

    }

    private void getShake() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(shakeViewUserLayout != null)
            shakeViewUserLayout.setVisibility(View.GONE);

        if(shakeImage != null){
            shakeImage.setVisibility(View.GONE);
        }
        if(animationView != null){
            animationView.show();
        }
        if(shakeViewTextView != null){
            shakeViewTextView.setVisibility(View.VISIBLE);
        }
       ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String apiKey = UserUtils.getApiKey(getActivity());
        Call<ShakeFriend> call = apiService.shake(userId, apiKey);
        call.enqueue(new Callback<ShakeFriend>() {
            @Override
            public void onResponse(@NonNull Call<ShakeFriend> call, @NonNull Response<ShakeFriend> response) {

                if (response.body() != null) {
                    shakeFriend = response.body();
                    if (shakeFriend != null){
                        if(!shakeFriend.isErrorStatus()){
                            if(shakeFriend.getData() != null && shakeFriend.getData().size() > 0)
                                loadData(shakeFriend.getData().get(0));
                        } else {
                            hideAnimation();
                        }
                    }
                    hideAnimation();
                    Log.d("getShake", "onSuccess");
                }

                isShaking = false;
            }
            @Override
            public void onFailure(@NonNull Call<ShakeFriend> call, @NonNull Throwable t) {
                Log.d("getShake", "onFailure");
                hideAnimation();
                isShaking = false;
            }
        });
    }

    private void hideAnimation() {
        if(animationView != null){
            animationView.hide();
        }
        if(shakeViewTextView != null){
            shakeViewTextView.setVisibility(View.GONE);
        }
        if(shakeImage != null){
            shakeImage.setVisibility(View.VISIBLE);
        }
    }


    public void  playSound(){
        MediaPlayer mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.shake);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.start();
    }



    @Override
    public void onResume() {
        super.onResume();
        if(sd != null)
            sd.register();
        if(mTracker != null){
            mTracker.setScreenName("Shake Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

    }

    private void loadData(ShakeFriend.ShakeFriendData shakeFriend) {
        if (shakeFriend != null) {
            shakeFriendData = shakeFriend;
            if(shakeViewUserLayout != null){
                hideAnimation();
                shakeViewUserLayout.setVisibility(View.VISIBLE);

                if(shakeFriend.getUserName() != null)
                    shakeUserName.setText(shakeFriend.getUserName());

                if(shakeFriend.isOnline()){
                    online.setVisibility(View.VISIBLE);
                } else {
                    online.setVisibility(View.GONE);
                }

                if(shakeFriend.getUserProfile() != null){
                    String image = shakeFriend.getUserProfile();
                    if (image != null && !image.equalsIgnoreCase("")){
                        if(image.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                || image.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                            shakeUserImage.setImageResource(R.drawable.bg_circle);
                            shakeUserImage.setColorFilter(DeviceUtils.getRandomMaterialColor(getContext(), "400"));
                            iconText.setVisibility(View.VISIBLE);
                            if (!image.equalsIgnoreCase("")){
                                String[] split = shakeFriend.getUserName().split("\\s+");
                                if (split.length > 1){
                                    String first = split[0].substring(0,1);
                                    String last = split[1].substring(0,1);
                                    String initial = first + ""+ last ;
                                    iconText.setText(initial.toUpperCase());
                                } else {
                                    if (split[0] != null && split[0].trim().length() >= 1) {
                                        String first = split[0].substring(0, 1);
                                        iconText.setText(first.toUpperCase());
                                    }
                                }
                            }
                        } else {
                            Uri uri = Uri.parse(image);
                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                            shakeUserImage.setHierarchy(new GenericDraweeHierarchyBuilder(getContext().getResources())
                                    .setRoundingParams(roundingParams)
                                    .build());
                            roundingParams.setRoundAsCircle(true);
                            shakeUserImage.setImageURI(uri);
                            shakeUserImage.setColorFilter(null);
                            iconText.setVisibility(View.GONE);
                        }
                    }
                }
                if(shakeFriend.getUserDistance() != null){
                    String distance = shakeFriend.getUserDistance() + shakeFriend.getDistanceUnit() +" away";
                    shakeUserDesignation.setText(distance);
                }

                playSound();
                Animation slideDown = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                        R.anim.slide_down);
                shakeViewUserLayout.startAnimation(slideDown);
            }
        } else {
            if(shakeViewUserLayout != null)
                shakeViewUserLayout.setVisibility(View.GONE);
        }
    }

    public void onPause() {
        super.onPause();
        if(sd != null)
            sd.deregister();

    }

    @Override
    public void onStop() {
        super.onStop();
        if(sd != null)
            sd.deregister();
    }
}
