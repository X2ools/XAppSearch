
package org.x2ools.t9apps;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class ViewManagerService extends Service implements HandleView.CallBack {
    private static final String TAG = "ViewManagerService";
    private T9AppsView mT9AppsView;
    private View mMainView;
    private ViewGroup mMainViewContainer;
    private WindowManager mWindowManager;
    private LayoutParams mLayoutParams;
    private boolean mShowing = false;
    private FrameLayout.LayoutParams mMainViewLayoutParams;
    private RelativeLayout.LayoutParams mMainViewContainerLayoutParams;
    private static final int ANIMATE_TIME = 1000;
    public final static String ACTION_HIDE_VIEW = "ACTION_DETROY_VIEW";
    private static final int HANDLER_WIDTH = 1;
    private static final boolean NO_ANIMATION = true;
    private int SCREEN_WIDTH;
    private HandleView mHandleView;
    public static final String EXTRA_HIDE_VIEW = "HIDE_VIEW";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * @description add a view to show T9
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        registerReceiver(mReceiver, new IntentFilter(ACTION_HIDE_VIEW));
        SCREEN_WIDTH = generateDisplayWidth();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeView();
        unregisterReceiver(mReceiver);
        Log.wtf(TAG, "CAP");
    }

    private void createView() {
        if (mT9AppsView == null) {
            mT9AppsView = (T9AppsView) LayoutInflater.from(this)
                    .inflate(R.layout.t9_apps_view, null);
            mHandleView = (HandleView) mT9AppsView.findViewById(R.id.handler);
            mHandleView.setCallBack(this);
            mMainView = mT9AppsView.findViewById(R.id.mainView);
            mMainViewContainer = (ViewGroup) mT9AppsView.findViewById(R.id.mainViewContainer);
            mMainViewLayoutParams = (FrameLayout.LayoutParams) mMainView.getLayoutParams();
            mMainViewContainerLayoutParams = (RelativeLayout.LayoutParams) mMainViewContainer
                    .getLayoutParams();
        }
    }

    public void addView(boolean hide) {
        createView();
        showMainView(false);
        if (hide) {
            showSideView(false);
        }
        mT9AppsView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent event) {
                Log.d(TAG, "onKeyDown : " + KeyEvent.keyCodeToString(event.getKeyCode()));
                return false;
            }
        });
        sendNotification();
    }

    public static final int NOTIFICATION_ID = 100088;

    public void sendNotification() {
        Intent viewService = new Intent(this, ViewManagerService.class);
        viewService.putExtra(ViewManagerService.EXTRA_HIDE_VIEW, false);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent intent = PendingIntent.getService(this, 0,
                viewService, 0);
        Notification notification = new Notification.Builder(this)
                .setAutoCancel(false)
                .setOngoing(false)
                .setContentTitle(getResources().getString(R.string.notification_title))
                .setContentText(getResources().getString(R.string.notification_text))
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_launcher)
                .addAction(0, "nihao", intent)
                .build();
        nm.notify(NOTIFICATION_ID, notification);
        Log.d(TAG, "notify " + NOTIFICATION_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean hide = intent.getBooleanExtra(EXTRA_HIDE_VIEW, true);
        addView(hide);
        return super.onStartCommand(intent, flags, startId);
    }

    public void removeView() {
        Log.d(TAG, "removeView " + mT9AppsView + mT9AppsView.getWindowToken());
        if (mT9AppsView != null && mT9AppsView.getWindowToken() != null) {
            mWindowManager.removeView(mT9AppsView);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_HIDE_VIEW)) {
                showSideView(true);
            }
        }

    };

    private LayoutParams generateLayoutParams() {
        if (mLayoutParams == null) {
            mLayoutParams = new LayoutParams();
            mLayoutParams.height = LayoutParams.MATCH_PARENT;
            mLayoutParams.width = SCREEN_WIDTH - mLayoutParams.x;
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE
                    | LayoutParams.FLAG_FULLSCREEN;

            mLayoutParams.format = PixelFormat.TRANSLUCENT;
        }
        if (mShowing) {
            mLayoutParams.x = 0;
            mLayoutParams.width = SCREEN_WIDTH;
            mLayoutParams.flags &= ~LayoutParams.FLAG_NOT_FOCUSABLE;

        }
        else {
            mLayoutParams.x = SCREEN_WIDTH - HANDLER_WIDTH;
            mLayoutParams.width = SCREEN_WIDTH - mLayoutParams.x;
            mLayoutParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        return mLayoutParams;
    }

    private void showSideView(boolean animate) {
        mT9AppsView.clearFilter();
        mMainView.setVisibility(View.GONE);
        mShowing = false;
        if (mT9AppsView.getParent() == null) {
            mWindowManager.addView(mT9AppsView, generateLayoutParams());
        }
        else {
            mWindowManager.updateViewLayout(mT9AppsView, generateLayoutParams());
        }
    }

    private void showMainView(boolean animate) {
        mT9AppsView.setSystemUiVisibility(0x00010000);// STATUS_BAR_DISABLE_EXPAND
        Log.d(TAG, "expand : " + (mT9AppsView.getSystemUiVisibility() & 0x00010000));
        showMainView(animate, 0);
    }

    private void showMainView(boolean animate, int touchX) {
        mMainView.setVisibility(View.VISIBLE);
        mShowing = true;

        if (mT9AppsView.getParent() == null) {
            mWindowManager.addView(mT9AppsView, generateLayoutParams());
        }
        else {
            mWindowManager.updateViewLayout(mT9AppsView, generateLayoutParams());
        }
        updateViewLayout(animate, touchX);
        mT9AppsView.onMainViewShow();
    }

    private void updateViewLayout(boolean animate, int targetX) {
        if (NO_ANIMATION) {
            updateViewLayout(targetX);
            return;
        }
        int originalX = SCREEN_WIDTH - mMainViewContainerLayoutParams.width;
        Log.d(TAG, "animate from " + originalX + "to " + targetX);
        ValueAnimator animator = ValueAnimator.ofInt(originalX, targetX);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                updateViewLayout(value);
                Log.d(TAG, "animate to " + value);
            }

        });
        animator.setDuration((long) (ANIMATE_TIME * Math.abs((originalX - targetX) * 1.0
                / SCREEN_WIDTH)));
        Log.d(TAG, "animate time : " + animator.getDuration());
        animator.start();
    }

    private void updateViewLayout(int targetX) {
        mMainViewContainerLayoutParams.width = SCREEN_WIDTH - targetX;
        mMainViewLayoutParams.width = SCREEN_WIDTH;
        mT9AppsView.updateViewLayout(mMainViewContainer, mMainViewContainerLayoutParams);
        mMainViewContainer.updateViewLayout(mMainView, mMainViewLayoutParams);
    }

    private int generateDisplayWidth() {
        Point p = new Point();
        mWindowManager.getDefaultDisplay().getSize(p);
        int width = p.x;
        return width;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SCREEN_WIDTH = generateDisplayWidth();
        if (mShowing) {
            showMainView(false);
        }
        else {
            showSideView(false);
        }
        Log.d(TAG, "width : " + SCREEN_WIDTH);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onMoved(int touchX) {
        if (!mShowing) {
            showMainView(false, touchX);
            return;
        }
        else {
            updateViewLayout(false, touchX);
        }
    }

    @Override
    public void onTouchUp(int touchX) {
        if (touchX > SCREEN_WIDTH / 2) {
            showSideView(true);
        } else {
            showMainView(true);
        }
    }

    @Override
    public void onSwipeRight() {
        showSideView(true);
    }

    @Override
    public void onSwipeLeft() {
        showMainView(true);
    }

}
