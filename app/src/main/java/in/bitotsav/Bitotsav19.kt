package `in`.bitotsav

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.koin.repositoriesModule
import `in`.bitotsav.koin.retrofitModule
import `in`.bitotsav.koin.sharedPrefsModule
import `in`.bitotsav.koin.viewModelsModule
import `in`.bitotsav.notification.utils.createNotificationChannels
import `in`.bitotsav.shared.network.scheduleWork
import `in`.bitotsav.shared.workers.EventWorkType
import `in`.bitotsav.shared.workers.EventWorker
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

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

        startKoin {
            androidContext(this@Bitotsav19)
            // Enable logging, log.INFO by default
            androidLogger()
            // Use properties from assets/koin.properties
            androidFileProperties()
            modules(repositoriesModule, retrofitModule, viewModelsModule, sharedPrefsModule)
        }

        get<EventRepository>().getEventsFromLocalJson()
        // TODO: Remove this.
        scheduleWork<EventWorker>(workDataOf("type" to EventWorkType.FETCH_ALL_EVENTS.name))
    }
}