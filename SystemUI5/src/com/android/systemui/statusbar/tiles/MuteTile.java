package com.android.systemui.statusbar.tiles;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.controller.MuteController;



/**
 * @author lcz
 * @date 17-12-4
 */
public class MuteTile extends QSTile implements MuteController.Callback {
    /**
     * MuteTile sign.
     */
    public static final String TILESPEC = "mute";

    /**
     * @param host
     * @param view
     */
    public MuteTile(final Host host, final TextView view) {
        super(host, view);
        MuteController.getController(getContext()).addStateChangedCallback(this);
    }

    @Override
    protected void handleClick() {
        MuteController.getController(getContext()).toggleMuteState();
    }

    @Override
    protected void handleLongClick() {

    }

    @Override
    protected void handleUpdateState(final State state) {
        final boolean enabled = MuteController.getController(getContext()).isMute();
        state.enabled = enabled;
        state.offIcon = ResourceIcon.get(R.drawable.muteon);
        state.onIcon = ResourceIcon.get(R.drawable.wifi_0);
    }

    @Override
    public void onMuteStateChange(final boolean isMute) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                refreshState();
            }
        });
    }
}
