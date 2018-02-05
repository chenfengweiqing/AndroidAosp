package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * @author lcz
 * @date 12/6/16
 */
public class TicautoStatusBarEventHelper {
    private static final String TAG = "StatusBarEventHelper";

    public enum Mode {
        EXPAND, COLLAPSE
    }

    private static final int CHANGED_VELOCITY = 1000;
    private static final float CHANGED_FACTOR = 0.5f;
    private static final long CHANGED_DURATION = 250L;

    private static final int STATE_IDLE = 1;
    private static final int STATE_EXPANDING = 2;
    private static final int STATE_COLLAPSING = 3;

    private Context mContext;
    private TicautoStatusBar mQsBar;
    private Callback mCallback;
    private Mode mMode;
    private VelocityTracker mVelocityTracker;
    private float mInitialTouchX, mInitialTouchY;
    private float mLastTouchX, mLastTouchY;
    private int mTouchSlop;
    private int mState = STATE_IDLE;
    private int mNextState = STATE_IDLE;
    private int mExpandedMaxHeight = 0;
    private boolean mIsExpandStart = true;

    public interface Callback {
        /**
         * @return quick setting view TranslationY.
         */
        int getPanelTranslationY();

        /**
         * @param translationY set quick setting view TranslationY.
         */
        void setPanelTranslationY(final int translationY);

        /**
         * @param isExpand quick setting view Translation end.
         */
        void animationEnd(final boolean isExpand);
    }

    /**
     * @param context
     * @param qsBar
     * @param callback
     * @param mode
     */
    public TicautoStatusBarEventHelper(final Context context, final TicautoStatusBar qsBar, final Callback callback, final Mode mode) {
        mContext = context;
        mQsBar = qsBar;
        mCallback = callback;
        mMode = mode;
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    /**
     * @param ev
     * @return
     */
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent: ");
//        trackVelocity(ev);
//        final int action = ev.getActionMasked();
//        mExpandedMaxHeight = mQsBar.getMaxContentHeight();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mQsBar.setStatusExpanded(true);
//                mIsExpandStart = true;
//                mLastTouchX = mInitialTouchX = ev.getRawX();
//                mLastTouchY = mInitialTouchY = ev.getRawY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                mLastTouchX = ev.getRawX();
//                mLastTouchY = ev.getRawY();
//                final float xDiff = mLastTouchX - mInitialTouchX;
//                final float yDiff = mLastTouchY - mInitialTouchY;
//                Log.d(TAG, "onInterceptTouchEvent: mTouchSlop " + mTouchSlop + " yDiff " + yDiff + " xDiff " + xDiff);
//                if (mState == STATE_IDLE && Math.abs(yDiff) > mTouchSlop && Math.abs(xDiff) > Math.abs(yDiff)) {
//                    if (mMode == Mode.EXPAND) {
//                        mState = STATE_EXPANDING;
//                    } else if (mMode == Mode.COLLAPSE) {
//                        mState = STATE_COLLAPSING;
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                finishTouch();
//                maybeRecycleVelocityTracker(ev);
//                break;
//            default:
//                break;
//        }
        return mState != STATE_IDLE;
    }

    /**
     * @param ev
     * @return
     */
    public boolean onTouchEvent(final MotionEvent ev) {
//        trackVelocity(ev);
//        mExpandedMaxHeight = mQsBar.getMaxContentHeight();
//        final int action = ev.getActionMasked();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mQsBar.setStatusExpanded(true);
//                mIsExpandStart = true;
//                mLastTouchX = mInitialTouchX = ev.getRawX();
//                mLastTouchY = mInitialTouchY = ev.getRawY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                final float touchY = ev.getRawY();
//                final float yDiff = touchY - mLastTouchY;
//                mLastTouchX = ev.getRawX();
//                final float xDiff = mLastTouchX - mInitialTouchX;
//                boolean ss = mState == STATE_IDLE && Math.abs(touchY - mInitialTouchY) > mTouchSlop &&
//                        Math.abs(yDiff) > Math.abs(xDiff);
//                Log.d(TAG, "onTouchEvent:ss " + ss + " mState " + mState + " touchY - mInitialTouchY " + Math.abs(touchY - mInitialTouchY) + " mTouchSlop " + mTouchSlop + " yDiff " + yDiff + "  xDiff " + xDiff + "  mMode " + mMode);
//                if (mState == STATE_IDLE && Math.abs(touchY - mInitialTouchY) > mTouchSlop &&
//                        Math.abs(yDiff) > Math.abs(xDiff)) {
//                    if (mMode == Mode.EXPAND) {
//                        mState = STATE_EXPANDING;
//                    } else if (mMode == Mode.COLLAPSE) {
//                        mState = STATE_COLLAPSING;
//                    }
//                } else if (mState == STATE_EXPANDING) {
//                    int translationY = mCallback.getPanelTranslationY() + (int) yDiff;
//                    Log.d(TAG, "onTouchEvent:  mLastTouchY " + mLastTouchY + "  getPanelTranslationY " +
//                            mCallback.getPanelTranslationY() + " translationY " + translationY + " yDiff " + yDiff);
//                    mCallback.setPanelTranslationY(translationY);
//                    mLastTouchY = touchY;
//                    mIsExpandStart = false;
//                } else if (mState == STATE_COLLAPSING) {
//                    int translationY = mCallback.getPanelTranslationY() + (int) yDiff;
//                    Log.d(TAG, "onTouchEvent: STATE_COLLAPSING translationY " + translationY);
//                    mCallback.setPanelTranslationY(translationY);
//                    mLastTouchY = touchY;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                finishTouch();
//                maybeRecycleVelocityTracker(ev);
//                break;
//            default:
//                break;
//        }
        return true;
    }

    /**
     * open quick setting view.
     */
    public void expand() {
        Log.d(TAG, "expand: mNextState " + mNextState);
        if (mNextState != STATE_EXPANDING) {
            mNextState = STATE_EXPANDING;
            mExpandedMaxHeight = mQsBar.getMaxContentHeight();
            playAnimation(true, false);
        }
    }

    /**
     * close quick setting view.
     */
    public void collapse() {
        Log.d(TAG, "collapse: mNextState " + mNextState);
        if (mNextState != STATE_COLLAPSING) {
            mNextState = STATE_COLLAPSING;
            mExpandedMaxHeight = mQsBar.getMaxContentHeight();
            playAnimation(false, false);
        }
    }

    private void finishTouch() {
        Log.d(TAG, "finishTouch: mState " + mState + " mMode " + mMode);
        if (mState == STATE_EXPANDING || mState == STATE_COLLAPSING) {
            mState = STATE_IDLE;
            int translationY = mCallback.getPanelTranslationY();
            float velocityY = getCurrentVelocity();
            if (mMode == Mode.EXPAND) {
                boolean ss = (Math.abs(translationY) <= CHANGED_FACTOR * mExpandedMaxHeight) ||
                        (Math.abs(velocityY) > CHANGED_VELOCITY) && velocityY < 0;
                if (ss) {
                    playAnimation(true, true);
                } else {
                    playAnimation(false, true);
                }
            } else {
                if ((translationY <= 0.3f * mExpandedMaxHeight) ||
                        (Math.abs(velocityY) > CHANGED_VELOCITY && velocityY < 0)) {
                    playAnimation(false, true);
                } else {
                    playAnimation(true, true);
                }
            }
        } else {
            animationEnd(mCallback.getPanelTranslationY() == 0);
        }
    }

    private void playAnimation(final boolean expand, final boolean isSlide) {
        int start = mCallback.getPanelTranslationY();
        int end = expand ? 0 : -mExpandedMaxHeight;
        if (start == end) {
            animationEnd(expand);
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
        valueAnimator.setDuration(CHANGED_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int transY = (int) valueAnimator.getAnimatedValue();
                Log.d(TAG, "onAnimationUpdate: transY " + transY);
                mCallback.setPanelTranslationY(transY);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(final Animator animation) {

            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                mCallback.animationEnd(expand);
                animationEnd(expand);
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                animationEnd(expand);
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {

            }
        });
        valueAnimator.start();
    }

    private void animationEnd(final boolean expand) {
        Log.d(TAG, "animationEnd: expand " + expand);
        if (!expand) {
            mQsBar.setStatusExpanded(false);
        }
        mCallback.setPanelTranslationY(expand ? 0 : -mExpandedMaxHeight);
        mState = STATE_IDLE;
        mNextState = STATE_IDLE;
    }

    private void trackVelocity(final MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                }
                mVelocityTracker.addMovement(event);
                break;
            default:
                break;
        }
    }

    private void maybeRecycleVelocityTracker(final MotionEvent event) {
        if (mVelocityTracker != null && (event.getActionMasked() == MotionEvent.ACTION_CANCEL
                || event.getActionMasked() == MotionEvent.ACTION_UP)) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private float getCurrentVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(1000);
            return mVelocityTracker.getYVelocity();
        } else {
            return 0f;
        }
    }
}

