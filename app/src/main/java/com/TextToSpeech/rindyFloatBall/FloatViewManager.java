package com.TextToSpeech.rindyFloatBall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.TextToSpeech.Utils;
import com.TextToSpeech.screenShot.ScreenCaptureActivity;

import java.lang.ref.WeakReference;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 *悬浮球view管理:
 *         显示位置
 *         启动模式？Instance
 * @author: Rindy
 * @date: 2018/12/19 11:13
 **/
public class FloatViewManager {
    //这里使用单例设计模式，为避免内存泄露所以使用弱引用
    private static WeakReference<Context> mContext;
    private WindowManager.LayoutParams mParams;
    private WindowManager mManager;
    private Point p = new Point();
    private int base;
    FloatView mfloatView;

    private boolean isClick;
    private int mDownX, mDownY, mLastX, mLastY;
    int deltaX;
    int deltaY;
    /**
     *ViewConfiguration.getScaledTouchSlop();触发移动事件的最小距离，自定义View处理touch事件的时候，
     *          有的时候需要判断用户是否真的存在movie，系统提供了这样的方法。表示滑动的时候，手的移
     *          动要大于这个返回的距离值才开始移动控件。
     * @date: 2018/12/27 16:45
     **/
    private int mTouchSlop;
    private Vibrator vibrator;   //长按震动
    private int time;
    private  Intent intent;

    private FloatViewManager(){
        mParams = new WindowManager.LayoutParams();
        mManager = (WindowManager)mContext.get().getSystemService(mContext.get().WINDOW_SERVICE);
        mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.gravity = Gravity.TOP | Gravity.LEFT;
        p = Utils.getScreenSize(mContext.get());
        base = Utils.dp2pix(mContext.get(),base);
        mTouchSlop = ViewConfiguration.get(mContext.get()).getScaledTouchSlop();
    }

    //显示悬浮球
    public void showFloatBall(){
        mfloatView = new FloatView(mContext.get().getApplicationContext());
        FloatViewManager.create(mContext.get().getApplicationContext()).attach(mfloatView,
                Utils.getScreenSize(mContext.get().getApplicationContext()).x,
                Utils.getScreenSize(mContext.get().getApplicationContext()).y/2);
    }

    //移除悬浮球
    public void remove(){
        Log.d("MainActivity","remove()");
        mManager.removeView(mfloatView);
    }
    private void attach(View v, int x, int y) {
        if (mParams == null || mManager == null || v == null) {
            throw new RuntimeException("windowManager not exists / view is null");
        }
        mParams.x = x;
        mParams.y = y;
        //设置悬浮窗口长宽数据
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mManager.addView(v, mParams);
    }

    //悬浮球事件
    @SuppressLint("ClickableViewAccessibility")
    public void onTouchListener() {
        mTouchSlop = ViewConfiguration.get(mContext.get()).getScaledTouchSlop();
        time = ViewConfiguration.getLongPressTimeout();
        vibrator = (Vibrator) mContext.get().getSystemService(VIBRATOR_SERVICE);
        //长按悬浮球开启截屏识别文字事件
        mfloatView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if ( isClick==true){
                    vibrator.vibrate(time);
                    intent = new Intent(mContext.get(),ScreenCaptureActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.get().startActivity(intent);
                }
                return true;
            }
        });
        mfloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("MainActivityACTION_DOWN", "x"+ mParams.x+"y"+mParams.y);
                        mDownX = x;
                        mDownY = y;
                        mLastX = mDownX;
                        mLastY = mDownY;
                        isClick = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int totalDeltaX = x - mDownX;   //这里的mDownX/mDownY为首次DOWN触发产生的坐标值，这里的x/y值为当前单次MOVE产生的坐标。
                        int totalDeltaY = y - mDownY;   //totalDeltaX表示当前单次MOVE产生的坐标与最原始的起点坐标之差。
                        deltaX = x - mLastX;    //首次执行到这里时deltaX的值都是为0；
                        deltaY = y - mLastY;    //之后每次执行deltaX的值都表示当前MOVE的坐标与前一次MOVE的坐标之差。
                        Log.d("MainActivityACTION_MOVE", "x"+ mParams.x+"y"+mParams.y);
                        if (Math.abs(totalDeltaX) > mTouchSlop || Math.abs(totalDeltaY) > mTouchSlop) {
                            //绝对值大于mTouchSlope时isClick 为false
                            isClick = false;
                        }
                        mLastX = x;   //将当前这一次MOVE产生的坐标赋值给mLastX。这里相当于是执行一次MOTION_MOVE产生的新坐标。
                        mLastY = y;
                        if (!isClick) {
                            onMove(deltaX, deltaY);
                            Log.d("MainActivity","onMove()");
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("MainActivityACTION_UP", "x"+ mParams.x+"y"+mParams.y);
                        onMove(deltaX, deltaY);
                        break;
                }
                return false;
            }
        });
    }
    public void onMove(int deltaX, int deltaY) {
        mParams.x += deltaX;
        mParams.y += deltaY;
        if (mManager != null) {
            mManager.updateViewLayout(mfloatView, mParams);
        }
        Log.d("MainActivityOnMove", "x"+ mParams.x+"y"+mParams.y);
        isClick = false;
    }
    private static class SingleInstance{
        public static final FloatViewManager INSTANCE = new FloatViewManager();
    }
    public static FloatViewManager create(Context context){
        mContext = new WeakReference<Context>(context);
        return SingleInstance.INSTANCE;
    }
}
