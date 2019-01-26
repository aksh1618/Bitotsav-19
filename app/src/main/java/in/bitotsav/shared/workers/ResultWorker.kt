package `in`.bitotsav.shared.workers

import `in`.bitotsav.events.data.EventRepository
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "ResultWorker"

enum class ResultWorkType {
    RESULT
}

class ResultWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    override fun doWork(): Result {

        return runBlocking {
            try {
                val eventId = inputData.getInt("eventId", -1)
                if (eventId == -1)
                    return@runBlocking Result.failure(workDataOf("Error" to "Event id is empty"))
                val event = get<EventRepository>().getById(eventId)
//                TODO("Check winner here and update or fetch teams")
                Log.d(TAG, "Analysing winners for event: $eventId")
                return@runBlocking Result.success()
            } catch (e: Exception) {
                Log.d(TAG, e.message)
                return@runBlocking Result.retry()
            }
        }
    }
}