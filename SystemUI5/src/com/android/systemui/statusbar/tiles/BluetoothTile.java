package com.android.systemui.statusbar.tiles;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.provider.Settings;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.controller.BluetoothController;

/**
 * @author lcz
 * @date 17-12-4
 */
public class BluetoothTile extends QSTile implements BluetoothController.Callback {
    /**
     * BluetoothTile sign.
     */
    public static final String TILESPEC = "bt";

    /**
     * @param host
     * @param view
     */
    public BluetoothTile(final Host host, final TextView view) {
        super(host, view);
        BluetoothController.getController(getContext()).addStateChangedCallback(this);
    }

    @Override
    protected void handleClick() {
        BluetoothController.getController(getContext()).toggleBluetoothState();
    }

    @Override
    protected void handleLongClick() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
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
        final boolean enabled = BluetoothController.getController(getContext()).isBluetoothEnabled();
        state.enabled = enabled;
        state.offIcon = ResourceIcon.get(R.drawable.bluetooth_on);
        state.onIcon = ResourceIcon.get(R.drawable.bluetooth_connected);

    }

    @Override
    public void onBluetoothStateChange(final boolean enabled, final boolean isConnected) {
        refreshState();
    }
}
