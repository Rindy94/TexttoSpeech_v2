package com.TextToSpeech.rindyFloatServices;
/**
 * 手动新建服务，继承自Service，完成以下：
 *      1.前台服务，保证程序不易被系统kill
 *      2.onCreate()指向FloatViewManager.create()
 *      3.onDestroy()关闭前台服务
 * @author: Rindy
 * @date: 2018/12/18 15:47
 **/
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.TextToSpeech.MainActivity;
import com.TextToSpeech.rindyFloatBall.FloatView;
import com.TextToSpeech.rindyFloatBall.FloatViewManager;
import com.TextToSpeech.rindyTextToSpeech.R;

public class FloatService extends Service {
    private Notification notification;
    private NotificationManager notificationManager;
    @Override
    public void onCreate() {
        createNotification();
        FloatViewManager.create(getApplicationContext()).showFloatBall();
        FloatViewManager.create(getApplicationContext()).onTouchListener();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        notificationManager.cancel(ID);
    }

    private static final int ID = 123;
    //前台服务
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void createNotification(){
        Intent nfIntent = new Intent(getApplicationContext(),MainActivity.class);
        PendingIntent pdIntent = PendingIntent.getActivity(getApplicationContext(),0, nfIntent,0);
        Notification.Builder builder;
        builder = new Notification.Builder(this)
                .setContentText("TTS正在运行....")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pdIntent);
        notification =  builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL|Notification.FLAG_ONGOING_EVENT;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(ID,notification);
        startForeground(ID,notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
