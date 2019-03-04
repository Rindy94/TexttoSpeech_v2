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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

//import com.TextToSpeech.rindyTextToSpeech.R;
import com.TextToSpeech.screenShot.ScreenCaptureActivity;

public class TTSmain extends Activity {
    private  TextToSpeech textToSpeech;
    private String extra_text;
    private static final int UPDATE_UI = 1;

    private ScreenCaptureActivity scActivity;

//    @SuppressLint("HandlerLeak")
//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case UPDATE_UI:
////                    windowManager.removeView();
//                    setContentView(R.layout.tess_two_layout);
//                    break;
//                    default:
//                        break;
//            }
//        }
//    };

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_screen_capture);
//        setContentView(R.layout.tess_two_layout);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Message message = new Message();
//                message.what = UPDATE_UI;
//                handler.sendMessage(message);
//            }
//        }).start();

        Intent intent = getIntent();
        extra_text = intent.getStringExtra("extra_str");

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeak());
    }

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
//            scActivity = new ScreenCaptureActivity();
//            scActivity.onBackPressed();
        }
    }

    public class MyUtteranceProgressListener extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {

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
            finishAndRemoveTask();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
