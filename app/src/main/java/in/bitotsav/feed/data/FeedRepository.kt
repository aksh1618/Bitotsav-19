package `in`.bitotsav.feed.data

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.feed.api.FeedService
import `in`.bitotsav.shared.Singleton
import `in`.bitotsav.shared.data.DatabaseException
import `in`.bitotsav.shared.data.Repository
import `in`.bitotsav.shared.network.NetworkException
import `in`.bitotsav.shared.utils.forEachParallel
import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val TAG = "FeedRepository"

class FeedRepository(private val feedDao: FeedDao): Repository<Feed> {
    override fun getAll(): LiveData<List<Feed>> {
        return feedDao.getAll()
    }

    @WorkerThread
    override suspend fun insert(vararg items: Feed) {
        feedDao.insert(*items)
    }

//    POST - /getFeedsAfter - body: {timestamp}
//    502 - Server error
//    200 - Array of announcements
    fun fetchFeedsAfter(timestamp: Long, context: Context): Deferred<Any> {
        return CoroutineScope(Dispatchers.IO).async {
            val body = mapOf("timestamp" to timestamp)
            val request = FeedService.api.getFeedsAfter(body)
            val response = request.await()
            if (response.code() == 200) {
                Log.d(TAG, "Feeds received after $timestamp")
                val feeds = response.body() ?: throw NetworkException("Response body is empty")
                val database = Singleton.database.getInstance(context).eventDao()
                feeds.forEachParallel {
                    if (it.eventId != null) {
                        val isStarred = EventRepository(database).isStarred(it.eventId) ?: false
                        val eventName = EventRepository(database).getEventName(it.eventId)
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