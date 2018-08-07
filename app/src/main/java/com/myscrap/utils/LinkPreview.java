package com.myscrap.utils;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myscrap.R;
import com.myscrap.application.AppController;
import com.myscrap.webservice.CheckNetworkConnection;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by ms4 on 2/11/2017.
 */

public class LinkPreview extends RelativeLayout {
    private static String TAG = LinkPreview.class.getSimpleName();
    private ImageView mPhotoDraweeView;
    private TextView mTxtViewTitle;
    private TextView mTxtViewDescription;
    private Handler mHandler;
    private String mTitle=null;
    private String mDescription=null;
    private String mImageLink=null;
    private String mSiteName=null;
    private String mSite;
    private String mLink;
    private ProgressBar mLoadingDialog;
    private PreviewListener mListener;
    private OkHttpClient client;

    public LinkPreview(Context context) {
        super(context);
        initialize(context);
    }

    public LinkPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public LinkPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context){
        inflate(context, R.layout.link_preview_layout, this);
        mPhotoDraweeView =(ImageView)findViewById(R.id.link_preview_image);
        mTxtViewTitle=(TextView)findViewById(R.id.title);
        mTxtViewDescription=(TextView)findViewById(R.id.link);
        mLoadingDialog=(ProgressBar)findViewById(R.id.progress_bar);
        mHandler = new Handler(context.getMainLooper());
    }

    public void setListener(PreviewListener listener)
    {
        this.mListener=listener;
    }

    private final Interceptor REWRITE_RESPONSE_INTERCEPTOR = new Interceptor() {
        @Override
        public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
            okhttp3.Response originalResponse = chain.proceed(chain.request());
            String cacheControl = originalResponse.header("Cache-Control");
            if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                    cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) {
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=" + 5000)
                        .build();
            } else {
                return originalResponse;
            }
        }
    };

    private final Interceptor REWRITE_RESPONSE_INTERCEPTOR_OFFLINE = chain -> {
        Request request = chain.request();
        if (!CheckNetworkConnection.isConnectionAvailable(getContext())) {
            request = request.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public, only-if-cached")
                    .build();
        }
        return chain.proceed(request);
    };


    public void setData(final String url){
        if(url != null && !TextUtils.isEmpty(url)) {
            runOnUiThread(() -> {
                if (mLoadingDialog != null)
                    mLoadingDialog.setVisibility(VISIBLE);
            });
            clear();
            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(new File(AppController.getInstance().getCacheDir(),"LinkPreview"), cacheSize);
            //client = new OkHttpClient();
            client = new OkHttpClient.Builder()
                    .cache(cache)
                    .addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR)
                    .addInterceptor(REWRITE_RESPONSE_INTERCEPTOR_OFFLINE)
                    .build();
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        if (e.getMessage() != null)
                            Log.e(TAG, String.valueOf(e.getMessage()));
                        runOnUiThread(() -> {
                            if (mLoadingDialog != null)
                                mLoadingDialog.setVisibility(GONE);
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        ResponseBody responseBody = null;
                        if (response.isSuccessful()){
                            Elements titleElements;
                            Elements descriptionElements;
                            Elements imageElements;
                            Elements videoElements;
                            Elements siteElements;
                            Elements linkElements;
                            String site = "";
                            Document doc;
                            boolean isCache;

                            /*if(response.cacheResponse() != null){
                                Response res = response.cacheResponse();
                                if(res != null){
                                    if(res.body() != null){
                                        isCache = true;
                                        responseBody = res.body();
                                    } else {
                                        isCache = false;
                                        responseBody = response.body();
                                    }
                                }
                            } else {
                                responseBody = response.body();
                                isCache = false;
                            }*/

                            responseBody = response.body();
                            assert responseBody != null;
                            BufferedSource source = responseBody.source();
                            if(source == null)
                                return;
                            source.request(Long.MAX_VALUE); // Buffer the entire body.
                            Buffer buffer = source.buffer();
                            if(buffer != null){
                            String responseBodyString = buffer.clone().readString(Charset.forName("UTF-8"));
                            Log.d("TAG", responseBodyString);

                            doc = Jsoup.parse(responseBodyString);
                            titleElements = doc.select("title");
                            descriptionElements = doc.select("meta[name=description]");
                            if (url.contains("bhphotovideo")) {
                                imageElements = doc.select("image[id=mainImage]");
                                site = "bhphotovideo";
                            } else if (url.contains("www.amazon.com/gp/aw/d")) {
                                imageElements = doc.select("image[id=mainImage]");
                                site = "www.amazon.com/gp/aw/d";
                            } else if (url.contains("www.amazon.com/")) {
                                imageElements = doc.select("img[data-old-hires]");
                                site = "www.amazon.com/";
                            } else if (url.contains("m.clove.co.uk")) {
                                imageElements = doc.select("img[id]");
                                site = "m.clove.co.uk";
                            } else if (url.contains("www.clove.co.uk")) {
                                imageElements = doc.select("li[data-thumbnail-path]");
                                site = "www.clove.co.uk";
                            } else
                                imageElements = doc.select("meta[property=og:image]");

                            mImageLink = getImageLinkFromSource(imageElements, site);
                            siteElements = doc.select("meta[property=og:site_name]");
                            linkElements = doc.select("meta[property=og:url]");

                            /*videoElements = doc.select("meta[property=og:type]");
                            mVideoLink = videoElements.get(0).attr("content");
                            isVideo = mVideoLink != null && !mVideoLink.equalsIgnoreCase("") && mVideoLink.equalsIgnoreCase("video");*/

                            if (titleElements != null && titleElements.size() > 0) {
                                mTitle = titleElements.get(0).text();
                            }
                            if (descriptionElements != null && descriptionElements.size() > 0) {
                                mDescription = descriptionElements.get(0).attr("content");
                            }
                            if (linkElements != null && linkElements.size() > 0) {
                                mLink = linkElements.get(0).attr("content");
                            } else {
                                linkElements = doc.select("link[rel=canonical]");
                                if (linkElements != null && linkElements.size() > 0) {
                                    mLink = linkElements.get(0).attr("href");
                                }
                            }
                            if (siteElements != null && siteElements.size() > 0) {
                                mSiteName = siteElements.get(0).attr("content");
                            }

                            if (getTitle() != null) {
                                Log.v(TAG, getTitle());
                                if (getTitle().length() >= 50)
                                    //mTitle = getTitle().substring(0, 49) + "...";
                                    mTitle = getTitle();
                                runOnUiThread(() -> mTxtViewTitle.setText(mTitle));
                            }
                            if (getLink() != null) {
                                Log.v(TAG, getLink());
                                if (getLink().length() >= 100)
                                    mDescription = getLink().substring(0, 99) + "...";
                                runOnUiThread(() -> mTxtViewDescription.setText(getLink()));
                            }


                            if (getImageLink() != null && !getImageLink().equals("")) {
                                Log.v(TAG, getImageLink());
                                runOnUiThread(() -> {
                                    if (mLoadingDialog != null)
                                        mLoadingDialog.setVisibility(GONE);
                                });
                                runOnUiThread(() -> {

                                    if(CheckNetworkConnection.isConnectionAvailable(getContext())){
                                        Picasso.with(getContext())
                                                .load(getImageLink())
                                                .into(mPhotoDraweeView);
                                    } else {
                                        Picasso.with(getContext())
                                                .load(getImageLink())
                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                .into(mPhotoDraweeView);
                                    }
                                });

                            } else {
                                runOnUiThread(() -> {
                                    if (mLoadingDialog != null)
                                        mLoadingDialog.setVisibility(GONE);
                                });
                                runOnUiThread(() -> {
                                    Picasso.with(getContext())
                                            .load(R.drawable.no_listing_image)
                                            .into(mPhotoDraweeView);
                                });
                            }
                            if (url.toLowerCase().contains("amazon"))
                                if (getSiteName() == null || getSiteName().equals(""))
                                    mSiteName = "Amazon";
                            if (getSiteName() != null) {
                                Log.v(TAG, getSiteName());
                                if (getSiteName().length() >= 30)
                                    mSiteName = getSiteName().substring(0, 29) + "...";
                            }

                            Log.v(TAG, "Link: " + getLink());
                            mListener.onDataReady(LinkPreview.this);
                            }
                            responseBody.close();
                        } else {
                            response.close();
                            runOnUiThread(() -> {
                                if (mLoadingDialog != null)
                                    mLoadingDialog.setVisibility(GONE);
                            });
                            mListener.onDataReady(LinkPreview.this);

                        }
                    }
                });
            }
            catch (Exception ex) {
                runOnUiThread(() -> {
                    if (mLoadingDialog != null)
                        mLoadingDialog.setVisibility(GONE);
                });
            }

        }
    }

    public void setMessage(final String message) {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
              *//*  if(message==null)
                    mTxtViewMessage.setVisibility(GONE);
                else
                    mTxtViewMessage.setVisibility(VISIBLE);
                mTxtViewMessage.setText(message);*//*
            }
        });*/
    }

    public void setMessage(final String message, final int color)
    {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                *//*if(message==null)
                    mTxtViewMessage.setVisibility(GONE);
                else
                    mTxtViewMessage.setVisibility(VISIBLE);
                mTxtViewMessage.setTextColor(color);
                mTxtViewMessage.setText(message);*//*
            }
        });*/
    }

    private String getImageLinkFromSource(Elements elements,String site)
    {
        String imageLink=null;
        if (elements != null && elements.size() > 0) {
            switch (site)
            {
                case "m.clove.co.uk":
                case "bhphotovideo":
                    imageLink = elements.get(0).attr("src");
                    break;
                case "www.amazon.com/gp/aw/d":

                    break;
                case "www.amazon.com/":
                    imageLink = elements.get(0).attr("data-old-hires");
                    break;
                case "www.clove.co.uk":
                    imageLink="https://www.clove.co.uk"+elements.get(0).attr("data-thumbnail-path");
                    break;
                default:
                    imageLink = elements.get(0).attr("content");
                    break;
            }

        }
        return imageLink;
    }

    private void clear()
    {
        mPhotoDraweeView.setImageResource(0);
        mTxtViewTitle.setText("");
        mTxtViewDescription.setText("");
        mTitle=null;
        mDescription=null;
        mImageLink=null;
        mSiteName=null;
        mSite=null;
        mLink=null;
        String mVideoLink = null;
    }

    public interface PreviewListener {
        void onDataReady(LinkPreview preview);
    }

    private void runOnUiThread(Runnable r) {
        if (mHandler != null && r != null)
            mHandler.post(r);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageLink() {
        return mImageLink;
    }

    public String getSiteName() {
        return mSiteName;
    }

    public String getSite() {
        return mSite;
    }

    public String getLink() {
        return mLink;
    }

    public boolean isVideoLink(){
        return false;}
}
