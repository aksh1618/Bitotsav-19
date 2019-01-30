package `in`.bitotsav.profile.ui

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.utils.syncUserProfile
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ProfileViewModel : ViewModel() {
    var mainColor: Int = 0

    val toastMessage = MutableLiveData<String>()
    val user = MutableLiveData<CurrentUser>()

    init {
        toastMessage.value = ""
        user.value = CurrentUser
    }

    fun syncUser() {
        scope.launch {
            // FIXME: Fix this somehow.
            syncUserProfile()
//                .addListener(
//                { user.postValue(CurrentUser); Log.d("worker thread", "current user is ${CurrentUser.name}") },
//                { it?.run() }
//            )
            // Give harbi's slow ass method some time to complete
            delay(1000)
            user.value = CurrentUser
        }
    }

    private fun toast(message: String) {
        toastMessage.value = message
    }

    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}