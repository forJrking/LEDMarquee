package com.example.surfaceapplication;

/**
 * @Description:
 * @Author: forjrking
 * @CreateDate: 2020/5/6 10:33
 * @Version: 1.0.0
 */
public interface AnimationListener {
    void notifyAnimationUpdate();

    void updateValue(float curentX, float curentY);
}
