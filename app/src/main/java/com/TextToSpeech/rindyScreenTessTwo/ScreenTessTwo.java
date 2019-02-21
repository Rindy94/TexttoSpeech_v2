package com.TextToSpeech.rindyScreenTessTwo;

/**
 * 使用Android的tess-two:参考https://www.jianshu.com/p/cc9ae05423a8
 *  tess-two 可以将bitmap中的文字提取出来。
 *  bitmap的传递： byte
 * @author: Rindy
 * @date: 2019/1/21 14:29
 **/

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.TextToSpeech.rindyTextToSpeak.TTSmain;
import com.TextToSpeech.rindyTextToSpeech.R;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

import static com.TextToSpeech.rindyScreenTessTwo.SDUtils.assets2SD;

public class ScreenTessTwo extends Activity {

    private long start_time = System.currentTimeMillis();

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

//    @SuppressLint("HandlerLeak")
//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case UPDATE_UI:
//                    break;
//                    default:
//                        break;
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity","onCreate");
        Log.d("MainActivity", String.valueOf(start_time));
        setContentView(R.layout.tess_two_layout);
        super.onCreate(savedInstanceState);
        //取出传递过来的bitmap
        Intent intent = getIntent();
        Bundle b=intent.getExtras();
        byte[] bytes=b.getByteArray("bitmap");
        Extra_bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        TessBaseAPI tessBaseAPI = new TessBaseAPI();

        if (!checkTraineddataExists()){
//                    Toast.makeText(getApplicationContext(),LANGUAGE_PATH+"不存在，开始复制",Toast.LENGTH_LONG).show();
            assets2SD(getApplicationContext(), LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
        }
//                Toast.makeText(getApplicationContext(),"正在识别文字",Toast.LENGTH_LONG).show();
        tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
        tessBaseAPI.setImage(Extra_bitmap);
        text = tessBaseAPI.getUTF8Text();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Message message = new Message();
//                message.what = UPDATE_UI;
//                handler.sendMessage(message);
//            }
//        }).start();
//        Handler handler = new Handler();
//        handler.postAtTime(new Runnable() {
//            @Override
//            public void run() {
//                Message message = new Message();
//                message.what = UPDATE_UI;
//                handler.sendMessage(message);
//            }
//        },start_time);
        Log.d("MainActivity","ScreenTessTwo---" + text);
        if (text != ""){
            long end_time = System.currentTimeMillis();
            Log.d("MainActivity", String.valueOf(end_time));
            Intent in = new Intent(ScreenTessTwo.this,TTSmain.class);
            in.putExtra("extra_str",text);
            startActivity(in);
        }else
            Toast.makeText(getApplicationContext(),"没有识别到文字，请重新选取文字内容",Toast.LENGTH_LONG).show();
        tessBaseAPI.end();
    }

    public boolean checkTraineddataExists(){
        File file = new File(LANGUAGE_PATH);
        return file.exists();
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
    protected void onDestroy() {
        Log.d("MainActivity","ScreenTessTwo---onDestroy()");
        super.onDestroy();
    }
}
