package com.myscrap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fenchtose.nocropper.BitmapResult;
import com.fenchtose.nocropper.CropperCallback;
import com.fenchtose.nocropper.CropperView;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.myscrap.application.AppController;
import com.myscrap.model.PictureUrl;
import com.myscrap.model.Post;
import com.myscrap.model.ProfilePicture;
import com.myscrap.utils.BitmapUtils;
import com.myscrap.utils.ImageUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.ProgressBarTransparentDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import id.zelory.compressor.Compressor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ImageUploadActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_PERMISSION = 22;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 23;
    private static final int REQUEST_GALLERY = 21;
    private static final int REQUEST_IMAGE_CAPTURE = 20;
    private static CropperView mImageView;
    private Bitmap mBitmap;
    private boolean isSnappedToCenter = false;
    private static String TAG = "IMAGE_UPLOAD";
    private String absPath;
    private String  encodedImage;
    private static ImageUploadActivity mImageUploadActivity;
    private Bitmap croppedBitmap;
    private boolean isCropped;
    private boolean isImageRotated;
    private Bitmap rotatedBitmap;
    private boolean isMyCompany = false;
    private String companyId;
    private Tracker mTracker;
    private boolean isMultiple;
    private long mLastClickTime = 0;
    private LruCache<String, Bitmap> mMemoryCache;
    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_upload);
        mImageUploadActivity = this;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
        mTracker = AppController.getInstance().getDefaultTracker();
        mImageView = (CropperView) findViewById(R.id.imageview);
        mImageView.setGestureEnabled(true);
        ImageView mGallery = (ImageView) findViewById(R.id.gallery_button);
        ImageView mCamera = (ImageView) findViewById(R.id.camera_button);
        Button mCrop = (Button) findViewById(R.id.crop_button);
        Button mGesture = (Button) findViewById(R.id.gesture_button);
        Button mReplace = (Button) findViewById(R.id.replace_button);
        ImageView mRotate = (ImageView) findViewById(R.id.rotate_button);
        ImageView mSnap = (ImageView) findViewById(R.id.snap_button);
        ImageView mClose = (ImageView) findViewById(R.id.close);
        TextView mUpload = (TextView) findViewById(R.id.upload);

        Intent companyIntent = getIntent();
        if(companyIntent.hasExtra("isMyCompany")){
            isMyCompany = companyIntent.getBooleanExtra("isMyCompany", false);
            companyId = companyIntent.getStringExtra("companyId");
        }

        if(companyIntent.hasExtra("isMultiple")){
            isMultiple = companyIntent.getBooleanExtra("isMultiple", false);
        }


        mClose.setOnClickListener(v -> onBackPressed());

        mUpload.setOnClickListener(v -> mUpload.post(this::performPostClicked));

        mGallery.setOnClickListener(v -> startGalleryIntent(isMultiple));

        mCamera.setOnClickListener(v -> startCameraIntent());


        mCrop.setOnClickListener(v -> cropImageAsync());

        mRotate.setOnClickListener(v -> rotateImage());

        mSnap.setOnClickListener(v -> snapImage());

        mGesture.setOnClickListener(v -> {
            boolean enabled = mImageView.isGestureEnabled();
            enabled = !enabled;
            mImageView.setGestureEnabled(enabled);
            Toast.makeText(getApplicationContext(), "Gesture " + (enabled ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });


        mReplace.setOnClickListener(v -> {
            if (mBitmap != null) {
                mBitmap = BitmapUtils.rotateBitmap(mBitmap, 180);
                mImageView.replaceBitmap(mBitmap);
            }
        });
    }


    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }


    private void performPostClicked() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        if(CheckNetworkConnection.isConnectionAvailable(mImageUploadActivity))
            uploadImageToServer();
        else
            if (mImageView != null)
            SnackBarDialog.showNoInternetError(mImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }else if(item.getItemId() == R.id.action_ok){
            if(CheckNetworkConnection.isConnectionAvailable(mImageUploadActivity))
                uploadImageToServer();
            else
                SnackBarDialog.showNoInternetError(mImageView);
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadImageToServer() {
        if(absPath != null && !absPath.equalsIgnoreCase("")){
            File file = new File(absPath);
            Compressor.getDefault(mImageUploadActivity)
                    .compressToFileAsObservable(file)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file1 -> {
                        update(file1);
                        Log.d("compressed ", String.format("Size : %s", getReadableFileSize(file1.length())));
                    }, throwable -> showError(throwable.getMessage()));
        } else if(mBitmap != null){
            StatusActivity.encodedBitmap = mBitmap;
            EventCreateActivity.encodedBitmap = mBitmap;
            StatusActivity.isBitmap = true;
            EventCreateActivity.isBitmap = true;
            encodedImage = convertToBitmap(mBitmap);
            StatusActivity.encodedImage = encodedImage;
            EventCreateActivity.encodedImage = encodedImage;
            if(isMyCompany){
                uploadCompanyPicture(companyId, encodedImage);
            } else {
                goBack("", encodedImage);

            }
            mLastClickTime = 0;

        } else {
            if (mImageView != null) SnackBarDialog.show(mImageView, "Please select a photo to upload.");
        }
    }

    private void update(File file1) {


        new Thread(() -> {
            encodedImage = null;
            if (file1 != null && file1.isFile()) {
               // ProgressBarDialog.showLoader(mImageUploadActivity, false);
                if (isCropped) {
                    if (isImageRotated) {
                        if (rotatedBitmap != null) {
                            StatusActivity.encodedBitmap = rotatedBitmap;
                            EventCreateActivity.encodedBitmap = rotatedBitmap;
                            StatusActivity.isBitmap = true;
                            EventCreateActivity.isBitmap = true;
                            encodedImage = convertToBitmap(rotatedBitmap);
                        } else {
                            encodedImage = convertToBitmap(file1.getAbsolutePath());
                            StatusActivity.isBitmap = false;
                            EventCreateActivity.isBitmap = false;
                        }
                    } else {
                        if (croppedBitmap != null) {
                            StatusActivity.encodedBitmap = croppedBitmap;
                            EventCreateActivity.encodedBitmap = croppedBitmap;
                            StatusActivity.isBitmap = true;
                            EventCreateActivity.isBitmap = true;
                            encodedImage = convertToBitmap(croppedBitmap);
                        } else {
                            encodedImage = convertToBitmap(file1.getAbsolutePath());
                            StatusActivity.isBitmap = false;
                            EventCreateActivity.isBitmap = false;
                        }
                    }
                } else {
                    if (isImageRotated) {
                        if (rotatedBitmap != null) {
                            encodedImage = convertToBitmap(rotatedBitmap);
                            StatusActivity.isBitmap = true;
                            EventCreateActivity.isBitmap = true;
                            StatusActivity.encodedBitmap = rotatedBitmap;
                            EventCreateActivity.encodedBitmap = rotatedBitmap;
                        } else {
                            encodedImage = convertToBitmap(file1.getAbsolutePath());
                            StatusActivity.isBitmap = false;
                            EventCreateActivity.isBitmap = false;
                        }
                    } else {
                        encodedImage = convertToBitmap(file1.getAbsolutePath());
                        StatusActivity.isBitmap = false;
                        EventCreateActivity.isBitmap = false;
                    }
                }
                //ProgressBarDialog.dismissLoader();
                StatusActivity.encodedImage = encodedImage;
                EventCreateActivity.encodedImage = encodedImage;

                if(isMyCompany){
                    uploadCompanyPicture(companyId, encodedImage);
                } else {
                    if (absPath != null && !absPath.equalsIgnoreCase("")
                            && encodedImage != null && !encodedImage.equalsIgnoreCase(""))
                        goBack(absPath, encodedImage);
                }
                mLastClickTime = 0;

            }
        }).start();


    }

    @Override
    protected void onDestroy() {
      //  GlideBitmapPool.shutDown();
        ProgressBarDialog.dismissLoader();
        super.onDestroy();
    }

    private void uploadCompanyPicture(String companyId, String encodedImage) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        long NOW = System.currentTimeMillis() / 1000L;
        String timeStamp = Long.toString(NOW);
        String feedImage;
        if(encodedImage != null && !encodedImage.equalsIgnoreCase("")){
            feedImage = encodedImage;
        } else{
            feedImage = "";
        }

        runOnUiThread(() -> {
            Toast.makeText(mImageUploadActivity, "Posting..", Toast.LENGTH_SHORT).show();
            ProgressBarDialog.showLoader(mImageUploadActivity, false);
        });

        Call<Post> call = apiService.insertCompanyImage(userId, "0", timeStamp, feedImage, apiKey, companyId, "", "", null, null, null, "");
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull retrofit2.Response<Post> response) {
                Log.d("Post", "onSuccess");
                ProgressBarDialog.dismissLoader();
                if(response.body() != null && response.isSuccessful()){
                    Post mLikedData = response.body();
                    if(mLikedData != null && !mLikedData.isErrorStatus()){
                        Toast.makeText(mImageUploadActivity, "Posted.", Toast.LENGTH_SHORT).show();
                        List<PictureUrl> mPictureUrl = mLikedData.getData().getPictureUrl();
                        if (mPictureUrl != null && !mPictureUrl.isEmpty()){
                            if (mPictureUrl.get(0) != null){
                                PictureUrl pictureUrl = mPictureUrl.get(0);
                                goBackPhotoActivity(pictureUrl);
                            }
                        }
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                Log.d("Post", "onFailure");
                //ProgressBarDialog.dismissLoader();
                finish();
            }
        });
    }


    private void goBackPhotoActivity(PictureUrl pictureUrl) {
        Intent returnIntent = new Intent();
        Gson gson = new Gson();
        String mPictureUrl = gson.toJson(pictureUrl);
        returnIntent.putExtra("pictureUrl", mPictureUrl);
        setResult(Activity.RESULT_OK,returnIntent);
        this.finish();
    }

    private void changeProfile(String profilePic) {
        ProgressBarDialog.showLoader(mImageUploadActivity, false);
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(this);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<ProfilePicture> call = apiService.changeProfile(userId, profilePic, apiKey);
        call.enqueue(new Callback<ProfilePicture>() {
            @Override
            public void onResponse(@NonNull Call<ProfilePicture> call, @NonNull Response<ProfilePicture> response) {
                Log.d("changeProfile", "onSuccess");
                if(response.isSuccessful()){
                    ProfilePicture mProfilePicture = response.body();
                    if(mProfilePicture != null && !mProfilePicture.isErrorStatus()) {
                        UserUtils.saveUserProfilePicture(mImageUploadActivity, mProfilePicture.getProfilePictureUrl());
                    }
                }
                ProgressBarDialog.dismissLoader();
            }
            @Override
            public void onFailure(@NonNull Call<ProfilePicture> call, @NonNull Throwable t) {
                Log.d("changeProfile", "onFailure");
                ProgressBarDialog.dismissLoader();
                if(mImageView != null)
                    SnackBarDialog.show(mImageView, "Failed to upload");
                goBack();
            }
        });
    }

    private void goBack() {
        ProgressBarDialog.dismissLoader();
        onBackPressed();
        this.finish();
    }

    private void goBack(String imageUrl, String encodedImage) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("imageUrl", imageUrl);
        //returnIntent.putExtra("imageBitmap", encodedImage);
        ImageUploadActivity.this.setResult(Activity.RESULT_OK, returnIntent);
        absPath = "";
        absPath = null;
        killActivity();
    }




    private void killActivity() {
        //AppController.runOnUIThread(this::finish);
        ProgressBarDialog.dismissLoader();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(ImageUploadActivity.this::finish);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ProgressBarDialog.dismissLoader();
    }

    private String convertToBitmap(String realPath) {
       // Bitmap bitmap = GlideBitmapFactory.decodeFile(realPath);
        Bitmap bitmap =ImageUtils.compressBitmap(realPath);
        String encodedImage = encodedToImage(bitmap);
        if (encodedImage != null)
            return encodedImage;
        return null;
    }

    private String convertToBitmap(Bitmap bitmap) {

        String encodedImage = encodedToImage(bitmap);
        if (encodedImage != null)
            return encodedImage;
        return null;

    }

    private String encodedToImage(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
        }
        if (out.toByteArray() != null) {
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        }
        return null;
    }

    private void showError(String errorMessage) {
        Toast.makeText(mImageUploadActivity, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void loadNewImage(String filePath) {
        Log.i(TAG, "load image: " + filePath);
        //mBitmap = BitmapFactory.decodeFile(filePath);
        final String imageKey = String.valueOf(filePath);

        /*final Bitmap bitmap = getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            mBitmap = bitmap;
            mImageView.setImageBitmap(bitmap);
        } else {

        }*/


        new DownloadImageTask(filePath, new DownloadImageTask.DownloadImageTaskListener() {
            @Override
            public void onImageDownloaded(Bitmap bitmap) {
                if (bitmap != null) {
                    mBitmap = bitmap;
                    mImageView.setImageBitmap(bitmap);
                    //addBitmapToMemoryCache(filePath, bitmap);
                }

            }

            @Override
            public void onImageDownloadError() {
                Toast.makeText(mImageUploadActivity, "Failed to decode", Toast.LENGTH_SHORT).show();
                Log.e("Error", "Failed to decode");
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static class DownloadImageTask
            extends AsyncTask<String, Void, Bitmap> {
        DownloadImageTask.DownloadImageTaskListener listener;
        String url;
        DownloadImageTask(String filePath, final DownloadImageTaskListener listener) {
            this.url = filePath;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBarDialog.showLoader(mImageUploadActivity, false);
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            // Logic to download an image from an URL

            if (url != null && !url.isEmpty()){
                Bitmap mBitmap = null;
                try {
                    //mBitmap = GlideBitmapFactory.decodeFile(url);
                    mBitmap = ImageUtils.compressBitmap(url);
                    Log.i(TAG, "bitmap: " + mBitmap.getWidth() + " " + mBitmap.getHeight());
                    /*int maxP = Math.max(mBitmap.getWidth(), mBitmap.getHeight());
                    float scale1280 = (float)maxP / 1280;

                    if (mImageView.getWidth() != 0) {
                        mImageView.setMaxZoom(mImageView.getWidth() * 2 / 1280f);
                    } else {

                        ViewTreeObserver vto = mImageView.getViewTreeObserver();
                        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                                mImageView.setMaxZoom(mImageView.getWidth() * 2 / 1280f);
                                return true;
                            }
                        });

                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
                    mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)(mBitmap.getWidth()/scale1280),
                            (int)(mBitmap.getHeight()/scale1280), true);*/
                } catch (Exception e) {
                    Log.d("Error", Arrays.toString(e.getStackTrace()));

                }
                return mBitmap;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            ProgressBarDialog.dismissLoader();
            if (result != null) {
                //GlideBitmapPool.putBitmap(result);
                this.listener.onImageDownloaded(result);
            } else {
                this.listener.onImageDownloadError();
            }

        }

        public interface DownloadImageTaskListener {
            void onImageDownloaded(final Bitmap bitmap);
            void onImageDownloadError();
        }
    }



    private void unLoadBitMap() {
        if (mImageView != null)
            mImageView.setImageBitmap(null);
        if (mBitmap!= null) {
            mBitmap.recycle();
        }
        mBitmap = null;
    }

    public void setImage() {
        unLoadBitMap();
        mImageView = (CropperView) findViewById(R.id.imageview);
        mImageView.setGestureEnabled(true);
    }

    private void startGalleryIntent(boolean isMultiple) {

        if (!hasGalleryPermission()) {
            askForGalleryPermission();
            return;
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        if(isMultiple)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select image"),REQUEST_GALLERY);
    }

    private void startCameraIntent() {
        if (!hasCameraPermission()) {
            askForCameraPermission();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private boolean hasGalleryPermission() {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraPermission() {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void askForGalleryPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_READ_PERMISSION);
    }

    private void askForCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.CAMERA},
                REQUEST_CODE_CAMERA_PERMISSION);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent resultIntent) {
        super.onActivityResult(requestCode, responseCode, resultIntent);

        if (responseCode == RESULT_OK && requestCode == REQUEST_GALLERY) {
            absPath = BitmapUtils.getFilePathFromUri(this, resultIntent.getData());
            loadNewImage(absPath);
        } else if(responseCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE){
            if (resultIntent.getData() != null) {
                absPath = BitmapUtils.getFilePathFromUri(this, resultIntent.getData());
                if (absPath != null)
                    loadNewImage(absPath);
                else
                    handleSmallCameraPhoto(resultIntent);
            } else {
                handleSmallCameraPhoto(resultIntent);
            }
        }
    }

    private void handleSmallCameraPhoto(Intent resultIntent) {
        Bundle extras = resultIntent.getExtras();
        if (extras != null) {
            Bitmap bitmap = (Bitmap) extras.get("data");
            if (bitmap != null) {
                mBitmap = bitmap;
                mImageView.setImageBitmap(bitmap);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGalleryIntent(isMultiple);
            } else{
                Toast.makeText(this, "Gallery permission not granted", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraIntent();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale
                        (this, android.Manifest.permission.CAMERA)) {
                    showAlert();
                }
            }
        }
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW",
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(ImageUploadActivity.this,
                            new String[]{android.Manifest.permission.CAMERA},
                            REQUEST_CODE_CAMERA_PERMISSION);

                });
        alertDialog.show();
    }

    private void cropImageAsync() {
        if (mBitmap != null && absPath != null && !absPath.equalsIgnoreCase("")) {
            //show();

            BitmapResult.State state = mImageView.getCroppedBitmapAsync(new CropperCallback() {
                @Override
                public void onCropped(Bitmap bitmap) {
                    if (bitmap != null) {
                        isCropped = true;
                        croppedBitmap = bitmap;
                    }
                }

                @Override
                public void onOutOfMemoryError() {
                    Log.e(TAG, "out of memory error");
                }
            });

            if (state == BitmapResult.State.FAILURE_GESTURE_IN_PROCESS) {
                Toast.makeText(this, "unable to crop. Gesture in progress", Toast.LENGTH_SHORT).show();
            }

        } else {
            if (mImageView != null) SnackBarDialog.show(mImageView, "Please select a photo to crop.");
        }

    }

    private void cropImage() {

        /*BitmapResult bitmap = mImageView.getCroppedBitmap();
        if (bitmap != null) {
            try {
                BitmapUtils.writeBitmapToFile(bitmap, new File(Environment.getExternalStorageDirectory() + "/crop_test.jpg"), 90);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    private void rotateImage() {
        if (mBitmap == null) {
            Log.e(TAG, "bitmap is not loaded yet");
            return;
        }
        isImageRotated = true;
        mBitmap = BitmapUtils.rotateBitmap(mBitmap, 90);
        rotatedBitmap = mBitmap;
        mImageView.setImageBitmap(mBitmap);
    }

    private void snapImage() {

        if (mBitmap != null && absPath != null && !absPath.equalsIgnoreCase("")) {
            if (isSnappedToCenter) {
                mImageView.cropToCenter();
            } else {
                mImageView.fitToCenter();
            }
           // cropImageAsync();
            isSnappedToCenter = !isSnappedToCenter;
        } else {
            if (mImageView != null) SnackBarDialog.show(mImageView, "Please select a photo to crop.");
        }


    }


    private void show() {
        if (mImageView != null) {
            mImageView.post(() -> ProgressBarTransparentDialog.showLoader(mImageUploadActivity,"Performing..."));
        }
    }

    private void hide() {
        if (mImageView != null) {
            mImageView.post(ProgressBarTransparentDialog::dismissLoader);
        }
    }

}
