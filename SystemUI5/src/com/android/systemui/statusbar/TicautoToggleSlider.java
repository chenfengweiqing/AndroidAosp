package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.settings.ToggleSeekBar;

/**
 * @author lcz
 * @date 18-1-15
 */
public class TicautoToggleSlider extends RelativeLayout {
    private boolean mTracking;
    private Listener mListener;
    private ToggleSeekBar mSlider;
    private ImageView mTopIconView;
    private TextView mTitleView;

    public interface Listener {
        /**
         * @param v init TicautoToggleSlider.
         */
        void onInit(final TicautoToggleSlider v);

        /**
         * @param v
         * @param tracking
         * @param checked
         * @param value
         * @param fromUser is user.
         */
        void onChanged(final TicautoToggleSlider v, final boolean tracking, final boolean checked, final int value, final boolean fromUser);
    }

    /**
     * @param context
     */
    public TicautoToggleSlider(final Context context) {
        this(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public TicautoToggleSlider(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public TicautoToggleSlider(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.ticauto_qs_panel_toggle_slider, this);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToggleSlider, defStyle, 0);
        mSlider = (ToggleSeekBar) findViewById(R.id.slider);
        mSlider.setOnSeekBarChangeListener(mSeekListener);
        mTopIconView = (ImageView) findViewById(R.id.top_icon);
        mTitleView = (TextView) findViewById(R.id.title);
        a.recycle();
    }

    /**
     * @param drawable set progress drawable.
     */
    public void setProgressDrawable(final Drawable drawable) {
        mSlider.setProgressDrawable(drawable);
    }

    /**
     * @param drawable set thumb drawable.
     */
    public void setThumb(final Drawable drawable) {
        mSlider.setThumb(drawable);
    }

    /**
     * @param drawable set top drawable.
     */
    public void setTopIcon(final Drawable drawable) {
        mTopIconView.setImageDrawable(drawable);
    }

    /**
     * @param title set title.
     */
    public void setTitle(final String title) {
        mTitleView.setText(title);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mListener != null) {
            mListener.onInit(this);
        }
    }

    /**
     * @param l set change listener.
     */
    public void setOnChangedListener(final Listener l) {
        mListener = l;
    }

    /**
     * @param checked set is auto adjust value.
     */
    public void setChecked(final boolean checked) {

    }

    /**
     * @return return is auto adjust value.
     */
    public boolean isChecked() {
        return false;
    }

    /**
     * @param max set max value.
     */
    public void setMax(final int max) {
        mSlider.setMax(max);
    }

    /**
     * @param value set value.
     */
    public void setValue(final int value) {
        mSlider.setProgress(value);
    }

    /**
     * @return progress drawable.
     */
    public Drawable getProgressDrawable() {
        return mSlider.getProgressDrawable();
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
            mTracking = true;
            if (mListener != null) {
                mListener.onChanged(TicautoToggleSlider.this, mTracking, isChecked(), progress, fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) {
            mTracking = true;
            if (mListener != null) {
                mListener.onChanged(TicautoToggleSlider.this, mTracking, isChecked(), mSlider.getProgress(), true);
            }
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            mTracking = false;
            if (mListener != null) {
                mListener.onChanged(TicautoToggleSlider.this, mTracking, isChecked(), mSlider.getProgress(), true);
            }
        }
    };
}
