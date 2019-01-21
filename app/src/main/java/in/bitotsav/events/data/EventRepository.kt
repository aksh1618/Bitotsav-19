package `in`.bitotsav.events.data

import `in`.bitotsav.events.api.EventService
import `in`.bitotsav.shared.data.Repository
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

private const val TAG = "EventRepository"

class EventRepository(private val eventDao: EventDao) : Repository<Event> {
    override fun getAll(): LiveData<List<Event>> {
        return eventDao.getAll()
    }

    @WorkerThread
    suspend fun getById(id: Int): Event? {
        return eventDao.getById(id)
    }

    @WorkerThread
    suspend fun getEventName(id: Int): String? {
        return eventDao.getEventName(id)
    }

    @WorkerThread
    suspend fun isStarred(id: Int): Boolean? {
        return eventDao.isStarred(id)
    }

    @WorkerThread
    override suspend fun insert(vararg items: Event) {
        eventDao.insert(*items)
    }

    fun fetchEventById(eventId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = EventService.api.getById(mapOf("eventId" to eventId))
            try {
                val response = request.await()
                if (response.code() != 200) {
                    if (response.code() == 404) {
                        throw Exception("Event: $eventId not found")
                    } else {
                        throw Exception("Error fetching $eventId from the server")
                    }
                }
                val event = response.body() ?: throw Exception("Request body is null")
                event.setProperties(isStarred(eventId))
                insert(event)
                Log.d(TAG, "Inserted $eventId into DB")
            } catch (e: HttpException) {
                Log.e(TAG, e.message)
            } catch (e: Throwable) {
                Log.e(TAG, e.message)
            }
        }
    }

    fun fetchAllEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = EventService.api.getAll()
            try {
                val response = request.await()
                if (response.code() != 200) {
                    throw Exception("Fetch all events failed")
                }
                val events = response.body() ?: throw Exception("Request body is null")
                events.forEach {
                    it.setProperties(isStarred(it.id))
                }
                insert(*events.toTypedArray())
                Log.d(TAG, "Inserted all events into DB")
            } catch (e: HttpException) {
                Log.e(TAG, e.message)
            } catch (e: Throwable) {
                Log.e(TAG, e.message)
            }
        }
    }
}