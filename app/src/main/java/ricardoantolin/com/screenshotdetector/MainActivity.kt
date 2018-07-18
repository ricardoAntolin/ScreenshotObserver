package ricardoantolin.com.screenshotdetector

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ricardoantolin.com.screenshotdetector.service.ScreenshotObserver

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ScreenshotObserver.instance.start(this)
    }

    override fun onBackPressed() {
        if (!supportFragmentManager.popBackStackImmediate()) {
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(homeIntent)
        }
    }
}
