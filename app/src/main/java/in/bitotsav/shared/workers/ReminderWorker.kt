package `in`.bitotsav.shared.workers

import `in`.bitotsav.HomeActivity
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.notification.utils.Channel
import `in`.bitotsav.notification.utils.displayNotification
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "ReminderWorker"

enum class ReminderWorkType {
    CHECK_UPCOMING_EVENTS
}

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {
    override fun doWork(): Result {
        try {
            Log.d(TAG, "Reminder work started")
            val events = runBlocking { get<EventRepository>().getAllStarred() }
            val timePeriod = 45 * 60 * 1000
            val currentTimestamp = System.currentTimeMillis()
            val futureTimestamp = currentTimestamp + timePeriod
            val intent = Intent(applicationContext, HomeActivity::class.java)
            events.forEach {
                if (it.timestamp in currentTimestamp..futureTimestamp) {
                    val timeInMinutes = (it.timestamp - currentTimestamp) / (60 * 1000)
                    displayNotification(
                        "Upcoming event!",
                        "Event ${it.name} is about to begin in $timeInMinutes minutes.",
                        currentTimestamp,
                        Channel.PM,
                        intent,
                        applicationContext
                    )
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Result.failure()
        }
    }
}