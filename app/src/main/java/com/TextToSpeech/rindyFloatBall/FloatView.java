package com.TextToSpeech.rindyFloatBall;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import com.TextToSpeech.Utils;
import com.TextToSpeech.rindyTextToSpeech.R;


/**
 *绘制悬浮球:
 *      形式：实心圆形，外圆透明
 * @author: Rindy
 * @date: 2018/12/19 11:13
 **/

public class FloatView extends View {
    private int width = 20;
    private int r = Utils.dp2pix(getContext(), width);
    private Paint paint;

    public  FloatView(Context context){
        super(context);
        paint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(2 * r,2 * r);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("MainActivity","onDraw()");
        super.onDraw(canvas);
        //画大圆
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setARGB((int)(100),0,0,0);
        int width = getMeasuredWidth() / 2;
        int height = getMeasuredHeight() / 2;
        canvas.drawCircle(width,height,r,paint);
        //画小圆圈
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(getResources().getColor(R.color.White));
        canvas.drawCircle(width,height, r*2/3,paint);
    }

}
