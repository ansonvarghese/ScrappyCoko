package com.myscrap.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

/**
 * Created by Ms2 on 4/21/2016.
 */
public class ProgressBarTransparentDialog {

        private static ProgressDialog pDialog = null;
        public static void showLoader(Context context, String message) {
            try {
                if (context != null) {
                    pDialog = new ProgressDialog(context);
                    pDialog.setCancelable(false);
                    pDialog.setMessage(message);
                    pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pDialog.show();
                }
            } catch (Exception e) {
                Log.d("ProgressDialog", e.toString());
            }
        }


    public static void showLoaderOnly(Context context) {
        try {
            if (context != null) {
                pDialog = new ProgressDialog(context);
                pDialog.setCancelable(false);
                pDialog.setMessage("Loading...");
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pDialog.show();
            }
        } catch (Exception e) {
            Log.d("ProgressDialog", e.toString());
        }
    }

        public static void dismissLoader() {
            if (pDialog != null) {
                pDialog.dismiss();
                pDialog = null;
            }
        }
}
