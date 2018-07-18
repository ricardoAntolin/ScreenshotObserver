package ricardoantolin.com.screenshotdetector.service

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.subjects.PublishSubject


class ScreenshotObserver private constructor() {
    val observer: PublishSubject<String> = PublishSubject.create<String>()


    fun start(activity: Activity) {
        RxPermissions(activity)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe({ granted ->
                    if (granted) {
                        startAfterPermissionGranted(activity)
                    }
                }, {
                    Log.d(TAG,"Permissions not granted")
                })
    }

    private fun startAfterPermissionGranted(context: Context) {
        val contentResolver = context.contentResolver
        val contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean, uri: Uri) {
                Log.d(TAG, "onChange: " + selfChange + ", " + uri.toString())
                if (uri.toString().startsWith(EXTERNAL_CONTENT_URI_MATCHER) ||
                        uri.toString().startsWith(INTERNAL_CONTENT_URI_MATCHER)) {
                    checkIfIsScreenshot(contentResolver, uri)
                }
                super.onChange(selfChange, uri)
            }
        }
        contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver)

        observer.doOnComplete { contentResolver.unregisterContentObserver(contentObserver) }
    }

    @Synchronized
    private fun checkIfIsScreenshot(contentResolver: ContentResolver, uri: Uri) {
        val cursor: Cursor? = contentResolver.query(uri, PROJECTION, null, null,
                SORT_ORDER)
        try {
            if (cursor?.moveToFirst() == true) {
                val path = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                val dateAdded = cursor.getLong(cursor.getColumnIndex(
                        MediaStore.Images.Media.DATE_ADDED))
                val currentTime = System.currentTimeMillis() / 1000
                Log.d(TAG, "path: " + path + ", dateAdded: " + dateAdded +
                        ", currentTime: " + currentTime)
                if (matchPath(path) && matchTime(currentTime, dateAdded)) {
                    observer.onNext(path)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "open cursor fail")
        } finally {
            cursor?.close()
        }
    }

    private fun matchPath(path: String): Boolean {
        return path.toLowerCase().contains("screenshot")
    }

    private fun matchTime(currentTime: Long, dateAdded: Long): Boolean {
        return Math.abs(currentTime - dateAdded) <= DEFAULT_DETECT_WINDOW_SECONDS
    }

    companion object {

        private const val TAG = "ScreenshotObserver"
        private val EXTERNAL_CONTENT_URI_MATCHER = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()
        private val INTERNAL_CONTENT_URI_MATCHER = MediaStore.Images.Media.INTERNAL_CONTENT_URI.toString()
        private val PROJECTION = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED
        )
        private const val SORT_ORDER = MediaStore.Images.Media.DATE_ADDED + " DESC"
        private const val DEFAULT_DETECT_WINDOW_SECONDS: Long = 10

        val instance: ScreenshotObserver by lazy { ScreenshotObserver() }

    }
}

