package com.android.systemui.statusbar.controller;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;

import static android.bluetooth.BluetoothAdapter.ERROR;

/**
 * @author lcz
 * @date 17-12-4
 */
public class BluetoothController {
    private static final String TAG = "system.ui.bluetooth.ctr";

    public interface Callback {
        /**
         * @param enabled     bluetooth open status.
         * @param isConnected bluetooth connect status.
         */
        void onBluetoothStateChange(final boolean enabled, final boolean isConnected);
    }

    private static volatile BluetoothController sController;

    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private final Receiver mReceiver = new Receiver();
    private BluetoothAdapter mAdapter;
    private Context mContext;
    private boolean mEnabled;
    private boolean mIsConnected;
    private boolean mIsHfpConnected;
    private boolean mIsA2dpConnected;
    private boolean mIsA2dpSinkConnected;
    private boolean mIsCustomConnected;

    /**
     * @param context
     * @return BluetoothController.
     */
    public static BluetoothController getController(final Context context) {
        if (sController == null) {
            synchronized (BluetoothController.class) {
                if (sController == null) {
                    sController = new BluetoothController(context);
                }
            }
        }
        return sController;
    }

    private BluetoothController(final Context context) {
        mContext = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = bluetoothManager.getAdapter();
        if (mAdapter == null) {
            Log.d(TAG, "Default BT adapter not found");
            return;
        }
        mReceiver.register();
        setAdapterState(mAdapter.getState());
    }

    /**
     * @param cb addStateChangedCallback.
     */
    public void addStateChangedCallback(final Callback cb) {
        mCallbacks.add(cb);
        fireStateChange(cb);
    }

    /**
     * @param cb removeStateChangedCallback.
     */
    public void removeStateChangedCallback(final Callback cb) {
        mCallbacks.remove(cb);
    }

    /**
     * toggleBluetoothState.
     */
    public void toggleBluetoothState() {
        setBluetoothEnabled(!isBluetoothEnabled());
    }

    /**
     * @param enabled setBluetoothEnabled
     */
    public void setBluetoothEnabled(final boolean enabled) {
        if (mAdapter != null) {
            if (enabled) {
                mAdapter.enable();
            } else {
                mAdapter.disable();
            }
        }
    }

    public boolean isBluetoothEnabled() {
        return mEnabled;
    }

    private void fireStateChange(final Callback cb) {
        Log.d(TAG, "fireStateChange bluetooth mEnabled : " + mEnabled + " mIsConnected " + mIsConnected);
        cb.onBluetoothStateChange(mEnabled, mIsConnected);

    }

    private void setAdapterState(final int adapterState) {
        final boolean enabled = (adapterState == BluetoothAdapter.STATE_ON ||
                adapterState == BluetoothAdapter.STATE_TURNING_ON);
        if (mEnabled == enabled) {
            return;
        }
        mEnabled = enabled;
        fireStateChange();
    }

    private void fireStateChange() {
        for (Callback cb : mCallbacks) {
            fireStateChange(cb);
        }
    }

    private final class Receiver extends BroadcastReceiver {
        public void register() {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            mContext.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive action=" + action);
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                setAdapterState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, ERROR));
                Log.d(TAG, "ACTION_STATE_CHANGED " + mEnabled);
            } else if (action.equals(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
                Log.d(TAG, "BluetoothHeadsetClient bluetooth connect state: " + state);
                mIsHfpConnected = state == BluetoothProfile.STATE_CONNECTED;
                mIsConnected = mIsA2dpConnected || mIsHfpConnected || mIsCustomConnected || mIsA2dpSinkConnected;
                fireStateChange();
            } else if (action.equals(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
                Log.d(TAG, "BluetoothA2dpSink bluetooth connect state: " + state);
                mIsA2dpSinkConnected = state == BluetoothProfile.STATE_CONNECTED;
                mIsConnected = mIsA2dpConnected || mIsHfpConnected || mIsCustomConnected || mIsA2dpSinkConnected;
                fireStateChange();
            } else if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
                Log.d(TAG, "BluetoothA2dp bluetooth connect state: " + state);
                mIsA2dpConnected = state == BluetoothProfile.STATE_CONNECTED;
                mIsConnected = mIsA2dpConnected || mIsHfpConnected || mIsCustomConnected || mIsA2dpSinkConnected;
                fireStateChange();
            } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
                Log.d(TAG, "ACTION_CONNECTION_STATE_CHANGED bluetooth connect state: " + state);
                mIsCustomConnected = state == BluetoothProfile.STATE_CONNECTED;
                mIsConnected = mIsA2dpConnected || mIsHfpConnected || mIsCustomConnected || mIsA2dpSinkConnected;
                fireStateChange();
            }
        }
    }
}
