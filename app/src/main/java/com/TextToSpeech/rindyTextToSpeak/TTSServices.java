package com.TextToSpeech.rindyTextToSpeak;

/**
 *  将TextToSpeak作为服务，Service在后台运行，默默地为用户提供功能，进行调度和统筹，
 */

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.TextToSpeech.rindyScreenTessTwo.ScreenTessTwo;

public class TTSServices extends Service {

    private TextToSpeech textToSpeech;
    private String extra_text;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        extra_text = intent.getStringExtra("extra_str");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeak());
//        Intent intent = new Intent();
//        intent.setClassName(this,"in_tts");
        super.onCreate();
    }

    public class TextToSpeak implements TextToSpeech.OnInitListener{

        @SuppressLint("NewApi")
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS){
                Log.d("MainActivity","初始化引擎成功:"+ extra_text);
                //读出文字内容
                textToSpeech.setPitch(2f);
                textToSpeech.setOnUtteranceProgressListener(new MyUtteranceProgressListener());
                textToSpeech.speak(extra_text,TextToSpeech.QUEUE_FLUSH,null,"test_data");
            }else
                Log.d("MainActivity","初始化引擎失败");
        }
    }

    public class MyUtteranceProgressListener extends UtteranceProgressListener {

        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            Log.d("MainActivity","TTService------onDone()");
            stopService(new Intent());
            onDestroy();
        }

        @Override
        public void onError(String utteranceId) {

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            Log.d("MainActivity","TTService----onDestroy()");
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onDestroy();
    }
}
