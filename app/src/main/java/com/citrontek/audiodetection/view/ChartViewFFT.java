package com.citrontek.audiodetection.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.LinkedList;

//图表自定义view
public class ChartViewFFT extends SurfaceView implements SurfaceHolder.Callback {
    private LinkedList<String> vertical_list;//垂直数据类别名称
    private LinkedList<String> horizontal_list;//水平数据类别名称
    private int vertical_interval;
    private int horizontal_interval;
    private Paint mPaint;
    private TextPaint mTextPaint;
    private int margin=60;
    private int width,height;
    private SurfaceHolder mSurfaceHolder;
    private Thread CanvasDraw;
    private boolean drawing=false;

    private int mSpectrumNum = 48;
    private byte[] mBytes;
    private float[] mPoints;

    public ChartViewFFT(Context context) {
        super(context);
        initView();
    }

    public ChartViewFFT(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ChartViewFFT(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public ChartViewFFT(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //获取并设置view宽高
        width=getMeasuredWidth();
        height=getMeasuredHeight();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawChart(canvas);
    }

    //初始化相关绘制数据
    private void initView(){
        mPaint=new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(2f);
        mPaint.setTextSize(14f);
        mTextPaint=new TextPaint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(14f);

        mSurfaceHolder=getHolder();
        mSurfaceHolder.addCallback(this);
    }

    public void updateVisualizer(byte[] fft)
    {
        byte[] model = new byte[fft.length / 2 + 1];
        model[0] = (byte) Math.abs(fft[0]);
        for (int i = 2, j = 1; j < mSpectrumNum;)
        {
            model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
            i += 2;
            j++;
        }
        mBytes = model;
        invalidate();
    }


    //绘制表格
    private void drawChart(Canvas canvas){
        //绘制背景
        canvas.drawColor(Color.WHITE);
        //绘制横纵2条直线
        mPaint.setStrokeWidth(2f);
        canvas.drawLine(margin,(height-margin),margin,0,mPaint);
        canvas.drawLine(margin,(height-margin),width,(height-margin),mPaint);
        mPaint.setStrokeWidth(6f);
        //绘制波形
        if (mBytes == null)
        {
            return;
        }
        if (mPoints == null || mPoints.length < mBytes.length * 4)
        {
            mPoints = new float[mBytes.length * 4];
        }
        for (int i = 0; i < mSpectrumNum ; i++)
        {
            if (mBytes[i] < 0)
            {
                mBytes[i] = 127;
            }

            final int baseX = (width-margin)/mSpectrumNum;
            final int xi = baseX*i + baseX/2 +margin;

            mPoints[i * 4] = xi;
            mPoints[i * 4 + 1] = height-margin;

            mPoints[i * 4 + 2] = xi;
            mPoints[i * 4 + 3] = height-margin - mBytes[i];
        }
        canvas.drawLines(mPoints, mPaint);


    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        //开启线程
        drawing=true;
        CanvasDraw=new Thread(){
            @Override
            public void run() {
                while (drawing){
                    long start_time = System.currentTimeMillis();
                    Canvas canvas = surfaceHolder.lockCanvas(); // 通过lockCanvas加锁并得到該SurfaceView的画布
                    drawChart(canvas);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                    long end_time = System.currentTimeMillis();

                    long value_time=end_time - start_time;
                    if (value_time < 60) {
                        try {
                            Thread.sleep(60 - (value_time));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        CanvasDraw.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        //关闭线程
        drawing=false;
    }

    //设置图表横纵坐标数据值及个数

    //刷新图表

}
