package com.TextToSpeech.rindyTextToSpeak;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

//import com.TextToSpeech.rindyTextToSpeech.R;
import com.TextToSpeech.rindyFloatBall.FloatView;
import com.TextToSpeech.rindyFloatBall.FloatViewManager;
import com.TextToSpeech.screenShot.ScreenCaptureActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TTSmain extends Activity {
    private  TextToSpeech textToSpeech;
    private String extra_text;
    private static final int UPDATE_UI = 1;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_UI:
                    FloatViewManager.create(getApplicationContext()).mfloatView.setVisibility(View.VISIBLE);
                    break;
                    default:
                        break;
            }
        }
    };

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        extra_text = intent.getStringExtra("extra_str");

//        EventBus.getDefault().register(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = UPDATE_UI;
                handler.sendMessage(message);
                textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeak());
            }
        }).start();
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void  onEventMainThread(MessageEvent messageEvent){
//        Log.d("MainActivity","TTSmain----Event()");
//        if (messageEvent.getMessage() == "BACK"){
////            Log.d("MainActivity","TTSmain----Event()");
//            this.finish();
//        }else {
//
//        }
//    }

    public class TextToSpeak implements TextToSpeech.OnInitListener{

        @SuppressLint("NewApi")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS){
                Log.d("MainActivity","初始化引擎成功:"+ textToSpeech.isSpeaking());
                //读出文字内容
                textToSpeech.setPitch(2f);
                textToSpeech.setOnUtteranceProgressListener(new MyUtteranceProgressListener());
                textToSpeech.speak(extra_text,TextToSpeech.QUEUE_FLUSH,null,"test_data");
                Log.d("MainActivity","初始化引擎成功:"+ textToSpeech.isSpeaking());

            }else
                Log.d("MainActivity","初始化引擎失败");
        }
    }

    public class MyUtteranceProgressListener extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {
            Log.d("MainActivity","TTSmain----onStart()");
        }

        @Override
        public void onDone(String utteranceId) {
            Log.d("MainActivity","TTSmain----onDone()");
            finish();
        }

        @Override
        public void onError(String utteranceId) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            Log.d("MainActivity","TTSmain----onDestroy()");
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
//        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //定义一个消息事件内部类
    public static class MessageEvent{
        public String message;
        public MessageEvent(String message){
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
