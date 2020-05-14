package com.example.surfaceapplication

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.surfaceapplication.oth.MyTextView
import kotlinx.android.synthetic.main.activity_main_t.*

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    lateinit var marqueeTextView: MarqueeTextView
    lateinit var info: MarqueeTextPlayInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_t)
        info = MarqueeTextPlayInfo()
        info.apply {
            alignment = Alignment.MIDDLE_CENTER
            displayType = DisplayType.SCROLL_RIGHT_TO_LEFT
            letterSpacing = 20f
            lineSpacing = 1
            speed = 480f
            height = 480
            width = 1920/2
            offsetY = 0
            isHeadTail = true
            headTailSpacing = 50
            textBody = Text().apply {
                text = "新天外印刷欢迎您！共创共享 天外家园 天外印刷 诚赢天下！"
                textColor = Color.rgb(100, 0, 0)
//                textColor = Color.parseColor("#FFFF0000")
//                textBgColor = Color.parseColor("#00000000")
                textBgColor = Color.parseColor("#234566")
                fontSize = 180f
                fontTypeface = Typeface.defaultFromStyle(Typeface.BOLD)
                isStrikeout = false
                isUnderline = false
            }
        }
        marqueeTextView = MarqueeTextView(this, info)
        marqueeTextView.keepScreenOn = true
        var text = MyTextView(this)
        marqueeLayout.addView(marqueeTextView)
//        marqueeLayout.addView(text)
        text.text = "新天外印刷欢迎您！共创共享 天外家园 天外印刷 诚赢天下！"
        text.textSize = 64f
        text.maxLines = 1
        speed_seekbar.setOnSeekBarChangeListener(this)
        spacing_seekbar.setOnSeekBarChangeListener(this)


//        text.postDelayed(object : Runnable {
//            override fun run() {
//                text.start()
//            }
//
//        }, 2000)
    }


    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        if (seekBar === speed_seekbar) {
            speed_text.setText("速度：$i")
            marqueeTextView.setSpeed(i)
        } else if (seekBar === spacing_seekbar) {
            spacing_text.setText("间隔：$i")
            marqueeTextView.onStop()
            info.apply {
                isHeadTail = true
                headTailSpacing = i
            }
            marqueeTextView.setMarquee(info)
            marqueeTextView.onStart()
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onResume() {
        super.onResume()
        marqueeTextView.onStart()
    }

    override fun onPause() {
        super.onPause()
        marqueeTextView.onPause()
    }

    override fun onStop() {
        super.onStop()
        marqueeTextView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        marqueeTextView.onRelease()
    }


}
