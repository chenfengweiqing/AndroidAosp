package com.android.systemui.statusbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;

/**
 * @author lcz
 * @date 18-1-15
 */

@SuppressLint("AppCompatCustomView")
public class TicautoToggleSeekBar extends SeekBar {
    private String mAccessibilityLabel;

    /**
     * @param context
     */
    public TicautoToggleSeekBar(final Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public TicautoToggleSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public TicautoToggleSeekBar(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!isEnabled()) {
            setEnabled(true);
        }

        return super.onTouchEvent(event);
    }

    /**
     * @param label label.
     */
    public void setAccessibilityLabel(final String label) {
        mAccessibilityLabel = label;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (mAccessibilityLabel != null) {
            info.setText(mAccessibilityLabel);
        }
    }
}
