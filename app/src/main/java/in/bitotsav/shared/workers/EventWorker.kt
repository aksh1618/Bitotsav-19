package `in`.bitotsav.shared.workers

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.shared.workers.EventWorkType.*
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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

        return runBlocking {
            try {
                val type = inputData.getString("type")?.let { valueOf(it) }
                    ?: return@runBlocking Result.failure(workDataOf("Error" to "Invalid work type"))
                when (type) {
                    FETCH_ALL_EVENTS -> get<EventRepository>().fetchAllEventsAsync().await()
                    FETCH_EVENT -> {
                        val eventId = inputData.getInt("eventId", -1)
                        if (eventId == -1)
                            return@runBlocking Result.failure(workDataOf("Error" to "Event id is empty"))
                        get<EventRepository>().fetchEventByIdAsync(eventId).await()
                        return@runBlocking Result.success()
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