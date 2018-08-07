package com.myscrap;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.myscrap.application.AppController;
import com.myscrap.model.Notification;
import com.myscrap.utils.UserOnlineStatus;
import com.myscrap.utils.UserUtils;
import com.myscrap.webservice.ApiClient;
import com.myscrap.webservice.ApiInterface;
import com.myscrap.webservice.CheckNetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity
{
    Fragment fragment ;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTracker = AppController.getInstance().getDefaultTracker();
        FragmentManager fm = getSupportFragmentManager();
        fragment = fm.findFragmentByTag("myFragmentTag");
        if (fragment == null)
        {
            FragmentTransaction ft = fm.beginTransaction();
            fragment =new NotificationFragment();
            ft.add(R.id.container,fragment,"myFragmentTag");
            ft.commit();
        }



    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        else if (item.getItemId() == R.id.clear)
        {
          //  clearNotification();

            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getApplicationContext());

            String[] params = {userId,apiKey};
                    new NotificationClearTask().execute(params);
        }
        return true;
    }

    private void clearNotification()
    {
        if (AppController.getInstance().getPrefManager().getUser() == null)
            return;
        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance()))
        {
        //    ProgressBarDialog.showLoader(getActivity(), false);
            ApiInterface apiService = ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
            String userId = AppController.getInstance().getPrefManager().getUser().getId();
            String apiKey = UserUtils.getApiKey(getApplicationContext());

            Toast.makeText(getApplicationContext(),userId+"\n"+apiKey, Toast.LENGTH_SHORT).show();

            Call<Notification> call = apiService.clearNotification(userId, apiKey);
            call.enqueue(new Callback<Notification>()
            {
                @Override
                public void onResponse(@NonNull Call<Notification> call, @NonNull Response<Notification> response)
                {

                }
                @Override
                public void onFailure(@NonNull Call<Notification> call, @NonNull Throwable t)
                {

                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No internet access", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(mTracker != null){
            mTracker.setScreenName("Notification Activity");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }

        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.ONLINE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        UserOnlineStatus.setUserOnline(this,UserOnlineStatus.OFFLINE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.notification, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }





    //  new method created by me on 9th of April
    public class NotificationClearTask extends AsyncTask<String, Void, String>
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
                        Boolean isError = responseObject.getBoolean("error");
                        String status = responseObject.getString("status");
                        if (!isError)
                        {
                            if (status.equalsIgnoreCase("success"))
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run()
                                    {

                                        reload();
            //                            Toast.makeText(getApplicationContext(),"Notification cleared",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                }

            };

            com.android.volley.Response.ErrorListener errorListener = new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ERROR SERVER ", error.toString());
                }
            };


            StringRequest loginStringRequest = new StringRequest(Request.Method.POST, "https://myscrap.com/android/msClearNotifications", jsonListener, errorListener) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("userId", strings[0]);
                    params.put("apiKey", strings[1]);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    return headers;
                }


            };
            Volley.newRequestQueue(getApplicationContext()).add(loginStringRequest);
            return null;

        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);




          /*  NotificationFragment.refreshAdapter();
            FragmentManager fm = getSupportFragmentManager();
            fragment = fm.findFragmentByTag("myFragmentTag");
            if (fragment == null)
            {
                FragmentTransaction ft = fm.beginTransaction();
                fragment =new NotificationFragment();
                ft.replace(R.id.container,fragment,"myFragmentTag");
                ft.commit();
            }*/
        }
    }

    public  void reload()
    {
        startActivity(new Intent(getApplicationContext(),NotificationActivity.class));
        finish();
    }



}
