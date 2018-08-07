package com.myscrap;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.myscrap.adapters.MyImageAdapter;
import com.myscrap.model.GalleryModel;
import com.myscrap.utils.BitmapUtils;
import com.myscrap.utils.Helper;
import com.myscrap.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;


public class MultiPhotoSelectActivity extends AppCompatActivity {

	private static final int PERMISSIONS_REQUEST = 1;
	private static final int REQUEST_CODE_CAMERA_PERMISSION = 23;
	private static final int REQUEST_IMAGE_CAPTURE = 20;
	private ProgressDialog pd;
	private RecyclerView recyclerView;
	private ArrayList<GalleryModel> galleryModels;
	private MyImageAdapter imageAdapter;
	private int mode = Helper.TAG_MODE_DIRECTORY;
	public MultiPhotoSelectActivity mMultiPhotoSelectActivity;
	private static final String BUNDLE_RECYCLER_LAYOUT = "gallery_view";
	public boolean isShowCheckBox  = false;
	private Toolbar toolbar;
	private ArrayList<String> paths = new ArrayList<>();
	private Handler handler = new Handler(new IncomingHandlerCallback());

	private class IncomingHandlerCallback implements Handler.Callback {
		@Override
		public boolean handleMessage(Message msg) {
			if (pd != null) {
				pd.dismiss();
				pd = null;
			}
			switch (msg.what) {
				case 0:
					setImagesOnAdapter();
					break;
				case 1:
					RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
					recyclerView.setLayoutManager(mLayoutManager);
					recyclerView.setHasFixedSize(true);
					//recyclerView.setItemAnimator(new DefaultItemAnimator());
					new Handler().post(() -> recyclerView.setAdapter(imageAdapter));
					break;
			}
			return true;
		}
	}

	private Cursor imageCursor;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (recyclerView.getLayoutManager() != null && outState != null)
			outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(savedInstanceState != null) {
			Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
			recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_image_grid);
		mMultiPhotoSelectActivity = this;
		recyclerView = (RecyclerView ) findViewById(R.id.recycler_view);
		RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("Gallery");
		}
		if (recyclerView == null) {
			RecyclerView.LayoutManager mRecyclerViewLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
			recyclerView.setLayoutManager(mRecyclerViewLayoutManager);
			//recyclerView.setItemAnimator(new DefaultItemAnimator());
		}
		checkMedia();
	}

	private void checkMedia() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkMediaPermission()) {
				setImagesOnAdapter();
			} else {
				requestMediaPermission();
			}
		} else {
			setImagesOnAdapter();
		}
	}

	private boolean checkMediaPermission() {
		return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
	}

	private void requestMediaPermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
		} else {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode == PERMISSIONS_REQUEST) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				setImagesOnAdapter();
			} else {
				if (recyclerView != null)
					Snackbar.make(recyclerView, "Permission Denied, You cannot access media data.", Snackbar.LENGTH_LONG).show();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (toolbar != null)
			toolbar.getMenu().clear();
		MenuInflater inflater = getMenuInflater();
        if (isShowCheckBox) {
            inflater.inflate(R.menu.option_menu_done, menu);
        } else {
			inflater.inflate(R.menu.option_menu_camera, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.action_done:
				new Handler().post(this::selectPhotoToUpload);
				return true;
				case R.id.action_camera:
					startCameraIntent();
				return true;
		}
		return false;
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

	private boolean hasCameraPermission() {
		return ActivityCompat.checkSelfPermission(this,
				android.Manifest.permission.CAMERA)
				== PackageManager.PERMISSION_GRANTED;
	}

	private void askForCameraPermission() {
		ActivityCompat.requestPermissions(this,
				new String[]{android.Manifest.permission.CAMERA},
				REQUEST_CODE_CAMERA_PERMISSION);
	}

	@Override
	public void onActivityResult(int requestCode, int responseCode, Intent resultIntent) {
		super.onActivityResult(requestCode, responseCode, resultIntent);

		if(responseCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE){
			String absPath = BitmapUtils.getFilePathFromUri(this, resultIntent.getData());
			ArrayList<String> selectedItems = new ArrayList<>();
			selectedItems.add(absPath);
			if(selectedItems.size() > 0) {
				Log.d("Photo", ""+selectedItems.size());
				Intent returnIntent = new Intent();
				returnIntent.putStringArrayListExtra("images", selectedItems);
				setResult(Activity.RESULT_OK,returnIntent);
				this.finish();
			} else {
				this.finish();
			}
		} else {
			this.finish();
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
                    ActivityCompat.requestPermissions(MultiPhotoSelectActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CODE_CAMERA_PERMISSION);

                });
		alertDialog.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@SuppressWarnings("deprecation")
	private void setImagesOnAdapter() {

        new Thread(() -> runOnUiThread(() -> {
            pd = new ProgressDialog(mMultiPhotoSelectActivity, R.style.progressBarTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        })).start();


		Thread thread = new Thread(() -> runOnUiThread(() -> {
            final String[] columns = { MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID };
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            imageCursor = managedQuery(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, orderBy + " DESC");
            galleryModels = Utils.getAllDirectoriesWithImages(imageCursor);
            imageAdapter = new MyImageAdapter(MultiPhotoSelectActivity.this, null, galleryModels);
            handler.sendEmptyMessage(1);
        }));
		thread.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void setGallery() {

        new Thread(() -> runOnUiThread(() -> {
            if (mode == Helper.TAG_MODE_FILES) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Gallery");
                }
                mode = Helper.TAG_MODE_DIRECTORY;
                imageAdapter = new MyImageAdapter(mMultiPhotoSelectActivity, null, galleryModels);
                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setHasFixedSize(true);
                //recyclerView.setItemAnimator(new DefaultItemAnimator());
                new Handler().post(() -> recyclerView.setAdapter(imageAdapter));
            }
        })).start();

	}

	private void selectPhotoToUpload() {
		ArrayList<String> selectedItems = imageAdapter.getCheckedItems();
        if(selectedItems.size() > 0) {
            Log.d("Photo", ""+selectedItems.size());
            Intent returnIntent = new Intent();
            returnIntent.putStringArrayListExtra("images", selectedItems);
            setResult(Activity.RESULT_OK,returnIntent);
            this.finish();
        } else {
            this.finish();
        }
	}

	public void setFilesFromFolder(final int position) {
		if (recyclerView != null)
			recyclerView.getRecycledViewPool().clear();
		mode = Helper.TAG_MODE_FILES;
		if (getSupportActionBar() != null) {
			if (galleryModels != null && galleryModels.get(position).folderName != null ){
				String folderName = galleryModels.get(position).folderName;
				getSupportActionBar().setTitle(folderName);
			}
		}


		new Thread(() -> runOnUiThread(() -> {
            if (position == 0) {
                paths = new ArrayList<>();
                final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
                final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
                Cursor imageCursor = mMultiPhotoSelectActivity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
                if (imageCursor != null) {
                    for (int i = 0; i < imageCursor.getCount(); i++) {
                        imageCursor.moveToPosition(i);
                        int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        paths.add(imageCursor.getString(dataColumnIndex));
                    }
                }
                if (imageCursor != null) {
                    imageCursor.close();
                }
            } else {
                if (galleryModels != null){
                    Object[] abc = galleryModels.get(position).folderImages.toArray();
                    paths = new ArrayList<>();
                    int size = abc.length;
                    for (int i = 0; i < size; i++) {
                        paths.add((String) abc[i]);
                    }
                    Collections.reverse(paths);
                }

            }
            imageAdapter = new MyImageAdapter(mMultiPhotoSelectActivity, paths, null);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setHasFixedSize(true);
            //recyclerView.setItemAnimator(new DefaultItemAnimator());
            new Handler().post(() -> recyclerView.setAdapter(imageAdapter));
        })).start();

    }

	@Override
	public void onBackPressed() {
		if (mode == Helper.TAG_MODE_FILES) {
			setGallery();
			return;
		}
		super.onBackPressed();
        finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
        new Thread(() -> runOnUiThread(() -> {
            if (pd != null) {
                pd.dismiss();
                pd = null;
            }
        }));

        if(imageCursor != null){
        	imageCursor.close();
		}
		super.onDestroy();
	}


}