package `in`.bitotsav.profile.ui

import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.utils.syncUserAndRun
import `in`.bitotsav.shared.ui.BaseViewModel
import androidx.lifecycle.MutableLiveData

class ProfileViewModel : BaseViewModel() {

    val user = MutableLiveData<CurrentUser>()

    init {
        user.value = CurrentUser
    }

    fun syncUser() {
        syncUserAndRun {
            user.value = CurrentUser
        }
    }

    fun logout() {
        // TODO: @ ashank
    }
}