package com.myscrap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.myscrap.adapters.TaggingAdapter;
import com.myscrap.application.AppController;
import com.myscrap.model.Feed;
import com.myscrap.model.Post;
import com.myscrap.model.Search;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.ImageUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StatusActivity extends AppCompatActivity implements TaggingAdapter.TaggingAdapterListener{

    private static final int PERMISSIONS_REQUEST = 1;
    private static final int IMAGE_REQUEST = 100;
    private HorizontalAdapter horizontalAdapter;
    private List<Bitmap> horizontalList = new ArrayList<>();
    private RecyclerView horizontal_recycler_view;
    private EditText postStatusEditText;
    private String postStatusEditTextCopy;
    private TextView hint;
    private StatusActivity mStatusActivity;
    private long mLastClickTime = 0;
    private String mStatusMessage;
    private String postingTime;
    private String editPostId;
    private String friendId;
    private String eventId;
    private String eventName;
    private String pageName;
    private boolean isPosting;

    private EditText mTaggingEditText;
    private LinearLayout rootStatusLayout;
    private LinearLayout taggingLayout;
    private RelativeLayout profilePhotoLayout;
    private ProgressBar mProgressBar;

    private TaggingAdapter mTaggingAdapter;
    private List<Search.SearchData.Users> mUsers = new ArrayList<>();
    private List<Search.SearchData.Users> mSelectedUsers = new ArrayList<>();
    private String imageBitmap;

    static String encodedImage;
    static String encodedImageURL;
    static Bitmap encodedBitmap;
    static boolean isBitmap;

    private SpannableString builder;
    private  boolean isPostStatusEditText;
    private TextWatcher mTextWatcher;
    private List<String> spanned = new ArrayList<>();
    private ConcurrentHashMap<Search.SearchData.Users, List<Integer>> map = new ConcurrentHashMap <>();
    private int cursorPositionStart;
    private int cursorPositionEnd;
    private List<String> taggedUserIdList = new ArrayList<>();
    private List<String> taggedUserNameList = new ArrayList<>();
    private Tracker mTracker;
    private String queryingText;
    private List<Integer> atGradeList;
    private Call<Search> call;
    private Subscription searchSubscription, postingFeedsSubscription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mStatusActivity = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        TaggingAdapter.TaggingAdapterListener mTaggingAdapterListener = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mTaggingAdapter = new TaggingAdapter(mStatusActivity, mUsers , mTaggingAdapterListener);
        mTaggingEditText = (EditText) findViewById(R.id.input_first_name);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        horizontal_recycler_view = (RecyclerView) findViewById(R.id.horizontal_recycler_view);
        RecyclerView taggingRecyclerView = (RecyclerView) findViewById(R.id.tagging);
        taggingRecyclerView.setHasFixedSize(true);
        taggingRecyclerView.addItemDecoration(new DividerItemDecoration(mStatusActivity, DividerItemDecoration.VERTICAL));
        taggingRecyclerView.setLayoutManager(new LinearLayoutManager(StatusActivity.this, LinearLayoutManager.VERTICAL, false));
        taggingRecyclerView.setAdapter(mTaggingAdapter);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(StatusActivity.this, LinearLayoutManager.VERTICAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManager);
        postStatusEditText = (EditText) findViewById(R.id.post_edit_text);
        hint = (TextView) findViewById(R.id.hint);
        TextView userName = (TextView) findViewById(R.id.username) ;
        LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.touch_layout);
        taggingLayout = (LinearLayout) findViewById(R.id.tagging_layout);
        profilePhotoLayout = (RelativeLayout) findViewById(R.id.user_status_layout);
        SimpleDraweeView mUserProfile = (SimpleDraweeView) findViewById(R.id.feeds_dialog_profile_header_photo);
        TextView iconText = (TextView) findViewById(R.id.icon_text);
        ImageView bottomCamera = (ImageView) findViewById(R.id.camera);
        TextView bottomStatusTextView = (TextView) findViewById(R.id.status);

        rootStatusLayout = (LinearLayout) findViewById(R.id.root_status_layout);
        bottomCamera.setOnClickListener(v -> checkMedia());

        String profilePic = UserUtils.getUserProfilePicture(mStatusActivity);
        String name = UserUtils.getFirstName(mStatusActivity) + " " + UserUtils.getLastName(mStatusActivity);
        if(profilePic != null) {
            if(profilePic.equalsIgnoreCase("") || profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                    || profilePic.equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                mUserProfile.setImageResource(R.drawable.bg_circle);
                mUserProfile.setColorFilter(R.color.guest);
                iconText.setVisibility(View.VISIBLE);
                if (!name.equalsIgnoreCase("")) {
                    String[] split = name.trim().split("\\s+");
                    if (split.length > 1) {
                        String first = split[0].trim().substring(0, 1);
                        String last = split[1].trim().substring(0, 1);
                        String initial = first + "" + last;
                        iconText.setText(initial.toUpperCase());
                    } else {
                        if (split[0] != null && split[0].trim().length() >= 1) {
                            String first = split[0].trim().substring(0, 1);
                            iconText.setText(first.toUpperCase());
                        }
                    }
                }
            } else {
                Uri uri = Uri.parse(profilePic);
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                mUserProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                        .setRoundingParams(roundingParams)
                        .build());
                roundingParams.setRoundAsCircle(true);
                mUserProfile.setImageURI(uri);
                mUserProfile.setColorFilter(null);
                iconText.setVisibility(View.GONE);
            }
        }

        userName.setText(name);

        if (mLinearLayout != null) {
            mLinearLayout.setOnTouchListener((v, event) -> {
                openKeyBoard(mLinearLayout);
                postStatusEditText.requestFocus();
                return true;
            });
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent mIntent = getIntent();
        if(mIntent != null){
            String editPost = mIntent.getStringExtra("editPost");
            editPostId = mIntent.getStringExtra("postId");
            friendId = mIntent.getStringExtra("friendId");
            String friendName = mIntent.getStringExtra("friendName");
            pageName = mIntent.getStringExtra("page");
            eventId = mIntent.getStringExtra("eventId");
            eventName = mIntent.getStringExtra("eventName");
            String buttonAction = mIntent.getStringExtra("click");
            if (getSupportActionBar() != null) {
                if(friendName != null && !friendName.equalsIgnoreCase("")){
                    getSupportActionBar().setTitle("On " + friendName + " 's Timeline" );
                    postStatusEditText.setHint("Write something to "+ friendName.substring(0,6)+"...");
                } else if(eventName != null && !eventName.equalsIgnoreCase("")){
                    getSupportActionBar().setTitle("On " + eventName + " 's Timeline" );
                    postStatusEditText.setHint("Write something to "+ eventName.substring(0,6)+"...");
                }
            }
            if(editPost != null && !editPost.equalsIgnoreCase("") && editPostId != null && !editPostId.equalsIgnoreCase("")){
                boolean isEditPost = true;
                postStatusEditText.setText(editPost);
                postStatusEditText.setSelection(editPost.length());
                postStatusEditText.requestFocus();
                rootStatusLayout.setVisibility(View.GONE);

                Gson gson = new Gson();
                Feed.FeedItem feedItem = gson.fromJson(getIntent().getStringExtra("tagData"), Feed.FeedItem.class);
                if(feedItem != null){
                    if(feedItem.getPostType()!= null && feedItem.getPostType().equalsIgnoreCase("friendUserPost")){
                        friendId = feedItem.getPostedFriendId();
                        if(feedItem.getPostedFriendId() == null)
                            friendId = "0";
                        friendName = feedItem.getPostedFriendName();
                        if (getSupportActionBar() != null) {
                            if(friendName != null && !friendName.equalsIgnoreCase("")){
                                getSupportActionBar().setTitle("On " + friendName + " 's Timeline" );
                                postStatusEditText.setHint("Write something to "+ friendName);
                            }
                        }
                    }
                    mSelectedUsers.clear();
                    mUsers.clear();
                    for(Feed.FeedItem.TagList tag : feedItem.getTagList()){
                        if(tag != null){
                            mUsers.add(new Search().new SearchData().new Users(tag.getTaggedId(),tag.getTaggedUserName()));
                        }
                    }
                    mSelectedUsers.addAll(mUsers);
                    if(mSelectedUsers.size() > 0){
                        if(mSelectedUsers != null && mSelectedUsers.size() > 0 ) {
                            for (Search.SearchData.Users data : mSelectedUsers){
                                loadFromEditPost(data, false);
                            }
                        }
                    }
                }
            }
            if(buttonAction != null && !buttonAction.equalsIgnoreCase("")){
                if(buttonAction.equalsIgnoreCase("camera")){
                    checkMedia();
                }
            }

        }


        if(postStatusEditText.getText().toString().length() == 0){
            mSelectedUsers.clear();
            hint.setVisibility(View.VISIBLE);
            if(mTaggingAdapter != null){
                mTaggingAdapter.swap(new ArrayList<>());
            }
        } else {
            hint.setVisibility(View.GONE);
        }


        if(friendId == null)
            friendId = "0";

        postStatusEditText.setOnTouchListener((clickedView, event) -> {
            int selStart = ((EditText) clickedView).getSelectionStart();
            cursorPositionStart = ((EditText) clickedView).getSelectionStart();
            cursorPositionEnd = ((EditText) clickedView).getSelectionEnd();
            // Determine the word "under" selStart...
            Log.d("EditText", "sel:" + String.valueOf(cursorPositionStart) + " end:"+ String.valueOf(cursorPositionEnd));
            return false;
        });


        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mTextWatcher == null)
                    return;

                cursorPositionStart = postStatusEditText.getSelectionStart();
                cursorPositionEnd = postStatusEditText.getSelectionEnd();
                Log.d("EditText", "sel:" + String.valueOf(cursorPositionStart) + " end:"+ String.valueOf(cursorPositionEnd));

                /*if(s.toString().contains("@")){
                    showTaggingLayout();
                } else {
                    showStatusLayout();
                }*/


                String word = s.toString();
                if(word.contains("@")){
                    char letter = '@';
                    int index = word.indexOf(letter);
                    atGradeList = new ArrayList<>();
                    while (index != -1) {
                        atGradeList.add(index);
                        index = word.indexOf(letter, index + 1);

                    }
                }

                if(atGradeList != null && !atGradeList.isEmpty()){
                    if(s.toString().contains("@") && (s.length() > atGradeList.get(atGradeList.size()-1))){
                        String queryCount = s.toString().substring(atGradeList.get(atGradeList.size()-1)+1, s.length());
                        if(queryCount.length() >= 2){
                            //showTaggingLayout(queryCount);
                            callTaggingUser(queryCount);
                        } else {
                            showStatusLayout();
                        }
                    } else {
                        showStatusLayout();
                    }
                }

                if(s.length() == 0){
                    mSelectedUsers.clear();
                    hint.setVisibility(View.VISIBLE);
                    if(mTaggingAdapter != null){
                        mTaggingAdapter.swap(new ArrayList<>());
                    }
                } else {
                    hint.setVisibility(View.GONE);
                }

                if(postStatusEditTextCopy != null){

                    if(s.toString().length() < postStatusEditTextCopy.length()){
                        if(postStatusEditTextCopy.equals(s.toString())){
                            Toast.makeText(mStatusActivity, "Same ", Toast.LENGTH_SHORT).show();
                        } else {
                            String filtered = diff(postStatusEditTextCopy.trim(), s.toString().trim());
                            if(!filtered.equalsIgnoreCase("")){
                                Iterator<Map.Entry<Search.SearchData.Users, List<Integer>>> itr = map.entrySet().iterator();
                                while(itr.hasNext()){
                                    Map.Entry<Search.SearchData.Users, List<Integer>> entry = itr.next();
                                    Search.SearchData.Users key = entry.getKey();
                                    List<Integer> values = entry.getValue();
                                    int startIndex = values.get(0);
                                    int endIndex = values.get(1);
                                    if(cursorPositionStart != -1 && cursorPositionEnd != -1 ){
                                        if(cursorPositionStart >= startIndex && cursorPositionEnd <= endIndex){
                                            final Editable cachedText = Editable.Factory.getInstance().newEditable(postStatusEditText.getText());
                                            cachedText.clearSpans();
                                            if(cursorPositionEnd <= endIndex){
                                                //map.remove(key);
                                                itr.remove();
                                                cachedText.delete(startIndex, postStatusEditText.getSelectionStart());
                                                postStatusEditText.setText(cachedText);
                                                postStatusEditText.setMovementMethod(LinkMovementMethod.getInstance());
                                                postStatusEditText.setSelection(cachedText.length());
                                                postStatusEditText.invalidate();
                                                break;
                                            }
                                        }
                                    }
                                }

                                mSelectedUsers.clear();
                                if(!map.isEmpty()){
                                    for (Map.Entry<Search.SearchData.Users, List<Integer>> ee : map.entrySet()) {
                                        Search.SearchData.Users key = ee.getKey();
                                        mSelectedUsers.add(key);
                                    }
                                }

                                if(mSelectedUsers != null && mSelectedUsers.size() > 0 ) {
                                    for (Search.SearchData.Users data : mSelectedUsers){
                                        loadSelectedUsers(data, false);
                                    }
                                }
                            }
                        }
                    }
                }

            }
            @Override
            public void afterTextChanged(Editable s)
            {
                if(postStatusEditText != null)
                    postStatusEditTextCopy = postStatusEditText.getText().toString();
            }

        };

        if(postStatusEditText != null )
            postStatusEditText.addTextChangedListener(mTextWatcher);

        mTaggingEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= (mTaggingEditText.getRight() - mTaggingEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    mTaggingEditText.setText("");
                    showStatusLayout();
                    return true;
                }
            }
            return false;
        });

    }

    public static String diff(String str1, String str2) {
        int index = str1.lastIndexOf(str2);
        if (index > -1) {
            return str1.substring(str2.length());
        }
        return str1;
    }

    private void callTaggingUser(CharSequence queryText) {
        if(CheckNetworkConnection.isConnectionAvailable(mStatusActivity)){
            if(queryText.toString().contains("@")){
                startSearch(queryText.toString().replace("@", ""));
            } else {
                startSearch(queryText.toString());
            }
        } else {
            if(mTaggingEditText != null)
                SnackBarDialog.showNoInternetError(mTaggingEditText);
        }
    }

    private void startSearch(String queryText) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            if(mProgressBar != null)
                mProgressBar.setVisibility(View.VISIBLE);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(mStatusActivity);
            queryingText = queryText;
            searchSubscription = apiService.search(userId, queryText, apiKey)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Search>() {
                        @Override
                        public void onCompleted() {
                            if(mProgressBar != null)
                                mProgressBar.setVisibility(View.GONE);
                            Log.d("startSearch", "onSuccess");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("startSearch", "onFailure");
                            ProgressBarDialog.dismissLoader();
                            if(mProgressBar != null)
                                mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onNext(Search mSearch) {
                            if(mSearch != null) {
                                Search.SearchData mData = mSearch.getSearchData();
                                if(mData != null){
                                    if(mData.getUser() != null){
                                        mUsers = mData.getUser();
                                        if(mUsers != null){
                                            if(mTaggingAdapter != null){
                                                if(mUsers.size() > 0){
                                                    mTaggingAdapter.swap(mUsers);
                                                } else {
                                                    mTaggingAdapter.swap(mUsers);
                                                }
                                            }
                                        } else {
                                            if(mTaggingAdapter != null){
                                                mTaggingAdapter.swap(new ArrayList<>());
                                            }
                                        }
                                    } else {
                                        if(mTaggingAdapter != null){
                                            mTaggingAdapter.swap(new ArrayList<>());
                                        }
                                    }
                                } else {
                                    if(mTaggingAdapter != null){
                                        if(mUsers == null)
                                            mUsers = new ArrayList<>();
                                        mUsers.clear();
                                        mTaggingAdapter.swap(mUsers);
                                    }
                                }
                            }
                        }
                    });
        } else {
            if(mTaggingEditText != null)
                SnackBarDialog.showNoInternetError(mTaggingEditText);
            if(mProgressBar != null)
                mProgressBar.setVisibility(View.GONE);
        }
    }

    private void openKeyBoard(LinearLayout mLinearLayout) {
        runOnUiThread(() -> UserUtils.showKeyBoard(AppController.getInstance(), mLinearLayout));
    }

    private void showStatusLayout(){
        postStatusEditText.setVisibility(View.VISIBLE);
        hint.setVisibility(View.GONE);
        profilePhotoLayout.setVisibility(View.VISIBLE);
        rootStatusLayout.setVisibility(View.VISIBLE);
        horizontal_recycler_view.setVisibility(View.VISIBLE);
        taggingLayout.setVisibility(View.GONE);
    }

    private void showTaggingLayout(String queryCount){
        profilePhotoLayout.setVisibility(View.GONE);
        postStatusEditText.setVisibility(View.GONE);
        hint.setVisibility(View.GONE);
        rootStatusLayout.setVisibility(View.GONE);
        horizontal_recycler_view.setVisibility(View.GONE);
        taggingLayout.setVisibility(View.VISIBLE);
        mTaggingEditText.setText("@"+queryCount);
        mTaggingEditText.setSelection(1);
        mTaggingEditText.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_menu, menu);
        return true;
    }

    private void setHorizontalAdapter(final List<Bitmap> horizontalList) {
        runOnUiThread(() -> {
            horizontalAdapter = new HorizontalAdapter(horizontalList);
            horizontal_recycler_view.setAdapter(horizontalAdapter);
            horizontalAdapter.notifyDataSetChanged();
            horizontal_recycler_view.smoothScrollToPosition(horizontalAdapter.getItemCount());
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                 onBackPressed();
                 return true;
            case R.id.action_post:
                 performPostClicked();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mStatusActivity);
        dialogBuilder.setMessage("Do you want to discard this post?");
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("CANCEL", (dialog, which) -> dialog.dismiss());
        dialogBuilder.setNegativeButton("DISCARD POST", (dialog, which) -> finish());
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void performPostClicked() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 10*1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        if (isPosting)
            return;

        performPost();
    }

    private void performPost(){
        if (CheckNetworkConnection.isConnectionAvailable(mStatusActivity)) {
            if ((postStatusEditText != null && !postStatusEditText.getText().toString().trim().equalsIgnoreCase("")) || (horizontalList != null && horizontalList.size() > 0)) {
                mStatusMessage = postStatusEditText.getText().toString();
                long NOW = System.currentTimeMillis() / 1000L;
                postingTime = Long.toString(NOW);
                hideKeyboard(postStatusEditText);
                isPosting = true;
                postingFeeds();
            } else {
                if (postStatusEditText != null) {
                    SnackBarDialog.show(postStatusEditText, "The post cannot be empty.");
                }
            }
        } else {
            if (postStatusEditText != null) {
                SnackBarDialog.show(postStatusEditText, "No internet connection available.");
            }
        }
    }

    private String convertToBitmap(String realPath) {
        //Bitmap bitmap = GlideBitmapFactory.decodeFile(realPath);
        Bitmap bitmap = ImageUtils.compressBitmap(realPath);
        String encodedImage = encodedToImage(bitmap);
        if (encodedImage != null)
            return encodedImage;
        return null;
    }

    private String encodedToImage(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        if (out.toByteArray() != null) {
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        }
        return null;
    }

    private void postingFeeds() {
        ProgressBarDialog.showLoader(this, false);
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(AppController.getInstance());
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        String content = mStatusMessage;
        String timeStamp = postingTime;
        String feedImage;
        if(horizontalList != null && horizontalList.size() > 0){
            if (isBitmap){
                feedImage = encodedImage;
            } else {
                feedImage = convertToBitmap(encodedImageURL);
            }
        } else{
            feedImage = "";
        }

        if(editPostId == null)
            editPostId = "";
        if(eventName == null)
            eventName = "";

        String eventPost;
        if(eventId != null && !eventId.equalsIgnoreCase("")){
            eventPost = "1";
        } else {
            eventPost = "0";
            eventId = "0";
        }
        taggedUserIdList.clear();
        taggedUserNameList.clear();
        JSONArray jsonArray = new JSONArray();
        Iterator<Map.Entry<Search.SearchData.Users, List<Integer>>> itr = map.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry<Search.SearchData.Users, List<Integer>> entry = itr.next();
            Search.SearchData.Users key = entry.getKey();
            taggedUserIdList.add(key.getId());
            taggedUserNameList.add(key.getName());
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("taggedId", key.getId());
                jsonObject.put("taggedUserName", key.getName());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        final String device = "android";
        final String ipAddress = CheckNetworkConnection.getIPAddress(getApplicationContext());

        JSONObject taggingObject = new JSONObject();
        try {
            taggingObject.put("tagging", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(friendId == null)
            friendId = "0";


        String companyId = "0";
        postingFeedsSubscription = apiService.insertPostInsert(userId, friendId, companyId, editPostId, eventId, eventPost,content, timeStamp, feedImage, 0, device, ipAddress, apiKey, taggingObject,taggedUserIdList, taggedUserNameList, null, null, null, null)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Post>() {
                    @Override
                    public void onCompleted() {
                        Log.d("Post", "onSuccess");
                        ProgressBarDialog.dismissLoader();
                        encodedImage = "";
                        encodedImageURL = "";
                        encodedBitmap = null;
                        isBitmap = false;
                        isPosting = false;
                        finish();
                        /*if(pageName != null && pageName.equalsIgnoreCase("feeds")){
                            //EventBus.getDefault().post("feeds");
                            goToHome();
                            finish();
                        } else {
                            finish();
                        }*/
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Post", "onFailure");
                        ProgressBarDialog.dismissLoader();
                        mLastClickTime = 0;
                        isPosting = false;
                        Toast.makeText(mStatusActivity, "Failed to post.", Toast.LENGTH_SHORT).show();
                        if(pageName != null && pageName.equalsIgnoreCase("feeds")){
                            EventBus.getDefault().post("feeds");
                            goToHome();
                        } else {
                            finish();
                        }
                    }

                    @Override
                    public void onNext(Post mPost) {
                        if (mPost != null) {
                            Toast.makeText(mStatusActivity, "Posted.", Toast.LENGTH_SHORT).show();
                            mLastClickTime = 0;
                            if(!mPost.isErrorStatus()){
                                if(horizontalList != null)
                                    horizontalList.clear();

                            }
                        }
                    }
                });

    }

    private void goToHome() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        this.finish();
        if(CheckOsVersion.isPreLollipop())
            this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void hideKeyboard(View v) {
        UserUtils.hideKeyBoard(mStatusActivity, v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Status Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        //UserOnlineStatus.setUserOnline(AppController.getInstance(),UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //UserOnlineStatus.setUserOnline(AppController.getInstance(),UserOnlineStatus.OFFLINE);
    }

    private void checkMedia()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkMediaPermission())
            {
                goToUploadImage();
            }
            else
            {
                requestMediaPermission();
            }
        }
        else
        {
            goToUploadImage();
        }
    }

    private void goToUploadImage()
    {
        Intent i = new Intent(mStatusActivity, ImageUploadActivity.class);
        startActivityForResult(i, IMAGE_REQUEST);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent imageIntent) {
        if (requestCode == IMAGE_REQUEST) {
            if(resultCode == Activity.RESULT_OK){
                if(imageIntent != null){
                    if(imageIntent.hasExtra("imageUrl")){
                        //imageBitmap = imageIntent.getStringExtra("imageBitmap");
                        String imageUrl = imageIntent.getStringExtra("imageUrl");
                        if (!horizontalList.isEmpty()){
                            horizontalList.clear();
                            setHorizontalAdapter(horizontalList);
                        }

                        if (isBitmap){
                            horizontalList.add(encodedBitmap);
                            setHorizontalAdapter(horizontalList);
                        } else {
                            encodedImageURL = imageUrl;
                            if (imageUrl != null) {
                                AsyncTask.execute(() -> {
                                    Bitmap mBitmap = ImageUtils.compressBitmap(encodedImageURL);
                                    horizontalList.add(mBitmap);
                                    setHorizontalAdapter(horizontalList);
                                });

                            }

                        }

                    }
                }
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
                    if (horizontal_recycler_view != null)
                        Snackbar.make(horizontal_recycler_view, "Permission Denied, You cannot access media data.", Snackbar.LENGTH_LONG).show();
                    break;
                }
        }
    }

    @Override
    public void onAdapterClicked(int position) {
        if(mUsers != null && mUsers.size() > 0) {
            if(mSelectedUsers != null){
                mSelectedUsers.add(mUsers.get(position));
                isPostStatusEditText = true;
                loadSelectedUsers(mUsers.get(position), true);
            }
            if(call != null){
                call.cancel();
            }
            if(mUsers != null) {
                mUsers.clear();
                mTaggingAdapter.swap(mUsers);
            }
            showStatusLayout();
        }
    }

    private void loadSelectedUsers(Search.SearchData.Users data, boolean toAdd) {

        if(this.mSelectedUsers != null){
                if(postStatusEditText.getText().length() == 0 && this.mSelectedUsers.size() == 0){
                    this.mSelectedUsers.clear();
                    this.spanned.clear();
                }

                SpannableStringBuilder sBuilder = new SpannableStringBuilder();
                String atSymbolRemoved = postStatusEditText.getText().toString();

                if(atSymbolRemoved.contains("@")){
                    //atSymbolRemoved = atSymbolRemoved.replaceAll("\\@","");
                    if(queryingText != null)
                        atSymbolRemoved = atSymbolRemoved.replace("@"+queryingText,"");
                    sBuilder.append(atSymbolRemoved);
                }

                if(data != null && toAdd) {
                    sBuilder.append(data.getName());
                    sBuilder.append(" ");
                    atSymbolRemoved = atSymbolRemoved + data.getName();
                    sBuilder.setSpan(new InternalClickableSpan(data.getName()), atSymbolRemoved.indexOf(data.getName()), atSymbolRemoved.indexOf(data.getName()) + data.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    postStatusEditText.setText(sBuilder);
                    Pattern pattern = Pattern.compile(data.getName());
                    Matcher matcher = pattern.matcher(sBuilder);
                    while (matcher.find()) {
                        System.out.print("Start index: " + matcher.start());
                        System.out.print(" End index: " + matcher.end() + " ");
                        System.out.println(matcher.group());
                        spanned.add(matcher.start()+","+matcher.end());
                    }
                }

                String total = postStatusEditText.getText().toString();
                map.clear();
                builder = new SpannableString(total);
                for (Search.SearchData.Users mData : mSelectedUsers){
                    Pattern pattern = Pattern.compile(mData.getName());
                    Matcher matcher = pattern.matcher(total);
                    while (matcher.find()) {
                        System.out.print("Start index: " + matcher.start());
                        System.out.print(" End index: " + matcher.end() + " ");
                        System.out.println(matcher.group());
                        map.put(mData, new ArrayList<>(Arrays.asList(matcher.start(), matcher.end())));
                        builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        builder.setSpan(new InternalClickableSpan(mData.getName(), matcher.start(), matcher.end()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                if(!toAdd){
                    postStatusEditText.removeTextChangedListener(mTextWatcher);
                }
                postStatusEditText.setText(builder);

                if(!toAdd){
                    postStatusEditText.addTextChangedListener(mTextWatcher);
                }
                isPostStatusEditText = true;
                postStatusEditText.setMovementMethod(LinkMovementMethod.getInstance());
                postStatusEditText.setSelection(builder.length());
        }
    }

    private void loadFromEditPost(Search.SearchData.Users data, boolean toAdd){
        if(this.mSelectedUsers != null) {
            if (postStatusEditText.getText().length() == 0 && this.mSelectedUsers.size() == 0) {
                this.mSelectedUsers.clear();
                this.spanned.clear();
            }
            String total = postStatusEditText.getText().toString();
            map.clear();
            builder = new SpannableString(total);
            for (Search.SearchData.Users mData : mSelectedUsers){
                Pattern pattern = Pattern.compile(mData.getName());
                Matcher matcher = pattern.matcher(total);
                while (matcher.find())
                {
                    System.out.print("Start index: " + matcher.start());
                    System.out.print(" End index: " + matcher.end() + " ");
                    System.out.println(matcher.group());
                    map.put(mData, new ArrayList<>(Arrays.asList(matcher.start(), matcher.end())));
                    builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new InternalClickableSpan(mData.getName(), matcher.start(), matcher.end()), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            postStatusEditText.setText(builder);
            postStatusEditText.setMovementMethod(LinkMovementMethod.getInstance());
            postStatusEditText.setSelection(builder.length());
        }
    }

    @Override
    protected void onDestroy()
    {
        if (searchSubscription != null && !searchSubscription.isUnsubscribed())
            searchSubscription.unsubscribe();

        if (postingFeedsSubscription != null && !postingFeedsSubscription.isUnsubscribed())
            postingFeedsSubscription.unsubscribe();
        encodedImage = "";
        encodedImageURL = "";
        encodedBitmap = null;
        isBitmap = false;
      //  GlideBitmapPool.clearMemory();
        super.onDestroy();
    }

    public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {
        class MyViewHolder extends RecyclerView.ViewHolder {
            private ImageView previewImages, deleteImage;
            private MyViewHolder(View view) {
                super(view);
                previewImages = (ImageView) view.findViewById(R.id.image_preview);
                deleteImage = (ImageView) view.findViewById(R.id.delete_image);
            }
        }
        private HorizontalAdapter(final List<Bitmap> mHorizontalList) {
            horizontalList= mHorizontalList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_recycler_view_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            //File file = new File(horizontalList.get(0));
            //Picasso.with(getBaseContext()).load(encodedBitmap)..placeholder(R.color.fb_background).into(holder.previewImages);
            holder.previewImages.setBackgroundColor(ContextCompat.getColor(mStatusActivity, R.color.fb_background));
            holder.previewImages.setImageBitmap(horizontalList.get(0));
            holder.deleteImage.setOnClickListener(v -> {
                if (horizontalList != null && !horizontalList.isEmpty()) {
                    horizontalList.remove(holder.getAdapterPosition());
                    horizontalAdapter.notifyItemRemoved(holder.getAdapterPosition());
                    encodedImage = "";
                    encodedImageURL = "";
                    encodedBitmap = null;
                    isBitmap = false;
                }

            });
        }

        @Override
        public int getItemCount() {
            return horizontalList.size();
        }
    }

    private class InternalClickableSpan extends ClickableSpan {

        private String clicked;

        InternalClickableSpan(String clickedString) {
            clicked = clickedString;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
            ds.bgColor= Color.parseColor("#aaaaaa");
            ds.setColor(ContextCompat.getColor(mStatusActivity, R.color.black));
        }

        InternalClickableSpan(String clickedString, int start, int end) {
            clicked = clickedString;
        }

        @Override
        public void onClick(View view) {
            Selection.setSelection((Spannable) ((TextView)view).getText(), 0);
        }
    }

}
