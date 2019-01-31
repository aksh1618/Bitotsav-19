package `in`.bitotsav.shared.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class BaseViewModel : ViewModel() {

    var mColor: Int = 0
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    init {
        _toastMessage.value = ""
    }

    fun toast(message: String) {
        _toastMessage.value = message
    }

    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    protected val scope = CoroutineScope(coroutineContext)

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

    class DoubleTriggerLiveData<S, T>(s: LiveData<S>, t: LiveData<T>) :
        MediatorLiveData<Pair<S?, T?>>() {
        init {
            addSource(s) { value = it to t.value }
            addSource(t) { value = s.value to it }
        }
    }
}