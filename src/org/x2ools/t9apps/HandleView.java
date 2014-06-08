package org.x2ools.t9apps;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class HandleView extends View {
	public HandleView(Context context, AttributeSet attrs) {
		super(context, attrs);
	    mGestureDetector = new GestureDetector(context, new GestureListener());
	}

	private static final boolean DEBUG = true;
	private static final String TAG = "HandlerView";
	
	private float mStartX;
	private float mStartY;
	private float mEndX;
	private float mTouchX;
	private VelocityTracker mVelocityTracker;
	
    private void acquireVelocityTracker(final MotionEvent event) {  
        if(null == mVelocityTracker) {  
            mVelocityTracker = VelocityTracker.obtain();  
        }  
        mVelocityTracker.addMovement(event);  
    }  
    
    private void releaseVelocityTracker() {  
        if(null != mVelocityTracker) {  
            mVelocityTracker.clear();  
            mVelocityTracker.recycle();  
            mVelocityTracker = null;  
        }  
    } 
	
	public interface CallBack {
		public void onMoved(int i);
		public void onTouchUp(int mTouchX);
		public void onSwipeRight();
		public void onSwipeLeft();
	}
	
	private CallBack mCallBack;

	
	public void setCallBack(CallBack callBack) {
		mCallBack = callBack;
	}


	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {

		mTouchX = motionEvent.getRawX();
		acquireVelocityTracker(motionEvent);
		final int action = motionEvent.getAction();
		float endX;
		if(mGestureDetector.onTouchEvent(motionEvent)) {
			return true;
		}
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			acquireVelocityTracker(motionEvent);
			mStartX = motionEvent.getX();
			mStartY = motionEvent.getY();
			if (DEBUG) {
				Log.d(TAG, "moved : mStartX : " + mStartX + " mStartY : "
						+ mStartY);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			endX = motionEvent.getX();
			if (DEBUG) {
				Log.d(TAG, "moved : mEndX : " + mEndX + " endX : " + endX + " mTouchX : " + mTouchX);
			}
			//if (Math.abs(endX - mEndX) > 5) {
				mCallBack.onMoved((int)mTouchX);
			//}
			mEndX = endX;
			break;
		case MotionEvent.ACTION_UP:
			endX  = motionEvent.getX();
			if (DEBUG) {
				Log.d(TAG, "moved : mEndX : " + mEndX + " endX : " + endX + " mTouchX : " + mTouchX);
			}
			mCallBack.onTouchUp((int)mTouchX);
			mEndX = endX;
			break;
		case MotionEvent.ACTION_CANCEL:
			releaseVelocityTracker();
		}
		super.onTouchEvent(motionEvent);
		return true;
	}


    private final GestureDetector mGestureDetector;

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            result = onSwipeRight();
                        } else {
                            result = onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            result = onSwipeBottom();
                        } else {
                            result = onSwipeTop();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public boolean onSwipeRight() {
    	mCallBack.onSwipeRight();
        return true;
    }

    public boolean onSwipeLeft() {
    	mCallBack.onSwipeLeft();
        return true;
    }

    public boolean onSwipeTop() {
        return false;
    }

    public boolean onSwipeBottom() {
        return false;
    }
	
}
