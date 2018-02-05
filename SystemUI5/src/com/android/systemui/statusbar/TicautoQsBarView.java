package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

/**
 * @author lcz
 * @date 17-12-4
 */

public class TicautoQsBarView extends FrameLayout {

    private TicautoStatusBarEventHelper mEventHelper;

    /**
     * @param context
     */
    public TicautoQsBarView(final Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public TicautoQsBarView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public TicautoQsBarView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param helper add event helper.
     */
    public void setEventHelper(final TicautoStatusBarEventHelper helper) {
        mEventHelper = helper;
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (!down) {
                    mEventHelper.collapse();
                }
                return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
