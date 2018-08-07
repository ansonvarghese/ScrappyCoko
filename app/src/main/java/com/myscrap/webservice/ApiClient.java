package com.myscrap.webservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.myscrap.application.AppController;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    public static final String BASE_URL = "https://myscrap.com/android/";
    private static Retrofit retrofit = null;

    private static int cacheSize = 10 * 1024 * 1024; // 10 MB
    private static Cache cache = new Cache(AppController.getInstance().getCacheDir(), cacheSize);


    public static Retrofit getClient(String baseUrl)
    {

        // Logging Interceptor
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)// Set connection timeout
                .readTimeout(20, TimeUnit.SECONDS)// Read timeout
                .writeTimeout(20, TimeUnit.SECONDS)// Write timeout
                .addInterceptor(interceptor)// Add log interceptor
              //  .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)// Add cache interceptor
               // .cache(cache)// Add cache
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    private static Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = chain -> {

        Request request = chain.request();
        Response response = chain.proceed(request);

        if (CheckNetworkConnection.isConnectionAvailable(AppController.getInstance())) {
            int maxAge = 60*60*24*2;// Cache expiration time ， Unit for seconds
            return response.newBuilder()
                    .removeHeader("Pragma")// Clear header information ， Because server if not supported ， Will return some interference information ， Does not clear the following can not be effective
                    .header("Cache-Control", "public ,max-age=" + maxAge)
                    .build();
        }
        return response;
    };
}
