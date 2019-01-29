package `in`.bitotsav.events.ui

import `in`.bitotsav.events.data.Event
import `in`.bitotsav.events.data.EventRepository
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ScheduleViewModel(private val eventRepository: EventRepository) : ViewModel() {

    companion object {
        val ALL_CATEGORIES = emptyList<String>()
        const val DAYS = 3
    }

    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private val _showStarredOnly = MutableLiveData<Boolean>()
    private val _toastMessage = MutableLiveData<String>()
    private val _isSheetVisible = MutableLiveData<Boolean>()
    private val categoriesToShow = MutableLiveData<List<String>>()
    private val dayWiseVisibleEventsListArray = Array<MediatorLiveData<List<Event>>>(DAYS) { MediatorLiveData() }
    private val scheduleFiltersList = MutableLiveData<List<ScheduleFilter>>()

    val starredOnly: LiveData<Boolean>
        get() = _showStarredOnly
    val toastMessage: LiveData<String>
        get() = _toastMessage
    val isSheetVisible: LiveData<Boolean>
        get() = _isSheetVisible
    // WARN: This might fail due to absence of custom getter.
    val dayWiseEventsArray = Array<LiveData<List<Event>>>(DAYS) { dayWiseVisibleEventsListArray[it] }
    val filters: LiveData<List<ScheduleFilter>>
        get() = scheduleFiltersList
    lateinit var filterColors: List<Int>
    lateinit var allCategories: List<String>

    init {
        _showStarredOnly.value = false
        _toastMessage.value = ""
        categoriesToShow.value = ALL_CATEGORIES
        _isSheetVisible.value = false

        scope.launch(Dispatchers.IO) {
            // TODO: Get from resources?
            allCategories = eventRepository.getAllCategories()
            // The events might not have been fetched in case of first run.
            while (allCategories.isEmpty()) {
                Log.i("allCategories fetch", "Retrying fetching categories after a half second delay...")
                delay(500)
                allCategories = eventRepository.getAllCategories()
            }
        }.invokeOnCompletion {
            scope.launch(Dispatchers.Main) { refreshScheduleFilterList() }
        }

        (1..DAYS).forEach { day ->
            val liveEventsList = Transformations.switchMap(
                DoubleTriggerLiveData(categoriesToShow, _showStarredOnly)
            ) { (categories, starredOnly) ->
                if (starredOnly == null || categories == null)
                    throw IllegalStateException("Categories and/or starred are null, somehow")
                when (categories) {
                    ALL_CATEGORIES -> eventRepository.getByDay(day, starredOnly)
                    // TODO: May need to be sorted by time
                    else -> eventRepository.getByCategoriesForDay(day, starredOnly, *categories.toTypedArray())
                }
            }
            dayWiseVisibleEventsListArray[day - 1].addSource(
                liveEventsList,
                dayWiseVisibleEventsListArray[day - 1]::setValue
            )
        }
    }

    private fun refreshScheduleFilterList() {
        scheduleFiltersList.value = allCategories.map { category ->
            ScheduleFilter(category, filterColors[0], categoriesToShow.value?.contains(category) ?: false)
        }
    }

    fun toggleStarredFilter() {
        _showStarredOnly.value = _showStarredOnly.value?.not()
    }

    fun toggleCategoryFilter(category: String) {
        categoriesToShow.value?.let {
            categoriesToShow.value = when (category in it) {
                true -> removeCategoryFromList(it, category)
                false -> addCategoryToList(it, category)
            }
        }
        refreshScheduleFilterList()
    }

    private fun addCategoryToList(categoryList: List<String>, category: String) =
        categoryList.toMutableList()
            .apply { add(category); areFiltersActive.set(true) }
            .toList()

    private fun removeCategoryFromList(categoryList: List<String>, category: String) =
        categoryList
            .toMutableList()
            .apply { remove(category) }
            .toList()
            .ifEmpty { areFiltersActive.set(false); ALL_CATEGORIES }

    fun clearFilters(): Boolean {
        categoriesToShow.value = ALL_CATEGORIES
        refreshScheduleFilterList()
        areFiltersActive.set(false)
        _showStarredOnly.value = false
//        toast("Filters cleared")
        return true
    }

    fun toast(message: String) {
        _toastMessage.value = message
        _toastMessage.value = ""
    }

    fun showFiltersSheet() {
        _isSheetVisible.value = true
    }

    fun hideFiltersSheet() {
        _isSheetVisible.value = false
    }

    val areFiltersActive = ObservableBoolean(categoriesToShow.value != ALL_CATEGORIES)

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}

class DoubleTriggerLiveData<S, T>(s: LiveData<S>, t: LiveData<T>) : MediatorLiveData<Pair<S?, T?>>() {
    init {
        addSource(s) { value = it to t.value }
        addSource(t) { value = s.value to it }
    }
}
