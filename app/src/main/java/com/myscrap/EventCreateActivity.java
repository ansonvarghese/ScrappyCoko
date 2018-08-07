package com.myscrap;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.glidebitmappool.GlideBitmapFactory;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.gson.Gson;
import com.myscrap.application.AppController;
import com.myscrap.model.CreateEvent;
import com.myscrap.model.Event;
import com.myscrap.utils.ImageUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EventCreateActivity extends AppCompatActivity {


    private EventCreateActivity mEventCreateActivity;
    private static final int PERMISSIONS_REQUEST = 1;
    private ImageView mEventImage;
    private EditText mEventName;
    private EditText mEventPlace;
    private EditText mEventDetails;
    private static TextView mEventStartDate;
    private static TextView mEventEndDate;
    private static TextView mEventStartTime;
    private static TextView mEventEndTime;
    private static SimpleDateFormat sDFormat;
    private static SimpleDateFormat sDTimeFormat;
    private String imageBitmap;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 0;
    private CheckBox mEventGuestInvite;
    private Spinner spinner;
    private String pageName;
    private Event.EventData eventData;
    private boolean isEdit = false;
    private EventStartDatePickerFragment mEventStartDatePickerFragment;
    private EventEndDatePickerFragment mEventEndDatePickerFragment;
    private EventStartTimePicker mEventStartTimePicker;
    private EventEndTimePicker mEventEndTimePicker;
    private long mLastClickTime = 0;
    private Subscription doCreateEventSubscription;
    private TextView addPhotoText;


    static String encodedImage;
    static String encodedImageURL;
    static Bitmap encodedBitmap;
    static boolean isBitmap;
    private TextView mEventCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_create);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mEventCreateActivity = this;
        if(getSupportActionBar() != null){
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ui_action_bar_cancel);
        }
        mEventImage = (ImageView) findViewById(R.id.event_image);
        mEventImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addPhotoText = (TextView) findViewById(R.id.add_photo);
        mEventName = (EditText)findViewById(R.id.event_name);
        mEventPlace = (EditText)findViewById(R.id.event_location_name);
        mEventDetails = (EditText)findViewById(R.id.event_details);
        mEventStartDate = (TextView) findViewById(R.id.start_date);
        mEventEndDate = (TextView) findViewById(R.id.end_date);
        mEventStartTime = (TextView) findViewById(R.id.start_time);
        mEventEndTime = (TextView) findViewById(R.id.end_time);
        mEventCreate = (TextView) findViewById(R.id.create);
        mEventGuestInvite = (CheckBox) findViewById(R.id.event_invite);

        Calendar calendar = Calendar.getInstance();
        int unRoundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unRoundedMinutes % 15;
        calendar.add(Calendar.MINUTE, mod < 8 ? -mod : (15-mod));
        sDFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        sDTimeFormat = new SimpleDateFormat("hh : mm a", Locale.getDefault());
        String strDate = sDFormat.format(calendar.getTime());
        String strTime = sDTimeFormat.format(calendar.getTime());
        mEventStartDate.setText(strDate);
        mEventStartTime.setText(strTime);


        Intent mIntent = getIntent();
        if(mIntent != null) {
            pageName = mIntent.getStringExtra("page");
            Gson gson = new Gson();
            eventData = gson.fromJson(getIntent().getStringExtra("eventDetails"), Event.EventData.class);
        }

        final LinearLayout place = (LinearLayout) findViewById(R.id.event_location_layout);

        place.setOnClickListener(v -> openAutoCompleteActivity());

        mEventPlace.setOnClickListener(v -> openAutoCompleteActivity());

        addPhotoText.setOnClickListener(v -> checkMedia());

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        mEventStartDatePickerFragment = new EventStartDatePickerFragment();
        mEventStartDatePickerFragment.mDatePickerDialog = new DatePickerDialog(mEventCreateActivity, mStartDatePickerListener, year, month, day);
        mEventEndDatePickerFragment = new EventEndDatePickerFragment();
        mEventEndDatePickerFragment.mDatePickerDialog = new DatePickerDialog(mEventCreateActivity, mEndDatePickerListener, year, month, day);
        mEventStartTimePicker = new EventStartTimePicker();
        mEventStartTimePicker.mTimePickerDialog = new TimePickerDialog(mEventCreateActivity, mStartTimePicker, hour, minute, DateFormat.is24HourFormat(mEventCreateActivity));
        mEventEndTimePicker = new EventEndTimePicker();
        mEventEndTimePicker.mTimePickerDialog = new TimePickerDialog(mEventCreateActivity, mEndTimePicker, hour, minute, DateFormat.is24HourFormat(mEventCreateActivity));

        mEventStartDate.setOnClickListener(v -> {
            if (isEdit) {
                mEventStartDatePickerFragment.mDatePickerDialog.setTitle("");
                mEventStartDatePickerFragment.mDatePickerDialog.show();
            } else {
                if (mEventStartDatePickerFragment == null)
                    mEventStartDatePickerFragment = new EventStartDatePickerFragment();
                mEventStartDatePickerFragment.show(getSupportFragmentManager(), "Event starts date");
            }
        });

        mEventEndDate.setOnClickListener(v -> {
            if (isEdit) {
                mEventEndDatePickerFragment.mDatePickerDialog.setTitle("");
                mEventEndDatePickerFragment.mDatePickerDialog.show();
            } else {
                if (mEventEndDatePickerFragment == null)
                    mEventEndDatePickerFragment = new EventEndDatePickerFragment();
                mEventEndDatePickerFragment.show(getSupportFragmentManager(), "Event ends date");
            }
        });

        mEventStartTime.setOnClickListener(v -> {
            if (isEdit) {
                mEventStartTimePicker.mTimePickerDialog.setTitle("");
                mEventStartTimePicker.mTimePickerDialog.show();
            } else {
                if (mEventStartTimePicker == null)
                    mEventStartTimePicker = new EventStartTimePicker();
                mEventStartTimePicker.show(getSupportFragmentManager(), "Event starts at");
            }

        });

        mEventEndTime.setOnClickListener(v -> {
            if (isEdit) {
                mEventEndTimePicker.mTimePickerDialog.setTitle("");
                mEventEndTimePicker.mTimePickerDialog.show();
            } else {
                if (mEventEndTimePicker == null)
                    mEventEndTimePicker = new EventEndTimePicker();
                mEventEndTimePicker.show(getSupportFragmentManager(), "Event ends at");
            }
        });

        mEventCreate.setOnClickListener(v -> {


            if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            new Handler().post(() -> {
                if(CheckNetworkConnection.isConnectionAvailable(mEventCreateActivity)){
                    if (isEdit){
                        if (eventData != null && !eventData.getEventId().equalsIgnoreCase("")) {
                            doValidateCreateEvent(v, eventData.getEventId());
                        } else {
                            doValidateCreateEvent(v, "");
                        }
                    } else {
                        doValidateCreateEvent(v, "");
                    }
                } else {
                    SnackBarDialog.showNoInternetError(v);
                }
            });
        });

    }


    private void doValidateCreateEvent(View v, String eventId) {
        if(mEventName != null && !mEventName.getText().toString().trim().equalsIgnoreCase("") && mEventName.getText().toString().trim().length() > 0){
            if(mEventEndDate != null && !mEventEndDate.getText().toString().trim().equalsIgnoreCase("") && mEventEndDate.getText().toString().length() > 0){
                if(mEventEndTime != null && !mEventEndTime.getText().toString().trim().equalsIgnoreCase("") && mEventEndTime.getText().toString().length() > 0){
                    if(mEventPlace != null && !mEventPlace.getText().toString().trim().equalsIgnoreCase("") && mEventPlace.getText().toString().length() > 0){
                        if(mEventDetails != null && !mEventDetails.getText().toString().trim().equalsIgnoreCase("") && mEventDetails.getText().toString().length() > 0){
                            doCreateEvent(eventId, mEventName.getText().toString(), mEventStartDate.getText().toString(), mEventStartTime.getText().toString(),
                                    mEventEndDate.getText().toString(), mEventEndTime.getText().toString(), mEventPlace.getText().toString(),
                                    mEventDetails.getText().toString(), String.valueOf(spinner.getSelectedItemPosition()), imageBitmap, mEventGuestInvite.isChecked());
                        } else {
                            SnackBarDialog.show(v, "Event details should not be an empty");
                        }
                    } else {
                        SnackBarDialog.show(v, "Event location should not be an empty");
                    }
                } else {
                    SnackBarDialog.show(v, "Event end time should not be an empty");
                }
            } else {
                SnackBarDialog.show(v, "Event end date should not be an empty");
            }
        } else {
            SnackBarDialog.show(v, "Event name should not be an empty");
        }
    }

    private void doCreateEvent(String eventId, String eventName, String startDate, String startTime, String endDate, String endTime, String location, String details, String privacy, String imageBitmap, boolean checked) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        ProgressBarDialog.showLoader(mEventCreateActivity, false);
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String ipAddress = CheckNetworkConnection.getIPAddress(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        doCreateEventSubscription = apiService.doCreateEvent(userId, eventId, ipAddress, eventName, startDate, startTime, endDate, endTime, location, details, privacy, imageBitmap, checked, apiKey)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CreateEvent>() {
                    @Override
                    public void onCompleted() {
                        ProgressBarDialog.dismissLoader();
                        Log.d("CreateEvent", "onSuccess");
                        encodedImage = "";
                        encodedImageURL = "";
                        encodedBitmap = null;
                        isBitmap = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CreateEvent", "onFailure");
                        ProgressBarDialog.dismissLoader();
                    }

                    @Override
                    public void onNext(CreateEvent mCreateEvent) {
                        if(mCreateEvent != null && !mCreateEvent.isErrorStatus()){
                            finish();
                        }
                    }
                });
    }

    private void openAutoCompleteActivity() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e("place picker", message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                mEventPlace.setText(place.getName());
                mEventPlace.setSelection(mEventPlace.getText().toString().length());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("Place picker", "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED) {
            }
        } else if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                if(data != null){
                    if(data.hasExtra("imageUrl")){
                       // imageBitmap = data.getStringExtra("imageBitmap");
                        String imageUrl = data.getStringExtra("imageUrl");
                        if (isBitmap){
                            mEventImage.post(() -> {
                                //mEventImage.setImageBitmap();
                                updateBitmap(encodedBitmap);
                                imageBitmap = encodedImage;
                            });

                        } else {
                            encodedImageURL = imageUrl;
                            if (encodedImageURL != null) {
                                imageBitmap = encodedImage;
                                AsyncTask.execute(() -> {
                                    //Bitmap mBitmap = GlideBitmapFactory.decodeFile(encodedImageURL);
                                    Bitmap mBitmap = ImageUtils.compressBitmap(encodedImageURL);
                                    updateBitmap(mBitmap);
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateBitmap(Bitmap mBitmap) {
        if (mBitmap != null && mEventImage != null) {

            mEventImage.post(() ->{
                mEventImage.setImageResource(0);
                mEventImage.setImageBitmap(mBitmap);
                });

        }
    }
    
    private String convertToBitmap(String realPath) {
        Bitmap bitmap = GlideBitmapFactory.decodeFile(realPath);
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


    private void checkMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkMediaPermission()) {
                goToUploadImage();
            } else {
                requestMediaPermission();
            }
        } else {
            goToUploadImage();
        }
    }

    private void goToUploadImage() {
        Intent i = new Intent(mEventCreateActivity, ImageUploadActivity.class);
        startActivityForResult(i, 1);
    }

    private boolean checkMediaPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestMediaPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goToUploadImage();
                } else {
                    if (mEventImage != null)
                        Snackbar.make(mEventImage, "Permission Denied, You cannot access media eventData.", Snackbar.LENGTH_LONG).show();
                    break;
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_spinner_menu, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        spinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_type, R.layout.layout_drop_title);
        adapter.setDropDownViewResource(R.layout.layout_drop_list);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        updateEditEventDetails();
        return true;
    }

    private void updateEditEventDetails() {
        if (pageName != null) {
            if (!pageName.equalsIgnoreCase("") && pageName.equalsIgnoreCase("editEvent")){
                isEdit = true;
                if (eventData != null) {


                    addPhotoText.setText("UPDATE PHOTO");
                    mEventCreate.setText("UPDATE");

                    if (eventData.getEventPicture() != null && !eventData.getEventPicture().equalsIgnoreCase("")) {
                        if(eventData.getEventPicture() != null && !eventData.getEventPicture().equalsIgnoreCase("")) {
                            Uri uri = Uri.parse(eventData.getEventPicture());
                            /*com.facebook.imagepipeline.request.ImageRequest imgReq = ImageRequestBuilder.newBuilderWithSource(uri)
                                    .setProgressiveRenderingEnabled(true)
                                    .build();
                            DraweeController controller = Fresco.newDraweeControllerBuilder()
                                    .setImageRequest(imgReq)
                                    .setTapToRetryEnabled(true)
                                    .setOldController(mEventImage.getController())
                                    .build();
                            mEventImage.getHierarchy().setPlaceholderImage(R.drawable.chat_heads_interstitial_map, ScalingUtils.ScaleType.CENTER_CROP);
                            mEventImage.setController(controller);*/

                            Picasso.with(mEventCreateActivity).load(uri).placeholder(R.drawable.chat_heads_interstitial_map).into(mEventImage);

                            mEventImage.setVisibility(View.VISIBLE);
                            new Thread(() -> {
                                try {
                                    URL url = new URL(eventData.getEventPicture());
                                    Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                    if (image != null) {
                                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                        image.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
                                        byte[] byteArray = byteArrayOutputStream .toByteArray();
                                        imageBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT);
                                        Log.d("imageBitmap", imageBitmap);
                                    } else {
                                        imageBitmap = "";
                                    }
                                } catch(IOException e) {
                                    Log.e("Error", e.getMessage());
                                }
                            }).start();
                        } else {
                            /*Uri uri = new Uri.Builder()
                                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                                    .path(String.valueOf(R.drawable.chat_heads_interstitial_map))
                                    .build();
                            mEventImage.setImageURI(uri);*/
                            Picasso.with(mEventCreateActivity).load(R.drawable.chat_heads_interstitial_map).into(mEventImage);
                            imageBitmap = "";
                        }
                    }

                    if (eventData.getEventName() != null && !eventData.getEventName().equalsIgnoreCase("")){
                        mEventName.setText(eventData.getEventName());
                    }

                    if (eventData.getEventLocation() != null && !eventData.getEventLocation().equalsIgnoreCase("")){
                        mEventPlace.setText(eventData.getEventLocation());
                    }

                    if (eventData.getEventDetail() != null && !eventData.getEventDetail().equalsIgnoreCase("")){
                        mEventDetails.setText(eventData.getEventDetail());
                    }

                    if (eventData.getStartDate() != null && !eventData.getStartDate().equalsIgnoreCase("")){
                        Calendar mCalendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            Date date = sdf.parse(eventData.getStartDate());
                            mCalendar.setTime(date);
                            String strDate = sDFormat.format(mCalendar.getTime());
                            mEventStartDate.setText(strDate);
                            mEventStartDatePickerFragment.mDatePickerDialog.updateDate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }

                    if (eventData.getEndDate() != null && !eventData.getEndDate().equalsIgnoreCase("")){
                        Calendar mCalendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            Date date = sdf.parse(eventData.getEndDate());
                            mCalendar.setTime(date);
                            String endDate = sDFormat.format(mCalendar.getTime());
                            mEventEndDate.setText(endDate);
                            mEventEndDatePickerFragment.mDatePickerDialog.updateDate(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (eventData.getStartTime() != null && !eventData.getStartTime().equalsIgnoreCase("")){
                        Calendar mCalendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("hh : mm a");
                        try {
                            mCalendar.setTime(sdf.parse(eventData.getStartTime()));
                            String strTime = sDTimeFormat.format(mCalendar.getTime());
                            mEventStartTime.setText(strTime);
                            mEventStartTimePicker.mTimePickerDialog.updateTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (eventData.getEndTime() != null && !eventData.getEndTime().equalsIgnoreCase("")){
                        Calendar mCalendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("hh : mm a");
                        try {
                            mCalendar.setTime(sdf.parse(eventData.getEndTime()));
                            String endTime = sDTimeFormat.format(mCalendar.getTime());
                            mEventEndTime.setText(endTime);
                            mEventEndTimePicker.mTimePickerDialog.updateTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (eventData.getEventGuest() == 1) {
                        mEventGuestInvite.setChecked(true);
                    } else  {
                        mEventGuestInvite.setChecked(false);
                    }

                    if (eventData.getEventPrivacy() == 0) {
                        spinner.setSelection(0);
                    } else {
                        spinner.setSelection(1);
                    }

                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (doCreateEventSubscription != null && !doCreateEventSubscription.isUnsubscribed())
            doCreateEventSubscription.unsubscribe();
        encodedImage = "";
        encodedImageURL = "";
        encodedBitmap = null;
        isBitmap = false;
        //GlideBitmapPool.clearMemory();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserOnlineStatus.setUserOnline(this, UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(this, UserOnlineStatus.OFFLINE);
    }

    public static class EventStartDatePickerFragment extends DialogFragment{
        public DatePickerDialog mDatePickerDialog;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            mDatePickerDialog = new DatePickerDialog(getActivity(), mStartDatePickerListener, year, month, day);
            mDatePickerDialog.setTitle("");
            mDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return mDatePickerDialog;
        }

    }


    static DatePickerDialog.OnDateSetListener mStartDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            String strDate = sDFormat.format(calendar.getTime());
            mEventStartDate.setText(strDate);
        }
    };

    public static class EventEndDatePickerFragment extends DialogFragment {
        public DatePickerDialog mDatePickerDialog;
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            mDatePickerDialog = new DatePickerDialog(getActivity(), mEndDatePickerListener, year, month, day);
            mDatePickerDialog.setTitle("");
            mDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            return mDatePickerDialog;
        }

    }

    static DatePickerDialog.OnDateSetListener mEndDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            String strDate = sDFormat.format(calendar.getTime());
            mEventEndDate.setText(strDate);
        }
    };

    public static class EventStartTimePicker extends DialogFragment{

        public TimePickerDialog mTimePickerDialog;
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            mTimePickerDialog = new TimePickerDialog(getActivity(), mStartTimePicker, hour, minute, DateFormat.is24HourFormat(getActivity()));
            mTimePickerDialog.setTitle("");
            return mTimePickerDialog;
        }
    }

    static TimePickerDialog.OnTimeSetListener mStartTimePicker = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            c.set(year, month, day, hourOfDay, minute);
            String time = sDTimeFormat.format(c.getTime());
            mEventStartTime.setText(time);
        }
    };

    public static class EventEndTimePicker extends DialogFragment {

        public TimePickerDialog mTimePickerDialog;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            mTimePickerDialog = new TimePickerDialog(getActivity(), mEndTimePicker, hour, minute, DateFormat.is24HourFormat(getActivity()));
            mTimePickerDialog.setTitle("");
            return mTimePickerDialog;
        }
    }

    static TimePickerDialog.OnTimeSetListener mEndTimePicker = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            c.set(year, month, day, hourOfDay, minute);
            String time = sDTimeFormat.format(c.getTime());
            mEventEndTime.setText(time);
        }
    };

}
