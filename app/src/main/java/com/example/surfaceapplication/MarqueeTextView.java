package com.example.surfaceapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;


public class MarqueeTextView extends View implements MarqueeLifeCycle {

    private MarqueeTextPlayInfo playInfo;
    private MarqueeAnimation animation;

    private float curentY;
    private float curentX;
    private float offsetY;
    private int textWidth;
    private int textHeight;
    private float startY;
    private boolean isAppend;
    /*** DES: 首位相连的间距*/
    private int space;
    private StaticLayout staticLayout;
    /*** DES: 文字背景色画笔*/
    private Paint highlightPaint;
    /*** DES: 文字背景色*/
    private Path path;

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, MarqueeTextPlayInfo marqueeTextPlayInfo) {
        super(context);
        setMarquee(marqueeTextPlayInfo);
    }

    private void initBackground() {
        int textBgColor = playInfo.getTextBody().textBgColor;
        if (textBgColor != Color.TRANSPARENT) {
            highlightPaint = new Paint();
            // DES: 文字背景色
            highlightPaint.setColor(textBgColor);
            highlightPaint.setStyle(Paint.Style.FILL);
        }
        // DES: 窗口整体背景色
        setBackgroundColor(playInfo.getBgColor());
    }

    public void setMarquee(MarqueeTextPlayInfo playInfo) {
        if (null == playInfo) {
            return;
        }
        this.playInfo = playInfo;
        setLayoutParams(new LinearLayout.LayoutParams(playInfo.getWidth(), playInfo.getHeight()));
        // DES: 这里设置背景色只需要绘制一次背景
        initBackground();
        initStaticLayout();
        // DES: 如果是动态才需要
        initAnimation();
    }

    public void initStaticLayout() {

        isAppend = playInfo.isHeadTail() && !isVertical();
        space = playInfo.getHeadTailSpacing();

        MarqueeTextPlayInfo.Text textItem = playInfo.getTextBody();
        if (null != textItem) {
            CharSequence strText = textItem.getText();
            // DES: 文字画笔
            TextPaint paint = new TextPaint();
//            paint.setAntiAlias(true);
            paint.setDither(true);
            // DES: 设置属性 或者用 spanned
            paint.setColor(textItem.textColor);
            paint.setTextSize(textItem.fontSize);
            paint.setTypeface(textItem.fontTypeface);
            paint.setStyle(Paint.Style.STROKE);
            // DES: 额外属性
            int flag = paint.getFlags();
            if (textItem.isUnderline) {
                flag |= Paint.UNDERLINE_TEXT_FLAG;
            }
            if (textItem.isStrikeout) {
                flag |= Paint.STRIKE_THRU_TEXT_FLAG;
            }
            paint.setFlags(flag);
            // DES: 行间距和字间距
            float textSize = textItem.fontSize;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && textSize > 0) {
                paint.setLetterSpacing(playInfo.getLetterSpacing() / textSize);
            }
            float lineSpacing = 1f;
            if (textSize > 0) {
                lineSpacing = 1 + (playInfo.getLineSpacing() / textSize);
            }
            int width = playInfo.getWidth();
            if (!isVertical()) {
                // DES: 真实的每行文字宽度
                float rtw = paint.measureText(strText, 0, strText.length());
                if (isAppend && width > rtw) {
                    int ceil = (int) Math.ceil(width / (rtw + space));
                    Log.d("Marquee", "文字:" + rtw + ", 宽度:" + width + ", 补充:" + ceil);
                    strText = getCharSequence(strText, ceil);
                    rtw = ceil * (rtw + space) - space;
                    Log.d("Marquee", "文字:" + rtw);
                    //把画笔置空让背景色可以绘制
                    highlightPaint = null;
                }
                this.textWidth = (int) (rtw + .5f);
            } else {
                strText = getCharSequence(strText, 1);
                //换行问题
                this.textWidth = width + 1;
                highlightPaint = null;
            }
            Layout.Alignment align = getAlign(playInfo.getAlignment());
            staticLayout = new StaticLayout(strText, paint, textWidth, align, lineSpacing, 0f, false);
            textHeight = staticLayout.getHeight();
            // DES: 文字根据设置的居中等属性进行偏移计算
            startY = getStartY(playInfo.getAlignment(), textHeight, playInfo.getHeight(), isVertical());
            offsetY = playInfo.getOffsetY();
            //文字区域
            if (highlightPaint != null) {
                RectF r = new RectF(0, 0, textWidth, textHeight);
                path = new Path();
                path.addRect(r, Path.Direction.CW);
            } else {
                path = null;
            }
            Log.d("Marquee", "textH:" + textHeight + ",textW:" + textWidth
                    + ",offsetY:" + offsetY
                    + ",startY:" + startY
            );
        }
    }

    /**
     * DES: 为特殊需求文字绘制背景色
     * TIME: 2020/5/8 20:28
     **/
    private CharSequence getCharSequence(CharSequence strText, int ceil) {
        SpanUtils span = new SpanUtils();
        for (int i = 0; i < ceil; i++) {
            span.append(strText).setBackgroundColor(playInfo.getTextBody().textBgColor);
            if (i < ceil - 1) {
                span.appendSpace(space);
            }
        }
        return span.create();
    }


    private void initAnimation() {
        animation = new MarqueeAnimation()
                .setPxSpeed(playInfo.getSpeed())
                .setAnimationListener(new AnimationListener() {
                    @Override
                    public void notifyAnimationUpdate() {
                        invalidate();
                    }

                    @Override
                    public void updateValue(float curentX, float curentY) {
                        setPosition(curentX, curentY);
                    }
                });

        animation.setDisplayType(playInfo.getDisplayType());
        animation.setLoopPlayback(true);
        setAnimTrack(textWidth, textHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (staticLayout == null || (highlightPaint == null && path != null)) {
            return;
        }
        canvas.translate(curentX, curentY + offsetY);
        //背景色加文字
        staticLayout.draw(canvas, path, highlightPaint, 0);
        if (isAppend) {
            //绘制首尾相连
            if (shouldDrawGhost()) {
                // DES: 原来移动过基础上再次移动位置
                canvas.translate(mMaxScroll, 0);
                staticLayout.draw(canvas, path, highlightPaint, 0);
            }
        }

    }

    /**
     * 是否纵向运动
     */
    public boolean isVertical() {
        int displayType = playInfo.getDisplayType();
        return displayType == DisplayType.SCROLL_DOWN_TO_UP || displayType == DisplayType.SCROLL_UP_TO_DOWN
                || displayType == DisplayType.SCROLL_STATIC;
    }

    public void setPosition(float curentX, float curentY) {
        this.curentX = curentX;
        this.curentY = curentY + startY;
    }

    private static float getStartY(int align, int textHeight, int height, boolean vertical) {
        float startY = 0;
        switch (align) {
            case Alignment.TOP_LEFT:
            case Alignment.TOP_CENTER:
            case Alignment.TOP_RIGHT:
                startY = 0;
                break;
            case Alignment.MIDDLE_LEFT:
            case Alignment.MIDDLE_CENTER:
            case Alignment.MIDDLE_RIGHT:
                startY = (height - textHeight) / 2f;
                break;
            case Alignment.BOTTOM_LEFT:
            case Alignment.BOTTOM_CENTER:
            case Alignment.BOTTOM_RIGHT:
                startY = (height - textHeight);
                break;
            default:
        }
        if (vertical) {
            startY = 0;
        }
        return startY;
    }

    private float mMaxScroll;
    private float mGhostStart;

    private boolean shouldDrawGhost() {
        // DES: 目前仅支持左右
        float curentX = mGhostStart == space ? this.curentX : Math.abs(this.curentX);
        return curentX > mGhostStart;
    }

    /**
     * DES: 动画路径
     * TIME: 2020/5/8 9:26
     **/
    public void setAnimTrack(int textWidth, int textHeight) {
        int viewWidth = playInfo.getWidth();
        int viewHeight = playInfo.getHeight();

        final float gap = space;
        Log.i("Marquee", String.format("viewWidth:%s ,viewHeight:%s ,text W:%s ,text H:%s ,", viewWidth, viewHeight, textWidth, textHeight));
        switch (playInfo.getDisplayType()) {
            case DisplayType.SCROLL_DOWN_TO_UP:
                animation.from(0, viewHeight)
                        .to(0, -textHeight);
                break;
            case DisplayType.SCROLL_UP_TO_DOWN:
                animation.from(0, -textHeight)
                        .to(0, viewHeight);
                break;
            case DisplayType.SCROLL_LEFT_TO_RIGHT:
                if (isAppend) {
                    mGhostStart = gap;
                    mMaxScroll = -(textWidth + gap);
                    animation.from(-(textWidth - viewWidth), 0)
                            .to((viewWidth + space), 0);
                } else {
                    animation.from(-textWidth, 0)
                            .to((viewWidth), 0);
                }
                break;
            case DisplayType.SCROLL_RIGHT_TO_LEFT:
                if (isAppend) {
                    mGhostStart = textWidth - viewWidth + gap;
                    mMaxScroll = textWidth + gap;
                    animation.from(0, 0)
                            .to(-(textWidth + space), 0);
                } else {
                    animation.from(viewWidth, 0)
                            .to(-(textWidth), 0);
                }
                break;
            default:
                //静止画面绘制一次即可 不需要重复绘制了
                animation.from(0, 0)
                        .to(0, 0);
                animation.setLoopPlayback(false);
                break;
        }
    }

    /**
     * DES: 获取文字对齐属性
     * TIME: 2020/5/6 14:23
     **/
    public static Layout.Alignment getAlign(int alignment) {
        Layout.Alignment gravity;
        switch (alignment) {
            case Alignment.TOP_CENTER:
            case Alignment.MIDDLE_CENTER:
            case Alignment.BOTTOM_CENTER:
                gravity = Layout.Alignment.ALIGN_CENTER;
                break;
            case Alignment.TOP_RIGHT:
            case Alignment.MIDDLE_RIGHT:
            case Alignment.BOTTOM_RIGHT:
                gravity = Layout.Alignment.ALIGN_OPPOSITE;
                break;
            default:
                gravity = Layout.Alignment.ALIGN_NORMAL;
                break;
        }
        return gravity;
    }


    @Override
    public void onPrepare() {

    }

    @Override
    public void onStart() {
        if (null != animation) {
            animation.start();
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStop() {
        if (animation != null) {
            animation.stop();
            animation = null;
        }
    }

    @Override
    public void onReset() {

    }

    @Override
    public void onRelease() {
        if (animation != null) {
            animation.stop();
            animation = null;
        }
    }

    public void setSpeed(int i) {
        if (animation != null) {
            animation.setSpeed(i);
        }
    }

}

