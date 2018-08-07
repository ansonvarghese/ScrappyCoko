package com.myscrap.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Ms2 on 2/4/2016.
 */
class CommonUtils {

    private static SharedPreferences getPreferences(Context context, String preferenceRegion) {
        return context.getSharedPreferences(preferenceRegion, Context.MODE_PRIVATE);
    }

    static String getPreferenceString(Context context, String preferenceRegion, String preferenceName) {
        try
        {
            final SharedPreferences prefs = getPreferences(context, preferenceRegion);
            return prefs.getString(preferenceName, "");
        }
        catch (Exception e)
        {

        }
        return null;
    }

    static void setPreferenceString(Context context, String preferenceRegion, String preferenceName, String preferenceValue) {
        final SharedPreferences prefs = getPreferences(context, preferenceRegion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    static void removePrefString(Context context, String preferenceRegion, String preferenceName)
    {
        final SharedPreferences prefs = getPreferences(context, preferenceRegion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(preferenceName);
        editor.apply();
    }

    public static void updatePreferenceString(Context context, String regionUserPreferences, String prefUserSubscription, String subscription)
    {
        final SharedPreferences prefs = getPreferences(context, regionUserPreferences);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefUserSubscription, subscription);
        editor.apply();
    }
}
