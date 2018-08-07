package com.myscrap;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.imagepipeline.image.ImageInfo;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.ChatRoom;
import com.myscrap.utils.GetImages;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.view.MultiTouchViewPager;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.webservice.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created by Ms2 on 6/22/2016.
 */
public class ImagesSlideshowDialogFragment extends DialogFragment {
    private MultiTouchViewPager viewPager;
    private static final int REQUEST_STORAGE = 1;
    private Tracker mTracker;
    private List<ChatRoom> chatRoom = new ArrayList<>();
    private int displayPosition = 0;
    private Dialog dialog;
    public static ImagesSlideshowDialogFragment newInstance() {
        return new ImagesSlideshowDialogFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_slider, container, false);
        viewPager = (MultiTouchViewPager) v.findViewById(R.id.viewpager);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.my_toolbar);

        mTracker = AppController.getInstance().getDefaultTracker();
        chatRoom =(ArrayList<ChatRoom>) getArguments().getSerializable("images");
        int displayScrollTo = getArguments().getInt("displayPosition");

        if(chatRoom == null)
            chatRoom = new ArrayList<>();

        //chatRoom = (ChatRoom) getArguments().getSerializable("images");
        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        setCurrentItem(displayScrollTo);
        final  ImageView overflow = (ImageView) toolbar.findViewById(R.id.overflow);

       if(chatRoom != null && chatRoom.size() > 0){
           if(chatRoom.get(displayPosition).getMessageChatImage() != null && !chatRoom.get(displayPosition).getMessageChatImage().equalsIgnoreCase("")
                   && chatRoom.get(displayPosition).getMessageType() != null && !chatRoom.get(displayPosition).getMessageType().equalsIgnoreCase("") && chatRoom.get(displayPosition).getMessageType().equalsIgnoreCase("2")){
               overflow.setVisibility(View.VISIBLE);
           } else {
               overflow.setVisibility(View.GONE);
           }
       }

        overflow.setOnClickListener(v1 -> {
            int current = viewPager.getCurrentItem();
            openBottomSheet(chatRoom.get(current).getMessageChatImage());
        });
        ImageView back = (ImageView) toolbar.findViewById(R.id.back);
        back.setOnClickListener(v12 -> {
            if(dialog != null)
                dialog.dismiss();
        });
        setHasOptionsMenu(true);
        return v;
    }

    private void doSave(String image) {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestExternalStoragePermission();
            return;
        }
        if(CheckNetworkConnection.isConnectionAvailable(getContext())){
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "MS_IMG_" + timeStamp;
            if(image != null && !image.equalsIgnoreCase(""))
                new GetImages(image, null, imageFileName).execute() ;
        } else {
            if(viewPager != null)
               SnackBarDialog.showNoInternetError(viewPager);
        }
    }


    private void requestExternalStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(((Activity)getContext()),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE);
        } else {

            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(viewPager != null) {
                    int current = viewPager.getCurrentItem();
                    if(chatRoom.get(current).getMessageChatImage() != null && !chatRoom.get(current).getMessageChatImage().isEmpty()){
                        doSave(Constants.CHAT_IMAGE_URL_PREFIX+chatRoom.get(current).getMessageChatImage());
                    }
                }
            } else {
                Toast.makeText(getContext(), "Storage permission was not granted.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Feed Image Slide Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayPosition = position;
    }

    private ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayPosition = position;
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        return dialog;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }


    public class MyViewPagerAdapter extends PagerAdapter {


        MyViewPagerAdapter() {}
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final PhotoDraweeView mPhotoDraweeView = new PhotoDraweeView(container.getContext());
            Uri imageUri = null;
            if(chatRoom.get(position).getMessageChatImage() != null && !chatRoom.get(position).getMessageChatImage().equalsIgnoreCase("")
                    && chatRoom.get(position).getMessageType() != null && !chatRoom.get(position).getMessageType().equalsIgnoreCase("") && chatRoom.get(position).getMessageType().equalsIgnoreCase("2")){
                imageUri = Uri.parse(Constants.CHAT_IMAGE_URL_PREFIX+chatRoom.get(position).getMessageChatImage());
            } else if(chatRoom.get(position).getMessageChatImage() != null && !chatRoom.get(position).getMessageChatImage().equalsIgnoreCase("")
                    && chatRoom.get(position).getMessageType() != null && !chatRoom.get(position).getMessageType().equalsIgnoreCase("") && chatRoom.get(position).getMessageType().equalsIgnoreCase("12")){
                imageUri= Uri.fromFile(new File(chatRoom.get(position).getMessageChatImage()));  // For files on device
            }
            mPhotoDraweeView.setBackgroundColor(ContextCompat.getColor(mPhotoDraweeView.getContext(), android.R.color.black));
            mPhotoDraweeView.setAdjustViewBounds(true);
            mPhotoDraweeView.getHierarchy().setFailureImage(R.drawable.refresh, ScalingUtils.ScaleType.CENTER_CROP);
            mPhotoDraweeView.getHierarchy().setProgressBarImage(R.drawable.custom_image_progress, ScalingUtils.ScaleType.CENTER);
            mPhotoDraweeView.getHierarchy().setPlaceholderImage(R.color.place_holder_view);
            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setTapToRetryEnabled(true);
            controller.setOldController(mPhotoDraweeView.getController());
            if(imageUri != null)
                controller.setUri(imageUri);
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {

                @Override
                public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                    super.onIntermediateImageSet(id, imageInfo);
                    assert imageInfo != null;
                    mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }

                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null) {
                        return;
                    }
                    mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });

            mPhotoDraweeView.setController(controller.build());

            mPhotoDraweeView.setOnPhotoTapListener((view, x, y) -> {
            });
            mPhotoDraweeView.setOnViewTapListener((view, x, y) -> {
            });

            mPhotoDraweeView.setOnLongClickListener(v -> {
                if(chatRoom.get(position).getMessageChatImage() != null && !chatRoom.get(position).getMessageChatImage().equalsIgnoreCase("")
                        && chatRoom.get(position).getMessageType() != null && !chatRoom.get(position).getMessageType().equalsIgnoreCase("") && chatRoom.get(position).getMessageType().equalsIgnoreCase("2")){
                    openBottomSheet(chatRoom.get(position).getMessageChatImage());
                }

                return true;
            });

            container.addView(mPhotoDraweeView);
            return mPhotoDraweeView;
        }



        @Override
        public int getCount() {
            return chatRoom.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    private void openBottomSheet(final String image) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.bottom_sheet_content, null, false);
        TextView savePhoto = (TextView) v.findViewById(R.id.save);
        LinearLayout reportLayout = (LinearLayout) v.findViewById(R.id.report_layout);
        final Dialog mBottomSheetDialog = new Dialog (getContext(),
                R.style.MaterialDialogSheet);
        mBottomSheetDialog.setContentView (v);
        mBottomSheetDialog.setCancelable (true);
        if (mBottomSheetDialog.getWindow() != null) {
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow ().setGravity (Gravity.BOTTOM);
            mBottomSheetDialog.show ();
        }

        if(AppController.getInstance().getPrefManager().getUser() == null)
            return;
        reportLayout.setVisibility(View.GONE);
        savePhoto.setOnClickListener(v1 -> {
            doSave(Constants.CHAT_IMAGE_URL_PREFIX+chatRoom.get(displayPosition).getMessageChatImage());
            mBottomSheetDialog.dismiss();
        });

    }
}
