package com.example.surfaceapplication;

import android.util.Log;
import android.view.Choreographer;

import androidx.annotation.FloatRange;


public class MarqueeAnimation {
    private AnimationListener animationListener;
    /*** DES: 绘制路线*/
    private int fromX;
    private int fromY;
    private int toX;
    private int toY;
    /*** DES: 当前执行位置*/
    private float curentX = 0;
    private float curentY = 0;
    /*** DES: 速度 */
    private float speed = 1.0f;
    private Choreographer.FrameCallback frameCallback;
    private final Choreographer mChoreographer;
    /*** DES:停止 */
    private boolean requestStop = false;
    /*** 循环播放*/
    private boolean loopPlayback = true;
    /*** DES: 滚动类型 */
    private int displayType;

    public MarqueeAnimation() {
        mChoreographer = Choreographer.getInstance();
        frameCallback = new Choreographer.FrameCallback() {

            @Override
            public void doFrame(long frameTimeNanos) {
                if (requestStop) {
                    return;
                }
                mChoreographer.removeFrameCallback(frameCallback);
                if (!loopPlayback) {
                    if (isComplete()) {
                        Log.i("MarqueeAnimation", "播放完成");
                        return;
                    }
                }
                //计算偏移
                calPosition();
                //让画笔记录
                if (animationListener != null) {
                    animationListener.updateValue(curentX, curentY);
                }
                //通知重绘
                if (animationListener != null) {
                    animationListener.notifyAnimationUpdate();
                }
                mChoreographer.postFrameCallback(frameCallback);
            }
        };

    }

    public boolean isComplete() {
        switch (displayType) {
            case DisplayType.SCROLL_DOWN_TO_UP:
                if (curentY <= toY) {
                    return true;
                }
                return false;
            case DisplayType.SCROLL_UP_TO_DOWN:
                if (curentY >= toY) {
                    return true;
                }
                return false;
            case DisplayType.SCROLL_LEFT_TO_RIGHT:
                if (curentX >= toX) {
                    return true;
                }
                return false;
            case DisplayType.SCROLL_RIGHT_TO_LEFT:
                if (curentX <= toX) {
                    return true;
                }
                return false;
            default:
                return true;
        }
    }

    float dx;
    float dy;

    /**
     * 计算位置
     */
    private void calPosition() {
        //从左到右
        if (fromX < toX) {
            if (curentX >= toX) {
                curentX = fromX;
            }
            dx = speed;
        } else { //从右到左
            if (curentX <= toX) {
                curentX = fromX;
            }
            dx = -speed;
        }
        //从上到下
        if (fromY < toY) {
            if (curentY >= toY) {
                curentY = fromY;
            }
            dy = speed;
        } else {//从下到上
            if (curentY <= toY) {
                curentY = fromY;
            }
            dy = -speed;
        }
        transX(dx);
        transY(dy);
    }

    private void transX(float dx) {
        if (fromX != toX) {
            curentX += dx;
        }
    }

    private void transY(float dy) {
        if (fromY != toY) {
            curentY += dy;
        }
    }

    public MarqueeAnimation from(int fromX, int fromY) {
        this.fromX = fromX;
        this.fromY = fromY;
        return this;
    }

    public MarqueeAnimation to(int toX, int toY) {
        this.toX = toX;
        this.toY = toY;
        return this;
    }

    public MarqueeAnimation setAnimationListener(AnimationListener animationListener) {
        this.animationListener = animationListener;
        return this;
    }

    /**
     * @param speedPx 速度单位 px/s
     * @return 60fps 每秒60帧 则换算为 px/60s
     */
    public MarqueeAnimation setPxSpeed(float speedPx) {
        Log.i("MarqueeAnimation", " speedPx " + speedPx);
        this.speed = Math.round(speedPx / 60);
        return this;
    }

    /**
     * @param speed 单位px/帧
     */
    public void setSpeed(@FloatRange(from = 0f, to = 10f) float speed) {
        this.speed = speed;
        Log.i("MarqueeAnimation", " speed " + speed);
    }

    public void start() {
        requestStop = false;
        curentX = fromX;
        curentY = fromY;
        mChoreographer.postFrameCallback(frameCallback);
    }

    public void stop() {
        requestStop = true;
        if (frameCallback != null) {
            mChoreographer.removeFrameCallback(frameCallback);
        }
    }

    public void setLoopPlayback(boolean loopPlayback) {
        this.loopPlayback = loopPlayback;
    }

    public void setDisplayType(int displayType) {
        this.displayType = displayType;
    }
}
