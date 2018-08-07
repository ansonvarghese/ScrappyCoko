package com.myscrap.utils;

import android.os.Build;

/**
 * Created by Ms2 on 8/3/2016.
 */
public class CheckOsVersion {

    public static  boolean isPreLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

}
