package com.TextToSpeech;
/**
 *工具类：
 *      获取屏幕尺寸
 *      判断服务是否正在运行
 * @author: Rindy
 * @date: 2018/12/19 11:24
 **/
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.WindowManager;

import java.util.List;

public class Utils {
    //android.view.WindowManager和android.content.context
    public static Point getScreenSize(Context context){
        WindowManager mWindowManager  = (WindowManager)context.
                getSystemService(context.WINDOW_SERVICE); //Context.WINDOW_SERVICE与getSystemService()
                                                //一起使用，以检索用于访问系统的窗口管理器的WindowManager
        //android.graphics.Point:保存两个整数坐标
        Point p = new Point();
        //getDefaultDisplay()返回Display此windowManager实例将在其上创建新窗口的位置
        //Display.getSize()获取显示的大小
        mWindowManager.getDefaultDisplay().getSize(p);
        return p;
    }

    public static int pix2dp(Context context, int px) {
        int dp;
        final float scale = context.getResources().getDisplayMetrics().density;
        dp = (int) (px / scale + 0.5f);
        return dp;
    }

    public static int dp2px(Context context, int dp) {
        int px;
        //Resources.getDisplayMetrics():返回对该资源对象有效的当前显示指标。返回的对象应该被视为只读的。
        //DisplayMetrics.density:显示的逻辑密度
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,context.getResources().getDisplayMetrics());
    }

    public static int dp2pix(Context context,int dp){
        int px;
        //getResource():android.content.Context该方法的实现应该返回与getAssets()返回的AssetManager实例一致的资源实例。
        //              例如，它们应该共享相同的Configuration对象。
        //getDisplayMetrics():android.content.res.Resources返回对该资源对象有效的当前显示指标(return
        //                  DisplayMetrics)。返回的对象应该被视为只读的。
        //density:(android.util.DisplayMetrics)显示的逻辑密度。
        final float scale = context.getResources().getDisplayMetrics().density;  //获取屏幕的显示密度
        px = (int) (dp * scale + 0.5f);
        return px;
    }

    //判断某一服务是否正在运行
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(200);
        if (myList.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : myList) {
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                isWork = true;
            }
        }
        return isWork;
    }
}
