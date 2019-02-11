package `in`.bitotsav.shared.data

import androidx.lifecycle.LiveData

interface Repository<T> {
    fun getAll(): LiveData<List<T>>

    suspend fun insert(vararg items: T)
}