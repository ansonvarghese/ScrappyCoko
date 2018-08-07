package com.myscrap.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.myscrap.application.AppController;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ms3 on 7/18/2017.
 */

public class GetImages extends AsyncTask<String, Void, Bitmap> {
    private String requestUrl, mImageName;

    public GetImages(String requestUrl, ImageView view, String imageName) {
        this.requestUrl = requestUrl;
        this.mImageName = imageName ;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(AppController.getInstance(), "Downloading...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Bitmap doInBackground(String... objects) {
        try {
            /*URL url = new URL(requestUrl);
            URLConnection conn = url.openConnection();
            conn.connect();
            is = conn.getInputStream();
            try {
                is.reset();
            } catch (IOException e) {
                return null;
            }
            is = conn.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeStream(is, null, options);*/
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.connect();
            InputStream is = connection.getInputStream();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            if (options.outWidth > 3000 || options.outHeight > 2000) {
                options.inSampleSize = 4;
            } else if (options.outWidth > 2000
                    || options.outHeight > 1500) {
                options.inSampleSize = 3;
            } else if (options.outWidth > 1000
                    || options.outHeight > 1000) {
                options.inSampleSize = 2;
            }
            options.inJustDecodeBounds = false;

            is.close();
            is = getHTTPConnectionInputStream(requestUrl);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();
            return bitmap;
            /*HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            int responseCode = connection.getResponseCode();*/


            /*if (responseCode == 200) {
                bitmap = BitmapFactory.decodeStream(connection.getInputStream(), null, options);
                return bitmap;
            } else
                return null;*/
        } catch (Exception ex) {
            Log.e("Error", ex.toString());
        }
        return null;
    }

    private InputStream getHTTPConnectionInputStream(String url1) {
        URL url;
        InputStream is = null;
        try {
            url = new URL(url1);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            is = connection.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap == null)
            return;
        if(!ImageStorage.checkIfImageExists(mImageName)) {
            String result = ImageStorage.saveToSdCard(bitmap, mImageName);
            if(result != null && result.equalsIgnoreCase("success")){
                Toast.makeText(AppController.getInstance(), "Photo saved to this device", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AppController.getInstance(), "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
