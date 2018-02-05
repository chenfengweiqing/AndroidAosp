package com.android.systemui.statusbar.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.statusbar.TicautoToggleSlider;

/**
 * @author lcz
 * @date 17-12-4
 */

public class VolumeController implements MuteController.Callback, TicautoToggleSlider.Listener {
    private static final String TAG = "VolumeController";
    private int mMaxVolume;
    private int mPlayId;
    private int mCurControlResId = -1;
    private boolean mListening;
    private Context mContext;
    private TicautoToggleSlider mVolumeSeekBar;
    private AudioManager mAudioManager;
    private SoundPool mSoundPool = null;
    private VolumeReceiver mVolumeReceiver = null;

    /**
     * @param context
     */
    public VolumeController(final Context context) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int volumeType = AudioManager.STREAM_MUSIC;
        mMaxVolume = mAudioManager.getStreamMaxVolume(volumeType);
//        mSoundPool = new SoundPool(5, volumeType, 5);
//        mPlayId = mSoundPool.load(mContext, R.raw.media_volume, 1);
    }

    /**
     * @param context
     * @param control
     */
    public VolumeController(final Context context, final TicautoToggleSlider control) {
        this(context);
        mVolumeSeekBar = control;
    }

    /**
     * @param control init.
     */
    @Override
    public void onInit(final TicautoToggleSlider control) {
        // Do nothing
    }

    /**
     * registerCallbacks.
     */
    public void registerCallbacks() {
        if (mListening) {
            return;
        }
        updateSlider();
        registerSoundChange();
        mVolumeSeekBar.setOnChangedListener(this);
        mListening = true;
        MuteController.getController(mContext).addStateChangedCallback(this);
    }

    /**
     * refresh.
     */
    public void refresh() {
        int volumeType = AudioManager.STREAM_MUSIC;
        int currVolume = mAudioManager.getStreamVolume(volumeType);
        Log.d(TAG, "refresh set volume value " + currVolume);
        mVolumeSeekBar.setValue(currVolume);
    }

    protected void registerSoundChange() {
        mVolumeReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
        mContext.registerReceiver(mVolumeReceiver, filter);
    }

    /**
     * Unregister all call backs, both to and from the controller
     */
    public void unregisterCallbacks() {
        if (!mListening) {
            return;
        }
        if (mVolumeReceiver != null) {
            mContext.unregisterReceiver(mVolumeReceiver);
        }
        mVolumeSeekBar.setOnChangedListener(null);
        mListening = false;
    }

    @Override
    public void onChanged(final TicautoToggleSlider view, final boolean tracking, final boolean automatic, final int value, final boolean fromUser) {
        Log.d(TAG, "onChanged value " + value + " fromUser " + fromUser);

        if (mOnProgressChangeListener != null) {
            mOnProgressChangeListener.onProgressChange();
        }
        if (fromUser) {
            int volumeType = AudioManager.STREAM_MUSIC;
            final int volume = value;
//            if (mAudioManager.isMasterMute()) {
//                Log.d(TAG, "onChanged isMasterMute is true");
//                mAudioManager.setMasterMute(false, 0);
//            }
            mAudioManager.setStreamVolume(volumeType, value, AudioManager.FLAG_ALLOW_RINGER_MODES);
//            if (tracking) {
//                mSoundPool.pause(mPlayId);
//            } else {
//                mSoundPool.play(mPlayId, 1, 1, 0, 0, 1);
//            }
        }
//        if (mVolumeSeekBar != null && mCurControlResId != R.color.sound_seek_bar_color) {
//            updateProgressColor(false);
//        }
    }

    private void updateSlider() {
        mVolumeSeekBar.setMax(mMaxVolume);
    }

    @Override
    public void onMuteStateChange(final boolean isMute) {
        if (mVolumeSeekBar != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    updateProgressColor(isMute);
                }
            });
        }
    }

    private void updateProgressColor(final boolean isMute) {
//        mCurControlResId = isMute ? R.color.fab_shape : R.color.fab_ripple;
        LayerDrawable layerDrawable = (LayerDrawable) mVolumeSeekBar.getProgressDrawable();
        Drawable drawable = layerDrawable.findDrawableByLayerId(android.R.id.progress);
        drawable.setColorFilter(mContext.getResources().getColor(mCurControlResId), PorterDuff.Mode.SRC);
        mVolumeSeekBar.invalidate();
    }

    private class VolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (AudioManager.VOLUME_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "VolumeReceiver action " + action);
                refresh();
            }
        }
    }

    private OnProgressChangeListener mOnProgressChangeListener;

    public void setProgressChangeListener(final OnProgressChangeListener listener) {
        mOnProgressChangeListener = listener;
    }

    public interface OnProgressChangeListener {
        /**
         * onProgressChange.
         */
        void onProgressChange();
    }
}
