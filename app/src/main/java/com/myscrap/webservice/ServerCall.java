package com.myscrap.webservice;

import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.myscrap.application.AppController;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by Ms2 on 2/4/2016.
 */
public class ServerCall {
    private static final String TAG = "Volley";
    private URLRequestListener mUrlRequestListener= null;

    public ServerCall(URLRequestListener mURLRequestListener) {
        mUrlRequestListener = mURLRequestListener;
    }

    public  void makePostStringURLRequest(final String url, final Map<String, String> params,final String tag)
    {

        new Thread(() -> {
            Log.d("POST_THREAD", Thread.currentThread().getName());
            Cache cache = AppController.getInstance().getRequestQueue().getCache();
            Cache.Entry entry = cache.get(url);
            if (entry != null){
                try {
                    String data = new String(entry.data, "UTF-8");
                    mUrlRequestListener.onRequestComplete(data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                        url, response ->
                {
                            if (response != null )
                            {
                                mUrlRequestListener.onRequestComplete(response);
                            }
                        }, error ->
                {
                            VolleyLog.d(TAG, "Error: " + error.getMessage());
                            mUrlRequestListener.onRequestFailure(error.getMessage());


                        }){

                    @Override
                    protected Map<String, String> getParams() {
                        return params;
                    }
                };

                strReq.setRetryPolicy(new DefaultRetryPolicy(0, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                AppController.getInstance().getRequestQueue().getCache().invalidate(url, true);
                strReq.setShouldCache(true);
                AppController.getInstance().addToRequestQueue(strReq, tag);

            }
        }).start();

    }

    public  void makeStringURLRequest(final String url,final String tag) {

        new Thread(() -> {
            Log.d("GET_THREAD", Thread.currentThread().getName());
            Cache cache = AppController.getInstance().getRequestQueue().getCache();
            Cache.Entry entry = cache.get(url);
            if (entry != null) {
                try {
                    String data = new String(entry.data, "UTF-8");
                    mUrlRequestListener.onRequestComplete(data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                StringRequest strReq = new StringRequest(com.android.volley.Request.Method.GET,
                        url, response -> mUrlRequestListener.onRequestComplete(response), error -> {
                            VolleyLog.d(TAG, "Error: " + error.getMessage());
                            mUrlRequestListener.onRequestFailure(error.getMessage());
                        });
                AppController.getInstance().getRequestQueue().getCache().invalidate(url, true);
                strReq.setShouldCache(true);
                strReq.setRetryPolicy(new DefaultRetryPolicy(0, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                AppController.getInstance().addToRequestQueue(strReq, tag);
            }
        }).start();
    }
}
