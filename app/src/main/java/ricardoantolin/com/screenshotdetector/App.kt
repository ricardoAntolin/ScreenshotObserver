package ricardoantolin.com.screenshotdetector

import android.app.Application
import android.content.Intent
import ricardoantolin.com.screenshotdetector.service.ScreenShotObserverService


class App: Application() {

    override fun onCreate() {
        super.onCreate()
        startService(Intent(this, ScreenShotObserverService::class.java))
    }
}