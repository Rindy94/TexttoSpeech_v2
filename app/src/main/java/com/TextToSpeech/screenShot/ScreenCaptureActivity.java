package com.TextToSpeech.screenShot;
/**
 * 长按悬浮球事件：
 *      因为调用截屏API必须将活动写在Activity中，但截取的屏幕内容须是用户当前所在的屏幕，所以此处将
 *      当前活动隐藏，并使当前活动的theme为透明(@android:style/Theme.Translucent.NoTitleBar.Fullscreen)，将
 *      截取的屏幕保存为bitmap传递给ScreenViewActivity。在ScreenTessTwo中对bitmap进行部分截取操作。
 *
 * TextToSpeech
 * @author: Rindy
 * @date: 2019/1/2 12:02
 **/

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.TextToSpeech.rindyScreenTessTwo.ScreenTessTwo;
import com.TextToSpeech.rindyTextToSpeech.R;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ScreenCaptureActivity extends Activity {
    private MediaProjectionManager mMediaProjectionManager;
    private int REQUEST_MEDIA_PROJECTION = 1;
    private WindowManager mWindowManager;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private int mResultCode;
    private Intent mResultData;
    private VirtualDisplay mVirtualDisplay;
    private int windowWidth;
    private int windowHeight;
    private int mScreenDensity;
    private Image image;
    private DisplayMetrics metrics;
    private Bitmap bitmap;

    private MarkSizeView markSizeView;
    private Rect markedArea;

//    private Handler handler;
    private Runnable runnable;
    private boolean exits = false;

//    public static final int UPDATE_TEXT = 1;

//    @SuppressLint("HandlerLeak")
//    private Handler handler  = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case UPDATE_TEXT:
//                    //在这里进行UI更新操作
//                    setContentView(R.layout.tess_two_layout);
//                    Log.d("MainActivity","ScreenCA----handleMessage");
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取MediaProjectionManager实例
        mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        setContentView(R.layout.activity_screen_capture);
//        setContentView(R.layout.tess_two_layout);
        //发起录屏的请求
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);

        createVirtualEnvironment();
//        moveTaskToBack(true);//activity 隐藏

        markSizeView = findViewById(R.id.mark_size);

        markSizeView.setmOnClickListener(new MarkSizeView.onClickListener() {
            @Override
            public void onConfirm(Rect markedArea) {
                //点击截屏后的"V"ic_done_white_36dp.png
                ScreenCaptureActivity.this.markedArea = new Rect(markedArea);
                markSizeView.reset();
                markSizeView.setUnmarkedColor(getResources().getColor(R.color.transparent));
                markSizeView.setEnabled(false);
                startIntent();
            }
            @Override
             public void onCancel() {
                //点击截屏后的"X"ic_close_capture.png
            }
            @Override
            public void onTouch() {
            }
        });
    }

    //通过 onActivityResult 返回结果获取 MediaProjection
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                //对取消录屏进行事件更新
                onBackPressed();
                Log.d("MainActivity","Screen:取消");
                return;
            } else if (data != null && resultCode != 0) {
                mResultCode = resultCode;
                mResultData = data;
                startVirtual();
            }
        }
    }
    //创建虚拟环境
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createVirtualEnvironment() {
        mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager.getDefaultDisplay().getHeight();
        metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565
    }

    //处理截屏图片
    @SuppressLint("HandlerLeak")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startCapture() {
        ScreenCaptureActivity.this.markedArea = new Rect(markedArea);
        image = mImageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        //返回具有指定宽度和高度的可变位图。
        bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);  //从当前位置开始复制缓冲区中的像素，覆盖位图的像素。
//        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        bitmap = Bitmap.createBitmap(bitmap, markedArea.left, markedArea.top, markedArea.right - markedArea.left, markedArea.bottom - markedArea.top);

        if (bitmap != null) {
            exits = true;
            //对截取图片进行操作
            //将截取的bitmap传递到ScreenTessTwo活动进行文字提取操作。
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes=baos.toByteArray();
            final Bundle bundle = new Bundle();
            bundle.putByteArray("bitmap", bytes);

            //在此标记耗时操作开始：转换文字是一个耗时操作
            //开始更新UI(缓冲中....)
            Intent intent = new Intent(ScreenCaptureActivity.this,ScreenTessTwo.class);
//            setContentView(R.layout.tess_two_layout);
            intent.putExtras(bundle);
            startActivity(intent);
            finishAndRemoveTask();
        }
//        onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startVirtual() {
        if (mMediaProjection != null) {
            Log.d("MainActivity","Screen:立即开始1");
            virtualDisplay();
        } else {
            Log.d("MainActivity","Screen:立即开始2");
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startIntent() {

        new Thread(new Runnable() {
            @Override
            public void run() {
//                Message message = new Message();
//                message.what = UPDATE_TEXT;
//                handler.sendMessage(message);
                startCapture();
            }
        }).start();
    }

    @SuppressLint("MissingSuperCall")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        Log.d("MainActivity","ScreenCaptureActivity---onDestory");
//        handler.removeCallbacks(runnable);
        tearDownMediaProjection();
//        finishAndRemoveTask();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        Log.d("MainActivity","ScreenCaptureActivity---onBackPressed");
        tearDownMediaProjection();
//        finishAndRemoveTask();
        super.onBackPressed();
    }
}