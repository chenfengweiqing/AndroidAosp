package com.android.systemui.statusbar.controller;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author lcz
 * @date 17-12-4
 */
public class GpsController {
    private static final String TAG = "GpsController";
    private LocationManager mLocationManager;

    /**
     * 最小卫星数.
     */
    private final int MIN_STAR = 4;

    public interface Callback {
        /**
         * @param enabled onGpsStateChange.
         */
        void onGpsStateChange(final boolean enabled, final boolean isGpsBad);
    }

    private static volatile GpsController sController;
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private Context mContext;
    private boolean mGpsEnable = false;
    private boolean mGpsBad = false;

    private GpsStatus.Listener mGpsListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(final int event) {
            Log.d(TAG, "status:" + event);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count < maxSatellites) {
                        GpsSatellite satellite = iters.next();
                        if (satellite.usedInFix()) {
                            count++;
                        }
                    }
                    if (count >= MIN_STAR) {
                        mGpsBad = false;
                    } else {
                        mGpsBad = true;
                    }
                    fireUpdateState();
                    Log.d(TAG, "max satellites:" + maxSatellites + " cur count " + count + " mGpsBad: " + mGpsBad);
                } else if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                    Log.d(TAG, "GPS_EVENT_FIRST_FIX");
                }
            } else {
                Log.d(TAG, "get ACCESS_FINE_LOCATION permission fail");
            }
        }
    };

    /**
     * @param context
     * @return
     */
    public static GpsController getController(final Context context) {
        if (sController == null) {
            synchronized (GpsController.class) {
                if (sController == null) {
                    sController = new GpsController(context);
                }
            }
        }
        return sController;
    }

    private GpsController(final Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        initListener();
        mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED), false, observer);
    }

    private void initListener() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (isLocationEnabled()) {
                Log.d(TAG, "add addGpsStatusListener ");
                mLocationManager.addGpsStatusListener(mGpsListener);
            } else {
                Log.d(TAG, "remove addGpsStatusListener ");
                mLocationManager.removeGpsStatusListener(mGpsListener);
            }
        } else {
            Log.d(TAG, "get ACCESS_FINE_LOCATION permission fail");
        }
    }

    /**
     * @param cb addStateChangedCallback.
     */
    public void addStateChangedCallback(final Callback cb) {
        mCallbacks.add(cb);
        mGpsEnable = isLocationEnabled();
        fireUpdateState();
    }

    public void removeStateChangedCallback(final Callback cb) {
        mCallbacks.remove(cb);
    }

    /**
     * update status.
     */
    public void fireUpdateState() {
        for (Callback cb : mCallbacks) {
            cb.onGpsStateChange(mGpsEnable, mGpsBad);
        }
    }

    ContentObserver observer = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (mGpsEnable != isLocationEnabled()) {
                mGpsEnable = isLocationEnabled();
                initListener();
                fireUpdateState();
            } else {
                Log.d(TAG, "mGpsEnable status no change ");
            }
        }
    };

    /**
     * Returns true if location isn't disabled in settings.
     */
    private boolean isLocationEnabled() {
        ContentResolver resolver = mContext.getContentResolver();
        int mode = Settings.Secure.getIntForUser(resolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF, ActivityManager.getCurrentUser());
        return mode != Settings.Secure.LOCATION_MODE_OFF;
    }

}
