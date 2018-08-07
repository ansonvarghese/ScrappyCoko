package com.myscrap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.myscrap.adapters.InterestAdapter;
import com.myscrap.adapters.InterestRoleAdapter;
import com.myscrap.application.AppController;
import com.myscrap.database.MyScrapSQLiteDatabase;
import com.myscrap.model.EditProfile;
import com.myscrap.model.MyItem;
import com.myscrap.model.ProfilePicture;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.ImageUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.view.FlowLayout;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import id.zelory.compressor.Compressor;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EditProfileActivity extends AppCompatActivity implements InterestAdapter.InterestAdapterClickListener, InterestRoleAdapter.InterestRoleAdapterClickListener{

    private SimpleDraweeView userProfile;
    private EditText firstNameText;
    private EditText lastNameText;
    private EditText positionText;
    private AutoCompleteTextView companyText;
    private AutoCompleteTextView locationText;
    private EditText bioText;
    private EditText phoneText;
    private EditText phoneCodeText;
    private EditText webSiteText;
    private EditProfileActivity mEditProfileActivity;
    private List<String> mInterestData = new ArrayList<>();
    private List<String> mInterestRoleData = new ArrayList<>();
    private String encodedImage;
    private EditProfile.EditProfileData mData;
    private List<Integer> selectedArray = new ArrayList<>();
    private List<Integer> selectedRoleArray = new ArrayList<>();
    private LinearLayout totalLayout;
    private MyScrapSQLiteDatabase mMyScrapSQLiteDatabase;
    private List<String> companyList = new ArrayList<>();
    private String selectedCompanyName = "";
    private String selectedCompanyId = "";
    private String countryCodeHint;
    private Tracker mTracker;
    private LinearLayout interest;
    private LinearLayout roles;
    private FlowLayout flowLayout;
    private FlowLayout flowRolesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mEditProfileActivity = this;
        mInterestData.add("Non-Ferrous Metals");
        mInterestData.add("Ferrous Metals");
        mInterestData.add("Stainless Steel");
        mInterestData.add("Tyres");
        mInterestData.add("Paper");
        mInterestData.add("Textiles");
        mInterestData.add("Plastic");
        mInterestData.add("E-Scrap");
        mInterestData.add("Red Metals");
        mInterestData.add("Aluminum");
        mInterestData.add("Zinc");
        mInterestData.add("Magnesium");
        mInterestData.add("Lead");
        mInterestData.add("Nickel/Stainless/Hi Temp");
        mInterestData.add("Mixed Metals");
        mInterestData.add("Electric Furnace Casting and Foundry");
        mInterestData.add("Specially Processed Grades");
        mInterestData.add("Cast Iron Grades");
        mInterestData.add("Special Boring Grades");
        mInterestData.add("Steel From Scrap Tiles");
        mInterestData.add("Railroad Ferrous Scrap");
        mInterestData.add("Stainless Alloy");
        mInterestData.add("Special Alloy");
        mInterestData.add("Copper");
        mInterestData.add("Finance");
        mInterestData.add("Insurance");
        mInterestData.add("Shipping");
        mInterestData.add("Equipments");
        mInterestData.add("Others");


        mInterestRoleData.add("Trader");
        mInterestRoleData.add("Agent");
        mInterestRoleData.add("Recycler");
        mInterestRoleData.add("Exporter");
        mInterestRoleData.add("Stocker");
        mInterestRoleData.add("Equipment");
        mInterestRoleData.add("Service");
        mInterestRoleData.add("Consumer");
        mInterestRoleData.add("Consultant");
        mInterestRoleData.add("Press");
        mInterestRoleData.add("Importer");
        mInterestRoleData.add("Supplier");
        mInterestRoleData.add("Other");

        interest = (LinearLayout) findViewById(R.id.interest);
        roles = (LinearLayout) findViewById(R.id.roles);
        flowLayout = new FlowLayout(this);
        flowRolesLayout = new FlowLayout(this);
        int padding= DeviceUtils.dp2px(this,13);
        flowLayout.setPadding(padding,padding,padding,padding);
        flowRolesLayout.setPadding(padding,padding,padding,padding);

        mTracker = AppController.getInstance().getDefaultTracker();
        if (mMyScrapSQLiteDatabase == null)
            mMyScrapSQLiteDatabase = MyScrapSQLiteDatabase.getInstance(AppController.getInstance());
        List<MyItem> markerList;
        markerList = mMyScrapSQLiteDatabase.getMarkerList();

        for (MyItem item : markerList){
            companyList.add(item.getCompanyName());
        }
        String[] companyListArr = new String[companyList.size()];
        companyListArr = companyList.toArray(companyListArr);

        Locale[] locale = Locale.getAvailableLocales();
        final ArrayList<String> countries = new ArrayList<>();
        String country;
        for( Locale loc : locale ){
            country = loc.getDisplayCountry();
            if( country.length() > 0 && !countries.contains(country) ){
                countries.add( country );
            }
        }
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);

        totalLayout = (LinearLayout) findViewById(R.id.total_layout);
        userProfile = (SimpleDraweeView) findViewById(R.id.userProfile);
        TextView userProfileEdit = (TextView) findViewById(R.id.user_profile_picture_edit);
        firstNameText = (EditText) findViewById(R.id.input_first_name);
        lastNameText = (EditText) findViewById(R.id.input_last_name);
        positionText = (EditText) findViewById(R.id.input_position);
        companyText = (AutoCompleteTextView) findViewById(R.id.input_company);
        CompanyListAdapter mCompanyListAdapter = new CompanyListAdapter(this, markerList);
        companyText.setThreshold(1);
        companyText.setAdapter(mCompanyListAdapter);
        locationText = (AutoCompleteTextView) findViewById(R.id.input_location);
        locationText.setThreshold(1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_country_adapter_item, countries);
        locationText.setAdapter(adapter);
        locationText.setOnItemClickListener((parent, view, position, id) -> {
            final String countrySelected = ((TextView)view).getText().toString().trim();
            String code = getCountryCode(countrySelected);
            countryCodeHint = code;
            phoneCodeText.setText(code);
            locationText.setText(countrySelected);
            locationText.setSelection(locationText.length());
        });
        bioText = (EditText) findViewById(R.id.input_bio);
        phoneText = (EditText) findViewById(R.id.input_phone);
        phoneCodeText = (EditText) findViewById(R.id.input_country_code);
        String code = GetCountryZipCode();
        if(code != null && !code.equalsIgnoreCase("")){
            phoneCodeText.setText("+"+code);
            phoneCodeText.setHint("+"+code);
        }
        webSiteText = (EditText) findViewById(R.id.input_web);
        userProfileEdit.setOnClickListener(v -> doCropAndUpload());
        loadEditDetails();

        positionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(positionText.isFocused() && s.length() >= 19){
                        Toast.makeText(mEditProfileActivity, "You reached maximum text", Toast.LENGTH_SHORT).show();
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        phoneCodeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(phoneCodeText.length() > 0){
                        phoneCodeText.setHint("");
                    } else {
                        if(countryCodeHint != null)
                            phoneCodeText.setHint(countryCodeHint);
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        phoneCodeText.setOnFocusChangeListener((view, hasFocus) -> phoneCodeText.post(new Runnable() {
            @Override
            public void run() {
                if (!hasFocus) {
                    phoneCodeText.setHint("");
                } else {
                    if(countryCodeHint != null)
                        phoneCodeText.setHint(countryCodeHint);
                }
            }
        }));


    }

    private void closeACT(){
        if(companyText != null){
            companyText.post(() -> companyText.dismissDropDown());
        }
    }


    public String GetCountryZipCode(){
        String countryID;
        String countryZipCode="";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            countryID= manager.getSimCountryIso().toUpperCase();
            String[] list=this.getResources().getStringArray(R.array.CountryCodes);
            for(int i=0;i<list.length;i++){
                String[] g=list[i].split(",");
                if(g[1].trim().equals(countryID.trim())){
                    countryZipCode=g[0];
                    break;
                }
            }
        }

        return countryZipCode;
    }

    public String getCountryCode(String countryName) {
        if(countryName == null || countryName.equalsIgnoreCase(""))
            return "";

        String[] isoCountryCodes = Locale.getISOCountries();
        Map<String, String> countryMap = new HashMap<>();
        for (String code : isoCountryCodes) {
            Locale locale = new Locale("", code);
            String name = locale.getDisplayCountry();
            countryMap.put(name, code);
        }
        String phoneCode = null;
        String mobileCode = countryMap.get(countryName);
        if(mobileCode == null)
            return "";
        String[] list=this.getResources().getStringArray(R.array.CountryCodes);
        for (String aList : list) {
            String[] g = aList.split(",");
            if (g[1] != null && g[1].trim().equals(mobileCode.trim())) {
                phoneCode = g[0];
                break;
            }
        }

        if(phoneCode == null)
            return "";

        return "+"+phoneCode;
    }


    private boolean validatePhone() {
        String countryCode = phoneCodeText.getText().toString().trim();
        String phoneNumber = phoneText.getText().toString().trim();
        if(countryCode.length() > 0 && phoneNumber.length() > 0){
            if(isValidPhoneNumber(phoneNumber)){
                boolean status = validateUsingLibraryPhoneNumber(countryCode, phoneNumber);
                if(status){
                    String phone = phoneCodeText.getText().toString() + phoneText.getText().toString();
                    return  true;
                } else {
                    Toast.makeText(this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                    return  false;
                }
            }
            else {
                Toast.makeText(this, "Invalid Phone Number", Toast.LENGTH_LONG).show();
                return  false;
            }
        } else {
            if (countryCode.equalsIgnoreCase("") || countryCode.length() < 0)
                Toast.makeText(getApplicationContext(), "Country Code is required", Toast.LENGTH_SHORT).show();
            else if (phoneNumber.equalsIgnoreCase("") || phoneNumber.length() < 0)
                Toast.makeText(getApplicationContext(), "Phone Number is required", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Country Code and Phone Number is required", Toast.LENGTH_SHORT).show();
            return  false;
        }
    }

    private boolean validateUsingLibraryPhoneNumber(String countryCode, String phoneNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(UserUtils.parsingInteger(countryCode)));
        Phonenumber.PhoneNumber pNumber = null;
        try {
            pNumber = phoneNumberUtil.parse(phoneNumber, isoCode);
        } catch (NumberParseException e) {
            System.err.println(e.toString());
        }
        return phoneNumberUtil.isValidNumber(pNumber);
    }

    private void splitCountryCode(String mValue) {
        if (mValue != null && !mValue.equalsIgnoreCase("") && mValue.startsWith("+")){
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(mValue, "");
                int countryCode = numberProto.getCountryCode();
                long nationalNumber = numberProto.getNationalNumber();
            } catch (NumberParseException e) {
                System.err.println("NumberParseException was thrown: " + e.toString());
            }
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches();
    }


    private void doCropAndUpload()
    {
     //   Intent mIntent = new Intent(mEditProfileActivity, ImageUploadActivity.class);
        Intent mIntent = new Intent(mEditProfileActivity, ProfileUploadActivity.class);
        startActivity(mIntent);
    }

    private void loadEditDetails() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ProgressBarDialog.showLoader(mEditProfileActivity, false);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(mEditProfileActivity);
            Call<EditProfile> call = apiService.editProfile(userId,apiKey);
            call.enqueue(new Callback<EditProfile>() {
                @Override
                public void onResponse(@NonNull Call<EditProfile> call, @NonNull Response<EditProfile> response) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("editProfile", "onSuccess");
                    if (response.body() != null && response.isSuccessful()) {
                        EditProfile editProfile = response.body();
                        if(editProfile != null && !editProfile.isErrorStatus()){
                            if(editProfile.getData() != null) {
                                mData = editProfile.getData();
                                if(mData != null) {
                                    if(totalLayout != null)
                                        totalLayout.setVisibility(View.VISIBLE);
                                    if(mData.getProfilePic() != null && !mData.getProfilePic().equalsIgnoreCase("")){

                                        if(mData.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/profile.png")
                                                || mData.getProfilePic().equalsIgnoreCase("https://myscrap.com/style/images/icons/no-profile-pic-female.png")){
                                            /*Glide.with(AppController.getInstance()).load(R.drawable.no_profiles)
                                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                    .transform(new CircleTransform(mEditProfileActivity))
                                                    .into(userProfile);*/

                                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                                            userProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                                                    .setRoundingParams(roundingParams)
                                                    .build());
                                            roundingParams.setRoundAsCircle(true);
                                            userProfile.setImageResource(R.drawable.no_profiles);


                                        } else {
                                            Uri uri = Uri.parse(mData.getProfilePic());
                                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                                            userProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                                                    .setRoundingParams(roundingParams)
                                                    .build());
                                            roundingParams.setRoundAsCircle(true);
                                            userProfile.setImageURI(uri);
                                        }

                                    }

                                    if(mData.getFirstName() != null){
                                        firstNameText.setText(mData.getFirstName());
                                        UserUtils.saveFirstName(mEditProfileActivity, mData.getFirstName());
                                    }


                                    if(mData.getLastName() != null){
                                        lastNameText.setText(mData.getLastName());
                                        UserUtils.saveLastName(mEditProfileActivity, mData.getLastName());
                                    }


                                    if(mData.getDesignation()!= null)
                                        positionText.setText(mData.getDesignation());

                                    if(mData.getCompany() != null)
                                        companyText.setText(mData.getCompany());

                                    if(mData.getUserLocation() != null)
                                        locationText.setText(mData.getUserLocation());


                                    if(mData.getUserBio() != null)
                                        bioText.setText(mData.getUserBio());

                                    if(mData.getPhoneNo() != null){
                                        phoneText.setText(mData.getPhoneNo());
                                    }

                                    if(mData.getCode() != null){
                                        if(mData.getCode().equalsIgnoreCase("")){
                                            phoneCodeText.setHint("");
                                        } else {
                                            phoneCodeText.setText(mData.getCode());
                                            phoneCodeText.setHint("");
                                        }

                                    }

                                    countryCodeHint = getCountryCode(mData.getUserLocation().trim());

                                    if(mData.getWebsite() != null){
                                        webSiteText.setText(mData.getWebsite());
                                    }

                                    if(mData.getUserInterest() != null){
                                        if(!mData.getUserInterest().equalsIgnoreCase("")){
                                            String selected = mData.getUserInterest();
                                            String[] sArray = selected.split("\\s*,\\s*");
                                            if(sArray.length > 0){
                                                for (String aSArray : sArray) {
                                                    selectedArray.add(Integer.valueOf(aSArray));
                                                }
                                            }
                                            Set<Integer> hashSet = new HashSet<>();
                                            hashSet.addAll(selectedArray);
                                            selectedArray.clear();
                                            selectedArray.addAll(hashSet);

                                            if(mInterestData != null && selectedArray != null && selectedArray.size() > 0){
                                                for (int i=0; i < mInterestData.size(); i++ ){
                                                    final TextView textView = new TextView(getApplicationContext());
                                                    if(selectedArray.contains(mInterestData.indexOf(mInterestData.get(i)))){
                                                        textView.setTag("clicked");
                                                        textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest_selected));
                                                        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                    } else {
                                                        textView.setTag("click");
                                                        textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest));
                                                        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.subPrimaryText));
                                                    }
                                                    textView.setId(i);
                                                    textView.setText(mInterestData.get(i));
                                                    textView.setGravity(Gravity.CENTER);
                                                    textView.setPadding(10,15,10,15);
                                                    textView.setOnClickListener(v -> {
                                                        if(v.getTag().equals("click")){
                                                            textView.setTag("clicked");
                                                            textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest_selected));
                                                            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                            Log.d("onInterestClicked: ", ""+textView.getId());
                                                            if(!selectedArray.contains(textView.getId())){
                                                                selectedArray.add(textView.getId());
                                                            } else {
                                                                selectedArray.remove(selectedArray.indexOf(textView.getId()));
                                                            }
                                                        } else {
                                                            v.setTag("click");
                                                            Log.d("onInterestClicked: ", ""+textView.getId());
                                                            if(!selectedArray.contains(textView.getId())){
                                                                selectedArray.add(textView.getId());
                                                            } else {
                                                                selectedArray.remove(selectedArray.indexOf(textView.getId()));
                                                            }
                                                            textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest));
                                                            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.subPrimaryText));
                                                        }
                                                    });
                                                    flowLayout.addView(textView);
                                                }
                                                interest.removeAllViews();
                                                interest.addView(flowLayout);
                                            }
                                        }
                                    }
                                    if(mData.getUserInterestRoles() != null){
                                        if(!mData.getUserInterestRoles().equalsIgnoreCase("")){
                                            String selected = mData.getUserInterestRoles();
                                            String[] sArray = selected.split("\\s*,\\s*");
                                            if(sArray.length > 0){
                                                for (String aSArray : sArray) {
                                                    selectedRoleArray.add(Integer.valueOf(aSArray));
                                                }
                                            }
                                            Set<Integer> hashSet = new HashSet<>();
                                            hashSet.addAll(selectedRoleArray);
                                            selectedRoleArray.clear();
                                            selectedRoleArray.addAll(hashSet);

                                            if(mInterestRoleData != null && selectedRoleArray != null && selectedRoleArray.size() > 0){
                                                for (int i=0; i < mInterestRoleData.size(); i++ ){
                                                    final TextView textView = new TextView(getApplicationContext());
                                                    if(selectedRoleArray.contains(mInterestRoleData.indexOf(mInterestRoleData.get(i)))){
                                                        textView.setTag("clicked");
                                                        textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest_selected));
                                                        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                    } else {
                                                        textView.setTag("click");
                                                        textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest));
                                                        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.subPrimaryText));
                                                    }
                                                    textView.setId(i);
                                                    textView.setText(mInterestRoleData.get(i));
                                                    textView.setGravity(Gravity.CENTER);
                                                    textView.setPadding(10,15,10,15);
                                                    textView.setOnClickListener(v -> {
                                                        if(v.getTag().equals("click")){
                                                            textView.setTag("clicked");
                                                            textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest_selected));
                                                            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                            Log.d("onInterestClicked: ", ""+textView.getId());
                                                            if(!selectedRoleArray.contains(textView.getId())){
                                                                selectedRoleArray.add(textView.getId());
                                                            } else {
                                                                selectedRoleArray.remove(selectedRoleArray.indexOf(textView.getId()));
                                                            }
                                                        } else {
                                                            v.setTag("click");
                                                            Log.d("onInterestClicked: ", ""+textView.getId());
                                                            if(!selectedRoleArray.contains(textView.getId())){
                                                                selectedRoleArray.add(textView.getId());
                                                            } else {
                                                                selectedRoleArray.remove(selectedRoleArray.indexOf(textView.getId()));
                                                            }
                                                            textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest));
                                                            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.subPrimaryText));
                                                        }
                                                    });
                                                    flowRolesLayout.addView(textView);
                                                }
                                                roles.removeAllViews();
                                                roles.addView(flowRolesLayout);
                                            }
                                        }
                                    }


                                    Map<String, Boolean> trueSet = new HashMap<>();
                                    trueSet.put("Profile", mData.isProfilePictureNeed());
                                    trueSet.put("Bio", mData.isBioNeed());
                                    trueSet.put("Company Name", mData.isCompanyNeed());
                                    trueSet.put("Country", mData.isCountryNeed());
                                    trueSet.put("Designation", mData.isDesignationNeed());
                                    trueSet.put("Interests", mData.isInterestsNeed());
                                    trueSet.put("Roles", mData.isRolesNeed());
                                    trueSet.put("Website", mData.isWebsiteNeed());
                                    trueSet.put("Phone Number", mData.isPhoneNeed());
                                    Set<Map.Entry<String, Boolean>> entrySet = trueSet.entrySet();
                                    List<String> toDisplayTip = new ArrayList<>();

                                    for(Map.Entry<String, Boolean> e : entrySet) {
                                        String key = e.getKey();
                                        boolean value = e.getValue();
                                        if (value) {
                                            toDisplayTip.add(key);
                                        }
                                    }


                                    if (!toDisplayTip.isEmpty()) {
                                        for (String toolTip : toDisplayTip){
                                            if (toolTip.equalsIgnoreCase("Profile")) {
                                                showHint(userProfile, "Add profile picture");
                                            } else if (toolTip.equalsIgnoreCase("Bio")) {
                                                bioText.setError("Add your bio");
                                            } else if (toolTip.equalsIgnoreCase("Company Name")) {
                                                companyText.setError("Add your company name");
                                            } else if (toolTip.equalsIgnoreCase("Country")) {
                                                locationText.setError("Add your country name");
                                            } else if (toolTip.equalsIgnoreCase("Designation")) {
                                                positionText.setError("Add your designation");
                                            } else if (toolTip.equalsIgnoreCase("Interests")) {
                                                showHint(interest, "Select your interests");
                                            } else if (toolTip.equalsIgnoreCase("Roles")) {
                                                 showHint(roles, "Select your roles");
                                            } else if (toolTip.equalsIgnoreCase("Website")) {
                                                 webSiteText.setError("Add your website");
                                            } else if (toolTip.equalsIgnoreCase("Phone Number")) {
                                                phoneText.setError("Add your phone number");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<EditProfile> call, @NonNull Throwable t) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("editProfile", "onFailure");

                }
            });
        } else {
            if(flowLayout != null)
                SnackBarDialog.showNoInternetError(flowLayout);
        }
    }

    private void showHint(final View view, String message) {

        if (view == null)
            return;

        new SimpleTooltip.Builder(this)
                .anchorView(view)
                .text(message)
                .gravity(Gravity.BOTTOM)
                .animated(false)
                .build()
                .show();
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
            if(phoneText.getText().toString().equalsIgnoreCase("") && phoneCodeText.getText().toString().equalsIgnoreCase("")){
                uploadToServer();
            } else {
                //boolean status = validatePhone();
                //if(status)
                    uploadToServer();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadToServer() {

        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;

        if(mData != null){
            String firstName = firstNameText.getText().toString().trim();
            String lastName = lastNameText.getText().toString().trim();
            String position = positionText.getText().toString().trim();
            String location = locationText.getText().toString().trim();
            String company;

            if(selectedCompanyName != null && !selectedCompanyName.equalsIgnoreCase("")){
                if(selectedCompanyName.equalsIgnoreCase(companyText.getText().toString())){
                    company = selectedCompanyName;
                } else {
                    company = companyText.getText().toString().trim();
                }
            } else {
                company = companyText.getText().toString().trim();
            }

            String companyId;
            if(selectedCompanyId != null && !selectedCompanyId.equalsIgnoreCase("")){
                companyId = selectedCompanyId;
            } else {
                if(mData.getCompanyId() != null && !mData.getCompanyId().equalsIgnoreCase("")){
                    companyId = mData.getCompanyId();
                } else {
                    companyId = "";
                }
            }

            String bio = bioText.getText().toString().trim();
            String phone = phoneText.getText().toString().trim();
            String code = phoneCodeText.getText().toString().trim();
            String webSite = webSiteText.getText().toString().trim();


            Set<Integer> hashSet = new HashSet<>();
            hashSet.addAll(selectedArray);
            selectedArray.clear();
            selectedArray.addAll(hashSet);

            Set<Integer> hashSetRoles = new HashSet<>();
            hashSetRoles.addAll(selectedRoleArray);
            selectedRoleArray.clear();
            selectedRoleArray.addAll(hashSetRoles);

            String interest = TextUtils.join(", ", selectedArray);
            String roles = TextUtils.join(", ", selectedRoleArray);

            if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                ProgressBarDialog.showLoader(mEditProfileActivity, false);
                ApiInterface apiService =
                        ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
                String userId = AppController.getInstance().getPrefManager().getUser().getId();
                String apiKey = UserUtils.getApiKey(mEditProfileActivity);
                Call<EditProfile> call = apiService.editProfile(userId,firstName,lastName,"", position, company,companyId,bio,"",interest,roles,phone,code,webSite,location,apiKey);
                call.enqueue(new Callback<EditProfile>() {
                    @Override
                    public void onResponse(@NonNull Call<EditProfile> call, @NonNull Response<EditProfile> response) {
                        ProgressBarDialog.dismissLoader();
                        Log.d("editProfile", "onSuccess");
                        goToHome();
                        if (response.body() != null && response.isSuccessful()) {
                            EditProfile editProfile = response.body();
                            if(editProfile != null && !editProfile.isErrorStatus()){
                                mEditProfileActivity.finish();
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<EditProfile> call, @NonNull Throwable t) {
                        ProgressBarDialog.dismissLoader();
                        Log.d("editProfile", "onFailure");

                    }
                });
            } else {
                if(flowLayout != null)
                    SnackBarDialog.showNoInternetError(flowLayout);
            }
        }
    }

    private void goToHome() {
        Intent i = new Intent(this, UserProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        this.finish();
    }

    @Override
    public void onInterestClicked(View v, int position) {
        Log.d("onInterestClicked: ", ""+position);
        if(!selectedArray.contains(position)){
            selectedArray.add(position);
        } else {
            selectedArray.remove(selectedArray.indexOf(position));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            File file = new File(Crop.getOutput(result).getPath());
            Compressor.getDefault(mEditProfileActivity)
                    .compressToFileAsObservable(file)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file1 -> {
                        encodedImage = convertToBitmap(file1.getAbsolutePath());
                        changeProfile(encodedImage);
                        Log.d("compressed ", String.format("Size : %s", getReadableFileSize(file1.length())));
                    }, throwable -> {
                        showError(throwable.getMessage());
                        /*Glide.with(AppController.getInstance()).load(R.drawable.image_overlay)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(new CircleTransform(mEditProfileActivity))
                                .into(userProfile);*/
                        userProfile.post(() -> {
                            Uri uri = new Uri.Builder()
                                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                                    .path(String.valueOf(R.drawable.image_overlay))
                                    .build();
                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                            userProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                                    .setRoundingParams(roundingParams)
                                    .build());
                            roundingParams.setRoundAsCircle(true);
                            userProfile.setImageURI(uri);
                        });


                    });

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void changeProfile(String profilePic) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ProgressBarDialog.showLoader(mEditProfileActivity, false);
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
                        UserUtils.saveUserProfilePicture(mEditProfileActivity, mProfilePicture.getProfilePictureUrl());
                        /*Glide.with(AppController.getInstance()).load(mProfilePicture.getProfilePictureUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(new CircleTransform(mEditProfileActivity))
                                .into(userProfile);*/
                        updateProfile(mProfilePicture.getProfilePictureUrl());
                    }
                }
                ProgressBarDialog.dismissLoader();
            }
            @Override
            public void onFailure(@NonNull Call<ProfilePicture> call, @NonNull Throwable t) {
                Log.d("changeProfile", "onFailure");
                ProgressBarDialog.dismissLoader();
            }
        });
    }

    private void updateProfile(String profilePictureUrl) {
       if (userProfile != null) {
           userProfile.post(() -> {
               Uri uri = Uri.parse(profilePictureUrl);
               RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
               userProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                       .setRoundingParams(roundingParams)
                       .build());
               roundingParams.setRoundAsCircle(true);
               userProfile.setImageURI(uri);
           });
       }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppController.runOnUIThread(() -> {
            String profile = UserUtils.getUserProfilePicture(mEditProfileActivity);
            if( profile != null && !profile.equalsIgnoreCase("")){
                updateProfile(profile);
            }
        });
        if(mTracker != null){
            mTracker.setScreenName("User Edit Profile Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(EditProfileActivity.this,UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(EditProfileActivity.this,UserOnlineStatus.OFFLINE);
    }

    private String convertToBitmap(String realPath) {
        String encodedImage = ImageUtils.compressImage(realPath);
        if (encodedImage != null)
            return encodedImage;
        return null;
    }

    private void showError(String errorMessage) {
        Toast.makeText(mEditProfileActivity, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public void onInterestRoleClicked(View v, int position) {
        Log.d("onInterestRoleClicked: ", ""+position);
        if(!selectedRoleArray.contains(position)){
            selectedRoleArray.add(position);
        } else {
            selectedRoleArray.remove(selectedRoleArray.indexOf(position));
        }
    }


    private class CompanyListAdapter extends ArrayAdapter<MyItem>{
        private final List<MyItem> companies;
        private List<MyItem> filteredCompanies = new ArrayList<>();

        public CompanyListAdapter(Context context, List<MyItem> companies) {
            super(context,0, companies);
            this.companies = companies;
        }

        @Override
        public int getCount() {
            return filteredCompanies.size();
        }

        public MyItem getItem(int position) {
            return filteredCompanies.get(position);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Company(this, companies);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            MyItem data = filteredCompanies.get(position);
            View row = convertView;
            ItemViewHolder holder;
            if(row == null){
                LayoutInflater inflater = LayoutInflater.from(getContext());
                row = inflater.inflate(R.layout.select_dialog_item, parent, false);
                holder = new ItemViewHolder(row);
                row.setTag(holder);
            }else {
                holder = (ItemViewHolder)row.getTag();
            }

            if(data.getCompanyImage() != null){
                if (holder.profileImage != null) {
                    holder.profileImage.post(() -> {
                        Uri uri = Uri.parse(data.getCompanyImage());
                        RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                        userProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                                .setRoundingParams(roundingParams)
                                .build());
                        roundingParams.setRoundAsCircle(true);
                        holder.profileImage.setImageURI(uri);
                    });
                    //Glide.with(getContext()).load(data.getCompanyImage()).transform(new CircleTransform(getContext())).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.profileImage);
                }
            }

            if(data.getCompanyName() != null) {
                holder.profileName.setText(data.getCompanyName());
            }
            holder.companyType.setVisibility(View.VISIBLE);
            holder.companyType.setText("Recycler");
            if(data.getCompanyCountry()!= null && !data.getCompanyCountry().equalsIgnoreCase("")){
                holder.country.setText(data.getCompanyCountry());
                holder.country.setVisibility(View.VISIBLE);
            } else {
                holder.country.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                if(filteredCompanies.size() > 0 && position != -1){
                    selectedCompanyName = filteredCompanies.get(position).getCompanyName();
                    companyText.setText(selectedCompanyName);
                    companyText.setSelection(selectedCompanyName.length());
                    selectedCompanyId = filteredCompanies.get(position).getMarkerId();
                }
                closeACT();
            });

            holder.rootView.setOnClickListener(v -> {
                if(filteredCompanies.size() > 0 && position != -1){
                    selectedCompanyName = filteredCompanies.get(position).getCompanyName();
                    companyText.setText(selectedCompanyName);
                    companyText.setSelection(selectedCompanyName.length());
                    selectedCompanyId = filteredCompanies.get(position).getMarkerId();
                }
                closeACT();
            });

            holder.messageContainer.setOnClickListener(v -> {
                if(filteredCompanies.size() > 0 && position != -1){
                    selectedCompanyName = filteredCompanies.get(position).getCompanyName();
                    companyText.setText(selectedCompanyName);
                    companyText.setSelection(selectedCompanyName.length());
                    selectedCompanyId = filteredCompanies.get(position).getMarkerId();
                }
                closeACT();
            });

            holder.iconFront.setOnClickListener(v -> {
                if(filteredCompanies.size() > 0 && position != -1){
                    selectedCompanyName = filteredCompanies.get(position).getCompanyName();
                    companyText.setText(selectedCompanyName);
                    companyText.setSelection(selectedCompanyName.length());
                    selectedCompanyId = filteredCompanies.get(position).getMarkerId();
                }
                closeACT();
            });

            return row;
        }

        private class Company extends Filter {
            CompanyListAdapter adapter;
            List<MyItem> originalList;
            List<MyItem> filteredList;
            public Company(CompanyListAdapter adapter, List<MyItem> originalList) {
                super();
                this.adapter = adapter;
                this.originalList = originalList;
                this.filteredList = new ArrayList<>();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                filteredList.clear();
                final FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(originalList);
                } else {
                    final String filterPattern = constraint.toString().toLowerCase().trim();
                    for (final MyItem dog : originalList) {
                        if (dog.getCompanyName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(dog);
                        }
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                adapter.filteredCompanies.clear();
                adapter.filteredCompanies.addAll((List) results.values);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout rootView, messageContainer, iconFront;
        private ImageView profileImage;
        private TextView profileName;
        private TextView companyType;
        private TextView country;
        private TextView iconText;


        public ItemViewHolder(View itemView) {
            super(itemView);
            rootView = (RelativeLayout) itemView.findViewById(R.id.root_view);
            messageContainer = (RelativeLayout) itemView.findViewById(R.id.message_container);
            iconFront = (RelativeLayout) itemView.findViewById(R.id.icon_front);
            profileImage = (ImageView) itemView.findViewById(R.id.icon_profile);
            iconText = (TextView) itemView.findViewById(R.id.icon_text);
            profileName = (TextView) itemView.findViewById(R.id.company_name);
            companyType = (TextView) itemView.findViewById(R.id.company_type);
            country = (TextView) itemView.findViewById(R.id.country);
        }
    }
}
