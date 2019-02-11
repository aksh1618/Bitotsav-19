package `in`.bitotsav.profile.data

import androidx.annotation.WorkerThread

private const val TAG = "UserRepository"

class UserRepository(private val userDao: UserDao) {
    fun get() = userDao.get()

    @WorkerThread
    suspend fun insert(user: User) = userDao.insert(user)

    @WorkerThread
    suspend fun delete() = userDao.delete()
}