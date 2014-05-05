package org.x2ools.t9apps;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
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
	private static final int HANDLER_WIDTH = 60;
	private static final boolean NO_ANIMATION = true;
	private int DISPLAY_WIDTH;
	private HandleView mHandleView;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * @description add a view to show T9
	 * */
	@Override
	public void onCreate() {
		super.onCreate();
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		registerReceiver(mReceiver, new IntentFilter(ACTION_HIDE_VIEW));
		DISPLAY_WIDTH = generateDisplayWidth();
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
			mHandleView = (HandleView)mT9AppsView.findViewById(R.id.handler);
			mHandleView.setCallBack(this);
			mMainView = mT9AppsView.findViewById(R.id.mainView);
			mMainViewContainer = (ViewGroup) mT9AppsView.findViewById(R.id.mainViewContainer);
			mMainViewLayoutParams = (FrameLayout.LayoutParams) mMainView.getLayoutParams();
			mMainViewContainerLayoutParams = (RelativeLayout.LayoutParams) mMainViewContainer.getLayoutParams();
		}		
	}
	
	public void addView() {
     	createView();
     	showMainView(false);
		showSideView(false);
		mT9AppsView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent event) {
				Log.d(TAG, "onKeyDown : " + KeyEvent.keyCodeToString(event.getKeyCode()));
				return false;
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		addView();
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
			mLayoutParams.width = DISPLAY_WIDTH - mLayoutParams.x;
			mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
					| LayoutParams.FLAG_NOT_FOCUSABLE;

			mLayoutParams.format = PixelFormat.TRANSLUCENT;
		}
		if(mShowing) {
			mLayoutParams.x = 0;
			mLayoutParams.width = DISPLAY_WIDTH;
			mLayoutParams.flags &= ~LayoutParams.FLAG_NOT_FOCUSABLE;

		}
		else {
			mLayoutParams.x = DISPLAY_WIDTH - HANDLER_WIDTH;
			mLayoutParams.width = DISPLAY_WIDTH - mLayoutParams.x;
			mLayoutParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
		}
		return mLayoutParams;
	}
	
	private void showSideView(boolean animate) {
		mT9AppsView.clearFilter();
		mMainView.setVisibility(View.GONE);
		mShowing = false;
		if(mT9AppsView.getParent() == null) {
			mWindowManager.addView(mT9AppsView, generateLayoutParams());
		}
		else {
			mWindowManager.updateViewLayout(mT9AppsView, generateLayoutParams());
		}
	}
	
	

//	private void showMainView() {
//		showMainView(false, 0);
//	}
	
	private void showMainView(boolean animate) {
		showMainView(animate, 0);
	}
	
	private void showMainView(boolean animate, int touchX) {
		mMainView.setVisibility(View.VISIBLE);
		mShowing = true;
		
		if(mT9AppsView.getParent() == null) {
			mWindowManager.addView(mT9AppsView, generateLayoutParams());
		}
		else {
			mWindowManager.updateViewLayout(mT9AppsView, generateLayoutParams());
		}
		updateViewLayout(animate, touchX);
		mT9AppsView.onMainViewShow();
	}

	private void updateViewLayout(boolean animate, int targetX) {
		if(NO_ANIMATION) {
			updateViewLayout(targetX);
			return;
		}
		int originalX = DISPLAY_WIDTH - mMainViewContainerLayoutParams.width;
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
		animator.setDuration((long) (ANIMATE_TIME * Math.abs((originalX - targetX)*1.0 / DISPLAY_WIDTH)));
		Log.d(TAG, "animate time : " + animator.getDuration());
		animator.start();
	}
	private void updateViewLayout(int targetX) {
		mMainViewContainerLayoutParams.width = DISPLAY_WIDTH - targetX;
		mMainViewLayoutParams.width = DISPLAY_WIDTH;
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
		DISPLAY_WIDTH = generateDisplayWidth();
		if(mShowing) {
			showMainView(false);
		}
		else {
			showSideView(false);
		}
		Log.d(TAG, "width : " + DISPLAY_WIDTH);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onMoved(int touchX) {
		if(!mShowing){
			showMainView(false, touchX);
			return;
		}
		else {
			updateViewLayout(false, touchX);
		}
	}

	@Override
	public void onTouchUp(int touchX) {
		if(touchX > DISPLAY_WIDTH/2) {
			showSideView(true);
		}
		else {
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
