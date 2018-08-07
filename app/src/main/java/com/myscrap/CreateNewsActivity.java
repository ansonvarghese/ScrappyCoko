package com.myscrap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;
import com.myscrap.application.AppController;
import com.myscrap.model.News;
import com.myscrap.model.Post;
import com.myscrap.utils.ImageUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import id.zelory.compressor.Compressor;
import in.myinnos.awesomeimagepicker.activities.AlbumSelectActivity;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Image;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class CreateNewsActivity extends AppCompatActivity{

    private EditText headline;
    private EditText subHeadLine;
    private EditText content;
    private AutoCompleteTextView location;
    private LinearLayout addPhotoLayout;
    private TextView publish;
    private CreateNewsActivity mCreateNewsActivity;
    private static final int PERMISSIONS_REQUEST = 1;
    int PLACE_PICKER_REQUEST = 11;

    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private ViewPager viewPager;
    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<String> imageConvertedList = new ArrayList<>();
    private ImageView left, right;
    private RelativeLayout leftRightLayout;
    private FrameLayout viewPagerLayout;
    private long mLastClickTime;
    private String editPostId = "0";
    private String companyId;
    private boolean isEditPost = false;
    private String editPost;

    private ArrayList<String> names;
    private ArrayAdapter<String> adapter;
    private String countrySelection = "";
    private Subscription doPostNewsSubscription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mCreateNewsActivity = this;
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        LinearLayout eventLocationLayout = (LinearLayout) findViewById(R.id.event_location_layout);
        headline = (EditText) findViewById(R.id.head_lines);
        subHeadLine = (EditText) findViewById(R.id.sub_head_lines);
        content = (EditText) findViewById(R.id.content);
        publish = (TextView) findViewById(R.id.publish);
        publish.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(mCreateNewsActivity))
                postANews();
            else
            if(publish != null)
                SnackBarDialog.showNoInternetError(publish);
        });
        location = (AutoCompleteTextView) findViewById(R.id.location_name);
        location.setThreshold(1);
        names = new ArrayList<>();
        location.setOnItemClickListener((adapterView, view, position, l) -> {
            countrySelection = (String)adapterView.getItemAtPosition(position);
            if (countrySelection != null && !countrySelection.equalsIgnoreCase("")) {
                String[] places = countrySelection.split(",");
                if (places.length != 0 && places.length == 3) {
                } else if (places.length != 0 && places.length == 2) {
                } else if (places.length != 0 && places.length == 1) {
                }
            }
        });

        location.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() >= 3) {
                    names = new ArrayList<>();
                    if (CheckNetworkConnection.isConnectionAvailable(mCreateNewsActivity))
                        updateList(s.toString());
                    else
                    if (location != null)
                        SnackBarDialog.show(location, "No internet connection available");
                }
            }
        });
        left = (ImageView) findViewById(R.id.left);
        right = (ImageView) findViewById(R.id.right);
        addPhotoLayout = (LinearLayout) findViewById(R.id.add_photo_layout);
        leftRightLayout = (RelativeLayout) findViewById(R.id.left_right_layout);
        viewPagerLayout = (FrameLayout) findViewById(R.id.view_pager_layout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if(getIntent() != null){
            companyId = getIntent().getStringExtra("companyId");
            editPost = getIntent().getStringExtra("editPost");
            editPostId = getIntent().getStringExtra("postId");
        }
        if(editPost != null && !editPost.equalsIgnoreCase("") && editPostId != null && !editPostId.equalsIgnoreCase("")){
            isEditPost = true;
            Gson gson = new Gson();
            News.NewsData newsItem = gson.fromJson(getIntent().getStringExtra("tagData"), News.NewsData.class);
            if(newsItem != null){
                if(newsItem.getHeading() != null && !newsItem.getHeading().equalsIgnoreCase("")){
                    headline.setText(newsItem.getHeading());
                    headline.setSelection(headline.getText().length());
                }

                if(newsItem.getSubHeading() != null && !newsItem.getSubHeading().equalsIgnoreCase("")){
                    subHeadLine.setText(newsItem.getSubHeading());
                    subHeadLine.setSelection(subHeadLine.getText().length());
                }

                if(newsItem.getStatus() != null && !newsItem.getStatus().equalsIgnoreCase("")){
                    content.setText(newsItem.getStatus());
                    content.setSelection(content.getText().length());
                }

                if(newsItem.getLocation() != null && !newsItem.getLocation().equalsIgnoreCase("")) {
                    location.setText(newsItem.getLocation());
                    location.setSelection(location.getText().length());
                }
            }
        }
        addBottomDots(0);
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        addPhotoLayout.setOnClickListener(v -> checkMedia());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_news_menu, menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    public void updateList(String place) {
        String input = "";
        try {
            input = "input=" + URLEncoder.encode(place, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String output = "json";
        String browserKey = getResources().getString(R.string.google_map_key);
        String parameter = input + "&types=geocode&sensor=true&key=" + browserKey;
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameter;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray ja = response.getJSONArray("predictions");
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject c = ja.getJSONObject(i);
                                String description = c.getString("description");
                                Log.d("description", description);
                                names.add(description);
                            }
                            adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, names) {
                                @NonNull
                                @Override
                                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                                    text.setTextColor(Color.BLACK);
                                    return view;
                                }
                            };
                            location.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.d("Exception", e.toString());
                        }
                    }
                }, error -> Toast.makeText(mCreateNewsActivity,"Server Error", Toast.LENGTH_SHORT).show());
        AppController.getInstance().getRequestQueue().getCache().invalidate(url, true);
        jsonObjReq.setShouldCache(true);
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(jsonObjReq, "search");
    }

    private void postANews() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        if(headline != null && !headline.getText().toString().equalsIgnoreCase("")){
            if(content!= null && !content.getText().toString().equalsIgnoreCase("")){
                if(!isEditPost){
                    if(imageConvertedList!= null && imageConvertedList.size() > 0){
                        JSONArray jsonArray = new JSONArray();
                        JSONObject imageObject = new JSONObject();
                        if(imageConvertedList.size() > 0) {
                            for (int i = 0; i < imageConvertedList.size(); i++) {
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("feedImage"+i, imageConvertedList.get(i));
                                    jsonArray.put(jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                imageObject.put("multiImage", jsonArray);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        doPostNews(content.getText().toString(), headline.getText().toString(), subHeadLine.getText().toString(),location.getText().toString(), imageObject);
                    } else {
                        doPostNews(content.getText().toString(), headline.getText().toString(), subHeadLine.getText().toString(),location.getText().toString(), null);
                    }
                } else {
                    doPostNews(content.getText().toString(), headline.getText().toString(), subHeadLine.getText().toString(),location.getText().toString(), null);
                }
            } else {
                if(content != null)
                    SnackBarDialog.show(content, "Content shouldn't be an empty");
            }
        } else {
            if(headline != null)
            SnackBarDialog.show(headline, "Headlines shouldn't be an empty");
        }
    }

    private String convertToBitmap(String realPath) {
        String encodedImage = ImageUtils.compressImage(realPath);
        if (encodedImage != null)
            return encodedImage;
        return null;
    }

    private void doPostNews(String content, String heading, String sunHeading, String location, JSONObject imageObject) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        long NOW = System.currentTimeMillis() / 1000L;
        String postingTime = Long.toString(NOW);
        final String device = "android";
        int imageCount;
        if(imageConvertedList.size() == 0)
            imageCount = 0;
        else
            imageCount = imageConvertedList.size();
        ProgressBarDialog.showLoader(mCreateNewsActivity, false);
        final String ipAddress = CheckNetworkConnection.getIPAddress(getApplicationContext());
        doPostNewsSubscription = apiService.insertPostInsert(userId, "0", companyId, editPostId, "0", "0", content, postingTime, null, imageCount, device, ipAddress, apiKey, null,null, null, heading, sunHeading, location, imageObject)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Post>() {
                    @Override
                    public void onCompleted() {
                        Log.d("Post", "onSuccess");
                        ProgressBarDialog.dismissLoader();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Post", "onFailure");
                        ProgressBarDialog.dismissLoader();
                        mLastClickTime = 0;
                    }

                    @Override
                    public void onNext(Post mPostData) {
                        Toast.makeText(mCreateNewsActivity, "Posted.", Toast.LENGTH_SHORT).show();
                        mLastClickTime = 0;
                        if(mPostData != null && !mPostData.isErrorStatus()){
                            finish();
                        }
                    }
                });

    }


    @Override
    protected void onDestroy() {
        if (doPostNewsSubscription != null && !doPostNewsSubscription.isUnsubscribed())
            doPostNewsSubscription.unsubscribe();
        super.onDestroy();
    }

    private void addBottomDots(int currentPage) {
        TextView[] dots = new TextView[imageList.size()];
        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dots[i].setText(Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY));
            } else {
                dots[i].setText(Html.fromHtml("&#8226;"));
            }
            dots[i].setTextSize(35);
            dots[i].setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));
            dotsLayout.addView(dots[i]);
        }

        if(imageList.size() > 0) {
            leftRightLayout.setVisibility(View.VISIBLE);
            if(currentPage == 0) {
                left.setVisibility(View.GONE);
                right.setVisibility(View.VISIBLE);
            }else if (currentPage == imageList.size() - 1) {
                left.setVisibility(View.VISIBLE);
                right.setVisibility(View.GONE);
            } else {
                left.setVisibility(View.VISIBLE);
                right.setVisibility(View.VISIBLE);
            }
        }

        left.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem() -1;
            if (current < imageList.size()) {
                // move to previous screen
                viewPager.setCurrentItem(current);
            }
        });

        right.setOnClickListener(v -> {
            int current = getItem(+1);
            if (current < imageList.size()) {
                // move to next screen
                viewPager.setCurrentItem(current);
            }
        });

        if (dots.length > 0)
            dots[currentPage].setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary));
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    private class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
        private SimpleDraweeView image;

        MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = null;
            if (layoutInflater != null) {
                view = layoutInflater.inflate(R.layout.image_list, container, false);
                image = (SimpleDraweeView) view.findViewById(R.id.image);
                Uri uri = Uri.fromFile(new File(imageList.get(position)));
                image.setImageURI(uri);
                Compressor.getDefault(mCreateNewsActivity)
                        .compressToFileAsObservable(new File(imageList.get(position)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(file -> imageConvertedList.add(convertToBitmap(file.getAbsolutePath())), throwable -> showError(throwable.getMessage()));
                container.addView(view);
            }

            return view;
        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    private void showError(String errorMessage) {
        Toast.makeText(mCreateNewsActivity, errorMessage, Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(this, AlbumSelectActivity.class);
        intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 5); // set limit for image selection
        startActivityForResult(intent, ConstantsCustomGallery.REQUEST_CODE);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConstantsCustomGallery.REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                if (data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES);
                    imageConvertedList.clear();
                    imageList.clear();
                    if(images != null){
                        if(images.size() > 0){
                            for (int i = 0; i < images.size(); i++) {
                                //Uri uri = Uri.fromFile(new File(images.get(i).path));
                                imageList.add(images.get(i).path);
                            }
                            addBottomDots(0);
                            viewPagerLayout.setVisibility(View.VISIBLE);
                        } else {
                            viewPagerLayout.setVisibility(View.GONE);
                        }
                    }
                    myViewPagerAdapter.notifyDataSetChanged();
                }
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("%s", place.getName());
                location.setText(toastMsg);
                location.setSelection(toastMsg.length());
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goToUploadImage();
                } else {
                    if (addPhotoLayout != null)
                        Snackbar.make(addPhotoLayout, "Permission Denied, You cannot access media data.", Snackbar.LENGTH_LONG).show();
                    break;
                }
        }
    }

}
