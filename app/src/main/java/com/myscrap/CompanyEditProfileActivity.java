package com.myscrap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.myscrap.model.CompanyEditProfile;
import com.myscrap.model.CompanyProfilePicture;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CompanyEditProfileActivity extends AppCompatActivity implements InterestAdapter.InterestAdapterClickListener, InterestRoleAdapter.InterestRoleAdapterClickListener{

    private SimpleDraweeView userProfile;
    private EditText firstNameText;
    private EditText positionText;
    private AutoCompleteTextView locationText;
    private EditText bioText;
    private EditText phoneText;
    private EditText phoneCodeText;
    private EditText webSiteText;
    private CompanyEditProfileActivity mCompanyEditProfileActivity;
    private List<String> mInterestData = new ArrayList<>();
    private List<String> mInterestRoleData = new ArrayList<>();
    private List<String> mInterestAffiliationData = new ArrayList<>();
    private List<Integer> mInterestSelectedData = new ArrayList<>();
    private List<Integer> mInterestRoleSelectedData = new ArrayList<>();
    private List<Integer> mInterestAffiliationSelectedData = new ArrayList<>();
    private String encodedImage;
    private CompanyEditProfile.CompanyEditProfileData mData;
    private List<Integer> selectedArray = new ArrayList<>();
    private List<Integer> selectedRoleArray = new ArrayList<>();
    private List<Integer> selectedAffiliationArray = new ArrayList<>();
    private LinearLayout totalLayout;
    private String companyId;
    private String countryCodeHint;
    private Tracker mTracker;
    private LinearLayout interest;
    private LinearLayout roles;
    private LinearLayout affiliation;
    private FlowLayout flowLayout;
    private FlowLayout flowRolesLayout;
    private FlowLayout flowAffiliationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
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
        mInterestRoleData.add("Indentor");
        mInterestRoleData.add("Recycler");
        mInterestRoleData.add("Exporter");
        mInterestRoleData.add("Stocker");
        mInterestRoleData.add("Equipment");
        mInterestRoleData.add("Service");
        mInterestRoleData.add("Consumer");
        mInterestRoleData.add("Consultant");
        mInterestRoleData.add("Importer");
        mInterestRoleData.add("Press");
        mInterestRoleData.add("Supplier");
        mInterestRoleData.add("Media");
        mInterestRoleData.add("Other");

        mInterestAffiliationData.add("BIR");
        mInterestAffiliationData.add("ISRI");
        mInterestAffiliationData.add("BMR");
        mInterestAffiliationData.add("CMRA");
        mInterestAffiliationData.add("MRAI");

        mCompanyEditProfileActivity = this;
        mTracker = AppController.getInstance().getDefaultTracker();
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
        interest = (LinearLayout) findViewById(R.id.interest);
        roles = (LinearLayout) findViewById(R.id.roles);
        affiliation = (LinearLayout) findViewById(R.id.affiliation);
        totalLayout = (LinearLayout) findViewById(R.id.total_layout);
        userProfile = (SimpleDraweeView) findViewById(R.id.userProfile);
        TextView userProfileEdit = (TextView) findViewById(R.id.user_profile_picture_edit);
        firstNameText = (EditText) findViewById(R.id.input_first_name);
        positionText = (EditText) findViewById(R.id.input_position);
        locationText = (AutoCompleteTextView) findViewById(R.id.input_location);
        locationText.setThreshold(1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_country_adapter_item, countries);
        locationText.setAdapter(adapter);
        locationText.setOnItemClickListener((parent, view, position, id) -> {
            final String countrySelected = ((TextView)view).getText().toString();
            String code = getCountryCode(countrySelected);
            countryCodeHint = code;
            phoneCodeText.setText(code);
            locationText.setText(countrySelected);
            locationText.setSelection(locationText.length());
        });
        bioText = (EditText) findViewById(R.id.input_bio);
        phoneText = (EditText) findViewById(R.id.input_phone);
        phoneCodeText = (EditText) findViewById(R.id.input_country_code);
        TextInputLayout countryCodeHintContainer = (TextInputLayout) findViewById(R.id.countryCodeHintContainer);
        String code = GetCountryZipCode();
        if(code != null && !code.equalsIgnoreCase("")){
            phoneCodeText.setText("+"+code);
            phoneCodeText.setHint("+"+code);
        }
        webSiteText = (EditText) findViewById(R.id.input_web);

        userProfileEdit.setOnClickListener(v -> goToProfileEdit(companyId));

        TextView editLocation = (TextView) findViewById(R.id.click_here);


        positionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(positionText.isFocused() && s.length() >= 19){
                    Toast.makeText(mCompanyEditProfileActivity, "You reached maximum text", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editLocation.setOnClickListener(v -> {
            if(CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
                openMapDialog(v);
            else
                SnackBarDialog.showNoInternetError(v);
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

    private void goToProfileEdit(String companyId) {
        Intent i =new Intent(this, CompanyProfileUploadActivity.class);
        i.putExtra("companyId", companyId);
        startActivity(i);
    }

    private void openMapDialog(View v) {
        Intent i =new Intent(this, CompanyUpdateMarkerLocationActivity.class);
        i.putExtra("companyId", companyId);
        i.putExtra("lat", mData.getCompanyLatitude());
        i.putExtra("lng", mData.getCompanyLongitude());
        startActivity(i);
    }

    public String GetCountryZipCode(){
        String countryID;
        String countryZipCode="";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            countryID= manager.getSimCountryIso().toUpperCase();
            String[] list=this.getResources().getStringArray(R.array.CountryCodes);
            for (String aList : list) {
                String[] g = aList.split(",");
                if (g[1].trim().equals(countryID.trim())) {
                    countryZipCode = g[0];
                    break;
                }
            }
        }
        return countryZipCode;
    }

    public String getCountryCode(String countryName) {
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
                    SnackBarDialog.show(phoneCodeText, "Invalid Phone Number");
                    return  false;
                }
            }
            else {
                SnackBarDialog.show(phoneCodeText, "Invalid Phone Number");
                return  false;
            }
        } else {
            if (countryCode.equalsIgnoreCase("") || countryCode.length() < 0){
                SnackBarDialog.show(phoneCodeText, "Country code is required");
                return  false;
            } else if (phoneNumber.equalsIgnoreCase("") || phoneNumber.length() < 0) {
                SnackBarDialog.show(phoneCodeText, "Phone number is required");
                return  false;
            } else {
                SnackBarDialog.show(phoneCodeText, "Country code and phone number is required");
                return false;
            }
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
        return pNumber != null && phoneNumberUtil.isValidNumber(pNumber);
    }

    private void splitCountryCode(String mValue) {
        if (mValue != null && !mValue.equalsIgnoreCase("") && mValue.startsWith("+")){
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(mValue, "");
                int countryCode = numberProto.getCountryCode();
                long nationalNumber = numberProto.getNationalNumber();
                if(countryCode !=0){
                    phoneCodeText.setText("+"+countryCode);
                }
                if(nationalNumber != 0){
                    phoneText.setText(""+nationalNumber);
                }
            } catch (NumberParseException e) {
                System.err.println("NumberParseException was thrown: " + e.toString());
            }
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches();
    }

    private void loadEditDetails() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
            ProgressBarDialog.showLoader(mCompanyEditProfileActivity, false);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(mCompanyEditProfileActivity);
            Call<CompanyEditProfile> call = apiService.companyEditProfile(userId,companyId,apiKey);
            call.enqueue(new Callback<CompanyEditProfile>() {
                @Override
                public void onResponse(@NonNull Call<CompanyEditProfile> call, @NonNull Response<CompanyEditProfile> response) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("editProfile", "onSuccess");
                    if (response.body() != null && response.isSuccessful()) {
                        CompanyEditProfile editProfile = response.body();
                        if(editProfile != null && !editProfile.isErrorStatus()){
                            if(editProfile.getData() != null) {
                                mData = editProfile.getData();
                                if(mData != null) {
                                    flowLayout = new FlowLayout(mCompanyEditProfileActivity);
                                    flowRolesLayout = new FlowLayout(mCompanyEditProfileActivity);
                                    flowAffiliationLayout = new FlowLayout(mCompanyEditProfileActivity);
                                    int padding= DeviceUtils.dp2px(mCompanyEditProfileActivity,13);
                                    flowLayout.setPadding(padding,padding,padding,padding);
                                    flowRolesLayout.setPadding(padding,padding,padding,padding);
                                    flowAffiliationLayout.setPadding(padding,padding,padding,padding);

                                    if(totalLayout != null)
                                        totalLayout.setVisibility(View.VISIBLE);
                                    if(mData.getCompanyProfilePic() != null && !mData.getCompanyProfilePic().equalsIgnoreCase("")){
                                        /*Glide.with(AppController.getInstance()).load(mData.getCompanyProfilePic())
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .transform(new CircleTransform(mCompanyEditProfileActivity))
                                                .into(userProfile);*/

                                        userProfile.post(() -> {
                                            Uri uri = Uri.parse(mData.getCompanyProfilePic());
                                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                                            userProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                                                    .setRoundingParams(roundingParams)
                                                    .build());
                                            roundingParams.setRoundAsCircle(true);
                                            userProfile.setImageURI(uri);
                                        });
                                    }

                                    if(mData.getCompanyName()!= null)
                                        firstNameText.setText(mData.getCompanyName());

                                    if(mData.getCompanyLocation() != null)
                                        locationText.setText(mData.getCompanyLocation());

                                    if(mData.getCompanyBio() != null)
                                        bioText.setText(mData.getCompanyBio());

                                    if(mData.getPhoneNo() != null){
                                        phoneText.setText(mData.getPhoneNo());
                                        if(mData.getPhoneNo().contains("+")){
                                            splitCountryCode(mData.getPhoneNo());
                                        }
                                    }


                                    if(mData.getCode() != null && !mData.getCode().equalsIgnoreCase("")){
                                        phoneCodeText.setText(mData.getCode());
                                    }

                                    countryCodeHint = getCountryCode(mData.getCompanyLocation().trim());
                                    if(countryCodeHint != null)
                                        phoneCodeText.setText(countryCodeHint);

                                    if(mData.getWebsite() != null){
                                        webSiteText.setText(mData.getWebsite());
                                    }

                                    if(mData.getCompanyInterest() != null){
                                        if(!mData.getCompanyInterest().equalsIgnoreCase("")){
                                            selectedArray.clear();
                                            mInterestSelectedData.clear();
                                            String selected = mData.getCompanyInterest();
                                            String[] sArray = selected.split("\\s*,\\s*");
                                            if(sArray.length > 0){

                                                for (String aSArray : sArray) {
                                                    mInterestSelectedData.add(Integer.valueOf(aSArray));
                                                    selectedArray.add(Integer.valueOf(aSArray));
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
                                    }

                                    if(mData.getUserInterestRoles() != null){
                                        if(!mData.getUserInterestRoles().equalsIgnoreCase("")){
                                            selectedRoleArray.clear();
                                            mInterestRoleSelectedData.clear();
                                            String selected = mData.getUserInterestRoles();
                                            String[] sArray = selected.split("\\s*,\\s*");
                                            if(sArray.length > 0){
                                                for (String aSArray : sArray) {
                                                    mInterestRoleSelectedData.add(Integer.valueOf(aSArray));
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


                                    if(mData.getCompanyAffiliation() == null)
                                        mData.setCompanyAffiliation("0");

                                    if(mData.getCompanyAffiliation() != null){
                                        if(!mData.getCompanyAffiliation().equalsIgnoreCase("")){
                                            selectedAffiliationArray.clear();
                                            mInterestAffiliationSelectedData.clear();
                                            String selected = mData.getCompanyAffiliation();
                                            String[] sArray = selected.split("\\s*,\\s*");
                                            if(sArray.length > 0){
                                                for (String aSArray : sArray) {
                                                    mInterestAffiliationSelectedData.add(Integer.valueOf(aSArray));
                                                    selectedAffiliationArray.add(Integer.valueOf(aSArray));
                                                }
                                            }

                                            Set<Integer> hashSet = new HashSet<>();
                                            hashSet.addAll(selectedAffiliationArray);
                                            selectedAffiliationArray.clear();
                                            selectedAffiliationArray.addAll(hashSet);

                                            if(mInterestAffiliationData != null && selectedAffiliationArray != null && selectedAffiliationArray.size() > 0){
                                                for (int i=0; i < mInterestAffiliationData.size(); i++ ){
                                                    final TextView textView = new TextView(getApplicationContext());
                                                    if(selectedAffiliationArray.contains(mInterestAffiliationData.indexOf(mInterestAffiliationData.get(i)))){
                                                        textView.setTag("clicked");
                                                        textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest_selected));
                                                        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                    } else {
                                                        textView.setTag("click");
                                                        textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest));
                                                        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.subPrimaryText));
                                                    }
                                                    textView.setId(i);
                                                    textView.setText(mInterestAffiliationData.get(i));
                                                    textView.setGravity(Gravity.CENTER);
                                                    textView.setPadding(10,15,10,15);
                                                    textView.setOnClickListener(v -> {
                                                        if(v.getTag().equals("click")){
                                                            textView.setTag("clicked");
                                                            textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest_selected));
                                                            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                                            Log.d("onInterestClicked: ", ""+textView.getId());
                                                            if(!selectedAffiliationArray.contains(textView.getId())){
                                                                selectedAffiliationArray.add(textView.getId());
                                                            } else {
                                                                selectedAffiliationArray.remove(selectedAffiliationArray.indexOf(textView.getId()));
                                                            }
                                                        } else {
                                                            v.setTag("click");
                                                            Log.d("onInterestClicked: ", ""+textView.getId());
                                                            if(!selectedAffiliationArray.contains(textView.getId())){
                                                                selectedAffiliationArray.add(textView.getId());
                                                            } else {
                                                                selectedAffiliationArray.remove(selectedAffiliationArray.indexOf(textView.getId()));
                                                            }
                                                            textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.interest));
                                                            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.subPrimaryText));
                                                        }
                                                    });
                                                    flowAffiliationLayout.addView(textView);
                                                }
                                                affiliation.removeAllViews();
                                                affiliation.addView(flowAffiliationLayout);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<CompanyEditProfile> call, @NonNull Throwable t) {
                    ProgressBarDialog.dismissLoader();
                    Log.d("editProfile", "onFailure");

                }
            });
        } else {
            if(flowLayout != null)
                SnackBarDialog.showNoInternetError(flowLayout);
        }
    }

    private void uploadToServer() {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if(mData != null){

            String firstName = firstNameText.getText().toString().trim();
            String position = positionText.getText().toString().trim();
            String location = locationText.getText().toString().trim();
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

            Set<Integer> hashSetAffiliation = new HashSet<>();
            hashSetAffiliation.addAll(selectedAffiliationArray);
            selectedAffiliationArray.clear();
            selectedAffiliationArray.addAll(hashSetAffiliation);

            String interest = TextUtils.join(", ", selectedArray);
            String roles = TextUtils.join(", ", selectedRoleArray);
            String affiliation = TextUtils.join(", ", selectedAffiliationArray);

            if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())){
                ProgressBarDialog.showLoader(mCompanyEditProfileActivity, false);
                ApiInterface apiService =
                        ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
                String userId = AppController.getInstance().getPrefManager().getUser().getId();
                String apiKey = UserUtils.getApiKey(mCompanyEditProfileActivity);
                Call<CompanyEditProfile> call = apiService.companyEditProfile(userId,companyId, firstName,phone,code,mData.getOwnerName(), mData.getOwnerId(),webSite,interest, roles, affiliation, mData.getEmail(),position,location, bio,apiKey);
                call.enqueue(new Callback<CompanyEditProfile>() {
                    @Override
                    public void onResponse(@NonNull Call<CompanyEditProfile> call, @NonNull Response<CompanyEditProfile> response) {
                        ProgressBarDialog.dismissLoader();
                        Log.d("editProfile", "onSuccess");
                        goToHome();
                        if (response.body() != null && response.isSuccessful()) {
                            CompanyEditProfile editProfile = response.body();
                            if(editProfile != null && !editProfile.isErrorStatus()){
                                mCompanyEditProfileActivity.finish();
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<CompanyEditProfile> call, @NonNull Throwable t) {
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
        this.finish();
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
            if(validatePhone())
                uploadToServer();
        }
        return super.onOptionsItemSelected(item);
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
    public void onInterestRoleClicked(View v, int position) {
        Log.d("onInterestRoleClicked: ", ""+position);
        if(!selectedRoleArray.contains(position)){
            selectedRoleArray.add(position);
        } else {
            selectedRoleArray.remove(selectedRoleArray.indexOf(position));
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
            Compressor.getDefault(mCompanyEditProfileActivity)
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
                                .transform(new CircleTransform(mCompanyEditProfileActivity))
                                .into(userProfile);*/

                        Uri uri = new Uri.Builder()
                                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                                //.path(String.valueOf(R.drawable.chat_heads_interstitial_map))
                                .path(String.valueOf(R.drawable.image_overlay))
                                .build();
                        userProfile.setImageURI(uri);

                    });

        }
    }

    private void changeProfile(String profilePic) {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        ProgressBarDialog.showLoader(mCompanyEditProfileActivity, false);
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(this);
        String userId = AppController.getInstance().getPrefManager().getUser().getId();
        Call<CompanyProfilePicture> call = apiService.changeCompanyProfile(userId, companyId, profilePic, apiKey);
        call.enqueue(new Callback<CompanyProfilePicture>() {
            @Override
            public void onResponse(@NonNull Call<CompanyProfilePicture> call, @NonNull Response<CompanyProfilePicture> response) {
                Log.d("changeProfile", "onSuccess");
                if(response.isSuccessful()){
                    CompanyProfilePicture mProfilePicture = response.body();
                    if(mProfilePicture != null && !mProfilePicture.isErrorStatus() && mProfilePicture.getProfilePictureUrl() != null) {
                        UserUtils.saveUserProfilePicture(mCompanyEditProfileActivity, mProfilePicture.getProfilePictureUrl());
                        /*Glide.with(AppController.getInstance()).load(mProfilePicture.getProfilePictureUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(new CircleTransform(mCompanyEditProfileActivity))
                                .into(userProfile);*/

                        userProfile.post(() -> {
                            Uri uri = Uri.parse(mProfilePicture.getProfilePictureUrl());
                            RoundingParams roundingParams = RoundingParams.fromCornersRadius(30f);
                            userProfile.setHierarchy(new GenericDraweeHierarchyBuilder(getResources())
                                    .setRoundingParams(roundingParams)
                                    .build());
                            roundingParams.setRoundAsCircle(true);
                            userProfile.setImageURI(uri);
                        });

                    }
                }
                ProgressBarDialog.dismissLoader();
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
        Intent mIntent = getIntent();
        if(mIntent != null) {
            if(mIntent.hasExtra("companyId")){
                companyId = mIntent.getStringExtra("companyId");
                if(companyId != null && !companyId.equalsIgnoreCase("")){
                        loadEditDetails();
                }
            }
        }
        if(mTracker != null){
            mTracker.setScreenName("Company Edit Profile Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        UserOnlineStatus.setUserOnline(CompanyEditProfileActivity.this,UserOnlineStatus.ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(CompanyEditProfileActivity.this,UserOnlineStatus.OFFLINE);
    }


    private String convertToBitmap(String realPath) {
        String encodedImage = ImageUtils.compressImage(realPath);
        if (encodedImage != null)
            return encodedImage;
        return null;
    }

    private void showError(String errorMessage) {
        Toast.makeText(mCompanyEditProfileActivity, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

}
