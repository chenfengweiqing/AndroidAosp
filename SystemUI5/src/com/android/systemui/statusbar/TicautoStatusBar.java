package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.SystemUI;
import com.android.systemui.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.android.systemui.statusbar.controller.NavigationUtils;
import com.ticauto.common.music.IMusicUI;
import com.ticauto.common.music.MusicContracts;
import com.ticauto.common.music.MusicControlHelper;
import com.ticauto.common.navigation.NaviController;
import com.ticauto.common.navigation.internal.INaviGuideInfoListener;
import com.ticauto.common.navigation.internal.NaviGuideInfo;

import android.graphics.Bitmap;
import android.media.session.PlaybackState;

/**
 * @author lcz
 * @date 17-12-4
 */

public class TicautoStatusBar extends SystemUI implements View.OnClickListener, IMusicUI {
    private static final String TAG = "TicautoStatusBar";
    private static final int DEFAULT_STAY_DURATION = 15 * 1000;
    private static final int UPDATE_VIEW = 1;
    private static final int ADD_LISTENER = 2;
    private static final int UPDATE_NAVIGATION = 3;
    private static final int THOUSAND = 1000;
    private static final int TEN = 10;
    private static final float THOUSAND_FLOAT = 1000f;
    private static final float TEN_FLOAT = 10f;
    private static final float BUTTON_SELECTED = 1.0f;
    private static final float BUTTON_UNSELECTED = 0.0f;
    private static final String DEFAULT_DISTANCE = "0";
    private static final String VEHICLE_APP_ACTION = "com.ticauto.VEHICLE";
    private static final String VEHICLE_APP_PACKAGE = "com.ticauto.vehicle";
    private static final String VOICE_SEARCH_APP_ACTION = "com.ticauto.action.SPEECH";
    private static final String VOICE_SEARCH_APP_PACKAGE = "com.ticauto.voicesearch";
    private static final String APP_STORE_APP_ACTION = "com.ticauto.home.action.more";
    private static final String APP_LIST_CLASS_NAME = "com.ticauto.home.HomeMoreActivity";
    private int mDefaultStatusBarHeight;
    private int mExpandStatusBarHeight;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLp;
    private WindowManager.LayoutParams mLpChanged;
    private IActivityManager mActivityManagerNative;
    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;
    private MusicControlHelper mMusicControlHelper;
    private Bundle mMetadata;
    private PlaybackState mPlaybackState;
    private TicautoStatusBarView mView;
    private TicautoQsBarView mQsbarView;
    private ImageView mNavigationIcon, mMusicIcon, mBottomLine, mHomeSelectBg, mAppListSelectBg,
            mVehicleSelectBg, mHomeRadio, mAppListRadio, mVehicleRadio;
    private TextView mTime, mDistance, mDistanceUnit, mMusicName;
    private TicautoQSPanel mQSPanel;
    private NaviGuideInfo mNaviGuideInfo = null;
    private boolean mIsExpanded = false;
    private HashSet<String> mLauncherPackageNames = new HashSet<>();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {

            switch (msg.what) {
                case UPDATE_VIEW:
                    updateStatusBar();
                    break;
                case ADD_LISTENER:
                    initListener();
                    break;
                case UPDATE_NAVIGATION:
                    updateNavigationInfo();
                    break;
            }
        }
    };
    private Runnable mTimeRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(mTimeRunnable);
            animateCollapse();
        }
    };

    @Override
    public void start() {
        mPackageManager = mContext.getPackageManager();
        mActivityManagerNative = ActivityManagerNative.getDefault();
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManagerNative != null) {
            try {
                mActivityManagerNative.registerProcessObserver(mProcessObserver);
            } catch (RemoteException e) {
            }
        }
        mMusicControlHelper = new MusicControlHelper(mContext, this);
        NaviController.getInstance().init(mContext.getApplicationContext());
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        initLauncherPackages();
        addStatusBarWindow();
        updateTimeDisplay();
        registerListener();
    }

    private void initLauncherPackages() {
        List<ResolveInfo> resolveInfo = new ArrayList<>();
        mLauncherPackageNames.clear();
        mPackageManager.getHomeActivities(resolveInfo);
        for (ResolveInfo ri : resolveInfo) {
            mLauncherPackageNames.add(ri.activityInfo.packageName);
            Log.i(TAG, "initLauncherPackages activity name " + ri.activityInfo.name + " packageName " + ri.activityInfo.packageName);
        }
    }

    private void registerListener() {
        Log.d(TAG, "registerListener   ");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        mContext.registerReceiver(mStatusReceiver, filter);
        initListener();
    }

    private void initListener() {
        Log.d(TAG, "initListener :   ");
        mMusicControlHelper.initHelper();
        NaviController.getInstance().init(mContext.getApplicationContext()).addGuideInfoListener(mINaviGuideInfoListener);
    }

    private void addStatusBarWindow() {
        mDefaultStatusBarHeight = (int) mContext.getResources().getDimension(R.dimen.ticauto_status_bar_height);
        mExpandStatusBarHeight = mWindowManager.getDefaultDisplay().getHeight();
        mLp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, mDefaultStatusBarHeight,
                WindowManager.LayoutParams.TYPE_STATUS_BAR,
                0
                        | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                        | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        mLp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        mLp.gravity = Gravity.TOP;
        mLp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        mLp.setTitle("TicautoStatusBar");
        mLp.packageName = mContext.getPackageName();
        mLp.windowAnimations = 0;
        mView = (TicautoStatusBarView) View.inflate(mContext, R.layout.navigation_status_bar_layout, null);
        mBottomLine = (ImageView) mView.findViewById(R.id.bottom_line);
        mHomeRadio = (ImageView) mView.findViewById(R.id.home_radio);
        mAppListRadio = (ImageView) mView.findViewById(R.id.app_list_radio);
        mVehicleRadio = (ImageView) mView.findViewById(R.id.vehicle_radio);
        mQsbarView = (TicautoQsBarView) mView.findViewById(R.id.qs_bar);
        mNavigationIcon = (ImageView) mView.findViewById(R.id.navigation_icon);
        mMusicIcon = (ImageView) mView.findViewById(R.id.music_icon);
        mHomeSelectBg = (ImageView) mView.findViewById(R.id.home_select_bg);
        mAppListSelectBg = (ImageView) mView.findViewById(R.id.app_list_select_bg);
        mVehicleSelectBg = (ImageView) mView.findViewById(R.id.vehicle_select_bg);
        mTime = (TextView) mView.findViewById(R.id.current_time);
        mDistance = (TextView) mView.findViewById(R.id.navigation_distance);
        mDistanceUnit = (TextView) mView.findViewById(R.id.navigation_distance_unit);
        mMusicName = (TextView) mView.findViewById(R.id.music_name);
        mQSPanel = (TicautoQSPanel) mQsbarView.findViewById(R.id.qs_panel);
        mHomeRadio.setOnClickListener(this);
        mAppListRadio.setOnClickListener(this);
        mVehicleRadio.setOnClickListener(this);
        mHomeRadio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                goVoice();
                return true;
            }
        });
        final TicautoStatusBarEventHelper eventHelper = new TicautoStatusBarEventHelper(mContext, this, mQSPanel, TicautoStatusBarEventHelper.Mode.EXPAND);
        mView.setEventHelper(eventHelper);
        mBottomLine.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "mBottomLine touch ");
                return eventHelper.onTouchEvent(motionEvent);
            }
        });
        mQSPanel.setPanelTranslationY(mExpandStatusBarHeight - mDefaultStatusBarHeight);
        mQSPanel.setEventHelper(eventHelper);
        mWindowManager.addView(mView, mLp);
        mLpChanged = new WindowManager.LayoutParams();
        mLpChanged.copyFrom(mLp);
        mHomeSelectBg.setAlpha(1.0f);
        mAppListSelectBg.setAlpha(0.0f);
        mVehicleSelectBg.setAlpha(0.0f);
    }

    @Override
    protected void onBootCompleted() {
        super.onBootCompleted();
        Log.d(TAG, "onBootCompleted: ");
        mQsbarView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMetadataChange(final Bundle metadataBundle) {
        mMetadata = metadataBundle;
        updateMediaDataDisplay();
    }

    @Override
    public void onArtworkChange(final Bitmap artworkBitmap) {
    }

    @Override
    public void onClear() {
        mMusicName.setVisibility(View.INVISIBLE);
        mMusicIcon.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onExtrasChange(final Bundle extrasBundle) {
    }

    @Override
    public void onPlaybackStateChange(final PlaybackState playbackState) {
        mPlaybackState = playbackState;
        updateMediaDataDisplay();
    }

    private void updateMediaDataDisplay() {
        int state = PlaybackState.STATE_NONE;
        if (mPlaybackState != null) {
            state = mPlaybackState.getState();
        }
        Log.d(TAG, "updateMediaDataDisplay , play state : " + state + " ,metadata : " + mMetadata);
        boolean isPlaying = (state == PlaybackState.STATE_PLAYING);
        boolean isBuffer = (state == PlaybackState.STATE_BUFFERING || state == PlaybackState.STATE_CONNECTING);
        boolean isPause = state == PlaybackState.STATE_PAUSED;
        boolean isShow = (isPlaying || isBuffer || isPause);
        if (isShow) {
            String title = mContext.getResources().getString(R.string.default_music_title);
            if (mMetadata != null && mMetadata.containsKey(MusicContracts.Metadata.TITLE)) {
                if (!TextUtils.isEmpty(mMetadata.getString(MusicContracts.Metadata.TITLE))) {
                    title = mMetadata.getString(MusicContracts.Metadata.TITLE);
                }
            } else {
                title = mContext.getResources().getString(R.string.default_music_title);
            }
            mMusicName.setVisibility(View.VISIBLE);
            mMusicIcon.setVisibility(View.VISIBLE);
            updateAnimation(isPlaying);
            mMusicName.setText(title);
        } else {
            mMusicName.setVisibility(View.INVISIBLE);
            mMusicIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void updateAnimation(final boolean isShow) {
        Drawable drawable = mMusicIcon.getDrawable();
        if (drawable instanceof AnimationDrawable) {
            AnimationDrawable anim = (AnimationDrawable) drawable;
            if (isShow) {
                if (!anim.isRunning()) {
                    anim.start();
                }
            } else {
                if (anim.isRunning()) {
                    anim.stop();
                }
            }
        }
    }

    private void updateNavigationInfo() {
        if (mNaviGuideInfo != null) {
            int mRemainDis = mNaviGuideInfo.mSegRemainDis;
            int iconType = mNaviGuideInfo.mIcon;
            int mCrossImage = NavigationUtils.getIconResource(iconType);
            Log.d(TAG, "remainDis: " + mRemainDis + " iconType  " + iconType);
            mNavigationIcon.setVisibility(View.VISIBLE);
            mDistance.setVisibility(View.VISIBLE);
            mDistanceUnit.setVisibility(View.VISIBLE);
            mNavigationIcon.setImageResource(mCrossImage);
            mDistance.setText(getFormatDis(mRemainDis));
            if (mRemainDis >= THOUSAND) {
                mDistanceUnit.setText(mContext.getString(R.string.kilometre));
            } else {
                mDistanceUnit.setText(mContext.getString(R.string.capital_m));
            }
        } else {
            Log.e(TAG, "onGuideInfoChanged: guideInfo is null");
            mNavigationIcon.setVisibility(View.INVISIBLE);
            mDistance.setVisibility(View.INVISIBLE);
            mDistanceUnit.setVisibility(View.INVISIBLE);
        }
    }

    public void destroy() {
        mContext.unregisterReceiver(mStatusReceiver);
        mMusicControlHelper.unInitHelper();
    }

    /**
     * @return quick setting height.
     */
    public int getMaxContentHeight() {
        return mExpandStatusBarHeight - mDefaultStatusBarHeight;
    }

    public void setStatusExpanded(final boolean expanded) {
        Log.d(TAG, "setStatusExpanded: expanded " + expanded);
        if (mIsExpanded != expanded) {
            mIsExpanded = expanded;
            updateHeight(expanded);
            updateFocusableFlag(expanded);
            apply();
            handleExpandedTime(expanded);
        }
    }

    /**
     * open quick setting.
     */
    public void animateExpand() {
        setStatusExpanded(true);
        mQSPanel.expandPanels();
    }

    /**
     * close quick setting.
     */
    public void animateCollapse() {
        if (mIsExpanded) {
            mQSPanel.collapsePanels();
        }
    }

    private void updateHeight(final boolean expanded) {
        if (expanded) {
            mLpChanged.height = mExpandStatusBarHeight;
        } else {
            mLpChanged.height = mDefaultStatusBarHeight;
        }
    }

    private void updateFocusableFlag(final boolean expanded) {
        if (expanded) {
            mLpChanged.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mLpChanged.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        } else {
            mLpChanged.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mLpChanged.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        }
    }

    private void apply() {
        if (mLp.copyFrom(mLpChanged) != 0) {
            mWindowManager.updateViewLayout(mView, mLp);
        }
    }

    private void handleExpandedTime(final boolean expanded) {
        if (expanded) {
            if (mHandler != null) {
                mHandler.postDelayed(mTimeRunnable, DEFAULT_STAY_DURATION);
            }
        } else {
            if (mHandler != null) {
                mHandler.removeCallbacks(mTimeRunnable);
            }
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.home_radio:
                goHome();
                break;
            case R.id.app_list_radio:
                goAppList();
                break;
            case R.id.vehicle_radio:
                goVehicle();
                break;
            default:
                break;
        }
    }

    private void goHome() {
        Log.d(TAG, "goHome ");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        if (intent.resolveActivity(mPackageManager) != null) {
            closeVoice();
            homeSelected();
            mContext.startActivity(intent);
        } else {
            Log.d(TAG, "goHome fail : intent is null ");
        }
    }

    private void goVoice() {
        Log.d(TAG, "goVoice ");
        homeSelected();
        Intent intent = new Intent(VOICE_SEARCH_APP_ACTION);
        intent.putExtra("trigger_mode", "normal");
        mContext.sendBroadcast(intent);
    }

    private void closeVoice() {
        Intent intent = new Intent(VOICE_SEARCH_APP_ACTION);
        intent.putExtra("trigger_mode", "close");
        mContext.sendBroadcast(intent);
    }

    private void goAppList() {
        Log.d(TAG, "goAppList ");
        Intent intent = new Intent(APP_STORE_APP_ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        if (intent.resolveActivity(mPackageManager) != null) {
            closeVoice();
            appListSelected();
            Log.d(TAG, "goAppList intent " + intent);
            mContext.startActivity(intent);
        } else {
            Log.d(TAG, "goAppList fail : intent is null ");
        }
    }

    private void goVehicle() {
        Log.d(TAG, "goVehicle ");
        Intent intent = new Intent(VEHICLE_APP_ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        if (intent.resolveActivity(mPackageManager) != null) {
            closeVoice();
            vehicleSelected();
            mContext.startActivity(intent);
        } else {
            Log.d(TAG, "goVehicle fail : intent is null ");
        }
    }

    private void updateStatusBar() {
        initLauncherPackages();
        ComponentName topActivity = mActivityManager.getRunningTasks(1).get(0).topActivity;
        String packageName = topActivity.getPackageName();
        String className = topActivity.getClassName();
        Log.d(TAG, "updateStatusBar: packageName " + packageName + " className  " + className);
        if (VOICE_SEARCH_APP_PACKAGE.equals(packageName) || (mLauncherPackageNames != null && mLauncherPackageNames.contains(packageName))) {
            if (APP_LIST_CLASS_NAME.equals(className)) {
                appListSelected();
            } else {
                homeSelected();
            }
        } else if (VEHICLE_APP_PACKAGE.equals(packageName)) {
            vehicleSelected();
        } else {
            appListSelected();
        }
    }

    private void homeSelected() {
        if (mHomeSelectBg.getAlpha() == BUTTON_UNSELECTED) {
            showButtonAnimator(BUTTON_SELECTED, BUTTON_UNSELECTED, BUTTON_UNSELECTED);
        } else {
            Log.e(TAG, "homeSelected view appha already  is 1.0f ");
        }
    }

    private void vehicleSelected() {
        if (mVehicleSelectBg.getAlpha() == BUTTON_UNSELECTED) {
            showButtonAnimator(BUTTON_UNSELECTED, BUTTON_UNSELECTED, BUTTON_SELECTED);
        } else {
            Log.e(TAG, "homeSelected view appha already  is 1.0f ");
        }
    }

    private void appListSelected() {
        if (mAppListSelectBg.getAlpha() == BUTTON_UNSELECTED) {
            showButtonAnimator(BUTTON_UNSELECTED, BUTTON_SELECTED, BUTTON_UNSELECTED);
        } else {
            Log.e(TAG, "homeSelected view appha already  is 1.0f ");
        }
    }

    private void showButtonAnimator(final float homeEndAlpha, final float appListEndAlpha, final float vehicleEndAlpha) {
        AnimatorSet animatorSet = new AnimatorSet();
        float homeAlpha = mHomeSelectBg.getAlpha();
        ObjectAnimator homeAnimator = ObjectAnimator.ofFloat(mHomeSelectBg,
                "alpha", homeAlpha, homeAlpha, homeEndAlpha);
        float appListAlpha = mAppListSelectBg.getAlpha();
        ObjectAnimator appListAnimator = ObjectAnimator.ofFloat(mAppListSelectBg,
                "alpha", appListAlpha, appListAlpha, appListEndAlpha);
        float vehicleAlpha = mVehicleSelectBg.getAlpha();
        ObjectAnimator vehicleAnimator = ObjectAnimator.ofFloat(mVehicleSelectBg,
                "alpha", vehicleAlpha, vehicleAlpha, vehicleEndAlpha);
        animatorSet.play(homeAnimator).with(appListAnimator).with(vehicleAnimator);
        animatorSet.setDuration(300);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setButtonImage(homeEndAlpha, appListEndAlpha, vehicleEndAlpha);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void setButtonImage(final float homeEndAlpha, final float appListEndAlpha, final float vehicleEndAlpha) {
        if (homeEndAlpha == BUTTON_SELECTED) {
            mHomeRadio.setImageDrawable(mContext.getResources().getDrawable(R.drawable.home_active));
        } else {
            mHomeRadio.setImageDrawable(mContext.getResources().getDrawable(R.drawable.home_inactive));
        }
        if (appListEndAlpha == BUTTON_SELECTED) {
            mAppListRadio.setImageDrawable(mContext.getResources().getDrawable(R.drawable.applist_active));
        } else {
            mAppListRadio.setImageDrawable(mContext.getResources().getDrawable(R.drawable.applist_inactive));
        }
        if (vehicleEndAlpha == BUTTON_SELECTED) {
            mVehicleRadio.setImageDrawable(mContext.getResources().getDrawable(R.drawable.carinfo_active));
        } else {
            mVehicleRadio.setImageDrawable(mContext.getResources().getDrawable(R.drawable.carinfo_inactive));
        }
    }

    private void updateTimeDisplay() {
        Calendar now = Calendar.getInstance();
        mTime.setText(DateFormat.getTimeFormat(mContext).format(now.getTime()));
    }

    private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_TIMEZONE_CHANGED.equals(action) || Intent.ACTION_TIME_TICK.equals(action) ||
                    Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action)) {
                updateTimeDisplay();
            } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                mHandler.obtainMessage(ADD_LISTENER).sendToTarget();
            }
        }
    };

    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {

    }

    private IProcessObserver.Stub mProcessObserver = new IProcessObserver.Stub() {
        @Override
        public void onForegroundActivitiesChanged(final int pid, final int uid, final boolean foregroundActivities) {
            mHandler.obtainMessage(UPDATE_VIEW).sendToTarget();
        }

        @Override
        public void onProcessStateChanged(final int i, final int i1, final int i2) throws RemoteException {
        }

        @Override
        public void onProcessDied(final int pid, final int uid) {
        }
    };

    private static String getFormatDis(final int distance) {
        String formatDis = String.valueOf(distance);
        if (distance >= THOUSAND) {
            float remainDistanceFloat = Math.round(distance / THOUSAND_FLOAT * TEN) / TEN_FLOAT;
            String[] temRemainDistance = String.valueOf(remainDistanceFloat).split("\\.");
            if (temRemainDistance.length > 1 && temRemainDistance[1] != null && !DEFAULT_DISTANCE.equals(temRemainDistance[1])) {
                formatDis = String.format(Locale.getDefault(), "%.1f", remainDistanceFloat);
            } else if (temRemainDistance[0] != null) {
                formatDis = temRemainDistance[0];
            }
        }
        return formatDis;
    }

    private INaviGuideInfoListener.Stub mINaviGuideInfoListener = new INaviGuideInfoListener.Stub() {
        @Override
        public void onGuideInfoChanged(NaviGuideInfo guideInfo) throws RemoteException {
            mNaviGuideInfo = guideInfo;
            mHandler.obtainMessage(UPDATE_NAVIGATION).sendToTarget();
        }
    };
}
