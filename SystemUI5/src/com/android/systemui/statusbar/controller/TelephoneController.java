package com.android.systemui.statusbar.controller;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.TelephonyIntents;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * @author lcz
 * @date 17-12-4
 */
public class TelephoneController {
    private static final String TAG = "TelephoneController";

    public interface Callback {
        /**
         * @param level updateTelephoneSignal.
         */
        void updateTelephoneSignal(final int level);

        /**
         * @param state updateTelephoneState.
         */
        void updateTelephoneState(final int state);

        /**
         * @param state
         * @param networkType updateTelephoneType.
         */
        void updateTelephoneType(final int state, final int networkType);

        /**
         * updateTelephoneDataEnabled.
         */
        void updateTelephoneDataEnabled();
    }

    private static volatile TelephoneController sController;

    private TelephonyManager mTelephonyManager;
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private Context mContext;
    private final Uri MOBILE_DATE_URI = Settings.Global.getUriFor(Settings.Global.MOBILE_DATA);
    private int mLevel;
    private int mState;
    private int mNetworkType;
    private Receiver mReceiver;
    private MyPhoneSignalListener mMyPhoneSignalListener = new MyPhoneSignalListener();

    /**
     * @param context
     * @return TelephoneController.
     */
    public static TelephoneController getController(final Context context) {
        if (sController == null) {
            synchronized (TelephoneController.class) {
                if (sController == null) {
                    sController = new TelephoneController(context);
                }
            }
        }
        return sController;
    }

    private TelephoneController(final Context context) {
        mContext = context;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mReceiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        mContext.getContentResolver().registerContentObserver(MOBILE_DATE_URI, false, mMobileDataContentObserver);
        registerPhoneSignalListener();
    }

    private void registerPhoneSignalListener() {
        if (hasTelephony()) {
            mTelephonyManager.listen(mMyPhoneSignalListener, PhoneStateListener.LISTEN_NONE);
            mTelephonyManager.listen(mMyPhoneSignalListener,
                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE
                            | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        } else {
            mTelephonyManager.listen(mMyPhoneSignalListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    /**
     * update TelephoneState.
     */
    @SuppressLint("NewApi")
    public void toggleTelephoneState() {
        if (isMobileDataEnabled()) {
            mTelephonyManager.setDataEnabled(false);
        } else {
            mTelephonyManager.setDataEnabled(true);
        }
        fireUpdateDataEnabled();
    }

    /**
     * @return isMobileDataEnabled.
     */
    public boolean isMobileDataEnabled() {
        boolean isMobileDataEnabled = false;
        if (hasTelephony()) {
            isMobileDataEnabled = mTelephonyManager.getDataEnabled();
        }
        Log.d(TAG, "isMobileDataEnabled   " + isMobileDataEnabled);
        return isMobileDataEnabled;
    }

    /**
     * @param cb add remove StateChangedCallback.
     */
    public void addStateChangedCallback(final Callback cb) {
        mCallbacks.add(cb);
    }

    /**
     * @param cb remove StateChangedCallback.
     */
    public void removeStateChangedCallback(final Callback cb) {
        mCallbacks.remove(cb);
    }

    /**
     * @return hasTelephony.
     */
    public boolean hasTelephony() {
        int simState = mTelephonyManager.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
            case TelephonyManager.SIM_STATE_UNKNOWN:
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                result = false;
                break;
            case TelephonyManager.SIM_STATE_READY:
                result = true;
                break;
        }
        Log.d(TAG, "hasTelephony card  " + result);
        return result;
    }

    private class MyPhoneSignalListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(final SignalStrength signalStrength) {
            try {
                Method getLevel = signalStrength.getClass().getMethod("getLevel");
                mLevel = (int) getLevel.invoke(signalStrength);
                fireUpdateSignal();
                Log.d(TAG, "onSignalStrengthsChanged " + mLevel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceStateChanged(final ServiceState serviceState) {
            mState = serviceState.getState();
            fireUpdateState();
            Log.d(TAG, "onServiceStateChanged " + serviceState.getState());
        }

        @Override
        public void onDataConnectionStateChanged(final int state, final int networkType) {
            Log.d(TAG, "onDataConnectionStateChanged state: " + state + "networkType: " + networkType);
            mState = state;
            mNetworkType = networkType;
            fireUpdateType();
        }

        @Override
        public void onDataActivity(final int direction) {
            Log.d(TAG, " onDataActivity direction " + direction);
        }
    }

    private void fireUpdateSignal() {
        for (Callback cb : mCallbacks) {
            cb.updateTelephoneSignal(mLevel);
        }
    }

    private void fireUpdateState() {
        for (Callback cb : mCallbacks) {
            cb.updateTelephoneState(mState);
        }
    }

    private void fireUpdateType() {
        for (Callback cb : mCallbacks) {
            cb.updateTelephoneType(mState, mNetworkType);
        }
    }

    private void fireUpdateDataEnabled() {
        for (Callback cb : mCallbacks) {
            cb.updateTelephoneDataEnabled();
        }
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                Log.d(TAG, "onReceive  action " + action);
                fireUpdateDataEnabled();
                fireUpdateState();
                registerPhoneSignalListener();
            }
        }
    }


    private ContentObserver mMobileDataContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(final boolean selfChange) {
            super.onChange(selfChange);
        }

        @Override
        public void onChange(final boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (MOBILE_DATE_URI.equals(uri)) {
                Log.d(TAG, "mMobileDataContentObserver  action ");
                fireUpdateDataEnabled();
            }
        }
    };

}
