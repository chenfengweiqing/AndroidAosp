package com.android.systemui.statusbar.tiles;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.provider.Settings;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.controller.WifiApAdminController;

/**
 * @author lcz
 * @date 17-12-4
 */
public class WifiTile extends QSTile implements WifiApAdminController.Callback {
    /**
     * WifiTile sign.
     */
    public static final String TILESPEC = "wifi";

    /**
     * @param host
     * @param view
     */
    public WifiTile(final Host host, final TextView view) {
        super(host, view);
        WifiApAdminController.getController(getContext()).addStateChangedCallback(this);
    }

    @Override
    protected void handleClick() {
        WifiApAdminController.getController(getContext()).toggleWifiState();
    }

    @Override
    protected void handleLongClick() {
        mHost.collapsePanels();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_WIFI_SETTINGS);
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
        final boolean enabled = WifiApAdminController.getController(getContext()).isWifiEnabled();
        state.enabled = enabled;
        state.offIcon = ResourceIcon.get(R.drawable.wifi_0);
        state.onIcon = ResourceIcon.get(R.drawable.wifi_4);
    }

    @Override
    public void onWifiStateChange(final boolean enabled, final int level) {
        refreshState();
    }

    @Override
    public void onHotspotStateChange(final boolean enabled) {

    }
}
