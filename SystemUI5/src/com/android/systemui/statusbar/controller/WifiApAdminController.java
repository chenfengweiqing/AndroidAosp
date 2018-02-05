package com.android.systemui.statusbar.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

/**
 * @author lcz
 * @date 17-12-4
 */
public class WifiApAdminController {
    private static final String TAG = "WifiApAdminController";
    private static final String WIFI = "WIFI";
    private static final String OPEN_HOT_POINT = "com.ticauto.settings.OPEN_HOT_POINT";
    private static final String CLOSE_HOT_POINT = "com.ticauto.settings.CLOSE_HOT_POINT";

    public interface Callback {
        /**
         * @param enabled
         * @param level
         */
        void onWifiStateChange(final boolean enabled, final int level);

        /**
         * @param enabled hot point
         */
        void onHotspotStateChange(final boolean enabled);
    }

    private static volatile WifiApAdminController sController;

    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private final Receiver mReceiver = new Receiver();
    private Context mContext;
    private boolean mWifiEnabled;
    private boolean mHotspotEnabled;
    private WifiManager mWifiManager;
    private int mLevel;

    /**
     * @param context
     * @return
     */
    public static WifiApAdminController getController(final Context context) {
        if (sController == null) {
            synchronized (WifiApAdminController.class) {
                if (sController == null) {
                    sController = new WifiApAdminController(context);
                }
            }
        }
        return sController;
    }

    private WifiApAdminController(final Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mReceiver.register();
        updateState();
    }

    /**
     * @param cb addStateChangedCallback
     */
    public void addStateChangedCallback(final Callback cb) {
        mCallbacks.add(cb);
        fireStateChange();
    }

    /**
     * @param cb removeStateChangedCallback.
     */
    public void removeStateChangedCallback(final Callback cb) {
        mCallbacks.remove(cb);
    }

    /**
     * toggleWifiState.
     */
    public void toggleWifiState() {
        setWifiEnabled(!isWifiEnabled());
    }

    /**
     * @return isWifiEnabled.
     */
    public boolean isWifiEnabled() {
        return mWifiEnabled;
    }

    /**
     * @param enabled setWifiEnabled enabled.
     */
    public void setWifiEnabled(final boolean enabled) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... args) {
                // Disable tethering if enabling Wifi
                final int wifiApState = mWifiManager.getWifiApState();
                if (enabled && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                        (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
                    mWifiManager.setWifiApEnabled(null, false);
                }

                mWifiManager.setWifiEnabled(enabled);
                return null;
            }
        }.execute();
    }

    /**
     * toggleHotspotState.
     */
    public void toggleHotspotState() {
        setHotspotEnabled(!isHotspotEnabled());
    }

    /**
     * @return isHotspotEnabled.
     */
    public boolean isHotspotEnabled() {
        return mHotspotEnabled;
    }

    /**
     * @param enable setHotspotEnabled.
     */
    public void setHotspotEnabled(final boolean enable) {
        if (enable) {
            mContext.sendBroadcast(new Intent(OPEN_HOT_POINT));
        } else {
            mContext.sendBroadcast(new Intent(CLOSE_HOT_POINT));
        }
    }

    private void fireStateChange() {
        for (Callback cb : mCallbacks) {
            cb.onHotspotStateChange(mHotspotEnabled);
            cb.onWifiStateChange(mWifiEnabled, mLevel);

        }
    }

    private void updateState() {
        mWifiEnabled = mWifiManager.isWifiEnabled();
        if (mWifiEnabled) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (WIFI.equals(NetWorkUtils.getTypeName(mContext))) {
                if (NetWorkUtils.isConnectNetWork(mContext)) {
                    mLevel = WifiManager.calculateSignalLevel(info.getRssi(), 5);
                } else {
                    Log.d(TAG, "updateWifiState　wifi not connect mLevel set 0 ");
                    mLevel = 0;
                }
            } else {
                Log.d(TAG, "updateWifiState　connection type is not wifi mLevel set 0 ");
                mLevel = 0;
            }
        } else {
            Log.d(TAG, "updateWifiState　wifi disable");
        }
        int state = mWifiManager.getWifiApState();
        mHotspotEnabled = state == WifiManager.WIFI_AP_STATE_ENABLED;
        fireStateChange();
    }

    private final class Receiver extends BroadcastReceiver {
        public void register() {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
            mContext.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            updateState();
        }
    }
}
