package com.tencent.cloud.tuikit.flutter.tuicallkit.view.floatwindow;

import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.tencent.cloud.tuikit.engine.call.TUICallDefine;
import com.tencent.cloud.tuikit.flutter.tuicallkit.state.TUICallState;

public class FloatWindowService extends Service {
    private CallFloatView mCallView;
    private Context       mContext;

    private WindowManager              mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;

    private int     mScreenWidth;
    private int     mWidth;
    private int     mTouchStartX;
    private int     mTouchStartY;
    private int     mTouchCurrentX;
    private int     mTouchCurrentY;
    private int     mStartX;
    private int     mStartY;
    private int     mStopX;
    private int     mStopY;
    private boolean mIsMove;
    private int     mLeftRightEdge = 20;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new FloatBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCallView) {
            mWindowManager.removeView(mCallView);
            mCallView = null;
        }
    }

    private void init() {
        mContext = getApplicationContext();
        mCallView = TUICallState.getInstance().mScene == TUICallDefine.Scene.SINGLE_CALL ?
                new SingleCallFloatView(mContext) : new GroupCallFloatView(mContext);
        mCallView.setOnClickListener((view) -> {
            mCallView.destory();
        });
        mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowLayoutParams = getViewParams();
        mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();

        if (null != mCallView) {
            mWindowManager.addView(mCallView, mWindowLayoutParams);
            mCallView.setOnTouchListener(new FloatingListener());
        }
    }

    private WindowManager.LayoutParams getViewParams() {
        mWindowLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        mWindowLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowLayoutParams.x = mLeftRightEdge;
        mWindowLayoutParams.y = mWindowManager.getDefaultDisplay().getHeight() / 2;

        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.format = PixelFormat.TRANSPARENT;

        return mWindowLayoutParams;
    }

    private void startScroll(int start, int end, boolean isLeft) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, end).setDuration(300);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mWindowLayoutParams == null || mCallView == null) {
                    return;
                }
                mWidth = mCallView.getWidth();
                if (isLeft) {
                    mWindowLayoutParams.x = (int) (start * (1 - animation.getAnimatedFraction()) + mLeftRightEdge);
                } else {
                    float end = (mScreenWidth - start - mWidth) * animation.getAnimatedFraction();
                    mWindowLayoutParams.x = (int) (start + end - mLeftRightEdge);
                }
                calculateHeight();
                mWindowManager.updateViewLayout(mCallView, mWindowLayoutParams);
            }
        });
        valueAnimator.start();
    }

    private void calculateHeight() {
        if (mWindowLayoutParams == null) {
            return;
        }
        int height = mCallView.getHeight();
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
        int resourceId = mContext.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        int statusBarHeight = mContext.getResources().getDimensionPixelSize(resourceId);
        if (mWindowLayoutParams.y < 0) {
            mWindowLayoutParams.y = 0;
        } else if (mWindowLayoutParams.y > (screenHeight - height - statusBarHeight)) {
            mWindowLayoutParams.y = screenHeight - height - statusBarHeight;
        }
    }

    public class FloatBinder extends Binder {
        public FloatWindowService getService() {
            return FloatWindowService.this;
        }
    }

    private class FloatingListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mIsMove = false;
                    mTouchStartX = (int) event.getRawX();
                    mTouchStartY = (int) event.getRawY();

                    mStartX = (int) event.getRawX();
                    mStartY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchCurrentX = (int) event.getRawX();
                    mTouchCurrentY = (int) event.getRawY();
                    if (mWindowLayoutParams != null && null != mCallView) {
                        mWindowLayoutParams.x += mTouchCurrentX - mTouchStartX;
                        mWindowLayoutParams.y += mTouchCurrentY - mTouchStartY;
                        mWindowManager.updateViewLayout(mCallView, mWindowLayoutParams);
                    }
                    mTouchStartX = mTouchCurrentX;
                    mTouchStartY = mTouchCurrentY;
                    break;
                case MotionEvent.ACTION_UP:
                    mStopX = (int) event.getRawX();
                    mStopY = (int) event.getRawY();
                    if (Math.abs(mStartX - mStopX) >= 5 || Math.abs(mStartY - mStopY) >= 5) {
                        mIsMove = true;
                        if (null != mCallView) {
                            mWidth = mCallView.getWidth();
                            if (mTouchCurrentX > (mScreenWidth / 2)) {
                                startScroll(mStopX, mScreenWidth - mWidth, false);
                            } else {
                                startScroll(mStopX, 0, true);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
            return mIsMove;
        }
    }
}
