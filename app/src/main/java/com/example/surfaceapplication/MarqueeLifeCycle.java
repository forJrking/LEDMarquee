package com.example.surfaceapplication;

/**
 * @Description:
 * @Author: forjrking
 * @CreateDate: 2020/5/6 10:21
 * @Version: 1.0.0
 */
public interface MarqueeLifeCycle {
    void onPrepare();
    void onStart();
    void onPause();
    void onResume();
    void onStop();

    void onReset();
    void onRelease();
}
