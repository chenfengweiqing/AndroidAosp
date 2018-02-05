package com.android.systemui.statusbar.tiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;

/**
 * @author lcz
 * @date 17-12-4
 */
public abstract class QSTile {
    protected final State mState = new State();
    private final State mTmpState = new State();
    private TextView mTileView;
    protected Host mHost;
    protected Context mContext;

    /**
     * @param host
     * @param view
     */
    public QSTile(final Host host, final TextView view) {
        mHost = host;
        mContext = host.getContext();
        mTileView = view;
        mTileView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                handleLongClick();
                return true;
            }
        });
        mTileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTileView.setEnabled(false);
                handleClick();
            }
        });
    }

    /**
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * handle click.
     */
    abstract protected void handleClick();

    /**
     * handle long click.
     */
    abstract protected void handleLongClick();

    /**
     * @param state
     */
    abstract protected void handleUpdateState(final State state);

    protected final void refreshState() {
        handleUpdateState(mTmpState);
        final boolean changed = mTmpState.copyTo(mState);
        if (changed) {
            handleStateChanged();
        }
        mTileView.setEnabled(true);
    }

    private void handleStateChanged() {
        if (mState.enabled) {
            Drawable drawable = mState.onIcon.getDrawable(getContext());
            mTileView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        } else {
            Drawable drawable = mState.offIcon.getDrawable(getContext());
            mTileView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        }
    }

    public interface Host {
        /**
         * collapse quick setting.
         */
        void collapsePanels();

        /**
         * @return Context.
         */
        Context getContext();
    }

    public static class State {
        public boolean enabled;
        public Icon offIcon;
        public Icon onIcon;
        public String label;

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public boolean copyTo(final State other) {
            if (other == null || !other.getClass().equals(getClass())) {
                throw new IllegalArgumentException();
            }
            final boolean changed = other.enabled != enabled
                    || !Objects.equals(other.offIcon, offIcon)
                    || !Objects.equals(other.onIcon, onIcon)
                    || !Objects.equals(other.label, label);
            other.enabled = enabled;
            other.offIcon = offIcon;
            other.onIcon = onIcon;
            other.label = label;
            return changed;
        }

        @Override
        public String toString() {
            return toStringBuilder().toString();
        }

        protected StringBuilder toStringBuilder() {
            final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
            sb.append("enabled=").append(enabled);
            sb.append(",offIcon=").append(offIcon);
            sb.append(",onIcon=").append(onIcon);
            sb.append(",label=").append(label);
            return sb.append(']');
        }
    }


    public static abstract class Icon {
        abstract public Drawable getDrawable(Context context);

        @Override
        public int hashCode() {
            return Icon.class.hashCode();
        }
    }

    public static class ResourceIcon extends Icon {
        private static final SparseArray<Icon> ICONS = new SparseArray<Icon>();

        private final int mResId;

        private ResourceIcon(final int resId) {
            mResId = resId;
        }

        public static Icon get(final int resId) {
            Icon icon = ICONS.get(resId);
            if (icon == null) {
                icon = new ResourceIcon(resId);
                ICONS.put(resId, icon);
            }
            return icon;
        }

        @Override
        public Drawable getDrawable(final Context context) {
            return context.getDrawable(mResId);
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof ResourceIcon && ((ResourceIcon) o).mResId == mResId;
        }

        @Override
        public String toString() {
            return String.format("ResourceIcon[resId=0x%08x]", mResId);
        }
    }
}
