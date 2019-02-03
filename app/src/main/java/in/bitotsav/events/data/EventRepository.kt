package `in`.bitotsav.events.data

import `in`.bitotsav.R
import `in`.bitotsav.events.api.EventService
import `in`.bitotsav.shared.data.Repository
import `in`.bitotsav.shared.exceptions.NetworkException
import `in`.bitotsav.shared.exceptions.NonRetryableException
import `in`.bitotsav.shared.utils.forEachParallel
import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.koin.core.context.GlobalContext.get

private const val TAG = "EventRepository"

class EventRepository(private val eventDao: EventDao) : Repository<Event> {
    override fun getAll() = eventDao.getAll()

    @WorkerThread
    suspend fun getAllEvents() = eventDao.getAllEvents()

    fun getByDay(day: Int, starredOnly: Boolean = false) = when (starredOnly) {
        true -> eventDao.getStarredByDay(day)
        false -> eventDao.getByDay(day)
    }

    fun getByCategories(vararg categories: String) = eventDao.getByCategories(*categories)

    fun getByCategoriesForDay(day: Int, starredOnly: Boolean = false, vararg categories: String) = when (starredOnly) {
        true -> eventDao.getStarredByCategoriesForDay(day, *categories)
        false -> eventDao.getByCategoriesForDay(day, *categories)
    }

    @WorkerThread
    suspend fun getAllCategories() = eventDao.getAllCategories()

    @WorkerThread
    suspend fun getById(id: Int) = eventDao.getById(id)

    @WorkerThread
    suspend fun getNameById(id: Int) = eventDao.getNameById(id)

    @WorkerThread
    suspend fun isStarred(id: Int) = eventDao.isStarred(id)

    @WorkerThread
    override suspend fun insert(vararg items: Event) = eventDao.insert(*items)

    fun getEventsFromLocalJson() {
        val eventsJsonString: String =
            get().koin.get<Context>().resources.openRawResource(R.raw.events_init).bufferedReader()
                .use { it.readText() }
        val events = Gson().fromJson(eventsJsonString, Array<Event>::class.java)
        CoroutineScope(Dispatchers.IO).async {
            insert(*events)
            Log.d(TAG, "Inserted events into DB from local json file.")
        }
    }

    //    POST - /getEventById - body: {eventId}
//    502 - Server error
//    404 - Event not found
//    200 - Object containing event details
    fun fetchEventByIdAsync(eventId: Int): Deferred<Int> {
        return CoroutineScope(Dispatchers.IO).async {
            val body = mapOf("eventId" to eventId)
            val request = EventService.api.getByIdAsync(body)
            val response = request.await()
            if (response.code() == 200) {
                val event = response.body() ?: throw NetworkException("Response body is empty")
                Log.d(TAG, "Event: $eventId received from server")
                event.setProperties(isStarred(eventId) ?: false)
                insert(event)
                Log.d(TAG, "Inserted $eventId into DB")
            } else {
                when (response.code()) {
                    404 -> throw NonRetryableException("Event:$eventId not found")
                    else -> throw NetworkException(
                        "Error fetching Event:$eventId from the server." +
                                " Response code: ${response.code()}"
                    )
                }
            }
        }
    }

    //    GET - /getAllEvents
//    502 - Server error
//    200 - Array of events
    fun fetchAllEventsAsync(): Deferred<Any> {
        return CoroutineScope(Dispatchers.IO).async {
            val request = EventService.api.getAllAsync()
            val response = request.await()
            if (response.code() == 200) {
                val events = response.body() ?: throw NetworkException("Response body is empty")
                Log.d(TAG, "All Events received from the server")
                events.forEachParallel {
                    it.setProperties(isStarred(it.id) ?: false)
                }
                insert(*events.toTypedArray())
                Log.d(TAG, "Inserted all events into DB")
            } else {
                throw NetworkException("Fetch all events failed. Code: ${response.code()}")
            }
        }
    }
}