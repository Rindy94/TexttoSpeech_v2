package com.TextToSpeech.screenShot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.TextToSpeech.Utils;
import com.TextToSpeech.rindyTextToSpeech.R;

/**
 *AttributeSet：自定义控件属性
 * @author: Rindy
 * @date: 2019/1/3 10:24
 **/
public class MarkSizeView extends View {

    private static final int DEFAULT_MARKED_COLOR = Color.parseColor("#00000000");
    private static final int DEFAULT_UNMARKED_COLOR = Color.parseColor("#80000000");
    private static final int DEFAULT_STROKE_WIDTH = 2;//dp
    private static final int DEFAULT_CONFIRM_BUTTON_RES = R.mipmap.ic_done_white_36dp;
    private static final int DEFAULT_CANCEL_BUTTON_RES = R.mipmap.ic_close_capture;

    private final int BUTTON_EXTRA_WIDTH = Utils.dp2px(getContext(),8);

    private static final int DEFAULT_VERTEX_WIDTH = 20;//dp

    private int markedColor = DEFAULT_MARKED_COLOR;
    private int unmarkedColor = DEFAULT_UNMARKED_COLOR;
    private int strokeWidth = (int) Utils.dp2px(getContext(),DEFAULT_STROKE_WIDTH);//dp
    private int confirmButtonRes = DEFAULT_CONFIRM_BUTTON_RES;
    private int cancelButtonRes = DEFAULT_CANCEL_BUTTON_RES;
    private int vertexWidth = (int) Utils.dp2px(getContext(),DEFAULT_VERTEX_WIDTH);
    private int mActionGap;


    private Paint unMarkPaint,markPaint, mBitPaint;

    private int downX,downY;
    private int startX,startY;
    private int endX,endY;

    private Rect markedArea;
    private Rect confirmArea,cancelArea;
    private RectF ltVer,rtVer,lbVer,rbVer;
    private boolean isValid=false;
    private boolean isUp=false;
    private boolean isMoveMode=false;
    private boolean isAdjustMode=false;
    private boolean isButtonClicked=false;
    private int adjustNum = 0;

    private Bitmap confirmBitmap,cancelBitmap;

    private onClickListener mOnClickListener;

    private boolean isMarkRect = true;

    public MarkSizeView(Context context) {
        super(context);
        init(getContext(),null);
    }

    public MarkSizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs){

        if (attrs!=null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarkSizeView);
            markedColor=typedArray.getColor(R.styleable.MarkSizeView_markedColor,DEFAULT_MARKED_COLOR);
            unmarkedColor=typedArray.getColor(R.styleable.MarkSizeView_unMarkedColor,DEFAULT_UNMARKED_COLOR);
            strokeWidth=typedArray.getDimensionPixelSize(R.styleable.MarkSizeView_strokeWidth, (int) Utils.dp2px(context,DEFAULT_STROKE_WIDTH));
            vertexWidth=typedArray.getDimensionPixelSize(R.styleable.MarkSizeView_vertexWidth, (int)Utils.dp2px(context,DEFAULT_VERTEX_WIDTH));
            confirmButtonRes =typedArray.getResourceId(R.styleable.MarkSizeView_confirmButtonRes,DEFAULT_CONFIRM_BUTTON_RES);
            cancelButtonRes=typedArray.getResourceId(R.styleable.MarkSizeView_cancleButtonRes,DEFAULT_CANCEL_BUTTON_RES);
        }

        unMarkPaint=new Paint();
        unMarkPaint.setColor(unmarkedColor);
        unMarkPaint.setAntiAlias(true);   //该方法作用是抗锯齿:true设置了抗锯齿，边界显得模糊
                                         //Paint.setDither()：该方法是设置防抖动。设置为true显得柔和

        markPaint=new Paint();
        markPaint.setColor(markedColor);
        markPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        markPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));  //设置和清除传输模式
        markPaint.setColor(markedColor);
        markPaint.setStrokeWidth(strokeWidth);
        markPaint.setAntiAlias(true);

        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);

        markedArea=new Rect();
        confirmArea=new Rect();
        cancelArea=new Rect();

        ltVer=new RectF();
        rtVer=new RectF();
        lbVer=new RectF();
        rbVer=new RectF();

        confirmBitmap= BitmapFactory.decodeResource(getResources(), confirmButtonRes);
        cancelBitmap = BitmapFactory.decodeResource(getResources(),cancelButtonRes);

        mActionGap = (int) Utils.dp2px(context,15);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        //draw unmarked
//      使用指定的绘图绘制指定的矩形。矩形将根据画中的样式填充或框起来。
        canvas.drawRect(0,0,width,height,unMarkPaint);
        //draw marked
        if (isValid || !isEnabled()) {
            //使用指定的油漆绘制指定的矩形。矩形将根据画中的样式填充或框起来。
            canvas.drawRect(markedArea, markPaint);
        }
        if (!isEnabled()){
            return;
        }
        //draw button
        if (isValid && isUp) {
            //绘制指定的位图，自动缩放/平移以填充目标矩形。如果源矩形不为null，则它指定要绘制的位图的子集。
            canvas.drawBitmap(confirmBitmap,null,confirmArea,mBitPaint);
            canvas.drawBitmap(cancelBitmap,null,cancelArea,mBitPaint);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()){
            return false;
        }
        int x= (int) event.getX();
        int y= (int) event.getY();
        if (isMarkRect) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isUp = false;
                    isAdjustMode = false;
                    isMoveMode = false;
                    isButtonClicked = false;
                    isValid = true;
                    adjustNum = 0;
                    downX = x;
                    downY = y;
                    if (mOnClickListener != null) {
                        mOnClickListener.onTouch();
                    }
                    if (isAreaContainPoint(confirmArea, x, y)) {
                        isButtonClicked = true;
                        isValid = true;
                        if (mOnClickListener != null) {
                            mOnClickListener.onConfirm(markedArea);
                        }
                    } else if (isAreaContainPoint(cancelArea, x, y)) {
                        isButtonClicked = true;
                        isValid = true;
                        if (mOnClickListener != null) {
                            mOnClickListener.onCancel();
                            isValid = false;
                            startX = startY = endX = endY = 0;
                            adjustMark(0, 0);
                        }
                    } else if (isAreaContainPoint(ltVer, x, y)) {
                        isAdjustMode = true;
                        adjustNum = 1;
                    } else if (isAreaContainPoint(rtVer, x, y)) {
                        isAdjustMode = true;
                        adjustNum = 2;
                    } else if (isAreaContainPoint(lbVer, x, y)) {
                        isAdjustMode = true;
                        adjustNum = 3;
                    } else if (isAreaContainPoint(rbVer, x, y)) {
                        isAdjustMode = true;
                        adjustNum = 4;
                    } else if (markedArea.contains(x, y)) {
                        isMoveMode = true;
                    }else {
                        isMoveMode = false;
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        endX = startX;
                        endY = startY;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isButtonClicked) {
                        break;
                    }
                    adjustMark(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    isUp = true;
                    if (isButtonClicked) {
                        break;
                    }
                    adjustMark(x, y);
                    startX = markedArea.left;
                    startY = markedArea.top;
                    endX = markedArea.right;
                    endY = markedArea.bottom;

                    if (markedArea.width() > confirmBitmap.getWidth() * 3 + mActionGap * 3 && markedArea.height() > confirmBitmap.getHeight() * 5) {
                        //显示在选区的内底部
                        confirmArea.set(endX - confirmBitmap.getWidth() - mActionGap, endY - confirmBitmap.getHeight() - mActionGap, endX - mActionGap, endY - mActionGap);
                        cancelArea.set(endX - 2 * confirmBitmap.getWidth() - mActionGap * 2, endY - confirmBitmap.getHeight() - mActionGap, endX - confirmBitmap.getWidth() - mActionGap * 2, endY - mActionGap);
                    } else if (endY > getHeight() - confirmBitmap.getHeight() * 3) {
                        //显示在选区的上面
                        confirmArea.set(endX - confirmBitmap.getWidth() - mActionGap, startY - confirmBitmap.getHeight() - mActionGap, endX - mActionGap, startY - mActionGap);
                        cancelArea.set(endX - 2 * confirmBitmap.getWidth() - mActionGap * 2, startY - confirmBitmap.getHeight() - mActionGap, endX - confirmBitmap.getWidth() - mActionGap * 2, startY - mActionGap);
                    } else {
                        //显示在选区的下面
                        confirmArea.set(endX - confirmBitmap.getWidth() - mActionGap, endY + mActionGap, endX - mActionGap, endY + confirmBitmap.getHeight() + mActionGap);
                        cancelArea.set(endX - 2 * confirmBitmap.getWidth() - mActionGap * 2, endY + mActionGap, endX - confirmBitmap.getWidth() - mActionGap * 2, endY + confirmBitmap.getHeight() + mActionGap);
                    }
                    if (cancelArea.left < 0){
                        int cancelAreaLeftMargin = Math.abs(cancelArea.left) + mActionGap;
                        cancelArea.left = cancelArea.left + cancelAreaLeftMargin;
                        cancelArea.right = cancelArea.right + cancelAreaLeftMargin;
                        confirmArea.left = confirmArea.left + cancelAreaLeftMargin;
                        confirmArea.right = confirmArea.right + cancelAreaLeftMargin;
                    }

                    if (!isValid) {
                        if (mOnClickListener != null) {
                            mOnClickListener.onCancel();
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    isUp = true;
                    break;
            }
        }
        postInvalidate();
        return true;
    }

    private boolean isAreaContainPoint(Rect area, int x, int y){
        Rect newArea=new Rect(area.left-BUTTON_EXTRA_WIDTH,area.top-BUTTON_EXTRA_WIDTH,area.right+BUTTON_EXTRA_WIDTH,area.bottom+BUTTON_EXTRA_WIDTH);
        if (newArea.contains(x,y)){
            return true;
        }
        return false;
    }
    private boolean isAreaContainPoint(RectF area,int x,int y){
        RectF newArea=new RectF(area.left-BUTTON_EXTRA_WIDTH,area.top-BUTTON_EXTRA_WIDTH,area.right+BUTTON_EXTRA_WIDTH,area.bottom+BUTTON_EXTRA_WIDTH);
        if (newArea.contains(x,y)){
            return true;
        }
        return false;
    }

    private void adjustMark(int x, int y) {
        if (isAdjustMode){
            int moveMentX = x-downX;
            int moveMentY = y-downY;

            switch (adjustNum){
                case 1:
                    startX = startX + moveMentX;
                    startY = startY + moveMentY;
                    break;
                case 2:
                    endX = endX + moveMentX;
                    startY = startY + moveMentY;
                    break;
                case 3:
                    startX = startX + moveMentX;
                    endY = endY + moveMentY;
                    break;
                case 4:
                    endX = endX + moveMentX;
                    endY = endY + moveMentY;
                    break;
            }
            downX = x;
            downY = y;
        }else if (isMoveMode){
            int moveMentX = x-downX;
            int moveMentY = y-downY;

            startX = startX + moveMentX;
            startY = startY + moveMentY;

            endX = endX + moveMentX;
            endY = endY + moveMentY;

            downX = x;
            downY = y;
        }else {
            endX = x;
            endY = y;
        }
        markedArea.set(Math.min(startX, endX), Math.min(startY, endY), Math.max(startX, endX), Math.max(startY, endY));
        ltVer.set(markedArea.left - vertexWidth / 2, markedArea.top - vertexWidth / 2, markedArea.left + vertexWidth / 2, markedArea.top + vertexWidth / 2);
        rtVer.set(markedArea.right - vertexWidth / 2, markedArea.top - vertexWidth / 2, markedArea.right + vertexWidth / 2, markedArea.top + vertexWidth / 2);
        lbVer.set(markedArea.left - vertexWidth / 2, markedArea.bottom - vertexWidth / 2, markedArea.left + vertexWidth / 2, markedArea.bottom + vertexWidth / 2);
        rbVer.set(markedArea.right - vertexWidth / 2, markedArea.bottom - vertexWidth / 2, markedArea.right + vertexWidth / 2, markedArea.bottom + vertexWidth / 2);
        if (markedArea.height()*markedArea.width()>200){
            isValid=true;
        }else {
            isValid = false;
        }
    }

    public interface onClickListener{
        void onConfirm(Rect markedArea);    //选中confirmBitmap的执行函数
        void onCancel();
        void onTouch();
    }

    public void setmOnClickListener(onClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    public void setUnmarkedColor(int unmarkedColor) {
        this.unmarkedColor = unmarkedColor;
        unMarkPaint.setColor(unmarkedColor);
        invalidate();
    }

    public void reset(){
        isUp = false;
        isValid = false;
        startX = startY = endX = endY = 0;
        adjustMark(0,0);
    }

    public void setIsMarkRect(boolean isMarkRect){
        Log.d("MainActivity","MarkSize.setIsMarkRect()");
        this.isMarkRect = isMarkRect;
    }
}
