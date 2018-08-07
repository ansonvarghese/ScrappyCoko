package com.myscrap.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.myscrap.R;

/**
 * Created by Ms2 on 4/21/2016.
 */
public class ProgressBarDialog
{

        private static ProgressDialog pDialog = null;

        public static void showLoader(final Context context, final boolean isCancelable) {
            dismissLoader();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() ->
            {
                try {
                    if (context != null)
                    {
                        pDialog = new ProgressDialog(context, R.style.progressBarTheme);

                        if(isCancelable)
                            pDialog.setCancelable(true);
                        else
                            pDialog.setCancelable(false);

                        pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);

                        if (context instanceof Activity && !((Activity)context).isFinishing())
                        {
                            if (pDialog != null && !pDialog.isShowing())
                               pDialog.show();
                        }

                    }
                } catch (Exception e) {
                    Log.d("ProgressDialog", e.toString());
                }
            });

        }

        public static void showLoaderColor(Context context)
        {
            try {
                if (context != null)
                {
                    pDialog = new ProgressDialog(context);
                    pDialog.setCancelable(true);
                    pDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                    pDialog.show();
                }
            } catch (Exception e) {
                Log.d("ProgressDialog", e.toString());
            }
        }

        public static void dismissLoader() {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                    pDialog = null;
                }
            });

        }
}
