package `in`.bitotsav.shared.workers

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.notification.utils.Channel
import `in`.bitotsav.notification.utils.displayNotification
import `in`.bitotsav.notification.utils.getEventDetailPendingIntent
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.utils.cancelReminderWork
import `in`.bitotsav.shared.utils.isBitotsavOver
import `in`.bitotsav.shared.workers.ReminderWorkType.valueOf
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get
import java.util.*

private const val TAG = "ReminderWorker"

enum class ReminderWorkType {
    CHECK_UPCOMING_EVENTS
}

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params),
    KoinComponent {
    override fun doWork(): Result {
        try {
            if (isBitotsavOver())
                return Result.success()
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: throw NonRetryableException("Invalid work type")
            when (type) {
                ReminderWorkType.CHECK_UPCOMING_EVENTS -> {
                    Log.d(TAG, "Reminder work running")
                    val events = runBlocking { get<EventRepository>().getAllStarred() }
                    val timePeriod = 45 * 60 * 1000
                    val currentTimestamp = System.currentTimeMillis()
                    val futureTimestamp = currentTimestamp + timePeriod
                    events.forEach {
                        if (it.timestamp in currentTimestamp..futureTimestamp) {
                            val timeInMinutes = (it.timestamp - currentTimestamp) / (60 * 1000)
                            displayNotification(
                                "Upcoming event alert!",
                                "${it.name} is about to start in $timeInMinutes minutes.",
                                currentTimestamp,
                                Channel.STARRED,
                                getEventDetailPendingIntent(applicationContext, it.id),
                                applicationContext
                            )
                        }
                    }
                    val calendar = GregorianCalendar(TimeZone.getTimeZone("Asia/Kolkata"))
                    calendar.set(2019, 1, 17, 20, 0)
                    val endOfBitotsav = calendar.timeInMillis
                    if (currentTimestamp > endOfBitotsav) {
                        cancelReminderWork()
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Unknown Error", e)
            return Result.failure()
        }
    }
}