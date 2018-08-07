package com.myscrap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.LoginCredential;
import com.myscrap.model.LoginResponse;
import com.myscrap.model.User;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SignUpActivity extends AppCompatActivity {

    private EditText firstNameText;
    private EditText lastNameText;
    private EditText emailText;
    private EditText passwordText;
    private Button signUpButton;
    private static final String TAG = "SignUpActivity";
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        firstNameText = (EditText) findViewById(R.id.input_first_name);
        lastNameText = (EditText) findViewById(R.id.input_last_name);
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        final Typeface regular = Typeface.createFromAsset(getAssets(),
                AppController.FONT_NAME);
        passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordText.setTypeface(regular);
        passwordText.setTransformationMethod(new PasswordTransformationMethod());
        signUpButton = (Button) findViewById(R.id.btn_sign_up);
        mTracker = AppController.getInstance().getDefaultTracker();
        TextView linkLogin = (TextView) findViewById(R.id.link_sign_in);
        if(linkLogin != null){
            String text = linkLogin.getText().toString();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
                int s1 = text.trim().length();
                spanned.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 24, 0);
                spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)), 24, s1, 0);
                linkLogin.setText(spanned);
            } else {
                final SpannableStringBuilder spanned = new SpannableStringBuilder(Html.fromHtml(text));
                int s1 = text.trim().length();
                spanned.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 24, 0);
                spanned.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)), 24, s1, 0);
                linkLogin.setText(spanned);
            }

            linkLogin.setOnClickListener(v -> {
                UserUtils.hideKeyBoard(SignUpActivity.this, linkLogin);
                screenMoveToLogin();
            });
        }

        if (signUpButton != null){
            signUpButton.setOnClickListener(v -> {
                UserUtils.hideKeyBoard(SignUpActivity.this, signUpButton);
                if(CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                    signUp();
                } else {
                    if (signUpButton != null)
                        SnackBarDialog.showNoInternetError(signUpButton);
                }
            });
        }
    }

    private void screenMoveToLogin() {
        Intent mIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(mIntent);
        if (CheckOsVersion.isPreLollipop()) {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    public void signUp() {
        Log.d(TAG, "Sign Up start");

        if (!validate()) {
            return;
        }
        signUpButton.setEnabled(false);
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String firstName = firstNameText.getText().toString();
        String lastName = lastNameText.getText().toString();

        if(CheckNetworkConnection.isConnectionAvailable(SignUpActivity.this)) {
            ProgressBarDialog.showLoader(this, false);
            doSignUp(email, password, firstName, lastName);
        }  else {
            if(emailText != null) {
                SnackBarDialog.showNoInternetError(emailText);
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onLoginSuccess() {
        signUpButton.setEnabled(true);
        screenMoveToHome();
    }

    private void screenMoveToHome() {
        Intent mIntent = new Intent(SignUpActivity.this, HomeActivity.class);
        startActivity(mIntent);
        this.finish();
        if (CheckOsVersion.isPreLollipop()) {
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    public void onLoginFailed() {
        signUpButton.setEnabled(true);
        if (signUpButton != null) {
            SnackBarDialog.show(signUpButton, "Sign up failed");
        }
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String firstName = firstNameText.getText().toString();
        String lastName = lastNameText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty()) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (firstName.isEmpty()) {
            firstNameText.setError("Enter a first name");
            valid = false;
        } else {
            firstNameText.setError(null);
        }

        if (lastName.isEmpty()) {
            lastNameText.setError("Enter a last name");
            valid = false;
        } else {
            lastNameText.setError(null);
        }

        return valid;
    }

    private void doSignUp(final String email, final String password, final String firstName, final String lastName){
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        final String ipAddress = CheckNetworkConnection.getIPAddress(SignUpActivity.this);
        apiService.doSignUp(email, password, firstName, lastName, ipAddress)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<LoginResponse>() {
                    @Override
                    public void onCompleted() {
                        Log.d("SignUp", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        ProgressBarDialog.dismissLoader();
                        onLoginFailed();
                        Log.e("SignUp", "onFailure");
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
                            Log.d("SignUp", "success");
                        }
                    }
                });
    }

    private void parseData(LoginCredential mLoginCredential) {
        new Handler().post(() ->
        {
            UserUtils.saveLoginStatus(SignUpActivity.this, "1");
            UserUtils.saveNotificationEnable(SignUpActivity.this, "1");
            UserUtils.saveGuestLoginStatus(SignUpActivity.this, "0");
            String firstName = mLoginCredential.getFirstName();
            if (firstName != null && !firstName.equalsIgnoreCase(""))
                UserUtils.saveFirstName(SignUpActivity.this, firstName);
            String lastName = mLoginCredential.getLastName();
            if (lastName != null && !lastName.equalsIgnoreCase(""))
                UserUtils.saveLastName(SignUpActivity.this, lastName);
            String email = mLoginCredential.getEmail();
            if (email != null && !email.equalsIgnoreCase(""))
                UserUtils.saveUserEmail(SignUpActivity.this, email);
            String password = mLoginCredential.getPassword();
            if (password != null && !password.equalsIgnoreCase(""))
                UserUtils.saveUserPassword(SignUpActivity.this, password);
            String profile = mLoginCredential.getProfilePic();
            if (profile != null &&  !profile.equalsIgnoreCase(""))
                UserUtils.saveUserProfilePicture(SignUpActivity.this, profile);
            String userId = mLoginCredential.getUserId();
            if (userId != null && !userId.equalsIgnoreCase(""))
            {
                UserUtils.saveLoggedUserId(SignUpActivity.this, userId);
                AppController.getInstance().getPrefManager().storeUser(new User(userId, "", "", ""));
            }
            String name = firstName + " " + lastName;
            if (email != null && !email.equalsIgnoreCase("") && userId != null && !userId.equalsIgnoreCase("") && !name.equalsIgnoreCase("") && password != null && !password.equalsIgnoreCase("")){
                AppController.getInstance().getPrefManager().storeUser(new User(userId, name, email, password));
            }
            String userJid = mLoginCredential.getjId();
            if (userJid != null && !userJid.equalsIgnoreCase(""))
            {
                UserUtils.saveUserJID(SignUpActivity.this, userJid);
            }

            onLoginSuccess();
        });
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

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("SignUp Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
