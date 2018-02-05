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
public class HotspotTile extends QSTile implements WifiApAdminController.Callback {
    /**
     * HotspotTile sign.
     */
    public static final String TILESPEC = "hotspot";

    /**
     * @param host
     * @param view
     */
    public HotspotTile(final Host host, final TextView view) {
        super(host, view);
        WifiApAdminController.getController(getContext()).addStateChangedCallback(this);
    }

    @Override
    protected void handleClick() {
        WifiApAdminController.getController(getContext()).toggleHotspotState();
    }

    @Override
    protected void handleLongClick() {
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
        final boolean enabled = WifiApAdminController.getController(getContext()).isHotspotEnabled();
        state.enabled = enabled;
        state.offIcon = ResourceIcon.get(R.drawable.ic_hotspot);
        state.onIcon = ResourceIcon.get(R.drawable.ic_hotspot);
    }

    @Override
    public void onWifiStateChange(final boolean enabled, final int level) {

    }

    @Override
    public void onHotspotStateChange(final boolean enabled) {
        refreshState();
    }
}
