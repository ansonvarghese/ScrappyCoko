package com.myscrap.webservice;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Ms2 on 2/6/2016.
 */
public class CheckNetworkConnection {
    public static boolean isConnectionAvailable(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected() && netInfo.isConnectedOrConnecting() && netInfo.isAvailable()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public static String getIPAddress(Context context) {
    String ipAddress = null;
        boolean WIFI = false;
        boolean MOBILE = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks;
            if (connectivityManager != null) {
                networks = connectivityManager.getAllNetworks();
                NetworkInfo networkInfo;
                for (Network mNetwork : networks) {
                    networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                    if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                        if (networkInfo.getTypeName().equalsIgnoreCase("WIFI"))
                            if (networkInfo.isConnected())
                                WIFI = true;
                        if (networkInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                            if (networkInfo.isConnected())
                                MOBILE = true;
                    }
                }
            }

        } else {
            if (connectivityManager != null) {
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo networkInfo : info) {
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            if (networkInfo.getTypeName().equalsIgnoreCase("WIFI"))
                                if (networkInfo.isConnected())
                                    WIFI = true;
                            if (networkInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                                if (networkInfo.isConnected())
                                    MOBILE = true;
                        }
                    }
                }
            }
        }
        if(WIFI) {
            ipAddress = GetDeviceIpWiFiData(context);
        }
        if(MOBILE) {
            ipAddress = GetDeviceIpMobileData();
        }
        return ipAddress;
    }


    private static String GetDeviceIpMobileData(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface networkinterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddress = networkinterface.getInetAddresses(); enumIpAddress.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Current IP", ex.toString());
        }
        return null;
    }

    private static String GetDeviceIpWiFiData(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = null;
        if (wm != null) {
            ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        }
        return ip;
    }
}
