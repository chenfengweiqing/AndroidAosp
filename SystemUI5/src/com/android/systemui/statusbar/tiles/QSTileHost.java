package com.android.systemui.statusbar.tiles;

import android.content.Context;
import android.widget.TextView;

import com.android.systemui.statusbar.TicautoQSPanel;

/**
 * @author lcz
 * @date 17-12-4
 */
public class QSTileHost implements QSTile.Host {

    private Context mContext;
    private TicautoQSPanel mQSPanel;

    /**
     * @param panel
     */
    public QSTileHost(final TicautoQSPanel panel) {
        mContext = panel.getContext();
        mQSPanel = panel;
    }

    @Override
    public void collapsePanels() {
        mQSPanel.collapsePanels();
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    /**
     * @param tileSpec
     * @param tileView
     * @return
     */
    public QSTile createTile(final String tileSpec, final TextView tileView) {
        if (BluetoothTile.TILESPEC.equals(tileSpec)) {
            return new BluetoothTile(this, tileView);
        } else if (HotspotTile.TILESPEC.equals(tileSpec)) {
            return new HotspotTile(this, tileView);
        } else if (MuteTile.TILESPEC.equals(tileSpec)) {
            return new MuteTile(this, tileView);
        } else if (SignalTile.TILESPEC.equals(tileSpec)) {
            return new SignalTile(this, tileView);
        } else if (WifiTile.TILESPEC.equals(tileSpec)) {
            return new WifiTile(this, tileView);
        }
        return null;
    }

}
