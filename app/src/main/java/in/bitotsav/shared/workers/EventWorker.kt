package `in`.bitotsav.shared.workers

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.workers.EventWorkType.*
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "EventWorker"

enum class EventWorkType {
    FETCH_EVENT,
    FETCH_ALL_EVENTS
}

class EventWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    override fun doWork(): Result {
        try {
            val type = inputData.getString("type")?.let { valueOf(it) }
                ?: throw NonRetryableException("Invalid work type")
            when (type) {
                FETCH_ALL_EVENTS -> runBlocking { get<EventRepository>().fetchAllEventsAsync().await() }
                FETCH_EVENT -> {
                    val eventId = inputData.getInt("eventId", -1)
                    if (eventId == -1)
                        throw NonRetryableException("Event id is empty")
                    runBlocking { get<EventRepository>().fetchEventByIdAsync(eventId).await() }
                    return Result.success()
                }
            }
            return Result.success()
        } catch (e: NonRetryableException) {
            Log.d(TAG, e.message ?: "Non-retryable exception")
            return Result.failure()
        } catch (e: Exception) {
            Log.d(TAG, e.message ?: "Unknown Error")
            return Result.retry()
        }
    }
}