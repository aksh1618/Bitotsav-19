package `in`.bitotsav.profile.ui

import `in`.bitotsav.notification.utils.deleteFcmTokenFromServer
import `in`.bitotsav.profile.CurrentUser
import `in`.bitotsav.profile.data.UserRepository
import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import `in`.bitotsav.profile.utils.syncUserAndRun
import `in`.bitotsav.shared.ui.BaseViewModel
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext.get
import java.io.IOException

class ProfileViewModel : BaseViewModel("ProfileVM") {

    val user = MutableLiveData<CurrentUser>()
    val loading = NonNullMutableLiveData(false)
    val loggedOut = NonNullMutableLiveData(false)

    init {
        user.value = CurrentUser
    }

    fun syncUser() {
        syncUserAndRun {
            user.value = CurrentUser
        }
    }

    fun logout() {
        loading.value = true
        deleteFcmTokenFromServer()
        CurrentUser.clearAllFields()
        // Delete previous FCM token to avoid conflicts
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId()
                } catch (e: IOException) {
                    Log.e(TAG, e.message, e)
                }
            }
        }
        scope.launch {
            withContext(Dispatchers.IO) {
                get().koin.get<UserRepository>().delete()
                loggedOut.postValue(true)
                user.postValue(CurrentUser)
            }
        }
    }
}