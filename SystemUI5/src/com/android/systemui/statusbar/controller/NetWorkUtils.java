package com.android.systemui.statusbar.controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Locale;

/**
 * @author lcz
 * @date 17-12-4
 */
public class NetWorkUtils {
    /**
     * @param context
     * @return
     */
    public static int getNetworkType(final Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        if (networkInfo == null) {
            return -1;
        }
        return networkInfo.getType();
    }


    /**
     * @param context
     * @return isConnectNetWork.
     */
    public static boolean isConnectNetWork(final Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    /**
     * @param context
     * @return NetworkInfo type.
     */
    public static String getTypeName(final Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null ? networkInfo.getTypeName() : "";
    }

    /**
     * @param context
     * @return NetworkInfo.
     */
    public static NetworkInfo getNetworkInfo(final Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo();
    }

    /**
     * @param context
     * @return WifiInfo.
     */
    public static WifiInfo getWifiInfo(final Context context) {
        WifiManager manager = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
        return manager.getConnectionInfo();
    }

    /**
     * @param context
     * @return IpAddress.
     */
    public static String getIpAddress(final Context context) {
        WifiInfo info = getWifiInfo(context);
        if (info == null || info.getIpAddress() == 0) {
            return "";
        }
        int ip = info.getIpAddress();
        return String.format(Locale.US, "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
    }
}
