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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.TextToSpeech.rindyFloatBall.FloatViewManager;
import com.TextToSpeech.rindyFloatServices.FloatService;
import com.TextToSpeech.rindyTextToSpeech.R;
import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.model.GuidePage;
import com.app.hubert.guide.model.HighLight;
import com.app.hubert.guide.model.HighlightOptions;
import com.app.hubert.guide.model.RelativeGuide;
//import com.example.ndh.myguideview.Guide;

public class MainActivity extends AppCompatActivity{
    private Switch switch_open;
    private int count = 1;   //引导页在程序中整个累加的次数
    private Button btn_demo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

//        操作演示
        btn_demo = findViewById(R.id.demo);
        btn_demo.setOnClickListener(new myDemoClick());

        //活动入口
        date();
    }

    private void date(){
        SharedPreferences shared = getSharedPreferences("is",MODE_PRIVATE);
        boolean isfre = shared.getBoolean("isfre",true);
        SharedPreferences.Editor editor = shared.edit();
        if (isfre){
            //第一次进入跳转，提醒用户开启权限：允许访问顶层显示权限
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getApplicationContext().getApplicationContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
//            isGuide();
            switch_open.setOnCheckedChangeListener(new myOnCheckedChangeListener());

            editor.putBoolean("isfre", false);
            editor.commit();
        }else {
//            count++;
//            isGuide();
            //第二次进入跳转(除非卸载之后重新安装，否则都属于二次进入)
            switch_open.setOnCheckedChangeListener(new myOnCheckedChangeListener());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        NewbieGuide.resetLabel(getApplicationContext(),"guide4");   //重置count次数
        if (switch_open.getText().toString() == "OFF"){
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class myOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener{

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
    }

    class myDemoClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this,"功能暂未开放",Toast.LENGTH_SHORT).show();
            count = count + 1;
//            isGuide();
        }
    }

    public void isGuide(){
        Log.d("MainActivitys","isGuide()");

        Animation enterAnimation = new AlphaAnimation(0f,1f);
        enterAnimation.setDuration(100);
        enterAnimation.setFillAfter(true);
        Animation exitAnimation = new AlphaAnimation(1f,0f);
        exitAnimation.setDuration(100);
        exitAnimation.setFillAfter(true);

        //高亮区域点击事件
        HighlightOptions hlOptions1 = new HighlightOptions.Builder()
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("Guide","onClick---switch");
                        switch_open.setChecked(true);
                        switch_open.setText("ON");
                    }
                }).build();
//        HighlightOptions hlOptions2 = new HighlightOptions.Builder()
//                .setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Log.d("Guide","OFF---floatball");
//                        new FloatViewManager().showFloatBall();
//                    }
//                }).build();

        //引导页1
        final GuidePage guidePage1 = GuidePage.newInstance()
                .addHighLightWithOptions(switch_open,hlOptions1)
                .addHighLight(switch_open,HighLight.Shape.RECTANGLE,8,new RelativeGuide(R.layout.layer_switch,Gravity.BOTTOM,-50))
//                .setLayoutRes(R.layout.layer_switch,R.layout.layer_floatball)
                .setEverywhereCancelable(true)     //是否点击任意位置引导页消失，默认为true
                .setEnterAnimation(enterAnimation)
                .setExitAnimation(exitAnimation);
        //引导页2
        final GuidePage guidePage2 = GuidePage.newInstance()
//                .setLayoutRes(R.layout.layer_floatball,R.id.switch_floating_ball)
                .addHighLight(switch_open,HighLight.Shape.CIRCLE,8,new RelativeGuide(R.layout.layer_floatball,Gravity.LEFT))
//                .addHighLightWithOptions(ftView,hlOptions2)
                .setEverywhereCancelable(true)
                .setEnterAnimation(enterAnimation)
                .setExitAnimation(exitAnimation);

        NewbieGuide.with(MainActivity.this)
                .setLabel("guide4")
//                .setOnPageChangedListener(new OnPageChangedListener() {
//                    @Override
//                    public void onPageChanged(int i) {
//                        Toast.makeText(getApplicationContext(),"切换成功",Toast.LENGTH_LONG).show();
//                    }
//                })
                .addGuidePage(guidePage1)
                .addGuidePage(guidePage2)
                .setShowCounts(count)
                .show();
    }
}
