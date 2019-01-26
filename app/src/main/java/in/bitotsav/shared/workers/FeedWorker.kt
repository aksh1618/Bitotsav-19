package `in`.bitotsav.shared.workers

import `in`.bitotsav.feed.data.FeedRepository
import `in`.bitotsav.shared.workers.FeedWorkType.valueOf
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "FeedWorker"

enum class FeedWorkType {
    FETCH_FEEDS
}

class FeedWorker(context: Context, params: WorkerParameters): Worker(context, params), KoinComponent {

    override fun doWork(): Result {

        return runBlocking {
            try {
                val type = inputData.getString("type")?.let { valueOf(it) }
                    ?: return@runBlocking Result.failure(workDataOf("Error" to "Invalid work type"))
                val latestFeedTimestamp = get<FeedRepository>().getLatestTimestamp()
                get<FeedRepository>().fetchFeedsAfterAsync(latestFeedTimestamp).await()
                return@runBlocking Result.success()
            } catch (e: Exception) {
                Log.d(TAG, e.message)
                return@runBlocking Result.retry()
            }
        }
    }
}