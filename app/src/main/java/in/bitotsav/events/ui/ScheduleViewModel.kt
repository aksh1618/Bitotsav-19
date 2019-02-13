package `in`.bitotsav.events.ui

import `in`.bitotsav.events.data.Event
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import `in`.bitotsav.shared.ui.BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ScheduleViewModel(
    private val eventRepository: EventRepository
) : BaseViewModel() {

    companion object {
        val ALL_CATEGORIES = emptyList<String>()
        const val DAYS = 3
    }

    private val _showStarredOnly = MutableLiveData<Boolean>()
    private val _isSheetVisible = MutableLiveData<Boolean>()
    private val categoriesToShow = MutableLiveData<List<String>>()
    private val dayWiseVisibleEventsListArray =
        Array<MediatorLiveData<List<Event>>>(DAYS) { MediatorLiveData() }
    private val scheduleFiltersList = MutableLiveData<List<ScheduleFilter>>()

    val starredOnly: LiveData<Boolean>
        get() = _showStarredOnly
    val isSheetVisible: LiveData<Boolean>
        get() = _isSheetVisible
    // WARN: This might fail due to absence of custom getter.
    val dayWiseEventsArray
        get() = Array<LiveData<List<Event>>>(DAYS) { dayWiseVisibleEventsListArray[it] }
    val filters: LiveData<List<ScheduleFilter>>
        get() = scheduleFiltersList
    lateinit var filterColors: IntArray
    private lateinit var allCategories: List<String>
    // TODO: Use switch map with double trigger for this
    val areFiltersActive = MutableLiveData<Boolean>()
    val filterFabVisible = NonNullMutableLiveData(true)

    init {
        _showStarredOnly.value = false
        areFiltersActive.value = false
        categoriesToShow.value = ALL_CATEGORIES
        _isSheetVisible.value = false

        (1..DAYS).forEach { day ->
            val liveEventsList = Transformations.switchMap(
                DoubleTriggerLiveData(categoriesToShow, _showStarredOnly)
            ) { (categories, starredOnly) ->
                if (starredOnly == null || categories == null)
                    throw IllegalStateException("Categories and/or starred are null, somehow")
                when (categories) {
                    ALL_CATEGORIES -> eventRepository.getByDay(day, starredOnly)
                    // TODO: May need to be sorted by time
                    else -> eventRepository.getByCategoriesForDay(
                        day,
                        starredOnly,
                        *categories.toTypedArray()
                    )
                }
            }
            dayWiseVisibleEventsListArray[day - 1].addSource(
                liveEventsList,
                dayWiseVisibleEventsListArray[day - 1]::setValue
            )
        }
    }

    fun setupFilters() {
        scope.launch(Dispatchers.IO) {
            runBlocking {
                allCategories = eventRepository.getAllCategories()
            }
            scope.launch { refreshScheduleFilterList() }
        }
    }

    private fun refreshScheduleFilterList() {
        scheduleFiltersList.value = allCategories.mapIndexed { i, category ->
            ScheduleFilter(
                category,
                filterColors[i],
                categoriesToShow.value?.contains(category) ?: false
            )
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
            .apply { add(category); areFiltersActive.value = true }
            .toList()

    private fun removeCategoryFromList(categoryList: List<String>, category: String) =
        categoryList
            .toMutableList()
            .apply { remove(category) }
            .toList()
            .ifEmpty { areFiltersActive.value = false; ALL_CATEGORIES }

    fun clearFilters(): Boolean {
        categoriesToShow.value = ALL_CATEGORIES
        refreshScheduleFilterList()
        areFiltersActive.value = false
        _showStarredOnly.value = false
//        toast("Filters cleared")
        return true
    }

    fun showFiltersSheet() {
        _isSheetVisible.value = true
    }

    fun hideFiltersSheet() {
        _isSheetVisible.value = false
    }

}
