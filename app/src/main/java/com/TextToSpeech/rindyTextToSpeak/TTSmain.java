package com.TextToSpeech.rindyTextToSpeak;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.TextToSpeech.rindyTextToSpeech.R;

public class TTSmain extends Activity {
    private  TextToSpeech textToSpeech;
    private String extra_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_capture);

        Intent intent = getIntent();
        extra_text = intent.getStringExtra("extra_str");

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeak());
    }

    public class TextToSpeak implements TextToSpeech.OnInitListener{
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS){
                Log.d("Main","初始化引擎成功");

                textToSpeech.setPitch(2f);
                textToSpeech.speak(extra_text,TextToSpeech.QUEUE_FLUSH,null,"test_data");
            }else
                Log.d("Main","初始化引擎失败");
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            Log.d("MainActivity","TTSmain----onDestroy()");
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onDestroy();
    }
}
