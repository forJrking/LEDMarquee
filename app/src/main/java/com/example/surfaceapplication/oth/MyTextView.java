package com.example.surfaceapplication.oth;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.example.surfaceapplication.R;

import java.io.IOException;
import java.io.InputStream;

public class MyTextView extends TextView {

    private Bitmap bitmap;
    private BitmapRegionDecoder bitmapRegionDecoder;
    private int outHeight,outWidth;

    public MyTextView(Context context) {
        super(context);
        bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher);
        try {
            InputStream is = getResources().openRawResource(R.raw.ic_launcher);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is,null,options);
            outHeight = options.outHeight;
            outHeight = options.outWidth;
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(is, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getText() != null) {
            int measureText = (int) this.getPaint().measureText(getText(), 0, getText().length());
            final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measureText, MeasureSpec.EXACTLY);
            setMeasuredDimension(childWidthMeasureSpec, getMeasuredHeight());
        }

    }

    Paint paint = new Paint();
    Paint highlightPaint;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mMaxScroll+=4;
        Rect rect = new Rect((int) mMaxScroll, 0, outWidth, outHeight);
        Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, null);
        canvas.drawBitmap(bitmap, 0,0, paint);
//        canvas.save();
//        SystemClock.sleep(15);
//        if (isRunning() && shouldDrawGhost()) {
//            // 平移重画
//            canvas.translate(mMaxScroll, 0.0f);
////            highlightPaint = new Paint();
////            // DES: 文字背景色
////            highlightPaint.setColor(Color.YELLOW);
////            highlightPaint.setStyle(Paint.Style.FILL);
////
////            getLayout().draw(canvas, path, highlightPaint, 0);
//            canvas.drawBitmap(bitmap,0f,0,paint);
//        }
//        canvas.restore();
    }

    // 跑马中
    boolean running = false;

    public boolean isRunning() {
        return running;
    }

    // 该画幽灵部分了
    boolean shouldDrawGhost() {
        return isRunning() && getScrollX() > mGhostStart;
    }

    private float mMaxScroll;
    private float mGhostStart;


    Path path;

    public void start() {
        running = true;
        final int textWidth = getWidth();
        final float lineWidth = getLayout().getLineWidth(0);
        final float gap = textWidth / 3.0f;
        mGhostStart = lineWidth - textWidth + gap;
//        mMaxScroll = lineWidth + gap;
        ValueAnimator animator = ValueAnimator.ofFloat(0, mMaxScroll);

        animator.setDuration((long) (mMaxScroll / 180 * 1000));
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (Float) animation.getAnimatedValue();
//                setScrollX((int) f);
                invalidate();
                if (1 == animation.getAnimatedFraction()) {
                    running = false;
                }
            }
        });
        animator.start();

        RectF r = new RectF(0, 0, lineWidth, getLayout().getHeight());
        path = new Path();
        path.addRect(r, Path.Direction.CW);
    }

}