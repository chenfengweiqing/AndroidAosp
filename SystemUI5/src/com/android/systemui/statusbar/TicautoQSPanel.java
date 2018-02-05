package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.tiles.BluetoothTile;
import com.android.systemui.statusbar.tiles.HotspotTile;
import com.android.systemui.statusbar.tiles.MuteTile;
import com.android.systemui.statusbar.tiles.QSTileHost;
import com.android.systemui.statusbar.tiles.SignalTile;
import com.android.systemui.statusbar.tiles.WifiTile;

/**
 * @author lcz
 * @date 17-12-4
 */

public class TicautoQSPanel extends LinearLayout implements TicautoStatusBarEventHelper.Callback {
    private static final String TAG = "QSPanel";
    private TicautoStatusBarEventHelper mEventHelper;
    private OnPanelTouchListener mOnPanelTouchListener;
    private HideQSReceiver mHideQSReceiver;
    private QSTileHost mQSTileHost;

    /**
     * @param context
     */
    public TicautoQSPanel(final Context context) {
        this(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public TicautoQSPanel(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public TicautoQSPanel(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        View.inflate(context, R.layout.ticauto_qs_panel, this);
        mQSTileHost = new QSTileHost(this);
        TextView signalView = (TextView) findViewById(R.id.signal);
        TextView hotspotView = (TextView) findViewById(R.id.hotspot);
        TextView wifiView = (TextView) findViewById(R.id.wifi);
        TextView muteView = (TextView) findViewById(R.id.mute);
        TextView btView = (TextView) findViewById(R.id.bluetooth);
        mHideQSReceiver = new HideQSReceiver();
        IntentFilter filter = new IntentFilter();
        getContext().registerReceiver(mHideQSReceiver, filter);
        mQSTileHost.createTile(BluetoothTile.TILESPEC, btView);
        mQSTileHost.createTile(HotspotTile.TILESPEC, hotspotView);
        mQSTileHost.createTile(MuteTile.TILESPEC, muteView);
        mQSTileHost.createTile(SignalTile.TILESPEC, signalView);
        mQSTileHost.createTile(WifiTile.TILESPEC, wifiView);
    }

    /**
     * @param helper add event helper.
     */
    public void setEventHelper(final TicautoStatusBarEventHelper helper) {
        mEventHelper = helper;
    }

    /**
     * close quick setting.
     */
    public void collapsePanels() {
        mEventHelper.collapse();
    }

    /**
     * open quick setting
     */
    public void expandPanels() {
        mEventHelper.expand();
    }

    @Override
    public int getPanelTranslationY() {
        return (int) getTranslationY();
    }

    @Override
    public void setPanelTranslationY(int translationY) {
        if (translationY > 0) {
            translationY = 0;
        }
        Log.d(TAG, "setPanelTranslationY: translationY " + translationY);
        setTranslationY(translationY);
    }

    @Override
    public void animationEnd(final boolean isExpand) {

    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        if (mOnPanelTouchListener != null) {
            mOnPanelTouchListener.onPanelTouch();
        }
        return mEventHelper.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        super.onTouchEvent(event);
        if (mOnPanelTouchListener != null) {
            mOnPanelTouchListener.onPanelTouch();
        }
        return mEventHelper.onTouchEvent(event);
    }

    private class HideQSReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
//            if (AutoIntent.HIDE_QS_ACTION.equals(action) ||
//                    Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
//                collapsePanels();
//            }
        }
    }

    /**
     * @param listener setOnPanelTouchListener.
     */
    public void setOnPanelTouchListener(final OnPanelTouchListener listener) {
        mOnPanelTouchListener = listener;
    }

    public interface OnPanelTouchListener {
        /**
         * panel touch.
         */
        void onPanelTouch();
    }

}

