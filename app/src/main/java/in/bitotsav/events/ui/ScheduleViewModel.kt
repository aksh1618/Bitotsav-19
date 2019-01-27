package `in`.bitotsav.events.ui

import `in`.bitotsav.events.data.Event
import `in`.bitotsav.events.data.EventRepository
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class ScheduleViewModel(private val eventRepository: EventRepository) : ViewModel() {

    companion object {
        val ALL_CATEGORIES = listOf("all")
    }

    private val visibleEventsList = MediatorLiveData<List<Event>>()
    private val categories = MutableLiveData<List<String>>()

    init {
        categories.value = ALL_CATEGORIES
        val liveEventsList = Transformations.switchMap(categories) {
            when (it) {
                ALL_CATEGORIES -> eventRepository.getAll()
                else -> eventRepository.getByCategories(*it.toTypedArray())
            }
        }
        visibleEventsList.addSource(liveEventsList, visibleEventsList::setValue)
    }

    fun getVisibleEvents() = visibleEventsList

    fun filterByCategories(vararg categories: String) {
        this.categories.value = categories.asList()
    }

    fun clearFilter() {
        categories.value = ALL_CATEGORIES
    }

    fun isFilterActive() = categories.value != ALL_CATEGORIES

//    private val parentJob = Job()
//    private val coroutineContext: CoroutineContext
//        get() = parentJob + Dispatchers.Main
//    private val scope = CoroutineScope(coroutineContext)
//    override fun onCleared() {
//        super.onCleared()
//        parentJob.cancel()
//    }
}

