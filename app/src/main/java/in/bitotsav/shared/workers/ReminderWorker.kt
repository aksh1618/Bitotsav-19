package `in`.bitotsav.shared.workers

import `in`.bitotsav.HomeActivity
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.notification.utils.Channel
import `in`.bitotsav.notification.utils.displayNotification
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.utils.cancelReminderWork
import `in`.bitotsav.shared.utils.startReminderWork
import `in`.bitotsav.shared.workers.ReminderWorkType.valueOf
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
    CHECK_UPCOMING_EVENTS,
    START_REMINDER_WORK,
    STOP_REMINDER_WORK
}

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {
    override fun doWork(): Result {
        try {
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: throw NonRetryableException("Invalid work type")
            when (type) {
                ReminderWorkType.START_REMINDER_WORK -> {
                    startReminderWork()
                    checkNotification("STARTING_REMINDER_WORK", applicationContext)
                }
                ReminderWorkType.STOP_REMINDER_WORK -> {
                    cancelReminderWork()
                    checkNotification("STOPPING_REMINDER_WORK", applicationContext)
                }
                ReminderWorkType.CHECK_UPCOMING_EVENTS -> {
                    Log.d(TAG, "Reminder work started")
                    checkNotification("RUNNING_REMINDER_WORK", applicationContext)
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
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Unknown Error", e)
            return Result.failure()
        }
    }
}

private fun checkNotification(title: String, context: Context) {
    val intent = Intent(context, HomeActivity::class.java)
    displayNotification(title, "Test content", System.currentTimeMillis(), Channel.ANNOUNCEMENT, intent, context)
}