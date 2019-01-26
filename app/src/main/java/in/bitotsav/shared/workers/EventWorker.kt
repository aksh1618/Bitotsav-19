package `in`.bitotsav.shared.workers

import `in`.bitotsav.events.data.EventRepository
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

class EventWorker(context: Context, params: WorkerParameters): Worker(context, params), KoinComponent {

    override fun doWork(): Result {
        val type = valueOf(inputData.getString("type")!!)

        return runBlocking {
            try {
                when (type) {
                    FETCH_ALL_EVENTS -> get<EventRepository>().fetchAllEventsAsync().await()
                    FETCH_EVENT -> {
                        val eventId = inputData.getInt("eventId", 1)
                        get<EventRepository>().fetchEventByIdAsync(eventId).await()
                    }
                }
                return@runBlocking Result.success()
            } catch (e: Exception) {
                Log.d(TAG, e.message)
                return@runBlocking Result.retry()
            }
        }
    }
}