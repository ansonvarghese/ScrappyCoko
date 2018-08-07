package com.myscrap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.imagepipeline.image.ImageInfo;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.LikedData;
import com.myscrap.model.PictureUrl;
import com.myscrap.model.Report;
import com.myscrap.utils.GetImages;
import com.myscrap.utils.GuestLoginDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.MultiTouchViewPager;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.relex.photodraweeview.PhotoDraweeView;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Ms2 on 6/22/2016.
 */
public class CompanyImagesSlideshowDialogFragment extends DialogFragment {
    private MultiTouchViewPager viewPager;
    private PhotoDraweeView mPhotoDraweeView;
    private List<PictureUrl> pictureUrlList = new ArrayList<>();
    private int currentPosition = 0;
    private TextView tsLikesCounter;
    private TextView tsCommentCounter;
    private TextView tsLikesCommentDot;
    private ImageView favouriteIcon, commentIcon;
    private TextView likeText, commentText;
    private LinearLayout bottomLayout;
    private MyViewPagerAdapter myViewPagerAdapter;
    private RelativeLayout inActiveEntireLayout;
    private ImageView btnReportBottom;
    private String image;
    private static final int REQUEST_STORAGE = 1;
    private Tracker mTracker;


    public static CompanyImagesSlideshowDialogFragment newInstance() {
        return new CompanyImagesSlideshowDialogFragment();
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_company_image_slider, container, false);
        viewPager = (MultiTouchViewPager) v.findViewById(R.id.viewpager);
        final Toolbar toolbar = (Toolbar) v.findViewById(R.id.my_toolbar);
        final  ImageView overflow = (ImageView) toolbar.findViewById(R.id.overflow);
        overflow.setOnClickListener(v1 -> openBottomSheet());
        mTracker = AppController.getInstance().getDefaultTracker();
        btnReportBottom = (ImageView) v.findViewById(R.id.ic_report_bottom);
        bottomLayout = (LinearLayout) v.findViewById(R.id.bottom_layout);
        inActiveEntireLayout = (RelativeLayout) v.findViewById(R.id.overall_active_layout);
        bottomLayout.setTag("open");
        tsLikesCounter = (TextView) v.findViewById(R.id.tsLikesCounter);
        tsCommentCounter = (TextView) v.findViewById(R.id.tsCommentsCounter);
        tsLikesCommentDot = (TextView) v.findViewById(R.id.dot);
        favouriteIcon = (ImageView) v.findViewById(R.id.user_like_icon);
        commentIcon = (ImageView) v.findViewById(R.id.user_comment_icon);
        likeText = (TextView) v.findViewById(R.id.like_text);
        commentText = (TextView) v.findViewById(R.id.comment_text);
        pictureUrlList =(ArrayList<PictureUrl>) getArguments().getSerializable("images");
        int position = getArguments().getInt("position");
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        setCurrentItem(position);

        return v;
    }



    private void showReportPopupMenu(final View v, final PictureUrl feedItem, final int itemPosition) {
        if (getContext() != null) {
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setMessage("Are you sure you want to report this post?");
            dialogBuilder.setCancelable(true);
            dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                doReport(feedItem.getPostid(), feedItem.getUserId());
                if(pictureUrlList != null && pictureUrlList.size() > 0 ){
                    Toast.makeText(getContext(), "Post Reported", Toast.LENGTH_SHORT).show();
                    feedItem.setReported(true);
                    feedItem.setReportedUserId(AppController.getInstance().getPrefManager().getUser().getId());
                    displayMetaInfo(itemPosition);
                }
            });

            dialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Company Image Slide Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayMetaInfo(position);
    }

    private void doSave() {
        if (getContext() != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestExternalStoragePermission();
                return;
            }

            if(CheckNetworkConnection.isConnectionAvailable(getContext())){
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "MS_IMG_" + timeStamp;
                if(image != null && !image.equalsIgnoreCase(""))
                    new GetImages(image, mPhotoDraweeView, imageFileName).execute() ;
                else
                    SnackBarDialog.show(mPhotoDraweeView, "Failed to download.");
            } else {
                SnackBarDialog.showNoInternetError(mPhotoDraweeView);
            }
        }
    }

    private void requestExternalStoragePermission() {

        if (getContext() != null) {
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doSave();
            } else {
                Toast.makeText(getContext(), "Storage permission was not granted.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void displayMetaInfo(final int position) {
        if (pictureUrlList != null && pictureUrlList.size() > 0) {
            if(pictureUrlList.get(position).isReported()){
                if(pictureUrlList.get(position).getReportedUserId() != null && pictureUrlList.get(position).getUserId() != null){
                    if(pictureUrlList.get(position).getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        inActiveEntireLayout.setVisibility(View.VISIBLE);
                        inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                        btnReportBottom.setVisibility(View.VISIBLE);
                    } else if(pictureUrlList.get(position).getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                        inActiveEntireLayout.setVisibility(View.VISIBLE);
                        inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                        btnReportBottom.setVisibility(View.VISIBLE);
                    } else {
                        inActiveEntireLayout.setVisibility(View.VISIBLE);
                        inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                        btnReportBottom.setVisibility(View.GONE);
                    }
                }
            } else {
                inActiveEntireLayout.setVisibility(View.GONE);
            }

            btnReportBottom.setOnClickListener(v -> {
                if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                    showInactiveReportPopupMenu(v, pictureUrlList.get(position), position);
                else
                    SnackBarDialog.showNoInternetError(v);
            });

            if(tsCommentCounter != null){
                if(pictureUrlList.get(position).getCommentCount()==0) {
                    tsCommentCounter.setText(getContext().getString(R.string.feed_comment));
                } else {
                    tsCommentCounter.setText(getContext().getResources().getQuantityString(
                            R.plurals.comments_count, pictureUrlList.get(position).getCommentCount(), pictureUrlList.get(position).getCommentCount()
                    ));
                }
            }

            if(tsLikesCounter != null){
                if(pictureUrlList.get(position).getLikeCount()==0) {
                    tsLikesCounter.setText(getContext().getString(R.string.feed_like));
                } else {
                    tsLikesCounter.setText(getContext().getResources().getQuantityString(
                            R.plurals.likes_count, pictureUrlList.get(position).getLikeCount(), pictureUrlList.get(position).getLikeCount()
                    ));
                }
            }

            if(pictureUrlList.get(position).getLikeCount() > 0 && pictureUrlList.get(position).getCommentCount() > 0){
                tsLikesCounter.setVisibility(View.VISIBLE);
                tsCommentCounter.setVisibility(View.VISIBLE);
                tsLikesCommentDot.setVisibility(View.VISIBLE);
            } else if (pictureUrlList.get(position).getLikeCount() > 0 && pictureUrlList.get(position).getCommentCount() ==  0){
                tsLikesCounter.setVisibility(View.VISIBLE);
                tsCommentCounter.setVisibility(View.GONE);
                tsLikesCommentDot.setVisibility(View.GONE);
            } else if (pictureUrlList.get(position).getLikeCount() == 0 && pictureUrlList.get(position).getCommentCount() >  0){
                tsLikesCounter.setVisibility(View.GONE);
                tsCommentCounter.setVisibility(View.VISIBLE);
                tsLikesCommentDot.setVisibility(View.GONE);
            } else {
                tsLikesCounter.setVisibility(View.GONE);
                tsCommentCounter.setVisibility(View.GONE);
                tsLikesCommentDot.setVisibility(View.GONE);
            }
        }
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        final String userId = AppController.getInstance().getPrefManager().getUser().getId();

        tsLikesCounter.setOnClickListener(v -> {
            if(UserUtils.isGuestLoggedIn(getContext())){
                GuestLoginDialog.show(getActivity());
                return;
            }
            if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                screenMoveToLikeActivity(pictureUrlList.get(position).getPostid(), userId, pictureUrlList.get(position).getLikeCount());
            else
                SnackBarDialog.showNoInternetError(tsLikesCounter);
        });
        tsCommentCounter.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                screenMoveToCommentActivity(pictureUrlList.get(position).getPostid(), userId);
            else
                SnackBarDialog.showNoInternetError(commentText);
        });

        commentText.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                screenMoveToCommentActivity(pictureUrlList.get(position).getPostid(), userId);
            else
                SnackBarDialog.showNoInternetError(commentText);
        });
        commentIcon.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                screenMoveToCommentActivity(pictureUrlList.get(position).getPostid(), userId);
            else
                SnackBarDialog.showNoInternetError(commentIcon);
        });


        if(pictureUrlList.get(position).isLikeStatus()){
            favouriteIcon.setImageResource(R.drawable.ic_heart_outline_black_filled);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                favouriteIcon.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.colorPrimary));
            }
        } else {
            favouriteIcon.setImageResource(R.drawable.ic_heart_outline_black);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                favouriteIcon.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.white));
            }
        }


        image = pictureUrlList.get(position).getImages();

        favouriteIcon.setOnClickListener(v -> {
            if (pictureUrlList != null && pictureUrlList.size() > 0) {
                if (!pictureUrlList.get(position).isLikeStatus()) {
                    favouriteIcon.setImageResource(R.drawable.ic_heart_outline_black_filled);
                    favouriteIcon.setColorFilter(null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        favouriteIcon.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.colorPrimary));
                    }
                    pictureUrlList.get(position).setLikeStatus(true);

                    int count = pictureUrlList.get(position).getLikeCount() + 1;
                    pictureUrlList.get(position).setLikeCount(count);

                    if (pictureUrlList.get(position).getLikeCount() != 0 && pictureUrlList.get(position).getLikeCount() > 0 ) {
                        if (pictureUrlList.get(position).getLikeCount() == 1) {
                            tsLikesCounter.setText(pictureUrlList.get(position).getLikeCount() +" Like" );
                            likeText.setText(" LIKE");
                        } else if (pictureUrlList.get(position).getLikeCount() > 1) {
                            tsLikesCounter.setText(pictureUrlList.get(position).getLikeCount() +" Likes" );
                            likeText.setText(" LIKE");
                        }
                        tsLikesCounter.setVisibility(View.VISIBLE);
                    } else {
                        tsLikesCounter.setVisibility(View.GONE);
                        likeText.setText(" LIKE");
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                        doLike(pictureUrlList.get(position));
                    else
                        SnackBarDialog.showNoInternetError(favouriteIcon);
                } else {
                    pictureUrlList.get(position).setLikeStatus(false);
                    favouriteIcon.setImageResource(R.drawable.ic_heart_outline_black);
                    favouriteIcon.setColorFilter(null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        favouriteIcon.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.white));
                    }
                    favouriteIcon.setColorFilter(null);
                    int count = pictureUrlList.get(position).getLikeCount() - 1;
                    pictureUrlList.get(position).setLikeCount(count);
                    if (pictureUrlList.get(position).getLikeCount() != 0 && pictureUrlList.get(position).getLikeCount() > 0 ) {
                        if (pictureUrlList.get(position).getLikeCount() == 1) {
                            tsLikesCounter.setText(pictureUrlList.get(position).getLikeCount() + " Like");
                            likeText.setText(" LIKE");
                        } else if (pictureUrlList.get(position).getLikeCount() > 1) {
                            tsLikesCounter.setText(pictureUrlList.get(position).getLikeCount() + " Likes");
                            likeText.setText(" LIKE");
                        }
                        tsLikesCounter.setVisibility(View.VISIBLE);
                    } else {
                        tsLikesCounter.setVisibility(View.GONE);
                        likeText.setText(" LIKE");
                    }
                    if(CheckNetworkConnection.isConnectionAvailable(getContext()))
                        doLike(pictureUrlList.get(position));
                    else
                        SnackBarDialog.showNoInternetError(favouriteIcon);
                }
            }
        });

    }

    private void showInactiveReportPopupMenu(final View v, final PictureUrl feedItem, final int itemPosition) {

        if (getActivity() != null) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            if(feedItem.getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                inflater.inflate(R.menu.un_report, popup.getMenu());
            } else {
                inflater.inflate(R.menu.report_contact_moderator, popup.getMenu());
            }

            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.action_un_report){
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setMessage("Are you sure you want to undo report this post?");
                    dialogBuilder.setCancelable(true);
                    dialogBuilder.setPositiveButton("YES", (dialog, which) -> {
                        undoReport(feedItem.getReportId());
                        if(pictureUrlList != null && pictureUrlList.size() > 0){
                            Toast.makeText(getActivity(), "Post Un Reported", Toast.LENGTH_SHORT).show();
                            feedItem.setReported(false);
                            feedItem.setReportedUserId(AppController.getInstance().getPrefManager().getUser().getId());
                            displayMetaInfo(itemPosition);
                        }
                    });

                    dialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                } else if(item.getItemId() == R.id.action_report_contact_moderator){
                    String moderatorEmailAddress = "support@myscrap.com";
                    Intent emailApp = new Intent(Intent.ACTION_SEND);
                    emailApp.putExtra(Intent.EXTRA_EMAIL, new String[]{moderatorEmailAddress});
                    emailApp.putExtra(Intent.EXTRA_SUBJECT, "Your post is hidden, Please contact moderator.");
                    emailApp.putExtra(Intent.EXTRA_TEXT, "");
                    emailApp.setType("message/rfc822");
                    startActivity(Intent.createChooser(emailApp, "Send Email Via"));
                }
                return true;
            });
            popup.show();
        }
    }

    private void undoReport(String reportId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        Call<Report> call = apiService.undoReportPost(reportId, apiKey);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull retrofit2.Response<Report> response) {
                if(response.body() != null && response.isSuccessful()){
                    Report mReportPost = response.body();
                    if(mReportPost != null && !mReportPost.isErrorStatus()){
                        Log.d("undoReport", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                Log.d("undoReport", "onFailure");
            }
        });
    }

    private void doReport(String postId, String postedUserId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<Report> call = apiService.reportPost(userId, postId, postedUserId, "", apiKey);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull retrofit2.Response<Report> response) {
                if(response.body() != null && response.isSuccessful()){
                    Report mReportPost = response.body();
                    if(mReportPost != null && !mReportPost.isErrorStatus()){
                        Log.d("doReport", "onSuccess");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                Log.d("doReport", "onFailure");
            }
        });
    }

    private void screenMoveToLikeActivity(String postId, String postedUserId, int likeCount) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if (getContext() != null) {
            Intent mIntent = new Intent(getContext(), LikeActivity.class);
            mIntent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
            mIntent.putExtra("postedUserId", postedUserId);
            mIntent.putExtra("postId", postId);
            mIntent.putExtra("count", likeCount);
            mIntent.putExtra("apiKey", UserUtils.getApiKey(getContext()));
            getContext().startActivity(mIntent);
        }

    }

    private void screenMoveToCommentActivity(String postId, String postedUserId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        final Intent intent = new Intent(getContext(), CommentActivity.class);
        intent.putExtra("userId", AppController.getInstance().getPrefManager().getUser().getId());
        intent.putExtra("postId", postId);
        intent.putExtra("postedUserId", postedUserId);
        intent.putExtra("apiKey", UserUtils.getApiKey(getContext()));
        startActivity(intent);

    }

    private void doLike(PictureUrl pictureUrl) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<LikedData> call = apiService.insertLike(userId, pictureUrl.getPostid(), pictureUrl.getUserId(),apiKey);
        call.enqueue(new Callback<LikedData>() {
            @Override
            public void onResponse(@NonNull Call<LikedData> call, @NonNull retrofit2.Response<LikedData> response) {
                Log.d("doLike", "onSuccess");
                if(response.body() != null && response.isSuccessful()){
                    LikedData mLikedData = response.body();
                    if(mLikedData != null && !mLikedData.getError()){
                        LikedData.InsertLikeData  data = mLikedData.getInsertLikeData();
                        if(data != null) {
                            if(pictureUrlList != null) {
                                int i = 0;
                                for(PictureUrl  feedItem : pictureUrlList){
                                    if(feedItem.getPostid().equalsIgnoreCase(data.getPostId())){
                                        feedItem.setLikeStatus(data.getLikeStatus());
                                        feedItem.setLikeCount(Integer.parseInt(UserUtils.parsingInteger(data.getLikeCount())));
                                        pictureUrlList.set(i, feedItem);
                                        if(myViewPagerAdapter != null){
                                            myViewPagerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                    i++;
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<LikedData> call, @NonNull Throwable t) {
                Log.d("doLike", "onFailure");
            }
        });
    }

    private ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            setCurrentItem(position);
            currentPosition = position;
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
        if (getActivity() != null) {
            final RelativeLayout root = new RelativeLayout(getActivity());
            root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(root);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            }
            return dialog;
        }
        return null;
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

        private LayoutInflater layoutInflater;
        ProgressBar mProgressBar;
        private RelativeLayout inActiveEntireLayout;
        private ImageView btnReportBottom;

        MyViewPagerAdapter() {}
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {

            if (getActivity() != null) {
                layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = null;
                if (layoutInflater != null) {
                    view = layoutInflater.inflate(R.layout.fullscreen_company_image_preview, container, false);
                    mPhotoDraweeView = (PhotoDraweeView) view.findViewById(R.id.pinchZoomImageView);
                    mProgressBar = (ProgressBar) view.findViewById(R.id.circular_progress);
                    btnReportBottom = (ImageView) view.findViewById(R.id.ic_report_bottom);
                    inActiveEntireLayout = (RelativeLayout) view.findViewById(R.id.overall_active_layout);
                }


                Uri uri = Uri.parse(pictureUrlList.get(position).getImages());
                mPhotoDraweeView.setBackgroundColor(ContextCompat.getColor(mPhotoDraweeView.getContext(), android.R.color.black));
                mPhotoDraweeView.setAdjustViewBounds(true);
                mPhotoDraweeView.getHierarchy().setFailureImage(R.drawable.refresh, ScalingUtils.ScaleType.CENTER_CROP);
                mPhotoDraweeView.getHierarchy().setProgressBarImage(new ProgressBarDrawable(), ScalingUtils.ScaleType.CENTER);
                mPhotoDraweeView.getHierarchy().setPlaceholderImage(R.drawable.empty_album_placeholder, ScalingUtils.ScaleType.CENTER);
                PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
                controller.setTapToRetryEnabled(true);
                controller.setOldController(mPhotoDraweeView.getController());
                if(uri != null)
                    controller.setUri(uri);
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

                mPhotoDraweeView.setOnPhotoTapListener((v, x, y) -> {
                    if (bottomLayout.getTag().equals("open")){
                        bottomLayout.setTag("close");
                        bottomLayout.setVisibility(View.GONE);
                    } else {
                        bottomLayout.setTag("open");
                        bottomLayout.setVisibility(View.VISIBLE);
                    }
                });
                mPhotoDraweeView.setOnViewTapListener((v, x, y) -> {
                });

                mPhotoDraweeView.setOnLongClickListener(v -> {
                    openBottomSheet();
                    return true;
                });

                if(pictureUrlList.get(position).isReported()){
                    if(pictureUrlList.get(position).getReportedUserId() != null && pictureUrlList.get(position).getUserId() != null){
                        if(pictureUrlList.get(position).getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                            inActiveEntireLayout.setVisibility(View.VISIBLE);
                            inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                            btnReportBottom.setVisibility(View.VISIBLE);
                        } else if(pictureUrlList.get(position).getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                            inActiveEntireLayout.setVisibility(View.VISIBLE);
                            inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                            btnReportBottom.setVisibility(View.VISIBLE);
                        } else {
                            inActiveEntireLayout.setVisibility(View.VISIBLE);
                            inActiveEntireLayout.setOnTouchListener((v, event) -> true);
                            btnReportBottom.setVisibility(View.GONE);
                        }
                    }
                } else {
                    inActiveEntireLayout.setVisibility(View.GONE);
                }

                container.addView(view);
                return view;
            }
            return null;
        }



        @Override
        public int getCount() {
            return pictureUrlList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

    }

    private void openBottomSheet() {
        if (getActivity() != null) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View v = inflater.inflate(R.layout.bottom_sheet_content, null, false);
            TextView savePhoto = (TextView) v.findViewById(R.id.save);
            TextView reportPhoto = (TextView) v.findViewById(R.id.report);
            LinearLayout reportLayout = (LinearLayout) v.findViewById(R.id.report_layout);
            final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog (getActivity(), R.style.MaterialDialogSheet);
            mBottomSheetDialog.setContentView (v);
            mBottomSheetDialog.setCancelable (true);
            if (mBottomSheetDialog.getWindow () == null)
                return;
            mBottomSheetDialog.getWindow ().setLayout (LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow ().setGravity (Gravity.BOTTOM);
            mBottomSheetDialog.show ();

            savePhoto.setOnClickListener(v1 -> {
                doSave();
                mBottomSheetDialog.dismiss();
            });

            if(AppController.getInstance().getPrefManager().getUser() == null)
                return;

            if(!pictureUrlList.get(currentPosition).getUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                reportLayout.setVisibility(View.VISIBLE);
            } else {
                reportLayout.setVisibility(View.GONE);
            }

            reportPhoto.setOnClickListener(v12 -> {
                if(pictureUrlList != null && viewPager != null){
                    if(!pictureUrlList.get(currentPosition).isReported()){
                        showReportPopupMenu(viewPager, pictureUrlList.get(currentPosition), currentPosition);
                    } else {
                        if(pictureUrlList.get(currentPosition).getReportedUserId().equalsIgnoreCase(AppController.getInstance().getPrefManager().getUser().getId())){
                            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage("Are you sure you want to un report this post?");
                            dialogBuilder.setCancelable(true);
                            dialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    undoReport(pictureUrlList.get(currentPosition).getReportId());
                                    if(pictureUrlList != null && pictureUrlList.size() > 0){
                                        Toast.makeText(getActivity(), "Post Un Reported", Toast.LENGTH_SHORT).show();
                                        pictureUrlList.get(currentPosition).setReported(false);
                                        pictureUrlList.get(currentPosition).setReportedUserId(AppController.getInstance().getPrefManager().getUser().getId());
                                        displayMetaInfo(currentPosition);
                                    }
                                }
                            });

                            dialogBuilder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
                            AlertDialog dialog = dialogBuilder.create();
                            dialog.show();
                        } else {
                            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage("Your post is hidden, Please contact moderator.");
                            dialogBuilder.setCancelable(true);
                            dialogBuilder.setPositiveButton("CONTACT", (dialog, which) -> {
                                String moderatorEmailAddress = "support@myscrap.com";
                                Intent emailApp = new Intent(Intent.ACTION_SEND);
                                emailApp.putExtra(Intent.EXTRA_EMAIL, new String[]{moderatorEmailAddress});
                                emailApp.putExtra(Intent.EXTRA_SUBJECT, "Your post is hidden, Please contact moderator.");
                                emailApp.putExtra(Intent.EXTRA_TEXT, "");
                                emailApp.setType("message/rfc822");
                                startActivity(Intent.createChooser(emailApp, "Send Email Via"));
                            });

                            dialogBuilder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
                            AlertDialog dialog = dialogBuilder.create();
                            dialog.show();
                        }
                    }
                }
                mBottomSheetDialog.dismiss();
            });
        }

    }

}
