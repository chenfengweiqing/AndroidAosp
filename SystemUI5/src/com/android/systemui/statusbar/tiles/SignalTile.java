package com.android.systemui.statusbar.tiles;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.provider.Settings;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.controller.TelephoneController;

/**
 * @author lcz
 * @date 17-12-4
 */
public class SignalTile extends QSTile implements TelephoneController.Callback {
    /**
     * SignalTile sign.
     */
    public static final String TILESPEC = "signal";

    public SignalTile(final Host host, final TextView view) {
        super(host, view);
        TelephoneController.getController(getContext()).addStateChangedCallback(this);
    }

    @Override
    protected void handleClick() {
        if (TelephoneController.getController(getContext()).hasTelephony()) {
            TelephoneController.getController(getContext()).toggleTelephoneState();
        } else {
            refreshState();
        }
    }

    @Override
    protected void handleLongClick() {
        mHost.collapsePanels();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mHost.collapsePanels();
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleUpdateState(final State state) {
        final boolean enabled = TelephoneController.getController(getContext()).isMobileDataEnabled();
        state.enabled = enabled;
        state.offIcon = ResourceIcon.get(R.drawable.mobile_signal_0);
        state.onIcon = ResourceIcon.get(R.drawable.mobile_signal_5);
    }

    @Override
    public void updateTelephoneState(final int state) {

    }

    @Override
    public void updateTelephoneSignal(final int level) {

    }

    @Override
    public void updateTelephoneType(final int state, final int networkType) {

    }

    @Override
    public void updateTelephoneDataEnabled() {
        refreshState();
    }

}
