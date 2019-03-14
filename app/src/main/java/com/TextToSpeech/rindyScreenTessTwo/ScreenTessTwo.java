package com.TextToSpeech.rindyScreenTessTwo;

/**
 * 该类已作废！！！ 2019年3月12日
 * 使用Android的tess-two:参考https://www.jianshu.com/p/cc9ae05423a8
 *  tess-two 可以将bitmap中的文字提取出来。
 *  bitmap的传递： byte
 *
 *  这个类是一个耗时操作
 *
 *  按下两次BACK键退出应用,并通知TTSmain方法结束读语音操作
 *
 *
 * @author: Rindy
 * @date: 2019/1/21 14:29
 **/

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.TextToSpeech.rindyFloatBall.FloatViewManager;
import com.TextToSpeech.rindyTextToSpeak.TTSmain;
import com.TextToSpeech.rindyTextToSpeech.R;
import com.googlecode.tesseract.android.TessBaseAPI;


import org.greenrobot.eventbus.EventBus;

import java.io.File;

import static com.TextToSpeech.rindyScreenTessTwo.SDUtils.assets2SD;

public class ScreenTessTwo extends Activity {

    //TessBaseAPI初始化用到的第一个参数，是个目录。
    private static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    
    //在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
    private static final String tessdata = DATAPATH + File.separator + "tessdata";

    //TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
    private static String DEFAULT_LANGUAGE = "chi_sim";

    //assets中的文件名
    private static  String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";

    //保存到SD卡中的完整文件名
    private static  String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

    //权限请求值
    private static final int PERMISSION_REQUEST_CODE = 0;

    private Bitmap Extra_bitmap;
    private String text;

    private static final int UPDATE_UI = 1;
    private static final int UPDATE_JUMP = 0;
    private TessBaseAPI tessBaseAPI = new TessBaseAPI();
    private Runnable runnable;
    private Thread thread;

    private static boolean isExit = false;
    private static boolean isJump = true;

//    private Intent in_tts;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_UI:
                    setContentView(R.layout.tess_two_layout);
                    isExit = false;
                    Log.d("MainActivity","ScreentessTwo----handleMessage");
                    break;
//                case UPDATE_JUMP:
//                    Intent in = new Intent(ScreenTessTwo.this, TTSmain.class);
//                    in.putExtra("extra_str", text);
//                    startActivity(in);
//                    break;
                    default:
                        break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tess_two_layout);

        //取出传递过来的bitmap
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        byte[] bytes = b.getByteArray("bitmap");
        Extra_bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        runnable = new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = UPDATE_UI;
                handler.sendMessage(message);
                tessData();
            }
        };
        thread = new Thread(runnable);
        thread.start();
    }

    public boolean checkTraineddataExists(){
        File file = new File(LANGUAGE_PATH);
        return file.exists();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void tessData(){

        if (!checkTraineddataExists()) {
            assets2SD(getApplicationContext(), LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
        }
        tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
        tessBaseAPI.setImage(Extra_bitmap);
        text = tessBaseAPI.getUTF8Text();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Message message = new Message();
//                message.what = UPDATE_JUMP;
//                handler.sendMessage(message);
//            }
//        }).start();   && isJump == true
        if (text != null ) {
            isJump = true;
            Log.d("MainActivity", "ScreenTessTwo---" + text);
            Intent in = new Intent(ScreenTessTwo.this, TTSmain.class);
            in.putExtra("extra_str", text);
            startActivity(in);
        } else
//            thread.interrupt();

            Toast.makeText(getApplicationContext(), "没有识别到文字，请重新选取文字内容", Toast.LENGTH_LONG).show();
        tessBaseAPI.end();
        finishAndRemoveTask();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    assets2SD(getApplicationContext(), LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        Log.d("MainActivity","ScreenTessTwo---onDestroy()");
        super.onDestroy();
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void onBackPressed() {
//        Log.d("MainActivity","ScreenTessTwo---onBackPressed()");
////        Toast.makeText(ScreenTessTwo.this,"正在读取文字...",Toast.LENGTH_LONG).show();
////        this.finish();
////        EventBus.getDefault().post(new TTSmain.MessageEvent("BACK"));
////        finish();
//        super.onBackPressed();
//    }

    private void exit(){
        if(!isExit){
            isExit = true;
            Toast.makeText(getApplicationContext(),"再按一次退出当前识别",Toast.LENGTH_SHORT).show();
            handler.sendEmptyMessageDelayed(0,2000);
        }else {
            Log.d("MainActivity", "ScreenTessTwo----exit()");
            FloatViewManager.create(getApplicationContext()).mfloatView.setVisibility(View.VISIBLE);
            thread.interrupt();
//            synchronized (this) {
//                isJump = false;
//            }
            this.finish();
        }
    }
}
