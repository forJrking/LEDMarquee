package com.example.surfaceapplication

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.surfaceapplication.oth.MyTextView
import com.example.surfaceapplication.pic.ImageLoader
import kotlinx.android.synthetic.main.activity_main_t.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


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
            width = 1920 / 2
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

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            val dir = Environment.getExternalStorageDirectory()
            Thread(Runnable {
                val broadSearch = scanPicture(dir)
                broadSearch.forEach {
                    System.out.println(it)
                }
            }).start()
        }

        var str ="/sdcard/nova/viplex_terminal/media/default_colorful_text_texture.png"

        ImageLoader.getInstance().loadImage(str,image)
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

    fun scanPicture(files: File): ArrayList<String> {
        val arrayList = ArrayList<String>()
        val myQueue: Queue<File> = LinkedList()
        myQueue.add(files)
        while (!myQueue.isEmpty()) {
            val node: File = myQueue.poll()
            if (node.isDirectory) {
                val listFiles = node?.listFiles()
                if (listFiles != null) {
                    myQueue.addAll(listFiles)
                }
            } else if (node.isFile && isPicture(node.name)) {
                arrayList.add(node.absolutePath)
            }
        }
        return arrayList
    }

    fun isPicture(path: String): Boolean {
        return path.endsWith(".bmp", true)
                || path.endsWith(".jpg", true)
                || path.endsWith(".jpeg", true)
                || path.endsWith(".png", true)
    }

}
