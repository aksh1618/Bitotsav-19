package `in`.bitotsav.events.data

import `in`.bitotsav.shared.data.Repository
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

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
}