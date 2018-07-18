package ricardoantolin.com.screenshotdetector.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import io.reactivex.observers.DisposableObserver
import org.jetbrains.anko.runOnUiThread
import ricardoantolin.com.screenshotdetector.R


class ScreenShotObserverService : Service() {
    inner class PushBinder : Binder() {
        internal val getNotificationService: ScreenShotObserverService
            get() = this@ScreenShotObserverService
    }

    override fun onCreate() {

        startForeground(1, createNotification(applicationContext))


        ScreenshotObserver.instance.observer.subscribeWith(disposableObserver())
    }

    private fun createNotification(context: Context): Notification {
        val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    "1",
                    "ScreenshotObserver",
                    NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(notificationChannel)
            NotificationCompat.Builder(context, notificationChannel.id)
        } else {
            NotificationCompat.Builder(context, "")
        }
                .setContentTitle("Screenshot Observer")
                .setContentText("Screenshot Observer ")
                .setColor(Color.parseColor("#ffffff"))
                .setGroupSummary(true)
                .setGroup("Group")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCategory("category")
                .build()
    }

    private fun disposableObserver() = object : DisposableObserver<String>() {
        override fun onComplete() {

        }

        override fun onNext(t: String) {
            runOnUiThread {
                Toast.makeText(applicationContext, "New screenshot", Toast.LENGTH_LONG).show()
            }
        }

        override fun onError(e: Throwable) {

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = PushBinder()

}
