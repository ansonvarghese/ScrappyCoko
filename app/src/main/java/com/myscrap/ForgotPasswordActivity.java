package com.myscrap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.ForgotPassword;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ForgotPasswordActivity mForgotPasswordActivity;
    private EditText emailText;
    private Button resetPasswordButton;
    private static final String TAG = "ForgotPasswordActivity";
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mForgotPasswordActivity = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        emailText = (EditText) findViewById(R.id.input_email);
        resetPasswordButton = (Button) findViewById(R.id.btn_forgot_password);
        if (resetPasswordButton != null){
            resetPasswordButton.setOnClickListener(v -> {
                if(CheckNetworkConnection.isConnectionAvailable(getApplicationContext())) {
                    resetPassword();
                } else {
                    if (resetPasswordButton != null)
                        SnackBarDialog.showNoInternetError(resetPasswordButton);
                }
            });
        }
    }

    private void resetPassword() {
        Log.d(TAG, "Reset Password start");
        if (!validate()) {
            return;
        }
        resetPasswordButton.setEnabled(false);
        ProgressBarDialog.showLoader(this, false);
        String email = emailText.getText().toString();
        doResetPassword(email);
    }

    private void onValidateFailed() {
        if (resetPasswordButton != null) {
            SnackBarDialog.show(resetPasswordButton, "This email address is invalid");
        }
    }

    private void doResetPassword(final String email)
    {
        ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        String apiKey = UserUtils.getApiKey(this);
        Call<ForgotPassword> call = apiService.forgotPassword(email, apiKey);
        call.enqueue(new Callback<ForgotPassword>() {
            @Override
            public void onResponse(@NonNull Call<ForgotPassword> call, @NonNull Response<ForgotPassword> response)
            {
                Log.d("doResetPassword", "onSuccess");
                if(response.isSuccessful()){
                    ForgotPassword mForgotPassword  = response.body();
                    if(mForgotPassword != null && !mForgotPassword.isErrorStatus()){
                        Intent i = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                        i.putExtra("email", email);
                        mForgotPasswordActivity.startActivity(i);
                        finish();
                        if (CheckOsVersion.isPreLollipop())
                            mForgotPasswordActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        if(resetPasswordButton != null)
                            SnackBarDialog.show(resetPasswordButton, mForgotPassword.getStatus());
                    }
                }
                ProgressBarDialog.dismissLoader();
            }
            @Override
            public void onFailure(@NonNull Call<ForgotPassword> call, @NonNull Throwable t) {
                Log.d("doResetPassword", "onFailure");
                ProgressBarDialog.dismissLoader();
            }
        });
    }

    public boolean validate() {
        boolean valid = true;
        String email = emailText.getText().toString();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("This email address is invalid");
            valid = false;
        } else {
            emailText.setError(null);
        }
        return valid;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Forgot Password Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
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
}
