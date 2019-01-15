package `in`.bitotsav

import `in`.bitotsav.utils.createNotificationChannels
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging.getInstance

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // [Init Block start]
//        TODO("Execute this on startup only")
        // Subscribe to global fcm topic
        getInstance().subscribeToTopic("global")
            .addOnCompleteListener { task ->
                var msg = "Subscription to global successful"
                if (!task.isSuccessful) {
                    msg = "Subscription to global not successful"
                }
                Log.d(TAG, msg)
//                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // TODO("Thread it")
            createNotificationChannels(this)
        }
        // [Init block end]
    }
}
