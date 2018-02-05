package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.statusbar.controller.BluetoothController;
import com.android.systemui.statusbar.controller.GpsController;
import com.android.systemui.statusbar.controller.MuteController;
import com.android.systemui.statusbar.controller.TelephoneController;
import com.android.systemui.statusbar.controller.WifiApAdminController;

/**
 * @author lcz
 * @date 17-12-7
 */

public class TicautoSignalClusterView extends LinearLayout implements BluetoothController.Callback,
        WifiApAdminController.Callback, TelephoneController.Callback, GpsController.Callback, MuteController.Callback {
    private static final String TAG = "SignalClusterView";
    private static final int[] WIFI_ICONS = {
            R.drawable.wifi_0,
            R.drawable.wifi_1,
            R.drawable.wifi_2,
            R.drawable.wifi_3,
            R.drawable.wifi_4
    };

    private static final int[] SIGNAL_ICONS = {
            R.drawable.mobile_signal_0,
            R.drawable.mobile_signal_1,
            R.drawable.mobile_signal_2,
            R.drawable.mobile_signal_3,
            R.drawable.mobile_signal_4,
            R.drawable.mobile_signal_5
    };

    private ImageView mMuteView, mBluetoothView, mMobileSignalView, mWifiSignalView, mGpsView, mHotspotView;

    /**
     * @param context
     */
    public TicautoSignalClusterView(final Context context) {
        this(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public TicautoSignalClusterView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public TicautoSignalClusterView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOrientation(HORIZONTAL);
        View.inflate(context, R.layout.ticauto_signal_cluster_view, this);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mMuteView = (ImageView) findViewById(R.id.icon_mute);
        mBluetoothView = (ImageView) findViewById(R.id.icon_bluetooth);
        mMobileSignalView = (ImageView) findViewById(R.id.icon_mobile_signal);
        mWifiSignalView = (ImageView) findViewById(R.id.icon_wifi);
        mGpsView = (ImageView) findViewById(R.id.icon_gps);
        mHotspotView = (ImageView) findViewById(R.id.icon_hotspot);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        BluetoothController.getController(getContext()).addStateChangedCallback(this);
        WifiApAdminController.getController(getContext()).addStateChangedCallback(this);
        TelephoneController.getController(getContext()).addStateChangedCallback(this);
        MuteController.getController(getContext()).addStateChangedCallback(this);
        GpsController.getController(getContext()).addStateChangedCallback(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BluetoothController.getController(getContext()).removeStateChangedCallback(this);
        WifiApAdminController.getController(getContext()).removeStateChangedCallback(this);
        TelephoneController.getController(getContext()).removeStateChangedCallback(this);
        GpsController.getController(getContext()).removeStateChangedCallback(this);
        MuteController.getController(getContext()).removeStateChangedCallback(this);
    }

    @Override
    public void onBluetoothStateChange(final boolean enabled, final boolean isConnected) {
        if (enabled) {
            int resId = isConnected ? R.drawable.bluetooth_on : R.drawable.bluetooth_connected;
            mBluetoothView.setImageResource(resId);
            mBluetoothView.setVisibility(VISIBLE);
        } else {
            mBluetoothView.setVisibility(GONE);
        }
    }

    @Override
    public void onWifiStateChange(final boolean enabled, final int level) {
        if (enabled) {
            mWifiSignalView.setVisibility(View.VISIBLE);
            mWifiSignalView.setImageResource(WIFI_ICONS[level]);
        } else {
            mWifiSignalView.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateTelephoneSignal(final int level) {
        mMobileSignalView.setImageResource(SIGNAL_ICONS[level]);
    }

    @Override
    public void updateTelephoneState(final int state) {
        mMobileSignalView.setVisibility(TelephoneController.getController(getContext()).hasTelephony() ? VISIBLE : GONE);
    }

    @Override
    public void updateTelephoneType(final int state, final int networkType) {
        if (state != TelephonyManager.DATA_CONNECTED && state != TelephonyManager.DATA_CONNECTING) {
            mMobileSignalView.setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                //Need to be 2G
                mMobileSignalView.setBackgroundResource(R.drawable.mobile_signal_3g);
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                mMobileSignalView.setBackgroundResource(R.drawable.mobile_signal_3g);
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
                mMobileSignalView.setBackgroundResource(R.drawable.mobile_signal_h);
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                //Need to be 4G
                mMobileSignalView.setBackgroundResource(R.drawable.mobile_signal_h);
                break;
            default:
                mMobileSignalView.setBackgroundColor(Color.TRANSPARENT);
                break;
        }
    }

    @Override
    public void updateTelephoneDataEnabled() {

    }

    @Override
    public void onHotspotStateChange(final boolean enabled) {
        mHotspotView.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onMuteStateChange(final boolean isMute) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mMuteView.setVisibility(isMute ? VISIBLE : GONE);
            }
        });
    }

    @Override
    public void onGpsStateChange(boolean enabled, boolean isGpsBad) {
        Log.d(TAG, "onGpsStateChange: enabled " + enabled + "  isGpsBad " + isGpsBad);
        if (enabled && isGpsBad) {
            mGpsView.setVisibility(VISIBLE);
        } else {
            mGpsView.setVisibility(GONE);
        }
    }
}
