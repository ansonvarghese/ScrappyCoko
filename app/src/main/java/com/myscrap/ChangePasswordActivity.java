package com.myscrap;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.ChangePassword;
import com.myscrap.utils.ProgressBarDialog;
import com.myscrap.utils.SnackBarDialog;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import retrofit2.Call;
import retrofit2.Callback;

public class ChangePasswordActivity extends AppCompatActivity {

    private ChangePasswordActivity mChangePasswordActivity;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mChangePasswordActivity = this;
        mTracker = AppController.getInstance().getDefaultTracker();
        final EditText currentPasswordText = (EditText) findViewById(R.id.current_password);
        final EditText newPasswordText = (EditText) findViewById(R.id.new_password);
        final Button changePassword = (Button) findViewById(R.id.change_password);
        changePassword.setOnClickListener(v -> {
            String cp = currentPasswordText.getText().toString();
            String np = newPasswordText.getText().toString();
            if(!cp.equalsIgnoreCase("") && !np.equalsIgnoreCase("")){
                if(!cp.equalsIgnoreCase(np)){
                    if(CheckNetworkConnection.isConnectionAvailable(getApplicationContext())){
                        doChangePassword(currentPasswordText.getText().toString().trim(), newPasswordText.getText().toString().trim());
                        UserUtils.hideKeyBoard(mChangePasswordActivity, v);
                    } else {
                        SnackBarDialog.showNoInternetError(v);
                    }
                } else {
                    SnackBarDialog.show(v, "Current password and New password were same.");
                }
            }

        });
    }

    private void doChangePassword(String currentPassword, String newPassword) {
            if (AppController.getInstance().getPrefManager().getUser() == null)
                return;
            ProgressBarDialog.showLoader(mChangePasswordActivity, false);
            ApiInterface apiService =
                    ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String apiKey = UserUtils.getApiKey(AppController.getInstance());
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            Call<ChangePassword> call = apiService.changePassword(userId, currentPassword, newPassword, apiKey);
            call.enqueue(new Callback<ChangePassword>() {
                @Override
                public void onResponse(@NonNull Call<ChangePassword> call, @NonNull retrofit2.Response<ChangePassword> response) {
                    Log.d("doChangePassword", "onSuccess");
                    ProgressBarDialog.dismissLoader();
                    if(response.body() != null && response.isSuccessful()){
                        ChangePassword mChangePassword = response.body();
                        if(mChangePassword != null){
                            if(!mChangePassword.isErrorStatus()){
                                if(mChangePassword.getChangedPassword() != null && !mChangePassword.getChangedPassword().equalsIgnoreCase("")){
                                    if(!mChangePassword.getChangedPassword().equalsIgnoreCase("error")){
                                        UserUtils.saveUserPassword(getApplicationContext(), mChangePassword.getChangedPassword());
                                        Toast.makeText(getApplicationContext(), "Successfully changed.", Toast.LENGTH_SHORT).show();
                                        mChangePasswordActivity.finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Current password not matched", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }

                    }
                }
                @Override
                public void onFailure(@NonNull Call<ChangePassword> call, @NonNull Throwable t) {
                    Log.d("doChangePassword", "onFailure");
                    ProgressBarDialog.dismissLoader();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Change Password Screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mChangePasswordActivity.finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
