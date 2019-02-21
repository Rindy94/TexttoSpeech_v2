package com.TextToSpeech;

/**
 * 程序入口
 *      加载UI，判断FloatService服务是否正在运行
 *              true:开关键Switch置为开启状态，Text置为ON
 *              false:开关键Switch置为关闭状态，Text置为OFF
 *      Switch的选择事件：
 *            checked = true：开启服务
 *           checked = false：关闭服务 --> back键：彻底杀死进程
 * @author: Rindy
 * @date: 2018/12/18 15:52
 **/

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.TextToSpeech.rindyFloatBall.FloatView;
import com.TextToSpeech.rindyFloatBall.FloatViewManager;
import com.TextToSpeech.rindyFloatServices.FloatService;
import com.TextToSpeech.rindyTextToSpeech.R;

public class MainActivity extends AppCompatActivity {
    Switch switch_open;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        switch_open = findViewById(R.id.switch_floating_ball);
        boolean toogle = Utils.isServiceWork(getApplicationContext(),FloatService.class.getName());
        if (toogle == true){
            switch_open.setText("ON ");
            switch_open.setChecked(true);
        }else {
            switch_open.setText("OFF ");
            switch_open.setChecked(false);
        }
        switch_open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("MainActivity","onCheckedChanged()");
                if (isChecked){
                    startService(new Intent(MainActivity.this,FloatService.class));
                    Toast.makeText(MainActivity.this,"Srevice Ongoing",Toast.LENGTH_SHORT).show();
                    switch_open.setText("ON ");
                }else {
                    stopService(new Intent(MainActivity.this,FloatService.class));
                    FloatViewManager.create(getApplicationContext()).remove();
                    Toast.makeText(MainActivity.this,"Service Off",Toast.LENGTH_SHORT).show();
                    switch_open.setText("OFF ");
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (switch_open.getText().toString() == "OFF"){
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
