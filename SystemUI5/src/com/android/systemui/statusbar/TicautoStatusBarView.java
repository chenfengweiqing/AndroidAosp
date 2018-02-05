package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * @author lcz
 * @date 17-12-5
 */

public class TicautoStatusBarView extends FrameLayout {

    private TicautoStatusBarEventHelper mEventHelper;

    /**
     * @param context
     */
    public TicautoStatusBarView(final Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public TicautoStatusBarView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public TicautoStatusBarView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param helper add event helper.
     */
    public void setEventHelper(final TicautoStatusBarEventHelper helper) {
        mEventHelper = helper;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return mEventHelper.onTouchEvent(event);
    }

}
