package com.android.systemui.statusbar.controller;

import android.content.Context;
import android.media.AudioManager;

import java.util.ArrayList;

/**
 * @author lcz
 * @date 17-12-4
 */

public class MuteController {

    public interface Callback {
        /**
         * @param isMute onMuteStateChange.
         */
        void onMuteStateChange(final boolean isMute);
    }

    private static volatile MuteController sController;
    private final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
    private Context mContext;
    private AudioManager mAudioManager;

    /**
     * @param context
     * @return MuteController.
     */
    public static MuteController getController(final Context context) {
        if (sController == null) {
            synchronized (MuteController.class) {
                if (sController == null) {
                    sController = new MuteController(context);
                }
            }
        }
        return sController;
    }

    private MuteController(final Context context) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        fireStateChange();
    }

    /**
     * @param cb addStateChangedCallback.
     */
    public void addStateChangedCallback(final MuteController.Callback cb) {
        mCallbacks.add(cb);
        fireStateChange(cb);
    }

    /**
     * @param cb removeStateChangedCallback.
     */
    public void removeStateChangedCallback(final MuteController.Callback cb) {
        mCallbacks.remove(cb);
    }

    /**
     * toggleMuteState.
     */
    public void toggleMuteState() {
        if (isMute()) {
            mAudioManager.setMasterMute(false, 0);
        } else {
            mAudioManager.setMasterMute(true, 0);
        }
    }

    /**
     * @return isMute.
     */
    public boolean isMute() {
        return mAudioManager.isMasterMute();
    }

    private void fireStateChange(final MuteController.Callback cb) {
        cb.onMuteStateChange(isMute());
    }

    /**
     * update status
     */
    public void fireStateChange() {
        for (Callback cb : mCallbacks) {
            fireStateChange(cb);
        }
    }

}


