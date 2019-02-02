package `in`.bitotsav.profile.ui

import `in`.bitotsav.notification.utils.deleteFcmTokenFromServer
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.data.UserRepository
import `in`.bitotsav.profile.utils.syncUserAndRun
import `in`.bitotsav.shared.ui.BaseViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext.get

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
        deleteFcmTokenFromServer()
        CurrentUser.clearAllFields()
        user.value = CurrentUser
//        TODO: @aksh Switch to login view
        // Delete previous FCM token to avoid conflicts
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseInstanceId.getInstance().deleteInstanceId()
            get().koin.get<UserRepository>().delete()
        }
    }
}