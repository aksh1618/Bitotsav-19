package `in`.bitotsav

import `in`.bitotsav.notification.utils.createNotificationChannels
import android.app.Application
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class Bitotsav19 : Application() {
    companion object {
        private const val TAG = "Bitotsav19"
    }
    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().subscribeToTopic("global")
            .addOnCompleteListener { task ->
                var msg = "Subscription to global successful"
                if (!task.isSuccessful) {
                    msg = "Subscription to global not successful"
                }
                Log.d(TAG, msg)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(this)
        }
    }
}