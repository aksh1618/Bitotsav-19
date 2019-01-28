package `in`.bitotsav.events.ui

import `in`.bitotsav.events.data.Event
import `in`.bitotsav.events.data.EventRepository
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ScheduleViewModel(private val eventRepository: EventRepository) : ViewModel() {

    companion object {
        val ALL_CATEGORIES = emptyList<String>()
    }

    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    private val _toastMessage = MutableLiveData<String>()
    private val _isSheetVisible = MutableLiveData<Boolean>()
    private lateinit var allCategories: List<String>
    private val categoriesToShow = MutableLiveData<List<String>>()
    private val visibleEventsList = MediatorLiveData<List<Event>>()
    private val scheduleFiltersList = MutableLiveData<List<ScheduleFilter>>()

    val toastMessage: LiveData<String>
        get() = _toastMessage
    val isSheetVisible: LiveData<Boolean>
        get() = _isSheetVisible
    val events: LiveData<List<Event>>
        get() = visibleEventsList
    val filters: LiveData<List<ScheduleFilter>>
        get() = scheduleFiltersList
    lateinit var filterColors: List<Int>

    init {
        _toastMessage.value = ""
        categoriesToShow.value = ALL_CATEGORIES
        _isSheetVisible.value = false
        val liveEventsList = Transformations.switchMap(categoriesToShow) {
            when (it) {
                ALL_CATEGORIES -> eventRepository.getAll()
                // TODO: May need to be sorted by time
                else -> eventRepository.getByCategories(*it.toTypedArray())
            }
        }
        scope.launch(Dispatchers.IO) {
            // TODO: Get from resources?
            allCategories = eventRepository.getAllCategories()
        }.invokeOnCompletion {
            scope.launch(Dispatchers.Main) { refreshScheduleFilterList() }
        }
        visibleEventsList.addSource(liveEventsList, visibleEventsList::setValue)
    }

    private fun refreshScheduleFilterList() {
        scheduleFiltersList.value = allCategories.map {
            ScheduleFilter(it, filterColors[0], categoriesToShow.value?.contains(it) ?: false)
        }
    }

//    fun filterByCategories(vararg categories: String) {
//        this.categoriesToShow.value = categories.asList()
//    }

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
        toast("Filters cleared")
        return true
    }

    fun toggleSheetVisibility() {
        _isSheetVisible.value = _isSheetVisible.value?.not()
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
