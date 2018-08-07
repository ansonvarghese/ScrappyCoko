package com.myscrap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.customphoto.cropoverlay.CropOverlayView;
import com.myscrap.customphoto.cropoverlay.edge.Edge;
import com.myscrap.customphoto.cropoverlay.utils.ConstantsImageCrop;
import com.myscrap.customphoto.cropoverlay.utils.ImageViewUtil;
import com.myscrap.customphoto.cropoverlay.utils.InternalStorageContentProvider;
import com.myscrap.customphoto.cropoverlay.utils.Utils;
import com.myscrap.customphoto.customcropper.CropperView;
import com.myscrap.model.ProfilePicture;
import com.myscrap.utils.BitmapUtils;
import com.myscrap.utils.ImageUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import id.zelory.compressor.Compressor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


interface IMainActivity {
    void initViews();

    void findViews();

    void makeLayoutSquare();


    void hideCropping();

    void showCropping();

    void onGetImages(String action);

    void createTempFile();

    void takePic();

    void pickImage();

    void initClickListner();
}



public class ProfileUploadActivity extends AppCompatActivity implements IMainActivity, View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    public static String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";

    private static final int REQUEST_CODE_PICK_GALLERY = 0x1;
    private static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_STORAGE = 1;
    protected ImageView imgImage;
    ProgressDialog dialog;
    private float minScale = 1f;
    private RelativeLayout relativeImage;
    private ImageView btnTakePicture, btnChooseGallery,rotateButton;
    private TextView cropDone;
    private ImageView cancelUpload;
    private CropperView cropperView;
    private CropOverlayView cropOverlayView;
    private File mFileTemp;
    private String mImagePath = null;
    private Uri mSaveUri = null;
    private Uri mImageUri = null;
    private ContentResolver mContentResolver;
    private ProfileUploadActivity mProfileUploadActivity;
    private String encodedImage;
    private Tracker mTracker;
    private long mLastClickTime;



    private Bitmap myBitMap;

    public Bitmap getMyBitMap() {
        return myBitMap;
    }

    public void setMyBitMap(Bitmap myBitMap) {
        this.myBitMap = myBitMap;
    }

    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[512];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public static int getCameraPhotoOrientation(@NonNull Context context, Uri imageUri) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            ExifInterface exif = new ExifInterface(
                    imageUri.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static boolean createDirIfNotExists()
    {
        boolean ret = true;
        File file = new File(Environment.getExternalStorageDirectory(), "Prototype");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                ret = false;
            }
        }
        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_profile_upload);
        mProfileUploadActivity = this;
        createDirIfNotExists();
        findViews();
        initViews();
        mTracker = AppController.getInstance().getDefaultTracker();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestExternalStoragePermission();
        }
    }

    @Override
    protected void onResume()

    {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Profile Upload Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    private void requestCameraPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.CAMERA)) {

            ActivityCompat.requestPermissions(ProfileUploadActivity.this,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        } else {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
    }

    private void requestExternalStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            ActivityCompat.requestPermissions(ProfileUploadActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE);
        } else {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE);
        }
    }

    private void showCameraPreview()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try
        {
            Uri mImageCaptureUri;
            String state = Environment.getExternalStorageState();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                if (Environment.MEDIA_MOUNTED.equals(state))
                {
                    //mImageCaptureUri = Uri.fromFile(mFileTemp);
                    mImageCaptureUri = FileProvider.getUriForFile(this, this
                            .getPackageName() + ".provider", mFileTemp);
                }
                else
                {
                    mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
                }
            }
            else
            {
                if (Environment.MEDIA_MOUNTED.equals(state))
                {
                    mImageCaptureUri = Uri.fromFile(mFileTemp);
                } else {
                    mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
                }
            }

          //  takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            takePictureIntent.putExtra("return-data", true);
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE);
        }
        catch (ActivityNotFoundException e)
        {
            Log.e("CameraPreview", e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_CAMERA)
        {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                takePic();
            }
            else
            {
                Toast.makeText(this, "Camera permission was not granted.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_STORAGE)
        {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
            }
            else
            {
                Toast.makeText(this, "Storage permission was not granted.", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void initViews() {
        mContentResolver = getContentResolver();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        makeLayoutSquare();
        initClickListner();
        cropperView.addListener(() -> new Rect((int) Edge.LEFT.getCoordinate(), (int) Edge.TOP.getCoordinate(), (int) Edge.RIGHT.getCoordinate(), (int) Edge.BOTTOM.getCoordinate()));
    }

    @Override
    public void findViews() {
        btnTakePicture = (ImageView) findViewById(R.id.camera_button);
        btnChooseGallery = (ImageView) findViewById(R.id.gallery_button);
        rotateButton = findViewById(R.id.rotate_button);
        relativeImage = (RelativeLayout) findViewById(R.id.relativeImage);
        cropperView = (CropperView) findViewById(R.id.cropperView);
        cropOverlayView = (CropOverlayView) findViewById(R.id.cropOverlayView);
        imgImage = (ImageView) findViewById(R.id.imgImage);
        cropDone = (TextView) findViewById(R.id.upload);
        cancelUpload = (ImageView) findViewById(R.id.close);
    }

    @Override
    public void makeLayoutSquare() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.x;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        relativeImage.setLayoutParams(params);
    }


    @Override
    public void hideCropping() {
        imgImage.setVisibility(View.VISIBLE);
        cropperView.setVisibility(View.GONE);
        cropOverlayView.setVisibility(View.GONE);
        findViewById(R.id.test).setVisibility(View.GONE);
    }

    @Override
    public void showCropping() {
        imgImage.setVisibility(View.GONE);
        cropperView.setVisibility(View.VISIBLE);
        cropOverlayView.setVisibility(View.GONE);
        findViewById(R.id.test).setVisibility(View.VISIBLE);
    }

    @Override
    public void initClickListner() {
        btnTakePicture.setOnClickListener(this);
        btnChooseGallery.setOnClickListener(this);
        rotateButton.setOnClickListener(this);
        cropDone.setOnClickListener(this);
        cancelUpload.setOnClickListener(this);
    }

    @Override
    public void onGetImages(String action) {
       // createTempFile();
        try {
            createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != action) {
            switch (action) {
                case ConstantsImageCrop.IntentExtras.ACTION_CAMERA:
                    getIntent().removeExtra("ACTION");
                    takePic();
                    return;
                case ConstantsImageCrop.IntentExtras.ACTION_GALLERY:
                    getIntent().removeExtra("ACTION");
                    pickImage();
                    return;
            }
        }

    }

/*
    private void init() {
        showCropping();
        Bitmap b = getBitmap(mImageUri);
        cropperView.setImageBitmap(b);
    }*/



    @Override
    public void createTempFile()
    {
        String state = Environment.getExternalStorageState();
        File mFolder = new File(Environment.getExternalStorageDirectory() + "imagecroplikeinstagram");
        if (!mFolder.exists())
        {
            mFolder.mkdir();
        }
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            mFileTemp = new File(Environment.getExternalStorageDirectory() + "/imagecroplikeinstagram", TEMP_PHOTO_FILE_NAME);
        }
        else
        {
            mFileTemp = new File(Environment.getExternalStorageDirectory()+ "/imagecroplikeinstagram", TEMP_PHOTO_FILE_NAME);
        }


        if (!mFileTemp.exists())
        {
            try
            {
                mFileTemp.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    private void createImageFile() throws IOException
    {
        // Create an image file name
        //Modified By Ussaid Iqbal Fixed Camera Blank Image
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, TEMP_PHOTO_FILE_NAME);
        // Save a file: path for use with ACTION_VIEW intents
        mFileTemp = new File(image.getAbsolutePath());
        Log.e("Ussaid Check Point 1", "mFileTemp - "+mFileTemp.getPath());
    }

    @Override
    public void takePic() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestCameraPermission();
        }
        else
        {
            showCameraPreview();
        }
    }

    @Override
    public void pickImage() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestExternalStoragePermission();
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
            try {
                startActivityForResult(intent, REQUEST_CODE_PICK_GALLERY);
            } catch (ActivityNotFoundException e) {
            }
        }

    }

    private void init() {

        showCropping();


        Bitmap b = getMyBitMap();
    //    Bitmap b = getBitmap(mImageUri);


        Drawable bitmap = new BitmapDrawable(getResources(), b);
        int h = bitmap.getIntrinsicHeight();
        int w = bitmap.getIntrinsicWidth();
        final float cropWindowWidth = Edge.getWidth();
        final float cropWindowHeight = Edge.getHeight();
        if (h <= w) {
            minScale = (cropWindowHeight + 1f) / h;
        } else if (w < h) {
            minScale = (cropWindowWidth + 1f) / w;
        }

        cropperView.setMaximumScale(minScale * 9);
        cropperView.setMediumScale(minScale * 6);
        cropperView.setMinimumScale(minScale);
        cropperView.setImageDrawable(bitmap);
        cropperView.setScale(minScale);
    }




    private Bitmap getBitmap(Uri uri)
    {
       /* InputStream in = null;
        Bitmap returnedBitmap = null;*/
        InputStream in ;
        Bitmap returnedBitmap ;
        try {
            in = mContentResolver.openInputStream(uri);
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            if (in != null)
            {
                in.close();
            }
            int scale = 1;
            int IMAGE_MAX_SIZE = 1024;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE)
            {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = mContentResolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, o2);
            if (in != null)
            {
                in.close();
            }
            returnedBitmap = fixOrientationBugOfProcessedBitmap(bitmap);
            return returnedBitmap;
        }
        catch (FileNotFoundException e)
        {
        }
        catch (IOException e)
        {
        }
        return null;

    }

    private Bitmap fixOrientationBugOfProcessedBitmap(Bitmap bitmap)
    {
        try {

            if (getCameraPhotoOrientation(this, Uri.parse(mFileTemp.getPath())) == 0)
            {
                return bitmap;
            }
            else
            {
                Matrix matrix = new Matrix();
                matrix.postRotate(getCameraPhotoOrientation(this, Uri.parse(mFileTemp.getPath())));
                // recreate the new Bitmap and set it back
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    private void saveAndUploadImage()
    {


        if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        configDialog();
        boolean saved = saveOutput();
        if(mImagePath != null){
            ProgressBarDialog.showLoader(mProfileUploadActivity, false);
            File file = new File(mImagePath);
            Compressor.getDefault(getApplicationContext())
                    .compressToFileAsObservable(file)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file1 -> {
                        encodedImage = convertToBitmap(file1.getAbsolutePath());
                        changeProfile(encodedImage);
                        Log.d("compressed ", String.format("Size : %s", getReadableFileSize(file1.length())));
                    }, throwable -> {
                        showError(throwable.getMessage());
                        ProgressBarDialog.dismissLoader();
                    });

            imgImage.setImageBitmap(getBitmap(mImageUri));

            if (saved) {
                Toast.makeText(this, "Starts uploading", Toast.LENGTH_SHORT).show();
            } else {
            }
        }
    }

    private void changeProfile(String profilePic) {
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
                        UserUtils.saveUserProfilePicture(mProfileUploadActivity, mProfilePicture.getProfilePictureUrl());
                        finishBack();
                        hideCropping();
                        Toast.makeText(mProfileUploadActivity, "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                    mLastClickTime = 0;
                }
                ProgressBarDialog.dismissLoader();
            }
            @Override
            public void onFailure(@NonNull Call<ProfilePicture> call, @NonNull Throwable t) {
                Log.d("changeProfile", "onFailure");
                ProgressBarDialog.dismissLoader();
                mLastClickTime = 0;
                if(imgImage != null)
                    SnackBarDialog.show(imgImage, "Failed to upload");
                finishBack();
            }
        });
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
        Toast.makeText(mProfileUploadActivity, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    void configDialog() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading..");
    }

    void dismisDialog() {
        if (dialog.isShowing())
            dialog.dismiss();
    }

    void showDialog() {
        dialog.show();

    }

    private boolean saveOutput() {
        Bitmap croppedImage = getCroppedImage();

        if (mSaveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(mSaveUri);
                if (outputStream != null) {
                    croppedImage.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable t) {
                    }
                }
            }
        } else {
            return false;
        }
        croppedImage.recycle();
        return true;
    }

    private Bitmap getCurrentDisplayedImage() {
        Bitmap result = Bitmap.createBitmap(cropperView.getWidth(), cropperView.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(result);
        cropperView.draw(c);
        return result;
    }

    public Bitmap getCroppedImage() {
        Bitmap mCurrentDisplayedBitmap = getCurrentDisplayedImage();
        Rect displayedImageRect = ImageViewUtil.getBitmapRectCenterInside(mCurrentDisplayedBitmap, cropperView);

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for width.
        float actualImageWidth = mCurrentDisplayedBitmap.getWidth();
        float displayedImageWidth = displayedImageRect.width();
        float scaleFactorWidth = actualImageWidth / displayedImageWidth;

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for height.
        float actualImageHeight = mCurrentDisplayedBitmap.getHeight();
        float displayedImageHeight = displayedImageRect.height();
        float scaleFactorHeight = actualImageHeight / displayedImageHeight;

        // Get crop window position relative to the displayed image.
        float cropWindowX = Edge.LEFT.getCoordinate() - displayedImageRect.left;
        float cropWindowY = Edge.TOP.getCoordinate() - displayedImageRect.top;
        float cropWindowWidth = Edge.getWidth();
        float cropWindowHeight = Edge.getHeight();

        // Scale the crop window position to the actual size of the Bitmap.
        float actualCropX = cropWindowX * scaleFactorWidth;
        float actualCropY = cropWindowY * scaleFactorHeight;
        float actualCropWidth = cropWindowWidth * scaleFactorWidth;
        float actualCropHeight = cropWindowHeight * scaleFactorHeight;

        // Crop the subset from the original Bitmap.
        return Bitmap.createBitmap(mCurrentDisplayedBitmap, (int) actualCropX, (int) actualCropY, (int) actualCropWidth, (int) actualCropHeight);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result)
    {

        super.onActivityResult(requestCode, resultCode, result);
      //  createTempFile();
        try
        {
            createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showCropping();
        if (requestCode == REQUEST_CODE_TAKE_PICTURE && resultCode == RESULT_OK) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                hideCropping();
                imgImage.setVisibility(View.VISIBLE);
                imgImage.setImageResource(R.color.fb_view_bg);
                return;
            }

            if (result != null)
            {

                Uri photo = result.getData();
                Bitmap bitmap = getBitmap(photo);
                setMyBitMap(bitmap);
            }


            mImagePath = mFileTemp.getPath();
            mSaveUri = Utils.getImageUri(mImagePath);
            mImageUri = Utils.getImageUri(mImagePath);
            Log.e("Ussaid Check Point 2", "mImagePath - "+mImagePath);

            init();

        }
        else if (requestCode == REQUEST_CODE_PICK_GALLERY && resultCode == RESULT_OK)
        {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
            {
                hideCropping();
                imgImage.setVisibility(View.VISIBLE);
                imgImage.setImageResource(R.color.fb_view_bg);
                Toast.makeText(this, "No permission on Storage", Toast.LENGTH_SHORT).show();

                //code for default image
                return;
            }

            try
            {
                if (result.getData() != null)
                {
                    InputStream inputStream = getContentResolver().openInputStream(result.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    mImagePath = mFileTemp.getPath();
                    mSaveUri = Utils.getImageUri(mImagePath);
                    mImageUri = Utils.getImageUri(mImagePath);
                    init();
                }

            } catch (Exception e) {
                Log.e("Error", e.toString());
            }
        } else {

            hideCropping();
            imgImage.setVisibility(View.VISIBLE);
        }
    }




    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }



    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        switch (id) {
            case R.id.gallery_button:
                mImagePath = null;//do again
                onGetImages(ConstantsImageCrop.IntentExtras.ACTION_GALLERY);
                break;
            case R.id.camera_button:
                mImagePath = null;
                onGetImages(ConstantsImageCrop.IntentExtras.ACTION_CAMERA);
                break;

            case R.id.rotate_button :
                rotateImage();
                break;


            case R.id.upload:
                if (mImagePath != null)
                    saveAndUploadImage();
                break;
            case R.id.close:
                finishBack();
                break;


        }
    }

    private void rotateImage()
    {
        Bitmap currentBitmap = getMyBitMap();
        if (getMyBitMap() == null)
        {
            Log.e("ROTATE_IMAGE", "bitmap is not loaded yet");
            return;
        }
        if (currentBitmap != null)
        {
            Bitmap mBitmap = BitmapUtils.rotateBitmap(currentBitmap, 90);
            setMyBitMap(mBitmap);
            init();
        }
    }

    void finishBack(boolean status) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("isupdate", status);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    void finishBack() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
