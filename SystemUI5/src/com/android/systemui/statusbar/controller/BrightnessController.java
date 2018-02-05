package com.android.systemui.statusbar.controller;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.widget.ImageView;

import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.TicautoToggleSlider;

import java.util.ArrayList;

/**
 * @author lcz
 * @date 17-12-4
 */
public class BrightnessController implements TicautoToggleSlider.Listener {
    private static final String TAG = "StatusBar.BrightnessController";
    private static final boolean SHOW_AUTOMATIC_ICON = false;

    /**
     * {@link android.provider.Settings.System#SCREEN_AUTO_BRIGHTNESS_ADJ} uses the range [-1, 1].
     * Using this factor, it is converted to [0, BRIGHTNESS_ADJ_RESOLUTION] for the SeekBar.
     */
    private static final float BRIGHTNESS_ADJ_RESOLUTION = 2048;

    private final int mMinimumBacklight;
    private final int mMaximumBacklight;
    private final boolean mAutomaticAvailable;
    private boolean mAutomatic;
    private boolean mListening;
    private boolean mExternalChange;

    private final Context mContext;
    private final ImageView mIcon;
    private final TicautoToggleSlider mControl;
    private final IPowerManager mPower;
    private final CurrentUserTracker mUserTracker;
    private final Handler mHandler;
    private final BrightnessController.BrightnessObserver mBrightnessObserver;
    private ArrayList<BrightnessStateChangeCallback> mChangeCallbacks = new ArrayList<>();

    public interface BrightnessStateChangeCallback {
        /**
         * brightness change.
         */
        void onBrightnessLevelChanged();
    }

    /**
     * ContentObserver to watch brightness
     **/
    private class BrightnessObserver extends ContentObserver {

        private final Uri BRIGHTNESS_MODE_URI =
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE);
        private final Uri BRIGHTNESS_URI =
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        private final Uri BRIGHTNESS_ADJ_URI =
                Settings.System.getUriFor(Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ);

        public BrightnessObserver(final Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(final boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(final boolean selfChange, final Uri uri) {
            if (selfChange) {
                return;
            }
            try {
                mExternalChange = true;
                if (BRIGHTNESS_MODE_URI.equals(uri)) {
                    updateMode();
                    updateSlider();
                } else if (BRIGHTNESS_URI.equals(uri) && !mAutomatic) {
                    updateSlider();
                } else if (BRIGHTNESS_ADJ_URI.equals(uri) && mAutomatic) {
                    updateSlider();
                } else {
                    updateMode();
                    updateSlider();
                }
                for (BrightnessStateChangeCallback cb : mChangeCallbacks) {
                    cb.onBrightnessLevelChanged();
                }
            } finally {
                mExternalChange = false;
            }
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    BRIGHTNESS_MODE_URI,
                    false, this, UserHandle.USER_ALL);
            cr.registerContentObserver(
                    BRIGHTNESS_URI,
                    false, this, UserHandle.USER_ALL);
            cr.registerContentObserver(
                    BRIGHTNESS_ADJ_URI,
                    false, this, UserHandle.USER_ALL);
        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }

    }

    /**
     * @param context
     * @param icon
     * @param control
     */
    public BrightnessController(final Context context, final ImageView icon, final TicautoToggleSlider control) {
        mContext = context;
        mIcon = icon;
        mControl = control;
        mHandler = new Handler();
        mUserTracker = new CurrentUserTracker(mContext) {
            @Override
            public void onUserSwitched(int newUserId) {
                updateMode();
                updateSlider();
            }
        };
        mBrightnessObserver = new BrightnessController.BrightnessObserver(mHandler);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mMinimumBacklight = pm.getMinimumScreenBrightnessSetting();
        mMaximumBacklight = pm.getMaximumScreenBrightnessSetting();

        mAutomaticAvailable = context.getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        mPower = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
    }

    /**
     * @param cb addStateChangedCallback.
     */
    public void addStateChangedCallback(final BrightnessStateChangeCallback cb) {
        mChangeCallbacks.add(cb);
    }

    /**
     * @param cb
     * @return removeStateChangedCallback.
     */
    public boolean removeStateChangedCallback(final BrightnessStateChangeCallback cb) {
        return mChangeCallbacks.remove(cb);
    }

    /**
     * registerCallbacks.
     */
    public void registerCallbacks() {
        if (mListening) {
            return;
        }

        mBrightnessObserver.startObserving();
        mUserTracker.startTracking();

        // Update the slider and mode before attaching the listener so we don't
        // receive the onChanged notifications for the initial values.
        updateMode();
        updateSlider();

        mControl.setOnChangedListener(this);
        mListening = true;
    }

    /**
     * Unregister all call backs, both to and from the controller
     */
    public void unregisterCallbacks() {
        if (!mListening) {
            return;
        }

        mBrightnessObserver.stopObserving();
        mUserTracker.stopTracking();
        mControl.setOnChangedListener(null);
        mListening = false;
    }

    private void setMode(final int mode) {
        Settings.System.putIntForUser(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode,
                mUserTracker.getCurrentUserId());
    }

    private void setBrightness(final int brightness) {
        try {
            mPower.setTemporaryScreenBrightnessSettingOverride(brightness);
        } catch (RemoteException ex) {
        }
    }

    private void setBrightnessAdj(final float adj) {
        try {
            mPower.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(adj);
        } catch (RemoteException ex) {
        }
    }

    private void updateIcon(final boolean automatic) {
        if (mIcon != null) {
            mIcon.setImageResource(automatic && SHOW_AUTOMATIC_ICON ?
                    com.android.systemui.R.drawable.ic_qs_brightness_auto_on :
                    com.android.systemui.R.drawable.ic_qs_brightness_auto_off);
        }
    }

    /**
     * Fetch the brightness mode from the system settings and update the icon
     */
    private void updateMode() {
        if (mAutomaticAvailable) {
            int automatic;
            automatic = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                    UserHandle.USER_CURRENT);
            mAutomatic = automatic != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
            updateIcon(mAutomatic);
        } else {
            mControl.setChecked(false);
            updateIcon(false);
        }
    }

    /**
     * Fetch the brightness from the system settings and update the slider
     */
    private void updateSlider() {
        if (mAutomatic) {
            float value = Settings.System.getFloatForUser(mContext.getContentResolver(),
                    Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, 0,
                    UserHandle.USER_CURRENT);
            mControl.setMax((int) BRIGHTNESS_ADJ_RESOLUTION);
            mControl.setValue((int) ((value + 1) * BRIGHTNESS_ADJ_RESOLUTION / 2f));
        } else {
            int value;
            value = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, mMaximumBacklight,
                    UserHandle.USER_CURRENT);
            mControl.setMax(mMaximumBacklight - mMinimumBacklight);
            mControl.setValue(value - mMinimumBacklight);
        }
    }

    @Override
    public void onInit(final TicautoToggleSlider v) {

    }

    @Override
    public void onChanged(final TicautoToggleSlider v, final boolean tracking, final boolean checked,
                          final int value, final boolean fromUser) {
        updateIcon(mAutomatic);
        if (mExternalChange) {
            return;
        }

        if (!mAutomatic) {
            final int val = value + mMinimumBacklight;
            setBrightness(val);
            if (!tracking) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Settings.System.putIntForUser(mContext.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS, val, UserHandle.USER_CURRENT);
                    }
                });
            }
        } else {
            final float adj = value / (BRIGHTNESS_ADJ_RESOLUTION / 2f) - 1;
            setBrightnessAdj(adj);
            if (!tracking) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Settings.System.putFloatForUser(mContext.getContentResolver(),
                                Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, adj, UserHandle.USER_CURRENT);
                    }
                });
            }
        }

        for (BrightnessStateChangeCallback cb : mChangeCallbacks) {
            cb.onBrightnessLevelChanged();
        }
    }
}
