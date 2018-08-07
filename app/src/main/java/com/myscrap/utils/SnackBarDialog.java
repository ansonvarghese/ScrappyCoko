package com.myscrap.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by Ms2 on 7/12/2016.
 */
public class SnackBarDialog {

    public static void show(View snackBarView, String message) {
        if (snackBarView != null && message != null) {
            Snackbar snackbar = Snackbar.make(snackBarView, message, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    public static void showNoInternetError(View snackBarView)
    {
        String message = "No internet connection.";
        if (snackBarView != null)
        {
            Snackbar snackbar = Snackbar.make(snackBarView, message, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}
