package `in`.bitotsav.profile.ui

import `in`.bitotsav.notification.utils.sendFcmTokenToServer
import `in`.bitotsav.profile.utils.AuthException
import `in`.bitotsav.profile.utils.loginAsync
import `in`.bitotsav.profile.utils.syncUserAndRun
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.onTrue
import `in`.bitotsav.shared.utils.or
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel() {

    val loginEmail = MutableLiveData<String>()
    val loginPassword = MutableLiveData<String>()
    val loginEmailErrorText = MutableLiveData<String>()
    val loginPasswordErrorText = MutableLiveData<String>()
    val loginErrorText = MutableLiveData<String>()
    val loggedIn = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    init {
        loginEmail.value = ""
        loginPassword.value = ""
        loginEmailErrorText.value = ""
        loginPasswordErrorText.value = ""
        loginErrorText.value = ""
        loggedIn.value = false
    }

    fun login() {
        Log.d("Login pressed", "${loginEmailErrorText.value}")
        loginErrorText.value = ""

        // Invalid email
        if (!loginEmailErrorText.value.isNullOrEmpty()) return
        // Empty email or password
        if (checkEmailOrPasswordEmpty()) {
            Log.i("LoginViewModel.login", "Empty email or password")
            return
        }
        Log.i("LoginViewModel.login", "Attempting login...")

        scope.launch {
            loading.value = true
            try {
                loginAsync(loginEmail.value!!, loginPassword.value!!).await()
                fetchUserAndLogin()
            } catch (exception: AuthException) {
                loginErrorText.value = exception.message
                Log.e("LoginViewModel.login", null, exception)
            } catch (exception: NullPointerException) {
                loginErrorText.value = "Som error occurred, try again"
                Log.e("LoginViewModel.login", null, exception)
            } catch (exception: Exception) {
                loginErrorText.value = exception.message
                    ?.let { "Unable to reach bitotsav :(" }
                    ?: "Unknown Error!!"
                Log.e("LoginViewModel.login", null, exception)
            } finally {
//                TODO: @aksh Handle this
//                loading.value = false
            }
        }
    }

    private fun checkEmailOrPasswordEmpty() = Boolean.or(
        loginEmail.value.isNullOrEmpty().onTrue {
            loginEmailErrorText.value = "Email required."
        },
        loginPassword.value.isNullOrEmpty().onTrue {
            loginPasswordErrorText.value = "Password required."
        }
    )

    private fun fetchUserAndLogin() = syncUserAndRun {
        sendFcmTokenToServer()
        loading.value = false
        loggedIn.value = true
    }
}
