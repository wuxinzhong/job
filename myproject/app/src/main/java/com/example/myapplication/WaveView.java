package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import java.util.Timer;
import java.util.TimerTask;

/**
 * author：dong.jingfeng
 * date：2019-12-06:14:47
 * email：315975450@qq.com
 * description：
 * remark：
 */

public class WaveView extends View {

    private Paint mPaint;
    private int mRadius;//里面圆圈的半径
    private Context mContext;
    private int mWidth;//控件的宽度
    /**
     * 设置波浪圆圈的宽度
     */
    private int mStrokeWidth = 4;
    /**
     * 多个扩散波浪间隙大小
     */
    private int gapSize;
    /**
     * 第一个扩散波浪的半径
     */
    private int firstRadius;
    /**
     * 控制生成扩散波浪的数量
     */
    private int numberOfCircle = 4;
    /**
     * 扩散波浪颜色
     */
    private int mLineColor;
    private boolean isFirstTime = true;
    //点击事件监听器
    private float mDownX, mDownY;
    private OnClickListener mClickListener;

    /**
     * 设置波浪扩散的速度，单位毫秒，值越小扩散越快
     */
    private int period = 100;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        mLineColor = ta.getColor(R.styleable.WaveView_waveColor, Color.BLACK);
        ta.recycle();  //注意回收
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mWidth = dip2px(50);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        numberOfCircle = 4;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                //match_parent 或者 精确的数值
                mWidth = width;
                break;
        }
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                mWidth = Math.min(mWidth, height);
                break;
        }
        mRadius = mWidth / (numberOfCircle - 1);
        gapSize = (mWidth / 2 - mRadius) / numberOfCircle;
        firstRadius = mRadius;// + gapSize;
        setMeasuredDimension(mWidth, mWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth / 2, mWidth / 2);//平移
        //画周围的波浪
        firstRadius += 3;//每次刷新半径增加3像素
        firstRadius %= (mWidth / 2);//控制在控件的范围中
        if (firstRadius < mRadius) isFirstTime = false;
        firstRadius = checkRadius(firstRadius);//检查半径的范围
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(mLineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        //画波浪
        for (int i = 0; i < numberOfCircle; i++) {
            //控制外部最大圆半径只有最大宽度的一半
            int radius = (firstRadius + i * gapSize) % (mWidth / 2);
            if (isFirstTime && radius > firstRadius) {
                continue;
            }
            //检查半径的范围
            radius = checkRadius(radius);
            //用半径来计算透明度  半径越大  越透明
            double x = 1.0;
            if (radius > mRadius + gapSize) {
                //后面圆的透明度是淡出
                x = (mWidth / 2 - radius) * 1.0 / (mWidth / 2 - mRadius);
            } else {
                //第一个圆的透明度是淡入效果
                x = (radius - mRadius * 1.0) / gapSize;
            }
            //255*0.8表示最白的时候都是80%
            mPaint.setAlpha((int) (255 * 0.8 * x));
            canvas.drawCircle(0, 0, radius, mPaint);
        }
    }

    //检查波浪的半径  如果小于圆圈，那么加上圆圈的半径
    private int checkRadius(int radius) {
        if (radius < mRadius) {
            return radius + mRadius;// + gapSize;
        }
        return radius;
    }

    public int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void startAnimation() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                postInvalidate();
            }
        }, 0, period);

    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mClickListener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                return checkIsInCircle((int) mDownX, (int) mDownY);

            case MotionEvent.ACTION_UP:
                int upX = (int) event.getX(), upY = (int) event.getY();
                if (checkIsInCircle(upX, upY) && mClickListener != null) {
                    mClickListener.onClick(this);
                }
                break;

        }
        return true;
    }

    /**
     * 检查点x,y是否落在圆圈内
     *
     * @param x
     * @param y
     * @return
     */
    private boolean checkIsInCircle(int x, int y) {
        int centerX = (getRight() - getLeft()) / 2;
        int centerY = (getBottom() - getTop()) / 2;
        boolean b = Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) < Math.pow(mRadius, 2);
        return b;
    }

    public int getLineColor() {
        return mLineColor;
    }

    public void setLineColor(int mLineColor) {
        this.mLineColor = mLineColor;
    }

    public int getGapSize() {
        return gapSize;
    }

    public void setGapSize(int gapSize) {
        this.gapSize = gapSize;
    }

    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(int mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
    }

    public int getPeriod() {
        return this.period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

}