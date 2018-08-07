package com.myscrap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.utils.CheckOsVersion;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.webservice.CheckNetworkConnection;
import com.myscrap.webservice.EndPoints;
import com.myscrap.webservice.ServerCall;
import com.myscrap.webservice.URLRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ResetPasswordActivity extends AppCompatActivity implements URLRequestListener
{

    private TextView title, subTitle, hint;
    private EditText editText;
    private Button continueButton, sendCodeAgainButton;
    private ResetPasswordActivity mResetPasswordActivity;
    private String email;
    private boolean isSendOTP  = false;
    private boolean isResendCode = false;
    private boolean isChangeResetPasswordLayout = false;
    private boolean isNewPassword  = false;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mResetPasswordActivity = this;
        Intent mIntent = getIntent();
        if (mIntent != null)
        {
            email = mIntent.getStringExtra("email");
        }
        mTracker = AppController.getInstance().getDefaultTracker();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Confirm Your Account");
        }
        title = (TextView) findViewById(R.id.title);
        subTitle = (TextView) findViewById(R.id.sub_title);
        hint = (TextView) findViewById(R.id.password_hint);
        editText = (EditText) findViewById(R.id.password_edit_text);
        continueButton = (Button) findViewById(R.id.continue_button);
        sendCodeAgainButton = (Button) findViewById(R.id.send_code_again);
        continueButton.setEnabled(true);
        sendCodeAgainButton.setVisibility(View.VISIBLE);
        if (email != null && !email.equalsIgnoreCase(""))
        {
            title.setText(email);
            String OTP_STRING = "We sent a message to this email address. Enter the code from that message here.";
            subTitle.setText(OTP_STRING);
            String OTP_EDIT_TEXT_HINT = "Enter Code";
            editText.setHint(OTP_EDIT_TEXT_HINT);
            sendCodeAgainButton.setVisibility(View.VISIBLE);
        }

        continueButton.setOnClickListener(v ->
        {
            if (editText != null && !editText.getText().toString().equalsIgnoreCase(""))
            {
                if (!isChangeResetPasswordLayout)
                {
                    if (CheckNetworkConnection.isConnectionAvailable(mResetPasswordActivity))
                    {
                        sendOTP(editText.getText().toString());
                    }
                    else
                    {
                        if (continueButton != null)
                            SnackBarDialog.show(continueButton, "No internet connection available.");
                    }
                }
                else
                {
                    if (CheckNetworkConnection.isConnectionAvailable(mResetPasswordActivity)) {
                        sendNewPassword(editText.getText().toString());
                    } else {
                        if (continueButton != null)
                            SnackBarDialog.show(continueButton, "No internet connection available.");
                    }
                }
            }
            else
            {
                Toast.makeText(mResetPasswordActivity, "Field shouldn't be empty", Toast.LENGTH_SHORT).show();
            }
        });
        sendCodeAgainButton.setOnClickListener(v ->
        {
            if (email != null && !email.equalsIgnoreCase(""))
            {
                if (CheckNetworkConnection.isConnectionAvailable(mResetPasswordActivity))
                {
                    sendForgotPassword(email);
                }
                else
                {
                    if (sendCodeAgainButton != null)
                        SnackBarDialog.show(sendCodeAgainButton,"No internet connection available.");
                }
            }
        });
    }

    private void sendOTP(String otpString)
    {
        if (CheckNetworkConnection.isConnectionAvailable(mResetPasswordActivity))
        {
            ProgressBarDialog.showLoader(mResetPasswordActivity, false);
            isSendOTP = true;
            ServerCall mServerCall = new ServerCall(this);
            mServerCall.makePostStringURLRequest(EndPoints.URL_SEND_OTP, getPostParam(otpString),"otp_req");
        }
    }

    private void sendForgotPassword(String userEmail)
    {
        if (CheckNetworkConnection.isConnectionAvailable(mResetPasswordActivity))
        {
            ProgressBarDialog.showLoader(mResetPasswordActivity, false);
            isResendCode = true;
            ServerCall mServerCall = new ServerCall(this);
            mServerCall.makePostStringURLRequest(EndPoints.URL_FORGOT_PASSWORD, getResendPasswordParam(userEmail), "forgot_req");
        }
    }
    private void sendNewPassword(String newPassword)
    {
        if (CheckNetworkConnection.isConnectionAvailable(mResetPasswordActivity))
        {
            ProgressBarDialog.showLoader(mResetPasswordActivity, false);
            isNewPassword = true;
            ServerCall mServerCall = new ServerCall(this);
            mServerCall.makePostStringURLRequest(EndPoints.URL_CHANGE_PASSWORD, getNewPasswordPasswordParam(newPassword), "forgot_req");
        }
    }

    private Map<String, String> getNewPasswordPasswordParam(String newPassword)
    {
        Map<String,String> params = new HashMap<>();
        params.put("newpassword",newPassword);
        params.put("email",email);
        return params;
    }

    private Map<String, String> getResendPasswordParam(String userEmail) {
        Map<String,String> params = new HashMap<>();
        params.put("reg_email",userEmail);
        return params;
    }

    private Map<String, String> getPostParam(String otpString) {
        Map<String, String> param = new HashMap<>();
        param.put("code", otpString);
        param.put("reg_email",email);
        return param;
    }

    private void changeResetPasswordLayout()
    {
        isChangeResetPasswordLayout = true;
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("Secure Your Account");
        }

        String CREATE_PASSWORD_TITLE = "Create a new password.";
        title.setText(CREATE_PASSWORD_TITLE);
        String CREATE_PASSWORD_STRING = "You,ll use this password to access your account.";
        subTitle.setText(CREATE_PASSWORD_STRING);
        editText.setText("");
        String PASSWORD_EDIT_TEXT_HINT = "Type a new password";
        editText.setHint(PASSWORD_EDIT_TEXT_HINT);
        String CREATE_PASSWORD_HINT = "Enter a combination of at least six numbers, letters and punctuation marks.";
        hint.setText(CREATE_PASSWORD_HINT);
        sendCodeAgainButton.setVisibility(View.GONE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(mTracker != null)
        {
            mTracker.setScreenName("Reset Password Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onRequestComplete(String result)
    {
        ProgressBarDialog.dismissLoader();
        if (result != null && !result.equalsIgnoreCase(""))
        {
            JSONObject jsonObject;
            String status = null;
            String message = null;
            String resetEmail = null;
            String resetPassword = null;
            try
            {
                jsonObject = new JSONObject(result);
                status = jsonObject.optString("status");
                message = jsonObject.optString("msg");
                resetEmail = jsonObject.optString("username");
                resetPassword = jsonObject.optString("password");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            if (isSendOTP)
            {
                isSendOTP = false;
                if (status != null && status.equalsIgnoreCase("1"))
                {
                    changeResetPasswordLayout();
                }
                else if (status != null && status.equalsIgnoreCase("2"))
                {
                    Toast.makeText(mResetPasswordActivity, message, Toast.LENGTH_SHORT).show();
                }
                else if (status != null && status.equalsIgnoreCase("3"))
                {
                    Toast.makeText(mResetPasswordActivity, message, Toast.LENGTH_SHORT).show();
                }
            }
            else if (isResendCode)
            {
                isResendCode = false;
                    if (status != null && status.equalsIgnoreCase("1"))
                    {
                        Toast.makeText(mResetPasswordActivity, message, Toast.LENGTH_SHORT).show();
                    }
                    else if (status != null && status.equalsIgnoreCase("2"))
                    {
                        Toast.makeText(mResetPasswordActivity, message, Toast.LENGTH_SHORT).show();
                    }
            }
            else if (isNewPassword)
            {
                isNewPassword = false;
                if (status != null && status.equalsIgnoreCase("1")){
                    if (!resetEmail.equalsIgnoreCase("") && !resetEmail.equalsIgnoreCase("null") &&!resetPassword.equalsIgnoreCase("") && !resetPassword.equalsIgnoreCase("null")) {
                        Intent mIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        mIntent.putExtra("pageName", "password_reset");
                        mIntent.putExtra("email", resetEmail);
                        mIntent.putExtra("password", resetPassword);
                        startActivity(mIntent);
                        this.finish();
                        if (CheckOsVersion.isPreLollipop()) {
                            if (mResetPasswordActivity != null)
                                mResetPasswordActivity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        }
                    }
                    Toast.makeText(mResetPasswordActivity, message, Toast.LENGTH_SHORT).show();
                } else if (status != null && status.equalsIgnoreCase("2")) {
                    Toast.makeText(mResetPasswordActivity, message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestFailure(String exception) {
        ProgressBarDialog.dismissLoader();
    }
}
