package com.example.surfaceapplication

import android.graphics.Color
import android.graphics.Typeface

/**
 * @Description:
 * @Author: forjrking
 * @CreateDate: 2020/5/6 10:35
 * @Version: 1.0.0
 */
open class MarqueeTextPlayInfo {

    var alignment: Int = Alignment.MIDDLE_LEFT
    var textBody: Text? = null
    val bgColor: Int = Color.TRANSPARENT
    /**行间距*/
    var lineSpacing: Int = 0
    /**字母间距*/
    var letterSpacing: Float = 0f
    /**运动轨迹*/
    var displayType: Int = DisplayType.SCROLL_RIGHT_TO_LEFT
    var height: Int = 0
    var width: Int = 0
    var speed: Float = 30f
    //isHeadTail	boolean	是否开启首尾相接
    var isHeadTail: Boolean = false
    //headTailSpacing	string	首尾相接字间距，单位像素
    var headTailSpacing: Int = 0
    var offsetY: Int = 0

    open inner class Text {
        @JvmField
        var isStrikeout: Boolean = false
        /**下划线*/
        @JvmField
        var isUnderline: Boolean = false
        /**字体*/
        @JvmField
        var fontTypeface: Typeface = Typeface.DEFAULT
        /**字体大小*/
        @JvmField
        var fontSize: Float = 14f
        @JvmField
        var textColor: Int = Color.RED
        @JvmField
        var textBgColor: Int = Color.TRANSPARENT

        var text: String? = null
            get() = field?.replace("\n", "")

    }
}