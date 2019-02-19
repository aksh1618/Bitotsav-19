package `in`.bitotsav.feed.data

import `in`.bitotsav.R
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.feed.api.FeedService
import `in`.bitotsav.shared.data.Repository
import `in`.bitotsav.shared.data.getJsonStringFromFile
import `in`.bitotsav.shared.exceptions.DatabaseException
import `in`.bitotsav.shared.exceptions.NetworkException
import `in`.bitotsav.shared.utils.forEachParallel
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "FeedRepository"

class FeedRepository(private val feedDao: FeedDao) : Repository<Feed>, KoinComponent {
    override fun getAll(): LiveData<List<Feed>> {
        return feedDao.getAll()
    }

    suspend fun getLatestTimestamp(): Long {
        return feedDao.getLatestTimestamp() ?: 0
    }

    @WorkerThread
    override suspend fun insert(vararg items: Feed) {
        feedDao.insert(*items)
    }

    fun getFeedsFromLocalJson() {
        val feedsJsonString = getJsonStringFromFile(R.raw.feeds)
        val listOfFeeds = Gson().fromJson(feedsJsonString, Array<Feed>::class.java).toList()
        CoroutineScope(Dispatchers.IO).async {
            listOfFeeds.forEachParallel {
                val isStarred = false
                val eventName = it.eventId?.let { id ->
                    get<EventRepository>().getNameById(id)
                        ?: throw DatabaseException("Event name not found for $id")
                }
                it.setProperties(isStarred, eventName)
            }
            insert(*listOfFeeds.toTypedArray())
            Log.d(TAG, "Inserted feeds into DB from local json file.")
        }
    }

    //    POST - /getFeedsAfter - body: {timestamp}
//    502 - Server error
//    200 - Array of announcements
    fun fetchFeedsAfterAsync(timestamp: Long): Deferred<Any> {
        return CoroutineScope(Dispatchers.IO).async {
            val body = mapOf("timestamp" to timestamp)
            val request = FeedService.api.getFeedsAfterAsync(body)
            val response = request.await()
            if (response.code() == 200) {
                Log.d(TAG, "Feeds received after $timestamp")
                val feeds = response.body() ?: throw NetworkException("Response body is empty")
                feeds.forEachParallel {
                    if (it.eventId != null) {
//                        if (FeedType.RESULT == FeedType.valueOf(it.type)) {
//                            val eventWork = getWork<EventWorker>(
//                                workDataOf("type" to EventWorkType.FETCH_EVENT.name, "eventId" to it.eventId)
//                            )
//                            val resultWork = getWork<ResultWorker>(
//                                workDataOf("type" to ResultWorkType.RESULT.name, "eventId" to it.eventId)
//                            )
//                            WorkManager.getInstance().beginUniqueWork(
//                                getWorkNameForResultWorker(ResultWorkType.RESULT, it.eventId),
//                                ExistingWorkPolicy.REPLACE,
//                                eventWork
//                            ).then(resultWork).enqueue()
//                        }
                        val isStarred = get<EventRepository>().isStarred(it.eventId) ?: false
                        val eventName = get<EventRepository>().getNameById(it.eventId)
                            ?: throw DatabaseException("Event name not found for ${it.eventId}")
                        it.setProperties(isStarred, eventName)
                    } else {
                        it.setProperties(false, null)
                    }
                }
                insert(*feeds.toTypedArray())
                Log.d(TAG, "Feeds after $timestamp inserted in DB")
            } else {
                throw NetworkException("Error fetching feeds after $timestamp. Response code: ${response.code()}")
            }
        }
    }
}