package `in`.bitotsav.profile.ui

import `in`.bitotsav.notification.utils.sendFcmTokenToServer
import `in`.bitotsav.profile.api.AuthenticationService
import `in`.bitotsav.profile.data.UserRepository
import `in`.bitotsav.profile.utils.NonNullMutableLiveData
import `in`.bitotsav.profile.utils.loginAsync
import `in`.bitotsav.profile.utils.syncUserAndRun
import `in`.bitotsav.shared.exceptions.AuthException
import `in`.bitotsav.shared.ui.BaseViewModel
import `in`.bitotsav.shared.utils.onFalse
import `in`.bitotsav.shared.utils.onTrue
import `in`.bitotsav.shared.utils.or
import android.util.Log
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authService: AuthenticationService,
    private val userRepository: UserRepository
    ) : BaseViewModel() {

    val user = userRepository.get()

    val loginEmail = NonNullMutableLiveData("")
    val loginPassword = NonNullMutableLiveData("")
    val loginEmailErrorText = NonNullMutableLiveData("")
    val loginPasswordErrorText = NonNullMutableLiveData("")
    val loginErrorText = NonNullMutableLiveData("")
    var loggedIn = false
    // Being observed by data binding
    val loading = NonNullMutableLiveData(false)

    fun login() {
        Log.d("Login pressed", loginEmailErrorText.value)
        loginErrorText.value = ""

        // Invalid email
        if (!loginEmailErrorText.value.isEmpty()) return
        // Empty email or password
        if (checkEmailOrPasswordEmpty()) {
            Log.i("LoginViewModel.login", "Empty email or password")
            return
        }
        Log.i("LoginViewModel.login", "Attempting login...")

        scope.launch {
            loading.value = true
            try {
                loginAsync(authService, loginEmail.value, loginPassword.value).await()
                loggedIn = true
                fetchUserAndLogin()
            } catch (exception: AuthException) {
                loginErrorText.value = exception.message ?: "Authentication Error"
                Log.e("LoginViewModel.login", null, exception)
            } catch (exception: NullPointerException) {
                loginErrorText.value = "Som error occurred, try again"
                Log.e("LoginViewModel.login", null, exception)
            } catch (exception: Exception) {
                loginErrorText.value = exception.message
                    // ?.let { "Unable to reach bitotsav :(" }
                    // Pack Up
                    ?.let { "Too late 🙃" }
                    ?: "Unknown Error!!"
                Log.e("LoginViewModel.login", null, exception)
            } finally {
                loggedIn.onFalse {
                    loading.value = false
                }
            }
        }
    }

    private fun checkEmailOrPasswordEmpty() = Boolean.or(
        loginEmail.value.isEmpty().onTrue {
            loginEmailErrorText.value = "Email required."
        },
        loginPassword.value.isEmpty().onTrue {
            loginPasswordErrorText.value = "Password required."
        }
    )

    private fun fetchUserAndLogin() = syncUserAndRun {
        sendFcmTokenToServer()
        loggedIn = false
    }
}
