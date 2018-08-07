package com.myscrap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.LoginCredential;
import com.myscrap.model.LoginResponse;
import com.myscrap.model.User;
import com.myscrap.service.DeviceModelService;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.DeviceUtils;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.webservice.Constants;
import com.myscrap.xmppdata.ChatMessagesTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity
{

    private EditText emailText;
    private EditText passwordText;
    private Button loginButton;
    private static final String TAG = "LoginActivity";
    private Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailText = findViewById(R.id.input_email);
        passwordText = findViewById(R.id.input_password);
        final Typeface regular = Typeface.createFromAsset(getAssets(),
                AppController.FONT_NAME);
        passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordText.setTypeface(regular);
        passwordText.setTransformationMethod(new PasswordTransformationMethod());
        loginButton = findViewById(R.id.btn_login);
        mTracker = AppController.getInstance().getDefaultTracker();
        TextView linkSignUp = findViewById(R.id.link_sign_up);
        TextView takeALook = findViewById(R.id.take_a_look);
        TextView forgotPassword = findViewById(R.id.forgot_password);
        if(linkSignUp != null)
        {
            String text = linkSignUp.getText().toString();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            {
                final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
                int s1 = text.trim().length();
                spanned.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 15, 0);
                spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)), 15, s1, 0);
                linkSignUp.setText(spanned);
            }
            else
            {
                final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml(text));
                int s1 = text.trim().length();
                spanned.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 15, 0);
                spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)), 15, s1, 0);
                linkSignUp.setText(spanned);
            }
            linkSignUp.setOnClickListener(v ->
            {
                UserUtils.hideKeyBoard(v.getContext(), linkSignUp);
                screenMoveToSignUp();
            });
        }
        if(forgotPassword != null)
        {
            forgotPassword.setOnClickListener(v ->
            {
                UserUtils.hideKeyBoard(v.getContext(), forgotPassword);
                screenMoveToForgotPassword();
            });
        }
        UserUtils.clearUserUtils(LoginActivity.this);
        DeviceUtils.init(LoginActivity.this);
        if(!UserUtils.isApiKeyAlreadySent(LoginActivity.this)){
            Intent mIntent = new Intent(LoginActivity.this, DeviceModelService.class);
            mIntent.putExtra("apiKey", DeviceUtils.getUUID(LoginActivity.this));
            mIntent.putExtra("mobileDevice", DeviceUtils.getDeviceName());
            mIntent.putExtra("mobileBrand", DeviceUtils.getDeviceBrand());
            startService(mIntent);
        }
        if(takeALook != null)
        {
            takeALook.setOnClickListener(v ->
            {
                UserUtils.hideKeyBoard(LoginActivity.this, takeALook);
                UserUtils.clearUserUtils(LoginActivity.this);
                UserUtils.saveApiKey(LoginActivity.this, Constants.GUEST_KEY);
                UserUtils.saveGuestLoginStatus(LoginActivity.this, "1");
                AppController.getInstance().getPrefManager().storeUser(new User("3", "Guest User", "", ""));
                screenMoveToHome();
            });
        }

        String email = null;
        String password = null;

        if(AppController.getInstance().getPrefManager().getUserCredentials() != null)
        {
            email = AppController.getInstance().getPrefManager().getUserCredentials().getEmail();
            password = AppController.getInstance().getPrefManager().getUserCredentials().getPassword();
        }

        if(email != null && !email.equalsIgnoreCase(""))
        {
            emailText.setText(email);
            if(password != null && !password.equalsIgnoreCase(""))
            {
                passwordText.setText(password);
            }
        }
        else
        {
            emailText.setText("");
            passwordText.setText("");
        }


        if (loginButton != null)
        {
            loginButton.setOnClickListener(v ->
            {
                UserUtils.hideKeyBoard(LoginActivity.this, loginButton);
                login();
            });
        }


        // drop the local xmpp persistent
        ChatMessagesTable chatMessagesTable = new ChatMessagesTable(LoginActivity.this);
        if (chatMessagesTable != null)
        {
            chatMessagesTable.dropTable();
        }


    }

    private void screenMoveToHome()
    {
        Intent mIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(mIntent);
        this.finish();
        if (CheckOsVersion.isPreLollipop())
        {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void screenMoveToSignUp()
    {
        Intent mIntent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop())
        {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void screenMoveToForgotPassword()
    {
        Intent mIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop())
        {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    public void login()
    {
        Log.d(TAG, "Login start");
        if (!validate())
        {
            onLoginFailed("Login failed");
            return;
        }
        final String email = emailText.getText().toString();
        final String password = passwordText.getText().toString();
        final String device = "android";
        final String ipAddress = CheckNetworkConnection.getIPAddress(LoginActivity.this);
        final String apiKey = UserUtils.getApiKey(LoginActivity.this);
        if(CheckNetworkConnection.isConnectionAvailable(LoginActivity.this))
        {
            showProgress();
            //new Thread(() -> ).start();




            // previously used login method
   //         loginMethod(email, password, device, ipAddress, apiKey);



            // new login method
            String[] str = {email,password,device,ipAddress,apiKey};
            new LoginTask().execute(str);




        }  else {
            if(emailText != null) {
                SnackBarDialog.showNoInternetError(emailText);
            }
        }
    }

    private void showProgress()
    {
        runOnUiThread(() -> ProgressBarDialog.showLoader(LoginActivity.this, false));
    }

    private void hideProgress() {
        runOnUiThread(ProgressBarDialog::dismissLoader);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess()
    {
        loginButton.setEnabled(true);
        screenMoveToHome();
        this.finish();
    }

    public void onLoginFailed(String msg)
    {
        loginButton.setEnabled(true);
        if (loginButton != null)
        {
            SnackBarDialog.show(loginButton, msg);
        }
    }

    public void showPopUp(String message) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setMessage(message);
        mBuilder.setCancelable(false);
        mBuilder.setPositiveButton("Ok", (dialog, which) -> {
            if(dialog != null)
                dialog.dismiss();
        }).create().show();
    }

    public boolean validate()
    {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty()) {
            passwordText.setError("enter a password");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }



    //  this is previous method which is not used by now
    private void loginMethod(final String userName, final String password, final String device, final String ipAddress, final String apiKey) {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        apiService.doLogin(userName, password, device, ipAddress, apiKey).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                hideProgress();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        LoginResponse mLoginResponse = response.body();
                        if (mLoginResponse != null) {
                            if(!mLoginResponse.isErrorStatus()){
                                LoginCredential mLoginCredential = mLoginResponse.getLoginCredential();
                                if(mLoginCredential != null)
                                {
                  //                parseData(mLoginCredential);
                                }
                            }
                            else
                            {
                                showPopUp(mLoginResponse.getStatus());
                            }
                            Log.d("LoginCredential", "OnNext");
                        }
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t)
            {
                hideProgress();
                onLoginFailed("Login failed");
                Log.e("LoginCredential", "onFailure");
            }
        });






        //   Already commented lines of code

                /*.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<LoginResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.d("LoginCredential", "success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        ProgressBarDialog.dismissLoader();
                        onLoginFailed();
                        Log.e("LoginCredential", "onFailure");
                    }

                    @Override
                    public void onNext(LoginResponse mLoginResponse) {
                        ProgressBarDialog.dismissLoader();
                        if (mLoginResponse != null) {
                            if(!mLoginResponse.isErrorStatus()){
                                LoginCredential mLoginCredential = mLoginResponse.getLoginCredential();
                                if(mLoginCredential != null) {
                                    parseData(mLoginCredential);
                                }
                            } else {
                                showPopUp(mLoginResponse.getStatus());
                            }
                            Log.d("LoginCredential", "OnNext");
                        }
                    }
                });*/



    }



    // this is previous method which is not used by now
    private void parseData(LoginCredential mLoginCredential)
    {
        new Handler().post(() -> {
            UserUtils.saveLoginStatus(LoginActivity.this, "1");
            UserUtils.saveNotificationEnable(LoginActivity.this, "1");
            UserUtils.saveGuestLoginStatus(LoginActivity.this, "0");
            String firstName = mLoginCredential.getFirstName();
            if (firstName != null && !firstName.equalsIgnoreCase(""))
                UserUtils.saveFirstName(LoginActivity.this, firstName);
            String lastName = mLoginCredential.getLastName();
            if (lastName != null && !lastName.equalsIgnoreCase(""))
                UserUtils.saveLastName(LoginActivity.this, lastName);
            String email = mLoginCredential.getEmail();
            if (email != null && !email.equalsIgnoreCase(""))
                UserUtils.saveUserEmail(LoginActivity.this, email);
            String password = mLoginCredential.getPassword();
            if (password != null && !password.equalsIgnoreCase(""))
                UserUtils.saveUserPassword(LoginActivity.this, password);
            String profile = mLoginCredential.getProfilePic();
            if (profile != null &&  !profile.equalsIgnoreCase(""))
                UserUtils.saveUserProfilePicture(LoginActivity.this, profile);
            String userId = mLoginCredential.getUserId();

            if (userId != null && !userId.equalsIgnoreCase(""))
            {
                UserUtils.saveLoggedUserId(LoginActivity.this, userId);
                AppController.getInstance().getPrefManager().storeUser(new User(userId, "", "", ""));
            }

            String name = firstName + " " + lastName;
            if (email != null && !email.equalsIgnoreCase("") && userId != null && !userId.equalsIgnoreCase("") && !name.equalsIgnoreCase("") && password != null && !password.equalsIgnoreCase("")){
                AppController.getInstance().getPrefManager().storeUser(new User(userId, name, email, password));
            }




        });
    }




    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Login Activity Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }


    @Override
    protected void onDestroy()
    {
        ProgressBarDialog.dismissLoader();
        super.onDestroy();
    }






    //  new method created by me on 9th of April
    public class LoginTask extends AsyncTask<String, Void, String>
    {

        protected String doInBackground(String... strings)
        {

            com.android.volley.Response.Listener<String> jsonListener = new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {

                    try
                    {
                        JSONObject responseObject = new JSONObject(response);


                        if (responseObject != null)
                        {

                            Boolean error = responseObject.getBoolean("error");
                            String status = responseObject.getString("status");


                            if (error)
                            {
                                hideProgress();
                                if (status != null)
                                {
                                    if (status.equalsIgnoreCase("Password not match"))
                                    {
                                        onLoginFailed("Incorrect password.");
                                    }
                                    else if (status.equalsIgnoreCase("Email id is wrong"))
                                    {
                                        onLoginFailed("Incorrect email.");
                                    }
                                    else
                                    {
                                        onLoginFailed("Login failed.");
                                    }

                                }
                            }



                            else if(!error && status.equalsIgnoreCase("Successfully Login"))
                            {
                                JSONObject dataObject = responseObject.getJSONObject("userData");



                                // save important status in shared preferences
                                UserUtils.saveLoginStatus(LoginActivity.this, "1");
                                UserUtils.saveNotificationEnable(LoginActivity.this, "1");
                                UserUtils.saveGuestLoginStatus(LoginActivity.this, "0");


                                if (dataObject != null)
                                {

                                    String firstName = dataObject.getString("firstName");
                                    if (firstName != null && !firstName.equalsIgnoreCase(""))
                                        UserUtils.saveFirstName(LoginActivity.this, firstName);

                                    String lastName = dataObject.getString("lastName");
                                    if (lastName != null && !lastName.equalsIgnoreCase(""))
                                        UserUtils.saveLastName(LoginActivity.this, lastName);

                                    String email = dataObject.getString("email");
                                    if (email != null && !email.equalsIgnoreCase(""))
                                        UserUtils.saveUserEmail(LoginActivity.this, email);

                                    String password = dataObject.getString("password");
                                    if (password != null && !password.equalsIgnoreCase(""))
                                        UserUtils.saveUserPassword(LoginActivity.this, password);

                                    String profile = dataObject.getString("profilePic");
                                    if (profile != null &&  !profile.equalsIgnoreCase(""))
                                        UserUtils.saveUserProfilePicture(LoginActivity.this, profile);

                                    String userId = dataObject.getString("userId");
                                    if (userId != null && !userId.equalsIgnoreCase(""))
                                    {
                                        UserUtils.saveLoggedUserId(LoginActivity.this, userId);
                                        AppController.getInstance().getPrefManager().storeUser(new User(userId, "", "", ""));
                                    }

                                    String name = firstName + " " + lastName;
                                    if (email != null && !email.equalsIgnoreCase("") && userId != null && !userId.equalsIgnoreCase("") && !name.equalsIgnoreCase("") && password != null && !password.equalsIgnoreCase("")){
                                        AppController.getInstance().getPrefManager().storeUser(new User(userId, name, email, password));
                                    }

                                   String userJid = dataObject.getString("jId");
                                    if (userJid != null && !userJid.equalsIgnoreCase(""))
                                    {
                                        UserUtils.saveUserJID(LoginActivity.this, userJid);
                                    }

                                    //  save the subscription status of user
                                    String subscription = dataObject.getString("isShared");
                                    if (subscription != null && ! subscription.equalsIgnoreCase(""))
                                    {
                                        UserUtils.savePriceSubscription(LoginActivity.this,subscription);
                                    }




                                    String userColor = dataObject.getString("colorCode");
                                    if (userColor != null && ! userColor.equalsIgnoreCase(""))
                                    {
                                        UserUtils.saveUserColor(LoginActivity.this,userColor);
                                    }




                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run()
                                        {
                                            onLoginSuccess();
                                        }
                                    });
                                }
                            }
                        }

                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        hideProgress();
                        onLoginFailed("Login failed");
                    }

                }

            };

            com.android.volley.Response.ErrorListener errorListener = new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("ERROR SERVER ", error.toString());
                    hideProgress();
                    onLoginFailed("Login failed");

                }
            };


            StringRequest loginStringRequest = new StringRequest(Request.Method.POST, "https://myscrap.com/android/msLogin", jsonListener, errorListener) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("userName", strings[0]);
                    params.put("password", strings[1]);
                    params.put("device", strings[2]);
                    params.put("apiKey", strings[4]);
                    params.put("ipAddress", strings[3]);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    return headers;
                }


            };
            Volley.newRequestQueue(getApplicationContext()).add(loginStringRequest);
            return null;

        }


    }



}
