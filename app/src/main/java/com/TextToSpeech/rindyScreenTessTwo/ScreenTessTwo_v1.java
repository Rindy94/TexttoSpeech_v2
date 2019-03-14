package com.TextToSpeech.rindyScreenTessTwo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.TextToSpeech.rindyFloatBall.FloatViewManager;
import com.TextToSpeech.rindyTextToSpeak.TTSmain;
import com.TextToSpeech.rindyTextToSpeech.R;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

import static com.TextToSpeech.rindyScreenTessTwo.SDUtils.assets2SD;

public class ScreenTessTwo_v1 extends Activity {
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
    private TessBaseAPI tessBaseAPI = new TessBaseAPI();
    private TessTask tessTask = new TessTask();

    private Bitmap Extra_bitmap;
    private String text;

    private static boolean isExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tess_two_layout);

        //取出传递过来的bitmap
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        byte[] bytes = b.getByteArray("bitmap");
        Extra_bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        tessTask.execute(Extra_bitmap);
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

    private class TessTask extends AsyncTask<Bitmap,String,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //通知用户开始提取文字
            setContentView(R.layout.tess_two_layout);
            isExit = false;
        }

        public boolean checkTraineddataExists(){
            File file = new File(LANGUAGE_PATH);
            return file.exists();
        }

        //提取文字操作：工作线程中进行，将提取出的文字作为结果返回
        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            Log.d("MainActivity","doInBackground()");
            if (!checkTraineddataExists()) {
                assets2SD(getApplicationContext(), LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
            }
            tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
            tessBaseAPI.setImage(Extra_bitmap);
            text = tessBaseAPI.getUTF8Text();
            Log.d("MainActivity","文字内容："+text);
            tessBaseAPI.end();
            return text;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d("MainActivity","onProgressUpdate()");
            super.onProgressUpdate(values);
        }

        //doInBackground结束后,判断是否进行下一步操作。主线程内
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!isExit) {
                if (text != "") {
                    Log.d("MainActivity", "onPostExecute()");
                    Intent in = new Intent(ScreenTessTwo_v1.this, TTSmain.class);
                    in.putExtra("extra_str", text);
                    startActivity(in);
                    finishAndRemoveTask();
                }else {
                    Toast.makeText(getApplicationContext(),"没有识别到文字，请重新选择",Toast.LENGTH_LONG).show();
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onCancelled() {
            Log.d("MainActivity","onCancelled()");
            super.onCancelled();
            finishAndRemoveTask();
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

    private void exit(){
        if(!isExit){
            isExit = true;
            Toast.makeText(getApplicationContext(),"再按一次退出当前识别",Toast.LENGTH_SHORT).show();
//            handler.sendEmptyMessageDelayed(0,2000);
        }else {
            Log.d("MainActivity", "ScreenTessTwo----exit()");
            FloatViewManager.create(getApplicationContext()).mfloatView.setVisibility(View.VISIBLE);
            this.finish();
        }
    }
}
