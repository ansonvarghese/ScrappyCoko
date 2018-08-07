package com.myscrap.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.security.MessageDigest;

/**
 * Created by ms3 on 5/13/2017.
 */

public class DeviceUtils
{
    private static String uniqueId;
    private static int screenWidth = 0;
    private static int screenHeight = 0;

    public static void init(Context ctx)
    {
        if(!isValidUniqueId())
            uniqueId = getSerialNo(ctx);
        if(!isValidUniqueId())
            uniqueId = getAndroidId(ctx);
        if(!isValidUniqueId())
            uniqueId = getMACAddress(ctx);

    }

    private static boolean isValidUniqueId()
    {
        return uniqueId != null && !"".equalsIgnoreCase(uniqueId) && !uniqueId.equals("000000000000000") ;
    }

    private static String getDeviceId(Context context)
    {
        if (!isValidUniqueId())
            init(context);
        return uniqueId;
    }

    @SuppressLint("HardwareIds")
    private static String getSerialNo(Context context){
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            return (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {

        }
        return null;
    }

    @SuppressLint("HardwareIds")
    private static String getAndroidId(Context context){
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
        }
        return null;
    }

    @SuppressLint("HardwareIds")
    private static String getMACAddress(Context context){
        try {
            WifiManager wm = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                return wm.getConnectionInfo().getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getMD5(Context context) {
        try {
            if (uniqueId == null || uniqueId.equals("000000000000000") || uniqueId.startsWith("000"))
                init(context);
            if(uniqueId != null){
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(uniqueId.getBytes());
                byte messageDigest[] = digest.digest();
                StringBuilder hexString = new StringBuilder();
                for (byte aMessageDigest : messageDigest) {
                    StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                    while (h.length() < 2)
                        h.insert(0, "0");
                    hexString.append(h);
                }
                return hexString.toString();
            }else{
                return null;
            }
        } catch (Exception e) {}
        return "";
    }


    private static void storeUUID(Context context,  String type, String uuiD)
    {
        final SharedPreferences prefs = context.getSharedPreferences("com.device.UUID", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(type, uuiD);
        editor.apply();
    }

    public static String getUUID(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences("com.device.UUID", Context.MODE_PRIVATE);
        String uuID = prefs.getString("dm_UUID", "");
        if ("".equalsIgnoreCase(uuID)) {
            uuID = getDeviceId(context);
            storeUUID(context,"dm_UUID", uuID);
        }
        return uuID;
    }


    public static String getDeviceName() {
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    public static String getDeviceBrand() {
        String brand = Build.BRAND;
        if (brand != null && !brand.equalsIgnoreCase("")) {
            return capitalize(brand);
        } else
            return "";
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display;
            if (wm != null) {
                display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenHeight = size.y;
            }

        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = null;
            if (wm != null) {
                display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
            }
        }
        return screenWidth;
    }

    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static int getRandomMaterialColor(Context context, String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array", context.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    public static int dp2px(Context context, int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal,context.getResources().getDisplayMetrics());
    }
}
