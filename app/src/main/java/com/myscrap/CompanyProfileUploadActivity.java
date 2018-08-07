package com.myscrap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fenchtose.nocropper.CropperCallback;
import com.fenchtose.nocropper.CropperView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.CompanyProfilePicture;
import com.myscrap.utils.BitmapUtils;
import com.myscrap.utils.ImageUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import java.io.File;
import java.text.DecimalFormat;

import id.zelory.compressor.Compressor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CompanyProfileUploadActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_PERMISSION = 22;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 23;
    private static final int REQUEST_GALLERY = 21;
    private static final int REQUEST_IMAGE_CAPTURE = 20;
    private CropperView mImageView;
    private Bitmap mBitmap;
    private boolean isSnappedToCenter = false;
    private String TAG = "IMAGE_UPLOAD";
    private String absPath;
    private String  encodedImage;
    private CompanyProfileUploadActivity mImageUploadActivity;
    private Bitmap croppedBitmap;
    private boolean isCropped;
    private boolean isImageRotated;
    private Bitmap rotatedBitmap;
    private String companyId;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_upload);
        mImageUploadActivity = this;
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

        Intent mIntent = getIntent();
        if(mIntent != null){
            companyId = mIntent.getStringExtra("companyId");
        }

        mTracker = AppController.getInstance().getDefaultTracker();

        mClose.setOnClickListener(v -> onBackPressed());

        mUpload.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(mImageUploadActivity))
                uploadImageToServer();
            else
                SnackBarDialog.showNoInternetError(v);
        });

        mGallery.setOnClickListener(v -> startGalleryIntent());

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
            uploadImageToServer();
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
                        if(isCropped && croppedBitmap != null){
                            if(isImageRotated && rotatedBitmap != null){
                                encodedImage = convertToBitmap(file1.getAbsolutePath(), rotatedBitmap);
                            } else {
                                encodedImage = convertToBitmap(file1.getAbsolutePath(), croppedBitmap);
                            }
                        } else if(isImageRotated && rotatedBitmap != null){
                            if(isCropped){
                                encodedImage = convertToBitmap(file1.getAbsolutePath(), croppedBitmap);
                            } else {
                                encodedImage = convertToBitmap(file1.getAbsolutePath(), rotatedBitmap);
                            }
                        } else {
                            encodedImage = convertToBitmap(file1.getAbsolutePath());
                        }
                        changeProfile(encodedImage, companyId);
                        Log.d("compressed ", String.format("Size : %s", getReadableFileSize(file1.length())));
                    }, throwable -> showError(throwable.getMessage()));
        }
    }

    private void changeProfile(String profilePic, String companyId) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ProgressBarDialog.showLoader(mImageUploadActivity, false);
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(this);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<CompanyProfilePicture> call = apiService.changeCompanyProfile(userId, companyId, profilePic, apiKey);
        call.enqueue(new Callback<CompanyProfilePicture>() {
            @Override
            public void onResponse(@NonNull Call<CompanyProfilePicture> call, @NonNull Response<CompanyProfilePicture> response) {
                Log.d("changeProfile", "onSuccess");
                ProgressBarDialog.dismissLoader();
                goBack();
            }
            @Override
            public void onFailure(@NonNull Call<CompanyProfilePicture> call, @NonNull Throwable t) {
                Log.d("changeProfile", "onFailure");
                ProgressBarDialog.dismissLoader();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Company Profile Upload Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    private void goBack() {
        onBackPressed();
        this.finish();
    }

    private void goBack(String imageUrl, String encodedImage) {
        Intent i = new Intent(mImageUploadActivity, StatusActivity.class);
        i.putExtra("imageUrl", imageUrl);
        i.putExtra("imageBitmap", encodedImage);
        startActivity(i);
        this.finish();
    }

    private String convertToBitmap(String realPath) {
        String encodedImage = ImageUtils.compressImage(realPath);
        if (encodedImage != null)
            return encodedImage;
        return null;
    }


    private String convertToBitmap(String realPath, Bitmap bitmap) {
        String encodedImage = ImageUtils.compressBitmap(bitmap,realPath);
        if (encodedImage != null)
            return encodedImage;
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
        mBitmap = BitmapFactory.decodeFile(filePath);
        Log.i(TAG, "bitmap: " + mBitmap.getWidth() + " " + mBitmap.getHeight());

        int maxP = Math.max(mBitmap.getWidth(), mBitmap.getHeight());
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

        mBitmap = Bitmap.createScaledBitmap(mBitmap, (int)(mBitmap.getWidth()/scale1280),
                (int)(mBitmap.getHeight()/scale1280), true);

        mImageView.setImageBitmap(mBitmap);

    }

    private void startGalleryIntent() {
        if (!hasGalleryPermission()) {
            askForGalleryPermission();
            return;
        }
        Intent intent = new Intent();
        intent.setType("image/*");
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
            absPath = BitmapUtils.getFilePathFromUri(this, resultIntent.getData());
            loadNewImage(absPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGalleryIntent();
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
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(CompanyProfileUploadActivity.this,
                            new String[]{android.Manifest.permission.CAMERA},
                            REQUEST_CODE_CAMERA_PERMISSION);

                });
        alertDialog.show();
    }

    private void cropImageAsync() {
        mImageView.getCroppedBitmapAsync(new CropperCallback() {
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
        if (isSnappedToCenter) {
            mImageView.cropToCenter();
        } else {
            mImageView.fitToCenter();
        }
        cropImageAsync();
        isSnappedToCenter = !isSnappedToCenter;
    }


}
