package `in`.bitotsav.feed.data

import `in`.bitotsav.shared.data.Repository
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class FeedRepository(private val feedDao: FeedDao): Repository<Feed> {
    override fun getAll(): LiveData<List<Feed>> {
        return feedDao.getAll()
    }

    @WorkerThread
    override suspend fun insert(vararg items: Feed) {
        feedDao.insert(*items)
    }
}